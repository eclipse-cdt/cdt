/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.tests.CModelElementsTests;
import org.eclipse.cdt.core.model.tests.StructuralCModelElementsTests;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ParserTestSuite extends TestCase {
	public static Test suite() { 
		TestSuite suite= new TestSuite(ParserTestSuite.class.getName()); 
		suite.addTestSuite(BranchTrackerTest.class);
		suite.addTestSuite(ScannerTestCase.class);
		suite.addTestSuite(ExprEvalTest.class);
		suite.addTestSuite(QuickParseASTTests.class);
		suite.addTestSuite(ParserSymbolTableTest.class);
		suite.addTestSuite(ParserSymbolTableTemplateTests.class );
		suite.addTestSuite(CModelElementsTests.class);
		suite.addTestSuite(StructuralCModelElementsTests.class);
		suite.addTestSuite(CompletionParseTest.class);
		suite.addTestSuite(QuickParseProblemTests.class);
//		suite.addTestSuite(MacroTests.class);
		suite.addTestSuite( PreprocessorConditionalTest.class );
		suite.addTestSuite( QuickParseASTQualifiedNameTest.class);
		suite.addTestSuite( CompleteParseASTTest.class );
		suite.addTestSuite( CompleteParseProblemTest.class );
		suite.addTestSuite( SelectionParseTest.class );
		suite.addTestSuite( CompleteParseASTExpressionTest.class );
		suite.addTestSuite( CompleteParseASTSymbolIteratorTest.class );
		suite.addTestSuite( CompleteParseASTTemplateTest.class );
		suite.addTestSuite( StructuralParseTest.class );
		return suite;
	}	
}
