/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 31, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author aniefer
 */
public class CPPClassTemplate extends CPPTemplateDefinition implements
		ICPPClassTemplate, ICPPClassType, ICPPInternalClassType {
	
	/**
	 * @param decl
	 */
	public CPPClassTemplate(IASTName name) {
		super(name);
	}

	public ICPPTemplateInstance deferredInstance( IType [] arguments ){
		ICPPTemplateInstance instance = getInstance( arguments );
		if( instance == null ){
			instance = new CPPDeferredClassInstance( this, arguments );
			addInstance( arguments, instance );
		}
		return instance;
	}
	private void checkForDefinition(){
//		CPPClassType.FindDefinitionAction action = new FindDefinitionAction();
//		IASTNode node = CPPVisitor.getContainingBlockItem( getPhysicalNode() ).getParent();
//
//		node.accept( action );
//	    definition = action.result;
//		
//		if( definition == null ){
//			node.getTranslationUnit().accept( action );
//		    definition = action.result;
//		}
//		
		return;
	}
	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(){
	    if( definition != null ){
	        return (ICPPASTCompositeTypeSpecifier) definition.getParent();
	    }
	    return null;
	}
	/**
	 * @param templateParameter
	 * @return
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase [] getBases() {
		if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPBase [] { new CPPBaseClause.CPPBaseProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
		ICPPASTBaseSpecifier [] bases = getCompositeTypeSpecifier().getBaseSpecifiers();
		if( bases.length == 0 )
		    return ICPPBase.EMPTY_BASE_ARRAY;
		
		ICPPBase [] bindings = new ICPPBase[ bases.length ];
		for( int i = 0; i < bases.length; i++ ){
		    bindings[i] = new CPPBaseClause( bases[i] );
		}
		
		return bindings; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() throws DOMException {
	    if( definition == null ){
	        checkForDefinition();
	        if( definition == null ){
	            IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
	            return new IField [] { new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        }
	    }

		IField[] fields = getDeclaredFields();
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
            fields = (IField[]) ArrayUtil.addAll( IField.class, fields, bases[i].getBaseClass().getFields() );
        }
		return (IField[]) ArrayUtil.trim( IField.class, fields );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(java.lang.String)
	 */
	public IField findField(String name) throws DOMException {
		IBinding [] bindings = CPPSemantics.findBindings( getCompositeScope(), name, true );
		IField field = null;
		for ( int i = 0; i < bindings.length; i++ ) {
            if( bindings[i] instanceof IField ){
                if( field == null )
                    field = (IField) bindings[i];
                else {
                    IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                    return new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, name.toCharArray() );
                }
            }
        }
		return field;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	public ICPPField[] getDeclaredFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public ICPPMethod[] getMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public ICPPMethod[] getAllDeclaredMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public ICPPMethod[] getDeclaredMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
	 */
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
	 */
	public IBinding[] getFriends() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
	    if( definition == null )
	        checkForDefinition();
		if( definition != null ){
			ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) definition.getParent(); 
			return compSpec.getScope();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalClassType#getConversionOperators()
	 */
	public ICPPMethod[] getConversionOperators() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType( IType type ) {
        if( type == this )
            return true;
        if( type instanceof ITypedef )
            return ((ITypedef)type).isSameType( this );
        return false;
    }
}
