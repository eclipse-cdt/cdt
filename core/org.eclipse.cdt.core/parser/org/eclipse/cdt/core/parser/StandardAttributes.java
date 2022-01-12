/*******************************************************************************
 * Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 6.3
 */
@SuppressWarnings("nls")
public class StandardAttributes {
	public static final String CARRIES_DEPENDENCY = "carries_dependency";
	public static final String DEPRECATED = "deprecated";
	public static final String FALLTHROUGH = "fallthrough";
	public static final String MAYBE_UNUSED = "maybe_unused";
	public static final String NODISCARD = "nodiscard";
	public static final String NORETURN = "noreturn";

	public static final char[] cCARRIES_DEPENDENCY = "carries_dependency".toCharArray();
	public static final char[] cDEPRECATED = "deprecated".toCharArray();
	public static final char[] cFALLTHROUGH = "fallthrough".toCharArray();
	public static final char[] cMAYBE_UNUSED = "maybe_unused".toCharArray();
	public static final char[] cNODISCARD = "nodiscard".toCharArray();
	public static final char[] cNORETURN = "noreturn".toCharArray();
}
