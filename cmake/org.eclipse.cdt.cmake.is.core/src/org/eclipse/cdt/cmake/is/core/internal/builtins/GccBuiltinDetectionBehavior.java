/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal.builtins;

/**
 * The {link IBuiltinsDetectionBehavior} for the GNU C and GNU C++ compiler
 * (includes clang). This implementation is for the 'gcc' and 'g++' command.
 *
 * @author Martin Weber
 */
public class GccBuiltinDetectionBehavior extends MaybeGccBuiltinDetectionBehavior {
	@Override
	public boolean suppressErrormessage() {
		// report an error, if the compiler does not understand the arguments that
		// enable built-in detection
		return false;
	}
}
