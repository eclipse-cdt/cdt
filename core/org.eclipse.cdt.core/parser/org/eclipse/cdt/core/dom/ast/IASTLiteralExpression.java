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

    /**
     * An integer literal e.g. 5
     */
    public static final int lk_integer_constant = 0;
    /**
     * A floating point literal e.g. 6.0
     */
    public static final int lk_float_constant = 1;
    /**
     * A char literal e.g. 'abc'
     */
    public static final int lk_char_constant = 2;
    /**
     * A string literal e.g. "abcdefg"
     */
    public static final int lk_string_literal = 3;
    /**
     * A constant defined for subclasses to extend from.  
     */
    public static final int lk_last = lk_string_literal;
 
    /**
     * Get the literal expression kind. 
     * 
     * @return int
     */
    public int getKind();
    /**
     * Set the literal expression kind.  
     * 
     * @param value int
     */
    public void setKind( int value );
    
    /**
     * Set the value of the literal expression.
     * 
     * @param value
     */
    public void setValue( String value );
    
}
