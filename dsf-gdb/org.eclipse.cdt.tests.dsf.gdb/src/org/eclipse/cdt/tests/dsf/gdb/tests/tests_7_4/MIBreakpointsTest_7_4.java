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

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3.MIBreakpointsTest_7_3;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIBreakpointsTest_7_4 extends MIBreakpointsTest_7_3 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_4);		
	}
	
	/*
	 * Starting with GDB 7.4, breakpoints at invalid lines succeed and become
	 * pending breakpoints.  This is because the invalid line for one file,
	 * may be valid for another file with the same name.
	 * One could argue that line 0 is an exception, but GDB does not make
	 * a difference.
	 * @see org.eclipse.cdt.tests.dsf.gdb.tests.MIBreakpointsTest#insertBreakpoint_InvalidLineNumber()
	 */
	@Override
	@Test
	public void insertBreakpoint_InvalidLineNumber() throws Throwable {

		// Create a line breakpoint
		Map<String, Object> breakpoint = new HashMap<String, Object>();
		breakpoint.put(BREAKPOINT_TYPE_TAG, BREAKPOINT_TAG);
		breakpoint.put(FILE_NAME_TAG, SOURCE_NAME);
		breakpoint.put(LINE_NUMBER_TAG, 0);

		// Perform the test
		IBreakpointDMContext ref = insertBreakpoint(fBreakpointsDmc, breakpoint);

		// Ensure that no BreakpointEvent was received
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
				+ fBreakpointEventCount, fBreakpointEventCount == 1);
		
		MIBreakpointDMData bpData = (MIBreakpointDMData) getBreakpoint(ref);
		assertTrue("Breakpoint should be pending", bpData.isPending());
		assertTrue("Breakpoint mismatch should be enabled", bpData.isEnabled());
	}
	
	// Re-enabled this test since it needs breakpoint synchronization
	// with the gdb console, which is available starting with GDB 7.4
	// We still leave the test in the base class MIBreakpointsTest because
	// the test could be written differently and made to work for older
	// gdb versions
	@Override
	@Test
	public void updateBreakpoint_AfterRestart() throws Throwable {
		super.updateBreakpoint_AfterRestart();
	}

}
