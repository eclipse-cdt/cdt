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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;

/**
 * @author jcamelon
 */
public class CPPASTLinkageSpecification extends CPPASTNode implements
        ICPPASTLinkageSpecification {

    private String literal;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification#getLiteral()
     */
    public String getLiteral() {
        return literal;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification#setLiteral(java.lang.String)
     */
    public void setLiteral(String value) {
        this.literal = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification#getDeclarations()
     */
    public List getDeclarations() {
        if( declarations == null ) return Collections.EMPTY_LIST;
        removeNullDeclarations();
        return Arrays.asList( declarations );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void addDeclaration(IASTDeclaration declaration) {
        if( declarations == null )
        {
            declarations = new IASTDeclaration[ DEFAULT_DECLARATIONS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( declarations.length == currentIndex )
        {
            IASTDeclaration [] old = declarations;
            declarations = new IASTDeclaration[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                declarations[i] = old[i];
        }
        declarations[ currentIndex++ ] = declaration;
    }

    private void removeNullDeclarations() {
        int nullCount = 0; 
        for( int i = 0; i < declarations.length; ++i )
            if( declarations[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTDeclaration [] old = declarations;
        int newSize = old.length - nullCount;
        declarations = new IASTDeclaration[ newSize ];
        for( int i = 0; i < newSize; ++i )
            declarations[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTDeclaration [] declarations = null;
    private static final int DEFAULT_DECLARATIONS_LIST_SIZE = 4;

}
