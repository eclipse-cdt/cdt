/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CPPASTDeclarator extends CPPASTNode implements IASTDeclarator {
    private IASTInitializer initializer;
    private IASTName name;
    private IASTDeclarator nestedDeclarator;
    private IASTPointerOperator [] pointerOps = null;


    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getPointerOperators()
     */
    public IASTPointerOperator[] getPointerOperators() {
        if( pointerOps == null ) return IASTPointerOperator.EMPTY_ARRAY;
        return (IASTPointerOperator[]) ArrayUtil.removeNulls( IASTPointerOperator.class, pointerOps );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getNestedDeclarator()
     */
    public IASTDeclarator getNestedDeclarator() {
        return nestedDeclarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getInitializer()
     */
    public IASTInitializer getInitializer() {
        return initializer;
    }

    /**
     * @param initializer
     */
    public void setInitializer(IASTInitializer initializer) {
        this.initializer = initializer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#addPointerOperator(org.eclipse.cdt.core.dom.ast.IASTPointerOperator)
     */
    public void addPointerOperator(IASTPointerOperator operator) {
        pointerOps = (IASTPointerOperator[]) ArrayUtil.append( IASTPointerOperator.class, pointerOps, operator );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#setNestedDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
     */
    public void setNestedDeclarator(IASTDeclarator nested) {
        this.nestedDeclarator = nested;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarators ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        IASTPointerOperator [] ptrOps = getPointerOperators();
        for ( int i = 0; i < ptrOps.length; i++ ) {
            if( !ptrOps[i].accept( action ) ) return false;
        }
        
        if( getPropertyInParent() != IASTTypeId.ABSTRACT_DECLARATOR &&
    		nestedDeclarator == null )
		{
            if( name != null ) if( !name.accept( action ) ) return false;
		}
        
        if( nestedDeclarator != null ) if( !nestedDeclarator.accept( action ) ) return false;
        
        return postAccept( action );
    }
    
    protected boolean postAccept( ASTVisitor action ){
        if( initializer != null ) if( !initializer.accept( action ) ) return false;
        return true;
    }


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName n) {
        IASTNode getParent = getParent();
        if( getParent instanceof IASTDeclaration )
        {
            if( getParent instanceof IASTFunctionDefinition )
                return r_definition;
            if( getParent instanceof IASTSimpleDeclaration )
            {
                IASTSimpleDeclaration sd = (IASTSimpleDeclaration) getParent;
                if( sd.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_extern )
                    return r_declaration;
                if( getInitializer() == null )
                    return r_declaration;
            }
            return r_definition;
        }
        if( getParent instanceof IASTTypeId )
            return r_reference;
        if( getParent instanceof IASTDeclarator )
        {
            IASTNode t = getParent;
            while ( t instanceof IASTDeclarator )
                t = t.getParent();
            if( t instanceof IASTDeclaration )
            {
                if( getParent instanceof IASTFunctionDefinition )
                    return r_definition;
                if( getParent instanceof IASTSimpleDeclaration )
                {
                    IASTSimpleDeclaration sd = (IASTSimpleDeclaration) getParent;
                    if( sd.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_extern )
                        return r_declaration;
                    if( getInitializer() != null )
                        return r_definition;
                }
                return r_definition;                    
            }
            if( t instanceof IASTTypeId )
                return r_reference;
        }
        return r_unclear;
	}
}
