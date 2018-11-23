/*******************************************************************************
 * Copyright (c) 2012, 2016 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 * Marc Khouzam (Ericsson) - Update tests to use long timeouts (Bug 439926)
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.commands.MITargetSelect;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CommandTimeoutTest extends BaseParametrizedTestCase {

	private static boolean fgTimeoutEnabled = false;
	private static int fgTimeout = IGdbDebugPreferenceConstants.COMMAND_TIMEOUT_VALUE_DEFAULT;
	private static boolean fgAutoTerminate;

	@BeforeClass
	public static void doBeforeClass() throws Exception {
		// Save the original values of the timeout-related preferences
		fgTimeoutEnabled = Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, false, null);
		fgTimeout = Platform.getPreferencesService().getInt(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE,
				IGdbDebugPreferenceConstants.COMMAND_TIMEOUT_VALUE_DEFAULT, null);
		fgAutoTerminate = Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true, null);
	}

	@Override
	public void doBeforeTest() throws Exception {
		removeTeminatedLaunchesBeforeTest();
		setLaunchAttributes();
		// Can't run the launch right away because each test needs to first set some
		// parameters.  The individual tests will be responsible for starting the launch.
	}

	@Override
	public void doAfterTest() throws Exception {
		// Don't call super here, as each test needs to deal with the launch in its own way

		// Restore the different preferences we might have changed
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, fgTimeoutEnabled);
		node.putInt(IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, fgTimeout);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, fgAutoTerminate);
	}

	/**
	 * Sends a command to which GDB will take a long time to reply, so as to generate a timeout.
	 * This is done after the launch has completed and while the debug session is ongoing.
	 */
	@Test
	public void commandTimedOutDuringSession() throws Exception {
		// Enable timeout
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, true);
		node.putInt(IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, 2000);

		// Note that sending a "target-select" command when a program is running will kill the program.
		// If that triggers us to kill GDB, then our testcase won't have time to timeout.
		// Therefore we set the preference to keep GDB alive even if the program is no longer running
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, false);

		doLaunch();

		final DsfSession session = getGDBLaunch().getSession();
		ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownEventWaitor = new ServiceEventWaitor<>(session,
				ICommandControlShutdownDMEvent.class);

		// Send the command that will timeout
		Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				DsfServicesTracker tracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), session.getId());
				ICommandControlService commandService = tracker.getService(ICommandControlService.class);
				tracker.dispose();
				commandService.queueCommand(new MITargetSelect(commandService.getContext(), "localhost", "1", false),
						rm);
			}
		};
		try {
			session.getExecutor().execute(query);
			query.get();
			// Cleanup in case the query does not throw the expected exception
			super.doAfterTest();
			Assert.fail("Command is expected to timeout");
		} catch (Exception e) {
			processException(e);
		}

		// Make sure we receive a shutdown event to confirm we have aborted the session
		shutdownEventWaitor.waitForEvent(TestsPlugin.massageTimeout(5000));

		// It can take a moment from when the shutdown event is received to when
		// the launch is actually terminated. Make sure that the launch does
		// terminate itself.
		assertLaunchTerminates();
	}

	/**
	 * Sends a command to which GDB will take a long time to reply, so as to generate a timeout.
	 * This is done during the launch to verify that we properly handle that case.
	 */
	@Test
	public void commandTimedOutDuringLaunch() {
		// Enable timeout
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, true);
		// Timeout must be shorter than the launch's timeout of 2 seconds (see BaseTestCase.doLaunch())
		node.putInt(IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, 1000);

		// Setup a remote launch so that it sends a "-target-remote" as part of the
		// launch steps...
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
		// ... but we won't start gdbserver, so the command will timeout
		setLaunchAttribute(ITestConstants.LAUNCH_GDB_SERVER, false);

		try {
			doLaunch();

			// Cleanup in case the launch does not throw the expected exception
			super.doAfterTest();
			Assert.fail("Launch is expected to fail");
		} catch (Exception e) {
			processException(e);
		}
	}

	/**
	 * Checks whether the given exception is an instance of {@link CoreException}
	 * with the status code 20100 which indicates that a gdb command has been timed out.
	 * 20100 comes from GDBControl.STATUS_CODE_COMMAND_TIMED_OUT which is private
	 */
	private void processException(Exception e) {
		Throwable t = getExceptionCause(e);
		if (t instanceof CoreException && ((CoreException) t).getStatus().getCode() == 20100) {
			// this is the exception we are looking for
			return;
		}
		throw new AssertionError("Unexpected exception", e);
	}

	private Throwable getExceptionCause(Throwable e) {
		Throwable current = e;
		while (true) {
			Throwable t = (current).getCause();
			if (t == null)
				break;
			current = t;
		}
		return current;
	}
}
