/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIMemory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Memory service that uses the enhancements from GDB 7.6:
 * 	=memory-changed MI event
 *
 * @since 4.2
 */
public class GDBMemory_7_6 extends GDBMemory_7_0 implements IEventListener {

	private ICommandControlService fConnection;

	public GDBMemory_7_6(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			public void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {
		register(new String[] { MIMemory.class.getName(), IMemory.class.getName(), IGDBMemory.class.getName(),
				IGDBMemory2.class.getName(), GDBMemory.class.getName(), GDBMemory_7_0.class.getName(),
				GDBMemory_7_6.class.getName() }, new Hashtable<String, String>());

		fConnection = getServicesTracker().getService(ICommandControlService.class);
		if (fConnection == null) {
			requestMonitor
					.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "CommandControl Service is not available")); //$NON-NLS-1$
			return;
		}
		fConnection.addEventListener(this);

		requestMonitor.done();
	}

	@Override
	public void shutdown(final RequestMonitor requestMonitor) {
		fConnection.removeEventListener(this);
		unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	public void eventReceived(Object output) {
		if (output instanceof MIOutput) {
			MIOOBRecord[] records = ((MIOutput) output).getMIOOBRecords();
			for (MIOOBRecord r : records) {
				if (r instanceof MINotifyAsyncOutput) {
					MINotifyAsyncOutput notifyOutput = (MINotifyAsyncOutput) r;
					String asyncClass = notifyOutput.getAsyncClass();
					// These events have been added with GDB 7.6
					if ("memory-changed".equals(asyncClass)) { //$NON-NLS-1$
						String groupId = null;
						String addr = null;
						int count = 0;

						MIResult[] results = notifyOutput.getMIResults();
						for (int i = 0; i < results.length; i++) {
							String var = results[i].getVariable();
							MIValue val = results[i].getMIValue();
							if (var.equals("thread-group")) { //$NON-NLS-1$
								if (val instanceof MIConst) {
									groupId = ((MIConst) val).getString();
								}
							} else if (var.equals("addr")) { //$NON-NLS-1$
								if (val instanceof MIConst) {
									addr = ((MIConst) val).getString();
								}
							} else if (var.equals("len")) { //$NON-NLS-1$
								if (val instanceof MIConst) {
									try {
										String lenStr = ((MIConst) val).getString().trim();
										// count is expected in addressable units
										if (lenStr.startsWith("0x")) { //$NON-NLS-1$
											count = Integer.parseInt(lenStr.substring(2), 16);
										} else {
											count = Integer.parseInt(lenStr);
										}
									} catch (NumberFormatException e) {
										assert false;
									}
								}
							} else if (var.equals("type")) { //$NON-NLS-1$
								if (val instanceof MIConst) {
									if ("code".equals(((MIConst) val).getString())) { //$NON-NLS-1$
									}
								}
							}
						}

						IMIProcesses procService = getServicesTracker().getService(IMIProcesses.class);
						if (procService != null && groupId != null && addr != null && count > 0) {
							IContainerDMContext containerDmc = procService
									.createContainerContextFromGroupId(fConnection.getContext(), groupId);

							// Now refresh our memory cache, it case it contained this address.  Don't have
							// it send the potential IMemoryChangedEvent as we will send it ourselves (see below).
							final IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(containerDmc,
									IMemoryDMContext.class);

							final IAddress address = new Addr64(addr);
							getMemoryCache(memoryDMC).refreshMemory(memoryDMC, address, 0,
									getAddressableSize(memoryDMC), count, false,
									new RequestMonitor(getExecutor(), null) {
										@Override
										protected void handleCompleted() {
											// Only once the memory cache is updated, we send the IMemoryChangedEvent.  If we were to do it
											// earlier, the memory view may not show the updated value.
											//
											// We must always send this event when GDB reports a memory change because it can mean that
											// an expression or register has changed, and therefore we must notify the different views
											// and services of it.  We cannot rely on this event to be sent by the memory cache after being
											// refreshed, because if the memory cache does not contain this address, it will not send
											// the event.
											getSession().dispatchEvent(
													new MemoryChangedEvent(memoryDMC, new IAddress[] { address }),
													getProperties());
										}
									});
						}
					}
				}
			}
		}
	}
}
