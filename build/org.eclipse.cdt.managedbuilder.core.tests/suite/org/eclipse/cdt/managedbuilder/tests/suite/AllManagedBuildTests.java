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
package org.eclipse.cdt.managedbuilder.tests.suite;

import org.eclipse.cdt.managedbuild.core.tests.ManagedBuildCoreTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllManagedBuildTests {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllManagedBuildTests.suite());
	}
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.cdt.managedbuild.core.tests");
		//$JUnit-BEGIN$
		suite.addTest(ManagedBuildCoreTests.suite());
		//$JUnit-END$
		return suite;
	}
}
