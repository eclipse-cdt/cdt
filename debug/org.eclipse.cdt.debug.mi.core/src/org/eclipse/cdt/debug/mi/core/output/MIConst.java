package org.eclipse.cdt.debug.mi.core.output;

/**
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
	private static char isoC(char c) {
		if (c == '"') {
			c = '"';
		} else if (c == '\'') {
			c = '\'';
		} else if (c == '?') {
			c = '?';
		} else if (c == 'a') {
			c = 7;
		} else if (c == 'b') {
			c = '\b';
		} else if (c == 'f') {
			c = '\f';
		} else if (c == 'n') {
			c = '\n';
		} else if (c == 'r') {
			c = '\r';
		} else if (c == 't') {
			c = '\t';
		} else if (c == 'v') {
			c = 11;
		}
		return c;
	}
}
