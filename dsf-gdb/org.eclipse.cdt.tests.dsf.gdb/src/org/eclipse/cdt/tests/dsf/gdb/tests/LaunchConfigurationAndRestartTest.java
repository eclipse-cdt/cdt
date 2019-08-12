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
 *     Ericsson			  - Initial Implementation
 *     Simon Marchi (Ericsson) - Remove a catch that just fails a test.
 *     Simon Marchi (Ericsson) - Disable tests for gdb < 7.2.
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.Intermittent;
import org.eclipse.cdt.tests.dsf.gdb.framework.IntermittentRule;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@Intermittent(repetition = 3)
@Ignore
public class LaunchConfigurationAndRestartTest extends BaseParametrizedTestCase {
	public @Rule IntermittentRule intermittentRule = new IntermittentRule();
	protected static final String EXEC_NAME = "LaunchConfigurationAndRestartTestApp.exe";
	protected static final String SOURCE_NAME = "LaunchConfigurationAndRestartTestApp.cc";

	protected static final String[] LINE_TAGS = new String[] { "FIRST_LINE_IN_MAIN", "LAST_LINE_IN_MAIN", };

	protected int FIRST_LINE_IN_MAIN;
	protected int LAST_LINE_IN_MAIN;

	// The exit code returned by the test program
	private static final int TEST_EXIT_CODE = 36;

	protected DsfSession fSession;
	protected DsfServicesTracker fServicesTracker;
	protected IExpressions fExpService;
	protected IGDBControl fGdbControl;

	// Indicates if a restart operation should be done
	// This allows us to re-use tests for restarts tests
	protected boolean fRestart;

	@Override
	public void doBeforeTest() throws Exception {
		assumeLocalSession();
		removeTeminatedLaunchesBeforeTest();
		setLaunchAttributes();
		// Can't run the launch right away because each test needs to first set some
		// parameters.  The individual tests will be responsible for starting the launch.

		// Looks up line tags in source file(s).
		clearLineTags();
		resolveLineTagLocations(SOURCE_NAME, LINE_TAGS);

		FIRST_LINE_IN_MAIN = getLineForTag("FIRST_LINE_IN_MAIN");
		LAST_LINE_IN_MAIN = getLineForTag("LAST_LINE_IN_MAIN");
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		// Set the binary
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	// This method cannot be tagged as @Before, because the launch is not
	// running yet.  We have to call this manually after all the proper
	// parameters have been set for the launch
	@Override
	protected void doLaunch() throws Exception {
		// perform the launch
		super.doLaunch();

		fSession = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

			fExpService = fServicesTracker.getService(IExpressions.class);
			fGdbControl = fServicesTracker.getService(IGDBControl.class);
		};
		fSession.getExecutor().submit(runnable).get();

		// Restart the program if we are testing such a case
		if (fRestart) {
			synchronized (this) {
				wait(1000); // XXX: horrible hack, what are we waiting for?
			}
			fRestart = false;
			SyncUtil.restart(getGDBLaunch());
		}
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		if (fServicesTracker != null)
			fServicesTracker.dispose();
	}

	// *********************************************************************
	// Below are the tests for the launch configuration.
	// *********************************************************************

	/**
	 * This test will tell the launch to set the working directory to data/launch/bin/
	 * and will verify that we can find the file LaunchConfigurationAndRestartTestApp.cpp.
	 * This will confirm that GDB has been properly configured with the working dir.
	 */
	@Test
	public void testSettingWorkingDirectory() throws Throwable {
		String dir = new File(EXEC_PATH).getAbsolutePath();
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, dir);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, dir + "/" + EXEC_NAME);

		doLaunch();

		Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fGdbControl.queueCommand(
						fGdbControl.getCommandFactory().createMIFileExecFile(fGdbControl.getContext(), EXEC_NAME), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query);
			query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * This test will verify that a launch will fail if the gdbinit file
	 * does not exist and is not called ".gdbinit".
	 */
	@Test
	public void testSourceInvalidGdbInit() throws Throwable {
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, "gdbinitThatDoesNotExist");
		try {
			doLaunch();
		} catch (CoreException e) {
			// Success of the test
			return;
		}

		fail("Launch seems to have succeeded even though the gdbinit file did not exist");
	}

	/**
	 * This test will verify that a launch does not fail if the gdbinit file
	 * is called ".gdbinit" and does not exist
	 */
	@Test
	public void testSourceDefaultGdbInit() throws Throwable {
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit");
		try {
			doLaunch();
		} catch (CoreException e) {
			throw new AssertionError("Launch has failed even though the gdbinit file has the default name of .gdbinit",
					e);
		}
	}

	/**
	 * This test will tell the launch to use data/launch/src/launchConfigTestGdbinit
	 * as the gdbinit file.  We then verify the that the content was properly read.
	 * launchConfigTestGdbinit will simply set some arguments for the program to read;
	 * the arguments are "1 2 3 4 5 6".
	 *
	 * This test is disabled for gdb.7.1 because gdb inserts an extraneous \n that messes up
	 * the launch sequence (more particularly, the byte length detection):
	 *
	 *     17-interpreter-exec console "p/x (char)-1"
	 *     ~"\n"
	 *     ~"$1 = 0xff\n"
	 *     17^done
	 */
	@Test

	public void testSourceGdbInit() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_2);
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, "data/launch/src/launchConfigTestGdbinit");
		doLaunch();

		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();

		// Check that argc is correct
		final IExpressionDMContext argcDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "argc");
		Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(argcDmc, MIExpressions.DETAILS_FORMAT), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query);
			FormattedValueDMData value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);

			// Argc should be 7: the program name and the six arguments
			assertTrue("Expected 7 but got " + value.getFormattedValue(), value.getFormattedValue().trim().equals("7"));
		}

		// Check that argv is also correct.  For simplicity we only check the last argument
		final IExpressionDMContext argvDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "argv[argc-1]");
		Query<FormattedValueDMData> query2 = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(argvDmc, MIExpressions.DETAILS_FORMAT), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query2);
			FormattedValueDMData value = query2.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			assertTrue("Expected \"6\" but got " + value.getFormattedValue(),
					value.getFormattedValue().trim().endsWith("\"6\""));
		}
	}

	/**
	 * Repeat the test testSourceGdbInit, but after a restart.
	 */
	@Test
	public void testSourceGdbInitRestart() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_2);
		fRestart = true;
		testSourceGdbInit();
	}

	/**
	 * This test will tell the launch to clear the environment variables.  We will
	 * then check that the variable $HOME cannot be found by the program.
	 */
	@Test
	public void testClearingEnvironment() throws Throwable {
		setLaunchAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
		doLaunch();

		SyncUtil.runToLocation("envTest");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		// The program has stored the content of $HOME into a variable called 'home'.
		// Let's verify this variable is 0x0 which means $HOME does not exist.
		final IExpressionDMContext exprDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "home");
		Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(exprDmc, MIExpressions.DETAILS_FORMAT), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query);
			FormattedValueDMData value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			assertTrue("Expected 0x0 but got " + value.getFormattedValue(), value.getFormattedValue().equals("0x0"));
		}
	}

	/**
	 * Repeat the test testClearingEnvironment, but after a restart.
	 */
	@Test
	public void testClearingEnvironmentRestart() throws Throwable {
		fRestart = true;
		testClearingEnvironment();
	}

	/**
	 * This test will tell the launch to set a new environment variable LAUNCHTEST.
	 * We will then check that this new variable can be read by the program.
	 */
	@Test
	public void testSettingEnvironment() throws Throwable {
		setLaunchAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);

		Map<String, String> map = new HashMap<>(1);
		map.put("LAUNCHTEST", "IS SET");
		setLaunchAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
		doLaunch();

		SyncUtil.runToLocation("envTest");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		// The program has stored the content of $LAUNCHTEST into a variable called 'launchTest'.
		// Let's verify this variable is set to "IS SET".
		final IExpressionDMContext exprDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "launchTest");
		Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(exprDmc, MIExpressions.DETAILS_FORMAT), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query);
			FormattedValueDMData value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			assertTrue("Expected a string ending with \"IS SET\" but got " + value.getFormattedValue(),
					value.getFormattedValue().trim().endsWith("\"IS SET\""));
		}

		// Check that the normal environment is there by checking that $HOME (which is stored in 'home" exists.
		final IExpressionDMContext exprDmc2 = SyncUtil.createExpression(stoppedEvent.getDMContext(), "home");
		Query<FormattedValueDMData> query2 = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(exprDmc2, MIExpressions.DETAILS_FORMAT), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query2);
			FormattedValueDMData value = query2.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			assertFalse("Expected something else than 0x0", value.getFormattedValue().equals("0x0"));
		}

	}

	/**
	 * Repeat the test testSettingEnvironment, but after a restart.
	 */
	@Test
	public void testSettingEnvironmentRestart() throws Throwable {
		fRestart = true;
		testSettingEnvironment();
	}

	/**
	 * This test will tell the launch to clear the environment variables and then
	 * set a new environment variable LAUNCHTEST.  We will then check that the variable
	 * $HOME cannot be found by the program and that the new variable LAUNCHTEST can be
	 * read by the program.
	 */
	@Test
	public void testClearingAndSettingEnvironment() throws Throwable {
		setLaunchAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);

		Map<String, String> map = new HashMap<>(1);
		map.put("LAUNCHTEST", "IS SET");
		setLaunchAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
		doLaunch();

		SyncUtil.runToLocation("envTest");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		// The program has stored the content of $LAUNCHTEST into a variable called 'launchTest'.
		// Let's verify this variable is set to "IS SET".
		final IExpressionDMContext exprDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "launchTest");
		Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(exprDmc, MIExpressions.DETAILS_FORMAT), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query);
			FormattedValueDMData value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			assertTrue("Expected a string ending with \"IS SET\" but got " + value.getFormattedValue(),
					value.getFormattedValue().trim().endsWith("\"IS SET\""));
		}

		// The program has stored the content of $HOME into a variable called 'home'.
		// Let's verify this variable is 0x0 which means it does not exist.
		final IExpressionDMContext exprDmc2 = SyncUtil.createExpression(stoppedEvent.getDMContext(), "home");
		Query<FormattedValueDMData> query2 = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(exprDmc2, MIExpressions.DETAILS_FORMAT), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query2);
			FormattedValueDMData value = query2.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			assertTrue("Expected 0x0 but got " + value.getFormattedValue(), value.getFormattedValue().equals("0x0"));
		}
	}

	/**
	 * Repeat the test testClearingAndSettingEnvironment, but after a restart.
	 */
	@Test
	public void testClearingAndSettingEnvironmentRestart() throws Throwable {
		fRestart = true;
		testClearingAndSettingEnvironment();
	}

	/**
	 * This test will tell the launch to set some arguments for the program.  We will
	 * then check that the program has the same arguments.
	 *
	 * NOTE: The main setting arguments tests are in {@link CommandLineArgsTest}, this
	 * test remains here to test interaction of command line arguments are restarting.
	 * See {@link #testSettingArgumentsRestart()}
	 */
	@Test
	public void testSettingArguments() throws Throwable {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "1 2 3\n4 5 6");
		doLaunch();

		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();

		// Check that argc is correct
		final IExpressionDMContext argcDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "argc");
		Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(argcDmc, MIExpressions.DETAILS_FORMAT), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query);
			FormattedValueDMData value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);

			// Argc should be 7: the program name and the six arguments
			assertTrue("Expected 7 but got " + value.getFormattedValue(), value.getFormattedValue().trim().equals("7"));
		}

		// Check that argv is also correct.  For simplicity we only check the last argument
		final IExpressionDMContext argvDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "argv[argc-1]");
		Query<FormattedValueDMData> query2 = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(argvDmc, MIExpressions.DETAILS_FORMAT), rm);
			}
		};
		{
			fExpService.getExecutor().execute(query2);
			FormattedValueDMData value = query2.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			assertTrue("Expected \"6\" but got " + value.getFormattedValue(),
					value.getFormattedValue().trim().endsWith("\"6\""));
		}
	}

	/**
	 * Repeat the test testSettingArguments, but after a restart.
	 */
	@Test
	public void testSettingArgumentsRestart() throws Throwable {
		fRestart = true;
		testSettingArguments();
	}

	/**
	 * This test will tell the launch to "stop on main" at method main(), which we will verify.
	 */
	@Test
	public void testStopAtMain() throws Throwable {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
		doLaunch();

		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		assertTrue(
				"Expected to stop at main:27 but got " + stoppedEvent.getFrame().getFunction() + ":"
						+ Integer.toString(stoppedEvent.getFrame().getLine()),
				stoppedEvent.getFrame().getFunction().equals("main") && stoppedEvent.getFrame().getLine() == 27);
	}

	/**
	 * Repeat the test testStopAtMain, but after a restart.
	 */
	@Test
	public void testStopAtMainRestart() throws Throwable {
		fRestart = true;
		testStopAtMain();
	}

	/**
	 * This test will tell the launch to "stop on main" at method stopAtOther(),
	 * which we will then verify.
	 */
	@Test
	public void testStopAtOther() throws Throwable {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "stopAtOther");
		doLaunch();

		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		assertTrue("Expected to stop at stopAtOther but got " + stoppedEvent.getFrame().getFunction() + ":",
				stoppedEvent.getFrame().getFunction().equals("stopAtOther"));
	}

	/**
	 * Repeat the test testStopAtOther, but after a restart.
	 */
	@Test
	public void testStopAtOtherRestart() throws Throwable {
		fRestart = true;
		testStopAtOther();
	}

	/**
	 * This test will set a breakpoint at some place in the program and will tell
	 * the launch to NOT "stop on main".  We will verify that the first stop is
	 * at the breakpoint that we set.
	 */
	@Ignore
	@Test
	public void testNoStopAtMain() throws Throwable {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
		// Set this one as well to make sure it gets ignored
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");

		// We need to set the breakpoint before the launch is started, but the only way to do that is
		// to set it in the platorm.  Ok, but how do I get an IResource that points to my binary?
		// The current workspace is the JUnit runtime workspace instead of the workspace containing
		// the JUnit tests.

		IFile fakeFile = null;
		CDIDebugModel.createLineBreakpoint(EXEC_PATH + EXEC_NAME, fakeFile, ICBreakpointType.REGULAR,
				LAST_LINE_IN_MAIN + 1, true, 0, "", true); //$NON-NLS-1$
		doLaunch();

		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		assertTrue("Expected to stop at envTest but got " + stoppedEvent.getFrame().getFunction() + ":",
				stoppedEvent.getFrame().getFunction().equals("envTest"));
	}

	/**
	 * Repeat the test testNoStopAtMain, but after a restart.
	 */
	@Ignore
	@Test
	public void testNoStopAtMainRestart() throws Throwable {
		fRestart = true;
		testNoStopAtMain();
	}

	/**
	 * Test that the exit code is available after the inferior as run to
	 * completion so that the console can use it.
	 */
	@Test
	public void testExitCodeSet() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_3);
		doLaunch();

		ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownEventWaitor = new ServiceEventWaitor<>(
				getGDBLaunch().getSession(), ICommandControlShutdownDMEvent.class);

		// The target is currently stopped.  We resume to get it running
		// and wait for a shutdown event to say execution has completed
		SyncUtil.resume();

		shutdownEventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));

		IProcess[] launchProcesses = getGDBLaunch().getProcesses();

		for (IProcess proc : launchProcesses) {
			if (proc instanceof InferiorRuntimeProcess) {
				assertThat(proc.getAttribute(IGdbDebugConstants.INFERIOR_EXITED_ATTR), is(notNullValue()));

				// Wait for the process to terminate so we can obtain its exit code
				int count = 0;
				while (count++ < 100 && !proc.isTerminated()) {
					try {
						synchronized (proc) {
							proc.wait(10);
						}
					} catch (InterruptedException ie) {
					}
				}

				int exitValue = proc.getExitValue();
				assertThat(exitValue, is(TEST_EXIT_CODE));
				return;
			}
		}
		assert false;
	}

	/**
	 * This test will confirm that we have turned on "pending breakpoints"
	 * The pending breakpoint setting only affects CLI commands so we have
	 * to test with one.  We don't have classes to set breakpoints using CLI,
	 * but we do for tracepoints, which is the same for this test.
	 *
	 * The pending breakpoint feature only works with tracepoints starting
	 * with GDB 7.0.
	 *
	 * We could run this test before 7.0 but we would have to use a breakpoint
	 * set using CLI commands.
	 */
	@Test
	public void testPendingBreakpointSetting() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_0);
		doLaunch();
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();

		final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(stoppedEvent.getDMContext(),
				IBreakpointsTargetDMContext.class);
		Query<MIBreakListInfo> query = new Query<MIBreakListInfo>() {
			@Override
			protected void execute(final DataRequestMonitor<MIBreakListInfo> rm) {
				fGdbControl.queueCommand(fGdbControl.getCommandFactory().createCLITrace(bpTargetDmc, "invalid", ""),
						new ImmediateDataRequestMonitor<CLITraceInfo>(rm) {
							@Override
							protected void handleSuccess() {
								fGdbControl.queueCommand(fGdbControl.getCommandFactory().createMIBreakList(bpTargetDmc),
										new ImmediateDataRequestMonitor<MIBreakListInfo>(rm) {
											@Override
											protected void handleSuccess() {
												rm.setData(getData());
												rm.done();
											}
										});
							}
						});
			}
		};
		{
			fExpService.getExecutor().execute(query);
			MIBreakListInfo value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			MIBreakpoint[] bps = value.getMIBreakpoints();
			assertTrue("Expected 1 breakpoint but got " + bps.length, bps.length == 1);
			assertTrue("Expending a <PENDING> breakpoint but got one at " + bps[0].getAddress(),
					bps[0].getAddress().equals("<PENDING>"));
		}
	}

	/**
	 * This test will tell the launch to "stop on main" at method main() with reverse
	 * debugging enabled.  We will verify that the launch stops at main() and that
	 * reverse debugging is enabled.
	 *
	 * In this test, the execution crosses getenv() while recording is enabled. gdb 7.0
	 * and 7.1 have trouble with that. We disable the test for those, and enable it for
	 * 7.2 and upwards.
	 */
	@Test
	public void testStopAtMainWithReverse() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_2);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE, true);
		doLaunch();

		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		// Make sure we stopped at the first line of main
		assertTrue(
				"Expected to stop at main:" + FIRST_LINE_IN_MAIN + " but got " + stoppedEvent.getFrame().getFunction()
						+ ":" + Integer.toString(stoppedEvent.getFrame().getLine()),
				stoppedEvent.getFrame().getFunction().equals("main")
						&& stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN);

		// Step a couple of times and check where we are
		final int NUM_STEPS = 3;
		stoppedEvent = SyncUtil.step(NUM_STEPS, StepType.STEP_OVER);
		assertTrue("Expected to stop at main:" + (FIRST_LINE_IN_MAIN + NUM_STEPS) + " but got "
				+ stoppedEvent.getFrame().getFunction() + ":" + Integer.toString(stoppedEvent.getFrame().getLine()),
				stoppedEvent.getFrame().getFunction().equals("main")
						&& stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN + NUM_STEPS);

		// Now step backwards to make sure reverse was enabled

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fSession, MIStoppedEvent.class);

		final int REVERSE_NUM_STEPS = 2;
		final IExecutionDMContext execDmc = stoppedEvent.getDMContext();
		Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fGdbControl.queueCommand(
						fGdbControl.getCommandFactory().createMIExecReverseNext(execDmc, REVERSE_NUM_STEPS), rm);
			}
		};
		{
			fGdbControl.getExecutor().execute(query);
			query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		}

		stoppedEvent = eventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));

		assertTrue("Expected to stop at main:" + (FIRST_LINE_IN_MAIN + NUM_STEPS - REVERSE_NUM_STEPS) + " but got "
				+ stoppedEvent.getFrame().getFunction() + ":" + Integer.toString(stoppedEvent.getFrame().getLine()),
				stoppedEvent.getFrame().getFunction().equals("main")
						&& stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN + NUM_STEPS - REVERSE_NUM_STEPS);
	}

	/**
	 * Repeat the test testStopAtMainWithReverse, but after a restart.
	 */
	@Test
	public void testStopAtMainWithReverseRestart() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_2);
		fRestart = true;
		testStopAtMainWithReverse();
	}

	/**
	 * This test will tell the launch to "stop on main" at method stopAtOther(),
	 * with reverse debugging enabled.  We will then verify that the launch is properly
	 * stopped at stopAtOther() and that it can go backwards until main() (this will
	 * confirm that reverse debugging was enabled at the very start).
	 *
	 * In this test, the execution crosses getenv() while recording is enabled. gdb 7.0
	 * and 7.1 have trouble with that. We disable the test for those, and enable it for
	 * 7.2 and upwards.
	 */
	@Test
	public void testStopAtOtherWithReverse() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_2);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "stopAtOther");
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE, true);
		doLaunch();

		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();

		// The initial stopped event is not the last stopped event.
		// With reverse we have to stop the program, turn on reverse and start it again.
		// Let's get the frame where we really are stopped right now.
		final IExecutionDMContext execDmc = stoppedEvent.getDMContext();
		IFrameDMData frame = SyncUtil.getFrameData(execDmc, 0);

		// Make sure we stopped at the first line of main
		assertTrue("Expected to stop at stopAtOther but got " + frame.getFunction(),
				frame.getFunction().equals("stopAtOther"));

		// Now step backwards all the way to the start to make sure reverse was enabled from the very start
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fSession, MIStoppedEvent.class);

		final int REVERSE_NUM_STEPS = 3;
		Query<MIInfo> query2 = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fGdbControl.queueCommand(
						fGdbControl.getCommandFactory().createMIExecReverseNext(execDmc, REVERSE_NUM_STEPS), rm);
			}
		};
		{
			fGdbControl.getExecutor().execute(query2);
			query2.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		}

		stoppedEvent = eventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));

		assertTrue(
				"Expected to stop at main:" + (FIRST_LINE_IN_MAIN) + " but got " + stoppedEvent.getFrame().getFunction()
						+ ":" + Integer.toString(stoppedEvent.getFrame().getLine()),
				stoppedEvent.getFrame().getFunction().equals("main")
						&& stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN);
	}

	/**
	 * Repeat the test testStopAtOtherWithReverse, but after a restart.
	 */
	@Test
	@Ignore("Fails. Investigate what it needs to wait for.")
	public void testStopAtOtherWithReverseRestart() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_2);
		fRestart = true;
		testStopAtOtherWithReverse();
	}

	/**
	 * This test will set a breakpoint at the last line of the program and will tell
	 * the launch to NOT "stop on main", with reverse debugging enabled.  We will
	 * verify that the first stop is at the last line of the program but that the program
	 * can run backwards until main() (this will confirm that reverse debugging was
	 * enabled at the very start).
	 */
	@Test
	@Ignore("TODO: this is not working because it does not insert the breakpoint propertly")
	public void testNoStopAtMainWithReverse() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_2);
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
		// Set this one as well to make sure it gets ignored
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE, true);

		// MUST SET BREAKPOINT AT LAST LINE BUT BEFORE LAUNCH IS STARTED
		// MUST SET BREAKPOINT AT LAST LINE BUT BEFORE LAUNCH IS STARTED
		// MUST SET BREAKPOINT AT LAST LINE BUT BEFORE LAUNCH IS STARTED
		// see testNoStopAtMain()

		doLaunch();

		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();

		// The initial stopped event is not the last stopped event.
		// With reverse we have to stop the program, turn on reverse and start it again.
		// Let's get the frame where we really are stopped right now.
		final IExecutionDMContext execDmc = stoppedEvent.getDMContext();
		IFrameDMData frame = SyncUtil.getFrameData(execDmc, 0);

		// Make sure we stopped at the first line of main
		assertTrue(
				"Expected to stop at main:" + LAST_LINE_IN_MAIN + " but got " + frame.getFunction() + ":"
						+ Integer.toString(frame.getLine()),
				frame.getFunction().equals("main") && frame.getLine() == LAST_LINE_IN_MAIN);

		// Now step backwards all the way to the start to make sure reverse was enabled from the very start
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fSession, MIStoppedEvent.class);

		final int REVERSE_NUM_STEPS = 3;
		Query<MIInfo> query2 = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fGdbControl.queueCommand(
						fGdbControl.getCommandFactory().createMIExecReverseNext(execDmc, REVERSE_NUM_STEPS), rm);
			}
		};
		{
			fGdbControl.getExecutor().execute(query2);
			query2.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		}

		stoppedEvent = eventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));

		assertTrue(
				"Expected to stop at main:" + (FIRST_LINE_IN_MAIN) + " but got " + stoppedEvent.getFrame().getFunction()
						+ ":" + Integer.toString(stoppedEvent.getFrame().getLine()),
				stoppedEvent.getFrame().getFunction().equals("main")
						&& stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN);
	}

	/**
	 * Repeat the test testNoStopAtMainWithReverse, but after a restart.
	 * TODO: remove ignore when parent test is fixed
	 */
	@Test
	@Ignore
	public void testNoStopAtMainWithReverseRestart() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_2);
		fRestart = true;
		testNoStopAtMainWithReverse();
	}
}
