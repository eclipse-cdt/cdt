package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


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
	/* The color key for everthing in C code for which no other color is specified. */
	String C_DEFAULT= "c_default"; //$NON-NLS-1$


}


