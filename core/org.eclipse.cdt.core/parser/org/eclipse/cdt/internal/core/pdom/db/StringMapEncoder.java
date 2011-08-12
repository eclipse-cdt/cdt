/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A set of static methods to encode Map<String, String> as an array of characters and to decode
 * it back.
 *
 * The map is encoded as:
 * <code>&lt;number_of_entries&gt;,&lt;key1&gt;&lt;value1&gt;...&lt;keyN&gt;&lt;valueN&gt;</code>.
 * <p>
 * Each string is encoded as: <code>&lt;number_of_characters&gt;,&lt;characters&gt;</code>.
 * A <code>null</code> string is encoded as a single comma.
 */
public class StringMapEncoder {
	/** Delimiter separating a number in decimal character representation from the remaining text */
	private static final char DELIMITER = ',';
	
	private static class InputCharStream {
		final char[] chars;
		int pos;
		
		InputCharStream(char[] chars) {
			this.chars = chars;
		}

		/**
		 * Decodes a string map encoded using {@link #encode(Map)}
		 */
		public Map<String, String> readMap() {
			int size = readCount();
			if (size == 0) {
				return Collections.emptyMap();
			}
			Map<String, String> map = new HashMap<String, String>(size);
			for (int i = 0; i < size; i++) {
				String key = readString();
				String value = readString();
				map.put(key, value);
			}
			return map;
		}

		private String readString() {
			if (chars[pos] == DELIMITER) {
				pos++;
				return null;
			}
			int length = readCount();
			return new String(chars, pos, length);
		}

		private int readCount() {
			int n = 0;
			while (true) {
				int c = chars[pos++];
				if (c == DELIMITER) {
					break;
				}
				n = n * 10 + c - '0';
			}
			return n;
		}
	}

	private StringMapEncoder() {
	}

	/**
	 * Encodes a string map to an array of characters. Some keys and values can be
	 * <code>null</code>. Use {@link #decodeMap(char[])} to decode.
	 */
	public static char[] encode(Map<String, String> map) {
		StringBuilder buf = new StringBuilder();
		buf.append(map.size());
		buf.append(DELIMITER);
		// Convert to a sorted map before iteration to guarantee canonical serialized representation.
		for (Map.Entry<String, String> entry : new TreeMap<String, String>(map).entrySet()) {
			writeString(entry.getKey(), buf);
			writeString(entry.getValue(), buf);
		}
		char[] chars = new char[buf.length()];
		buf.getChars(0, buf.length(), chars, 0);
		return chars;
	}

	/**
	 * Appends length prefix encoded string to a string builder.
	 * The string can be <code>null</code>.
	 */
	private static void writeString(String str, StringBuilder buf) {
		if (str != null) {
			buf.append(str.length());
		}
		buf.append(DELIMITER);
		if (str != null) {
			buf.append(str);
		}
	}

	/**
	 * Decodes a string map encoded using {@link #encode(Map)}
	 */
	public static Map<String, String> decodeMap(char[] chars) {
		return new InputCharStream(chars).readMap();
	}
}
