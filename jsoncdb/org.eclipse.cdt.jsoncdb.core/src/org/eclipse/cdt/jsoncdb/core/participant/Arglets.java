/*******************************************************************************
 * Copyright (c) 2015-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.jsoncdb.core.participant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.jsoncdb.core.participant.IArglet.IArgumentCollector;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Various Arglet implementation for parsing tool arguments.
 *
 * @author Martin Weber
 */
public final class Arglets {
	private static final String EMPTY_STR = ""; //$NON-NLS-1$

	/** matches a macro name, with optional macro parameter list */
	@SuppressWarnings("nls")
	private static final String REGEX_MACRO_NAME = "([\\w$]+)(?:\\([\\w$, ]*?\\))?";
	/**
	 * matches a macro name, skipping leading whitespace. Name in matcher group 1
	 */
	@SuppressWarnings("nls")
	private static final String REGEX_MACRO_NAME_SKIP_LEADING_WS = "\\s*" + REGEX_MACRO_NAME;
	/**
	 * matches a macro argument in quotes, skipping leading whitespace. Quote
	 * character in matcher group 1, Name in matcher group 2
	 */
	@SuppressWarnings("nls")
	private static final String REGEX_MACRO_ARG_QUOTED__SKIP_LEADING_WS = "\\s*([\"'])" + REGEX_MACRO_NAME;
	/** matches an include path with quoted directory. Name in matcher group 2 */
	@SuppressWarnings("nls")
	private static final String REGEX_INCLUDEPATH_QUOTED_DIR = "\\s*([\"'])(.+?)\\1";
	/**
	 * matches an include path with unquoted directory. Name in matcher group 1
	 */
	@SuppressWarnings("nls")
	private static final String REGEX_INCLUDEPATH_UNQUOTED_DIR = "\\s*([^\\s]+)";

	/**
	 * nothing to instantiate
	 */
	private Arglets() {
	}

	////////////////////////////////////////////////////////////////////
	// Matchers for options
	////////////////////////////////////////////////////////////////////
	/**
	 * A matcher for option names. Includes information of the matcher groups that
	 * hold the option name.
	 *
	 * @author Martin Weber
	 */
	public static class NameOptionMatcher {
		final Matcher matcher;
		final int nameGroup;

		/**
		 * Constructor.
		 *
		 * @param pattern   - regular expression pattern being parsed by the parser.
		 * @param nameGroup - capturing group number defining name of an entry.
		 */
		public NameOptionMatcher(String pattern, int nameGroup) {
			this.matcher = Pattern.compile(pattern).matcher(EMPTY_STR);
			this.nameGroup = nameGroup;
		}

		@SuppressWarnings("nls")
		@Override
		public String toString() {
			return "NameOptionMatcher [matcher=" + this.matcher + ", nameGroup=" + this.nameGroup + "]";
		}
	}

	/**
	 * A matcher for preprocessor define options. Includes information of the
	 * matcher groups that hold the macro name and value.
	 *
	 * @author Martin Weber
	 */
	public static class NameValueOptionMatcher extends NameOptionMatcher {
		/**
		 * the number of the value group, or {@code -1} for a pattern that does not
		 * recognize a macro value
		 */
		private final int valueGroup;

		/**
		 * Constructor.
		 *
		 * @param pattern    - regular expression pattern being parsed by the parser.
		 * @param nameGroup  - capturing group number defining name of an entry.
		 * @param valueGroup - capturing group number defining value of an entry.
		 */
		/**
		 * @param pattern
		 * @param nameGroup
		 * @param valueGroup the number of the value group, or {@code -1} for a pattern
		 *                   that does not recognize a macro value
		 */
		public NameValueOptionMatcher(String pattern, int nameGroup, int valueGroup) {
			super(pattern, nameGroup);
			this.valueGroup = valueGroup;
		}

		@SuppressWarnings("nls")
		@Override
		public String toString() {
			return "NameValueOptionMatcher [matcher=" + this.matcher + ", nameGroup=" + this.nameGroup + ", valueGroup="
					+ this.valueGroup + "]";
		}
	}

	////////////////////////////////////////////////////////////////////
	// generic option parsers
	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a C-compiler macro definition
	 * argument.
	 */
	public static abstract class MacroDefineGeneric {

		protected final int processArgument(IArgumentCollector resultCollector, String args,
				NameValueOptionMatcher[] optionMatchers) {
			for (NameValueOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.matcher;

				matcher.reset(args);
				if (matcher.lookingAt()) {
					final String name = matcher.group(oMatcher.nameGroup);
					final String value = oMatcher.valueGroup == -1 ? null : matcher.group(oMatcher.valueGroup);
					resultCollector.addDefine(name, value);
					final int end = matcher.end();
					return end;
				}
			}
			return 0;// no input consumed
		}
	}

	/**
	 * A tool argument parser capable to parse a C-compiler macro cancel argument.
	 */
	public static class MacroUndefineGeneric {

		/*-
		 * @see org.eclipse.cdt.jsoncdb.IArglet#processArgument(java.util.List, java.lang.String)
		 */
		protected final int processArgument(IArgumentCollector resultCollector, String argsLine,
				NameOptionMatcher optionMatcher) {
			final Matcher oMatcher = optionMatcher.matcher;

			oMatcher.reset(argsLine);
			if (oMatcher.lookingAt()) {
				final String name = oMatcher.group(1);
				resultCollector.addUndefine(name);
				final int end = oMatcher.end();
				return end;
			}
			return 0;// no input consumed
		}
	}

	/**
	 * A tool argument parser capable to parse a C-compiler include path argument.
	 */
	public static abstract class IncludePathGeneric {
		/**
		 * @param isSystemIncludePath <code>true</code> if the include path is a system include path otherwise <code>false</code>
		 * @param cwd the current working directory of the compiler at its invocation
		 * @see org.eclipse.cdt.jsoncdb.core.participant.IArglet#processArgument(IArgumentCollector,
		 *      IPath, String)
		 */
		protected final int processArgument(boolean isSystemIncludePath, IArgumentCollector resultCollector, IPath cwd,
				String argsLine, NameOptionMatcher[] optionMatchers) {
			for (NameOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.matcher;

				matcher.reset(argsLine);
				if (matcher.lookingAt()) {
					String name = matcher.group(oMatcher.nameGroup);
					// workaround for relative path by cmake bug
					// https://gitlab.kitware.com/cmake/cmake/issues/13894 : prepend cwd
					IPath path = Path.fromOSString(name);
					if (!path.isAbsolute()) {
						// prepend CWD
						name = cwd.append(path).toOSString();
					}
					if (isSystemIncludePath) {
						resultCollector.addSystemIncludePath(name);
					} else {
						resultCollector.addIncludePath(name);
					}
					final int end = matcher.end();
					return end;
				}
			}
			return 0;// no input consumed
		}
	}

	/**
	 * A tool argument parser capable to parse a C-compiler include file argument.
	 */
	public static abstract class IncludeFileGeneric {
		/**
		 * @param cwd
		 *          the current working directory of the compiler at its invocation
		 */
		protected final int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine,
				NameOptionMatcher[] optionMatchers) {
			for (NameOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.matcher;

				matcher.reset(argsLine);
				if (matcher.lookingAt()) {
					String name = matcher.group(oMatcher.nameGroup);
					resultCollector.addIncludeFile(name);
					final int end = matcher.end();
					return end;
				}
			}
			return 0;// no input consumed
		}
	}

	/**
	 * A tool argument parser capable to parse a C-compiler macros file argument.
	 */
	public static abstract class MacrosFileGeneric {
		/**
		 * @param cwd
		 *          the current working directory of the compiler at its invocation
		 */
		protected final int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine,
				NameOptionMatcher[] optionMatchers) {
			for (NameOptionMatcher oMatcher : optionMatchers) {
				final Matcher matcher = oMatcher.matcher;

				matcher.reset(argsLine);
				if (matcher.lookingAt()) {
					String name = matcher.group(oMatcher.nameGroup);
					resultCollector.addMacroFile(name);
					final int end = matcher.end();
					return end;
				}
			}
			return 0;// no input consumed
		}
	}

	////////////////////////////////////////////////////////////////////
	// POSIX compatible option parsers
	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a POSIX compatible C-compiler macro
	 * definition argument: {@code -DNAME=value}.
	 */
	public static class MacroDefine_C_POSIX extends MacroDefineGeneric implements IArglet {

		@SuppressWarnings("nls")
		private static final NameValueOptionMatcher[] optionMatchers = {
				/* string or char literal value, with whitespace in value and escaped quotes */
				new NameValueOptionMatcher("-D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "=(" + "([\"'])" // the quote char
																										// in group 3
						+ "(?:" // non-capturing
						+ "\\\\\\\\" // the escaped escape char
						+ "|" // OR
						+ "\\\\\\3" // the escaped quote char
						+ "|" // OR
						+ "(?!\\3)." // any character except the quote char
						+ ")*" // zero or more times
						+ "\\3" // the quote char
						+ ")", 1, 2),
				/* macro name only, w/ optional macro arglist */
				new NameValueOptionMatcher("-D" + REGEX_MACRO_NAME_SKIP_LEADING_WS + "=((\\S+))", 1, 3),
				/* separated, quoted name-value arg, whitespace in value */
				new NameValueOptionMatcher("-D" + REGEX_MACRO_ARG_QUOTED__SKIP_LEADING_WS + "=((.+?))\\1", 2, 4),
				/* macro name only */
				new NameValueOptionMatcher("-D" + REGEX_MACRO_NAME_SKIP_LEADING_WS, 1, -1), };

		/*-
		 * @see org.eclipse.cdt.jsoncdb.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, argsLine, optionMatchers);
		}

	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a POSIX compatible C-compiler macro
	 * cancel argument: {@code -UNAME}.
	 */
	public static class MacroUndefine_C_POSIX extends MacroUndefineGeneric implements IArglet {

		@SuppressWarnings("nls")
		private static final NameOptionMatcher optionMatcher = new NameOptionMatcher(
				"-U" + REGEX_MACRO_NAME_SKIP_LEADING_WS, 1);

		/*-
		 * @see org.eclipse.cdt.jsoncdb.IArglet#processArgument(java.util.List, java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, argsLine, optionMatcher);
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a POSIX compatible C-compiler include
	 * path argument: {@code -Ipath}.
	 */
	public static class IncludePath_C_POSIX extends IncludePathGeneric implements IArglet {
		@SuppressWarnings("nls")
		private static final NameOptionMatcher[] optionMatchers = {
				/* quoted directory */
				new NameOptionMatcher("-I" + REGEX_INCLUDEPATH_QUOTED_DIR, 2),
				/* unquoted directory */
				new NameOptionMatcher("-I" + REGEX_INCLUDEPATH_UNQUOTED_DIR, 1) };

		/*-
		 * @see org.eclipse.cdt.jsoncdb.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(true, resultCollector, cwd, argsLine, optionMatchers);
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a C-compiler system include path
	 * argument: {@code -system path}.
	 */
	public static class SystemIncludePath_C extends IncludePathGeneric implements IArglet {
		@SuppressWarnings("nls")
		static final NameOptionMatcher[] optionMatchers = {
				/* quoted directory */
				new NameOptionMatcher("-isystem" + REGEX_INCLUDEPATH_QUOTED_DIR, 2),
				/* unquoted directory */
				new NameOptionMatcher("-isystem" + REGEX_INCLUDEPATH_UNQUOTED_DIR, 1), };

		/*-
		 * @see org.eclipse.cdt.jsoncdb.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(true, resultCollector, cwd, argsLine, optionMatchers);
		}
	}

	////////////////////////////////////////////////////////////////////
	// POSIX compatible option parsers
	////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	// compiler built-ins detection
	////////////////////////////////////////////////////////////////////

	/**
	 * @deprecated use <code>BuiltinDetectionArgsGeneric</code> instead
	 */
	@Deprecated
	public static abstract class BuiltinDetctionArgsGeneric {
		protected int processArgument(IArgumentCollector resultCollector, String argsLine, Matcher[] optionMatchers) {
			throw new IllegalStateException(
					"This class is deprecated - extend class BuiltinDetectionArgsGeneric instead"); //$NON-NLS-1$
		}
	}

	/**
	 * A tool argument parser capable to parse arguments from the command-line that
	 * affect built-in detection.
	 * @since 1.2
	 */
	public static abstract class BuiltinDetectionArgsGeneric extends BuiltinDetctionArgsGeneric {
		/**
		 * @see org.eclipse.cdt.jsoncdb.core.participant.IArglet#processArgument(IArgumentCollector,
		 *      IPath, String)
		 */
		@Override
		protected final int processArgument(IArgumentCollector resultCollector, String argsLine,
				Matcher[] optionMatchers) {
			for (Matcher matcher : optionMatchers) {
				matcher.reset(argsLine);
				if (matcher.lookingAt()) {
					resultCollector.addBuiltinDetectionArgument(matcher.group());
					return matcher.end();
				}
			}
			return 0;// no input consumed
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a GCC include file argument {@code -include <file>}.
	 */
	public static class IncludeFile_GCC extends IncludeFileGeneric implements IArglet {
		@SuppressWarnings("nls")
		private static final NameOptionMatcher[] optionMatchers = {
				/* "-include=" quoted directory */
				new NameOptionMatcher("-include" + REGEX_INCLUDEPATH_QUOTED_DIR, 2),
				/* "-include=" unquoted directory */
				new NameOptionMatcher("-include" + REGEX_INCLUDEPATH_UNQUOTED_DIR, 1), };

		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, cwd, argsLine, optionMatchers);
		}
	}

	/**
	 * A tool argument parser capable to parse a GCC macros file argument {@code -imacros <file>}.
	 */
	public static class MacrosFile_GCC extends MacrosFileGeneric implements IArglet {
		@SuppressWarnings("nls")
		private static final NameOptionMatcher[] optionMatchers = {
				/* "-include=" quoted directory */
				new NameOptionMatcher("-imacros" + REGEX_INCLUDEPATH_QUOTED_DIR, 2),
				/* "-include=" unquoted directory */
				new NameOptionMatcher("-imacros" + REGEX_INCLUDEPATH_UNQUOTED_DIR, 1), };

		/*-
		 * @see de.marw.cmake.cdt.lsp.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, cwd, argsLine, optionMatchers);
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a GCC option to specify paths
	 * {@code --sysrooot}.
	 */
	public static class Sysroot_GCC extends BuiltinDetectionArgsGeneric implements IArglet {
		@SuppressWarnings("nls")
		private static final Matcher[] optionMatchers = {
				/* "--sysroot=" quoted directory */
				Pattern.compile("--sysroot=" + REGEX_INCLUDEPATH_QUOTED_DIR).matcher(EMPTY_STR),
				/* "--sysroot=" unquoted directory */
				Pattern.compile("--sysroot=" + REGEX_INCLUDEPATH_UNQUOTED_DIR).matcher(EMPTY_STR),
				/* "-isysroot=" quoted directory */
				Pattern.compile("-isysroot=" + REGEX_INCLUDEPATH_QUOTED_DIR).matcher(EMPTY_STR),
				/* "-isysroot=" unquoted directory */
				Pattern.compile("-isysroot=" + REGEX_INCLUDEPATH_UNQUOTED_DIR).matcher(EMPTY_STR),
				/* "--no-sysroot-prefix" */
				Pattern.compile("--no-sysroot-prefix").matcher(EMPTY_STR) };

		/*-
		 * @see org.eclipse.cdt.jsoncdb.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, argsLine, optionMatchers);
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a Clang option to specify the compilation target {@code --target}.
	 * @since 1.1
	 */
	public static class Target_Clang extends BuiltinDetectionArgsGeneric implements IArglet {
		private static final Matcher[] optionMatchers = {
				/* "--target=" triple */
				Pattern.compile("--target=\\w+(-\\w+)*").matcher(EMPTY_STR) }; //$NON-NLS-1$

		/*-
		* @see de.marw.cmake.cdt.lsp.IArglet#processArgs(java.lang.String)
		*/
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, argsLine, optionMatchers);
		}
	}

	////////////////////////////////////////////////////////////////////
	/**
	 * A tool argument parser capable to parse a GCC option to specify the language
	 * standard {@code -std=xxx}.
	 */
	public static class LangStd_GCC extends BuiltinDetectionArgsGeneric implements IArglet {
		@SuppressWarnings("nls")
		private static final Matcher[] optionMatchers = { Pattern.compile("-std=\\S+").matcher(EMPTY_STR),
				Pattern.compile("-ansi").matcher(EMPTY_STR),
				Pattern.compile("-fPIC", Pattern.CASE_INSENSITIVE).matcher(EMPTY_STR),
				Pattern.compile("-fPIE", Pattern.CASE_INSENSITIVE).matcher(EMPTY_STR),
				Pattern.compile("-fstack-protector\\S+").matcher(EMPTY_STR),
				Pattern.compile("-march=\\\\S+").matcher(EMPTY_STR), Pattern.compile("-mcpu=\\\\S+").matcher(EMPTY_STR),
				Pattern.compile("-mtune=\\\\S+").matcher(EMPTY_STR), Pattern.compile("-pthread").matcher(EMPTY_STR), };

		/*-
		 * @see org.eclipse.cdt.jsoncdb.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, argsLine, optionMatchers);
		}
	}

	////////////////////////////////////////////////////////////////////
}
