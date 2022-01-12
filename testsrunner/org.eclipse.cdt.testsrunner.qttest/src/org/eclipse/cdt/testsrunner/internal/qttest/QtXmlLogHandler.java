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
package org.eclipse.cdt.testsrunner.internal.qttest;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses the Qt Test XML log and notifies the Tests Runner Core about how the
 * testing process is going.
 *
 * @note There is a terminology conflict between Qt Test library and Test
 * Runner. Qt Test's "test case" is a "test suite" in Test Runner's terminology,
 * Qt's "test function" is a "test case", the "incident" and "message" are
 * "test messages". Be careful with it!
 */
public class QtXmlLogHandler extends DefaultHandler {

	// Qt Test XML log tags
	private static final String XML_NODE_TEST_CASE = "TestCase"; //$NON-NLS-1$
	private static final String XML_NODE_TEST_FUNCTION = "TestFunction"; //$NON-NLS-1$
	private static final String XML_NODE_INCIDENT = "Incident"; //$NON-NLS-1$
	private static final String XML_NODE_MESSAGE = "Message"; //$NON-NLS-1$
	private static final String XML_NODE_DESCRIPTION = "Description"; //$NON-NLS-1$
	private static final String XML_NODE_ENVIRONMENT = "Environment"; //$NON-NLS-1$
	private static final String XML_NODE_QTVERSION = "QtVersion"; //$NON-NLS-1$
	private static final String XML_NODE_QTBUILD = "QtBuild"; //$NON-NLS-1$
	private static final String XML_NODE_QTESTVERSION = "QTestVersion"; //$NON-NLS-1$
	private static final String XML_NODE_BENCHMARK = "BenchmarkResult"; //$NON-NLS-1$
	private static final String XML_NODE_DATATAG = "DataTag"; //$NON-NLS-1$
	private static final String XML_NODE_DURATION = "Duration"; //$NON-NLS-1$

	// Qt Test XML case statuses representation
	private static final String XML_VALUE_INCIDENT_PASS = "pass"; //$NON-NLS-1$
	private static final String XML_VALUE_INCIDENT_XFAIL = "xfail"; //$NON-NLS-1$
	private static final String XML_VALUE_INCIDENT_FAIL = "fail"; //$NON-NLS-1$
	private static final String XML_VALUE_INCIDENT_XPASS = "xpass"; //$NON-NLS-1$
	private static final String XML_VALUE_INCIDENT_UNKNOWN = "??????"; //$NON-NLS-1$

	// Qt Test XML log message levels representation
	private static final String XML_VALUE_MESSAGE_WARN = "warn"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_SYSTEM = "system"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_QDEBUG = "qdebug"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_QWARN = "qwarn"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_QFATAL = "qfatal"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_SKIP = "skip"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_INFO = "info"; //$NON-NLS-1$
	private static final String XML_VALUE_MESSAGE_UNKNOWN = "??????"; //$NON-NLS-1$

	// Qt Test XML log attributes
	private static final String XML_ATTR_TEST_CASE_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_TEST_FUNCTION_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_MSECS = "msecs"; //$NON-NLS-1$
	private static final String XML_ATTR_TYPE = "type"; //$NON-NLS-1$
	private static final String XML_ATTR_FILE = "file"; //$NON-NLS-1$
	private static final String XML_ATTR_LINE = "line"; //$NON-NLS-1$
	private static final String XML_ATTR_BENCHMARK_METRIC = "metric"; //$NON-NLS-1$
	private static final String XML_ATTR_BENCHMARK_VALUE = "value"; //$NON-NLS-1$
	private static final String XML_ATTR_BENCHMARK_ITERATIONS = "iterations"; //$NON-NLS-1$
	private static final String XML_ATTR_DATA_TAG = "tag"; //$NON-NLS-1$

	/** Maps the string message level representation to the Tests Runner internal enum code. */
	private static final Map<String, ITestMessage.Level> STRING_TO_MESSAGE_LEVEL;
	static {
		Map<String, ITestMessage.Level> aMap = new HashMap<>();
		aMap.put(XML_VALUE_MESSAGE_WARN, ITestMessage.Level.Warning);
		aMap.put(XML_VALUE_MESSAGE_SYSTEM, ITestMessage.Level.Message);
		aMap.put(XML_VALUE_MESSAGE_QDEBUG, ITestMessage.Level.Message);
		aMap.put(XML_VALUE_MESSAGE_QWARN, ITestMessage.Level.Warning);
		aMap.put(XML_VALUE_MESSAGE_QFATAL, ITestMessage.Level.FatalError);
		aMap.put(XML_VALUE_MESSAGE_SKIP, ITestMessage.Level.Info);
		aMap.put(XML_VALUE_MESSAGE_INFO, ITestMessage.Level.Info);
		aMap.put(XML_VALUE_MESSAGE_UNKNOWN, ITestMessage.Level.FatalError);
		// NOTE: Exception node is processed separately
		STRING_TO_MESSAGE_LEVEL = Collections.unmodifiableMap(aMap);
	}

	/** Maps the string incident status representation to the test case status. */
	private static final Map<String, ITestCase.Status> STRING_TO_TEST_STATUS;
	static {
		Map<String, ITestCase.Status> aMap = new HashMap<>();
		aMap.put(XML_VALUE_INCIDENT_PASS, ITestCase.Status.Passed);
		aMap.put(XML_VALUE_INCIDENT_XFAIL, ITestCase.Status.Failed);
		aMap.put(XML_VALUE_INCIDENT_FAIL, ITestCase.Status.Failed);
		aMap.put(XML_VALUE_INCIDENT_XPASS, ITestCase.Status.Failed);
		aMap.put(XML_VALUE_INCIDENT_UNKNOWN, ITestCase.Status.Aborted);
		// NOTE: Exception node is processed separately
		STRING_TO_TEST_STATUS = Collections.unmodifiableMap(aMap);
	}

	/** Maps the string incident status representation to the test message level to log about it. */
	private static final Map<String, ITestMessage.Level> STRING_INCIDENT_TO_MESSAGE_LEVEL;
	static {
		Map<String, ITestMessage.Level> aMap = new HashMap<>();
		aMap.put(XML_VALUE_INCIDENT_PASS, ITestMessage.Level.Info);
		aMap.put(XML_VALUE_INCIDENT_XFAIL, ITestMessage.Level.Error);
		aMap.put(XML_VALUE_INCIDENT_FAIL, ITestMessage.Level.FatalError);
		aMap.put(XML_VALUE_INCIDENT_XPASS, ITestMessage.Level.Error);
		aMap.put(XML_VALUE_INCIDENT_UNKNOWN, ITestMessage.Level.FatalError);
		// NOTE: Exception node is processed separately
		STRING_INCIDENT_TO_MESSAGE_LEVEL = Collections.unmodifiableMap(aMap);
	}

	/** Maps the metrics unit ids to the user readable names. */
	private static final Map<String, String> XML_METRICS_TO_UNIT_NAME;
	static {
		Map<String, String> aMap = new HashMap<>();
		aMap.put("events", QtTestsRunnerMessages.QtXmlLogHandler_metrics_unit_events); //$NON-NLS-1$
		aMap.put("callgrind", QtTestsRunnerMessages.QtXmlLogHandler_metrics_unit_instructions); //$NON-NLS-1$
		aMap.put("walltime", QtTestsRunnerMessages.QtXmlLogHandler_metrics_unit_msec); //$NON-NLS-1$
		aMap.put("cputicks", QtTestsRunnerMessages.QtXmlLogHandler_metrics_unit_ticks); //$NON-NLS-1$
		// NOTE: Exception node is processed separately
		XML_METRICS_TO_UNIT_NAME = Collections.unmodifiableMap(aMap);
	}

	/** The interface to notify the Tests Runner Core */
	private ITestModelUpdater modelUpdater;

	/** Stores the text between current XML tag. */
	private String elementData;

	/** Stores the text for currently parsed test message. */
	private String messageText;

	/** Stores the file name part of location for currently parsed test message. */
	private String fileName;

	/** Stores the line number part of location for currently parsed test message. */
	private int lineNumber;

	/** Stores the message level for currently parsed test message. */
	private ITestMessage.Level messageLevel;

	/** Stores the duration in msecs for currently parsed test function. */
	private int duration;

	/** Stores the status for currently parsed test case. */
	private ITestItem.Status testCaseStatus;

	/** Stores the name for currently parsed test case. */
	private String testCaseName;

	/** Stores the currently parsing data tag. */
	private String currentDataTag;

	/** Stores the last parsed data tag. */
	private String lastDataTag;

	/** Stores whether the test case was already added (means Tests Runner Core notified). */
	private boolean testCaseAdded;

	QtXmlLogHandler(ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
	}

	/**
	 * Notifies about test case exiting (if it was entered).
	 */
	private void exitTestCaseIfNecessary() {
		if (testCaseAdded) {
			modelUpdater.setTestStatus(testCaseStatus);
			modelUpdater.exitTestCase();
			testCaseAdded = false;
		}
	}

	/**
	 * Creates a new test case if a new data tag is met.
	 */
	private void createTestCaseIfNecessary() {
		if (!lastDataTag.equals(currentDataTag)) {
			exitTestCaseIfNecessary();
			currentDataTag = lastDataTag;
			String suffix = !currentDataTag.isEmpty()
					? MessageFormat.format(QtTestsRunnerMessages.QtXmlLogHandler_datatag_format, currentDataTag)
					: ""; //$NON-NLS-1$
			modelUpdater.enterTestCase(testCaseName + suffix);
			testCaseAdded = true;
		}
	}

	/**
	 * Adds a new test message if there is a text for it.
	 */
	private void addTestMessageIfNecessary() {
		if (messageText != null) {
			modelUpdater.addTestMessage(fileName, lineNumber, messageLevel, messageText);
		}
	}

	/**
	 * Sets a new status for the currently parsing test case.
	 *
	 * @param newStatus new test status
	 *
	 * @note Passed status is set by default and should not be set explicitly.
	 * But in case of errors it should not override Failed or Skipped statuses.
	 */
	private void setCurrentTestCaseStatus(ITestItem.Status newStatus) {
		// Passed status is set by default and should not be set explicitly.
		// But in case of errors it should not override Failed or Skipped statuses.
		if (newStatus != ITestItem.Status.Passed) {
			testCaseStatus = newStatus;
		}
	}

	/**
	 * Converts the metric unit ids to user readable names.
	 *
	 * @param benchmarkMetric metric unit id
	 * @return user readable name
	 * @throws SAXException if metric unit id is not known
	 */
	private String getUnitsByBenchmarkMetric(String benchmarkMetric) throws SAXException {
		String units = XML_METRICS_TO_UNIT_NAME.get(benchmarkMetric);
		if (units == null) {
			logAndThrowError(MessageFormat.format(QtTestsRunnerMessages.QtXmlLogHandler_unknown_benchmarck_metric,
					benchmarkMetric));
		}
		return units;
	}

	/**
	 * Converts the message level string to the internal enumeration core.
	 *
	 * @param map map to use
	 * @param incidentTypeStr message level string
	 * @return message level code
	 * @throws SAXException if message level string is not known
	 */
	private ITestMessage.Level getMessageLevel(Map<String, ITestMessage.Level> map, String incidentTypeStr)
			throws SAXException {
		Level result = map.get(incidentTypeStr);
		if (result == null) {
			logAndThrowError(
					MessageFormat.format(QtTestsRunnerMessages.QtXmlLogHandler_unknown_message_level, incidentTypeStr));
		}
		return result;
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs)
			throws SAXException {

		elementData = null;
		if (qName == XML_NODE_TEST_CASE) {
			// NOTE: Terminology mapping: Qt Test Case is actually a Test Suite
			String testSuiteName = attrs.getValue(XML_ATTR_TEST_CASE_NAME);
			modelUpdater.enterTestSuite(testSuiteName);

		} else if (qName == XML_NODE_TEST_FUNCTION) {
			// NOTE: Terminology mapping: Qt Test Function is actually a Test Case
			testCaseName = attrs.getValue(XML_ATTR_TEST_FUNCTION_NAME);
			currentDataTag = null;
			lastDataTag = ""; //$NON-NLS-1$
			testCaseAdded = false;
			testCaseStatus = ITestItem.Status.Passed;
			duration = 0;

		} else if (qName == XML_NODE_MESSAGE) {
			String messageLevelStr = attrs.getValue(XML_ATTR_TYPE);
			fileName = attrs.getValue(XML_ATTR_FILE);
			lineNumber = Integer.parseInt(attrs.getValue(XML_ATTR_LINE).trim());
			messageLevel = getMessageLevel(STRING_TO_MESSAGE_LEVEL, messageLevelStr);
			messageText = null;
			if (messageLevelStr.equals(XML_VALUE_MESSAGE_SKIP)) {
				setCurrentTestCaseStatus(ITestCase.Status.Skipped);
			}

		} else if (qName == XML_NODE_INCIDENT) {
			String strType = attrs.getValue(XML_ATTR_TYPE);
			fileName = attrs.getValue(XML_ATTR_FILE);
			lineNumber = Integer.parseInt(attrs.getValue(XML_ATTR_LINE).trim());
			messageLevel = getMessageLevel(STRING_INCIDENT_TO_MESSAGE_LEVEL, strType);
			messageText = null;
			setCurrentTestCaseStatus(STRING_TO_TEST_STATUS.get(strType));
			duration = 0;

		} else if (qName == XML_NODE_BENCHMARK) {
			lastDataTag = attrs.getValue(XML_ATTR_DATA_TAG);
			createTestCaseIfNecessary();
			int benchmarkResultIteratations = Integer.parseInt(attrs.getValue(XML_ATTR_BENCHMARK_ITERATIONS).trim());
			float benchmarkResultValue = Integer.parseInt(attrs.getValue(XML_ATTR_BENCHMARK_VALUE).trim());
			String units = getUnitsByBenchmarkMetric(attrs.getValue(XML_ATTR_BENCHMARK_METRIC).trim());
			modelUpdater.addTestMessage("", 0, ITestMessage.Level.Info, //$NON-NLS-1$
					MessageFormat.format(QtTestsRunnerMessages.QtXmlLogHandler_benchmark_result_message,
							benchmarkResultValue / benchmarkResultIteratations, units, benchmarkResultValue,
							benchmarkResultIteratations));

		} else if (qName == XML_NODE_DURATION) {
			float msecs = Float.parseFloat(attrs.getValue(XML_ATTR_MSECS));
			duration = Math.round(msecs);

		} else if (qName == XML_NODE_DATATAG) {
			lastDataTag = ""; //$NON-NLS-1$

		} else if (qName == XML_NODE_DESCRIPTION || qName == XML_NODE_ENVIRONMENT || qName == XML_NODE_QTVERSION
				|| qName == XML_NODE_QTBUILD || qName == XML_NODE_QTESTVERSION) {
			/* just skip, do nothing */

		} else {
			logUnknownTag(qName);
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

		if (qName == XML_NODE_TEST_CASE) {
			modelUpdater.exitTestSuite();

		} else if (qName == XML_NODE_TEST_FUNCTION) {
			createTestCaseIfNecessary();
			exitTestCaseIfNecessary();
			if (duration != 0) {
				modelUpdater.setTestingTime(duration);
			}

		} else if (qName == XML_NODE_DATATAG) {
			lastDataTag = elementData;

		} else if (qName == XML_NODE_INCIDENT) {
			createTestCaseIfNecessary();
			addTestMessageIfNecessary();

		} else if (qName == XML_NODE_MESSAGE) {
			createTestCaseIfNecessary();
			addTestMessageIfNecessary();

		} else if (qName == XML_NODE_DESCRIPTION) {
			messageText = elementData == null || elementData.isEmpty() ? "" : elementData; //$NON-NLS-1$

		} else if (qName == XML_NODE_ENVIRONMENT || qName == XML_NODE_QTVERSION || qName == XML_NODE_QTESTVERSION
				|| qName == XML_NODE_QTBUILD || qName == XML_NODE_DURATION || qName == XML_NODE_BENCHMARK) {
			/* just skip, do nothing */

		} else {
			logUnknownTag(qName);
		}
		elementData = null;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < start + length; i++) {
			sb.append(ch[i]);
		}
		elementData = sb.toString();
	}

	/**
	 * Throws the testing exception for the specified XML tag.
	 *
	 * @param tagName XML tag name
	 * @throws SAXException the exception that will be thrown
	 */
	@SuppressWarnings("unused")
	private void logAndThrowErrorForElement(String tagName) throws SAXException {
		logAndThrowError(MessageFormat.format(QtTestsRunnerMessages.QtXmlLogHandler_wrong_tag_name, tagName));
	}

	/**
	 * Logs a message regarding an unknown XML tag.
	 *
	 * @param tagName XML tag name
	 */
	private void logUnknownTag(String tagName) {
		QtTestsRunnerPlugin.log(
				new SAXException(MessageFormat.format(QtTestsRunnerMessages.QtXmlLogHandler_wrong_tag_name, tagName)));
	}

	/**
	 * Throws the testing exception with the specified message.
	 *
	 * @param message the reason
	 * @throws SAXException the exception that will be thrown
	 */
	private void logAndThrowError(String message) throws SAXException {
		SAXException e = new SAXException(message);
		QtTestsRunnerPlugin.log(e);
		throw e;
	}

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		QtTestsRunnerPlugin.log(ex);
	}

	@Override
	public void error(SAXParseException ex) throws SAXException {
		QtTestsRunnerPlugin.log(ex);
		throw ex;
	}

	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		QtTestsRunnerPlugin.log(ex);
		throw ex;
	}

}
