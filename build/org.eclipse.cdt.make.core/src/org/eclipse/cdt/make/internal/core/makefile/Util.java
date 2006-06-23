/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

/**
 * Utility methods.
 */
public class Util {

	private Util() {
	}

	public static boolean isCommand(String line) {
		return line.length() > 1 && line.startsWith("\t"); //$NON-NLS-1$
	}

	public static boolean isEscapedLine(String line) {
		return (line.endsWith("\\") && !line.endsWith("\\\\")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isEmptyLine(String line) {
		return line.trim().length() == 0;
	}

	public static int indexOfComment(String line) {
		boolean escaped = false;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == '#' && !escaped) {
				return i;
			}
			escaped = line.charAt(i) == '\\';
		}
		return -1;
	}

	public static boolean isSpace(char c) {
		return (c == ' '  || c == '\t' || c == '\r' || c == '\n');
	}

	public static int indexOf(String line, char c) {
		return indexOf(line, new Character(c).toString());
	}

	/**
	 * Special indexOf() method that makes sure that what we are searching
	 * is not between parentheses and brackets like a macro $(foo) ${bar}
	 */
	public static int indexOf(String line, String tokens) {
		int paren = 0;
		int bracket = 0;
		char prev = 0;
		char pprev = 0;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (ch == '(' && prev == '$' && pprev != '\\') {
				paren++;
			} else if (ch == '{' && prev == '$'  && pprev != '\\') {
				bracket++;
			} else if (ch == ')' && prev != '\\') {
				if (paren > 0) {
					paren--;
				}
			} else if (ch == '}' && prev != '\\') {
				if (bracket > 0) {
					bracket--;
				}
			} else if (tokens.indexOf(ch) != -1) {
				if (paren == 0 && bracket == 0) {
					return i;
				}
			}
			pprev = prev;
			prev = ch;
		}
		return -1;
	}

}
