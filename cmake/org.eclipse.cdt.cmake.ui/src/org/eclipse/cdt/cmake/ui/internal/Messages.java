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
package org.eclipse.cdt.cmake.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String CMakeBuildTab_BuildCommand;
	public static String CMakeBuildTab_CleanCommand;
	public static String CMakeBuildTab_Cmake;
	public static String CMakeBuildTab_CMakeArgs;
	public static String CMakeBuildTab_Generator;
	public static String CMakeBuildTab_Ninja;
	public static String CMakeBuildTab_NoneAvailable;
	public static String CMakeBuildTab_Settings;
	public static String CMakeBuildTab_Toolchain;
	public static String CMakeBuildTab_UnixMakefiles;
	public static String CMakePreferencePage_Add;
	public static String CMakePreferencePage_ConfirmRemoveDesc;
	public static String CMakePreferencePage_ConfirmRemoveTitle;
	public static String CMakePreferencePage_Files;
	public static String CMakePreferencePage_Path;
	public static String CMakePreferencePage_Remove;
	public static String CMakePreferencePage_Toolchain;
	public static String CMakePropertyPage_FailedToStartCMakeGui_Body;
	public static String CMakePropertyPage_FailedToStartCMakeGui_Title;
	public static String CMakePropertyPage_FailedToGetOS_Body;
	public static String CMakePropertyPage_FailedToGetOS_Title;
	public static String CMakePropertyPage_FailedToGetCMakeConfiguration_Body;
	public static String CMakePropertyPage_FailedToGetCMakeConfiguration_Title;
	public static String CMakePropertyPage_FailedToConfigure;
	public static String CMakePropertyPage_Terminated;
	public static String CMakePropertyPage_LaunchCMakeGui;

	public static String NewCMakeProjectWizard_Description;
	public static String NewCMakeProjectWizard_PageTitle;
	public static String NewCMakeProjectWizard_WindowTitle;

	public static String NewCMakeToolChainFilePage_Browse;
	public static String NewCMakeToolChainFilePage_NoPath;
	public static String NewCMakeToolChainFilePage_Path;
	public static String NewCMakeToolChainFilePage_Select;
	public static String NewCMakeToolChainFilePage_Title;
	public static String NewCMakeToolChainFilePage_Toolchain;

	static {
		// initialize resource bundle
		NLS.initializeMessages("org.eclipse.cdt.cmake.ui.internal.messages", Messages.class); //$NON-NLS-1$
	}

	private Messages() {
	}
}
