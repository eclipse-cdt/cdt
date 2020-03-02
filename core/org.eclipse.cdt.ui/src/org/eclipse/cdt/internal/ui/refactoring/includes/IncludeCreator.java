/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import static org.eclipse.cdt.core.index.IndexLocationFactory.getAbsolutePath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
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
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.util.TextUtil;
import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.internal.corext.codemanipulation.StyledInclude;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

import com.ibm.icu.text.Collator;

/**
 * Adds an include statement and, optionally, a 'using' declaration for the currently
 * selected name.
 */
public class IncludeCreator {
	private static final Collator COLLATOR = Collator.getInstance();

	private final IElementSelector fAmbiguityResolver;
	private final IncludeCreationContext fContext;

	public IncludeCreator(ITranslationUnit tu, IIndex index, IElementSelector ambiguityResolver) {
		fAmbiguityResolver = ambiguityResolver;
		fContext = new IncludeCreationContext(tu, index);
	}

	public MultiTextEdit createInclude(IASTTranslationUnit ast, ITextSelection selection) throws CoreException {
		try {
			CPPSemantics.enablePromiscuousBindingResolution();
			return createIncludeImpl(ast, selection);
		} finally {
			CPPSemantics.disablePromiscuousBindingResolution();
		}
	}

	private MultiTextEdit createIncludeImpl(IASTTranslationUnit ast, ITextSelection selection) throws CoreException {
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

		final Map<String, IncludeCandidate> candidatesMap = new HashMap<>();
		final IndexFilter filter = IndexFilter.getDeclaredBindingFilter(ast.getLinkage().getLinkageID(), false);

		final List<IncludeInfo> requiredIncludes = new ArrayList<>();
		final List<UsingDeclaration> usingDeclarations = new ArrayList<>();

		List<IIndexBinding> bindings = new ArrayList<>();
		try {
			IIndex index = fContext.getIndex();
			IIndexBinding adaptedBinding = index.adaptBinding(binding);
			if (adaptedBinding == null) {
				bindings.addAll(Arrays.asList(index.findBindings(nameChars, false, filter, new NullProgressMonitor())));
			} else {
				bindings.add(adaptedBinding);
				while (adaptedBinding instanceof ICPPSpecialization) {
					adaptedBinding = index.adaptBinding(((ICPPSpecialization) adaptedBinding).getSpecializedBinding());
					if (adaptedBinding != null) {
						bindings.add(adaptedBinding);
					}
				}
			}

			HeaderSubstitutor headerSubstitutor = new HeaderSubstitutor(fContext);

			for (IIndexBinding indexBinding : bindings) {
				// Replace ctor with the class itself.
				if (indexBinding instanceof ICPPConstructor) {
					indexBinding = indexBinding.getOwner();
				}
				IIndexName[] definitions = null;
				// class, struct, union, enum-type, enum-item
				if (indexBinding instanceof ICompositeType || indexBinding instanceof IEnumeration
						|| indexBinding instanceof IEnumerator) {
					definitions = index.findDefinitions(indexBinding);
				} else if (indexBinding instanceof ITypedef || indexBinding instanceof IFunction) {
					definitions = index.findDeclarations(indexBinding);
				}
				if (definitions != null) {
					for (IIndexName definition : definitions) {
						considerForInclusion(ast, definition, indexBinding, index, headerSubstitutor, candidatesMap);
					}
					if (definitions.length > 0 && adaptedBinding != null)
						break;
				}
			}
			IIndexMacro[] macros = index.findMacros(nameChars, filter, new NullProgressMonitor());
			for (IIndexMacro macro : macros) {
				IIndexName definition = macro.getDefinition();
				considerForInclusion(ast, definition, macro, index, headerSubstitutor, candidatesMap);
			}

			final ArrayList<IncludeCandidate> candidates = new ArrayList<>(candidatesMap.values());
			if (candidates.size() > 1) {
				// First, try to resolve the ambiguity by comparing the namespaces of the
				// candidate bindings to the namespace in which the source name occurs.
				IncludeCandidate candidate = selectCandidateByNamespace(name, candidates);
				// If that doesn't disambiguate, fall back to the ambiguity resolver
				// provided by the user of this class.
				if (candidate == null)
					candidate = fAmbiguityResolver.selectElement(candidates);
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

	private ICPPNamespaceScope getContainingNamespaceScope(IASTNode node) {
		IScope scope = CPPVisitor.getContainingScope(node);
		while (scope != null) {
			if (scope instanceof ICPPNamespaceScope && !(scope instanceof ICPPBlockScope)) {
				return (ICPPNamespaceScope) scope;
			}
			try {
				scope = scope.getParent();
			} catch (DOMException e) {
				return null;
			}
		}
		return null;
	}

	private ICPPNamespace getContainingNamespace(IASTName name) {
		ICPPNamespaceScope scope = getContainingNamespaceScope(name);
		// TODO(nathanridge): Move this ICPPNamespaceScope -> ICPPNamespace
		// mapping code to a utility class.
		if (scope instanceof ICPPNamespace) {
			return (ICPPNamespace) scope;
		}
		if (scope instanceof IASTInternalScope) {
			IASTNode node = ((IASTInternalScope) scope).getPhysicalNode();
			if (node instanceof ICPPASTNamespaceDefinition) {
				IBinding namespace = ((ICPPASTNamespaceDefinition) node).getName().resolveBinding();
				if (namespace instanceof ICPPNamespace) {
					return (ICPPNamespace) namespace;
				}
			}
		}
		return null;
	}

	private ICPPNamespace getContainingNamespace(IBinding binding) {
		while (binding != null) {
			if (binding instanceof ICPPNamespace) {
				return (ICPPNamespace) binding;
			}
			binding = binding.getOwner();
		}
		return null;
	}

	private IncludeCandidate selectCandidateByNamespace(IASTName sourceName, ArrayList<IncludeCandidate> candidates) {
		// If one of the candidates is in the same namespace as the source name,
		// and the others aren't, prefer that candidate.
		ICPPNamespace sourceNamespace = getContainingNamespace(sourceName);
		if (sourceNamespace == null) {
			return null;
		}
		IncludeCandidate winner = null;
		for (IncludeCandidate candidate : candidates) {
			if (SemanticUtil.isSameNamespace(sourceNamespace, getContainingNamespace(candidate.binding))) {
				if (winner != null) {
					return null; // ambiguous between 'winner' and 'candidate'
				}
				winner = candidate;
			}
		}
		return winner;
	}

	private MultiTextEdit createEdit(List<IncludeInfo> includes, List<UsingDeclaration> usingDeclarations,
			IASTTranslationUnit ast, ITextSelection selection) {
		NodeCommentMap commentedNodeMap = ASTCommenter.getCommentedNodeMap(ast);
		String contents = fContext.getSourceContents();
		IRegion includeRegion = IncludeUtil.getSafeIncludeReplacementRegion(contents, ast, commentedNodeMap);

		IncludePreferences preferences = fContext.getPreferences();

		MultiTextEdit rootEdit = new MultiTextEdit();

		IASTPreprocessorIncludeStatement[] existingIncludes = ast.getIncludeDirectives();
		fContext.addHeadersIncludedPreviously(existingIncludes);

		List<StyledInclude> styledIncludes = new ArrayList<>();
		// Put the new includes into styledIncludes.
		for (IncludeInfo includeInfo : includes) {
			IPath header = fContext.resolveInclude(includeInfo);
			if (header != null && !fContext.wasIncludedPreviously(header)) {
				IncludeGroupStyle style = fContext.getIncludeStyle(includeInfo);
				StyledInclude prototype = new StyledInclude(header, includeInfo, style);
				styledIncludes.add(prototype);
			}
		}
		Collections.sort(styledIncludes, preferences);

		List<StyledInclude> mergedIncludes = IncludeUtil.getIncludesInRegion(existingIncludes, includeRegion, fContext);

		if (preferences.allowReordering) {
			// Since the order of existing include statements may not match the include order
			// preferences, we find positions for the new include statements by pushing them up
			// from the bottom of the include insertion region.
			for (StyledInclude include : styledIncludes) {
				int i = mergedIncludes.size();
				while (--i >= 0 && preferences.compare(include, mergedIncludes.get(i)) < 0) {
				}
				mergedIncludes.add(i + 1, include);
			}
		} else {
			mergedIncludes.addAll(styledIncludes);
		}

		int offset = includeRegion.getOffset();
		StringBuilder text = new StringBuilder();
		StyledInclude previousInclude = null;
		for (StyledInclude include : mergedIncludes) {
			IASTPreprocessorIncludeStatement existingInclude = include.getExistingInclude();
			if (existingInclude == null) {
				if (previousInclude != null) {
					IASTNode previousNode = previousInclude.getExistingInclude();
					if (previousNode != null) {
						offset = ASTNodes.skipToNextLineAfterNode(contents, previousNode);
						flushEditBuffer(offset, text, rootEdit);
						if (contents.charAt(offset - 1) != '\n')
							text.append(fContext.getLineDelimiter());
					}
					if (include.getStyle().isBlankLineNeededAfter(previousInclude.getStyle(),
							preferences.includeStyles)) {
						if (TextUtil.isLineBlank(contents, offset)) {
							offset = TextUtil.skipToNextLine(contents, offset);
						} else {
							text.append(fContext.getLineDelimiter());
						}
					}
				}
				text.append(include.getIncludeInfo().composeIncludeStatement());
				text.append(fContext.getLineDelimiter());
			} else {
				if (previousInclude != null && previousInclude.getExistingInclude() == null
						&& include.getStyle().isBlankLineNeededAfter(previousInclude.getStyle(),
								preferences.includeStyles)
						&& !TextUtil.isPreviousLineBlank(contents, ASTNodes.offset(existingInclude))) {
					text.append(fContext.getLineDelimiter());
				}
				flushEditBuffer(offset, text, rootEdit);
			}
			previousInclude = include;
		}
		if (includeRegion.getLength() == 0 && !TextUtil.isLineBlank(contents, includeRegion.getOffset())
				&& !includes.isEmpty()) {
			text.append(fContext.getLineDelimiter());
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
			if (usingDeclaration.existingDeclaration.isPartOfTranslationUnitFile()
					&& ASTNodes.endOffset(usingDeclaration.existingDeclaration) <= selection.getOffset()) {
				if (temp == null)
					temp = new ArrayList<>();
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
			text.append(fContext.getLineDelimiter()); // Blank line between includes and using declarations.
		} else {
			offset = commentedNodeMap.getOffsetIncludingComments(mergedUsingDeclarations.get(0).existingDeclaration);
		}

		// Since the order of existing using declarations may not be alphabetical, we find positions
		// for the new using declarations by pushing them from them up from the bottom of
		// the using declaration list.
		for (UsingDeclaration using : usingDeclarations) {
			int i = mergedUsingDeclarations.size();
			while (--i >= 0 && using.compareTo(mergedUsingDeclarations.get(i)) < 0) {
			}
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
							text.append(fContext.getLineDelimiter());
					}
				}
				text.append(using.composeDirective());
				text.append(fContext.getLineDelimiter());
			} else {
				flushEditBuffer(offset, text, rootEdit);
			}
			previousUsing = using;
		}
		flushEditBuffer(offset, text, rootEdit);

		return rootEdit;
	}

	private List<UsingDeclaration> getUsingDeclarations(IASTTranslationUnit ast) {
		List<UsingDeclaration> usingDeclarations = new ArrayList<>();
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
	 * Adds an include candidate to the {@code candidates} map if the file containing the definition
	 * is suitable for inclusion.
	 */
	private void considerForInclusion(IASTTranslationUnit ast, IIndexName definition, IIndexBinding binding,
			IIndex index, HeaderSubstitutor headerSubstitutor, Map<String, IncludeCandidate> candidates)
			throws CoreException {
		if (definition == null) {
			return;
		}
		IIndexFile file = definition.getFile();
		// Consider the file for inclusion only if it is not a source file,
		// or a source file that was already included by some other file.
		if (fContext.canBeIncluded(file)) {
			IncludeInfo include;
			IPath header = getAbsolutePath(file.getLocation());
			header = headerSubstitutor.getPreferredRepresentativeHeader(header);
			if (fContext.getPreferences().heuristicHeaderSubstitution) {
				boolean reachable = ast.getIndexFileSet().contains(file);
				InclusionRequest request = new InclusionRequest(binding, Collections.singletonMap(file, header),
						reachable);
				header = headerSubstitutor.getPreferredRepresentativeHeaderByHeuristic(request, header);
			}
			IncludeGroupStyle style = fContext.getIncludeStyle(header);
			include = fContext.createIncludeInfo(header, style);

			if (include != null) {
				IncludeCandidate candidate = new IncludeCandidate(binding, include);
				if (!candidates.containsKey(candidate.toString())) {
					candidates.put(candidate.toString(), candidate);
				}
			}
		}
	}

	private UsingDeclaration deduceUsingDeclaration(IBinding source, IBinding target, IASTTranslationUnit ast) {
		if (source.equals(target)) {
			return null; // No using declaration is needed.
		}
		ArrayList<String> targetChain = getUsingChain(target);
		if (targetChain.size() <= 1) {
			return null; // Target is not in a namespace.
		}

		// Check if any of the existing using declarations and directives matches the target.
		final IASTDeclaration[] declarations = ast.getDeclarations(false);
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
				return null; // Source is in the target's namespace
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
		ICPPASTNameSpecifier[] qualifiers;
		if (name instanceof ICPPASTQualifiedName) {
			qualifiers = ((ICPPASTQualifiedName) name).getQualifier();
		} else {
			qualifiers = ICPPASTNameSpecifier.EMPTY_NAME_SPECIFIER_ARRAY;
		}
		if (qualifiers.length + 1 != usingChain.size() - (excludeLast ? 1 : 0))
			return false;

		for (int i = 0; i < qualifiers.length; i++) {
			if (!qualifiers[i].toString().equals(usingChain.get(usingChain.size() - 1 - i)))
				return false;
		}
		return name.getLastName().toString().equals(usingChain.get(usingChain.size() - 1 - qualifiers.length));
	}

	private ArrayList<String> getUsingChainForProblemBinding(IProblemBinding binding) {
		ArrayList<String> chain = new ArrayList<>(4);
		chain.add(binding.getName());
		IASTNode node = binding.getASTNode();
		if (node.getParent() instanceof ICPPASTQualifiedName) {
			// If the ProblemBinding is for a name inside the qualified name,
			// use the chain of preceding segments in the qualifier as the
			// using chain.
			ICPPASTQualifiedName qualifiedName = (ICPPASTQualifiedName) node.getParent();
			ICPPASTNameSpecifier[] qualifier = qualifiedName.getQualifier();
			int i = qualifier.length;
			if (node != qualifiedName.getLastName()) {
				while (--i >= 0) {
					if (qualifier[i] == node) {
						break;
					}
				}
			}
			while (--i >= 0) {
				chain.add(qualifier[i].resolveBinding().getName());
			}
		} else {
			// Otherwise, fall back to the chain of namespaces that physically
			// contain the name.
			ICPPNamespaceScope namespace = getContainingNamespaceScope(node);
			while (namespace != null) {
				IName namespaceName = namespace.getScopeName();
				if (namespaceName != null) {
					chain.add(new String(namespaceName.getSimpleID()));
				}
				try {
					IScope parent = namespace.getParent();
					if (parent instanceof ICPPNamespaceScope) {
						namespace = (ICPPNamespaceScope) parent;
					} else {
						break;
					}
				} catch (DOMException e) {
					break;
				}
			}
		}
		return chain;
	}

	/**
	 * Returns components of the qualified name in reverse order.
	 * For ns1::ns2::Name, e.g., it returns [Name, ns2, ns1].
	 */
	private ArrayList<String> getUsingChain(IBinding binding) {
		// For ProblemBindings, getOwner() is very heuristic and doesn't
		// produce the chain of owner bindings we want here, so we
		// handle it specially.
		if (binding instanceof IProblemBinding) {
			return getUsingChainForProblemBinding((IProblemBinding) binding);
		}
		ArrayList<String> chain = new ArrayList<>(4);
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
	 * Returns the fully qualified name for a given index binding.
	 *
	 * @param binding
	 * @return binding's fully qualified name
	 * @throws CoreException
	 */
	private static String getBindingQualifiedName(IIndexBinding binding) throws CoreException {
		String[] qname = CPPVisitor.getQualifiedName(binding);
		StringBuilder result = new StringBuilder();
		boolean needSep = false;
		for (String element : qname) {
			if (needSep)
				result.append(Keywords.cpCOLONCOLON);
			result.append(element);
			needSep = true;
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
