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
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_6;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.LaunchConfigurationAndRestartTest;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class LaunchConfigurationAndRestartTest_6_6 extends LaunchConfigurationAndRestartTest {
	
	// For the launch config test, we must set the attributes in the @Before method
	// instead of the @BeforeClass method.  This is because the attributes are overwritten
	// by the tests themselves
	@Before
	public void beforeMethod_6_6() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_6);
	}
}
