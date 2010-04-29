/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

public final class PreferencesMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.preferences.PreferencesMessages";//$NON-NLS-1$

	private PreferencesMessages() {
		// Do not instantiate
	}

	public static String CodeAssistAdvancedConfigurationBlock_default_table_category_column_title;
	public static String CodeAssistAdvancedConfigurationBlock_default_table_description;
	public static String CodeAssistAdvancedConfigurationBlock_default_table_keybinding_column_title;
	public static String CodeAssistAdvancedConfigurationBlock_key_binding_hint;
	public static String CodeAssistAdvancedConfigurationBlock_page_description;
	public static String CodeAssistAdvancedConfigurationBlock_separate_table_category_column_title;
	public static String CodeAssistAdvancedConfigurationBlock_separate_table_description;
	public static String CodeAssistAdvancedConfigurationBlock_no_shortcut;
	public static String CodeAssistAdvancedConfigurationBlock_Up;
	public static String CodeAssistAdvancedConfigurationBlock_Down;
	public static String CEditorPreferencePage_link;
	public static String CEditorPreferencePage_link_tooltip;
	public static String CEditorPreferencePage_colors;
	public static String CEditorPreferencePage_invalid_input;
	public static String CEditorPreferencePage_empty_input;
//	public static String CEditorPreferencePage_ContentAssistPage_searchGroupTitle;
//	public static String CEditorPreferencePage_ContentAssistPage_searchGroupCurrentFileOption;
//	public static String CEditorPreferencePage_ContentAssistPage_searchGroupCurrentProjectOption;
	public static String CEditorPreferencePage_ContentAssistPage_insertionGroupTitle;
	public static String CEditorPreferencePage_ContentAssistPage_insertSingleProposalAutomatically;
	public static String CEditorPreferencePage_ContentAssistPage_insertCommonProposalAutomatically;
	public static String CEditorPreferencePage_ContentAssistPage_showProposalsInAlphabeticalOrder;
//	public static String CEditorPreferencePage_ContentAssistPage_timeoutDelay;
	public static String CEditorPreferencePage_ContentAssistPage_autoActivationGroupTitle;
	public static String CEditorPreferencePage_ContentAssistPage_autoActivationEnableDot;
	public static String CEditorPreferencePage_ContentAssistPage_autoActivationEnableArrow;
	public static String CEditorPreferencePage_ContentAssistPage_autoActivationDelay;
	public static String CEditorPreferencePage_ContentAssistPage_proposalFilterSelect;
	public static String CEditorPreferencePage_ContentAssistPage_completionProposalBackgroundColor;
	public static String CEditorPreferencePage_ContentAssistPage_completionProposalForegroundColor;
	public static String CEditorPreferencePage_ContentAssistPage_parameterBackgroundColor;
	public static String CEditorPreferencePage_ContentAssistPage_parameterForegroundColor;
	public static String CEditorPreferencePage_ContentAssistPage_autoActivationEnableDoubleColon;
	public static String CEditorPreferencePage_ContentAssistPage_sortingSection_title;
	public static String CEditorPreferencePage_sourceHoverBackgroundColor;
	public static String CEditorColoringConfigurationBlock_MultiLine;
	public static String CEditorColoringConfigurationBlock_singleLine;
	public static String CEditorColoringConfigurationBlock_keywords;
//	public static String CEditorColoringConfigurationBlock_returnKeyword;
	public static String CEditorColoringConfigurationBlock_builtInTypes;
	public static String CEditorColoringConfigurationBlock_strings;
	public static String CEditorColoringConfigurationBlock_operators;
	public static String CEditorColoringConfigurationBlock_braces;
	public static String CEditorColoringConfigurationBlock_numbers;
	public static String CEditorColoringConfigurationBlock_asmLabels;
	public static String CEditorColoringConfigurationBlock_asmDirectives;
	public static String CEditorColoringConfigurationBlock_others;
	public static String CEditorColoringConfigurationBlock_ppDirectives;
	public static String CEditorColoringConfigurationBlock_ppOthers;
	public static String CEditorColoringConfigurationBlock_ppHeaders;
	public static String CEditorColoringConfigurationBlock_cCommentTaskTags;
	public static String CEditorColoringConfigurationBlock_DoxygenTagRecognized;
	public static String CEditorColoringConfigurationBlock_DoxygenSingleLineComment;
	public static String CEditorColoringConfigurationBlock_DoxygenMultiLineComment;
	public static String CEditorColoringConfigurationBlock_coloring_category_code;
	public static String CEditorColoringConfigurationBlock_coloring_category_comments;
	public static String CEditorColoringConfigurationBlock_coloring_category_preprocessor;
	public static String CEditorColoringConfigurationBlock_coloring_category_assembly;
	public static String CEditorColoringConfigurationBlock_coloring_category_doxygen;
	public static String CEditorColoringConfigurationBlock_coloring_element;
	public static String CEditorColoringConfigurationBlock_link;
	public static String CEditorColoringConfigurationBlock_enable_semantic_highlighting;
	public static String CEditorColoringConfigurationBlock_enable;
	public static String CEditorColoringConfigurationBlock_preview;
	public static String CEditorColoringConfigurationBlock_color;
	public static String CEditorColoringConfigurationBlock_bold;
	public static String CEditorColoringConfigurationBlock_italic;
	public static String CEditorColoringConfigurationBlock_underline;
	public static String CEditorColoringConfigurationBlock_strikethrough;
	public static String CEditorPreferencePage_colorPage_systemDefault;
	public static String CEditorPreferencePage_behaviorPage_matchingBrackets;
	public static String CEditorPreferencePage_behaviorPage_subWordNavigation;
	public static String CEditorPreferencePage_behaviorPage_inactiveCode;
	public static String CEditorPreferencePage_behaviorPage_appearanceColorOptions;
	public static String CEditorPreferencePage_behaviorPage_matchingBracketColor;
	public static String CEditorPreferencePage_behaviorPage_inactiveCodeColor;
	public static String CEditorPreferencePage_behaviorPage_Color;
	public static String TemplatePreferencePage_Viewer_preview;
	public static String CFileTypesPreferencePage_description;
	public static String CFileTypesPreferenceBlock_New___;
	public static String CFileTypesPreferenceBlock_Remove;
	public static String CFileTypesPreferencePage_colTitlePattern;
	public static String CFileTypesPreferencePage_colTitleDescription;
	public static String CFileTypesPreferenceBlock_addAssociationError_title;
	public static String CFileTypesPreferenceBlock_addAssociationErrorMessage;
	public static String CFileTypesPreferencePage_colTitleStatus;
	public static String CFileTypesPreferencePage_userDefined;
	public static String CFileTypesPreferencePage_preDefined;
	public static String CFileTypesPropertyPage_useWorkspaceSettings;
	public static String CFileTypesPropertyPage_useProjectSettings;
	public static String CFileTypeDialog_title;
	public static String CFileTypeDialog_patternLabel;
	public static String CFileTypeDialog_typeLabel;
	public static String CEditorPreferencePage_hover_title;
	public static String CEditorHoverConfigurationBlock_hoverPreferences;
	public static String CEditorHoverConfigurationBlock_keyModifier;
	public static String CEditorHoverConfigurationBlock_description;
	public static String CEditorHoverConfigurationBlock_modifierIsNotValid;
	public static String CEditorHoverConfigurationBlock_modifierIsNotValidForHover;
	public static String CEditorHoverConfigurationBlock_duplicateModifier;
	public static String CEditorHoverConfigurationBlock_nameColumnTitle;
	public static String CEditorHoverConfigurationBlock_modifierColumnTitle;
	public static String CEditorHoverConfigurationBlock_delimiter;
	public static String CEditorHoverConfigurationBlock_insertDelimiterAndModifierAndDelimiter;
	public static String CEditorHoverConfigurationBlock_insertModifierAndDelimiter;
	public static String CEditorHoverConfigurationBlock_insertDelimiterAndModifier;
	public static String CBufferPreferences_CodeReaderBuffer_CodeReaderBufferGroup;
	public static String CBufferPreferences_CodeReaderBuffer_Size;
	public static String CEditorPreferencePage_behaviourPage_EnableEditorProblemAnnotation;
	public static String AppearancePreferencePage_description;
	public static String AppearancePreferencePage_showTUChildren_label;
	public static String AppearancePreferencePage_cviewGroupIncludes_label;
	public static String AppearancePreferencePage_cviewSeparateHeaderAndSource_label;
	public static String AppearancePreferencePage_cviewGroupMacros_label;
	public static String AppearancePreferencePage_outlineGroupIncludes_label;
	public static String AppearancePreferencePage_outlineGroupMethods_label;
	public static String AppearancePreferencePage_outlineGroupNamespaces_label;
	public static String AppearancePreferencePage_outlineGroupMacros_label;
	public static String AppearancePreferencePage_note;
	public static String AppearancePreferencePage_preferenceOnlyForNewViews;
	public static String AppearancePreferencePage_showSourceRootsAtTopOfProject_label;
	public static String CEditorPreferencePage_folding_title;
	public static String FoldingConfigurationBlock_enable;
	public static String FoldingConfigurationBlock_combo_caption;
	public static String FoldingConfigurationBlock_info_no_preferences;
	public static String FoldingConfigurationBlock_error_not_exist;
	public static String PathEntryVariablePreference_explanation;
	public static String PathEntryVariableDialog_shellTitle_newVariable;
	public static String PathEntryVariableDialog_shellTitle_existingVariable;
	public static String PathEntryVariableDialog_dialogTitle_newVariable;
	public static String PathEntryVariableDialog_dialogTitle_existingVariable;
	public static String PathEntryVariableDialog_message_newVariable;
	public static String PathEntryVariableDialog_message_existingVariable;
	public static String PathEntryVariableDialog_variableName;
	public static String PathEntryVariableDialog_variableValue;
	public static String PathEntryVariableDialog_variableNameEmptyMessage;
	public static String PathEntryVariableDialog_variableValueEmptyMessage;
	public static String PathEntryVariableDialog_variableValueInvalidMessage;
	public static String PathEntryVariableDialog_file;
	public static String PathEntryVariableDialog_folder;
	public static String PathEntryVariableDialog_selectFileTitle;
	public static String PathEntryVariableDialog_selectFolderTitle;
	public static String PathEntryVariableDialog_selectFolderMessage;
	public static String PathEntryVariableDialog_variableAlreadyExistsMessage;
	public static String PathEntryVariableDialog_pathIsRelativeMessage;
	public static String PathEntryVariableDialog_pathDoesNotExistMessage;
	public static String PathEntryVariablesBlock_variablesLabel;
	public static String PathEntryVariablesBlock_addVariableButton;
	public static String PathEntryVariablesBlock_editVariableButton;
	public static String PathEntryVariablesBlock_removeVariableButton;
	public static String ProposalFilterPreferencesUtil_defaultFilterName;

	public static String CEditorPreferencePage_typing_tabTitle;
	public static String CEditorPreferencePage_closeStrings;
	public static String CEditorPreferencePage_closeBrackets;
	public static String CEditorPreferencePage_closeAngularBrackets;
	public static String CEditorPreferencePage_closeBraces;
	public static String CEditorPreferencePage_wrapStrings;
	public static String CEditorPreferencePage_escapeStrings;
	public static String CEditorPreferencePage_GeneralAppearanceGroupTitle;
	public static String CEditorPreferencePage_SaveActionsTitle;
	public static String CEditorPreferencePage_SelectDocToolDescription;
	public static String CEditorPreferencePage_smartPaste;

	public static String CEditorPreferencePage_typing_smartTab;
	public static String CEditorPreferencePage_WorkspaceDefaultLabel;

	public static String SaveActionsPreferencePage_removeTrailingWhitespace;
	public static String SaveActionsPreferencePage_inEditedLines;
	public static String SaveActionsPreferencePage_inAllLines;
	public static String SaveActionsPreferencePage_ensureNewline;

	public static String SmartTypingConfigurationBlock_autoclose_title;
	public static String SmartTypingConfigurationBlock_autoindent_newlines;
	public static String SmartTypingConfigurationBlock_autoindent_title;
	public static String SmartTypingConfigurationBlock_tabs_title;
	public static String SmartTypingConfigurationBlock_tabs_message_tab_text;
	public static String SmartTypingConfigurationBlock_tabs_message_others_text;
	public static String SmartTypingConfigurationBlock_tabs_message_tooltip;
	public static String SmartTypingConfigurationBlock_tabs_message_spaces;
	public static String SmartTypingConfigurationBlock_tabs_message_tabs;
	public static String SmartTypingConfigurationBlock_tabs_message_tabsAndSpaces;
	public static String SmartTypingConfigurationBlock_pasting_title;
	public static String SmartTypingConfigurationBlock_strings_title;

	public static String CodeFormatterPreferencePage_title;
	public static String CodeFormatterPreferencePage_description;

	public static String TodoTaskPreferencePage_title;
	public static String TodoTaskPreferencePage_description;
	public static String TodoTaskConfigurationBlock_markers_tasks_high_priority;
	public static String TodoTaskConfigurationBlock_markers_tasks_normal_priority;
	public static String TodoTaskConfigurationBlock_markers_tasks_low_priority;
	public static String TodoTaskConfigurationBlock_markers_tasks_add_button;
	public static String TodoTaskConfigurationBlock_markers_tasks_remove_button;
	public static String TodoTaskConfigurationBlock_markers_tasks_edit_button;
	public static String TodoTaskConfigurationBlock_markers_tasks_name_column;
	public static String TodoTaskConfigurationBlock_markers_tasks_priority_column;
	public static String TodoTaskConfigurationBlock_markers_tasks_setdefault_button;
	public static String TodoTaskConfigurationBlock_casesensitive_label;
	public static String TodoTaskConfigurationBlock_tasks_default;

	public static String TodoTaskInputDialog_new_title;
	public static String TodoTaskInputDialog_edit_title;
	public static String TodoTaskInputDialog_name_label;
	public static String TodoTaskInputDialog_priority_label;
	public static String TodoTaskInputDialog_priority_high;
	public static String TodoTaskInputDialog_priority_normal;
	public static String TodoTaskInputDialog_priority_low;
	public static String TodoTaskInputDialog_error_enterName;
	public static String TodoTaskInputDialog_error_comma;
	public static String TodoTaskInputDialog_error_entryExists;
	public static String TodoTaskInputDialog_error_noSpace;

	public static String SpellingPreferencePage_empty_threshold;
	public static String SpellingPreferencePage_invalid_threshold;
	public static String SpellingPreferencePage_ignore_digits_label;
	public static String SpellingPreferencePage_ignore_mixed_label;
	public static String SpellingPreferencePage_ignore_sentence_label;
	public static String SpellingPreferencePage_ignore_upper_label;
	public static String SpellingPreferencePage_ignore_url_label;
	public static String SpellingPreferencePage_ignore_non_letters_label;
	public static String SpellingPreferencePage_ignore_single_letters_label;
	public static String SpellingPreferencePage_ignore_string_literals_label;
	public static String SpellingPreferencePage_proposals_threshold;
	public static String SpellingPreferencePage_problems_threshold;
	public static String SpellingPreferencePage_dictionary_label;
	public static String SpellingPreferencePage_encoding_label;
	public static String SpellingPreferencePage_workspace_dictionary_label;
	public static String SpellingPreferencePage_browse_label;
	public static String SpellingPreferencePage_dictionary_error;
	public static String SpellingPreferencePage_dictionary_none;
	public static String SpellingPreferencePage_locale_error;
	public static String SpellingPreferencePage_filedialog_title;
	public static String SpellingPreferencePage_enable_contentassist_label;
	public static String SpellingPreferencePage_group_user;
	public static String SpellingPreferencePage_group_dictionary;
	public static String SpellingPreferencePage_group_dictionaries;
	public static String SpellingPreferencePage_group_advanced;
	public static String SpellingPreferencePage_user_dictionary_description;
	public static String SpellingPreferencePage_variables;

	public static String LanguageMappings_missingLanguageTitle;

	public static String WorkspaceLanguagesPreferencePage_description;
	public static String WorkspaceLanguagesPreferencePage_missingLanguage;
	public static String WorkspaceLanguagesPreferencePage_mappingTableTitle;

	public static String ProjectLanguagesPropertyPage_description;
	public static String ProjectLanguagesPropertyPage_configurationColumn;
	public static String ProjectLanguagesPropertyPage_contentTypeColumn;
	public static String ProjectLanguagesPropertyPage_languageColumn;
	public static String ProjectLanguagesPropertyPage_addMappingButton;
	public static String ProjectLanguagesPropertyPage_removeMappingButton;
	public static String ProjectLanguagesPropertyPage_inheritedWorkspaceMappingsGroup;
	public static String ProjectLanguagesPropertyPage_overriddenContentType;
	public static String ProjectLanguagesPropertyPage_missingLanguage;
	public static String ProjectLanguagesPropertyPage_mappingTableTitle;

	public static String ContentTypeMappingsDialog_title;
	public static String ContentTypeMappingsDialog_configuration;
	public static String ContentTypeMappingsDialog_contentType;
	public static String ContentTypeMappingsDialog_language;
	public static String ContentTypeMappingsDialog_allConfigurations;

	public static String FileLanguagesPropertyPage_contentTypeLabel;
	public static String FileLanguagesPropertyPage_inheritedFromSystem;
	public static String FileLanguagesPropertyPage_inheritedFromProject;
	public static String FileLanguagesPropertyPage_inheritedFromWorkspace;
	public static String FileLanguagesPropertyPage_inheritedFromFile;
	public static String FileLanguagesPropertyPage_description;
	public static String FileLanguagesPropertyPage_configurationColumn;
	public static String FileLanguagesPropertyPage_defaultMapping;
	public static String FileLanguagesPropertyPage_missingLanguage;
	public static String FileLanguagesPropertyPage_mappingTableTitle;

	public static String CPluginPreferencePage_0;
	public static String CPluginPreferencePage_1;
	public static String CPluginPreferencePage_2;
	public static String CPluginPreferencePage_3;
	public static String CPluginPreferencePage_4;
	public static String CPluginPreferencePage_5;
	public static String CPluginPreferencePage_7;
	public static String CPluginPreferencePage_caption;
	public static String CPluginPreferencePage_cdtDialogs_group;
	public static String CPluginPreferencePage_clear_button;
	public static String CPluginPreferencePage_clearDoNotShowAgainSettings_label;
	public static String CPluginPreferencePage_structuralParseMode_label;
	public static String CPluginPreferencePage_note;
	public static String CPluginPreferencePage_performanceHint;

	public static String PropertyAndPreferencePage_useworkspacesettings_change;
	public static String PropertyAndPreferencePage_showprojectspecificsettings_label;
	public static String PropertyAndPreferencePage_useprojectsettings_label;

	public static String ProjectSelectionDialog_title;
	public static String ProjectSelectionDialog_desciption;
	public static String ProjectSelectionDialog_filter;

	public static String CodeTemplatesPreferencePage_title;
	public static String CodeTemplateBlock_templates_comment_node;
	public static String CodeTemplateBlock_templates_code_node;
	public static String CodeTemplateBlock_templates_file_node;
	public static String CodeTemplateBlock_methodstub_label;
	public static String CodeTemplateBlock_constructorstub_label;
	public static String CodeTemplateBlock_destructorstub_label;
	public static String CodeTemplateBlock_typecomment_label;
	public static String CodeTemplateBlock_fieldcomment_label;
	public static String CodeTemplateBlock_filecomment_label;
	public static String CodeTemplateBlock_methodcomment_label;
	public static String CodeTemplateBlock_constructorcomment_label;
	public static String CodeTemplateBlock_destructorcomment_label;
	public static String CodeTemplateBlock_templates_new_button;
	public static String CodeTemplateBlock_templates_edit_button;
	public static String CodeTemplateBlock_templates_remove_button;
	public static String CodeTemplateBlock_templates_import_button;
	public static String CodeTemplateBlock_templates_export_button;
	public static String CodeTemplateBlock_templates_exportall_button;
	public static String CodeTemplateBlock_createcomment_label;
	public static String CodeTemplateBlock_templates_label;
	public static String CodeTemplateBlock_preview;
	public static String CodeTemplateBlock_import_title;
	public static String CodeTemplateBlock_import_extension;
	public static String CodeTemplateBlock_export_title;
	public static String CodeTemplateBlock_export_filename;
	public static String CodeTemplateBlock_export_extension;
	public static String CodeTemplateBlock_export_exists_title;
	public static String CodeTemplateBlock_export_exists_message;
	public static String CodeTemplateBlock_error_read_title;
	public static String CodeTemplateBlock_error_read_message;
	public static String CodeTemplateBlock_error_parse_message;
	public static String CodeTemplateBlock_error_write_title;
	public static String CodeTemplateBlock_error_write_message;
	public static String CodeTemplateBlock_export_error_title;
	public static String CodeTemplateBlock_export_error_hidden;
	public static String CodeTemplateBlock_export_error_canNotWrite;

	public static String EditTemplateDialog_error_noname;
	public static String EditTemplateDialog_error_invalidName;
	public static String EditTemplateDialog_title_new;
	public static String EditTemplateDialog_title_edit;
	public static String EditTemplateDialog_name;
	public static String EditTemplateDialog_description;
	public static String EditTemplateDialog_contextType;
	public static String EditTemplateDialog_pattern;
	public static String EditTemplateDialog_insert_variable;
	public static String EditTemplateDialog_undo;
	public static String EditTemplateDialog_redo;
	public static String EditTemplateDialog_cut;
	public static String EditTemplateDialog_copy;
	public static String EditTemplateDialog_paste;
	public static String EditTemplateDialog_select_all;
	public static String EditTemplateDialog_content_assist;
//	public static String EditTemplateDialog_autoinsert;

	public static String MarkOccurrencesConfigurationBlock_title;
	public static String MarkOccurrencesConfigurationBlock_link;
	public static String MarkOccurrencesConfigurationBlock_link_tooltip;
	public static String MarkOccurrencesConfigurationBlock_markOccurrences;
	public static String MarkOccurrencesConfigurationBlock_stickyOccurrences;
	
	public static String ScalabilityPreferencePage_description;
	public static String ScalabilityPreferencePage_detection_label;
	public static String ScalabilityPreferencePage_detection_group_label;
	public static String ScalabilityPreferencePage_trigger_lines_label;
	public static String ScalabilityPreferencePage_error;
	public static String ScalabilityPreferencePage_scalabilityMode_group_label;
	public static String ScalabilityPreferencePage_scalabilityMode_label;
	public static String ScalabilityPreferencePage_reconciler_label;
	public static String ScalabilityPreferencePage_syntaxColor_label;
	public static String ScalabilityPreferencePage_semanticHighlighting_label;
	public static String ScalabilityPreferencePage_contentAssist_label;
	public static String ScalabilityPreferencePage_note;
	public static String ScalabilityPreferencePage_preferenceOnlyForNewEditors;
	public static String ScalabilityPreferencePage_contentAssist_autoActivation;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, PreferencesMessages.class);
	}
}