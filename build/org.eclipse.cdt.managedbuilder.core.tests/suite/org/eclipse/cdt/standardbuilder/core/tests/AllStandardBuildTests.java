/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.standardbuilder.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Invokes all standard builder tests
 * 
 * @author vhirsl
 */
public class AllStandardBuildTests extends TestSuite {

	/**
	 * @param string
	 */
	public AllStandardBuildTests(String title) {
		super(title);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllStandardBuildTests.suite());
	}

	public static Test suite() {
		TestSuite suite = new AllStandardBuildTests("Test for org.eclipse.cdt.standardbuild.core.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(ScannerConfigConsoleParserTests.class);
		//$JUnit-END$
		return suite;
	}
}
