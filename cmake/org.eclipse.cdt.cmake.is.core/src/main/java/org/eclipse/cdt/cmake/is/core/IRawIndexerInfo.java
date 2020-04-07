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

import java.util.List;
import java.util.Map;

/**
 * Information about C preprocessor symbols and include paths collected from a
 * compiler command-line or by performing compiler built-ins detection.
 *
 * @author weber
 */
public interface IRawIndexerInfo {
	/**
	 * Gets the preprocessor symbols (macro definition)s.
	 */
	Map<String, String> getDefines();

	/**
	 * Gets the preprocessor symbol cancellations (macro undefine) collected from
	 * the command-line.
	 */
	List<String> getUndefines();

	/**
	 * Gets the preprocessor include paths collected from the command-line.
	 */
	List<String> getIncludePaths();

	/**
	 * Gets the preprocessor system include paths collected from the command-line.
	 */
	List<String> getSystemIncludePaths();
}
