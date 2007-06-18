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

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.utils.Addr32;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.DsfDebugPlugin;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Implementation of memory access API of the Eclipse standard debug model.
 * <br/>Note: This is only a sample implementation intended as a starting point.
 */
public class DsfMemoryBlockRetrieval extends PlatformObject 
    implements IMemoryBlockRetrieval 
{
    private final String fModelId;
    private final DsfSession fSession;
    private final DsfExecutor fExecutor;
    private final IDMContext<?> fContext;
    private final ServiceTracker fServiceTracker;
    
    public DsfMemoryBlockRetrieval(String modelId, IDMContext<?> dmc) throws DebugException {
        fModelId = modelId;
        fContext = dmc;
        fSession = DsfSession.getSession(fContext.getSessionId());
        if (fSession == null) {
            throw new IllegalArgumentException("Session for context " + fContext + " is not active"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        fExecutor = fSession.getExecutor();
        String memoryServiceFilter = 
            "(&" +  //$NON-NLS-1$
            "(OBJECTCLASS=" + IMemory.class.getName() + ")" +   //$NON-NLS-1$//$NON-NLS-2$
            "(" + IDsfService.PROP_SESSION_ID + "=" + dmc.getSessionId() + ")" +  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            ")"; //$NON-NLS-1$
        BundleContext bundle = DsfDebugPlugin.getBundleContext();
        try {
            fServiceTracker = new ServiceTracker(bundle, bundle.createFilter(memoryServiceFilter), null);
        } catch (InvalidSyntaxException e) {
            throw new DebugException(new Status(IStatus.ERROR, DsfDebugPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Error creating service filter.", e)); //$NON-NLS-1$
        }
        fServiceTracker.open();
    }
    
    public IMemoryBlock getMemoryBlock(final long startAddress, final long length) throws DebugException {
        Query<DsfMemoryBlock> query = new Query<DsfMemoryBlock>() {
            @Override
            protected void execute(final DataRequestMonitor<DsfMemoryBlock> rm) {
                IMemory memoryService = (IMemory)fServiceTracker.getService();
                if (memoryService != null) {
                    final byte[] buf = new byte[(int)length];
                    memoryService.getMemory(
                        fContext, new Addr32(startAddress), 32, buf, 0, (int)length, 0, 
                        new RequestMonitor(fExecutor, rm) {
                            @Override
                            protected void handleOK() {
                                rm.setData(new DsfMemoryBlock(DsfMemoryBlockRetrieval.this, fModelId, startAddress, buf));
                                rm.done();
                            }
                        });
                }
                
            }
        };
        fExecutor.execute(query);
        try {
            return query.get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
        return null;
    }

    public boolean supportsStorageRetrieval() {
        return true;
    }
}
