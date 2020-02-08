/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal;

/**
 * String manipulation functions.
 *
 * @author Martin Weber
 */
public class StringUtil {

	/**
	 * Just static methods.
	 *
	 */
	private StringUtil() {
	}

	/**
	 * Returns a copy of the string, with leading whitespace omitted.
	 *
	 * @param string the string to remove whitespace from
	 * @return A copy of the string with leading white space removed, or the string
	 *         if it has no leading white space.
	 */
	public static String trimLeadingWS(String string) {
		int len = string.length();
		int st = 0;

		while ((st < len) && (string.charAt(st) <= ' ')) {
			st++;
		}
		return st > 0 ? string.substring(st, len) : string;
	}

}
