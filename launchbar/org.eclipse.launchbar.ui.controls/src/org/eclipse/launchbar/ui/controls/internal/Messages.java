/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *     Torkild U. Resheim - add preference to control target selector
 *     Vincent Guignot - Ingenico - add preference to control Build button
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.launchbar.ui.controls.internal.messages"; //$NON-NLS-1$
	public static String ConfigSelector_0;
	public static String ConfigSelector_1;
	public static String ConfigSelector_2;
	public static String ConfigSelector_3;
	public static String CSelector_0;
	public static String CSelector_1;
	public static String EditButton_0;
	public static String FilterControl_0;
	public static String FilterControl_1;
	public static String LaunchBarControl_0;
	public static String LaunchBarControl_Build;
	public static String LaunchBarControl_Launch;
	public static String LaunchBarControl_Stop;
	public static String LaunchBarListViewer_0;
	public static String LaunchBarPreferencePage_0;
	public static String LaunchBarPreferencePage_1;
	public static String LaunchBarPreferencePage_AlwaysTargetSelector;
	public static String LaunchBarPreferencePage_EnableBuildButton;
	public static String LaunchConfigurationEditDialog_0;
	public static String LaunchConfigurationEditDialog_1;
	public static String LaunchConfigurationEditDialog_2;
	public static String LaunchConfigurationEditDialog_3;
	public static String LaunchConfigurationEditDialog_4;
	public static String LaunchConfigurationEditDialog_5;
	public static String LaunchConfigurationEditDialog_6;
	public static String ModeSelector_0;
	public static String ModeSelector_ToolTip;
	public static String TargetSelector_ToolTipPrefix;
	public static String TargetSelector_CreateNewTarget;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
