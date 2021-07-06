/*******************************************************************************
 * Copyright (c) 2019-2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.jsoncdb.hpenonstop;

import org.eclipse.cdt.jsoncdb.core.participant.Arglets;
import org.eclipse.cdt.jsoncdb.core.participant.DefaultToolCommandlineParser;
import org.eclipse.cdt.jsoncdb.core.participant.DefaultToolDetectionParticipant;
import org.eclipse.cdt.jsoncdb.core.participant.IArglet;
import org.eclipse.cdt.jsoncdb.core.participant.ResponseFileArglets;

/**
 * HPE NonStop c11 C & C++.
 *
 * @author Martin Weber
 */
public class HpeC11ToolDetectionParticipant extends DefaultToolDetectionParticipant {

	public HpeC11ToolDetectionParticipant() {
		super("c11", true, "exe", new ToolCommandlineParser()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static class ToolCommandlineParser extends DefaultToolCommandlineParser {

		private static final IArglet[] arglets = { new Arglets.IncludePath_C_POSIX(), new Arglets.MacroDefine_C_POSIX(),
				new Arglets.MacroUndefine_C_POSIX() };

		private ToolCommandlineParser() {
			super(new ResponseFileArglets.At(), null, arglets);
		}
	}
}
