/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *	   Mathias Kunter
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.PathUtil;

import org.eclipse.cdt.internal.core.resources.PathCanonicalizationStrategy;

import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludePreferences.IncludeType;

/**
 * Organizes the include directives and forward declarations of a source or header file.
 */
public class IncludeOrganizer {
	// TODO(sprigogin): Move to a preference.
	private static final String[] PARTNER_FILE_SUFFIXES = { "test", "unittest" };  //$NON-NLS-1$//$NON-NLS-2$

	private final ITextEditor fEditor;
	private final InclusionContext fContext;

	/**
	 * Constructor
	 * @param editor The editor on which this organize includes action should operate.
	 * @param index
	 */
	public IncludeOrganizer(ITextEditor editor, ITranslationUnit tu, IIndex index) {
		fEditor = editor;
		fContext = new InclusionContext(tu, index);
	}

	/**
	 * Organizes the includes for a given translation unit.
	 * @param ast The AST translation unit to process.
	 * @throws CoreException
	 */
	public void organizeIncludes(IASTTranslationUnit ast) throws CoreException {
		// Process the given translation unit with the inclusion resolver.
		BindingClassifier resolver = new BindingClassifier(fContext, ast);
		Set<IBinding> bindingsToDefine = resolver.getBindingsToDefine();

		HeaderSubstitutor headerSubstitutor = new HeaderSubstitutor(fContext);
		// Create the list of header files which have to be included by examining the list of
		// bindings which have to be defined.
		IIndexFileSet reachableHeaders = ast.getIndexFileSet();

		List<InclusionRequest> requests = createInclusionRequests(bindingsToDefine, reachableHeaders);
		processInclusionRequests(requests, headerSubstitutor);

		// Stores the forward declarations for composite types and enumerations as text.
		List<String> forwardDeclarations = new ArrayList<String>();

		// Stores the forward declarations for C-style functions as text.
		List<String> functionForwardDeclarations = new ArrayList<String>();

		// Create the forward declarations by examining the list of bindings which have to be
		// declared.
		Set<IBinding> bindings = removeBindingsDefinedInIncludedHeaders(resolver.getBindingsToDeclare(), reachableHeaders);
		for (IBinding binding : bindings) {
			// Create the text of the forward declaration of this binding.
			StringBuilder declarationText = new StringBuilder();

			// Consider the namespace(s) of the binding.
			List<IName> scopeNames = new ArrayList<IName>();
			try {
				IScope scope = binding.getScope();
				while (scope != null && scope.getKind() == EScopeKind.eNamespace) {
					IName scopeName = scope.getScopeName();
					if (scopeName != null) {
						scopeNames.add(scopeName);
					}
					scope = scope.getParent();
				}
			} catch (DOMException e) {
			}
			Collections.reverse(scopeNames);
			for (IName scopeName : scopeNames) {
				declarationText.append("namespace "); //$NON-NLS-1$
				declarationText.append(scopeName.toString());
				declarationText.append(" { "); //$NON-NLS-1$
			}

			// Initialize the list which should be used to store the declaration.
			List<String> forwardDeclarationListToUse = forwardDeclarations;

			// Check the type of the binding and create a corresponding forward declaration text.
			if (binding instanceof ICompositeType) {
				// Forward declare a composite type.
				ICompositeType compositeType = (ICompositeType) binding;

				// Check whether this is a template type.
				ICPPTemplateDefinition templateDefinition = null;
				if (compositeType instanceof ICPPTemplateDefinition) {
					templateDefinition = (ICPPTemplateDefinition) compositeType;
				} else if (compositeType instanceof ICPPTemplateInstance) {
					templateDefinition = ((ICPPTemplateInstance) compositeType).getTemplateDefinition();
				}
				if (templateDefinition != null) {
					// Create the template text.
					declarationText.append("template "); //$NON-NLS-1$
					ICPPTemplateParameter[] templateParameters = templateDefinition.getTemplateParameters();
					for (int i = 0; i < templateParameters.length; i++) {
						ICPPTemplateParameter templateParameter = templateParameters[i];
						if (i == 0) {
							declarationText.append("<"); //$NON-NLS-1$
						}
						declarationText.append("typename "); //$NON-NLS-1$
						declarationText.append(templateParameter.getName());
						if (i != templateParameters.length - 1) {
							declarationText.append(", "); //$NON-NLS-1$
						}
					}
					if (templateParameters.length > 0) {
						declarationText.append("> "); //$NON-NLS-1$
					}
				}

				// Append the corresponding keyword.
				switch (compositeType.getKey()) {
					case ICPPClassType.k_class:
						declarationText.append("class"); //$NON-NLS-1$
						break;
					case ICompositeType.k_struct:
						declarationText.append("struct"); //$NON-NLS-1$
						break;
					case ICompositeType.k_union:
						declarationText.append("union"); //$NON-NLS-1$
						break;
				}

				// Append the name of the composite type.
				declarationText.append(' ');
				declarationText.append(binding.getName());

				// Append the semicolon.
				declarationText.append(';');
			} else if (binding instanceof IEnumeration) {
				// Forward declare an enumeration class (C++11 syntax).
				declarationText.append("enum class "); //$NON-NLS-1$
				declarationText.append(binding.getName());
				declarationText.append(';');
			} else if (binding instanceof IFunction && !(binding instanceof ICPPMethod)) {
				// Forward declare a C-style function.
				IFunction function = (IFunction) binding;

				// Append return type and function name.
				IFunctionType functionType = function.getType();
				// TODO(sprigogin) Improper use of IType.toString();
				declarationText.append(functionType.getReturnType().toString());
				declarationText.append(' ');
				declarationText.append(function.getName());
				declarationText.append('(');

				// Append parameter types and names.
				IType[] parameterTypes = functionType.getParameterTypes();
				IParameter[] parameters = function.getParameters();
				for (int i = 0; i < parameterTypes.length && i < parameters.length; i++) {
					// TODO(sprigogin) Improper use of IType.toString();
					declarationText.append(parameterTypes[i].toString());
					char lastChar = declarationText.charAt(declarationText.length() - 1);
					if (lastChar != '*' && lastChar != '&') {
						// Append a space to separate the type name from the parameter name.
						declarationText.append(' ');
					}
					declarationText.append(parameters[i].getName());
					if (i != parameterTypes.length - 1 && i != parameters.length - 1) {
						declarationText.append(", "); //$NON-NLS-1$
					}
				}

				declarationText.append(");"); //$NON-NLS-1$

				// Add this forward declaration to the separate function forward declaration list.
				forwardDeclarationListToUse = functionForwardDeclarations;
			} else {
				// We don't handle forward declarations for those types of bindings. Ignore it.
				continue;
			}

			// Append the closing curly brackets from the namespaces (if any).
			for (int i = 0; i < scopeNames.size(); i++) {
				declarationText.append(" }"); //$NON-NLS-1$
			}

			// Add the forward declaration to the corresponding list.
			forwardDeclarationListToUse.add(declarationText.toString());
		}

		// Obtain the final lists of library, project, and relative headers.
		List<String> relativeIncludeDirectives = new ArrayList<String>();
		List<String> projectIncludeDirectives = new ArrayList<String>();
		List<String> libraryIncludeDirectives = new ArrayList<String>();
		List<String> allIncludeDirectives = new ArrayList<String>();
		IncludePreferences preferences = fContext.getPreferences();
		for (IPath file : fContext.getHeadersToInclude()) {
			if (preferences.allowReordering && preferences.sortByHeaderLocation) {
				// Add the created include directives to different lists.
				createIncludeDirective(file, relativeIncludeDirectives, projectIncludeDirectives, libraryIncludeDirectives);
			} else {
				// Add all created include directives to the same list, making sure that no sort
				// order is applied.
				createIncludeDirective(file, allIncludeDirectives, allIncludeDirectives, allIncludeDirectives);
			}
		}

		// Create the source code to insert into the editor.
		IBuffer fBuffer = fContext.getTranslationUnit().getBuffer();
		String lineSep = getLineSeparator(fBuffer);
		String insertText = new String();

		if (preferences.allowReordering) {
			if (preferences.removeUnusedIncludes) {
				// Remove *all* existing includes and forward declarations. Those which are required
				// will be added again right afterwards.

				// TODO implement this
			}

			if (preferences.sortByHeaderLocation) {
				// Sort by header file location.

				// Process the different types of include directives separately.
				for (IncludeType includeType : preferences.groupOrder) {
					List<String> stringList = null;

					if (includeType == IncludeType.RELATIVE_HEADER) {
						stringList = relativeIncludeDirectives;
					} else if (includeType == IncludeType.PROJECT_HEADER) {
						stringList = projectIncludeDirectives;
					} else if (includeType == IncludeType.LIBRARY_HEADER) {
						stringList = libraryIncludeDirectives;
					} else if (includeType == IncludeType.FORWARD_DECLARATION) {
						stringList = forwardDeclarations;
					} else if (includeType == IncludeType.FUNCTION_FORWARD_DECLARATION) {
						stringList = functionForwardDeclarations;
					}
					if (stringList == null || stringList.isEmpty()) {
						continue;
					}

					// Sort alphabetically
					if (preferences.sortAlphabetically) {
						Collections.sort(stringList);
					}

					// Insert the actual text.
					for (String str : stringList) {
						insertText += str + lineSep;
					}

					// Insert blank line
					if (preferences.separateIncludeBlocks) {
						insertText += lineSep;
					}
				}
			} else {
				// Don't sort by header file location.

				// Sort alphabetically
				if (preferences.sortAlphabetically) {
					Collections.sort(allIncludeDirectives);
				}

				// Insert the actual text.
				for (String str : allIncludeDirectives) {
					insertText += str + lineSep;
				}
			}
		} else {
			// Existing include directives must not be reordered.

			// Compare the list of existing include directives with the list of required include directives.
			// TODO: Implement this. The following code template may be used for that:
			/*for (IInclude includeDirective : fTu.getIncludes()) {
				if (!allIncludeDirectives.contains(includeDirective)) {
					// This include directive from the editor isn't present within the list of required includes and is therefore unused.
					// Remove it from the editor, if enabled within the preferences.
					if (OrganizeIncludesPreferences.getPreferenceStore().getBoolean(PREF_REMOVE_UNUSED_INCLUDES)) {
						removeIncludeDirective(fTu, includeDirective);
					}
				} else {
					// This include directive from the editor is required. Remove it from the list of required includes.
					allIncludeDirectives.remove(includeDirective);
				}
			}*/

			// Insert those includes which still remain within the list of required includes (i.e. those include directives which have
			// been added now).
			for (String str : allIncludeDirectives) {
				insertText += str + lineSep;
			}

			// Insert forward declarations.
			for (String str : forwardDeclarations) {
				insertText += str + lineSep;
			}
		}

		if (!insertText.isEmpty()) {
			// Insert the text plus a separating blank line into the editor.
			insertText = insertText.trim() + lineSep + lineSep;
			fBuffer.replace(0, 0, insertText);
		}
	}

	private Set<IBinding> removeBindingsDefinedInIncludedHeaders(Set<IBinding> bindings,
			IIndexFileSet reachableHeaders) throws CoreException {
		Set<IBinding> filteredBindings = new HashSet<IBinding>(bindings);

		List<InclusionRequest> requests = createInclusionRequests(bindings, reachableHeaders);
		Set<IPath> allIncludedHeaders = new HashSet<IPath>();
		allIncludedHeaders.addAll(fContext.getHeadersAlreadyIncluded());
		allIncludedHeaders.addAll(fContext.getHeadersToInclude());

		for (InclusionRequest request : requests) {
			if (isSatisfiedByIncludedHeaders(request, allIncludedHeaders))
				filteredBindings.remove(request.getBinding());
		}
		return filteredBindings;
	}

	protected boolean isSatisfiedByIncludedHeaders(InclusionRequest request, Set<IPath> includedHeaders)
			throws CoreException {
		for (IIndexFile file : request.getDeclaringFiles().keySet()) {
			IIndexInclude[] includedBy = fContext.getIndex().findIncludedBy(file, IIndex.DEPTH_INFINITE);
			for (IIndexInclude include : includedBy) {
				IPath path = getPath(include.getIncludedByLocation());
				if (includedHeaders.contains(path)) {
					return true;
				}
			}
		}
		return false;
	}

	private void processInclusionRequests(List<InclusionRequest> requests,
			HeaderSubstitutor headerSubstitutor) {
		// Add partner header if necessary.
		HashSet<IIndexFile> partnerIndexFiles = new HashSet<IIndexFile>();
		for (InclusionRequest request : requests) {
			List<IPath> candidatePaths = request.getCandidatePaths();
			if (candidatePaths.size() == 1) {
				IPath path = candidatePaths.iterator().next();
				if (isPartnerFile(path)) {
					request.resolve(path);
					fContext.addHeaderToInclude(path);
					try {
						IIndexFile indexFile = request.getDeclaringFiles().keySet().iterator().next();
						if (!partnerIndexFiles.contains(indexFile)) {
							for (IIndexInclude include : indexFile.getIncludes()) {
								fContext.addHeaderAlreadyIncluded(getPath(include.getIncludesLocation()));
							}
							partnerIndexFiles.add(indexFile);
						}
					} catch (CoreException e) {
						CUIPlugin.log(e);
					}
				}
			}
		}

		// Process headers that are either indirectly included or have unique representatives.
		for (InclusionRequest request : requests) {
			if (!request.isResolved()) {
				List<IPath> candidatePaths = request.getCandidatePaths();
				Set<IPath> representativeHeaders = new HashSet<IPath>();
				boolean allRepresented = true;
				for (IPath path : candidatePaths) {
					if (fContext.isIncluded(path)) {
						request.resolve(path);
						break;
					} else {
						IPath header = headerSubstitutor.getUniqueRepresentativeHeader(path);
						if (header != null) {
							representativeHeaders.add(header);
						} else {
							allRepresented = false;
						}
					}
				}

				if (!request.isResolved() && allRepresented && representativeHeaders.size() == 1) {
					IPath path = representativeHeaders.iterator().next();
					request.resolve(path);
					if (!fContext.isAlreadyIncluded(path))
						fContext.addHeaderToInclude(path);
				}
			}
		}

		// Process remaining unambiguous inclusion requests.
		for (InclusionRequest request : requests) {
			if (!request.isResolved()) {
				List<IPath> candidatePaths = request.getCandidatePaths();
				if (candidatePaths.size() == 1) {
					IPath path = candidatePaths.iterator().next();
					if (fContext.isIncluded(path)) {
						request.resolve(path);
					} else {
						IPath header = headerSubstitutor.getPreferredRepresentativeHeader(path);
						if (header.equals(path) && fContext.getPreferences().heuristicHeaderSubstitution) {
							header = headerSubstitutor.getPreferredRepresentativeHeaderByHeuristic(request);
						}
						request.resolve(header);
						if (!fContext.isAlreadyIncluded(header))
							fContext.addHeaderToInclude(header);
					}
				}
			}
		}

		// Resolve ambiguous inclusion requests.

		// Maps a set of header files presented to the user to the file selected by the user.
		HashMap<Collection<IPath>, IPath> userChoiceCache = new HashMap<Collection<IPath>, IPath>();

		for (InclusionRequest request : requests) {
			if (!request.isResolved()) {
				List<IPath> candidatePaths = request.getCandidatePaths();
				for (IPath path : candidatePaths) {
					if (fContext.isIncluded(path)) {
						request.resolve(path);
						break;
					}
				}
				IPath header = askUserToSelectHeader(request.getBinding(), candidatePaths, userChoiceCache);
				request.resolve(header);
				if (!fContext.isAlreadyIncluded(header))
					fContext.addHeaderToInclude(header);
			}
		}
	}

	private IPath getPath(IIndexFileLocation location) {
		return IndexLocationFactory.getAbsolutePath(location);
	}

	/**
	 * Checks if the given path points to a partner header of the current translation unit.
	 * A header is considered a partner if its name without extension is the same as the name of
	 * the translation unit, or the name of the translation unit differs by one of the suffixes
	 * used for test files.
	 */
	private boolean isPartnerFile(IPath path) {
		String headerName = path.removeFileExtension().lastSegment();
		String sourceName = fContext.getTranslationUnit().getLocation().removeFileExtension().lastSegment();
		if (headerName.equals(sourceName))
			return true;
		if (sourceName.startsWith(headerName)) {
			int pos = headerName.length();
			while (pos < sourceName.length() && !Character.isLetterOrDigit(sourceName.charAt(pos))) {
				pos++;
			}
			if (pos == sourceName.length())
				return true;
			String suffix = sourceName.substring(pos);
			for (String s : PARTNER_FILE_SUFFIXES) {
				if (suffix.equalsIgnoreCase(s))
					return true;
			}
		}
		return false;
	}

	private List<InclusionRequest> createInclusionRequests(Set<IBinding> bindingsToDefine,
			IIndexFileSet reachableHeaders) throws CoreException {
		List<InclusionRequest> requests = new ArrayList<InclusionRequest>(bindingsToDefine.size());
		IIndex index = fContext.getIndex();

		binding_loop: for (IBinding binding : bindingsToDefine) {
			IIndexName[] indexNames;
			if (binding instanceof IFunction) {
				// For functions we need to include the declaration.
				indexNames = index.findDeclarations(binding);
			} else {
				// For all other bindings we need to include the definition.
				indexNames = index.findDefinitions(binding);
			}

			if (indexNames.length != 0) {
				// Check whether the index name is (also) present within the current file.
				// If yes, we don't need to include anything.
				for (IIndexName indexName : indexNames) {
					IIndexFile indexFile = indexName.getFile();
					if (indexFile.getLocation().getURI().equals(fContext.getTranslationUnit().getLocationURI())) {
						continue binding_loop;
					}
				}

				Map<IIndexFile, IPath> declaringHeaders = new HashMap<IIndexFile, IPath>();
				Map<IIndexFile, IPath> reachableDeclaringHeaders = new HashMap<IIndexFile, IPath>();
				for (IIndexName indexName : indexNames) {
					IIndexFile indexFile = indexName.getFile();
					if (IncludeUtil.isSource(indexFile, fContext.getProject()) &&
							index.findIncludedBy(indexFile, 0).length == 0) {
						// The target is a source file which isn't included by any other files.
						// Don't include it.
						continue;
					}
					IPath path = getPath(indexFile.getLocation());
					declaringHeaders.put(indexFile, path);
					if (reachableHeaders.contains(indexFile))
						reachableDeclaringHeaders.put(indexFile, path);
				}

				if (!declaringHeaders.isEmpty()) {
					boolean reachable = false;
					if (!reachableDeclaringHeaders.isEmpty()) {
						reachable = true;
						declaringHeaders = reachableDeclaringHeaders;
					}
					requests.add(new InclusionRequest(binding, declaringHeaders, reachable));
				}
			}
		}
		return requests;
	}

	/**
	 * Returns the line separator for the given buffer.
	 * @param fBuffer
	 * @return
	 */
	private String getLineSeparator(IBuffer fBuffer) {
		try {
			if (fBuffer instanceof IAdaptable) {
				IDocument doc= (IDocument) ((IAdaptable) fBuffer).getAdapter(IDocument.class);
				if (doc != null) {
					String delim= doc.getLineDelimiter(0);
					if (delim != null) {
						return delim;
					}
				}
			}
		} catch (BadLocationException e) {
		}
		return System.getProperty("line.separator", "\n");  //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Picks a suitable header file that should be used to include the given binding.
	 *
	 * @param binding The binding which should be resolved.
	 * @param headers The available header files to pick from.
	 * @param userChoiceCache the cache of previous user choices
	 * @return The chosen header file.
	 */
	private IPath askUserToSelectHeader(IBinding binding, Collection<IPath> headers,
			HashMap<Collection<IPath>, IPath> userChoiceCache) {
		if (headers.isEmpty())
			return null;
		if (headers.size() == 1)
			return headers.iterator().next();

		// Check the decision cache. If the cache doesn't help, ask the user.
		// Query the cache.
		if (userChoiceCache.containsKey(headers)) {
			return userChoiceCache.get(headers);
		}

		// Ask the user.
		final IPath[] elemArray = headers.toArray(new IPath[headers.size()]);
		final IPath[] selectedElement = new IPath[1];
		final String bindingName = binding.getName();
		runInUIThread(new Runnable() {
			@Override
			public void run() {
				ElementListSelectionDialog dialog =
						new ElementListSelectionDialog(fEditor.getSite().getShell(), new LabelProvider());
				dialog.setElements(elemArray);
				dialog.setTitle(CEditorMessages.OrganizeIncludes_label);
				dialog.setMessage(NLS.bind(Messages.IncludeOrganizer_ChooseHeader, bindingName));
				if (dialog.open() == Window.OK) {
					selectedElement[0] = (IPath) dialog.getFirstResult();
				}
			}
		});

		IPath selectedHeader = selectedElement[0];

		if (selectedHeader == null)
			throw new OperationCanceledException();

		userChoiceCache.put(headers, selectedHeader); // Remember user's choice.
		return selectedHeader;
	}

	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().syncExec(runnable);
		}
	}

	/**
	 * Adds an include directive.
	 * @param headerFile The header file which should be included.
	 * @param relativeIncludeDirectives Out parameter. The list of relative headers to include.
	 * @param projectIncludeDirectives Out parameter. The list of project headers to include.
	 * @param libraryIncludeDirectives Out parameter. The list of library headers to include.
	 * @throws CoreException
	 */
	private void createIncludeDirective(IPath headerFile,
			Collection<String> relativeIncludeDirectives,
			Collection<String> projectIncludeDirectives,
			Collection<String> libraryIncludeDirectives) throws CoreException {
		IPath targetLocation = headerFile;
		IPath targetDirectory = targetLocation.removeLastSegments(1);
		targetDirectory = new Path(PathCanonicalizationStrategy.getCanonicalPath(targetDirectory.toFile()));
		IPath sourceDirectory = fContext.getCurrentDirectory();
		sourceDirectory = new Path(PathCanonicalizationStrategy.getCanonicalPath(sourceDirectory.toFile()));

		IncludePreferences preferences = fContext.getPreferences();
		boolean relativeToSource = false;
		if (preferences.relativeHeaderInSameDir &&
				PathUtil.equalPath(sourceDirectory, targetDirectory)) {
			// The header is located within the same directory as the source file.
			relativeToSource = true;
		} else if (preferences.relativeHeaderInSubdir &&
				PathUtil.isPrefix(sourceDirectory, targetLocation)) {
			// The header is located within a subdirectory of the source file's directory.
			relativeToSource = true;
		} else if (preferences.relativeHeaderInParentDir &&
				PathUtil.isPrefix(targetDirectory, sourceDirectory)) {
			// The header is located within a parent directory of the source file's directory.
			relativeToSource = true;
		}

		IncludeInfo includeInfo = null;
		if (!relativeToSource)
			includeInfo = fContext.getIncludeForHeaderFile(targetLocation);
		Collection<String> headerList;
		if (includeInfo != null) {
			headerList = includeInfo.isSystem() ? libraryIncludeDirectives : projectIncludeDirectives;
		} else {
			// Include the header relative to the source file.
			includeInfo = new IncludeInfo(PathUtil.makeRelativePath(targetLocation, sourceDirectory).toPortableString());
			// Add this header to the relative headers.
			headerList = relativeIncludeDirectives;
		}

		headerList.add("#include " + includeInfo.toString()); //$NON-NLS-1$
	}
}
