/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *     Alex Ruiz (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized messages
 */
public class CodanUIMessages extends NLS {
	public static String BuildPropertyPage_Description;
	public static String BuildPropertyPage_RunAsYouType;
	public static String BuildPropertyPage_RunWithBuild;
	public static String CheckedTreeEditor_SelectionCannotBeEmpty;
	public static String CodanPreferencePage_Customize;
	public static String CodanPreferencePage_Description;
	public static String CodanPreferencePage_HasPreferences;
	public static String CodanPreferencePage_Info;
	public static String CodanPreferencePage_MessageLabel;
	public static String CodanPreferencePage_NoInfo;
	public static String CodanPreferencePage_Parameters;
	public static String CodanPreferencePage_Update_markers;
	public static String ProblemsTreeEditor_NameColumn;
	public static String ProblemsTreeEditor_Problems;
	public static String ProblemsTreeEditor_SeverityColumn;
	public static String ProblemsTreeEditor_ToolTip;
	public static String OverlayPage_Use_Workspace_Settings;
	public static String OverlayPage_Use_Project_Settings;
	public static String OverlayPage_Configure_Workspace_Settings;
	public static String PropertyStore_Cannot_write_resource_property;
	public static String CustomizeProblemComposite_LaunchingTab;
	public static String CustomizeProblemComposite_TabParameters;
	public static String CustomizeProblemComposite_TabScope;
	public static String CustomizeProblemDialog_Message;
	public static String CustomizeProblemDialog_Title;
	public static String Job_TitleRunningAnalysis;
	public static String ParametersComposite_DoNotChange;
	public static String ParametersComposite_IsEnabled;
	public static String ParametersComposite_MessagePattern;
	public static String ParametersComposite_NewValue;
	public static String ParametersComposite_None;
	public static String ParametersComposite_Severity;
	//
	public static String ExclusionInclusionDialog_title;
	public static String ExclusionInclusionDialog_description;
	public static String ExclusionInclusionDialog_description2;
	public static String ExclusionInclusionDialog_exclusion_pattern_label;
	public static String ExclusionInclusionDialog_inclusion_pattern_label;
	public static String ExclusionInclusionDialog_inclusion_pattern_add;
	public static String ExclusionInclusionDialog_inclusion_pattern_add_multiple;
	public static String ExclusionInclusionDialog_inclusion_pattern_remove;
	public static String ExclusionInclusionDialog_inclusion_pattern_edit;
	public static String ExclusionInclusionDialog_exclusion_pattern_add;
	public static String ExclusionInclusionDialog_exclusion_pattern_add_multiple;
	public static String ExclusionInclusionDialog_exclusion_pattern_remove;
	public static String ExclusionInclusionDialog_exclusion_pattern_edit;
	public static String ExclusionInclusionDialog_ChooseExclusionPattern_title;
	public static String ExclusionInclusionDialog_ChooseExclusionPattern_description;
	public static String ExclusionInclusionDialog_ChooseInclusionPattern_title;
	public static String ExclusionInclusionDialog_ChooseInclusionPattern_description;
	public static String ExclusionInclusionEntryDialog_exclude_add_title;
	public static String ExclusionInclusionEntryDialog_exclude_edit_title;
	public static String ExclusionInclusionEntryDialog_exclude_description;
	public static String ExclusionInclusionEntryDialog_exclude_pattern_label;
	public static String ExclusionInclusionEntryDialog_include_add_title;
	public static String ExclusionInclusionEntryDialog_include_edit_title;
	public static String ExclusionInclusionEntryDialog_include_description;
	public static String ExclusionInclusionEntryDialog_include_pattern_label;
	public static String ExclusionInclusionEntryDialog_pattern_button;
	public static String ExclusionInclusionEntryDialog_error_empty;
	public static String ExclusionInclusionEntryDialog_error_notrelative;
	public static String ExclusionInclusionEntryDialog_error_exists;
	public static String ExclusionInclusionEntryDialog_ChooseExclusionPattern_title;
	public static String ExclusionInclusionEntryDialog_ChooseExclusionPattern_description;
	public static String ExclusionInclusionEntryDialog_ChooseInclusionPattern_title;
	public static String ExclusionInclusionEntryDialog_ChooseInclusionPattern_description;
	public static String LaunchModesPropertyPage_RunAsYouType;
	public static String LaunchModesPropertyPage_RunOnDemand;
	public static String LaunchModesPropertyPage_RunOnFullBuild;
	public static String LaunchModesPropertyPage_RunOnIncrementalBuild;
	public static String LaunchModesPropertyPage_RunOnFileSave;
	public static String LaunchModesPropertyPage_RunOnFileOpen;

	static {
		NLS.initializeMessages(CodanUIMessages.class.getName(), CodanUIMessages.class);
	}

	// Do not instantiate
	private CodanUIMessages() {
	}
}
