/*******************************************************************************
 * Copyright (c) 2015 Jonah Graham and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jonah Graham - Initial API and implementation
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

@SuppressWarnings("restriction")
public class DsfShowVersionHandler implements IShowVersionHandler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public DsfShowVersionHandler(DsfSession session) {
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
		});

		return true;
	}
}