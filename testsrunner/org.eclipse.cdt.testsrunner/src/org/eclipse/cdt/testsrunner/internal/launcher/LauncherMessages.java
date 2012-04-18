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
package org.eclipse.cdt.testsrunner.internal.launcher;

import org.eclipse.osgi.util.NLS;

public class LauncherMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.testsrunner.internal.launcher.LauncherMessages"; //$NON-NLS-1$
	public static String BaseTestsLaunchDelegate_invalid_tests_runner;
	public static String BaseTestsLaunchDelegate_tests_runner_load_failed;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LauncherMessages.class);
	}

	private LauncherMessages() {
	}
}
