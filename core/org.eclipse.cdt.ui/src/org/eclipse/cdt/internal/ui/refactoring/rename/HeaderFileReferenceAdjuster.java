/*******************************************************************************
 * Copyright (c) 2014, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.io.File;
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
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;
import org.eclipse.cdt.internal.core.model.SourceRoot;
import org.eclipse.cdt.internal.core.util.TextUtil;
import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.cdt.internal.corext.codemanipulation.StyledInclude;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeCreationContext;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludePreferences;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.refactoring.CTextFileChange;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Updates include statements and include guards in response to file or folder move or rename.
 */
public class HeaderFileReferenceAdjuster {
	private static final int PARSE_MODE = ITranslationUnit.AST_SKIP_ALL_HEADERS
			| ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_FUNCTION_BODIES
			| ITranslationUnit.AST_PARSE_INACTIVE_CODE;

	private final Map<IFile, IFile> movedFiles;
	private final Map<String, IPath> movedFilesByLocation;
	private final Map<IContainer, IContainer> renamedContainers;
	private ASTManager astManager;
	private IIndex index;
	private int indexLockCount;

	/**
	 * @param movedFiles keys are files being moved or renamed, values are new, not yet existing,
	 *     files
	 * @param renamedContainers keys are folders and projects being renamed, values are new,
	 *     not yet existing folders and projects.
	 * @param processor the refactoring processor
	 */
	public HeaderFileReferenceAdjuster(Map<IFile, IFile> movedFiles, Map<IContainer, IContainer> renamedContainers,
			RefactoringProcessor processor) {
		this.movedFiles = movedFiles;
		this.movedFilesByLocation = new HashMap<>();
		for (Entry<IFile, IFile> entry : movedFiles.entrySet()) {
			//Construct map using normalised file paths
			this.movedFilesByLocation.put(entry.getKey().getLocation().toString(), entry.getValue().getLocation());
		}
		this.renamedContainers = renamedContainers;
		this.astManager = getASTManager(processor);
	}

	public Change createChange(CheckConditionsContext context, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(pm, 10);
		CompositeChange change = null;
		Set<IFile> affectedFiles = new HashSet<>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		lockIndex();

		ASTManager sharedASTManager = astManager;
		if (astManager == null)
			astManager = new ASTManager(null);

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
			ValidateEditChecker checker = context.getChecker(ValidateEditChecker.class);
			for (ITranslationUnit tu : workingCopies) {
				addFileChange(tu, changes, checker, progress.newChild(1));
			}

			CoreModel coreModel = CoreModel.getDefault();
			for (IFile file : affectedFiles) {
				ITranslationUnit tu = (ITranslationUnit) coreModel.create(file);
				if (tu != null) {
					if (workingCopyManager.findSharedWorkingCopy(tu) != null)
						continue; // Shared working copies have already been processed.
					addFileChange(tu, changes, checker, progress.newChild(1));
				}
			}

			if (!changes.isEmpty()) {
				change = new CompositeChange("", changes.toArray(new Change[changes.size()])); //$NON-NLS-1$
				change.markAsSynthetic();
			}
		} finally {
			if (astManager != sharedASTManager) {
				astManager.dispose();
				astManager = null;
			}
			unlockIndex();
			pm.done();
		}
		return change;
	}

	private void addFileChange(ITranslationUnit tu, List<Change> changes, ValidateEditChecker checker,
			IProgressMonitor pm) throws CoreException {
		TextEditGroup editGroup = createEdit(tu, pm);
		if (editGroup != null) {
			CTextFileChange fileChange = new CTextFileChange(tu.getElementName(), tu);
			TextEdit[] edits = editGroup.getTextEdits();
			if (edits.length == 1) {
				fileChange.setEdit(edits[0]);
			} else {
				fileChange.setEdit(new MultiTextEdit());
				for (TextEdit edit : edits) {
					fileChange.addEdit(edit);
				}
			}
			fileChange.addTextEditGroup(editGroup);
			changes.add(fileChange);
			checker.addFile(fileChange.getFile());
		}
	}

	private TextEditGroup createEdit(ITranslationUnit tu, IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {
		checkCanceled(monitor);

		SubMonitor progress = SubMonitor.convert(monitor, 3);
		IASTTranslationUnit ast = astManager.getAST(index, tu.getFile(), PARSE_MODE, false);
		progress.setWorkRemaining(1);
		if (ast == null)
			return null;
		return createEdit(ast, tu, progress.newChild(1));
	}

	private TextEditGroup createEdit(IASTTranslationUnit ast, ITranslationUnit tu, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		IncludeCreationContext context = new IncludeCreationContext(tu, index);
		// Adjust the translation unit location in the inclusion context.
		IFile movedFile = movedFiles.get(tu.getFile());
		if (movedFile != null)
			context.setTranslationUnitLocation(movedFile.getLocation());

		String contents = context.getSourceContents();

		MultiTextEdit rootEdit = createIncludeGuardEdit(ast, tu, contents);
		int numIncludeGuardEdits = rootEdit == null ? 0 : rootEdit.getChildrenSize();

		Map<IASTPreprocessorIncludeStatement, IPath> affectedIncludes = new IdentityHashMap<>();
		IASTPreprocessorIncludeStatement[] existingIncludes = ast.getIncludeDirectives();
		for (IASTPreprocessorIncludeStatement include : existingIncludes) {
			if (include.isPartOfTranslationUnitFile()) {
				String location;
				if (include.isActive()) {
					location = include.getPath();
					if (location.isEmpty())
						continue; // Unresolved include.
					if (File.separatorChar == '\\') {
						// Normalize path separators on Windows.
						location = new Path(location).toString();
					}
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
		if (!affectedIncludes.isEmpty()) {
			NodeCommentMap commentedNodeMap = ASTCommenter.getCommentedNodeMap(ast);
			IRegion includeRegion = IncludeUtil.getSafeIncludeReplacementRegion(contents, ast, commentedNodeMap);

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
				List<StyledInclude> mergedIncludes = IncludeUtil.getIncludesInRegion(existingIncludes, includeRegion,
						context);
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
					deletes.add(new DeleteEdit(deleteOffset,
							includeRegion.getOffset() + includeRegion.getLength() - deleteOffset));

				// Since the order of existing include statements may not match the include order
				// preferences, we find positions for the new include statements by pushing them up
				// from the bottom of the include insertion region.
				for (StyledInclude include : modifiedIncludes) {
					if (IncludeUtil.isContainedInRegion(include.getExistingInclude(), includeRegion)) {
						int i = mergedIncludes.size();
						while (--i >= 0 && preferences.compare(include, mergedIncludes.get(i)) < 0) {
						}
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
						if (previousInclude != null
								&& affectedIncludes.containsKey(previousInclude.getExistingInclude())
								&& isBlankLineNeededBetween(previousInclude, include, preferences)
								&& TextUtil.findBlankLine(contents, skipDeletedRegion(offset, deletes),
										ASTNodes.offset(existingInclude)) < 0) {
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
				if (header != null && (!preferences.allowReordering
						|| !IncludeUtil.isContainedInRegion(existingInclude, includeRegion))) {
					IncludeGroupStyle style = context.getIncludeStyle(header);
					IncludeInfo includeInfo = context.createIncludeInfo(header, style);
					IASTName name = existingInclude.getName();
					int offset = ASTNodes.offset(name) - 1;
					int length = ASTNodes.endOffset(name) + 1 - offset;
					rootEdit.addChild(new ReplaceEdit(offset, length, includeInfo.toString()));
				}
			}
		}

		if (rootEdit == null)
			return null;

		int numEdits = rootEdit.getChildrenSize();
		String message = numEdits == numIncludeGuardEdits ? RenameMessages.HeaderReferenceAdjuster_update_include_guards
				: numIncludeGuardEdits == 0 ? RenameMessages.HeaderReferenceAdjuster_update_includes
						: RenameMessages.HeaderReferenceAdjuster_update_include_guards_and_includes;
		TextEditGroup editGroup = new TextEditGroup(message, rootEdit);

		return editGroup;
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
		String guard = generateNewIncludeGuardSymbol(resource, newFile, tu.getCProject());
		if (guard == null || guard.equals(oldGuard))
			return null;
		MultiTextEdit rootEdit = new MultiTextEdit();
		for (IRegion region : includeGuardPositions) {
			rootEdit.addChild(new ReplaceEdit(region.getOffset(), region.getLength(), guard));
		}
		return rootEdit;
	}

	private String generateNewIncludeGuardSymbol(IResource resource, IFile newFile, ICProject cProject) {
		switch (getIncludeGuardScheme(cProject.getProject())) {
		case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_PATH:
			IProject newProject = newFile.getProject();
			if (newProject.exists()) {
				// Move within the same or to a different existing project.
				cProject = CoreModel.getDefault().create(newProject);
				if (cProject == null)
					break;
			}
			ISourceRoot[] roots;
			try {
				roots = cProject.getAllSourceRoots();
			} catch (CModelException e) {
				break;
			}
			IContainer base = null;
			for (ISourceRoot root : roots) {
				root = getModifiedSourceRoot(cProject, root);
				if (root.isOnSourceEntry(newFile)) {
					base = root.getResource();
					break;
				}
			}

			if (base == null)
				break;
			IPath path = PathUtil.makeRelativePath(newFile.getFullPath(), base.getFullPath());
			if (path == null)
				break;
			return StubUtility.generateIncludeGuardSymbolFromFilePath(path.toString());

		case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME:
			return StubUtility.generateIncludeGuardSymbolFromFilePath(newFile.getName());

		default:
			break;
		}
		return null;
	}

	protected ISourceRoot getModifiedSourceRoot(ICProject cProject, ISourceRoot root) {
		IContainer container = root.getResource();
		ICSourceEntry sourceEntry = ((SourceRoot) root).getSourceEntry();
		for (Entry<IContainer, IContainer> entry : renamedContainers.entrySet()) {
			IPath oldFolderPath = entry.getKey().getFullPath();
			IPath newFolderPath = entry.getValue().getFullPath();
			sourceEntry = RenameCSourceFolderChange.renameSourceEntry(sourceEntry, oldFolderPath, newFolderPath);
		}
		IContainer newContainer = getModifiedContainer(container);
		return new SourceRoot(cProject, newContainer, sourceEntry);
	}

	private IContainer getModifiedContainer(IContainer container) {
		IPath relativePath = Path.EMPTY;
		for (IContainer ancestor = container; ancestor.getType() != IResource.ROOT; ancestor = ancestor.getParent()) {
			IContainer newContainer = renamedContainers.get(ancestor);
			if (newContainer != null) {
				if (relativePath.isEmpty()) {
					return newContainer;
				}
				return newContainer.getFolder(relativePath);
			}
			relativePath = new Path(ancestor.getName()).append(relativePath);
		}
		return container;
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

	private int skipDeletedRegion(int offset, Deque<DeleteEdit> deletes) {
		for (DeleteEdit edit : deletes) {
			if (edit.getOffset() > offset)
				break;
			offset = edit.getExclusiveEnd();
		}
		return offset;
	}

	private void lockIndex() throws CoreException, OperationCanceledException {
		if (indexLockCount == 0) {
			if (index == null) {
				ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
				index = CCorePlugin.getIndexManager().getIndex(projects, IIndexManager.ADD_EXTENSION_FRAGMENTS_EDITOR);
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

	private static ASTManager getASTManager(RefactoringProcessor processor) {
		if (processor instanceof CRenameProcessor) {
			return ((CRenameProcessor) processor).getAstManager();
		}
		return null;
	}

	private static void checkCanceled(IProgressMonitor pm) throws OperationCanceledException {
		if (pm != null && pm.isCanceled())
			throw new OperationCanceledException();
	}

	private static boolean areIncludeGuardsAffected(IFile oldfile, IFile newFile) {
		IProject project = oldfile.getProject();
		String filename = oldfile.getLocation().lastSegment();
		if (!CoreModel.isValidHeaderUnitName(project, filename))
			return false;
		switch (getIncludeGuardScheme(project)) {
		case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_PATH:
			return true;

		case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME:
			return !filename.equals(newFile.getName());

		default:
			return false;
		}
	}

	private static int getIncludeGuardScheme(IProject project) {
		IPreferencesService preferences = Platform.getPreferencesService();
		IScopeContext[] scopes = PreferenceConstants.getPreferenceScopes(project);
		int scheme = preferences.getInt(CUIPlugin.PLUGIN_ID, PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME,
				PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME, scopes);
		return scheme;
	}
}
