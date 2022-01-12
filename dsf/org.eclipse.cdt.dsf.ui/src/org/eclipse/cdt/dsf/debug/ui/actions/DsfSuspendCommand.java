/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems and others.
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
 *     Marc Khouzam (Ericsson) - Added support for multi-selection (Bug 330974)
 *     John Dallaway - Report command execution error (Bug 539455)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IMultiRunControl;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ISuspendHandler;

/**
 *
 * @since 1.0
 */
@Immutable
public class DsfSuspendCommand implements ISuspendHandler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public DsfSuspendCommand(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
	}

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	public void canExecute(final IEnabledStateRequest request) {
		if (request.getElements().length == 1) {
			canExecuteSingle(request);
			return;
		}

		// Handle multi-selection
		fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements(), request) {
			@Override
			public void doExecute() {
				final IMultiRunControl multiRun = fTracker.getService(IMultiRunControl.class);
				if (multiRun == null) {
					// No multi run control service: multi selection not allowed
					request.setEnabled(false);
					request.done();
					return;
				}

				// Check if some of the selections can be suspended
				multiRun.canSuspendSome(getContexts(), new ImmediateDataRequestMonitor<Boolean>() {
					@Override
					protected void handleCompleted() {
						request.setEnabled(isSuccess() && getData());
						request.done();
					}
				});
			}
		});
	}

	private void canExecuteSingle(final IEnabledStateRequest request) {
		fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) {
			@Override
			public void doExecute() {
				getRunControl().canSuspend(getContext(), new ImmediateDataRequestMonitor<Boolean>() {
					@Override
					protected void handleCompleted() {
						request.setEnabled(isSuccess() && getData());
						request.done();
					}
				});
			}
		});
	}

	@Override
	public boolean execute(final IDebugCommandRequest request) {
		if (request.getElements().length == 1) {
			executeSingle(request);
			return false;
		}

		// Handle multi-selection
		fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements(), request) {
			@Override
			public void doExecute() {
				final IMultiRunControl multiRun = fTracker.getService(IMultiRunControl.class);
				if (multiRun == null) {
					// No multi run control service: multi selection not allowed
					request.done();
					return;
				}

				multiRun.suspend(getContexts(), new ImmediateRequestMonitor() {
					@Override
					protected void handleError() {
						super.handleError();
						CDebugUtils.error(getStatus(), DsfSuspendCommand.this);
					}
				});
			}
		});
		return false;
	}

	private void executeSingle(IDebugCommandRequest request) {
		fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) {
			@Override
			public void doExecute() {
				getRunControl().suspend(getContext(), new ImmediateRequestMonitor() {
					@Override
					protected void handleError() {
						super.handleError();
						CDebugUtils.error(getStatus(), DsfSuspendCommand.this);
					}
				});
			}
		});
	}
}
