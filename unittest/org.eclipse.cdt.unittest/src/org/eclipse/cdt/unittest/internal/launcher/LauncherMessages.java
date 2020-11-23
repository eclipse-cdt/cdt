/*******************************************************************************
 * Copyright (c) 2011, 2020 Anton Gorenkov.
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
package org.eclipse.cdt.unittest.internal.launcher;

import org.eclipse.osgi.util.NLS;

public class LauncherMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.unittest.internal.launcher.LauncherMessages"; //$NON-NLS-1$

	public static String BaseTestsLaunchDelegate_invalid_tests_runner;
	public static String BaseTestsLaunchDelegate_tests_runner_load_failed;

	public static String CTestingTab_no_tests_runner_error;
	public static String CTestingTab_no_tests_runner_label;
	public static String CTestingTab_tab_name;
	public static String CTestingTab_tests_runner_is_not_set;
	public static String CTestingTab_tests_runner_label;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LauncherMessages.class);
	}

	private LauncherMessages() {
	}
}
