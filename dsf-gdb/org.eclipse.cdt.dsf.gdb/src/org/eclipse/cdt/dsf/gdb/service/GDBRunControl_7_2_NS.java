/*******************************************************************************
 * Copyright (c) 2011, 2016 Ericsson and others.
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
 *     Marc Khouzam (Ericsson) - Wait for *stopped event when suspending (bug 429621)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IMultiRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Version of the non-stop runControl for GDB 7.2.
 *
 * @since 4.0
 */
public class GDBRunControl_7_2_NS extends GDBRunControl_7_0_NS {

	private ICommandControlService fConnection;
	private CommandFactory fCommandFactory;

	/**
	 * Keeps track if we are currently visualizing trace data or not
	 */
	private boolean fTraceVisualization;

	///////////////////////////////////////////////////////////////////////////
	// Initialization and shutdown
	///////////////////////////////////////////////////////////////////////////

	public GDBRunControl_7_2_NS(DsfSession session) {
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

	private void doInitialize(final RequestMonitor rm) {
		register(new String[] { IRunControl.class.getName(), IRunControl2.class.getName(),
				IMIRunControl.class.getName(), IMultiRunControl.class.getName(), GDBRunControl_7_0_NS.class.getName(),
				GDBRunControl_7_2_NS.class.getName(), }, new Hashtable<String, String>());
		fConnection = getServicesTracker().getService(ICommandControlService.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
		getSession().addServiceEventListener(this, null);
		rm.done();
	}

	@Override
	public void shutdown(final RequestMonitor rm) {
		unregister();
		getSession().removeServiceEventListener(this);
		super.shutdown(rm);
	}

	/** @since 4.1 */
	protected boolean getTraceVisualization() {
		return fTraceVisualization;
	}

	/** @since 4.1 */
	protected void setTraceVisualization(boolean visualizing) {
		fTraceVisualization = visualizing;
	}

	// Now that the flag --thread-group is globally supported
	// by GDB 7.2, we have to make sure not to use it twice.
	// Bug 340262
	@Override
	protected void doSuspend(IMIContainerDMContext context, RequestMonitor rm) {
		if (!doCanSuspend(context)) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED,
					"Given context: " + context + ", is already suspended.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		fConnection.queueCommand(fCommandFactory.createMIExecInterrupt(context),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}

	// Now that the flag --thread-group is globally supported
	// by GDB 7.2, we have to make sure not to use it twice.
	// Bug 340262
	/** @since 5.0 */
	@Override
	protected void doResume(IMIContainerDMContext context, final RequestMonitor rm) {
		if (!doCanResume(context)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE,
					"Given context: " + context + ", is already running.", null)); //$NON-NLS-1$ //$NON-NLS-2$
			rm.done();
			return;
		}

		fConnection.queueCommand(fCommandFactory.createMIExecContinue(context),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}

	/**
	 * @since 4.1
	 */
	@DsfServiceEventHandler
	public void eventDispatched(ITraceRecordSelectedChangedDMEvent e) {
		setTraceVisualization(e.isVisualizationModeEnabled());

		// Disable or re-enable run control operations if we are looking
		// at trace data or we are not, respectively.
		setRunControlOperationsEnabled(!e.isVisualizationModeEnabled());
	}

	@Override
	protected void refreshThreadStates() {
		// We should not refresh the thread state while we are visualizing trace data.
		// This is because GDB will report the state of the threads of the executing
		// program, while we should only deal with a 'fake' stopped thread 1, during
		// visualization.
		// So, simply keep the state of the threads as is until visualization stops.
		// Bug 347514
		if (getTraceVisualization() == false) {
			super.refreshThreadStates();
		}
	}
}
