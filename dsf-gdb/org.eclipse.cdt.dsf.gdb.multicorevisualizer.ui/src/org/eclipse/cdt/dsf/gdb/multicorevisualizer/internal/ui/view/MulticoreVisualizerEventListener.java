/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Marc Dumais (Ericsson) - Bug 400231
 *     Marc Dumais (Ericsson) - Bug 399419
 *     Marc Dumais (Ericsson) - Bug 405390
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFDebugModel;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.IMIDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;


/**
 * DSF event listener class for the Multicore Visualizer.
 * This class will handle different relevant DSF events
 * and update the Multicore Visualizer accordingly.
 */
public class MulticoreVisualizerEventListener {
	
	// --- members ---

	/** Visualizer we're managing events for. */
	protected MulticoreVisualizer fVisualizer;

	
	// --- constructors/destructors ---
	
	/** Constructor */
	public MulticoreVisualizerEventListener(MulticoreVisualizer visualizer) {
		fVisualizer = visualizer;
	}

	
	// --- event handlers ---
	
	/** 
	 * Invoked when a thread or process is suspended. 
	 * Updates both state of the thread and the core it's running on
	 */
	@DsfServiceEventHandler
	public void handleEvent(final ISuspendedDMEvent event) {
		IDMContext context = event.getDMContext();
		if (context instanceof IContainerDMContext) {
    		// We don't deal with processes
    	} else if (context instanceof IMIExecutionDMContext) {
    		// Thread suspended
    		
    		final IMIExecutionDMContext execDmc = (IMIExecutionDMContext)context;
			IThreadDMContext threadContext =
					DMContexts.getAncestorOfType(execDmc, IThreadDMContext.class);

			DsfServicesTracker tracker = 
					new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(), 
                                           execDmc.getSessionId());
			IProcesses procService = tracker.getService(IProcesses.class);
			tracker.dispose();
			
			procService.getExecutionData(threadContext, 
				new ImmediateDataRequestMonitor<IThreadDMData>() {
					@Override
					protected void handleSuccess() {
						IThreadDMData data = getData();
					
						// Check whether we know about cores
						if (data instanceof IGdbThreadDMData) {
							String[] cores = ((IGdbThreadDMData)data).getCores();
							if (cores != null) {
								assert cores.length == 1; // A thread belongs to a single core
								int coreId = Integer.parseInt(cores[0]);
								VisualizerCore vCore = fVisualizer.getModel().getCore(coreId);
								
								int tid = execDmc.getThreadId();
																
					    		VisualizerThread thread = fVisualizer.getModel().getThread(tid);
					    		
					    		if (thread != null) {
					    			assert thread.getState() == VisualizerExecutionState.RUNNING;
					    			
					    			VisualizerExecutionState newState = VisualizerExecutionState.SUSPENDED;

					    			if (event.getReason() == StateChangeReason.SIGNAL) {
					    				if (event instanceof IMIDMEvent) {
					    					Object miEvent = ((IMIDMEvent)event).getMIEvent();
					    					if (miEvent instanceof MISignalEvent) {
					    						String signalName = ((MISignalEvent)miEvent).getName();
					    						if (DSFDebugModel.isCrashSignal(signalName)) {
					    							newState = VisualizerExecutionState.CRASHED;
					    						}
					    					}
					    				}
					    			}

					    			thread.setState(newState);
					    			thread.setCore(vCore);
					    			fVisualizer.refresh();
					    		}
							}
						}
					}
				}
			);
    	}
	}

	/** Invoked when a thread or process is resumed. */
	@DsfServiceEventHandler
	public void handleEvent(IResumedDMEvent event) {
		IDMContext context = event.getDMContext();
		if (context instanceof IContainerDMContext) {
    		// We don't deal with processes
    	} else if (context instanceof IMIExecutionDMContext) {
    		// Thread resumed
    		int tid = ((IMIExecutionDMContext)context).getThreadId();

    		VisualizerThread thread = fVisualizer.getModel().getThread(tid);
    		
    		if (thread != null) {
    			assert thread.getState() == VisualizerExecutionState.SUSPENDED ||
     				   thread.getState() == VisualizerExecutionState.CRASHED;
    			
    			thread.setState(VisualizerExecutionState.RUNNING);
    			fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();
    		}
    	}
	}

	/** Invoked when a thread or process starts. */
	@DsfServiceEventHandler
	public void handleEvent(IStartedDMEvent event) {
		IDMContext context = event.getDMContext();
		if (context instanceof IContainerDMContext) {
    		// We don't deal with processes
    	} else if (context instanceof IMIExecutionDMContext) {
    		// New thread added
    		final IMIExecutionDMContext execDmc = (IMIExecutionDMContext)context;
			final IMIProcessDMContext processContext =
					DMContexts.getAncestorOfType(execDmc, IMIProcessDMContext.class);
			IThreadDMContext threadContext =
					DMContexts.getAncestorOfType(execDmc, IThreadDMContext.class);

			DsfServicesTracker tracker = 
					new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(), 
                                           execDmc.getSessionId());
			IProcesses procService = tracker.getService(IProcesses.class);
			tracker.dispose();
			
			procService.getExecutionData(threadContext, 
				new ImmediateDataRequestMonitor<IThreadDMData>() {
					@Override
					protected void handleSuccess() {
						IThreadDMData data = getData();
					
						// Check whether we know about cores
						if (data instanceof IGdbThreadDMData) {
							String[] cores = ((IGdbThreadDMData)data).getCores();
							if (cores != null) {
								assert cores.length == 1; // A thread belongs to a single core
								int coreId = Integer.parseInt(cores[0]);
								VisualizerCore vCore = fVisualizer.getModel().getCore(coreId);
								
								int pid = Integer.parseInt(processContext.getProcId());
								int tid = execDmc.getThreadId();
								
								int osTid = 0;
								try {
									osTid = Integer.parseInt(data.getId());
								} catch (NumberFormatException e) {
									// I've seen a case at startup where GDB is not ready to
									// return the osTID so we get null.  
									// That is ok, we'll be refreshing right away at startup
								}

								// add thread if not already there - there is a potential race condition where a 
								// thread can be added twice to the model: once at model creation and once more 
								// through the listener.   Checking at both places to prevent this.
								if (fVisualizer.getModel().getThread(tid) == null ) {
									fVisualizer.getModel().addThread(new VisualizerThread(vCore, pid, osTid, tid, VisualizerExecutionState.RUNNING));
									fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();	
								}
							}
						}
					}
				}
			);
    	}
	}
	
	/** Invoked when a thread or process exits. */
	@DsfServiceEventHandler
	public void handleEvent(IExitedDMEvent event) {
		IDMContext context = event.getDMContext();
		if (context instanceof IContainerDMContext) {
    		// We don't deal with processes
    	} else if (context instanceof IMIExecutionDMContext) {
    		// Thread exited
    		int tid = ((IMIExecutionDMContext)context).getThreadId();

			fVisualizer.getModel().markThreadExited(tid);
			
			MulticoreVisualizerCanvas canvas = fVisualizer.getMulticoreVisualizerCanvas();
			if (canvas != null) {
				canvas.requestUpdate();
			}
    	}
	}

	
	/** Invoked when the debug data model is ready */
	@DsfServiceEventHandler
	public void handleEvent(DataModelInitializedEvent event) {
		// re-create the visualizer model now that CPU and core info is available
		fVisualizer.update();
	}

}

