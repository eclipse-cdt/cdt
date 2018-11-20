/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;

public interface IInternalCDebugUIConstants {

	/**
	 * C/C++ Debug UI plug-in identifier (value <code>"org.eclipse.cdt.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = CDebugUIPlugin.getUniqueIdentifier();

	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

	/**
	 * Specifies the conditions under which the disassembly editor will be activated
	 */
	public static final String PREF_OPEN_DISASSEMBLY_MODE = PREFIX + "openDisassemblyMode"; //$NON-NLS-1$

	/**
	 * The name of the font to use for disassembly view. This font is managed via
	 * the workbench font preference page.
	 */
	public static final String DISASSEMBLY_FONT = PREFIX + "disassemblyFont"; //$NON-NLS-1$

	// Current stack frame instruction pointer
	public static final String DISASM_INSTRUCTION_POINTER = PREFIX + "disassemblyInstructionPointer"; //$NON-NLS-1$

	// marker types for instruction pointer annotations - top stack frame, and secondary
	public static final String DISASM_INSTRUCTION_POINTER_CURRENT = PREFIX + "disassemblyInstructionPointer.current"; //$NON-NLS-1$
	public static final String DISASM_INSTRUCTION_POINTER_SECONDARY = PREFIX
			+ "disassemblyInstructionPointer.secondary"; //$NON-NLS-1$

	// annotation types for instruction pointers
	public static final String ANN_DISASM_INSTR_POINTER_CURRENT = PREFIX + "currentDisassemblyIP"; //$NON-NLS-1$
	public static final String ANN_DISASM_INSTR_POINTER_SECONDARY = PREFIX + "secondaryDisassemblyIP"; //$NON-NLS-1$

	// object images
	public static final String IMG_OBJS_DISASM_INSTRUCTION_POINTER_TOP = "IMG_OBJS_DISASM_INSTRUCTION_POINTER_TOP"; //$NON-NLS-1$
	public static final String IMG_OBJS_DISASM_INSTRUCTION_POINTER = "IMG_OBJS_DISASM_INSTRUCTION_POINTER"; //$NON-NLS-1$

	// action ids
	public static final String ACTION_TOGGLE_BREAKPOINT = PREFIX + "toggleBreakpoint"; //$NON-NLS-1$
	public static final String ACTION_ENABLE_DISABLE_BREAKPOINT = PREFIX + "enableDisableBreakpoint"; //$NON-NLS-1$
	public static final String ACTION_BREAKPOINT_PROPERTIES = PREFIX + "breakpointProperties"; //$NON-NLS-1$

	/**
	 * The name of the font to use for detail panes. This font is managed via
	 * the workbench font preference page.
	 */
	public static final String DETAIL_PANE_FONT = PREFIX + "ModulesDetailPaneFont"; //$NON-NLS-1$

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 150;

	// new disassembly

	public static final String DISASM_DISPLAY_MODE_INSTRUCTIONS = "instructions"; //$NON-NLS-1$
	public static final String DISASM_DISPLAY_MODE_SOURCE = "source"; //$NON-NLS-1$

	// wizard images
	public static final String IMG_ADD_COMP_DIR_WIZ = "IMG_ADD_SRC_DIRECTORY"; //$NON-NLS-1$
	public static final String IMG_EDIT_COMP_DIR_WIZ = "IMG_EDIT_SRC_DIRECTORY"; //$NON-NLS-1$
}
