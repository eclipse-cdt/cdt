/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.unittest.launcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.testsrunner.internal.launcher.ITestsLaunchConfigurationConstants;
import org.eclipse.cdt.unittest.CDTUnitTestPlugin;
import org.eclipse.cdt.unittest.ui.OpenEditorAtLineAction;
import org.eclipse.cdt.unittest.ui.OpenTestAction;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.text.StringMatcher;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.unittest.launcher.ITestRunnerClient;
import org.eclipse.unittest.model.ITestCaseElement;
import org.eclipse.unittest.model.ITestElement;
import org.eclipse.unittest.model.ITestRunSession;
import org.eclipse.unittest.model.ITestSuiteElement;
import org.eclipse.unittest.ui.ITestViewSupport;

public class CDTTestViewSupport implements ITestViewSupport {
	/**
	 * The delimiter between parts of serialized test path. Should not be met in
	 * test paths names.
	 */
	private static final String TEST_PATH_PART_DELIMITER = "\n"; //$NON-NLS-1$
	public static final String FRAME_PREFIX = " at "; //$NON-NLS-1$

	@Override
	public Collection<StringMatcher> getTraceExclusionFilterPatterns() {
		return Collections.emptySet();
	}

	@Override
	public IAction getOpenTestAction(Shell shell, ITestCaseElement testCase) {
		return new OpenTestAction(shell, testCase.getParent(), testCase);
	}

	@Override
	public IAction getOpenTestAction(Shell shell, ITestSuiteElement testSuite) {
		return new OpenTestAction(shell, testSuite);
	}

	@Override
	public IAction createOpenEditorAction(Shell shell, ITestElement failure, String traceLine) {
		try {
			String testName = traceLine;
			int indexOfFramePrefix = testName.indexOf(FRAME_PREFIX);
			if (indexOfFramePrefix == -1) {
				return null;
			}
			testName = testName.substring(indexOfFramePrefix);
			testName = testName.substring(FRAME_PREFIX.length(), testName.lastIndexOf(':')).trim();

			String lineNumber = traceLine;
			lineNumber = lineNumber.substring(lineNumber.indexOf(':') + 1);
			int line = Integer.parseInt(lineNumber);
			return new OpenEditorAtLineAction(shell, testName, failure.getTestRunSession(), line);
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			CDTUnitTestPlugin.log(e);
		}
		return null;
	}

	@Override
	public Runnable createShowStackTraceInConsoleViewActionDelegate(ITestElement failedTest) {
		return null;
	}

	@Override
	public ILaunchConfiguration getRerunLaunchConfiguration(List<ITestElement> tests) {
		if (tests.isEmpty()) {
			return null;
		}
		ILaunchConfiguration origin = tests.get(0).getTestRunSession().getLaunch().getLaunchConfiguration();
		ILaunchConfigurationWorkingCopy res;
		try {
			res = origin.copy(origin.getName() + "\uD83D\uDD03"); //$NON-NLS-1$
			res.setAttribute(ITestsLaunchConfigurationConstants.ATTR_TESTS_FILTER,
					tests.stream().map(CDTTestViewSupport::packTestPaths).collect(Collectors.toList()));
			return res;
		} catch (CoreException e) {
			CDTUnitTestPlugin.log(e);
			return null;
		}
	}

	/**
	 * Pack the paths to specified test items to string list.
	 * @param testElement test element to pack
	 *
	 * @return string list
	 */
	private static String packTestPaths(ITestElement testElement) {
		List<String> testPath = new ArrayList<>();

		// Collect test path parts (in reverse order)
		testPath.clear();
		ITestElement element = testElement;
		while (element != null && !(element instanceof ITestRunSession)) {
			// Exclude root test suite
			if (element.getParent() != null) {
				testPath.add(element.getTestName());
			}
			element = element.getParent();
		}
		// Join path parts into the only string
		StringBuilder sb = new StringBuilder();
		boolean needDelimiter = false;
		for (int pathPartIdx = testPath.size() - 1; pathPartIdx >= 0; pathPartIdx--) {
			if (needDelimiter) {
				sb.append(TEST_PATH_PART_DELIMITER);
			} else {
				needDelimiter = true;
			}
			sb.append(testPath.get(pathPartIdx));
		}
		return sb.toString();
	}

	@Override
	public String getDisplayName() {
		return "C/C++ Unit Tests"; //$NON-NLS-1$
	}

	@Override
	public ITestRunnerClient newTestRunnerClient(ITestRunSession session) {
		return new CDTTestRunnerClient(session);
	}
}
