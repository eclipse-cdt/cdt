/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;



/*
 * Color constants that we use for the preferences
 */
 
public interface ICColorConstants {
	/* The prefix all color constants start with */
	String PREFIX= "c_"; //$NON-NLS-1$
	
	/* The color key for multi-line comments in C code. */
	String C_MULTI_LINE_COMMENT= "c_multi_line_comment"; //$NON-NLS-1$
	/* The color key for single-line comments in C code. */
	String C_SINGLE_LINE_COMMENT= "c_single_line_comment"; //$NON-NLS-1$
	/* The color key for keywords in C code. */
	String C_KEYWORD= "c_keyword"; //$NON-NLS-1$
	/* The color key for builtin types in C code. */
	String C_TYPE= "c_type"; //$NON-NLS-1$
	/* The color key for string and character literals in C code. */
	String C_STRING= "c_string"; //$NON-NLS-1$
    /** The color key for operators. */
    String C_OPERATOR = "c_operators";
    /** The color key for braces. */
    String C_BRACES = "c_braces";
    /** The color key for numbers. */
    String C_NUMBER = "c_numbers";
	/* The color key for everthing in C code for which no other color is specified. */
	String C_DEFAULT= "c_default"; //$NON-NLS-1$
    
    /**
     * The color key for task tags in C comments
     * (value <code>"c_comment_task_tag"</code>).
     */
    String TASK_TAG= "c_comment_task_tag"; //$NON-NLS-1$
}


