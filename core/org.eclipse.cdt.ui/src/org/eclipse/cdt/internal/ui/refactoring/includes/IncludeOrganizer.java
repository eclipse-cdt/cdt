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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.icu.text.Collator;

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

import org.eclipse.cdt.internal.core.resources.ResourceLookup;

import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle.IncludeKind;

/**
 * Organizes the include directives and forward declarations of a source or header file.
 */
public class IncludeOrganizer {
	private static final Collator COLLATOR = Collator.getInstance();

	private static final Comparator<IPath> PATH_COMPARATOR = new Comparator<IPath>() {
		@Override
		public int compare(IPath path1, IPath path2) {
			int length1 = path1.segmentCount();
			int length2 = path2.segmentCount();
			for (int i = 0; i < length1 && i < length2; i++) {
				int c = COLLATOR.compare(path1.segment(i), path2.segment(i));
				if (c != 0)
					return c;
			}
			return length1 - length2;
		}
	};

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
		Set<IBinding> bindings =
				removeBindingsDefinedInIncludedHeaders(resolver.getBindingsToDeclare(), reachableHeaders);
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

		Collections.sort(forwardDeclarations);
		Collections.sort(functionForwardDeclarations);

		IncludePreferences preferences = fContext.getPreferences();
		Map<IPath, IncludeGroupStyle> classifiedHeaders =
				new HashMap<IPath, IncludeGroupStyle>(fContext.getHeadersToInclude().size());
		for (IPath file : fContext.getHeadersToInclude()) {
			classifiedHeaders.put(file, getIncludeStyle(file));
		}
		@SuppressWarnings("unchecked")
		List<IPath>[] orderedHeaders = (List<IPath>[]) new List<?>[classifiedHeaders.size()];
		for (Map.Entry<IPath, IncludeGroupStyle> entry : classifiedHeaders.entrySet()) {
			IPath path = entry.getKey();
			IncludeGroupStyle style = entry.getValue();
			IncludeGroupStyle groupingStyle = getGroupingStyle(style);
			int position = groupingStyle.getOrder();
			List<IPath> headers = orderedHeaders[position];
			if (headers == null) {
				headers = new ArrayList<IPath>();
				orderedHeaders[position] = headers;
			}
			headers.add(path);
		}

		List<String> includeDirectives = new ArrayList<String>();
		IncludeGroupStyle previousParentStyle = null;
		for (List<IPath> headers : orderedHeaders) {
			if (headers != null && !headers.isEmpty()) {
				Collections.sort(headers, PATH_COMPARATOR);
				IncludeGroupStyle style = classifiedHeaders.get(headers.get(0));
				IncludeGroupStyle groupingStyle = getGroupingStyle(style);
				IncludeGroupStyle parentStyle = getParentStyle(groupingStyle);
				boolean blankLineBefore = groupingStyle.isBlankLineBefore() ||
						(parentStyle != null && parentStyle != previousParentStyle &&
						parentStyle.isKeepTogether() && parentStyle.isBlankLineBefore());
				previousParentStyle = parentStyle;
				if (!includeDirectives.isEmpty() && blankLineBefore)
					includeDirectives.add(""); // Blank line separator //$NON-NLS-1$
				for (IPath header : headers) {
					style = classifiedHeaders.get(header);
					includeDirectives.add(createIncludeDirective(header, style, "")); //$NON-NLS-1$
				}
			}
		}

		// Create the source code to insert into the editor.
		IBuffer fBuffer = fContext.getTranslationUnit().getBuffer();
		String lineSep = getLineSeparator(fBuffer);

		StringBuilder buf = new StringBuilder();
		for (String include : includeDirectives) {
			buf.append(include);
			buf.append(lineSep);
		}

		if (buf.length() != 0 && !forwardDeclarations.isEmpty())
			buf.append(lineSep);
		for (String declaration : forwardDeclarations) {
			buf.append(declaration);
			buf.append(lineSep);
		}

		if (buf.length() != 0 && !functionForwardDeclarations.isEmpty())
			buf.append(lineSep);
		for (String declaration : functionForwardDeclarations) {
			buf.append(declaration);
			buf.append(lineSep);
		}

		if (buf.length() != 0) {
			buf.append(lineSep);
			fBuffer.replace(0, 0, buf.toString());
		}
	}

	private IncludeGroupStyle getGroupingStyle(IncludeGroupStyle style) {
		if (style.isKeepTogether())
			return style;
		IncludeGroupStyle parent = getParentStyle(style);
		if (parent != null && (parent.isKeepTogether() || parent.getIncludeKind() == IncludeKind.OTHER))
			return parent;
		return fContext.getPreferences().includeStyles.get(IncludeKind.OTHER);
	}

	private IncludeGroupStyle getParentStyle(IncludeGroupStyle style) {
		IncludeKind kind = style.getIncludeKind().parent;
		if (kind == null)
			return null;
		return fContext.getPreferences().includeStyles.get(kind);
	}

	private IncludeGroupStyle getIncludeStyle(IPath headerPath) {
		IncludeKind includeKind;
		IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(headerPath);
		if (includeInfo.isSystem()) {
			if (headerPath.getFileExtension() == null) {
				includeKind = IncludeKind.SYSTEM_WITHOUT_EXTENSION;
			} else {
				includeKind = IncludeKind.SYSTEM_WITH_EXTENSION;
			}
		} else if (isPartnerFile(headerPath)) {
			includeKind = IncludeKind.PARTNER;
		} else {
			IPath dir = fContext.getCurrentDirectory();
			if (dir.isPrefixOf(headerPath)) {
				if (headerPath.segmentCount() == dir.segmentCount() + 1) {
					includeKind = IncludeKind.IN_SAME_FOLDER;
				} else {
					includeKind = IncludeKind.IN_SUBFOLDER;
				}
			} else {
				IFile[] files = ResourceLookup.findFilesForLocation(headerPath);
				if (files.length == 0) {
					includeKind = IncludeKind.EXTERNAL;
				} else {
					IProject project = fContext.getProject();
					includeKind = IncludeKind.IN_OTHER_PROJECT;
					for (IFile file : files) {
						if (file.getProject().equals(project)) {
							includeKind = IncludeKind.IN_SAME_PROJECT;
							break;
						}
					}
				}
			}
		}
		Map<IncludeKind, IncludeGroupStyle> styles = fContext.getPreferences().includeStyles;
		return styles.get(includeKind);
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
			for (String s : fContext.getPreferences().partnerFileSuffixes) {
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

	private String createIncludeDirective(IPath header, IncludeGroupStyle style, String lineComment) {
		StringBuilder buf = new StringBuilder("#include "); //$NON-NLS-1$
		buf.append(style.isAngleBrackets() ? '<' : '"');
		String name = null;
		if (style.isRelativePath()) {
			IPath relativePath = PathUtil.makeRelativePath(header, fContext.getCurrentDirectory());
			if (relativePath != null)
				name = relativePath.toPortableString();
		}
		if (name == null) {
			IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(header);
			name = includeInfo.getName();
		}
		buf.append(name);
		buf.append(style.isAngleBrackets() ? '>' : '"');
		buf.append(lineComment);
		return buf.toString();
	}
}
