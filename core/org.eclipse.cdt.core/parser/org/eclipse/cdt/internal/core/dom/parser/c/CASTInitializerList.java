/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation */
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CASTInitializerList extends CASTNode implements
        IASTInitializerList {

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration#getDeclarators()
     */
    public IASTInitializer[] getInitializers() {
        if( initializers == null ) return IASTInitializer.EMPTY_INITIALIZER_ARRAY;
        initializers = (IASTInitializer[]) ArrayUtil.removeNullsAfter( IASTInitializer.class, initializers, initializersPos );
        return initializers;
    }
    
    public void addInitializer( IASTInitializer d )
    {
    	if (d != null) {
    		initializersPos++;
    		initializers = (IASTInitializer[]) ArrayUtil.append( IASTInitializer.class, initializers, d );	
    	}
    }
    
    private IASTInitializer [] initializers = null;
    private int initializersPos=-1;

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
