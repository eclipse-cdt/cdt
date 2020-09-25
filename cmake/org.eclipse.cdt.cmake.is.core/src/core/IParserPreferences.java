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

package org.eclipse.cdt.cmake.is.core;

/**
 * Preferences to configure the {@code compile_commands.json} parser.
 *
 * @author weber
 */
public interface IParserPreferences {
	/** Gets whether to also try a version suffix to detect a compiler. */
	boolean getTryVersionSuffix();

	/** Sets whether to also try a version suffix to detect a compiler. */
	void setTryVersionSuffix(boolean tryVersionSuffix);

	/** Sets the version suffix to detect a compiler. This is interpreted as a regular expression pattern. */
	String getVersionSuffixPattern();

	/** Sets the version suffix to detect a compiler.
	 *
	 * @param versionSuffixPattern The version suffix as a regular expression pattern
	 */
	void setVersionSuffixPattern(String versionSuffixPattern);

	/** Gets whether to allocate a console showing the output of compiler built-ins detection. */
	boolean getAllocateConsole();

	/** Sets whether to allocate a console showing the output of compiler built-ins detection. */
	void setAllocateConsole(boolean allocateConsole);
}
