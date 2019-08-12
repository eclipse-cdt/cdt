/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.osgi.service.prefs.Preferences;

/**
 * Tests that we can perform different operations while the target
 * is running.
 */
@RunWith(Parameterized.class)
public class OperationsWhileTargetIsRunningTest extends BaseParametrizedTestCase {
	private DsfServicesTracker fServicesTracker;
	private IGDBProcesses fProcesses;
	private IMIContainerDMContext fContainerDmc;
	private IGDBControl fControl;

	private static final String EXEC_NAME = "TargetAvail.exe";

	private static boolean fgAutoTerminate;

	@BeforeClass
	public static void doBeforeClass() throws Exception {
		// Save the original values of the preferences used in this class
		fgAutoTerminate = Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true, null);
	}

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		final DsfSession session = getGDBLaunch().getSession();

		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), session.getId());

			fProcesses = fServicesTracker.getService(IGDBProcesses.class);
			fControl = fServicesTracker.getService(IGDBControl.class);
		};
		session.getExecutor().submit(runnable).get();

		fContainerDmc = (IMIContainerDMContext) SyncUtil.getContainerContext();

	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		if (fServicesTracker != null)
			fServicesTracker.dispose();

		// Restore the different preferences we might have changed
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, fgAutoTerminate);
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	/**
	 * Test that the restart operation works properly while the target is running, and
	 * with the option to kill GDB after the process terminates, enabled.
	 */
	@Test
	public void restartWhileTargetRunningKillGDB() throws Throwable {
		// Restart is not supported for a remote session
		if (isRemoteSession()) {
			Assert.assertFalse("Restart operation should not be allowed for a remote session", SyncUtil.canRestart());
			return;
		}

		// First set the preference to kill GDB (although it should not happen in this test)
		Preferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true);

		// The target is currently stopped.  We resume to get it running
		// then we do the restart, and confirm we are then stopped on main
		SyncUtil.resume();
		MIStoppedEvent stoppedEvent = SyncUtil.restart(getGDBLaunch());

		String func = stoppedEvent.getFrame().getFunction();
		Assert.assertTrue("Expected to be stopped at main, but is stopped at " + func, "main".equals(func));

		// Now make sure GDB is still alive
		Assert.assertTrue("GDB should have been still alive", fControl.isActive());
	}

	/**
	 * Test that the restart operation works properly while the target is running, and
	 * with the option to kill GDB after the process terminates, disabled.
	 */
	@Test
	public void restartWhileTargetRunningGDBAlive() throws Throwable {
		// Restart is not supported for a remote session
		if (isRemoteSession()) {
			Assert.assertFalse("Restart operation should not be allowed for a remote session", SyncUtil.canRestart());
			return;
		}

		// First set the preference not to kill gdb
		Preferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, false);

		// The target is currently stopped.  We resume to get it running
		// then we do the restart, and confirm we are then stopped on main
		SyncUtil.resume();
		MIStoppedEvent stoppedEvent = SyncUtil.restart(getGDBLaunch());

		String func = stoppedEvent.getFrame().getFunction();
		Assert.assertTrue("Expected to be stopped at main, but is stopped at " + func, "main".equals(func));

		// Now make sure GDB is still alive
		Assert.assertTrue("GDB should have been still alive", fControl.isActive());
	}

	/**
	 * Test that the terminate operation works properly while the target is running, and
	 * with the option to kill GDB after the process terminates, enabled.
	 */
	@Test
	public void terminateWhileTargetRunningKillGDB() throws Throwable {
		// First set the preference to kill GDB
		Preferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true);

		// The target is currently stopped.  We resume to get it running
		// then we terminate, and confirm that we shutdown right away
		SyncUtil.resume();

		ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownEventWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), ICommandControlShutdownDMEvent.class);

		// Don't use a query here.  The terminate, because it kills GDB, may not return right away
		// but that is ok because we wait for a shutdown event right after
		Runnable runnable = () -> {
			IProcessDMContext processDmc = DMContexts.getAncestorOfType(fContainerDmc, IProcessDMContext.class);
			fProcesses.terminate(processDmc, new ImmediateRequestMonitor());
		};
		fProcesses.getExecutor().execute(runnable);

		// The shutdown must happen quickly, which will confirm that it was
		// our own terminate that did it.  If it take longer, it indicates
		// that the program terminated on its own, which is not what we want.
		// See Bug 518643 for details as to length of this delay
		shutdownEventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));

		// Now make sure GDB is dead
		Assert.assertTrue("GDB should have been terminated", !fControl.isActive());
	}

	/**
	 * Test that the terminate operation works properly while the target is running, and
	 * with the option to kill GDB after the process terminates, disabled.
	 */
	@Test
	public void terminateWhileTargetRunningKeepGDBAlive() throws Throwable {
		// First set the preference not to kill gdb
		Preferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, false);

		// The target is currently stopped.  We resume to get it running
		// then we terminate the process, and confirm that there are no more processes
		SyncUtil.resume();

		ServiceEventWaitor<IExitedDMEvent> exitedEventWaitor = new ServiceEventWaitor<>(getGDBLaunch().getSession(),
				IExitedDMEvent.class);

		Query<Object> query = new Query<Object>() {
			@Override
			protected void execute(final DataRequestMonitor<Object> rm) {
				IProcessDMContext processDmc = DMContexts.getAncestorOfType(fContainerDmc, IProcessDMContext.class);
				fProcesses.terminate(processDmc, rm);
			}
		};
		{
			fProcesses.getExecutor().execute(query);
			query.get(TestsPlugin.massageTimeout(1000), TimeUnit.MILLISECONDS);
		}

		IExitedDMEvent event = exitedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
		if (!(event.getDMContext() instanceof IMIContainerDMContext)) {
			// This was the thread exited event, we want the container exited event
			event = exitedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
		}

		// Make sure this event shows that the process was terminated
		Assert.assertTrue("Process was not terminated", event.getDMContext() instanceof IMIContainerDMContext);
		IMIContainerDMContext dmc = (IMIContainerDMContext) event.getDMContext();
		Assert.assertTrue("Expected process " + fContainerDmc.getGroupId() + " but got " + dmc.getGroupId(),
				fContainerDmc.getGroupId().equals(dmc.getGroupId()));

		// Now make sure GDB is still alive
		Assert.assertTrue("GDB should have been still alive", fControl.isActive());
	}

	/**
	 * Test that the detach operation works properly while the target is running, and
	 * with the option to kill GDB after the process terminates, enabled.
	 */
	@Test
	public void detachWhileTargetRunningKillGDB() throws Throwable {
		// First set the preference to kill GDB
		Preferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true);

		// The target is currently stopped.  We resume to get it running
		// then we detach the process, and confirm that we are shutdown
		SyncUtil.resume();

		ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownEventWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), ICommandControlShutdownDMEvent.class);

		// Don't use a query here.  Because GDB will be killed, the call to detach may not return right away
		// but that is ok because we wait for a shutdown event right after
		Runnable runnable = () -> fProcesses.detachDebuggerFromProcess(fContainerDmc, new ImmediateRequestMonitor());
		fProcesses.getExecutor().execute(runnable);

		// The shutdown must happen quickly, which will confirm that it was
		// our own terminate that did it.  If it take longer, it indicates
		// that the program terminated on its own, which is not what we want.
		// See Bug 518643 for details as to length of this delay
		shutdownEventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));

		// Now make sure GDB is dead
		Assert.assertTrue("GDB should have been terminated", !fControl.isActive());
	}

	/**
	 * Test that the detach operation works properly while the target is running, and
	 * with the option to kill GDB after the process terminates, disabled.
	 */
	@Test
	public void detachWhileTargetRunningGDBAlive() throws Throwable {
		// First set the preference not to kill gdb
		Preferences node = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, false);

		// The target is currently stopped.  We resume to get it running
		// then we detach the process, and confirm that we are not longer running
		SyncUtil.resume();

		ServiceEventWaitor<IExitedDMEvent> exitedEventWaitor = new ServiceEventWaitor<>(getGDBLaunch().getSession(),
				IExitedDMEvent.class);

		Query<Object> query = new Query<Object>() {
			@Override
			protected void execute(final DataRequestMonitor<Object> rm) {
				fProcesses.detachDebuggerFromProcess(fContainerDmc, rm);
			}
		};
		{
			fProcesses.getExecutor().execute(query);
			query.get(TestsPlugin.massageTimeout(1000), TimeUnit.MILLISECONDS);
		}

		IExitedDMEvent event = exitedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
		if (!(event.getDMContext() instanceof IMIContainerDMContext)) {
			// This was the thread exited event, we want the container exited event
			event = exitedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(500));
		}

		// Make sure this event shows that the process was detached
		Assert.assertTrue("Process was not detached", event.getDMContext() instanceof IMIContainerDMContext);
		IMIContainerDMContext dmc = (IMIContainerDMContext) event.getDMContext();
		Assert.assertTrue("Expected process " + fContainerDmc.getGroupId() + " but got " + dmc.getGroupId(),
				fContainerDmc.getGroupId().equals(dmc.getGroupId()));

		// Now make sure GDB is still alive
		Assert.assertTrue("GDB should have been still alive", fControl.isActive());
	}
}
