/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ScannerTestSuite extends TestSuite {

	public static Test suite() { 
		TestSuite suite= new ScannerTestSuite();
		suite.addTest(LexerTests.suite());
		suite.addTest(LocationMapTests.suite());
		suite.addTest(PortedScannerTests.suite());
		suite.addTest(PreprocessorTests.suite());
		suite.addTest(InclusionTests.suite());
		suite.addTest(PreprocessorBugsTests.suite());
		suite.addTest(ExpansionExplorerTests.suite());
		suite.addTest(InactiveCodeTests.suite());
		suite.addTest(StreamHasherTests.suite());
		return suite;
	}	
}
