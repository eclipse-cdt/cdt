/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.timers;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.cdt.examples.dsf.timers.TimersVMProvider.TimersViewLayoutChanged;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * 
 */
@SuppressWarnings("restriction")
public class TimersRootVMNode extends RootDMVMNode {

    public TimersRootVMNode(AbstractVMProvider provider) {
        super(provider);
    }

    @Override
    public boolean isDeltaEvent(Object rootObject, Object e) {
        if (e instanceof TimersViewLayoutChanged) {
            return true;
        }
        return super.isDeltaEvent(rootObject, e);
    }
    
    @Override
    public int getDeltaFlags(Object e) {
        if (e instanceof TimersViewLayoutChanged) {
            return IModelDelta.CONTENT;
        }
        
        return IModelDelta.NO_CHANGE;
    }

    @Override
    public void createRootDelta(Object rootObject, Object event, final DataRequestMonitor<VMDelta> rm) {
        int flags = IModelDelta.NO_CHANGE;
        if (event instanceof TimersViewLayoutChanged) {
            flags |= IModelDelta.CONTENT;
        }
        rm.setData( new VMDelta(rootObject, 0, flags) );
        rm.done();
    }

}
