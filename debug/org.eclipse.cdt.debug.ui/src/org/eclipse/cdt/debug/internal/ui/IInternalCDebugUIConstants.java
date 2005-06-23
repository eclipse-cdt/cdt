/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public interface IInternalCDebugUIConstants {

	/**
	 * C/C++ Debug UI plug-in identifier (value <code>"org.eclipse.cdt.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID = CDebugUIPlugin.getUniqueIdentifier();
	
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

	/**
	 * The name of the font to use for disassembly view. This font is managed via
	 * the workbench font preference page.
	 */ 
	public static final String DISASSEMBLY_FONT = PREFIX + "disassemblyFont"; //$NON-NLS-1$	

	/**
	 * The color id to highlight the source lines in disassembly view. This color is managed via
	 * the workbench font preference page.
	 */ 
	public static final String DISASSEMBLY_SOURCE_LINE_COLOR = PREFIX + "disassembly.sourceLineColor"; //$NON-NLS-1$	
	
	public static final RGB DEFAULT_DISASSEMBLY_SOURCE_LINE_RGB = Display.getDefault().getSystemColor( SWT.COLOR_DARK_BLUE ).getRGB();

	//Current stack frame instruction pointer
	public static final String DISASM_INSTRUCTION_POINTER = PREFIX + "disassemblyInstructionPointer"; //$NON-NLS-1$

	// marker types for instruction pointer annotations - top stack frame, and secondary
	public static final String DISASM_INSTRUCTION_POINTER_CURRENT = PREFIX + "disassemblyInstructionPointer.current"; //$NON-NLS-1$
	public static final String DISASM_INSTRUCTION_POINTER_SECONDARY = PREFIX + "disassemblyInstructionPointer.secondary"; //$NON-NLS-1$

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
}
