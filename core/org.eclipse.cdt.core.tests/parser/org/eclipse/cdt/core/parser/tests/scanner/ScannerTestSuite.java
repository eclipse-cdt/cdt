/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Emanuel Graf (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author jcamelon
 */
public class ScannerTestSuite extends TestSuite {

	public static Test suite() { 
		TestSuite suite= new ScannerTestSuite();
		suite.addTest(LexerTests.suite());
		suite.addTest(LocationMapTests.suite());
		return suite;
	}	
}
