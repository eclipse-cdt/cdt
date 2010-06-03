/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel;

import org.eclipse.core.runtime.Platform;

/**
 * Implementation of basic view model context interface.
 * <p>  The main purpose of the VMC wrapper is to re-direct adapter 
 * queries.  The redirecting of adapter queries follows this order:
 * <ol>
 * <li>If context implements the adapter itself, it is returned.</li>
 * <li>If the VM Adapter implements the adapter, the VM Adapter is returned.</li>
 * <li>If the VM Provider implements the adapter, the VM Provider is returned.</li>
 * <li>If the VM Node implements the adapter, the VM Node is returned.</li>
 * </ol>
 * </p> 
 * <p> 
 * Note: Deriving classes must override the Object.equals/hashCode methods.
 * This is because the view model context objects are just wrappers that are 
 * created by the view model on demand, so the equals methods must use the 
 * object being wrapped to perform a meaningful comparison.    
 * 
 * @since 1.0
 */
abstract public class AbstractVMContext implements IVMContext {
    protected final IVMNode fNode;
    
    public AbstractVMContext(IVMNode node) {
        fNode = node;
    }
    
    public IVMNode getVMNode() { return fNode; }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        // If the context implements the given adapter directly, it always takes
        // precedence.
        if (adapter.isInstance(this)) {
            return this;
        }
        
        IVMProvider vmProvider = getVMNode().getVMProvider();
        IVMAdapter vmAdapter = vmProvider.getVMAdapter();
        if (adapter.isInstance(vmAdapter)) {
            return vmAdapter;
        } else if (adapter.isInstance(vmProvider)) {
            return vmProvider;
        } else if (adapter.isInstance(getVMNode())) {
            return getVMNode();
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /** Deriving classes must override. */
    @Override
    abstract public boolean equals(Object obj);
    
    /** Deriving classes must override. */
    @Override
    abstract public int hashCode();
}