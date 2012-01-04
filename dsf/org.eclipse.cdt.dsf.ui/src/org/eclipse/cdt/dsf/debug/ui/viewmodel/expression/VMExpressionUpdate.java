/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.VMViewerUpdateTracing;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.internal.LoggingUtils;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.VMViewerUpdate;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/**
 * 
 */
class VMExpressionUpdate extends VMViewerUpdate implements IExpressionUpdate {

    private final IExpression fExpression;
    private Object fExpressionElement;
    
    public VMExpressionUpdate(IViewerUpdate clientUpdate, IExpression expression, DataRequestMonitor<Object> rm) 
    {
        super(clientUpdate, rm);
        fExpression = expression;
    }

    public VMExpressionUpdate(IModelDelta delta, IPresentationContext presentationContext, IExpression expression, DataRequestMonitor<Object> rm) 
    {
        super(delta, presentationContext, rm);
        fExpression = expression;
    }
    
    public VMExpressionUpdate(TreePath elementPath, Object viewerInput, IPresentationContext presentationContext, IExpression expression, DataRequestMonitor<Object> rm) 
    {
        super(elementPath, viewerInput, presentationContext, rm);
        fExpression = expression;
    }


    @Override
	public IExpression getExpression() {
        return fExpression;
    }


    @Override
	public void setExpressionElement(Object element) {
        fExpressionElement = element;
    }

    @Override
    public String toString() {
        return "VMExpressionUpdate for elements under parent = " + getElement() + ", in for expression " + getExpression().getExpressionText();  //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void done() {
        @SuppressWarnings("unchecked")
        
        DataRequestMonitor<Object> rm = (DataRequestMonitor<Object>)getRequestMonitor();
        if (fExpressionElement != null) {
            rm.setData(fExpressionElement);
        } else if (rm.isSuccess()) {
            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Incomplete elements of updates", null)); //$NON-NLS-1$
        }
        
        // trace our result
        if (VMViewerUpdateTracing.DEBUG_VMUPDATES && !isCanceled() && VMViewerUpdateTracing.matchesFilterRegex(this.getClass())) {
			DsfUIPlugin.debug(DsfPlugin.getDebugTime()
				+ " " + LoggingUtils.toString(this) + " marked done; element = " + LoggingUtils.toString(getElement()) + //$NON-NLS-1$ //$NON-NLS-2$
				"\n   expression = " //$NON-NLS-1$
				+ (fExpressionElement != null ? LoggingUtils.toString(fExpressionElement) : "<unset>"));  //$NON-NLS-1$
        }
        
        super.done();
    }
}
