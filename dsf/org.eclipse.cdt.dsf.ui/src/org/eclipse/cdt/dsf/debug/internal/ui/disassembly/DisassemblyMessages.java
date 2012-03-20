/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Bug fix (326670)
 *     Patrick Chuong (Texas Instruments) - Bug fix (329682)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public final class DisassemblyMessages extends NLS {
	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS = "org.eclipse.cdt.dsf.debug.internal.ui.disassembly.ConstructedDisassemblyMessages";//$NON-NLS-1$
	private static ResourceBundle fgBundleForConstructedKeys = ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);

	/**
	 * Returns the message bundle which contains constructed keys.
	 *
	 * @return the message bundle
	 */
	public static ResourceBundle getBundleForConstructedKeys() {
		return fgBundleForConstructedKeys;
	}

	public static String Disassembly_action_ShowSource_label;
	public static String Disassembly_action_ShowSymbols_label;
	public static String Disassembly_action_GotoPC_label;
	public static String Disassembly_action_GotoPC_tooltip;
	public static String Disassembly_action_GotoAddress_label;
	public static String Disassembly_action_Copy_label;
	public static String Disassembly_action_SelectAll_label;
	public static String Disassembly_action_BreakpointProperties_label;
	public static String Disassembly_action_DisableBreakpoint_label;
	public static String Disassembly_action_EnableBreakpoint_label;
	public static String Disassembly_action_RefreshView_label;
	public static String Disassembly_action_OpenPreferences_label;
	public static String Disassembly_action_Sync_label;
	public static String Disassembly_action_TrackExpression_label;
	public static String Disassembly_GotoAddressDialog_title;
	public static String Disassembly_GotoAddressDialog_label;
	public static String Disassembly_message_notConnected;
	public static String Disassembly_log_error_expression_eval;
	public static String Disassembly_log_error_locateFile;
	public static String Disassembly_log_error_readFile;
	public static String DisassemblyPart_showRulerColumn_label;
	public static String DisassemblyPreferencePage_addressFormatTooltip;
	public static String DisassemblyPreferencePage_addressRadix;
	public static String DisassemblyPreferencePage_showAddressRadix;
	public static String DisassemblyPreferencePage_showSource;
	public static String DisassemblyPreferencePage_showSourceTooltip;
	public static String DisassemblyPreferencePage_showSymbols;
	public static String DisassemblyPreferencePage_showSymbolsTooltip;
	public static String DisassemblyPreferencePage_error_not_a_number;
	public static String DisassemblyPreferencePage_error_negative_number;
	public static String DisassemblyPreferencePage_radix_octal;
	public static String DisassemblyPreferencePage_radix_decimal;
	public static String DisassemblyPreferencePage_radix_hexadecimal;
	public static String DisassemblyPreferencePage_showFunctionOffsets;
	public static String DisassemblyPreferencePage_showFunctionOffsetsTooltip;
	public static String DisassemblyPreferencePage_showCodeBytes;
	public static String DisassemblyPreferencePage_showCodeBytesTooltip;
	public static String DisassemblyPreferencePage_OpcodeFormat;
	public static String DisassemblyPreferencePage_OpcodeFormatTooltip;
	public static String DisassemblyPreferencePage_showAddress;
	public static String DisassemblyPreferencePage_showAddressTooltip;
	public static String DisassemblyPreferencePage_showRadixTooltip;
	public static String DisassemblyIPAnnotation_primary;
	public static String DisassemblyIPAnnotation_secondary;
	public static String SourceReadingJob_name;
	public static String SourceColorerJob_name;
	public static String EditionFinderJob_name;
	public static String EditionFinderJob_task_get_timestamp;
	public static String EditionFinderJob_task_search_history;
	public static String Disassembly_GotoLocation_initial_text;
	public static String Disassembly_GotoLocation_warning;
	public static String Disassembly_Error_Dialog_title;
	public static String Disassembly_Error_Dialog_ok_button;
	public static String DisassemblyBackendDsf_error_UnableToRetrieveData;

	static {
		NLS.initializeMessages(DisassemblyMessages.class.getName(), DisassemblyMessages.class);
	}

	// Do not instantiate
	private DisassemblyMessages() {
	}
}
