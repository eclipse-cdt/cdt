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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;

/** 
 * Helper class implementation of an update object to use with IElementContentProvider.
 * @see IElementContentProvider
 */
@SuppressWarnings("restriction")
public class VMElementsUpdate extends VMViewerUpdate implements IChildrenUpdate {
    private final int fOffset;
    private final int fLength;
    protected final List<Object> fElements;
    
    public VMElementsUpdate(IChildrenUpdate clientUpdate, int offset, int length, DataRequestMonitor<List<Object>> requestMonitor) {
        super(clientUpdate, requestMonitor);
        fOffset = offset;
        fLength = length;
        fElements = length > 0 ? new ArrayList<Object>(length) : new ArrayList<Object>();
    }

    public int getOffset() {
        return fOffset;
    }

    public int getLength() {
        return fLength;
    }

    public void setChild(Object element, int offset) {
        // Calculate the index in array based on configured offset.
        int idx = offset - (fOffset > 0 ? fOffset : 0);
        
        // To make sure that index is in valid range.
        if (idx < 0 || (fLength > 0 && idx >= fLength)) return;
        
        // Increase the list size if needed.
        ensureElementsSize(idx + 1);
        
        // Finally set the element in elements list.
        fElements.set(idx, element);
    }

    private void ensureElementsSize(int size) {
        while (fElements.size() < size) {
            fElements.add(null);
        }
    }
        
    @Override
    public String toString() {
        return "VMElementsUpdate for elements under parent = " + getElement() + ", in range " + getOffset() + " -> " + (getOffset() + getLength());  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
    }

    @Override
    public void done() {
        @SuppressWarnings("unchecked")
        DataRequestMonitor<List<Object>> rm = (DataRequestMonitor<List<Object>>)fRequestMonitor;
        if (fElements.size() == fLength || fLength == -1 ) {
            rm.setData(fElements);
        } else {
            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfService.REQUEST_FAILED, "Incomplete elements of updates", null)); //$NON-NLS-1$
        }
        super.done();
    }
    
}
