/*******************************************************************************
 * Copyright (c) 2015, 2016 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonah Graham (Kichwa Coders) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.examples.dsf.gdb.actions;

import java.util.Optional;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.examples.dsf.gdb.GDBExamplePlugin;
import org.eclipse.cdt.examples.dsf.gdb.commands.IShowVersionHandler;
import org.eclipse.cdt.examples.dsf.gdb.service.IGDBExtendedFunctions;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

public class GdbShowVersionHandler implements IShowVersionHandler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public GdbShowVersionHandler(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GDBExamplePlugin.getBundleContext(), session.getId());
	}

	public void dispose() {
		fTracker.dispose();
	}

	private Optional<ICommandControlDMContext> getContext(final IDebugCommandRequest request) {
		if (request.getElements().length != 1 || !(request.getElements()[0] instanceof IDMVMContext)) {
			return Optional.empty();
		}

		final IDMVMContext context = (IDMVMContext) request.getElements()[0];
		ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(context.getDMContext(),
				ICommandControlDMContext.class);
		if (controlDmc != null)
			return Optional.of(controlDmc);
		return Optional.empty();
	}

	@Override
	public void canExecute(final IEnabledStateRequest request) {
		final Optional<ICommandControlDMContext> context = getContext(request);
		if (!context.isPresent()) {
			request.setEnabled(false);
			request.done();
			return;
		}

		fExecutor.execute(new DsfRunnable() {
			@Override
			public void run() {
				final IGDBExtendedFunctions funcService = fTracker.getService(IGDBExtendedFunctions.class);
				if (funcService == null) {
					request.setEnabled(false);
					request.done();
				} else {
					funcService.canGetVersion(context.get(), new DataRequestMonitor<Boolean>(fExecutor, null) {
						@Override
						protected void handleCompleted() {
							if (!isSuccess()) {
								request.setEnabled(false);
							} else {
								request.setEnabled(getData());
							}
							request.done();
						}
					});
				}
			}
		});

	}

	@Override
	public boolean execute(final IDebugCommandRequest request) {
		final Optional<ICommandControlDMContext> context = getContext(request);
		if (!context.isPresent()) {
			request.done();
			return false;
		}

		fExecutor.execute(new DsfRunnable() {
			@Override
			public void run() {
				final IGDBExtendedFunctions funcService = fTracker.getService(IGDBExtendedFunctions.class);
				if (funcService == null) {
					request.done();
				} else {
					funcService.getVersion(context.get(), new DataRequestMonitor<String>(fExecutor, null) {
						@Override
						protected void handleCompleted() {
							String str;
							if (isSuccess()) {
								str = "======= GDB version: " + getData() + " ======="; //$NON-NLS-1$ //$NON-NLS-2$
							} else {
								str = "Could not obtain GDB version.  Error: " + //$NON-NLS-1$
								getStatus();
							}
							funcService.notify(context.get(), str, new RequestMonitor(fExecutor, null) {
								@Override
								protected void handleCompleted() {
									request.done();
								}
							});
						}
					});
				}
			}
		});

		/*
		 * Return true so that the show version pop-up command is immediately
		 * reenabled, this allows users to run the command multiple times
		 * without any other operations in between. There is a small chance the
		 * command may be issued again while it is still running, however with
		 * this particular command it is very fast so that is unlikely. In
		 * addition for this command no harm comes if it is issued twice.
		 *
		 * Most commands return false here because they need to be reenabled by
		 * some operation. For example continue would only be reenabled by the
		 * state change in the backend to being suspended again.
		 */
		return true;
	}
}