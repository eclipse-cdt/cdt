/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import org.eclipse.cdt.core.lrparser.tests.c99.*;
import org.eclipse.cdt.core.lrparser.tests.cpp.ISOCPPParserTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;

public class LRParserTestSuite extends TestSuite {
	
	// TODO: the following test are not being reused
	//
	// DOMGCCSelectionParseExtensionsTest
	// DOMSelectionParseTest
	// GCCCompleteParseExtensionsTest
	// QuickParser2Tests
	//
	// and perhaps others
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		suite.addTest(C99ParserTestSuite.suite());
		suite.addTest(ISOCPPParserTestSuite.suite());
		
		return suite;
	}	
}

