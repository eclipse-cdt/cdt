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
package org.eclipse.cdt.dsf.ui.viewmodel;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.VMViewerUpdateTracing;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.internal.LoggingUtils;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/** 
 * Helper class implementation of the {@link IHasChildrenUpdate} update object.
 * 
 * @see VMViewerUpdate
 * 
 * @since 1.0
 */
public class VMHasChildrenUpdate extends VMViewerUpdate implements IHasChildrenUpdate {

    final private DataRequestMonitor<Boolean> fHasElemsRequestMonitor;
    
    public VMHasChildrenUpdate(IViewerUpdate clientUpdate, DataRequestMonitor<Boolean> rm) {
        super(clientUpdate, rm);
        fHasElemsRequestMonitor = rm;
    }
    
    public VMHasChildrenUpdate(IModelDelta delta, IPresentationContext presentationContext, DataRequestMonitor<Boolean> rm) {
        super(delta, presentationContext, rm);
        fHasElemsRequestMonitor = rm;
    }

    public VMHasChildrenUpdate(TreePath elementPath, Object viewerInput, IPresentationContext presentationContext, DataRequestMonitor<Boolean> rm) {
        super(elementPath, viewerInput, presentationContext, rm);
        fHasElemsRequestMonitor = rm;        
    }

    public void setHasChilren(boolean hasChildren) {
        fHasElemsRequestMonitor.setData(hasChildren);
    }

    @Override
    public String toString() {
        return "VMHasChildrenUpdate: " + getElement(); //$NON-NLS-1$
    }
    
    @Override
    public void done() {
        assert isCanceled() || fHasElemsRequestMonitor.getData() != null || !fHasElemsRequestMonitor.isSuccess();

        // trace our result
        if (VMViewerUpdateTracing.DEBUG_VMUPDATES && !isCanceled() && VMViewerUpdateTracing.matchesFilterRegex(this.getClass())) {
        	final Boolean data = fHasElemsRequestMonitor.getData();
			DsfUIPlugin.debug(DsfPlugin.getDebugTime() + " " //$NON-NLS-1$
					+ LoggingUtils.toString(this) + " marked done; element = " //$NON-NLS-1$
					+ LoggingUtils.toString(getElement())
					+ "\n   has children = " //$NON-NLS-1$
					+ (data != null ? data.toString() : "<unset>"));  //$NON-NLS-1$
        }

        super.done();            
    }
}
