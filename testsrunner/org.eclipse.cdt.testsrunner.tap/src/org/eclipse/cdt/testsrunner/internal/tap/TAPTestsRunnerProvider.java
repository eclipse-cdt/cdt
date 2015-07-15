/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *     Colin Leitner - adapted to TAP support
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.tap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;

/**
 * The Tests Runner provider plug-in to run tests with the Test Anything
 * Protocol.
 * 
 * <p>
 * Parses the standard output of an application for TAP conforming output.
 *
 * <p>
 * The YAML output isn't parsed, but logged like any unknown output. As an
 * unofficial extension, any lines with gcc compatible diagnostic output of the
 * kind
 * 
 * <pre>
 * &lt;filename&gt;: (error|warning|info): ...
 * &lt;filename&gt;:&lt;line&gt;: (error|warning|info): ...</pre>
 * 
 * <p>
 * will be reported with the correct level and location. 
 *
 * <p>
 * As with the Boost test runner, the <tt>stdout</tt> buffering might delay
 * test output if not disabled by
 * 
 * <pre>
 * setvbuf(stdout, NULL, _IONBF, 0);</pre>
 */
public class TAPTestsRunnerProvider implements ITestsRunnerProvider {

	private final Pattern VERSION_PATTERN = Pattern.compile("^TAP version \\d+$", Pattern.CASE_INSENSITIVE);  //$NON-NLS-1$
	private final Pattern PLAN_PATTERN = Pattern.compile("^1..(?<count>\\d+)(\\s*#\\s*(?<message>(?<skip>skip).*|.*))?$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
	private final Pattern BAIL_OUT_PATTERN = Pattern.compile("^Bail out!(\\s*(?<message>.+))?$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
	private final Pattern TEST_RESULT_PATTERN = Pattern.compile("^(?<not>not )?ok( (?<number>\\d+))?(\\s*(?<name>[^#]+))?(\\s*#\\s*(?<message>((?<skip>SKIP)|(?<todo>TODO)).*|.*))?", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
	private final Pattern GCC_DIAGNOSTIC_PATTERN = Pattern.compile("^(?<filename>[^:]+):((?<line>\\d+):)? (?<level>(error|warning|info)):\\s*(?<message>.*)"); //$NON-NLS-1$
	
	@Override
	public String[] getAdditionalLaunchParameters(String[][] testPaths) throws TestingException {
		// No arguments specified by TAP
		return new String[0];
	}
	
    /**
     * Construct the error message from prefix and detailed description.
     *
     * @param prefix prefix
     * @param description detailed description
     * @return the full message
     */
	private String getErrorText(String prefix, String description) {
		return MessageFormat.format(TAPTestsRunnerMessages.TAPTestsRunner_error_format, prefix, description);
	}
	
	@Override
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) throws TestingException {
		// The TAP is really has lots of optional data and supports the most
		// bare minimum possible for test output 
		
		try {
			// The TAP version may only be specified on the first line
			boolean firstLine = true;
			// The test plan is optional, but may be specified at most once
			boolean hasPlan = false;
			int plannedCount = -1;
			int currentTestNumber = 1;

			Queue<String> output = new LinkedList<>();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher m;
				if ((m = VERSION_PATTERN.matcher(line)).matches()) {
					// Version must be on the first line, or missing
					if (!firstLine) {
						throw new TestingException(getErrorText(TAPTestsRunnerMessages.TAPTestsRunner_tap_error_prefix, TAPTestsRunnerMessages.TAPTestsRunner_invalid_version_line));
					}
					
					// We actually don't care about the version itself
					
				} else if ((m = PLAN_PATTERN.matcher(line)).matches()) {
					// Only one plan is allowed (after the optional version or at the end of the test run)
					if (hasPlan) {
						throw new TestingException(getErrorText(TAPTestsRunnerMessages.TAPTestsRunner_tap_error_prefix, TAPTestsRunnerMessages.TAPTestsRunner_multiple_plans));
					}
					hasPlan = true;
					
					plannedCount = Integer.parseInt(m.group("count")); //$NON-NLS-1$
					
					// The plan might decide to skip all tests. We fill up
					// remaining tests if the count doesn't add up.
					//
					// This loop is almost identical to the final filler loop,
					// but we may have a message as to why we had to skip these
					// tests, which we don't have if the test has completed
					// without a matching number of test results to the
					// planned number of tests  
					if (m.group("skip") != null) { //$NON-NLS-1$
						String message = m.group("message"); //$NON-NLS-1$
						for (; currentTestNumber <= plannedCount; currentTestNumber += 1) {
							modelUpdater.enterTestCase(Integer.toString(currentTestNumber));
							if (message != null) {
								modelUpdater.addTestMessage(null, 0, Level.Message, message);
							}
							modelUpdater.setTestStatus(Status.Skipped);
							modelUpdater.exitTestCase();
						}
						
						// Exit early
						break;
					}
					
				} else if ((m = BAIL_OUT_PATTERN.matcher(line)).matches()) {
					// The test has been aborted by the module. Mark any
					// planned tests accordingly

					String message = m.group("message"); //$NON-NLS-1$
					for (; currentTestNumber <= plannedCount; currentTestNumber += 1) {
						modelUpdater.enterTestCase(Integer.toString(currentTestNumber));
						if (message != null) {
							modelUpdater.addTestMessage(null, 0, Level.Message, message);
						}
						modelUpdater.setTestStatus(Status.Aborted);
						modelUpdater.exitTestCase();
					}
					
				} else if ((m = TEST_RESULT_PATTERN.matcher(line)).matches()) {
					// The index number optional. It may indicate skipped tests
					// if it jumps ahead
					String number = m.group("number"); //$NON-NLS-1$
					if (number != null) {
						int newNumber = Integer.parseInt(number);

						// Negative jumps however, are not allowed
						if (newNumber < currentTestNumber) {
							throw new TestingException(getErrorText(TAPTestsRunnerMessages.TAPTestsRunner_tap_error_prefix, TAPTestsRunnerMessages.TAPTestsRunner_invalid_test_number));
						}
						
						// Skip tests for all numbers that have been skipped by
						// the new test number
						for (; currentTestNumber < newNumber; currentTestNumber += 1) {
							modelUpdater.enterTestCase(Integer.toString(currentTestNumber));
							modelUpdater.setTestStatus(Status.Skipped);
							modelUpdater.exitTestCase();
						}
						
						assert currentTestNumber == newNumber;
					}
					
					// The test name is of course also optional
					String name = m.group("name"); //$NON-NLS-1$
					if (name == null || name.trim().isEmpty()) {
						name = Integer.toString(currentTestNumber);
					} else {
						name = name.trim();
					}
					
					modelUpdater.enterTestCase(name);
					
					// Add the optional test result message of skip and todo
					// results
					String message = m.group("message"); //$NON-NLS-1$
					if (message != null) {
						modelUpdater.addTestMessage(null, 0, Level.Message, message);
					}

					for (String o : output) {
						Matcher diagnosticMatch = GCC_DIAGNOSTIC_PATTERN.matcher(o);
						if (diagnosticMatch.matches()) {
							int lineNumber = 0;
							Level level = Level.Message;
							
							String diagLine = diagnosticMatch.group("line"); //$NON-NLS-1$
							if (diagLine != null) {
								lineNumber = Integer.parseInt(diagLine);
							}
							
							String diagLevel = diagnosticMatch.group("level"); //$NON-NLS-1$
							if (diagLevel.equals("error")) { //$NON-NLS-1$
								level = Level.Error;
							} else if (diagLevel.equals("warning")) { //$NON-NLS-1$
								level = Level.Warning;
							} else if (diagLevel.equals("info")) { //$NON-NLS-1$
								level = Level.Info;
							}
							
							modelUpdater.addTestMessage(diagnosticMatch.group("filename"), lineNumber, level, diagnosticMatch.group("message")); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							modelUpdater.addTestMessage(null, 0, Level.Message, o);
						}
					}
					output.clear();
					
					if (m.group("skip") != null) { //$NON-NLS-1$
						modelUpdater.setTestStatus(Status.Skipped);
					} else if (m.group("todo") != null) { //$NON-NLS-1$
						modelUpdater.setTestStatus(Status.NotRun);
					} else if (m.group("not") != null) { //$NON-NLS-1$
						modelUpdater.setTestStatus(Status.Failed);
					} else {
						modelUpdater.setTestStatus(Status.Passed);
					}
					
					modelUpdater.exitTestCase();

					currentTestNumber += 1;
					
				} else {
					// Add unknown output to the test case. We associate that
					// data as soon as we see the "ok/not ok" line
					output.add(line);
				}
				
				firstLine = false;
			}

			// The test plan may contain more tests than we have test results
			// for. Skip the remaining tests.
			for (; currentTestNumber <= plannedCount; currentTestNumber += 1) {
				modelUpdater.enterTestCase(Integer.toString(currentTestNumber));
				modelUpdater.setTestStatus(Status.Skipped);
				modelUpdater.exitTestCase();
			}
			
		} catch (IOException e) {
			throw new TestingException(getErrorText(TAPTestsRunnerMessages.TAPTestsRunner_io_error_prefix, e.getLocalizedMessage()));			
		}
	}
}
