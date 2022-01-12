/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.parser.scanner.StreamHasher;

import junit.framework.TestSuite;

/**
 * Unit test for StreamHasher class.
 */
public class StreamHasherTests extends BaseTestCase {

	private static final String TEXT = "'Twas brillig, and the slithy toves\r\n"
			+ "Did gyre and gimble in the wabe;\r\n" + "All mimsy were the borogoves,\r\n"
			+ "And the mome raths outgrabe.\r\n" + "\r\n" + "\"Beware the Jabberwock, my son!\r\n"
			+ "The jaws that bite, the claws that catch!\r\n" + "Beware the Jubjub bird, and shun\r\n"
			+ "The frumious Bandersnatch!\"\r\n" + "\r\n" + "He took his vorpal sword in hand:\r\n"
			+ "Long time the manxome foe he sought—\r\n" + "So rested he by the Tumtum tree,\r\n"
			+ "And stood awhile in thought.\r\n" + "\r\n" + "And as in uffish thought he stood,\r\n"
			+ "The Jabberwock, with eyes of flame,\r\n" + "Came whiffling through the tulgey wood,\r\n"
			+ "And burbled as it came!\r\n" + "\r\n" + "One, two! One, two! and through and through\r\n"
			+ "The vorpal blade went snicker-snack!\r\n" + "He left it dead, and with its head\r\n"
			+ "He went galumphing back.\r\n" + "\r\n" + "\"And hast thou slain the Jabberwock?\r\n"
			+ "Come to my arms, my beamish boy!\r\n" + "O frabjous day! Callooh! Callay!\"\r\n"
			+ "He chortled in his joy.\r\n" + "\r\n" + "'Twas brillig, and the slithy toves\r\n"
			+ "Did gyre and gimble in the wabe;\r\n" + "All mimsy were the borogoves,\r\n"
			+ "And the mome raths outgrabe.\r\n";

	public static TestSuite suite() {
		return suite(StreamHasherTests.class);
	}

	public StreamHasherTests() {
		super();
	}

	public StreamHasherTests(String name) {
		super(name);
	}

	public void testEmpty() throws Exception {
		// Verify that an empty string has a zero hash value.
		assertEquals(0, StreamHasher.hash(""));
		assertEquals(0, new StreamHasher().computeHash());
	}

	public void testChunks() throws Exception {
		// Verify that the hash value does not depend on partitioning of the character string into chunks.
		long h = StreamHasher.hash(TEXT);
		assertTrue(h != 0);
		for (int chunkSize = 1; chunkSize <= 20; chunkSize++) {
			StreamHasher hasher = new StreamHasher();
			for (int offset = 0; offset < TEXT.length(); offset += chunkSize) {
				char[] chunk = TEXT.substring(offset, Math.min(offset + chunkSize, TEXT.length())).toCharArray();
				hasher.addChunk(chunk);
			}
			assertEquals(h, hasher.computeHash());
		}
	}
}
