/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *     Simon Marchi (Ericsson) - Check for thread name support.
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_2.GDBProcessesTest_7_2;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBProcessesTest_7_3 extends GDBProcessesTest_7_2 {   
    @Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_3);		
	}

	@Override
	protected boolean threadNamesSupported() {
		// Thread names are reported starting with gdb 7.3, except on Windows
		// and not for remote sessions.
		return !runningOnWindows() && !isRemoteSession();
	}
}