/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.rules.FastPartitioner;

import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.ui.text.CPairMatcher;
import org.eclipse.cdt.internal.ui.text.FastCPartitionScanner;

public class PairMatcherTest extends TestCase {
	
	private static boolean BEFORE_MATCHES_DISABLED= true;
	
	protected IDocument fDocument;
	protected CPairMatcher fPairMatcher;
	
	
	public PairMatcherTest(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() {
		Document document= new Document("xx(yy(xx)yy)xx");
		String[] types= new String[] {
				ICPartitions.C_MULTI_LINE_COMMENT,
				ICPartitions.C_SINGLE_LINE_COMMENT,
				ICPartitions.C_STRING,
				ICPartitions.C_CHARACTER,
				IDocument.DEFAULT_CONTENT_TYPE
		};
		FastPartitioner partitioner= new FastPartitioner(new FastCPartitionScanner(), types);
		partitioner.connect(document);
		document.setDocumentPartitioner(ICPartitions.C_PARTITIONING, partitioner);

		fDocument= document;
		fPairMatcher= new CPairMatcher(new char[] { '(', ')', '<', '>' });
	}
	
	public static Test suite() {
		return new TestSuite(PairMatcherTest.class); 
	}
	
	@Override
	protected void tearDown () {
		fDocument= null;
		fPairMatcher= null;
	}
	
	public void testBeforeOpeningMatch() {
		IRegion match= fPairMatcher.match(fDocument, 2);
		if (BEFORE_MATCHES_DISABLED) {
			assertNull(match);
		} else {
			assertNotNull(match);
			assertTrue(match.getOffset() == 2 && match.getLength() == 10);
		}
		
		match= fPairMatcher.match(fDocument, 5);
		if (BEFORE_MATCHES_DISABLED) {
			assertNull(match);
		} else {
			assertNotNull(match);
			assertTrue(match.getOffset() == 5 && match.getLength() == 4);		
		}
	}
	
	public void testAfterOpeningMatch() {
		IRegion match= fPairMatcher.match(fDocument, 3);
		assertNotNull(match);
		assertTrue(match.getOffset() == 2 && match.getLength() == 10);
		
		match= fPairMatcher.match(fDocument, 6);
		assertNotNull(match);
		assertTrue(match.getOffset() == 5 && match.getLength() == 4);		
	}
	
	public void testBeforeClosingMatch() {
		IRegion match= fPairMatcher.match(fDocument, 11);
		if (BEFORE_MATCHES_DISABLED) {
			assertNull(match);
		} else {
			assertNotNull(match);
			assertTrue(match.getOffset() == 2 && match.getLength() == 10);
		}
		
		match= fPairMatcher.match(fDocument, 8);
		if (BEFORE_MATCHES_DISABLED) {
			assertNull(match);
		} else {
			assertNotNull(match);
			assertTrue(match.getOffset() == 5 && match.getLength() == 4);		
		}
	}
	
	public void testAfterClosingMatch() {
		IRegion match= fPairMatcher.match(fDocument, 12);
		assertNotNull(match);
		assertTrue(match.getOffset() == 2 && match.getLength() == 10);
		
		match= fPairMatcher.match(fDocument, 9);
		assertNotNull(match);
		assertTrue(match.getOffset() == 5 && match.getLength() == 4);		
	}	
	
	public void testBeforeClosingMatchWithNL() {
		fDocument.set("x(y\ny)x");
		IRegion match= fPairMatcher.match(fDocument, 5);
		if (BEFORE_MATCHES_DISABLED) {
			assertNull(match);
		} else {
			assertNotNull(match);
			assertTrue(match.getOffset() == 1 && match.getLength() == 5);
		}
	}	
	
	public void testAfterClosingMatchWithNL() {
		fDocument.set("x(y\ny)x");
		IRegion match= fPairMatcher.match(fDocument, 6);
		assertNotNull(match);
		assertTrue(match.getOffset() == 1 && match.getLength() == 5);
	}
	
	public void testBeforeClosingMatchWithNLAndSingleLineComment() {
		fDocument.set("x\nx(y\nx //(x\ny)x");
		IRegion match= fPairMatcher.match(fDocument, 14);
		if (BEFORE_MATCHES_DISABLED) {
			assertNull(match);
		} else {
			assertNotNull(match);
			assertTrue(match.getOffset() == 3 && match.getLength() == 12);
		}
	}
	
	public void testAfterClosingMatchWithNLAndSingleLineComment() {
		fDocument.set("x\nx(y\nx //(x\ny)x");
		IRegion match= fPairMatcher.match(fDocument, 15);
		assertNotNull(match);
		assertTrue(match.getOffset() == 3 && match.getLength() == 12);
	}

	public void testAngleBracketsAsOperators() {
		fDocument.set("void f(){ \n\tif (x<y);\n\twhile(x>y)\n\t\tx << 2; y >> 1;\n}");
		int idx= fDocument.get().indexOf('<', 0);
		while (idx >= 0) {
			IRegion match= fPairMatcher.match(fDocument, idx + 1);
			assertNull(match);
			idx= fDocument.get().indexOf('<', idx + 1);
		}
		idx= fDocument.get().indexOf('>', 0);
		while (idx >= 0) {
			IRegion match= fPairMatcher.match(fDocument, idx + 1);
			assertNull(match);
			idx= fDocument.get().indexOf('>', idx + 1);
		}
	}

	public void testAngleBracketsAsPairs() {
		fDocument.set("template < class X > class A {};}");
		int idx= fDocument.get().indexOf('<', 0);
		IRegion match= fPairMatcher.match(fDocument, idx + 1);
		assertNotNull(match);
		int otherIdx= fDocument.get().indexOf('>');
		assertEquals(otherIdx, match.getOffset() + match.getLength() - 1);
		
		match= fPairMatcher.match(fDocument, otherIdx + 1);
		assertNotNull(match);
		assertEquals(idx, match.getOffset());
	}

	public void testAngleBracketsAsPairs2() {
		fDocument.set("ConstTemplate c<5>;");
		int idx= fDocument.get().indexOf('<', 0);
		IRegion match= fPairMatcher.match(fDocument, idx + 1);
		assertNotNull(match);
		int otherIdx= fDocument.get().indexOf('>');
		assertEquals(otherIdx, match.getOffset() + match.getLength() - 1);
		
		match= fPairMatcher.match(fDocument, otherIdx + 1);
		assertNotNull(match);
		assertEquals(idx, match.getOffset());
	}

	public void testAngleBracketsAsPairsNested() {
		fDocument.set("OtherTemplate nested<map<int,int>,Y>;");
		int idx= fDocument.get().indexOf('<', 0);
		IRegion match= fPairMatcher.match(fDocument, idx + 1);
		assertNotNull(match);
		int otherIdx= fDocument.get().lastIndexOf('>');
		assertEquals(otherIdx, match.getOffset() + match.getLength() - 1);

		match= fPairMatcher.match(fDocument, otherIdx + 1);
		assertNotNull(match);
		assertEquals(idx, match.getOffset());

		idx= fDocument.get().indexOf('<', idx+1);
		match= fPairMatcher.match(fDocument, idx + 1);
		assertNotNull(match);
		otherIdx= fDocument.get().indexOf('>', idx + 1);
		assertEquals(otherIdx, match.getOffset() + match.getLength() - 1);
	}
	
	public void testAngleBracketsAsPairsMultiline() {
		fDocument.set("OtherTemplate nested<\n\tmap<int,int>,Y\n>;");
		int idx= fDocument.get().indexOf('<', 0);
		IRegion match= fPairMatcher.match(fDocument, idx + 1);
		assertNotNull(match);
		int otherIdx= fDocument.get().lastIndexOf('>');
		assertEquals(otherIdx, match.getOffset() + match.getLength() - 1);

		match= fPairMatcher.match(fDocument, otherIdx + 1);
		assertNotNull(match);
		assertEquals(idx, match.getOffset());

		idx= fDocument.get().indexOf('<', idx+1);
		match= fPairMatcher.match(fDocument, idx + 1);
		assertNotNull(match);
		otherIdx= fDocument.get().indexOf('>', idx + 1);
		assertEquals(otherIdx, match.getOffset() + match.getLength() - 1);
	}	

	public void testDoubleClosingAngleBrackets_Bug335702() {
		fDocument.set("list<list<int>> a;");
		int idx= fDocument.get().indexOf('<', 0);
		IRegion match= fPairMatcher.match(fDocument, idx + 1);
		assertNotNull(match);
		int otherIdx= fDocument.get().lastIndexOf('>');
		assertEquals(otherIdx, match.getOffset() + match.getLength() - 1);

		match= fPairMatcher.match(fDocument, otherIdx + 1);
		assertNotNull(match);
		assertEquals(idx, match.getOffset());

		idx= fDocument.get().indexOf('<', idx+1);
		match= fPairMatcher.match(fDocument, idx + 1);
		assertNotNull(match);
		otherIdx= fDocument.get().indexOf('>', idx + 1);
		assertEquals(otherIdx, match.getOffset() + match.getLength() - 1);
	}
	
}
