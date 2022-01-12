/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.debug.core.model.IStepIntoSelectionHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.DsfSourceSelectionResolver;
import org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.DsfSourceSelectionResolver.LineLocation;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl3;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Display;

/**
 * @since 2.4
 */
public class DsfStepIntoSelectionCommand extends AbstractDebugCommand
		implements IStepIntoSelectionHandler, IDsfStepIntoSelection {
	private final DsfSession fSession;
	private final DsfServicesTracker fTracker;

	public DsfStepIntoSelectionCommand(DsfSession session) {
		fSession = session;
		fTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
	}

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		// No multiple selections allowed for Step into selection
		if (targets.length != 1) {
			return;
		}

		final IExecutionDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(),
				IExecutionDMContext.class);
		if (dmc == null) {
			return;
		}

		DsfSourceSelectionResolver resolveSelection = new DsfSourceSelectionResolver();
		// Resolve UI selection from the the UI thread
		Display.getDefault().syncExec(resolveSelection);
		if (resolveSelection.isSuccessful()) {
			LineLocation location = resolveSelection.getLineLocation();
			runToSelection(location.getFileName(), location.getLineNumber(), resolveSelection.getFunction(), dmc);
		} else {
			DsfUIPlugin.debug("DSfStepIntoSelectionCommand: Unable to resolve a selected function"); //$NON-NLS-1$
		}
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
			throws CoreException {
		// No multiple selections allowed for Step into selection
		if (targets.length != 1) {
			return false;
		}

		final IExecutionDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext) targets[0]).getDMContext(),
				IExecutionDMContext.class);
		return isExecutable(dmc);
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
	public boolean isExecutable(final IExecutionDMContext dmc) {
		if (dmc == null) {
			return false;
		}

		if (fSession != null && fSession.isActive()) {
			try {
				Query<Boolean> query = new Query<Boolean>() {
					@Override
					protected void execute(DataRequestMonitor<Boolean> rm) {
						IRunControl3 runControl = fTracker.getService(IRunControl3.class);
						if (runControl == null) {
							rm.done(false);
							return;
						}

						// The selection may not be up to date, this is indicated with
						// the selectedFunction being set to null
						runControl.canStepIntoSelection(dmc, null, 0, null, rm);
					}
				};

				fSession.getExecutor().execute(query);
				return query.get();
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
		}

		return false;
	}

	@Override
	public void runToSelection(final String fileName, final int lineLocation,
			final IFunctionDeclaration selectedFunction, final IExecutionDMContext dmc) {
		if (fSession != null && fSession.isActive()) {
			Throwable exception = null;
			try {
				Query<Object> query = new Query<Object>() {
					@Override
					protected void execute(final DataRequestMonitor<Object> rm) {
						IRunControl3 runControl = fTracker.getService(IRunControl3.class);
						if (runControl == null) {
							rm.done(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED,
									"IRunControl3 service not available", null)); //$NON-NLS-1$
							return;
						}

						boolean skipBreakpoints = DebugUITools.getPreferenceStore()
								.getBoolean(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE);
						runControl.stepIntoSelection(dmc, fileName, lineLocation, skipBreakpoints, selectedFunction,
								rm);
					}
				};

				fSession.getExecutor().execute(query);
				query.get();
			} catch (RejectedExecutionException e) {
				exception = e;
			} catch (InterruptedException e) {
				exception = e;
			} catch (ExecutionException e) {
				exception = e;
			}

			if (exception != null) {
				DsfUIPlugin.log(new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID,
						DebugException.REQUEST_FAILED, "Failed executing Step into Selection", exception)));//$NON-NLS-1$
			}
		} else {
			DsfUIPlugin.log(new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID,
					DebugException.REQUEST_FAILED, "Debug session is not active", null))); //$NON-NLS-1$
		}
	}

}
