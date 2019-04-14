/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.cmake.core.internal.messages"; //$NON-NLS-1$
	public static String CMakeBuildConfiguration_Building;
	public static String CMakeBuildConfiguration_BuildingIn;
	public static String CMakeBuildConfiguration_BuildingComplete;
	public static String CMakeBuildConfiguration_BuildComplete;
	public static String CMakeBuildConfiguration_Cleaning;
	public static String CMakeBuildConfiguration_Configuring;
	public static String CMakeBuildConfiguration_NotFound;
	public static String CMakeBuildConfiguration_NoToolchainFile;
	public static String CMakeBuildConfiguration_ProcCompCmds;
	public static String CMakeBuildConfiguration_ProcCompJson;
	public static String CMakeBuildConfiguration_Failure;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
