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
