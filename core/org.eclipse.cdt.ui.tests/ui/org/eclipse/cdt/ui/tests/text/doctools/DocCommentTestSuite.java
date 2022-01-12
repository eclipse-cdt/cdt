/*******************************************************************************
 * Copyright (c) 2008, 2015 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.doctools;

import org.eclipse.cdt.ui.tests.text.doctools.doxygen.DoxygenCCommentAutoEditStrategyTest;
import org.eclipse.cdt.ui.tests.text.doctools.doxygen.DoxygenCCommentSingleAutoEditStrategyTest;

import junit.framework.TestSuite;

public class DocCommentTestSuite extends TestSuite {

	public static TestSuite suite() {
		return new DocCommentTestSuite();
	}

	public DocCommentTestSuite() {
		super(DocCommentTestSuite.class.getName());

		addTest(CommentOwnerManagerTests.suite());
		addTest(DocCommentHighlightingTest.suite());
		addTest(DoxygenCCommentAutoEditStrategyTest.suite());
		addTest(DoxygenCCommentSingleAutoEditStrategyTest.suite());
	}
}
