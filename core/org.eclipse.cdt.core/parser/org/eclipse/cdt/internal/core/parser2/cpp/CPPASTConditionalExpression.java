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

import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * @author jcamelon
 */
public class CPPASTConditionalExpression extends CPPASTNode implements
        IASTConditionalExpression {
    private IASTExpression condition;
    private IASTExpression negative;
    private IASTExpression postive;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#getLogicalConditionExpression()
     */
    public IASTExpression getLogicalConditionExpression() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#setLogicalConditionExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setLogicalConditionExpression(IASTExpression expression) {
        condition = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#getPositiveResultExpression()
     */
    public IASTExpression getPositiveResultExpression() {
        return postive;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#setPositiveResultExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setPositiveResultExpression(IASTExpression expression) {
        this.postive = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#getNegativeResultExpression()
     */
    public IASTExpression getNegativeResultExpression() {
        return negative;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTConditionalExpression#setNegativeResultExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setNegativeResultExpression(IASTExpression expression) {
        this.negative = expression;
    }

}
