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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

/**
 * @author jcamelon
 */
public class CPPASTQualifiedName extends CPPASTNode implements ICPPASTQualifiedName {

    /**
     * @param duple
     */
    public CPPASTQualifiedName() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
     */
    public IBinding resolveBinding() {
        // TODO Auto-generated method stub
        return null;
    }

    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if( names == null ) return null;
        removeNullNames();
        StringBuffer buffer = new StringBuffer();
        for( int i = 0; i < names.length; ++i )
        {
            String n = names[i].toString();
            if( n == null ) 
                return null;
            buffer.append( n );
            if( i != names.length - 1 )
                buffer.append( "::"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#addName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void addName(IASTName name) {
        if( names == null )
        {
            names = new IASTName[ DEFAULT_NAMES_LIST_SIZE ];
            currentIndex = 0;
        }
        if( names.length == currentIndex )
        {
            IASTName [] old = names;
            names = new IASTName[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                names[i] = old[i];
        }
        names[ currentIndex++ ] = name;
    }

    /**
     * @param decls2
     */
    private void removeNullNames() {
        int nullCount = 0; 
        for( int i = 0; i < names.length; ++i )
            if( names[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTName [] old = names;
        int newSize = old.length - nullCount;
        names = new IASTName[ newSize ];
        for( int i = 0; i < newSize; ++i )
            names[i] = old[i];
        currentIndex = newSize;
    }
    
    private int currentIndex = 0;    
    private IASTName [] names = null;
    private static final int DEFAULT_NAMES_LIST_SIZE = 4;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#getNames()
     */
    public List getNames() {
        if( names == null ) return Collections.EMPTY_LIST;
        removeNullNames();
        return Arrays.asList( names );
    }
    
    
}
