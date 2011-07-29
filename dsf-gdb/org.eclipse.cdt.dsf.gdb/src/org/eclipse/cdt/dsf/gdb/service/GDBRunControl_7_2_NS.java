/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo.IThreadGroupInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo.IThreadGroupInfo2;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThread;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Version of the non-stop runControl for GDB 7.2.
 * 
 * @since 4.0
 */
public class GDBRunControl_7_2_NS extends GDBRunControl_7_0_NS
{

	private ICommandControlService fConnection;
	private CommandFactory fCommandFactory;
	private IGDBProcesses fProcService;

	///////////////////////////////////////////////////////////////////////////
	// Initialization and shutdown
	///////////////////////////////////////////////////////////////////////////

	public GDBRunControl_7_2_NS(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
		register(new String[]{ IRunControl.class.getName(), 
				IRunControl2.class.getName(),
				IMIRunControl.class.getName(),
				GDBRunControl_7_0_NS.class.getName(),
				GDBRunControl_7_2_NS.class.getName(),
		}, 
		new Hashtable<String,String>());
		fConnection = getServicesTracker().getService(ICommandControlService.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
		fProcService = getServicesTracker().getService(IGDBProcesses.class);
		getSession().addServiceEventListener(this, null);
		rm.done();
	}

	@Override
	public void shutdown(final RequestMonitor rm) {
		unregister();
		getSession().removeServiceEventListener(this);
		super.shutdown(rm);
	}

	// Now that the flag --thread-group is globally supported
	// by GDB 7.2, we have to make sure not to use it twice.
	// Bug 340262
	@Override
	public void suspend(final IExecutionDMContext context, final RequestMonitor rm) {
		assert context != null;

		IMIExecutionDMContext thread = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		IMIContainerDMContext container = DMContexts.getAncestorOfType(context, IMIContainerDMContext.class);
		if (thread == null && container == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Invalid context type.", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		canSuspend(context, new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), rm) {
			@Override
			protected void handleSuccess() {
				if (getData()) {
					fConnection.queueCommand(fCommandFactory.createMIExecInterrupt(context), new DataRequestMonitor<MIInfo>(getExecutor(), rm));
				} else {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
							"Given context: " + context + ", is already suspended.", null)); //$NON-NLS-1$ //$NON-NLS-2$
					rm.done();
				}
			}
		});
	}

	// Now that the flag --thread-group is globally supported
	// by GDB 7.2, we have to make sure not to use it twice.
	// Bug 340262
	@Override
	public void resume(final IExecutionDMContext context, final RequestMonitor rm) {
		assert context != null;

		final IMIExecutionDMContext thread = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
		final IMIContainerDMContext container = DMContexts.getAncestorOfType(context, IMIContainerDMContext.class);
		if (thread == null && container == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Invalid context type.", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		canResume(context, new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(), rm) {
			@Override
			protected void handleSuccess() {
				if (getData()) {
					if (thread != null) {
						doResumeThread(thread, rm);
						return;
					}

					if (container != null) {
						doResumeContainer(container, rm);
						return;
					}
				} else {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
							"Given context: " + context + ", is already running.", null)); //$NON-NLS-1$ //$NON-NLS-2$
					rm.done();
				}
			}
		});
	}

	private void doResumeThread(IMIExecutionDMContext context, final RequestMonitor rm) {
		final MIThreadRunState threadState = fThreadRunStates.get(context);
		if (threadState == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
                "Given context: " + context + " can't be found.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}
		
		threadState.fResumePending = true;
		fConnection.queueCommand(fCommandFactory.createMIExecContinue(context), new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
			@Override
			protected void handleFailure() {
				threadState.fResumePending = false;
				super.handleFailure();
			}
		});
	}

	private void doResumeContainer(IMIContainerDMContext context, final RequestMonitor rm) {
		fConnection.queueCommand(fCommandFactory.createMIExecContinue(context), new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}
	
    
	/**
	 * @since 4.1
	 */
	protected void refreshThreads() {
		fConnection.queueCommand(
			fCommandFactory.createMIListThreadGroups(fConnection.getContext(), false, true),
			new DataRequestMonitor<MIListThreadGroupsInfo>(getExecutor(), null) {
				@Override
				protected void handleSuccess() {
					IThreadGroupInfo[] groups = getData().getGroupList();
					for (IThreadGroupInfo group : groups) {
						if (group instanceof IThreadGroupInfo2) {
							MIThread[] threadList = ((IThreadGroupInfo2)group).getThreads(); 
							for (MIThread thread : threadList) {
								String threadId = thread.getThreadId();
								IMIContainerDMContext containerDmc = 
										fProcService.createContainerContextFromThreadId(fConnection.getContext(), threadId);
								IProcessDMContext processDmc = DMContexts.getAncestorOfType(containerDmc, IProcessDMContext.class);
								IThreadDMContext threadDmc =
										fProcService.createThreadContext(processDmc, threadId);
								IMIExecutionDMContext execDmc = fProcService.createExecutionContext(containerDmc, threadDmc, threadId);

								MIThreadRunState threadState = fThreadRunStates.get(execDmc);
								 if (threadState != null) {
									 // We may not know this thread.  This can happen when dealing with a remote
									 // where thread events are not reported immediately.
									 // However, the -list-thread-groups command we just sent will make
									 // GDB send those events.  Therefore, we can just ignore threads we don't
									 // know about, and wait for those events.
									 if (MIThread.MI_THREAD_STATE_RUNNING.equals(thread.getState())) {
										 if (threadState.fSuspended == true) {
											 // We missed a resumed event!  Send it now.
											 IResumedDMEvent resumedEvent = new ResumedEvent(execDmc, null);
											 fConnection.getSession().dispatchEvent(resumedEvent, getProperties());
										 }
									 } else if (MIThread.MI_THREAD_STATE_STOPPED.equals(thread.getState())) {
										 if (threadState.fSuspended == false) {
											 // We missed a suspend event!  Send it now.
											 ISuspendedDMEvent suspendedEvent = new SuspendedEvent(execDmc, null);
											 fConnection.getSession().dispatchEvent(suspendedEvent, getProperties());
										 }
									 } else {
										 assert false : "Invalid thread state: " + thread.getState(); //$NON-NLS-1$
									 }
								 }
							}
						}
					}
				}
			});
	}
	
	@Override
	public void flushCache(IDMContext context) {
		super.flushCache(context);
		refreshThreads();
	}
}
