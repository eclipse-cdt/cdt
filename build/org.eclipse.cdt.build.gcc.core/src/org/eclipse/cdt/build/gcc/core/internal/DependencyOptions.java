/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core.internal;

import java.util.List;
import java.util.Set;

/**
 * Removes the GCC options that make dependency files (-M, -MD, -MMD, -MF and
 * so on) from a compiler command line.
 *
 * The scanner info discovery runs the compiler again with the options of the
 * build and with the output set to "-". If the options make dependency files,
 * the compiler writes a "-.d" file in the build directory.
 *
 * @see <a href="https://github.com/eclipse-cdt/cdt/issues/70">Issue 70</a>
 */
public final class DependencyOptions {

	/** Dependency options that take the next argument as their value. */
	private static final Set<String> OPTIONS_WITH_VALUE = Set.of("-MF", "-MT", "-MQ", "-MJ"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private DependencyOptions() {
	}

	/**
	 * Removes the dependency options and their values from the command line.
	 * All GCC and Clang options that start with "-M" are dependency options.
	 *
	 * @param commandLine the arguments of the compiler, without the compiler
	 *            itself; the list is changed in place
	 */
	public static void remove(List<String> commandLine) {
		for (int i = 0; i < commandLine.size();) {
			String arg = commandLine.get(i);
			if (arg.startsWith("-M")) { //$NON-NLS-1$
				commandLine.remove(i);
				if (OPTIONS_WITH_VALUE.contains(arg) && i < commandLine.size()) {
					commandLine.remove(i);
				}
			} else {
				i++;
			}
		}
	}
}
