/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.ParserTestSuite;
import org.eclipse.cdt.core.parser.tests.prefix.CompletionTestSuite;

/**
 * @author jcamelon
 */
public class DOMParserTestSuite extends TestCase {

	public static Test suite() { 
		TestSuite suite= new TestSuite(ParserTestSuite.class.getName());
		suite.addTest( AST2Tests.suite() );
		suite.addTestSuite( GCCTests.class );
		suite.addTestSuite( AST2CPPTests.class );
		suite.addTest( AST2TemplateTests.suite() );
		suite.addTestSuite( QuickParser2Tests.class );
		suite.addTest( CompleteParser2Tests.suite() );
		suite.addTest( DOMLocationTests.suite() );
        suite.addTestSuite( DOMLocationMacroTests.class );
		suite.addTest( DOMLocationInclusionTests.suite() );
		suite.addTestSuite( AST2KnRTests.class );
		suite.addTestSuite( AST2UtilTests.class );
		suite.addTestSuite( AST2UtilOldTests.class );
		suite.addTestSuite( AST2SelectionParseTest.class );		
		suite.addTestSuite( CodeReaderCacheTest.class );
		suite.addTestSuite( AST2CPPSpecTest.class );
		suite.addTestSuite( AST2CPPSpecFailingTest.class );
		suite.addTestSuite( AST2CSpecTest.class );
		suite.addTestSuite( AST2CSpecFailingTest.class );
		suite.addTestSuite( DOMSelectionParseTest.class );
		suite.addTestSuite( GCCCompleteParseExtensionsTest.class );
		suite.addTest( CompletionTestSuite.suite() );
		return suite;
	}	

}
