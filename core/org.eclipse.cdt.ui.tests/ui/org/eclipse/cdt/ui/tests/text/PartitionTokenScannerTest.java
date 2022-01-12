/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.cdt.internal.ui.text.FastCPartitionScanner;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Compares two <code>IParitionTokenScanner</code>s for performance.
 */
public class PartitionTokenScannerTest extends TestCase {
	private static boolean PRINT_TIMING = false;
	private IPartitionTokenScanner fReference;
	private IPartitionTokenScanner fTestee;

	public PartitionTokenScannerTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() {
		fReference = new CPartitionScanner();
		fTestee = new FastCPartitionScanner();
	}

	// read sample C file
	private IDocument getDocument(String name, String lineDelimiter) {
		try {
			InputStream stream = getClass().getResourceAsStream(name);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

			StringBuilder buffer = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line);
				buffer.append(lineDelimiter);
				line = reader.readLine();
			}
			return new Document(buffer.toString());
		} catch (IOException e) {
		}

		return null;
	}

	private static IDocument getRandomDocument(int size) {
		final char[] characters = { '/', '*', '\'', '"', '\r', '\n', '\\' };
		final StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < size; i++) {
			final int randomIndex = (int) (Math.random() * characters.length);
			buffer.append(characters[randomIndex]);
		}

		return new Document(buffer.toString());
	}

	public static Test suite() {
		return new TestSuite(PartitionTokenScannerTest.class);
	}

	/**
	 * Tests performance of the testee against the reference IPartitionTokenScanner.
	 */
	public void testPerformance() {
		final int COUNT = 5000;
		final IDocument document = getDocument("TestCase.txt", "\n");

		final long referenceTime = getTime(fReference, document, COUNT);
		final long testeeTime = getTime(fTestee, document, COUNT);

		if (PRINT_TIMING) {
			System.out.println("reference time = " + referenceTime / 1000.0f);
			System.out.println("testee time = " + testeeTime / 1000.0f);
			System.out.println("factor = " + (float) referenceTime / testeeTime);
		}

		// dangerous: assert no regression in performance
		// assertTrue(testeeTime <= referenceTime);
	}

	private long getTime(IPartitionTokenScanner scanner, IDocument document, int count) {
		final long start = System.currentTimeMillis();

		for (int i = 0; i < count; i++)
			testPerformance(scanner, document);

		final long end = System.currentTimeMillis();

		return end - start;
	}

	private static void testPerformance(final IPartitionTokenScanner scanner, final IDocument document) {
		scanner.setRange(document, 0, document.getLength());

		IToken token;
		do {
			token = scanner.nextToken();
			scanner.getTokenOffset();
			scanner.getTokenLength();

		} while (!token.isEOF());
	}

	private void assertTokenEquals(IToken expected, IToken actual) {
		assertEquals(expected.isEOF(), actual.isEOF());
		assertEquals(expected.isOther(), actual.isOther());
		assertEquals(expected.isUndefined(), actual.isUndefined());
		assertEquals(expected.isWhitespace(), actual.isWhitespace());
	}

	private static String extractString(IDocument document, int offset) {
		final StringBuilder buffer = new StringBuilder();

		try {
			IRegion region = document.getLineInformationOfOffset(offset);
			String line = document.get(region.getOffset(), region.getLength());

			int offsetIndex = offset - region.getOffset();

			// TODO kludge
			if (offsetIndex > line.length())
				offsetIndex = line.length();

			buffer.append("line = ").append(document.getLineOfOffset(offset)).append(": [");
			buffer.append(line.substring(0, offsetIndex));
			buffer.append("<POS>");
			buffer.append(line.substring(offsetIndex));
			buffer.append(']');
		} catch (BadLocationException e) {
		}

		return buffer.toString();
	}

	/**
	 * Escapes CR, LF and TAB in a string.
	 */
	private static String escape(String string) {
		final StringBuilder buffer = new StringBuilder();

		final int length = string.length();
		for (int i = 0; i < length; i++) {
			final char character = string.charAt(i);
			switch (character) {
			case '\t':
				buffer.append("\\t");
				break;

			case '\r':
				buffer.append("\\r");
				break;

			case '\n':
				buffer.append("\\n");
				break;

			default:
				buffer.append(character);
				break;
			}
		}

		return buffer.toString();
	}
}
