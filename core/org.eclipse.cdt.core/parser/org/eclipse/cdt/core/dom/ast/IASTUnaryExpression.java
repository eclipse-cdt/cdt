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
public interface IASTUnaryExpression extends IASTExpression {

    public static final int op_prefixIncr = 0;
    public static final int op_prefixDecr = 1;
    public static final int op_plus       = 2;
    public static final int op_minus      = 3;
    public static final int op_star       = 4;
    public static final int op_amper      = 5;
    public static final int op_tilde      = 6;
    public static final int op_not        = 7;
    public static final int op_sizeof     = 8;
    public static final int op_postFixIncr = 9;
    public static final int op_postFixDecr = 10;
    public static final int op_last       = op_postFixDecr;
    
    public int getOperator();
    public void setOperator( int value );
    
    public static final ASTNodeProperty OPERAND = new ASTNodeProperty( "Operand" ); //$NON-NLS-1$
    
    
    public IASTExpression getOperand();
    public void setOperand( IASTExpression expression );
    
}
