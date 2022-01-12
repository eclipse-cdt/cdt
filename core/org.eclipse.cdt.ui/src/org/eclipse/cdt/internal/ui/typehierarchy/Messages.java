/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.typehierarchy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.typehierarchy.messages"; //$NON-NLS-1$
	public static String OpenTypeHierarchyAction_label;
	public static String OpenTypeHierarchyAction_tooltip;
	public static String OpenTypeInHierarchyAction_errorNoDefinition;
	public static String OpenTypeInHierarchyAction_errorTitle;
	public static String OpenTypeInHierarchyAction_message;
	public static String OpenTypeInHierarchyAction_title;
	public static String OpenTypeInHierarchyAction_upperListLabel;
	public static String THHierarchyModel_errorComputingHierarchy;
	public static String THHierarchyModel_Job_title;
	public static String THHistoryDropDownAction_ClearHistory;
	public static String THHistoryDropDownAction_tooltip;
	public static String THHistoryListAction_HistoryList_label;
	public static String THHistoryListAction_HistoryList_title;
	public static String THHistoryListAction_label;
	public static String THHistoryListAction_Remove;
	public static String THInformationControl_titleHierarchy;
	public static String THInformationControl_titleSubHierarchy;
	public static String THInformationControl_titleSuperHierarchy;
	public static String THInformationControl_titleMemberInHierarchy;
	public static String THInformationControl_titleMemberInSubHierarchy;
	public static String THInformationControl_titleMemberInSuperHierarchy;
	public static String THInformationControl_toggle_typeHierarchy_label;
	public static String THInformationControl_toggle_subTypeHierarchy_label;
	public static String THInformationControl_toggle_superTypeHierarchy_label;
	public static String THViewPart_AutomaticOrientation;
	public static String THViewPart_Cancel;
	public static String THViewPart_Cancel_tooltip;
	public static String THViewPart_CompleteTypeHierarchy;
	public static String THViewPart_CompleteTypeHierarchy_tooltip;
	public static String THViewPart_FocusOn;
	public static String THViewPart_HideFields_label;
	public static String THViewPart_HideFields_tooltip;
	public static String THViewPart_HideNonPublic_label;
	public static String THViewPart_HideNonPublic_tooltip;
	public static String THViewPart_HideStatic_label;
	public static String THViewPart_HideStatic_tooltip;
	public static String THViewPart_HorizontalOrientation;
	public static String THViewPart_instruction;
	public static String THViewPart_LayoutMenu;
	public static String THViewPart_MethodPane_title;
	public static String THViewPart_Open;
	public static String THViewPart_Open_tooltip;
	public static String THViewPart_CopyTypeHierarchy;
	public static String THViewPart_Refresh;
	public static String THViewPart_Refresh_tooltip;
	public static String THViewPart_ShowFileNames;
	public static String THViewPart_ShowFileNames_tooltip;
	public static String THViewPart_ShowInherited_label;
	public static String THViewPart_ShowInherited_tooltip;
	public static String THViewPart_SinglePaneOrientation;
	public static String THViewPart_SubtypeHierarchy;
	public static String THViewPart_SubtypeHierarchy_tooltip;
	public static String THViewPart_SupertypeHierarchy;
	public static String THViewPart_SupertypeHierarchy_tooltip;
	public static String THViewPart_VerticalOrientation;
	public static String TypeHierarchyUI_OpenFailure_message;
	public static String TypeHierarchyUI_OpenTypeHierarchy;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
