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
package org.eclipse.cdt.core.dom.ast;

/**
 * This expression represents a literal in the program.
 * 
 * @author Doug Schaefer
 */
public interface IASTLiteralExpression extends IASTExpression {

    public static final int lk_integer_constant = 0;
    public static final int lk_float_constant = 1;
    public static final int lk_char_constant = 2;
    public static final int lk_string_literal = 3;
    public static final int lk_last = lk_string_literal;
 
    public int getKind();
    public void setKind( int value );
    
    public void setValue( String value );
    
}
