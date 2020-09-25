/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.arm;

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
	public void testDetermineToolDetectionParticipant_armcc() {
		IToolDetectionParticipant result = ParticipantTestUtil
				.determineToolDetectionParticipant("/usr/bin/armcc -I /foo/cc -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(ArmccToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("armcc -I /foo/cc -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(ArmccToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("/usr/bin/armcc.exe  -I /foo/cc -C blah.c", null,
				true);
		assertNotNull(result);
		assertEquals(ArmccToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("armcc.exe  -I /foo/cc -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(ArmccToolDetectionParticipant.class, result.getClass());
	}

	@Test
	public void testDetermineToolDetectionParticipant_armcc_quote() {
		String[] quotes = { "\"", "'" };
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s/usr/bin/armcc%1$s -I /foo/cc -C blah.c", quote);
			IToolDetectionParticipant result = ParticipantTestUtil.determineToolDetectionParticipant(args, null, true);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(ArmccToolDetectionParticipant.class, result.getClass());
		}
	}

	@Test
	public void testDetermineToolDetectionParticipant_armclang() {
		IToolDetectionParticipant result = ParticipantTestUtil
				.determineToolDetectionParticipant("/usr/bin/armclang  -I /foo/clang -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(ArmClangToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("armclang  -I /foo/clang -C blah.c", null, true);
		assertNotNull(result);
		assertEquals(ArmClangToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("/usr/bin/armclang.exe -I /foo/clang -C blah.c",
				null, true);
		assertNotNull(result);
		assertEquals(ArmClangToolDetectionParticipant.class, result.getClass());

		result = ParticipantTestUtil.determineToolDetectionParticipant("armclang.exe -I /foo/clang -C blah.c", null,
				true);
		assertNotNull(result);
		assertEquals(ArmClangToolDetectionParticipant.class, result.getClass());
	}

	@Test
	public void testDetermineToolDetectionParticipant_armclang_quote() {
		String[] quotes = { "\"", "'" };
		for (String quote : quotes) {
			String args = String.format(Locale.ROOT, "%1$s/usr/bin/armclang%1$s -I /foo/clang -C blah.c", quote);
			IToolDetectionParticipant result = ParticipantTestUtil.determineToolDetectionParticipant(args, null, true);
			assertNotNull("Command in quotes= " + quote, result);
			assertEquals(ArmClangToolDetectionParticipant.class, result.getClass());
		}
	}
}