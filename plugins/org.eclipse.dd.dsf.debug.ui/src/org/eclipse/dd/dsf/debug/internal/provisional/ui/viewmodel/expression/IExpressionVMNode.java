/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.jface.viewers.TreePath;

/**
 * Interface for view model nodes that can be used within the expression view.  
 * The methods of this interface allow the {@link ExpressionManagerVMNode} 
 * to use this node to delegate expression parsing to this node, and to 
 * generate deltas for expressions that are owned by this node.
 */
public interface IExpressionVMNode extends IVMNode {
    
    /**
     * Returns whether the given expression node recognizes and can parse the given 
     * expression.
     * @param expression Expression that needs to be parsed.
     * @return true if expression can be parsed
     */
    public boolean canParseExpression(IExpression expression);
    
    /**
     * Asynchronously fills in the given expression update.
     * @param update Update to complete.
     */
    public void update(IExpressionUpdate update);
    
    /**
     * Returns the flags that this node can generate for the given expression and 
     * event.
     */
    public int getDeltaFlagsForExpression(IExpression expression, Object event);
    
    /**
     * Adds delta flags to the given parent delta based on the expression object 
     * given.  
     */
    public void buildDeltaForExpression(IExpression expression, int elementIdx, Object event, VMDelta parentDelta, 
        TreePath path, RequestMonitor rm);
    
    /**
     * Adds delta to the given parent delta based on the given element which was created base on 
     * an expression parsed by this node.
     */
    public void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm);
}
