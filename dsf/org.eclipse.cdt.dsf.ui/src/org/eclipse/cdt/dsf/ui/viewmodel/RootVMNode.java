/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
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
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * Default implementation of a root view model node.  This class may be sub-classed
 * to implement model-specific event handling.
 * 
 * @since 1.0
 */
public class RootVMNode extends AbstractVMNode implements IRootVMNode {

    public RootVMNode(AbstractVMProvider provider) {
        super(provider);
    }

    public void update(IChildrenUpdate[] updates) {
        throw new UnsupportedOperationException("Root view model node should never be queried for list of elements."); //$NON-NLS-1$
    }
    
    public void update(IChildrenCountUpdate[] updates) {
        throw new UnsupportedOperationException("Root view model node should never be queried for list of elements."); //$NON-NLS-1$
    }

    public void update(IHasChildrenUpdate[] updates) {
        throw new UnsupportedOperationException("Root view model node should never be queried for list of elements."); //$NON-NLS-1$
    }

    /** 
     * Default implementation does not examine the event and assumes that every
     * event should be processed to generate a delta.
     */
    public boolean isDeltaEvent(Object rootObject, Object event) {
        if (event instanceof ModelProxyInstalledEvent) {
            return rootObject.equals( ((ModelProxyInstalledEvent)event).getRootElement() ); 
        }
        return true;
    } 
    
    /**
     * Default implementation creates a delta assuming that the root layout node
     * is the input object into the view.  
     */
    public void createRootDelta(Object rootObject, Object event, final DataRequestMonitor<VMDelta> rm) {
        rm.setData(new VMDelta(rootObject, 0, IModelDelta.NO_CHANGE));
        rm.done();
    }
    
    
    public int getDeltaFlags(Object event) {
        return IModelDelta.NO_CHANGE;
    }
    
    public void buildDelta(Object event, VMDelta parent, int nodeOffset, RequestMonitor requestMonitor) {
        requestMonitor.done();
    }
}
