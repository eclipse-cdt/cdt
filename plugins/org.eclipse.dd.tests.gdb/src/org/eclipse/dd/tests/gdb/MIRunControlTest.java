/*******************************************************************************
 * Copyright (c) 2007 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.dd.tests.gdb;


import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.IRunControl.StepType;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControlDMContext;
import org.eclipse.dd.gdb.internal.provisional.service.command.IGDBControl;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.IMIProcesses;
import org.eclipse.dd.mi.service.MIProcesses;
import org.eclipse.dd.mi.service.MIRunControl;
import org.eclipse.dd.mi.service.command.events.MIStoppedEvent;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.eclipse.dd.tests.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.dd.tests.gdb.framework.BaseTestCase;
import org.eclipse.dd.tests.gdb.framework.ServiceEventWaitor;
import org.eclipse.dd.tests.gdb.framework.SyncUtil;
import org.eclipse.dd.tests.gdb.launching.TestsPlugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/*
 * Tests MIRunControl class for Multi-threaded application. 
 */
public class MIRunControlTest extends BaseTestCase {

    private DsfServicesTracker fServicesTracker;    

    private IGDBControl fGDBCtrl;
	private MIRunControl fRunCtrl;
	private IMIProcesses fProcService;

	private GDBControlDMContext fGdbControlDmc;
	
	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";
	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	private static final String SOURCE_NAME = "MultiThread.cc";
	
	@Before
	public void init() throws Exception {
		fServicesTracker = 
			new DsfServicesTracker(TestsPlugin.getBundleContext(), 
                     			   getGDBLaunch().getSession().getId());
		fGDBCtrl = fServicesTracker.getService(IGDBControl.class);
		fGdbControlDmc = (GDBControlDMContext)fGDBCtrl.getContext();
		fRunCtrl = fServicesTracker.getService(MIRunControl.class);
		fProcService = fServicesTracker.getService(IMIProcesses.class);
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

	/*
	 * For Multi-threaded application - In case of one thread, Thread id should start with 1. 
	 */
	@Test
	public void getExecutionContext() throws InterruptedException{
	    final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Create a request monitor 
		 */
        final DataRequestMonitor<IExecutionDMContext[]> rm = 
        	new DataRequestMonitor<IExecutionDMContext[]>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    wait.setReturnInfo(getData());
                }
                wait.waitFinished(getStatus());
            }
        };
        
        /*
         * Test getExecutionContexts() when only one thread exist. 
         */
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	String pid = MIProcesses.UNIQUE_GROUP_ID;
            	IProcessDMContext procDmc = fProcService.createProcessContext(fGDBCtrl.getContext(), pid);
            	IContainerDMContext groupDmc = fProcService.createExecutionGroupContext(procDmc, pid);
            	fRunCtrl.getExecutionContexts(groupDmc, rm);
            }
        });
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(wait.getMessage(), wait.isOK());

        /*
         * Get data from the Request Monitor
         */
        IExecutionDMContext[] ctxts = (IExecutionDMContext[])wait.getReturnInfo();

        // Context can not be null
        if(ctxts == null)
       	 Assert.fail("Context returned is null. At least one context should have been returned");
        else {
       	 // Only one Context in this case
       	 if(ctxts.length > 1)
       	 	Assert.fail("Context returned can not be more than 1. This test case is for single context application.");
       	 
       	 IMIExecutionDMContext dmc = (IMIExecutionDMContext)ctxts[0];
       	 // Thread id for the main thread should be one
       	 Assert.assertEquals(1, dmc.getThreadId());
       } 
       wait.waitReset();
	}
	
	
	/*
	 * Get Execution DMCs for a valid container DMC
	 * Testing for two execution DMC with id 1 & 2
	 */
	@Test
	public void getExecutionContexts() throws InterruptedException{
	    final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Create a request monitor 
		 */
        final DataRequestMonitor<IExecutionDMContext[]> rmExecutionCtxts = 
        	new DataRequestMonitor<IExecutionDMContext[]>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
            	   wait.setReturnInfo(getData());
               }
               wait.waitFinished(getStatus());
            }
        };
        
        // Prepare a waiter to make sure we have received the thread started event
        final ServiceEventWaitor<IStartedDMEvent> startedEventWaitor =
            new ServiceEventWaitor<IStartedDMEvent>(
            		getGDBLaunch().getSession(),
            		IStartedDMEvent.class);
		
        try{
        	/*
        	 * Run till line for 2 threads to be created
        	 */
        	SyncUtil.SyncRunToLine(fGdbControlDmc, SOURCE_NAME, "22", true);	
        }
        catch(Throwable t){
        	Assert.fail("Exception in SyncUtil.SyncRunToLine: " + t.getMessage());
        }
        
		// Make sure thread started event was received because it could arrive
        // after the stopped event is received
        IStartedDMEvent startedEvent = null;
        try {
        	startedEvent = startedEventWaitor.waitForEvent(1000);
        } catch (Exception e) {
        	Assert.fail("Timeout waiting for Thread create event");
        	return;
        }

		if (((IMIExecutionDMContext)startedEvent.getDMContext()).getThreadId() != 2)
        	Assert.fail("Thread create event has failed expected thread id 2 but got " +
        			((IMIExecutionDMContext)startedEvent.getDMContext()).getThreadId());
        
        /*
         * Test getExecutionContexts for a valid container DMC
         */
         fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	String pid = MIProcesses.UNIQUE_GROUP_ID;
            	IProcessDMContext procDmc = fProcService.createProcessContext(fGDBCtrl.getContext(), pid);
            	IContainerDMContext groupDmc = fProcService.createExecutionGroupContext(procDmc, pid);
            	fRunCtrl.getExecutionContexts(groupDmc, rmExecutionCtxts);
            }
        });
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
        /*
         * Get data
         */
        IExecutionDMContext[] data = rmExecutionCtxts.getData();
        /*
         * Contexts returned can not be null
         */
        if(data == null)
        	Assert.fail("No context returned. 2 Contexts with id 1 & 2 should have been returned");
        else{
        	// 2 Contexts shd be returned 
        	Assert.assertTrue(data.length==2);
         	IMIExecutionDMContext dmc1 = (IMIExecutionDMContext)data[0];
          	IMIExecutionDMContext dmc2 = (IMIExecutionDMContext)data[1];
          	// Context ids should be 1 & 2 
          	Assert.assertTrue(dmc1.getThreadId()==2 && dmc2.getThreadId() == 1);
        }
     } 

	/*
	 * Testing getModelData() for ExecutionDMC
	 */
	@Test
	public void getModelDataForThread() throws InterruptedException{
	    final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Create a request monitor
		 */
        final DataRequestMonitor<IExecutionDMData> rm = 
        	new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    wait.setReturnInfo(getData());
                }
                wait.waitFinished(getStatus());
            }
        };
        /*
         * Call getModelData for Execution DMC
         */
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	String pid = MIProcesses.UNIQUE_GROUP_ID;
            	IProcessDMContext procDmc = fProcService.createProcessContext(fGDBCtrl.getContext(), pid);
            	IContainerDMContext groupDmc = fProcService.createExecutionGroupContext(procDmc, pid);
            	fRunCtrl.getExecutionData(fRunCtrl.createMIExecutionContext(groupDmc, 1), rm);
            }
        });
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(wait.getMessage(), wait.isOK());
        
        IRunControl.IExecutionDMData data = rm.getData();
        if(data == null)
        	Assert.fail("No data returned.");
        else{
        	/*
        	 * getModelData should return StateChangeReason.  
        	 */
	   	 	Assert.assertTrue(" State change reason for a normal execution should be USER_REQUEST." , 
                StateChangeReason.USER_REQUEST == data.getStateChangeReason());
       } 
	}

	@Test
	public void getModelDataForThreadWhenStep() throws Throwable {
	    final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/*
		 * Run till step returns
		 */
	    final MIStoppedEvent stoppedEvent = SyncUtil.SyncStep(StepType.STEP_OVER);
		
        final DataRequestMonitor<IExecutionDMData> rm = 
        	new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    wait.setReturnInfo(getData());
                }
                wait.waitFinished(getStatus());
            }
        };
        /*
         * getModelData for Execution DMC
         */
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	fRunCtrl.getExecutionData(stoppedEvent.getDMContext(), rm);
            }
        });
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(wait.getMessage(), wait.isOK());
        
        IRunControl.IExecutionDMData data = rm.getData();
        if(data == null)
        	Assert.fail("No data Returned.");
        else{
        	/*
        	 * getModelData for Execution DMC in case Step has been performed. 
        	 */
	   	 	Assert.assertTrue("getModelData for ExecutionDMC in case of step should be STEP." , 
	   	 					  StateChangeReason.STEP == data.getStateChangeReason());
       } 
	}
	
	/*
	 * getModelData() for ExecutionDMC when a breakpoint is hit
	 */
	@Test
	public void getModelDataForThreadWhenBreakpoint() throws Throwable {
	    final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		/* 
		 * Add a breakpoint
		 */
	    SyncUtil.SyncAddBreakpoint(SOURCE_NAME + ":21", false);
		
		/*
		 * Resume till the breakpoint is hit
		 */
		final MIStoppedEvent stoppedEvent = SyncUtil.SyncResumeUntilStopped();
		
        final DataRequestMonitor<IExecutionDMData> rm = 
        	new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    wait.setReturnInfo(getData());
                }
                wait.waitFinished(getStatus());
            }
        };
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	fRunCtrl.getExecutionData(stoppedEvent.getDMContext(), rm);
            }
        });
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(wait.getMessage(), wait.isOK());
        
        IRunControl.IExecutionDMData data = rm.getData();
        if(data == null)
        	Assert.fail("No data Returned.");
        else{
        	/*
        	 * getModelData for ExecutionDMC in case a breakpoint is hit
        	 */
        	Assert.assertTrue("getModelData for an Execution DMC when a breakpoint is hit is not BREAKPOINT and is " +  data.getStateChangeReason(), 
	   	 					   StateChangeReason.BREAKPOINT == data.getStateChangeReason());
       } 
	}
	
	/*
	 * getModelData() for Container DMC
	 */
	@Test
	public void getModelDataForContainer() throws InterruptedException{
	    final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

	    final DataRequestMonitor<IExecutionDMData> rm = 
        	new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    wait.setReturnInfo(getData());
                }
                wait.waitFinished(getStatus());
            }
        };
        
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	fRunCtrl.getExecutionData(fGdbControlDmc, rm);
            }
        });
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(wait.getMessage(), wait.isOK());
        
        IRunControl.IExecutionDMData data = rm.getData();
        if(data == null)
        	Assert.fail("No data returned.");
        else{
        	/*
        	 * StateChangeReason in getModelData for Container DMC is null. 
        	 */
            Assert.assertTrue(" State change reason for a normal execution should be USER_REQUEST." , 
                StateChangeReason.USER_REQUEST == data.getStateChangeReason());
       } 
	}
        
	/*
	 * getExecutionContexts for an invalid container DMC 
	 */
	@Ignore
	@Test
	public void getExecutionContextsForInvalidContainerDMC() throws InterruptedException{
	    final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

	    final DataRequestMonitor<IExecutionDMContext[]> rm = 
        	new DataRequestMonitor<IExecutionDMContext[]>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    wait.setReturnInfo(getData());
                }
                wait.waitFinished(getStatus());
            }
        };
//        final IContainerDMContext ctxt = new GDBControlDMContext("-1", getClass().getName() + ":" + 1);
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	// Pass an invalid dmc
            	fRunCtrl.getExecutionContexts(fGdbControlDmc, rm);
            }
        });
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(wait.getMessage(), !wait.isOK());
        
        IStatus status = rm.getStatus();
   	 	Assert.assertEquals("Error message for invalid container", IStatus.ERROR, status.getSeverity());
	}

    /*
     * Cache after ContainerSuspendEvent should be re-set
     */
    @Test
    public void cacheAfterContainerSuspendEvent() throws InterruptedException{
    	/*
    	 * Step to fire ContainerSuspendEvent
    	 */
        try {
			SyncUtil.SyncStep(StepType.STEP_OVER);
		} catch (Throwable e) {
			Assert.fail("Exception in SyncUtil.SyncStep: " + e.getMessage());
		}
		/*
		 * Cache should be re-set
		 */
		//TODO TRy going to back end and fetching values instead
		//Assert.assertEquals(fRunCtrl.getCache().getCachedContext().size(), 0);
    }

    
     //Also test Cache after ContainerResumeEvent 
    @Test
    public void resume() throws InterruptedException{
	    final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
	    
        final DataRequestMonitor<MIInfo> rm = 
        	new DataRequestMonitor<MIInfo>(fRunCtrl.getExecutor(), null) {
            @Override
			protected void handleCompleted() {
                wait.waitFinished(getStatus());
                //TestsPlugin.debug("handleCompleted over");
             }
        };
        final ServiceEventWaitor<IResumedDMEvent> eventWaitor =
            new ServiceEventWaitor<IResumedDMEvent>(
                    getGDBLaunch().getSession(),
                    IResumedDMEvent.class);
        
         fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	String pid = MIProcesses.UNIQUE_GROUP_ID;
            	IProcessDMContext procDmc = fProcService.createProcessContext(fGDBCtrl.getContext(), pid);
            	IContainerDMContext groupDmc = fProcService.createExecutionGroupContext(procDmc, pid);
            	fRunCtrl.resume(groupDmc, rm);
            }
        });
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        try {
			eventWaitor.waitForEvent(ServiceEventWaitor.WAIT_FOREVER);
		} catch (Exception e) {
			Assert.fail("Exception raised:: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		Assert.assertTrue(wait.getMessage(), wait.isOK());
		
		wait.waitReset();
		fRunCtrl.getExecutor().submit(new Runnable() {
			public void run() {
            	String pid = MIProcesses.UNIQUE_GROUP_ID;
				IProcessDMContext procDmc = fProcService.createProcessContext(fGDBCtrl.getContext(), pid);
				IContainerDMContext groupDmc = fProcService.createExecutionGroupContext(procDmc, pid);

				wait.setReturnInfo(fRunCtrl.isSuspended(groupDmc));
				wait.waitFinished();
			}
		});

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertFalse("Target is suspended. It should have been running", (Boolean)wait.getReturnInfo());

        wait.waitReset();
    }
    
    
    

    @Test
    public void resumeContainerContext() throws InterruptedException{
	    final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

	    final DataRequestMonitor<MIInfo> rm = 
        	new DataRequestMonitor<MIInfo>(fRunCtrl.getExecutor(), null) {
            @Override
			protected void handleCompleted() {
                wait.waitFinished(getStatus());
             }
        };
        
        final ServiceEventWaitor<IResumedDMEvent> eventWaitor =
            new ServiceEventWaitor<IResumedDMEvent>(
                    getGDBLaunch().getSession(),
                    IResumedDMEvent.class);

         fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
           		fRunCtrl.resume(fGdbControlDmc, rm);
            }
        });
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        try {
			eventWaitor.waitForEvent(ServiceEventWaitor.WAIT_FOREVER);
			//TestsPlugin.debug("DsfMIRunningEvent received");	
		} catch (Exception e) {
			Assert.fail("Exception raised:: " + e.getMessage());
			e.printStackTrace();
			return;
		}

		Assert.assertTrue(wait.getMessage(), wait.isOK());
		
		wait.waitReset();
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	String pid = MIProcesses.UNIQUE_GROUP_ID;
            	IProcessDMContext procDmc = fProcService.createProcessContext(fGDBCtrl.getContext(), pid);
            	IContainerDMContext groupDmc = fProcService.createExecutionGroupContext(procDmc, pid);

            	wait.setReturnInfo(fRunCtrl.isSuspended(groupDmc));
            	wait.waitFinished();
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertFalse("Target is suspended. It should have been running", (Boolean)wait.getReturnInfo());

        wait.waitReset();
    }
}
