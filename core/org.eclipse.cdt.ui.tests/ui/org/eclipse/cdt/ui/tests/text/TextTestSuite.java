/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.tests.text;

import junit.framework.TestSuite;

public class TextTestSuite extends TestSuite {

    public static TestSuite suite() {
        return new TextTestSuite();
    }
    
    public TextTestSuite() {
        super("Tests in package org.eclipse.cdt.ui.tests.text");

        // partitioning tests
		addTest(PartitionTokenScannerTest.suite());
		addTest(CPartitionerTest.suite());

        // smart edit tests
		addTest(CAutoIndentTest.suite());
		addTest(CHeuristicScannerTest.suite());
		addTest(BracketInserterTest.suite());
		addTest(IndentActionTest.suite());
		addTest(FormatActionTest.suite());

		// Break iterator tests.
		addTest(CBreakIteratorTest.suite());
		addTest(CWordIteratorTest.suite());

		// highlighting tests
		addTest(SemanticHighlightingTest.suite());
		addTest(InactiveCodeHighlightingTest.suite());
		addTest(CHeaderRuleTest.suite());
		addTest(NumberRuleTest.suite());
		addTest(PairMatcherTest.suite());

		// folding tests
		addTest(FoldingTest.suite());
		
		// basic editing tests
		addTest(BasicCEditorTest.suite());
		
		// editor hyperlink tests
		addTest(HyperlinkTest.suite());
    }
}
