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

package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.osgi.util.NLS;

public class CHMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.callhierarchy.CHMessages"; //$NON-NLS-1$
	public static String CHHistoryListAction_HistoryDialog_title;
	public static String CHHistoryListAction_HistoryList_label;
	public static String CHHistoryListAction_OpenHistory_label;
	public static String CHHistoryListAction_Remove_label;
	public static String CHViewPart_emptyPageMessage;
	public static String CHViewPart_FilterVariables_label;
	public static String CHViewPart_FilterVariables_tooltip;
	public static String CHViewPart_HideMacros_label;
	public static String CHViewPart_HideMacros_tooltip;
	public static String CHViewPart_NextReference_label;
	public static String CHViewPart_NextReference_tooltip;
	public static String CHViewPart_OpenReference_label;
	public static String CHViewPart_PreviousReference_label;
	public static String CHViewPart_PreviousReference_tooltip;
	public static String CHViewPart_Refresh_label;
	public static String CHViewPart_Refresh_tooltip;
	public static String CHViewPart_ShowCallees_label;
	public static String CHViewPart_ShowCallees_tooltip;
	public static String CHViewPart_ShowCallers_label;
	public static String CHViewPart_ShowCallers_tooltip;
	public static String CHViewPart_ShowFiles_label;
	public static String CHViewPart_ShowFiles_tooltip;
	public static String CHViewPart_Title_callees;
	public static String CHViewPart_Title_callers;
	public static String CHViewPart_WorkspaceScope;
	public static String CHHistoryDropDownAction_ClearHistory_label;
	public static String CHHistoryDropDownAction_ShowHistoryList_tooltip;
	public static String OpenCallHierarchyAction_label;
	public static String OpenCallHierarchyAction_tooltip;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CHMessages.class);
	}

	private CHMessages() {
	}
}
