/*******************************************************************************
 * Copyright (c) 2008, 2015 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
