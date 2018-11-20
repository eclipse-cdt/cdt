/*******************************************************************************
 * Copyright (c) 2012 Anton Gorenkov.
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
package org.eclipse.cdt.testsrunner.internal.launcher;

/**
 * Constants used for attributes in CDT Unit Testing Support launch
 * configurations.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITestsLaunchConfigurationConstants {

	public static final String CDT_TESTS_LAUNCH_ID = "org.eclipse.cdt.testsrunner.launch"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a string specifying
	 * tests runner string unique identifier
	 */
	public static final String ATTR_TESTS_RUNNER = CDT_TESTS_LAUNCH_ID + ".TESTS_RUNNER"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a string list specifying
	 * tests paths to run
	 */
	public static final String ATTR_TESTS_FILTER = CDT_TESTS_LAUNCH_ID + ".TESTS_FILTER"; //$NON-NLS-1$

}
