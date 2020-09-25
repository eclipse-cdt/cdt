/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.nvidia;

import org.eclipse.cdt.cmake.is.core.participant.Arglets.IncludePathGeneric;
import org.eclipse.cdt.cmake.is.core.participant.Arglets.NameOptionMatcher;
import org.eclipse.cdt.cmake.is.core.participant.IArglet;
import org.eclipse.core.runtime.IPath;

/**
 * A tool argument parser capable to parse a nvcc-compiler system include path
 * argument: {@code -system=dir}.<br>
 * Note that nvcc seems to treat {@code -system=dir} differently from GCC
 * which`s manpage says:
 * <q>If dir begins with "=", then the "=" will be replaced by the sysroot
 * prefix; see --sysroot and -isysroot.</q>
 */
class NvccSystemIncludePathArglet extends IncludePathGeneric implements IArglet {
	static final NameOptionMatcher[] optionMatchers = {
			/* quoted directory */
			new NameOptionMatcher("-isystem=" + "([\"'])(.+?)\\1", 2), //$NON-NLS-1$ //$NON-NLS-2$
			/* unquoted directory */
			new NameOptionMatcher("-isystem=" + "([^\\s]+)", 1), }; //$NON-NLS-1$ //$NON-NLS-2$

	/*-
	 * @see org.eclipse.cdt.cmake.is.IArglet#processArgs(java.lang.String)
	 */
	@Override
	public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
		return processArgument(true, resultCollector, cwd, argsLine, optionMatchers);
	}
}