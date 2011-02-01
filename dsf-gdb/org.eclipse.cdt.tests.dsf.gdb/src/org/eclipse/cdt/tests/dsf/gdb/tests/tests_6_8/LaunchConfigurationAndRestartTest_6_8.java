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
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_8;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_7.LaunchConfigurationAndRestartTest_6_7;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class LaunchConfigurationAndRestartTest_6_8 extends LaunchConfigurationAndRestartTest_6_7 {
	
	// For the launch config test, we must set the attributes in the @Before method
	// instead of the @BeforeClass method.  This is because the attributes are overwritten
	// by the tests themselves
	@Before
	public void beforeMethod_6_8() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_8);
	}
}
