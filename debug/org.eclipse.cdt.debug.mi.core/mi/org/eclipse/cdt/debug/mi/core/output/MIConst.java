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

package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI const value represents a ios-c string.
 */
public class MIConst extends MIValue {
	String cstring = ""; //$NON-NLS-1$

	public String getCString() {
		return cstring;
	}

	public void setCString(String str) {
		cstring = str;
	}

	/**
	 * Translate gdb c-string.
	 */
	public String getString() {
		return getString(cstring);
	}

	public static String getString(String str) {
		StringBuffer buffer = new StringBuffer();
		boolean escape = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\\') {
				if (escape) {
					buffer.append(c);
					escape = false;
				} else {
					escape = true;
				}
			} else {
				if (escape) {
					if (isIsoCSpecialChar(c)) {
						buffer.append(isoC(c));
					} else {
						buffer.append('\\');
						buffer.append(c);
					}
				} else {
					buffer.append(c);
				}
				escape = false;
			}
		}

		// If escape is still true it means that the
		// last char was an '\'.
		if (escape) {
			buffer.append('\\');
		}

		return buffer.toString();
	}

	public String toString() {
		return getCString();
	}

	/**
	 * Assuming that the precedent character was the
	 * escape sequence '\'
	 */
	private static String isoC(char c) {
		String s = new Character(c).toString();
		if (c == '"') {
			s = "\""; //$NON-NLS-1$
		} else if (c == '\'') {
			s = "\'"; //$NON-NLS-1$
		} else if (c == '?') {
			s = "?"; //$NON-NLS-1$
		} else if (c == 'a') {
			s = "\007"; //$NON-NLS-1$
		} else if (c == 'b') {
			s = "\b"; //$NON-NLS-1$
		} else if (c == 'f') {
			s = "\f"; //$NON-NLS-1$
		} else if (c == 'n') {
			s = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
		} else if (c == 'r') {
			s = "\r"; //$NON-NLS-1$
		} else if (c == 't') {
			s = "\t"; //$NON-NLS-1$
		} else if (c == 'v') {
			s = "\013"; //$NON-NLS-1$
		}
		return s;
	}

	private static boolean isIsoCSpecialChar(char c) {
		switch (c) {
			case '"':
			case '\'':
			case '?':
			case 'a':
			case 'b':
			case 'f':
			case 'n':
			case 'r':
			case 't':
			case 'v':
				return true;
		}
		return false;
		
	}
}
