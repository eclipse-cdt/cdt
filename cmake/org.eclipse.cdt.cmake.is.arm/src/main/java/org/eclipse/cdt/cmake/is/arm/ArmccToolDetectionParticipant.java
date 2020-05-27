/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.arm;

import org.eclipse.cdt.cmake.is.core.participant.Arglets;
import org.eclipse.cdt.cmake.is.core.participant.DefaultToolCommandlineParser;
import org.eclipse.cdt.cmake.is.core.participant.DefaultToolDetectionParticipant;
import org.eclipse.cdt.cmake.is.core.participant.IArglet;

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
				new Arglets.MacroUndefine_C_POSIX(), new Arglets.SystemIncludePath_armcc() };

		private ToolCommandlineParser() {
			super(null, new ArmccBuiltinDetectionBehavior(), arglets);
		}
	}
}
