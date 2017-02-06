/*******************************************************************************
 * Copyright (c) 2015 QNX Software System and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.CommentChecker;
import org.junit.Test;

/**
 * Tests for CommentChecker
 */
public class CommentCheckerNestedTests extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(CommentChecker.COMMENT_NO_START);
	}

	//	void foo() {
	//	  return; /* /* */ // error
	//	}
	@Test
	public void testLineComment() throws Exception {
		checkSampleAbove();
	}
	//	void foo() {
	//	  return; /*
	//    /* // error
	//    */
	//	}
	@Test
	public void testLineComment2() throws Exception {
		checkSampleAbove();
	}

	//	void foo() {
	//	  return; /* */
	//	}
	@Test
	public void testNoLineComment() throws Exception {
		checkSampleAbove();
	}
}
