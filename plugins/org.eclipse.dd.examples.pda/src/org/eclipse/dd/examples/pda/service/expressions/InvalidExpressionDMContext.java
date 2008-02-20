/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.expressions;

import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMContext;

class InvalidExpressionDMContext extends AbstractDMContext 
    implements IExpressionDMContext
{
    private final String expression;

    public InvalidExpressionDMContext(String sessionId, IDMContext parent, String expr) {
        super(sessionId, new IDMContext[] { parent });
        expression = expr;
    }

    @Override
    public boolean equals(Object other) {
        return super.baseEquals(other) && 
            expression == null 
                ? ((InvalidExpressionDMContext) other).getExpression() == null 
                : expression.equals(((InvalidExpressionDMContext) other).getExpression());
    }

    @Override
    public int hashCode() {
        return expression == null ? super.baseHashCode() : super.baseHashCode() ^ expression.hashCode();
    }

    @Override
    public String toString() {
        return baseToString() + ".invalid_expr[" + expression + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getExpression() {
        return expression;
    }
}