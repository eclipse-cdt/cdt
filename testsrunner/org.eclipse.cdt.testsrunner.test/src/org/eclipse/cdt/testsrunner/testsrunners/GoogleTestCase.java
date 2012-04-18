/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov  - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.testsrunners;

import org.eclipse.cdt.testsrunner.internal.gtest.GoogleTestsRunnerProvider;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;


/**
 * Tests for {@see GoogleTestsRunner} class
 */
@SuppressWarnings("nls")
public class GoogleTestCase extends BaseTestCase {

	private static final String DEFAULT_LOCATION_FILE = null;
	private static final int DEFAULT_LOCATION_LINE = 1;

	@Override
	protected ITestsRunnerProvider createTestsRunner() {
		return new GoogleTestsRunnerProvider();
	}

	//Running main() from gtest_main.cc
	//[==========] Running 0 tests from 0 test cases.
	//[==========] 0 tests from 0 test cases ran. (0 ms total)
	//[  PASSED  ] 0 tests.
	public void testNoTestCases() {
	}
	
	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] DemoTestCase.DemoTest
	//[       OK ] DemoTestCase.DemoTest (0 ms)
	//[----------] 1 test from DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 1 test from 1 test case ran. (0 ms total)
	//[  PASSED  ] 1 test.
	public void testTheOnlyTestCase() {
		mockModelUpdater.skipCalls("setTestStatus");
		mockModelUpdater.skipCalls("setTestingTime");
		
		mockModelUpdater.enterTestSuite("DemoTestCase");
		mockModelUpdater.enterTestCase("DemoTest");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 3 tests from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 3 tests from DemoTestCase
	//[ RUN      ] DemoTestCase.DemoTest1
	//[       OK ] DemoTestCase.DemoTest1 (0 ms)
	//[ RUN      ] DemoTestCase.DemoTest2
	//[       OK ] DemoTestCase.DemoTest2 (0 ms)
	//[ RUN      ] DemoTestCase.DemoTest3
	//[       OK ] DemoTestCase.DemoTest3 (0 ms)
	//[----------] 3 tests from DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 3 tests from 1 test case ran. (0 ms total)
	//[  PASSED  ] 3 tests.
	public void testAFewTestCasesInTheOnlyTestSuites() {
		mockModelUpdater.skipCalls("setTestStatus");
		mockModelUpdater.skipCalls("setTestingTime");
		
		mockModelUpdater.enterTestSuite("DemoTestCase");
		mockModelUpdater.enterTestCase("DemoTest1");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("DemoTest2");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("DemoTest3");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}
	
	
	//Running main() from gtest_main.cc
	//[==========] Running 2 tests from 2 test cases.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] DemoTestCase.DemoTest
	//[       OK ] DemoTestCase.DemoTest (0 ms)
	//[----------] 1 test from DemoTestCase (0 ms total)
	//
	//[----------] 1 test from DemoTestCase2
	//[ RUN      ] DemoTestCase2.DemoTest2
	//[       OK ] DemoTestCase2.DemoTest2 (0 ms)
	//[----------] 1 test from DemoTestCase2 (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 2 tests from 2 test cases ran. (0 ms total)
	//[  PASSED  ] 2 tests.
	public void testTheOnlyTestCasesInAFewTestSuites() {
		mockModelUpdater.skipCalls("setTestStatus");
		mockModelUpdater.skipCalls("setTestingTime");
		
		mockModelUpdater.enterTestSuite("DemoTestCase");
		mockModelUpdater.enterTestCase("DemoTest");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.enterTestSuite("DemoTestCase2");
		mockModelUpdater.enterTestCase("DemoTest2");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 4 tests from 2 test cases.
	//[----------] Global test environment set-up.
	//[----------] 2 tests from DemoTestCase1
	//[ RUN      ] DemoTestCase1.DemoTest1
	//[       OK ] DemoTestCase1.DemoTest1 (0 ms)
	//[ RUN      ] DemoTestCase1.DemoTest2
	//[       OK ] DemoTestCase1.DemoTest2 (0 ms)
	//[----------] 2 tests from DemoTestCase1 (0 ms total)
	//
	//[----------] 2 tests from DemoTestCase2
	//[ RUN      ] DemoTestCase2.DemoTest1
	//[       OK ] DemoTestCase2.DemoTest1 (0 ms)
	//[ RUN      ] DemoTestCase2.DemoTest2
	//[       OK ] DemoTestCase2.DemoTest2 (0 ms)
	//[----------] 2 tests from DemoTestCase2 (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 4 tests from 2 test cases ran. (0 ms total)
	//[  PASSED  ] 4 tests.
	public void testAFewTestCasesWithTheSameNameInDifferentTestSuites() {
		mockModelUpdater.skipCalls("setTestStatus");
		mockModelUpdater.skipCalls("setTestingTime");
		
		mockModelUpdater.enterTestSuite("DemoTestCase1");
		mockModelUpdater.enterTestCase("DemoTest1");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("DemoTest2");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.enterTestSuite("DemoTestCase2");
		mockModelUpdater.enterTestCase("DemoTest1");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("DemoTest2");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}


	//Running main() from gtest_main.cc
	//[==========] Running 4 tests from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 4 tests from DemoTestCase
	//[ RUN      ] DemoTestCase.TestPass
	//[       OK ] DemoTestCase.TestPass (0 ms)
	//[ RUN      ] DemoTestCase.TestFail
	//demo_file.cc:38: Failure
	//Value of: 2
	//Expected: 1
	//[  FAILED  ] DemoTestCase.TestFail (0 ms)
	//[ RUN      ] DemoTestCase.TestAFewFails
	//demo_file.cc:42: Failure
	//Value of: 2
	//Expected: 1
	//demo_file.cc:43: Failure
	//Value of: 2
	//Expected: 1
	//[  FAILED  ] DemoTestCase.TestAFewFails (0 ms)
	//[ RUN      ] DemoTestCase.TestCustomFails
	//demo_file.cc:47: Failure
	//Failed
	//Custom fatal fail!
	//demo_file.cc:48: Failure
	//Failed
	//Another custom fatal fail!
	//demo_file.cc:49: Failure
	//Failed
	//Yet another custom fatal fail!
	//[  FAILED  ] DemoTestCase.TestCustomFails (0 ms)
	//[----------] 4 tests from DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 4 tests from 1 test case ran. (1 ms total)
	//[  PASSED  ] 1 test.
	//[  FAILED  ] 3 tests, listed below:
	//[  FAILED  ] DemoTestCase.TestFail
	//[  FAILED  ] DemoTestCase.TestAFewFails
	//[  FAILED  ] DemoTestCase.TestCustomFails
	//
	// 3 FAILED TESTS
	public void testDifferentTestStatuses() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("exitTestSuite");
		mockModelUpdater.skipCalls("setTestingTime");
		
		mockModelUpdater.enterTestCase("TestPass");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("TestFail");
		mockModelUpdater.addTestMessage("demo_file.cc", 38, ITestMessage.Level.Error, "Value of: 2"+EOL+"Expected: 1");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("TestAFewFails");
		mockModelUpdater.addTestMessage("demo_file.cc", 42, ITestMessage.Level.Error, "Value of: 2"+EOL+"Expected: 1");
		mockModelUpdater.addTestMessage("demo_file.cc", 43, ITestMessage.Level.Error, "Value of: 2"+EOL+"Expected: 1");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("TestCustomFails");
		mockModelUpdater.addTestMessage("demo_file.cc", 47, ITestMessage.Level.Error, "Failed"+EOL+"Custom fatal fail!");
		mockModelUpdater.addTestMessage("demo_file.cc", 48, ITestMessage.Level.Error, "Failed"+EOL+"Another custom fatal fail!");
		mockModelUpdater.addTestMessage("demo_file.cc", 49, ITestMessage.Level.Error, "Failed"+EOL+"Yet another custom fatal fail!");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 2 tests from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 2 tests from DemoTestCase
	//[ RUN      ] DemoTestCase.TestWithSimpleTrace
	//demo_file.cc:36: Failure
	//Value of: 2
	//Expected: 1
	//Google Test trace:
	//demo_file.cc:41: Trace point #2 in TestWithSimpleTrace
	//demo_file.cc:40: Trace point #1 in TestWithSimpleTrace
	//[  FAILED  ] DemoTestCase.TestWithSimpleTrace (1 ms)
	//[ RUN      ] DemoTestCase.TestTraceForMultipleFails
	//demo_file.cc:36: Failure
	//Value of: 2
	//Expected: 1
	//Google Test trace:
	//demo_file.cc:46: Trace point #1 in TestTraceForMultipleFails
	//demo_file.cc:36: Failure
	//Value of: 2
	//Expected: 1
	//Google Test trace:
	//demo_file.cc:48: Trace point #2 in TestTraceForMultipleFails
	//demo_file.cc:46: Trace point #1 in TestTraceForMultipleFails
	//[  FAILED  ] DemoTestCase.TestTraceForMultipleFails (0 ms)
	//[----------] 2 tests from DemoTestCase (1 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 2 tests from 1 test case ran. (1 ms total)
	//[  PASSED  ] 0 tests.
	//[  FAILED  ] 2 tests, listed below:
	//[  FAILED  ] DemoTestCase.TestWithSimpleTrace
	//[  FAILED  ] DemoTestCase.TestTraceForMultipleFails
	//
	// 2 FAILED TESTS
	public void testScopedTraceSupport() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("exitTestSuite");
		mockModelUpdater.skipCalls("setTestingTime");
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestCase("TestWithSimpleTrace");
		mockModelUpdater.addTestMessage("demo_file.cc", 36, ITestMessage.Level.Error, "Value of: 2"+EOL+"Expected: 1");
		mockModelUpdater.addTestMessage("demo_file.cc", 41, ITestMessage.Level.Info, "Trace point #2 in TestWithSimpleTrace");
		mockModelUpdater.addTestMessage("demo_file.cc", 40, ITestMessage.Level.Info, "Trace point #1 in TestWithSimpleTrace");
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("TestTraceForMultipleFails");
		mockModelUpdater.addTestMessage("demo_file.cc", 36, ITestMessage.Level.Error, "Value of: 2"+EOL+"Expected: 1");
		mockModelUpdater.addTestMessage("demo_file.cc", 46, ITestMessage.Level.Info, "Trace point #1 in TestTraceForMultipleFails");
		mockModelUpdater.addTestMessage("demo_file.cc", 36, ITestMessage.Level.Error, "Value of: 2"+EOL+"Expected: 1");
		mockModelUpdater.addTestMessage("demo_file.cc", 48, ITestMessage.Level.Info, "Trace point #2 in TestTraceForMultipleFails");
		mockModelUpdater.addTestMessage("demo_file.cc", 46, ITestMessage.Level.Info, "Trace point #1 in TestTraceForMultipleFails");
		mockModelUpdater.exitTestCase();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] DemoTestCase.TestMessageLocationFormats
	//demo_file_name2.cpp:40: Failure
	//Standard format with file name & line number
	//unknown file:41: Failure
	//Standard format with unknown file name
	//demo_file_name2.cpp(42): Failure
	//VS-like format with file name & line number
	//unknown file(43): Failure
	//VS-like format with unknown file name
	//demo_file_name2.cpp: Failure
	//Location with unknown line number
	//unknown file: Failure
	//Location with unknown file name & line number
	//[  FAILED  ] DemoTestCase.TestMessageLocationFormats (1 ms)
	//[----------] 1 test from DemoTestCase (1 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 1 test from 1 test case ran. (1 ms total)
	//[  PASSED  ] 0 tests.
	//[  FAILED  ] 1 test, listed below:
	//[  FAILED  ] DemoTestCase.TestMessageLocationFormats
	//
	// 1 FAILED TEST
	public void testDifferentLocationsFormatsWithStandardMessageFormat() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("exitTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		mockModelUpdater.skipCalls("exitTestCase");
		mockModelUpdater.skipCalls("setTestingTime");
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.addTestMessage("demo_file_name2.cpp", 40, ITestMessage.Level.Error, "Standard format with file name & line number");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, 41, ITestMessage.Level.Error, "Standard format with unknown file name");
		mockModelUpdater.addTestMessage("demo_file_name2.cpp", 42, ITestMessage.Level.Error, "VS-like format with file name & line number");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, 43, ITestMessage.Level.Error, "VS-like format with unknown file name");
		mockModelUpdater.addTestMessage("demo_file_name2.cpp", DEFAULT_LOCATION_LINE, ITestMessage.Level.Error, "Location with unknown line number");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Error, "Location with unknown file name & line number");
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] DemoTestCase.TestMessageLocationFormats
	//demo_file_name2.cpp:40: error: Standard format with file name & line number
	//unknown file:41: error: Standard format with unknown file name
	//demo_file_name2.cpp(42): error: VS-like format with file name & line number
	//unknown file(43): error: VS-like format with unknown file name
	//demo_file_name2.cpp: error: Location with unknown line number
	//unknown file: error: Location with unknown file name & line number
	//[  FAILED  ] DemoTestCase.TestMessageLocationFormats (1 ms)
	//[----------] 1 test from DemoTestCase (1 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 1 test from 1 test case ran. (1 ms total)
	//[  PASSED  ] 0 tests.
	//[  FAILED  ] 1 test, listed below:
	//[  FAILED  ] DemoTestCase.TestMessageLocationFormats
	//
	// 1 FAILED TEST
	public void testDifferentLocationsFormatsWithVSLikeMessageFormat() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("exitTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		mockModelUpdater.skipCalls("exitTestCase");
		mockModelUpdater.skipCalls("setTestingTime");
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.addTestMessage("demo_file_name2.cpp", 40, ITestMessage.Level.Error, "Standard format with file name & line number");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, 41, ITestMessage.Level.Error, "Standard format with unknown file name");
		mockModelUpdater.addTestMessage("demo_file_name2.cpp", 42, ITestMessage.Level.Error, "VS-like format with file name & line number");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, 43, ITestMessage.Level.Error, "VS-like format with unknown file name");
		mockModelUpdater.addTestMessage("demo_file_name2.cpp", DEFAULT_LOCATION_LINE, ITestMessage.Level.Error, "Location with unknown line number");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Error, "Location with unknown file name & line number");
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] DemoTestCase.TestMultiLineMessage
	//demo_file.cc:40: Failure
	//Line 1
	//Line 2
	//Line 3
	//[  FAILED  ] DemoTestCase.TestMultiLineMessage (0 ms)
	//[----------] 1 test from DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 1 test from 1 test case ran. (0 ms total)
	//[  PASSED  ] 0 tests.
	//[  FAILED  ] 1 test, listed below:
	//[  FAILED  ] DemoTestCase.TestMultiLineMessage
	//
	// 1 FAILED TEST
	public void testMultiLineMessage() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("exitTestSuite");
		mockModelUpdater.skipCalls("setTestingTime");
		mockModelUpdater.skipCalls("setTestStatus");

		mockModelUpdater.enterTestCase("TestMultiLineMessage");
		mockModelUpdater.addTestMessage("demo_file.cc", 40, ITestMessage.Level.Error, "Line 1"+EOL+"Line 2"+EOL+"Line 3");
		mockModelUpdater.exitTestCase();
	}
	
	
	//Running main() from gtest_main.cc
	//[==========] Running 2 tests from 2 test cases.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase1
	//[ RUN      ] DemoTestCase1.Test1
	//unknown file: Failure
	//Unknown C++ exception thrown in the test body.
	//[  FAILED  ] DemoTestCase1.Test1 (1000 ms)
	//[----------] 1 test from DemoTestCase1 (1000 ms total)
	//
	//[----------] 1 test from DemoTestCase2
	//[ RUN      ] DemoTestCase2.Test2
	//[       OK ] DemoTestCase2.Test2 (2000 ms)
	//[----------] 1 test from DemoTestCase2 (2000 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 2 tests from 2 test cases ran. (3000 ms total)
	//[  PASSED  ] 1 test.
	//[  FAILED  ] 1 test, listed below:
	//[  FAILED  ] DemoTestCase1.Test1
	//
	// 1 FAILED TEST
	public void testTestingTimeSupport() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("exitTestSuite");
		mockModelUpdater.skipCalls("setTestStatus");
		mockModelUpdater.skipCalls("addTestMessage");

		mockModelUpdater.enterTestCase("Test1");
		mockModelUpdater.setTestingTime(1000);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("Test2");
		mockModelUpdater.setTestingTime(2000);
		mockModelUpdater.exitTestCase();
	}


	//Running main() from gtest_main.cc
	//[==========] Running 3 tests from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 3 tests from Inst/DemoTestCase
	//[ RUN      ] Inst/DemoTestCase.Test/0
	//demo_file.cc:50: Failure
	//Failed
	//Param1
	//[  FAILED  ] Inst/DemoTestCase.Test/0, where GetParam() = "Param1" (0 ms)
	//[ RUN      ] Inst/DemoTestCase.Test/1
	//[       OK ] Inst/DemoTestCase.Test/1 (0 ms)
	//[ RUN      ] Inst/DemoTestCase.Test/2
	//demo_file.cc:50: Failure
	//Failed
	//Param3
	//[  FAILED  ] Inst/DemoTestCase.Test/2, where GetParam() = "Param3" (0 ms)
	//[----------] 3 tests from Inst/DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 3 tests from 1 test case ran. (0 ms total)
	//[  PASSED  ] 1 test.
	//[  FAILED  ] 2 tests, listed below:
	//[  FAILED  ] Inst/DemoTestCase.Test/0, where GetParam() = "Param1"
	//[  FAILED  ] Inst/DemoTestCase.Test/2, where GetParam() = "Param3"
	//
	// 2 FAILED TESTS
	public void testParametrizedTestsSupport() {
		mockModelUpdater.enterTestSuite("Inst/DemoTestCase");
		mockModelUpdater.enterTestCase("Test/0");
		mockModelUpdater.addTestMessage("demo_file.cc", 50, ITestMessage.Level.Error, "Failed"+EOL+"Param1");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Info, "Instantiated with GetParam() = \"Param1\"");
		mockModelUpdater.setTestingTime(0);
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("Test/1");
		mockModelUpdater.setTestingTime(0);
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("Test/2");
		mockModelUpdater.addTestMessage("demo_file.cc", 50, ITestMessage.Level.Error, "Failed"+EOL+"Param3");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Info, "Instantiated with GetParam() = \"Param3\"");
		mockModelUpdater.setTestingTime(0);
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from Inst/DemoTestCase
	//[ RUN      ] Inst/DemoTestCase.Test/0
	//demo_file.cc:50: Failure
	//Failed
	//[  FAILED  ] Inst/DemoTestCase.Test/0, where GetParam() = 0x4f50cc (0 ms)
	//[----------] 1 test from Inst/DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 1 test from 1 test case ran. (0 ms total)
	//[  PASSED  ] 0 tests.
	//[  FAILED  ] 1 test, listed below:
	//[  FAILED  ] Inst/DemoTestCase.Test/0, where GetParam() = 0x4f50cc
	//
	// 2 FAILED TESTS
	public void testParametrizedTestsWithoutQuotesSupport() {
		mockModelUpdater.enterTestSuite("Inst/DemoTestCase");
		mockModelUpdater.enterTestCase("Test/0");
		mockModelUpdater.addTestMessage("demo_file.cc", 50, ITestMessage.Level.Error, "Failed");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Info, "Instantiated with GetParam() = 0x4f50cc");
		mockModelUpdater.setTestingTime(0);
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 3 tests from 3 test cases.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase/0, where TypeParam = char
	//[ RUN      ] DemoTestCase/0.Test
	//demo_file.cc:60: Failure
	//Failed
	//char type
	//[  FAILED  ] DemoTestCase/0.Test, where TypeParam = char (0 ms)
	//[----------] 1 test from DemoTestCase/0 (0 ms total)
	//
	//[----------] 1 test from DemoTestCase/1, where TypeParam = int
	//[ RUN      ] DemoTestCase/1.Test
	//[       OK ] DemoTestCase/1.Test (0 ms)
	//[----------] 1 test from DemoTestCase/1 (0 ms total)
	//
	//[----------] 1 test from DemoTestCase/2, where TypeParam = unsigned int
	//[ RUN      ] DemoTestCase/2.Test
	//demo_file.cc:60: Failure
	//Failed
	//unsigned int type
	//[  FAILED  ] DemoTestCase/2.Test, where TypeParam = unsigned int (0 ms)
	//[----------] 1 test from DemoTestCase/2 (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 3 tests from 3 test cases ran. (0 ms total)
	//[  PASSED  ] 1 test.
	//[  FAILED  ] 2 tests, listed below:
	//[  FAILED  ] DemoTestCase/0.Test, where TypeParam = char
	//[  FAILED  ] DemoTestCase/2.Test, where TypeParam = unsigned int
	//
	// 2 FAILED TESTS
	public void testTypedTestsSupport() {
		mockModelUpdater.enterTestSuite("DemoTestCase/0(char)");
		mockModelUpdater.enterTestCase("Test");
		mockModelUpdater.addTestMessage("demo_file.cc", 60, ITestMessage.Level.Error, "Failed"+EOL+"char type");
		mockModelUpdater.setTestingTime(0);
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.enterTestSuite("DemoTestCase/1(int)");
		mockModelUpdater.enterTestCase("Test");
		mockModelUpdater.setTestingTime(0);
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
		mockModelUpdater.enterTestSuite("DemoTestCase/2(unsigned int)");
		mockModelUpdater.enterTestCase("Test");
		mockModelUpdater.addTestMessage("demo_file.cc", 60, ITestMessage.Level.Error, "Failed"+EOL+"unsigned int type");
		mockModelUpdater.setTestingTime(0);
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	
	//Running main() from gtest_main.cc
	//Unknown line in the output
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//Another unknown line in the output
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] DemoTestCase.DemoTest
	//Yet another unknown line in the output
	//[       OK ] DemoTestCase.DemoTest (0 ms)
	//[----------] 1 test from DemoTestCase (0 ms total)
	//One more unknown line in the output
	//
	//[----------] Global test environment tear-down
	//And one more unknown line in the output
	//[==========] 1 test from 1 test case ran. (0 ms total)
	//[  PASSED  ] 1 test.
	public void testAllUnrecognizedLinesShouldBeSkipped() {
		mockModelUpdater.enterTestSuite("DemoTestCase");
		mockModelUpdater.enterTestCase("DemoTest");
		mockModelUpdater.setTestingTime(0);
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}


	//
	public void testNoInput() {
		// NOTE: The comment above is left blank intentionally
		expectTestingException();
	}

	
	// This is not an input from a Google Test Module
	public void testAbsolutelyIncorrectInput() {
		expectTestingException();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] Not_A_DemoTestCase.DemoTest
	public void testUnexpectedOutputEnd() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		
		expectTestingException();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] Not_A_DemoTestCase.DemoTest
	//[       OK ] DemoTestCase.DemoTest (0 ms)
	//[----------] 1 test from DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 1 test from 1 test case ran. (0 ms total)
	//[  PASSED  ] 1 test.
	public void testTestSuiteNameMismatch1() {
		mockModelUpdater.skipCalls("enterTestSuite");
		
		expectTestingException();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] DemoTestCase.DemoTest
	//[       OK ] Not_A_DemoTestCase.DemoTest (0 ms)
	//[----------] 1 test from DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 1 test from 1 test case ran. (0 ms total)
	//[  PASSED  ] 1 test.
	public void testTestSuiteNameMismatch2() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		
		expectTestingException();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] DemoTestCase.DemoTest
	//[       OK ] DemoTestCase.DemoTest (0 ms)
	//[----------] 1 test from Not_A_DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 1 test from 1 test case ran. (0 ms total)
	//[  PASSED  ] 1 test.
	public void testTestSuiteNameMismatch3() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		mockModelUpdater.skipCalls("setTestingTime");
		mockModelUpdater.skipCalls("setTestStatus");
		mockModelUpdater.skipCalls("exitTestCase");
		
		expectTestingException();
	}

	
	//Running main() from gtest_main.cc
	//[==========] Running 1 test from 1 test case.
	//[----------] Global test environment set-up.
	//[----------] 1 test from DemoTestCase
	//[ RUN      ] DemoTestCase.DemoTest
	//[       OK ] DemoTestCase.NOT_A_DemoTest (0 ms)
	//[----------] 1 test from DemoTestCase (0 ms total)
	//
	//[----------] Global test environment tear-down
	//[==========] 1 test from 1 test case ran. (0 ms total)
	//[  PASSED  ] 1 test.
	public void testTestCaseNameMismatch() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		
		expectTestingException();
	}

}
