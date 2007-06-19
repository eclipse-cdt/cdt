/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.LineRange;

import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.formatter.DefaultCodeFormatterOptions;

import org.eclipse.cdt.internal.ui.editor.CDocumentSetupParticipant;
import org.eclipse.cdt.internal.ui.editor.IndentUtil;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.CIndenter;

/**
 * Tests for the CIndenter.
 *
 * @since 4.0
 */
public class CIndenterTest extends BaseUITestCase {

	private Map fOptions;
	private Map fDefaultOptions;

	public static TestSuite suite() {
		return suite(CIndenterTest.class, "_");
	}

	protected void setUp() throws Exception {
		super.setUp();
		fDefaultOptions= DefaultCodeFormatterOptions.getDefaultSettings().getMap();
		fOptions= new HashMap(fDefaultOptions);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void assertIndenterResult() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		IDocument document= new Document(before);
		String expected= contents[1].toString();
		new CDocumentSetupParticipant().setup(document);
		CIndenter indenter= new CIndenter(document, new CHeuristicScanner(document));
		IndentUtil.indentLines(document, new LineRange(0, document.getNumberOfLines()), null, null);
		assertEquals(expected, document.get());
	}
	
	//foo(arg,
	//"string");
	
	//foo(arg,
	//		"string");
	public void testIndentationOfStringLiteralAsLastArgument_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//if (1)
	//foo->bar();
	//dontIndent();
	
	//if (1)
	//	foo->bar();
	//dontIndent();
	public void testIndentationAfterArrowOperator_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//if (1)
	//foo>>bar;
	//  dontIndent();
	
	//if (1)
	//	foo>>bar;
	//dontIndent();
	public void testIndentationAfterShiftRight_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//if (1)
	//foo >= bar();
	//  dontIndent();
	
	//if (1)
	//	foo >= bar();
	//dontIndent();
	public void testIndentationAfterGreaterOrEquals_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//struct x {
	// int f1 : 1;
	// int f2 : 1;
	// int f3 : 1;
	//}
	
	//struct x {
	//	int f1 : 1;
	//	int f2 : 1;
	//	int f3 : 1;
	//}
	public void testIndentationOfBitFields_Bug193298() throws Exception {
		assertIndenterResult();
	}

}
