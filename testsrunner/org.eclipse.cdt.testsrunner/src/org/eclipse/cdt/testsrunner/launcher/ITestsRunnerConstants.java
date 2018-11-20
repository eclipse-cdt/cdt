/*******************************************************************************
 * Copyright (c) 2012 Anton Gorenkov
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
package org.eclipse.cdt.testsrunner.launcher;

/**
 * Constants used for attributes in CDT Tests Runner launch configurations.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITestsRunnerConstants {

	/**
	 * Tests Runner launch configuration type.
	 */
	public static final String LAUNCH_CONFIGURATION_TYPE_ID = "org.eclipse.cdt.testsrunner.launch.CTestsRunner"; //$NON-NLS-1$

	/**
	 * Specifies the default launch delegate for a Tests Run session.
	 */
	public static final String PREFERRED_RUN_TESTS_LAUNCH_DELEGATE = "org.eclipse.cdt.testsrunner.launch.runTests"; //$NON-NLS-1$

	/**
	 * Specifies the default launch delegate for a Tests Debug session.
	 */
	public static final String PREFERRED_DEBUG_TESTS_LAUNCH_DELEGATE = "org.eclipse.cdt.testsrunner.launch.dsf.runTests"; //$NON-NLS-1$

	/**
	 * Specifies the ID of the Tests Runner main view for browsing results.
	 */
	public static final String TESTS_RUNNER_RESULTS_VIEW_ID = "org.eclipse.cdt.testsrunner.resultsview"; //$NON-NLS-1$
}
