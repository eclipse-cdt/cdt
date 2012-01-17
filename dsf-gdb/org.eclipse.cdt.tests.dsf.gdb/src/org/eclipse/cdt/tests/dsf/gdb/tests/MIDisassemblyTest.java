/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - Initial Implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.MIDisassembly;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.utils.Addr64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * This is the Disassembly Service test suite.
 * 
 * It is meant to be a regression suite to be executed automatically against
 * the DSF nightly builds.
 * 
 * It is also meant to be augmented with a proper test case(s) every time a
 * feature is added or in the event (unlikely :-) that a bug is found in the
 * Disassembly Service.
 * 
 * Refer to the JUnit4 documentation for an explanation of the annotations.
 */

@RunWith(BackgroundRunner.class)
public class MIDisassemblyTest extends BaseTestCase {

    private static final String FILE_NAME = "MemoryTestApp.cc";
    private static final int LINE_NUMBER = 35;
    private static final String INVALID_FILE_NAME = "invalid_filename";
    
    private final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    private DsfSession          fSession;
    private DsfServicesTracker  fServicesTracker;
    private IDisassemblyDMContext fDisassemblyDmc;
    private MIDisassembly       fDisassembly;
    private IExpressions        fExpressionService;

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
    public void testCaseInitialization() throws Exception {
        fSession = getGDBLaunch().getSession();
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
               // Get a reference to the memory service
                fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
                assert(fServicesTracker != null);
                    
                fDisassembly = fServicesTracker.getService(MIDisassembly.class);
                assert(fDisassembly != null);

                fExpressionService = fServicesTracker.getService(IExpressions.class);
                assert(fExpressionService != null);
            }
        };
        fSession.getExecutor().submit(runnable).get();
        
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
        fDisassemblyDmc = DMContexts.getAncestorOfType(containerDmc, IDisassemblyDMContext.class);
        assert(fDisassemblyDmc != null);

    }

    @After
    public void testCaseCleanup() {
        fExpressionService = null;
        fDisassembly = null;
        fServicesTracker.dispose();
        fServicesTracker = null;
    }

    // ========================================================================
    // Helper Functions
    // ========================================================================

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
    private IAddress evaluateExpression(String expression) throws Throwable
    {
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        // Create the expression and format contexts 
        final IExpressionDMContext expressionDMC = SyncUtil.createExpression(frameDmc, expression);
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
            @Override
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
     * getInstruction
     * ------------------------------------------------------------------------
     * Issues a disassembly request. The result is stored in fWait.
     * ------------------------------------------------------------------------
     * Typical usage:
     *  getInstruction(dmc, start, end);
     *  fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
     *  assertTrue(fWait.getMessage(), fWait.isOK());
     * ------------------------------------------------------------------------
     * @param dmc       the data model context
     * @param start     the start address (null == $pc)
     * @param end       the end address
     * @throws InterruptedException
     * ------------------------------------------------------------------------
     */
    private void getInstruction(final IDisassemblyDMContext dmc,
            final BigInteger startAddress, final BigInteger endAddress)
    throws InterruptedException
    {
        // Set the Data Request Monitor
        final DataRequestMonitor<IInstruction[]> drm = 
            new DataRequestMonitor<IInstruction[]>(fSession.getExecutor(), null) {
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
            @Override
			public void run() {
                fDisassembly.getInstructions(dmc, startAddress, endAddress, drm);
            }
        });
    }

    /* ------------------------------------------------------------------------
     * getInstruction
     * ------------------------------------------------------------------------
     * Issues a disassembly request. The result is stored in fWait.
     * ------------------------------------------------------------------------
     * Typical usage:
     *  getInstruction(dmc, start, end);
     *  fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
     *  assertTrue(fWait.getMessage(), fWait.isOK());
     * ------------------------------------------------------------------------
     * @param dmc       the data model context
     * @param fucntion  the function
     * @param linenum   the line
     * @param count     the instruction count
     * @throws InterruptedException
     * ------------------------------------------------------------------------
     */
    private void getInstruction(final IDisassemblyDMContext dmc,
            final String function, final int linenum, final int count)
    throws InterruptedException
    {
        // Set the Data Request Monitor
        final DataRequestMonitor<IInstruction[]> drm = 
            new DataRequestMonitor<IInstruction[]>(fSession.getExecutor(), null) {
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
            @Override
			public void run() {
                fDisassembly.getInstructions(dmc, function, linenum, count, drm);
            }
        });
    }

    /* ------------------------------------------------------------------------
     * getMixedInstruction
     * ------------------------------------------------------------------------
     * Issues a disassembly request. The result is stored in fWait.
     * ------------------------------------------------------------------------
     * Typical usage:
     *  getInstruction(dmc, start, end);
     *  fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
     *  assertTrue(fWait.getMessage(), fWait.isOK());
     * ------------------------------------------------------------------------
     * @param dmc       the data model context
     * @param start     the start address (null == $pc)
     * @param end       the end address
     * @throws InterruptedException
     * ------------------------------------------------------------------------
     */
    private void getMixedInstruction(final IDisassemblyDMContext dmc,
            final BigInteger startAddress, final BigInteger endAddress)
    throws InterruptedException
    {
        // Set the Data Request Monitor
        final DataRequestMonitor<IMixedInstruction[]> drm = 
            new DataRequestMonitor<IMixedInstruction[]>(fSession.getExecutor(), null) {
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
            @Override
			public void run() {
                fDisassembly.getMixedInstructions(dmc, startAddress, endAddress, drm);
            }
        });
    }


    /* ------------------------------------------------------------------------
     * getMixedInstruction
     * ------------------------------------------------------------------------
     * Issues a disassembly request. The result is stored in fWait.
     * ------------------------------------------------------------------------
     * Typical usage:
     *  getInstruction(dmc, start, end);
     *  fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
     *  assertTrue(fWait.getMessage(), fWait.isOK());
     * ------------------------------------------------------------------------
     * @param dmc       the data model context
     * @param start     the start address (null == $pc)
     * @param end       the end address
     * @throws InterruptedException
     * ------------------------------------------------------------------------
     */
    private void getMixedInstruction(final IDisassemblyDMContext dmc,
            final String function, final int linenum, final int count)
    throws InterruptedException
    {
        // Set the Data Request Monitor
        final DataRequestMonitor<IMixedInstruction[]> drm = 
            new DataRequestMonitor<IMixedInstruction[]>(fSession.getExecutor(), null) {
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
            @Override
			public void run() {
                fDisassembly.getMixedInstructions(dmc, function, linenum, count, drm);
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
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readWithNullContext() throws Throwable {

        // Setup call parameters
        BigInteger startAddress = null;
        BigInteger endAddress = null;
        
        // Perform the test
        fWait.waitReset();
        getInstruction(null, startAddress, endAddress);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        String expected = "Unknown context type";
        assertFalse(fWait.getMessage(), fWait.isOK());
        assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
                fWait.getMessage().contains(expected));
    }

    // ------------------------------------------------------------------------
    // readWithInvalidAddress
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readWithInvalidAddress() throws Throwable {

        // Setup call parameters
        BigInteger startAddress = BigInteger.ZERO;
        BigInteger endAddress = null;
        
        // Perform the test
        fWait.waitReset();
        getInstruction(fDisassemblyDmc, startAddress, endAddress);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        String expected = "Cannot access memory at address";
        assertFalse(fWait.getMessage(), fWait.isOK());
        assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
                fWait.getMessage().contains(expected));
    }

    // ------------------------------------------------------------------------
    // readWithNullAddress
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readWithNullAddress() throws Throwable {

        // Setup call parameters
        BigInteger startAddress = null;
        BigInteger endAddress = null;
        
        // Perform the test
        fWait.waitReset();
        getInstruction(fDisassemblyDmc, startAddress, endAddress);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        assertTrue(fWait.getMessage(), fWait.isOK());
        IInstruction[] result = (IInstruction[]) fWait.getReturnInfo();
        assertTrue("No instruction retrieved", result.length != 0);
    }

    // ------------------------------------------------------------------------
    // readWithValidAddress
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readWithValidAddress() throws Throwable {

        // Setup call parameters
        Addr64 main = (Addr64) evaluateExpression("&main");
        BigInteger startAddress = main.getValue();
        BigInteger endAddress = null;
        
        // Perform the test
        fWait.waitReset();
        getInstruction(fDisassemblyDmc, startAddress, endAddress);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        assertTrue(fWait.getMessage(), fWait.isOK());
        IInstruction[] result = (IInstruction[]) fWait.getReturnInfo();
        assertTrue("No instruction retrieved", result.length != 0);
    }

    // ------------------------------------------------------------------------
    // readWithInvalidFilename
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readWithValidFunction() throws Throwable {

        // Setup call parameters
        String filename = INVALID_FILE_NAME;
        int linenum = 1;
        int count = -1;
        
        // Perform the test
        fWait.waitReset();
        getInstruction(fDisassemblyDmc, filename, linenum, count);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        String expected = "Invalid filename";
        assertFalse(fWait.getMessage(), fWait.isOK());
        assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
                fWait.getMessage().contains(expected));
    }

    // ------------------------------------------------------------------------
    // readWithInvalidLineNumber
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readWithInvalidLineNumber() throws Throwable {

        // Setup call parameters
        String filename = FILE_NAME;
        int linenum = -1;
        int count = -1;

        // Perform the test
        fWait.waitReset();
        getInstruction(fDisassemblyDmc, filename, linenum, count);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        String expected = "Invalid line number";
        assertFalse(fWait.getMessage(), fWait.isOK());
        assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
                fWait.getMessage().contains(expected));
    }

    // ------------------------------------------------------------------------
    // readWithValidFilename
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readWithValidFilename() throws Throwable {

        // Setup call parameters
        String filename = FILE_NAME;
        int linenum = LINE_NUMBER;
        int count = -1;

        // Perform the test
        fWait.waitReset();
        getInstruction(fDisassemblyDmc, filename, linenum, count);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        assertTrue(fWait.getMessage(), fWait.isOK());
        IInstruction[] result = (IInstruction[]) fWait.getReturnInfo();
        assertTrue("No instruction retrieved", result.length != 0);
    }

    // ------------------------------------------------------------------------
    // readWithLineCount
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readWithLineCount() throws Throwable {

        // Setup call parameters
        String filename = FILE_NAME;
        int linenum = LINE_NUMBER;
        int count = 5;
      
        // Perform the test
        fWait.waitReset();
        getInstruction(fDisassemblyDmc, filename, linenum, count);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        assertTrue(fWait.getMessage(), fWait.isOK());
        IInstruction[] result = (IInstruction[]) fWait.getReturnInfo();
        assertTrue("Wrong number of instructions retrieved, expected " + count + ", got " + result.length,
                result.length == count);
    }

    // ------------------------------------------------------------------------
    // readMixedWithValidAddress
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readMixedWithValidAddress() throws Throwable {

        // Setup call parameters
        Addr64 main = (Addr64) evaluateExpression("&main");
        BigInteger startAddress = main.getValue();
        BigInteger endAddress = null;
        
        // Perform the test
        fWait.waitReset();
        getMixedInstruction(fDisassemblyDmc, startAddress, endAddress);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        assertTrue(fWait.getMessage(), fWait.isOK());
        IMixedInstruction[] result = (IMixedInstruction[]) fWait.getReturnInfo();
        assertTrue("No instruction retrieved", result.length != 0);
    }

    // ------------------------------------------------------------------------
    // readMixedWithLineCount
    // ------------------------------------------------------------------------
    @Test(timeout=20000)
    public void readMixedWithLineCount() throws Throwable {

        // Setup call parameters
        String filename = FILE_NAME;
        int linenum = LINE_NUMBER;
        int count = 5;
      
        // Perform the test
        fWait.waitReset();
        getMixedInstruction(fDisassemblyDmc, filename, linenum, count);
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        // Verify the result
        assertTrue(fWait.getMessage(), fWait.isOK());
        IMixedInstruction[] result = (IMixedInstruction[]) fWait.getReturnInfo();
        int total = 0;
        for (IMixedInstruction mixed : result) {
            IInstruction[] inst = mixed.getInstructions();
            total += inst.length;
        }
        assertTrue("Wrong number of instructions retrieved, expected " + count + ", got " + result.length,
                total == count);
    }

}
