/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.make.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.make.builder.tests.StandardBuildTests;

public class AutomatedIntegrationSuite extends TestSuite {

	public AutomatedIntegrationSuite() {
	}

	public AutomatedIntegrationSuite(Class theClass, String name) {
		super(theClass, name);
	}

	public AutomatedIntegrationSuite(Class theClass) {
		super(theClass);
	}

	public AutomatedIntegrationSuite(String name) {
		super(name);
	}

	public static Test suite() {
		final AutomatedIntegrationSuite suite = new AutomatedIntegrationSuite();

		suite.addTest(StandardBuildTests.suite());
		return suite;
	}

}
