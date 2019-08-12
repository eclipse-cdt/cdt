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
 *     Jonah Graham (Kichwa Coders) - Split arguments tests out of LaunchConfigurationAndRestartTest
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CommandLineArgsTest extends BaseParametrizedTestCase {
	protected static final String EXEC_NAME = "LaunchConfigurationAndRestartTestApp.exe";

	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	private IExpressions fExpService;

	@Override
	public void doBeforeTest() throws Exception {
		assumeLocalSession();
		removeTeminatedLaunchesBeforeTest();
		setLaunchAttributes();
		// Can't run the launch right away because each test needs to first set
		// ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		// Set the binary
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	// This method cannot be tagged as @Before, because the launch is not
	// running yet. We have to call this manually after all the proper
	// parameters have been set for the launch
	@Override
	protected void doLaunch() throws Exception {
		// perform the launch
		super.doLaunch();

		fSession = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

			fExpService = fServicesTracker.getService(IExpressions.class);
		};
		fSession.getExecutor().submit(runnable).get();
	}

	/**
	 * Convert a string of form 0x123456 "ab\"cd" to ab"cd
	 */
	protected String convertDetails(String details) {

		// check parser assumptions on input format
		assertThat(details, startsWith("0x"));
		assertThat(details, containsString(" \""));
		assertThat(details, endsWith("\""));

		int firstSpace = details.indexOf(' ');
		boolean lastWasEscape = false;
		StringBuilder sb = new StringBuilder();
		for (int i = firstSpace + 2; i < details.length() - 1; i++) {
			char c = details.charAt(i);
			if (lastWasEscape) {
				switch (c) {
				case 't':
					sb.append('\t');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'n':
					sb.append('\n');
					break;
				default:
					sb.append(c);
					break;
				}
				lastWasEscape = false;
			} else {
				if (c == '\\') {
					lastWasEscape = true;
				} else {
					sb.append(c);
				}
			}
		}

		assertFalse("unexpected trailing backslash (\\)", lastWasEscape);
		return sb.toString();
	}

	/**
	 * Check that the target program received the arguments as expected
	 *
	 * @param expected
	 *            arguments to check, e.g. check expected[0].equals(argv[1])
	 */
	protected void checkArguments(String... expected) throws Throwable {

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

		fExpService.getExecutor().execute(query);
		FormattedValueDMData value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);

		assertTrue("Expected " + (1 + expected.length) + " but got " + value.getFormattedValue(),
				value.getFormattedValue().trim().equals(Integer.toString(1 + expected.length)));

		// check all argvs are correct
		for (int i = 0; i < expected.length; i++) {
			final IExpressionDMContext argvDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(),
					"argv[" + (i + 1) + "]");
			Query<FormattedValueDMData> query2 = new Query<FormattedValueDMData>() {
				@Override
				protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
					fExpService.getFormattedExpressionValue(
							fExpService.getFormattedValueContext(argvDmc, MIExpressions.DETAILS_FORMAT), rm);
				}
			};

			fExpService.getExecutor().execute(query2);
			FormattedValueDMData value2 = query2.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			String details = value2.getFormattedValue();
			String actual = convertDetails(details);
			assertEquals(expected[i], actual);
		}
	}

	/**
	 * Run the program, setting ATTR_PROGRAM_ARGUMENTS to the attrProgramArgs
	 * and ensuring debugged program receives args for argv (excluding argv[0]
	 * which isn't checked)
	 */
	protected void doTest(String attrProgramArgs, String... args) throws Throwable {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, attrProgramArgs);
		doLaunch();
		checkArguments(args);
	}

	/**
	 * This test will tell the launch to set some arguments for the program. We
	 * will then check that the program has the same arguments.
	 */
	@Test
	public void testSettingArguments() throws Throwable {
		doTest("1 2 3\n4 5 6", "1", "2", "3", "4", "5", "6");
	}

	/**
	 * This test will tell the launch to set some arguments for the program. We
	 * will then check that the program has the same arguments. See bug 381804
	 */
	@Test
	public void testSettingArgumentsWithSymbols() throws Throwable {
		// Set a argument with double quotes and spaces, which should be
		// considered a single argument
		doTest("--c=\"c < s: 'a' t: 'b'>\"", "--c=c < s: 'a' t: 'b'>");
	}

	/**
	 * This test will tell the launch to set some more arguments for the
	 * program. We will then check that the program has the same arguments. See
	 * bug 474648
	 */
	@Test
	@Ignore
	public void testSettingArgumentsWithSpecialSymbols() throws Throwable {
		// Test that arguments are parsed correctly:
		// The string provided by the user is split into arguments on spaces
		// except for those inside quotation marks, double or single.
		// Any character within quotation marks or after the backslash character
		// is treated literally, whilst these special characters have to be
		// escaped explicitly to be recorded.
		// All other characters including semicolons, backticks, pipes, dollars
		// and newlines
		// must be treated literally.
		doTest("--abc=\"x;y;z\nsecondline: \"`date`$PS1\"`date | wc`\"",
				"--abc=x;y;z\nsecondline: `date`$PS1`date | wc`");
	}

	/**
	 * Check combinations of quote characters
	 */
	@Test
	public void testSettingArgumentsWithQuotes() throws Throwable {
		doTest("\"'\" '\"'", "'", "\"");
	}

	/**
	 * Check tab characters
	 */
	@Test
	public void testSettingArgumentsWithTabs() throws Throwable {
		doTest("\"\t\"\t'\t'", "\t", "\t");
	}
}
