/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *                                Save build output
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.osgi.util.NLS;

public final class ConsoleMessages extends NLS {

	static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.buildconsole.ConsoleMessages";//$NON-NLS-1$

	private ConsoleMessages() {
		// Do not instantiate
	}

	public static String find_replace_action_label;
	public static String find_replace_action_tooltip;
	public static String find_replace_action_image;
	public static String find_replace_action_description;
	public static String BuildConsolePage__Copy_Ctrl_C_6;
	public static String BuildConsolePage_Copy_7;
	public static String BuildConsolePage_Select__All_Ctrl_A_12;
	public static String BuildConsolePage_Select_All;
	public static String ScrollLockAction_Scroll_Lock_1;
	public static String PreviousErrorAction_Tooltip;
	public static String NextErrorAction_Tooltip;
	public static String ShowErrorAction_Tooltip;
	public static String CopyLog_ActionTooltip;
	public static String CopyLog_BuildNotLogged;
	public static String CopyLog_ChooseDestination;
	public static String CopyLog_ErrorCopyingFile;
	public static String CopyLog_ErrorWhileCopyingLog;
	public static String CopyLog_InvalidDestination;
	public static String CopyLog_LogFileIsNotAvailable;
	public static String CopyLog_UnableToAccess;
	public static String CopyLog_UnavailableLog;

	static {
		NLS.initializeMessages(BUNDLE_NAME, ConsoleMessages.class);
	}
}