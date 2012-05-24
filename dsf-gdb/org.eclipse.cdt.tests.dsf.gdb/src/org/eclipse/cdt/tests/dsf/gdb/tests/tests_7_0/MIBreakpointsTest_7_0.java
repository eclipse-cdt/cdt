/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_0;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_8.MIBreakpointsTest_6_8;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIBreakpointsTest_7_0 extends MIBreakpointsTest_6_8 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_0);		
	}
	
	// GDB 6.8 has a bug that ignores watchpoint conditions,which makes this 
	// test fail.  We therefore ignore this test for GDB 6.8 only, but run it 
	// for all other versions, so the code below re-enables the test starting
	// with GDB 7.0.
	@Override
	@Test
	public void breakpointHit_watchpointUpdateCondition() throws Throwable {
		super.breakpointHit_watchpointUpdateCondition();
	}
}
