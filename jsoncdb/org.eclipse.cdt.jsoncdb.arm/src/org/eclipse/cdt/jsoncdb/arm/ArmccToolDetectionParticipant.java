/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.jsoncdb.arm;

import org.eclipse.cdt.jsoncdb.core.participant.Arglets;
import org.eclipse.cdt.jsoncdb.core.participant.Arglets.IncludePathGeneric;
import org.eclipse.cdt.jsoncdb.core.participant.Arglets.NameOptionMatcher;
import org.eclipse.cdt.jsoncdb.core.participant.DefaultToolCommandlineParser;
import org.eclipse.cdt.jsoncdb.core.participant.DefaultToolDetectionParticipant;
import org.eclipse.cdt.jsoncdb.core.participant.IArglet;
import org.eclipse.core.runtime.IPath;

/**
 * armcc C & C++.
 *
 * @author Martin Weber
 */
public class ArmccToolDetectionParticipant extends DefaultToolDetectionParticipant {

	@SuppressWarnings("nls")
	public ArmccToolDetectionParticipant() {
		super("armcc", true, "exe", new ToolCommandlineParser());
	}

	private static class ToolCommandlineParser extends DefaultToolCommandlineParser {

		private static final IArglet[] arglets = { new Arglets.IncludePath_C_POSIX(), new Arglets.MacroDefine_C_POSIX(),
				new Arglets.MacroUndefine_C_POSIX(), new SystemIncludePath_armcc() };

		private ToolCommandlineParser() {
			super(null, new ArmccBuiltinDetectionBehavior(), arglets);
		}
	}

	/**
	 * A tool argument parser capable to parse a armcc-compiler system include path
	 * argument: {@code -Jdir}.
	 */
	/* package */ static class SystemIncludePath_armcc extends IncludePathGeneric implements IArglet {
		@SuppressWarnings("nls")
		static final NameOptionMatcher[] optionMatchers = {
				/* quoted directory */
				new NameOptionMatcher("-J" + "([\"'])(.+?)\\1", 2),
				/* unquoted directory */
				new NameOptionMatcher("-J" + "([^\\s]+)", 1), };

		/*-
		 * @see org.eclipse.cdt.jsoncdb.IArglet#processArgs(java.lang.String)
		 */
		@Override
		public int processArgument(IArgumentCollector resultCollector, IPath cwd, String argsLine) {
			return processArgument(true, resultCollector, cwd, argsLine, optionMatchers);
		}
	}
}
