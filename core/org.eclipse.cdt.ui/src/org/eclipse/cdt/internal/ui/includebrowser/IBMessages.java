/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.includebrowser;

import org.eclipse.osgi.util.NLS;

public class IBMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.includebrowser.IBMessages"; //$NON-NLS-1$

	public static String IBHistoryDropDownAction_ClearHistory_label;
	public static String IBHistoryDropDownAction_tooltip;
	public static String IBHistoryListAction_HistoryDialog_title;
	public static String IBHistoryListAction_HistoryList_label;
	public static String IBHistoryListAction_label;
	public static String IBHistoryListAction_Remove_label;
	public static String IBViewPart_falseInputMessage;
	public static String IBViewPart_FocusOn_label;
	public static String IBViewPart_hideInactive_label;
	public static String IBViewPart_hideInactive_tooltip;
	public static String IBViewPart_hideSystem_label;
	public static String IBViewPart_hideSystem_tooltip;
	public static String IBViewPart_IncludedByContentDescription;
	public static String IBViewPart_IncludesToContentDescription;
	public static String IBViewPart_instructionMessage;
	public static String IBViewPart_nextMatch_label;
	public static String IBViewPart_nextMatch_tooltip;
	public static String IBViewPart_OpenWithMenu_label;
	public static String IBViewPart_previousMatch_label;
	public static String IBViewPart_previousMatch_tooltip;
	public static String IBViewPart_refresh_label;
	public static String IBViewPart_refresh_tooltip;
	public static String IBViewPart_showFolders_label;
	public static String IBViewPart_showFolders_tooltip;
	public static String IBViewPart_showInclude_label;
	public static String IBViewPart_showIncludedBy_label;
	public static String IBViewPart_showIncludedBy_tooltip;
	public static String IBViewPart_showIncludesTo_label;
	public static String IBViewPart_showIncludesTo_tooltip;
	public static String IBViewPart_ShowInMenu_label;
	public static String IBViewPart_workspaceScope;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, IBMessages.class);
	}

	private IBMessages() {
	}
}
