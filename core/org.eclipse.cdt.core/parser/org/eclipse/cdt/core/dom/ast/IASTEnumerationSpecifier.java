/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast;


/**
 * @author jcamelon
 */
public interface IASTEnumerationSpecifier extends IASTDeclSpecifier {

    /**
     * @author jcamelon
     */
    public interface IASTEnumerator extends IASTNode {
        public static final IASTEnumerator[] EMPTY_ENUMERATOR_ARRAY = new IASTEnumerator[0];

        public static final ASTNodeProperty ENUMERATOR_NAME = new ASTNodeProperty( "Enumerator Name"); //$NON-NLS-1$
        public void setName( IASTName name );
        public IASTName getName();
        
        public static final ASTNodeProperty ENUMERATOR_VALUE = new ASTNodeProperty( "Enumerator Value"); //$NON-NLS-1$
        public void setValue( IASTExpression expression );
        public IASTExpression getValue();

    }
    
    public static final ASTNodeProperty ENUMERATOR = new ASTNodeProperty( "Enumerator" ); //$NON-NLS-1$
    public void addEnumerator( IASTEnumerator enumerator );
    public IASTEnumerator[] getEnumerators();
    
    public static final ASTNodeProperty ENUMERATION_NAME = new ASTNodeProperty( "Enum Name"); //$NON-NLS-1$
    public void setName( IASTName name );
    public IASTName getName();
    
}
