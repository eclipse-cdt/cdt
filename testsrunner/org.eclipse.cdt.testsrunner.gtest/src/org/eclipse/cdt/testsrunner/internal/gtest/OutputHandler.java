/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.gtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.xml.sax.SAXException;

/**
 * <p>
 * Parses the output of Google Testing Framework and notifies the Tests Runner
 * Core about how the testing process is going.
 * </p>
 * <p>
 * Unfortunately, gtest does not provide a realtime XML output (yes, it has XML
 * output, but it is generated after testing process is done), so we have to
 * parse its output that is less reliable.
 * </p>
 * <p>
 * The parsing is done with a simple FSM (Final State Machine). There is an
 * internal state that changes when input tokens (gtest output lines) come.
 * There is a transitions table that is used to determine what is the next state
 * depending on the current one and the input token. The state may define
 * onEnter and onExit actions to do the useful job.
 * </p>
 */
public class OutputHandler {

	/**
	 * Base class for the FSM internal state.
	 */
	class State {

		/** Stores the regular expression by which the state should be entered. */
		private Pattern enterPattern;

		/** The regular expression matcher. */
		private Matcher matcher;

		/** Groups count in a regular expression. */
		private int groupCount;

		/**
		 * The constructor.
		 *
		 * @param enterRegex the regular expression by which the state should be
		 * entered
		 */
		State(String enterRegex) {
			this(enterRegex, -1);
		}

		/**
		 * The constructor.
		 *
		 * @param enterRegex the regular expression by which the state should be
		 * entered
		 * @param groupCount groups count in a regular expression. It is used
		 * just to make debug easier and the parser more reliable.
		 */
		State(String enterRegex, int groupCount) {
			enterPattern = Pattern.compile(enterRegex);
			this.groupCount = groupCount;
		}

		/**
		 * Checks whether the specified string matches the enter pattern
		 * (regular expression). If it is so the state should be entered.
		 *
		 * @param line input line (token)
		 * @return true if matches and false otherwise
		 * @throws TestingException if groups count does not match the defined
		 * in constructor number.
		 */
		public boolean match(String line) throws TestingException {
			matcher = enterPattern.matcher(line);
			boolean groupsCountOk = groupCount == -1 || matcher.groupCount() == groupCount;
			if (!groupsCountOk) {
				generateInternalError(MessageFormat.format(GoogleTestsRunnerMessages.OutputHandler_wrong_groups_count,
						enterPattern.pattern(), matcher.groupCount(), groupCount));
			}
			boolean matches = matcher.matches();
			if (!matches || !groupsCountOk) {
				// Do not keep the reference - it will be unnecessary anyway
				matcher = null;
			}
			return matches;
		}

		/**
		 * Returns the matched group value by index.
		 *
		 * @param groupNumber group index
		 * @return group value
		 */
		protected String group(int groupNumber) {
			return matcher.group(groupNumber);
		}

		/**
		 * Action that triggers on state enter.
		 *
		 * @param previousState previous state
		 * @throws TestingException if testing error is detected
		 */
		public void onEnter(State previousState) throws TestingException {
		}

		/**
		 * Action that triggers on state exit.
		 *
		 * @param previousState next state
		 * @throws TestingException if testing error is detected
		 */
		public void onExit(State nextState) {
		}

		/**
		 * Common routine that constructs full test suite name by name and type
		 * parameter.
		 *
		 * @param name test suite name
		 * @param typeParameter type parameter
		 * @return full test suite name
		 */
		protected String getTestSuiteName(String name, String typeParameter) {
			return (typeParameter != null) ? MessageFormat.format("{0}({1})", name, typeParameter.trim()) : name; //$NON-NLS-1$
		}
	}

	/**
	 * The state is activated when a new test suite is started.
	 */
	class TestSuiteStart extends State {

		/** Stores the matched type parameter. */
		private String typeParameter;

		TestSuiteStart(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		/**
		 * Stores type parameter and notify Tests Runner Core about test suite
		 * start.
		 */
		@Override
		public void onEnter(State previousState) {
			typeParameter = group(3);
			modelUpdater.enterTestSuite(getTestSuiteName(group(1), typeParameter));
		}

		/**
		 * Provides access to the matched type parameter.
		 *
		 * @return type parameter value
		 */
		public String getTypeParameter() {
			return typeParameter;
		}
	}

	/**
	 * The state is activated when a new test case is started.
	 */
	class TestCaseStart extends State {

		TestCaseStart(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		/**
		 * Extract current test case and test suite names and notify Tests
		 * Runner Core about test case start.
		 *
		 * @throws TestingException if extracted test suite name does not match
		 * last entered test suite name.
		 */
		@Override
		public void onEnter(State previousState) throws TestingException {
			String testCaseName = group(2);
			String lastTestSuiteName = modelUpdater.currentTestSuite().getName();
			String currTestSuiteName = getTestSuiteName(group(1), stateTestSuiteStart.getTypeParameter());
			if (!lastTestSuiteName.equals(currTestSuiteName)) {
				generateInternalError(MessageFormat.format(GoogleTestsRunnerMessages.OutputHandler_wrong_suite_name,
						testCaseName, currTestSuiteName, lastTestSuiteName));
			}
			modelUpdater.enterTestCase(testCaseName);
		}
	}

	/**
	 * The state is activated when an error message's location is started.
	 */
	class ErrorMessageLocation extends State {

		/** Stores the message location file name. */
		private String messageFileName;

		/** Stores the message location line number. */
		private int messageLineNumber;

		/** Stores the first part of the message. */
		private String messagePart;

		ErrorMessageLocation(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		/**
		 * Extract the data for the message location (file name, line number).
		 * The data may be provided in a common style ("/path/file:line" with
		 * the message text starting on the next line) or Visual Studio style
		 * ("/path/file(line):" with the message text continuing on the same
		 * line). It is also possible not to specify line number at all
		 * ("/path/file:").
		 *
		 * @throws TestingException if location format cannot be recognized.
		 */
		@Override
		public void onEnter(State previousState) throws TestingException {
			String fileNameIfLinePresent = group(2);
			String fileNameIfLineAbsent = group(6);
			String lineNumberCommon = group(4);
			String lineNumberVS = group(5);
			if (fileNameIfLinePresent != null) {
				if (lineNumberCommon != null) {
					messageFileName = fileNameIfLinePresent;
					messageLineNumber = Integer.parseInt(lineNumberCommon.trim());
				} else if (lineNumberVS != null) {
					messageFileName = fileNameIfLinePresent;
					messageLineNumber = Integer.parseInt(lineNumberVS.trim());
				} else {
					if (!modelUpdater.currentTestSuite().getName().equals(group(1))) {
						generateInternalError(GoogleTestsRunnerMessages.OutputHandler_unknown_location_format);
					}
				}
			} else if (fileNameIfLineAbsent != null) {
				if (lineNumberCommon == null && lineNumberVS == null) {
					messageFileName = fileNameIfLineAbsent;
					messageLineNumber = DEFAULT_LOCATION_LINE;
				} else {
					generateInternalError(GoogleTestsRunnerMessages.OutputHandler_unknown_location_format);
				}
			}
			// Check special case when file is not known - reset location
			if (messageFileName.equals("unknown file")) { //$NON-NLS-1$
				messageFileName = DEFAULT_LOCATION_FILE;
			}
			// NOTE: For Visual Studio style there is also first part of the message at this line
			messagePart = group(8);
		}

		/**
		 * Provides access to the message location file name.
		 *
		 * @return file name
		 */
		public String getMessageFileName() {
			return messageFileName;
		}

		/**
		 * Provides access to the message location line number.
		 *
		 * @return line number
		 */
		public int getMessageLineNumber() {
			return messageLineNumber;
		}

		/**
		 * Provides access to the first part of the message.
		 *
		 * @return message part
		 */
		public String getMessagePart() {
			return messagePart;
		}
	}

	/**
	 * The state is activated when an error message text is started or continued.
	 */
	class ErrorMessage extends State {

		/** Stores the error message text that was already read. */
		private StringBuilder messagePart = new StringBuilder();

		ErrorMessage(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		/**
		 * Collects the error message parts into internal buffer. If the
		 * previous state is not the same (it should be
		 * stateErrorMessageLocation) - get the message part from it.
		 */
		@Override
		public void onEnter(State previousState) {
			boolean needEndOfLine = (this == previousState);
			if (this != previousState) {
				String firstMessagePart = stateErrorMessageLocation.getMessagePart();
				if (firstMessagePart != null) {
					messagePart.append(firstMessagePart);
					needEndOfLine = true;
				}
			}
			if (needEndOfLine) {
				messagePart.append(System.getProperty("line.separator")); //$NON-NLS-1$
			}
			messagePart.append(group(1));
		}

		/**
		 * Notifies the Tests Runner Core about new test message.
		 */
		@Override
		public void onExit(State nextState) {
			if (this != nextState) {
				modelUpdater.addTestMessage(stateErrorMessageLocation.getMessageFileName(),
						stateErrorMessageLocation.getMessageLineNumber(), ITestMessage.Level.Error,
						messagePart.toString());
				messagePart.setLength(0);
			}
		}
	}

	/**
	 * The state is activated when a test trace is started or continued.
	 */
	class TestTrace extends ErrorMessageLocation {

		TestTrace(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		/**
		 * Notifies the Tests Runner Core about new test message with test trace
		 * info.
		 */
		@Override
		public void onEnter(State previousState) throws TestingException {
			super.onEnter(previousState);
			modelUpdater.addTestMessage(getMessageFileName(), getMessageLineNumber(), ITestMessage.Level.Info,
					getMessagePart());
		}
	}

	/**
	 * The state is activated when a test case is finished.
	 */
	class TestCaseEnd extends State {

		TestCaseEnd(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		/**
		 * Sets the test case execution time, status and notify Tests Runner
		 * Core about test case end.
		 *
		 * @throws TestingException if current test suite or case name does not
		 * match last entered test suite or case name or if test status is not
		 * known.
		 */
		@Override
		public void onEnter(State previousState) throws TestingException {
			String lastTestSuiteName = modelUpdater.currentTestSuite().getName();
			String explicitTypeParameter = group(5);
			String typeParameter = explicitTypeParameter != null ? explicitTypeParameter
					: stateTestSuiteStart.getTypeParameter();
			String currTestSuiteName = getTestSuiteName(group(2), typeParameter);
			if (!lastTestSuiteName.equals(currTestSuiteName)) {
				generateInternalError(MessageFormat.format(GoogleTestsRunnerMessages.OutputHandler_wrong_suite_name,
						group(2), currTestSuiteName, lastTestSuiteName));
			}
			String lastTestCaseName = modelUpdater.currentTestCase().getName();
			if (!lastTestCaseName.equals(group(3))) {
				generateInternalError(MessageFormat.format(GoogleTestsRunnerMessages.OutputHandler_unexpected_case_end,
						group(3), lastTestCaseName));
			}
			String testStatusStr = group(1);
			ITestItem.Status testStatus = ITestItem.Status.Skipped;
			if (testStatusStr.equals(testStatusOk)) {
				testStatus = ITestItem.Status.Passed;
			} else if (testStatusStr.equals(testStatusFailed)) {
				testStatus = ITestItem.Status.Failed;
			} else {
				generateInternalError(MessageFormat.format(GoogleTestsRunnerMessages.OutputHandler_unknown_test_status,
						testStatusStr));
			}
			String getParamValue = group(7);
			if (getParamValue != null) {
				modelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Info,
						MessageFormat.format(GoogleTestsRunnerMessages.OutputHandler_getparam_message, getParamValue));

			}
			modelUpdater.setTestingTime(Integer.parseInt(group(8)));
			modelUpdater.setTestStatus(testStatus);
			modelUpdater.exitTestCase();
		}
	}

	/**
	 * The state is activated when a test suite is finished.
	 */
	class TestSuiteEnd extends State {

		TestSuiteEnd(String enterRegex, int groupCount) {
			super(enterRegex, groupCount);
		}

		/**
		 * Notify Tests Runner Core about test suite end.
		 *
		 * @throws TestingException if current test suite name does not match
		 * last entered test suite name.
		 */
		@Override
		public void onEnter(State previousState) throws TestingException {
			String lastTestSuiteName = modelUpdater.currentTestSuite().getName();
			String currTestSuiteName = getTestSuiteName(group(1), stateTestSuiteStart.getTypeParameter());
			if (!lastTestSuiteName.equals(currTestSuiteName)) {
				generateInternalError(MessageFormat.format(GoogleTestsRunnerMessages.OutputHandler_unexpected_suite_end,
						currTestSuiteName, lastTestSuiteName));
			}
			modelUpdater.exitTestSuite();
		}
	}

	/** The default file name for test message location. */
	private static final String DEFAULT_LOCATION_FILE = null;

	/** The default line number for test message location. */
	private static final int DEFAULT_LOCATION_LINE = 1;

	// Common regular expression parts
	static private String regexTestSuiteName = "([^, ]+)"; //$NON-NLS-1$
	static private String regexParameterInstantiation = "(\\s*,\\s+where\\s+TypeParam\\s*=(.+))?"; //$NON-NLS-1$
	static private String regexTestName = regexTestSuiteName + "\\.([^,]+)"; //$NON-NLS-1$
	static private String regexTestCount = "\\d+\\s+tests?"; //$NON-NLS-1$
	static private String regexTestTime = "(\\d+)\\s+ms"; //$NON-NLS-1$
	/* Matches location in the following formats:
	 *   - /file:line:
	 *   - /file(line):
	 *   - /file:       (with no line number specified)
	 * Groups:
	 *   1 - all except ":"
	 *   2 - file name (if line present) *
	 *   3 - line number with delimiters
	 *   4 - line number (common style) *
	 *   5 - line number (Visual Studio style) *
	 *   6 - file name (if no line number specified) *
	 * Using:
	 *   - group 2 with 4 or 5 (if line number was specified)
	 *   - group 6 (if filename only was specified)
	 */
	static private String regexLocation = "((.*)(:(\\d+)|\\((\\d+)\\))|(.*[^):])):"; //$NON-NLS-1$

	// Test statuses representation
	static private String testStatusOk = "OK"; //$NON-NLS-1$
	static private String testStatusFailed = "FAILED"; //$NON-NLS-1$

	// All available states in FSM
	private State stateInitial = new State(""); //$NON-NLS-1$
	private State stateInitialized = new State(".*Global test environment set-up.*"); //$NON-NLS-1$
	private TestSuiteStart stateTestSuiteStart = new TestSuiteStart(
			"\\[-*\\]\\s+" + regexTestCount + "\\s+from\\s+" + regexTestSuiteName + regexParameterInstantiation, 3); //$NON-NLS-1$ //$NON-NLS-2$
	private State stateTestCaseStart = new TestCaseStart("\\[\\s*RUN\\s*\\]\\s+" + regexTestName, 2); //$NON-NLS-1$
	private ErrorMessageLocation stateErrorMessageLocation = new ErrorMessageLocation(
			regexLocation + "\\s+(Failure|error: (.*))", 8); //$NON-NLS-1$
	private State stateErrorMessage = new ErrorMessage("(.*)", 1); //$NON-NLS-1$
	private State stateTestTraceStart = new State(".*Google Test trace.*"); //$NON-NLS-1$
	// NOTE: Use 8 groups instead of 7 cause we need to be consistent with ErrorMessageLocation (as we subclass it)
	private State stateTestTrace = new TestTrace(regexLocation + "\\s+((.*))", 8); //$NON-NLS-1$
	private State stateTestCaseEnd = new TestCaseEnd("\\[\\s*(" + testStatusOk + "|" + testStatusFailed + ")\\s*\\]\\s+" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			+ regexTestName + regexParameterInstantiation
			+ "(\\s*,\\s+where\\s+GetParam\\s*\\(\\s*\\)\\s*=\\s*(.+))?\\s+\\(" + regexTestTime + "\\)", 8); //$NON-NLS-1$ //$NON-NLS-2$
	private State stateTestSuiteEnd = new TestSuiteEnd("\\[-*\\]\\s+" + regexTestCount + "\\s+from\\s+" //$NON-NLS-1$//$NON-NLS-2$
			+ regexTestSuiteName + "\\s+\\(" + regexTestTime + "\\s+total\\)", 2); //$NON-NLS-1$ //$NON-NLS-2$
	private State stateFinal = new State(".*Global test environment tear-down.*"); //$NON-NLS-1$
	// NOTE: This state is a special workaround for empty test modules (they haven't got global test environment set-up/tear-down). They should be always passed.
	private State stateEmptyTestModuleFinal = new State(".*\\[\\s*PASSED\\s*\\]\\s+0\\s+tests.*"); //$NON-NLS-1$

	// Transitions table
	private Map<State, State[]> transitions = new HashMap<>();
	{
		// NOTE: Next states order is important!
		transitions.put(from(stateInitial), to(stateInitialized, stateEmptyTestModuleFinal));
		transitions.put(from(stateInitialized), to(stateTestSuiteStart));
		transitions.put(from(stateTestSuiteStart), to(stateTestCaseStart));
		transitions.put(from(stateTestCaseStart), to(stateTestCaseEnd, stateErrorMessageLocation));
		transitions.put(from(stateErrorMessageLocation),
				to(stateTestTraceStart, stateTestCaseEnd, stateErrorMessageLocation, stateErrorMessage));
		transitions.put(from(stateErrorMessage),
				to(stateTestTraceStart, stateTestCaseEnd, stateErrorMessageLocation, stateErrorMessage));
		transitions.put(from(stateTestTraceStart), to(stateTestTrace));
		transitions.put(from(stateTestTrace), to(stateTestCaseEnd, stateErrorMessageLocation, stateTestTrace));
		transitions.put(from(stateTestCaseEnd), to(stateTestCaseStart, stateTestSuiteEnd));
		transitions.put(from(stateTestSuiteEnd), to(stateTestSuiteStart, stateFinal));
	}

	/** Current FSM state. */
	private State currentState;

	/** The interface to notify the Tests Runner Core */
	private ITestModelUpdater modelUpdater;

	OutputHandler(ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
	}

	/**
	 * Runs the parsing process. Initializes the FSM, selects new states with
	 * transitions table and checks whether the parsing completes successfully.
	 *
	 * @param inputStream gtest test module output stream
	 * @throws IOException if stream reading error happens
	 * @throws TestingException if testing error happens
	 */
	public void run(InputStream inputStream) throws IOException, TestingException {
		// Initialize input stream reader
		InputStreamReader streamReader = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(streamReader);
		String line;
		boolean finalizedProperly = false;

		// Initialize internal state
		currentState = stateInitial;
		while ((line = reader.readLine()) != null) {
			// Search for the next possible state
			State[] possibleNextStates = transitions.get(currentState);
			if (possibleNextStates == null) {
				// Final state, stop running
				finalizedProperly = true;
				break;
			}
			for (State nextState : possibleNextStates) {
				if (nextState.match(line)) {
					// Next state found - send notifications to the states
					currentState.onExit(nextState);
					State previousState = currentState;
					currentState = nextState;
					nextState.onEnter(previousState);
					break;
				}
			}
			// NOTE: We cannot be sure that we cover all the output of gtest with our regular expressions
			//       (e.g. some framework notes or warnings may be uncovered etc.), so we just skip unmatched
			//       lines without an error
		}
		// Check whether the last line leads to the final state
		if (transitions.get(currentState) == null) {
			finalizedProperly = true;
		}
		if (!finalizedProperly) {
			generateInternalError(GoogleTestsRunnerMessages.OutputHandler_unexpected_output);
		}
	}

	/**
	 * Throws the testing exception with unknown internal error prefix and the specified description.
	 *
	 * @param additionalInfo additional description of what happens
	 * @throws SAXException the exception that will be thrown
	 */
	private void generateInternalError(String additionalInfo) throws TestingException {
		TestingException e = new TestingException(
				GoogleTestsRunnerMessages.OutputHandler_unknown_error_prefix + additionalInfo);
		GoogleTestsRunnerPlugin.log(e);
		throw e;
	}

	/**
	 * Helper functions to make code more readable.
	 *
	 * @param fromState state to return
	 * @return passed state
	 */
	private State from(State fromState) {
		return fromState;
	}

	/**
	 * Helper functions to make code more readable.
	 *
	 * @param toStates states array to return
	 * @return passed states array
	 */
	private State[] to(State... toStates) {
		return toStates;
	}

}
