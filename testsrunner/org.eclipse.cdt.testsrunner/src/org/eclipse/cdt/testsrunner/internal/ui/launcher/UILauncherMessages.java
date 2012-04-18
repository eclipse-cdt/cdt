/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.launcher;

import org.eclipse.osgi.util.NLS;

public class UILauncherMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.ui.launcher.UILauncherMessages"; //$NON-NLS-1$
	public static String CTestingTab_no_tests_runner_error;
	public static String CTestingTab_no_tests_runner_label;
	public static String CTestingTab_tab_name;
	public static String CTestingTab_tests_runner_is_not_set;
	public static String CTestingTab_tests_runner_label;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, UILauncherMessages.class);
	}

	private UILauncherMessages() {
	}
}
