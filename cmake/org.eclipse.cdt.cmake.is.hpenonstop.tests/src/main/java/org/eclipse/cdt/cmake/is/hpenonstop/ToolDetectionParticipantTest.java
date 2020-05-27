/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.hpenonstop;

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
	public void testDetermineToolDetectionParticipant_c89() {
		IToolDetectionParticipant result = ParticipantTestUtil
				.determineToolDetectionParticipant("/usr/bin/c89 -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC89ToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("c89 -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC89ToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("/usr/bin/c89.exe -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC89ToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("c89.exe -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC89ToolDetectionParticipant.class, result.getClass());
	}

	@Test
	public void testDetermineToolDetectionParticipant_c89_quote() {
		String[] quotes = { "\"", "'" };
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s/usr/bin/c89%1$s -I /foo/c89 -C blah.c", quote);
			IToolDetectionParticipant result = ParticipantTestUtil.determineToolDetectionParticipant(args, null, true);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(HpeC89ToolDetectionParticipant.class, result.getClass());
		}
	}

	@Test
	public void testDetermineToolDetectionParticipant_c99() {
		IToolDetectionParticipant result = ParticipantTestUtil
				.determineToolDetectionParticipant("/usr/bin/c99 -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC99ToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("c99 -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC99ToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("/usr/bin/c99.exe -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC99ToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("c99.exe -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC99ToolDetectionParticipant.class, result.getClass());
	}

	@Test
	public void testDetermineToolDetectionParticipant_c99_quote() {
		String[] quotes = { "\"", "'" };
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s/usr/bin/c99%1$s -I /foo/c99 -C blah.c", quote);
			IToolDetectionParticipant result = ParticipantTestUtil.determineToolDetectionParticipant(args, null, true);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(HpeC99ToolDetectionParticipant.class, result.getClass());
		}
	}

	@Test
	public void testDetermineToolDetectionParticipant_c11() {
		IToolDetectionParticipant result = ParticipantTestUtil
				.determineToolDetectionParticipant("/usr/bin/c11 -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC11ToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("c11 -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC11ToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("/usr/bin/c11.exe -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC11ToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("c11.exe -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(HpeC11ToolDetectionParticipant.class, result.getClass());
	}

	@Test
	public void testDetermineToolDetectionParticipant_c11_quote() {
		String[] quotes = { "\"", "'" };
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s/usr/bin/c11%1$s -I /foo/c11 -C blah.c", quote);
			IToolDetectionParticipant result = ParticipantTestUtil.determineToolDetectionParticipant(args, null, true);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(HpeC11ToolDetectionParticipant.class, result.getClass());
		}
	}
}
