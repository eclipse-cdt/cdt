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
        removeNullDeclarators();
        return declarators;
    }
    
    public void addDeclarator( IASTDeclarator d )
    {
        if( declarators == null )
        {
            declarators = new IASTDeclarator[ DEFAULT_DECLARATORS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( declarators.length == currentIndex )
        {
            IASTDeclarator [] old = declarators;
            declarators = new IASTDeclarator[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                declarators[i] = old[i];
        }
        declarators[ currentIndex++ ] = d;
    }
    
    private void removeNullDeclarators() {
        int nullCount = 0; 
        for( int i = 0; i < declarators.length; ++i )
            if( declarators[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTDeclarator [] old = declarators;
        int newSize = old.length - nullCount;
        declarators = new IASTDeclarator[ newSize ];
        for( int i = 0; i < newSize; ++i )
            declarators[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTDeclarator [] declarators = null;
    private static final int DEFAULT_DECLARATORS_LIST_SIZE = 2;
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
