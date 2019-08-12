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
 *     Marc Khouzam (Ericsson) - Added test to handle different cases of core
 *                               file specification (Bug 362039)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PostMortemCoreTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "ExpressionTestApp.exe";
	private static final String INVALID_CORE_NAME = "MultiThread.exe";
	private static final String CORE_NAME = "core";

	private DsfSession fSession;

	private DsfServicesTracker fServicesTracker;

	private IExpressions fExpService;

	private IMemoryDMContext fMemoryDmc;

	@Override
	public void doBeforeTest() throws Exception {
		assumeLocalSession();
		removeTeminatedLaunchesBeforeTest();
		setLaunchAttributes();
		// Can't run the launch right away because each test needs to first set some
		// parameters.  The individual tests will be responsible for starting the launch.
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		// Set a working directory for GDB that is different than eclipse's directory.
		// This allows us to make sure we properly handle finding the core file,
		// especially in the case of a relative path
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "${workspace_loc}");
		// Because we just set a different working directory, we must use an absolute path for the program
		String absoluteProgram = new Path(EXEC_PATH + EXEC_NAME).toFile().getAbsolutePath();
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, absoluteProgram);

		// Set post-mortem launch
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
		// Set post-mortem type to core file
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_POST_MORTEM_TYPE,
				IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_CORE_FILE);
		// Set default core file path
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, EXEC_PATH + CORE_NAME);
	}

	// This method cannot be tagged as @Before, because the launch is not
	// running yet.  We have to call this manually after all the proper
	// parameters have been set for the launch
	@Override
	protected void doLaunch() throws Exception {
		// perform the launch
		super.doLaunch();

		fSession = getGDBLaunch().getSession();

		fMemoryDmc = (IMemoryDMContext) SyncUtil.getContainerContext();
		assert (fMemoryDmc != null);

		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
			fExpService = fServicesTracker.getService(IExpressions.class);
		};
		fSession.getExecutor().submit(runnable).get();
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		if (fSession != null) {
			fSession.getExecutor().submit(() -> fSession.removeServiceEventListener(PostMortemCoreTest.this)).get();
		}

		fExpService = null;
		if (fServicesTracker != null)
			fServicesTracker.dispose();
	}

	/**
	 * Test that we support specifying a core file with an absolute path.
	 */
	@Test
	public void testAbsoluteCoreFilePath() throws Throwable {
		File file = new File(EXEC_PATH + CORE_NAME);
		assertTrue("Cannot find test file; " + file.toString(), file.exists());

		String absoluteCoreFile = file.getAbsolutePath();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, absoluteCoreFile);

		doLaunch();

		// If the launch passed, we are ok, nothing more to check
	}

	/**
	 * Test that we support specifying a core file with a relative path.
	 */
	@Test
	public void testRelativeCoreFilePath() throws Throwable {
		File file = new File(EXEC_PATH + CORE_NAME);
		assertTrue("Cannot find test file; " + file.toString(), file.exists());

		String relativeCoreFile = file.toString();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, relativeCoreFile);

		doLaunch();

		// If the launch passed, we are ok, nothing more to check
	}

	/**
	 * Test that we handle specifying an invalid core file with an absolute path.
	 */
	@Test
	public void testAbsoluteCoreFilePathInvalid() throws Throwable {
		File file = new File(EXEC_PATH + INVALID_CORE_NAME);
		assertTrue("Cannot find test file: " + file.toString(), file.exists());

		String absoluteCoreFile = file.getAbsolutePath();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, absoluteCoreFile);

		try {
			doLaunch();
		} catch (DebugException e) {
			// Success of the test
			return;
		}

		fail("Launch seems to have succeeded even though the specified core file is invalid");
	}

	/**
	 * Test that we handle specifying an invalid core file with a relative path.
	 */
	@Test
	public void testRelativeCoreFilePathInvalid() throws Throwable {
		File file = new File(EXEC_PATH + INVALID_CORE_NAME);
		assertTrue("Cannot find test file: " + file.toString(), file.exists());

		String relativeCoreFile = file.toString();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, relativeCoreFile);

		try {
			doLaunch();
		} catch (CoreException e) {
			// Success of the test
			return;
		}

		fail("Launch seems to have succeeded even though the specified core file is invalid");
	}

	/**
	 * Test that we handle specifying a missing core file with an absolute path.
	 */
	@Test
	public void testAbsoluteCoreFilePathMissing() throws Throwable {
		File file = new File(EXEC_PATH + "MissingFile");
		assertTrue("Should not have found test file: " + file.toString(), !file.exists());

		String absoluteCoreFile = file.getAbsolutePath();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, absoluteCoreFile);

		try {
			doLaunch();
		} catch (CoreException e) {
			// Success of the test
			return;
		}

		fail("Launch seems to have succeeded even though the specified core file does not exist");
	}

	/**
	 * Test that we handle specifying a missing core file with a relative path.
	 */
	@Test
	public void testRelativeCoreFilePathMissing() throws Throwable {
		File file = new File(EXEC_PATH + "MissingFile");
		assertTrue("Should not have found test file: " + file.toString(), !file.exists());

		String relativeCoreFile = file.toString();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, relativeCoreFile);

		try {
			doLaunch();
		} catch (CoreException e) {
			// Success of the test
			return;
		}

		fail("Launch seems to have succeeded even though the specified core file does not exist");
	}

	/**
	 * Test that we support a valid core file path using variables.
	 */
	@Test
	public void testCoreFilePathWithVariable() throws Throwable {
		// I couldn't find an easy way to test with a variable.
		// Here what we do here:
		//   create the variable for the workspace location and expand it
		//   The resulting path has a common part with the absolute core
		//   file path.  Find that common part and count how many .. we
		//   have to insert to use the common part of the variablePath
		//   inside the absolute path.
		//   Then, send the variable itself, with all the .., and the
		//   absolute path, and make sure the variable gets translated
		//   properly.

		// Absolute path of the core file
		File file = new File(EXEC_PATH + CORE_NAME);
		String absoluteCoreFile = file.getAbsolutePath();

		// Variable for workspace location
		String variable = "${workspace_loc}";
		// Expand workspace location
		String workspaceLocation = VariablesPlugin.getDefault().getStringVariableManager()
				.performStringSubstitution(variable, false);

		// Path to the core file
		IPath corePath = new Path(absoluteCoreFile);
		// Prepare to find the common path between the core file and the workspace
		IPath commonPath = new Path(workspaceLocation);

		StringBuilder backwards = new StringBuilder("/");
		// While the commonPath is not the prefix of the core file path
		// remove one more segment of the potential commonPath
		while (!commonPath.isPrefixOf(corePath)) {
			commonPath = commonPath.removeLastSegments(1);
			backwards.append("../");
		}

		// Remove the commonPath from the workspace path
		IPath trailingPathCoreFile = corePath.removeFirstSegments(commonPath.segmentCount());

		// Build the path using the variable unexpanded, the number of ..
		// to remove all non-common segments, the trailing part of the
		// path of the core file
		String coreFile = variable + backwards.toString() + trailingPathCoreFile;

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, coreFile);

		doLaunch();
	}

	/**
	 * Test that we can correctly evaluate integer expressions.
	 */
	@Test
	public void testLiteralIntegerExpressions() throws Throwable {
		doLaunch();

		// Create a map of expressions and their expected values.
		Map<String, String[]> tests = new HashMap<>();

		tests.put("0 + 0 - 0", new String[] { "0x0", "0", "0", "0", "0", "0" });
		tests.put("3 + 4", new String[] { "0x7", "07", "111", "7", "7", "7" });
		tests.put("3 + 4 * 5", new String[] { "0x17", "027", "10111", "23", "23", "23" });
		tests.put("5 * 3 + 4", new String[] { "0x13", "023", "10011", "19", "19", "19" });
		tests.put("5 * (3 + 4)", new String[] { "0x23", "043", "100011", "35", "35", "35" });
		tests.put("10 - 15",
				new String[] { "0xFFFFFFFB", "037777777773", "11111111111111111111111111111011", "-5", "-5", "-5" });
		tests.put("10 + -15",
				new String[] { "0xFFFFFFFB", "037777777773", "11111111111111111111111111111011", "-5", "-5", "-5" });

		executeExpressionSubTests(tests, SyncUtil.getStackFrame(SyncUtil.getExecutionContext(0), 0));
	}

	/**
	 * Test that we can correctly evaluate floating-point expressions.
	 */
	@Test
	public void testLiteralFloatingPointExpressions() throws Throwable {
		doLaunch();

		// Create a map of expressions and their expected values.
		Map<String, String[]> tests = new HashMap<>();

		tests.put("3.14159 + 1.1111", new String[] { "0x4", "04", "100", "4", "4.2526", "4.2526" });
		tests.put("100.0 / 3.0", new String[] { "0x21", "041", "100001", "33", "33.3333", "33.3333" });
		tests.put("-100.0 / 3.0", new String[] { "0xffffffffffffffdf", "01777777777777777777737",
				"1111111111111111111111111111111111111111111111111111111111011111", "-33", "-33.3333", "-33.3333" });
		tests.put("-100.0 / -3.0", new String[] { "0x21", "041", "100001", "33", "33.3333", "33.3333" });
		executeExpressionSubTests(tests, false, SyncUtil.getStackFrame(SyncUtil.getExecutionContext(0), 0));

		tests.clear();
		tests.put("100.0 / 0.5", new String[] { "0xc8", "0310", "11001000", "200", "200", "200" });
		executeExpressionSubTests(tests, true, SyncUtil.getStackFrame(SyncUtil.getExecutionContext(0), 0));

	}

	/**
	 * Test that we can correctly evaluate C expressions involving local
	 * variables.
	 */
	@Test
	@Ignore
	public void testLocalVariables() throws Throwable {
		doLaunch();

		// Create a map of expressions to expected values.
		Map<String, String[]> tests1 = new HashMap<>();

		tests1.put("lIntVar", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345", "12345" });
		tests1.put("lDoubleVar", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345.123449999999",
				"12345.123449999999" });
		tests1.put("lCharVar", new String[] { "0x6d", "0155", "1101101", "109", "109 'm'", "109 'm'" });
		tests1.put("lBoolVar", new String[] { "0x0", "0", "0", "0", "false", "false" });

		tests1.put("lIntArray[1]", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345", "12345" });
		tests1.put("lDoubleArray[1]", new String[] { "0x3039", "030071", "11000000111001", "12345",
				"12345.123449999999", "12345.123449999999" });
		tests1.put("lCharArray[1]", new String[] { "0x6d", "0155", "1101101", "109", "109 'm'", "109 'm'" });
		tests1.put("lBoolArray[1]", new String[] { "0x0", "0", "0", "0", "false", "false" });

		tests1.put("*lIntPtr", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345", "12345" });
		tests1.put("*lDoublePtr", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345.123449999999",
				"12345.123449999999" });
		tests1.put("*lCharPtr", new String[] { "0x6d", "0155", "1101101", "109", "109 'm'", "109 'm'" });
		tests1.put("*lBoolPtr", new String[] { "0x0", "0", "0", "0", "false", "false" });

		tests1.put("lIntPtr2", new String[] { "0x1", "01", "1", "1", "0x1", "0x1" });
		tests1.put("lDoublePtr2", new String[] { "0x2345", "021505", "10001101000101", "9029", "0x2345", "0x2345" });
		// GDB says a char* is out of bounds, but not the other pointers???
		// tests1.put("CharPtr2", new String[] { "0x1234", "011064",
		// "1001000110100", "4660", "0x1234" });
		tests1.put("lBoolPtr2", new String[] { "0x123ABCDE", "02216536336", "10010001110101011110011011110",
				"305839326", "0x123ABCDE", "0x123ABCDE" });

		executeExpressionSubTests(tests1, SyncUtil.getStackFrame(SyncUtil.getExecutionContext(0), 0));
	}

	@Test
	@Ignore
	public void readMemoryArray() throws Throwable {
		doLaunch();

		IAddress address = evaluateExpression(SyncUtil.getStackFrame(SyncUtil.getExecutionContext(0), 0), "&lBoolPtr2");

		final int LENGTH = 4;

		// Get the memory block
		MemoryByte[] buffer = SyncUtil.readMemory(fMemoryDmc, address, 0, 1, LENGTH);

		assertEquals(LENGTH, buffer.length);

		assertEquals(buffer[0].getValue(), 0xffffffde);
		assertEquals(buffer[1].getValue(), 0xffffffbc);
		assertEquals(buffer[2].getValue(), 0x3a);
		assertEquals(buffer[3].getValue(), 0x12);
	}

	/**
	 * Test that we support setting only the initial path with an absolute path.
	 */
	@Ignore("Can't test without the UI")
	@Test
	public void testAbsoluteInitialPath() throws Throwable {
	}

	/**
	 * Test that we support setting an invalid initial path with an absolute path.
	 */
	@Ignore("Can't test without the UI")
	@Test
	public void testAbsoluteInitialPathInvalid() throws Throwable {
	}

	/**
	 * Test that we support setting only the initial path with a relative path.
	 */
	@Ignore("Can't test without the UI")
	@Test
	public void testRelativeInitialPath() throws Throwable {
	}

	/**
	 * Test that we support an empty path
	 */
	@Ignore("Can't test without the UI")
	@Test
	public void testEmptyInitialPath() throws Throwable {
	}

	/**
	 * Test that we support a valid initial path using variables.
	 */
	@Ignore("Can't test without the UI")
	@Test
	public void testInitialPathWithVariable() throws Throwable {
	}

	/**
	 * Test that we support setting an invalid initial path with a relative path.
	 */
	@Ignore("Can't test without the UI")
	@Test
	public void testRelativeInitialPathInvalid() throws Throwable {
	}

	private IAddress evaluateExpression(IDMContext ctx, String expression) throws Throwable {
		// Create the expression and format contexts
		final IExpressionDMContext expressionDMC = SyncUtil.createExpression(ctx, expression);
		final FormattedValueDMContext formattedValueDMC = SyncUtil.getFormattedValue(fExpService, expressionDMC,
				IFormattedValues.HEX_FORMAT);

		Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(formattedValueDMC, rm);
			}
		};

		fSession.getExecutor().execute(query);
		FormattedValueDMData value = null;
		{
			value = query.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
		}

		return new Addr64(value.getFormattedValue());
	}

	/**
	 * Executes a group of sub-tests.
	 *
	 * @param tests
	 *            A Map in which the key is an expression to evaluate and the
	 *            value is an array of expected values, one for each of the
	 *            formats supported by the Expressions service (hex, octal,
	 *            binary, decimal, natural, details).
	 * @param exact
	 *            Indicates whether the natural and details format should
	 *            require an exact match to the expected value, or whether the
	 *            comparison should match only up to the number of characters
	 *            provided in the expected value. Where this is used is in
	 *            expressions that involve floating point calculation. Such
	 *            calculations are not exact (even when you'd think they should
	 *            be) and these tests cannot predict what exactly the result
	 *            will be. When this param is false, then we consider it a match
	 *            if, e.g., the gdb expression resolves to "1.23456789", but the
	 *            caller only supplied "1.2345".
	 */
	private void executeExpressionSubTests(final Map<String, String[]> tests, final boolean exact, IDMContext dmc)
			throws Throwable {

		// Now evaluate each of the above expressions and compare the actual
		// value against
		// the expected value.
		for (final String expressionToEvaluate : tests.keySet()) {

			// Get an IExpressionDMContext object representing the expression to
			// be evaluated.
			final IExpressionDMContext exprDMC = SyncUtil.createExpression(dmc, expressionToEvaluate);

			final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

			// Get the list of available format IDs for this expression and for
			// each one,
			// get the value of the expression
			fExpService.getExecutor().submit(() -> fExpService.getAvailableFormats(exprDMC,
					new DataRequestMonitor<String[]>(fExpService.getExecutor(), null) {
						@Override
						protected void handleCompleted() {
							if (!isSuccess()) {
								wait.waitFinished(getStatus());
							} else {
								final String[] formatIds = getData();

								// Now run the current sub-test using each of
								// the formats available for the type of
								// the expression in the sub-test.

								for (final String formatId : formatIds) {
									// Get a FormattedValueCMContext object for
									// the expression-formatID pair.
									final FormattedValueDMContext valueDmc = fExpService
											.getFormattedValueContext(exprDMC, formatId);

									// Increment the number of completed
									// requests to wait for, since we will send
									// multiple concurrent requests
									wait.increment();

									// Evaluate the expression represented by
									// the FormattedValueDMContext object
									// This actually evaluates the expression.
									fExpService.getFormattedExpressionValue(valueDmc,
											new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(),
													null) {
												@Override
												protected void handleCompleted() {
													if (!isSuccess()) {
														wait.waitFinished(getStatus());
													} else {

														// Get the
														// FormattedValueDMData
														// object from the waiter.
														FormattedValueDMData exprValueDMData = getData();

														final String[] expectedValues = tests.get(expressionToEvaluate);

														// Check the value of the expression for correctness.
														String actualValue = exprValueDMData.getFormattedValue();
														String expectedValue;

														if (formatId.equals(IFormattedValues.HEX_FORMAT))
															expectedValue = expectedValues[0];
														else if (formatId.equals(IFormattedValues.OCTAL_FORMAT))
															expectedValue = expectedValues[1];
														else if (formatId.equals(IFormattedValues.BINARY_FORMAT))
															expectedValue = expectedValues[2];
														else if (formatId.equals(IFormattedValues.DECIMAL_FORMAT))
															expectedValue = expectedValues[3];
														else if (formatId.equals(IFormattedValues.NATURAL_FORMAT))
															expectedValue = expectedValues[4];
														else if (formatId.equals(MIExpressions.DETAILS_FORMAT))
															expectedValue = expectedValues[5];
														else
															expectedValue = "[Unrecognized format ID: " + formatId
																	+ "]";

														if ((exact == false)
																&& (formatId.equals(IFormattedValues.NATURAL_FORMAT)
																		|| formatId
																				.equals(MIExpressions.DETAILS_FORMAT))
																&& (expectedValue.length() < actualValue.length())) {
															actualValue = actualValue.substring(0,
																	expectedValue.length());
														}

														if (actualValue.equalsIgnoreCase(expectedValue)) {
															wait.waitFinished();
														} else {
															String errorMsg = "Failed to correctly evalutate '"
																	+ expressionToEvaluate + "': expected '"
																	+ expectedValue + "', got '" + actualValue + "'";
															wait.waitFinished(new Status(IStatus.ERROR,
																	TestsPlugin.PLUGIN_ID, errorMsg, null));
														}
													}
												}
											});
								}
							}
						}
					}));
			wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
			assertTrue(wait.getMessage(), wait.isOK());
		}
	}

	private void executeExpressionSubTests(final Map<String, String[]> tests, IDMContext dmc) throws Throwable {
		executeExpressionSubTests(tests, true, dmc);
	}
}
