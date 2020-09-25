/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.microsoft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import org.eclipse.cdt.cmake.is.core.participant.IToolDetectionParticipant;
import org.eclipse.cdt.cmake.is.core.participant.ParticipantTestUtil;
import org.junit.Test;

/**
 * @author Martin Weber
 */
public class ToolDetectionParticipantTest {
	@Test
	public void testDetermineToolDetectionParticipant() {
		IToolDetectionParticipant result = ParticipantTestUtil
				.determineToolDetectionParticipant("/usr/bin/cl -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(MsclToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("cl -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(MsclToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("/usr/bin/cl.exe -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(MsclToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("cl.exe -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(MsclToolDetectionParticipant.class, result.getClass());
	}

	@Test
	public void testDetermineToolDetectionParticipant_quote() {
		String[] quotes = { "\"", "'" };
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s/usr/bin/cl%1$s -I /foo/cl -C blah.c", quote);
			IToolDetectionParticipant result = ParticipantTestUtil.determineToolDetectionParticipant(args, null, true);
			assertNotNull("Command in quotes=" + quote, result);
			assertEquals(MsclToolDetectionParticipant.class, result.getClass());
		}
	}

}
