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
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String MesonBuildTab_BuildCommand;
	public static String MesonBuildTab_CleanCommand;
	public static String MesonBuildTab_Meson;
	public static String MesonBuildTab_MesonArgs;
	public static String MesonBuildTab_Generator;
	public static String MesonBuildTab_Ninja;
	public static String MesonBuildTab_NoneAvailable;
	public static String MesonBuildTab_Settings;
	public static String MesonBuildTab_Toolchain;
	public static String MesonBuildTab_UnixMakefiles;
	public static String MesonPreferencePage_Add;
	public static String MesonPreferencePage_ConfirmRemoveDesc;
	public static String MesonPreferencePage_ConfirmRemoveTitle;
	public static String MesonPreferencePage_Files;
	public static String MesonPreferencePage_Path;
	public static String MesonPreferencePage_Remove;
	public static String MesonPreferencePage_Toolchain;
	public static String MesonPropertyPage_FailedToStartMesonGui_Body;
	public static String MesonPropertyPage_FailedToStartMesonGui_Title;
	public static String MesonPropertyPage_LaunchMesonGui;

	public static String NewMesonProjectWizard_Description;
	public static String NewMesonProjectWizard_PageTitle;
	public static String NewMesonProjectWizard_WindowTitle;

	public static String NewMesonToolChainFilePage_Browse;
	public static String NewMesonToolChainFilePage_NoPath;
	public static String NewMesonToolChainFilePage_Path;
	public static String NewMesonToolChainFilePage_Select;
	public static String NewMesonToolChainFilePage_Title;
	public static String NewMesonToolChainFilePage_Toolchain;

	static {
		// initialize resource bundle
		NLS.initializeMessages("org.eclipse.cdt.internal.meson.ui.messages", Messages.class); //$NON-NLS-1$
	}

	private Messages() {
	}
}
