/*******************************************************************************
 * Copyright (c) 2013, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.util;

/**
 * Collection of methods for working with text.
 */
public class TextUtil {
	/** Not instantiatable. */
	private TextUtil() {
	}

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
	 * Returns the offset of the beginning of the line before the one containing the given offset,
	 * or the beginning of the line containing the given offset if it is first in the text.
	 */
	public static int getPreviousLineStart(String text, int offset) {
		offset = getLineStart(text, offset);
		if (offset != 0)
			offset = getLineStart(text, offset);
		return offset;
	}

	/**
	 * Returns {@code true} if the line corresponding to the given {@code offset} does not contain
	 * non-whitespace characters.
	 */
	public static boolean isLineBlank(String text, int offset) {
		while (--offset >= 0) {
			if (text.charAt(offset) == '\n')
				break;
		}
		while (++offset < text.length()) {
			char c = text.charAt(offset);
			if (c == '\n')
				return true;
			if (!Character.isWhitespace(c))
				return false;
		}
		return true;
	}

	/**
	 * Returns {@code true} if the line prior to the line corresponding to the given {@code offset}
	 * does not contain non-whitespace characters.
	 */
	public static boolean isPreviousLineBlank(String text, int offset) {
		while (--offset >= 0) {
			if (text.charAt(offset) == '\n')
				break;
		}
		if (offset < 0)
			return false;

		while (--offset >= 0) {
			char c = text.charAt(offset);
			if (c == '\n')
				return true;
			if (!Character.isWhitespace(c))
				return false;
		}
		return true;
	}

	/**
	 * Returns the beginning offset of the first blank line contained between the two given offsets.
	 * Returns -1, if not found.
	 */
	public static int findBlankLine(String text, int startOffset, int endOffset) {
		int blankOffset = startOffset == 0 || text.charAt(startOffset - 1) == '\n' ? startOffset : -1;
		while (startOffset < endOffset) {
			char c = text.charAt(startOffset++);
			if (c == '\n') {
				if (blankOffset >= 0)
					return blankOffset;
				blankOffset = startOffset;
			} else if (!Character.isWhitespace(c)) {
				blankOffset = -1;
			}
		}
		return -1;
	}

	/**
	 * Returns an escaped version of the string 'input' where instances of the
	 * character 'specialChar' are escaped by replacing them with a two instances
	 * of 'specialChar'.
	 */
	public static String escape(String input, char specialChar) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char ch = input.charAt(i);
			if (ch == specialChar) {
				builder.append(specialChar);
			}
			builder.append(ch);
		}
		return builder.toString();
	}
}
