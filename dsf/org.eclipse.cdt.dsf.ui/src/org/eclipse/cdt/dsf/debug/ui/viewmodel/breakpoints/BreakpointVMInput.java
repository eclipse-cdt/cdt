/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;

/**
 * @since 2.1
 */
public class BreakpointVMInput extends PlatformObject implements IElementMementoProvider {

    private IVMNode fVMNode;
    
    public BreakpointVMInput(IVMNode node, IDMContext activeDMContext) {
        fVMNode = node;
    }

    public void encodeElements(IElementMementoRequest[] requests) {
        for (IElementMementoRequest request : requests) {
            request.getMemento().putString("ELEMENT_NAME", "BreakpointInputMemento");  //$NON-NLS-1$//$NON-NLS-2$
            request.done();
        }
    }
    
    public void compareElements(IElementCompareRequest[] requests) {
        for (IElementCompareRequest request : requests) {
            request.setEqual( "BreakpointInputMemento".equals(request.getMemento().getString("ELEMENT_NAME")) );  //$NON-NLS-1$//$NON-NLS-2$
            request.done();
        }
    }
    
    @Override
    @SuppressWarnings({"rawtypes" })
    public Object getAdapter(Class adapter) {
        // If the context implements the given adapter directly, it always takes
        // precedence.
        if (adapter.isInstance(this)) {
            return this;
        }
        
        IVMProvider vmProvider = fVMNode.getVMProvider();
        IVMAdapter vmAdapter = vmProvider.getVMAdapter();
        if (adapter.isInstance(vmAdapter)) {
            return vmAdapter;
        } else if (adapter.isInstance(vmProvider)) {
            return vmProvider;
        } else if (adapter.isInstance(fVMNode)) {
            return fVMNode;
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BreakpointVMInput;
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode(); 
    }
}
