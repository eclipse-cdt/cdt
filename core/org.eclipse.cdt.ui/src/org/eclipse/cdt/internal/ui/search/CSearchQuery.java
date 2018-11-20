/*******************************************************************************
 * Copyright (c) 2006, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Andrey Eremchenko (LEDAS)
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.browser.ASTTypeInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.ui.search.LineSearchElement.Match;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.extensions.IExternalSearchProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class CSearchQuery implements ISearchQuery {
	public static final int FIND_DECLARATIONS = IIndex.FIND_DECLARATIONS;
	public static final int FIND_DEFINITIONS = IIndex.FIND_DEFINITIONS;
	public static final int FIND_REFERENCES = IIndex.FIND_REFERENCES;
	public static final int FIND_DECLARATIONS_DEFINITIONS = FIND_DECLARATIONS | FIND_DEFINITIONS;
	public static final int FIND_ALL_OCCURRENCES = FIND_DECLARATIONS | FIND_DEFINITIONS | FIND_REFERENCES;

	protected static final long LABEL_FLAGS = CElementLabels.M_PARAMETER_TYPES | CElementLabels.ALL_FULLY_QUALIFIED
			| CElementLabels.TEMPLATE_ARGUMENTS;

	protected CSearchResult result;
	protected int flags;

	protected ICElement[] scope;
	protected ICProject[] projects;
	private Set<String> fullPathFilter;

	protected CSearchQuery(ICElement[] scope, int flags) {
		result = new CSearchResult(this);
		this.flags = flags;
		this.scope = scope;

		try {
			if (scope == null) {
				// All CDT projects in workspace
				ICProject[] allProjects = CoreModel.getDefault().getCModel().getCProjects();
				// Filter out closed projects for this case
				for (int i = 0; i < allProjects.length; i++) {
					if (!allProjects[i].getProject().isOpen()) {
						allProjects[i] = null;
					}
				}
				projects = ArrayUtil.removeNulls(ICProject.class, allProjects);
			} else {
				Map<String, ICProject> projectMap = new HashMap<>();
				Set<String> pathFilter = new HashSet<>();
				boolean needFilter = false;
				for (int i = 0; i < scope.length; ++i) {
					ICProject project = scope[i].getCProject();
					if (project != null && project.getProject().isOpen()) {
						IResource res = scope[i].getResource();
						if (res != null) {
							pathFilter.add(res.getFullPath().toString());
							needFilter = needFilter || !(res instanceof IProject);
						}
						projectMap.put(project.getElementName(), project);
					}
				}

				projects = projectMap.values().toArray(new ICProject[projectMap.size()]);
				if (needFilter) {
					fullPathFilter = pathFilter;
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	protected String labelForBinding(final IIndex index, IBinding binding, String defaultLabel) throws CoreException {
		IIndexName[] names = index.findNames(binding, IIndex.FIND_DECLARATIONS_DEFINITIONS);
		if (names.length > 0) {
			ICElementHandle elem = IndexUI.getCElementForName((ICProject) null, index, names[0]);
			if (elem != null) {
				return CElementLabels.getElementLabel(elem, LABEL_FLAGS);
			}
		}
		return defaultLabel;
	}

	@Override
	public String getLabel() {
		String type;
		if ((flags & FIND_REFERENCES) != 0) {
			type = CSearchMessages.PDOMSearchQuery_refs_label;
		} else if ((flags & FIND_DECLARATIONS) != 0) {
			type = CSearchMessages.PDOMSearchQuery_decls_label;
		} else {
			type = CSearchMessages.PDOMSearchQuery_defs_label;
		}
		return type;
	}

	public abstract String getResultLabel(int matchCount);

	public String getResultLabel(String pattern, int matchCount) {
		return getResultLabel(pattern, null, matchCount);
	}

	public String getResultLabel(String pattern, String scope, int matchCount) {
		// Report pattern and number of matches
		String label;
		final int kindFlags = flags & FIND_ALL_OCCURRENCES;
		switch (kindFlags) {
		case FIND_REFERENCES:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_refs_result_label, pattern);
			break;
		case FIND_DECLARATIONS:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_decls_result_label, pattern);
			break;
		case FIND_DEFINITIONS:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_defs_result_label, pattern);
			break;
		case FIND_DECLARATIONS_DEFINITIONS:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_decldefs_result_label, pattern);
			break;
		default:
			label = NLS.bind(CSearchMessages.PDOMSearchQuery_occurrences_result_label, pattern);
			break;
		}

		if (scope != null) {
			label = NLS.bind(CSearchMessages.PDOMSearchPatternQuery_PatternQuery_labelPatternInScope, label, scope);
		}

		String countLabel = Messages.format(CSearchMessages.CSearchResultCollector_matches, matchCount);
		return label + " " + countLabel; //$NON-NLS-1$
	}

	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public ISearchResult getSearchResult() {
		return result;
	}

	/**
	 * Return true to filter name out of the match list.
	 * Override in a subclass to add scoping.
	 * @param name
	 * @return true to filter name out of the match list
	 */
	protected boolean filterName(IIndexName name) {
		return false; // i.e. keep it
	}

	private void createMatchesFromNames(IIndex index, Map<IIndexFile, Set<Match>> fileMatches,
			Collection<IIndexName> names, boolean isPolymorphicOnly) throws CoreException {
		if (names == null)
			return;

		ICProject preferred = getPreferredProject();
		for (IIndexName name : names) {
			if (!filterName(name)) {
				if (!isPolymorphicOnly || name.couldBePolymorphicMethodCall()) {
					IASTFileLocation loc = name.getFileLocation();
					if (loc == null) {
						continue;
					}
					IIndexFile file = name.getFile();
					Set<Match> matches = fileMatches.get(file);
					if (matches == null) {
						matches = new HashSet<>();
						fileMatches.put(file, matches);
					}
					int nodeOffset = loc.getNodeOffset();
					int nodeLength = loc.getNodeLength();
					ICElement enclosingElement = null;
					IIndexName enclosingDefinition = name.getEnclosingDefinition();
					if (enclosingDefinition != null) {
						enclosingElement = IndexUI.getCElementForName(preferred, index, enclosingDefinition);
					} else {
						// Get names from the external search providers.
						for (IExternalSearchProvider provider : CSearchProviderManager.INSTANCE
								.getExternalSearchProviders()) {
							enclosingElement = provider.getEnclosingElement(name);
							if (enclosingElement != null)
								break;
						}
					}

					boolean isWriteAccess = name.isWriteAccess();
					matches.add(new Match(nodeOffset, nodeLength, isPolymorphicOnly, enclosingElement, isWriteAccess));
				}
			}
		}
	}

	private Set<Match> convertMatchesPositions(IIndexFile file, Set<Match> matches) throws CoreException {
		IPath path = IndexLocationFactory.getPath(file.getLocation());
		long timestamp = file.getTimestamp();
		IPositionConverter converter = CCorePlugin.getPositionTrackerManager().findPositionConverter(path, timestamp);
		if (converter != null) {
			Set<Match> convertedMatches = new HashSet<>();
			for (Match match : matches) {
				IRegion region = new Region(match.getOffset(), match.getLength());
				region = converter.historicToActual(region);
				int offset = region.getOffset();
				int length = region.getLength();
				boolean isPolymorphicCall = match.isPolymorphicCall();
				ICElement enclosingElement = match.getEnclosingElement();
				boolean isWriteAccess = match.isWriteAccess();
				convertedMatches.add(new Match(offset, length, isPolymorphicCall, enclosingElement, isWriteAccess));
			}
			matches = convertedMatches;
		}
		return matches;
	}

	private void collectNames(IIndex index, Collection<IIndexName> names, Collection<IIndexName> polymorphicNames)
			throws CoreException {
		// group all matched names by files
		Map<IIndexFile, Set<Match>> fileMatches = new HashMap<>();
		createMatchesFromNames(index, fileMatches, names, false);
		createMatchesFromNames(index, fileMatches, polymorphicNames, true);
		// compute mapping from paths to dirty text editors
		IEditorPart[] dirtyEditors = CUIPlugin.getDirtyEditors();
		Map<IPath, ITextEditor> pathsDirtyEditors = new HashMap<>();
		for (IEditorPart editorPart : dirtyEditors) {
			if (editorPart instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editorPart;
				IEditorInput editorInput = editorPart.getEditorInput();
				if (editorInput instanceof IPathEditorInput) {
					IPathEditorInput pathEditorInput = (IPathEditorInput) editorInput;
					pathsDirtyEditors.put(pathEditorInput.getPath(), textEditor);
				}
			}
		}
		// for each file with matches create line elements with matches
		for (Entry<IIndexFile, Set<Match>> entry : fileMatches.entrySet()) {
			IIndexFile file = entry.getKey();
			Set<Match> matches = entry.getValue();
			LineSearchElement[] lineElements = {};
			// check if there is dirty text editor corresponding to file and convert matches
			IPath absolutePath = IndexLocationFactory.getAbsolutePath(file.getLocation());
			if (pathsDirtyEditors.containsKey(absolutePath)) {
				matches = convertMatchesPositions(file, matches);
				// scan dirty editor and group matches by line elements
				ITextEditor textEditor = pathsDirtyEditors.get(absolutePath);
				IEditorInput input = textEditor.getEditorInput();
				IDocument document = textEditor.getDocumentProvider().getDocument(input);
				Match[] matchesArray = matches.toArray(new Match[matches.size()]);
				lineElements = LineSearchElement.createElements(file.getLocation(), matchesArray, document);
			} else {
				// scan file and group matches by line elements
				Match[] matchesArray = matches.toArray(new Match[matches.size()]);
				lineElements = LineSearchElement.createElements(file.getLocation(), matchesArray);
			}
			// create real PDOMSearchMatch with corresponding line elements
			for (LineSearchElement searchElement : lineElements) {
				for (Match lineMatch : searchElement.getMatches()) {
					int offset = lineMatch.getOffset();
					int length = lineMatch.getLength();
					CSearchMatch match = new CSearchMatch(searchElement, offset, length);
					if (lineMatch.isPolymorphicCall())
						match.setIsPolymorphicCall();
					if (lineMatch.isWriteAccess()) {
						match.setIsWriteAccess();
					}
					result.addMatch(match);
				}
			}
		}
	}

	protected void createMatches(IIndex index, IBinding binding) throws CoreException {
		createMatches(index, new IBinding[] { binding });
	}

	protected void createMatches(IIndex index, IBinding[] bindings) throws CoreException {
		if (bindings == null)
			return;
		List<IIndexName> names = new ArrayList<>();
		List<IIndexName> polymorphicNames = null;
		HashSet<IBinding> handled = new HashSet<>();

		for (IBinding binding : bindings) {
			if (binding != null && handled.add(binding)) {
				createMatches1(index, binding, names);
			}
		}

		if ((flags & FIND_REFERENCES) != 0) {
			for (IBinding binding : bindings) {
				if (binding != null) {
					List<? extends IBinding> specializations = IndexUI.findSpecializations(index, binding);
					for (IBinding spec : specializations) {
						if (spec != null && handled.add(spec)) {
							createMatches1(index, spec, names);
						}
					}

					if (binding instanceof ICPPMethod) {
						ICPPMethod m = (ICPPMethod) binding;
						ICPPMethod[] msInBases = ClassTypeHelper.findOverridden(m);
						if (msInBases.length > 0) {
							if (polymorphicNames == null) {
								polymorphicNames = new ArrayList<>();
							}
							for (ICPPMethod mInBase : msInBases) {
								if (mInBase != null && handled.add(mInBase)) {
									createMatches1(index, mInBase, polymorphicNames);
								}
							}
						}
					}
				}
			}
		}

		// Get names from the external search providers.
		for (IExternalSearchProvider provider : CSearchProviderManager.INSTANCE.getExternalSearchProviders()) {
			for (IBinding binding : bindings) {
				provider.findNames(binding, flags, projects, index, names);
			}
		}

		if (!(names.isEmpty() && (polymorphicNames == null || polymorphicNames.isEmpty()))) {
			collectNames(index, names, polymorphicNames);
		}
	}

	private void createMatches1(IIndex index, IBinding binding, List<IIndexName> names) throws CoreException {
		IIndexName[] bindingNames = index.findNames(binding, flags);
		if (fullPathFilter == null) {
			names.addAll(Arrays.asList(bindingNames));
		} else {
			for (IIndexName name : bindingNames) {
				String fullPath = name.getFile().getLocation().getFullPath();
				if (fullPath != null && accept(fullPath))
					names.add(name);
			}
		}
	}

	private boolean accept(String fullPath) {
		while (true) {
			if (fullPathFilter.contains(fullPath))
				return true;
			int idx = fullPath.lastIndexOf('/');
			if (idx < 0)
				return false;
			fullPath = fullPath.substring(0, idx);
		}
	}

	protected void createLocalMatches(IASTTranslationUnit ast, IBinding binding) throws CoreException {
		if (binding != null) {
			Set<IASTName> names = new HashSet<>();
			names.addAll(Arrays.asList(ast.getDeclarationsInAST(binding)));
			names.addAll(Arrays.asList(ast.getDefinitionsInAST(binding)));
			names.addAll(Arrays.asList(ast.getReferences(binding)));
			// Collect local matches from AST
			IIndexFileLocation fileLocation = null;
			Set<Match> localMatches = new HashSet<>();
			for (IASTName name : names) {
				if (((flags & FIND_DECLARATIONS) != 0 && name.isDeclaration())
						|| ((flags & FIND_DEFINITIONS) != 0 && name.isDefinition())
						|| ((flags & FIND_REFERENCES) != 0 && name.isReference())) {
					ASTTypeInfo typeInfo = ASTTypeInfo.create(name);
					if (typeInfo != null) {
						ITypeReference ref = typeInfo.getResolvedReference();
						if (ref != null) {
							ICElement element = null;
							IASTNode node = name;
							while (node != null && !(node instanceof IASTFunctionDefinition)) {
								node = node.getParent();
							}
							if (node != null) {
								IASTFunctionDefinition definition = (IASTFunctionDefinition) node;
								element = IndexUI.getCElementForName(getPreferredProject(), ast.getIndex(),
										definition.getDeclarator().getName());
							}
							boolean isWrite = CSearchUtil.isWriteOccurrence(name, binding);
							localMatches.add(new Match(ref.getOffset(), ref.getLength(), false, element, isWrite));
							fileLocation = typeInfo.getIFL();
						}
					}
				}
			}
			if (localMatches.isEmpty())
				return;
			// Search for dirty editor
			ITextEditor dirtyTextEditor = null;
			String fullPath = ast.getFilePath();
			for (IEditorPart editorPart : CUIPlugin.getDirtyEditors()) {
				if (editorPart instanceof ITextEditor) {
					ITextEditor textEditor = (ITextEditor) editorPart;
					IEditorInput editorInput = editorPart.getEditorInput();
					if (editorInput instanceof IPathEditorInput) {
						IPathEditorInput pathEditorInput = (IPathEditorInput) editorInput;
						IPath path = pathEditorInput.getPath();
						if (fullPath.equals(path.toOSString())) {
							dirtyTextEditor = textEditor;
							break;
						}
					}
				}
			}
			// Create line search elements
			Match[] matchesArray = localMatches.toArray(new Match[localMatches.size()]);
			LineSearchElement[] lineElements;
			if (dirtyTextEditor != null) {
				IEditorInput input = dirtyTextEditor.getEditorInput();
				IDocument document = dirtyTextEditor.getDocumentProvider().getDocument(input);
				lineElements = LineSearchElement.createElements(fileLocation, matchesArray, document);
			} else {
				lineElements = LineSearchElement.createElements(fileLocation, matchesArray);
			}
			// Create real PDOMSearchMatch with corresponding line elements
			for (LineSearchElement searchElement : lineElements) {
				for (Match lineMatch : searchElement.getMatches()) {
					int offset = lineMatch.getOffset();
					int length = lineMatch.getLength();
					CSearchMatch match = new CSearchMatch(searchElement, offset, length);
					if (lineMatch.isWriteAccess()) {
						match.setIsWriteAccess();
					}
					result.addMatch(match);
				}
			}
		}
	}

	private ICProject getPreferredProject() {
		ICProject preferred = null;
		if (projects != null && projects.length == 1) {
			preferred = projects[0];
		}
		return preferred;
	}

	@Override
	public final IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		CSearchResult result = (CSearchResult) getSearchResult();
		result.removeAll();

		result.setIndexerBusy(!CCorePlugin.getIndexManager().isIndexerIdle());

		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(projects,
					IIndexManager.ADD_EXTENSION_FRAGMENTS_SEARCH);
			try {
				index.acquireReadLock();
			} catch (InterruptedException e) {
				return Status.CANCEL_STATUS;
			}
			try {
				return runWithIndex(index, monitor);
			} finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	abstract protected IStatus runWithIndex(IIndex index, IProgressMonitor monitor);

	/**
	 * Get the projects involved in the search.
	 * @return array, never <code>null</code>
	 */
	public ICProject[] getProjects() {
		return projects;
	}

	public String getScopeDescription() {
		StringBuilder buf = new StringBuilder();
		switch (scope.length) {
		case 0:
			break;
		case 1:
			buf.append(scope[0].getElementName());
			break;
		case 2:
			buf.append(scope[0].getElementName());
			buf.append(", "); //$NON-NLS-1$
			buf.append(scope[1].getElementName());
			break;
		default:
			buf.append(scope[0].getElementName());
			buf.append(", "); //$NON-NLS-1$
			buf.append(scope[1].getElementName());
			buf.append(", ..."); //$NON-NLS-1$
			break;
		}
		return buf.toString();
	}
}
