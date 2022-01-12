/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson and others.
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
 *     Marc Dumais (Ericsson) - Bug 400231
 *     Marc Dumais (Ericsson) - Bug 399419
 *     Marc Dumais (Ericsson) - Bug 405390
 *     Marc Dumais (Ericsson) - Bug 396269
 *     Marc Dumais (Ericsson) - Bug 409512
 *     Marc Dumais (Ericsson) - Bug 409965
 *     Marc Dumais (Ericsson) - Bug 416524
 *     Xavier Raynaud (Kalray) - Bug 431935
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.List;

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
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFDebugModel;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl.MIRunMode;
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

	private static final String THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER = "The thread id does not convert to an integer: "; //$NON-NLS-1$
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
		// make sure model exists
		final VisualizerModel model = fVisualizer.getModel();
		if (model == null) {
			return;
		}

		IDMContext context = event.getDMContext();

		// all-stop mode? If so, we take the opportunity, now that GDB has suspended
		// execution, to re-create the model so that we synchronize with the debug session
		if (context != null && isSessionAllStop(context.getSessionId())) {
			fVisualizer.update();
			return;
		}

		// non-stop mode
		if (context instanceof IContainerDMContext) {
			// We don't deal with processes
		} else if (context instanceof IMIExecutionDMContext) {
			// Thread suspended

			final IMIExecutionDMContext execDmc = (IMIExecutionDMContext) context;
			IThreadDMContext threadContext = DMContexts.getAncestorOfType(execDmc, IThreadDMContext.class);

			final DsfServicesTracker tracker = new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(),
					execDmc.getSessionId());
			IProcesses procService = tracker.getService(IProcesses.class);
			final IStack stackService = tracker.getService(IStack.class);
			tracker.dispose();

			procService.getExecutionData(threadContext, new ImmediateDataRequestMonitor<IThreadDMData>() {
				@Override
				protected void handleSuccess() {
					IThreadDMData data = getData();

					// Check whether we know about cores
					if (data instanceof IGdbThreadDMData) {
						String[] cores = ((IGdbThreadDMData) data).getCores();
						if (cores != null) {
							assert cores.length == 1; // A thread belongs to a single core
							int coreId = Integer.parseInt(cores[0]);
							final VisualizerCore vCore = model.getCore(coreId);

							int tid;
							VisualizerThread threadTmp = null;
							try {
								tid = Integer.parseInt(execDmc.getThreadId());
								threadTmp = model.getThread(tid);
							} catch (NumberFormatException e) {
								// unable to resolve thread
								assert false : THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER + execDmc.getThreadId();
								return;
							}

							if (threadTmp != null) {
								final VisualizerThread thread = threadTmp;
								assert thread.getState() == VisualizerExecutionState.RUNNING;

								VisualizerExecutionState _newState = VisualizerExecutionState.SUSPENDED;

								if (event.getReason() == StateChangeReason.SIGNAL) {
									if (event instanceof IMIDMEvent) {
										Object miEvent = ((IMIDMEvent) event).getMIEvent();
										if (miEvent instanceof MISignalEvent) {
											String signalName = ((MISignalEvent) miEvent).getName();
											if (DSFDebugModel.isCrashSignal(signalName)) {
												_newState = VisualizerExecutionState.CRASHED;
											}
										}
									}
								}
								final VisualizerExecutionState newState = _newState;
								if (stackService != null) {
									stackService.getTopFrame(execDmc,
											new ImmediateDataRequestMonitor<IFrameDMContext>(null) {
												@Override
												protected void handleCompleted() {
													IFrameDMContext targetFrameContext = null;
													if (isSuccess()) {
														targetFrameContext = getData();
													}
													if (targetFrameContext != null) {
														stackService.getFrameData(targetFrameContext,
																new ImmediateDataRequestMonitor<IFrameDMData>(null) {
																	@Override
																	protected void handleCompleted() {
																		IFrameDMData frameData = null;
																		if (isSuccess()) {
																			frameData = getData();
																		}
																		updateThread(thread, newState, vCore,
																				frameData);
																	}
																});
													} else {
														updateThread(thread, newState, vCore, null);
													}
												}
											});
								} else {
									updateThread(thread, newState, vCore, null);
								}

							}
						}
					}
				}
			});
		}
	}

	private void updateThread(VisualizerThread thread, VisualizerExecutionState newState, VisualizerCore vCore,
			IFrameDMData frameData) {
		thread.setState(newState);
		thread.setCore(vCore);
		thread.setLocationInfo(frameData);
		fVisualizer.refresh();
	}

	/** Invoked when a thread or process is resumed. */
	@DsfServiceEventHandler
	public void handleEvent(IResumedDMEvent event) {
		// make sure model exists
		VisualizerModel model = fVisualizer.getModel();
		if (model == null) {
			return;
		}

		IDMContext context = event.getDMContext();

		// in all-stop mode... : update all threads states to "running"
		if (context != null && isSessionAllStop(context.getSessionId())) {
			List<VisualizerThread> tList = model.getThreads();
			for (VisualizerThread t : tList) {
				t.setState(VisualizerExecutionState.RUNNING);
				t.setLocationInfo((String) null);
			}
			fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();
			return;
		}

		// Non-stop mode
		if (context instanceof IContainerDMContext) {
			// We don't deal with processes
		} else if (context instanceof IMIExecutionDMContext) {
			// Thread resumed
			int tid;
			VisualizerThread thread = null;
			String strThreadId = ((IMIExecutionDMContext) context).getThreadId();
			try {
				tid = Integer.parseInt(strThreadId);
				thread = model.getThread(tid);
			} catch (NumberFormatException e) {
				// unable to resolve thread
				assert false : THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER + strThreadId;
				return;
			}

			if (thread != null) {
				assert thread.getState() == VisualizerExecutionState.SUSPENDED
						|| thread.getState() == VisualizerExecutionState.CRASHED;

				thread.setState(VisualizerExecutionState.RUNNING);
				thread.setLocationInfo((String) null);
				fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();
			}
		}
	}

	/** Invoked when a thread or process starts. */
	@DsfServiceEventHandler
	public void handleEvent(IStartedDMEvent event) {
		// make sure model exists
		final VisualizerModel model = fVisualizer.getModel();
		if (model == null) {
			return;
		}

		IDMContext context = event.getDMContext();
		if (context == null)
			return;
		final String sessionId = context.getSessionId();

		// all-stop mode?
		// If so we can't ask GDB for more info about the new thread at this moment.
		// So we still add it to the model, on core zero and with a OS thread id of
		// zero.  The next time the execution is stopped, the model will be re-created
		// and show the correct thread ids and cores.
		if (isSessionAllStop(sessionId) && context instanceof IMIExecutionDMContext) {
			final IMIExecutionDMContext execDmc = (IMIExecutionDMContext) context;
			final IMIProcessDMContext processContext = DMContexts.getAncestorOfType(execDmc, IMIProcessDMContext.class);

			// put it on core zero
			VisualizerCore vCore = model.getCore(0);
			if (vCore == null)
				return;

			int pid = Integer.parseInt(processContext.getProcId());

			int tid;
			try {
				tid = Integer.parseInt(execDmc.getThreadId());
			} catch (NumberFormatException e) {
				// unable to resolve thread
				assert false : THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER + execDmc.getThreadId();
				return;
			}

			int osTid = 0;

			// add thread if not already there - there is a potential race condition where a
			// thread can be added twice to the model: once at model creation and once more
			// through the listener.   Checking at both places to prevent this.
			if (model.getThread(tid) == null) {
				model.addThread(new VisualizerThread(vCore, pid, osTid, tid, VisualizerExecutionState.RUNNING));
				fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();
			}
			return;
		}

		// non-stop mode
		if (context instanceof IContainerDMContext) {
			// We don't deal with processes
		} else if (context instanceof IMIExecutionDMContext) {
			// New thread added
			final IMIExecutionDMContext execDmc = (IMIExecutionDMContext) context;
			final IMIProcessDMContext processContext = DMContexts.getAncestorOfType(execDmc, IMIProcessDMContext.class);
			IThreadDMContext threadContext = DMContexts.getAncestorOfType(execDmc, IThreadDMContext.class);

			DsfServicesTracker tracker = new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(),
					sessionId);
			IProcesses procService = tracker.getService(IProcesses.class);
			tracker.dispose();

			procService.getExecutionData(threadContext, new ImmediateDataRequestMonitor<IThreadDMData>() {
				@Override
				protected void handleSuccess() {
					IThreadDMData data = getData();

					// Check whether we know about cores
					if (data instanceof IGdbThreadDMData) {
						String[] cores = ((IGdbThreadDMData) data).getCores();
						if (cores != null) {
							assert cores.length == 1; // A thread belongs to a single core
							int coreId = Integer.parseInt(cores[0]);
							VisualizerCore vCore = model.getCore(coreId);
							// There is a race condition that sometimes happens here.  We can reach
							// here because we were notified that a thread is started, but the model
							// is not yet completely constructed.  If the model doesn't yet contain the
							// core the thread runs-on, the getCore() call above will return null.  This
							// will later cause a problem when we try to draw this thread, if we allow
							// this to pass.  See Bug 396269/
							if (vCore == null)
								return;

							int pid = Integer.parseInt(processContext.getProcId());
							int tid;
							try {
								tid = Integer.parseInt(execDmc.getThreadId());
							} catch (NumberFormatException e) {
								// Unable to resolve thread information
								assert false : THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER + execDmc.getThreadId();
								return;
							}

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
							if (model.getThread(tid) == null) {
								model.addThread(
										new VisualizerThread(vCore, pid, osTid, tid, VisualizerExecutionState.RUNNING));
								fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();
							}
						}
					}
				}
			});
		}
	}

	/** Invoked when a thread or process exits. */
	@DsfServiceEventHandler
	public void handleEvent(IExitedDMEvent event) {
		// make sure model exists
		final VisualizerModel model = fVisualizer.getModel();
		if (model == null) {
			return;
		}

		IDMContext context = event.getDMContext();
		final MulticoreVisualizerCanvas canvas = fVisualizer.getMulticoreVisualizerCanvas();

		if (context instanceof IContainerDMContext) {
			// process exited

			// Note: this is required because we noticed that in GDB 7.6 and older,
			// the "thread exited" signal is not sent for the local detach case.
			// see bug 409512
			DsfServicesTracker tracker = new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(),
					context.getSessionId());
			IProcesses procService = tracker.getService(IProcesses.class);
			tracker.dispose();

			// get all threads associated to this process and
			// mark them as exited in the model.
			procService.getProcessesBeingDebugged(context, new ImmediateDataRequestMonitor<IDMContext[]>() {
				@Override
				protected void handleSuccess() {
					assert getData() != null;

					IDMContext[] contexts = getData();
					for (IDMContext c : contexts) {
						if (c instanceof IMIExecutionDMContext) {
							int tid;
							String strThreadId = ((IMIExecutionDMContext) c).getThreadId();
							try {
								tid = Integer.parseInt(strThreadId);
							} catch (NumberFormatException e) {
								// unable to resolve the thread id
								assert false : THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER + strThreadId;
								continue;
							}

							model.markThreadExited(tid);
						}
					}

					if (canvas != null) {
						canvas.requestUpdate();
					}
				}

				@Override
				protected void handleFailure() {
					// we are overriding handleFailure() to avoid an error message
					// in the log, in the all-stop mode.
				}
			});

		} else if (context instanceof IMIExecutionDMContext) {
			// Thread exited
			int tid;
			String strThreadId = ((IMIExecutionDMContext) context).getThreadId();
			try {
				tid = Integer.parseInt(strThreadId);
				model.markThreadExited(tid);
			} catch (NumberFormatException e) {
				assert false : THE_THREAD_ID_DOES_NOT_CONVERT_TO_AN_INTEGER + strThreadId;
			}

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

	// helper functions

	/** Returns whether the session is the "all-stop" kind */
	private boolean isSessionAllStop(String sessionId) {
		DsfServicesTracker servicesTracker = new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(),
				sessionId);
		IMIRunControl runCtrlService = servicesTracker.getService(IMIRunControl.class);
		servicesTracker.dispose();

		if (runCtrlService != null && runCtrlService.getRunMode() == MIRunMode.ALL_STOP) {
			return true;
		}
		return false;
	}
}
