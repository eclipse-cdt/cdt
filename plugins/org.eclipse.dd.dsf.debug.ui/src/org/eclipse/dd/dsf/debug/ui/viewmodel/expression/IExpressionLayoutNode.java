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
package org.eclipse.dd.dsf.debug.ui.viewmodel.expression;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.jface.viewers.TreePath;

/**
 * Interface for layout nodes that can be used within the expression view.  
 * The methods of this interface allow the {@link ExpressionManagerLayoutNode} 
 * to use this layout node to delegate expression parsing to this node, and to 
 * generate deltas for expressions that are owned by this node.
 */
@SuppressWarnings("restriction")
public interface IExpressionLayoutNode extends IVMLayoutNode {
    
    /**
     * Returns the length of the portion of the expression that can be parsed 
     * by this node.    
     * @param expression String to parse
     * @return length of the expression recognized by this node.  Length of less than 1
     * indicates that this node cannot parse this expression.
     */
    int getExpressionLength(String expression);
    
    /**
     * Retrieves the element for the given expression.  The node implementing 
     * this method should parse the expression and set a valid view model 
     * context (VMC) element in the update provided as an argument.
     * @param update to fill in with the element.  The tree path in this update 
     * object may contain elements which are not actually displayed in the viewer.  
     * These element may have been added to the original path by other expression 
     * layout nodes that have parsed preceding parts of the expression.  
     * @param expressionText expression string to parse
     * @param expression expression object that the returned element should contain 
     */
    void getElementForExpression(IChildrenUpdate update, String expressionText, IExpression expression);

    int getDeltaFlagsForExpression(String expressionText, Object event);
    
    void buildDeltaForExpression(IExpression expression, int elementIdx, String expressionText, Object event, 
                                 VMDelta parentDelta, TreePath path, RequestMonitor rm); 
}
