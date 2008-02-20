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
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;

/**
 * 
 */
public class ExpressionDMContext extends AbstractDMContext implements IExpressionDMContext {

    private final String fExpression;
    
    ExpressionDMContext(String sessionId, IFrameDMContext frameDmc, String expressin) {
        super(sessionId, new IDMContext[] { frameDmc });
        fExpression = expressin;
    }

    public String getExpression() { 
        return fExpression;
    }
    
    @Override
    public boolean equals(Object other) {
        return super.baseEquals(other) && ((ExpressionDMContext)other).fExpression.equals(fExpression);
    }
    
    @Override
    public int hashCode() {
        return super.baseHashCode() + fExpression.hashCode();
    }
    
    @Override
    public String toString() { 
        return baseToString() + ".expression(" + fExpression + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }
}
