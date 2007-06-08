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
package org.eclipse.dd.dsf.debug.model;

import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.DsfDebugPlugin;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.osgi.util.tracker.ServiceTracker;

/**
 * 
 */
public class DsfMemoryBlockRetrieval implements IMemoryBlockRetrieval {
    
    private final IDMContext<?> fContext;
    private final ServiceTracker fServiceTracker;
    
    public DsfMemoryBlockRetrieval(IDMContext<?> dmc) {
        fContext = dmc;
        String memoryServiceFilter = 
            "(&" + 
            "(OBJECTCLASS=" + IMemory.class.getName() + ")" + 
            "(" + IDsfService.PROP_SESSION_ID + "=" + dmc.getSessionId() + ")" +
            ")";
        fServiceTracker = new ServiceTracker(DsfDebugPlugin.getBundleContext(), memoryServiceFilter, null);
        fServiceTracker.open();
    }
    
    public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
        return null;
    }

    public boolean supportsStorageRetrieval() {
        return false;
    }

}
