/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.nvidia;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.cmake.is.core.participant.Arglets.BuiltinDetctionArgsGeneric;
import org.eclipse.cdt.cmake.is.core.participant.IArglet;
import org.eclipse.core.runtime.IPath;

/**
 * A tool argument parser capable to parse a nvcc option to specify the language
 * standard {@code --std=xxx}.
 */
public class NvccLangStdArglet extends BuiltinDetctionArgsGeneric implements IArglet {
	private static final Matcher[] optionMatchers = { Pattern.compile("--std \\S+").matcher(""), //$NON-NLS-1$ //$NON-NLS-2$
			Pattern.compile("-std \\S+").matcher(""), }; //$NON-NLS-1$ //$NON-NLS-2$

	/*-
	 * @see org.eclipse.cdt.cmake.is.IArglet#processArgs(java.lang.String)
	 */
	@Override
	public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
		return processArgument(resultCollector, argsLine, optionMatchers);
	}
}