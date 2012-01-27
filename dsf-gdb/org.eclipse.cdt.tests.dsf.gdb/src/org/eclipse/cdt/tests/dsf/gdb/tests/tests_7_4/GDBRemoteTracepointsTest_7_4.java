/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4;


import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3.GDBRemoteTracepointsTest_7_3;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBRemoteTracepointsTest_7_4 extends GDBRemoteTracepointsTest_7_3 {
	@BeforeClass
	public static void beforeClassMethod_7_4() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_4);		
	}
	
	@Override
	protected boolean acceptsFastTpOnFourBytes() {
		// With GDB 7.4, fast tracepoints only need an
		// instruction of 4 bytes or more, instead of 5.
		return true;
	}
}
