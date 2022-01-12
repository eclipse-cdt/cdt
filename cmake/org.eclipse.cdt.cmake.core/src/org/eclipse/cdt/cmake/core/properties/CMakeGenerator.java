/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.properties;

/**
 * Represents a cmake build-script generator including information about the
 * makefile (build-script) processor that builds from the generated script.
 *
 * @author Martin Weber
 * @since 1.4
 */
public enum CMakeGenerator {
	/*
	 * Implementation Note: Please do not include generators for IDE project files,
	 * such as "Eclipse CDT4 - Unix Makefiles".
	 */

	// linux generators
	UnixMakefiles("Unix Makefiles"), //$NON-NLS-1$
	// Ninja
	Ninja("Ninja", "-k 0") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String getMakefileName() {
			return "build.ninja"; //$NON-NLS-1$
		}
	},
	// windows generators
	NMakeMakefilesJOM("NMake Makefiles JOM"), //$NON-NLS-1$
	MinGWMakefiles("MinGW Makefiles"), //$NON-NLS-1$
	MSYSMakefiles("MSYS Makefiles"), //$NON-NLS-1$
	NMakeMakefiles("NMake Makefiles"), //$NON-NLS-1$
	BorlandMakefiles("Borland Makefiles"), //$NON-NLS-1$
	WatcomWMake("Watcom WMake"); //$NON-NLS-1$

	private final String name;
	private String ignoreErrOption;

	private CMakeGenerator(String name, String ignoreErrOption) {
		this.name = name;
		this.ignoreErrOption = ignoreErrOption;
	}

	private CMakeGenerator(String name) {
		this(name, "-k"); //$NON-NLS-1$
	}

	/**
	 * Gets the cmake argument that specifies the build-script generator.
	 *
	 * @return a non-empty string, which must be a valid argument for cmake's -G
	 *         option.
	 */
	public String getCMakeName() {
		return name;
	}

	/**
	 * Gets the name of the top-level makefile (build-script) which is interpreted
	 * by the build-script processor.
	 *
	 * @return name of the makefile.
	 */
	public String getMakefileName() {
		return "Makefile"; //$NON-NLS-1$
	}

	/**
	 * Gets the build-script processor´s command argument(s) to ignore build errors.
	 *
	 * @return the command option string or {@code null} if no argument is needed.
	 */
	public String getIgnoreErrOption() {
		return ignoreErrOption;
	}
}
