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
 *     Anton Gorenkov  - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.testsrunners;

import java.text.MessageFormat;

import org.eclipse.cdt.testsrunner.internal.qttest.QtTestsRunnerProvider;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;

/**
 * Test for {@see QtTestsRunner} class
 */
@SuppressWarnings("nls")
public class QtTestCase extends BaseTestCase {

	private static final String DEFAULT_LOCATION_FILE = "";
	private static final int DEFAULT_LOCATION_LINE = 0;

	@Override
	protected ITestsRunnerProvider createTestsRunner() {
		return new QtTestsRunnerProvider();
	}

	private void addStandardBenchmarkMessage(int value, String units, int iterations) {
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Info,
				MessageFormat.format("{0,number,#.####} {1} per iteration (total: {2}, iterations: {3})",
						((float) value) / iterations, units, value, iterations));
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testNoCustomTestCases() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testDemo">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testTheOnlyPassingCustomTestCase() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testDemo">
	//<Incident type="fail" file="qt_test_demo.cpp" line="6">
	//    <Description><![CDATA[Compared values are not the same
	//   Actual (1): 1
	//   Expected (2): 2]]></Description>
	//</Incident>
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testTheOnlyFailingCustomTestCase() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 6, ITestMessage.Level.FatalError,
				"Compared values are not the same\n   Actual (1): 1\n   Expected (2): 2");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testDemo">
	//<Incident type="fail" file="qtestcase.cpp" line="1675">
	//    <Description><![CDATA[Caught unhandled exception]]></Description>
	//</Incident>
	//</TestFunction>
	//</TestCase>
	public void testTheOnlyAbortedCustomTestCase() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo");
		mockModelUpdater.addTestMessage("qtestcase.cpp", 1675, ITestMessage.Level.FatalError,
				"Caught unhandled exception");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		// NOTE: Qt.Test does not run any other test cases after exception throwing in a test case
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testWarning">
	//<Message type="warn" file="" line="0">
	//    <Description><![CDATA[Test warning!]]></Description>
	//</Message>
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testFailure">
	//<Incident type="fail" file="qt_test_demo.cpp" line="41">
	//    <Description><![CDATA[Test fail!]]></Description>
	//</Incident>
	//</TestFunction>
	//<TestFunction name="testSkip">
	//<Message type="skip" file="qt_test_demo.cpp" line="47">
	//    <Description><![CDATA[Test skip!]]></Description>
	//</Message>
	//</TestFunction>
	//<TestFunction name="testExpectedFailWithContinue">
	//<Incident type="xfail" file="qt_test_demo.cpp" line="60">
	//    <Description><![CDATA[Will fix in the next release]]></Description>
	//</Incident>
	//<Incident type="fail" file="qt_test_demo.cpp" line="61">
	//    <Description><![CDATA[Failed!]]></Description>
	//</Incident>
	//</TestFunction>
	//<TestFunction name="testExpectedFailWithAbort">
	//<Incident type="xfail" file="qt_test_demo.cpp" line="68">
	//    <Description><![CDATA[Will fix in the next release]]></Description>
	//</Incident>
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testExpectedFailPassed">
	//<Incident type="xpass" file="qt_test_demo.cpp" line="70">
	//    <Description><![CDATA[COMPARE()]]></Description>
	//</Incident>
	//</TestFunction>
	//<TestFunction name="testUnknownIncidentType">
	//<Incident type="??????" file="qt_test_demo.cpp" line="72">
	//    <Description><![CDATA[Unknown incident test!]]></Description>
	//</Incident>
	//</TestFunction>
	//<TestFunction name="testUnknownMessageType">
	//<Message type="??????" file="qt_test_demo.cpp" line="80">
	//    <Description><![CDATA[Unknown message type test!]]></Description>
	//</Message>
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testDifferentMessageLevels() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testWarning");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Warning,
				"Test warning!");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testFailure");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 41, ITestMessage.Level.FatalError, "Test fail!");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testSkip");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 47, ITestMessage.Level.Info, "Test skip!");
		mockModelUpdater.setTestStatus(ITestItem.Status.Skipped);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testExpectedFailWithContinue");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 60, ITestMessage.Level.Error,
				"Will fix in the next release");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 61, ITestMessage.Level.FatalError, "Failed!");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testExpectedFailWithAbort");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 68, ITestMessage.Level.Error,
				"Will fix in the next release");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testExpectedFailPassed");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 70, ITestMessage.Level.Error, "COMPARE()");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testUnknownIncidentType");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 72, ITestMessage.Level.FatalError,
				"Unknown incident test!");
		mockModelUpdater.setTestStatus(ITestItem.Status.Aborted);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testUnknownMessageType");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 80, ITestMessage.Level.FatalError,
				"Unknown message type test!");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testDemo">
	//<BenchmarkResult metric="walltime" tag="" value="28" iterations="8192" />
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testBenchmarkBasicSupport() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo");
		addStandardBenchmarkMessage(28, "msec", 8192);
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testDemo">
	//<BenchmarkResult metric="callgrind" tag="locale aware compare" value="30" iterations="8192" />
	//<BenchmarkResult metric="callgrind" tag="standard compare" value="24" iterations="10485" />
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testBenchmarkWithDataTag() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo(locale aware compare)");
		addStandardBenchmarkMessage(30, "instr.", 8192);
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo(standard compare)");
		addStandardBenchmarkMessage(24, "instr.", 10485);
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testDemo">
	//<Incident type="fail" file="qt_test_demo.cpp" line="57">
	//    <DataTag><![CDATA[locale aware compare]]></DataTag>
	//    <Description><![CDATA[Failed!]]></Description>
	//</Incident>
	//<Incident type="fail" file="qt_test_demo.cpp" line="57">
	//    <DataTag><![CDATA[locale aware compare]]></DataTag>
	//    <Description><![CDATA[Failed!]]></Description>
	//</Incident>
	//<BenchmarkResult metric="cputicks" tag="locale aware compare" value="29" iterations="8192" />
	//<Incident type="fail" file="qt_test_demo.cpp" line="58">
	//    <DataTag><![CDATA[standard compare]]></DataTag>
	//    <Description><![CDATA[Failed!]]></Description>
	//</Incident>
	//<Incident type="fail" file="qt_test_demo.cpp" line="58">
	//    <DataTag><![CDATA[standard compare]]></DataTag>
	//    <Description><![CDATA[Failed!]]></Description>
	//</Incident>
	//<BenchmarkResult metric="cputicks" tag="standard compare" value="24" iterations="10485" />
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testBenchmarkMixedWithIncidentsWithDataTag() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo(locale aware compare)");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 57, ITestMessage.Level.FatalError, "Failed!");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 57, ITestMessage.Level.FatalError, "Failed!");
		addStandardBenchmarkMessage(29, "ticks", 8192);
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo(standard compare)");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 58, ITestMessage.Level.FatalError, "Failed!");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 58, ITestMessage.Level.FatalError, "Failed!");
		addStandardBenchmarkMessage(24, "ticks", 10485);
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testDemo">
	//<Incident type="fail" file="qt_test_demo.cpp" line="20">
	//    <DataTag><![CDATA[all lower]]></DataTag>
	//    <Description><![CDATA[Compared values are not the same
	//   Actual (string.toUpper()): HELLO
	//   Expected (result): HELLO2]]></Description>
	//</Incident>
	//<Incident type="fail" file="qt_test_demo.cpp" line="20">
	//    <DataTag><![CDATA[mixed]]></DataTag>
	//    <Description><![CDATA[Compared values are not the same
	//   Actual (string.toUpper()): HELLO
	//   Expected (result): HELLO3]]></Description>
	//</Incident>
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testIncidentWithDataTag() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo(all lower)");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 20, ITestMessage.Level.FatalError,
				"Compared values are not the same\n   Actual (string.toUpper()): HELLO\n   Expected (result): HELLO2");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo(mixed)");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 20, ITestMessage.Level.FatalError,
				"Compared values are not the same\n   Actual (string.toUpper()): HELLO\n   Expected (result): HELLO3");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testDemo">
	//<Message type="warn" file="" line="0">
	//    <DataTag><![CDATA[all lower]]></DataTag>
	//    <Description><![CDATA[hello]]></Description>
	//</Message>
	//<Incident type="fail" file="qt_test_demo.cpp" line="66">
	//    <DataTag><![CDATA[all lower]]></DataTag>
	//    <Description><![CDATA[HELLO2]]></Description>
	//</Incident>
	//<Message type="warn" file="" line="0">
	//    <DataTag><![CDATA[mixed]]></DataTag>
	//    <Description><![CDATA[Hello]]></Description>
	//</Message>
	//<Incident type="fail" file="qt_test_demo.cpp" line="66">
	//    <DataTag><![CDATA[mixed]]></DataTag>
	//    <Description><![CDATA[HELLO3]]></Description>
	//</Incident>
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testMessageWithDataTag() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo(all lower)");
		mockModelUpdater.addTestMessage("", 0, ITestMessage.Level.Warning, "hello");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 66, ITestMessage.Level.FatalError, "HELLO2");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testDemo(mixed)");
		mockModelUpdater.addTestMessage("", 0, ITestMessage.Level.Warning, "Hello");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 66, ITestMessage.Level.FatalError, "HELLO3");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testWithEmptyIncident">
	//<Incident type="fail" file="qt_test_demo.cpp" line="6">
	//    <Description><![CDATA[]]></Description>
	//</Incident>
	//</TestFunction>
	//<TestFunction name="testWithEmptyMessage">
	//<Message type="warn" file="" line="0">
	//    <Description><![CDATA[]]></Description>
	//</Message>
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testWithEmptyMessage() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testWithEmptyIncident");
		mockModelUpdater.addTestMessage("qt_test_demo.cpp", 6, ITestMessage.Level.FatalError, "");
		mockModelUpdater.setTestStatus(ITestItem.Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testWithEmptyMessage");
		mockModelUpdater.addTestMessage(DEFAULT_LOCATION_FILE, DEFAULT_LOCATION_LINE, ITestMessage.Level.Warning, "");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//<TestFunction name="testWithoutAnyIncidents">
	//</TestFunction>
	//<TestFunction name="cleanupTestCase">
	//<Incident type="pass" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testWithoutAnyIncidents() {
		mockModelUpdater.enterTestSuite("MainTS");
		mockModelUpdater.enterTestCase("initTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("testWithoutAnyIncidents");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("cleanupTestCase");
		mockModelUpdater.setTestStatus(ITestItem.Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.exitTestSuite();
	}

	//
	public void testNoInput() {
		// NOTE: The comment above is left blank intentionally
		expectTestingException();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2  <!-- QTestVersion is not closed -->
	//</Environment>
	//</TestCase>
	public void testBadFormedXml() {
		mockModelUpdater.skipCalls("enterTestSuite");
		expectTestingException();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	public void testUnexceptedXmlEnd() {
		mockModelUpdater.skipCalls("enterTestSuite");
		expectTestingException();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="some_wrong_value" file="" line="0" />
	//</TestFunction>
	//</TestCase>
	public void testBadIncidentTypeValue() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		expectTestingException();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Message type="some_wrong_value" file="" line="0">
	//    <Description><![CDATA[Test warning!]]></Description>
	//</Message>
	//</TestFunction>
	//</TestCase>
	public void testBadMessageTypeValue() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		expectTestingException();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Incident type="some_wrong_value" file="" line="<wrong_value>" />
	//</TestFunction>
	//</TestCase>
	public void testBadLineNumberValueOfIncident() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		expectTestingException();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="initTestCase">
	//<Message type="warn" file="" line="<wrong_value>">
	//    <Description><![CDATA[Test warning!]]></Description>
	//</Message>
	//</TestFunction>
	//</TestCase>
	public void testBadLineNumberValueOfMessage() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		expectTestingException();
	}

	//<?xml version="1.0" encoding="ISO-8859-1"?>
	//<TestCase name="MainTS">
	//<Environment>
	//    <QtVersion>4.6.2</QtVersion>
	//    <QTestVersion>4.6.2</QTestVersion>
	//</Environment>
	//<TestFunction name="testDemo">
	//<BenchmarkResult metric="<wrong_value>" tag="" value="28" iterations="8192" />
	//</TestFunction>
	//</TestCase>
	public void testBadBenchmarkMetricValue() {
		mockModelUpdater.skipCalls("enterTestSuite");
		mockModelUpdater.skipCalls("enterTestCase");
		expectTestingException();
	}

}
