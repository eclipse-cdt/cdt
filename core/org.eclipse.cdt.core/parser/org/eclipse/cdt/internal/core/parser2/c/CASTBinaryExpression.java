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

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * @author jcamelon
 */
public class CASTBinaryExpression extends CASTNode implements
        IASTBinaryExpression {

    private int op;
    private IASTExpression operand1;
    private IASTExpression operand2;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTBinaryExpression#getOperator()
     */
    public int getOperator() {
        return op;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTBinaryExpression#getOperand1()
     */
    public IASTExpression getOperand1() {
        return operand1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTBinaryExpression#getOperand2()
     */
    public IASTExpression getOperand2() {
        return operand2;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTBinaryExpression#setOperator(int)
     */
    public void setOperator(int op) {
        this.op = op;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTBinaryExpression#setOperand1(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setOperand1(IASTExpression expression) {
        operand1 = expression;   
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTBinaryExpression#setOperand2(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setOperand2(IASTExpression expression) {
        operand2 = expression;
    }

}
