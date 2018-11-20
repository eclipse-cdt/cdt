/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScopeMapper.InlineNamespaceDirective;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of namespace scopes, including global scope.
 */
public class CPPNamespaceScope extends CPPScope implements ICPPInternalNamespaceScope {
	private static final ICPPInternalNamespaceScope[] NO_NAMESPACE_SCOPES = {};

	private List<ICPPUsingDirective> fUsingDirectives;

	private boolean fIsInline;
	private boolean fIsInlineInitialized;
	private ICPPNamespaceScope[] fEnclosingNamespaceSet;
	private List<ICPPASTNamespaceDefinition> fInlineNamespaceDefinitions;
	private ICPPInternalNamespaceScope[] fInlineNamespaces;

	// The set of names declared in this scope that are currently only visible to argument-dependent lookup.
	private CharArraySet fVisibleToAdlOnly = new CharArraySet(0);

	public CPPNamespaceScope(IASTNode physicalNode) {
		super(physicalNode);
	}

	@Override
	public EScopeKind getKind() {
		if (getPhysicalNode() instanceof IASTTranslationUnit)
			return EScopeKind.eGlobal;

		return EScopeKind.eNamespace;
	}

	@Override
	public ICPPUsingDirective[] getUsingDirectives() {
		initUsingDirectives();
		populateCache();
		return fUsingDirectives.toArray(new ICPPUsingDirective[fUsingDirectives.size()]);
	}

	private void initUsingDirectives() {
		if (fUsingDirectives == null) {
			fUsingDirectives = new ArrayList<>(1);
			// Insert a using directive for every inline namespace found in the index.
			for (ICPPInternalNamespaceScope inline : getIndexInlineNamespaces()) {
				if (!(inline instanceof CPPNamespaceScope)) {
					fUsingDirectives.add(new InlineNamespaceDirective(this, inline));
				}
			}
		}
	}

	@Override
	public void addUsingDirective(ICPPUsingDirective directive) {
		initUsingDirectives();
		fUsingDirectives.add(directive);
	}

	@Override
	public IName getScopeName() {
		IASTNode node = getPhysicalNode();
		if (node instanceof ICPPASTNamespaceDefinition) {
			return ((ICPPASTNamespaceDefinition) node).getName();
		}
		return null;
	}

	public IScope findNamespaceScope(IIndexScope scope) {
		final ArrayList<IBinding> parentChain = new ArrayList<>();
		for (IBinding binding = scope.getScopeBinding(); binding != null; binding = binding.getOwner()) {
			parentChain.add(binding);
		}

		final IScope[] result = { null };
		final ASTVisitor visitor = new ASTVisitor() {
			private int position = parentChain.size();

			{
				shouldVisitNamespaces = shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof ICPPASTLinkageSpecification)
					return PROCESS_CONTINUE;
				return PROCESS_SKIP;
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition namespace) {
				final char[] name = namespace.getName().toCharArray();
				IBinding binding = parentChain.get(--position);
				if (!CharArrayUtils.equals(name, binding.getNameCharArray())) {
					++position;
					return PROCESS_SKIP;
				}
				if (position == 0) {
					result[0] = namespace.getScope();
					return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}

			@Override
			public int leave(ICPPASTNamespaceDefinition namespace) {
				++position;
				return PROCESS_CONTINUE;
			}
		};

		getPhysicalNode().accept(visitor);
		return result[0];
	}

	@Override
	public void addName(IASTName name, boolean adlOnly) {
		if (name instanceof ICPPASTQualifiedName && !canDenoteNamespaceMember((ICPPASTQualifiedName) name))
			return;
		super.addName(name, adlOnly);
		if (adlOnly) {
			fVisibleToAdlOnly.put(name.getLookupKey());
		} else {
			fVisibleToAdlOnly.remove(name.getLookupKey());
		}
	}

	@Override
	protected boolean nameIsVisibleToLookup(ScopeLookupData lookup) {
		if (lookup.isArgumentDependent()) {
			return true;
		}
		return !fVisibleToAdlOnly.containsKey(lookup.getLookupKey());
	}

	public boolean canDenoteNamespaceMember(ICPPASTQualifiedName name) {
		IScope scope = this;
		ICPPASTNameSpecifier[] segments = name.getQualifier();
		try {
			for (int i = segments.length; --i >= 0;) {
				if (scope == null)
					return false;
				IName scopeName = scope.getScopeName();
				if (scopeName == null)
					return false;

				if (segments[i] instanceof IASTName) {
					IASTName segmentName = (IASTName) segments[i];
					if (segmentName instanceof ICPPASTTemplateId
							|| !CharArrayUtils.equals(scopeName.getSimpleID(), segmentName.getSimpleID())) {
						return false;
					}
				} else {
					IBinding segmentBinding = segments[i].resolveBinding();
					if (segmentBinding instanceof ICPPTemplateInstance
							|| !CharArrayUtils.equals(scopeName.getSimpleID(), segmentBinding.getNameCharArray())) {
						return false;
					}
				}
				scope = scope.getParent();
			}
			if (!name.isFullyQualified() || scope == null) {
				return true;
			}
			return ASTInternal.getPhysicalNodeOfScope(scope) instanceof IASTTranslationUnit;
		} catch (DOMException e) {
			return false;
		}
	}

	@Override
	public boolean isInlineNamepace() {
		if (!fIsInlineInitialized) {
			fIsInline = computeIsInline();
			fIsInlineInitialized = true;
		}
		return fIsInline;
	}

	public boolean computeIsInline() {
		final IASTNode node = getPhysicalNode();
		if (!(node instanceof ICPPASTNamespaceDefinition)) {
			return false;
		}

		if (((ICPPASTNamespaceDefinition) node).isInline())
			return true;

		IASTTranslationUnit tu = node.getTranslationUnit();
		if (tu != null) {
			final IIndex index = tu.getIndex();
			IIndexFileSet fileSet = tu.getASTFileSet();
			if (index != null && fileSet != null) {
				fileSet = fileSet.invert();
				ICPPNamespace nsBinding = getNamespaceIndexBinding(index);
				if (nsBinding != null && nsBinding.isInline()) {
					try {
						IIndexName[] names = index.findDefinitions(nsBinding);
						for (IIndexName name : names) {
							if (name.isInlineNamespaceDefinition() && fileSet.contains(name.getFile())) {
								return true;
							}
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
		}
		return false;
	}

	@Override
	public ICPPNamespaceScope[] getEnclosingNamespaceSet() {
		if (fEnclosingNamespaceSet == null) {
			return fEnclosingNamespaceSet = computeEnclosingNamespaceSet(this);
		}
		return fEnclosingNamespaceSet;
	}

	@Override
	public ICPPInternalNamespaceScope[] getInlineNamespaces() {
		if (getKind() == EScopeKind.eLocal)
			return NO_NAMESPACE_SCOPES;

		if (fInlineNamespaces == null) {
			fInlineNamespaces = computeInlineNamespaces();
		}
		return fInlineNamespaces;
	}

	ICPPInternalNamespaceScope[] computeInlineNamespaces() {
		populateCache();
		Set<ICPPInternalNamespaceScope> result = null;
		if (fInlineNamespaceDefinitions != null) {
			result = new HashSet<>(fInlineNamespaceDefinitions.size());
			for (ICPPASTNamespaceDefinition nsdef : fInlineNamespaceDefinitions) {
				final IScope scope = nsdef.getScope();
				if (scope instanceof ICPPInternalNamespaceScope) {
					result.add((ICPPInternalNamespaceScope) scope);
				}
			}
		}

		for (ICPPInternalNamespaceScope inline : getIndexInlineNamespaces()) {
			if (result == null)
				result = new HashSet<>();
			result.add(inline);
		}

		if (result == null) {
			return NO_NAMESPACE_SCOPES;
		}
		return result.toArray(new ICPPInternalNamespaceScope[result.size()]);
	}

	private ICPPInternalNamespaceScope[] getIndexInlineNamespaces() {
		IASTTranslationUnit tu = getPhysicalNode().getTranslationUnit();
		if (tu instanceof CPPASTTranslationUnit) {
			CPPASTTranslationUnit ast = (CPPASTTranslationUnit) tu;
			IIndex index = tu.getIndex();
			if (index != null) {
				IScope[] inlineScopes = null;
				ICPPNamespace namespace = getNamespaceIndexBinding(index);
				try {
					if (namespace != null) {
						ICPPNamespaceScope scope = namespace.getNamespaceScope();
						inlineScopes = scope.getInlineNamespaces();
					} else if (getKind() == EScopeKind.eGlobal) {
						inlineScopes = index.getInlineNamespaces();
					}
				} catch (CoreException e) {
				}
				if (inlineScopes != null) {
					List<ICPPInternalNamespaceScope> result = null;
					for (IScope scope : inlineScopes) {
						scope = ast.mapToASTScope(scope);
						if (scope instanceof ICPPInternalNamespaceScope) {
							if (result == null) {
								result = new ArrayList<>();
							}
							result.add((ICPPInternalNamespaceScope) scope);
						}
					}
					if (result != null) {
						return result.toArray(new ICPPInternalNamespaceScope[result.size()]);
					}
				}
			}
		}
		return NO_NAMESPACE_SCOPES;
	}

	/**
	 * Called while populating scope.
	 */
	public void addInlineNamespace(ICPPASTNamespaceDefinition nsDef) {
		if (fInlineNamespaceDefinitions == null) {
			fInlineNamespaceDefinitions = new ArrayList<>();
		}
		fInlineNamespaceDefinitions.add(nsDef);
	}

	public static ICPPNamespaceScope[] computeEnclosingNamespaceSet(ICPPInternalNamespaceScope nsScope) {
		if (nsScope.isInlineNamepace()) {
			try {
				IScope parent = nsScope.getParent();
				if (parent instanceof ICPPInternalNamespaceScope) {
					return ((ICPPInternalNamespaceScope) parent).getEnclosingNamespaceSet();
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}

		Set<ICPPInternalNamespaceScope> result = new HashSet<>();
		result.add(nsScope);
		addInlineNamespaces(nsScope, result);
		return result.toArray(new ICPPNamespaceScope[result.size()]);
	}

	private static void addInlineNamespaces(ICPPInternalNamespaceScope nsScope,
			Set<ICPPInternalNamespaceScope> result) {
		ICPPInternalNamespaceScope[] inlineNss = nsScope.getInlineNamespaces();
		for (ICPPInternalNamespaceScope inlineNs : inlineNss) {
			if (result.add(inlineNs)) {
				addInlineNamespaces(inlineNs, result);
			}
		}
	}
}
