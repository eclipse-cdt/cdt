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
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.IRunControl.StepType;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.gdb.service.command.GDBControl;
import org.eclipse.dd.gdb.service.command.GDBControlDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.MIRunControl;
import org.eclipse.dd.mi.service.MIStack;
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

    private GDBControl fGDBCtrl;
	private MIRunControl fRunCtrl;
	private MIStack fStack;

	/*
	 * Boolean variables for testing events. Test thread create event only when this is set to true
	 */
	private boolean fIsTestingThreadCreateEvent = false;
	/*
	 * Boolean variables for error from events. Set to true only if there is an error in the event being tested.
	 */
	private boolean fIsEventError = false;


	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";
	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	private static final String SOURCE_NAME = "MultiThread.cc";
	
	
	/*
	 * Variable to wait for asynchronous call to complete 
	 */
    private final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    
	@Before
	public void init() throws Exception {
		fServicesTracker = 
			new DsfServicesTracker(TestsPlugin.getBundleContext(), 
                     			   getGDBLaunch().getSession().getId());
        /*
         *  Get the MIRunControl & MIStack service.
         */
		fGDBCtrl = fServicesTracker.getService(GDBControl.class);
		fRunCtrl = fServicesTracker.getService(MIRunControl.class);
		fStack = fServicesTracker.getService(MIStack.class);
		/*
		 * Add to the Listeners list 
		 */
		getGDBLaunch().getSession().addServiceEventListener(this, null);
	}

	@After
	public void tearDown() {
		fRunCtrl = null;
		fStack = null;
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
		//TestsPlugin.debugMethod("getExecutionContext()");
		/*
		 * Create a request monitor 
		 */
        final DataRequestMonitor<IExecutionDMContext[]> rm = 
        	new DataRequestMonitor<IExecutionDMContext[]>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (getStatus().isOK()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
            }
        };
        
        /*
         * Test getExecutionContexts() when only one thread exist. 
         */
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	fRunCtrl.getExecutionContexts(fGDBCtrl.getGDBDMContext(), rm);
            }
        });
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(fWait.getMessage(), fWait.isOK());

        /*
         * Get data from the Request Monitor
         */
        IRunControl.IExecutionDMContext[] ctxts = rm.getData();

        // Context can not be null
        if(ctxts == null)
       	 Assert.fail("Context returned is null. Atleast one context should have been returned");
        else{
       	 // Only one Context in this case
       	 if(ctxts.length > 1)
       	 	Assert.fail("Context returned canot be more than 1. This test case is for single context application.");
       	 IMIExecutionDMContext dmc = (IMIExecutionDMContext)ctxts[0];
       	 // Thread id for the main thread should be one
       	 Assert.assertEquals(1, dmc.getThreadId());
       } 
       fWait.waitReset();
	}
	
	
	/*
	 * Get Execution DMCs for a valid container DMC
	 * Testing for two execution DMC with id 1 & 2
	 */
	@Test
	public void getExecutionContexts() throws InterruptedException{
		//TestsPlugin.debugMethod("getExecutionContexts()");
		/*
		 * Create a request monitor 
		 */
        final DataRequestMonitor<IExecutionDMContext[]> rmExecutionCtxts = 
        	new DataRequestMonitor<IExecutionDMContext[]>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (getStatus().isOK()) {
            	   fWait.setReturnInfo(getData());
               }
               fWait.waitFinished(getStatus());
            }
        };
        /*
         * Also Testing Thread create event. Set boolean variable to true
         */
        fIsTestingThreadCreateEvent = true;
        try{
        	/*
        	 * Run till line for 2 threads to be created
        	 */
        	SyncUtil.SyncRunToLine(fGDBCtrl.getGDBDMContext(), SOURCE_NAME, "22", true);	
        }
        catch(Throwable t){
        	Assert.fail("Exception in SyncUtil.SyncRunToLine: " + t.getMessage());
        }
        /*
         * Re-set the boolean variable for testing thread create event.
         */
        fIsTestingThreadCreateEvent = false;
        /*
         * Check if error in thread create event 
         */
        if(fIsEventError){
        	Assert.fail("Thread create event has failed.");
        }
        /*
         * Test getExecutionContexts for a valid container DMC
         */
         fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
           		fRunCtrl.getExecutionContexts(fGDBCtrl.getGDBDMContext(), rmExecutionCtxts);
            }
        });
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(fWait.getMessage(), fWait.isOK());
        fWait.waitReset();
        /*
         * Get data
         */
        IRunControl.IExecutionDMContext[] data = rmExecutionCtxts.getData();
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
		//TestsPlugin.debugMethod("getModelDataForThread(");
		/*
		 * Create a request monitor
		 */
        final DataRequestMonitor<IExecutionDMData> rm = 
        	new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (getStatus().isOK()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
            }
        };
        /*
         * Call getModelData for Execution DMC
         */
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	fRunCtrl.getExecutionData(fRunCtrl.createMIExecutionContext(fGDBCtrl.getGDBDMContext(), 1), rm);
            }
        });
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(fWait.getMessage(), fWait.isOK());
        
        IRunControl.IExecutionDMData data = rm.getData();
        if(data == null)
        	Assert.fail("No data returned.");
        else{
        	/*
        	 * getModelData should return StateChangeReason.  
        	 */
	   	 	Assert.assertTrue(" State change reason for a normal execution should be CONTAINER." , 
	   	 			StateChangeReason.CONTAINER == data.getStateChangeReason());
       } 
	}

	@Test
	public void getModelDataForThreadWhenStep() throws Throwable {
		//TestsPlugin.debugMethod("getModelDataForThread()");		
		/*
		 * Run till step returns
		 */
	    final MIStoppedEvent stoppedEvent = SyncUtil.SyncStep(StepType.STEP_OVER);
		
        final DataRequestMonitor<IExecutionDMData> rm = 
        	new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (getStatus().isOK()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
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
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(fWait.getMessage(), fWait.isOK());
        
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
		//TestsPlugin.debugMethod("getModelDataForThreadWhenBreakpoint()");
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
               if (getStatus().isOK()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
            }
        };
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	fRunCtrl.getExecutionData(stoppedEvent.getDMContext(), rm);
            }
        });
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(fWait.getMessage(), fWait.isOK());
        
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
		//TestsPlugin.debugMethod("getModelDataForContainer()");
        final DataRequestMonitor<IExecutionDMData> rm = 
        	new DataRequestMonitor<IExecutionDMData>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (getStatus().isOK()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
            }
        };
        
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	fRunCtrl.getExecutionData(fGDBCtrl.getGDBDMContext(), rm);
            }
        });
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(fWait.getMessage(), fWait.isOK());
        
        IRunControl.IExecutionDMData data = rm.getData();
        if(data == null)
        	Assert.fail("No data returned.");
        else{
        	/*
        	 * StateChangeReason in getModelData for Container DMC is null. 
        	 */
	   	 	Assert.assertNull(data.getStateChangeReason());
       } 
	}
        
	/*
	 * getExecutionContexts for an invalid container DMC 
	 */
	@Ignore
	@Test
	public void getExecutionContextsForInvalidContainerDMC() throws InterruptedException{
		//TestsPlugin.debug("getExecutionContextsForInvalidContainerDMC()");
        final DataRequestMonitor<IExecutionDMContext[]> rm = 
        	new DataRequestMonitor<IExecutionDMContext[]>(fRunCtrl.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (getStatus().isOK()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
            }
        };
        final IContainerDMContext ctxt = new GDBControlDMContext("-1", getClass().getName() + ":" + 1);
        fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
            	// Pass an invalid dmc
            	fRunCtrl.getExecutionContexts(fGDBCtrl.getGDBDMContext(), rm);
            }
        });
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        Assert.assertTrue(fWait.getMessage(), !fWait.isOK());
        
        IStatus status = rm.getStatus();
   	 	Assert.assertEquals("Error message for invalid container", IStatus.ERROR, status.getSeverity());
	}

	/*
	 * Test Thread Create event for thread ID. Thread IDs should be GDB generated thread ids.
	 */
    @DsfServiceEventHandler 
    public void eventDispatched(IStartedDMEvent e) {
    	if(fIsTestingThreadCreateEvent){
    		if(((IMIExecutionDMContext)e.getExecutionContext()).getThreadId() != 2)
    			/*
    			 * Set variable if thread create event is unsuccesful 
    			 */
    			fIsEventError = true;
    	}	
	}

    /*
     * Cache after ContainerSuspendEvent should be re-set
     */
    @Test
    public void cacheAfterContainerSuspendEvent() throws InterruptedException{
		//TestsPlugin.debugMethod("cacheAfterContainerSuspendEvent()");
    	final IExecutionDMContext dmc = fRunCtrl.createMIExecutionContext(fGDBCtrl.getGDBDMContext(), 1);
    	/*
    	 * Step to fire ContainerSuspendEvent
    	 */
        try {
			SyncUtil.SyncStep(dmc, StepType.STEP_OVER);
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
		//TestsPlugin.debugMethod("resume()");
		
        final DataRequestMonitor<MIInfo> rm = 
        	new DataRequestMonitor<MIInfo>(fRunCtrl.getExecutor(), null) {
            @Override
			protected void handleCompleted() {
                fWait.waitFinished(getStatus());
                //TestsPlugin.debug("handleCompleted over");
             }
        };
        final ServiceEventWaitor<IResumedDMEvent> eventWaitor =
            new ServiceEventWaitor<IResumedDMEvent>(
                    getGDBLaunch().getSession(),
                    IResumedDMEvent.class);
        
         fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
           		fRunCtrl.resume(fGDBCtrl.getGDBDMContext(), rm);
            }
        });
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);

        try {
			eventWaitor.waitForEvent(ServiceEventWaitor.WAIT_FOREVER);
		} catch (Exception e) {
			Assert.fail("Exception raised:: " + e.getMessage());
			e.printStackTrace();
			return;
		}
        if (fWait.isOK() == false)
            Assert.assertTrue(fWait.getMessage(), false);
        Assert.assertFalse("Target is suspended. It should have been running", fRunCtrl.isSuspended(fGDBCtrl.getGDBDMContext()));
        fWait.waitReset();
    }
    
    
    

    @Test
    public void resumeContainerContext() throws InterruptedException{
		//TestsPlugin.debugMethod("resumeContainerContext()");
        final DataRequestMonitor<MIInfo> rm = 
        	new DataRequestMonitor<MIInfo>(fRunCtrl.getExecutor(), null) {
            @Override
			protected void handleCompleted() {
                fWait.waitFinished(getStatus());
             }
        };
        
        final ServiceEventWaitor<IResumedDMEvent> eventWaitor =
            new ServiceEventWaitor<IResumedDMEvent>(
                    getGDBLaunch().getSession(),
                    IResumedDMEvent.class);

         fRunCtrl.getExecutor().submit(new Runnable() {
            public void run() {
           		fRunCtrl.resume(fGDBCtrl.getGDBDMContext(), rm);
            }
        });
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        try {
			eventWaitor.waitForEvent(ServiceEventWaitor.WAIT_FOREVER);
			//TestsPlugin.debug("DsfMIRunningEvent received");	
		} catch (Exception e) {
			Assert.fail("Exception raised:: " + e.getMessage());
			e.printStackTrace();
			return;
		}

        if (fWait.isOK() == false)
            Assert.assertTrue(fWait.getMessage(), false);
        Assert.assertFalse("Target is suspended. It should have been running", fRunCtrl.isSuspended(fGDBCtrl.getGDBDMContext()));
        fWait.waitReset();
    }
    
 // PP: test no longer applies, the resume command now takes a strongly-typed execution context as an argument.
 //  
//    @Test
//    public void resumeFrameContext() throws Throwable {
//		//TestsPlugin.debugMethod("resumeFrameContext()");
//        final DataRequestMonitor<DsfMIInfo> rm = 
//        	new DataRequestMonitor<DsfMIInfo>(fRunCtrl.getExecutor(), null) {
//            @Override
//			protected void handleCompleted() {
//                fWait.waitFinished(getStatus());
//             }
//        };
//        final ServiceEventWaitor<IResumedDMEvent> eventWaitor =
//            new ServiceEventWaitor<IResumedDMEvent>(
//                    getGDBLaunch().getSession(),
//                    IResumedDMEvent.class);
//
//        IExecutionDMContext execDmc = fRunCtrl.createMIExecutionContext(fGDBCtrl.getGDBDMContext(), 1);
//        final IFrameDMContext dmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
//        fRunCtrl.getExecutor().submit(new Runnable() {
//            public void run() {
//           		fRunCtrl.resume(dmc, rm);
//            }
//        });
//        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
//        
//        try {
//			eventWaitor.waitForEvent(ServiceEventWaitor.WAIT_FOREVER);
//		} catch (Exception e) {
//			Assert.fail("Exception raised:: " + e.getMessage());
//			e.printStackTrace();
//			return;
//		}
//
//        if (fWait.isOK() == false)
//            Assert.assertTrue(fWait.getMessage(), false);
//        Assert.assertFalse("Target is suspended. It should have been running", fRunCtrl.isSuspended(fGDBCtrl.getGDBDMContext()));
//        fWait.waitReset();
//    }
    
//    @Test
//    public void resumeAndSuspend() throws InterruptedException{
//        final DataRequestMonitor<DsfMIInfo> rm = 
//        	new DataRequestMonitor<DsfMIInfo>(fRunCtrl.getExecutor(), null) {
//            @Override
//			protected void handleCompleted() {
//                if (getStatus().isOK()) {
//             	   assert true;
//             	   fWait.setReturnInfo(getData());
//                }
//                System.out.println("Wait Finished called on getTHreads rm with status " + getStatus().getMessage());
//                fWait.waitFinished(getStatus());
//             }
//        };
//        final MIExecutionDMC dmc = new MIExecutionDMC(fRunCtrl, 1);       
//         fRunCtrl.getExecutor().submit(new Runnable() {
//            public void run() {
//           		fRunCtrl.resume(dmc, rm);
//            }
//        });
//        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
//        if (fWait.isOK() == false)
//            Assert.assertTrue(fWait.getMessage(), false);
//        System.out.println("Message from isSuspended " +fRunCtrl.isSuspended(dmc));
//        Assert.assertFalse("Target is suspended. It should have been running", fRunCtrl.isSuspended(dmc));
//        fWait.waitReset();
//        
//        final DataRequestMonitor<DsfMIInfo> rmSuspend = 
//        	new DataRequestMonitor<DsfMIInfo>(fRunCtrl.getExecutor(), null) {
//            @Override
//			protected void handleCompleted() {
//                if (getStatus().isOK()) {
//             	   assert true;
//             	   fWait.setReturnInfo(getData());
//                }
//                System.out.println("Wait Finished called on getTHreads rm with status " + getStatus().getMessage());
//                fWait.waitFinished(getStatus());
//             }
//        };
//
//        final ServiceEventWaitor eventWaitor =
//            new ServiceEventWaitor(
//                    getGDBLaunch().getSession(),
//                    DsfMIStoppedEvent.class);
//
//        fRunCtrl.getExecutor().submit(new Runnable() {
//            public void run() {
//           		fRunCtrl.suspend(dmc, rmSuspend);
//            }
//        });
//        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
//		try {
//			eventWaitor.waitForEvent(ServiceEventWaitor.WAIT_FOREVER);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//
//        if (fWait.isOK() == false)
//            Assert.assertTrue(fWait.getMessage(), false);
//        System.out.println("Message from isSuspended !!!  " +fRunCtrl.isSuspended(dmc));
//        Assert.assertTrue("Target is running. It should have been suspended.", fRunCtrl.isSuspended(dmc));
//        fWait.waitReset();
//    }    
}
