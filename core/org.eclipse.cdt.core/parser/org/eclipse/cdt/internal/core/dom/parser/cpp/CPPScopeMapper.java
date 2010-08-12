/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
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
			fContainer= container;
			fNominated= inline;
		}
		public IScope getContainingScope() {
			return fContainer;
		}
		public ICPPNamespaceScope getNominatedScope() throws DOMException {
			return fNominated;
		}
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
			fScope= scope;
			assert fScope instanceof IIndexScope;
		}

		public EScopeKind getKind() {
			return fScope.getKind();
		}

		public IBinding[] find(String name) throws DOMException {
			return fScope.find(name);
		}
		public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
			return fScope.getBinding(name, resolve);
		}
		public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) throws DOMException {
			return fScope.getBinding(name, resolve, acceptLocalBindings);
		}
		public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) throws DOMException {
			return fScope.getBindings(name, resolve, prefixLookup);
		}
		public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,	IIndexFileSet acceptLocalBindings) throws DOMException {
			return fScope.getBindings(name, resolve, prefixLookup, acceptLocalBindings);
		}
		public IScope getParent() throws DOMException {
			IScope parent= fScope.getParent();
			if (parent instanceof IIndexScope) {
				return mapToASTScope((IIndexScope) parent);
			}
			return fTu.getScope();
		}

		public IName getScopeName() throws DOMException {
			return fScope.getScopeName();
		}

		public void addUsingDirective(ICPPUsingDirective usingDirective) {
			initUsingDirectives();
			fUsingDirectives.add(usingDirective);
		}

		private void initUsingDirectives() {
			if (fUsingDirectives == null) {
				fUsingDirectives= new ArrayList<ICPPUsingDirective>(1);
				// Insert a using directive for every inline namespace
				for (ICPPInternalNamespaceScope inline: getInlineNamespaces()) {
					fUsingDirectives.add(new InlineNamespaceDirective(this, inline));
				}
			}
		}

		public ICPPUsingDirective[] getUsingDirectives() {
			initUsingDirectives();
			return fUsingDirectives.toArray(new ICPPUsingDirective[fUsingDirectives.size()]);
		}

		public ICPPNamespaceScope[] getEnclosingNamespaceSet() {
			if (fEnclosingNamespaceSet == null)
				return fEnclosingNamespaceSet= CPPNamespaceScope.computeEnclosingNamespaceSet(this);
			
			return fEnclosingNamespaceSet;
		}

		public boolean isInlineNamepace() {
			IIndexBinding binding = ((IIndexScope) fScope).getScopeBinding();
			if (binding instanceof ICPPNamespace && ((ICPPNamespace) binding).isInline())
				return true;
			
			return false;
		}

		public ICPPInternalNamespaceScope[] getInlineNamespaces() {
			// Obtain the inline namespaces from the index and map them to the ast
			ICPPNamespaceScope[] pre = fScope.getInlineNamespaces();
			ICPPInternalNamespaceScope[] result= new ICPPInternalNamespaceScope[pre.length];
			for (int i = 0; i < result.length; i++) {
				result[i]= (ICPPInternalNamespaceScope) mapToASTScope((IIndexScope) pre[i]);
			}
			return result;
		}
	}

	/**
	 * Wrapper for using directives from the index.
	 */
	private class UsingDirectiveWrapper implements ICPPUsingDirective {
		private final int fOffset;
		private final ICPPUsingDirective fDirective;

		public UsingDirectiveWrapper(int offset, ICPPUsingDirective ud) {
			fOffset= offset;
			fDirective= ud;
		}

		public IScope getContainingScope() {
			final IScope scope= fDirective.getContainingScope();
			if (scope == null) {
				return fTu.getScope();
			}
			return scope;
		}

		public ICPPNamespaceScope getNominatedScope() throws DOMException {
			return fDirective.getNominatedScope();
		}

		public int getPointOfDeclaration() {
			return fOffset;
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
						IASTName[] names= fClasses.get(nameChars);
						names= (IASTName[]) ArrayUtil.append(IASTName.class, names, name);
						fClasses.put(nameChars, names);
					}
					return PROCESS_CONTINUE;
				}
				return PROCESS_SKIP;
			} else if (declaration instanceof IASTASMDeclaration
					|| declaration instanceof IASTFunctionDefinition) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
	}
	
	private final HashMap<IIndexScope, IScope> fMappedScopes= new HashMap<IIndexScope, IScope>();
	private final HashMap<String, NamespaceScopeWrapper> fNamespaceWrappers= new HashMap<String, NamespaceScopeWrapper>();
	private final Map<String, List<UsingDirectiveWrapper>> fPerName= new HashMap<String, List<UsingDirectiveWrapper>>();
	private final CPPASTTranslationUnit fTu;
	protected CharArrayMap<IASTName[]> fClasses;


	public CPPScopeMapper(CPPASTTranslationUnit tu) {
		fTu= tu;
	}

	/**
	 * Register an additional list of using directives to be considered.
	 * @param offset the global offset at which the using directives are provided
	 * @param usingDirectives the list of additional directives.
	 */
	public void registerAdditionalDirectives(int offset, List<ICPPUsingDirective> usingDirectives) {
		if (!usingDirectives.isEmpty()) {
			for (ICPPUsingDirective ud : usingDirectives) {
				IScope container= ud.getContainingScope();
				try {
					final String name= getReverseQualifiedName(container);
					List<UsingDirectiveWrapper> list= fPerName.get(name);
					if (list == null) {
						list= new LinkedList<UsingDirectiveWrapper>();
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
			List<UsingDirectiveWrapper> candidates= fPerName.remove(qname);
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
		if (scope == tuscope || scope == null) {
			return "";    //$NON-NLS-1$
		}
		StringBuilder buf= new StringBuilder();
		IName scopeName = scope.getScopeName();
		if (scopeName != null) {
			buf.append(scopeName.getSimpleID());
		}
		scope= scope.getParent();
		while (scope != null && scope != tuscope) {
			buf.append(':');
			scopeName= scope.getScopeName();
			if (scopeName != null) {
				buf.append(scope.getScopeName().getSimpleID());
			}
			scope= scope.getParent();
		}
		return buf.toString();
	}

	/**
	 * Maps namespace scopes from the index back into the AST.
	 */
	public IScope mapToASTScope(IIndexScope scope) {
		if (scope == null) {
			return fTu.getScope();
		}
		if (scope instanceof ICPPNamespaceScope) {
			IScope result= fMappedScopes.get(scope);
			if (result == null) {
				result= fTu.getScope().findNamespaceScope(scope);
				if (result == null) {
					result= wrapNamespaceScope((ICPPNamespaceScope) scope);
				}
				fMappedScopes.put(scope, result);
			}
			return result;
		}
		return scope;
	}

	private IScope wrapNamespaceScope(ICPPNamespaceScope scope) {
		try {
			String rqname= getReverseQualifiedName(scope);
			NamespaceScopeWrapper result= fNamespaceWrappers.get(rqname);
			if (result == null) {
				result= new NamespaceScopeWrapper(getCompositeNamespaceScope(scope));
				fNamespaceWrappers.put(rqname, result);
			}
			return result;
		} catch (DOMException e) {
			assert false;	// index scopes don't throw dom-exceptions
			return null;
		}	
	}
	
	private ICPPNamespaceScope getCompositeNamespaceScope(ICPPNamespaceScope scope) throws DOMException {
		if (scope instanceof IIndexScope) {
			IIndexBinding binding= fTu.getIndex().adaptBinding(((IIndexScope) scope).getScopeBinding());
			if (binding instanceof ICPPNamespace) {
				scope= ((ICPPNamespace) binding).getNamespaceScope();
			}
		}
		return scope;
	}
	
	public ICPPClassType mapToAST(ICPPClassType type) {
		if (type instanceof ICPPTemplateInstance) {
			ICPPTemplateInstance inst= (ICPPTemplateInstance) type;
			ICPPTemplateDefinition template= inst.getTemplateDefinition();
			if (template instanceof IIndexBinding && template instanceof ICPPClassType) {
				IBinding mapped= mapToAST((ICPPClassType) template);
				if (mapped != template && mapped instanceof ICPPClassType) {
					mapped= CPPTemplates.instantiate((ICPPClassTemplate) mapped, inst.getTemplateArguments(), false);
					if (mapped instanceof ICPPClassType)
						return (ICPPClassType) mapped;
				}
			}
			return type;
		}
		
		if (fClasses == null) {
			fClasses= new CharArrayMap<IASTName[]>();
			fTu.accept(new Visitor());
		}
		IASTName[] names= fClasses.get(type.getNameCharArray());
		if (names != null) {
			for (IASTName name : names) {
				if (name == null)
					break;
				IBinding b= name.resolveBinding();
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
}
