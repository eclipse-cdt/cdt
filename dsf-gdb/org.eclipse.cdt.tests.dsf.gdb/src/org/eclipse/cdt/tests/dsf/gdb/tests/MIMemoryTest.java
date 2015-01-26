/*******************************************************************************
 * Copyright (c) 2007, 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		- Initial Implementation
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
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
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.debug.core.model.MemoryByte;
import org.hamcrest.CoreMatchers;
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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Unknown context type");

		// Perform the test
		try {
			SyncUtil.readMemory(dmc, fBaseAddress, offset, word_size, count);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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
		MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		//	Ensure that we receive a block of invalid memory bytes
		assertTrue("Wrong value: expected '0, 32', received '" + buffer[0].getValue() + ", " + buffer[0].getFlags() + "'",
				(buffer[0].getValue() == (byte) 0) && (buffer[0].getFlags() == (byte) 32));

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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Word size not supported (< 1)");

		// Perform the test
		try {
			SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, 0, count);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Invalid word count (< 0)");

		// Perform the test
		try {
			SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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
			MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, address, offset, word_size, count);
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
			MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, address, offset, word_size, count);
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
			MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
			assertTrue("Wrong value read at offset " + offset + ": expected '" + 0 + "', received '" + buffer[0].getValue() + "'",
				(buffer[0].getValue() == (byte) 0));
		}

		// Run to the point where the array is set
		SyncUtil.addBreakpoint("MemoryTestApp.cc:setBlocks", true);
		SyncUtil.resumeUntilStopped();
		SyncUtil.step(StepType.STEP_RETURN);

		// Verify that all bytes are set
		for (int offset = 0; offset < BLOCK_SIZE; offset++) {
			MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
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
		MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

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
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Unknown context type");

		// Perform the test
		try {
			SyncUtil.writeMemory(null, fBaseAddress, offset, word_size, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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

		expectedException.expect(ExecutionException.class);
		// Don't test the error message since it changes from one GDB version to another

		// Perform the test
		try {
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Word size not supported (< 1)");

		// Perform the test
		try {
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, 0, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Invalid word count (< 0)");

		// Perform the test
		try {
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Buffer too short");

		// Perform the test
		try {
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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
			MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, i, word_size, 1);
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[0].getValue() + "'",
					(block[0].getValue() == (byte) 0));
			
			// [2] Write a byte value (count - i - 1)
			IAddress address = fBaseAddress.add(i);
			byte expected = (byte) (count - i - 1);
			buffer[0] = expected;
			SyncUtil.writeMemory(fMemoryDmc, address, offset, word_size, 1, buffer);

			// [3] Verify that the correct MemoryChangedEvent was sent
			// (I hardly believe there are no synchronization problems here...)
			// TODO FOR REVIEW: This assert fails
			//assertTrue("MemoryChangedEvent problem at offset " + i + ": expected " + (i + 1) + " events, received " + getEventCount(),
			//		getEventCount() == (i + 1));
			//assertTrue("MemoryChangedEvent problem at offset " + i, fMemoryAddressesChanged[i]);

			// [4] Verify that the memory byte was written correctly
			block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, i, word_size, 1);
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
			MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, 1);
			assertTrue("Wrong value read at offset " + offset + ": expected '" + 0 + "', received '" + block[0].getValue() + "'",
					(block[0].getValue() == (byte) 0));
			
			// [2] Write a byte value (count - offset - 1)
			byte expected = (byte) (count - offset - 1);
			buffer[0] = expected;
			SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, 1, buffer);

			// [3] Verify that the correct MemoryChangedEvent was sent
			// TODO FOR REVIEW: this fails
			//assertTrue("MemoryChangedEvent problem at offset " + offset + ": expected " + (offset + 1) + " events, received " + getEventCount(),
			//		getEventCount() == (offset + 1));
			//assertTrue("MemoryChangedEvent problem at offset " + offset, fMemoryAddressesChanged[offset]);

			// [4] Verify that the memory byte was written correctly
			block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, 1);
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
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[i].getValue() + "'",
				(block[i].getValue() == (byte) 0));
		}

		// Write an initialized memory block
		byte[] buffer = new byte[count];
		for (int i = 0; i < count; i++) {
			buffer[i] = (byte) i;
		}
		SyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);

		// Make sure that the memory block is initialized
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
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
	// AsyncCompletionWaitor wait = AsyncUtil.fillMemory( tests
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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Unknown context type");

		// Perform the test
		try {
			SyncUtil.fillMemory(null, fBaseAddress, offset, word_size, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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

		// Depending on the GDB, a different command can be used.  Both error message are valid.
		// Error message for -data-write-memory command
		String expectedStr1 = "Cannot access memory at address";
		// Error message for new -data-write-memory-bytes command
		String expectedStr2 = "Could not write memory";
		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage(CoreMatchers.anyOf(
				CoreMatchers.containsString(expectedStr1),
				CoreMatchers.containsString(expectedStr2)));

		// Perform the test
		try {
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Word size not supported (< 1)");

		// Perform the test
		try {
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, 0, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Invalid repeat count (< 0)");

		// Perform the test
		try {
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Empty pattern");

		// Perform the test
		try {
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, pattern);
		} finally {
			// Ensure no MemoryChangedEvent event was received
			assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
		}
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
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count * length);
		for (int i = 0; i < (count * length); i++)
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[i].getValue() + "'",
					(block[i].getValue() == (byte) 0));
		
		for (int i = 0; i < BLOCK_SIZE; i += length) {
			IAddress address = fBaseAddress.add(i);
			SyncUtil.fillMemory(fMemoryDmc, address, offset, word_size, count, pattern);
		}

		// Verify that the memory is correctly set
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, word_size, count * length);
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
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count * length);;
		for (int i = 0; i < (count * length); i++)
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[i].getValue() + "'",
					(block[i].getValue() == (byte) 0));
		
		for (int i = 0; i < (BLOCK_SIZE / length); i++) {
			offset = i * length;
			SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, 1, pattern);
		}

		// Verify that the memory is correctly set
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, word_size, count * length);
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
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count * length);
		for (int i = 0; i < (count * length); i++)
			assertTrue("Wrong value read at offset " + i + ": expected '" + 0 + "', received '" + block[i].getValue() + "'",
					(block[i].getValue() == (byte) 0));
		
		// Write the pattern [count] times
		SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, pattern);

		// Verify that the memory is correctly set
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count * length);
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

		// Interesting issue. Believe it or not, requests can get serviced 
		// faster than we can queue them. E.g., we queue up five, and before we
		// queue the sixth, the five are serviced. Before, when that happened
		// the waitor went into the 'complete' state before we were done queuing
		// all the requests. To avoid that, we need to add our own tick and then
		// clear it once we're done queuing all the requests.
		
		// Verify asynchronously that all bytes are '0'
		@SuppressWarnings("unchecked")
		Query<MemoryByte[]>[] readQueries = new Query[BLOCK_SIZE];
		for (int offset = 0; offset < readQueries.length; offset++) {
			readQueries[offset] = AsyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		}

		for (Query<MemoryByte[]> query : readQueries) {
			MemoryByte[] result = query.get();
			assertEquals("Wrong value read", 0, result[0].getValue());
		}

		// Write asynchronously
		@SuppressWarnings("unchecked")
		Query<Void>[] writeQueries = new Query[BLOCK_SIZE];
		for (int offset = 0; offset < writeQueries.length; offset++) {
			byte[] block = new byte[count];
			block[0] = (byte) offset;
			writeQueries[offset] = AsyncUtil.writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, block);
		}

		for (Query<Void> query : writeQueries) {
			query.get();
		}

		// Ensure the MemoryChangedEvent events were received
		// TODO FOR REVIEW: This fails
		//assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " events, received " + getEventCount(),
		//		getEventCount() == BLOCK_SIZE);
		//assertTrue("MemoryChangedEvent problem: expected " + BLOCK_SIZE + " distinct addresses, received " + getAddressCount(),
		//		getAddressCount() == BLOCK_SIZE);

		// Verify asynchronously that all bytes are set
		for (int offset = 0; offset < readQueries.length; offset++) {
			readQueries[offset] = AsyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);
		}

		for (int offset = 0; offset < readQueries.length; offset++) {
			MemoryByte[] result = readQueries[offset].get();
			assertEquals("Wrong value read", (byte)offset, result[0].getValue());
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
		MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		// Verify that all bytes are set to 'i'
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) i));
		}

		// Clear the cache
		SyncUtil.step(StepType.STEP_OVER);

		// Get a first block
		offset =  0;
		count = 64;
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a second block
		offset =  128;
		count = 64;
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a third block between the first 2
		offset =  80;
		count = 32;
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a block that is contiguous to the end of an existing block
		offset =  192;
		count = 32;
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a block that ends beyond an existing block
		offset =  192;
		count = 64;
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a block that will require 2 reads (for the gaps between blocks 1-2 and 2-3)
		offset =  32;
		count = 128;
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}

		// Get a block that involves multiple cached blocks
		offset =  48;
		count = 192;
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		// Verify that all bytes are set to 'i'
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}
		
		// Get the whole block
		offset =  0;
		count = BLOCK_SIZE;
		buffer = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, offset, word_size, count);

		// Verify that all bytes are correctly set
		for (int i = 0; i < count; i++) {
			assertTrue("Wrong value read at offset " + i + ": expected '" + offset + i + "', received '" + buffer[i].getValue() + "'",
				(buffer[i].getValue() == (byte) (offset + i)));
		}
		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}

}
