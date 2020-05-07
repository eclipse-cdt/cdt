/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.nvidia;

import java.util.Collections;
import java.util.Set;

import org.eclipse.cdt.cmake.is.core.Arglets;
import org.eclipse.cdt.cmake.is.core.DefaultToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.DefaultToolDetectionParticipant;
import org.eclipse.cdt.cmake.is.core.IArglet;
import org.eclipse.cdt.cmake.is.core.ResponseFileArglets;

/**
 * CUDA: nvcc compilers (POSIX compatible).
 *
 * @author Martin Weber
 */
public class NvccToolDetectionParticipant extends DefaultToolDetectionParticipant {
	static final String COM_NVIDIA_CUDA_LANGUAGE_ID = "com.nvidia.cuda.toolchain.language.cuda.cu";

	public NvccToolDetectionParticipant() {
		super("nvcc", true, "exe", new ToolCommandlineParser());
	}

	private static class ToolCommandlineParser extends DefaultToolCommandlineParser {

		private static final IArglet[] arglets = { new Arglets.IncludePath_C_POSIX(), new Arglets.MacroDefine_C_POSIX(),
				new Arglets.MacroUndefine_C_POSIX(), new NvccSystemIncludePathArglet(),
				new Arglets.SystemIncludePath_C(), new NvccLangStdArglet() };

		private ToolCommandlineParser() {
			super(new ResponseFileArglets.At(), new NvccBuiltinDetectionBehavior(), arglets);
		}
	}
}
