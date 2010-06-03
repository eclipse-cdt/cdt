/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.jface.viewers.TreePath;

class ExpressionsChangedUpdateTester implements IElementUpdateTester {
    
    private final ExpressionsChangedEvent fEvent;
    
    public ExpressionsChangedUpdateTester(ExpressionsChangedEvent event) {
        fEvent = event;
    }

    public int getUpdateFlags(Object viewerInput, TreePath path) {
        // Check whether the element in the cache matches the expression manager element.
        Object element = path.getSegmentCount() == 0 ? viewerInput : path.getLastSegment();
        if (fEvent.getExpressionManagerElements().contains(element)) {
            return ExpressionsManualUpdatePolicy.FLUSH;
        }
        
        // If the expressions were modified, flush the entries which are under the 
        // given expression. To do that, check whether the element path contains one 
        // of the changed expressions.
        if (fEvent.getType().equals(ExpressionsChangedEvent.Type.CHANGED)) {
            for (int i = 0; i < path.getSegmentCount(); i++) {
                if (eventContainsElement(path.getSegment(i))) {
                    return ExpressionsManualUpdatePolicy.FLUSH;
                }
            }
        }
        return 0;
    }
    
    private boolean eventContainsElement(Object element) {
        if (element instanceof IAdaptable) {
            IExpression expression = (IExpression)((IAdaptable)element).getAdapter(IExpression.class);
            if (expression != null) {
                for (int i = 0; i < fEvent.getExpressions().length; i++) {
                    if (expression.equals(fEvent.getExpressions()[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean includes(IElementUpdateTester tester) {
        return tester instanceof ExpressionsChangedUpdateTester;
    }
    
    @Override
    public String toString() {
        return "(" + fEvent + ") update tester"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}