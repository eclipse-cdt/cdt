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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;

/**
 * @author jcamelon
 */
public class CPPASTExpressionStatement extends CPPASTNode implements
        IASTExpressionStatement {
    private IASTExpression expression;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTExpressionStatement#getExpression()
     */
    public IASTExpression getExpression() {
        return expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTExpressionStatement#setExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setExpression(IASTExpression expression) {
        this.expression = expression;
    }

}
