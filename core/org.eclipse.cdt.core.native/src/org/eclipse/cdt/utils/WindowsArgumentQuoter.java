/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.util.regex.Pattern;

/**
 * Implementation of proper Windows quoting based on blog post:
 * https://docs.microsoft.com/en-ca/archive/blogs/twistylittlepassagesallalike/everyone-quotes-command-line-arguments-the-wrong-way
 *
 * @noreference This class is not intended to be referenced by clients.
 */
public class WindowsArgumentQuoter {

	public static String quoteArgv(String[] cmdarray, boolean force) {
		StringBuilder quoted = new StringBuilder();
		for (String arg : cmdarray) {
			quoteArg(arg, quoted, force);
			quoted.append(' ');
		}
		quoted.deleteCharAt(quoted.length() - 1);
		return quoted.toString();
	}

	private static Pattern spaces = Pattern.compile(".*\\s.*"); //$NON-NLS-1$

	private static void quoteArg(String arg, StringBuilder quoted, boolean force) {

		// Unless we're told otherwise, don't quote unless we actually
		// need to do so --- hopefully avoid problems if programs won't
		// parse quotes properly

		if (!force && !arg.isEmpty() && !spaces.matcher(arg).matches()) {
			quoted.append(arg);
		} else {
			quoted.append('"');
			for (int i = 0; i < arg.length(); i++) {
				int numberBackslashes = 0;

				while (i < arg.length() && arg.charAt(i) == '\\') {
					i++;
					numberBackslashes++;
				}

				if (i == arg.length()) {
					// Escape all backslashes, but let the terminating
					// double quotation mark we add below be interpreted
					// as a metacharacter.
					quoted.append("\\".repeat(numberBackslashes * 2)); //$NON-NLS-1$
					break;
				} else if (arg.charAt(i) == '"') {

					// Escape all backslashes and the following
					// double quotation mark.
					quoted.append("\\".repeat(numberBackslashes)); //$NON-NLS-1$
					quoted.append('"');
				} else {
					// Backslashes aren't special here.
					quoted.append("\\".repeat(numberBackslashes)); //$NON-NLS-1$
					quoted.append(arg.charAt(i));
				}
			}
			quoted.append('"');
		}
	}

}
