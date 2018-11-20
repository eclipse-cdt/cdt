/*******************************************************************************
 * Copyright (c) 2009,2012 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.cdt.codan.core.tests.TestUtils;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * Test for quick fix for suggested parenthesis
 */
@SuppressWarnings("restriction")
public class SuggestedParenthesisQuickFixTest extends QuickFixTestCase {
	@Override
	public AbstractCodanCMarkerResolution createQuickFix() {
		return new SuggestedParenthesisQuickFix();
	}

	//	 main() {
	//	   int a=1,b=3;
	//	   if (b+a && a>b || b-a) b--; // error here
	//	 }
	public void testSimple() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("(b+a && a>b)", result); //$NON-NLS-1$
	}

	// @file:header.h
	// int foo();

	// @file:main.c
	// #include "header.h"
	// main() {
	//   foo();
	// }
	public void test2FilesExample() throws Exception {
		/*
		 * There are no problems in either of the two files, so quick fix is not called.
		 */
		CharSequence[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		// lets pretend marker is found in main.c but fixes go in both files,
		// to check do something like this
		EditorUtility.openInEditor(f2);
		runCodan();
		doRunQuickFix();
		String result_main = TestUtils.loadFile(new FileInputStream(f2));
		String result_header = TestUtils.loadFile(new FileInputStream(f1));
		assertContainedIn("foo", result_main); //$NON-NLS-1$
		assertContainedIn("foo", result_header); //$NON-NLS-1$
	}
}
