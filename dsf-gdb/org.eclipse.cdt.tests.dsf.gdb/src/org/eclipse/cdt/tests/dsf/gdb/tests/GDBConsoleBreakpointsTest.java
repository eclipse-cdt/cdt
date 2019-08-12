/*******************************************************************************
 * Copyright (c) 2012, 2018 Mentor Graphics and others.
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
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CFunctionBreakpoint;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsAddedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsChangedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsRemovedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsUpdatedEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsSynchronizer;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * This test case verifies whether breakpoints or watchpoints set from GDB console
 * are properly synchronized with platform breakpoints.
 */
@SuppressWarnings("restriction")
@RunWith(Parameterized.class)
public class GDBConsoleBreakpointsTest extends BaseParametrizedTestCase {

	final static protected String SOURCE_NAME = "GDBMIGenericTestApp.cc";

	final static private int DEFAULT_TIMEOUT = 20000;
	final static private TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

	final static private String SOURCE_NAME_VALID = new Path(SOURCE_PATH + SOURCE_NAME).toFile().getAbsolutePath();
	final static private int LINE_NUMBER_VALID = 8;
	final static private String SOURCE_NAME_INVALID = new Path("x.c").toFile().getAbsolutePath();
	final static private int LINE_NUMBER_INVALID = 2;

	final static private String FUNCTION_VALID = "main()";
	final static private String FUNCTION_INVALID = "xxx";

	final static private String EXPRESSION_VALID = "path";

	final static private String ATTR_FILE_NAME = "FILE_NAME";
	final static private String ATTR_LINE_NUMBER = "LINE_NUMBER";
	final static private String ATTR_FUNCTION = "FUNCTION";
	final static private String ATTR_ADDRESS = "ADDRESS";
	final static private String ATTR_EXPRESSION = "EXPRESSION";
	final static private String ATTR_READ = "READ";
	final static private String ATTR_WRITE = "WRITE";

	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	protected IBreakpointsTargetDMContext fBreakpointsDmc;
	private IGDBControl fCommandControl;
	private IBreakpoints fBreakpointService;
	private MIBreakpointsSynchronizer fBreakpointsSynchronizer;

	private List<IBreakpointsChangedEvent> fBreakpointEvents = new ArrayList<>();

	@Override
	@Before
	public void doBeforeTest() throws Exception {
		deleteAllPlatformBreakpoints();

		super.doBeforeTest();

		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
			Assert.assertTrue(fServicesTracker != null);

			fCommandControl = fServicesTracker.getService(IGDBControl.class);
			Assert.assertTrue(fCommandControl != null);

			fBreakpointService = fServicesTracker.getService(IBreakpoints.class);
			Assert.assertTrue(fBreakpointService != null);

			fBreakpointsSynchronizer = fServicesTracker.getService(MIBreakpointsSynchronizer.class);
			Assert.assertTrue(fBreakpointsSynchronizer != null);

			// Register to breakpoint events
			fSession.addServiceEventListener(GDBConsoleBreakpointsTest.this, null);
		};
		fSession = getGDBLaunch().getSession();
		fSession.getExecutor().submit(runnable).get();

		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		fBreakpointsDmc = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);
		Assert.assertTrue(fBreakpointsDmc != null);
	}

	@Override
	@After
	public void doAfterTest() throws Exception {
		if (fSession != null) {
			fSession.getExecutor().submit(() -> fSession.removeServiceEventListener(GDBConsoleBreakpointsTest.this))
					.get();
		}

		fBreakpointEvents.clear();
		if (fServicesTracker != null) {
			fServicesTracker.dispose();
			fServicesTracker = null;
		}

		super.doAfterTest();

		deleteAllPlatformBreakpoints();
	}

	@Test
	public void testValidLineBreakpoints() throws Throwable {
		testConsoleBreakpoint(ICLineBreakpoint.class, getLocationBreakpointAttributes(ICLineBreakpoint.class, true));
	}

	@Test
	public void testInvalidLineBreakpoints() throws Throwable {
		testConsoleBreakpoint(ICLineBreakpoint.class, getLocationBreakpointAttributes(ICLineBreakpoint.class, false));
	}

	@Test
	public void testValidFunctionBreakpoints() throws Throwable {
		testConsoleBreakpoint(ICFunctionBreakpoint.class,
				getLocationBreakpointAttributes(ICFunctionBreakpoint.class, true));
	}

	@Test
	public void testValidFunctionNameOnlyBreakpoints() throws Throwable {
		Map<String, Object> breakpointAttributes = getLocationBreakpointAttributes(ICFunctionBreakpoint.class, true);
		breakpointAttributes.remove(ATTR_FILE_NAME);
		testConsoleBreakpoint(ICFunctionBreakpoint.class, breakpointAttributes);
	}

	@Test
	public void testInvalidFunctionBreakpoints() throws Throwable {
		testConsoleBreakpoint(ICFunctionBreakpoint.class,
				getLocationBreakpointAttributes(ICFunctionBreakpoint.class, false));
	}

	@Test
	public void testInvalidFunctionNameOnlyBreakpoints() throws Throwable {
		Map<String, Object> breakpointAttributes = getLocationBreakpointAttributes(ICFunctionBreakpoint.class, false);
		breakpointAttributes.remove(ATTR_FILE_NAME);
		testConsoleBreakpoint(ICFunctionBreakpoint.class, breakpointAttributes);
	}

	@Test
	public void testValidAddressBreakpoints() throws Throwable {
		testConsoleBreakpoint(ICAddressBreakpoint.class,
				getLocationBreakpointAttributes(ICAddressBreakpoint.class, true));
	}

	@Test
	public void testAddressBreakpointsAtZeroAddress() throws Throwable {
		testConsoleBreakpoint(ICAddressBreakpoint.class,
				getLocationBreakpointAttributes(ICAddressBreakpoint.class, false));
	}

	@Test
	public void testWriteWatchpoints() throws Throwable {
		testConsoleBreakpoint(ICWatchpoint.class, getWatchpointAttributes(ICWatchpoint.class, false, true));
	}

	@Test
	public void testReadWatchpoints() throws Throwable {
		testConsoleBreakpoint(ICWatchpoint.class, getWatchpointAttributes(ICWatchpoint.class, true, false));
	}

	@Test
	public void testAccessWatchpoints() throws Throwable {
		testConsoleBreakpoint(ICWatchpoint.class, getWatchpointAttributes(ICWatchpoint.class, true, true));
	}

	/**
	 * Shortcut to CDIDebugModel.createFunctionBreakpoint
	 */
	private static void createFunctionBreakpoint(String filename, String function) throws CoreException {
		CDIDebugModel.createFunctionBreakpoint(filename, ResourcesPlugin.getWorkspace().getRoot(), 0, function, -1, -1,
				-1, true, 0, "", true);
	}

	private List<IBreakpoint> getPlatformBreakpoints(Predicate<IBreakpoint> predicate) {
		return Arrays.asList(DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()).stream()
				.filter(predicate).collect(Collectors.toList());
	}

	private List<IBreakpoint> getPlatformFunctionBreakpoints() {
		return getPlatformBreakpoints(CFunctionBreakpoint.class::isInstance);
	}

	/**
	 * Test of the tests. This test ensures that basic creating/deleting of a function breakpoint works
	 * as expected for the other  testFunctionBreakpointsAreIndependent* tests.
	 */
	@Test
	public void testFunctionBreakpointsAreIndependent0() throws Throwable {
		List<IBreakpoint> bps = getPlatformFunctionBreakpoints();
		assertEquals(0, bps.size());

		setConsoleFunctionBreakpoint(SOURCE_NAME_VALID, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(1, bps.size());

		assertEquals(1, getTargetBreakpoints().length);

		bps.get(0).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(0, getTargetBreakpoints().length);
	}

	/**
	 * Check that console inserted breakpoint with explicit file does not share platform
	 * breakpoint that is not for a file.
	 */
	@Test
	public void testFunctionBreakpointsAreIndependent1() throws Throwable {
		List<IBreakpoint> bps = getPlatformFunctionBreakpoints();
		assertEquals(0, bps.size());

		createFunctionBreakpoint(null, FUNCTION_VALID);
		bps = getPlatformFunctionBreakpoints();
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		assertEquals(1, bps.size());

		setConsoleFunctionBreakpoint(SOURCE_NAME_VALID, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(2, bps.size());

		assertEquals(2, getTargetBreakpoints().length);

		bps.get(0).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(1, getTargetBreakpoints().length);

		bps.get(1).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(0, getTargetBreakpoints().length);
	}

	/**
	 * Check that console inserted breakpoint without explicit file does not share platform
	 * breakpoint that is for a file.
	 */
	@Test
	public void testFunctionBreakpointsAreIndependent2() throws Throwable {
		List<IBreakpoint> bps = getPlatformFunctionBreakpoints();
		assertEquals(0, bps.size());

		createFunctionBreakpoint(SOURCE_NAME_VALID, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(1, bps.size());

		setConsoleFunctionBreakpoint(null, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(2, bps.size());

		assertEquals(2, getTargetBreakpoints().length);

		bps.get(0).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(1, getTargetBreakpoints().length);

		bps.get(1).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(0, getTargetBreakpoints().length);
	}

	/**
	 * Check that console inserted breakpoint with explicit file does not share platform
	 * breakpoint that is for a different file.
	 */
	@Test
	public void testFunctionBreakpointsAreIndependent3() throws Throwable {
		List<IBreakpoint> bps = getPlatformFunctionBreakpoints();
		assertEquals(0, bps.size());

		createFunctionBreakpoint(SOURCE_NAME_VALID, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(1, bps.size());

		setConsoleFunctionBreakpoint(SOURCE_NAME_INVALID, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(2, bps.size());

		assertEquals(2, getTargetBreakpoints().length);

		bps.get(0).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(1, getTargetBreakpoints().length);

		bps.get(1).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(0, getTargetBreakpoints().length);
	}

	/**
	 * Check that console inserted breakpoint without explicit file shares platform breakpoint
	 * without file. This means that when the 1 platform breakpoint is deleted, both
	 * target breakpoints should be removed.
	 */
	@Test
	public void testFunctionBreakpointsAreIndependent4() throws Throwable {
		List<IBreakpoint> bps = getPlatformFunctionBreakpoints();
		assertEquals(0, bps.size());

		createFunctionBreakpoint(null, FUNCTION_VALID);
		bps = getPlatformFunctionBreakpoints();
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		assertEquals(1, bps.size());

		setConsoleFunctionBreakpoint(null, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(1, bps.size());

		assertEquals(2, getTargetBreakpoints().length);

		bps.get(0).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(0, getTargetBreakpoints().length);
	}

	/**
	 * Check that console inserted breakpoint with explicit file shares platform breakpoint
	 * with a file. This means that when the 1 platform breakpoint is deleted, both
	 * target breakpoints should be removed.
	 */
	@Test
	public void testFunctionBreakpointsAreIndependent5() throws Throwable {
		List<IBreakpoint> bps = getPlatformFunctionBreakpoints();
		assertEquals(0, bps.size());

		createFunctionBreakpoint(SOURCE_NAME_VALID, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(1, bps.size());

		setConsoleFunctionBreakpoint(SOURCE_NAME_VALID, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(1, bps.size());

		assertEquals(2, getTargetBreakpoints().length);

		bps.get(0).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(0, getTargetBreakpoints().length);
	}

	/**
	 * Check that console inserted breakpoint with explicit (invalid) file shares platform breakpoint
	 * with (invalid) file. This means that when the 1 platform breakpoint is deleted, both
	 * target breakpoints should be removed.
	 */
	@Test
	public void testFunctionBreakpointsAreIndependent6() throws Throwable {
		List<IBreakpoint> bps = getPlatformFunctionBreakpoints();
		assertEquals(0, bps.size());

		createFunctionBreakpoint(SOURCE_NAME_INVALID, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(1, bps.size());

		setConsoleFunctionBreakpoint(SOURCE_NAME_INVALID, FUNCTION_VALID);
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		bps = getPlatformFunctionBreakpoints();
		assertEquals(1, bps.size());

		assertEquals(2, getTargetBreakpoints().length);

		bps.get(0).delete();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		assertEquals(0, getTargetBreakpoints().length);
	}

	/**
	 * Bug 530377
	 */
	@Test
	public void testFastEvents() throws Throwable {
		List<IBreakpoint> bps = getPlatformFunctionBreakpoints();
		assertEquals(0, bps.size());

		java.nio.file.Path tempFile = Files.createTempFile("testFastEvents", "gdb");
		try {

			StringBuilder sb = new StringBuilder();
			for (int bpId = 2; bpId < 1000; bpId++) {
				sb.append(String.format("break %s\n", FUNCTION_VALID));
				sb.append(String.format("delete %s\n", bpId));
			}
			Files.write(tempFile, sb.toString().getBytes("UTF-8"));
			queueConsoleCommand("source " + tempFile.toString());
		} finally {
			Files.delete(tempFile);
		}

		bps = getPlatformFunctionBreakpoints();
		assertEquals(1, bps.size());

		IBreakpoint breakpoint = bps.get(0);

		CBreakpoint cBreakpoint = ((CBreakpoint) breakpoint);
		waitForInstallCountChange(cBreakpoint, 0);
		breakpoint.delete();
	}

	@DsfServiceEventHandler
	public void eventDispatched(IBreakpointsChangedEvent e) {
		synchronized (this) {
			fBreakpointEvents.add(e);
			notifyAll();
		}
	}

	/**
	 * Run a set of console breakpoint tests, twice. Once using events from GDB, and
	 * then again with manual refreshes to make sure we get the same results.
	 */
	private void testConsoleBreakpoint(Class<? extends ICBreakpoint> type, Map<String, Object> attributes)
			throws Throwable {
		testConsoleBreakpointStandard(type, attributes, () -> {
		});
		fBreakpointEvents.clear();

		/*
		 * Run the test without the breakpoints service handling the updates via async
		 * messages to ensure we end up with the same behaviour from refreshing
		 * manually. Because we want to test the manual refreshing behaviour, we need to
		 * stop listening to those async messages to do that we temporarily remove the
		 * breakpoint service from the event listeners
		 */
		fCommandControl.removeEventListener((IEventListener) fBreakpointService);
		try {
			testConsoleBreakpointStandard(type, attributes, () -> fBreakpointsSynchronizer.flushCache(null));
		} finally {
			fCommandControl.addEventListener((IEventListener) fBreakpointService);
		}
	}

	private void testConsoleBreakpointStandard(Class<? extends ICBreakpoint> type, Map<String, Object> attributes,
			Runnable flushCache) throws Throwable {
		// Set a console breakpoint and verify that
		// the corresponding platform breakpoint is created
		// and its install count is 1 if the breakpoint is installed
		// and 0 if the breakpoint is pending.
		// Check for a duplicate target breakpoint.
		setConsoleBreakpoint(type, attributes);
		MIBreakpoint[] miBpts = getTargetBreakpoints();
		Assert.assertTrue(miBpts.length == 1);
		flushCache.run();
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		Assert.assertTrue(getPlatformBreakpointCount() == 1);
		ICBreakpoint plBpt = findPlatformBreakpoint(type, attributes);
		Assert.assertTrue(plBpt instanceof CBreakpoint);
		// We can't rely on IBreakpointsAddedEvent because it is fired
		// before the install count is incremented.
		if (!miBpts[0].isPending()) {
			// If the target breakpoint is not pending wait
			// until the install count becomes 1.
			waitForInstallCountChange((CBreakpoint) plBpt, 1);
		} else {
			// For pending breakpoints the install count is expected to remain
			// unchanged. Give it some time and verify that it is 0.
			Thread.sleep(1000);
			Assert.assertTrue(((CBreakpoint) plBpt).getInstallCount() == 0);
		}

		// Disable the console breakpoint and verify that
		// the platform breakpoint is disabled.
		enableConsoleBreakpoint(miBpts[0].getNumber(), false);
		flushCache.run();
		waitForBreakpointEvent(IBreakpointsUpdatedEvent.class);
		Assert.assertTrue(!plBpt.isEnabled());

		// Enable the console breakpoint and verify that
		// the platform breakpoint is enabled.
		enableConsoleBreakpoint(miBpts[0].getNumber(), true);
		flushCache.run();
		waitForBreakpointEvent(IBreakpointsUpdatedEvent.class);
		Assert.assertTrue(plBpt.isEnabled());

		// Set the condition of the console breakpoint and
		// verify that the platform breakpoint's condition
		// is updated.
		setConsoleBreakpointCondition(miBpts[0].getNumber(), "path==0");
		flushCache.run();
		waitForBreakpointEvent(IBreakpointsUpdatedEvent.class);
		Assert.assertTrue(plBpt.getCondition().equals("path==0"));

		// Reset the condition of the console breakpoint and
		// verify that the platform breakpoint's condition
		// is updated.
		setConsoleBreakpointCondition(miBpts[0].getNumber(), "");
		flushCache.run();
		waitForBreakpointEvent(IBreakpointsUpdatedEvent.class);
		Assert.assertTrue(plBpt.getCondition().isEmpty());

		// Delete the console breakpoint and verify that
		// the install count of the platform breakpoint is 0.
		deleteConsoleBreakpoint(miBpts[0].getNumber());
		flushCache.run();
		waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		Assert.assertTrue(getPlatformBreakpointCount() == 1);
		plBpt = findPlatformBreakpoint(type, attributes);
		Assert.assertTrue(plBpt instanceof CBreakpoint);
		waitForInstallCountChange((CBreakpoint) plBpt, 0);

		// Make sure the breakpoint does not get re-installed
		// once it gets a notification that the platform bp changed
		// (through its install count changing) Bug 433044
		// Give it some time and verify that it is still 0.
		Thread.sleep(3000); // One second was not enough
		Assert.assertTrue("Install count no longer 0", ((CBreakpoint) plBpt).getInstallCount() == 0);

		// Set the console breakpoint again and verify that
		// the install count of the platform breakpoint is 1
		// for installed breakpoints and 0 for pending breakpoints.
		setConsoleBreakpoint(type, attributes);
		miBpts = getTargetBreakpoints();
		Assert.assertTrue(miBpts.length == 1);
		flushCache.run();
		waitForBreakpointEvent(IBreakpointsAddedEvent.class);
		Assert.assertTrue(getPlatformBreakpointCount() == 1);

		// Give a little delay to allow queued Executor operations
		// to complete before deleting the breakpoint again.
		// If we don't we may delete it so fast that the MIBreakpointsManager
		// has not yet updated its data structures
		// Bug 438934 comment 10
		Thread.sleep(500);

		plBpt = findPlatformBreakpoint(type, attributes);
		Assert.assertTrue(plBpt instanceof CBreakpoint);
		if (!miBpts[0].isPending()) {
			waitForInstallCountChange((CBreakpoint) plBpt, 1);
		} else {
			// For pending breakpoints the install count is expected to remain
			// unchanged. Give it some time and verify that it is 0.
			Thread.sleep(1000);
			Assert.assertTrue(((CBreakpoint) plBpt).getInstallCount() == 0);
		}

		// Remove the platform breakpoint and verify that
		// the target breakpoint is deleted.
		deletePlatformBreakpoint(plBpt);

		// Don't fail right away if we don't get the breakpoint event
		// as we can't tell the true cause.
		// Let further checks happen to help figure things out.

		String failure = "";
		try {
			waitForBreakpointEvent(IBreakpointsRemovedEvent.class);
		} catch (Exception e) {
			failure += e.getMessage();
		}

		int platformBp = getPlatformBreakpointCount();
		if (platformBp != 0) {
			if (!failure.isEmpty())
				failure += ", ";
			failure += "Platform breakpoints remaining: " + platformBp;
		}

		miBpts = getTargetBreakpoints();
		if (miBpts.length != 0) {
			if (!failure.isEmpty())
				failure += ", ";
			failure += "Target breakpoints remaining: " + miBpts.length;
		}

		Assert.assertTrue(failure, failure.isEmpty());
	}

	private void setConsoleLineBreakpoint(String fileName, int lineNumber) throws Throwable {
		queueConsoleCommand(String.format("break %s:%d", fileName, lineNumber));
	}

	private void setConsoleFunctionBreakpoint(String fileName, String function) throws Throwable {
		if (fileName == null) {
			queueConsoleCommand(String.format("break %s", function));
		} else {
			queueConsoleCommand(String.format("break %s:%s", fileName, function));
		}
	}

	private void setConsoleAddressBreakpoint(String address) throws Throwable {
		queueConsoleCommand(String.format("break *%s", address));
	}

	private void setConsoleWatchpoint(String expression, boolean read, boolean write) throws Throwable {
		String command = (write) ? ((read) ? "awatch" : "watch") : "rwatch";
		queueConsoleCommand(String.format("%s %s", command, expression));
	}

	private void deleteConsoleBreakpoint(String bpId) throws Throwable {
		queueConsoleCommand(String.format("delete %s", bpId));
	}

	private void enableConsoleBreakpoint(String bpId, boolean enable) throws Throwable {
		String cmd = (enable) ? "enable" : "disable";
		queueConsoleCommand(String.format("%s %s", cmd, bpId));
	}

	private void setConsoleBreakpointCondition(String bpId, String condition) throws Throwable {
		queueConsoleCommand(String.format("condition %s %s", bpId, condition));
	}

	private MIBreakpoint[] getTargetBreakpoints() throws Throwable {
		return getTargetBreakpoints(DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
	}

	private MIBreakpoint[] getTargetBreakpoints(int timeout, TimeUnit unit) throws Throwable {
		Query<MIBreakListInfo> query = new Query<MIBreakListInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIBreakListInfo> rm) {
				fCommandControl.queueCommand(fCommandControl.getCommandFactory().createMIBreakList(fBreakpointsDmc),
						rm);
			}
		};
		fSession.getExecutor().execute(query);
		return query.get(timeout, unit).getMIBreakpoints();
	}

	private void waitForBreakpointEvent(Class<? extends IBreakpointsChangedEvent> eventType) throws Exception {
		waitForBreakpointEvent(eventType, DEFAULT_TIMEOUT);
	}

	private void waitForBreakpointEvent(Class<? extends IBreakpointsChangedEvent> eventType, int timeout)
			throws Exception {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() <= start + timeout) {
			if (breakpointEventReceived(eventType)) {
				return;
			}
			synchronized (this) {
				wait(timeout);
			}
		}
		if (!breakpointEventReceived(eventType)) {
			throw new Exception(String.format("Timed out waiting for '%s' to occur.", eventType.getName()));
		}
	}

	private void queueConsoleCommand(String command) throws Throwable {
		queueConsoleCommand(command, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
	}

	private void queueConsoleCommand(final String command, int timeout, TimeUnit unit) throws Throwable {
		Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fCommandControl.queueCommand(fCommandControl.getCommandFactory()
						.createMIInterpreterExecConsole(fCommandControl.getContext(), command), rm);
			}
		};
		fSession.getExecutor().execute(query);
		query.get(timeout, unit);
	}

	private ICLineBreakpoint findPlatformLineBreakpoint(String fileName, int lineNumber) throws Throwable {
		for (IBreakpoint b : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
			if (b instanceof ICLineBreakpoint && fileName.equals(((ICLineBreakpoint) b).getSourceHandle())
					&& lineNumber == ((ICLineBreakpoint) b).getLineNumber()) {
				return (ICLineBreakpoint) b;
			}
		}
		return null;
	}

	private ICFunctionBreakpoint findPlatformFunctionBreakpoint(String fileName, String function) throws Throwable {
		for (IBreakpoint b : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
			if (b instanceof ICFunctionBreakpoint && Objects.equals(fileName, ((ICLineBreakpoint) b).getSourceHandle())
					&& function.equals(((ICLineBreakpoint) b).getFunction())) {
				return (ICFunctionBreakpoint) b;
			}
		}
		return null;
	}

	private ICAddressBreakpoint findPlatformAddressBreakpoint(String address) throws Throwable {
		Addr64 a = new Addr64(address);
		for (IBreakpoint b : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
			if (b instanceof ICAddressBreakpoint
					&& a.toHexAddressString().equals(((ICAddressBreakpoint) b).getAddress())) {
				return (ICAddressBreakpoint) b;
			}
		}
		return null;
	}

	private ICWatchpoint findPlatformWatchpoint(String expression, boolean read, boolean write) throws Throwable {
		for (IBreakpoint b : DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()) {
			if (b instanceof ICWatchpoint && ((ICWatchpoint) b).isReadType() == read
					&& ((ICWatchpoint) b).isWriteType() == write) {
				return (ICWatchpoint) b;
			}
		}
		return null;
	}

	private void deletePlatformBreakpoint(final IBreakpoint plBpt) throws Throwable {
		new Job("") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IStatus result = Status.OK_STATUS;
				try {
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(plBpt, true);
				} catch (CoreException e) {
					result = e.getStatus();
				}
				return result;
			}
		}.schedule();
	}

	private int getPlatformBreakpointCount() {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints().length;
	}

	private void waitForInstallCountChange(CBreakpoint plBpt, int expected) throws Throwable {
		waitForInstallCountChange(plBpt, expected, DEFAULT_TIMEOUT);
	}

	private void waitForInstallCountChange(CBreakpoint plBpt, int expected, long timeout) throws Throwable {
		long startMs = System.currentTimeMillis();
		while (plBpt.getInstallCount() != expected) {
			synchronized (this) {
				try {
					wait(30);
				} catch (InterruptedException ex) {
				}
				if (System.currentTimeMillis() - startMs > timeout) {
					throw new Exception("Timed out waiting for breakpoint's install count to change");
				}
			}
		}
	}

	private synchronized boolean breakpointEventReceived(Class<? extends IBreakpointsChangedEvent> eventType) {
		for (IBreakpointsChangedEvent e : fBreakpointEvents) {
			if (eventType.isAssignableFrom(e.getClass())) {
				return fBreakpointEvents.remove(e);
			}
		}
		return false;
	}

	private Map<String, Object> getLocationBreakpointAttributes(Class<? extends ICBreakpoint> type, boolean valid) {
		Map<String, Object> map = new HashMap<>();
		if (ICFunctionBreakpoint.class.equals(type)) {
			map.put(ATTR_FILE_NAME, (valid) ? SOURCE_NAME_VALID : SOURCE_NAME_INVALID);
			map.put(ATTR_FUNCTION, (valid) ? FUNCTION_VALID : FUNCTION_INVALID);
		} else if (ICAddressBreakpoint.class.equals(type)) {
			// '0x0" is not invalid address
			map.put(ATTR_ADDRESS, (valid) ? getInitialStoppedEvent().getFrame().getAddress()
					: new Addr64("0x0").toHexAddressString());
		} else if (ICLineBreakpoint.class.equals(type)) {
			map.put(ATTR_FILE_NAME, (valid) ? SOURCE_NAME_VALID : SOURCE_NAME_INVALID);
			map.put(ATTR_LINE_NUMBER, (valid) ? LINE_NUMBER_VALID : LINE_NUMBER_INVALID);
		}
		return map;
	}

	public Map<String, Object> getWatchpointAttributes(Class<? extends ICWatchpoint> type, boolean read, boolean write)
			throws Throwable {
		Assert.assertTrue(read || write);
		Map<String, Object> map = new HashMap<>();
		map.put(ATTR_EXPRESSION, EXPRESSION_VALID);
		map.put(ATTR_READ, Boolean.valueOf(read));
		map.put(ATTR_WRITE, Boolean.valueOf(write));
		return map;
	}

	private void setConsoleBreakpoint(Class<? extends ICBreakpoint> type, Map<String, Object> attributes)
			throws Throwable {
		if (ICFunctionBreakpoint.class.equals(type)) {
			setConsoleFunctionBreakpoint((String) attributes.get(ATTR_FILE_NAME),
					(String) attributes.get(ATTR_FUNCTION));
		} else if (ICAddressBreakpoint.class.equals(type)) {
			setConsoleAddressBreakpoint((String) attributes.get(ATTR_ADDRESS));
		} else if (ICLineBreakpoint.class.equals(type)) {
			setConsoleLineBreakpoint((String) attributes.get(ATTR_FILE_NAME),
					((Integer) attributes.get(ATTR_LINE_NUMBER)).intValue());
		} else if (ICWatchpoint.class.equals(type)) {
			setConsoleWatchpoint((String) attributes.get(ATTR_EXPRESSION),
					((Boolean) attributes.get(ATTR_READ)).booleanValue(),
					((Boolean) attributes.get(ATTR_WRITE)).booleanValue());
		}
	}

	private ICBreakpoint findPlatformBreakpoint(Class<? extends ICBreakpoint> type, Map<String, Object> attributes)
			throws Throwable {
		if (ICFunctionBreakpoint.class.equals(type)) {
			return findPlatformFunctionBreakpoint((String) attributes.get(ATTR_FILE_NAME),
					(String) attributes.get(ATTR_FUNCTION));
		} else if (ICAddressBreakpoint.class.equals(type)) {
			return findPlatformAddressBreakpoint((String) attributes.get(ATTR_ADDRESS));
		} else if (ICLineBreakpoint.class.equals(type)) {
			return findPlatformLineBreakpoint((String) attributes.get(ATTR_FILE_NAME),
					((Integer) attributes.get(ATTR_LINE_NUMBER)).intValue());
		} else if (ICWatchpoint.class.equals(type)) {
			return findPlatformWatchpoint((String) attributes.get(ATTR_EXPRESSION),
					((Boolean) attributes.get(ATTR_READ)).booleanValue(),
					((Boolean) attributes.get(ATTR_WRITE)).booleanValue());
		}
		throw new Exception(String.format("Invalid breakpoint type: %s", type.getName()));
	}

	private void deleteAllPlatformBreakpoints() throws Exception {
		IBreakpointManager bm = DebugPlugin.getDefault().getBreakpointManager();
		for (IBreakpoint b : bm.getBreakpoints()) {
			bm.removeBreakpoint(b, true);
		}
	}
}
