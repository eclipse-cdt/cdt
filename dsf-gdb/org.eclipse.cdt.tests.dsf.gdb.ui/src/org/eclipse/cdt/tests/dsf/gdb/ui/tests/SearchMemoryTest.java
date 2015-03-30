/*******************************************************************************
 * Copyright (c) 2015 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson) - Find / Replace for 16 bits addressable sizes (Bug 462073)
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.ui.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.ui.memory.search.FindReplaceDialog;
import org.eclipse.cdt.debug.ui.memory.search.FindReplaceDialog.IMemorySearchQuery;
import org.eclipse.cdt.debug.ui.memory.search.FindReplaceDialog.SearchPhrase;
import org.eclipse.cdt.debug.ui.memory.search.MemoryByteBuffer;
import org.eclipse.cdt.debug.ui.memory.search.MemoryMatch;
import org.eclipse.cdt.debug.ui.memory.search.MemorySearch;
import org.eclipse.cdt.debug.ui.memory.search.MemorySearchResult;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.internal.memory.GdbMemoryBlock;
import org.eclipse.cdt.dsf.gdb.internal.memory.GdbMemoryBlock.MemorySpaceDMContext;
import org.eclipse.cdt.dsf.gdb.internal.memory.GdbMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.ui.UITestsPlugin;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.search.ui.ISearchResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/*
 * This is testing the memory search functionality needed by the Find Replace Dialog of the memory view.
 * 
 * Refer to the JUnit4 documentation for an explanation of the annotations.
 */

@RunWith(BackgroundRunner.class)
public class SearchMemoryTest extends BaseTestCase {
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
        		fServicesTracker = new DsfServicesTracker(UITestsPlugin.getDefault().getBundleContext(), fSession.getId());
        		assert(fServicesTracker != null);

        		fRunControl = fServicesTracker.getService(MIRunControl.class);
        		assert(fRunControl != null);

        		fMemoryService = fServicesTracker.getService(IMemory.class);
        		assert(fMemoryService != null);

        		fExpressionService = fServicesTracker.getService(IExpressions.class);
        		assert(fExpressionService != null);

        		fSession.addServiceEventListener(SearchMemoryTest.this, null);
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
            	fSession.removeServiceEventListener(SearchMemoryTest.this);
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

	/**
	 * Convert a given value to a byte array with the number of octets needed by the addressable size of the
	 * target system
	 */
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

	/**
	 * Execute the actual search test for numerical values
	 * @param forward - if true searches forward, false searches backwards
	 */
	private void searchPatterCountTimes(boolean forward) throws Throwable {
	
		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
	    IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
	
		// Setup call parameters
		int maxPatternRepetitionCount = 64;
		// patter length in addressable units
		int patternLength = 4;
	
		// Create the pattern and adjust it to the system's minimum addressable size
		ByteBuffer mBuff = ByteBuffer.allocate(patternLength * fWordSize);
		for (int i = 0; i < patternLength; i++) {
			mBuff.put(valueToBytes(i));
		}
	
		byte[] pattern = mBuff.array();
	
		// Resolve the base address for this test
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");
	
		int nWords = maxPatternRepetitionCount * patternLength;
		
		// Ensure that the memory is zeroed
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, maxPatternRepetitionCount * patternLength);
		MemoryByteBuffer memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < nWords; i++) {
			assertThat(memBuf.getNextWord(), is(0L));
		}
	
		// Write the pattern [count] times
		SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, maxPatternRepetitionCount, pattern);
	
		// Verify that the memory is correctly set
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, nWords);
		memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < maxPatternRepetitionCount; i++) {
			for (long j = 0; j < patternLength; j++) {
				assertThat(memBuf.getNextWord(), is(j));
			}
		}
	
		// Ensure the MemoryChangedEvent events were received
		assertEquals("Incorrect count of MemoryChangedEvent", 1, getEventCount());
		assertEquals("Incorrect count of events for distinct addresses", BLOCK_SIZE, getAddressCount());
		
		IMemoryBlockExtension memoryBlock = getMemoryBlock(fBaseAddress); 
		Properties properties = new Properties();
		
		// Get a handle to the search functionality
		MemorySearch search = new MemorySearch( memoryBlock,  null,  properties,  null);
		//Adjust endAddress since we are including base address in the query
		IAddress endAddress = fBaseAddress.add(nWords-1);
	
		// Resolve target Endianess
		assert(block != null && block.length > 0);
		boolean littleEndian = !block[0].isBigEndian();
		
		byte[] searchBytes = pattern;
		if (littleEndian) {
			assertTrue("Memory Search is not supportted for " +
					"Little Endian systems with addressable sizes bigger than one octet", fWordSize == 1);
			// Invert the search phrase octets
			searchBytes = new byte[pattern.length];
			for (int i = 0; i < pattern.length; i++) {
				searchBytes[i] = pattern[pattern.length - i - 1];
			}
		}
	
		// Get our find text converted to a SearchPhrase as used by the FindReplaceDialog
		String findText = DatatypeConverter.printHexBinary(searchBytes);
		SearchPhrase phrase = new FindReplaceDialog.BigIntegerSearchPhrase(new BigInteger(findText, 16), 16); //$NON-NLS-1$
		
		byte[] replaceData = null;
		boolean all = true;
		boolean replaceThenFind = false;
		IMemorySearchQuery query = search.createSearchQuery(fBaseAddress.getValue(), endAddress.getValue(), phrase, forward, replaceData, all, replaceThenFind);
		
		//Trigger query and wait for the result
		Job job = performFind(query, null, all);
		job.join();
		
		// read the results
		ISearchResult iResult = query.getSearchResult();
		assert(iResult instanceof MemorySearchResult);
		MemorySearchResult result = (MemorySearchResult) iResult;
		MemoryMatch[] matches = result.getMatches();
		
		assertEquals("Wrong number of expected matches", 	maxPatternRepetitionCount, matches.length);
	}

	/**
	 * Execute the actual search test for ASCII values
	 * @param forward - if true searches forward, false searches backwards
	 */
	private void searchPatternCountTimes_ASCII(boolean forward) throws Throwable {
		// Run to the point where the variable is zeroed
		SyncUtil.runToLocation("MemoryTestApp.cc:zeroBlocks");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_RETURN);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// Setup call parameters
		int maxPatternRepetitionCount = 64;

		// Define string pattern
		String strPattern = "abcd";

		// Resolve the string pattern using the number of octets needed as per the system's minimum
		// addressable size
		byte[] octetPattern = strPattern.getBytes();
		// pattern length in addressable units i.e. one word per character
		int patternLength = octetPattern.length;
	
		ByteBuffer mBuff = ByteBuffer.allocate(patternLength * fWordSize);
		for (int i = 0; i < patternLength; i++) {
			mBuff.put(valueToBytes(octetPattern[i]));
		}

		byte[] pattern = mBuff.array();	
		
		// Define the expected memory
		ByteBuffer expectedBytesBuff = ByteBuffer.allocate(maxPatternRepetitionCount * pattern.length);
		for (int i=0; i < maxPatternRepetitionCount; i++)  {
			expectedBytesBuff.put(pattern);
		}
		
		MemoryByteBuffer expectedMemBlock = new MemoryByteBuffer(expectedBytesBuff, fByteOrder, fWordSize);
		
		// Select a base address
		fBaseAddress = evaluateExpression(frameDmc, "&charBlock");

		int nWords = maxPatternRepetitionCount * patternLength;
		
		// Ensure that the memory is zeroed
		MemoryByte[] block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, nWords);
		MemoryByteBuffer memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < nWords; i++) {
			assertThat(memBuf.getNextWord(), is(0L));
		}

		// Write the pattern [count] times
		SyncUtil.fillMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, maxPatternRepetitionCount, pattern);

		// Verify that the memory is correctly set
		block = SyncUtil.readMemory(fMemoryDmc, fBaseAddress, 0, fWordSize, nWords);
		memBuf = new MemoryByteBuffer(block, fByteOrder, fWordSize);
		for (int i = 0; i < nWords; i++) {
			long expected = expectedMemBlock.getNextWord();
			long actual = memBuf.getNextWord();
			assertThat(actual, is(expected));
		}

		// Ensure the MemoryChangedEvent events were received
		assertEquals("Incorrect count of MemoryChangedEvent", 1, getEventCount());
		assertEquals("Incorrect count of events for distinct addresses", nWords, getAddressCount());
		
		// Create a memory search for the memory block
		IMemoryBlockExtension memoryBlock = getMemoryBlock(fBaseAddress); 
		Properties properties = new Properties();
		MemorySearch search = new MemorySearch( memoryBlock,  null,  properties,  null);
		
		//Adjust endAddress since we are including base address in the query
		IAddress endAddress = fBaseAddress.add(nWords-1);
		SearchPhrase phrase = new FindReplaceDialog.AsciiSearchPhrase(strPattern, true, fWordSize); //$NON-NLS-1$
		
		byte[] replaceData = null;
		boolean all = true;
		boolean replaceThenFind = false;
		IMemorySearchQuery query = search.createSearchQuery(fBaseAddress.getValue(), endAddress.getValue(), phrase, forward, replaceData, all, replaceThenFind);
		
		//Trigger query and wait for the result
		Job job = performFind(query, null, all);
		job.join();
		
		// read the results
		ISearchResult iResult = query.getSearchResult();
		assert(iResult instanceof MemorySearchResult);
		MemorySearchResult result = (MemorySearchResult) iResult;
		MemoryMatch[] matches = result.getMatches();
		
		assertEquals("Wrong number of expected matches", 	maxPatternRepetitionCount, matches.length);
	}
	
	private Job performFind(final IMemorySearchQuery query, final byte[] replaceData, final boolean all) {
			Job job = new Job("Searching memory") { //$NON-NLS-1$
				@Override
				public IStatus run(IProgressMonitor monitor) {
					return query.run(monitor);
				}
			};
			
			job.schedule();
			return job;
	}

	private IMemoryBlockExtension getMemoryBlock(IAddress address) {
		IMemoryBlockExtension block = null;
		String memorySpaceID = "__ldm";
		// Use a memory space context if the memory space id is valid
		IMemoryDMContext memoryCtx = new MemorySpaceDMContext(fSession.getId(), memorySpaceID, fMemoryDmc);
	
		ILaunchConfiguration config = getGDBLaunch().getLaunchConfiguration();
		DsfMemoryBlockRetrieval retrieval;
		try {
			retrieval = new GdbMemoryBlockRetrieval(GdbLaunchDelegate.GDB_DEBUG_MODEL_ID, config, fSession);
			block = new GdbMemoryBlock(retrieval, memoryCtx, GdbLaunchDelegate.GDB_DEBUG_MODEL_ID, address.toHexAddressString(), address.getValue(), fWordSize, 0, memorySpaceID);
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return block;
	}

	// ------------------------------------------------------------------------
	// Test Triggers
	// ------------------------------------------------------------------------
	@Test
	public void searchPatternCountTimesForward() throws Throwable {
		searchPatterCountTimes(true);
	}
	
	@Test
	public void searchPatternCountTimesBackwards() throws Throwable {
		searchPatterCountTimes(false);
	}
	
	@Test
	public void searchPatternCountTimesForward_ASCII() throws Throwable {
		searchPatternCountTimes_ASCII(true);
	}
	
	@Test
	public void searchPatternCountTimesBackwards_ASCII() throws Throwable {
		searchPatternCountTimes_ASCII(false);
	}
}
