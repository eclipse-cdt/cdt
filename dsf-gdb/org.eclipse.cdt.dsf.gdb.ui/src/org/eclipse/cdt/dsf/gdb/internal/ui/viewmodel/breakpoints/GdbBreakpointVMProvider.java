/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Mikhail Khodjaiants (Mentor), Marc Khouzam (Ericsson)
 *                        - Optionally use aggressive breakpoint filtering (Bug 360735)
 *     Marc Khouzam (Ericsson) - Add support to display proper breakpoints when dealing
 *                               with a multi-selection in the debug view (Bug 360735)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.breakpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateCountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMData;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints.BreakpointVMProvider;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.0
 */
public class GdbBreakpointVMProvider extends BreakpointVMProvider {

	final private DsfSession fSession;

	final private DsfServicesTracker fServicesTracker;

	/** Indicator that we should use aggressive breakpoint filtering */
	private boolean fUseAggressiveBpFilter = false;

	/** PropertyChangeListener to keep track of the PREF_HIDE_RUNNING_THREADS preference */
	private IPropertyChangeListener fPropertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			if (IGdbDebugPreferenceConstants.PREF_AGGRESSIVE_BP_FILTER.equals(event.getProperty())) {
				fUseAggressiveBpFilter = (Boolean) event.getNewValue();
				// Set the property in the presentation context so it can be seen by the vmnode which
				// will refresh the view
				getPresentationContext().setProperty(IGdbDebugPreferenceConstants.PREF_AGGRESSIVE_BP_FILTER,
						fUseAggressiveBpFilter);
			}
		}
	};

	public GdbBreakpointVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext,
			DsfSession session) {
		super(adapter, presentationContext);
		fSession = session;
		fServicesTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());

		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(fPropertyChangeListener);
		fUseAggressiveBpFilter = store.getBoolean(IGdbDebugPreferenceConstants.PREF_AGGRESSIVE_BP_FILTER);
	}

	@Override
	public void dispose() {
		GdbUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
		fServicesTracker.dispose();
		super.dispose();
	}

	@Override
	protected void calcFileteredBreakpoints(final DataRequestMonitor<IBreakpoint[]> rm) {
		if (Boolean.TRUE.equals(
				getPresentationContext().getProperty(IBreakpointUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION))) {
			if (fUseAggressiveBpFilter) {
				// Aggressive filtering of breakpoints.  Only return bps that are installed on the target.
				ISelection debugContext = getDebugContext();
				if (debugContext instanceof IStructuredSelection) {
					// Use a set to avoid duplicates
					final Set<IBreakpoint> bps = new HashSet<>();

					int count = 0;
					final ImmediateCountingRequestMonitor crm = new ImmediateCountingRequestMonitor(rm) {
						@Override
						protected void handleSuccess() {
							rm.done(bps.toArray(new IBreakpoint[bps.size()]));
						}
					};

					for (Object element : ((IStructuredSelection) debugContext).toList()) {

						IBreakpointsTargetDMContext bpContext = null;
						IExecutionDMContext execContext = null;
						if (element instanceof IDMVMContext) {
							bpContext = DMContexts.getAncestorOfType(((IDMVMContext) element).getDMContext(),
									IBreakpointsTargetDMContext.class);
							execContext = DMContexts.getAncestorOfType(((IDMVMContext) element).getDMContext(),
									IExecutionDMContext.class);

							if (bpContext != null && fSession.getId().equals(bpContext.getSessionId())) {
								count++;
								getInstalledBreakpoints(bpContext, execContext,
										new DataRequestMonitor<Collection<IBreakpoint>>(getExecutor(), crm) {
											@Override
											protected void handleCompleted() {
												if (isSuccess()) {
													bps.addAll(getData());
												}
												crm.done();
											}
										});
							}
						}
					}

					crm.setDoneCount(count);
				} else {
					rm.done(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
							"Invalid debug selection", //$NON-NLS-1$
							null));
				}
			} else {
				// Original behavior of bp filtering.  Return all bp of type ICBreakpoint
				IBreakpoint[] allBreakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
				List<IBreakpoint> filteredBPs = new ArrayList<>(allBreakpoints.length);
				for (IBreakpoint bp : allBreakpoints) {
					if (bp instanceof ICBreakpoint && bp.getModelIdentifier().equals(CDebugCorePlugin.PLUGIN_ID)) {
						filteredBPs.add(bp);
					}
				}
				rm.done(filteredBPs.toArray(new IBreakpoint[filteredBPs.size()]));
			}
		} else {
			super.calcFileteredBreakpoints(rm);
		}
	}

	@Override
	protected IVMNode createBreakpointVMNode() {
		return new GdbBreakpointVMNode(this);
	}

	@Override
	public void getBreakpointsForDebugContext(ISelection debugContext, final DataRequestMonitor<IBreakpoint[]> rm) {
		IExecutionDMContext _execCtx = null;
		if (debugContext instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) debugContext).getFirstElement();
			if (element instanceof IDMVMContext) {
				_execCtx = DMContexts.getAncestorOfType(((IDMVMContext) element).getDMContext(),
						IExecutionDMContext.class);
			}
		}

		if (_execCtx == null || !fSession.getId().equals(_execCtx.getSessionId())) {
			rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"Debug context doesn't contain a thread", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		final IExecutionDMContext execCtx = _execCtx;

		try {
			fSession.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					IBreakpointsExtension bpService = fServicesTracker.getService(IBreakpointsExtension.class);
					if (bpService == null) {
						rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE,
								"Breakpoints service not available", null)); //$NON-NLS-1$
						rm.done();
						return;
					}
					bpService.getExecutionContextBreakpoints(execCtx,
							new DataRequestMonitor<IBreakpoints.IBreakpointDMContext[]>(fSession.getExecutor(), rm) {
								@Override
								protected void handleSuccess() {
									MIBreakpointsManager bpManager = fServicesTracker
											.getService(MIBreakpointsManager.class);
									if (bpManager == null) {
										rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID,
												IDsfStatusConstants.INVALID_STATE, "Breakpoints service not available", //$NON-NLS-1$
												null));
										rm.done();
										return;
									}

									IBreakpoint bp = null;

									if (getData().length > 0) {
										bp = bpManager.findPlatformBreakpoint(getData()[0]);
									}

									if (bp != null) {
										rm.setData(new IBreakpoint[] { bp });
									} else {
										rm.setData(new IBreakpoint[0]);
									}
									rm.done();
								}
							});
				}
			});
		} catch (RejectedExecutionException e) {
			rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE,
					"Request for monitor: '" + toString() + "' resulted in a rejected execution exception.", e)); //$NON-NLS-1$ //$NON-NLS-2$);
			rm.done();
		}
	}

	private ISelection getDebugContext() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return DebugUITools.getDebugContextManager().getContextService(window).getActiveContext();
		}
		return StructuredSelection.EMPTY;
	}

	private void getInstalledBreakpoints(final IBreakpointsTargetDMContext targetContext,
			final IExecutionDMContext execContext, final DataRequestMonitor<Collection<IBreakpoint>> rm) {

		try {
			fSession.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					final IBreakpointsExtension bpService = fServicesTracker.getService(IBreakpointsExtension.class);
					if (bpService == null) {
						rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE,
								"Breakpoints service not available", //$NON-NLS-1$
								null));
						rm.done();
						return;
					}
					bpService.getBreakpoints(targetContext,
							new DataRequestMonitor<IBreakpointDMContext[]>(fSession.getExecutor(), rm) {

								@Override
								protected void handleSuccess() {
									final MIBreakpointsManager bpManager = fServicesTracker
											.getService(MIBreakpointsManager.class);
									if (bpManager == null) {
										rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID,
												IDsfStatusConstants.INVALID_STATE,
												"Breakpoint manager service not available", //$NON-NLS-1$
												null));
										rm.done();
										return;
									}

									if (getData().length > 0) {
										final Set<IBreakpoint> bps = new HashSet<>(getData().length);
										final CountingRequestMonitor crm = new CountingRequestMonitor(
												ImmediateExecutor.getInstance(), rm) {

											@Override
											protected void handleSuccess() {
												rm.setData(bps);
												rm.done();
											}
										};
										crm.setDoneCount(getData().length);

										for (final IBreakpointDMContext bpCtx : getData()) {
											bpService.getBreakpointDMData(bpCtx,
													new DataRequestMonitor<IBreakpointDMData>(
															ImmediateExecutor.getInstance(), crm) {

														/* (non-Javadoc)
														 * @see org.eclipse.cdt.dsf.concurrent.RequestMonitor#handleSuccess()
														 */
														@Override
														protected void handleSuccess() {
															if (getData() instanceof MIBreakpointDMData) {
																MIBreakpointDMData data = (MIBreakpointDMData) getData();
																if (!data.isPending()) {
																	bpBelongsToContext(execContext, data,
																			new ImmediateDataRequestMonitor<Boolean>(
																					crm) {
																				@Override
																				protected void handleSuccess() {
																					if (getData()) {
																						IBreakpoint bp = bpManager
																								.findPlatformBreakpoint(
																										bpCtx);
																						if (bp != null)
																							bps.add(bp);
																					}
																					crm.done();
																				}
																			});
																	return;
																}
															}
															crm.done();
														}
													});
										}
									} else {
										rm.setData(new HashSet<IBreakpoint>());
										rm.done();
									}
								}
							});
				}
			});
		} catch (RejectedExecutionException e) {
			rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE,
					"Request for monitor: '" + toString() + "' resulted in a rejected execution exception.", e)); //$NON-NLS-1$ //$NON-NLS-2$);
			rm.done();
		}
	}

	private void bpBelongsToContext(IExecutionDMContext execContext, final MIBreakpointDMData data,
			final DataRequestMonitor<Boolean> rm) {
		if (execContext == null) {
			// No execution context so accept all breakpoints
			rm.done(true);
			return;
		}

		// First check if a thread is selected as the context.  In that case, we only want to show breakpoints that
		// are applicable to that thread.
		IMIExecutionDMContext threadContext = DMContexts.getAncestorOfType(execContext, IMIExecutionDMContext.class);
		if (threadContext != null) {
			// A thread is selected.  Now make sure this breakpoint is assigned to this thread.
			if (data.getThreadId() != null && data.getThreadId().length() > 0) {
				String bpThreadId = data.getThreadId().trim();
				// A threadId of 0 means all threads of this process. We therefore will have to check
				// if this breakpoint applies to the process that is selected. But if the threadId is not 0
				// we simply make sure we have the right thread selected.
				if (!bpThreadId.equals("0")) { //$NON-NLS-1$
					String ctxThreadId = threadContext.getThreadId();
					rm.done(ctxThreadId.equals(bpThreadId));
					return;
				}
			}
		}

		// If we get here it is that the breakpoint is not assigned to a single thread.
		// We therefore make sure the breakpoint is applicable to the selected process.
		final IMIContainerDMContext containerContext = DMContexts.getAncestorOfType(execContext,
				IMIContainerDMContext.class);
		if (containerContext != null) {
			if (data.getGroupIds() != null) {
				List<String> groupIds = Arrays.asList(data.getGroupIds());
				rm.done(groupIds.contains(containerContext.getGroupId()));
			} else {
				// This happens if we are debugging a single process, so all breakpoints apply.
				rm.done(true);
			}
		} else {
			// Accept breakpoint
			rm.done(true);
		}
	}
}
