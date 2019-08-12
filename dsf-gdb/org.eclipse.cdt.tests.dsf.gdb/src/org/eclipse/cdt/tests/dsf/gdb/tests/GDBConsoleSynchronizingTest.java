/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl.IReverseModeChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.debug.core.model.MemoryByte;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * This test case verifies that different commands issued from the
 * GDB console cause proper updating within the CDT views.
 */
@RunWith(Parameterized.class)
public class GDBConsoleSynchronizingTest extends BaseParametrizedTestCase {
	final static private String EXEC_NAME = "ConsoleSyncTestApp.exe";

	final static private int DEFAULT_TIMEOUT = TestsPlugin.massageTimeout(1000);
	final static private TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
	final static private String NEW_VAR_VALUE = "0x12345678";
	final static private int NEW_VAR_SIZE = 4; // The number of bytes of NEW_VAR_VALUE
	final static private byte[] NEW_MEM = { 0x12, 0x34, 0x56, 0x78 }; // The individual bytes of NEW_VAR_VALUE

	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	private IGDBControl fCommandControl;
	private IMemory fMemoryService;
	private IExpressions fExprService;
	private IRunControl fRunControl;

	private List<IDMEvent<? extends IDMContext>> fEventsReceived = new ArrayList<>();

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	@Override
	public void doBeforeTest() throws Exception {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_6);
		super.doBeforeTest();

		fSession = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
			Assert.assertTrue(fServicesTracker != null);

			fCommandControl = fServicesTracker.getService(IGDBControl.class);
			Assert.assertTrue(fCommandControl != null);

			fMemoryService = fServicesTracker.getService(IMemory.class);
			Assert.assertTrue(fMemoryService != null);

			fExprService = fServicesTracker.getService(IExpressions.class);
			Assert.assertTrue(fExprService != null);

			fRunControl = fServicesTracker.getService(IRunControl.class);
			Assert.assertTrue(fRunControl != null);

			// Register to breakpoint events
			fSession.addServiceEventListener(GDBConsoleSynchronizingTest.this, null);
		};
		fSession.getExecutor().submit(runnable).get();
	}

	@Override
	public void doAfterTest() throws Exception {
		if (fSession != null) {
			fSession.getExecutor().submit(() -> fSession.removeServiceEventListener(GDBConsoleSynchronizingTest.this))
					.get();
		}
		fEventsReceived.clear();
		if (fServicesTracker != null)
			fServicesTracker.dispose();
		super.doAfterTest();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Start of tests
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This test verifies that setting a variable from the console
	 * using the set command will properly trigger a DSF event to
	 * indicate the change.  This test makes sure the value that
	 * changes is in the memory cache also.
	 */
	@Test
	public void testSettingVariableWithSetWithMemory() throws Throwable {
		MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testMemoryChanges");

		final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "i");

		// Read the memory that will change first, or else there will be no event for it
		Query<IExpressionDMAddress> query = new Query<IExpressionDMAddress>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMAddress> rm) {
				fExprService.getExpressionAddressData(exprDmc, rm);
			}
		};

		fSession.getExecutor().execute(query);
		IExpressionDMAddress data = query.get();

		IMemoryDMContext memoryDmc = DMContexts.getAncestorOfType(frameDmc, IMemoryDMContext.class);
		SyncUtil.readMemory(memoryDmc, data.getAddress(), 0, 1, NEW_VAR_SIZE);

		fEventsReceived.clear();

		String newValue = NEW_VAR_VALUE;
		queueConsoleCommand("set variable i=" + newValue);

		IMemoryChangedEvent memoryEvent = waitForEvent(IMemoryChangedEvent.class);
		assertEquals(1, memoryEvent.getAddresses().length);
		assertEquals(data.getAddress(), memoryEvent.getAddresses()[0]);

		// Now verify the memory service knows the new memory value
		MemoryByte[] memory = SyncUtil.readMemory(memoryDmc, data.getAddress(), 0, 1, NEW_VAR_SIZE);
		assertEquals(NEW_VAR_SIZE, memory.length);
		for (int i = 0; i < NEW_VAR_SIZE; i++) {
			if (memory[0].isBigEndian()) {
				assertEquals(NEW_MEM[i], memory[i].getValue());
			} else {
				assertEquals(NEW_MEM[i], memory[NEW_VAR_SIZE - 1 - i].getValue());
			}
		}

		// Now verify the expressions service knows the new value
		String exprValue = SyncUtil.getExpressionValue(exprDmc, IFormattedValues.HEX_FORMAT);
		assertEquals(newValue, exprValue);
	}

	private void testSettingVariableWithCommon(String commandPrefix) throws Throwable {
		MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testMemoryChanges");

		final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "i");

		// Read the memory that will change first, or else there will be no event for it
		Query<IExpressionDMAddress> query = new Query<IExpressionDMAddress>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMAddress> rm) {
				fExprService.getExpressionAddressData(exprDmc, rm);
			}
		};

		fSession.getExecutor().execute(query);
		IExpressionDMAddress data = query.get();

		fEventsReceived.clear();

		final String newValue = NEW_VAR_VALUE;
		queueConsoleCommand(commandPrefix + " = " + newValue);

		IMemoryChangedEvent memoryEvent = waitForEvent(IMemoryChangedEvent.class);
		assertEquals(1, memoryEvent.getAddresses().length);
		assertEquals(data.getAddress(), memoryEvent.getAddresses()[0]);

		// Now verify the memory service knows the new memory value
		IMemoryDMContext memoryDmc = DMContexts.getAncestorOfType(frameDmc, IMemoryDMContext.class);
		MemoryByte[] memory = SyncUtil.readMemory(memoryDmc, data.getAddress(), 0, 1, NEW_VAR_SIZE);
		assertEquals(NEW_VAR_SIZE, memory.length);
		for (int i = 0; i < NEW_VAR_SIZE; i++) {
			if (memory[0].isBigEndian()) {
				assertEquals(NEW_MEM[i], memory[i].getValue());
			} else {
				assertEquals(NEW_MEM[i], memory[NEW_VAR_SIZE - 1 - i].getValue());
			}
		}

		// Now verify the expressions service knows the new value
		String exprValue = SyncUtil.getExpressionValue(exprDmc, IFormattedValues.HEX_FORMAT);
		assertEquals(newValue, exprValue);
	}

	/**
	 * This test verifies that setting a variable from the console
	 * using the set command will properly trigger a DSF event to
	 * indicate the change, when the address is not in the memory cache.
	 */
	@Test
	public void testSettingVariableWithSet() throws Throwable {
		testSettingVariableWithCommon("set variable i");
	}

	/**
	 * This test verifies that setting a variable from the console
	 * using the print command will properly trigger a DSF event
	 * to indicate the change.
	 */
	@Test
	public void testSettingVariableWithPrint() throws Throwable {
		testSettingVariableWithCommon("print i");
	}

	/**
	 * This test verifies that setting a memory location from the
	 * console will properly trigger a DSF event to indicate the change.
	 */
	@Test
	public void testSettingMemory() throws Throwable {
		MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testMemoryChanges");

		final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "i");

		// Read the memory that will change first, or else there will be no event for it
		Query<IExpressionDMAddress> query = new Query<IExpressionDMAddress>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMAddress> rm) {
				fExprService.getExpressionAddressData(exprDmc, rm);
			}
		};

		fSession.getExecutor().execute(query);
		IExpressionDMAddress data = query.get();

		fEventsReceived.clear();

		final String newValue = NEW_VAR_VALUE;
		queueConsoleCommand("set {int}&i=" + newValue);

		IMemoryChangedEvent memoryEvent = waitForEvent(IMemoryChangedEvent.class);
		assertEquals(1, memoryEvent.getAddresses().length);
		assertEquals(data.getAddress(), memoryEvent.getAddresses()[0]);

		// Now verify the memory service knows the new memory value
		IMemoryDMContext memoryDmc = DMContexts.getAncestorOfType(frameDmc, IMemoryDMContext.class);
		MemoryByte[] memory = SyncUtil.readMemory(memoryDmc, data.getAddress(), 0, 1, NEW_VAR_SIZE);
		assertEquals(NEW_VAR_SIZE, memory.length);
		for (int i = 0; i < NEW_VAR_SIZE; i++) {
			if (memory[0].isBigEndian()) {
				assertEquals(NEW_MEM[i], memory[i].getValue());
			} else {
				assertEquals(NEW_MEM[i], memory[NEW_VAR_SIZE - 1 - i].getValue());
			}
		}

		// Now verify the expressions service knows the new value
		String exprValue = SyncUtil.getExpressionValue(exprDmc, IFormattedValues.HEX_FORMAT);
		assertEquals(newValue, exprValue);
	}

	/**
	 * This test verifies that enabling reverse debugging from the
	 * console will properly trigger a DSF event to indicate the change and
	 * will be processed by the service.
	 */
	@Test
	public void testEnableRecord() throws Throwable {
		assertTrue("Reverse debugging is not supported", fRunControl instanceof IReverseRunControl);
		final IReverseRunControl reverseService = (IReverseRunControl) fRunControl;

		SyncUtil.runToLocation("testMemoryChanges");

		// check starting state
		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				reverseService.isReverseModeEnabled(fCommandControl.getContext(), rm);
			}
		};

		fSession.getExecutor().execute(query);
		Boolean enabled = query.get();
		assertTrue("Reverse debugging should not be enabled", !enabled);

		fEventsReceived.clear();

		queueConsoleCommand("record");

		// Wait for the event
		IReverseModeChangedDMEvent event = waitForEvent(IReverseModeChangedDMEvent.class);
		assertEquals(true, event.isReverseModeEnabled());

		// Check the service
		query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				reverseService.isReverseModeEnabled(fCommandControl.getContext(), rm);
			}
		};
		fSession.getExecutor().execute(query);
		enabled = query.get();
		assertTrue("Reverse debugging should be enabled", enabled);
	}

	/**
	 * This test verifies that disabling reverse debugging from the
	 * console will properly trigger a DSF event to indicate the change and
	 * will be processed by the service.
	 */
	@Test
	public void testDisableRecord() throws Throwable {
		assertTrue("Reverse debugging is not supported", fRunControl instanceof IReverseRunControl);
		final IReverseRunControl reverseService = (IReverseRunControl) fRunControl;

		SyncUtil.runToLocation("testMemoryChanges");

		fEventsReceived.clear();

		// check starting state
		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				reverseService.enableReverseMode(fCommandControl.getContext(), true, new ImmediateRequestMonitor(rm) {
					@Override
					protected void handleSuccess() {
						reverseService.isReverseModeEnabled(fCommandControl.getContext(), rm);
					}
				});
			}
		};

		fSession.getExecutor().execute(query);
		Boolean enabled = query.get();
		assertTrue("Reverse debugging should be enabled", enabled);

		// Wait for the event to avoid confusing it with the next one
		IReverseModeChangedDMEvent event = waitForEvent(IReverseModeChangedDMEvent.class);
		assertEquals(true, event.isReverseModeEnabled());
		fEventsReceived.clear();

		queueConsoleCommand("record stop");

		// Wait for the event
		event = waitForEvent(IReverseModeChangedDMEvent.class);
		assertEquals(false, event.isReverseModeEnabled());

		// Check the service
		query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				reverseService.isReverseModeEnabled(fCommandControl.getContext(), rm);
			}
		};
		fSession.getExecutor().execute(query);
		enabled = query.get();
		assertTrue("Reverse debugging should not be enabled", !enabled);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// End of tests
	//////////////////////////////////////////////////////////////////////////////////////

	@DsfServiceEventHandler
	public void eventDispatched(IDMEvent<? extends IDMContext> e) {
		synchronized (this) {
			fEventsReceived.add(e);
			notifyAll();
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

	private <V extends IDMEvent<? extends IDMContext>> V waitForEvent(Class<V> eventType) throws Exception {
		return waitForEvent(eventType, DEFAULT_TIMEOUT);
	}

	@SuppressWarnings("unchecked")
	private <V extends IDMEvent<? extends IDMContext>> V waitForEvent(Class<V> eventType, int timeout)
			throws Exception {
		IDMEvent<?> event = getEvent(eventType);
		if (event == null) {
			synchronized (this) {
				try {
					wait(timeout);
				} catch (InterruptedException ex) {
				}
			}
			event = getEvent(eventType);
			if (event == null) {
				throw new Exception(String.format("Timed out waiting for '%s' to occur.", eventType.getName()));
			}
		}
		return (V) event;
	}

	@SuppressWarnings("unchecked")
	private synchronized <V extends IDMEvent<? extends IDMContext>> V getEvent(Class<V> eventType) {
		for (IDMEvent<?> e : fEventsReceived) {
			if (eventType.isAssignableFrom(e.getClass())) {
				fEventsReceived.remove(e);
				return (V) e;
			}
		}
		return null;
	}

}
