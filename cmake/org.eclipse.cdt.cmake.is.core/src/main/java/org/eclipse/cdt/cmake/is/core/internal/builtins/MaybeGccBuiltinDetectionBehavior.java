/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal.builtins;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.cmake.is.core.participant.builtins.GccOutputProcessor;
import org.eclipse.cdt.cmake.is.core.participant.builtins.IBuiltinsDetectionBehavior;
import org.eclipse.cdt.cmake.is.core.participant.builtins.IBuiltinsOutputProcessor;

/**
 * The {link IBuiltinsDetectionBehavior} for the GNU C and GNU C++ compiler
 * (includes clang). This implementation assumes that the 'cc' command (which is
 * the same as any POSIX compliant compiler) actually is a GNU compiler.
 *
 * @author Martin Weber
 */
public class MaybeGccBuiltinDetectionBehavior implements IBuiltinsDetectionBehavior {
	// -E -P -dM for macros, -Wp,-v for include paths
	@SuppressWarnings("nls")
	private final List<String> enablingArgs = Arrays.asList("-E", "-P", "-dM", "-Wp,-v");

	@Override
	public List<String> getBuiltinsOutputEnablingArgs() {
		return enablingArgs;
	}

	@Override
	public IBuiltinsOutputProcessor createCompilerOutputProcessor() {
		return new GccOutputProcessor();
	}

	@Override
	public boolean suppressErrormessage() {
		// Assume 'cc' is a GNU compiler: do not report an error, if the compiler
		// actually is
		// a POSIX compiler that does not understand the arguments that enable built-in
		// detection
		return true;
	}
}
