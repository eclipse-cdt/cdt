/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Help context ids for the c ui.
 * <p>
 * This interface contains constants only; it is not intended to be implemented or extended.
 * </p>
 *  
 */
public interface ICHelpContextIds {
	public static final String PREFIX = CUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	// Wizard pages
	public static final String NEW_CPROJECT_WIZARD_PAGE = PREFIX + "new_cproject_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_LAUNCH_WIZARD_PAGE = PREFIX + "new_launch_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_CLASS_WIZARD_PAGE = PREFIX + "new_class_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_SRCFLDER_WIZARD_PAGE = PREFIX + "new_srcfldr_wizard_page_context"; //$NON-NLS-1$
	public static final String OPEN_CLASS_WIZARD_ACTION = PREFIX + "open_class_wizard_action"; //$NON-NLS-1$
	public static final String OPEN_PROJECT_WIZARD_ACTION = PREFIX + "open_project_wizard_action"; //$NON-NLS-1$
	public static final String CONVERT_TO_CCPP_WIZARD_PAGE = PREFIX + "cdt_t_conv_proj_context"; //$NON-NLS-1$
	public static final String NEW_C_FILE_WIZARD_PAGE = PREFIX + "cdt_creating_cpp_file_context"; //$NON-NLS-1$
	// Actions

	public static final String ADD_INCLUDE_ON_SELECTION_ACTION = PREFIX + "add_includes_on_selection_action_context"; //$NON-NLS-1$;
	public static final String FILTER_PUBLIC_ACTION= PREFIX + "filter_public_action"; //$NON-NLS-1$
	public static final String FILTER_FIELDS_ACTION= PREFIX + "filter_fields_action"; //$NON-NLS-1$
	public static final String FILTER_STATIC_ACTION= PREFIX + "filter_static_action"; //$NON-NLS-1$
	public static final String FILTER_LOCALTYPES_ACTION= PREFIX + "filter_localtypes_action"; //$NON-NLS-1$

	public static final String GOTO_NEXT_ERROR_ACTION= PREFIX + "goto_next_error_action"; 	 //$NON-NLS-1$	
	public static final String GOTO_PREVIOUS_ERROR_ACTION=	PREFIX + "goto_previous_error_action"; 	 //$NON-NLS-1$	
	public static final String TOGGLE_MARK_OCCURRENCES_ACTION= PREFIX + "toggle_mark_occurrences_action_context"; //$NON-NLS-1$
	public static final String FORMAT_ALL= PREFIX + "format_all_action"; 	 //$NON-NLS-1$
	public static final String GOTO_MATCHING_BRACKET_ACTION= PREFIX + "goto_matching_bracket_action"; 	 //$NON-NLS-1$

	// Preference/property pages
	public static final String C_PREF_PAGE = PREFIX + "c_pref"; //$NON-NLS-1$
	public static final String C_EDITOR_PREF_PAGE = PREFIX + "c_editor_gen"; //$NON-NLS-1$
	public static final String C_EDITOR_COLORS_PREF_PAGE = PREFIX + "c_editor_color"; //$NON-NLS-1$
	public static final String C_EDITOR_CONTENT_ASSIST_PREF_PAGE = PREFIX + "c_editor_con_assist"; //$NON-NLS-1$
	public static final String C_EDITOR_HOVERS_PAGE = PREFIX + "c_editor_hov"; //$NON-NLS-1$
	public static final String C_EDITOR_TYPING_PAGE = PREFIX + "c_editor_typing"; //$NON-NLS-1$;
	public static final String C_EDITOR_FOLDING_PAGE = PREFIX + "c_editor_folding"; //$NON-NLS-1$;
	public static final String FILE_TYPES_STD_PAGE = PREFIX + "std_prop_file_types"; //$NON-NLS-1$
	public static final String FILE_TYPES_MAN_PAGE = PREFIX + "std_prop_file_types"; //$NON-NLS-1$
	public static final String FILE_TYPES_PREF_PAGE = PREFIX + "c_file_types"; //$NON-NLS-1$
	public static final String TEMPLATE_PREFERENCE_PAGE = PREFIX + "code_temp"; //$NON-NLS-1$
	public static final String LAUNCH_PROPERTY_PAGE = PREFIX + "new_launch_property_page_context"; //$NON-NLS-1$
	public static final String PROJECT_PROPERTY_PAGE = PREFIX + "new_project_property_page_context"; //$NON-NLS-1$
	public static final String CODEFORMATTER_PREFERENCE_PAGE = PREFIX + "codeformatter_preference_page_context"; //$NON-NLS-1$
	public static final String INDEXER_PREFERENCE_PAGE = PREFIX + "indexer_preference_page"; //$NON-NLS-1$
	public static final String LANGUAGE_MAPPING_PREFERENCE_PAGE = PREFIX + "language_mapping_preference_page"; //$NON-NLS-1$
	public static final String BUILD_CONSOLE_PREFERENCE_PAGE = PREFIX + "build_console_preference_page"; //$NON-NLS-1$

	public static final String PROJ_CONF_BLOCK = PREFIX + "new_proj_conf_block_context"; //$NON-NLS-1$

	public static final String TODO_TASK_INPUT_DIALOG = PREFIX + "todo_task_input_dialog_context"; //$NON-NLS-1$

	public static final String TODO_TASK_PROPERTY_PAGE = PREFIX + "tasktags_property_page_context"; //$NON-NLS-1$
	public static final String TODO_TASK_PREFERENCE_PAGE = PREFIX + "tasktags_preference_page_context"; //$NON-NLS-1$

	public static final String BINARY_PARSER_PAGE = PREFIX + "newproj_parser_binary"; //$NON-NLS-1$
	public static final String ERROR_PARSERS_PAGE = PREFIX + "newproj_parser_error"; //$NON-NLS-1$
	public static final String PROJECT_PATHS_SOURCE = PREFIX + "std_prop_source"; //$NON-NLS-1$
	public static final String PROJECT_PATHS_OUTPUT = PREFIX + "std_prop_output"; //$NON-NLS-1$
	public static final String PROJECT_PATHS_PROJECTS = PREFIX + "std_prop_projects"; //$NON-NLS-1$
	public static final String PROJECT_PATHS_LIBRARIES = PREFIX + "std_prop_libraries"; //$NON-NLS-1$
	public static final String PROJECT_PATHS_CONTAINERS	 = PREFIX + "std_prop_containers"; //$NON-NLS-1$
	public static final String PROJECT_REFERENCES = PREFIX + "std_prop_references";  //$NON-NLS-1$
	public static final String PROJECT_INCLUDE_PATHS_SYMBOLS = PREFIX + "std_prop_include";  //$NON-NLS-1$

	public static final String APPEARANCE_PREFERENCE_PAGE = PREFIX + "appearance_preference_page_context"; //$NON-NLS-1$
	public static final String SPELLING_CONFIGURATION_BLOCK= PREFIX + "spelling_configuration_block_context"; //$NON-NLS-1$
	public static final String CODE_STYLE_PREFERENCE_PAGE = PREFIX + "code_style_preference_context"; //$NON-NLS-1$
	public static final String CODE_TEMPLATES_PREFERENCE_PAGE = PREFIX + "code_templates_preference_context"; //$NON-NLS-1$
	public static final String NAME_STYLE_PREFERENCE_PAGE = PREFIX + "name_style_preference_context"; //$NON-NLS-1$

	// Console view
	public static final String CLEAR_CONSOLE_ACTION = PREFIX + "clear_console_action_context"; //$NON-NLS-1$
	public static final String CLEAR_CONSOLE_VIEW = PREFIX + "clear_console_view_context"; //$NON-NLS-1$

	public static final String TOGGLE_PRESENTATION_ACTION = PREFIX + "toggle_presentation_action_context"; //$NON-NLS-1$
	public static final String TOGGLE_TEXTHOVER_ACTION = PREFIX + "toggle_texthover_action_context"; //$NON-NLS-1$

	public static final String COLLAPSE_ALL_ACTION = PREFIX + "collapse_all_action"; //$NON-NLS-1$

	public static final String C_SEARCH_PAGE = PREFIX + "cdt_u_search"; //$NON-NLS-1$

	public static final String COPY_ACTION = PREFIX + "copy_action_context"; //$NON-NLS-1$

	// Custom Filters
	public static final String CUSTOM_FILTERS_DIALOG= PREFIX + "open_custom_filters_dialog_context"; //$NON-NLS-1$


	public static final String PASTE_ACTION = PREFIX + "paste_action_context"; //$NON_NLS-1$ //$NON-NLS-1$

	public static final String MOVE_ACTION = PREFIX + "move_action_context"; //$NON-NLS-1$
	public static final String RENAME_ACTION = PREFIX + "rename_action_context"; //$NON-NLS-1$

	
	public static final String REFACTORING_PREFERENCE_PAGE= PREFIX + "refactoring_preference_page_context"; //$NON-NLS-1$
	public static final String REFACTORING_ERROR_WIZARD_PAGE=	PREFIX + "refactoring_error_wizard_page_context";  //$NON-NLS-1$
	public static final String REFACTORING_PREVIEW_WIZARD_PAGE= PREFIX + "refactoring_preview_wizard_page_context"; //$NON-NLS-1$	
	public static final String RENAME_PARAMS_WIZARD_PAGE= 	PREFIX + "rename_params_wizard_page"; //$NON-NLS-1$
	public static final String RENAME_METHOD_WIZARD_PAGE= 	PREFIX + "rename_method_wizard_page_context"; //$NON-NLS-1$
	public static final String RENAME_TYPE_WIZARD_PAGE= 	PREFIX + "rename_type_wizard_page_context"; //$NON-NLS-1$
	public static final String RENAME_FIELD_WIZARD_PAGE=	PREFIX + "rename_field_wizard_page_context"; //$NON-NLS-1$
	public static final String RENAME_RESOURCE_WIZARD_PAGE=	PREFIX + "rename_resource_wizard_page_context"; //$NON-NLS-1$

	// Dialogs
	public static final String EDIT_TEMPLATE_DIALOG = PREFIX + "edit_template_dialog_context"; //$NON-NLS-1$
	public static final String OPEN_ELEMENT_DIALOG = PREFIX + "open_element_dialog_context"; //$NON-NLS-1$

	// view parts
	public static final String TYPE_HIERARCHY_VIEW= PREFIX + "type_hierarchy_view_context"; //$NON-NLS-1$
	public static final String CALL_HIERARCHY_VIEW= PREFIX + "call_hierarchy_view_context"; //$NON-NLS-1$
	public static final String INCLUDE_BROWSER_VIEW= PREFIX + "include_browser_view_context"; //$NON-NLS-1$

	public static final String OPEN_ACTION = PREFIX + "open_action"; //$NON-NLS-1$
	public static final String OPEN_PROJECT_ACTION = PREFIX + "open_project_action"; //$NON-NLS-1$

	public static final String OPEN_TYPE_ACTION = PREFIX + "open_type_action"; //$NON-NLS-1$
	public static final String OPEN_TYPE_IN_HIERARCHY_ACTION = PREFIX + "open_type_in_hierarchy_action"; //$NON-NLS-1$
	public static final String OPEN_TYPE_HIERARCHY_ACTION = PREFIX + "open_type_hierarchy_action"; //$NON-NLS-1$	
	public static final String SELECT_ALL_ACTION = PREFIX + "select_all_action"; //$NON-NLS-1$
	public static final String LINK_EDITOR_ACTION = PREFIX + "link_editor_action"; //$NON-NLS-1$
	public static final String TYPEHIERARCHY_HISTORY_ACTION = PREFIX + "typehierarchy_history_action"; //$NON-NLS-1$
	public static final String HISTORY_ACTION = PREFIX + "history_action"; //$NON-NLS-1$
	public static final String HISTORY_LIST_ACTION = PREFIX + "history_list_action"; //$NON-NLS-1$
	public static final String TOGGLE_ORIENTATION_ACTION = PREFIX + "toggle_orientations_action"; //$NON-NLS-1$		
	public static final String FOCUS_ON_TYPE_ACTION = PREFIX + "focus_on_type_action"; //$NON-NLS-1$
	public static final String FOCUS_ON_SELECTION_ACTION = PREFIX + "focus_on_selection_action"; //$NON-NLS-1$

	public static final String HISTORY_LIST_DIALOG = PREFIX + "history_list_dialog_context"; //$NON-NLS-1$	

	public static final String SHOW_INHERITED_ACTION = PREFIX + "show_inherited_action"; //$NON-NLS-1$
	public static final String SHOW_SUPERTYPES = PREFIX + "show_supertypes_action"; //$NON-NLS-1$
	public static final String SHOW_SUBTYPES = PREFIX + "show_subtypes_action"; //$NON-NLS-1$
	public static final String SHOW_HIERARCHY = PREFIX + "show_hierarchy_action"; //$NON-NLS-1$

	public static final String SORT_BY_DEFINING_TYPE_ACTION = PREFIX + "sort_by_defining_type_action"; //$NON-NLS-1$	
	public static final String SHOW_QUALIFIED_NAMES_ACTION = PREFIX + "show_qualified_names_action"; //$NON-NLS-1$	
	public static final String ENABLE_METHODFILTER_ACTION = PREFIX + "enable_methodfilter_action"; //$NON-NLS-1$
	
	public static final String LEXICAL_SORTING_BROWSING_ACTION = PREFIX + "lexical_sorting_browsing_action"; //$NON-NLS-1$
	
	public static final String PROJECT_INDEXER_PROPERTIES = PREFIX +  "std_prop_indexer"; //$NON-NLS-1$
	public static final String CEDITOR_VIEW = PREFIX + "editor_view"; //$NON-NLS-1$
	public static final String COUTLINE_VIEW = PREFIX + "outline_view"; //$NON-NLS-1$
	public static final String CPROJECT_VIEW = PREFIX + "projects_view"; //$NON-NLS-1$
	public static final String C_SEARCH_VIEW = PREFIX + "search_view"; //$NON-NLS-1$

	public static final String SAVE_ACTIONS_PREFERENCE_PAGE = PREFIX + "save_actions_preference_page_context"; //$NON-NLS-1$
	public static final String SCALABILITY_PREFERENCE_PAGE = PREFIX + "scalability_preference_page_context"; //$NON-NLS-1$
}
