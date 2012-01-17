/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;


import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Tests MIRunControl class for for the execWhileTargetAvailable() method. 
 */
@RunWith(BackgroundRunner.class)
public class MIRunControlTargetAvailableTest extends BaseTestCase {

	private static final String TIMEOUT_MESSAGE = "Timeout";

	private DsfServicesTracker fServicesTracker;    

    private IGDBControl fGDBCtrl;
	private IMIRunControl fRunCtrl;

	private IContainerDMContext fContainerDmc;

	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";
	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "TargetAvail.exe";
	private static final String SOURCE_NAME = "TargetAvail.cc";
	
	@Before
	public void init() throws Exception {
		final DsfSession session = getGDBLaunch().getSession();
		
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
           	fServicesTracker = 
            		new DsfServicesTracker(TestsPlugin.getBundleContext(), 
            				session.getId());
            	fGDBCtrl = fServicesTracker.getService(IGDBControl.class);
            	
            	fRunCtrl = fServicesTracker.getService(IMIRunControl.class);
            }
        };
        session.getExecutor().submit(runnable).get();

        fContainerDmc = SyncUtil.getContainerContext();
	}


	@After
	public void tearDown() {
		fServicesTracker.dispose();
	}
	
	@BeforeClass
	public static void beforeClassMethod() {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           EXEC_PATH + EXEC_NAME);
	}

    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for a single operation with a single step when the target is stopped. 
     */
    @Test
    public void executeSingleStepSingleOpWhileTargetStopped() throws Throwable {
    	// The target is currently stopped.
 
        // A single step that will set a breakpoint at PrintHello, which we will then make sure hits
    	final Step[] steps = new Step[] {
        	new Step() {
        		@Override
        		public void execute(RequestMonitor rm) {
        	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
        	    	
        	        fGDBCtrl.queueCommand(
        	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintHello", 0),
        	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
        		}}
        };
        
        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	// Now resume the target and check that we stop at the breakpoint.
    	       	
        ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
        		getGDBLaunch().getSession(),
        		ISuspendedDMEvent.class);

    	SyncUtil.resume();
    	
        // Wait up to 3 second for the target to suspend. Should happen within 2 second.
        suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));
    }
    
    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for a single operation with a single step when the target is running. 
     */
    @Test
    public void executeSingleStepSingleOpWhileTargetRunning() throws Throwable {
        // A single step that will set a breakpoint at PrintHello, which we will then make sure hits
    	final Step[] steps = new Step[] {
        	new Step() {
        		@Override
        		public void execute(RequestMonitor rm) {
        	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
        	    	
        	        fGDBCtrl.queueCommand(
        	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintHello", 0),
        	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
        		}}
        };
        
    	// The target is currently stopped so we resume it
        ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
        		getGDBLaunch().getSession(),
        		ISuspendedDMEvent.class);

    	SyncUtil.resume();
    	
        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	// Now check that the target is stopped at the breakpoint.
        // Wait up to 3 second for the target to suspend. Should happen at most in 2 seconds.
        suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));
    }
    
    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for a single operation with multiple steps when the target is stopped. 
     */
    @Test
    public void executeMultiStepSingleOpWhileTargetStopped() throws Throwable {
    	// The target is currently stopped.

        // Multiple steps that will set three temp breakpoints at three different lines
        // We then check that the target will stop three times
    	final Step[] steps = new Step[] {
        	new Step() {
        		@Override
        		public void execute(RequestMonitor rm) {
        	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
        	    	
        	        fGDBCtrl.queueCommand(
        	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintHello", 0),
        	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
        		}},
           	new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintHi", 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}},
           	new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintBonjour", 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}}
        };
        
        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	// Now resume the target three times and check that we stop three times.
    	for (int i=0; i<steps.length; i++) {
            ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
            		getGDBLaunch().getSession(),
            		ISuspendedDMEvent.class);

        	SyncUtil.resume();

    		// Wait up to 3 second for the target to suspend. Should happen within 2 seconds.
    		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));
    	}
    }
    
    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for a single operation with multiple steps when the target is stopped
     * and one of the steps fails. 
     */
    @Test
    public void executeMultiStepSingleOpWhileTargetStoppedWithError() throws Throwable {
    	// The target is currently stopped.

        // Multiple steps that will set three temp breakpoints at three different lines
        // We then check that the target will stop three times
    	final Step[] steps = new Step[] {
        	new Step() {
        		@Override
        		public void execute(RequestMonitor rm) {
        	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
        	    	
        	        fGDBCtrl.queueCommand(
        	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintHello", 0),
        	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
        		}},
           	new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, "invalid condition", 0, "PrintHi", 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}},
           	new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintBonjour", 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}}
        };
        
        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		// We want to detect the error, so this is success
    		return;
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	fail("Did not detect the error of the step");
    }
    
    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for a single operation with multiple steps when the target is running. 
     */
    @Test
    public void executeMultiStepSingleOpWhileTargetRunning() throws Throwable {
        // Multiple steps that will set three temp breakpoints at three different lines
        // We then check that the target will stop three times
    	final Step[] steps = new Step[] {
        	new Step() {
        		@Override
        		public void execute(RequestMonitor rm) {
        	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
        	    	
        	        fGDBCtrl.queueCommand(
        	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintHello", 0),
        	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
        		}},
           	new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintHi", 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}},
           	new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintBonjour", 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}}
        };
        
    	// The target is currently stopped so we resume it
        ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
        		getGDBLaunch().getSession(),
        		ISuspendedDMEvent.class);

    	SyncUtil.resume();

        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	// Now resume the target three times and check that we stop three times.
    	for (int i=0; i<steps.length; i++) {
    		// Wait up to 3 second for the target to suspend. Should happen within two seconds.
    		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));

    		suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
            		getGDBLaunch().getSession(),
            		ISuspendedDMEvent.class);

        	SyncUtil.resume();

    	}
    }
    
    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for a single operation with multiple steps when the target is running
     * and one of the steps fails. 
     */
    @Test
    public void executeMultiStepSingleOpWhileTargetRunningWithError() throws Throwable {
        // Multiple steps that will set three temp breakpoints at three different lines
        // We then check that the target will stop three times
    	final Step[] steps = new Step[] {
        	new Step() {
        		@Override
        		public void execute(RequestMonitor rm) {
        	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
        	    	
        	        fGDBCtrl.queueCommand(
        	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintHello", 0),
        	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
        		}},
           	new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, "invalid condition", 0, "PrintHi", 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}},
           	new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, "PrintBonjour", 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}}
        };
        
    	// The target is currently stopped so we resume it
        ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
        		getGDBLaunch().getSession(),
        		ISuspendedDMEvent.class);

    	SyncUtil.resume();

        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
        
        boolean caughtError = false;
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		caughtError = true;
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	Assert.assertTrue("Did not catch the error of the step", caughtError);
    	
    	// Now make sure the target stop of the first breakpoint
  		// Wait up to 3 second for the target to suspend. Should happen within two seconds.
   		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));
    }

    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for concurrent operations with a single step when the target is stopped. 
     */
    @Test
    public void executeSingleStepConcurrentOpWhileTargetStopped() throws Throwable {
    	// The target is currently stopped.
 
    	final int NUM_CONCURRENT = 3;
    	
    	String[] locations = { "PrintHello", "PrintHi", "PrintBonjour" };
    	final Step[][] steps = new Step[NUM_CONCURRENT][1]; // one step for each concurrent operation
    	for (int i=0; i<steps.length; i++) {
    		final String location = locations[i];
           	steps[i] = new Step[] {
           			new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location, 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}}
          };
    	}
            
        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				CountingRequestMonitor crm = new CountingRequestMonitor(fGDBCtrl.getExecutor(), null) {
					@Override
					protected void handleCompleted() {
						rm.done();
					};
				};
				

				int index;
				for (index=0; index<steps.length; index++) {
					fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps[index], crm);
				}
				
				crm.setDoneCount(index);
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	for (int i=0; i<steps.length; i++) {
    		// Now resume the target and check that we stop at all the breakpoints.
    		ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
    				getGDBLaunch().getSession(),
    				ISuspendedDMEvent.class);

    		SyncUtil.resume();

    		// Wait up to 3 second for the target to suspend. Should happen within 2 second.
    		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));
    	}
    }
    
    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for concurrent operations with a single step when the target is running. 
     */
    @Test
    public void executeSingleStepConcurrentOpWhileTargetRunning() throws Throwable { 
    	final int NUM_CONCURRENT = 3;
    	
    	String[] locations = { "PrintHello", "PrintHi", "PrintBonjour" };
    	final Step[][] steps = new Step[NUM_CONCURRENT][1]; // one step for each concurrent operation
    	for (int i=0; i<steps.length; i++) {
    		final String location = locations[i];
           	steps[i] = new Step[] {
           			new Step() {
           		@Override
           		public void execute(RequestMonitor rm) {
           	        IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);
           	    	
           	        fGDBCtrl.queueCommand(
           	        		fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location, 0),
           	        		new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm));
           		}}
          };
    	}
            
    	// The target is currently stopped so we resume it
        ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
        		getGDBLaunch().getSession(),
        		ISuspendedDMEvent.class);

    	SyncUtil.resume();

        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				CountingRequestMonitor crm = new CountingRequestMonitor(fGDBCtrl.getExecutor(), null) {
					@Override
					protected void handleCompleted() {
						rm.done();
					};
				};
				

				int index;
				for (index=0; index<steps.length; index++) {
					fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps[index], crm);
				}
				
				crm.setDoneCount(index);
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	for (int i=0; i<steps.length; i++) {
    		// Wait up to 3 second for the target to suspend. Should happen within 2 seconds.
    		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));

    		// Now resume the target and check that we stop at all the breakpoints.
    		suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
    				getGDBLaunch().getSession(),
    				ISuspendedDMEvent.class);

    		SyncUtil.resume();
    	}
    }
    
    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for concurrent operations with a single step when the target is stopped. 
     * This tests verifies that we properly handle concurrent operations that are sent
     * while other operations are already being run.
     */
    @Test
    public void executeSingleStepConcurrentButDelayedOpWhileTargetStopped() throws Throwable {
    	// The target is currently stopped.

    	final String location = "PrintHello";
    	final String location2 = "PrintHi";
    	final Step[] steps = new  Step[] {
    			new Step() {
    				@Override
    				public void execute(final RequestMonitor rm) {
    					final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);

    					fGDBCtrl.queueCommand(
    							fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location, 0),
    							new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm) {
    								@Override
    								protected void handleSuccess() {
    									// Now that time has elapsed, send another command
    									fRunCtrl.executeWithTargetAvailable(fContainerDmc, new Step[] {
    											new Step() {
    												@Override
    												public void execute(final RequestMonitor rm) {
    													fGDBCtrl.queueCommand(
    															fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location2, 0),
    															new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), null));
    												}}}, new RequestMonitor(fGDBCtrl.getExecutor(), null));

    									// Complete the first operation because the two are supposed to be independent
    									rm.done();
    								}});
    				}}
          };
            
        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	for (int i=0; i<2; i++) {
    	   	// The target is currently stopped so we resume it
            ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
            		getGDBLaunch().getSession(),
            		ISuspendedDMEvent.class);

        	SyncUtil.resume();

    		// Wait up to 3 second for the target to suspend. Should happen within 2 seconds.
    		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));
    	}
    }
    
    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for concurrent operations with a single step when the target is running. 
     * This tests verifies that we properly handle concurrent operations that are sent
     * while other operations are already being run.
     */
    @Test
    public void executeSingleStepConcurrentButDelayedOpWhileTargetRunning() throws Throwable {    	
    	final String location = "PrintHello";
    	final String location2 = "PrintHi";
    	final Step[] steps = new  Step[] {
    			new Step() {
    				@Override
    				public void execute(final RequestMonitor rm) {
    					final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);

    					fGDBCtrl.queueCommand(
    							fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location, 0),
    							new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm) {
    								@Override
    								protected void handleSuccess() {
    									// Now that time has elapsed, send another command
    									fRunCtrl.executeWithTargetAvailable(fContainerDmc, new Step[] {
    											new Step() {
    												@Override
    												public void execute(final RequestMonitor otherRm) {
    													fGDBCtrl.queueCommand(
    															fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location2, 0),
    															new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), otherRm));
    												}}}, new RequestMonitor(fGDBCtrl.getExecutor(), null));

    									// Complete the first operation because the two are supposed to be independent
    									rm.done();
    								}});
    				}}
          };
            
    	// The target is currently stopped so we resume it
        ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
        		getGDBLaunch().getSession(),
        		ISuspendedDMEvent.class);

    	SyncUtil.resume();

        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	for (int i=0; i<2; i++) {
    		// Wait up to 3 second for the target to suspend. Should happen within 2 seconds.
    		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));

    		// Now resume the target and check that we stop at all the breakpoints.
    		suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
    				getGDBLaunch().getSession(),
    				ISuspendedDMEvent.class);

    		SyncUtil.resume();
    	}
    }

    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for concurrent operations with a single step when the target is stopped. 
     * This tests verifies that we properly handle concurrent operations that are
     * dependent on each other; this means that the second operation needs to complete
     * for the second one to complete.
     */
    @Test
    public void executeSingleStepConcurrentAndDependentOpWhileTargetStopped() throws Throwable {
    	// The target is currently stopped.

    	final String location = "PrintHello";
    	final String location2 = "PrintHi";
    	final Step[] steps = new  Step[] {
    			new Step() {
    				@Override
    				public void execute(final RequestMonitor rm) {
    					final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);

    					fGDBCtrl.queueCommand(
    							fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location, 0),
    							new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm) {
    								@Override
    								protected void handleSuccess() {
    									// Send another such operation and wait for it to complete to mark the original one as completed
    									fRunCtrl.executeWithTargetAvailable(fContainerDmc, new Step[] {
    											new Step() {
    												@Override
    												public void execute(final RequestMonitor otherRm) {
    													fGDBCtrl.queueCommand(
    															fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location2, 0),
    															new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), otherRm));
    												}}}, rm);
    								}});
    				}}
          };
            
        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	for (int i=0; i<2; i++) {
    	   	// The target is currently stopped so we resume it
            ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
            		getGDBLaunch().getSession(),
            		ISuspendedDMEvent.class);

        	SyncUtil.resume();

    		// Wait up to 3 second for the target to suspend. Should happen within 2 seconds.
    		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));
    	}
    }
    
    /**
     * Test that the executeWhileTargetAvailale interface works properly
     * for concurrent operations with a single step when the target is running. 
     * This tests verifies that we properly handle concurrent operations that are
     * dependent on each other; this means that the second operation needs to complete
     * for the second one to complete.
     */
    @Test
    public void executeSingleStepConcurrentAndDependentOpWhileTargetRunning() throws Throwable {    	
    	final String location = "PrintHello";
    	final String location2 = "PrintHi";
    	final Step[] steps = new  Step[] {
    			new Step() {
    				@Override
    				public void execute(final RequestMonitor rm) {
    					final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(fContainerDmc, IBreakpointsTargetDMContext.class);

    					fGDBCtrl.queueCommand(
    							fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location, 0),
    							new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), rm) {
    								@Override
    								protected void handleSuccess() {
    									// Send another such operation and wait for it to complete to mark the original one as completed
    									fRunCtrl.executeWithTargetAvailable(fContainerDmc, new Step[] {
    											new Step() {
    												@Override
    												public void execute(final RequestMonitor otherRm) {
    													fGDBCtrl.queueCommand(
    															fGDBCtrl.getCommandFactory().createMIBreakInsert(bpTargetDmc, true, false, null, 0, location2, 0),
    															new DataRequestMonitor<MIBreakInsertInfo> (fGDBCtrl.getExecutor(), otherRm));
    												}}}, rm);
    								}});
    				}}
          };
            
    	// The target is currently stopped so we resume it
        ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
        		getGDBLaunch().getSession(),
        		ISuspendedDMEvent.class);

    	SyncUtil.resume();

        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				fRunCtrl.executeWithTargetAvailable(fContainerDmc, steps, rm);				
			}
        };
    	try {
    		fRunCtrl.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(TIMEOUT_MESSAGE);
    	}
    	
    	for (int i=0; i<2; i++) {
    		// Wait up to 3 second for the target to suspend. Should happen within 2 seconds.
    		suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(3000));

    		// Now resume the target and check that we stop at all the breakpoints.
    		suspendedEventWaitor = new ServiceEventWaitor<ISuspendedDMEvent>(
    				getGDBLaunch().getSession(),
    				ISuspendedDMEvent.class);

    		SyncUtil.resume();
    	}
    }
}
