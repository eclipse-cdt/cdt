/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import static org.eclipse.cdt.internal.ui.editor.ASTProvider.WAIT_ACTIVE_ONLY;
import static org.eclipse.cdt.internal.ui.editor.ASTProvider.getASTProvider;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.refactoring.CTextFileChange;

import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;
import org.eclipse.cdt.internal.core.dom.rewrite.util.TextUtil;
import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.cdt.internal.corext.codemanipulation.StyledInclude;

import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeCreationContext;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludePreferences;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeUtil;

/**
 * Updates include statements and include guards in response to file or folder move or rename.
 */
public class HeaderFileReferenceAdjuster {
	private static final int PARSE_MODE = ITranslationUnit.AST_SKIP_ALL_HEADERS
			| ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT
			| ITranslationUnit.AST_SKIP_FUNCTION_BODIES
			| ITranslationUnit.AST_PARSE_INACTIVE_CODE;

	private final Map<IFile, IFile> movedFiles;
	private final Map<String, IPath> movedFilesByLocation;
	private IIndex index;
	private int indexLockCount;

	/**
	 * @param movedFiles keys are moved files, values are new, not yet existing, files
	 */
	public HeaderFileReferenceAdjuster(Map<IFile, IFile> movedFiles) {
		this.movedFiles = movedFiles;
		this.movedFilesByLocation = new HashMap<>();
		for (Entry<IFile, IFile> entry : movedFiles.entrySet()) {
			this.movedFilesByLocation.put(entry.getKey().getLocation().toOSString(), entry.getValue().getLocation());
		}
	}

	public Change createChange(CheckConditionsContext context, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(pm, 10);
		CompositeChange change = null;
		Set<IFile> affectedFiles = new HashSet<>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		lockIndex();
		try {
			for (Entry<IFile, IFile> entry : movedFiles.entrySet()) {
				IFile oldFile = entry.getKey();
				IFile newFile = entry.getValue();
				if (areIncludeGuardsAffected(oldFile, newFile))
					affectedFiles.add(oldFile);

				IIndexFileLocation indexFileLocation = IndexLocationFactory.getWorkspaceIFL(oldFile);
				IIndexFile[] indexFiles = index.getFiles(indexFileLocation);
				for (IIndexFile indexFile : indexFiles) {
					IIndexInclude[] includes = index.findIncludedBy(indexFile);
					for (IIndexInclude include : includes) {
						IIndexFileLocation includeLocation = include.getIncludedByLocation();
						String path = includeLocation.getFullPath();
						if (path != null) {
							IResource resource = workspaceRoot.findMember(path);
							if (resource.getType() == IResource.FILE) {
								IFile includer = (IFile) resource;
								affectedFiles.add(includer);
							}
						}
					}
				}
			}

			IWorkingCopyManager workingCopyManager = CUIPlugin.getDefault().getWorkingCopyManager();
			IWorkingCopy[] workingCopies = workingCopyManager.getSharedWorkingCopies();
			progress.worked(1);
			progress = SubMonitor.convert(progress.newChild(9), workingCopies.length + affectedFiles.size());

			List<Change> changes = new ArrayList<>();
			ValidateEditChecker checker= (ValidateEditChecker) context.getChecker(ValidateEditChecker.class);
			for (ITranslationUnit tu : workingCopies) {
				addFileChange(tu, changes, checker, progress.newChild(1));
			}

			CoreModel coreModel = CoreModel.getDefault();
			for (IFile file : affectedFiles) {
				ITranslationUnit tu = (ITranslationUnit) coreModel.create(file);
				if (workingCopyManager.findSharedWorkingCopy(tu) != null)
					continue;
				addFileChange(tu, changes, checker, progress.newChild(1));
			}

			if (!changes.isEmpty()) {
				change = new CompositeChange("", changes.toArray(new Change[changes.size()])); //$NON-NLS-1$
				change.markAsSynthetic();
			}
		} finally {
			unlockIndex();
			pm.done();
		}
		return change;
	}

	private void addFileChange(ITranslationUnit tu, List<Change> changes, ValidateEditChecker checker,
			IProgressMonitor pm) throws CoreException {
		TextEdit edit = createEdit(tu, pm);
		if (edit != null) {
			CTextFileChange fileChange = new CTextFileChange(tu.getElementName(), tu);
			fileChange.setEdit(edit);
			changes.add(fileChange);
			checker.addFile(fileChange.getFile());
		}
	}

	private TextEdit createEdit(ITranslationUnit tu, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		checkCanceled(pm);

		IASTTranslationUnit sharedAst = null;

		SubMonitor progress = SubMonitor.convert(pm, 2);
		try {
			IASTTranslationUnit ast =
					getASTProvider().acquireSharedAST(tu, index, WAIT_ACTIVE_ONLY, progress.newChild(1));
	    	if (ast == null) {
	    		checkCanceled(pm);
				ast= tu.getAST(index, PARSE_MODE);
		    	if (ast == null)
		    		return null;
	    	} else {
	    		sharedAst = ast;
	    	}
	       	return createEdit(ast, tu, progress.newChild(1));
		} finally {
			if (sharedAst != null) {
    			getASTProvider().releaseSharedAST(sharedAst);
			}
			pm.done();
		}
    }

	private TextEdit createEdit(IASTTranslationUnit ast, ITranslationUnit tu, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		IncludeCreationContext context = new IncludeCreationContext(tu, index);
		String contents = context.getSourceContents();

		MultiTextEdit rootEdit = createIncludeGuardEdit(ast, tu, contents);

		Map<IASTPreprocessorIncludeStatement, IPath> affectedIncludes = new IdentityHashMap<>();
		IASTPreprocessorIncludeStatement[] existingIncludes = ast.getIncludeDirectives();
		for (IASTPreprocessorIncludeStatement include : existingIncludes) {
			if (include.isPartOfTranslationUnitFile()) {
				String location;
				if (include.isActive()) {
					location = include.getPath();
					if (location.isEmpty())
						continue;	// Unresolved include.
				} else {
					String name = new String(include.getName().getSimpleID());
					IncludeInfo includeInfo = new IncludeInfo(name, include.isSystemInclude());
					IPath path = context.resolveInclude(includeInfo);
					if (path == null)
						continue;
					location = path.toString();
				}
				IPath newLocation = movedFilesByLocation.get(location);
				if (newLocation != null) {
					affectedIncludes.put(include, newLocation);
				}
			}
		}
		if (affectedIncludes.isEmpty())
			return rootEdit;

		NodeCommentMap commentedNodeMap = ASTCommenter.getCommentedNodeMap(ast);
		IRegion includeRegion =
				IncludeUtil.getSafeIncludeReplacementRegion(contents, ast, commentedNodeMap);

		IncludePreferences preferences = context.getPreferences();

		if (rootEdit == null)
			rootEdit = new MultiTextEdit();

		context.addHeadersIncludedPreviously(existingIncludes);

		if (preferences.allowReordering) {
			List<StyledInclude> modifiedIncludes = new ArrayList<>();
			// Put the changed includes into modifiedIncludes.
			for (Entry<IASTPreprocessorIncludeStatement, IPath> entry : affectedIncludes.entrySet()) {
				IASTPreprocessorIncludeStatement existingInclude = entry.getKey();
				if (IncludeUtil.isContainedInRegion(existingInclude, includeRegion)) {
					IPath header = entry.getValue();
					IncludeGroupStyle style = context.getIncludeStyle(header);
					IncludeInfo includeInfo = context.createIncludeInfo(header, style);
					StyledInclude include = new StyledInclude(header, includeInfo, style, existingInclude);
					modifiedIncludes.add(include);
				}
			}

			Collections.sort(modifiedIncludes, preferences);

			// Populate a list of the existing unchanged includes in the include insertion region.
			List<StyledInclude> mergedIncludes =
					IncludeUtil.getIncludesInRegion(existingIncludes, includeRegion, context);
			Deque<DeleteEdit> deletes = new ArrayDeque<>();
			// Create text deletes for old locations of the includes that will be changed.
			int deleteOffset = -1;
			boolean emptyLineEncountered = false;
			int j = 0;
			for (int i = 0; i < mergedIncludes.size(); i++) {
				StyledInclude include = mergedIncludes.get(i);
				IASTPreprocessorIncludeStatement existingInclude = include.getExistingInclude();
				int offset = ASTNodes.offset(existingInclude);
				boolean previousLineBlank = TextUtil.isPreviousLineBlank(contents, offset);
				if (affectedIncludes.containsKey(existingInclude)) {
					if (deleteOffset < 0) {
						deleteOffset = offset;
					} else if (!emptyLineEncountered && previousLineBlank) {
						// Preserve the first encountered blank line.
						deletes.add(new DeleteEdit(deleteOffset, offset - deleteOffset));
						deleteOffset = -1;
					}
					emptyLineEncountered |= previousLineBlank;
				} else {
					if (deleteOffset >= 0) {
						if (!emptyLineEncountered && previousLineBlank) {
							offset = TextUtil.getPreviousLineStart(contents, offset);
						}
						deletes.add(new DeleteEdit(deleteOffset, offset - deleteOffset));
						deleteOffset = -1;
					}
					emptyLineEncountered = false;
					if (j < i)
						mergedIncludes.set(j, include);
					j++;
				}
			}
			while (j < mergedIncludes.size()) {
				mergedIncludes.remove(mergedIncludes.size() - 1);
			}
			if (deleteOffset >= 0)
				deletes.add(new DeleteEdit(deleteOffset, includeRegion.getOffset() + includeRegion.getLength() - deleteOffset));

			// Since the order of existing include statements may not match the include order
			// preferences, we find positions for the new include statements by pushing them up
			// from the bottom of the include insertion region.
			for (StyledInclude include : modifiedIncludes) {
				if (IncludeUtil.isContainedInRegion(include.getExistingInclude(), includeRegion)) {
					int i = mergedIncludes.size();
					while (--i >= 0 && preferences.compare(include, mergedIncludes.get(i)) < 0) {}
					mergedIncludes.add(i + 1, include);
				}
			}

			int offset = includeRegion.getOffset();
			StringBuilder text = new StringBuilder();
			StyledInclude previousInclude = null;
			for (StyledInclude include : mergedIncludes) {
				IASTPreprocessorIncludeStatement existingInclude = include.getExistingInclude();
				if (affectedIncludes.containsKey(existingInclude)) {
					if (previousInclude != null) {
						IASTNode previousNode = previousInclude.getExistingInclude();
						if (!affectedIncludes.containsKey(previousNode)) {
							offset = ASTNodes.skipToNextLineAfterNode(contents, previousNode);
							flushEditBuffer(offset, text, deletes, rootEdit);
							if (contents.charAt(offset - 1) != '\n')
								text.append(context.getLineDelimiter());
						}
						if (isBlankLineNeededBetween(previousInclude, include, preferences)) {
							if (TextUtil.isLineBlank(contents, offset)) {
								int oldOffset = offset;
								offset = TextUtil.skipToNextLine(contents, offset);
								if (offset == oldOffset || contents.charAt(offset - 1) != '\n')
									text.append(context.getLineDelimiter());
							} else {
								text.append(context.getLineDelimiter());
							}
						}
					}
					text.append(include.getIncludeInfo().composeIncludeStatement());
					List<IASTComment> comments = commentedNodeMap.getTrailingCommentsForNode(existingInclude);
					for (IASTComment comment : comments) {
						text.append(ASTNodes.getPrecedingWhitespaceInLine(contents, comment));
						text.append(comment.getRawSignature());
					}
					text.append(context.getLineDelimiter());
				} else {
					if (previousInclude != null && affectedIncludes.containsKey(previousInclude.getExistingInclude()) &&
							isBlankLineNeededBetween(previousInclude, include, preferences) &&
							TextUtil.findBlankLine(contents, offset, ASTNodes.offset(existingInclude)) < 0) {
						text.append(context.getLineDelimiter());
					}
					flushEditBuffer(offset, text, deletes, rootEdit);
				}
				previousInclude = include;
			}
			if (includeRegion.getLength() == 0 && !TextUtil.isLineBlank(contents, includeRegion.getOffset())) {
				text.append(context.getLineDelimiter());
			}
			offset = includeRegion.getOffset() + includeRegion.getLength();
			flushEditBuffer(offset, text, deletes, rootEdit);
		}

		for (IASTPreprocessorIncludeStatement existingInclude : existingIncludes) {
			IPath header = affectedIncludes.get(existingInclude);
			if (header != null &&
					(!preferences.allowReordering || !IncludeUtil.isContainedInRegion(existingInclude, includeRegion))) {
				IncludeGroupStyle style = context.getIncludeStyle(header);
				IncludeInfo includeInfo = context.createIncludeInfo(header, style);
				IASTName name = existingInclude.getName();
				int offset = ASTNodes.offset(name) - 1;
				int length = ASTNodes.endOffset(name) + 1 - offset;
				rootEdit.addChild(new ReplaceEdit(offset, length, includeInfo.toString()));
			}
		}

		return rootEdit;
	}

	private static boolean isBlankLineNeededBetween(StyledInclude include1, StyledInclude include2,
			IncludePreferences preferences) {
		return include2.getStyle().isBlankLineNeededAfter(include1.getStyle(), preferences.includeStyles);
	}

	private MultiTextEdit createIncludeGuardEdit(IASTTranslationUnit ast, ITranslationUnit tu, String contents) {
		IResource resource = tu.getResource();
		IFile newFile = movedFiles.get(resource);
		if (newFile == null)
			return null;
		boolean guardsAffected = areIncludeGuardsAffected((IFile) resource, newFile);
		if (!guardsAffected)
			return null;
		List<IRegion> includeGuardPositions = new ArrayList<>();
		String oldGuard = IncludeUtil.findIncludeGuard(contents, ast, includeGuardPositions);
		if (oldGuard == null)
			return null;
		if (!oldGuard.equals(StubUtility.generateIncludeGuardSymbol(resource, tu.getCProject())))
			return null;
		IProject newProject = newFile.getProject();
		ICProject newCProject = CoreModel.getDefault().create(newProject);
		if (newCProject == null)
			return null;
		String guard = StubUtility.generateIncludeGuardSymbol(newFile, newCProject);
		if (guard.equals(oldGuard))
			return null;
		MultiTextEdit rootEdit = new MultiTextEdit();
		for (IRegion region : includeGuardPositions) {
			rootEdit.addChild(new ReplaceEdit(region.getOffset(), region.getLength(), guard));
		}
		return rootEdit;
	}

	private void flushEditBuffer(int offset, StringBuilder text, Deque<DeleteEdit> deletes, MultiTextEdit edit) {
		consumeDeletesUpTo(offset, deletes, edit);
		if (text.length() != 0) {
			edit.addChild(new InsertEdit(offset, text.toString()));
			text.delete(0, text.length());
		}
	}

	private void consumeDeletesUpTo(int offset, Deque<DeleteEdit> deletes, MultiTextEdit rootEdit) {
		while (!deletes.isEmpty()) {
			DeleteEdit edit = deletes.peek();
			if (edit.getOffset() > offset)
				break;
			deletes.remove();
			rootEdit.addChild(edit);
		}
	}

	private void lockIndex() throws CoreException, OperationCanceledException {
		if (indexLockCount == 0) {
			if (index == null) {
				ICProject[] projects= CoreModel.getDefault().getCModel().getCProjects();
				index = CCorePlugin.getIndexManager().getIndex(projects,
						IIndexManager.ADD_EXTENSION_FRAGMENTS_EDITOR);
			}
			try {
				index.acquireReadLock();
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
		}
		indexLockCount++;
	}

	private void unlockIndex() {
		if (--indexLockCount <= 0) {
			if (index != null) {
				index.releaseReadLock();
			}
			index = null;
		}
	}

	private static void checkCanceled(IProgressMonitor pm) throws OperationCanceledException {
		if (pm != null && pm.isCanceled())
			throw new OperationCanceledException();
	}

	private static boolean areIncludeGuardsAffected(IFile oldfile, IFile newFile) {
		String filename = oldfile.getLocation().lastSegment();
		if (!CoreModel.isValidHeaderUnitName(null, filename))
			return false;
		IPreferencesService preferences = Platform.getPreferencesService();
		IScopeContext[] scopes = PreferenceConstants.getPreferenceScopes(oldfile.getProject());
		int schema = preferences.getInt(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME,
				PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME, scopes);
		switch (schema) {
		case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_PATH:
			return true;

		case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME:
			return !filename.equals(newFile.getName());

		default:
			return false;
		}
	}
}
