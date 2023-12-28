/*******************************************************************************
 * Copyright (c) 2023 Thomas Kucharcyzk
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.jsoncdb.freescale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import org.eclipse.cdt.jsoncdb.core.participant.IToolDetectionParticipant;
import org.eclipse.cdt.jsoncdb.core.participant.ParticipantTestUtil;
import org.junit.Test;

/**
 * @author Thomas Kucharcyzk
 */
public class ToolDetectionParticipantTest {
	@Test
	public void testDetermineToolDetectionParticipant_chc12() {
		IToolDetectionParticipant result = ParticipantTestUtil.determineToolDetectionParticipant(
				"C:/C99toC89/c99wrap_chc12.exe -keep C:/HCS12_CW/Prog/chc12.exe -I /foo/cc -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil
				.determineToolDetectionParticipant("C:/HCS12_CW/Prog/chc12.exe -I /foo/cc -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("/usr/bin/chc12.exe  -I /foo/cc -C blah.c", null,
				true);
		assertNotNull(result);
		assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("chc12.exe  -I /foo/cc -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());
	}

	@Test
	public void testDetermineToolDetectionParticipant_chc12_quote() {
		String[] quotes = { "\"", "'" };
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s/usr/bin/chc12%1$s -I /foo/cc -C blah.c", quote);
			IToolDetectionParticipant result = ParticipantTestUtil.determineToolDetectionParticipant(args, null, true);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());
		}
	}

	@Test
	public void testDetermineToolDetectionParticipant_chc08() {
		IToolDetectionParticipant result = ParticipantTestUtil
				.determineToolDetectionParticipant("/usr/bin/chc08  -I /foo/clang -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("chc08  -I /foo/clang -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("/usr/bin/chc08.exe -I /foo/clang -C blah.c",
				null, true);
		assertNotNull(result);
		assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("chc08.exe -I /foo/clang -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());
	}

	@Test
	public void testDetermineToolDetectionParticipant_chc08_quote() {
		String[] quotes = { "\"", "'" };
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s/usr/bin/chc08%1$s -I /foo/clang -C blah.c", quote);
			IToolDetectionParticipant result = ParticipantTestUtil.determineToolDetectionParticipant(args, null, true);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(FreescaleToolDetectionParticipant.class, result.getClass());
		}
	}
}