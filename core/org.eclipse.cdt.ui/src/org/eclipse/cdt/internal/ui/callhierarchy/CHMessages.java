/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.osgi.util.NLS;

public class CHMessages extends NLS {
	public static String CallHierarchyUI_label;
	public static String CallHierarchyUI_openFailureMessage;
	public static String CallHierarchyUI_selectMessage;
	public static String CHHistoryListAction_HistoryDialog_title;
	public static String CHHistoryListAction_HistoryList_label;
	public static String CHHistoryListAction_OpenHistory_label;
	public static String CHHistoryListAction_Remove_label;
	public static String CHLabelProvider_matches;
	public static String CHViewPart_emptyPageMessage;
	public static String CHViewPart_FilterVariables_label;
	public static String CHViewPart_FilterVariables_tooltip;
	public static String CHViewPart_FocusOn_label;
	public static String CHViewPart_NextReference_label;
	public static String CHViewPart_NextReference_tooltip;
	public static String CHViewPart_Open_label;
	public static String CHViewPart_Open_tooltip;
	public static String CHViewPart_PreviousReference_label;
	public static String CHViewPart_PreviousReference_tooltip;
	public static String CHViewPart_Refresh_label;
	public static String CHViewPart_Refresh_tooltip;
	public static String CHViewPart_RemoveFromView_label;
	public static String CHViewPart_CopyCallHierarchy_label;
	public static String CHViewPart_ShowCallees_label;
	public static String CHViewPart_ShowCallees_tooltip;
	public static String CHViewPart_ShowCallers_label;
	public static String CHViewPart_ShowCallers_tooltip;
	public static String CHViewPart_ShowFiles_label;
	public static String CHViewPart_ShowFiles_tooltip;
	public static String CHViewPart_ShowReference_label;
	public static String CHViewPart_ShowReference_tooltip;
	public static String CHViewPart_Title_callees;
	public static String CHViewPart_Title_callers;
	public static String CHViewPart_WorkspaceScope;
	public static String CHHistoryDropDownAction_ClearHistory_label;
	public static String CHHistoryDropDownAction_ShowHistoryList_tooltip;
	public static String OpenCallHierarchyAction_label;
	public static String OpenCallHierarchyAction_tooltip;
	public static String OpenElementInCallHierarchyAction_errorDlgTitle;
	public static String OpenElementInCallHierarchyAction_errorNoDefinition;
	public static String OpenElementInCallHierarchyAction_message;
	public static String OpenElementInCallHierarchyAction_title;
	public static String OpenElementInCallHierarchyAction_upperListLabel;
	public static String CHPinAction_label;
	public static String CHPinAction_tooltip;
	public static String CHProviderManager_InvalidCallHierarchyProvider;

	static {
		// initialize resource bundle
		NLS.initializeMessages(CHMessages.class.getName(), CHMessages.class);
	}

	private CHMessages() {
	}
}
