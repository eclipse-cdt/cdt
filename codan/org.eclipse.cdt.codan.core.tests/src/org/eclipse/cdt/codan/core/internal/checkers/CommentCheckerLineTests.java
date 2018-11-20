/*******************************************************************************
 * Copyright (c) 2015 QNX Software System and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import java.io.File;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.CommentChecker;
import org.junit.Test;

/**
 * Tests for CommentChecker
 */
public class CommentCheckerLineTests extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(CommentChecker.COMMENT_NO_LINE);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//	void foo() {
	//	  return; // error
	//	}
	@Test
	public void testLineComment() throws Exception {
		checkSampleAboveC();
	}

	//	void foo() {
	//	  return;
	//	}
	@Test
	public void testNoLineComment() throws Exception {
		checkSampleAboveC();
	}

	//	char * foo() {
	//	  return "// this is a string";
	//	}
	@Test
	public void testNoLineCommentInString() throws Exception {
		checkSampleAboveC();
	}

	//	void foo() {
	//	  return; // not an error in c++
	//	}
	@Test
	public void testLineCommentCpp() throws Exception {
		checkSampleAboveCpp();
	}

	//	#define AAA // error even in prepro
	@Test
	public void testLineCommentInPrepro() throws Exception {
		checkSampleAboveC();
	}

	// @file:test.h
	// int foo();// error too

	// @file:test.c
	// #include "test.h"
	// int bar() {
	//    foo();
	// }
	public void testHeader() throws Exception {
		loadcode(getContents(2));
		runOnProject();
		checkErrorLine(new File("test.h"), 1); //$NON-NLS-1$
	}
}
