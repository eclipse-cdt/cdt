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
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CPPASTSimpleDeclaration extends CPPASTNode implements
        IASTSimpleDeclaration {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration#getDeclSpecifier()
     */
    public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }


    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration#getDeclarators()
     */
    public IASTDeclarator[] getDeclarators() {
        if( declarators == null ) return IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
        declarators = (IASTDeclarator[]) ArrayUtil.removeNullsAfter( IASTDeclarator.class, declarators, declaratorsPos );
        return declarators;
    }
    
    public void addDeclarator( IASTDeclarator d )
    {
    	if (d != null) {
    		declaratorsPos++;
    		declarators = (IASTDeclarator[]) ArrayUtil.append( IASTDeclarator.class, declarators, d );
    	}
    }
    
    private IASTDeclarator [] declarators = null;
    private int declaratorsPos=-1;
    private IASTDeclSpecifier declSpecifier;

    /**
     * @param declSpecifier The declSpecifier to set.
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpecifier) {
        this.declSpecifier = declSpecifier;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( declSpecifier != null ) if( !declSpecifier.accept( action ) ) return false;
        IASTDeclarator [] dtors = getDeclarators();
        for( int i = 0; i < dtors.length; i++ )
            if( !dtors[i].accept( action ) ) return false;
        return true;
    }

}
