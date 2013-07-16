/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.thirdParty.tests.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllThirdPartyTests {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllThirdPartyTests.suite());
	}
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.cdt.thirdParty.tests");
		//$JUnit-BEGIN$

		suite.addTest(ThirdPartyLegacyScannerInfoProviderTest.suite());
		suite.addTest(ThirdPartyLegacyPathEntryContainerProviderTest.suite());
		
		//$JUnit-END$
		return suite;
	}
}
