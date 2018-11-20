/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.scannerdiscovery;

import junit.framework.TestSuite;

public class ScannerDiscoveryTests extends TestSuite {

	public static TestSuite suite() {
		return new ScannerDiscoveryTests();
	}

	public ScannerDiscoveryTests() {
		super(ScannerDiscoveryTests.class.getName());
		addTestSuite(ScannerConfigDiscoveryTests.class);
		addTest(GCCScannerInfoConsoleParserTests.suite());
		addTest(GCCPerFileBOPConsoleParserTests.suite());
		addTestSuite(ScannerConfigProfileTests.class);
	}
}
