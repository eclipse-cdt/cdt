/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI const value represents a ios-c string.
 */
public class MIConst extends MIValue {
	String cstring = "";

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
					buffer.append(isoC(c));
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
			s = "\"";
		} else if (c == '\'') {
			s = "\'";
		} else if (c == '?') {
			s = "?";
		} else if (c == 'a') {
			s = "\007";
		} else if (c == 'b') {
			s = "\b";
		} else if (c == 'f') {
			s = "\f";
		} else if (c == 'n') {
			s = System.getProperty("line.separator", "\n");
		} else if (c == 'r') {
			s = "\r";
		} else if (c == 't') {
			s = "\t";
		} else if (c == 'v') {
			s = "\013";
		}
		return s;
	}
}
