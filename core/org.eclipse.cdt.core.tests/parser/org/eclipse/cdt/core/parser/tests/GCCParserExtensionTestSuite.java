/**********************************************************************
 * Copyright (c) 2004 IBM Canada Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import org.eclipse.cdt.core.parser.tests.scanner2.GCCScannerExtensionsTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author jcamelon
 *
 */
public class GCCParserExtensionTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite= new TestSuite(GCCParserExtensionTestSuite.class.getName());
		suite.addTestSuite( GCCScannerExtensionsTest.class );
		suite.addTestSuite( GCCQuickParseExtensionsTest.class );
		suite.addTestSuite( GCCCompleteExtensionsParseTest.class );
		return suite;
	}
	
}
