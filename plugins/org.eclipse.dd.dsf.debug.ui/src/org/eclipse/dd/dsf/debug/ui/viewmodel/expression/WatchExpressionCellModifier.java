/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.expression;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dd.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.jface.viewers.ICellModifier;

/**
 * 
 */
@SuppressWarnings("restriction")
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
        return IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(property) && getWatchExpression(element) != null; 
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

        IWatchExpression expression = getWatchExpression(element);
        if (expression != null && value instanceof String) {
            expression.setExpressionText((String)value);
        }        
    }

    private IWatchExpression getWatchExpression(Object element) {
        if (element instanceof IAdaptable) {
            return (IWatchExpression)((IAdaptable)element).getAdapter(IWatchExpression.class);
        }
        return null;
    }

}
