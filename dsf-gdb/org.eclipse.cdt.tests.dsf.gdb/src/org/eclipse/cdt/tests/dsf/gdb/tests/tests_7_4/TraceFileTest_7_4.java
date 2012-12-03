/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4;

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.CollectAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.EvaluateAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.junit.Assert;
import org.junit.Test;

public class TraceFileTest_7_4 extends BaseTestCase {

	private final static String FILE_NAME = "TracepointTestApp.cc";
	private final static int LINE_NUMBER_1 = 97;
	private final static int LINE_NUMBER_2 = 102;
	private final static String TEVAL_STRING = "lBoolPtr2";
	private final static String COLLECT_STRING1 = "counter";
	private final static String COLLECT_STRING2 = "$regs";
	
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_4);		
	}

    @Override
	public void doBeforeTest() throws Exception {
		setLaunchAttributes();
		// Can't run the launch right away because each test needs to first set some 
		// parameters.  The individual tests will be responsible for starting the launch. 
	}
	
	@Override
 	protected void setLaunchAttributes() {
    	super.setLaunchAttributes();
    	
		// Set a working directory for GDB that is different than eclipse's directory.
		// This allows us to make sure we properly handle finding the core file,
		// especially in the case of a relative path
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "${workspace_loc}");
		// Because we just set a different working directory, we must use an absolute path for the program
    	String absoluteProgram = new Path("data/launch/bin/TracepointTestApp.exe").toFile().getAbsolutePath();
        setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, absoluteProgram);
        
        // Set post-mortem launch
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				           ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
		// Set post-mortem type to core file
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_POST_MORTEM_TYPE,
		                   IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE);
		// Set default core file path
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, "data/launch/bin/trace");
    }

    /**
     * The session the trace file is generated from sets two tracepoints at 
     * data/launch/src/TracepointTestApp.cc:97 and data/launch/src/TracepointTestApp.cc:102.
     * The first tracepoint's command is "teval lBoolPtr2".
     * The second tracepoint's commands are "collect counter" and "collect $regs".
     * The purpose of this test is verify that corresponding platform tracepoints with 
     * the proper actions are created and associated with the target tracepoints.
     */
    @Test
    public void testTracepoints() throws Throwable {
    	TracepointActionManager tam = TracepointActionManager.getInstance();
    	IBreakpointManager bm = DebugPlugin.getDefault().getBreakpointManager();
    	
    	// Verify there is no platform tracepoints or tracepoint actions at 
    	// the beginning of the test.
    	ArrayList<ITracepointAction> actions = tam.getActions();
    	Assert.assertTrue(actions.size() == 0);
    	IBreakpoint[] bpts = bm.getBreakpoints();
    	Assert.assertTrue(bpts.length == 0);
    	
    	// First session. 
    	doLaunch();
    	
    	// Verify that two new platform tracepoints and three new tracepoint 
    	// actions are created.
    	actions = tam.getActions();
    	Assert.assertTrue(actions.size() == 3);
    	bpts = bm.getBreakpoints();
    	Assert.assertTrue(bpts.length == 2);
    	for (IBreakpoint b : bpts) {
    		Assert.assertTrue(b instanceof ICTracepoint);
    		checkTracepoint((ICTracepoint)b);
    	}
    	
    	// Terminate the first session and start a new session.
    	terminateSession();
    	doLaunch();

    	// Verify that no new platform tracepoints or new tracepoint actions are created.
    	actions = tam.getActions();
    	Assert.assertTrue(actions.size() == 3);
    	bpts = bm.getBreakpoints();
    	Assert.assertTrue(bpts.length == 2);
    	for (IBreakpoint b : bpts) {
    		Assert.assertTrue(b instanceof ICTracepoint);
    		checkTracepoint((ICTracepoint)b);
    	}
    }

    private void checkTracepoint(ICTracepoint tracepoint) throws Throwable {
    	TracepointActionManager tam = TracepointActionManager.getInstance();
		Assert.assertTrue(FILE_NAME.equals(new Path(tracepoint.getFileName()).lastSegment()));
		Assert.assertTrue(LINE_NUMBER_1 == tracepoint.getLineNumber() || LINE_NUMBER_2 == tracepoint.getLineNumber());
		String[] actionNames = 
			((String)tracepoint.getMarker().getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE)).split(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
		for (String name : actionNames) { 
			ITracepointAction a = tam.findAction(name);
			Assert.assertNotNull(a);
			if (a instanceof EvaluateAction) {
				Assert.assertTrue(TEVAL_STRING.equals(((EvaluateAction)a).getEvalString()));
			}
			if (a instanceof CollectAction) {
				Assert.assertTrue(COLLECT_STRING1.equals(((CollectAction)a).getCollectString()) 
							|| COLLECT_STRING2.equals(((CollectAction)a).getCollectString()));
			}
		}
    }

    private void terminateSession() throws Throwable {
    	getGDBLaunch().terminate();
    }
}
