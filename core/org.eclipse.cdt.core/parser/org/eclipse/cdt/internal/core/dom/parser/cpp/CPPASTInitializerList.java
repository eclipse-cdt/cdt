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
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;

/**
 * @author jcamelon
 */
public class CPPASTInitializerList extends CPPASTNode implements
        IASTInitializerList {

    private int currentIndex = 0;
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration#getDeclarators()
     */
    public IASTInitializer [] getInitializers() {
        if( initializers == null ) return IASTInitializer.EMPTY_INIALIZER_ARRAY;
        removeNullInitializers();
        return initializers;
    }
    
    public void addInitializer( IASTInitializer d )
    {
        if( initializers == null )
        {
            initializers = new IASTInitializer[ DEFAULT_INITIALIZERS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( initializers.length == currentIndex )
        {
            IASTInitializer [] old = initializers;
            initializers = new IASTInitializer[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                initializers[i] = old[i];
        }
        initializers[ currentIndex++ ] = d;

    }
    
    /**
     * @param decls2
     */
    private void removeNullInitializers() {
        int nullCount = 0; 
        for( int i = 0; i < initializers.length; ++i )
            if( initializers[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTInitializer [] old = initializers;
        int newSize = old.length - nullCount;
        initializers = new IASTInitializer[ newSize ];
        for( int i = 0; i < newSize; ++i )
            initializers[i] = old[i];
        currentIndex = newSize;
    }

    
    private IASTInitializer [] initializers = null;
    private static final int DEFAULT_INITIALIZERS_LIST_SIZE = 4;

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitInitializers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        IASTInitializer [] list = getInitializers();
        for ( int i = 0; i < list.length; i++ ) {
            if( !list[i].accept( action ) ) return false;
        }
        return true;
    }

}
