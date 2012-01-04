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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.VMViewerUpdateTracing;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.internal.LoggingUtils;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/** 
 * Helper class implementation of the {@link IChildrenUpdate} update object.
 * 
 * @see VMViewerUpdate
 * 
 * @since 1.0
 */
public class VMChildrenUpdate extends VMViewerUpdate implements IChildrenUpdate {
    private final int fOffset;
    private final int fLength;
    protected final List<Object> fElements;
    
    public VMChildrenUpdate(IViewerUpdate clientUpdate, int offset, int length, 
        DataRequestMonitor<List<Object>> requestMonitor) 
    {
        super(clientUpdate, requestMonitor);
        fOffset = offset;
        fLength = length;
        fElements = length > 0 ? new ArrayList<Object>(length) : new ArrayList<Object>();
    }

    public VMChildrenUpdate(IModelDelta delta, IPresentationContext presentationContext, int offset, int length, 
        DataRequestMonitor<List<Object>> rm) 
    {
        super(delta, presentationContext, rm);
        fOffset = offset;
        fLength = length;
        fElements = length > 0 ? new ArrayList<Object>(length) : new ArrayList<Object>();
    }

    public VMChildrenUpdate(TreePath elementPath, Object viewerInput, IPresentationContext presentationContext, 
        int offset, int length, DataRequestMonitor<List<Object>> rm) 
    {
        super(elementPath, viewerInput, presentationContext, rm);
        fOffset = offset;
        fLength = length;
        fElements = length > 0 ? new ArrayList<Object>(length) : new ArrayList<Object>();
    }

    @Override
	public int getOffset() {
        return fOffset;
    }

    @Override
	public int getLength() {
        return fLength;
    }

    @Override
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
        return "VMChildrenUpdate:" + getElement() + " {"+ getOffset() + "->" + (getOffset() + getLength()) + "}";  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public void done() {
        @SuppressWarnings("unchecked")
        DataRequestMonitor<List<Object>> rm = (DataRequestMonitor<List<Object>>)getRequestMonitor();
        
        /* See https://bugs.eclipse.org/bugs/show_bug.cgi?id=202109
         * 
         * A flexible hierarchy bug/optimization causes query with incorrect
         * IChildrenUpdate[] array length.
         *
         * The problem manifests itself while deleting a register node. 
         * For example, if the register view displays:
         *     PC
         *     EAX
         *     EBX
         *     ECX
         *     EDX
         *  And EBX is deleted, forcing a refresh, the viewer will query 
         *  for IChildrenUpdate[5] and IChildrenCountUpdate at the same time.
         *  
         *  To avoid this problem do not generate an error if the list of 
         *  children is smaller than the list of requested indexes.  Also,
         *  do not check if any of the elements are null.
         */        
        rm.setData(fElements);
        
        // trace our result
        if (VMViewerUpdateTracing.DEBUG_VMUPDATES && !isCanceled() && VMViewerUpdateTracing.matchesFilterRegex(this.getClass())) {
        	StringBuilder str = new StringBuilder();
        	str.append(DsfPlugin.getDebugTime() + " " + LoggingUtils.toString(this) + " marked done; element = " + LoggingUtils.toString(getElement())); //$NON-NLS-1$ //$NON-NLS-2$
        	if (fElements != null && fElements.size() > 0) {
	            for (Object element : fElements) {
	                str.append("   " + LoggingUtils.toString(element) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ 
	            }
	            str.deleteCharAt(str.length()-1); // remove trailing \n
        	}
        	DsfUIPlugin.debug(str.toString());        	
        }
        
        super.done();
    }
    
}
