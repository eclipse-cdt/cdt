/*******************************************************************************
 *  Copyright (c) 2009 Wind River Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.debug.core.model.IExpression;

/**
 * Object representing a change in configured expressions.  This event is 
 * object is used when generating a model delta.
 */
public class ExpressionsChangedEvent {

    /**
     * Enumeration for the type of change in expressions. 
     * @since 2.2
     */
    public enum Type {ADDED, CHANGED, REMOVED, MOVED, INSERTED}

    private final Set<Object> fExpressionManagerElements;
    private final ExpressionsChangedEvent.Type fType;
    private final IExpression[] fExpressions;
    private final int fIndex;
    
    public ExpressionsChangedEvent(ExpressionsChangedEvent.Type type, Set<Object> expressionManagerElements, 
        IExpression[] expressions, int index) 
    {
        fExpressionManagerElements = expressionManagerElements;
        fType = type;
        fExpressions = expressions; 
        fIndex = index;
    }
    
    /**
     * The set of root elements of the expressions view model.
     */
    public Set<Object> getExpressionManagerElements() { return fExpressionManagerElements; }
    
    /**
     * Returns the type of change.
     */
    public ExpressionsChangedEvent.Type getType() { return fType; }
    
    /**
     * Returns expressions affected by the change.
     */
    public IExpression[] getExpressions() { return fExpressions; }
    
    /**
     * Returns index of the affected expression.
     */
    public int getIndex() { return fIndex; }
    
    @Override
    public String toString() {
        return Arrays.asList(fExpressions).toString() + " " + fType + "@" + fIndex; //$NON-NLS-1$ //$NON-NLS-2$
    }
}