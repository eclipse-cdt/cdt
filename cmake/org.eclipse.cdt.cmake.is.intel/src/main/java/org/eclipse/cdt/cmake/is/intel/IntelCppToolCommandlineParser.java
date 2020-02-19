/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.intel;

import org.eclipse.cdt.cmake.is.core.Arglets;
import org.eclipse.cdt.cmake.is.core.DefaultToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.IToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.ResponseFileArglets;

/**
 * An {@link IToolCommandlineParser} for the Intel C compilers.
 *
 * @author Martin Weber
 */
class IntelCppToolCommandlineParser extends DefaultToolCommandlineParser {

	static final IntelCppToolCommandlineParser INSTANCE = new IntelCppToolCommandlineParser();

	private IntelCppToolCommandlineParser() {
		super("org.eclipse.cdt.core.g++", new ResponseFileArglets.At(), null, new Arglets.IncludePath_C_POSIX(),
				new Arglets.MacroDefine_C_POSIX(), new Arglets.MacroUndefine_C_POSIX());
	}
}
