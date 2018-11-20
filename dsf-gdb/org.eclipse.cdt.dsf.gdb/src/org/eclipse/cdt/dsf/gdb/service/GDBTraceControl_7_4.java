/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * This class adds extra support for Trace Control for GDB 7.4
 *
 * @since 4.4
 */
public class GDBTraceControl_7_4 extends GDBTraceControl_7_2 {

	private ICommandControlService fConnection;
	private CommandFactory fCommandFactory;

	public GDBTraceControl_7_4(DsfSession session, ILaunchConfiguration config) {
		super(session, config);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(RequestMonitor requestMonitor) {
		// Register this service.
		register(
				new String[] { IGDBTraceControl.class.getName(), IGDBTraceControl2.class.getName(),
						GDBTraceControl_7_2.class.getName(), GDBTraceControl_7_4.class.getName() },
				new Hashtable<String, String>());

		fConnection = getServicesTracker().getService(ICommandControlService.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		requestMonitor.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	public void setTraceUser(final ITraceTargetDMContext context, String userName, final RequestMonitor rm) {
		if (context == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
			return;
		}

		if (isTracingCurrentlySupported() == false) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
			return;
		}

		fConnection.queueCommand(fCommandFactory.createMIGDBSetTraceUser(context, userName),
				new ImmediateDataRequestMonitor<MIInfo>(rm) {
					@Override
					protected void handleSuccess() {
						getTraceStatusCache().reset(context);
						rm.done();
					}
				});
	}

	@Override
	public void setTraceNotes(final ITraceTargetDMContext context, String note, final RequestMonitor rm) {
		if (context == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Invalid context", null)); //$NON-NLS-1$
			return;
		}

		if (isTracingCurrentlySupported() == false) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Tracing not supported", null)); //$NON-NLS-1$
			return;
		}

		getTraceStatusCache().reset(context);

		fConnection.queueCommand(fCommandFactory.createMIGDBSetTraceNotes(context, note),
				new ImmediateDataRequestMonitor<MIInfo>(rm) {
					@Override
					protected void handleSuccess() {
						getTraceStatusCache().reset(context);
						rm.done();
					}
				});
	}
}
