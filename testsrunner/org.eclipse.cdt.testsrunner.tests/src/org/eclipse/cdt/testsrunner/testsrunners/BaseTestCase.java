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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.eclipse.cdt.testsrunner.test.TestsRunnerTestActivator;
import org.eclipse.core.runtime.Plugin;

import junit.framework.TestCase;

/**
 * Base test case for Tests Runner provider plug-ins testing.
 */
@SuppressWarnings("nls")
public abstract class BaseTestCase extends TestCase {

	protected static final String EOL = System.getProperty("line.separator");

	protected MockTestModelUpdater mockModelUpdater = new MockTestModelUpdater();
	protected ITestsRunnerProvider testsRunner = createTestsRunner();
	protected boolean expectTestingException = false;

	protected abstract ITestsRunnerProvider createTestsRunner();

	protected String getAboveComment() {
		return getContents(1)[0].toString();
	}

	private StringBuilder[] getContents(int sections) {
		try {
			return TestSourceReader.getContentsForTest(getPlugin().getBundle(), getSourcePrefix(), getClass(),
					getName(), sections);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

	protected Plugin getPlugin() {
		return TestsRunnerTestActivator.getDefault();
	}

	protected String getSourcePrefix() {
		return "src";
	}

	protected void runTestsRunner() throws TestingException {
		try {
			mockModelUpdater.replay();
			String inputString = getAboveComment();
			InputStream inStream = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
			boolean testingExceptionHappen = false;
			String exceptionMessage = null;
			try {
				testsRunner.run(mockModelUpdater, inStream);

			} catch (TestingException e) {
				testingExceptionHappen = true;
				exceptionMessage = e.getMessage();
			}
			if (expectTestingException != testingExceptionHappen) {
				if (testingExceptionHappen) {
					fail("Unexpected exception: " + exceptionMessage);
				} else {
					fail("TestingException is expected, but did not happen!");
				}
			}

		} catch (UnsupportedEncodingException e) {
			fail(e.getMessage());
		}
	}

	protected void expectTestingException() {
		expectTestingException = true;
	}

	@Override
	protected void tearDown() throws Exception {
		runTestsRunner();
	}

}
