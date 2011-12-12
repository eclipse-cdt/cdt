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
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_0;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_8.LaunchConfigurationAndRestartTest_6_8;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class LaunchConfigurationAndRestartTest_7_0 extends LaunchConfigurationAndRestartTest_6_8 {
	// For the launch config test, we must set the attributes in the @Before method
	// instead of the @BeforeClass method.  This is because the attributes are overwritten
	// by the tests themselves
	@Before
	public void beforeMethod_7_0() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_0);
	}
	
	/**
	 * This test will confirm that we have turned on "pending breakpoints"
	 * The pending breakpoint setting only affects CLI commands so we have
	 * to test with one.  We don't have classes to set breakpoints using CLI,
	 * but we do for tracepoints, which is the same for this test.
	 * 
	 * The pending breakpoint feature only works with tracepoints starting
	 * with GDB 7.0.
	 * 
	 * We could run this test before 7.0 but we would have to use a breakpoint
	 * set using CLI commands.
	 */
    @Test
    public void testPendingBreakpointSetting() throws Throwable {
        performLaunch();
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();

    	final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(stoppedEvent.getDMContext(),
    																				 IBreakpointsTargetDMContext.class);
    	Query<MIBreakListInfo> query = new Query<MIBreakListInfo>() {
    		@Override
    		protected void execute(final DataRequestMonitor<MIBreakListInfo> rm) {
    			fGdbControl.queueCommand(
    					fGdbControl.getCommandFactory().createCLITrace(bpTargetDmc, "invalid", ""),
    					new ImmediateDataRequestMonitor<CLITraceInfo>(rm) {
    						@Override
    						protected void handleSuccess() {
    							fGdbControl.queueCommand(
    									fGdbControl.getCommandFactory().createMIBreakList(bpTargetDmc), 
    									new ImmediateDataRequestMonitor<MIBreakListInfo>(rm) {
    			    						@Override
    			    						protected void handleSuccess() {
    			    							rm.setData(getData());
    			    							rm.done();
    			    						}
    									});
    						}
    					});
    		}
    	};
    	try {
    		fExpService.getExecutor().execute(query);
    		MIBreakListInfo value = query.get(500, TimeUnit.MILLISECONDS);
    		MIBreakpoint[] bps = value.getMIBreakpoints();
    		assertTrue("Expected 1 breakpoint but got " + bps.length,
    				   bps.length == 1);
    		assertTrue("Expending a <PENDING> breakpoint but got one at " + bps[0].getAddress(),
    				   bps[0].getAddress().equals("<PENDING>"));
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    }
    
    /**
     * This test will tell the launch to "stop on main" at method main() with reverse
     * debugging enabled.  We will verify that the launch stops at main() and that
     * reverse debugging is enabled.
     */
    @Test
    public void testStopAtMainWithReverse() throws Throwable {
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
    	setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE, true);
    	performLaunch();

    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
    	// Make sure we stopped at the first line of main
    	assertTrue("Expected to stop at main:" + FIRST_LINE_IN_MAIN + " but got " +
    			   stoppedEvent.getFrame().getFunction() + ":" +
    			   Integer.toString(stoppedEvent.getFrame().getLine()),
    			   stoppedEvent.getFrame().getFunction().equals("main") &&
    			   stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN);
    	
    	// Step a couple of times and check where we are
    	final int NUM_STEPS = 3;
    	stoppedEvent = SyncUtil.step(NUM_STEPS,  StepType.STEP_OVER);
    	assertTrue("Expected to stop at main:" + (FIRST_LINE_IN_MAIN+NUM_STEPS) + " but got " +
 			   stoppedEvent.getFrame().getFunction() + ":" +
 			   Integer.toString(stoppedEvent.getFrame().getLine()),
 			   stoppedEvent.getFrame().getFunction().equals("main") &&
 			   stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN+NUM_STEPS);
    	
    	// Now step backwards to make sure reverse was enabled
    	
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
			new ServiceEventWaitor<MIStoppedEvent>(
					fSession,
					MIStoppedEvent.class);

    	final int REVERSE_NUM_STEPS = 2;
    	final IExecutionDMContext execDmc = stoppedEvent.getDMContext();
    	Query<MIInfo> query = new Query<MIInfo>() {
    		@Override
    		protected void execute(DataRequestMonitor<MIInfo> rm) {
    			fGdbControl.queueCommand(
    					fGdbControl.getCommandFactory().createMIExecReverseNext(execDmc, REVERSE_NUM_STEPS),
    					rm);
    		}
    	};
    	try {
    		fGdbControl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    	
    	stoppedEvent = eventWaitor.waitForEvent(1000);
    	
    	assertTrue("Expected to stop at main:" + (FIRST_LINE_IN_MAIN+NUM_STEPS-REVERSE_NUM_STEPS) + " but got " +
  			   stoppedEvent.getFrame().getFunction() + ":" +
  			   Integer.toString(stoppedEvent.getFrame().getLine()),
  			   stoppedEvent.getFrame().getFunction().equals("main") &&
  			   stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN+NUM_STEPS-REVERSE_NUM_STEPS);
    }
    
    /**
     * Repeat the test testStopAtMainWithReverse, but after a restart.
     */
    @Test
    public void testStopAtMainWithReverseRestart() throws Throwable {
    	fRestart = true;
    	testStopAtMainWithReverse();
    }

    /**
     * This test will tell the launch to "stop on main" at method stopAtOther(), 
     * with reverse debugging enabled.  We will then verify that the launch is properly
     * stopped at stopAtOther() and that it can go backwards until main() (this will
     * confirm that reverse debugging was enabled at the very start).
     */
	@Test
    public void testStopAtOtherWithReverse() throws Throwable {
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "stopAtOther");
    	setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE, true);
    	performLaunch();

    	// Wait for the launch to properly complete.  This is because with reverse
    	// the first stopped event does not mean the launch is complete.  There will
    	// be another stopped event
    	synchronized (this) {
    		wait(1000);			
		}
    	
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
    	
    	// The initial stopped event is not the last stopped event.
    	// With reverse we have to stop the program, turn on reverse and start it again.
    	// Let's get the frame where we really are stopped right now.
    	final IExecutionDMContext execDmc = stoppedEvent.getDMContext();
    	IFrameDMData frame = SyncUtil.getFrameData(execDmc, 0);
 
    	// Make sure we stopped at the first line of main
    	assertTrue("Expected to stop at stopAtOther but got " +
    			   frame.getFunction(),
    			   frame.getFunction().equals("stopAtOther"));
    	
    	// Now step backwards all the way to the start to make sure reverse was enabled from the very start   	
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
			new ServiceEventWaitor<MIStoppedEvent>(
					fSession,
					MIStoppedEvent.class);

    	final int REVERSE_NUM_STEPS = 3;
    	Query<MIInfo> query2 = new Query<MIInfo>() {
    		@Override
    		protected void execute(DataRequestMonitor<MIInfo> rm) {
    			fGdbControl.queueCommand(
    					fGdbControl.getCommandFactory().createMIExecReverseNext(execDmc, REVERSE_NUM_STEPS),
    					rm);
    		}
    	};
    	try {
    		fGdbControl.getExecutor().execute(query2);
    		query2.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    	
    	stoppedEvent = eventWaitor.waitForEvent(1000);
    	
    	assertTrue("Expected to stop at main:" + (FIRST_LINE_IN_MAIN) + " but got " +
  			   stoppedEvent.getFrame().getFunction() + ":" +
  			   Integer.toString(stoppedEvent.getFrame().getLine()),
  			   stoppedEvent.getFrame().getFunction().equals("main") &&
  			   stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN);
    }
    
    /**
     * Repeat the test testStopAtOtherWithReverse, but after a restart.
     */
    @Test
    public void testStopAtOtherWithReverseRestart() throws Throwable {
    	fRestart = true;
    	testStopAtOtherWithReverse();
    }
    /**
     * This test will set a breakpoint at the last line of the program and will tell 
     * the launch to NOT "stop on main", with reverse debugging enabled.  We will 
     * verify that the first stop is at the last line of the program but that the program
     * can run backwards until main() (this will confirm that reverse debugging was 
     * enabled at the very start).
     */
    @Ignore
	@Test
    public void testNoStopAtMainWithReverse() throws Throwable {
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
    	// Set this one as well to make sure it gets ignored
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
    	setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE, true);
    	
    	// MUST SET BREAKPOINT AT LAST LINE BUT BEFORE LAUNCH IS STARTED
    	// MUST SET BREAKPOINT AT LAST LINE BUT BEFORE LAUNCH IS STARTED
    	// MUST SET BREAKPOINT AT LAST LINE BUT BEFORE LAUNCH IS STARTED
    	// see testNoStopAtMain()
    	
    	performLaunch();

    	// Wait for the launch to properly complete.  This is because with reverse
    	// the first stopped event does not mean the launch is complete.  There will
    	// be another stopped event
    	synchronized (this) {
    		wait(1000);			
		}
    	
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
    	
    	// The initial stopped event is not the last stopped event.
    	// With reverse we have to stop the program, turn on reverse and start it again.
    	// Let's get the frame where we really are stopped right now.
    	final IExecutionDMContext execDmc = stoppedEvent.getDMContext();
    	IFrameDMData frame = SyncUtil.getFrameData(execDmc, 0);
 
    	// Make sure we stopped at the first line of main
    	assertTrue("Expected to stop at main:" + LAST_LINE_IN_MAIN + " but got " +
    			   frame.getFunction() + ":" +
    			   Integer.toString(frame.getLine()),
    			   frame.getFunction().equals("main") &&
    			   frame.getLine() == LAST_LINE_IN_MAIN);
    	
    	// Now step backwards all the way to the start to make sure reverse was enabled from the very start   	
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
			new ServiceEventWaitor<MIStoppedEvent>(
					fSession,
					MIStoppedEvent.class);

    	final int REVERSE_NUM_STEPS = 3;
    	Query<MIInfo> query2 = new Query<MIInfo>() {
    		@Override
    		protected void execute(DataRequestMonitor<MIInfo> rm) {
    			fGdbControl.queueCommand(
    					fGdbControl.getCommandFactory().createMIExecReverseNext(execDmc, REVERSE_NUM_STEPS),
    					rm);
    		}
    	};
    	try {
    		fGdbControl.getExecutor().execute(query2);
    		query2.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    	
    	stoppedEvent = eventWaitor.waitForEvent(1000);
    	
    	assertTrue("Expected to stop at main:" + (FIRST_LINE_IN_MAIN) + " but got " +
  			   stoppedEvent.getFrame().getFunction() + ":" +
  			   Integer.toString(stoppedEvent.getFrame().getLine()),
  			   stoppedEvent.getFrame().getFunction().equals("main") &&
  			   stoppedEvent.getFrame().getLine() == FIRST_LINE_IN_MAIN);
    }
    
    /**
     * Repeat the test testNoStopAtMainWithReverse, but after a restart.
     */
    @Ignore
    @Test
    public void testNoStopAtMainWithReverseRestart() throws Throwable {
    	fRestart = true;
    	testNoStopAtMainWithReverse();
    }
}
