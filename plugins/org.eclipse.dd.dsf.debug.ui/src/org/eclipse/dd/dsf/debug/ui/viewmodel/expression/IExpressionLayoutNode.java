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
 * 
 */
@SuppressWarnings("restriction")
public interface IExpressionLayoutNode extends IVMLayoutNode {
    int getExpressionLength(String expression);
    void getElementForExpression(IChildrenUpdate update, String expressionText, IExpression expression);
    
    int getDeltaFlagsForExpression(String expressionText, Object event); 
    void buildDeltaForExpression(IExpression expression, int elementIdx, String expressionText, Object event, 
                                 VMDelta parentDelta, TreePath path, RequestMonitor rm); 
}
