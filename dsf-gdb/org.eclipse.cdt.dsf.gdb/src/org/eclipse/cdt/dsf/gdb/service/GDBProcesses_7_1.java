/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo.IThreadGroupInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThread;
import org.eclipse.cdt.dsf.mi.service.command.output.MIThreadInfoInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class implements the IProcesses interface for GDB 7.1
 * which provides new information about cores for threads and processes.
 * 
 * @since 4.0
 */
public class GDBProcesses_7_1 extends GDBProcesses_7_0 {

	@Immutable
	protected static class MIThreadDMData_7_1 extends MIThreadDMData implements IGdbThreadDMData {
		final String[] fCores;

		public MIThreadDMData_7_1(String name, String id, String[] cores) {
			super(name, id);
			fCores = cores;
		}

		public String[] getCores() { return fCores; }

		public String getOwner() { return null; }
	}

    private CommandFactory fCommandFactory;
    // This cache is used when we send command to get the cores.
    // The value of the cores can change at any time, but we provide
    // an updated value whenever there is a suspended event.
    private CommandCache fCommandForCoresCache;
    private IGDBControl fCommandControl;

	public GDBProcesses_7_1(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	/**
	 * This method initializes this service after our superclass's initialize()
	 * method succeeds.
	 * 
	 * @param requestMonitor
	 *            The call-back object to notify when this service's
	 *            initialization is done.
	 */
	private void doInitialize(RequestMonitor requestMonitor) {
		fCommandControl = getServicesTracker().getService(IGDBControl.class);

		// This caches stores the result of a command when received; also, this cache
		// is manipulated when receiving events.  Currently, events are received after
		// three scheduling of the executor, while command results after only one.  This
		// can cause problems because command results might be processed before an event
		// that actually arrived before the command result.
		// To solve this, we use a bufferedCommandControl that will delay the command
		// result by two scheduling of the executor.
		// See bug 280461
        fCommandForCoresCache = new CommandCache(getSession(), 
        		new BufferedCommandControl(fCommandControl, getExecutor(), 2));
        fCommandForCoresCache.setContextAvailable(fCommandControl.getContext(), true);

        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
        getSession().addServiceEventListener(this, null);

        requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
        getSession().removeServiceEventListener(this);

		super.shutdown(requestMonitor);
	}

	@Override
	public void getExecutionData(final IThreadDMContext dmc, final DataRequestMonitor<IThreadDMData> rm) {
		if (dmc instanceof IMIProcessDMContext) {
			// Starting with GDB 7.1, we can obtain the list of cores a process is currently
			// running on (each core that has a thread of that process).
			// We have to use -list-thread-groups to obtain that information
			// Note that -list-thread-groups does not show the 'user' field
			super.getExecutionData(dmc, new DataRequestMonitor<IThreadDMData>(ImmediateExecutor.getInstance(), rm) {
				@Override
				protected void handleSuccess() {
					final IThreadDMData firstLevelData = getData();
					
					ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
					final String groupId = ((IMIProcessDMContext)dmc).getProcId();
					fCommandForCoresCache.execute(
							fCommandFactory.createMIListThreadGroups(controlDmc),
							new DataRequestMonitor<MIListThreadGroupsInfo>(ImmediateExecutor.getInstance(), rm) {
								@Override
								protected void handleCompleted() {
									String[] cores = null;
									if (isSuccess()) {
										IThreadGroupInfo[] groups = getData().getGroupList();
										if (groups != null) {
											for (IThreadGroupInfo group : groups) {
												if (group.getGroupId().equals(groupId)) {
													cores = group.getCores();
													break;
												}
											}
										}
									}
									rm.setData(new MIThreadDMData_7_1(firstLevelData.getName(),
				                                                      firstLevelData.getId(),
				                                                      cores));
									rm.done();	
								}
							});
				} 
			});
		} else if (dmc instanceof MIThreadDMC) {
			// Starting with GDB 7.1, we can obtain the core on which a thread
			// is currently located.  The info is a new field in -thread-info
			final MIThreadDMC threadDmc = (MIThreadDMC)dmc;

			ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
			fCommandForCoresCache.execute(fCommandFactory.createMIThreadInfo(controlDmc, threadDmc.getId()),
					new DataRequestMonitor<MIThreadInfoInfo>(ImmediateExecutor.getInstance(), rm) {
				        @Override
			          	protected void handleSuccess() {
				        	IThreadDMData threadData = null;
				        	if (getData().getThreadList().length != 0) {
				        		MIThread thread = getData().getThreadList()[0];
				        		if (thread.getThreadId().equals(threadDmc.getId())) {
				        			String core = thread.getCore();
				        			threadData = new MIThreadDMData_7_1("", thread.getOsId(), //$NON-NLS-1$
				        					                            core == null ? null : new String[] { core });
				        		}
				        	}

				        	if (threadData != null) {
				        		rm.setData(threadData);        	        			
				        	} else {
				        		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Could not get thread info", null)); //$NON-NLS-1$        	        			
				        	}
				        	rm.done();
				        }
			});
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
			rm.done();
		}
	}
	
	@DsfServiceEventHandler
	public void eventDispatched_7_1(IResumedDMEvent e) {
		if (e instanceof IContainerResumedDMEvent) {
			// This will happen in all-stop mode
			fCommandForCoresCache.setContextAvailable(e.getDMContext(), false);
		} else {
			// This will happen in non-stop mode
			// Keep target available for Container commands
		}
	}

	// Something has suspended, core allocation could have changed
	// during the time it was running.
    @DsfServiceEventHandler
    public void eventDispatched_7_1(ISuspendedDMEvent e) {
       	if (e instanceof IContainerSuspendedDMEvent) {
    		// This will happen in all-stop mode
       		fCommandForCoresCache.setContextAvailable(fCommandControl.getContext(), true);
       	} else {
       		// This will happen in non-stop mode
       	}
       	
       	fCommandForCoresCache.reset();
    }
    
    // Event handler when a thread or threadGroup starts, core allocation 
    // could have changed
    @DsfServiceEventHandler
    public void eventDispatched_7_1(IStartedDMEvent e) {
       	fCommandForCoresCache.reset();
	}
    
    // Event handler when a thread or a threadGroup exits, core allocation
    // could have changed
    @DsfServiceEventHandler
    public void eventDispatched_7_1(IExitedDMEvent e) {
       	fCommandForCoresCache.reset();
    }
    
	@Override
	public void flushCache(IDMContext context) {
		fCommandForCoresCache.reset(context);
		super.flushCache(context);
	}
}
