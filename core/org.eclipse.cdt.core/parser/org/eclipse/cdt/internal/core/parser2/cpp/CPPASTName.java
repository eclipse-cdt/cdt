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
package org.eclipse.cdt.internal.core.parser2.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * @author jcamelon
 */
public class CPPASTName extends CPPASTNode implements IASTName {

    private char[] name;
    private static final char[] EMPTY_CHAR_ARRAY = { };
    private IBinding binding = null;

    /**
     * @param name 
     */
    public CPPASTName(char [] name ) {
        this.name = name;
    }

    /**
     * 
     */
    public CPPASTName() {
        name = EMPTY_CHAR_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
     */
    public IBinding resolveBinding() {
    	if( binding == null )
    		binding = CPPVisitor.createBinding( this ); 
    	
        return binding;
    }
    
    protected void setBinding( IBinding binding ){
    	this.binding = binding;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if( name == EMPTY_CHAR_ARRAY ) return null;
        return new String( name );
    }
    
    public char[] toCharArray() {
    	return name;
    }
    
    public void setName( char [] name )
    {
        this.name = name;
    }

}
