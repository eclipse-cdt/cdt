/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import static org.eclipse.cdt.core.index.IndexLocationFactory.getAbsolutePath;
import static org.eclipse.cdt.internal.ui.refactoring.includes.IncludeUtil.isContainedInRegion;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

import com.ibm.icu.text.Collator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.internal.corext.codemanipulation.StyledInclude;

import org.eclipse.cdt.internal.ui.CHelpProviderManager;

/**
 * Adds an include statement and, optionally, a 'using' declaration for the currently
 * selected name.
 */
public class IncludeCreator {
	private static final Collator COLLATOR = Collator.getInstance();

	private final String fLineDelimiter;
	private final IElementSelector fAmbiguityResolver;
	private final IncludeCreationContext fContext;

	public IncludeCreator(ITranslationUnit tu, IIndex index, String lineDelimiter,
			IElementSelector ambiguityResolver) {
		fLineDelimiter = lineDelimiter;
		fAmbiguityResolver = ambiguityResolver;
		fContext = new IncludeCreationContext(tu, index);
	}

	public MultiTextEdit createInclude(IASTTranslationUnit ast, ITextSelection selection)
			throws CoreException {
		MultiTextEdit rootEdit = new MultiTextEdit();
		ITranslationUnit tu = fContext.getTranslationUnit();
		IASTNodeSelector selector = ast.getNodeSelector(tu.getLocation().toOSString());
		IASTName name = selector.findEnclosingName(selection.getOffset(), selection.getLength());
		if (name == null) {
			return rootEdit;
		}
		char[] nameChars = name.toCharArray();
		String lookupName = new String(nameChars);
		IBinding binding = name.resolveBinding();
		if (binding instanceof ICPPVariable) {
			IType type = ((ICPPVariable) binding).getType();
			type = SemanticUtil.getNestedType(type,
					SemanticUtil.ALLCVQ | SemanticUtil.PTR | SemanticUtil.ARRAY | SemanticUtil.REF);
			if (type instanceof IBinding) {
				binding = (IBinding) type;
				nameChars = binding.getNameCharArray();
			}
		}
		if (nameChars.length == 0) {
			return rootEdit;
		}

		final Map<String, IncludeCandidate> candidatesMap= new HashMap<String, IncludeCandidate>();
		final IndexFilter filter = IndexFilter.getDeclaredBindingFilter(ast.getLinkage().getLinkageID(), false);
		
		final List<IncludeInfo> requiredIncludes = new ArrayList<IncludeInfo>();
		final List<UsingDeclaration> usingDeclarations = new ArrayList<UsingDeclaration>();

		List<IIndexBinding> bindings = new ArrayList<IIndexBinding>();
		try {
			IIndex index = fContext.getIndex();
			IIndexBinding adaptedBinding= index.adaptBinding(binding);
			if (adaptedBinding == null) {
				bindings.addAll(Arrays.asList(index.findBindings(nameChars, false, filter, new NullProgressMonitor())));
			} else {
				bindings.add(adaptedBinding);
				while (adaptedBinding instanceof ICPPSpecialization) {
					adaptedBinding= index.adaptBinding(((ICPPSpecialization) adaptedBinding).getSpecializedBinding());
					if (adaptedBinding != null) {
						bindings.add(adaptedBinding);
					}
				}
			}
	
			for (IIndexBinding indexBinding : bindings) {
				// Replace ctor with the class itself.
				if (indexBinding instanceof ICPPConstructor) {
					indexBinding = indexBinding.getOwner();
				}
				IIndexName[] definitions= null;
				// class, struct, union, enum-type, enum-item
				if (indexBinding instanceof ICompositeType || indexBinding instanceof IEnumeration || indexBinding instanceof IEnumerator) {
					definitions= index.findDefinitions(indexBinding);
				} else if (indexBinding instanceof ITypedef || (indexBinding instanceof IFunction)) {
					definitions = index.findDeclarations(indexBinding);
				}
				if (definitions != null) {
					for (IIndexName definition : definitions) {
						considerForInclusion(definition, indexBinding, index, candidatesMap);
					}
					if (definitions.length > 0 && adaptedBinding != null) 
						break;
				}
			}
			IIndexMacro[] macros = index.findMacros(nameChars, filter, new NullProgressMonitor());
			for (IIndexMacro macro : macros) {
				IIndexName definition = macro.getDefinition();
				considerForInclusion(definition, macro, index, candidatesMap);
			}
	
			final ArrayList<IncludeCandidate> candidates = new ArrayList<IncludeCandidate>(candidatesMap.values());
			if (candidates.size() > 1) {
				IncludeCandidate candidate = fAmbiguityResolver.selectElement(candidates);
				if (candidate == null)
					return rootEdit;
				candidates.clear();
				candidates.add(candidate);
			}
	
			if (candidates.size() == 1) {
				IncludeCandidate candidate = candidates.get(0);
				requiredIncludes.add(candidate.include);
				IIndexBinding indexBinding = candidate.binding;
	
				if (indexBinding instanceof ICPPBinding && !(indexBinding instanceof IIndexMacro)) {
					// Decide what 'using' declaration, if any, should be added along with the include.
					UsingDeclaration usingDeclaration = deduceUsingDeclaration(binding, indexBinding, ast);
					if (usingDeclaration != null)
						usingDeclarations.add(usingDeclaration);
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return rootEdit;
		}

		if (requiredIncludes.isEmpty() && !lookupName.isEmpty()) {
			// Try contribution from plug-ins.
			IFunctionSummary fs = findContribution(lookupName);
			if (fs != null) {
				IRequiredInclude[] functionIncludes = fs.getIncludes();
				if (functionIncludes != null) {
					for (IRequiredInclude include : functionIncludes) {
						requiredIncludes.add(new IncludeInfo(include.getIncludeName(), include.isStandard()));
					}
				}
				String ns = fs.getNamespace();
				if (ns != null && !ns.isEmpty()) {
					usingDeclarations.add(new UsingDeclaration(ns + "::" + fs.getName())); //$NON-NLS-1$
				}
			}
		}

		return createEdit(requiredIncludes, usingDeclarations, ast, selection);
	}

	private MultiTextEdit createEdit(List<IncludeInfo> includes,
			List<UsingDeclaration> usingDeclarations, IASTTranslationUnit ast,
			ITextSelection selection) {
		NodeCommentMap commentedNodeMap = ASTCommenter.getCommentedNodeMap(ast);
		String contents = fContext.getSourceContents();
		IRegion includeRegion =
				IncludeOrganizer.getSafeIncludeReplacementRegion(contents, ast, commentedNodeMap);
		
		IncludePreferences preferences = fContext.getPreferences();

		MultiTextEdit rootEdit = new MultiTextEdit();

		IASTPreprocessorIncludeStatement[] existingIncludes = ast.getIncludeDirectives();
		fContext.addHeadersIncludedPreviously(existingIncludes);

		List<StyledInclude> styledIncludes = new ArrayList<StyledInclude>();
		// Put the new includes into styledIncludes.
		for (IncludeInfo includeInfo : includes) {
			IPath header = fContext.resolveInclude(includeInfo);
			if (!fContext.wasIncludedPreviously(header)) {
				IncludeGroupStyle style = fContext.getIncludeStyle(includeInfo);
				StyledInclude prototype = new StyledInclude(header, includeInfo, style);
				styledIncludes.add(prototype);
			}
		}
		Collections.sort(styledIncludes, preferences);

		// Populate list of existing includes in the include insertion region.
		List<StyledInclude> mergedIncludes = new ArrayList<StyledInclude>();
		for (IASTPreprocessorIncludeStatement include : existingIncludes) {
			if (include.isPartOfTranslationUnitFile() && isContainedInRegion(include, includeRegion)) {
				String name = new String(include.getName().getSimpleID());
				IncludeInfo includeInfo = new IncludeInfo(name, include.isSystemInclude());
				String path = include.getPath();
				// An empty path means that the include was not resolved.
				IPath header = path.isEmpty() ? null : Path.fromOSString(path);
				IncludeGroupStyle style =
						header != null ? fContext.getIncludeStyle(header) : fContext.getIncludeStyle(includeInfo);
				StyledInclude prototype = new StyledInclude(header, includeInfo, style, include);
				mergedIncludes.add(prototype);
			}
		}

		if (preferences.allowReordering) {
			// Since the order of existing include statements may not match the include order
			// preferences, we find positions for the new include statements by pushing them from
			// them up from the bottom of the include insertion region.  
			for (StyledInclude include : styledIncludes) {
				int i = mergedIncludes.size();
				while (--i >= 0 && preferences.compare(include, mergedIncludes.get(i)) < 0) {}
				mergedIncludes.add(i + 1, include);
			}
		} else {
			mergedIncludes.addAll(styledIncludes);
		}

		int offset = includeRegion.getOffset();
		StringBuilder text = new StringBuilder();
		StyledInclude previousInclude = null;
		for (StyledInclude include : mergedIncludes) {
			if (include.getExistingInclude() == null) {
				if (previousInclude != null) {
					IASTNode previousNode = previousInclude.getExistingInclude();
					if (previousNode != null) {
						offset = ASTNodes.skipToNextLineAfterNode(contents, previousNode);
						flushEditBuffer(offset, text, rootEdit);
						if (contents.charAt(offset - 1) != '\n')
							text.append(fLineDelimiter);
					}
					if (include.getStyle().isBlankLineNeededAfter(previousInclude.getStyle(), preferences.includeStyles))
						text.append(fLineDelimiter);
				}
				text.append(include.getIncludeInfo().composeIncludeStatement());
				text.append(fLineDelimiter);
			} else {
				if (previousInclude != null && previousInclude.getExistingInclude() == null &&
						include.getStyle().isBlankLineNeededAfter(previousInclude.getStyle(), preferences.includeStyles)) {
					text.append(fLineDelimiter);
				}
				flushEditBuffer(offset, text, rootEdit);
			}
			previousInclude = include;
		}
		flushEditBuffer(offset, text, rootEdit);

		List<UsingDeclaration> mergedUsingDeclarations = getUsingDeclarations(ast);
		for (UsingDeclaration usingDeclaration : mergedUsingDeclarations) {
			for (Iterator<UsingDeclaration> iter = usingDeclarations.iterator(); iter.hasNext();) {
				UsingDeclaration using = iter.next();
				if (using.equals(usingDeclaration.name))
					iter.remove();
			}
		}

		if (usingDeclarations.isEmpty())
			return rootEdit;

		List<UsingDeclaration> temp = null;
		for (Iterator<UsingDeclaration> iter = mergedUsingDeclarations.iterator(); iter.hasNext();) {
			UsingDeclaration usingDeclaration = iter.next();
			if (usingDeclaration.existingDeclaration.isPartOfTranslationUnitFile() &&
					ASTNodes.endOffset(usingDeclaration.existingDeclaration) <= selection.getOffset()) {
				if (temp == null)
					temp = new ArrayList<UsingDeclaration>();
				temp.add(usingDeclaration);
			}
		}
		if (temp == null) {
			mergedUsingDeclarations.clear();
		} else {
			mergedUsingDeclarations = temp;
		}

		Collections.sort(usingDeclarations);

		if (mergedUsingDeclarations.isEmpty()) {
			offset = includeRegion.getOffset() + includeRegion.getLength();
			text.append(fLineDelimiter);  // Blank line between includes and using declarations.
		} else {
			offset = commentedNodeMap.getOffsetIncludingComments(mergedUsingDeclarations.get(0).existingDeclaration);
		}

		// Since the order of existing using declarations may not be alphabetical, we find positions
		// for the new using declarations by pushing them from them up from the bottom of
		// the using declaration list.  
		for (UsingDeclaration using : usingDeclarations) {
			int i = mergedUsingDeclarations.size();
			while (--i >= 0 && using.compareTo(mergedUsingDeclarations.get(i)) < 0) {}
			mergedUsingDeclarations.add(i + 1, using);
		}

		UsingDeclaration previousUsing = null;
		for (UsingDeclaration using : mergedUsingDeclarations) {
			if (using.existingDeclaration == null) {
				if (previousUsing != null) {
					IASTNode previousNode = previousUsing.existingDeclaration;
					if (previousNode != null) {
						offset = ASTNodes.skipToNextLineAfterNode(contents, previousNode);
						flushEditBuffer(offset, text, rootEdit);
						if (contents.charAt(offset - 1) != '\n')
							text.append(fLineDelimiter);
					}
				}
				text.append(using.composeDirective());
				text.append(fLineDelimiter);
			} else {
				flushEditBuffer(offset, text, rootEdit);
			}
			previousUsing = using;
		}
		flushEditBuffer(offset, text, rootEdit);

		return rootEdit;
	}

	private List<UsingDeclaration> getUsingDeclarations(IASTTranslationUnit ast) {
		List<UsingDeclaration> usingDeclarations = new ArrayList<UsingDeclaration>();
		IASTDeclaration[] declarations = ast.getDeclarations();
		for (IASTDeclaration declaration : declarations) {
			if (declaration instanceof ICPPASTUsingDeclaration) {
				usingDeclarations.add(new UsingDeclaration((ICPPASTUsingDeclaration) declaration));
			}
		}
		return usingDeclarations;
	}

	private void flushEditBuffer(int offset, StringBuilder text, MultiTextEdit edit) {
		if (text.length() != 0) {
			edit.addChild(new InsertEdit(offset, text.toString()));
			text.delete(0, text.length());
		}
	}

	/**
	 * Adds an include candidate to the <code>candidates</code> map if the file containing
	 * the definition is suitable for inclusion.
	 */
	private void considerForInclusion(IIndexName definition, IIndexBinding binding,
			IIndex index, Map<String, IncludeCandidate> candidates) throws CoreException {
		if (definition == null) {
			return;
		}
		IIndexFile file = definition.getFile();
		// Consider the file for inclusion only if it is not a source file,
		// or a source file that was already included by some other file. 
		if (!isSource(getPath(file)) || index.findIncludedBy(file, 0).length > 0) {
			IIndexFile representativeFile = getRepresentativeFile(file, index);
			IncludeInfo include = getRequiredInclude(representativeFile, index);
			if (include != null) {
				IncludeCandidate candidate = new IncludeCandidate(binding, include);
				if (!candidates.containsKey(candidate.toString())) {
					candidates.put(candidate.toString(), candidate);
				}
			}
		}
	}

	private UsingDeclaration deduceUsingDeclaration(IBinding source, IBinding target,
			IASTTranslationUnit ast) {
		if (source.equals(target)) {
			return null;  // No using declaration is needed.
		}
		ArrayList<String> targetChain = getUsingChain(target);
		if (targetChain.size() <= 1) {
			return null;  // Target is not in a namespace
		}

		// Check if any of the existing using declarations and directives matches
		// the target.
		final IASTDeclaration[] declarations= ast.getDeclarations(false);
		for (IASTDeclaration declaration : declarations) {
			if (declaration.isPartOfTranslationUnitFile()) {
				IASTName name = null;
				if (declaration instanceof ICPPASTUsingDeclaration) {
					name = ((ICPPASTUsingDeclaration) declaration).getName();
					if (match(name, targetChain, false)) {
						return null;
					}
				} else if (declaration instanceof ICPPASTUsingDirective) {
					name = ((ICPPASTUsingDirective) declaration).getQualifiedName();
					if (match(name, targetChain, true)) {
						return null;
					}
				}
			}
		}

		ArrayList<String> sourceChain = getUsingChain(source);
		if (sourceChain.size() >= targetChain.size()) {
			int j = targetChain.size();
			for (int i = sourceChain.size(); --j >= 1 && --i >= 1;) {
				if (!sourceChain.get(i).equals(targetChain.get(j))) {
					break;
				}
			}
			if (j <= 0) {
				return null;  // Source is in the target's namespace
			}
		}
		StringBuilder buf = new StringBuilder();
		for (int i = targetChain.size(); --i >= 0;) {
			if (buf.length() > 0) {
				buf.append("::"); //$NON-NLS-1$
			}
			buf.append(targetChain.get(i));
		}
		return new UsingDeclaration(buf.toString());
	}

	private boolean match(IASTName name, ArrayList<String> usingChain, boolean excludeLast) {
		IASTName[] names;
		if (name instanceof ICPPASTQualifiedName) {
			names = ((ICPPASTQualifiedName) name).getNames();
		} else {
			names = new IASTName[] { name };
		}
		if (names.length != usingChain.size() - (excludeLast ? 1 : 0)) {
			return false;
		}
		for (int i = 0; i < names.length; i++) {
			if (!names[i].toString().equals(usingChain.get(usingChain.size() - 1 - i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns components of the qualified name in reverse order.
	 * For ns1::ns2::Name, e.g., it returns [Name, ns2, ns1].
	 */
	private ArrayList<String> getUsingChain(IBinding binding) {
		ArrayList<String> chain = new ArrayList<String>(4);
		for (; binding != null; binding = binding.getOwner()) {
			String name = binding.getName();
			if (binding instanceof ICPPNamespace) {
				if (name.length() == 0) {
					continue;
				}
			} else {
				chain.clear();
			}
			chain.add(name);
		}
		return chain;
	}

	/**
	 * Given a header file, decides if this header file should be included directly or
	 * through another header file. For example, <code>bits/stl_map.h</code> is not supposed
	 * to be included directly, but should be represented by <code>map</code>.
	 * @return the header file to include.
	 */
	private IIndexFile getRepresentativeFile(IIndexFile headerFile, IIndex index) {
		try {
			if (isWorkspaceFile(headerFile.getLocation().getURI())) {
				return headerFile;
			}
			ArrayDeque<IIndexFile> front = new ArrayDeque<IIndexFile>();
			front.add(headerFile);
			HashSet<IIndexFile> processed = new HashSet<IIndexFile>();
			processed.add(headerFile);
			while (!front.isEmpty()) {
				IIndexFile file = front.remove();
				// A header without an extension is a good candidate for inclusion into a C++ source file.
				if (fContext.isCXXLanguage() && !hasExtension(getPath(file))) {
					return file;
				}
				IIndexInclude[] includes = index.findIncludedBy(file, 0);
				for (IIndexInclude include : includes) {
					IIndexFile includer = include.getIncludedBy();
					if (!processed.contains(includer)) {
						URI uri = includer.getLocation().getURI();
						if (isSource(uri.getPath()) || isWorkspaceFile(uri)) {
							return file;
						}
						front.add(includer);
						processed.add(includer);
					}
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return headerFile;
	}

	private boolean isWorkspaceFile(URI uri) {
		for (IFile file : ResourceLookup.findFilesForLocationURI(uri)) {
			if (file.exists()) {
				return true;
			}
		}
		return false;
	}

	private boolean hasExtension(String path) {
		return path.indexOf('.', path.lastIndexOf('/') + 1) >= 0;
	}

	private IFunctionSummary findContribution(final String name) throws CoreException {
		ICHelpInvocationContext context = new ICHelpInvocationContext() {
			@Override
			public IProject getProject() {
				return fContext.getProject();
			}

			@Override
			public ITranslationUnit getTranslationUnit() {
				return fContext.getTranslationUnit();
			}
		};

		return CHelpProviderManager.getDefault().getFunctionInfo(context, name);
	}

	/**
	 * Checks if a file is a source file (.c, .cpp, .cc, etc). Header files are not considered
	 * source files.
	 *
	 * @return Returns {@code true} if the the file is a source file.
	 */
	private boolean isSource(String filename) {
		IContentType ct= CCorePlugin.getContentType(fContext.getProject(), filename);
		if (ct != null) {
			String id = ct.getId();
			if (CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id) || CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(id)) {
				return true;
			}
		}
		return false;
	}

	private static String getPath(IIndexFile file) throws CoreException {
		return file.getLocation().getURI().getPath();
	}

	/**
	 * Returns the RequiredInclude object to be added to the include list
	 * @param path - the full path of the file to include
	 * @return the required include
	 * @throws CoreException 
	 */
	private IncludeInfo getRequiredInclude(IIndexFile file, IIndex index) throws CoreException {
		IIndexInclude[] includes = index.findIncludedBy(file);
		if (includes.length > 0) {
			// Let the existing includes vote. To be eligible to vote, an include
			// has to be resolvable in the context of the current translation unit.
			int systemIncludeVotes = 0;
			String[] ballotBox = new String[includes.length];
			int k = 0;
			for (IIndexInclude include : includes) {
				if (isResolvableInCurrentContext(include)) {
					ballotBox[k++] = include.getFullName();
					if (include.isSystemInclude()) {
						systemIncludeVotes++;
					}
				}
			}
			if (k != 0) {
				Arrays.sort(ballotBox, 0, k);
				String contender = ballotBox[0];
				int votes = 1;
				String winner = contender;
				int winnerVotes = votes;
				for (int i = 1; i < k; i++) {
					if (!ballotBox[i].equals(contender)) {
						contender = ballotBox[i]; 
						votes = 1;
					}
					votes++;
					if (votes > winnerVotes) {
						winner = contender;
						winnerVotes = votes;
					}
				}
				return new IncludeInfo(winner, systemIncludeVotes * 2 >= k);
			}
		}

		// The file has never been included before.
        IPath targetLocation = getAbsolutePath(file.getLocation());
        return fContext.getIncludeForHeaderFile(targetLocation);
    }

	/**
	 * Returns {@code true} if the given include can be resolved in the context of
	 * the current translation unit.
	 */
	private boolean isResolvableInCurrentContext(IIndexInclude include) {
		try {
			IncludeInfo includeInfo = new IncludeInfo(include.getFullName(), include.isSystemInclude());
			return fContext.resolveInclude(includeInfo) != null;
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return false;
		}
	}

	/**
	 * Returns the fully qualified name for a given index binding.
	 * @param binding
	 * @return binding's fully qualified name
	 * @throws CoreException
	 */
	private static String getBindingQualifiedName(IIndexBinding binding) throws CoreException {
		String[] qname= CPPVisitor.getQualifiedName(binding);
		StringBuilder result = new StringBuilder();
		boolean needSep= false;
		for (String element : qname) {
			if (needSep)
				result.append(Keywords.cpCOLONCOLON);
			result.append(element);  
			needSep= true;
		}
		return result.toString();
	}

	/**
	 * To be used by ElementListSelectionDialog for user to choose which declarations/
	 * definitions for "add include" when there are more than one to choose from.  
	 */
	private static class IncludeCandidate {
		final IIndexBinding binding;
		final IncludeInfo include;
		final String label;

		IncludeCandidate(IIndexBinding binding, IncludeInfo include) throws CoreException {
			this.binding = binding;
			this.include = include;
			this.label = getBindingQualifiedName(binding) + " - " + include.toString(); //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private static class UsingDeclaration implements Comparable<UsingDeclaration> {
		final String name;
		final ICPPASTUsingDeclaration existingDeclaration;

		UsingDeclaration(String name) {
			this.name = name;
			this.existingDeclaration = null;
		}

		UsingDeclaration(ICPPASTUsingDeclaration existingDeclaration) {
			this.name = ASTStringUtil.getQualifiedName(existingDeclaration.getName());
			this.existingDeclaration = existingDeclaration;
		}

		@Override
		public int compareTo(UsingDeclaration other) {
			return COLLATOR.compare(name, other.name);
		}

		String composeDirective() {
			return "using " + name + ';'; //$NON-NLS-1$
		}
	}
}
