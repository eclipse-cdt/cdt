/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * Utility to map index-scopes to scopes from the AST. This is important for
 * scopes that can be reopened, i.e. namespaces.
 */
public class CPPScopeMapper {
	/**
	 * Used for implicit inline directives for inline namespaces found in the index.
	 */
	public static final class InlineNamespaceDirective implements ICPPUsingDirective {
		private final ICPPInternalNamespaceScope fContainer;
		private final ICPPInternalNamespaceScope fNominated;

		public InlineNamespaceDirective(ICPPInternalNamespaceScope container, ICPPInternalNamespaceScope inline) {
			fContainer = container;
			fNominated = inline;
		}

		@Override
		public IScope getContainingScope() {
			return fContainer;
		}

		@Override
		public ICPPNamespaceScope getNominatedScope() throws DOMException {
			return fNominated;
		}

		@Override
		public int getPointOfDeclaration() {
			return 0;
		}
	}

	/**
	 * Wrapper for namespace-scopes from the index.
	 */
	private class NamespaceScopeWrapper implements ICPPInternalNamespaceScope {
		private final ICPPNamespaceScope fScope;
		private ArrayList<ICPPUsingDirective> fUsingDirectives;
		private ICPPNamespaceScope[] fEnclosingNamespaceSet;

		public NamespaceScopeWrapper(ICPPNamespaceScope scope) {
			fScope = scope;
			assert fScope instanceof IIndexScope;
		}

		@Override
		public EScopeKind getKind() {
			return fScope.getKind();
		}

		@Override
		public IBinding[] find(String name, IASTTranslationUnit tu) {
			return fScope.find(name, tu);
		}

		@Override
		@Deprecated
		public IBinding[] find(String name) {
			return fScope.find(name);
		}

		@Override
		public IBinding getBinding(IASTName name, boolean resolve) {
			return fScope.getBinding(name, resolve);
		}

		@Override
		public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) {
			return fScope.getBinding(name, resolve, acceptLocalBindings);
		}

		@Override
		@Deprecated
		public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) {
			return fScope.getBindings(name, resolve, prefixLookup);
		}

		@Override
		@Deprecated
		public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,
				IIndexFileSet acceptLocalBindings) {
			return getBindings(name, resolve, prefixLookup, acceptLocalBindings);
		}

		@Override
		public IBinding[] getBindings(ScopeLookupData lookup) {
			return fScope.getBindings(lookup);
		}

		@Override
		public IScope getParent() throws DOMException {
			IScope parent = fScope.getParent();
			if (parent instanceof IIndexScope) {
				return mapToASTScope((IIndexScope) parent);
			}
			return fTu.getScope();
		}

		@Override
		public IName getScopeName() {
			return fScope.getScopeName();
		}

		@Override
		public void addUsingDirective(ICPPUsingDirective usingDirective) {
			initUsingDirectives();
			fUsingDirectives.add(usingDirective);
		}

		private void initUsingDirectives() {
			if (fUsingDirectives == null) {
				fUsingDirectives = new ArrayList<>(1);
				// Insert a using directive for every inline namespace
				for (ICPPInternalNamespaceScope inline : getInlineNamespaces()) {
					fUsingDirectives.add(new InlineNamespaceDirective(this, inline));
				}
			}
		}

		@Override
		public ICPPUsingDirective[] getUsingDirectives() {
			initUsingDirectives();
			return fUsingDirectives.toArray(new ICPPUsingDirective[fUsingDirectives.size()]);
		}

		@Override
		public ICPPNamespaceScope[] getEnclosingNamespaceSet() {
			if (fEnclosingNamespaceSet == null)
				return fEnclosingNamespaceSet = CPPNamespaceScope.computeEnclosingNamespaceSet(this);

			return fEnclosingNamespaceSet;
		}

		@Override
		public boolean isInlineNamepace() {
			IIndexBinding binding = ((IIndexScope) fScope).getScopeBinding();
			if (binding instanceof ICPPNamespace && ((ICPPNamespace) binding).isInline())
				return true;

			return false;
		}

		@Override
		public ICPPInternalNamespaceScope[] getInlineNamespaces() {
			// Obtain the inline namespaces from the index and map them to the AST.
			ICPPNamespaceScope[] pre = fScope.getInlineNamespaces();
			if (pre.length == 0)
				return ICPPInternalNamespaceScope.EMPTY_NAMESPACE_SCOPE_ARRAY;
			ICPPInternalNamespaceScope[] result = new ICPPInternalNamespaceScope[pre.length];
			for (int i = 0; i < result.length; i++) {
				result[i] = (ICPPInternalNamespaceScope) mapToASTScope((IIndexScope) pre[i]);
			}
			return result;
		}

		@Override
		public String toString() {
			return fScope.toString();
		}
	}

	/**
	 * Wrapper for using directives from the index.
	 */
	private class UsingDirectiveWrapper implements ICPPUsingDirective {
		private final int fOffset;
		private final ICPPUsingDirective fDirective;

		public UsingDirectiveWrapper(int offset, ICPPUsingDirective ud) {
			fOffset = offset;
			fDirective = ud;
		}

		@Override
		public IScope getContainingScope() {
			final IScope scope = fDirective.getContainingScope();
			if (scope == null) {
				return fTu.getScope();
			}
			return scope;
		}

		@Override
		public ICPPNamespaceScope getNominatedScope() throws DOMException {
			return fDirective.getNominatedScope();
		}

		@Override
		public int getPointOfDeclaration() {
			return fOffset;
		}

		@Override
		public String toString() {
			return fDirective.toString();
		}
	}

	/**
	 * Collector for class definitions.
	 */
	private class Visitor extends ASTVisitor {
		Visitor() {
			shouldVisitDeclarations = true;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof IASTSimpleDeclaration) {
				IASTDeclSpecifier declspec = ((IASTSimpleDeclaration) declaration).getDeclSpecifier();
				if (declspec instanceof IASTCompositeTypeSpecifier) {
					IASTCompositeTypeSpecifier cts = (IASTCompositeTypeSpecifier) declspec;
					final IASTName name = cts.getName();
					final char[] nameChars = name.getLookupKey();
					if (nameChars.length > 0) {
						IASTName[] names = fClasses.get(nameChars);
						names = ArrayUtil.append(IASTName.class, names, name);
						fClasses.put(nameChars, names);
					}
					return PROCESS_CONTINUE;
				}
				return PROCESS_SKIP;
			} else if (declaration instanceof IASTASMDeclaration || declaration instanceof IASTFunctionDefinition) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
	}

	private final HashMap<IIndexScope, IScope> fMappedScopes = new HashMap<>();
	private final HashMap<String, NamespaceScopeWrapper> fNamespaceWrappers = new HashMap<>();
	private final Map<String, List<UsingDirectiveWrapper>> fPerName = new HashMap<>();
	private final CPPASTTranslationUnit fTu;
	protected CharArrayMap<IASTName[]> fClasses;

	private final Map<ICPPClassTemplatePartialSpecialization, ICPPClassTemplatePartialSpecialization> fPartialSpecs = new HashMap<>();

	public CPPScopeMapper(CPPASTTranslationUnit tu) {
		fTu = tu;
	}

	/**
	 * Register an additional list of using directives to be considered.
	 * @param offset the global offset at which the using directives are provided
	 * @param usingDirectives the list of additional directives.
	 */
	public void registerAdditionalDirectives(int offset, List<ICPPUsingDirective> usingDirectives) {
		if (!usingDirectives.isEmpty()) {
			for (ICPPUsingDirective ud : usingDirectives) {
				IScope container = ud.getContainingScope();
				try {
					final String name = getReverseQualifiedName(container);
					List<UsingDirectiveWrapper> list = fPerName.get(name);
					if (list == null) {
						list = new LinkedList<>();
						fPerName.put(name, list);
					}
					list.add(new UsingDirectiveWrapper(offset, ud));
				} catch (DOMException e) {
				}
			}
		}
	}

	/**
	 * Adds additional directives previously registered to the given scope.
	 */
	public void handleAdditionalDirectives(ICPPNamespaceScope scope) {
		assert !(scope instanceof IIndexScope);
		if (fPerName.isEmpty()) {
			return;
		}
		try {
			String qname = getReverseQualifiedName(scope);
			List<UsingDirectiveWrapper> candidates = fPerName.remove(qname);
			if (candidates != null) {
				for (UsingDirectiveWrapper ud : candidates) {
					scope.addUsingDirective(ud);
				}
			}
		} catch (DOMException e) {
		}
	}

	private String getReverseQualifiedName(IScope scope) throws DOMException {
		final CPPNamespaceScope tuscope = fTu.getScope();
		if (scope == tuscope || scope == null || scope.getKind() == EScopeKind.eGlobal) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder buf = new StringBuilder();
		IName scopeName = scope.getScopeName();
		if (scopeName != null) {
			buf.append(scopeName.getSimpleID());
		}
		scope = scope.getParent();
		while (scope.getKind() != EScopeKind.eGlobal && scope != tuscope) {
			buf.append(':');
			scopeName = scope.getScopeName();
			if (scopeName != null) {
				buf.append(scope.getScopeName().getSimpleID());
			}
			scope = scope.getParent();
		}
		return buf.toString();
	}

	/**
	 * Maps namespace scopes from the index back into the AST.
	 */
	public IScope mapToASTScope(IIndexScope scope) {
		if (scope.getKind() == EScopeKind.eGlobal) {
			return fTu.getScope();
		}
		if (scope instanceof ICPPNamespaceScope) {
			IScope result = fMappedScopes.get(scope);
			if (result == null) {
				result = fTu.getScope().findNamespaceScope(scope);
				if (result == null) {
					result = wrapNamespaceScope((ICPPNamespaceScope) scope);
				}
				fMappedScopes.put(scope, result);
			}
			return result;
		}
		return scope;
	}

	private IScope wrapNamespaceScope(ICPPNamespaceScope scope) {
		try {
			String rqname = getReverseQualifiedName(scope);
			NamespaceScopeWrapper result = fNamespaceWrappers.get(rqname);
			if (result == null) {
				result = new NamespaceScopeWrapper(getCompositeNamespaceScope(scope));
				fNamespaceWrappers.put(rqname, result);
			}
			return result;
		} catch (DOMException e) {
			assert false; // index scopes don't throw dom-exceptions
			return null;
		}
	}

	private ICPPNamespaceScope getCompositeNamespaceScope(ICPPNamespaceScope scope) throws DOMException {
		if (scope instanceof IIndexScope) {
			IIndexBinding binding = fTu.getIndex().adaptBinding(((IIndexScope) scope).getScopeBinding());
			if (binding instanceof ICPPNamespace) {
				scope = ((ICPPNamespace) binding).getNamespaceScope();
			}
		}
		return scope;
	}

	public ICPPClassType mapToAST(ICPPClassType type) {
		if (type instanceof ICPPTemplateInstance) {
			ICPPTemplateInstance inst = (ICPPTemplateInstance) type;
			ICPPTemplateDefinition template = inst.getTemplateDefinition();
			if (template instanceof IIndexBinding && template instanceof ICPPClassType) {
				IBinding mapped = mapToAST((ICPPClassType) template);
				if (mapped != template && mapped instanceof ICPPClassType) {
					mapped = CPPTemplates.instantiate((ICPPClassTemplate) mapped, inst.getTemplateArguments());
					if (mapped instanceof ICPPClassType)
						return (ICPPClassType) mapped;
				}
			}
			return type;
		}

		if (fClasses == null) {
			fClasses = new CharArrayMap<>();
			fTu.accept(new Visitor());
		}
		IASTName[] names = fClasses.get(type.getNameCharArray());
		if (names != null) {
			for (IASTName name : names) {
				if (name == null)
					break;
				IBinding b = name.resolveBinding();
				if (b instanceof ICPPClassType) {
					final ICPPClassType mapped = (ICPPClassType) b;
					if (mapped.isSameType(type)) {
						return mapped;
					}
				}
			}
		}
		return type;
	}

	public void recordPartialSpecialization(ICPPClassTemplatePartialSpecialization indexSpec,
			ICPPClassTemplatePartialSpecialization astSpec) {
		fPartialSpecs.put(indexSpec, astSpec);
	}

	public ICPPClassTemplatePartialSpecialization mapToAST(ICPPClassTemplatePartialSpecialization indexSpec) {
		ICPPClassTemplatePartialSpecialization astSpec = fPartialSpecs.get(indexSpec);
		if (astSpec != null) {
			return astSpec;
		}
		return indexSpec;
	}
}
