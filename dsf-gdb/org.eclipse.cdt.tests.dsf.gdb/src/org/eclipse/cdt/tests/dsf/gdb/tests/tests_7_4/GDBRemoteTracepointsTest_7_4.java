/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.CollectAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3.GDBRemoteTracepointsTest_7_3;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBRemoteTracepointsTest_7_4 extends GDBRemoteTracepointsTest_7_3 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_4);		
	}
	
	@Override
	protected boolean acceptsFastTpOnFourBytes() {
		// With GDB 7.4, fast tracepoints only need an
		// instruction of 4 bytes or more when on a 32bit architecture, instead of 5.
	    if (Platform.getOS().equals(Platform.ARCH_X86)) {
	    	return true;
	    }
	    return false;
	}
	
	/**
	 * This test sets the different types of tracepoints and then sets some string collection actions
	 */
	@Test
	public void tracepointActionsWithCollectStrings() throws Throwable {
		TracepointActionManager tracepointActionMgr = TracepointActionManager.getInstance();

		CollectAction action1 = new CollectAction();
		action1.setCollectString("/s $locals");
		action1.setName("Collect string locals");
		tracepointActionMgr.addAction(action1);
		
		CollectAction action2 = new CollectAction();
		action2.setCollectString("/s3 $locals, $reg");
		action2.setName("Collect string locals, reg");
		tracepointActionMgr.addAction(action2);
		
		createTracepoints();

		Map<String, Object> delta = new HashMap<String, Object>();
		// Set conditions for all tracepoints
		delta.put(MIBreakpoints.COMMANDS, action1.getName());
		updateBreakpoint(fTracepoints[0], delta);
		delta.put(MIBreakpoints.COMMANDS, action2.getName());
		updateBreakpoint(fTracepoints[1], delta);
		delta.put(MIBreakpoints.COMMANDS, action1.getName());
		updateBreakpoint(fTracepoints[2], delta);
		delta.put(MIBreakpoints.COMMANDS, action1.getName());
		updateBreakpoint(fTracepoints[3], delta);
		delta.put(MIBreakpoints.COMMANDS, action2.getName());
		updateBreakpoint(fTracepoints[4], delta);

		TracepointData[] dataArray = new TracepointData[] {
			new TracepointData(LINE_NUMBER_1_BYTE_INSTR, NO_CONDITION, 0, true, action1.toString(), false),
			new TracepointData(LINE_NUMBER_2_BYTE_INSTR, NO_CONDITION, 0, true, action2.toString(), false),
			new TracepointData(LINE_NUMBER_3_BYTE_INSTR, NO_CONDITION, 0, true, action1.toString(), false),
			new TracepointData(LINE_NUMBER_4_BYTE_INSTR, NO_CONDITION, 0, true, action1.toString(), acceptsFastTpOnFourBytes()),
			new TracepointData(LINE_NUMBER_5_BYTE_INSTR, NO_CONDITION, 0, true, action2.toString(), true),
		};

		checkTracepoints(dataArray);
	}
}
