/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public static String THInformationControl_regularTitle;
	public static String THInformationControl_showDefiningTypesTitle;
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
