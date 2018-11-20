/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
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
package org.eclipse.cdt.core.parser.util;

import java.util.Collection;

/**
 * Static methods for working with strings.
 *
 * @since 5.6
 */
public class StringUtil {

	private StringUtil() {
	}

	/**
	 * Joins strings using the given delimiter.
	 */
	public static String join(Iterable<String> strings, String delimiter) {
		if (strings instanceof Collection) {
			int size = ((Collection<String>) strings).size();
			if (size == 1)
				return strings.iterator().next();
			if (size == 0)
				return ""; //$NON-NLS-1$
		}

		StringBuilder buf = new StringBuilder();
		for (String str : strings) {
			if (buf.length() != 0)
				buf.append(delimiter);
			buf.append(str);
		}
		return buf.toString();
	}

	/**
	 * Joins strings using the given delimiter.
	 */
	public static String join(String[] strings, String delimiter) {
		if (strings.length == 1)
			return strings[0];
		if (strings.length == 0)
			return ""; //$NON-NLS-1$

		StringBuilder buf = new StringBuilder();
		for (String str : strings) {
			if (buf.length() != 0)
				buf.append(delimiter);
			buf.append(str);
		}
		return buf.toString();
	}
}
