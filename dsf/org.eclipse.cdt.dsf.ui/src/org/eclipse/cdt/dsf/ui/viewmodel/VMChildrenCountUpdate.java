/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/** 
 * Helper class implementation of the {@link IChildrenCountUpdate} update object.
 * 
 * @see VMViewerUpdate
 * 
 * @since 1.0
 */
@SuppressWarnings("restriction")
public class VMChildrenCountUpdate extends VMViewerUpdate implements IChildrenCountUpdate {
    final private DataRequestMonitor<Integer> fCountRequestMonitor;
    
    public VMChildrenCountUpdate(IViewerUpdate clientUpdate, DataRequestMonitor<Integer> rm) {
        super(clientUpdate, rm);
        fCountRequestMonitor = rm;
    }

    public VMChildrenCountUpdate(IModelDelta delta, IPresentationContext presentationContext, DataRequestMonitor<Integer> rm) {
        super(delta, presentationContext, rm);
        fCountRequestMonitor = rm;
    }

    public VMChildrenCountUpdate(TreePath elementPath, Object viewerInput, IPresentationContext presentationContext, DataRequestMonitor<Integer> rm) {
        super(elementPath, viewerInput, presentationContext, rm);
        fCountRequestMonitor = rm;
    }

    public void setChildCount(int count) {
        fCountRequestMonitor.setData(count);
    }
    
    @Override
    public String toString() {
        return "VMChildrenCountUpdate: " + getElement(); //$NON-NLS-1$
    }
    
    @Override
    public void done() {
        assert isCanceled() || fCountRequestMonitor.getData() != null || !fCountRequestMonitor.isSuccess();
        super.done();
    }

}
