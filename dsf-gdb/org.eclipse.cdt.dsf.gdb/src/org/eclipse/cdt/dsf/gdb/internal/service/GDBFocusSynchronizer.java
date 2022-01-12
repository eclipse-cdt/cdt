/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MITuple;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * This service keeps synchronized the CDT Debug View selection and GDB's
 * internal focus.
 *
 * To keep the Debug View selection synchronized to CDT's selection, the service keeps
 * track of what is the current GDB focus, by listening to the GDB MI notification
 * "=thread-selected". When this notification is received, the service orders a change
 * to CDT's Debug View selection to match, by sending an IGDBFocusChangedEvent.
 *
 * To keep GDB's focus synchronized to the Debug View selections, the UI listens to
 * platform 'Debug Selection changed' events, and then uses this service, to order GDB
 * to change focus to match the selection.
 *
 * Note: the mapping between the DV selection and GDB focus is not 1 to 1; there can
 * be multiple debug sessions at one time, all shown in the DV. There is however a single
 * effective DV selection. On the other end, each debug session has a dedicated instance
 * of GDB, having its own unique focus, at any given time. Also not all DV selections map
 * to a valid GDB focus.
 *
 * @since 5.2
 */
public class GDBFocusSynchronizer extends AbstractDsfService implements IGDBFocusSynchronizer, IEventListener {
	/** This service's opinion of what is the current GDB focus - it can be
	 * a process, thread or stack frame context */
	private IDMContext fCurrentGDBFocus;

	private IStack fStackService;
	private IGDBProcesses fProcesses;
	private IGDBControl fGdbcontrol;
	private CommandFactory fCommandFactory;

	// default initial values
	private static final String THREAD_ID_DEFAULT = "1"; //$NON-NLS-1$

	public GDBFocusSynchronizer(DsfSession session) {
		super(session);
	}

	private class GDBFocusChangedEvent extends AbstractDMEvent<IDMContext> implements IGDBFocusChangedEvent {
		public GDBFocusChangedEvent(IDMContext ctx) {
			super(ctx);
		}
	}

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(RequestMonitor requestMonitor) {
		// obtain reference to a few needed services
		fProcesses = getServicesTracker().getService(IGDBProcesses.class);
		fStackService = getServicesTracker().getService(IStack.class);
		fGdbcontrol = getServicesTracker().getService(IGDBControl.class);
		fCommandFactory = fGdbcontrol.getCommandFactory();

		register(new String[] { IGDBFocusSynchronizer.class.getName() }, new Hashtable<String, String>());

		fGdbcontrol.addEventListener(this);
		getSession().addServiceEventListener(this, null);

		// set a sane initial value for current GDB focus.
		// This value will be updated when the session has finished launching.
		// See updateContexts() below.
		fCurrentGDBFocus = createThreadContextFromThreadId(THREAD_ID_DEFAULT);
		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		fGdbcontrol.removeEventListener(this);
		getSession().removeServiceEventListener(this);

		unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	public void setFocus(final IDMContext[] focus, RequestMonitor rm) {
		assert focus != null;
		// new Debug View thread or stack frame selection
		IDMContext elem = focus[0];

		// new selection is a frame?
		if (elem instanceof IFrameDMContext) {
			final IFrameDMContext finalFrameCtx = (IFrameDMContext) elem;

			setFrameFocus(finalFrameCtx, new ImmediateRequestMonitor(rm) {
				@Override
				public void handleSuccess() {
					// update the current focus, to match new GDB focus
					fCurrentGDBFocus = finalFrameCtx;
					rm.done();
				}
			});
		}
		// new selection is a thread?
		else if (elem instanceof IMIExecutionDMContext) {
			final IMIExecutionDMContext finalThreadCtx = (IMIExecutionDMContext) elem;

			setThreadFocus(finalThreadCtx, new ImmediateRequestMonitor(rm) {
				@Override
				protected void handleSuccess() {
					// update the current focus, to match new GDB focus
					fCurrentGDBFocus = finalThreadCtx;
					rm.done();
				}
			});
		}
		// new selection is a process?
		else if (elem instanceof IMIContainerDMContext) {
			setProcessFocus((IMIContainerDMContext) elem, rm);
		} else {
			assert false;
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context to set focus to", //$NON-NLS-1$
					null)); //);
			return;
		}
	}

	protected void setProcessFocus(IMIContainerDMContext newProc, RequestMonitor rm) {
		if (newProc == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"GdbFocusSynchronizer unable to resolve process context for the selected element", null)); //$NON-NLS-1$
			return;
		}
		// There is no MI command to set the inferior.  We could use the CLI 'inferior' command, but it would then
		// generate a =thread-selected event, which would cause us to change the selection in the Debug view and
		// select the stack frame of the first thread, or the first thread itself if it is running.
		// That would prevent the user from being able to leave the selection to a process node.
		// What we can do instead, is tell GDB to select the first thread of that inferior, which is what
		// GDB would do anyway, but since we have an MI -thread-select command it will prevent GDB from
		// issuing a =thread-selected event.
		fProcesses.getProcessesBeingDebugged(newProc, new ImmediateDataRequestMonitor<IDMContext[]>(rm) {
			@Override
			protected void handleSuccess() {
				if (getData().length > 0) {
					IDMContext finalThread = getData()[0];
					if (finalThread instanceof IMIExecutionDMContext) {
						setThreadFocus((IMIExecutionDMContext) (finalThread), new ImmediateRequestMonitor(rm) {
							@Override
							protected void handleSuccess() {
								// update the current focus, to match new GDB focus
								fCurrentGDBFocus = finalThread;
								rm.done();
							}
						});
						return;
					}
					rm.done();
				} else {
					// If there are no threads, it probably implies the inferior is not running
					// e.g., an exited process.  In this case, we cannot set the thread, but it
					// then becomes safe to set the inferior using the CLI command since
					// there is no thread for that inferior and therefore no =thread-selected event
					String miInferiorId = newProc.getGroupId();
					// Remove the 'i' prefix
					String cliInferiorId = miInferiorId.substring(1, miInferiorId.length());
					ICommand<MIInfo> command = fCommandFactory.createCLIInferior(fGdbcontrol.getContext(),
							cliInferiorId);
					fGdbcontrol.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo>(rm) {
						@Override
						protected void handleSuccess() {
							// update the current focus, to match new GDB focus
							fCurrentGDBFocus = newProc;
							rm.done();
						}
					});
				}
			}
		});
	}

	protected void setThreadFocus(IMIExecutionDMContext newThread, RequestMonitor rm) {
		if (newThread == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"GdbFocusSynchronizer unable to resolve thread context for the selected element", null)); //$NON-NLS-1$
			return;
		}

		// Create a mi-thread-select and send the command
		ICommand<MIInfo> command = fCommandFactory.createMIThreadSelect(fGdbcontrol.getContext(),
				newThread.getThreadId());
		fGdbcontrol.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo>(rm));
	}

	protected void setFrameFocus(IFrameDMContext newFrame, RequestMonitor rm) {
		if (newFrame == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE,
					"GdbFocusSynchronizer unable to resolve frame context for the selected element", null)); //$NON-NLS-1$
			return;
		}

		// We must specify the thread for which we want to set the frame in the -stack-select-frame command
		IMIExecutionDMContext threadDmc = DMContexts.getAncestorOfType(newFrame, IMIExecutionDMContext.class);
		if (isThreadSuspended(threadDmc)) {
			// Create a mi-stack-select-frame and send the command
			ICommand<MIInfo> command = fCommandFactory.createMIStackSelectFrame(threadDmc, newFrame.getLevel());
			fGdbcontrol.queueCommand(command, new ImmediateDataRequestMonitor<MIInfo>(rm));
		} else {
			rm.done();
		}
	}

	private boolean isThreadSuspended(IExecutionDMContext ctx) {
		assert ctx != null;
		IRunControl runControl = getServicesTracker().getService(IRunControl.class);
		if (runControl != null) {
			return runControl.isSuspended(ctx);
		} else {
			return false;
		}
	}

	/**
	 * Parses gdb output for the =thread-selected notification.
	 * When this is detected, generate a DSF event to notify listeners
	 *
	 * example :
	 * =thread-selected,id="7",frame={level="0",addr="0x000000000041eab0",func="main",args=[]}
	 */
	@Override
	public void eventReceived(Object output) {
		for (MIOOBRecord oobr : ((MIOutput) output).getMIOOBRecords()) {
			if (oobr instanceof MINotifyAsyncOutput) {
				MINotifyAsyncOutput out = (MINotifyAsyncOutput) oobr;
				String miEvent = out.getAsyncClass();
				if ("thread-selected".equals(miEvent)) { //$NON-NLS-1$
					// extract tid
					MIResult[] results = out.getMIResults();
					String tid = null;
					String frameLevel = null;
					for (int i = 0; i < results.length; i++) {
						String var = results[i].getVariable();
						MIValue val = results[i].getMIValue();

						if (var.equals("frame") && val instanceof MITuple) { //$NON-NLS-1$
							// dig deeper to get the frame level
							MIResult[] res = ((MITuple) val).getMIResults();

							for (int j = 0; j < res.length; j++) {
								var = res[j].getVariable();
								val = res[j].getMIValue();

								if (var.equals("level")) { //$NON-NLS-1$
									if (val instanceof MIConst) {
										frameLevel = ((MIConst) val).getString();
									}
								}
							}
						} else {
							if (var.equals("id")) { //$NON-NLS-1$
								if (val instanceof MIConst) {
									tid = ((MIConst) val).getString();
								}
							}
						}
					}

					// tid should never be null
					assert (tid != null);
					if (tid == null) {
						return;
					}

					// update current focus
					if (frameLevel == null) {
						// thread running - current focus is a thread
						fCurrentGDBFocus = createThreadContextFromThreadId(tid);
						createAndDispatchGDBFocusChangedEvent();
					} else {
						// thread suspended - current focus is a stack frame
						int intFrameNum = 0;
						try {
							intFrameNum = Integer.parseInt(frameLevel);
						} catch (NumberFormatException e) {
							GdbPlugin.log(e);
						}
						String finalTid = tid;
						fStackService.getFrames(createThreadContextFromThreadId(finalTid), intFrameNum, intFrameNum,
								new ImmediateDataRequestMonitor<IFrameDMContext[]>() {
									@Override
									protected void handleCompleted() {
										if (isSuccess() && getData().length > 0) {
											fCurrentGDBFocus = getData()[0];
										} else {
											fCurrentGDBFocus = createThreadContextFromThreadId(finalTid);
										}
										createAndDispatchGDBFocusChangedEvent();
									}
								});
					}
				}
			}
		}
	}

	private void createAndDispatchGDBFocusChangedEvent() {
		assert fCurrentGDBFocus != null;

		fGdbcontrol.getSession().dispatchEvent(new GDBFocusChangedEvent(fCurrentGDBFocus), fGdbcontrol.getProperties());
	}

	/**
	 * Creates an execution context from a thread id
	 *
	 * @param tid The thread id on which the execution context is based
	 */
	private IMIExecutionDMContext createThreadContextFromThreadId(String tid) {
		assert tid != null;

		IContainerDMContext parentContainer = fProcesses.createContainerContextFromThreadId(fGdbcontrol.getContext(),
				tid);
		IProcessDMContext processDmc = DMContexts.getAncestorOfType(parentContainer, IProcessDMContext.class);
		IThreadDMContext threadDmc = fProcesses.createThreadContext(processDmc, tid);
		return fProcesses.createExecutionContext(parentContainer, threadDmc, tid);
	}

	@Override
	public void sessionSelected() {
		// get debug view to select this session's current thread/frame
		createAndDispatchGDBFocusChangedEvent();
	}

	@Override
	public IDMContext[] getFocus() {
		return new IDMContext[] { fCurrentGDBFocus };
	}

	@DsfServiceEventHandler
	public void updateContexts(DataModelInitializedEvent event) {
		// the debug session has finished launching - update the current focus
		// to something sane. i.e. thread1 or thread1->frame0

		IMIExecutionDMContext threadCtx = createThreadContextFromThreadId(THREAD_ID_DEFAULT);

		if (!isThreadSuspended(threadCtx)) {
			fCurrentGDBFocus = threadCtx;
		} else {
			fStackService.getTopFrame(threadCtx, new ImmediateDataRequestMonitor<IFrameDMContext>() {
				@Override
				protected void handleCompleted() {
					if (isSuccess()) {
						fCurrentGDBFocus = getData();
					} else {
						fCurrentGDBFocus = threadCtx;
					}
				}
			});
		}
	}
}
