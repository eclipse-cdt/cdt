/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2.c;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * @author jcamelon
 */
public class CASTUnaryExpression extends CASTNode implements
        IASTUnaryExpression {

    private int operator;
    private IASTExpression operand;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryExpression#getOperator()
     */
    public int getOperator() {
        return operator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryExpression#setOperator(int)
     */
    public void setOperator(int value) {
        this.operator = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryExpression#getOperand()
     */
    public IASTExpression getOperand() {
        return operand;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryExpression#setOperand(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setOperand(IASTExpression expression) {
        operand = expression;
    }

}
