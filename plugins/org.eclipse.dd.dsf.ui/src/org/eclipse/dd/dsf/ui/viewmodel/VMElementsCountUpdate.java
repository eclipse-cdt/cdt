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
package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/** 
 * Helper class implementation of an update object to use with IElementContentProvider.
 * @see IElementContentProvider
 */
@SuppressWarnings("restriction")
public class VMElementsCountUpdate extends VMViewerUpdate implements IChildrenCountUpdate {
    final private DataRequestMonitor<Integer> fCountRequestMonitor;
    
    public VMElementsCountUpdate(IViewerUpdate clientUpdate, DataRequestMonitor<Integer> rm) {
        super(clientUpdate, rm);
        fCountRequestMonitor = rm;
    }

    public void setChildCount(int count) {
        fCountRequestMonitor.setData(count);
    }
    
    @Override
    public void done() {
        assert isCanceled() || fCountRequestMonitor.getData() != null || !fCountRequestMonitor.getStatus().isOK();
        super.done();
    }

}
