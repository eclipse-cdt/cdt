/*******************************************************************************
 * Copyright (c) 2011, 2013 Anton Gorenkov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial implementation
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.testsrunners;

import org.eclipse.cdt.testsrunner.internal.boost.BoostTestsRunnerProvider;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;

/**
 * Tests for {@see BoostTestsRunner} class
 */
@SuppressWarnings("nls")
public class BoostTestCase extends BaseTestCase {

	private static final String DEFAULT_LOCATION_FILE = null;
	private static final int DEFAULT_LOCATION_LINE = -1;
	private static final String EXCEPTION_CHECKPOINT_SUFFIX = "\nLast check point was here.";

	@Override
	public ITestsRunnerProvider createTestsRunner() {
		return new BoostTestsRunnerProvider();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//	      <TestCase name="test"/>
	//    </TestSuite>
	//</TestLog>
	public void testTheOnlyTestCase() {
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("test");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//	      <TestCase name="test1"/>
	//	      <TestCase name="test2"/>
	//	      <TestCase name="test3"/>
	//    </TestSuite>
	//</TestLog>
	public void testAFewTestCases() {
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("test1");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test2");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test3");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestSuite name="InnerTS"/>
	//    </TestSuite>
	//</TestLog>
	public void testEmptyTestSuite() {
		// NOTE: This is impossible input data (at least, for current version of Boost), but we check it anyway
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestSuite("InnerTS");
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestSuite name="InnerTS">
	//	           <TestCase name="test1"/>
	//	           <TestCase name="test2"/>
	//	           <TestCase name="test3"/>
	//        </TestSuite>
	//    </TestSuite>
	//</TestLog>
	public void testAFewTestCasesInTestSuite() {
		// NOTE: This is impossible input data (at least, for current version of Boost), but we check it anyway
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestSuite("InnerTS");
		mockModelUpdater.enterTestCase("test1");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test2");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test3");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestSuite name="InnerTS">
	//            <TestSuite name="InnerInnerTS"/>
	//        </TestSuite>
	//    </TestSuite>
	//</TestLog>
	public void testEmptyTestSuiteInTestSuite() {
		// NOTE: This is impossible input data (at least, for current version of Boost), but we check it anyway
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestSuite("InnerTS");
		mockModelUpdater.enterTestSuite("InnerInnerTS");
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestSuite name="InnerTS">
	//            <TestSuite name="InnerInnerTS">
	//	              <TestCase name="test1"/>
	//            </TestSuite>
	//	          <TestCase name="test2"/>
	//	          <TestCase name="test3"/>
	//        </TestSuite>
	//    </TestSuite>
	//	   <TestCase name="test4">
	//    </TestCase>
	//</TestLog>
	public void testSimpleTestsHierarchy() {
		// NOTE: This is impossible input data (at least, for current version of Boost), but we check it anyway
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestSuite("InnerTS");
		mockModelUpdater.enterTestSuite("InnerInnerTS");
		mockModelUpdater.enterTestCase("test1");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.enterTestCase("test2");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test3");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.enterTestCase("test4");
		mockModelUpdater.exitTestCase();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="testPass">
	//            <Info file="file.cpp" line="22">check 1 passed</Info>
	//        </TestCase>
	//        <TestCase name="testPassWithWarning">
	//            <Warning file="file.cpp" line="27">condition 0 is not satisfied</Warning>
	//            <Message file="file2.h" line="220">Test case testPassWithWarning did not run any assertions</Message>
	//        </TestCase>
	//        <TestCase name="testPassIfEmpty">
	//            <Message file="file2.h" line="220">Test case testPassIfEmpty did not run any assertions</Message>
	//        </TestCase>
	//        <TestCase name="testFailWithCheck">
	//            <Error file="file.cpp" line="32">check 0 failed</Error>
	//            <Info file="file.cpp" line="33">check 1 passed</Info>
	//        </TestCase>
	//        <TestCase name="testFailWithRequire">
	//            <FatalError file="file.cpp" line="38">critical check 0 failed</FatalError>
	//        </TestCase>
	//        <TestCase name="testAbortedOnException">
	//            <Exception>unknown type</Exception>
	//        </TestCase>
	//        <TestCase name="testAbortedOnNullDereference">
	//            <Exception>memory access violation at address: 0x00000000: no mapping at fault address</Exception>
	//            <Message file="file2.h" line="164">Test is aborted</Message>
	//        </TestCase>
	//    </TestSuite>
	//</TestLog>
	public void testDifferentTestStatuses() {
		mockModelUpdater.skipCalls("addTestMessage");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("testPass");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testPassWithWarning");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testPassIfEmpty");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testFailWithCheck");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testFailWithRequire");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testAbortedOnException");
		mockModelUpdater.setTestStatus(ITestItem.Status.Aborted);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testAbortedOnNullDereference");
		mockModelUpdater.setTestStatus(ITestItem.Status.Aborted);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="test">
	//            <Info file="file.cpp" line="22"/>
	//            <Exception><LastCheckpoint file="file2.cpp" line="47"/></Exception>
	//        </TestCase>
	//    </TestSuite>
	//</TestLog>
	public void testEmptyMessage() {
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("test");
		mockModelUpdater.addTestMessage("file.cpp", 22, ITestMessage.Level.Info, "");
		mockModelUpdater.addTestMessage("file2.cpp", 47, ITestMessage.Level.Exception, EXCEPTION_CHECKPOINT_SUFFIX);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="test">
	//            <Warning file="file" line="42">  Custom warning   </Warning>
	//            <Exception>  Exception message <LastCheckpoint file="file2" line="47"/> end   </Exception>
	//        </TestCase>
	//    </TestSuite>
	//</TestLog>
	public void testSpacesInBeginAndEndOfMessage() {
		// NOTE: Last checkpoint tag cannot be in the middle of exception (at least, for current version of Boost), but we check it anyway
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("test");
		mockModelUpdater.addTestMessage("file", 42, ITestMessage.Level.Warning, "  Custom warning   ");
		mockModelUpdater.addTestMessage("file2", 47, ITestMessage.Level.Exception,
				"  Exception message  end   " + EXCEPTION_CHECKPOINT_SUFFIX);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="test">
	//            <Warning>Custom warning</Warning>
	//            <Exception file="file.cpp" line="1">Exceptions should be located by pass point</Exception>
	//        </TestCase>
	//    </TestSuite>
	//</TestLog>
	public void testMessageWithoutLocation() {
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("test");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Warning,
				"Custom warning");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Exception,
				"Exceptions should be located by pass point");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="test">
	//            <Warning file="file.cpp">No line number</Warning>
	//            <Warning file="" line="1">Empty file name</Warning>
	//            <Warning line="2">No file name</Warning>
	//            <Exception>Exception without line number<LastCheckpoint file="file2.cpp"/></Exception>
	//            <Exception>Exception with empty file name<LastCheckpoint file="" line="3"/></Exception>
	//            <Exception>Exception without file name<LastCheckpoint line="4"/></Exception>
	//        </TestCase>
	//    </TestSuite>
	//</TestLog>
	public void testMessageWithLocation() {
		// NOTE: This is impossible input data (at least, for current version of Boost), but we check it anyway
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("test");
		mockModelUpdater.addTestMessage("file.cpp", DEFAULT_LOCATION_LINE, ITestMessage.Level.Warning,
				"No line number");
		mockModelUpdater.addTestMessage("", 1, ITestMessage.Level.Warning, "Empty file name");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, 2, ITestMessage.Level.Warning, "No file name");
		// NOTE: Last check point is not available, so EXCEPTION_CHECKPOINT_SUFFIX should not be added
		mockModelUpdater.addTestMessage("file2.cpp", DEFAULT_LOCATION_LINE, ITestMessage.Level.Exception,
				"Exception without line number");
		mockModelUpdater.addTestMessage("", 3, ITestMessage.Level.Exception, "Exception with empty file name");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, 4, ITestMessage.Level.Exception,
				"Exception without file name");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="test1">
	//            <Error file="file1.cpp" line="1">Message with location</Error>
	//            <Error>Message without location</Error>
	//        </TestCase>
	//        <TestCase name="test2">
	//            <Exception>Exception with location<LastCheckpoint file="file2.cpp" line="2"/></Exception>
	//            <Exception>Exception without location<LastCheckpoint/></Exception>
	//        </TestCase>
	//        <TestCase name="test3">
	//            <Error file="file3.cpp" line="3">Another message with location</Error>
	//            <Exception>Another exception without location</Exception>
	//        </TestCase>
	//        <TestCase name="test4">
	//            <Exception>Another exception with location<LastCheckpoint file="file4.cpp" line="4"/></Exception>
	//            <Error>Another message without location</Error>
	//        </TestCase>
	//    </TestSuite>
	//</TestLog>
	public void testMessagesWithAndWithoutLocation() {
		// NOTE: This is impossible input data (at least, for current version of Boost), but we check it anyway
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("test1");
		mockModelUpdater.addTestMessage("file1.cpp", 1, ITestMessage.Level.Error, "Message with location");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Error,
				"Message without location");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test2");
		mockModelUpdater.addTestMessage("file2.cpp", 2, ITestMessage.Level.Exception,
				"Exception with location" + EXCEPTION_CHECKPOINT_SUFFIX);
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Exception,
				"Exception without location");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test3");
		mockModelUpdater.addTestMessage("file3.cpp", 3, ITestMessage.Level.Error, "Another message with location");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Exception,
				"Another exception without location");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test4");
		mockModelUpdater.addTestMessage("file4.cpp", 4, ITestMessage.Level.Exception,
				"Another exception with location" + EXCEPTION_CHECKPOINT_SUFFIX);
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Error,
				"Another message without location");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="testPass">
	//            <Info file="file.cpp" line="22">check 1 passed</Info>
	//            <TestingTime>1000</TestingTime>
	//        </TestCase>
	//        <TestCase name="testFail">
	//            <FatalError file="file2.cpp" line="38">critical check 0 failed</FatalError>
	//            <TestingTime>2000</TestingTime>
	//        </TestCase>
	//        <TestCase name="testAbortedOnException">
	//            <Exception>exception message</Exception>
	//            <TestingTime>3000</TestingTime>
	//        </TestCase>
	//        <TestCase name="testAbortedOnNullDereference">
	//            <Exception>another exception message</Exception>
	//            <Message file="file3.h" line="164">Test is aborted</Message>
	//            <TestingTime>4000</TestingTime>
	//        </TestCase>
	//        <TestCase name="testAbortedOnExceptionWithLocation">
	//            <Exception>yet another exception message<LastCheckpoint file="file4.cpp" line="47"/></Exception>
	//            <TestingTime>5000</TestingTime>
	//        </TestCase>
	//    </TestSuite>
	//</TestLog>
	public void testExecutionTimePresence() {
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("testPass");
		mockModelUpdater.addTestMessage("file.cpp", 22, ITestMessage.Level.Info, "check 1 passed");
		mockModelUpdater.setTestingTime(1);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testFail");
		mockModelUpdater.addTestMessage("file2.cpp", 38, ITestMessage.Level.FatalError, "critical check 0 failed");
		mockModelUpdater.setTestingTime(2);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testAbortedOnException");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Exception,
				"exception message");
		mockModelUpdater.setTestingTime(3);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testAbortedOnNullDereference");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Exception,
				"another exception message");
		mockModelUpdater.addTestMessage("file3.h", 164, ITestMessage.Level.Message, "Test is aborted");
		mockModelUpdater.setTestingTime(4);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testAbortedOnExceptionWithLocation");
		mockModelUpdater.addTestMessage("file4.cpp", 47, ITestMessage.Level.Exception,
				"yet another exception message" + EXCEPTION_CHECKPOINT_SUFFIX);
		mockModelUpdater.setTestingTime(5);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//
	public void testNoInput() {
		// NOTE: The comment above is left blank intentionally
		expectTestingException();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="testPass">
	//        <!-- TestCase is not closed -->
	//    </TestSuite>
	//</TestLog>
	public void testBadFormedXml() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		expectTestingException();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="testPass">
	public void testUnexceptedXmlEnd() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		expectTestingException();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <UnexpectedXmlElement name="testPass">
	//            <Info file="file.cpp" line="22">check 1 passed</Info>
	//            <TestingTime>100</TestingTime>
	//        </UnexpectedXmlElement>
	//    </TestSuite>
	//</TestLog>
	public void testUnexpectedXmlElement() {
		mockModelUpdater.skipCalls("enterTestSuite");
		expectTestingException();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="testPass">
	//            <Info file="file.cpp" line="wrong_value">check 1 passed</Info>
	//        </TestCase>
	//    </TestSuite>
	//</TestLog>
	public void testWrongLineNumberValue() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		expectTestingException();
	}

	//<TestLog>
	//    <TestSuite name="MainTS">
	//        <TestCase name="testPass">
	//            <TestingTime>wrong value</TestingTime>
	//        </TestCase>
	//    </TestSuite>
	//</TestLog>
	public void testWrongExecutionTimeValue() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		expectTestingException();
	}

	// <TestLog>
	// <TestSuite name="MainTS">
	// <TestCase name="test_function">
	// </TestCase>
	// <TestCase name="test_function">
	// </TestCase>
	// <TestCase name="test_function">
	// </TestCase>
	// <TestCase name="another_test_function">
	// </TestCase>
	// <TestCase name="another_test_function">
	// </TestCase>
	// </TestSuite>
	// </TestLog>
	public void testParameterizedTests() {
		mockModelUpdater.skipCalls("setTestStatus");
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("test_function");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test_function (2)");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("test_function (3)");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("another_test_function");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("another_test_function (2)");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}
}
