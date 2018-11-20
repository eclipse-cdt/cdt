/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson and others.
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
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBVersionInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.GDBExamplePlugin;
import org.eclipse.cdt.examples.dsf.gdb.service.command.GdbExtendedCommandFactory_6_8;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.osgi.framework.BundleContext;

public class GDBExtendedService extends AbstractDsfService implements IGDBExtendedFunctions {

	private IMICommandControl fCommandControl;
	private CommandFactory fCommandFactory;
	private CommandCache fVersionCache;

	public GDBExtendedService(DsfSession session) {
		super(session);
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

	private void doInitialize(RequestMonitor rm) {
		fCommandControl = getServicesTracker().getService(IMICommandControl.class);
		fCommandFactory = fCommandControl.getCommandFactory();

		fVersionCache = new CommandCache(getSession(), fCommandControl);
		fVersionCache.setContextAvailable(fCommandControl.getContext(), true);

		register(new String[] { IGDBExtendedFunctions.class.getName() }, new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		unregister();
		super.shutdown(rm);
	}

	@Override
	protected BundleContext getBundleContext() {
		return GDBExamplePlugin.getBundleContext();
	}

	@Override
	public void notify(ICommandControlDMContext ctx, String str, RequestMonitor rm) {
		IStatus status = new Status(IStatus.INFO, GdbPlugin.getUniqueIdentifier(),
				IGdbDebugConstants.STATUS_HANDLER_CODE, str, null);
		IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
		if (statusHandler != null) {
			try {
				statusHandler.handleStatus(status, null);
			} catch (CoreException e) {
				GDBExamplePlugin.getDefault().getLog().log(e.getStatus());
			}
		}
		rm.done();
	}

	@Override
	public void getVersion(ICommandControlDMContext ctx, final DataRequestMonitor<String> rm) {
		if (fCommandFactory instanceof GdbExtendedCommandFactory_6_8) {
			GdbExtendedCommandFactory_6_8 factory = (GdbExtendedCommandFactory_6_8) fCommandFactory;

			// Use the cache to avoid having to go to GDB more than once for a value
			// that does not change.  No need to even clear the cache since the GDB version will never change.
			fVersionCache.execute(factory.createCLIGDBVersion(ctx),
					new ImmediateDataRequestMonitor<MIGDBVersionInfo>(rm) {
						@Override
						protected void handleSuccess() {
							rm.done(getData().getVersion());
						}
					});
		} else {
			rm.done(new Status(IStatus.ERROR, GDBExamplePlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		}
	}

	@Override
	public void canGetVersion(ICommandControlDMContext ctx, DataRequestMonitor<Boolean> rm) {
		rm.done(fCommandFactory instanceof GdbExtendedCommandFactory_6_8);
	}
}
