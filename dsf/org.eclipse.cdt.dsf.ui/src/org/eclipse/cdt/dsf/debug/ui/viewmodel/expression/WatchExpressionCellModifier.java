/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionManagerVMNode.NewExpressionVMC;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.jface.viewers.ICellModifier;

/**
 * 
 */
@ThreadSafeAndProhibitedFromDsfExecutor("")
public class WatchExpressionCellModifier implements ICellModifier {

    /**
     * Constructor for the modifier requires a valid DSF session in order to 
     * initialize the service tracker.  
     * @param session DSF session this modifier will use.
     */
    public WatchExpressionCellModifier() {
    }

    public boolean canModify(Object element, String property) {
        return IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(property) && 
               (getWatchExpression(element) != null  || element instanceof NewExpressionVMC); 
    }

    public Object getValue(Object element, String property) {
        if (!IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(property)) return ""; //$NON-NLS-1$

        IWatchExpression expression = getWatchExpression(element);
        
        if (expression != null) {
            return expression.getExpressionText();
        }
        return ""; //$NON-NLS-1$
    }

    public void modify(Object element, String property, Object value) {
        if (!IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(property)) return;
        if (!(value instanceof String)) return;
        
        String origStrValue = (String) value;
        String strValue = origStrValue.trim();
        IWatchExpression expression = getWatchExpression(element);
        IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager(); 
        if (expression != null) {
            if (strValue.length() != 0) {
                expression.setExpressionText(origStrValue);
            } else {
                // (bug 233111) If user entered a blank string, remove the expression.
                expressionManager.removeExpression(expression);
            }
        } else if (element instanceof NewExpressionVMC && strValue.length() != 0) {
            IWatchExpression watchExpression = expressionManager.newWatchExpression(origStrValue); 
            expressionManager.addExpression(watchExpression);            
        }
    }

    private IWatchExpression getWatchExpression(Object element) {
        if (element instanceof IAdaptable) {
            return (IWatchExpression)((IAdaptable)element).getAdapter(IWatchExpression.class);
        }
        return null;
    }

}
