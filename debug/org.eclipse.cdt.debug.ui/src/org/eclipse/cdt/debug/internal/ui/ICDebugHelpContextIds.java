/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.ui.ICDebugUIConstants;

/**
 * 
 * Help context ids for the C/C++ debug ui.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 * @since Jul 23, 2002
 */
public interface ICDebugHelpContextIds
{
	public static final String PREFIX = ICDebugUIConstants.PLUGIN_ID + "."; //$NON-NLS-1$

	// Actions
	public static final String CHANGE_REGISTER_VALUE_ACTION = PREFIX + "change_register_value_action_context"; //$NON-NLS-1$
	public static final String SHOW_TYPES_ACTION = PREFIX + "show_types_action_context"; //$NON-NLS-1$
	public static final String REFRESH_MEMORY_ACTION = PREFIX + "refresh_memory_action_context"; //$NON-NLS-1$
	public static final String AUTO_REFRESH_MEMORY_ACTION = PREFIX + "auto_refresh_memory_action_context"; //$NON-NLS-1$
	public static final String MEMORY_CLEAR_ACTION = PREFIX + "memory_clear_action_context"; //$NON-NLS-1$
	public static final String MEMORY_SAVE_ACTION = PREFIX + "memory_save_action_context"; //$NON-NLS-1$
	public static final String MEMORY_SHOW_ASCII_ACTION = PREFIX + "memory_show_ascii_action_context"; //$NON-NLS-1$

	// Views
	public static final String REGISTERS_VIEW = PREFIX + "registers_view_context"; //$NON-NLS-1$
	public static final String MEMORY_VIEW = PREFIX + "memory_view_context"; //$NON-NLS-1$

	// Preference pages
	public static final String MEMORY_PREFERENCE_PAGE = PREFIX + "memory_preference_page_context"; //$NON-NLS-1$
	public static final String REGISTERS_PREFERENCE_PAGE = PREFIX + "registers_preference_page_context"; //$NON-NLS-1$
	public static final String C_DEBUG_PREFERENCE_PAGE = PREFIX + "c_debug_preference_page_context"; //$NON-NLS-1$
}
