/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;

/**
 * C++-specific implementation of a translation-unit.
 */
public class CPPASTTranslationUnit extends ASTTranslationUnit implements ICPPASTTranslationUnit, IASTAmbiguityParent {
    private CPPNamespaceScope fScope = null;
    private ICPPNamespace fBinding = null;
	private CPPScopeMapper fScopeMapper= new CPPScopeMapper(this);
	
	public CPPASTTranslationUnit() {
	}
	
	public CPPASTTranslationUnit copy() {
		CPPASTTranslationUnit copy = new CPPASTTranslationUnit();
		copyAbstractTU(copy);
		return copy;
	}
	
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
        // void *
        IType cpp_void_p = new GPPPointerType(new CPPQualifierType(new CPPBasicType(Kind.eVoid, 0), false, false), new GPPASTPointer());
        // size_t // assumed: unsigned long int
        IType cpp_size_t = new CPPBasicType(Kind.eInt, IBasicType.IS_LONG & IBasicType.IS_UNSIGNED);

		// void * operator new (std::size_t);
        IBinding temp = null;
        IType[] newParms = new IType[1];
        newParms[0] = cpp_size_t;
        ICPPFunctionType newFunctionType = new CPPFunctionType(cpp_void_p, newParms);
        ICPPParameter[] newTheParms = new ICPPParameter[1];
        newTheParms[0] = new CPPBuiltinParameter(newParms[0]);
        temp = new CPPImplicitFunction(OverloadableOperator.NEW.toCharArray(), theScope, newFunctionType, newTheParms, false);
        theScope.addBinding(temp);
		
		// void * operator new[] (std::size_t);
		temp = null;
        temp = new CPPImplicitFunction(OverloadableOperator.NEW_ARRAY.toCharArray(), theScope, newFunctionType, newTheParms, false);
        theScope.addBinding(temp);
		
		// void operator delete(void*);
        temp = null;
        IType[] deleteParms = new IType[1];
        deleteParms[0] = cpp_void_p;
        ICPPFunctionType deleteFunctionType = new CPPFunctionType(cpp_void, deleteParms);
        ICPPParameter[] deleteTheParms = new ICPPParameter[1];
        deleteTheParms[0] = new CPPBuiltinParameter(deleteParms[0]);
        temp = new CPPImplicitFunction(OverloadableOperator.DELETE.toCharArray(), theScope, deleteFunctionType, deleteTheParms, false);
        theScope.addBinding(temp);
		
		// void operator delete[](void*);
		temp = null;
        temp = new CPPImplicitFunction(OverloadableOperator.DELETE_ARRAY.toCharArray(), theScope, deleteFunctionType, deleteTheParms, false);
        theScope.addBinding(temp);
	}
	
    public IASTName[] getDeclarationsInAST(IBinding binding) {
        if (binding instanceof IMacroBinding) {
        	return getMacroDefinitionsInAST((IMacroBinding) binding);
        }
        return CPPVisitor.getDeclarations(this, binding);
    }

    public IASTName[] getDefinitionsInAST(IBinding binding) {
        if (binding instanceof IMacroBinding) {
        	return getMacroDefinitionsInAST((IMacroBinding) binding);
        }
    	IASTName[] names = CPPVisitor.getDeclarations(this, binding);
        for (int i = 0; i < names.length; i++) {
            if (!names[i].isDefinition())
                names[i] = null;
        }
    	// nulls can be anywhere, don't use trim()
        return (IASTName[])ArrayUtil.removeNulls(IASTName.class, names);
    }

    public IASTName[] getReferences(IBinding binding) {
        if (binding instanceof IMacroBinding) {
            return getMacroReferencesInAST((IMacroBinding) binding);
        }
        return CPPVisitor.getReferences(this, binding);
    }
    
    public IBinding resolveBinding() {
        if (fBinding == null)
            fBinding = new CPPNamespace(this);
        return fBinding;
    }
	
    @Deprecated
    public ParserLanguage getParserLanguage() {
        return ParserLanguage.CPP;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getLinkage()
	 */
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.ISkippedIndexedFilesListener#skippedFile(org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent)
	 */
	@Override
	public void skippedFile(int offset, InternalFileContent fileContent) {
		super.skippedFile(offset, fileContent);
		fScopeMapper.registerAdditionalDirectives(offset, fileContent.getUsingDirectives());
	}	
	
	// bug 217102: namespace scopes from the index have to be mapped back to the AST.
	public IScope mapToASTScope(IIndexScope scope) {
		return fScopeMapper.mapToASTScope(scope);
	}
	// bug 262719: class types from the index have to be mapped back to the AST.
	public ICPPClassType mapToAST(ICPPClassType binding) {
		return fScopeMapper.mapToAST(binding);
	}

	/**
	 * Stores directives from the index into this scope.
	 */
	public void handleAdditionalDirectives(ICPPNamespaceScope scope) {
		fScopeMapper.handleAdditionalDirectives(scope);
	}

	@Override
	public void resolveAmbiguities() {
		accept(new CPPASTAmbiguityResolver()); 
	}
}
