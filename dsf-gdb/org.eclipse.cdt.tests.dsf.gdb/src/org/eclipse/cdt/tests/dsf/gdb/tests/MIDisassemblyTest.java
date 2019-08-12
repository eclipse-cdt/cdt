/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial Implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.MIDisassembly;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.utils.Addr64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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

@RunWith(Parameterized.class)
public class MIDisassemblyTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "MemoryTestApp.exe";
	private static final String SOURCE_NAME = "MemoryTestApp.cc";
	private static final String INVALID_SOURCE_NAME = "invalid_filename";

	protected static final String[] LINE_TAGS = { "LINE_NUMBER", };

	protected int LINE_NUMBER;

	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	private IDisassemblyDMContext fDisassemblyDmc;
	private MIDisassembly fDisassembly;
	private IExpressions fExpressionService;

	@Rule
	final public ExpectedException expectedException = ExpectedException.none();

	// ========================================================================
	// Housekeeping stuff
	// ========================================================================

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		fSession = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			// Get a reference to the memory service
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
			assert (fServicesTracker != null);

			fDisassembly = fServicesTracker.getService(MIDisassembly.class);
			assert (fDisassembly != null);

			fExpressionService = fServicesTracker.getService(IExpressions.class);
			assert (fExpressionService != null);
		};
		fSession.getExecutor().submit(runnable).get();

		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		fDisassemblyDmc = DMContexts.getAncestorOfType(containerDmc, IDisassemblyDMContext.class);
		assert (fDisassemblyDmc != null);

		resolveLineTagLocations(SOURCE_NAME, LINE_TAGS);
		LINE_NUMBER = getLineForTag("LINE_NUMBER");
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

		fExpressionService = null;
		fDisassembly = null;
		if (fServicesTracker != null) {
			fServicesTracker.dispose();
			fServicesTracker = null;
		}
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
	private IAddress evaluateExpression(String expression) throws Throwable {
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext ctx = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		IExpressionDMContext expressionDMC = SyncUtil.createExpression(ctx, expression);
		return new Addr64(SyncUtil.getExpressionValue(expressionDMC, IFormattedValues.HEX_FORMAT));
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
	private IInstruction[] getInstruction(final IDisassemblyDMContext dmc, final BigInteger startAddress,
			final BigInteger endAddress) throws InterruptedException, ExecutionException {
		Query<IInstruction[]> query = new Query<IInstruction[]>() {

			@Override
			protected void execute(DataRequestMonitor<IInstruction[]> rm) {
				fDisassembly.getInstructions(dmc, startAddress, endAddress, rm);
			}
		};

		fDisassembly.getExecutor().submit(query);

		return query.get();
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
	private IInstruction[] getInstruction(final IDisassemblyDMContext dmc, final String function, final int linenum,
			final int count) throws InterruptedException, ExecutionException {
		Query<IInstruction[]> query = new Query<IInstruction[]>() {

			@Override
			protected void execute(DataRequestMonitor<IInstruction[]> rm) {
				fDisassembly.getInstructions(dmc, function, linenum, count, rm);
			}
		};

		fDisassembly.getExecutor().submit(query);

		return query.get();
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
	private IMixedInstruction[] getMixedInstruction(final IDisassemblyDMContext dmc, final BigInteger startAddress,
			final BigInteger endAddress) throws InterruptedException, ExecutionException {
		Query<IMixedInstruction[]> query = new Query<IMixedInstruction[]>() {

			@Override
			protected void execute(DataRequestMonitor<IMixedInstruction[]> rm) {
				fDisassembly.getMixedInstructions(dmc, startAddress, endAddress, rm);
			}
		};

		fDisassembly.getExecutor().submit(query);

		return query.get();
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
	private IMixedInstruction[] getMixedInstruction(final IDisassemblyDMContext dmc, final String function,
			final int linenum, final int count) throws InterruptedException, ExecutionException {
		Query<IMixedInstruction[]> query = new Query<IMixedInstruction[]>() {

			@Override
			protected void execute(DataRequestMonitor<IMixedInstruction[]> rm) {
				fDisassembly.getMixedInstructions(dmc, function, linenum, count, rm);
			}
		};

		fDisassembly.getExecutor().submit(query);

		return query.get();
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
	@Test(timeout = 20000)
	public void readWithNullContext() throws Throwable {

		// Setup call parameters
		BigInteger startAddress = null;
		BigInteger endAddress = null;

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Unknown context type");

		// Perform the test
		getInstruction(null, startAddress, endAddress);
	}

	// ------------------------------------------------------------------------
	// readWithInvalidAddress
	// ------------------------------------------------------------------------
	@Test(timeout = 20000)
	public void readWithInvalidAddress() throws Throwable {

		// Setup call parameters
		BigInteger startAddress = BigInteger.ZERO;
		BigInteger endAddress = null;

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Cannot access memory at address");

		// Perform the test
		getInstruction(fDisassemblyDmc, startAddress, endAddress);
	}

	// ------------------------------------------------------------------------
	// readWithNullAddress
	// ------------------------------------------------------------------------
	@Test(timeout = 20000)
	public void readWithNullAddress() throws Throwable {

		// Setup call parameters
		BigInteger startAddress = null;
		BigInteger endAddress = null;

		// Perform the test
		IInstruction[] result = getInstruction(fDisassemblyDmc, startAddress, endAddress);

		// Verify the result
		assertThat(result.length, is(not(0)));
	}

	// ------------------------------------------------------------------------
	// readWithValidAddress
	// ------------------------------------------------------------------------
	@Test(timeout = 20000)
	public void readWithValidAddress() throws Throwable {

		// Setup call parameters
		Addr64 main = (Addr64) evaluateExpression("&main");
		BigInteger startAddress = main.getValue();
		BigInteger endAddress = null;

		// Perform the test
		IInstruction[] result = getInstruction(fDisassemblyDmc, startAddress, endAddress);

		// Verify the result
		assertThat(result.length, is(not(0)));
	}

	// ------------------------------------------------------------------------
	// readWithInvalidFilename
	// ------------------------------------------------------------------------
	@Test(timeout = 20000)
	public void readWithInvalidFilename() throws Throwable {

		// Setup call parameters
		String filename = INVALID_SOURCE_NAME;
		int linenum = 1;
		int count = -1;

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Invalid filename");

		// Perform the test
		getInstruction(fDisassemblyDmc, filename, linenum, count);
	}

	// ------------------------------------------------------------------------
	// readWithInvalidLineNumber
	// ------------------------------------------------------------------------
	@Test(timeout = 20000)
	public void readWithInvalidLineNumber() throws Throwable {

		// Setup call parameters
		String filename = SOURCE_NAME;
		int linenum = -1;
		int count = -1;

		expectedException.expect(ExecutionException.class);
		expectedException.expectMessage("Invalid line number");

		// Perform the test
		getInstruction(fDisassemblyDmc, filename, linenum, count);
	}

	// ------------------------------------------------------------------------
	// readWithValidFilename
	// ------------------------------------------------------------------------
	@Test(timeout = 20000)
	public void readWithValidFilename() throws Throwable {

		// Setup call parameters
		String filename = SOURCE_NAME;
		int linenum = LINE_NUMBER;
		int count = -1;

		// Perform the test
		IInstruction[] result = getInstruction(fDisassemblyDmc, filename, linenum, count);

		// Verify the result
		assertThat(result.length, is(not(0)));
	}

	// ------------------------------------------------------------------------
	// readWithLineCount
	// ------------------------------------------------------------------------
	@Test(timeout = 20000)
	public void readWithLineCount() throws Throwable {

		// Setup call parameters
		String filename = SOURCE_NAME;
		int linenum = LINE_NUMBER;
		int count = 5;

		// Perform the test
		IInstruction[] result = getInstruction(fDisassemblyDmc, filename, linenum, count);

		// Verify the result
		assertThat(result.length, is(count));
	}

	// ------------------------------------------------------------------------
	// readMixedWithValidAddress
	// ------------------------------------------------------------------------
	@Test(timeout = 20000)
	public void readMixedWithValidAddress() throws Throwable {

		// Setup call parameters
		Addr64 main = (Addr64) evaluateExpression("&main");
		BigInteger startAddress = main.getValue();
		BigInteger endAddress = null;

		// Perform the test
		IMixedInstruction[] result = getMixedInstruction(fDisassemblyDmc, startAddress, endAddress);

		// Verify the result
		assertThat(result.length, is(not(0)));
	}

	// ------------------------------------------------------------------------
	// readMixedWithLineCount
	// ------------------------------------------------------------------------
	@Test(timeout = 20000)
	public void readMixedWithLineCount() throws Throwable {

		// Setup call parameters
		String filename = SOURCE_NAME;
		int linenum = LINE_NUMBER;
		int count = 5;

		// Perform the test
		IMixedInstruction[] result = getMixedInstruction(fDisassemblyDmc, filename, linenum, count);

		// Verify the result
		int total = 0;
		for (IMixedInstruction mixed : result) {
			IInstruction[] inst = mixed.getInstructions();
			total += inst.length;
		}
		assertThat(total, is(count));
	}
}
