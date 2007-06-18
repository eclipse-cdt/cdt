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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.debug.DsfDebugPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;

public class DsfMemoryBlock extends PlatformObject implements IMemoryBlock 
{
    private final DsfMemoryBlockRetrieval fRetrieval;
    private final String fModelId;
    private final long fStartAddress;
    private final byte[] fBytes;
    
    DsfMemoryBlock(DsfMemoryBlockRetrieval retrieval, String modelId, long startAddress, byte[] bytes) {
        fRetrieval = retrieval;
        fModelId = modelId;
        fStartAddress = startAddress;
        fBytes = bytes;
    }
    
    public byte[] getBytes() throws DebugException {
        return fBytes;
    }

    public long getLength() {
        return fBytes.length;
    }

    public long getStartAddress() {
        return fStartAddress;
    }

    public void setValue(long offset, byte[] bytes) throws DebugException {
        throw new DebugException(new Status(IStatus.ERROR, DsfDebugPlugin.PLUGIN_ID, DebugException.NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
    }

    public boolean supportsValueModification() {
        return false;
    }

    public IDebugTarget getDebugTarget() {
        return null;
    }

    public ILaunch getLaunch() {
        return null;
    }

    public String getModelIdentifier() {
        return fModelId;
    }
    
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(DsfMemoryBlockRetrieval.class)) {
            return fRetrieval;
        }
        return super.getAdapter(adapter);
    }
}
