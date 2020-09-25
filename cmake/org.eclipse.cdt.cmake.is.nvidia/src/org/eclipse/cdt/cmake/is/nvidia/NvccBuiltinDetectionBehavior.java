/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.nvidia;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.cmake.is.core.participant.builtins.GccOutputProcessor;
import org.eclipse.cdt.cmake.is.core.participant.builtins.IBuiltinsDetectionBehavior;
import org.eclipse.cdt.cmake.is.core.participant.builtins.IBuiltinsOutputProcessor;

/**
 * The {link IBuiltinsDetectionBehavior} for the NVidia CUDA compiler.
 *
 * @author Martin Weber
 */
class NvccBuiltinDetectionBehavior implements IBuiltinsDetectionBehavior {
	// -E -Xcompiler -P -Xcompiler -dM for macros, -Xcompiler -v for include paths
	@SuppressWarnings("nls")
	private final List<String> enablingArgs = Arrays.asList("-E", "-Xcompiler", "-P", "-Xcompiler", "-dM", "-Xcompiler",
			"-v");

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
		// report an error, if the compiler does not understand the arguments that
		// enable built-in detection
		return false;
	}
}
