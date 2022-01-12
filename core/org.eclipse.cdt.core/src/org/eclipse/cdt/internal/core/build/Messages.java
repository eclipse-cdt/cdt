/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.build;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.build.messages"; //$NON-NLS-1$
	public static String CBuildConfiguration_CreateJob;
	public static String CBuildConfiguration_ToolchainMissing;
	public static String CBuildConfiguration_Location;
	public static String CBuildConfiguration_RunningScannerInfo;
	public static String CBuilder_ExceptionWhileBuilding;
	public static String CBuilder_ExceptionWhileBuilding2;
	public static String CBuilder_NotConfiguredCorrectly;
	public static String CBuilder_NotConfiguredCorrectly2;
	public static String CBuildConfiguration_CommandNotFound;
	public static String CBuildConfiguration_BuildComplete;
	public static String ErrorBuildConfiguration_What;
	public static String ErrorBuildConfiguration_ErrorWritingToConsole;
	public static String StandardBuildConfiguration_0;
	public static String StandardBuildConfiguration_1;
	public static String StandardBuildConfiguration_Failure;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
