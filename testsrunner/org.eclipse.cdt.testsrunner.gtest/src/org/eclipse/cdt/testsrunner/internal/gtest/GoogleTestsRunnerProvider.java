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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;

/**
 * The Tests Runner provider plug-in to run tests with Google Testing framework.
 *
 * Parses the text test module to output and provides the data for the Tests
 * Runner Plug-In.
 */
public class GoogleTestsRunnerProvider implements ITestsRunnerProvider {

	private static final String TEST_PATHS_DELIMITED = ":"; //$NON-NLS-1$
	private static final String TEST_PATH_PARTS_DELIMITED = "."; //$NON-NLS-1$
	private static final String ALL_TESTS = ".*"; //$NON-NLS-1$

	@Override
	public String[] getAdditionalLaunchParameters(String[][] testPaths) {
		final String[] gtestParameters = { "--gtest_repeat=1", //$NON-NLS-1$
				"--gtest_print_time=1", //$NON-NLS-1$
				"--gtest_color=no", //$NON-NLS-1$
		};
		String[] result = gtestParameters;

		// Build tests filter
		if (testPaths != null && testPaths.length >= 1) {
			StringBuilder sb = new StringBuilder("--gtest_filter="); //$NON-NLS-1$
			boolean needTestPathDelimiter = false;
			for (String[] testPath : testPaths) {
				if (needTestPathDelimiter) {
					sb.append(TEST_PATHS_DELIMITED);
				} else {
					needTestPathDelimiter = true;
				}
				boolean needTestPathPartDelimiter = false;
				for (String testPathPart : testPath) {
					if (needTestPathPartDelimiter) {
						sb.append(TEST_PATH_PARTS_DELIMITED);
					} else {
						needTestPathPartDelimiter = true;
					}
					sb.append(testPathPart);
				}
				// If it is a test suite
				if (testPath.length <= 1) {
					sb.append(ALL_TESTS);
				}
			}
			result = new String[gtestParameters.length + 1];
			System.arraycopy(gtestParameters, 0, result, 0, gtestParameters.length);
			result[gtestParameters.length] = sb.toString();
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
		return MessageFormat.format(GoogleTestsRunnerMessages.GoogleTestsRunner_error_format, prefix, description);
	}

	@Override
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) throws TestingException {

		try {
			OutputHandler ouputHandler = new OutputHandler(modelUpdater);
			ouputHandler.run(inputStream);
		} catch (IOException e) {
			throw new TestingException(
					getErrorText(GoogleTestsRunnerMessages.GoogleTestsRunner_io_error_prefix, e.getLocalizedMessage()));
		}
	}

}
