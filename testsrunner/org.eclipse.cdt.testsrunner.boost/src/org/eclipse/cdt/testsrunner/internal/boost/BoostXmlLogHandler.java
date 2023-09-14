/*******************************************************************************
 * Copyright (c) 2011, 2023 Anton Gorenkov and others.
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
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.boost;

import java.text.MessageFormat;
import java.util.Stack;

import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses the Boost.Test XML log and notifies the Tests Runner Core about how
 * the testing process is going.
 */
public class BoostXmlLogHandler extends DefaultHandler {

	// Boost.Test XML log tags
	private static final String XML_NODE_TEST_LOG = "TestLog"; //$NON-NLS-1$
	private static final String XML_NODE_TEST_SUITE = "TestSuite"; //$NON-NLS-1$
	private static final String XML_NODE_TEST_CASE = "TestCase"; //$NON-NLS-1$
	private static final String XML_NODE_TESTING_TIME = "TestingTime"; //$NON-NLS-1$
	private static final String XML_NODE_LAST_CHECKPOINT = "LastCheckpoint"; //$NON-NLS-1$

	// Boost.Test XML log message levels representation
	private static final String XML_NODE_INFO = "Info"; //$NON-NLS-1$
	private static final String XML_NODE_MESSAGE = "Message"; //$NON-NLS-1$
	private static final String XML_NODE_WARNING = "Warning"; //$NON-NLS-1$
	private static final String XML_NODE_ERROR = "Error"; //$NON-NLS-1$
	private static final String XML_NODE_FATAL_ERROR = "FatalError"; //$NON-NLS-1$
	private static final String XML_NODE_EXCEPTION = "Exception"; //$NON-NLS-1$
	private static final String XML_NODE_CONTEXT = "Context"; //$NON-NLS-1$
	private static final String XML_NODE_FRAME = "Frame"; //$NON-NLS-1$

	// Boost.Test XML log attributes
	private static final String XML_ATTR_TEST_SUITE_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_TEST_CASE_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_MESSAGE_FILE = "file"; //$NON-NLS-1$
	private static final String XML_ATTR_MESSAGE_LINE = "line"; //$NON-NLS-1$

	/**
	 * The context can be of arbitrary length, to prevent excessively long strings
	 * in the tree limit the context to this length in the tree. The full context
	 * is available in the details tab.
	 */
	private static final int MAX_CONTEXT_LENGTH_IN_TREE = 50;

	/** The default file name for test message location. */
	private static final String DEFAULT_LOCATION_FILE = null;

	/** The default line number for test message location. */
	private static final int DEFAULT_LOCATION_LINE = -1;

	/** The interface to notify the Tests Runner Core */
	private ITestModelUpdater modelUpdater;

	/** Stores the text between XML tags. */
	private Stack<StringBuilder> elementDataStack = new Stack<>();

	/** File name for current test message location. */
	private String fileName;

	/** Line number for current test message location. */
	private int lineNumber;

	/** Current test case status. */
	private ITestItem.Status testStatus;

	/**
	 * Keep track of the last test case name so that we can handle
	 * parameterized test cases which have the same name
	 */
	private String lastTestCaseName = ""; //$NON-NLS-1$
	private static final int SAME_TEST_CASE_NAME_COUNT_START = 2;
	private int sameTestCaseNameCount = SAME_TEST_CASE_NAME_COUNT_START;
	private StringBuilder context = new StringBuilder();

	private boolean testCaseEnterDeferred = false;
	private StringBuilder testCaseName = new StringBuilder();

	BoostXmlLogHandler(ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs)
			throws SAXException {

		if (qName == null) {
			throw createAndLogExceptionForElement(qName);
		}

		elementDataStack.push(new StringBuilder());
		switch (qName) {
		case XML_NODE_TEST_SUITE:
			String testSuiteName = attrs.getValue(XML_ATTR_TEST_SUITE_NAME);
			modelUpdater.enterTestSuite(testSuiteName);
			break;

		case XML_NODE_TEST_CASE:
			testCaseName.setLength(0);
			testCaseName.append(attrs.getValue(XML_ATTR_TEST_CASE_NAME));

			if (lastTestCaseName.equals(testCaseName.toString())) {
				testCaseName.append(" (" + sameTestCaseNameCount + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				++sameTestCaseNameCount;
			} else {
				lastTestCaseName = testCaseName.toString();
				sameTestCaseNameCount = SAME_TEST_CASE_NAME_COUNT_START;
			}

			testCaseEnterDeferred = true;
			testStatus = Status.Passed;
			break;

		case XML_NODE_INFO:
		case XML_NODE_MESSAGE:
		case XML_NODE_WARNING:
		case XML_NODE_ERROR:
		case XML_NODE_FATAL_ERROR:
		case XML_NODE_LAST_CHECKPOINT:
			fileName = attrs.getValue(XML_ATTR_MESSAGE_FILE);
			String lineNumberStr = attrs.getValue(XML_ATTR_MESSAGE_LINE);
			lineNumber = lineNumberStr != null ? Integer.parseInt(lineNumberStr.trim()) : DEFAULT_LOCATION_LINE;
			break;

		case XML_NODE_EXCEPTION:
			fileName = DEFAULT_LOCATION_FILE;
			lineNumber = DEFAULT_LOCATION_LINE;
			break;

		case XML_NODE_CONTEXT:
		case XML_NODE_FRAME:
			/* handle in endElement */
			break;

		case XML_NODE_TESTING_TIME:
		case XML_NODE_TEST_LOG:
			/* just skip, do nothing */
			break;

		default:
			throw createAndLogExceptionForElement(qName);
		}
	}

	/**
	 * Common routing: notifies the Tests Runner core about new test message
	 * and resets the internal state.
	 *
	 * @param level test message level
	 */
	private void addCurrentMessage(ITestMessage.Level level) {
		String text = elementDataStack.peek().toString().trim();
		if (testCaseEnterDeferred) {
			if (!context.isEmpty()) {
				testCaseName.append(BoostTestsRunnerMessages.BoostXmlLogHandler_ContextPrefix);
				if (context.length() > MAX_CONTEXT_LENGTH_IN_TREE) {
					testCaseName.append(context.subSequence(0, MAX_CONTEXT_LENGTH_IN_TREE));
					testCaseName.append(BoostTestsRunnerMessages.BoostXmlLogHandler_ContextOverflow);
				} else {
					testCaseName.append(context);
				}
				testCaseName.append(BoostTestsRunnerMessages.BoostXmlLogHandler_ContextSuffix);
			}
			modelUpdater.enterTestCase(testCaseName.toString());
			testCaseEnterDeferred = false;
		}
		if (!context.isEmpty()) {
			text += BoostTestsRunnerMessages.BoostXmlLogHandler_ContextHeader + context.toString().trim();
			context.setLength(0);
		}
		modelUpdater.addTestMessage(fileName, lineNumber, level, text.trim());
		fileName = DEFAULT_LOCATION_FILE;
		lineNumber = DEFAULT_LOCATION_LINE;
		if (level == ITestMessage.Level.Error || level == ITestMessage.Level.FatalError) {
			if (testStatus != ITestItem.Status.Aborted) {
				testStatus = ITestItem.Status.Failed;
			}

		} else if (level == ITestMessage.Level.Exception) {
			testStatus = ITestItem.Status.Aborted;
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

		if (qName == null) {
			throw createAndLogExceptionForElement(qName);
		}
		switch (qName) {
		case XML_NODE_TEST_SUITE:
			modelUpdater.exitTestSuite();
			break;

		case XML_NODE_TEST_CASE:
			if (testCaseEnterDeferred) {
				modelUpdater.enterTestCase(testCaseName.toString());
				testCaseEnterDeferred = false;
			}
			modelUpdater.setTestStatus(testStatus);
			modelUpdater.exitTestCase();
			break;

		case XML_NODE_TESTING_TIME:
			modelUpdater.setTestingTime(Integer.parseInt(elementDataStack.peek().toString().trim()) / 1000);
			break;

		case XML_NODE_INFO:
			addCurrentMessage(ITestMessage.Level.Info);
			break;
		case XML_NODE_MESSAGE:
			addCurrentMessage(ITestMessage.Level.Message);
			break;
		case XML_NODE_WARNING:
			addCurrentMessage(ITestMessage.Level.Warning);
			break;
		case XML_NODE_ERROR:
			addCurrentMessage(ITestMessage.Level.Error);
			break;
		case XML_NODE_FATAL_ERROR:
			addCurrentMessage(ITestMessage.Level.FatalError);
			break;

		case XML_NODE_EXCEPTION:
			if (fileName != DEFAULT_LOCATION_FILE && !fileName.isEmpty() && lineNumber >= 0) {
				StringBuilder current = elementDataStack.peek();
				String trimmed = current.toString().trim();
				current.setLength(0);
				current.append(trimmed);
				current.append(BoostTestsRunnerMessages.BoostXmlLogHandler_exception_suffix);
			}
			addCurrentMessage(ITestMessage.Level.Exception);
			break;

		case XML_NODE_CONTEXT:
			context.insert(0, elementDataStack.peek().toString().trim());
			break;
		case XML_NODE_FRAME:
			context.append(elementDataStack.peek().toString().trim());
			break;

		case XML_NODE_TEST_LOG:
		case XML_NODE_LAST_CHECKPOINT:
			/* just skip, do nothing */
			break;

		default:
			throw createAndLogExceptionForElement(qName);
		}
		elementDataStack.pop();
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		StringBuilder builder = elementDataStack.peek();
		for (int i = start; i < start + length; i++) {
			builder.append(ch[i]);
		}
	}

	/**
	 * Throws the testing exception for the specified XML tag.
	 *
	 * @param tagName XML tag name
	 * @return SAXException the exception that will be thrown
	 */
	private SAXException createAndLogExceptionForElement(String tagName) {
		SAXException e = new SAXException(
				MessageFormat.format(BoostTestsRunnerMessages.BoostXmlLogHandler_wrong_tag_name, tagName));
		BoostTestsRunnerPlugin.log(e);
		return e;
	}

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		BoostTestsRunnerPlugin.log(ex);
	}

	@Override
	public void error(SAXParseException ex) throws SAXException {
		BoostTestsRunnerPlugin.log(ex);
		throw ex;
	}

	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		BoostTestsRunnerPlugin.log(ex);
		throw ex;
	}

}
