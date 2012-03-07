/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ken Ryall (Nokia) - 207675
 *     Patrick Chuong (Texas Instruments) -	Update CDT ToggleBreakpointTargetFactory enablement (340177)
 *     Mathias Kunter - Support for different charsets (bug 370462)
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

/**
 * Constant definitions for C/C++ debug plug-in.
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICDebugConstants {

	/**
	 * C/C++ debug plug-in identifier (value
	 * <code>"org.eclipse.cdt.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = CDebugCorePlugin.getUniqueIdentifier();

	/**
	 * The identifier of the default variable format to use in the variables
	 * view
	 */
	public static final String PREF_DEFAULT_VARIABLE_FORMAT = PLUGIN_ID + "cDebug.default_variable_format"; //$NON-NLS-1$

	/**
	 * The identifier of the default register format to use in the registers
	 * view
	 */
	public static final String PREF_DEFAULT_REGISTER_FORMAT = PLUGIN_ID + "cDebug.default_register_format"; //$NON-NLS-1$
	
	/**
	 * The charset to use for decoding char type strings. We however can't use the ID
	 * "character_set" here because that would break backwards compatibility as it was
	 * already used for wide charsets.
	 * @since 7.2
	 */
	public static final String PREF_DEBUG_CHARSET = PLUGIN_ID + "cDebug.non_wide_character_set"; //$NON-NLS-1$

	/**
	 * The charset to use for decoding wchar_t type strings. We have to use the ID
	 * "character_set" here so that we don't break backwards compatibility.
	 * @since 7.2
	 */
	public static final String PREF_DEBUG_WIDE_CHARSET = PLUGIN_ID + "cDebug.character_set"; //$NON-NLS-1$

	/**
	 * Deprecated id for the charset used for decoding wchar_t type strings.
	 * Replaced by ICDebugConstants.PREF_DEBUG_WIDE_CHARSET.
	 * @deprecated
	 */
	@Deprecated
	public static final String PREF_CHARSET = PLUGIN_ID + "cDebug.character_set"; //$NON-NLS-1$

	/**
	 * The identifier of the default expression format to use in the expressions
	 * views
	 */
	public static final String PREF_DEFAULT_EXPRESSION_FORMAT = PLUGIN_ID + "cDebug.default_expression_format"; //$NON-NLS-1$

	/**
	 * The identifier of the maximum number of instructions displayed in
	 * disassembly.
	 */
	public static final String PREF_MAX_NUMBER_OF_INSTRUCTIONS = PLUGIN_ID + "cDebug.max_number_of_instructions"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether the search for duplicate source
	 * files will be performed by debugger.
	 */
	public static final String PREF_SEARCH_DUPLICATE_FILES = PLUGIN_ID + "cDebug.Source.search_duplicate_files"; //$NON-NLS-1$

	/**
	 * The identifier of the common source locations list
	 */
	public static final String PREF_SOURCE_LOCATIONS = PLUGIN_ID + "cDebug.Source.source_locations"; //$NON-NLS-1$

	/**
	 * The default number of instructions displayed in disassembly.
	 */
	public static final int DEF_NUMBER_OF_INSTRUCTIONS = 100;

	/**
	 * The minimal valid number of instructions displayed in disassembly.
	 */
	public static final int MIN_NUMBER_OF_INSTRUCTIONS = 1;

	/**
	 * The maximal valid number of instructions displayed in disassembly.
	 */
	public static final int MAX_NUMBER_OF_INSTRUCTIONS = 999;

    /**
     * Preference that saves the default debugger type
     * @since 3.1
     */
    public static final String PREF_DEFAULT_DEBUGGER_TYPE = PLUGIN_ID + ".cDebug.defaultDebugger"; //$NON-NLS-1$

    /**
     * Preference that saves the deactivated debugger types
     * @since 3.1
     */
    public static final String PREF_FILTERED_DEBUGGERS = PLUGIN_ID + ".cDebug.filteredDebuggers"; //$NON-NLS-1$

	/**
	 * Boolean preference used to persist the instruction-stepping mode. The
	 * persistence is global but the mode is per debug target. We update the
	 * persisted global value when a debug session ends, using the mode that
	 * session is in at that time. In other words, the most recently terminated
	 * debug session dictates the initial mode of the next new debug session.
	 * 
	 * Temporary. See bugs 79872 and 80323.
	 */
	public static final String PREF_INSTRUCTION_STEP_MODE_ON = PLUGIN_ID + "cDebug.Disassembly.instructionStepOn"; //$NON-NLS-1$

	/**
	 * The default character set to use.
	 * @deprecated Provided for compatibility reasons only. Use the default value
	 * from the Preferences object instead.
	 */
	@Deprecated
	public static final String DEF_CHARSET = "UTF-16"; //$NON-NLS-1$
	
    /**
     * Specifies the stepping mode (context/source/instruction)
     */
    public static final String PREF_STEP_MODE = PLUGIN_ID + ".steppingMode"; //$NON-NLS-1$

    public static final String PREF_VALUE_STEP_MODE_CONTEXT = "context"; //$NON-NLS-1$
    public static final String PREF_VALUE_STEP_MODE_SOURCE = "source"; //$NON-NLS-1$
    public static final String PREF_VALUE_STEP_MODE_INSTRUCTION = "instruction"; //$NON-NLS-1$
    
    /**
     * Preference key for toggle breakpoint model identifier. Debugger that contribute custom
     * CBreakpoint should set the system property with this key to true. when this system property
     * is set to true, the standard ICBreakpoint toggle breakpoint factory enablement will take 
     * into account for non-standard ICElement input.
     * 
     * @since 7.1
     */
    public static final String PREF_TOGGLE_BREAKPOINT_MODEL_IDENTIFIER = PLUGIN_ID + ".toggleBreakpointModel"; //$NON-NLS-1$    
}
