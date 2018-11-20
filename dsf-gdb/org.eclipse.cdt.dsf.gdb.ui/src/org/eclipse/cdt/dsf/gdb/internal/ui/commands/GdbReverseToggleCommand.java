/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
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
 *     Marc Khouzam (Ericsson) - Listen for IReverseModeChangedDMEvent (Bug 399163)
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl.IReverseModeChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl2;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.services.IEvaluationService;

/**
 * Command that toggles the Reverse Debugging feature
 *
 * @since 2.1
 */
public class GdbReverseToggleCommand extends AbstractDebugCommand implements IChangeReverseMethodHandler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;
	private final DsfSession fSession;

	/** The reverse debugging method that was last returned by the service **/
	private ReverseDebugMethod fCurrentMethod;
	/** The reverse debugging method that was used before the new method was selected **/
	private ReverseDebugMethod fPreviousMethod;
	/** The reverse debugging method to be used when the toggle button is selected */
	private ReverseDebugMethod fNextMethod;

	public GdbReverseToggleCommand(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
		fSession = session;
		fCurrentMethod = ReverseDebugMethod.OFF;
		fPreviousMethod = ReverseDebugMethod.OFF;
		fNextMethod = ReverseDebugMethod.OFF;

		try {
			fExecutor.execute(new DsfRunnable() {
				@Override
				public void run() {
					fSession.addServiceEventListener(GdbReverseToggleCommand.this, null);
				}
			});
		} catch (RejectedExecutionException e) {
		}
	}

	public void dispose() {
		try {
			fExecutor.execute(new DsfRunnable() {
				@Override
				public void run() {
					fSession.removeServiceEventListener(GdbReverseToggleCommand.this);
				}
			});
		} catch (RejectedExecutionException e) {
			// Session already gone.
		}
		fTracker.dispose();
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, final IRequest request) throws CoreException {
		if (targets.length != 1) {
			return;
		}

		IDMContext dmc = ((IDMVMContext) targets[0]).getDMContext();
		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		if (controlDmc == null) {
			return;
		}

		Query<Object> setReverseMode = new Query<Object>() {
			@Override
			public void execute(final DataRequestMonitor<Object> rm) {
				final IReverseRunControl2 runControl = fTracker.getService(IReverseRunControl2.class);

				if (runControl != null) {
					final ReverseDebugMethod newMethod;
					if (fNextMethod == ReverseDebugMethod.HARDWARE) {
						String defaultValue = Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
								IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
								IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE, null);

						if (defaultValue.equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE)) {
							newMethod = ReverseDebugMethod.BRANCH_TRACE;
						} else if (defaultValue
								.equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE)) {
							newMethod = ReverseDebugMethod.PROCESSOR_TRACE;
						} else {
							newMethod = ReverseDebugMethod.GDB_TRACE;
						}
					} else {
						newMethod = fNextMethod;
					}
					runControl.enableReverseMode(controlDmc, newMethod, new RequestMonitor(fExecutor, rm) {
						@Override
						public void handleError() {
							// Call the parent function
							// Since otherwise the status is not updated
							super.handleError();
							// Here we avoid setting any status other than OK, since we want to
							// avoid the default dialog box from eclipse and we propagate the error
							// with the plugin specific code of 1, here the ReverseToggleCommandHandler
							//  interprets it as, the selected trace method is not available
							if (newMethod == ReverseDebugMethod.PROCESSOR_TRACE) {
								request.setStatus(new Status(IStatus.OK, GdbPlugin.PLUGIN_ID, 1,
										Messages.GdbReverseDebugging_ProcessorTraceReverseDebugNotAvailable, null));
							} else if (newMethod == ReverseDebugMethod.BRANCH_TRACE
									|| newMethod == ReverseDebugMethod.GDB_TRACE) {
								request.setStatus(new Status(IStatus.OK, GdbPlugin.PLUGIN_ID, 1,
										Messages.GdbReverseDebugging_HardwareReverseDebugNotAvailable, null));
							} else {
								request.setStatus(new Status(IStatus.OK, GdbPlugin.PLUGIN_ID, 1,
										Messages.GdbReverseDebugging_ReverseDebugNotAvailable, null));
							}
						}
					});
				} else {
					final IReverseRunControl runControl_old = fTracker.getService(IReverseRunControl.class);
					if (runControl_old != null) {
						if (fCurrentMethod != ReverseDebugMethod.OFF && fCurrentMethod != ReverseDebugMethod.SOFTWARE) {
							runControl_old.enableReverseMode(controlDmc, false, rm); // Switch Off tracing
							request.setStatus(new Status(IStatus.OK, GdbPlugin.PLUGIN_ID, 1,
									Messages.GdbReverseDebugging_HardwareReverseDebugNotAvailable, null));
							return;
						}
						runControl_old.isReverseModeEnabled(controlDmc, new DataRequestMonitor<Boolean>(fExecutor, rm) {
							@Override
							public void handleSuccess() {
								runControl_old.enableReverseMode(controlDmc, !getData(), rm);
							}
						});
					} else {
						rm.done();
					}
				}
			}
		};
		try {
			fExecutor.execute(setReverseMode);
			setReverseMode.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		}
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
			throws CoreException {
		if (targets.length != 1) {
			return false;
		}

		IDMContext dmc = ((IDMVMContext) targets[0]).getDMContext();
		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		final IExecutionDMContext execDmc = DMContexts.getAncestorOfType(dmc, IExecutionDMContext.class);
		if (controlDmc == null && execDmc == null) {
			return false;
		}

		Query<Boolean> canSetReverseMode = new Query<Boolean>() {
			@Override
			public void execute(DataRequestMonitor<Boolean> rm) {
				IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

				// Only allow to toggle reverse if the program is suspended.
				// When the program is running, GDB will not answer our command
				// in toggleReverse() and since it is blocking, it will hang the entire UI!
				if (runControl != null && runControl instanceof IRunControl
						&& ((IRunControl) runControl).isSuspended(execDmc)) {
					runControl.canEnableReverseMode(controlDmc, rm);
				} else {
					rm.setData(false);
					rm.done();
				}
			}
		};
		try {
			fExecutor.execute(canSetReverseMode);
			return canSetReverseMode.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		}

		return false;
	}

	@Override
	protected Object getTarget(Object element) {
		if (element instanceof IDMVMContext) {
			return element;
		}
		return null;
	}

	@Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return true;
	}

	@Override
	public boolean toggleNeedsUpdating() {
		return true;
	}

	@Override
	public boolean isReverseToggled(Object context) {
		IDMContext dmc;

		if (context instanceof IDMContext) {
			dmc = (IDMContext) context;
		} else if (context instanceof IDMVMContext) {
			dmc = ((IDMVMContext) context).getDMContext();
		} else {
			return false;
		}

		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		if (controlDmc == null) {
			return false;
		}

		Query<Boolean> isToggledQuery = new Query<Boolean>() {
			@Override
			public void execute(final DataRequestMonitor<Boolean> rm) {
				final IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

				if (runControl != null) {
					runControl.isReverseModeEnabled(controlDmc, rm);
				} else {
					rm.setData(false);
					rm.done();
				}
			}
		};
		try {
			fExecutor.execute(isToggledQuery);
			return isToggledQuery.get(500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		} catch (TimeoutException e) {
			// If we timeout, we default to false.
			// This is to avoid a deadlock
		}

		return false;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@DsfServiceEventHandler
	public void eventDispatched(IReverseModeChangedDMEvent e) {
		new WorkbenchJob("") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// Request re-evaluation of property "org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled" to update
				// visibility of reverse stepping commands.
				IEvaluationService exprService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
				if (exprService != null) {
					exprService.requestEvaluation("org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled"); //$NON-NLS-1$
				}
				// Refresh reverse toggle commands with the new state of reverse enabled.
				// This is in order to keep multiple toggle actions in UI in sync.
				ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
				if (commandService != null) {
					commandService.refreshElements("org.eclipse.cdt.debug.ui.command.reverseToggle", null); //$NON-NLS-1$
				}

				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public void setReverseDebugMethod(ReverseDebugMethod traceMethod) {
		fNextMethod = traceMethod;
	}

	@Override
	public ReverseDebugMethod getReverseDebugMethod(final Object context) {
		IDMContext dmc;

		if (context instanceof IDMContext) {
			dmc = (IDMContext) context;
		} else if (context instanceof IDMVMContext) {
			dmc = ((IDMVMContext) context).getDMContext();
		} else {
			return ReverseDebugMethod.OFF;
		}

		final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
		if (controlDmc == null) {
			return ReverseDebugMethod.OFF;
		}

		Query<ReverseDebugMethod> reverseMethodQuery = new Query<ReverseDebugMethod>() {
			@Override
			public void execute(DataRequestMonitor<ReverseDebugMethod> rm) {
				IReverseRunControl2 runControl = fTracker.getService(IReverseRunControl2.class);
				if (runControl != null) {
					runControl.getReverseTraceMethod(controlDmc,
							new ImmediateDataRequestMonitor<ReverseDebugMethod>(rm) {
								@Override
								protected void handleCompleted() {
									if (!isSuccess()) {
										rm.done(ReverseDebugMethod.OFF);
									} else {
										ReverseDebugMethod method = getData();
										if (method == ReverseDebugMethod.BRANCH_TRACE
												|| method == ReverseDebugMethod.PROCESSOR_TRACE
												|| method == ReverseDebugMethod.GDB_TRACE) {
											method = ReverseDebugMethod.HARDWARE;
										}
										rm.done(method);
									}
								}
							});
				} else {
					IReverseRunControl runControl_old = fTracker.getService(IReverseRunControl.class);
					if (runControl_old != null) {
						runControl_old.isReverseModeEnabled(controlDmc, new ImmediateDataRequestMonitor<Boolean>(rm) {
							@Override
							protected void handleCompleted() {
								if (isSuccess() && getData()) {
									rm.done(ReverseDebugMethod.SOFTWARE);
								} else {
									rm.done(ReverseDebugMethod.OFF);
								}
							}
						});
					} else {
						rm.done(ReverseDebugMethod.OFF);
					}
				}
			}
		};
		try {
			fExecutor.execute(reverseMethodQuery);
			ReverseDebugMethod currMethod = reverseMethodQuery.get(500, TimeUnit.MILLISECONDS);

			if (currMethod != fCurrentMethod) {
				fPreviousMethod = fCurrentMethod;
				fCurrentMethod = currMethod;
			}
			return fCurrentMethod;
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
		} catch (TimeoutException e) {
			// If we timeout, we default to OFF.
			// This is to avoid a deadlock
		}

		return ReverseDebugMethod.OFF;
	}

	@Override
	public ReverseDebugMethod getPreviousReverseDebugMethod(Object context) {
		return fPreviousMethod;
	}
}
