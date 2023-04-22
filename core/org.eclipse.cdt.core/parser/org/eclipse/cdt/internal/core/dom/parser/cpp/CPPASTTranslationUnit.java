/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPInheritance.FinalOverriderMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeInstantiationRequest;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;

/**
 * C++-specific implementation of a translation-unit.
 */
public class CPPASTTranslationUnit extends ASTTranslationUnit implements ICPPASTTranslationUnit, IASTAmbiguityParent {
	private CPPNamespaceScope fScope;
	private ICPPNamespace fBinding;
	private final CPPScopeMapper fScopeMapper;
	private CPPASTAmbiguityResolver fAmbiguityResolver;

	// Caches.
	private final Map<ICPPClassType, FinalOverriderMap> fFinalOverriderMapCache = new HashMap<>();
	// Cache for type instantiations. This is currently only used for instantiations of
	// alias template instances, but its use could potentially be expanded to cover other
	// instantiations. Note that class template instances are already cached by the
	// template definition, so we wouldn't want to double-cache those. (But we could e.g.
	// cache instantiations of function types if we found it worthwhile.)
	private final Map<TypeInstantiationRequest, IType> fInstantiationCache = new HashMap<>();

	public CPPASTTranslationUnit() {
		fScopeMapper = new CPPScopeMapper(this);
	}

	@Override
	public CPPASTTranslationUnit copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTTranslationUnit copy(CopyStyle style) {
		CPPASTTranslationUnit copy = new CPPASTTranslationUnit();
		return copy(copy, style);
	}

	@Override
	public CPPNamespaceScope getScope() {
		if (fScope == null) {
			fScope = new CPPNamespaceScope(this);
			addBuiltinOperators(fScope);
		}
		return fScope;
	}

	private void addBuiltinOperators(CPPScope theScope) {
		// void
		IType cpp_void = new CPPBasicType(Kind.eVoid, 0);
		// void*
		IType cpp_void_p = new CPPPointerType(new CPPQualifierType(new CPPBasicType(Kind.eVoid, 0), false, false),
				new CPPASTPointer());
		// size_t // assumed: unsigned long int
		IType cpp_size_t = new CPPBasicType(Kind.eInt, IBasicType.IS_LONG & IBasicType.IS_UNSIGNED);

		// void* operator new(std::size_t);
		IBinding temp = null;
		IType[] newParms = new IType[1];
		newParms[0] = cpp_size_t;
		ICPPFunctionType newFunctionType = new CPPFunctionType(cpp_void_p, newParms, null);
		ICPPParameter[] newTheParms = new ICPPParameter[1];
		newTheParms[0] = new CPPBuiltinParameter(newParms[0]);
		temp = new CPPImplicitFunction(OverloadableOperator.NEW.toCharArray(), theScope, newFunctionType, newTheParms,
				false, false);
		theScope.addBinding(temp);

		// void* operator new[](std::size_t);
		temp = null;
		temp = new CPPImplicitFunction(OverloadableOperator.NEW_ARRAY.toCharArray(), theScope, newFunctionType,
				newTheParms, false, false);
		theScope.addBinding(temp);

		// void operator delete(void*);
		temp = null;
		IType[] deleteParms = new IType[1];
		deleteParms[0] = cpp_void_p;
		ICPPFunctionType deleteFunctionType = new CPPFunctionType(cpp_void, deleteParms, null);
		ICPPParameter[] deleteTheParms = new ICPPParameter[1];
		deleteTheParms[0] = new CPPBuiltinParameter(deleteParms[0]);
		temp = new CPPImplicitFunction(OverloadableOperator.DELETE.toCharArray(), theScope, deleteFunctionType,
				deleteTheParms, false, false);
		theScope.addBinding(temp);

		// void operator delete[](void*);
		temp = null;
		temp = new CPPImplicitFunction(OverloadableOperator.DELETE_ARRAY.toCharArray(), theScope, deleteFunctionType,
				deleteTheParms, false, false);
		theScope.addBinding(temp);
	}

	@Override
	public IASTName[] getDeclarationsInAST(IBinding binding) {
		if (binding instanceof IMacroBinding) {
			return getMacroDefinitionsInAST((IMacroBinding) binding);
		}
		return CPPVisitor.getDeclarations(this, binding);
	}

	@Override
	public IASTName[] getDefinitionsInAST(IBinding binding) {
		return getDefinitionsInAST(binding, false);
	}

	@Override
	public IASTName[] getDefinitionsInAST(IBinding binding, boolean permissive) {
		if (binding instanceof IMacroBinding) {
			return getMacroDefinitionsInAST((IMacroBinding) binding);
		}
		IASTName[] names = CPPVisitor.getDeclarations(this, binding, permissive);
		for (int i = 0; i < names.length; i++) {
			if (!names[i].isDefinition())
				names[i] = null;
		}
		// nulls can be anywhere, don't use trim()
		return ArrayUtil.removeNulls(IASTName.class, names);
	}

	@Override
	public IASTName[] getReferences(IBinding binding) {
		if (binding instanceof IMacroBinding) {
			return getMacroReferencesInAST((IMacroBinding) binding);
		}
		return CPPVisitor.getReferences(this, binding);
	}

	@Override
	public ICPPNamespace getGlobalNamespace() {
		if (fBinding == null)
			fBinding = new CPPNamespace(this);
		return fBinding;
	}

	@Override
	@Deprecated
	public IBinding resolveBinding() {
		return getGlobalNamespace();
	}

	@Override
	@Deprecated
	public ParserLanguage getParserLanguage() {
		return ParserLanguage.CPP;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public void skippedFile(int offset, InternalFileContent fileContent) {
		super.skippedFile(offset, fileContent);
		fScopeMapper.registerAdditionalDirectives(offset, fileContent.getUsingDirectives());
	}

	@Override
	public IScope mapToASTScope(IScope scope) {
		if (scope instanceof IIndexScope) {
			return fScopeMapper.mapToASTScope((IIndexScope) scope);
		}
		return scope;
	}

	/**
	 * Maps a class type to the AST.
	 *
	 * @param binding a class type, possibly from index
	 * @return the corresponding class in the AST, or the original class type if it doesn't have
	 *     a counterpart in the AST.
	 */
	public ICPPClassType mapToAST(ICPPClassType binding) {
		return fScopeMapper.mapToAST(binding);
	}

	/**
	 * Stores directives from the index into this scope.
	 */
	public void handleAdditionalDirectives(ICPPNamespaceScope scope) {
		fScopeMapper.handleAdditionalDirectives(scope);
	}

	private class ProblemBindingClearer extends ASTVisitor {
		public ProblemBindingClearer() {
			shouldVisitNames = true;
		}

		@Override
		public int visit(IASTName name) {
			if (name.getBinding() instanceof IProblemBinding) {
				name.setBinding(null);
			}
			return PROCESS_CONTINUE;
		}
	}

	@Override
	public void resolveAmbiguities() {
		fAmbiguityResolver = new CPPASTAmbiguityResolver();
		accept(fAmbiguityResolver);
		// During ambiguity resolution, names can incorrectly get stuck with ProblemBindings.
		// To prevent this, clear all ProblemBindings here, allowing name resolution for
		// the affected names to be attempted again with a fully ambiguity-resolved AST.
		accept(new ProblemBindingClearer());
		fAmbiguityResolver = null;
	}

	@Override
	protected IType createType(IASTTypeId typeid) {
		return CPPVisitor.createType(typeid);
	}

	@Override
	public void resolvePendingAmbiguities(IASTNode node) {
		if (fAmbiguityResolver != null) {
			fAmbiguityResolver.resolvePendingAmbiguities(node);
		}
	}

	public Map<ICPPClassType, FinalOverriderMap> getFinalOverriderMapCache() {
		return fFinalOverriderMapCache;
	}

	public Map<TypeInstantiationRequest, IType> getInstantiationCache() {
		return fInstantiationCache;
	}

	public void recordPartialSpecialization(ICPPClassTemplatePartialSpecialization indexSpec,
			ICPPClassTemplatePartialSpecialization astSpec) {
		fScopeMapper.recordPartialSpecialization(indexSpec, astSpec);
	}

	public ICPPClassTemplatePartialSpecialization mapToAST(ICPPClassTemplatePartialSpecialization indexSpec) {
		return fScopeMapper.mapToAST(indexSpec);
	}
}
