package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.ui.*;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Help context ids for the c ui.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
public interface ICHelpContextIds {
	public static final String PREFIX= CUIPlugin.PLUGIN_ID + ".";

	// Wizard pages
	public static final String NEW_CPROJECT_WIZARD_PAGE= PREFIX + "new_cproject_wizard_page_context";
	public static final String NEW_LAUNCH_WIZARD_PAGE= PREFIX + "new_launch_wizard_page_context";


	// Preference/property pages
	public static final String C_PREF_PAGE= PREFIX + "new_c_pref_page_context";
	public static final String C_EDITOR_PREF_PAGE= PREFIX + "new_c_editor_pref_page_context";
	public static final String TEMPLATE_PREFERENCE_PAGE= PREFIX + "new_c_templates_pref_page_context";
	public static final String LAUNCH_PROPERTY_PAGE= PREFIX + "new_launch_property_page_context";
	public static final String PROJECT_PROPERTY_PAGE= PREFIX + "new_project_property_page_context";

	public static final String PROJ_CONF_BLOCK= PREFIX + "new_proj_conf_block_context";
	
	// Console view
	public static final String CLEAR_CONSOLE_ACTION= PREFIX + "clear_console_action_context";
	public static final String CLEAR_CONSOLE_VIEW= PREFIX + "clear_console_view_context";

	public static final String TOGGLE_PRESENTATION_ACTION= 	PREFIX + "toggle_presentation_action_context"; //$NON-NLS-1$
	public static final String TOGGLE_TEXTHOVER_ACTION= PREFIX + "toggle_texthover_action_context"; //$NON-NLS-1$

	public static final String COLLAPSE_ALL_ACTION= 	PREFIX + "collapse_all_action"; //$NON-NLS-1$

}