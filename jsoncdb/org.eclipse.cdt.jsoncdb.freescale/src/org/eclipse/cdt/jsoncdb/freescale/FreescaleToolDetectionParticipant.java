/*******************************************************************************
 * Copyright (c) 2023 Thomas Kucharczyk
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.jsoncdb.freescale;

import java.util.regex.Pattern;

import org.eclipse.cdt.jsoncdb.core.participant.Arglets;
import org.eclipse.cdt.jsoncdb.core.participant.Arglets.BuiltinDetectionArgsGeneric;
import org.eclipse.cdt.jsoncdb.core.participant.Arglets.IncludePathGeneric;
import org.eclipse.cdt.jsoncdb.core.participant.Arglets.NameOptionMatcher;
import org.eclipse.cdt.jsoncdb.core.participant.DefaultToolCommandlineParser;
import org.eclipse.cdt.jsoncdb.core.participant.DefaultToolDetectionParticipant;
import org.eclipse.cdt.jsoncdb.core.participant.IArglet;
import org.eclipse.core.runtime.IPath;

/**
 * Freescale compilers
 *
 * @author Thomas Kucharczyk
 */
public class FreescaleToolDetectionParticipant extends DefaultToolDetectionParticipant {
	public FreescaleToolDetectionParticipant() {
		super("(?:c99wrap_)*?chc\\d\\d", true, "exe", new ToolCommandlineParser()); //$NON-NLS-1$//$NON-NLS-2$
	}

	private static class ToolCommandlineParser extends DefaultToolCommandlineParser {

		private static final IArglet[] arglets = { new Arglets.IncludePath_C_POSIX(), new Arglets.MacroDefine_C_POSIX(),
				new Arglets.MacroUndefine_C_POSIX(), new SystemIncludePath_chc(), new LangStd_CHC() };

		private ToolCommandlineParser() {
			super(null, new FreescaleBuiltinDetectionBehavior(), arglets);
		}
	}

	/**
	 * A tool argument parser capable to parse a chc12-compiler system include path
	 * argument: {@code -EnvLIBPATH=dir}.
	 */
	/* package */ static class SystemIncludePath_chc extends IncludePathGeneric implements IArglet {
		@SuppressWarnings("nls")
		static final NameOptionMatcher[] optionMatchers = {
				/* quoted directory */
				new NameOptionMatcher("-EnvLIBPATH=" + "\\s*([\"'])(.+?)\\1", 2),
				/* unquoted directory */
				new NameOptionMatcher("-EnvLIBPATH=" + "\\s*([^\\s-]+)", 1), };

		/*-
		 * @see org.eclipse.cdt.jsoncdb.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(true, resultCollector, cwd, argsLine, optionMatchers);
		}
	}

	/**
	* A tool argument parser capable to parse a CHC option to specify the language
	* standard {@code -ansi}.
	*/
	/* package */ static class LangStd_CHC extends BuiltinDetectionArgsGeneric implements IArglet {
		@SuppressWarnings("nls")
		private static final Pattern[] optionPatterns = { Pattern.compile("-ansi"),
				Pattern.compile("-C\\+\\+[fec]?", Pattern.CASE_INSENSITIVE), Pattern.compile("-Cf"), };

		/*-
		* @see org.eclipse.cdt.jsoncdb.core.participant.IArglet#processArgs(java.lang.String)
		*/
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(resultCollector, argsLine, optionPatterns);
		}
	}

}
