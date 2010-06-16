/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.ui.ICDebugUIConstants;

/**
 * Constants defining the keys to be used for accessing preferences inside the debug ui plugin's preference bundle.
 * In descriptions (of the keys) below describe the preference stored at the given key. 
 * The type indicates type of the stored preferences
 * The preference store is loaded by the plugin (CDebugUIPlugin).
 * @see CDebugUIPlugin.initializeDefaultPreferences(IPreferenceStore) - for initialization of the store
 */
public interface ICDebugPreferenceConstants {

	/**
	 * Boolean preference controlling whether primitive types display hexadecimal values.
	 */
	public static final String PREF_SHOW_HEX_VALUES = ICDebugUIConstants.PLUGIN_ID + ".cDebug.showHexValues"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether primitive types display char values.
	 */
	public static final String PREF_SHOW_CHAR_VALUES = ICDebugUIConstants.PLUGIN_ID + ".cDebug.showCharValues"; //$NON-NLS-1$

    /**
     * Boolean preference controlling whether the disassembly instructions is to be shown in the disassembly window.
     */
    public static final String PREF_DISASM_SHOW_INSTRUCTIONS = ICDebugUIConstants.PLUGIN_ID + ".disassembly.showInstructions"; //$NON-NLS-1$

    /**
     * Boolean preference controlling whether the source lines is to be shown in the disassembly window.
     */
    public static final String PREF_DISASM_SHOW_SOURCE = ICDebugUIConstants.PLUGIN_ID + ".disassembly.showSource"; //$NON-NLS-1$

    /**
     * Boolean preference controlling whether the disassembly editor is be activated if the source information is not available.
     */
    public static final String PREF_DISASM_OPEN_NO_SOURCE_INFO = ICDebugUIConstants.PLUGIN_ID + ".disassembly.openNoSourceInfo"; //$NON-NLS-1$

    /**
     * Boolean preference controlling whether the disassembly editor is be activated if the source file can't be found.
     */
    public static final String PREF_DISASM_OPEN_SOURCE_NOT_FOUND = ICDebugUIConstants.PLUGIN_ID + ".disassembly.openSourceNotFound"; //$NON-NLS-1$
}
