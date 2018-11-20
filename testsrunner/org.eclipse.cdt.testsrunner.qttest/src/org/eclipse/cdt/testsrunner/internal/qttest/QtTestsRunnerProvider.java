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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.xml.sax.SAXException;

/**
 * The Tests Runner provider plug-in to run tests with Qt Test library.
 *
 * Configures the test module to output in XML format, parses the output and
 * provides the data for the Tests Runner Plug-in.
 */
public class QtTestsRunnerProvider implements ITestsRunnerProvider {

	/**
	 * Checks whether the specified path is "special" one ("initTestCase" or
	 * "cleanupTestCase").
	 *
	 * @param testPath test path to check
	 * @return true if the path is special and false otherwise
	 */
	private boolean isSpecialTestPath(String[] testPath) {
		// Root test suite should not be explicitly specified for rerun
		if (testPath.length <= 1) {
			return true;
		}
		// "initTestCase" & "cleanupTestCase" are special test case names and they should be skipped too
		String testName = testPath[testPath.length - 1];
		return testName.equals("initTestCase") || testName.equals("cleanupTestCase"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the count of not "special" test paths.
	 *
	 * @param testPaths test paths array
	 * @return the count
	 * @see isSpecialTestPath()
	 */
	private int getNonSpecialTestsCount(String[][] testPaths) {
		int result = 0;
		for (int i = 0; i < testPaths.length; i++) {
			String[] testPath = testPaths[i];
			result += isSpecialTestPath(testPath) ? 0 : 1;
		}
		return result;
	}

	@Override
	public String[] getAdditionalLaunchParameters(String[][] testPaths) throws TestingException {
		final String[] qtParameters = { "-xml", //$NON-NLS-1$
		};
		String[] result = qtParameters;

		if (testPaths != null) {
			int testPathsLength = getNonSpecialTestsCount(testPaths);
			// If there are only special test cases specified
			if ((testPathsLength == 0) != (testPaths.length == 0)) {
				throw new TestingException(QtTestsRunnerMessages.QtTestsRunner_no_test_cases_to_rerun);
			}

			// Build tests filter
			if (testPathsLength >= 1) {
				result = new String[qtParameters.length + testPathsLength];
				System.arraycopy(qtParameters, 0, result, 0, qtParameters.length);
				int resultIdx = qtParameters.length;
				for (int i = 0; i < testPaths.length; i++) {
					String[] testPath = testPaths[i];
					if (!isSpecialTestPath(testPath)) {
						result[resultIdx] = testPath[testPath.length - 1];
						resultIdx++;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Construct the error message from prefix and detailed description.
	 *
	 * @param prefix prefix
	 * @param description detailed description
	 * @return the full message
	 */
	private String getErrorText(String prefix, String description) {
		return MessageFormat.format(QtTestsRunnerMessages.QtTestsRunner_error_format, prefix, description);
	}

	@Override
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) throws TestingException {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(inputStream, new QtXmlLogHandler(modelUpdater));

		} catch (IOException e) {
			throw new TestingException(
					getErrorText(QtTestsRunnerMessages.QtTestsRunner_io_error_prefix, e.getLocalizedMessage()));

		} catch (ParserConfigurationException e) {
			throw new TestingException(
					getErrorText(QtTestsRunnerMessages.QtTestsRunner_xml_error_prefix, e.getLocalizedMessage()));

		} catch (SAXException e) {
			throw new TestingException(
					getErrorText(QtTestsRunnerMessages.QtTestsRunner_xml_error_prefix, e.getLocalizedMessage()));
		}

	}

}
