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
public interface IASTElaboratedTypeSpecifier extends IASTDeclSpecifier {

    public static final int k_struct = 0;
    public static final int k_union  = 1;
    public static final int k_enum   = 2;
    
    public int   getKind();
    public void  setKind( int value );
    
    public static final ASTNodeProperty TYPE_NAME = new ASTNodeProperty( "Type Name"); //$NON-NLS-1$
    public IASTName getName();
    public void setName( IASTName name );
    
}
