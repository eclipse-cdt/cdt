/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.ui;

public interface IInternalCDebugUIConstants {

	/**
	 * The name of the font to use for disassembly view. This font is managed via
	 * the workbench font preference page.
	 */ 
	public static final String DISASSEMBLY_FONT = "org.eclipse.cdt.debug.ui.DisassemblyFont"; //$NON-NLS-1$	

	//Current stack frame instruction pointer
	public static final String DISASM_INSTRUCTION_POINTER = "org.eclipse.cdt.debug.ui.disassemblyInstructionPointer"; //$NON-NLS-1$

	// marker types for instruction pointer annotations - top stack frame, and secondary
	public static final String DISASM_INSTRUCTION_POINTER_CURRENT = "org.eclipse.cdt.debug.ui.disassemblyInstructionPointer.current"; //$NON-NLS-1$
	public static final String DISASM_INSTRUCTION_POINTER_SECONDARY = "org.eclipse.cdt.debug.ui.disassemblyInstructionPointer.secondary"; //$NON-NLS-1$

	// annotation types for instruction pointers
	public static final String ANN_DISASM_INSTR_POINTER_CURRENT = "org.eclipse.cdt.debug.ui.currentDisassemblyIP"; //$NON-NLS-1$
	public static final String ANN_DISASM_INSTR_POINTER_SECONDARY = "org.eclipse.cdt.debug.ui.secondaryDisassemblyIP"; //$NON-NLS-1$

	// object images
	public static final String IMG_OBJS_DISASM_INSTRUCTION_POINTER_TOP = "IMG_OBJS_DISASM_INSTRUCTION_POINTER_TOP"; //$NON-NLS-1$
	public static final String IMG_OBJS_DISASM_INSTRUCTION_POINTER = "IMG_OBJS_DISASM_INSTRUCTION_POINTER"; //$NON-NLS-1$
}
