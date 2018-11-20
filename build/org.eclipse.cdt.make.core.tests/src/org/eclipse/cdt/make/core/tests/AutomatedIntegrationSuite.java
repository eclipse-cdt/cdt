/*******************************************************************************
 * Copyright (c) 2004, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.core.tests;

import org.eclipse.cdt.make.builder.tests.StandardBuildTests;
import org.eclipse.cdt.make.scannerdiscovery.ScannerDiscoveryTests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AutomatedIntegrationSuite extends TestSuite {

	public AutomatedIntegrationSuite() {
	}

	public AutomatedIntegrationSuite(Class<? extends TestCase> theClass, String name) {
		super(theClass, name);
	}

	public AutomatedIntegrationSuite(Class<? extends TestCase> theClass) {
		super(theClass);
	}

	public AutomatedIntegrationSuite(String name) {
		super(name);
	}

	public static Test suite() {
		final AutomatedIntegrationSuite suite = new AutomatedIntegrationSuite();

		suite.addTest(StandardBuildTests.suite());
		suite.addTest(ScannerDiscoveryTests.suite());
		suite.addTest(MakefileReaderProviderTests.suite());
		return suite;
	}
}
