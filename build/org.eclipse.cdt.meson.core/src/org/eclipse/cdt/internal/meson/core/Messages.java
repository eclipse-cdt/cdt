/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Red Hat Inc. - initial version
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.meson.core.messages"; //$NON-NLS-1$

	public static String MesonBuildConfiguration_Building;
	public static String MesonBuildConfiguration_BuildingIn;
	public static String MesonBuildConfiguration_BuildingComplete;
	public static String MesonBuildConfiguration_Cleaning;
	public static String MesonBuildConfiguration_RunningMeson;
	public static String MesonBuildConfiguration_RunningNinja;
	public static String MesonBuildConfiguration_RefreshingScannerInfo;
	public static String MesonBuildConfiguration_RunningMesonFailure;
	public static String MesonBuildConfiguration_RunningNinjaFailure;
	public static String MesonBuildConfiguration_NoToolchainFile;
	public static String MesonBuildConfiguration_NoNinjaFile;
	public static String MesonBuildConfiguration_NoNinjaFileToClean;
	public static String MesonBuildConfiguration_ProcCompCmds;
	public static String MesonBuildConfiguration_ProcCompJson;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
