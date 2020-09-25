/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.microsoft;

import org.eclipse.cdt.cmake.is.core.participant.Arglets.IncludePathGeneric;
import org.eclipse.cdt.cmake.is.core.participant.Arglets.MacroDefineGeneric;
import org.eclipse.cdt.cmake.is.core.participant.Arglets.MacroUndefineGeneric;
import org.eclipse.cdt.cmake.is.core.participant.Arglets.NameOptionMatcher;
import org.eclipse.cdt.cmake.is.core.participant.Arglets.NameValueOptionMatcher;
import org.eclipse.cdt.cmake.is.core.participant.DefaultToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.participant.IArglet;
import org.eclipse.cdt.cmake.is.core.participant.IToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.participant.ResponseFileArglets;
import org.eclipse.core.runtime.IPath;

/**
 * An {@link IToolCommandlineParser} for the microsoft C and C++ compiler (cl).
 *
 * @author Martin Weber
 */
class MsclToolCommandlineParser extends DefaultToolCommandlineParser {

	private static final IArglet[] arglets = { new IncludePath_C_CL(), new MacroDefine_C_CL(),
			new MacroUndefine_C_CL() };

	public MsclToolCommandlineParser() {
		super(new ResponseFileArglets.At(), null, arglets);
	}

	////////////////////////////////////////////////////////////////////
	/** matches a macro name, with optional macro parameter list */
	private static final String REGEX_MACRO_NAME = "([\\w$]+)(?:\\([\\w$, ]*?\\))?"; //$NON-NLS-1$
	/**
	 * matches a macro name, skipping leading whitespace. Name in matcher group 1
	 */
	private static final String REGEX_MACRO_NAME_SKIP_LEADING_WS = "\\s*" + REGEX_MACRO_NAME; //$NON-NLS-1$
	/** matches an include path with quoted directory. Name in matcher group 2 */
	private static final String REGEX_INCLUDEPATH_QUOTED_DIR = "\\s*([\"'])(.+?)\\1"; //$NON-NLS-1$
	/**
	 * matches an include path with unquoted directory. Name in matcher group 1
	 */
	private static final String REGEX_INCLUDEPATH_UNQUOTED_DIR = "\\s*([^\\s]+)"; //$NON-NLS-1$

	/**
	 * A tool argument parser capable to parse a cl (Microsoft c compiler)
	 * compatible C-compiler include path argument: {@code /Ipath}.
	 */
	public static class IncludePath_C_CL extends IncludePathGeneric implements IArglet {
		private static final NameOptionMatcher[] optionMatchers = {
				/* quoted directory */
				new NameOptionMatcher("[-/]I" + REGEX_INCLUDEPATH_QUOTED_DIR, 2), //$NON-NLS-1$
				/* unquoted directory */
				new NameOptionMatcher("[-/]I" + REGEX_INCLUDEPATH_UNQUOTED_DIR, 1), }; //$NON-NLS-1$

		/*-
		 * @see org.eclipse.cdt.cmake.is.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(false, resultCollector, cwd, argsLine, optionMatchers);
		}
	}

	/**
	 * A tool argument parser capable to parse a cl (Microsoft c compiler)
	 * compatible C-compiler macro definition argument: {@code /DNAME=value}.
	 */
	public static class MacroDefine_C_CL extends MacroDefineGeneric implements IArglet {

		private static final NameValueOptionMatcher[] optionMatchers = {
				/* quoted value, whitespace in value, w/ macro arglist */
				new NameValueOptionMatcher("[-/]D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "((?:=)([\"'])(.+?)\\4)", 1, 5), //$NON-NLS-1$ //$NON-NLS-2$
				/* w/ macro arglist */
				new NameValueOptionMatcher("[-/]D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "((?:=)(\\S+))?", 1, 3), //$NON-NLS-1$ //$NON-NLS-2$
				/* quoted name, whitespace in value, w/ macro arglist */
				new NameValueOptionMatcher("[-/]D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "((?:=)(.+?))?\\1", 2, 5), //$NON-NLS-1$ //$NON-NLS-2$
				/* w/ macro arglist, shell escapes \' and \" in value */
				new NameValueOptionMatcher("[-/]D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "(?:=)((\\\\([\"']))(.*?)\\2)", //$NON-NLS-1$ //$NON-NLS-2$
						1, 2), };

		/*-
		 * @see org.eclipse.cdt.cmake.is.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, argsLine, optionMatchers);
		}
	}

	/**
	 * A tool argument parser capable to parse a cl (Microsoft c compiler)
	 * compatible C-compiler macro cancel argument: {@code /UNAME}.
	 */
	public static class MacroUndefine_C_CL extends MacroUndefineGeneric implements IArglet {

		private static final NameOptionMatcher optionMatcher = new NameOptionMatcher(
				"[-/]U" + REGEX_MACRO_NAME_SKIP_LEADING_WS, 1); //$NON-NLS-1$

		/*-
		 * @see org.eclipse.cdt.cmake.is.IArglet#processArgument(java.util.List, java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, argsLine, optionMatcher);
		}
	}
}
