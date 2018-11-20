/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.qt.core.Activator;

/**
 * Provides a parser for QMake output.
 */
public final class QMakeParser {

	public static final String KEY_QMAKE_VERSION = "QMAKE_VERSION";
	public static final String KEY_QT_VERSION = "QT_VERSION";
	public static final String KEY_QT_INSTALL_IMPORTS = "QT_INSTALL_IMPORTS";
	public static final String KEY_QT_INSTALL_QML = "QT_INSTALL_QML";
	public static final String KEY_QT_INSTALL_DOCS = "QT_INSTALL_DOCS";
	public static final String KEY_QMAKE_INTERNAL_INCLUDED_FILES = "QMAKE_INTERNAL_INCLUDED_FILES";
	public static final String KEY_SOURCES = "SOURCES";
	public static final String KEY_HEADERS = "HEADERS";
	public static final String KEY_INCLUDEPATH = "INCLUDEPATH";
	public static final String KEY_DEFINES = "DEFINES";
	public static final String KEY_RESOURCES = "RESOURCES";
	public static final String KEY_FORMS = "FORMS";
	public static final String KEY_OTHER_FILES = "OTHER_FILES";
	public static final String KEY_QML_IMPORT_PATH = "QML_IMPORT_PATH";

	/**
	 * Parses QMake output via a specified reg. exp.
	 *
	 * @param regex the reg. exp.
	 * @param reader the QMake output
	 * @return the modifiable map of parsed key-value pairs
	 * @throws IOException when io error happens
	 */
	public static Map<String, String> parse(Pattern regex, BufferedReader reader) throws IOException {
		Map<String, String> result = new LinkedHashMap<>();

		String line;
		while ((line = reader.readLine()) != null) {
			Matcher m = regex.matcher(line);
			if (!m.matches() || m.groupCount() != 2) {
				Activator.log("qmake: cannot decode query line '" + line + '\'');
			} else {
				String key = m.group(1);
				String value = m.group(2);
				String oldValue = result.put(key, value);
				if (oldValue != null)
					Activator.log("qmake: duplicate keys in query info '" + line + "' was '" + oldValue + '\'');
			}
		}

		return result;
	}

	/**
	 * Returns an unmodifiable list with 0-1 values for a specific QMake variable.
	 *
	 * @param map the map
	 * @param key the QMake variable
	 * @return the unmodifiable list of values
	 */
	public static List<String> singleValue(Map<String, String> map, String key) {
		String value = map.get(key);
		return value == null ? Collections.<String>emptyList() : Collections.singletonList(value);
	}

	/**
	 * Returns an unmodifiable list of values for a specific QMake variable that is decoded as a list of values.
	 *
	 * @param map the map
	 * @param key the QMake variable
	 * @return the unmodifiable list of values
	 */
	public static List<String> qmake3DecodeValueList(Map<String, String> map, String key) {
		String value = map.get(key);
		if (value == null) {
			return Collections.emptyList();
		}

		List<String> result = new ArrayList<>();
		for (String item : qmake3SplitValueList(value)) {
			result.add(qmake3DecodeValue(item));
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Decodes a specified QMake variable value.
	 *
	 * @param value the value
	 * @return the decoded value
	 */
	public static String qmake3DecodeValue(String value) {
		int length = value.length();
		if (length >= 2 && value.charAt(0) == '"' && value.charAt(length - 1) == '"') {
			value = value.substring(1, length - 1);
			length = value.length();
		}
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			char c = value.charAt(i);
			if (c == '\\') {
				++i;
				if (i < length) {
					char next = value.charAt(i);
					switch (next) {
					case 'r':
						sb.append('\r');
						break;
					case 'n':
						sb.append('\n');
						break;
					case 't':
						sb.append('\t');
						break;
					case '\\':
					case '\'':
					case '"':
						sb.append(next);
						break;
					case 'x':
						i += 2;
						if (i < length) {
							char first = value.charAt(i - 1);
							char second = value.charAt(i);
							if (first >= '0' && first <= '9' && second >= '0' && second <= '9') {
								sb.append((char) ((first - '0') * 16 + (second - '0')));
							}
						}
					}
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Splits a specified QMake variable value into a list of values.
	 *
	 * @param value the value
	 * @return the modifiable list of values
	 */
	private static List<String> qmake3SplitValueList(String value) {
		List<String> result = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		char quote = 0;
		boolean hadWord = false;
		final int length = value.length();
		for (int i = 0; i < length; i++) {
			char c = value.charAt(i);
			if (quote == c) {
				quote = 0;
				hadWord = true;
				sb.append(c);
				continue;
			}

			switch (c) {
			case '"':
			case '\'':
				quote = c;
				hadWord = true;
				break;
			case ' ':
			case '\t':
				if (quote == 0) {
					if (hadWord) {
						result.add(sb.toString());
						sb.delete(0, sb.length());
						hadWord = false;
					}
					continue;
				}
				break;
			case '\\':
				if (i + 1 < length) {
					char nextChar = value.charAt(i + 1);
					if (nextChar == '\'' || nextChar == '"' || nextChar == '\\') {
						sb.append(c);
						c = nextChar;
						++i;
					}
				}
				//$FALL-THROUGH$
			default:
				hadWord = true;
				break;
			}
			sb.append(c);
		}

		if (hadWord) {
			result.add(sb.toString());
		}

		return Collections.unmodifiableList(result);
	}
}
