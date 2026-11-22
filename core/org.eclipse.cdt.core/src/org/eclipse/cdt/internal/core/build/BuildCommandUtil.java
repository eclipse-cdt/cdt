/*******************************************************************************
 * Copyright (c) 2026 Harsh Bhanushali and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.build;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Helpers for working with the build commands configured in a Core Build
 * configuration.
 */
public class BuildCommandUtil {
	/**
	 * Matches the executable names of make implementations: make, gmake, nmake
	 * and prefixed variants such as mingw32-make (with an optional Windows
	 * extension). Deliberately does not match cmake, ninja and friends.
	 */
	private static final Pattern MAKE_NAME = Pattern.compile("(?:[a-z0-9_]+-)?[gn]?make(?:\\.exe|\\.cmd|\\.bat)?"); //$NON-NLS-1$

	private BuildCommandUtil() {
	}

	/**
	 * Whether the given build command executable understands make's
	 * {@code -f <makefile>} option. Other build tools, such as ninja, reject it.
	 *
	 * @param executable the command or path of the executable, e.g. {@code make}
	 *                   or {@code /usr/bin/gmake}
	 * @return true if {@code -f <makefile>} can be passed to the command
	 */
	public static boolean acceptsMakefileOption(String executable) {
		if (executable == null || executable.isBlank()) {
			return false;
		}
		// split by hand so that Windows paths are handled on any platform
		String name = executable.substring(Math.max(executable.lastIndexOf('/'), executable.lastIndexOf('\\')) + 1);
		return MAKE_NAME.matcher(name.toLowerCase(Locale.ROOT)).matches();
	}
}
