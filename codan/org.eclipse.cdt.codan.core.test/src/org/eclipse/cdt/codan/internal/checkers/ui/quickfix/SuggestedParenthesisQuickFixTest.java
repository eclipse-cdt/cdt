/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.cdt.codan.core.test.TestUtils;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.runtime.CoreException;

/**
 * Test for quick fix for suggested parenthesis
 */
public class SuggestedParenthesisQuickFixTest extends QuickFixTestCase {
	@SuppressWarnings("restriction")
	@Override
	public AbstractCodanCMarkerResolution createQuickFix() {
		return new SuggestedParenthesisQuickFix();
	}

	//	 main() {
	//	   int a=1,b=3;
	//	   if (b+a && a>b || b-a) b--; // error here 
	//	 }
	public void testSimple() throws IOException, CoreException {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("(b+a && a>b)", result); //$NON-NLS-1$
	}

	// @file:header.h
	// int foo();
	/* ---- */
	// @file:main.c
	// #include "header.h"
	// main() {
	//   foo();
	// }
	/*
	 * this test is using two files, there was not actually bugs here so
	 * quick fix is not called
	 */
	public void test2FilesExample() throws FileNotFoundException, IOException {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		// lets pretend marker is found in main.c but fixes go in both files,
		// to check do something like this
		try {
			EditorUtility.openInEditor(f2);
			runCodan();
			doRunQuickFix();
			String result_main = TestUtils.loadFile(new FileInputStream(f2));
			String result_header = TestUtils.loadFile(new FileInputStream(f1));
			assertContainedIn("foo", result_main); //$NON-NLS-1$
			assertContainedIn("foo", result_header); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
