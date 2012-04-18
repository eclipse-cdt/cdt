/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.boost;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
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
	
	// Boost.Test XML log attributes
	private static final String XML_ATTR_TEST_SUITE_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_TEST_CASE_NAME = "name"; //$NON-NLS-1$
	private static final String XML_ATTR_MESSAGE_FILE = "file"; //$NON-NLS-1$
	private static final String XML_ATTR_MESSAGE_LINE = "line"; //$NON-NLS-1$

	/** Maps the string message level representation to the Tests Runner internal enum code. */
    private static final Map<String, ITestMessage.Level> STRING_TO_MESSAGE_LEVEL;
    static {
        Map<String, ITestMessage.Level> aMap = new HashMap<String, ITestMessage.Level>();
        aMap.put(XML_NODE_INFO, ITestMessage.Level.Info);
        aMap.put(XML_NODE_MESSAGE, ITestMessage.Level.Message);
        aMap.put(XML_NODE_WARNING, ITestMessage.Level.Warning);
        aMap.put(XML_NODE_ERROR, ITestMessage.Level.Error);
        aMap.put(XML_NODE_FATAL_ERROR, ITestMessage.Level.FatalError);
        // NOTE: Exception node is processed separately
        STRING_TO_MESSAGE_LEVEL = Collections.unmodifiableMap(aMap);
    }

    /** The default file name for test message location. */
	private static final String DEFAULT_LOCATION_FILE = null;

	/** The default line number for test message location. */
	private static final int DEFAULT_LOCATION_LINE = -1;
    
	/** The interface to notify the Tests Runner Core */
	private ITestModelUpdater modelUpdater;
	
	/** Stores the text between XML tags. */
	private Stack<StringBuilder> elementDataStack = new Stack<StringBuilder>();
	
	/** File name for current test message location. */
	private String fileName;

	/** Line number for current test message location. */
	private int lineNumber;

	/** Current test case status. */
	private ITestItem.Status testStatus;
	
	
	BoostXmlLogHandler(ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
	}
	
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
		
		elementDataStack.push(new StringBuilder());
		if (qName == XML_NODE_TEST_SUITE) {
			String testSuiteName = attrs.getValue(XML_ATTR_TEST_SUITE_NAME);
			modelUpdater.enterTestSuite(testSuiteName);

		} else if (qName == XML_NODE_TEST_CASE) {
			String testCaseName = attrs.getValue(XML_ATTR_TEST_CASE_NAME);
			modelUpdater.enterTestCase(testCaseName);
			testStatus = Status.Passed;

		} else if (STRING_TO_MESSAGE_LEVEL.containsKey(qName)
				|| qName == XML_NODE_LAST_CHECKPOINT) {
			fileName = attrs.getValue(XML_ATTR_MESSAGE_FILE);
			String lineNumberStr = attrs.getValue(XML_ATTR_MESSAGE_LINE);
			lineNumber = lineNumberStr != null ? Integer.parseInt(lineNumberStr.trim()) : DEFAULT_LOCATION_LINE;
			
		} else if (qName == XML_NODE_EXCEPTION) {
			fileName = DEFAULT_LOCATION_FILE;
			lineNumber = DEFAULT_LOCATION_LINE;

		} else if (qName == XML_NODE_TESTING_TIME ) {
			
		} else if (qName == XML_NODE_TEST_LOG) {
			/* just skip, do nothing */
			
		} else {
			logAndThrowErrorForElement(qName);
		}
	}
	
	/**
	 * Common routing: notifies the Tests Runner core about new test message
	 * and resets the internal state.
	 * 
	 * @param level test message level
	 */
	private void addCurrentMessage(ITestMessage.Level level) {
		modelUpdater.addTestMessage(fileName, lineNumber, level, elementDataStack.peek().toString());
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

		if (qName == XML_NODE_TEST_SUITE) {
			modelUpdater.exitTestSuite();

		} else if (qName == XML_NODE_TEST_CASE) {
			modelUpdater.setTestStatus(testStatus);
			modelUpdater.exitTestCase();
		
		} else if (qName == XML_NODE_TESTING_TIME) {
			modelUpdater.setTestingTime(Integer.parseInt(elementDataStack.peek().toString().trim())/1000);

		} else if (STRING_TO_MESSAGE_LEVEL.containsKey(qName)) {
			addCurrentMessage(STRING_TO_MESSAGE_LEVEL.get(qName));

		} else if (qName == XML_NODE_EXCEPTION) {
			if (fileName != DEFAULT_LOCATION_FILE && !fileName.isEmpty() && lineNumber >= 0) {
				elementDataStack.peek().append(BoostTestsRunnerMessages.BoostXmlLogHandler_exception_suffix);
			}
			addCurrentMessage(ITestMessage.Level.Exception);

		} else if (qName == XML_NODE_TEST_LOG || qName == XML_NODE_LAST_CHECKPOINT) {
			/* just skip, do nothing */
			
		} else {
			logAndThrowErrorForElement(qName);
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
	 * @throws SAXException the exception that will be thrown
	 */
	private void logAndThrowErrorForElement(String tagName) throws SAXException {
		logAndThrowError(
			MessageFormat.format(BoostTestsRunnerMessages.BoostXmlLogHandler_wrong_tag_name, tagName)
		);
	}
	
	/**
	 * Throws the testing exception with the specified message.
	 * 
	 * @param message the reason
	 * @throws SAXException the exception that will be thrown
	 */
	private void logAndThrowError(String message) throws SAXException {
		SAXException e = new SAXException(message);
		BoostTestsRunnerPlugin.log(e);
		throw e;
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
