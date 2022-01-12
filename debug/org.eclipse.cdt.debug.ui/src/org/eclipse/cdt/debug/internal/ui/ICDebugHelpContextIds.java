/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;

/**
 * Help context ids for the C/C++ debug ui.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 *
 * @since Jul 23, 2002
 */
public interface ICDebugHelpContextIds {
	/**
	 * C/C++ Debug UI plug-in identifier (value <code>"org.eclipse.cdt.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = CDebugUIPlugin.getUniqueIdentifier();

	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

	// Actions
	public static final String CHANGE_REGISTER_VALUE_ACTION = PREFIX + "change_register_value_action_context"; //$NON-NLS-1$
	public static final String SHOW_TYPES_ACTION = PREFIX + "show_types_action_context"; //$NON-NLS-1$
	public static final String REFRESH_MEMORY_ACTION = PREFIX + "refresh_memory_action_context"; //$NON-NLS-1$
	public static final String AUTO_REFRESH_MEMORY_ACTION = PREFIX + "auto_refresh_memory_action_context"; //$NON-NLS-1$
	public static final String MEMORY_CLEAR_ACTION = PREFIX + "memory_clear_action_context"; //$NON-NLS-1$
	public static final String MEMORY_SAVE_ACTION = PREFIX + "memory_save_action_context"; //$NON-NLS-1$
	public static final String MEMORY_SHOW_ASCII_ACTION = PREFIX + "memory_show_ascii_action_context"; //$NON-NLS-1$
	public static final String REFRESH_SHARED_LIBRARIES_ACTION = PREFIX + "refresh_shared_libraries_action_context"; //$NON-NLS-1$
	public static final String AUTO_REFRESH_SHARED_LIBRARIES_ACTION = PREFIX
			+ "auto_refresh_shared_libraries_action_context"; //$NON-NLS-1$
	public static final String LOAD_SYMBOLS_FOR_ALL = PREFIX + "load_symbols_for_all_action_context"; //$NON-NLS-1$
	public static final String REFRESH_REGISTERS_ACTION = PREFIX + "refresh_registers_action_context"; //$NON-NLS-1$
	public static final String AUTO_REFRESH_REGISTERS_ACTION = PREFIX + "auto_refresh_registers_action_context"; //$NON-NLS-1$
	public static final String TOGGLE_BREAKPOINT_ACTION = PREFIX + "manage_breakpoint_action_context"; //$NON-NLS-1$
	public static final String ENABLE_DISABLE_BREAKPOINT_ACTION = PREFIX + "enable_disable_breakpoint_action_context"; //$NON-NLS-1$
	public static final String BREAKPOINT_PROPERTIES_ACTION = PREFIX + "breakpoint_properties_action_context"; //$NON-NLS-1$
	public static final String SHOW_DETAIL_PANE_ACTION = PREFIX + "show_detail_pane_action_context"; //$NON-NLS-1$
	public static final String CONFIGURE_COLUMNS_ACTION = PREFIX + "configure_columns_context"; //$NON-NLS-1$;
	public static final String SHOW_COLUMNS_ACTION = PREFIX + "show_columns_context"; //$NON-NLS-1$;
	public static final String MODULE_DETAIL_PANE_SELECT_ALL_ACTION = PREFIX
			+ "module_detail_pane_select_all_action_context"; //$NON-NLS-1$
	public static final String MODULE_DETAIL_PANE_COPY_ACTION = PREFIX + "detail_pane_copy_action_context"; //$NON-NLS-1$

	// Views & Editors
	/**
	 * The memory view is part of the platform, the context id is assigned there.
	 */
	public static final String MEMORY_VIEW = PREFIX + "MemoryView_context"; //$NON-NLS-1$
	public static final String SHARED_LIBRARIES_VIEW = PREFIX + "shared_libraries_view_context"; //$NON-NLS-1$
	public static final String MODULES_VIEW = PREFIX + "modules_view_context"; //$NON-NLS-1$
	public static final String MODULES_DETAIL_PANE = PREFIX + "modules_detail_pane"; //$NON-NLS-1$
	public static final String SIGNALS_VIEW = PREFIX + "signals_view_context"; //$NON-NLS-1$
	public static final String DISASSEMBLY_VIEW = PREFIX + "disassembly_view_context"; //$NON-NLS-1$
	public static final String SOURCE_NOT_FOUND = PREFIX + "source_not_found"; //$NON-NLS-1$

	// Preference pages
	public static final String SOURCE_PREFERENCE_PAGE = PREFIX + "source_preference_page_context"; //$NON-NLS-1$
	public static final String SHARED_LIBRARIES_PREFERENCE_PAGE = PREFIX + "shared_libraries_preference_page_context"; //$NON-NLS-1$
	public static final String MEMORY_PREFERENCE_PAGE = PREFIX + "memory_preference_page_context"; //$NON-NLS-1$
	public static final String C_DEBUG_PREFERENCE_PAGE = PREFIX + "c_debug_preference_page_context"; //$NON-NLS-1$
	public static final String DEBUGGER_TYPES_PAGE = PREFIX + "debugger_typpes_preference_page_context"; //$NON-NLS-1$

	// Dialogs
	public static final String SOURCE_PATH_MAPPING_DIALOG = PREFIX + "source_path_mapping_dialog_context"; //$NON-NLS-1$
	public static final String SOURCE_PATH_MAP_ENTRY_DIALOG = PREFIX + "source_path_map_entry_dialog_context"; //$NON-NLS-1$
	public static final String ADD_SOURCE_CONTAINER_DIALOG = PREFIX + "add_source_container_dialog"; //$NON-NLS-1$
	public static final String ADD_DIRECTORY_CONTAINER_DIALOG = PREFIX + "add_directory_container_dialog"; //$NON-NLS-1$
	public static final String REGISTER_GROUP = PREFIX + "register_group_dialog"; //$NON-NLS-1$
	public static final String CONFIGURE_COLUMNS_DIALOG = PREFIX + "configure_columns_dialog_context"; //$NON-NLS-1$;
	public static final String COMPILATION_DIRECTORY_SOURCE_CONTAINER_DIALOG = PREFIX
			+ "compilation_directory_source_container_dialog_context"; //$NON-NLS-1$;;
	public static final String ADD_TARGET_PROJECT_CONTAINER_DIALOG = PREFIX + "target_project_source_container_dialog";//$NON-NLS-1$

	// Wizards
	public static final String IMPORT_EXECUTABLE_PAGE_ONE = PREFIX + "import_executable_page_one"; //$NON-NLS-1$
	public static final String IMPORT_EXECUTABLE_PAGE_TWO = PREFIX + "import_executable_page_two"; //$NON-NLS-1$
}
