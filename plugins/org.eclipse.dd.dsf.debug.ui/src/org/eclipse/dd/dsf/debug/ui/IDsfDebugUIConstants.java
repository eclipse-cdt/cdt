/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui;

import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDsfDebugUIConstants {

	/**
	 * Debug UI plug-in identifier (value <code>"org.eclipse.dd.dsf.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.dd.dsf.debug.ui"; //$NON-NLS-1$;
	
	/** Loaded shared library symbols image identifier. */
	public static final String IMG_OBJS_SHARED_LIBRARY_SYMBOLS_LOADED = "icons/library_syms_obj.gif"; //$NON-NLS-1$
	
	/** Unloaded Shared library symbols image identifier. */
	public static final String IMG_OBJS_SHARED_LIBRARY_SYMBOLS_UNLOADED = "icons/library_obj.gif"; //$NON-NLS-1$
	
	/**
	 * The orientation of the detail view in the VariablesView
	 */
	public static final String VARIABLES_DETAIL_PANE_ORIENTATION = "Variables.detail.orientation"; //$NON-NLS-1$
	public static final String EXPRESSIONS_DETAIL_PANE_ORIENTATION = "Expressions.detail.orientation"; //$NON-NLS-1$
	public static final String REGISTERS_DETAIL_PANE_ORIENTATION = "Registers.detail.orientation"; //$NON-NLS-1$
    public static final String MODULES_DETAIL_PANE_ORIENTATION = "Modules.detail.orientation"; //$NON-NLS-1$
	public static final String VARIABLES_DETAIL_PANE_RIGHT = "Variables.detail.orientation.right"; //$NON-NLS-1$
	public static final String VARIABLES_DETAIL_PANE_UNDERNEATH = "Variables.detail.orientation.underneath"; //$NON-NLS-1$
	public static final String VARIABLES_DETAIL_PANE_HIDDEN = "Variables.detail.orientation.hidden"; //$NON-NLS-1$
	
	/**
	 * Boolean preference controlling whether the text in the detail panes is
	 * wrapped. When <code>true</code> the text in the detail panes will be
	 * wrapped in new variable view.
	 */
	public static final String PREF_DETAIL_PANE_WORD_WRAP = PLUGIN_ID + ".detail_pane_word_wrap"; //$NON-NLS-1$
	
	/**
	 * Maximum number of characters to display in the details area of the variables
	 * view, or 0 if unlimited.
	 */
	public static final String PREF_MAX_DETAIL_LENGTH = PLUGIN_ID + ".max_detail_length"; //$NON-NLS-1$
	
	/**
     * The name of the font to use for detail panes. This font is managed via
     * the workbench font preference page.
     */ 
    public static final String DETAIL_PANE_FONT= PLUGIN_ID + "DetailPaneFont"; //$NON-NLS-1$ 

	/**
	 * Integer preference to control the maximum amount of stack frames to
	 * retrieve from the backend. Default value is 10.
	 * @see {@link #PREF_STACK_FRAME_LIMIT_ENABLE}
	 * 
	 * @since 1.1
	 */
	public static final String PREF_STACK_FRAME_LIMIT = "stackFrameLimit"; //$NON-NLS-1$
	
	/**
	 * Boolean preference whether to apply the stack frame limit preference. Default is <code>true</code>.
	 * @see {@link #PREF_STACK_FRAME_LIMIT}
	 * 
	 * @since 1.1
	 */
	public static final String PREF_STACK_FRAME_LIMIT_ENABLE = "stackFrameLimitEnable"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to keep stepping speed in sync with UI updates. Default is <code>true</code>.
	 * 
	 * @since 1.1
	 */
	public static final String PREF_SYNCHRONIZED_STEPPING_ENABLE = "synchronizedSteppingEnable"; //$NON-NLS-1$

	/**
	 * Integer preference to enforce a minimum time interval between steps. Default is <code>0</code>.
	 * 
	 * @since 1.1
	 */
	public static final String PREF_MIN_STEP_INTERVAL= "minStepInterval"; //$NON-NLS-1$
	
	/**
	 * Boolean preference whether to wait for view update before continuing run control requests
	 * 
	 * @since 1.1
	 */
	public static final String PREF_ATOMIC_UPDATE_ENABLE = "atomicUpdateEnable"; //$NON-NLS-1$

    /**
     * Help prefixes.
     */
    public static final String PREFIX = IDebugUIConstants.PLUGIN_ID + "."; //$NON-NLS-1$
    
	public static final String DETAIL_PANE = PREFIX + "detail_pane_context"; //$NON-NLS-1$
	public static final String DETAIL_PANE_ASSIGN_VALUE_ACTION = PREFIX + "detail_pane_assign_value_action_context"; //$NON-NLS-1$
	public static final String DETAIL_PANE_CONTENT_ASSIST_ACTION = PREFIX + "detail_pane_content_assist_action_context"; //$NON-NLS-1$
	public static final String DETAIL_PANE_CUT_ACTION = PREFIX + "detail_pane_cut_action_context"; //$NON-NLS-1$
	public static final String DETAIL_PANE_COPY_ACTION = PREFIX + "detail_pane_copy_action_context"; //$NON-NLS-1$
	public static final String DETAIL_PANE_PASTE_ACTION = PREFIX + "detail_pane_paste_action_context"; //$NON-NLS-1$
	public static final String DETAIL_PANE_SELECT_ALL_ACTION = PREFIX + "detail_pane_select_all_action_context"; //$NON-NLS-1$
	public static final String DETAIL_PANE_FIND_REPLACE_ACTION = PREFIX + "detail_pane_find_replace_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_WORD_WRAP_ACTION = PREFIX + "detail_pane_word_wrap_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_MAX_LENGTH_ACTION = PREFIX + "detail_pane_max_length_action_context"; //$NON-NLS-1$

    /**
	 * @since 1.1
	 */
    public static final String PREFERENCE_PAGE= PREFIX + "preference_page_context"; //$NON-NLS-1$
}
