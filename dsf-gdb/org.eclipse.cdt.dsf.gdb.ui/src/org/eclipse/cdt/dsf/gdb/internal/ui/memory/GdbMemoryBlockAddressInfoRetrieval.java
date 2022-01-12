/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.internal.memory.GdbMemoryAddressInfoRegistersRetrieval;
import org.eclipse.cdt.dsf.gdb.internal.memory.GdbMemoryAddressInfoVariablesRetrieval;
import org.eclipse.cdt.dsf.gdb.memory.IGdbMemoryAddressInfoTypeRetrieval;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IMemoryBlock;

public class GdbMemoryBlockAddressInfoRetrieval implements IMemoryBlockAddressInfoRetrieval {

	private final DsfSession fSession;
	private final Set<IAddressInfoUpdateListener> fListeners = new HashSet<>();

	public GdbMemoryBlockAddressInfoRetrieval(DsfSession session) {
		fSession = session;
		fSession.getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				fSession.addServiceEventListener(GdbMemoryBlockAddressInfoRetrieval.this, null);
			}
		});
	}

	protected IGdbMemoryAddressInfoTypeRetrieval[] resolveMemoryAddressInfoProviders() {
		return new IGdbMemoryAddressInfoTypeRetrieval[] { new GdbMemoryAddressInfoVariablesRetrieval(fSession),
				new GdbMemoryAddressInfoRegistersRetrieval(fSession) };
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
					if (memoryContainerCtx != null && memoryContainerCtx.equals(selectedContainerCtx)) {
						fSession.getExecutor().execute(new DsfRunnable() {
							@Override
							public void run() {
								// Resolve the memory address info providers
								IGdbMemoryAddressInfoTypeRetrieval[] infoTypeProviders = resolveMemoryAddressInfoProviders();
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
									}
								};

								for (final IGdbMemoryAddressInfoTypeRetrieval infoProvider : infoTypeProviders) {
									infoProvider.itemsRequest(frameCtx, memBlock,
											new DataRequestMonitor<IMemoryBlockAddressInfoItem[]>(
													fSession.getExecutor(), crm) {
												@Override
												protected void handleCompleted() {
													if (isSuccess()) {
														// Load the information from this provider
														request.setAddressInfoItems(infoProvider.getInfoType(),
																getData());
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
					} else {
						request.done();
					}
				} else {
					// The selection context does not match the block memory context,
					// Simply close the request
					request.done();
				}
			} else {
				request.done();
			}
		} else {
			request.done();
		}
	}

	// The GdbSessionAdapters class will call this method automatically when it cleans up
	public void dispose() {
		fListeners.clear();
	}

	@Override
	public void addAddressInfoUpdateListener(IAddressInfoUpdateListener listener) {
		synchronized (fListeners) {
			fListeners.add(listener);
		}
	}

	@Override
	public void removeAddressInfoUpdateListener(IAddressInfoUpdateListener listener) {
		synchronized (fListeners) {
			fListeners.remove(listener);
		}
	}

	@DsfServiceEventHandler
	public void eventDispatched(IRegisterChangedDMEvent e) {
		synchronized (fListeners) {
			for (IAddressInfoUpdateListener listener : fListeners) {
				listener.handleAddressInfoUpdate(EventType.VALUE_CHANGED, null);
			}
		}

	}

	@DsfServiceEventHandler
	public void eventDispatched(IExpressionChangedDMEvent e) {
		synchronized (fListeners) {
			for (IAddressInfoUpdateListener listener : fListeners) {
				listener.handleAddressInfoUpdate(EventType.VALUE_CHANGED, null);
			}
		}
	}
}
