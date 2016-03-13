/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.memory;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlock;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IMemoryAddressInfo;
import org.eclipse.cdt.dsf.gdb.service.IMemoryAddressInfo.IGdbMemoryAddressInfoTypeRetrieval;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IMemoryBlock;

public class GdbMemoryBlockAddressInfoRetrieval implements IMemoryBlockAddressInfoRetrieval {

    private final DsfSession fSession;
    private final Set<IAddressInfoUpdateListener> fListeners;

    public GdbMemoryBlockAddressInfoRetrieval(DsfSession session) {
        fSession = session;
        fListeners = new HashSet<IAddressInfoUpdateListener>();
        fSession.getExecutor().execute(new DsfRunnable() {
            @Override
            public void run() {
                fSession.addServiceEventListener(GdbMemoryBlockAddressInfoRetrieval.this, null);
            }
        });
    }

    @Override
    public void getMemoryBlockAddressInfo(Object selection, final IMemoryBlock memBlock,
            final IGetMemoryBlockAddressInfoReq request) {
        IDMContext memBlockContext = null;
        if (memBlock instanceof DsfMemoryBlock) {
            memBlockContext = ((DsfMemoryBlock) memBlock).getContext();

            if (selection instanceof IDMVMContext) {
                IDMContext context = ((IDMVMContext) selection).getDMContext();
                final IFrameDMContext frameCtx = DMContexts.getAncestorOfType(context, IFrameDMContext.class);
                if (frameCtx != null) {
                    // Resolve container context of selection
                    IContainerDMContext selectedContainerCtx = DMContexts.getAncestorOfType(frameCtx,
                            IContainerDMContext.class);

                    // Resolve container context of memory block
                    IContainerDMContext memoryContainerCtx = DMContexts.getAncestorOfType(memBlockContext,
                            IContainerDMContext.class);

                    // Continue if the selected container matches the container for the memory context
                    if (memoryContainerCtx.equals(selectedContainerCtx)) {
                        fSession.getExecutor().execute(new DsfRunnable() {
                            @Override
                            public void run() {
                            	// Resolve the memory address info providers
                                final IMemoryAddressInfo service = resolveService(IMemoryAddressInfo.class);
                                IGdbMemoryAddressInfoTypeRetrieval[] infoTypeProviders = service
                                        .getMemoryAddressInfoProviders();
                                if (infoTypeProviders == null || infoTypeProviders.length == 0) {
                                    // No providers available
                                    request.done();
                                    return;
                                }

                                final CountingRequestMonitor crm = new CountingRequestMonitor(fSession.getExecutor(),
                                        null) {
                                    // mark the request done when all available infoTypeProviders have
                                    // returned its information
                                    @Override
                                    protected void handleCompleted() {
                                        request.done();
                                    };
                                };

                                for (final IGdbMemoryAddressInfoTypeRetrieval infoProvider : infoTypeProviders) {
                                    infoProvider.itemsRequest(frameCtx, memBlock,
                                            new DataRequestMonitor<IMemoryBlockAddressInfoItem[]>(
                                                    fSession.getExecutor(), crm) {
                                        @Override
                                        protected void handleCompleted() {
                                            if (isSuccess()) {
                                            	// Load the information from this provider
                                                request.setAddressInfoItems(infoProvider.getInfoType(), getData());
                                            } else {
                                                request.setStatus(getStatus());
                                            }
                                            crm.done();
                                        }
                                    });
                                }

                                crm.setDoneCount(infoTypeProviders.length);
                            }

                        });
                    }
                } else {
                    // The selection context does not match the block memory context, 
                    // Simply close the request
                    request.done();
                }
            }
        }
    }

    private <V> V resolveService(Class<V> type) {
        V service = null;
        if (fSession != null) {
            DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getDefault().getBundle().getBundleContext(),
                    fSession.getId());
            service = tracker.getService(type);
            tracker.dispose();
        }
        return service;
    }

    @Override
    public void registerAddressInfoUpdateListener(IAddressInfoUpdateListener listener) {
        fListeners.add(listener);
    }

    @Override
    public void removeAddressInfoUpdateListener(IAddressInfoUpdateListener listener) {
        fListeners.remove(listener);
    }
    
    @DsfServiceEventHandler
    public void eventDispatched(IRegisterChangedDMEvent e) {
        for (IAddressInfoUpdateListener listener : fListeners) {
            listener.handleAddressInfoUpdate(EventType.VALUE_CHANGED, null);
        }
    }
}
