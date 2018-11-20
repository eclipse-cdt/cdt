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
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.launcher.ITestsLaunchConfigurationConstants;
import org.eclipse.cdt.testsrunner.internal.ui.view.TestPathUtils;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Launches the new run or debug session for the currently selected items of
 * test hierarchy.
 */
public abstract class RelaunchSelectedAction extends Action {

	private ITestingSession testingSession;
	private TreeViewer treeViewer;

	public RelaunchSelectedAction(ITestingSession testingSession, TreeViewer treeViewer) {
		this.testingSession = testingSession;
		this.treeViewer = treeViewer;
	}

	/**
	 * Returns the launch mode that should be use to run selected test item.
	 *
	 * @return launch mode
	 */
	protected abstract String getLaunchMode();

	@Override
	public void run() {
		if (testingSession != null) {
			try {
				ILaunch launch = testingSession.getLaunch();
				ILaunchConfigurationWorkingCopy launchConf = launch.getLaunchConfiguration().getWorkingCopy();
				List<String> testsFilterAttr = Arrays.asList(TestPathUtils.packTestPaths(getSelectedTestItems()));
				launchConf.setAttribute(ITestsLaunchConfigurationConstants.ATTR_TESTS_FILTER, testsFilterAttr);
				DebugUITools.launch(launchConf, getLaunchMode());
				return;
			} catch (CoreException e) {
				TestsRunnerPlugin.log(e);
			}
		}
		setEnabled(false);
	}

	/**
	 * Returns the currently selected items of test hierarchy.
	 *
	 * @return array of test items
	 */
	private ITestItem[] getSelectedTestItems() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		ITestItem[] result = new ITestItem[selection.size()];
		int resultIndex = 0;
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			result[resultIndex] = (ITestItem) it.next();
			++resultIndex;
		}
		return result;
	}

	/**
	 * Sets actual testing session.
	 *
	 * @param testingSession testing session
	 */
	public void setTestingSession(ITestingSession testingSession) {
		this.testingSession = testingSession;
	}

}
