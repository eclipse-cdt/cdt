/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.formatter;

import java.util.Map;

import org.eclipse.text.edits.TextEdit;

/**
 * Specification for a generic source code formatter.
 * 
 * @since 3.0
 */
public abstract class CodeFormatter {

	/**
	 * Unknown kind
	 */
	public static final int K_UNKNOWN = 0x00;

	/**
	 * Kind used to format an expression
	 */
	public static final int K_EXPRESSION = 0x01;
	
	/**
	 * Kind used to format a set of statements
	 */
	public static final int K_STATEMENTS = 0x02;
	
	/**
	 * Kind used to format a set of class body declarations
	 */
	public static final int K_CLASS_BODY_DECLARATIONS = 0x04;
	
	/**
	 * Kind used to format a compilation unit
	 */
	public static final int K_COMPILATION_UNIT = 0x08;

	/** 
	 * Format <code>source</code>,
	 * and returns a text edit that correspond to the difference between the given string and the formatted string.
	 * It returns null if the given string cannot be formatted.
	 * 
	 * If the offset position is matching a whitespace, the result can include whitespaces. It would be up to the
	 * caller to get rid of preceeding whitespaces.
	 * 
	 * @param kind Use to specify the kind of the code snippet to format. It can be any of these:
	 * 		  K_EXPRESSION, K_STATEMENTS, K_CLASS_BODY_DECLARATIONS, K_COMPILATION_UNIT, K_UNKNOWN
	 * @param file - file associated with this source (null if no file is associated)
	 * @param source the document to format
	 * @param offset the given offset to start recording the edits (inclusive).
	 * @param length the given length to stop recording the edits (exclusive).
	 * @param indentationLevel the initial indentation level, used 
	 *      to shift left/right the entire source fragment. An initial indentation
	 *      level of zero or below has no effect.
	 * @param lineSeparator the line separator to use in formatted source,
	 *     if set to <code>null</code>, then the platform default one will be used.
	 * @return the text edit
	 * @throws IllegalArgumentException if offset is lower than 0, length is lower than 0 or
	 * length is greater than source length.
	 */
	public abstract TextEdit format(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator);
	
	/**
	 * @param options - general formatter options
	 */
	public abstract void setOptions(Map options);
}
