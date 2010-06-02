/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.osgi.util.NLS;

public final class CHoverMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.text.c.hover.CHoverMessages";//$NON-NLS-1$

	private CHoverMessages() {
		// Do not instantiate
	}

	public static String AbstractAnnotationHover_action_configureAnnotationPreferences;
	public static String AbstractAnnotationHover_message_singleQuickFix;
	public static String AbstractAnnotationHover_message_multipleQuickFix;

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
		NLS.initializeMessages(BUNDLE_NAME, CHoverMessages.class);
	}
}