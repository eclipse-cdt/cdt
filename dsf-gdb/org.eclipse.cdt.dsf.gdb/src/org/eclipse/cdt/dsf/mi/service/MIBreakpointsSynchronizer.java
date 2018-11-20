/*******************************************************************************
 * Copyright (c) 2012, 2018 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Mentor Graphics - Initial API and implementation
 * 		Salvatore Culcasi (ST) - Bug 407163 - GDB Console: breakpoint not added with MinGW and gdb
 *      Marc Khouzam (Ericsson) - Update breakpoint handling for GDB >= 7.4 (Bug 389945)
 *      Marc Khouzam (Ericsson) - Support for dynamic printf (Bug 400628)
 *      Jonah Graham (Kichwa Coders) - Bug 317173 - cleanup warnings
 *      Jonah Graham (Kichwa Coders) - Bug 530377 - Corruption of state due to fast events from GDB
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMData;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IDsfBreakpointExtension;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.CollectAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.EvaluateAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.WhileSteppingAction;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.MIBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager.IMIBreakpointsTrackingListener;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.osgi.framework.BundleContext;

/**
 * Provides synchronization between breakpoints set from outside of the Eclipse
 * breakpoint framework (GDB console, trace files, etc.) and the Breakpoints
 * view.
 * <p>
 * Bug 530377: Prior to fixing 530377, events that arrived from GDB faster than
 * DSF/Eclipse fully processed them could cause the state within the
 * synchronizer and manager to become corrupt. This would happen because it
 * takes multiple DSF stages to complete handling 1 event, so the handling of
 * the next event would become intermingled. That violated many assumptions in
 * the code that the code run in the respective RequestMonitor would be on the
 * same state. This is an unsuprising assumption based on the general idea of
 * DSF as not requiring the normal synchronization primitives as everything is
 * single-threaded. To resolve this problem, there is some code
 * {@link #queueEvent(BreakpointEvent)} that ensures each event is fully
 * processed before the next event starts processing.
 *
 * @since 4.2
 */
public class MIBreakpointsSynchronizer extends AbstractDsfService
		implements IMIBreakpointsTrackingListener, ICachingService {

	// Catchpoint expressions
	private static final String CE_EXCEPTION_CATCH = "exception catch"; //$NON-NLS-1$
	private static final String CE_EXCEPTION_THROW = "exception throw"; //$NON-NLS-1$

	// GDB tracepoint commands
	private static final String TC_COLLECT = "collect "; //$NON-NLS-1$
	private static final String TC_TEVAL = "teval "; //$NON-NLS-1$
	private static final String TC_WHILE_STEPPING = "while-stepping "; //$NON-NLS-1$
	private static final String TC_END = "end"; //$NON-NLS-1$

	private IMICommandControl fConnection;
	private MIBreakpoints fBreakpointsService;
	private MIBreakpointsManager fBreakpointsManager;

	/**
	 * Collection of the target contexts that are being tracked.
	 */
	private Set<IBreakpointsTargetDMContext> fTrackedTargets;

	/**
	 * Collection of breakpoints created from the GDB console or outside of Eclipse.
	 *
	 * Map of breakpoint contexts to Map of breakpoint number (String) to MIBreakpoint
	 */
	private Map<IBreakpointsTargetDMContext, Map<String, MIBreakpoint>> fCreatedTargetBreakpoints;

	/**
	 * Collection of breakpoints deleted from the GDB console or outside of Eclipse
	 */
	private Map<IBreakpointsTargetDMContext, Set<String>> fDeletedTargetBreakpoints;

	/**
	 * Collection of pending breakpoint modifications
	 */
	private Map<IBreakpointsTargetDMContext, Map<String, MIBreakpoint>> fPendingModifications;

	/**
	 * Class to store an event that needs to be performed by the synchronizer
	 *
	 * @see MIBreakpointsSynchronizer class documentation for design comments
	 */
	private static class BreakpointEvent {
		MIBreakpoint created;
		MIBreakpoint modified;
		String deleted;

		static class BreakpointEventSynchronize {
			IBreakpointsTargetDMContext dmc;
			MIBreakListInfo list;
		}

		BreakpointEventSynchronize synchronize;
	}

	/**
	 * List of events that are queued, waiting to be processed.
	 *
	 * @see MIBreakpointsSynchronizer class documentation for design comments
	 */
	private Deque<BreakpointEvent> fBreakpointEvents = new LinkedList<>();

	/**
	 * True if the delayed events processing task is idle. If idle, a new event
	 * should trigger restarting the processing.
	 *
	 * @see MIBreakpointsSynchronizer class documentation for design comments
	 */
	private boolean fEventsIdle = true;

	public MIBreakpointsSynchronizer(DsfSession session) {
		super(session);
		fTrackedTargets = new HashSet<>();
		fCreatedTargetBreakpoints = new HashMap<>();
		fDeletedTargetBreakpoints = new HashMap<>();
		fPendingModifications = new HashMap<>();
	}

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
		fConnection = getServicesTracker().getService(IMICommandControl.class);
		fBreakpointsService = getServicesTracker().getService(MIBreakpoints.class);
		fBreakpointsManager = getServicesTracker().getService(MIBreakpointsManager.class);
		if (fConnection == null || fBreakpointsService == null && fBreakpointsManager == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
			rm.done();
			return;
		}
		fBreakpointsManager.addBreakpointsTrackingListener(this);
		getSession().addServiceEventListener(this, null);

		// Register this service
		register(new String[] { MIBreakpointsSynchronizer.class.getName() }, new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		fTrackedTargets.clear();
		fCreatedTargetBreakpoints.clear();
		fDeletedTargetBreakpoints.clear();
		fPendingModifications.clear();
		fBreakpointEvents.clear();
		getSession().removeServiceEventListener(this);
		MIBreakpointsManager bm = getBreakpointsManager();
		if (bm != null) {
			bm.removeBreakpointsTrackingListener(this);
		}
		unregister();
		super.shutdown(rm);
	}

	/**
	 * Obtain the collection of MI breakpoints created
	 *
	 * @return collection of target breakpoints
	 * @since 5.3
	 */
	protected Collection<MIBreakpoint> getCreatedTargetBreakpoints(IBreakpointsTargetDMContext context) {
		Map<String, MIBreakpoint> map = fCreatedTargetBreakpoints.get(context);
		if (map != null) {
			return map.values();
		}
		return null;
	}

	@Override
	public void breakpointTrackingStarted(IBreakpointsTargetDMContext bpTargetDMC) {
		fTrackedTargets.add(bpTargetDMC);
	}

	@Override
	public void breakpointTrackingStopped(IBreakpointsTargetDMContext bpTargetDMC) {
		fTrackedTargets.remove(bpTargetDMC);
	}

	private IMICommandControl getCommandControl() {
		return fConnection;
	}

	private MIBreakpoints getBreakpointsService() {
		return fBreakpointsService;
	}

	private MIBreakpointsManager getBreakpointsManager() {
		return fBreakpointsManager;
	}

	/**
	 * Queue (and potentially start processing) breakpoint events from GDB.
	 *
	 * @param event
	 *            from GDB that needs to be processed once the synchronizer is idle
	 *            and has completed the previous event.
	 */
	private void queueEvent(BreakpointEvent event) {
		fBreakpointEvents.add(event);
		if (fEventsIdle) {
			runNextEvent();
		}
	}

	private void runNextEvent() {
		fEventsIdle = false;
		BreakpointEvent event = fBreakpointEvents.poll();
		if (event == null) {
			fEventsIdle = true;
			return;
		}

		RequestMonitor rm = new RequestMonitor(getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				runNextEvent();
				super.handleCompleted();
			}
		};

		if (event.created != null) {
			doTargetBreakpointCreated(event.created, rm);
		} else if (event.deleted != null) {
			doTargetBreakpointDeleted(event.deleted, rm);
		} else if (event.modified != null) {
			doTargetBreakpointModified(event.modified, rm);
		} else if (event.synchronize != null) {
			doTargetBreakpointsSynchronized(event.synchronize.dmc, event.synchronize.list, rm);
		} else {
			rm.done();
		}
	}

	/**
	 * The effect of flushing the cache of the synchronizer is to recollect all
	 * breakpoint info from GDB and update the IBreakpoints and MIBreakpointManager
	 * services too.
	 *
	 * Note that an optimization in the number of calls to synchronize can be done, see
	 * synchronize's removeBpsForAllDmcs parameter.
	 */
	@Override
	public void flushCache(IDMContext context) {
		Collection<IBreakpointsTargetDMContext> contexts;
		IBreakpointsTargetDMContext breakpointsTargetDMContext = DMContexts.getAncestorOfType(context,
				IBreakpointsTargetDMContext.class);
		if (breakpointsTargetDMContext != null) {
			contexts = Arrays.asList(breakpointsTargetDMContext);
		} else {
			contexts = getBreakpointsManager().getTrackedBreakpointTargetContexts();
		}

		for (IBreakpointsTargetDMContext bpContext : contexts) {
			synchronize(bpContext, false);
		}
	}

	/**
	 * Synchronize the breakpoint state with the back end. This is done by issuing a
	 * -break-list to the backend and adding that result to the list of queue'd
	 * events from the backend. When this entry in the queue is processed, it
	 * converts itself to a series of new events that represent the difference
	 * between the state in the breakpoint manager and GDB.
	 *
	 * @param bpContext
	 *            context to issue MI Break List on
	 * @param removeBpsForAllDmcs
	 *            If the break list command returns breakpoints for all contexts,
	 *            pass true. If false, the synchronizer assumes only bps not listed
	 *            for bpContext will be removed. This provides an optimization to
	 *            prevent issuing createMIBreakList multiple times that will return
	 *            the same value. (Note that using a CommandCache will not achieve
	 *            the optimization because each call to createMIBreakList is a
	 *            different context.)
	 * @since 5.5
	 */
	protected void synchronize(IBreakpointsTargetDMContext bpContext, boolean removeBpsForAllDmcs) {
		fConnection.queueCommand(fConnection.getCommandFactory().createMIBreakList(bpContext),
				new DataRequestMonitor<MIBreakListInfo>(getExecutor(), null) {
					@Override
					protected void handleSuccess() {
						BreakpointEvent event = new BreakpointEvent();
						event.synchronize = new BreakpointEvent.BreakpointEventSynchronize();
						event.synchronize.dmc = removeBpsForAllDmcs ? null : bpContext;
						event.synchronize.list = getData();
						queueEvent(event);
					}
				});
	}

	private void doTargetBreakpointsSynchronized(IBreakpointsTargetDMContext breakpointsContext, MIBreakListInfo data,
			RequestMonitor rm) {
		final MIBreakpointsManager bm = getBreakpointsManager();
		Map<IBreakpointsTargetDMContext, Map<IBreakpointDMContext, ICBreakpoint>> bpToPlatformMaps = bm
				.getBPToPlatformMaps();
		Stream<IBreakpointDMContext> breakpointsKnownToManager = bpToPlatformMaps.entrySet().stream()
				.flatMap(m -> m.getValue().keySet().stream());
		Collector<MIBreakpointDMContext, ?, Map<IBreakpointsTargetDMContext, String>> collector = Collectors.toMap(
				(MIBreakpointDMContext dmc) -> DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class),
				(MIBreakpointDMContext dmc) -> dmc.getReference());
		Map<IBreakpointsTargetDMContext, String> numbersKnownToManager = breakpointsKnownToManager
				.filter(MIBreakpointDMContext.class::isInstance).map(MIBreakpointDMContext.class::cast)
				.collect(collector);

		for (MIBreakpoint miBpt : data.getMIBreakpoints()) {
			String number = miBpt.getNumber();
			if (numbersKnownToManager.values().remove(number)) {
				BreakpointEvent event = new BreakpointEvent();
				event.modified = miBpt;
				fBreakpointEvents.addFirst(event);
			} else {
				BreakpointEvent event = new BreakpointEvent();
				event.created = miBpt;
				fBreakpointEvents.addFirst(event);
			}
		}
		for (Entry<IBreakpointsTargetDMContext, String> entry : numbersKnownToManager.entrySet()) {
			IBreakpointsTargetDMContext dmc = entry.getKey();
			String number = entry.getValue();
			if (number != null && !number.isEmpty() && (breakpointsContext == null || breakpointsContext.equals(dmc))) {
				BreakpointEvent event = new BreakpointEvent();
				event.deleted = number;
				fBreakpointEvents.addFirst(event);
			}
		}

		rm.done();
	}

	public void targetBreakpointCreated(final MIBreakpoint miBpt) {
		BreakpointEvent event = new BreakpointEvent();
		event.created = miBpt;
		queueEvent(event);
	}

	private void doTargetBreakpointCreated(final MIBreakpoint miBpt, RequestMonitor rm) {
		if (isCatchpoint(miBpt)) {
			rm.done();
			return;
		}
		MIBreakpoints breakpointsService = getBreakpointsService();
		final MIBreakpointsManager bm = getBreakpointsManager();
		if (breakpointsService == null || bm == null) {
			rm.done();
			return;
		}

		final IBreakpointsTargetDMContext bpTargetDMC = getBreakpointsTargetContext(miBpt);
		if (bpTargetDMC == null) {
			rm.done();
			return;
		}

		// Store the target breakpoint data
		Map<String, MIBreakpointDMData> contextBreakpoints = breakpointsService.getBreakpointMap(bpTargetDMC);
		if (contextBreakpoints == null) {
			contextBreakpoints = breakpointsService.createNewBreakpointMap(bpTargetDMC);
		}
		contextBreakpoints.put(miBpt.getNumber(), fBreakpointsService.createMIBreakpointDMData(miBpt));

		// Store the created target breakpoint to prevent setting it again on the target
		// when addBreakpoint() is called.
		Map<String, MIBreakpoint> targetMap = fCreatedTargetBreakpoints.get(bpTargetDMC);
		if (targetMap == null) {
			targetMap = new HashMap<>();
			fCreatedTargetBreakpoints.put(bpTargetDMC, targetMap);
		}
		targetMap.put(miBpt.getNumber(), miBpt);

		// Convert the debug info file path into the file path in the local file system
		String debuggerPath = getFileName(miBpt);
		getSource(bpTargetDMC, debuggerPath, new DataRequestMonitor<String>(getExecutor(), rm) {
			@Override
			@ConfinedToDsfExecutor("fExecutor")
			protected void handleSuccess() {
				String fileName = getData();
				if (fileName == null) {
					fileName = getFileName(miBpt);
				}
				// Try to find matching platform breakpoint
				ICBreakpoint plBpt = getPlatformBreakpoint(miBpt, fileName);
				String threadId = miBpt.getThreadId();
				boolean isThreadSpecific = threadId != null && !threadId.isEmpty() && !"0".equals(threadId); //$NON-NLS-1$
				try {
					if (plBpt == null) {
						// If matching platform breakpoint doesn't exist create a new one
						plBpt = createPlatformBreakpoint(fileName, miBpt);
						// If the target breakpoint is thread specific, update thread filters
						if (isThreadSpecific) {
							setThreadSpecificBreakpoint(plBpt, miBpt);
						}
						doTargetBreakpointCreatedSync(miBpt, bpTargetDMC, plBpt);
						delayDone(100, rm);
						return;
					} else {
						// The corresponding platform breakpoint already exists.
						// If the breakpoint tracking has already started we need
						// to notify MIBreakpointsManager which will increment its
						// install count.
						// Otherwise the breakpoint will be processed as an initial
						// breakpoint when the breakpoint tracking starts.
						if (isBreakpointTargetTracked(bpTargetDMC)) {
							// If the target breakpoint is thread specific, update thread filters
							if (isThreadSpecific) {
								setThreadSpecificBreakpoint(plBpt, miBpt);
							}

							ICBreakpoint plBpt2 = plBpt;
							bm.breakpointAdded(plBpt2, miBpt, new RequestMonitor(getExecutor(), rm) {
								@Override
								protected void handleCompleted() {
									doTargetBreakpointCreatedSync(miBpt, bpTargetDMC, plBpt2);
									rm.done();
								}
							});
							return;
						} else {
							doTargetBreakpointCreatedSync(miBpt, bpTargetDMC, plBpt);
							rm.done();
							return;
						}
					}
				} catch (CoreException e) {
					GdbPlugin.log(getStatus());
				}
				rm.done();
				return;
			}

		});
	}

	private void doTargetBreakpointCreatedSync(final MIBreakpoint miBpt, final IBreakpointsTargetDMContext bpTargetDMC,
			ICBreakpoint plBpt) {
		// Make sure the platform breakpoint's parameters are synchronized
		// with the target breakpoint.
		Map<String, MIBreakpoint> map = fPendingModifications.get(bpTargetDMC);
		if (map != null) {
			MIBreakpoint mod = map.remove(miBpt.getNumber());
			if (mod != null) {
				targetBreakpointModified(bpTargetDMC, plBpt, mod);
			}
		} else {
			targetBreakpointModified(bpTargetDMC, plBpt, miBpt);
		}
	}

	/**
	 * Some operations that are passed to platform require a number or delays before
	 * they complete. The reason is that the platform code will retrigger DSF code
	 * and continue updating state. Ideally there would be completion monitors for
	 * the platform operations, but that is not available. Use this method to delay
	 * calling .done() until at least delayExecutorCycles cycles of the executor
	 * have run.
	 *
	 * @param delayExecutorCycles
	 * @param rm
	 */
	private void delayDone(int delayExecutorCycles, RequestMonitor rm) {
		getExecutor().execute(() -> {
			int remaining = delayExecutorCycles - 1;
			if (remaining < 0) {
				rm.done();
			} else {
				delayDone(remaining, rm);
			}
		});
	}

	/**
	 * @since 5.0
	 */
	public void targetBreakpointDeleted(final String id) {
		BreakpointEvent event = new BreakpointEvent();
		event.deleted = id;
		queueEvent(event);
	}

	private void doTargetBreakpointDeleted(final String id, RequestMonitor rm) {
		MIBreakpoints breakpointsService = getBreakpointsService();
		final MIBreakpointsManager bm = getBreakpointsManager();
		if (breakpointsService == null || bm == null) {
			rm.done();
			return;
		}
		final IBreakpointsTargetDMContext bpTargetDMC = breakpointsService.getBreakpointTargetContext(id);
		if (bpTargetDMC != null) {
			final MIBreakpointDMContext bpDMC = new MIBreakpointDMContext(breakpointsService,
					new IDMContext[] { bpTargetDMC }, id);
			breakpointsService.getBreakpointDMData(bpDMC, new DataRequestMonitor<IBreakpointDMData>(getExecutor(), rm) {
				@Override
				@ConfinedToDsfExecutor("fExecutor")
				protected void handleSuccess() {
					if (!(getData() instanceof MIBreakpointDMData)) {
						rm.done();
						return;
					}
					MIBreakpointDMData data = (MIBreakpointDMData) getData();
					if (MIBreakpoints.CATCHPOINT.equals(data.getBreakpointType())) {
						rm.done();
						return;
					}

					IBreakpoint plBpt = bm.findPlatformBreakpoint(bpDMC);
					if (plBpt instanceof ICBreakpoint) {
						Set<String> set = fDeletedTargetBreakpoints.get(bpTargetDMC);
						if (set == null) {
							set = new HashSet<>();
							fDeletedTargetBreakpoints.put(bpTargetDMC, set);
						}
						set.add(id);

						try {
							String threadId = data.getThreadId();
							if (!threadId.equals("0")) { //$NON-NLS-1$
								IDsfBreakpointExtension bpExtension = fBreakpointsManager
										.getFilterExtension((ICBreakpoint) plBpt);

								IMIProcesses processes = getServicesTracker().getService(IMIProcesses.class);
								if (processes == null) {
									rm.done();
									return;
								}

								IContainerDMContext contDMC = processes.createContainerContextFromThreadId(
										getCommandControl().getContext(), data.getThreadId());
								if (contDMC == null) {
									rm.done();
									return;
								}

								IExecutionDMContext[] execDMCs = bpExtension.getThreadFilters(contDMC);
								List<IExecutionDMContext> list = new ArrayList<>(execDMCs.length);
								for (IExecutionDMContext c : execDMCs) {
									if (c instanceof IMIExecutionDMContext
											&& !((IMIExecutionDMContext) c).getThreadId().equals(threadId)) {
										list.add(c);
									}
								}
								if (!list.isEmpty()) {
									bpExtension.setThreadFilters(list.toArray(new IExecutionDMContext[list.size()]));
									rm.done();
									return;
								} else {
									bm.uninstallBreakpoint(bpTargetDMC, (ICBreakpoint) plBpt,
											new RequestMonitor(getExecutor(), rm));
									return;
								}
							} else {
								bm.uninstallBreakpoint(bpTargetDMC, (ICBreakpoint) plBpt,
										new RequestMonitor(getExecutor(), rm));
								return;
							}
						} catch (CoreException e) {
							GdbPlugin.log(e.getStatus());
						}
					}
					rm.done();
				}
			});

		} else {
			rm.done();
		}
	}

	public void targetBreakpointModified(final MIBreakpoint miBpt) {
		BreakpointEvent event = new BreakpointEvent();
		event.modified = miBpt;
		queueEvent(event);
	}

	/**
	 * Find the platform breakpoint, returning it, if it exists via the DRM. If the
	 * drm's data is null, it has not been found.
	 */
	private void findPlatformBreakpoint(final MIBreakpoint miBpt, DataRequestMonitor<IBreakpoint> drm) {
		MIBreakpoints breakpointsService = getBreakpointsService();
		final MIBreakpointsManager bm = getBreakpointsManager();
		if (breakpointsService != null && bm != null) {
			final IBreakpointsTargetDMContext bpTargetDMC = getBreakpointsTargetContext(miBpt);
			if (bpTargetDMC == null) {
				drm.done((IBreakpoint) null);
				return;
			}
			final Map<String, MIBreakpointDMData> contextBreakpoints = breakpointsService.getBreakpointMap(bpTargetDMC);
			if (contextBreakpoints == null) {
				drm.done((IBreakpoint) null);
				return;
			}
			IBreakpoint b = bm.findPlatformBreakpoint(
					new MIBreakpointDMContext(breakpointsService, new IDMContext[] { bpTargetDMC }, miBpt.getNumber()));
			if (b != null) {
				drm.done(b);
			} else {
				// Convert the debug info file path into the file path in the local file system
				String debuggerPath = getFileName(miBpt);
				getSource(bpTargetDMC, debuggerPath, new DataRequestMonitor<String>(getExecutor(), drm) {
					@Override
					@ConfinedToDsfExecutor("fExecutor")
					protected void handleSuccess() {
						String fileName = getData();
						if (fileName == null) {
							fileName = getFileName(miBpt);
						}
						// Try to find matching platform breakpoint
						ICBreakpoint plBpt = getPlatformBreakpoint(miBpt, fileName);
						drm.done(plBpt);
					}
				});
			}
		} else {
			drm.done((ICBreakpoint) null);
		}
	}

	private void doTargetBreakpointModified(final MIBreakpoint miBpt, RequestMonitor rm) {
		if (isCatchpoint(miBpt)) {
			rm.done();
			return;
		}

		findPlatformBreakpoint(miBpt, new DataRequestMonitor<IBreakpoint>(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				IBreakpointsTargetDMContext bpTargetDMC = getBreakpointsTargetContext(miBpt);
				if (bpTargetDMC == null) {
					rm.done();
					return;
				}

				IBreakpoint breakpoint = getData();
				if (!(breakpoint instanceof ICBreakpoint)) {
					// Platform breakpoint hasn't been created yet. Store the latest
					// modification data, it will be picked up later.
					Map<String, MIBreakpoint> map = fPendingModifications.get(bpTargetDMC);
					if (map == null) {
						map = new HashMap<>();
						fPendingModifications.put(bpTargetDMC, map);
					}
					map.put(miBpt.getNumber(), miBpt);
					rm.done();
				} else {
					ICBreakpoint plBpt = (ICBreakpoint) breakpoint;
					targetBreakpointModified(bpTargetDMC, plBpt, miBpt);
					delayDone(100, rm);
				}
			}
		});
	}

	private void targetBreakpointModified(IBreakpointsTargetDMContext bpTargetDMC, ICBreakpoint plBpt,
			MIBreakpoint miBpt) {
		Map<String, MIBreakpointDMData> contextBreakpoints = getBreakpointsService().getBreakpointMap(bpTargetDMC);
		MIBreakpointDMData oldData = contextBreakpoints.get(miBpt.getNumber());
		contextBreakpoints.put(miBpt.getNumber(), fBreakpointsService.createMIBreakpointDMData(miBpt));
		try {
			if (plBpt.isEnabled() != miBpt.isEnabled()) {
				plBpt.setEnabled(miBpt.isEnabled());
			}
			if (!plBpt.getCondition().equals(miBpt.getCondition())) {
				plBpt.setCondition(miBpt.getCondition());
			}
			if (oldData.isPending() != miBpt.isPending()) {
				if (miBpt.isPending()) {
					plBpt.decrementInstallCount();
				} else {
					plBpt.incrementInstallCount();
				}
			}
			if (plBpt instanceof ICTracepoint && miBpt.isTracepoint()) {
				ICTracepoint plTpt = (ICTracepoint) plBpt;
				if (plTpt.getPassCount() != miBpt.getPassCount()) {
					// GDB (up to 7.5) doesn't emit notification when the pass count is modified.
					plTpt.setPassCount(miBpt.getPassCount());
				}

				if (!miBpt.getCommands()
						.equals(plBpt.getMarker().getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE))) {
					StringBuilder sb = new StringBuilder();
					boolean first = true;
					String[] commands = miBpt.getCommands().split(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
					for (ITracepointAction action : getActionsFromCommands(commands)) {
						if (first) {
							first = false;
						} else {
							sb.append(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
						}
						sb.append(action.getName());
					}
					// Target breakpoints and platform breakpoints use the same format
					// to store trace commands. This format is different than the format
					// used by GDB. We need to switch to the platform format to avoid unnecessary
					// modifications of target breakpoints.
					miBpt.setCommands(sb.toString());
					plBpt.getMarker().setAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, sb.toString());
				}
			} else if (plBpt instanceof ICDynamicPrintf && miBpt.isDynamicPrintf()) {
				// Cannot synchronize the string as there is a bug in GDB 7.7 that corrupts it.
				// https://sourceware.org/bugzilla/show_bug.cgi?id=15806
				// If we were to synchronize here, we would overwrite the string defined by
				// the user with the corrupted one!
				// Truth is that we don't need to synchronize the string anyway because there
				// is currently no way to change a dprintf string in GDB; instead a new
				// dprintf must be created.  That means that there will be no =breakpoint-modifed
				// event that indicates a real dprintf string change; only the other fields can
				// change and are handled as any other breakpoint.
				//
				// ICDynamicPrintf plDPrintf = (ICDynamicPrintf)plBpt;
				// if (!plDPrintf.getPrintfString().equals(miBpt.getPrintfString())) {
				// 	plDPrintf.setPrintfString(miBpt.getPrintfString());
				// }
			}
		} catch (CoreException e) {
			contextBreakpoints.put(miBpt.getNumber(), oldData);
			GdbPlugin.log(e.getStatus());
		}
	}

	private void setThreadSpecificBreakpoint(final ICBreakpoint plBpt, MIBreakpoint miBpt) {

		try {
			IMIProcesses processes = getServicesTracker().getService(IMIProcesses.class);
			if (processes == null) {
				return;
			}
			String threadId = miBpt.getThreadId();
			IContainerDMContext contDMC = processes.createContainerContextFromThreadId(getCommandControl().getContext(),
					threadId);
			if (contDMC == null) {
				return;
			}
			IProcessDMContext procDmc = DMContexts.getAncestorOfType(contDMC, IProcessDMContext.class);
			if (procDmc == null) {
				return;
			}
			IDsfBreakpointExtension bpExtension = fBreakpointsManager.getFilterExtension(plBpt);

			IExecutionDMContext[] execDMCs = bpExtension.getThreadFilters(contDMC);
			if (execDMCs == null) {
				execDMCs = new IExecutionDMContext[0];
			}
			for (IExecutionDMContext execDMC : execDMCs) {
				String ctxThreadId = ((IMIExecutionDMContext) execDMC).getThreadId();
				if (execDMC instanceof IMIExecutionDMContext && ctxThreadId.equals(threadId)) {
					// The platform breakpoint is already restricted to the given thread.
					return;
				}
			}
			IExecutionDMContext[] newExecDMCs = new IExecutionDMContext[execDMCs.length + 1];
			System.arraycopy(execDMCs, 0, newExecDMCs, 0, execDMCs.length);
			newExecDMCs[execDMCs.length] = processes.createExecutionContext(contDMC,
					processes.createThreadContext(procDmc, threadId), threadId);
			bpExtension.setThreadFilters(newExecDMCs);
		} catch (CoreException e) {
			GdbPlugin.log(e);
		}
	}

	private ICBreakpoint getPlatformBreakpoint(MIBreakpoint miBpt, String fileName) {
		for (IBreakpoint b : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
			if (b instanceof ICBreakpoint) {
				ICBreakpoint cBreakpoint = (ICBreakpoint) b;
				if (isPlatformBreakpoint(cBreakpoint, miBpt, fileName)) {
					return cBreakpoint;
				}
			}
		}
		return null;
	}

	/**
	 * Return true if the target breakpoint is the same as the platform breakpoint.
	 *
	 * Whether breakpoints are considered the same depends on their type and their key attributes, with each type
	 * defining its own key attributes.
	 *
	 * @param platformBreakpoint
	 * @param targetBreakpoint
	 * @param fileName
	 *            source lookup resolved file name. The file name in targetBreakpoint will be the file name as GDB
	 *            understands it, and fileName will be the resolved value as Eclipse (and therefore the platform
	 *            breakpoint) understands it.
	 * @return if platformBreakpoint and targetBreakpoint match
	 * @since 5.3
	 */
	protected boolean isPlatformBreakpoint(ICBreakpoint platformBreakpoint, MIBreakpoint targetBreakpoint,
			String fileName) {
		if (platformBreakpoint instanceof ICTracepoint && targetBreakpoint.isTracepoint()
				&& isPlatformTracepoint((ICTracepoint) platformBreakpoint, targetBreakpoint, fileName)) {
			return true;
		}
		if (platformBreakpoint instanceof ICDynamicPrintf && targetBreakpoint.isDynamicPrintf()
				&& isPlatformDynamicPrintf((ICDynamicPrintf) platformBreakpoint, targetBreakpoint, fileName)) {
			return true;
		}
		if (platformBreakpoint instanceof ICWatchpoint && targetBreakpoint.isWatchpoint()
				&& isPlatformWatchpoint((ICWatchpoint) platformBreakpoint, targetBreakpoint)) {
			return true;
		}
		if (platformBreakpoint instanceof ICLineBreakpoint && !targetBreakpoint.isWatchpoint()
				&& !isCatchpoint(targetBreakpoint) && !targetBreakpoint.isTracepoint()
				&& !targetBreakpoint.isDynamicPrintf()
				&& isPlatformLineBreakpoint((ICLineBreakpoint) platformBreakpoint, targetBreakpoint, fileName)) {
			return true;
		}
		return false;
	}

	private ICBreakpoint createPlatformBreakpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		ICBreakpoint bp = createPlatformBreakpoint0(fileName, miBpt);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(bp);
		return bp;
	}

	/**
	 * Create the platform breakpoint, but don't register it with breakpoint manager. This method is called when the
	 * synchronizer needs to create a new platform breakpoint for a new breakpoint created by the user in the CLI
	 * interface (in response to =breakpoint-created event).
	 *
	 * If further fine tuning on the created breakpoint is needed, consider overriding
	 * {@link #targetBreakpointCreated(MIBreakpoint)} or even replacing the entire breakpoint synchronizer.
	 *
	 * @param fileName
	 *            the name of the file that breakpoint was inserted into, as determined by source lookup
	 * @param miBpt
	 *            the MI breakpoint created
	 * @return the newly created Platform breakpoint
	 * @throws CoreException
	 * @since 5.3
	 */
	protected ICBreakpoint createPlatformBreakpoint0(String fileName, MIBreakpoint miBpt) throws CoreException {
		if (miBpt.isWatchpoint()) {
			return createPlatformWatchpoint(fileName, miBpt);
		} else if (miBpt.isTracepoint()) {
			return createPlatformTracepoint(fileName, miBpt);
		} else if (miBpt.isDynamicPrintf()) {
			return createPlatformDynamicPrintf(fileName, miBpt);
		} else {
			return createPlatformLocationBreakpoint(fileName, miBpt);
		}
	}

	private ICBreakpoint createPlatformLocationBreakpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		if (isAddressBreakpoint(miBpt)) {
			return createPlatformAddressBreakpoint(fileName, miBpt);
		} else if (isFunctionBreakpoint(miBpt)) {
			return createPlatformFunctionBreakpoint(fileName, miBpt);
		} else {
			return createPlatformLineBreakpoint(fileName, miBpt);
		}
	}

	private ICBreakpoint createPlatformAddressBreakpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = getResource(fileName);

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		try {
			return CDIDebugModel.createAddressBreakpoint(null, null, resource, type,
					getPlatformAddress(miBpt.getAddress()), miBpt.isEnabled(), miBpt.getIgnoreCount(),
					miBpt.getCondition(), false);
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.getUniqueIdentifier(),
					String.format("Invalid breakpoint address: %s", miBpt.getAddress()))); //$NON-NLS-1$
		}
	}

	private ICBreakpoint createPlatformFunctionTracepoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = getResource(fileName);

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		return CDIDebugModel.createFunctionTracepoint(fileName, resource, type, getFunctionName(miBpt), -1, -1,
				getLineNumber(miBpt), miBpt.isEnabled(), miBpt.getIgnoreCount(), miBpt.getCondition(), false);
	}

	private ICBreakpoint createPlatformLineTracepoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = getResource(fileName);

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		return CDIDebugModel.createLineTracepoint(fileName, resource, type, getLineNumber(miBpt), miBpt.isEnabled(),
				miBpt.getIgnoreCount(), miBpt.getCondition(), false);
	}

	private ICBreakpoint createPlatformTracepoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		if (isAddressBreakpoint(miBpt)) {
			return createPlatformAddressTracepoint(fileName, miBpt);
		} else if (isFunctionBreakpoint(miBpt)) {
			return createPlatformFunctionTracepoint(fileName, miBpt);
		} else {
			return createPlatformLineTracepoint(fileName, miBpt);
		}
	}

	private ICBreakpoint createPlatformAddressTracepoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = getResource(fileName);

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		try {
			return CDIDebugModel.createAddressTracepoint(null, null, resource, type, getLineNumber(miBpt),
					getPlatformAddress(miBpt.getAddress()), miBpt.isEnabled(), miBpt.getIgnoreCount(),
					miBpt.getCondition(), false);
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.getUniqueIdentifier(),
					String.format("Invalid breakpoint address: %s", miBpt.getAddress()))); //$NON-NLS-1$
		}
	}

	/**
	 * Create a new platform breakpoint for the function breakpoint. This method is
	 * called when =breakpoint-created is received from GDB and there is not already
	 * a matching platform breakpoint
	 *
	 * @param fileName
	 *            resolved filename
	 * @param miBpt
	 *            breakpoint info from GDB, must be one for which
	 *            {@link #isFunctionBreakpoint(MIBreakpoint)} returns true.
	 * @return new platform breakpoint
	 * @throws CoreException
	 */
	private ICBreakpoint createPlatformFunctionBreakpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource;
		String resolvedFileName;

		if (userRequestedSpecificFile(miBpt)) {
			resource = getResource(fileName);
			resolvedFileName = fileName;
		} else {
			resource = ResourcesPlugin.getWorkspace().getRoot();
			resolvedFileName = null;
		}

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		return CDIDebugModel.createFunctionBreakpoint(resolvedFileName, resource, type, getFunctionName(miBpt), -1, -1,
				getLineNumber(miBpt), miBpt.isEnabled(), miBpt.getIgnoreCount(), miBpt.getCondition(), false);
	}

	/**
	 * If the user inserted the breakpoint with a filename (e.g. "b main.c:main")
	 * then create the breakpoint with that file, otherwise the function breakpoint
	 * should be inserted in the same way as if it was done with the UI "Add
	 * Function Breakpoint (C/C++)".
	 *
	 * @param miBpt
	 *            an MI Breakpoint that is a function breakpoint
	 * @return true if the user specified file and function, false if just a
	 *         function was specified.
	 */
	private boolean userRequestedSpecificFile(MIBreakpoint miBpt) {
		assert isFunctionBreakpoint(miBpt);
		String originalLocation = miBpt.getOriginalLocation();
		return originalLocation != null && originalLocation.contains(":"); //$NON-NLS-1$
	}

	private ICBreakpoint createPlatformLineBreakpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = getResource(fileName);

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		return CDIDebugModel.createLineBreakpoint(fileName, resource, type, getLineNumber(miBpt), miBpt.isEnabled(),
				miBpt.getIgnoreCount(), miBpt.getCondition(), false);
	}

	private ICBreakpoint createPlatformDynamicPrintf(String fileName, MIBreakpoint miBpt) throws CoreException {
		if (isAddressBreakpoint(miBpt)) {
			return createPlatformAddressDynamicPrintf(fileName, miBpt);
		}
		// TODO This is currently causing problems because we think a normal dprintf is a function one
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=400628#c16 which says:
		// "synchronization of function dprintf does not work"
		//		else if (isFunctionBreakpoint(miBpt)) {
		//			return createPlatformFunctionDynamicPrintf(fileName, miBpt);
		//		}
		else {
			return createPlatformLineDynamicPrintf(fileName, miBpt);
		}
	}

	private ICBreakpoint createPlatformAddressDynamicPrintf(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = getResource(fileName);

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		try {
			return CDIDebugModel.createAddressDynamicPrintf(null, null, resource, type, getLineNumber(miBpt),
					getPlatformAddress(miBpt.getAddress()), miBpt.isEnabled(), miBpt.getIgnoreCount(),
					miBpt.getCondition(), miBpt.getPrintfString(), false);
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.getUniqueIdentifier(),
					String.format("Invalid breakpoint address: %s", miBpt.getAddress()))); //$NON-NLS-1$
		}
	}

	// Unused, see TODO in createPlatformDynamicPrintf and Bug 400628 Comment 16
	@SuppressWarnings("unused")
	private ICBreakpoint createPlatformFunctionDynamicPrintf(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = getResource(fileName);

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		return CDIDebugModel.createFunctionDynamicPrintf(fileName, resource, type, getFunctionName(miBpt), -1, -1,
				getLineNumber(miBpt), miBpt.isEnabled(), miBpt.getIgnoreCount(), miBpt.getCondition(),
				miBpt.getPrintfString(), false);
	}

	private ICBreakpoint createPlatformLineDynamicPrintf(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = getResource(fileName);

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		return CDIDebugModel.createLineDynamicPrintf(fileName, resource, type, getLineNumber(miBpt), miBpt.isEnabled(),
				miBpt.getIgnoreCount(), miBpt.getCondition(), miBpt.getPrintfString(), false);
	}

	private ICBreakpoint createPlatformWatchpoint(String fileName, MIBreakpoint miBpt) throws CoreException {
		IResource resource = getResource(fileName);

		int type = 0;
		if (miBpt.isTemporary()) {
			type |= ICBreakpointType.TEMPORARY;
		}
		if (miBpt.isHardware()) {
			type |= ICBreakpointType.HARDWARE;
		}

		return CDIDebugModel.createWatchpoint(fileName, resource, type,
				miBpt.isAccessWatchpoint() || miBpt.isWriteWatchpoint(),
				miBpt.isAccessWatchpoint() || miBpt.isReadWatchpoint(), miBpt.getExpression(), miBpt.isEnabled(),
				miBpt.getIgnoreCount(), miBpt.getCondition(), false);
	}

	/**
	 * Retrieve the breakpoint context from the given target breakpoint
	 * @param miBpt target breakpoint
	 * @return breakpoint context, or {@code null} if not available
	 * @since 5.3
	 */
	protected IBreakpointsTargetDMContext getBreakpointsTargetContext(MIBreakpoint miBpt) {
		IMIProcesses processes = getServicesTracker().getService(IMIProcesses.class);
		if (processes == null || getCommandControl() == null) {
			return null;
		}

		// For GDB  < 7.4, each process is its own breakpointTargetDMC so we need to find a the proper process
		// based on the threadId.  For GDB >= 7.4, this does not matter as we'll always end up with the global bpTargetDMC
		String threadId = (miBpt != null) ? miBpt.getThreadId() : null;
		IContainerDMContext contContext = processes.createContainerContextFromThreadId(getCommandControl().getContext(),
				threadId);
		return DMContexts.getAncestorOfType(contContext, IBreakpointsTargetDMContext.class);
	}

	public void getTargetBreakpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes,
			DataRequestMonitor<MIBreakpoint> rm) {
		Collection<MIBreakpoint> targetBreakpoints = getCreatedTargetBreakpoints(context);
		if (targetBreakpoints == null) {
			rm.done();
			return;
		}
		String type = (String) attributes.get(MIBreakpoints.BREAKPOINT_TYPE);
		if (MIBreakpoints.BREAKPOINT.equals(type)) {
			rm.done(getTargetLineBreakpoint(targetBreakpoints, (String) attributes.get(MIBreakpoints.FILE_NAME),
					(Integer) attributes.get(MIBreakpoints.LINE_NUMBER),
					(String) attributes.get(MIBreakpoints.FUNCTION), (String) attributes.get(MIBreakpoints.ADDRESS),
					(Boolean) attributes.get(MIBreakpointDMData.IS_HARDWARE),
					(Boolean) attributes.get(MIBreakpointDMData.IS_TEMPORARY)));
		} else if (MIBreakpoints.TRACEPOINT.equals(type)) {
			rm.done(getTargetTracepoint(targetBreakpoints, (String) attributes.get(MIBreakpoints.FILE_NAME),
					(Integer) attributes.get(MIBreakpoints.LINE_NUMBER),
					(String) attributes.get(MIBreakpoints.FUNCTION), (String) attributes.get(MIBreakpoints.ADDRESS),
					(Boolean) attributes.get(MIBreakpointDMData.IS_HARDWARE),
					(Boolean) attributes.get(MIBreakpointDMData.IS_TEMPORARY)));
		} else if (MIBreakpoints.DYNAMICPRINTF.equals(type)) {
			rm.done(getTargetDPrintf(targetBreakpoints, (String) attributes.get(MIBreakpoints.FILE_NAME),
					(Integer) attributes.get(MIBreakpoints.LINE_NUMBER),
					(String) attributes.get(MIBreakpoints.FUNCTION), (String) attributes.get(MIBreakpoints.ADDRESS),
					(Boolean) attributes.get(MIBreakpointDMData.IS_HARDWARE),
					(Boolean) attributes.get(MIBreakpointDMData.IS_TEMPORARY)));
		} else if (MIBreakpoints.WATCHPOINT.equals(type)) {
			rm.done(getTargetWatchpoint(targetBreakpoints, (String) attributes.get(MIBreakpoints.EXPRESSION),
					(Boolean) attributes.get(MIBreakpoints.READ), (Boolean) attributes.get(MIBreakpoints.WRITE),
					(Boolean) attributes.get(MIBreakpointDMData.IS_HARDWARE),
					(Boolean) attributes.get(MIBreakpointDMData.IS_TEMPORARY)));
		} else {
			rm.done();
		}
	}

	private MIBreakpoint getTargetLineBreakpoint(Collection<MIBreakpoint> targetBreakpoints, String fileName,
			Integer lineNumber, String function, String address, Boolean isHardware, Boolean isTemporary) {
		for (MIBreakpoint miBpt : targetBreakpoints) {
			if (!miBpt.isWatchpoint() && !isCatchpoint(miBpt) && !miBpt.isTracepoint() && !miBpt.isDynamicPrintf()
					&& compareBreakpointAttributes(miBpt, fileName, lineNumber, function, address, isHardware,
							isTemporary)) {
				return miBpt;
			}
		}
		return null;
	}

	private MIBreakpoint getTargetTracepoint(Collection<MIBreakpoint> targetBreakpoints, String fileName,
			Integer lineNumber, String function, String address, Boolean isHardware, Boolean isTemporary) {
		for (MIBreakpoint miBpt : targetBreakpoints) {
			if (miBpt.isTracepoint() && compareBreakpointAttributes(miBpt, fileName, lineNumber, function, address,
					isHardware, isTemporary)) {
				return miBpt;
			}
		}
		return null;
	}

	private MIBreakpoint getTargetDPrintf(Collection<MIBreakpoint> targetBreakpoints, String fileName,
			Integer lineNumber, String function, String address, Boolean isHardware, Boolean isTemporary) {
		for (MIBreakpoint miBpt : targetBreakpoints) {
			if (miBpt.isDynamicPrintf() && compareBreakpointAttributes(miBpt, fileName, lineNumber, function, address,
					isHardware, isTemporary)) {
				return miBpt;
			}
		}
		return null;
	}

	private MIBreakpoint getTargetWatchpoint(Collection<MIBreakpoint> targetBreakpoints, String expression,
			boolean readAccess, boolean writeAccess, Boolean isHardware, Boolean isTemporary) {
		for (MIBreakpoint miBpt : targetBreakpoints) {
			if (!miBpt.isWatchpoint()) {
				continue;
			}
			if (expression == null || !expression.equals(miBpt.getExpression())) {
				continue;
			}
			if (readAccess && writeAccess && !miBpt.isAccessWatchpoint()) {
				continue;
			}
			if (readAccess && !writeAccess && !miBpt.isReadWatchpoint()) {
				continue;
			}
			if (!readAccess && writeAccess && !miBpt.isWriteWatchpoint()) {
				continue;
			}
			if (!compareBreakpointTypeAttributes(miBpt, isHardware, isTemporary)) {
				continue;
			}
			return miBpt;
		}
		return null;
	}

	private boolean compareBreakpointAttributes(MIBreakpoint miBpt, String fileName, Integer lineNumber,
			String function, String address, Boolean isHardware, Boolean isTemporary) {
		return compareBreakpointLocationAttributes(miBpt, fileName, lineNumber, function, address)
				&& compareBreakpointTypeAttributes(miBpt, isHardware, isTemporary);
	}

	private boolean compareBreakpointLocationAttributes(MIBreakpoint miBpt, String fileName, Integer lineNumber,
			String function, String address) {
		if (isFunctionBreakpoint(miBpt) && (function == null || !function.equals(getFunctionName(miBpt)))) {
			return false;
		}
		if (isAddressBreakpoint(miBpt)
				&& (address == null || !address.equals(getPlatformAddress(miBpt.getAddress()).toHexAddressString()))) {
			return false;
		}
		if (isLineBreakpoint(miBpt)) {
			String miBptFileName = getFileName(miBpt);
			if (fileName == null || miBptFileName == null || !new File(fileName).equals(new File(miBptFileName))) {
				return false;
			}
			if (lineNumber == null || lineNumber.intValue() != getLineNumber(miBpt)) {
				return false;
			}
		}
		return true;
	}

	private boolean compareBreakpointTypeAttributes(MIBreakpoint miBpt, Boolean isHardware, Boolean isTemporary) {
		if ((isHardware == null && miBpt.isHardware())
				|| (isHardware != null && isHardware.booleanValue() != miBpt.isHardware())) {
			return false;
		}
		if ((isTemporary == null && miBpt.isTemporary())
				|| (isTemporary != null && isTemporary.booleanValue() != miBpt.isTemporary())) {
			return false;
		}
		return true;
	}

	public void removeCreatedTargetBreakpoint(IBreakpointsTargetDMContext context, MIBreakpoint miBpt) {
		Map<String, MIBreakpoint> map = fCreatedTargetBreakpoints.get(context);
		if (map != null) {
			map.remove(miBpt.getNumber());
		}
	}

	private boolean isPlatformLineBreakpoint(ICLineBreakpoint plBpt, MIBreakpoint miBpt, String fileName) {
		if (plBpt instanceof ICAddressBreakpoint) {
			return isAddressBreakpoint(miBpt) ? isPlatformAddressBreakpoint((ICAddressBreakpoint) plBpt, miBpt) : false;
		}
		if (plBpt instanceof ICFunctionBreakpoint) {
			return isFunctionBreakpoint(miBpt)
					? isPlatformFunctionBreakpoint((ICFunctionBreakpoint) plBpt, miBpt, fileName)
					: false;
		}
		try {
			if (fileName == null || plBpt.getSourceHandle() == null
					|| !new File(fileName).equals(new File(plBpt.getSourceHandle()))) {
				return false;
			}
			if (plBpt.getLineNumber() != getLineNumber(miBpt)) {
				return false;
			}
			return true;
		} catch (CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
		return false;
	}

	private boolean isPlatformFunctionBreakpoint(ICFunctionBreakpoint plBpt, MIBreakpoint miBpt, String fileName) {
		try {
			if (!Objects.equals(plBpt.getFunction(), getFunctionName(miBpt))) {
				return false;
			}
			if (userRequestedSpecificFile(miBpt)) {
				if (fileName == null || plBpt.getSourceHandle() == null
						|| !new File(fileName).equals(new File(plBpt.getSourceHandle()))) {
					return false;
				}
			} else {
				if (plBpt.getSourceHandle() != null) {
					return false;
				}
			}
			return true;
		} catch (CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
		return false;
	}

	private boolean isPlatformAddressBreakpoint(ICAddressBreakpoint plBpt, MIBreakpoint miBpt) {
		try {
			return (plBpt.getAddress() != null
					&& plBpt.getAddress().equals(getPlatformAddress(miBpt.getAddress()).toHexAddressString()));
		} catch (CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
		return false;
	}

	private boolean isPlatformWatchpoint(ICWatchpoint plBpt, MIBreakpoint miBpt) {
		try {
			if (plBpt.getExpression() != null && plBpt.getExpression().equals(miBpt.getExpression())) {
				if (miBpt.isAccessWatchpoint()) {
					return plBpt.isWriteType() && plBpt.isReadType();
				} else if (miBpt.isReadWatchpoint()) {
					return !plBpt.isWriteType() && plBpt.isReadType();
				} else if (miBpt.isWriteWatchpoint()) {
					return plBpt.isWriteType() && !plBpt.isReadType();
				}
			}
		} catch (CoreException e) {
			GdbPlugin.log(e.getStatus());
		}
		return false;
	}

	private boolean isPlatformTracepoint(ICTracepoint plBpt, MIBreakpoint miBpt, String fileName) {
		return isPlatformLineBreakpoint(plBpt, miBpt, fileName);
	}

	private boolean isPlatformDynamicPrintf(ICDynamicPrintf plBpt, MIBreakpoint miBpt, String fileName) {
		return isPlatformLineBreakpoint(plBpt, miBpt, fileName);
	}

	/** @since 5.0 */
	public boolean isTargetBreakpointDeleted(IBreakpointsTargetDMContext context, String bpId, boolean remove) {
		Set<String> set = fDeletedTargetBreakpoints.get(context);
		if (set != null) {
			return (remove) ? set.remove(bpId) : set.contains(bpId);
		}
		return false;
	}

	/**
	 * Returns the list of tracepoint actions generated from the given command string.
	 * If the corresponding action for a command doesn't exist in TracepointActionManager
	 * the new action is created and added.
	 *
	 * @param commands list of gdb tracepoint commands separated by TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
	 */
	private ITracepointAction[] getActionsFromCommands(String[] commands) {
		List<ITracepointAction> list = new ArrayList<>();
		TracepointActionManager tam = TracepointActionManager.getInstance();
		WhileSteppingAction whileStepping = null;
		List<ITracepointAction> subActions = null;
		for (String command : commands) {
			// Check if an action for this command exists
			boolean found = false;
			for (ITracepointAction action : tam.getActions()) {
				if (command.equals(action.getSummary())) {
					if (whileStepping == null || subActions == null) {
						list.add(action);
					} else {
						subActions.add(action);
					}
					found = true;
					break;
				}
			}
			if (!found) {
				// Create a new action if an action for this command doesn't exists
				ITracepointAction action = null;
				if (command.startsWith(TC_COLLECT)) {
					action = createCollectAction(command.substring(TC_COLLECT.length()));
				} else if (command.startsWith(TC_TEVAL)) {
					action = createEvaluateAction(command.substring(TC_TEVAL.length()));
				} else if (command.startsWith(TC_WHILE_STEPPING)) {
					whileStepping = createWhileSteppingAction(command.substring(TC_WHILE_STEPPING.length()));
					if (whileStepping != null) {
						subActions = new ArrayList<>();
					}
				} else if (command.equals(TC_END)) {
					if (whileStepping == null || subActions == null) {
						continue;
					}
					StringBuilder sb = new StringBuilder();
					boolean first = true;
					for (ITracepointAction a : subActions) {
						if (first) {
							first = false;
						} else {
							sb.append(',');
						}
						sb.append(a.getName());
					}
					whileStepping.setSubActionsNames(sb.toString());
					whileStepping.setSubActionsContent(sb.toString());
					action = whileStepping;
					// Search for existing action for this 'while-stepping' command
					for (ITracepointAction a : tam.getActions()) {
						if (whileStepping.getSummary().equals(a.getSummary())) {
							action = a;
							found = true;
							break;
						}
					}
					whileStepping = null;
					subActions.clear();
					subActions = null;
				}
				if (action != null) {
					if (!found) {
						TracepointActionManager.getInstance().addAction(action);
					}
					if (whileStepping == null || subActions == null) {
						list.add(action);
					} else {
						subActions.add(action);
					}
				}
			}
			TracepointActionManager.getInstance().saveActionData();
		}
		return list.toArray(new ITracepointAction[list.size()]);
	}

	private CollectAction createCollectAction(String collectStr) {
		CollectAction action = new CollectAction();
		action.setName(TracepointActionManager.getInstance().makeUniqueActionName(action.getDefaultName()));
		action.setCollectString(collectStr);
		return action;
	}

	private EvaluateAction createEvaluateAction(String evalStr) {
		EvaluateAction action = new EvaluateAction();
		action.setName(TracepointActionManager.getInstance().makeUniqueActionName(action.getDefaultName()));
		action.setEvalString(evalStr);
		return action;
	}

	private WhileSteppingAction createWhileSteppingAction(String str) {
		WhileSteppingAction action = new WhileSteppingAction();
		action.setName(TracepointActionManager.getInstance().makeUniqueActionName(action.getDefaultName()));
		try {
			action.setStepCount(Integer.parseInt(str.trim()));
		} catch (NumberFormatException e) {
			return null;
		}
		return action;
	}

	protected void getSource(IBreakpointsTargetDMContext bpTargetDMC, final String debuggerPath,
			final DataRequestMonitor<String> rm) {

		ISourceLookup sourceLookup = getServicesTracker().getService(ISourceLookup.class);
		if (sourceLookup == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Source lookup service is not available")); //$NON-NLS-1$
			rm.done();
			return;
		}

		ISourceLookupDMContext srcDmc = DMContexts.getAncestorOfType(bpTargetDMC, ISourceLookupDMContext.class);
		if (srcDmc == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "No source lookup context")); //$NON-NLS-1$
			rm.done();
			return;
		}

		if (debuggerPath == null || debuggerPath.isEmpty()) {
			rm.done();
			return;
		}

		sourceLookup.getSource(srcDmc, debuggerPath, new DataRequestMonitor<Object>(getExecutor(), rm) {
			@Override
			@ConfinedToDsfExecutor("fExecutor")
			protected void handleCompleted() {
				String fileName = null;
				if (isSuccess()) {
					if (getData() instanceof IFile) {
						fileName = ((IFile) getData()).getLocation().toOSString();
					} else if (getData() instanceof File) {
						fileName = ((File) getData()).getAbsolutePath();
					} else if (getData() instanceof ITranslationUnit) {
						IPath location = ((ITranslationUnit) getData()).getLocation();
						if (location != null) {
							fileName = location.toOSString();
						}
					} else if (getData() instanceof LocalFileStorage) {
						fileName = ((LocalFileStorage) getData()).getFile().getAbsolutePath();
					}
				}
				rm.setData((fileName != null && !fileName.isEmpty()) ? fileName : debuggerPath);
				rm.done();
			}
		});
	}

	/**
	 * Return true if target breakpoint is a function breakpoint
	 * @param miBpt target breakpoint
	 * @return true if this is a function breakpoint
	 * @since 5.3
	 */
	protected boolean isFunctionBreakpoint(MIBreakpoint miBpt) {
		String origFunction = getFunctionFromOriginalLocation(miBpt.getOriginalLocation());
		if (miBpt.getFunction().isEmpty()) {
			return !origFunction.isEmpty();
		}
		String function = miBpt.getFunction();
		// For C++ the function name for "break x" is reported as "x()".
		// To compare it to the name retrieved from the original location
		// we need to remove "()".
		int index = function.indexOf('(');
		if (index > 0 && origFunction.indexOf('(') == -1) {
			return function.substring(0, index).equals(origFunction);
		}
		return function.equals(origFunction);
	}

	/**
	 * Return true if target breakpoint is an address breakpoint
	 * @param miBpt target breakpoint
	 * @return true if this is an address breakpoint
	 * @since 5.3
	 */
	protected boolean isAddressBreakpoint(MIBreakpoint miBpt) {
		return miBpt.getOriginalLocation().startsWith("*"); //$NON-NLS-1$
	}

	/**
	 * Return true if target breakpoint is a line breakpoint
	 * @param miBpt target breakpoint
	 * @return true if this is a line breakpoint
	 * @since 5.3
	 */
	protected boolean isLineBreakpoint(MIBreakpoint miBpt) {
		return !isFunctionBreakpoint(miBpt) && !isAddressBreakpoint(miBpt);
	}

	private IAddress getPlatformAddress(String miAddress) {
		int radix = 10;
		if (miAddress.startsWith("0x")) { //$NON-NLS-1$
			radix = 16;
			miAddress = miAddress.substring(2);
		}
		return new Addr64(new BigInteger(miAddress, radix));
	}

	private boolean isBreakpointTargetTracked(IBreakpointsTargetDMContext btTargetDMC) {
		return fTrackedTargets.contains(btTargetDMC);
	}

	/**
	 * Obtain the file name of the target breakpoint.
	 *
	 * @param miBpt target breakpoint
	 * @return file name
	 * @since 5.3
	 */
	protected String getFileName(MIBreakpoint miBpt) {
		String fileName = (miBpt.getFullName() != null && !miBpt.getFullName().isEmpty()) ? miBpt.getFullName()
				: miBpt.getFile();
		if (fileName != null && !fileName.isEmpty()) {
			return fileName;
		}
		// When a breakpoint is set from the console on an invalid file both
		// 'file' and 'fullname' attributes are not available, we need to parse
		// the 'original-location' attribute to retrieve the file name.
		String origLocation = miBpt.getOriginalLocation();
		if (origLocation.isEmpty()) {
			// Shouldn't happen
			GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Invalid 'original-location' attribute")); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
		if (origLocation.startsWith("*")) { //$NON-NLS-1$
			// Address breakpoint
			return ""; //$NON-NLS-1$
		}
		int index = origLocation.lastIndexOf(':');
		return (index > 0) ? origLocation.substring(0, index) : ""; //$NON-NLS-1$
	}

	/**
	 * Obtain the line number of the target breakpoint.
	 *
	 * @param miBpt target breakpoint
	 * @return line number
	 * @since 5.3
	 */
	protected int getLineNumber(MIBreakpoint miBpt) {
		int lineNumber = miBpt.getLine();
		if (lineNumber != -1) {
			return lineNumber;
		}
		// When a breakpoint is set from the console on an invalid file
		// the 'line' attributes is not available, we need to parse
		// the 'original-location' attribute to retrieve the line number.
		String origLocation = miBpt.getOriginalLocation();
		if (origLocation.isEmpty()) {
			// Shouldn't happen
			GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Invalid 'original-location' attribute")); //$NON-NLS-1$
			return -1;
		}
		if (origLocation.startsWith("*")) { //$NON-NLS-1$
			// Address breakpoint
			return -1;
		}
		int index = origLocation.lastIndexOf(':');
		if (index > 0 && origLocation.length() > index + 1) {
			try {
				return Integer.valueOf(origLocation.substring(index + 1, origLocation.length())).intValue();
			} catch (NumberFormatException e) {
				// not a line breakpoint
			}
		}
		return -1;
	}

	/**
	 * Obtain the function name of the target breakpoint.
	 *
	 * @param miBpt target breakpoint
	 * @return function name
	 * @since 5.3
	 */
	protected String getFunctionName(MIBreakpoint miBpt) {
		if (miBpt.getFunction() != null && !miBpt.getFunction().isEmpty())
			return miBpt.getFunction();
		// When a function breakpoint is set from the console, the symbol associated with
		// the function may not be known to GDB. In this case the 'function' attribute is
		// not available, we need to parse the 'original-location' attribute to retrieve
		// the function name.
		return getFunctionFromOriginalLocation(miBpt.getOriginalLocation());
	}

	private IResource getResource(String fileName) {
		IResource resource = null;
		if (fileName == null || fileName.isEmpty()) {
			resource = ResourcesPlugin.getWorkspace().getRoot();
		} else {
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocationURI(new File(fileName).toURI());
			if (files.length > 0) {
				resource = files[0];
			} else {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
		}
		return resource;
	}

	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent e) {
		if (e.getDMContext() instanceof IBreakpointsTargetDMContext) {
			// Remove breakpoint entries when a breakpoint target is removed.
			// This will happen for GDB < 7.4 where the container is the breakpoint target.
			// For GDB >= 7.4, GDB is the breakpoint target and will not be removed.
			IBreakpointsTargetDMContext bpTargetDMContext = (IBreakpointsTargetDMContext) e.getDMContext();
			Map<String, MIBreakpoint> createdBreakpoints = fCreatedTargetBreakpoints.remove(bpTargetDMContext);
			if (createdBreakpoints != null) {
				createdBreakpoints.clear();
			}
			Map<String, MIBreakpoint> modifications = fPendingModifications.remove(bpTargetDMContext);
			if (modifications != null) {
				modifications.clear();
			}
			Set<String> deletedBreakpoints = fDeletedTargetBreakpoints.remove(bpTargetDMContext);
			if (deletedBreakpoints != null) {
				deletedBreakpoints.clear();
			}
		}
	}

	private String getFunctionFromOriginalLocation(String origLocation) {
		if (origLocation.isEmpty()) {
			// Shouldn't happen
			GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Invalid 'original-location' attribute")); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
		if (origLocation.startsWith("*")) { //$NON-NLS-1$
			// Address breakpoint
			return ""; //$NON-NLS-1$
		}
		int index = origLocation.lastIndexOf(':');
		String function = (index >= 0) ? origLocation.substring(index + 1) : origLocation;
		try {
			//TODO This does not work for dprintf since the output of the orginal location can look like this:
			//original-location="/home/lmckhou/runtime-TestDSF/Producer/src/Producer.cpp:100,\\"Hit line %d of /home/lmckhou/runtime-TestDSF/Producer/src/Producer.cpp\\\\n\\",100"
			Integer.valueOf(function);
			// Line breakpoint
			return ""; //$NON-NLS-1$
		} catch (NumberFormatException e) {
			// possible function breakpoint
		}
		return function;
	}

	protected boolean isCatchpoint(MIBreakpoint miBpt) {
		// Since we are using the CLI 'catch' command to set catchpoints GDB will emit
		// the 'breakpoint-created' notification even if the catchpoint is set from UI.
		// In case of 'catch' and 'throw' events the value of the 'type' attribute in
		// the breakpoint notification's data is 'breakpoint' instead of 'catchpoint'.
		// In this cases to identify the correct type we need to check the content of
		// the 'what' attribute.
		return (miBpt.isCatchpoint() || (!miBpt.isWatchpoint() && (CE_EXCEPTION_CATCH.equals(miBpt.getExpression())
				|| CE_EXCEPTION_THROW.equals(miBpt.getExpression()))));
	}

}
