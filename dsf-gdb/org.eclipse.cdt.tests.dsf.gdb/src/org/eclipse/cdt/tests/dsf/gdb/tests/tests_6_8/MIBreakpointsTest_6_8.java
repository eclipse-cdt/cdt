/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - Initial Implementation
 *     Simon Marchi (Ericsson) - Add and use runningOnWindows().
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_8;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.MIBreakpointDMContext;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_7.MIBreakpointsTest_6_7;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIBreakpointsTest_6_8 extends MIBreakpointsTest_6_7 {	
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_8);
	}
	
	// GDB 6.8 has a bug that ignores watchpoint conditions,which makes this 
	// test fail.  We therefore ignore this test for GDB 6.8 only, but run it 
	// for all other versions
	@Override
	@Ignore("This test does not work with GDB 6.8")
	@Test
	public void breakpointHit_watchpointUpdateCondition() throws Throwable {
		// Must call the test in the super class to allow further derived
		// classes to run this test.
		super.breakpointHit_watchpointUpdateCondition();
	}
	
	/**
	 * Starting with GDB 6.8, we request failed breakpoints to be pending in
	 * GDB.  So we no longer get an installation error from GDB.
	 */
	@Override
	@Test
	public void insertBreakpoint_InvalidFileName() throws Throwable {

		// Create an invalid line breakpoint
		Map<String, Object> breakpoint = new HashMap<String, Object>();
		breakpoint.put(BREAKPOINT_TYPE_TAG, BREAKPOINT_TAG);
		breakpoint.put(FILE_NAME_TAG, SOURCE_NAME + "_bad");
		breakpoint.put(LINE_NUMBER_TAG, LINE_NUMBER_1);

		// Perform the test, which we still expect to succeed
		// giving us a pending breakpoint
		IBreakpointDMContext ref = insertBreakpoint(fBreakpointsDmc, breakpoint);

		// Ensure that right BreakpointEvents were received
		waitForBreakpointEvent(1);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
				+ fBreakpointEventCount, fBreakpointEventCount == 1);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT_ADDED event(s), received "
				+ getBreakpointEventCount(BP_ADDED), getBreakpointEventCount(BP_ADDED) == 1);
		clearEventCounters();

		// Ensure that the breakpoint was correctly installed
		MIBreakpointDMData breakpoint1 = (MIBreakpointDMData) getBreakpoint(ref);
		assertTrue("BreakpointService problem: breakpoint mismatch (wrong file name)",
				breakpoint1.getFileName().equals(""));
		assertTrue("BreakpointService problem: breakpoint mismatch (wrong line number)",
				breakpoint1.getLineNumber() == -1);
		assertTrue("BreakpointService problem: breakpoint mismatch (wrong condition)",
				breakpoint1.getCondition().equals(NO_CONDITION));
		assertTrue("BreakpointService problem: breakpoint mismatch (wrong ignore count)",
				breakpoint1.getIgnoreCount() == 0);
		assertTrue("BreakpointService problem: breakpoint mismatch (wrong state)",
				breakpoint1.isEnabled());
		assertTrue("BreakpointService problem: breakpoint mismatch (not pending)",
				breakpoint1.isPending());

		// Ensure the BreakpointService holds only the right breakpoints
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertTrue("BreakpointService problem: expected " + 1 + " breakpoint(s), received "
				+ breakpoints.length, breakpoints.length == 1);
		MIBreakpointDMData breakpoint2 = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		assertTrue("BreakpointService problem: breakpoint mismatch",
				breakpoint1.equals(breakpoint2));
	}
	
	/**
	 * Starting with GDB 6.8, we request failed breakpoints to be pending in
	 * GDB.  So we no longer get an installation error from GDB.
	 */
	@Override
	@Test
	public void insertBreakpoint_InvalidFunctionName() throws Throwable {

		// Create an invalid function breakpoint
		Map<String, Object> breakpoint = new HashMap<String, Object>();
		breakpoint.put(BREAKPOINT_TYPE_TAG, BREAKPOINT_TAG);
		breakpoint.put(FILE_NAME_TAG, SOURCE_NAME);
		breakpoint.put(FUNCTION_TAG, "invalid-function-name");

		// Perform the test, which we still expect to succeed
		// giving us a pending breakpoint		
		IBreakpointDMContext ref = insertBreakpoint(fBreakpointsDmc, breakpoint);

		// Ensure that right BreakpointEvents were received
		waitForBreakpointEvent(1);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
				+ fBreakpointEventCount, fBreakpointEventCount == 1);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT_ADDED event(s), received "
				+ getBreakpointEventCount(BP_ADDED), getBreakpointEventCount(BP_ADDED) == 1);
		clearEventCounters();

		// Ensure that the breakpoint was correctly installed
		MIBreakpointDMData breakpoint1 = (MIBreakpointDMData) getBreakpoint(ref);
		assertTrue("BreakpointService problem: breakpoint mismatch (wrong file name)",
				breakpoint1.getFileName().equals(""));
		assertTrue("BreakpointService problem: breakpoint mismatch (wrong function)",
				breakpoint1.getFunctionName().equals(""));
		assertTrue("BreakpointService problem: breakpoint mismatch (wrong condition)",
				breakpoint1.getCondition().equals(NO_CONDITION));
		assertTrue("BreakpointService problem: breakpoint mismatch (wrong ignore count)",
				breakpoint1.getIgnoreCount() == 0);
		assertTrue("BreakpointService problem: breakpoint mismatch (not pending)",
				breakpoint1.isPending());

		// Ensure the BreakpointService holds only the right breakpoints
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertTrue("BreakpointService problem: expected " + 1 + " breakpoint(s), received "
				+ breakpoints.length, breakpoints.length == 1);
		MIBreakpointDMData breakpoint2 = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		assertTrue("BreakpointService problem: breakpoint mismatch",
				breakpoint1.equals(breakpoint2));
	}
	
	/**
	 * Starting with GDB 6.8, we request failed breakpoints to be pending in
	 * GDB.  So we no longer get an installation error from GDB.
	 */
	@Override
	@Test
	public void insertInvalidBreakpoint_WhileTargetRunning() throws Throwable {
		// Interrupting the target on Windows is susceptible to an additional,
		// unwanted suspension. That means that silently interrupting the target
		// to set/modify/remove a breakpoint then resuming it can leave the
		// target in a suspended state. Unfortunately, there is nothing
		// practical CDT can do to address this issue except wait for the gdb
		// folks to resolve it. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=304096#c27
	    if (runningOnWindows()) {
	    	return;
	    }
	    
		// Create an invalid line breakpoint
		Map<String, Object> breakpoint = new HashMap<String, Object>();
		breakpoint.put(BREAKPOINT_TYPE_TAG, BREAKPOINT_TAG);
		breakpoint.put(FILE_NAME_TAG, "Bad file name");
		breakpoint.put(LINE_NUMBER_TAG, LINE_NUMBER_5);

		// Run the program. It will make a two second sleep() call, during which time... 
		SyncUtil.resume();

		// ...we install the breakpoint
		MIBreakpointDMContext ref = (MIBreakpointDMContext) insertBreakpoint(fBreakpointsDmc, breakpoint);

		waitForBreakpointEvent(1);
    	// Ensure the correct BreakpointEvent was received
		MIBreakpointDMData breakpoint1 = (MIBreakpointDMData) getBreakpoint(ref);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
				+ fBreakpointEventCount, fBreakpointEventCount == 1);
		assertTrue("BreakpointEvent problem: expected " + 0 + " BREAKPOINT_HIT event(s), received "
				+ getBreakpointEventCount(BP_HIT), getBreakpointEventCount(BP_HIT) == 0);
		assertTrue("BreakpointService problem: breakpoint mismatch",
				fBreakpointRef == breakpoint1.getNumber());
		assertTrue("BreakpointService problem: breakpoint mismatch (not pending)",
				   breakpoint1.isPending());
		clearEventCounters();
	}
}
