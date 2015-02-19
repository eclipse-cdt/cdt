/*******************************************************************************
 * Copyright (c) 2007, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson	AB		- Initial Implementation
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Make tests run with different values of addressable size (Bug 460241)
 *     Simon Marchi (Ericsson) - Refactoring, remove usage of AsyncCompletionWaitor
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
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
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.MemoryByteBuffer;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.debug.core.model.MemoryByte;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
	private static final String EXEC_NAME = "MemoryTestApp.exe";

	private DsfSession          fSession;
	private DsfServicesTracker  fServicesTracker;
	private IMemoryDMContext    fMemoryDmc;
	private MIRunControl        fRunControl;
	private IMemory             fMemoryService;
	private IExpressions        fExpressionService;
	private int 				fWordSize = 1 /* Default */;
	private ByteOrder 			fByteOrder;

	// Keeps track of the MemoryChangedEvents
	private final int BLOCK_SIZE = 256;
	private IAddress fBaseAddress;
	private Integer fMemoryChangedEventCount = new Integer(0);
	private boolean[] fMemoryAddressesChanged = new boolean[BLOCK_SIZE];

	@Rule
	final public ExpectedException expectedException = ExpectedException.none();

	// ========================================================================
	// Housekeeping stuff
	// ========================================================================



	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();
		
	    fSession = getGDBLaunch().getSession();
	    fMemoryDmc = (IMemoryDMContext)SyncUtil.getContainerContext();
	    assert(fMemoryDmc != null);

	    Runnable runnable = new Runnable() {
            @Override
			public void run() {
       	    // Get a reference to the memory service
        		fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
        		assert(fServicesTracker != null);

        		fRunControl = fServicesTracker.getService(MIRunControl.class);
        		assert(fRunControl != null);

        		fMemoryService = fServicesTracker.getService(IMemory.class);
        		assert(fMemoryService != null);

        		fExpressionService = fServicesTracker.getService(IExpressions.class);
        		assert(fExpressionService != null);

        		fSession.addServiceEventListener(MIMemoryTest.this, null);
        		fBaseAddress = null;
        		clearEventCounters();

				fWordSize = SyncUtil.readAddressableSize(fMemoryDmc);
				fByteOrder = SyncUtil.getMemoryByteOrder(fMemoryDmc);
            }
        };
        fSession.getExecutor().submit(runnable).get();
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		// Select the binary to run the tests against
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();
		
		// Clear the references (not strictly necessary)
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
            	fSession.removeServiceEventListener(MIMemoryTest.this);
            }
        };
        fSession.getExecutor().submit(runnable).get();
        
        fBaseAddress = null;
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


	private byte[] valueToBytes(int val) {
		ByteBuffer buff = ByteBuffer.allocate(fWordSize);
		switch (fWordSize) {
		case 1:
			byte bvalue = (byte) val;
			return buff.put(bvalue).array();
		case 2:
			short svalue = (short) val;
			return buff.putShort(svalue).array();
		case 4:
			return buff.putInt(val).array();
		case 8:
			long lvalue = val;
			return buff.putLong(lvalue).array();
		default:
			return null;
		}
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
		IExpressionDMContext expressionDMC = SyncUtil.createExpression(ctx, expression);
		return new Addr64(SyncUtil.getExpressionValue(expressionDMC, IFormattedValues.HEX_FORMAT));
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
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		IMemoryDMContext dmc = null;
		long offset = 0;
		int count = 1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Unknown context type");

		// Perform the test
		try {
			SyncUtil.readMemory(dmc, fBaseAddress, offset, fWordSize, count);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// readWithInvalidAddress
	// Test that an invalid address is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void readWithInvalidAddress() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		SyncUtil.step(StepType.STEP_RETURN);

		// Setup call parameters
		long offset = 0;
		int count = 1;
		fBaseAddress = new Addr64("0");

		// Perform the test
		MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count);

		// Ensure that we receive a block of invalid memory bytes
		byte flags = MemoryByte.ENDIANESS_KNOWN;
		if (fByteOrder == ByteOrder.BIG_ENDIAN) {
			flags |= MemoryByte.BIG_ENDIAN;
		}

		assertThat(buffer[0].getValue(), is((byte) 0));
		assertThat(buffer[0].getFlags(), is(flags));

		// Ensure no MemoryChangedEvent event was received
		assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
	}

	// ------------------------------------------------------------------------
	// readWithInvalidWordSize
	// Test that an invalid word size is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void readWithInvalidWordSize() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = -1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Word size not supported (< 1)");

		// Perform the test
		try {
			SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, 0, count);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// readWithInvalidCount
	// Test that an invalid count is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void readWithInvalidCount() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = -1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Perform the test
		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Invalid word count (< 0)");

		// Perform the test
		try {
			SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// readCharVaryingBaseAddress
	// Test the reading of individual bytes by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void readCharVaryingBaseAddress() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		int count = 1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Verify that all bytes are '0'
		for (int i = 0; i < BLOCK_SIZE; i++) {
			IAddress address = fBaseAddress.add(i);
			MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, address, 0, fWordSize, count);
			MemoryByteBuffer memBuf = new MemoryByteBuffer(buffer, fByteOrder, fWordSize);
			assertThat(memBuf.getNextWord(), is(0L));
		}

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:setBlocks");
		SyncUtil.step(StepType.STEP_RETURN);

		// Verify that all bytes are set
		for (long i = 0; i < BLOCK_SIZE; i++) {
			IAddress address = fBaseAddress.add(i);
			MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, address, 0, fWordSize, count);
			MemoryByteBuffer memBuf = new MemoryByteBuffer(buffer, fByteOrder, fWordSize);

			assertThat(memBuf.getNextWord(), is(i));
		}

		// Ensure no MemoryChangedEvent event was received
		assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
	}

	// ------------------------------------------------------------------------
	// readCharVaryingOffset
	// Test the reading of individual bytes by varying the offset
	// ------------------------------------------------------------------------
	@Test
	public void readCharVaryingOffset() throws Throwable {

		// Run to the point where the array is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		int count = 1;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Verify that all bytes are '0'
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count);
			MemoryByteBuffer memBuf = new MemoryByteBuffer(buffer, fByteOrder, fWordSize);
			assertThat(memBuf.getNextWord(), is(0L));
		}

		// Run to the point where the array is set
		SyncUtil.runToLocation("MemoryTestApp.cc:setBlocks");
		SyncUtil.step(StepType.STEP_RETURN);

		// Verify that all bytes are set
		for (long offset = 0; offset < BLOCK_SIZE; offset++) {
			MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count);
			MemoryByteBuffer memBuf = new MemoryByteBuffer(buffer, fByteOrder, fWordSize);
			assertThat(memBuf.getNextWord(), is(offset));
		}

		// Ensure no MemoryChangedEvent event was received
		assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
	}

	// ------------------------------------------------------------------------
	// readCharArray
	// Test the reading of a byte array
	// ------------------------------------------------------------------------
	@Test
	public void readCharArray() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		int count = BLOCK_SIZE;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Get the memory block
		MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, count);
		MemoryByteBuffer memBuf = new MemoryByteBuffer(buffer, fByteOrder, fWordSize);

		// Verify that all bytes are '0'
		for (int i = 0; i < count; i++) {
			assertThat(memBuf.getNextWord(), is(0L));
		}

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:setBlocks");
		SyncUtil.step(StepType.STEP_RETURN);

		// Get the memory block
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, count);
		memBuf = new MemoryByteBuffer(buffer, fByteOrder, fWordSize);

		// Verify that all bytes are '0'
		for (long i = 0; i < count; i++) {
			assertThat(memBuf.getNextWord(), is(i));

		}

		// Ensure no MemoryChangedEvent event was received
		assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
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
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = 1;
		byte[] buffer = new byte[count * fWordSize];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Unknown context type");

		// Perform the test
		try {
			SyncUtil.writeMemory(null, fBaseAddress, offset, fWordSize, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// writeWithInvalidAddress
	// Test that an invalid address is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void writeWithInvalidAddress() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		SyncUtil.step(StepType.STEP_RETURN);

		// Setup call parameters
		long offset = 0;
		int count = 1;
		byte[] buffer = new byte[count * fWordSize];
		fBaseAddress = new Addr64("0");

		expectedException.expect(ExecutionException.class);
		String expectedStr1 = "Cannot access memory at address";
		// Error message for new -data-write-memory-bytes command
		String expectedStr2 = "Could not write memory";
		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage(anyOf(
				containsString(expectedStr1),
				containsString(expectedStr2)));

		// Perform the test
		try {
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// writeWithInvalidWordSize
	// Test that an invalid word size is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void writeWithInvalidWordSize() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = -1;
		byte[] buffer = new byte[1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Word size not supported (< 1)");

		// Perform the test
		try {
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, 0, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// writeWithInvalidCount
	// Test that an invalid count is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void writeWithInvalidCount() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = -1;
		byte[] buffer = new byte[1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Invalid word count (< 0)");

		// Perform the test
		try {
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// writeWithInvalidBuffer
	// Test that the buffer contains at least count bytes
	// ------------------------------------------------------------------------
	@Test
	public void writeWithInvalidBuffer() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = 10;
		byte[] buffer = new byte[count * fWordSize - 1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Buffer too short");

		// Perform the test
		try {
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// writeCharVaryingAddress
	// Test the writing of individual bytes by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void writeCharVaryingAddress() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = BLOCK_SIZE;
		//initialize write data buffer
		byte[] buffer;

		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		ServiceEventWaitor<IMemoryChangedEvent> eventWaitor = new ServiceEventWaitor<>(
				fSession, IMemoryChangedEvent.class);

		// Perform the test
		for (int i = 0; i < count; i++) {

			// [1] Ensure that the memory byte = 0
			MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, i, fWordSize, 1);
			assertEquals("Wrong value read at offset " + i, (byte) 0, block[0].getValue());

			// [2] Write a byte value (count - i - 1)
			IAddress address = fBaseAddress.add(i);
			byte expected = (byte) (count - i - 1);
			buffer = valueToBytes(expected);
			buffer[0] = expected;
			SyncUtil.writeMemory(fMemoryDmc, address, offset, fWordSize, 1, buffer);

			// [3] Verify that the correct MemoryChangedEvent was sent
			IMemoryChangedEvent event = eventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));
			assertThat(event.getAddresses().length, is(1));
			assertThat(event.getAddresses()[0], is(address));

			// [4] Verify that the memory byte was written correctly
			block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, i, fWordSize, 1);
			assertEquals("Wrong value read at offset " + i, expected, block[0].getValue());
		}

		// Ensure the MemoryChangedEvent events were received
		assertEquals("Incorrect count of MemoryChangedEvent", BLOCK_SIZE, getEventCount());
		assertEquals("Incorrect count of events for distinct addresses", BLOCK_SIZE, getAddressCount());
	}

	// ------------------------------------------------------------------------
	// writeCharVaryingOffset
	// Test the writing of individual bytes by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void writeCharVaryingOffset() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		int count = BLOCK_SIZE;
		byte[] buffer;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		ServiceEventWaitor<IMemoryChangedEvent> eventWaitor = new ServiceEventWaitor<>(
				fSession, IMemoryChangedEvent.class);

		// Perform the test
		for (int offset = 0; offset < count; offset++) {

			// [1] Ensure that the memory byte = 0
			MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, 1);
			assertEquals("Wrong value read at offset " + offset, (byte) 0, block[0].getValue());

			// [2] Write a byte value (count - offset - 1)
			byte expected = (byte) (count - offset - 1);
			buffer = valueToBytes(expected);
			buffer[0] = expected;
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, 1, buffer);

			// [3] Verify that the correct MemoryChangedEvent was sent
			IMemoryChangedEvent event = eventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));
			assertThat(event.getAddresses().length, is(1));
			assertThat(event.getAddresses()[0], is(fBaseAddress.add(offset)));

			// [4] Verify that the memory byte was written correctly
			block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, 1);
			assertEquals("Wrong value read at offset " + offset, expected, block[0].getValue());
		}

		// Ensure the MemoryChangedEvent events were received
		assertEquals("Incorrect count of MemoryChangedEvent", BLOCK_SIZE, getEventCount());
		assertEquals("Incorrect count of events for distinct addresses", BLOCK_SIZE, getAddressCount());
	}

	// ------------------------------------------------------------------------
	// writeCharArray
	// Test the writing of a byte array
	// ------------------------------------------------------------------------
	@Test
	public void writeCharArray() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		int count = BLOCK_SIZE;
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Make sure that the memory block is zeroed
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, count);
		MemoryByteBuffer memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < count; i++) {
			assertThat(memBuf.getNextWord(), is(0L));
		}

		// Write an initialized memory block
		ByteBuffer buffer = ByteBuffer.allocate(count * fWordSize); 
		for (int i = 0; i < count; i++) {
			buffer.put(valueToBytes(i));
		}

		SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, count, buffer.array());

		// Make sure that the memory block is initialized
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, count);
		memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (long i = 0; i < count; i++) {
			assertThat(memBuf.getNextWord(), is(i));
		}

		// Ensure the MemoryChangedEvent events were received
		assertEquals("Incorrect count of MemoryChangedEvent", 1, getEventCount());
		assertEquals("Incorrect count of events for distinct addresses", BLOCK_SIZE, getAddressCount());
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
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = 1;
		byte[] pattern = new byte[count * fWordSize];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Unknown context type");

		// Perform the test
		try {
			SyncUtil.fillMemory(null, fBaseAddress, offset, fWordSize, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("Incorrect count of MemoryChangedEvent", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// fillWithInvalidAddress
	// Test that an invalid address is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void fillWithInvalidAddress() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		SyncUtil.step(StepType.STEP_RETURN);

		// Setup call parameters
		long offset = 0;
		int count = 1;

		byte[] pattern = valueToBytes(1);
		fBaseAddress = new Addr64("0");
		// Depending on the GDB, a different command can be used.  Both error message are valid.
		// Error message for -data-write-memory command
		String expectedStr1 = "Cannot access memory at address";
		// Error message for new -data-write-memory-bytes command
		String expectedStr2 = "Could not write memory";
		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage(anyOf(
				containsString(expectedStr1),
				containsString(expectedStr2)));

		// Perform the test
		try {
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// fillWithInvalidWordSize
	// Test that an invalid word size is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void fillWithInvalidWordSize() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = 1;
		byte[] pattern = new byte[1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Word size not supported (< 1)");

		// Perform the test
		try {
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, 0, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// fillWithInvalidCount
	// Test that an invalid count is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void fillWithInvalidCount() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = -1;
		byte[] pattern = new byte[1];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Invalid repeat count (< 0)");

		// Perform the test
		try {
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// fillWithInvalidPattern
	// Test that an empty pattern is caught and generates an error
	// ------------------------------------------------------------------------
	@Test
	public void fillWithInvalidPattern() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int count = 1;
		byte[] pattern = new byte[0];
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Empty pattern");

		// Perform the test
		try {
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertEquals("MemoryChangedEvent problem: expected 0 events", 0, getEventCount());
		}
	}

	// ------------------------------------------------------------------------
	// writePatternVaryingAddress
	// Test the writing of the pattern by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void writePatternVaryingAddress() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		int repetitionCount = 1;
		int patternLen = 4;

		// Prepare the buffer
		ByteBuffer patternBuffer = ByteBuffer.allocate(patternLen * fWordSize);
		for (int i = 0; i < patternLen; i++) {
			patternBuffer.put(valueToBytes(i));
		}

		byte[] pattern = patternBuffer.array();
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Ensure that the memory is zeroed
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, repetitionCount * patternLen);
		MemoryByteBuffer memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < (repetitionCount * patternLen); i++) {
			assertThat(memBuf.getNextWord(), is(0L));
		}

		for (int i = 0; i < BLOCK_SIZE; i += patternLen) {
			IAddress address = fBaseAddress.add(i);
			SyncUtil.fillMemory(fMemoryDmc, address, 0, fWordSize, repetitionCount, pattern);
		}

		// Verify that the memory is correctly set
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, repetitionCount * patternLen);
		memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (long i = 0; i < repetitionCount; i++) {
			for (long j = 0; j < patternLen; j++) {
				assertThat(memBuf.getNextWord(), is(j));
			}
		}

		// Ensure the MemoryChangedEvent events were received
		assertEquals("Incorrect count of MemoryChangedEvent", BLOCK_SIZE / patternLen, getEventCount());
		assertEquals("Incorrect count of events for distinct addresses", BLOCK_SIZE, getAddressCount());
	}

	// ------------------------------------------------------------------------
	// writePatternVaryingOffset
	// Test the writing of the pattern by varying the base address
	// ------------------------------------------------------------------------
	@Test
	public void writePatternVaryingOffset() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		long offset = 0;
		int patternLength = 4;
		int patternRepetitionCount = BLOCK_SIZE / patternLength;

		ByteBuffer patternBuf = ByteBuffer.allocate(patternLength * fWordSize);
		for (int i = 0; i < patternLength; i++) {
			patternBuf.put(valueToBytes(i));
		}

		byte[] pattern = patternBuf.array();
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Ensure that the memory is zeroed
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, patternRepetitionCount * patternLength);
		MemoryByteBuffer memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < (patternRepetitionCount * patternLength); i++) {
			assertThat(memBuf.getNextWord(), is(0L));
		}

		for (int i = 0; i < patternRepetitionCount; i++) {
			offset = i * patternLength;
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, fWordSize, 1, pattern);
		}

		// Verify that the memory is correctly set
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, patternRepetitionCount * patternLength);
		memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < patternRepetitionCount; i++)
			for (long j = 0; j < patternLength; j++) {
				assertThat(memBuf.getNextWord(), is(j));
			}

		// Ensure the MemoryChangedEvent events were received
		assertEquals("Incorrect count of MemoryChangedEvent", patternRepetitionCount, getEventCount());
		assertEquals("Incorrect count of events for distinct addresses", BLOCK_SIZE, getAddressCount());
	}

	// ------------------------------------------------------------------------
	// writePatternCountTimes
	// Test the writing of the pattern [count] times
	// ------------------------------------------------------------------------
	@Test
	public void writePatternCountTimes() throws Throwable {

		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		int maxPatternRepetitionCount = 64;
		int patternLength = 4;

		ByteBuffer mBuff = ByteBuffer.allocate(patternLength * fWordSize);
		for (int i = 0; i < patternLength; i++) {
			mBuff.put(valueToBytes(i));
		}

		byte[] pattern = mBuff.array();

		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Ensure that the memory is zeroed
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, maxPatternRepetitionCount * patternLength);
		MemoryByteBuffer memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < (maxPatternRepetitionCount * patternLength); i++) {
			assertThat(memBuf.getNextWord(), is(0L));
		}

		// Write the pattern [count] times
		SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, maxPatternRepetitionCount, pattern);

		// Verify that the memory is correctly set
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, maxPatternRepetitionCount * patternLength);
		memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < maxPatternRepetitionCount; i++) {
			for (long j = 0; j < patternLength; j++) {
				assertThat(memBuf.getNextWord(), is(j));
			}
		}

		// Ensure the MemoryChangedEvent events were received
		assertEquals("Incorrect count of MemoryChangedEvent", 1, getEventCount());
		assertEquals("Incorrect count of events for distinct addresses", BLOCK_SIZE, getAddressCount());

	}

	// ------------------------------------------------------------------------
	// asynchronousReadWrite
	// Test the asynchronous reading/writing of individual bytes (varying offset)
	// ------------------------------------------------------------------------
	@Test
	public void asynchronousReadWrite() throws Throwable {
		// Run to the point where the array is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Verify asynchronously that all bytes are '0'
		MemoryReadQuery readQueries[] = new MemoryReadQuery[BLOCK_SIZE];

		// Send many read queries
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			readQueries[offset] = new MemoryReadQuery(fMemoryService,
					fMemoryDmc, fBaseAddress, offset, fWordSize, 1);
			fMemoryService.getExecutor().submit(readQueries[offset]);
		}

		// Wait for all the queries to finish
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			MemoryByte[] data = readQueries[offset].get();
			assertThat(data.length, is(1));
			assertThat(data[0].getValue(), is((byte) 0));
		}

		// Write asynchronously
		ServiceEventWaitor<IMemoryChangedEvent> eventWaitor = new ServiceEventWaitor<IMemoryChangedEvent>(
				fSession, IMemoryChangedEvent.class);
		MemoryWriteQuery writeQueries[] = new MemoryWriteQuery[BLOCK_SIZE];
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			byte[] block = new byte[1];
			block[0] = (byte) offset;

			writeQueries[offset] = new MemoryWriteQuery(fMemoryService,
					fMemoryDmc, fBaseAddress, offset, fWordSize, 1, block);
			fMemoryService.getExecutor().submit(writeQueries[offset]);
		}

		// Wait for all the queries to finish
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			writeQueries[offset].get();
		}

		// Expect BLOCK_SIZE "memory changed" events
		for (int i = 0; i < BLOCK_SIZE; i++) {
			eventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));
		}

		// Verify asynchronously that all bytes are set
		// Send many read queries
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			readQueries[offset] = new MemoryReadQuery(fMemoryService,
					fMemoryDmc, fBaseAddress, offset, fWordSize, 1);
			fMemoryService.getExecutor().submit(readQueries[offset]);
		}

		// Wait for all the queries to finish
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			MemoryByte[] data = readQueries[offset].get();
			assertThat(data.length, is(1));
			assertThat(data[0].getValue(), is((byte) offset));
		}
	}

	private void memoryCacheReadHelper(long offset, int count, int wordSize)
			throws InterruptedException, ExecutionException {
		MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, wordSize, count);
		MemoryByteBuffer memBuf = new MemoryByteBuffer(buffer, fByteOrder, fWordSize);

		// Verify that all bytes are correctly set
		for (long i = 0; i < count; i++) {
			assertThat("index " + i, memBuf.getNextWord(), is(offset + i));
		}
	}

	// ------------------------------------------------------------------------
	// memoryCacheRead
	// Get a bunch of blocks to exercise the memory cache
	// ------------------------------------------------------------------------
	@Test
	public void memoryCacheRead() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.runToLocation("MemoryTestApp.cc:setBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		// Get the 'reference' memory block
		memoryCacheReadHelper(0, BLOCK_SIZE, fWordSize);

		// Clear the cache
		SyncUtil.step(StepType.STEP_OVER);

		// Get a first block
		memoryCacheReadHelper(0, 64, fWordSize);

		// Get a second block
		memoryCacheReadHelper(128, 64, fWordSize);

		// Get a third block between the first 2
		memoryCacheReadHelper(80, 32, fWordSize);

		// Get a block that is contiguous to the end of an existing block
		memoryCacheReadHelper(192, 32, fWordSize);

		// Get a block that ends beyond an existing block
		memoryCacheReadHelper(192, 64, fWordSize);

		// Get a block that will require 2 reads (for the gaps between blocks 1-2 and 2-3)
		memoryCacheReadHelper(32, 128, fWordSize);

		// Get a block that involves multiple cached blocks
		memoryCacheReadHelper(48, 192, fWordSize);

		// Get the whole block
		memoryCacheReadHelper(0, BLOCK_SIZE, fWordSize);

		// Ensure no MemoryChangedEvent event was received
		assertEquals("Incorrect count of MemoryChangedEvent", 0, getEventCount());
	}

	private static class MemoryReadQuery extends Query<MemoryByte[]> {

		private IMemory fMemoryService;
		private IMemoryDMContext fMemoryDmc;
		private IAddress fBaseAddress;
		private int fOffset;
		private int fWordSize;
		private int fCount;

		public MemoryReadQuery(IMemory fMemoryService,
				IMemoryDMContext memoryDmc, IAddress baseAddress, int offset,
				int wordSize, int count) {
			this.fMemoryService = fMemoryService;
			this.fMemoryDmc = memoryDmc;
			this.fBaseAddress = baseAddress;
			this.fOffset = offset;
			this.fWordSize = wordSize;
			this.fCount = count;
		}

		@Override
		protected void execute(DataRequestMonitor<MemoryByte[]> rm) {
			fMemoryService.getMemory(fMemoryDmc, fBaseAddress, fOffset,
					fWordSize, fCount, rm);
		}
	}

	private static class MemoryWriteQuery extends Query<Void> {

		private IMemory fMemoryService;
		private IMemoryDMContext fMemoryDmc;
		private IAddress fBaseAddress;
		private int fOffset;
		private int fWordSize;
		private int fCount;
		private byte[] fBuffer;

		public MemoryWriteQuery(IMemory fMemoryService,
				IMemoryDMContext memoryDmc, IAddress baseAddress, int offset,
				int wordSize, int count, byte[] buffer) {
			this.fMemoryService = fMemoryService;
			this.fMemoryDmc = memoryDmc;
			this.fBaseAddress = baseAddress;
			this.fOffset = offset;
			this.fWordSize = wordSize;
			this.fCount = count;
			this.fBuffer = buffer;
		}

		@Override
		protected void execute(DataRequestMonitor<Void> rm) {
			fMemoryService.setMemory(fMemoryDmc, fBaseAddress, fOffset,
					fWordSize, fCount, fBuffer, rm);
		}
	}
}
