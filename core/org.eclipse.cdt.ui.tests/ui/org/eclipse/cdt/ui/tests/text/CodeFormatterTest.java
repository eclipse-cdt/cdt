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
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.formatter.DefaultCodeFormatterOptions;

/**
 * Tests for the CodeFormatter.
 *
 * @since 4.0
 */
public class CodeFormatterTest extends BaseUITestCase {

	private Map fOptions;
	private Map fDefaultOptions;

	public static TestSuite suite() {
		return suite(CodeFormatterTest.class, "_");
	}

	protected void setUp() throws Exception {
		super.setUp();
		fDefaultOptions= DefaultCodeFormatterOptions.getDefaultSettings().getMap();
		fOptions= new HashMap(fDefaultOptions);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void assertFormatterResult() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		IDocument document= new Document(before);
		String expected= contents[1].toString();
		TextEdit edit= CodeFormatterUtil.format(CodeFormatter.K_COMPILATION_UNIT, before, 0, TextUtilities.getDefaultLineDelimiter(document), fOptions);
		assertNotNull(edit);
		edit.apply(document);
		assertEquals(expected, document.get());
	}
	
	//void foo(int arg);
	//void foo(int arg){}
	
	//void foo (int arg);
	//void foo (int arg) {
	//}
	public void testInsertSpaceBeforeOpeningParen_Bug190184() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_DECLARATION, CCorePlugin.INSERT);
		assertFormatterResult();
	}
}
