/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.ParserTestSuite;

/**
 * @author jcamelon
 */
public class DOMParserTestSuite extends TestCase {

	public static Test suite() { 
		TestSuite suite= new TestSuite(ParserTestSuite.class.getName());
		suite.addTestSuite( AST2Tests.class );
		suite.addTestSuite( GCCTests.class );
		suite.addTestSuite( AST2CPPTests.class );
		suite.addTestSuite( AST2TemplateTests.class );
		suite.addTestSuite( QuickParser2Tests.class );
		suite.addTestSuite( CompleteParser2Tests.class );
//		suite.addTestSuite( DOMScannerTests.class );
		suite.addTestSuite( DOMLocationTests.class );
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
		return suite;
	}	


}
