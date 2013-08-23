/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.util;

/**
 * Collection of methods for working with text.
 */
public class TextUtil {
	/** Not instantiatable. */
	private TextUtil() {}

	/**
	 * Returns the offset of the beginning of the next line after the given offset,
	 * or the end-of-file offset if there is no line delimiter after the given offset.
	 */
	public static int skipToNextLine(String text, int offset) {
		while (offset < text.length()) {
			if (text.charAt(offset++) == '\n')
				break;
		}
		return offset;
	}

	/**
	 * Returns the offset of the beginning of the line containing the given offset.
	 */
	public static int getLineStart(String text, int offset) {
		while (--offset >= 0) {
			if (text.charAt(offset) == '\n')
				break;
		}
		return offset + 1;
	}

	/**
	 * Returns {@code true} the line prior to the line corresponding to the given {@code offset} 
	 * does not contain non-whitespace characters.
	 */
	public static boolean isPreviousLineBlank(String text, int offset) {
		while (--offset >= 0) {
			if (text.charAt(offset) == '\n')
				break;
		}
		while (--offset >= 0) {
			char c = text.charAt(offset);
			if (c == '\n')
				return true;
			if (!Character.isWhitespace(c))
				return false;
		}
		return false;
	}
}
