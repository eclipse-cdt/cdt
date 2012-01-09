/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import junit.framework.TestSuite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.text.CWordFinder;

/**
 * Tests for CWordFinder.
 */
public class CWordFinderTest extends BaseUITestCase {

	public static TestSuite suite() {
		return suite(CWordFinderTest.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testBasic() {
		IDocument doc= new Document(" func(int a, int b);");
		IRegion region = CWordFinder.findWord(doc, 1);
		assertEquals(1, region.getOffset());
		assertEquals(4, region.getLength());
		region = CWordFinder.findWord(doc, 4);
		assertEquals(1, region.getOffset());
		assertEquals(4, region.getLength());
		region = CWordFinder.findWord(doc, 5);
		assertEquals(1, region.getOffset());
		assertEquals(4, region.getLength());
		region = CWordFinder.findWord(doc, 6);
		assertEquals(6, region.getOffset());
		assertEquals(3, region.getLength());
		region = CWordFinder.findWord(doc, 12);
		assertEquals(12, region.getOffset());
		assertEquals(0, region.getLength());
		doc= new Document("func();");
		region = CWordFinder.findWord(doc, 0);
		assertEquals(0, region.getOffset());
		assertEquals(4, region.getLength());
		region = CWordFinder.findWord(doc, 5);
		assertEquals(5, region.getOffset());
		assertEquals(0, region.getLength());
	}

	public void testFindWord() throws BadLocationException {
		IDocument doc= new Document();
		StringBuffer buf= new StringBuffer();
		String word= "word_0815";
		for (int i= 0; i < 10; i++) {
			buf.append(' ').append(word);
		}
		doc.set(buf.toString());
		for (int i= 0; i < doc.getLength(); i++) {
			IRegion wordRegion= CWordFinder.findWord(doc, i);
			assertNotNull(wordRegion);
			if (wordRegion.getLength() != 0) {
				assertEquals(word.length(), wordRegion.getLength());
				assertEquals(word, doc.get(wordRegion.getOffset(), wordRegion.getLength()));
			}
		}
	}

	public void testFindWordOnDocumentStart_Bug193461() {
		IDocument doc= new Document();
		doc.set("word");
		for (int i= 0; i < doc.getLength(); i++) {
			IRegion wordRegion= CWordFinder.findWord(doc, i);
			assertNotNull(wordRegion);
			assertEquals(doc.getLength(), wordRegion.getLength());
			assertEquals(0, wordRegion.getOffset());
		}
	}
}
