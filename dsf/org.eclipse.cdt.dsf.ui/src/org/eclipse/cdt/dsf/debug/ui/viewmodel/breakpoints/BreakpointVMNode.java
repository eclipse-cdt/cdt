/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IMemento;

/**
 * Breakpiont VM Node which uses VM Contexts to populate breakpoint elements 
 * in the view.  Any actions or other selection listeners which depend on the 
 * breakpoint object will not work with these elements unless they use the 
 * adapter mechanism to obtain the breakpoints. 
 * 
 * @since 2.1
 */
public class BreakpointVMNode extends AbstractBreakpointVMNode implements IElementLabelProvider, IElementMementoProvider {

    public BreakpointVMNode(BreakpointVMProvider provider) {
        super(provider);
    }
    
    @Override
    protected Object createBreakpiontElement(IBreakpoint bp) {
        return new BreakpointVMContext(this, bp);
    }

    @Override
    public void update(ILabelUpdate[] updates) {
        Map<IElementLabelProvider, List<ILabelUpdate>> delegatesMap = new HashMap<IElementLabelProvider, List<ILabelUpdate>>(1,1);
        
        for (final ILabelUpdate update : updates) {
            final IBreakpoint bp = ((BreakpointVMContext)update.getElement()).getBreakpoint();
            IElementLabelProvider provider = (IElementLabelProvider)bp.getAdapter(IElementLabelProvider.class);
            if (provider == null) {
                update.done();
                continue;
            }
            
            List<ILabelUpdate> delegatesList = delegatesMap.get(provider);
            if (delegatesList == null) {
                delegatesList = new ArrayList<ILabelUpdate>(updates.length);
                delegatesMap.put(provider, delegatesList);
            }
            delegatesList.add(new ICheckUpdate() {
                @Override
                public void setChecked(boolean checked, boolean grayed) {
                    if (update instanceof ICheckUpdate) {
                        ((ICheckUpdate)update).setChecked(checked, grayed);
                    }
                }
                @Override
                public String[] getColumnIds() { return update.getColumnIds(); }
                @Override
                public void setLabel(String text, int columnIndex) { 
                	update.setLabel(text, columnIndex); 
                	}
                @Override
                public void setFontData(FontData fontData, int columnIndex) { update.setFontData(fontData, columnIndex); }
                @Override
                public void setImageDescriptor(ImageDescriptor image, int columnIndex) { update.setImageDescriptor(image, columnIndex); }
                @Override
                public void setForeground(RGB foreground, int columnIndex) { update.setForeground(foreground, columnIndex); }
                @Override
                public void setBackground(RGB background, int columnIndex) { update.setBackground(background, columnIndex); }
                @Override
                public IPresentationContext getPresentationContext() { return update.getPresentationContext(); }
                @Override
                public Object getElement() { return bp; }
                @Override
                public TreePath getElementPath() { return update.getElementPath().getParentPath().createChildPath(bp); }
                @Override
                public Object getViewerInput() { return update.getViewerInput(); }
                @Override
                public void setStatus(IStatus status) { update.setStatus(status); }
                @Override
                public IStatus getStatus() { return update.getStatus(); }
                @Override
                public void done() { update.done(); }
                @Override
                public void cancel() { update.cancel(); }
                @Override
                public boolean isCanceled() { return update.isCanceled(); }
            });
        }
        
        for (IElementLabelProvider provider : delegatesMap.keySet()) {
            List<ILabelUpdate> updatesList = delegatesMap.get(provider);
            provider.update(updatesList.toArray(new ILabelUpdate[updatesList.size()]));
        }
    }
    
    @Override
    public void encodeElements(IElementMementoRequest[] updates) {
        Map<IElementMementoProvider, List<IElementMementoRequest>> delegatesMap = new HashMap<IElementMementoProvider, List<IElementMementoRequest>>(1,1);
        
        for (final IElementMementoRequest update : updates) {
            final IBreakpoint bp = ((BreakpointVMContext)update.getElement()).getBreakpoint();
            IElementMementoProvider provider = (IElementMementoProvider)bp.getAdapter(IElementMementoProvider.class);
            if (provider == null) {
                update.done();
                continue;
            }
            
            List<IElementMementoRequest> delegatesList = delegatesMap.get(provider);
            if (delegatesList == null) {
                delegatesList = new ArrayList<IElementMementoRequest>(updates.length);
                delegatesMap.put(provider, delegatesList);
            }
            delegatesList.add(new IElementMementoRequest() {
                @Override
                public IMemento getMemento() { return update.getMemento(); }
                @Override
                public IPresentationContext getPresentationContext() { return update.getPresentationContext(); }
                @Override
                public Object getElement() { return bp; }
                @Override
                public TreePath getElementPath() { return update.getElementPath().getParentPath().createChildPath(bp); }
                @Override
                public Object getViewerInput() { return update.getViewerInput(); }
                @Override
                public void setStatus(IStatus status) { update.setStatus(status); }
                @Override
                public IStatus getStatus() { return update.getStatus(); }
                @Override
                public void done() { update.done(); }
                @Override
                public void cancel() { update.cancel(); }
                @Override
                public boolean isCanceled() { return update.isCanceled(); }
            });
        }
        
        for (IElementMementoProvider provider : delegatesMap.keySet()) {
            List<IElementMementoRequest> updatesList = delegatesMap.get(provider);
            provider.encodeElements(updatesList.toArray(new IElementMementoRequest[updatesList.size()]));
        }
    }
    
    @Override
    public void compareElements(IElementCompareRequest[] updates) {
        Map<IElementMementoProvider, List<IElementCompareRequest>> delegatesMap = new HashMap<IElementMementoProvider, List<IElementCompareRequest>>(1,1);
        
        for (final IElementCompareRequest update : updates) {
            final IBreakpoint bp = ((BreakpointVMContext)update.getElement()).getBreakpoint();
            IElementMementoProvider provider = (IElementMementoProvider)bp.getAdapter(IElementMementoProvider.class);
            if (provider == null) {
                update.done();
                continue;
            }
            
            List<IElementCompareRequest> delegatesList = delegatesMap.get(provider);
            if (delegatesList == null) {
                delegatesList = new ArrayList<IElementCompareRequest>(updates.length);
                delegatesMap.put(provider, delegatesList);
            }
            delegatesList.add(new IElementCompareRequest() {
                @Override
                public IMemento getMemento() { return update.getMemento(); }
                @Override
                public void setEqual(boolean equal) { update.setEqual(equal);}
                @Override
                public IPresentationContext getPresentationContext() { return update.getPresentationContext(); }
                @Override
                public Object getElement() { return bp; }
                @Override
                public TreePath getElementPath() { return update.getElementPath().getParentPath().createChildPath(bp); }
                @Override
                public Object getViewerInput() { return update.getViewerInput(); }
                @Override
                public void setStatus(IStatus status) { update.setStatus(status); }
                @Override
                public IStatus getStatus() { return update.getStatus(); }
                @Override
                public void done() { update.done(); }
                @Override
                public void cancel() { update.cancel(); }
                @Override
                public boolean isCanceled() { return update.isCanceled(); }
            });
        }
        
        for (IElementMementoProvider provider : delegatesMap.keySet()) {
            List<IElementCompareRequest> updatesList = delegatesMap.get(provider);
            provider.compareElements(updatesList.toArray(new IElementCompareRequest[updatesList.size()]));
        }
    }
    
}
