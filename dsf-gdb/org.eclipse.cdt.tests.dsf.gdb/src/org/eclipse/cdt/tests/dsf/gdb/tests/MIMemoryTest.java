/*******************************************************************************
 * Copyright (c) 2007 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		- Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.debug.core.model.MemoryByte;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * This is the Memory Service test suite.
 * 
 * It is meant to be a regression suite to be executed automatically against
 * the DSF nightly builds.
 * 
 * It is also meant to be augmented with a proper test case(s) every time a
 * feature is added or in the event (unlikely :-) that a bug is found in the
 * Memory Service.
 * 
 * Refer to the JUnit4 documentation for an explanation of the annotations.
 */

@RunWith(BackgroundRunner.class)
public class MIMemoryTest extends BaseTestCase {

	private final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
	private DsfSession          fSession;
	private DsfServicesTracker  fServicesTracker;
	private IMemoryDMContext    fMemoryDmc;
	private MIRunControl        fRunControl;
	private IMemory             fMemoryService;
	private IExpressions        fExpressionService;

	// Keeps track of the MemoryChangedEvents
	private final int BLOCK_SIZE = 256;
	private IAddress fBaseAddress;
	private Integer fMemoryChangedEventCount = new Integer(0);
	private boolean[] fMemoryAddressesChanged = new boolean[BLOCK_SIZE];

	// ========================================================================
	// Housekeeping stuff
	// ========================================================================

	@BeforeClass
	public static void testSuiteInitialization() {
		// Select the binary to run the tests against
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "data/launch/bin/MemoryTestApp.exe");
	}

	@AfterClass
	public static void testSuiteCleanup() {
	}

	@Before
	public void testCaseInitialization() throws Throwable {
	    fSession = getGDBLaunch().getSession();

	    // Get a reference to the memory service
		fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
		assert(fServicesTracker != null);

   		fMemoryDmc = (IMemoryDMContext)SyncUtil.getContainerContext();
        assert(fMemoryDmc != null);
		    
		fRunControl = fServicesTracker.getService(MIRunControl.class);
		assert(fRunControl != null);

		fMemoryService = fServicesTracker.getService(IMemory.class);
		assert(fMemoryService != null);

		fExpressionService = fServicesTracker.getService(IExpressions.class);
		assert(fExpressionService != null);

		fSession.addServiceEventListener(MIMemoryTest.this, null);
		fBaseAddress = null;
		clearEventCounters();
	}

	@After
	public void testCaseCleanup() {
		// Clear the references (not strictly necessary)
		fBaseAddress = null;
		fSession.removeServiceEventListener(MIMemoryTest.this);
		fExpressionService = null;
		fMemoryService = null;
		fRunControl = null;
        fServicesTracker.dispose();
		fServicesTracker = null;
		clearEventCounters();
	}

	// ========================================================================
	// Helper Functions
	// ========================================================================

	 /* ------------------------------------------------------------------------
	 * eventDispatched
	 * ------------------------------------------------------------------------
	 * Processes MemoryChangedEvents.
	 * First checks if the memory block base address was set so the individual
	 * test can control if it wants to verify the event(s).   
	 * ------------------------------------------------------------------------
	 * @param e The MemoryChangedEvent
	 * ------------------------------------------------------------------------
	 */
	 @DsfServiceEventHandler
	 public void eventDispatched(IMemoryChangedEvent e) {
		 synchronized(fMemoryChangedEventCount) {
			 fMemoryChangedEventCount++;
		 }
		 IAddress[] addresses = e.getAddresses();
		 for (int i = 0; i < addresses.length; i++) {
			 int offset = Math.abs(addresses[i].distanceTo(fBaseAddress).intValue());
			 if (offset < BLOCK_SIZE)
				 synchronized(fMemoryAddressesChanged) {
					 fMemoryAddressesChanged[offset] = true;
				 }
		 }
	 }

	 // Clears the counters
	 private void clearEventCounters() {
		 synchronized(fMemoryChangedEventCount) {
			 fMemoryChangedEventCount = 0;
		 }
		 synchronized(fMemoryAddressesChanged) {
			 for (int i = 0; i < BLOCK_SIZE; i++)
				 fMemoryAddressesChanged[i] = false;
		 }
	 }

	 // Returns the total number of events received
	 private int getEventCount() {
		 int count;
		 synchronized(fMemoryChangedEventCount) {
			 count = fMemoryChangedEventCount;
		 }
		 return count;
	 }

	 // Returns the number of distinct addresses reported
	 private int getAddressCount() {
		 int count = 0;
		 synchronized(fMemoryAddressesChanged) {
			 for (int i = 0; i < BLOCK_SIZE; i++)
				 if (fMemoryAddressesChanged[i])
					 count++;
		 }
		 return count;
	 }

	 /* ------------------------------------------------------------------------
	 * evaluateExpression
	 * ------------------------------------------------------------------------
	 * Invokes the ExpressionService to evaluate an expression. In theory, we
	 * shouldn't rely on another service to test this one but we need a way to
	 * access a variable from the test application in order verify that the
	 * memory operations (read/write) are working properly.   
	 * ------------------------------------------------------------------------
	 * @param expression Expression to resolve
	 * @return Resolved expression  
	 * @throws InterruptedException
	 * ------------------------------------------------------------------------
	 */
	private IAddress evaluateExpression(IDMContext ctx, String expression) throws Throwable
	{
		// Create the expression and format contexts 
		final IExpressionDMContext expressionDMC = SyncUtil.createExpression(ctx, expression);
		final FormattedValueDMContext formattedValueDMC = SyncUtil.getFormattedValue(fExpressionService, expressionDMC, IFormattedValues.HEX_FORMAT);

		// Create the DataRequestMonitor which will store the operation result in the wait object
		final DataRequestMonitor<FormattedValueDMData> drm =
			new DataRequestMonitor<FormattedValueDMData>(fSession.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					fWait.setReturnInfo(getData());
				}
				fWait.waitFinished(getStatus());
			}
		};

		// Evaluate the expression (asynchronously)
		fSession.getExecutor().submit(new Runnable() {
			public void run() {
				fExpressionService.getFormattedExpressionValue(formattedValueDMC, drm);
			}
		});

		// Wait for completion
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());

		// Return the string formatted by the back-end
		String result = "";
		Object returnInfo = fWait.getReturnInfo();
		if (returnInfo instanceof FormattedValueDMData)
			result = ((FormattedValueDMData) returnInfo).getFormattedValue();
		return new Addr64(result);
	}

	/* ------------------------------------------------------------------------
	 * readMemory
	 * ------------------------------------------------------------------------
	 * Issues a memory read request. The result is stored in fWait.
	 * ------------------------------------------------------------------------
	 * Typical usage:
	 *  getMemory(dmc, address, offset, count);
	 *  fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
	 *  assertTrue(fWait.getMessage(), fWait.isOK());
	 * ------------------------------------------------------------------------
	 * @param dmc		the data model context
	 * @param address	the memory block address
	 * @param offset	the offset in the buffer
	 * @param count		the number of bytes to read
	 * @throws InterruptedException
	 * ------------------------------------------------------------------------
	 */
	private void readMemory(final IMemoryDMContext dmc, final IAddress address,
			final long offset, final int word_size, final int count)
	throws InterruptedException
	{
		// Set the Data Request Monitor
		final DataRequestMonitor<MemoryByte[]> drm = 
			new DataRequestMonitor<MemoryByte[]>(fSession.getExecutor(), null) {
				@Override
				protected void handleCompleted() {
					if (isSuccess()) {
						fWait.setReturnInfo(getData());
					}
					fWait.waitFinished(getStatus());
				}
			};

		// Issue the get memory request
		fSession.getExecutor().submit(new Runnable() {
			public void run() {
				fMemoryService.getMemory(dmc, address, offset, word_size, count, drm);
			}
		});
	}

	/* ------------------------------------------------------------------------
	 * readMemoryByteAtOffset
	 * ------------------------------------------------------------------------
	 * Issues a memory read request. The result is stored in fWait.
	 * ------------------------------------------------------------------------
	 * Typical usage:
	 *  getMemory(dmc, address, offset, count);
	 *  fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
	 *  assertTrue(fWait.getMessage(), fWait.isOK());
	 * ------------------------------------------------------------------------
	 * @param dmc		the data model context
	 * @param address	the memory block address
	 * @param offset	the offset in the buffer
	 * @param count		the number of bytes to read
	 * @param result	the expected byte
	 * @throws InterruptedException
	 * ------------------------------------------------------------------------
	 */
	private void readMemoryByteAtOffset(final IMemoryDMContext dmc, final IAddress address,
			final long offset, final int word_size, final int count, final MemoryByte[] result)
	throws InterruptedException
	{
		// Set the Data Request Monitor
		final DataRequestMonitor<MemoryByte[]> drm = 
			new DataRequestMonitor<MemoryByte[]>(fSession.getExecutor(), null) {
				@Override
				protected void handleCompleted() {
					if (isSuccess()) {
						result[(int) offset] = getData()[0];
					}
					fWait.waitFinished(getStatus());
				}
			};

		// Issue the get memory request
		fSession.getExecutor().submit(new Runnable() {
			public void run() {
				fMemoryService.getMemory(dmc, address, offset, word_size, count, drm);
			}
		});
	}

	/* ------------------------------------------------------------------------
	 * writeMemory
	 * ------------------------------------------------------------------------
	 * Issues a memory write request.
	 * ------------------------------------------------------------------------
	 * Typical usage:
	 *  writeMemory(dmc, address, offset, count, buffer);
	 *  fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
	 *  assertTrue(fWait.getMessage(), fWait.isOK());
	 * ------------------------------------------------------------------------
	 * @param dmc		the data model context
	 * @param address	the memory block address (could be an expression)
	 * @param offset	the offset from address
	 * @param count		the number of bytes to write
	 * @param buffer	the byte buffer to write from
	 * @throws InterruptedException
	 * ------------------------------------------------------------------------
	 */
	private void writeMemory(final IMemoryDMContext dmc, final IAddress address,
			final long offset, final int word_size, final int count, final byte[] buffer)
	throws InterruptedException
	{
		// Set the Data Request Monitor
		final RequestMonitor rm = 
			new RequestMonitor(fSession.getExecutor(), null) {
				@Override
				protected void handleCompleted() {
					fWait.waitFinished(getStatus());
				}
			};

		// Issue the get memory request
		fSession.getExecutor().submit(new Runnable() {
			public void run() {
				fMemoryService.setMemory(dmc, address, offset, word_size, count, buffer, rm);
			}
		});
	}

	/* ------------------------------------------------------------------------
	 * fillMemory
	 * ------------------------------------------------------------------------
	 * Issues a memory write request.
	 * ------------------------------------------------------------------------
	 * Typical usage:
	 *  writeMemory(dmc, address, offset, count, buffer);
	 *  fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
	 *  assertTrue(fWait.getMessage(), fWait.isOK());
	 * ------------------------------------------------------------------------
	 * @param dmc		the data model context
	 * @param address	the memory block address (could be an expression)
	 * @param offset	the offset from address
	 * @param count		the number of bytes to write
	 * @param pattern	the byte pattern to write
	 * @throws InterruptedException
	 * ------------------------------------------------------------------------
	 */
	private void fillMemory(final IMemoryDMContext dmc, final IAddress address,
			final long offset, final int word_size, final int count, final byte[] pattern)
	throws InterruptedException
	{
		// Set the Data Request Monitor
		final RequestMonitor rm = 
			new RequestMonitor(fSession.getExecutor(), null) {
				@Override
				protected void handleCompleted() {
					fWait.waitFinished(getStatus());
				}
			};

		// Issue the fill memory request
		fSession.getExecutor().submit(new Runnable() {
			public void run() {
				fMemoryService.fillMemory(dmc, address, offset, word_size, count, pattern, rm);
			}
		});
	}

	// ========================================================================
	// Test Cases
	// ------------------------------------------------------------------------
	// Templates:
	// ------------------------------------------------------------------------
	// @ Test
	// public void basicTest() {
	//     // First test to run
	//     assertTrue("", true);
	// }
	// ------------------------------------------------------------------------
	// @ Test(timeout=5000)
	// public void timeoutTest() {
	//     // Second test to run, which will timeout if not finished on time
	//     assertTrue("", true);
	// }
	// ------------------------------------------------------------------------
	// @ Test(expected=FileNotFoundException.class)
	// public void exceptionTest() throws FileNotFoundException {
	//     // Third test to run which expects an exception
	//     throw new FileNotFoundException("Just testing");
	// }
	// ========================================================================

	///////////////////////////////////////////////////////////////////////////
	// getMemory tests
	///////////////////////////////////////////////////////////////////////////

	// ------------------------------------------------------------------------
	// readWithNullContext
	// Test that a null context is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void readWithNullContext() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		IMemoryDMContext dmc = null;
		long offset = 0;
		int word_size = 1;
		int count = 1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Unknown context type";
		fWait.waitReset();
		readMemory(dmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// readWithInvalidAddress
	// Test that an invalid address is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void readWithInvalidAddress() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		SyncUtil.step(StepType.STEP_RETURN);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 1;
		fBaseAddress = new Addr64("0");

		// Perform the test
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

		//	Ensure that we receive a block of invalid memory bytes
		assertTrue(fWait.getMessage(), fWait.isOK());
		MemoryByte[] buffer = (MemoryByte[]) fWait.getReturnInfo();
		assertTrue("Wrong value: expected '-1, 0', received '" + buffer[0].getValue() + ", " + buffer[0].getFlags() + "'",
				(buffer[0].getValue() == (byte) 0) && (buffer[0].getFlags() == (byte) 0));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// readWithInvalidWordSize
	// Test that an invalid word size is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void readWithInvalidWordSize() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = -1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Word size not supported (!= 1)";
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, 0, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, 2, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// readWithInvalidCount
	// Test that an invalid count is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void readWithInvalidCount() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = -1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Invalid word count (< 0)";
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// readCharVaryingBaseAddress
	// Test the reading of individual bytes by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void readCharVaryingBaseAddress() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Verify that all bytes are '0'
		for (int i = 0; i < BLOCK_SIZE; i++) {
			IAddress address = fBaseAddress.add(i);
			fWait.waitReset();
			readMemory(fMemoryDmc, address, offset, word_size, count);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());
			MemoryByte[] buffer = (MemoryByte[]) fWait.getReturnInfo();
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + buffer[0].getValue() + "'",
				(buffer[0].getValue() == (byte) 0));
		}

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:setBlocks", true);
		SyncUtil.resumeUntilStopped();
		SyncUtil.step(StepType.STEP_RETURN);

		// Verify that all bytes are set
		for (int i = 0; i < BLOCK_SIZE; i++) {
			IAddress address = fBaseAddress.add(i);
			fWait.waitReset();
			readMemory(fMemoryDmc, address, offset, word_size, count);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			MemoryByte[] buffer = (MemoryByte[]) fWait.getReturnInfo();
			assertTrue(fWait.getMessage(), fWait.isOK());
			assertTrue("Wrong value read at offset " + i + ": expected '" + i + "', received '" + buffer[0].getValue() + "'",
				(buffer[0].getValue() == (byte) i));
		}

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// readCharVaryingOffset
	// Test the reading of individual bytes by varying the offset
	// ------------------------------------------------------------------------
	@Test
	public void readCharVaryingOffset() throws Throwable {

		// Run to the point where the array is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		int word_size = 1;
		int count = 1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Verify that all bytes are '0'
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			fWait.waitReset();
			readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());
			MemoryByte[] buffer = (MemoryByte[]) fWait.getReturnInfo();
			assertTrue("Wrong value read at offset " + offset + ": expected '" + 0 + "', received '" + buffer[0].getValue() + "'",
				(buffer[0].getValue() == (byte) 0));
		}

		// Run to the point where the array is set
		SyncUtil.addBreakpoint("MemoryTestApp.cc:setBlocks", true);
		SyncUtil.resumeUntilStopped();
		SyncUtil.step(StepType.STEP_RETURN);

		// Verify that all bytes are set
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			fWait.waitReset();
			readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());
			MemoryByte[] buffer = (MemoryByte[]) fWait.getReturnInfo();
			assertTrue("Wrong value read at offset " + offset + ": expected '" + offset + "', received '" + buffer[0].getValue() + "'",
				(buffer[0].getValue() == (byte) offset));
		}

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// readCharArray
	// Test the reading of a byte array
	// ------------------------------------------------------------------------
	@Test
	public void readCharArray() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = BLOCK_SIZE;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Get the memory block
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		MemoryByte[] buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are '0'
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) 0));
		}

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:setBlocks", true);
		SyncUtil.resumeUntilStopped();
		SyncUtil.step(StepType.STEP_RETURN);

		// Get the memory block
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are '0'
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) i));
		}

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	///////////////////////////////////////////////////////////////////////////
	// setMemory tests
	///////////////////////////////////////////////////////////////////////////

	// ------------------------------------------------------------------------
	// writeWithNullContext
	// Test that a null context is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void writeWithNullContext() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 1;
		byte[] buffer = new byte[count];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Unknown context type";
		fWait.waitReset();
		writeMemory(null, fBaseAddress, offset, word_size, count, buffer);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// writeWithInvalidAddress
	// Test that an invalid address is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void writeWithInvalidAddress() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		SyncUtil.step(StepType.STEP_RETURN);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 1;
		byte[] buffer = new byte[count];
		fBaseAddress = new Addr64("0");

		// Perform the test
		fWait.waitReset();
		writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		String expected = "Cannot access memory at address";	// Error msg returned by gdb
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// writeWithInvalidWordSize
	// Test that an invalid word size is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void writeWithInvalidWordSize() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = -1;
		byte[] buffer = new byte[1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Word size not supported (!= 1)";
		fWait.waitReset();
		writeMemory(fMemoryDmc, fBaseAddress, offset, 0, count, buffer);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		fWait.waitReset();
		writeMemory(fMemoryDmc, fBaseAddress, offset, 2, count, buffer);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// writeWithInvalidCount
	// Test that an invalid count is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void writeWithInvalidCount() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = -1;
		byte[] buffer = new byte[1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Invalid word count (< 0)";
		fWait.waitReset();
		writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// writeWithInvalidBuffer
	// Test that the buffer contains at least count bytes
	// ------------------------------------------------------------------------
	@Test
	public void writeWithInvalidBuffer() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 10;
		byte[] buffer = new byte[count - 1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Buffer too short";
		fWait.waitReset();
		writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// writeCharVaryingAddress
	// Test the writing of individual bytes by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void writeCharVaryingAddress() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = BLOCK_SIZE;
		byte[] buffer = new byte[count];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		for (int i = 0; i < count; i++) {
			
			// [1] Ensure that the memory byte = 0
			fWait.waitReset();
			readMemory(fMemoryDmc, fBaseAddress, i, word_size, 1);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());
			MemoryByte[] block = (MemoryByte[]) fWait.getReturnInfo();
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[0].getValue() + "'",
					(block[0].getValue() == (byte) 0));
			
			// [2] Write a byte value (count - i - 1)
			IAddress address = fBaseAddress.add(i);
			fWait.waitReset();
			byte expected = (byte) (count - i - 1);
			buffer[0] = expected;
			writeMemory(fMemoryDmc, address, offset, word_size, 1, buffer);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());

			// [3] Verify that the correct MemoryChangedEvent was sent
			// (I hardly believe there are no synchronization problems here...)
			assertTrue("MemoryChangedEvent problem at offset " + i + ": expected " + (i + 1) + " events, received " + getEventCount(),
					getEventCount() == (i + 1));
			assertTrue("MemoryChangedEvent problem at offset " + i, fMemoryAddressesChanged[i]);

			// [4] Verify that the memory byte was written correctly
			fWait.waitReset();
			readMemory(fMemoryDmc, fBaseAddress, i, word_size, 1);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());
			block = (MemoryByte[]) fWait.getReturnInfo();
			assertTrue("Wrong value read at offset " + i + ": expected '" + expected + "', received '" + block[0].getValue() + "'",
					(block[0].getValue() == expected));
		}

		// Ensure the MemoryChangedEvent events were received
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " events, received " + getEventCount(),
				getEventCount() == BLOCK_SIZE);
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " distinct addresses, received " + getAddressCount(),
				getEventCount() == BLOCK_SIZE);
	}

	// ------------------------------------------------------------------------
	// writeCharVaryingOffset
	// Test the writing of individual bytes by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void writeCharVaryingOffset() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		int word_size = 1;
		int count = BLOCK_SIZE;
		byte[] buffer = new byte[count];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		for (int offset = 0; offset < count; offset++) {
			
			// [1] Ensure that the memory byte = 0
			fWait.waitReset();
			readMemory(fMemoryDmc, fBaseAddress, offset, word_size, 1);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());
			MemoryByte[] block = (MemoryByte[]) fWait.getReturnInfo();
			assertTrue("Wrong value read at offset " + offset + ": expected '" + 0 + "', received '" + block[0].getValue() + "'",
					(block[0].getValue() == (byte) 0));
			
			// [2] Write a byte value (count - offset - 1)
			fWait.waitReset();
			byte expected = (byte) (count - offset - 1);
			buffer[0] = expected;
			writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, 1, buffer);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());

			// [3] Verify that the correct MemoryChangedEvent was sent
			assertTrue("MemoryChangedEvent problem at offset " + offset + ": expected " + (offset + 1) + " events, received " + getEventCount(),
					getEventCount() == (offset + 1));
			assertTrue("MemoryChangedEvent problem at offset " + offset, fMemoryAddressesChanged[offset]);

			// [4] Verify that the memory byte was written correctly
			fWait.waitReset();
			readMemory(fMemoryDmc, fBaseAddress, offset, word_size, 1);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());
			block = (MemoryByte[]) fWait.getReturnInfo();
			assertTrue("Wrong value read at offset " + offset + ": expected '" + expected + "', received '" + block[0].getValue() + "'",
					(block[0].getValue() == expected));
		}

		// Ensure the MemoryChangedEvent events were received
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " events, received " + getEventCount(),
				getEventCount() == BLOCK_SIZE);
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " distinct addresses, received " + getAddressCount(),
				getAddressCount() == BLOCK_SIZE);
	}

	// ------------------------------------------------------------------------
	// writeCharArray
	// Test the writing of a byte array
	// ------------------------------------------------------------------------
	@Test
	public void writeCharArray() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = BLOCK_SIZE;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Make sure that the memory block is zeroed
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		MemoryByte[] block = (MemoryByte[]) fWait.getReturnInfo();
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[i].getValue() + "'",
				(block[i].getValue() == (byte) 0));
		}

		// Write an initialized memory block
		byte[] buffer = new byte[count];
		for (int i = 0; i < count; i++) {
			buffer[i] = (byte) i;
		}
		fWait.waitReset();
		writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());

		// Make sure that the memory block is initialized
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		block = (MemoryByte[]) fWait.getReturnInfo();
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[i].getValue() + "'",
				(block[i].getValue() == (byte) i));
		}

		// Ensure the MemoryChangedEvent events were received
		assertTrue("MemoryChangedEvent problem: expected " + 1 + " event, received " + getEventCount(),
				getEventCount() == 1);
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " distinct addresses, received " + getAddressCount(),
				getAddressCount() == BLOCK_SIZE);
	}

	///////////////////////////////////////////////////////////////////////////
	// fillMemory tests
	///////////////////////////////////////////////////////////////////////////

	// ------------------------------------------------------------------------
	// fillWithNullContext
	// Test that a null context is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void fillWithNullContext() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 1;
		byte[] pattern = new byte[count];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Unknown context type";
		fWait.waitReset();
		fillMemory(null, fBaseAddress, offset, word_size, count, pattern);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// fillWithInvalidAddress
	// Test that an invalid address is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void fillWithInvalidAddress() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		SyncUtil.step(StepType.STEP_RETURN);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 1;
		byte[] pattern = new byte[count];
		fBaseAddress = new Addr64("0");

		// Perform the test
		fWait.waitReset();
		fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, pattern);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		String expected = "Cannot access memory at address";	// Error msg returned by gdb
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// fillWithInvalidWordSize
	// Test that an invalid word size is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void fillWithInvalidWordSize() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = 1;
		byte[] pattern = new byte[1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Word size not supported (!= 1)";
		fWait.waitReset();
		fillMemory(fMemoryDmc, fBaseAddress, offset, 0, count, pattern);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		fWait.waitReset();
		fillMemory(fMemoryDmc, fBaseAddress, offset, 2, count, pattern);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// fillWithInvalidCount
	// Test that an invalid count is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void fillWithInvalidCount() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = -1;
		byte[] pattern = new byte[1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Invalid repeat count (< 0)";
		fWait.waitReset();
		fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, pattern);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// fillWithInvalidPattern
	// Test that an empty pattern is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void fillWithInvalidPattern() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 1;
		byte[] pattern = new byte[0];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		String expected = "Empty pattern";
		fWait.waitReset();
		fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, pattern);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

	// ------------------------------------------------------------------------
	// writePatternVaryingAddress
	// Test the writing of the pattern by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void writePatternVaryingAddress() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 1;
		int length = 4;
		byte[] pattern = new byte[length];
		for (int i = 0; i < length; i++) pattern[i] = (byte) i;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Ensure that the memory is zeroed
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count * length);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		MemoryByte[] block = (MemoryByte[]) fWait.getReturnInfo();
		for (int i = 0; i < (count * length); i++)
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[i].getValue() + "'",
					(block[i].getValue() == (byte) 0));
		
		for (int i = 0; i < BLOCK_SIZE; i += length) {
			IAddress address = fBaseAddress.add(i);
			fWait.waitReset();
			fillMemory(fMemoryDmc, address, offset, word_size, count, pattern);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());

		}

		// Verify that the memory is correctly set
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, 0, word_size, count * length);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		block = (MemoryByte[]) fWait.getReturnInfo();
		for (int i = 0; i < count; i++)
			for (int j = 0; j < length; j++) {
				int index = i * length + j;
				assertTrue("Wrong value read at offset " + index + ": expected '" + j + "', received '" + block[index].getValue() + "'",
						(block[index].getValue() == (byte) j));
			}

		// Ensure the MemoryChangedEvent events were received
		assertTrue("MemoryChangedEvent problem: expected " + (BLOCK_SIZE / length) + " events, received " + getEventCount(),
				getEventCount() == (BLOCK_SIZE / length));
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " distinct addresses, received " + getAddressCount(),
				getAddressCount() == BLOCK_SIZE);
	}

	// ------------------------------------------------------------------------
	// writePatternVaryingOffset
	// Test the writing of the pattern by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void writePatternVaryingOffset() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 64;
		int length = 4;
		byte[] pattern = new byte[length];
		for (int i = 0; i < length; i++) pattern[i] = (byte) i;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Ensure that the memory is zeroed
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count * length);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		MemoryByte[] block = (MemoryByte[]) fWait.getReturnInfo();
		for (int i = 0; i < (count * length); i++)
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[i].getValue() + "'",
					(block[i].getValue() == (byte) 0));
		
		for (int i = 0; i < (BLOCK_SIZE / length); i++) {
			offset = i * length;
			fWait.waitReset();
			fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, 1, pattern);
			fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(fWait.getMessage(), fWait.isOK());

		}

		// Verify that the memory is correctly set
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, 0, word_size, count * length);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		block = (MemoryByte[]) fWait.getReturnInfo();
		for (int i = 0; i < count; i++)
			for (int j = 0; j < length; j++) {
				int index = i * length + j;
				assertTrue("Wrong value read at offset " + index + ": expected '" + j + "', received '" + block[index].getValue() + "'",
						(block[index].getValue() == (byte) j));
			}

		// Ensure the MemoryChangedEvent events were received
		assertTrue("MemoryChangedEvent problem: expected " + (BLOCK_SIZE / length) + " events, received " + getEventCount(),
				getEventCount() == (BLOCK_SIZE / length));
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " distinct addresses, received " + getAddressCount(),
				getAddressCount() == BLOCK_SIZE);
	}

	// ------------------------------------------------------------------------
	// writePatternCountTimes
	// Test the writing of the pattern [count] times
	// ------------------------------------------------------------------------
	@Test
	public void writePatternCountTimes() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 64;
		int length = 4;
		byte[] pattern = new byte[length];
		for (int i = 0; i < length; i++) pattern[i] = (byte) i;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Ensure that the memory is zeroed
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count * length);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		MemoryByte[] block = (MemoryByte[]) fWait.getReturnInfo();
		for (int i = 0; i < (count * length); i++)
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[i].getValue() + "'",
					(block[i].getValue() == (byte) 0));
		
		// Write the pattern [count] times
		fWait.waitReset();
		fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, pattern);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());

		// Verify that the memory is correctly set
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count * length);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		block = (MemoryByte[]) fWait.getReturnInfo();
		for (int i = 0; i < count; i++)
			for (int j = 0; j < length; j++) {
				int index = i * length + j;
				assertTrue("Wrong value read at offset " + index + ": expected '" + j + "', received '" + block[index].getValue() + "'",
						(block[index].getValue() == (byte) j));
			}

		// Ensure the MemoryChangedEvent events were received
		assertTrue("MemoryChangedEvent problem: expected " + 1 + " events, received " + getEventCount(),
				getEventCount() == 1);
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " distinct addresses, received " + getAddressCount(),
				getAddressCount() == BLOCK_SIZE);
	}

	// ------------------------------------------------------------------------
	// asynchronousReadWrite
	// Test the asynchronous reading/writing of individual bytes (varying offset)
	// ------------------------------------------------------------------------
	@Test
	public void asynchronousReadWrite() throws Throwable {

		// Run to the point where the array is zeroed
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		int word_size = 1;
		int count = 1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Verify asynchronously that all bytes are '0'
		fWait.waitReset();
		MemoryByte[] buffer = new MemoryByte[BLOCK_SIZE];
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			fWait.increment();
			readMemoryByteAtOffset(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		}
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			assertTrue("Wrong value read at offset " + offset + ": expected '" + 0 + "', received '" + buffer[offset].getValue() + "'",
					(buffer[offset].getValue() == (byte) 0));
		}

		// Write asynchronously
		fWait.waitReset();
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			fWait.increment();
			byte[] block = new byte[count];
			block[0] = (byte) offset;
			writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, block);
		}
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());

		// Ensure the MemoryChangedEvent events were received
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " events, received " + getEventCount(),
				getEventCount() == BLOCK_SIZE);
		assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " distinct addresses, received " + getAddressCount(),
				getAddressCount() == BLOCK_SIZE);

		// Verify asynchronously that all bytes are set
		fWait.waitReset();
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			fWait.increment();
			readMemoryByteAtOffset(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		}
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			assertTrue("Wrong value read at offset " + offset + ": expected '" + offset + "', received '" + buffer[offset].getValue() + "'",
					(buffer[offset].getValue() == (byte) offset));
		}
	}

	// ------------------------------------------------------------------------
	// memoryCacheRead
	// Get a bunch of blocks to exercise the memory cache 
	// ------------------------------------------------------------------------
	@Test
	public void memoryCacheRead() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:setBlocks", true);
		SyncUtil.resumeUntilStopped();
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = BLOCK_SIZE;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Get the 'reference' memory block
		fWait.waitReset();
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		MemoryByte[] buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are set to 'i'
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) i));
		}

		// Clear the cache
		SyncUtil.step(StepType.STEP_OVER);

		// Get a first block
		fWait.waitReset();
		offset =  0;
		count = 64;
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a second block
		fWait.waitReset();
		offset =  128;
		count = 64;
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a third block between the first 2
		fWait.waitReset();
		offset =  80;
		count = 32;
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a block that is contiguous to the end of an existing block
		fWait.waitReset();
		offset =  192;
		count = 32;
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a block that ends beyond an existing block
		fWait.waitReset();
		offset =  192;
		count = 64;
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a block that will require 2 reads (for the gaps between blocks 1-2 and 2-3)
		fWait.waitReset();
		offset =  32;
		count = 128;
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a block that involves multiple cached blocks
		fWait.waitReset();
		offset =  48;
		count = 192;
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are set to 'i'
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}
		
		// Get the whole block
		fWait.waitReset();
		offset =  0;
		count = BLOCK_SIZE;
		readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(fWait.getMessage(), fWait.isOK());
		buffer = (MemoryByte[]) fWait.getReturnInfo();

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}
		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

}
