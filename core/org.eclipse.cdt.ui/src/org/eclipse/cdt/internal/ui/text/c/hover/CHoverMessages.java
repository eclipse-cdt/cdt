/*******************************************************************************
 * Copyright (c) 2002, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.osgi.util.NLS;

public final class CHoverMessages extends NLS {

	public static String AbstractAnnotationHover_action_configureAnnotationPreferences;
	public static String AbstractAnnotationHover_message_singleQuickFix;
	public static String AbstractAnnotationHover_message_multipleQuickFix;

	public static String CExpandHover_tooltip_noBreakpoint;
	public static String CMacroExpansionControl_exploreMacroExpansion;
	public static String CMacroExpansionControl_statusText;

	public static String CMacroExpansionControl_title_expansion;
	public static String CMacroExpansionControl_title_fullyExpanded;
	public static String CMacroExpansionControl_title_macroExpansion;
	public static String CMacroExpansionControl_title_macroExpansionExploration;
	public static String CMacroExpansionControl_title_original;

	public static String CMacroExpansionInput_jobTitle;

	public static String CSourceHover_jobTitle;

	static {
		NLS.initializeMessages(CHoverMessages.class.getName(), CHoverMessages.class);
	}

	// Do not instantiate
	private CHoverMessages() {
	}
}