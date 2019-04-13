/*******************************************************************************
 * Copyright (c) 2001, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.osgi.util.NLS;

/**
 * Class that gives access to the action messages resource bundle.
 */
public class ActionMessages extends NLS {
	public static String SourceMenu_label;
	public static String SelectionConverter_codeResolve_failed;
	public static String OpenAction_label;
	public static String OpenAction_tooltip;
	public static String OpenAction_description;
	public static String OpenAction_declaration_label;
	public static String OpenAction_select_element;
	public static String OpenAction_error_title;
	public static String OpenAction_error_message;
	public static String OpenAction_error_messageArgs;
	public static String OpenAction_error_messageProblems;
	public static String OpenAction_error_messageBadSelection;
	public static String MemberFilterActionGroup_hide_fields_label;
	public static String MemberFilterActionGroup_hide_fields_tooltip;
	public static String MemberFilterActionGroup_hide_fields_description;
	public static String MemberFilterActionGroup_hide_static_label;
	public static String MemberFilterActionGroup_hide_static_tooltip;
	public static String MemberFilterActionGroup_hide_static_description;
	public static String MemberFilterActionGroup_hide_nonpublic_label;
	public static String MemberFilterActionGroup_hide_nonpublic_tooltip;
	public static String MemberFilterActionGroup_hide_nonpublic_description;
	public static String MemberFilterActionGroup_hide_inactive_label;
	public static String MemberFilterActionGroup_hide_inactive_tooltip;
	public static String MemberFilterActionGroup_hide_inactive_description;
	public static String ActionUtil_notOnBuildPath_title;
	public static String ActionUtil_notOnBuildPath_message;
	public static String ActionUtil_warning_derived_dontShowAgain;
	public static String ActionUtil_warning_derived_message;
	public static String ActionUtil_warning_derived_title;
	public static String SelectAllAction_label;
	public static String SelectAllAction_tooltip;
	public static String ToggleLinkingAction_label;
	public static String ToggleLinkingAction_tooltip;
	public static String ToggleLinkingAction_description;
	public static String IncludesGroupingAction_label;
	public static String IncludesGroupingAction_tooltip;
	public static String IncludesGroupingAction_description;
	public static String NamespacesGroupingAction_label;
	public static String NamespacesGroupingAction_tooltip;
	public static String NamespacesGroupingAction_description;
	public static String MemberGroupingAction_label;
	public static String MemberGroupingAction_tooltip;
	public static String MemberGroupingAction_description;
	public static String MacroGroupingAction_label;
	public static String MacroGroupingAction_tooltip;
	public static String MacroGroupingAction_description;
	public static String COutlineInformationControl_viewMenu_sort_label;
	public static String ChangeBuildConfigMenuAction_title;
	public static String ChangeBuildConfigMenuAction_text;
	public static String CreateParserLogAction_existingFile;
	public static String CreateParserLogAction_readOnlyFile;
	public static String CreateParserLogAction_title;
	public static String DeleteResConfigsAction_0;
	public static String DeleteResConfigsAction_1;
	public static String ExcludeFromBuildAction_0;
	public static String ExcludeFromBuildAction_1;
	public static String BuildActiveConfigMenuAction_defaultTooltip;
	public static String BuildActiveConfigMenuAction_buildConfigTooltip;
	public static String SurroundWithTemplateMenuAction_SubMenuName;
	public static String SurroundWithTemplateMenuAction_ConfigureTemplatesActionName;
	public static String SurroundWithTemplateMenuAction_NoneApplicable;
	public static String CopyTreeAction_problem;
	public static String CopyTreeAction_clipboard_busy;
	public static String FormatAllAction_label;
	public static String FormatAllAction_tooltip;
	public static String FormatAllAction_description;
	public static String FormatAllAction_status_description;
	public static String FormatAllAction_multi_status_title;
	public static String FormatAllAction_error_title;
	public static String FormatAllAction_error_message;
	public static String FormatAllAction_operation_description;
	public static String FormatAllAction_failedvalidateedit_title;
	public static String FormatAllAction_failedvalidateedit_message;
	public static String FormatAllAction_noundo_title;
	public static String FormatAllAction_noundo_message;
	public static String AlignAllConstAction_label;
	public static String AlignAllConstAction_tooltip;
	public static String AlignAllConstAction_description;
	public static String CollapseAllAction_label;
	public static String CollapseAllAction_tooltip;
	public static String CollapseAllAction_description;
	public static String ExpandAllAction_label;
	public static String ExpandAllAction_tooltip;
	public static String ExpandAllAction_description;
	public static String CopyQualifiedNameAction_ToolTipText;
	public static String CopyQualifiedNameAction_InfoDialogTitel;
	public static String CopyQualifiedNameAction_ErrorDescription;
	public static String CopyQualifiedNameAction_ActionName;
	public static String CopyQualifiedNameAction_ErrorTitle;
	public static String CopyQualifiedNameAction_NoElementToQualify;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(ActionMessages.class.getName(), ActionMessages.class);
	}

	private ActionMessages() {
	}
}
