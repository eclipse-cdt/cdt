/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.text;


 
/**
 * Color keys used for syntax highlighting C/C++ and Assembly code
 * A <code>IColorManager</code> is responsible for mapping
 * concrete colors to these keys.
 * <p>
 * This interface declares static final fields only; it is not intended to be
 * implemented.
 * </p>
 * @see org.eclipse.cdt.ui.text.IColorManager
 * 
 * @since 5.1
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICColorConstants {
	/** The color key for multi-line comments in C code. */
	String C_MULTI_LINE_COMMENT= "c_multi_line_comment"; //$NON-NLS-1$
	/** The color key for single-line comments in C code. */
	String C_SINGLE_LINE_COMMENT= "c_single_line_comment"; //$NON-NLS-1$
	/** The color key for keywords in C code. */
	String C_KEYWORD= "c_keyword"; //$NON-NLS-1$
	/** The color key for builtin types in C code. */
	String C_TYPE= "c_type"; //$NON-NLS-1$
	/** The color key for string and character literals in C code. */
	String C_STRING= "c_string"; //$NON-NLS-1$
    /** The color key for operators. */
    String C_OPERATOR= "c_operators"; //$NON-NLS-1$
    /** The color key for braces. */
    String C_BRACES= "c_braces"; //$NON-NLS-1$
    /** The color key for numbers. */
    String C_NUMBER= "c_numbers"; //$NON-NLS-1$
    /** The color key for everthing in C code for which no other color is specified. */
	String C_DEFAULT= "c_default"; //$NON-NLS-1$

	/** The color key for preprocessor directives. */
	String PP_DIRECTIVE= "pp_directive"; //$NON-NLS-1$
	/** The color key for preprocessor text not colored otherwise. */
	String PP_DEFAULT= "pp_default"; //$NON-NLS-1$
    /** The color key for preprocessor include files. */
    String PP_HEADER= "pp_header"; //$NON-NLS-1$

	/** The color key for keywords in assembly code. */
	String ASM_DIRECTIVE= "asm_directive"; //$NON-NLS-1$
    /** The color key for assembly labels. */
    String ASM_LABEL= "asm_label"; //$NON-NLS-1$

    /**
     * The color key for task tags in C comments
     * (value <code>"c_comment_task_tag"</code>).
     */
    String TASK_TAG= "c_comment_task_tag"; //$NON-NLS-1$
}


