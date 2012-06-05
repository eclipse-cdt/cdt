/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.framework;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * This base class sets the attributes necessary to do a
 * remote debugging session.
 */
public class BaseRemoteSuite {
	@BeforeClass
    public static void baseRemoteSuiteBeforeClassMethod() {
		BaseTestCase.setGlobalLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				                        IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
	}
	@AfterClass
    public static void baseRemoteSuiteAfterClassMethod() {
		BaseTestCase.removeGlobalLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE);
	}
}
