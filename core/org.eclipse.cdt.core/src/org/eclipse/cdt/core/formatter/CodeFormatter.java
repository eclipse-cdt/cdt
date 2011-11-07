/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.formatter;

import java.util.Map;

import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

/**
 * Specification for a generic source code formatter.
 * 
 * @since 3.0
 */
public abstract class CodeFormatter {
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Unknown kind.
	 */
	public static final int K_UNKNOWN = 0x00;

	/**
	 * Kind used to format an expression.
	 */
	public static final int K_EXPRESSION = 0x01;
	
	/**
	 * Kind used to format a set of statements.
	 */
	public static final int K_STATEMENTS = 0x02;
	
	/**
	 * Kind used to format a set of class body declarations.
	 */
	public static final int K_CLASS_BODY_DECLARATIONS = 0x04;
	
	/**
	 * Kind used to format a compilation unit.
	 * @deprecated Use #K_TRANSLATION_UNIT
	 */
	@Deprecated
	public static final int K_COMPILATION_UNIT = 0x08;
	/**
	 * Kind used to format a translation unit.
	 */
	public static final int K_TRANSLATION_UNIT = 0x08;

	/**
	 * Kind used to format a single-line comment.
	 */
	public static final int K_SINGLE_LINE_COMMENT = 0x10;
	/**
	 * Kind used to format a multi-line comment.
	 */
	public static final int K_MULTI_LINE_COMMENT = 0x20;

	/**
	 * Formats <code>source</code>, and returns a text edit that correspond to the difference
	 * between the given string and the formatted string. It returns null if the given string cannot
	 * be formatted.
	 * 
	 * If the offset position is matching a whitespace, the result can include whitespaces.
	 * It would be up to the caller to get rid of preceding whitespaces.
	 * 
	 * @param kind Use to specify the kind of the code snippet to format. It can be any of these:
	 * 	   K_EXPRESSION, K_STATEMENTS, K_CLASS_BODY_DECLARATIONS, K_TRANSLATION_UNIT, K_UNKNOWN
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
	public abstract TextEdit format(int kind, String source, int offset, int length,
			int indentationLevel, String lineSeparator);

	/**
	 * Formats one or more regions of <code>source</code>, and returns an array of edits, one edit
	 * per region. If some of the regions cannot be formatted, the corresponding elements of
	 * the returned array will be <code>null</code>.
	 * 
	 * If the offset of a region is matching a whitespace, the result can include whitespaces.
	 * It would be up to the caller to get rid of preceding whitespaces.
	 * 
	 * Subclasses may override this method to provide a more efficient implementation.
	 * 
	 * @param kind Use to specify the kind of the code snippet to format. It can be any of these:
	 * 	   K_EXPRESSION, K_STATEMENTS, K_CLASS_BODY_DECLARATIONS, K_TRANSLATION_UNIT, K_UNKNOWN.
	 * @param source the document to format.
	 * @param regions regions of the source to be formatted.
	 * @param lineSeparator the line separator to use in formatted source,
	 *     if set to <code>null</code>, then the platform default one will be used.
	 * @return the text edits, one per region.
	 * @throws IllegalArgumentException if any of the regions is invalid.
	 * @since 5.4
	 */
	public TextEdit[] format(int kind, String source, IRegion[] regions, String lineSeparator) {
		TextEdit[] edits = new TextEdit[regions.length];
		for (int i = 0; i < regions.length; i++) {
			IRegion region = regions[i];
			edits[i] = format(kind, source, region.getOffset(), region.getLength(), 0, lineSeparator);
		}
		return edits;
	}

	/**
	 * @param options - general formatter options
	 */
	public abstract void setOptions(Map<String, ?> options);
  
	/**
	 * Answers the string that corresponds to the indentation to the given indentation level,
	 * or an empty string if the indentation cannot be computed.
	 * <p>This method needs to be overridden in a subclass.</p>
	 * 
	 * <p>The default implementation returns an empty string.</p>
	 * 
	 * @param indentationLevel the given indentation level
	 * @return the string corresponding to the right indentation level
	 * @exception IllegalArgumentException if the given indentation level is lower than zero
	 */
	public String createIndentationString(int indentationLevel) {
		return EMPTY_STRING;
	}
}
