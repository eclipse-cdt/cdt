/*******************************************************************************
 * Copyright (c) 2011 - 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361363] [TERMINALS] Implement "Pin&Clone" for the "Terminals" view
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.nls;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

/**
 * Terminals plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tm.terminal.view.ui.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	/**
	 * Returns the corresponding string for the given externalized strings
	 * key or <code>null</code> if the key does not exist.
	 *
	 * @param key The externalized strings key or <code>null</code>.
	 * @return The corresponding string or <code>null</code>.
	 */
	public static String getString(String key) {
		if (key != null) {
			try {
				Field field = Messages.class.getDeclaredField(key);
				return (String)field.get(null);
			} catch (Exception e) { /* ignored on purpose */ }
		}

		return null;
	}

	// **** Declare externalized string id's down here *****

	public static String Extension_error_missingRequiredAttribute;
	public static String Extension_error_duplicateExtension;
	public static String Extension_error_invalidExtensionPoint;

	public static String AbstractTriggerCommandHandler_error_executionFailed;

	public static String AbstractAction_error_commandExecutionFailed;

	public static String AbstractConfigurationPanel_delete;
	public static String AbstractConfigurationPanel_deleteButtonTooltip;
	public static String AbstractConfigurationPanel_hosts;
	public static String AbstractConfigurationPanel_encoding;
	public static String AbstractConfigurationPanel_encoding_custom;
	public static String AbstractConfigurationPanel_encoding_custom_title;
	public static String AbstractConfigurationPanel_encoding_custom_message;
	public static String AbstractConfigurationPanel_encoding_custom_error;

	public static String TabTerminalListener_consoleClosed;
	public static String TabTerminalListener_consoleConnecting;

	public static String PinTerminalAction_menu;
	public static String PinTerminalAction_toolTip;

	public static String ToggleCommandFieldAction_menu;
	public static String ToggleCommandFieldAction_toolTip;

	public static String SelectEncodingAction_menu;
	public static String SelectEncodingAction_tooltip;

	public static String ProcessSettingsPage_dialogTitle;
	public static String ProcessSettingsPage_processImagePathSelectorControl_label;
	public static String ProcessSettingsPage_processImagePathSelectorControl_button;
	public static String ProcessSettingsPage_processArgumentsControl_label;
	public static String ProcessSettingsPage_processWorkingDirControl_label;
	public static String ProcessSettingsPage_localEchoSelectorControl_label;

	public static String OutputStreamMonitor_error_readingFromStream;

	public static String InputStreamMonitor_error_writingToStream;

	public static String TerminalService_error_cannotCreateConnector;
	public static String TerminalService_defaultTitle;

	public static String LaunchTerminalSettingsDialog_title;
	public static String LaunchTerminalSettingsDialog_combo_label;
	public static String LaunchTerminalSettingsDialog_group_label;

    public static String TabScrollLockAction_text;
    public static String TabScrollLockAction_tooltip;

    public static String LaunchTerminalSettingsDialog_error_title;
	public static String LaunchTerminalSettingsDialog_error_invalidSettings;
	public static String LaunchTerminalSettingsDialog_error_unknownReason;

	public static String EncodingSelectionDialog_title;

	public static String TabFolderManager_encoding;
	public static String TabFolderManager_state_connected;
	public static String TabFolderManager_state_connecting;
	public static String TabFolderManager_state_closed;

	public static String NoteCompositeHelper_note_label;
}
