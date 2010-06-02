/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_0;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMData;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsAddedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsRemovedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsUpdatedEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBRemoteTracepointsTest_7_0 extends BaseTestCase {
	@BeforeClass
	public static void beforeClassMethod_7_0() {
		setGdbProgramNamesLaunchAttributes("7.0");
		
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "data/launch/bin/TracepointTestApp.exe");
		
		// GDB tracepoints are only supported on a remote target (e.g., using gdbserver)
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
                           IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);
	}
	
	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	private IBreakpoints fBreakpointService;
//	private ITraceControl fTraceService;
	private IBreakpointsTargetDMContext fBreakpointsDmc;
//	private ITraceTargetDMContext fTraceTargetDmc;

//	private int fTotalTracingBufferSize = 0;
	
	private final static int[] PASS_COUNTS = {12, 2, 32, 6, 128, 0, 0, 0, 0, 0, 0, 0};

//	private static CollectAction COLLECT_ACTION_1;
//	private static CollectAction COLLECT_ACTION_2;
//	private static CollectAction COLLECT_ACTION_3;
//	private static EvalAction EVAL_ACTION_1;
//	private static EvalAction EVAL_ACTION_2;
//	private static EvalAction EVAL_ACTION_3;
//	private static WhileSteppingAction STEPPING_ACTION_1;
//	private static WhileSteppingAction STEPPING_ACTION_2;
//	private static WhileSteppingAction STEPPING_ACTION_3;


//	 static {
//		BreakpointActionManager breakpointActionMgr = CDebugCorePlugin.getDefault().getBreakpointActionManager();
//
//		COLLECT_ACTION_1 = new CollectAction();
//		COLLECT_ACTION_1.setCollectString("$locals, counter");
//		COLLECT_ACTION_1.setName("CollectAction1");
//		breakpointActionMgr.addAction(COLLECT_ACTION_1);
//		
//		COLLECT_ACTION_2 = new CollectAction();
//		COLLECT_ACTION_2.setCollectString("$reg");
//		COLLECT_ACTION_2.setName("CollectAction2");
//		breakpointActionMgr.addAction(COLLECT_ACTION_2);
//		
//		COLLECT_ACTION_3 = new CollectAction();
//		COLLECT_ACTION_3.setCollectString("$myTraceVariable");
//		COLLECT_ACTION_3.setName("CollectAction3");
//		breakpointActionMgr.addAction(COLLECT_ACTION_3);
//		
//		
//		EVAL_ACTION_1 = new EvalAction();
//		EVAL_ACTION_1.setEvalString("$count=$count+1");
//		EVAL_ACTION_1.setName("EvalAction1");
//		breakpointActionMgr.addAction(EVAL_ACTION_1);
//		
//		EVAL_ACTION_2 = new EvalAction();
//		EVAL_ACTION_2.setEvalString("$count2=$count2+2");
//		EVAL_ACTION_2.setName("EvalAction2");
//		breakpointActionMgr.addAction(EVAL_ACTION_2);
//		
//		EVAL_ACTION_3 = new EvalAction();
//		EVAL_ACTION_3.setEvalString("$count3=$count3+3");
//		EVAL_ACTION_3.setName("EvalAction3");
//		breakpointActionMgr.addAction(EVAL_ACTION_3);
//		
//		//TODO do while stepping actions
//		
//	}

	 @Before
	 public void initialTest() throws Throwable {
		fSession = getGDBLaunch().getSession();
		fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

		ICommandControlService commandControl = fServicesTracker.getService(ICommandControlService.class);
   		fBreakpointsDmc = (IBreakpointsTargetDMContext)commandControl.getContext();
        assert(fBreakpointsDmc != null);

//        fTraceTargetDmc = (ITraceTargetDMContext)commandControl.getContext();
        
		fBreakpointService = fServicesTracker.getService(IBreakpoints.class);
//		fTraceService = fServicesTracker.getService(ITraceControl.class);
		fSession.addServiceEventListener(this, null);
		
		// Create a large array to make sure we don't run out
		fTracepoints = new IBreakpointDMContext[100];
		
		// Run an initial test to check that everything is ok with GDB
		checkTraceInitialStatus();
	}
	
	@After
	public void shutdown() {
		fSession.removeServiceEventListener(this);
		fBreakpointService = null;
		fServicesTracker.dispose();
	}


	// *********************************************************************
	// Below are utility methods.
	// *********************************************************************
    private static Boolean lock = true;
    enum Events {BP_ADDED, BP_UPDATED, BP_REMOVED, BP_HIT}
    final int BP_ADDED   = Events.BP_ADDED.ordinal();
    final int BP_UPDATED = Events.BP_UPDATED.ordinal();
    final int BP_REMOVED = Events.BP_REMOVED.ordinal();
    final int BP_HIT     = Events.BP_HIT.ordinal();
    private int[]   fBreakpointEvents = new int[Events.values().length];
    private boolean fBreakpointEvent;
    private int     fBreakpointEventCount;

  	@DsfServiceEventHandler
	public void eventDispatched(IBreakpointsAddedEvent e) {
  		synchronized (lock) {
  			fBreakpointEvents[BP_ADDED]++;
  			fBreakpointEventCount++;
  			fBreakpointEvent = true;
  			lock.notifyAll();
  		}
	}

  	@DsfServiceEventHandler
	public void eventDispatched(IBreakpointsUpdatedEvent e) {
  		synchronized (lock) {
  			fBreakpointEvents[BP_UPDATED]++;
  			fBreakpointEventCount++;
  			fBreakpointEvent = true;
  			lock.notifyAll();
  		}
	}

  	@DsfServiceEventHandler
	public void eventDispatched(IBreakpointsRemovedEvent e) {
  		synchronized (lock) {
  			fBreakpointEvents[BP_REMOVED]++;
  			fBreakpointEventCount++;
  			fBreakpointEvent = true;
  			lock.notifyAll();
  		}
	}

	// Clears the counters
	private void clearEventCounters() {
		synchronized (lock) {
			for (int i = 0; i < fBreakpointEvents.length; i++) {
				fBreakpointEvents[i] = 0;
			}
			fBreakpointEvent = false;
			fBreakpointEventCount = 0;
		}
	}

	// Get the breakpoint hit count
	private int getBreakpointEventCount(int event) {
		int count = 0;
  		synchronized (lock) {
  			count = fBreakpointEvents[event];
  		}
		return count;
	}

	// Suspends the thread until an event is flagged
	// NOTE: too simple for real life but good enough for this test suite
	private void waitForBreakpointEvent() {
		synchronized (lock) {
			while (!fBreakpointEvent) {
				try {
					lock.wait();
				} catch (InterruptedException ex) {
				}
			}
			fBreakpointEvent = false;
  		}
	}

	// *********************************************************************
	// Breakpoint service methods (to use with tracepoints).
	// *********************************************************************

    private IBreakpointDMContext insertBreakpoint(final IBreakpointsTargetDMContext context,
    		final Map<String,Object> attributes) throws InterruptedException
    {
        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fBreakpointService.getExecutor().submit(new Runnable() {
            public void run() {
                fBreakpointService.insertBreakpoint(context, attributes,
                		new DataRequestMonitor<IBreakpointDMContext>(fBreakpointService.getExecutor(), null) {
                	@Override
                	protected void handleCompleted() {
                		wait.setReturnInfo(getData());
                		wait.waitFinished(getStatus());
                	}
                });
            }
        });

        // Wait for the result and return the breakpoint id
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());

        return (IBreakpointDMContext)wait.getReturnInfo();
    }

    private void removeBreakpoint(final IBreakpointDMContext breakpoint) throws InterruptedException
    {
    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fBreakpointService.getExecutor().submit(new Runnable() {
            public void run() {
                fBreakpointService.removeBreakpoint(breakpoint, 
                		new RequestMonitor(fBreakpointService.getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        wait.waitFinished(getStatus());
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());
    }

    private void updateBreakpoint(final IBreakpointDMContext breakpoint,
    		final Map<String, Object> delta) throws InterruptedException
    {
    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fBreakpointService.getExecutor().submit(new Runnable() {
            public void run() {
                fBreakpointService.updateBreakpoint(breakpoint, delta,             
                		new RequestMonitor(fBreakpointService.getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        wait.waitFinished(getStatus());
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());
    }
    
    private IBreakpointDMData getBreakpoint(final IBreakpointDMContext breakpoint) throws InterruptedException
    {
    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

    	fBreakpointService.getExecutor().submit(new Runnable() {
    		public void run() {
    			fBreakpointService.getBreakpointDMData(breakpoint, 
    					new DataRequestMonitor<IBreakpointDMData>(fBreakpointService.getExecutor(), null) {
    				@Override
    				protected void handleCompleted() {
    					wait.setReturnInfo(getData());
    					wait.waitFinished(getStatus());
    				}
    			});
    		}
    	});

    	wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
    	assertTrue(wait.getMessage(), wait.isOK());

    	return (IBreakpointDMData)wait.getReturnInfo();
    }
    
    private IBreakpointDMContext[] getBreakpoints(final IBreakpointsTargetDMContext context) throws InterruptedException
    {
    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fBreakpointService.getExecutor().submit(new Runnable() {
            public void run() {
                fBreakpointService.getBreakpoints(context, new DataRequestMonitor<IBreakpointDMContext[]>(fBreakpointService.getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                    	wait.setReturnInfo(getData());
                        wait.waitFinished(getStatus());
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

    	return (IBreakpointDMContext[])wait.getReturnInfo();
    }

	// *********************************************************************
	// TraceControl service methods.
	// *********************************************************************

//    private void startTracing() throws InterruptedException
//    {
//    	startTracing(null);
//    }
//
//    private void startTracing(String errorMessage) throws InterruptedException
//    {
//    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
//
//    	fTraceService.getExecutor().submit(new Runnable() {
//            public void run() {
//                fTraceService.canStartTracing(fTraceTargetDmc, 
//                		new DataRequestMonitor<Boolean>(fTraceService.getExecutor(), null) {
//                    @Override
//                    protected void handleCompleted() {
//                    	wait.setReturnInfo(getData());
//                        wait.waitFinished(getStatus());
//                    }
//                });
//            }
//        });
//
//        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
//		assertTrue(wait.getMessage(), wait.isOK());
//		assertTrue("Not allowed to start tracing!", (Boolean)wait.getReturnInfo());
//
//		wait.waitReset();
//		
//    	fTraceService.getExecutor().submit(new Runnable() {
//            public void run() {
//                fTraceService.startTracing(fTraceTargetDmc, 
//                		new RequestMonitor(fTraceService.getExecutor(), null) {
//                    @Override
//                    protected void handleCompleted() {
//                        wait.waitFinished(getStatus());
//                    }
//                });
//            }
//        });
//
//        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
//        if (errorMessage == null) {
//        	assertTrue(wait.getMessage(), wait.isOK());
//        } else {
//        	assertTrue(wait.getMessage(), !wait.isOK());
//        	assertTrue("Message " + wait.getMessage() + " does not contain \"" + errorMessage +"\"",
//        			   wait.getMessage().indexOf(errorMessage) != -1);
//        }
//    }
//
//    private void stopTracing() throws InterruptedException
//    {
//       	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
//
//    	fTraceService.getExecutor().submit(new Runnable() {
//            public void run() {
//                fTraceService.canStopTracing(fTraceTargetDmc, 
//                		new DataRequestMonitor<Boolean>(fTraceService.getExecutor(), null) {
//                    @Override
//                    protected void handleCompleted() {
//                    	wait.setReturnInfo(getData());
//                        wait.waitFinished(getStatus());
//                    }
//                });
//            }
//        });
//
//        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
//		assertTrue(wait.getMessage(), wait.isOK());
//		assertTrue("Not allowed to stop tracing!", (Boolean)wait.getReturnInfo());
//
//		wait.waitReset();
//		
//    	fTraceService.getExecutor().submit(new Runnable() {
//            public void run() {
//                fTraceService.stopTracing(fTraceTargetDmc, 
//                		new RequestMonitor(fTraceService.getExecutor(), null) {
//                    @Override
//                    protected void handleCompleted() {
//                        wait.waitFinished(getStatus());
//                    }
//                });
//            }
//        });
//
//        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
//		assertTrue(wait.getMessage(), wait.isOK());
//    }
//
//    private boolean isTracing() throws InterruptedException
//    {
//       	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
//
//    	fTraceService.getExecutor().submit(new Runnable() {
//            public void run() {
//                fTraceService.isTracing(fTraceTargetDmc, 
//                		new DataRequestMonitor<Boolean>(fTraceService.getExecutor(), null) {
//                    @Override
//                    protected void handleCompleted() {
//                    	wait.setReturnInfo(getData());
//                        wait.waitFinished(getStatus());
//                    }
//                });
//            }
//        });
//
//        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
//		assertTrue(wait.getMessage(), wait.isOK());
//		
//		return (Boolean)wait.getReturnInfo();
//    }
//    
//    private ITraceStatusDMData getTraceStatus() throws InterruptedException
//    {
//       	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
//
//    	fTraceService.getExecutor().submit(new Runnable() {
//            public void run() {
//                fTraceService.getTraceStatus(fTraceTargetDmc, 
//                		new DataRequestMonitor<ITraceStatusDMData>(fTraceService.getExecutor(), null) {
//                    @Override
//                    protected void handleCompleted() {
//                        wait.setReturnInfo(getData());
//                        wait.waitFinished(getStatus());
//                    }
//                });
//            }
//        });
//
//        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
//		assertTrue(wait.getMessage(), wait.isOK());
//		
//		return (ITraceStatusDMData)wait.getReturnInfo();
//    }

	// *********************************************************************
	// Below are the tests for the control of tracepoints.
	// *********************************************************************

    private static final String SOURCE_FILE     = "TracepointTestApp.cc";
    private static final String METHOD_NAME     = "testTracepoints";
    private static final int    LINE_NUMBER_1   = 84;
    private static final int    LINE_NUMBER_2   = 55;
    private static final int    LINE_NUMBER_3   = 56;
    private static final int    LINE_LOOP_1     = 81;
    private static final int    LINE_LOOP_2     = 88;
    private static final String NO_CONDITION    = "";
//    private static final int    LAST_LINE_NUMBER   = 94;
//    
//	private static final int TOTAL_FRAMES_TO_BE_COLLECTED = 1 + 1 + 10 + 1 + 10000;


    private IBreakpointDMContext[] fTracepoints = null;

//	private void checkTraceStatus(boolean supported, boolean active, int frames, 
//			                      STOP_REASON_ENUM reason, Integer stoppingTracepoint) throws Throwable {
//		ITraceStatusDMData status = getTraceStatus();
//		assertTrue("Error tracing supported should be " + supported + " but was " + status.isTracingSupported(),
//				   status.isTracingSupported() == supported);
//		assertTrue("Error tracing active should be " + active + " but was " + status.isTracingActive(),
//				   status.isTracingActive() == active);
//		boolean isTracing = isTracing();
//		assertTrue("Error, tracing active is " + status.isTracingActive() + " but the tracepoint service thinks it is " + isTracing,
//				   status.isTracingActive() == isTracing);
//
//		assertTrue("Wrong number of collected frames.  Expected " + frames + " but got " +
//				   status.getNumberOfCollectedFrame(),
//				   status.getNumberOfCollectedFrame() == frames);
//		assertTrue("Total buffer size should be positive but is " +
//				   status.getTotalBufferSize(),
//				   status.getTotalBufferSize() > 0);
//		
//		if (fTotalTracingBufferSize == 0) {
//			// Initialize buffer
//			fTotalTracingBufferSize = status.getTotalBufferSize();			
//		} else {
//			assertTrue("Total buffer size changed!  Should be " + fTotalTracingBufferSize +
//					   " but got " + status.getTotalBufferSize(),
//					   status.getTotalBufferSize() == fTotalTracingBufferSize);
//		}
//		
//		assertTrue("Expected stopping reason " + reason + " but got " + status.getStopReason(),
//				   status.getStopReason() == reason);
//		assertTrue("Expected stopping bp " + stoppingTracepoint + " but got " + status.getStoppingTracepoint(),
//				   status.getStoppingTracepoint() == stoppingTracepoint);
//	}
//
//	private void checkTraceStatus(boolean supported, boolean active, int frames) throws Throwable {
//		checkTraceStatus(supported, active, frames, null, null);
//	}

    // GDB 7.0 does not support fast tracepoints, but GDB 7.1 will
    protected boolean fastTracepointsSupported() { return false; }
    
	private class TracepointData {
		String sourceFile;
		int lineNumber;
		String condition;
		int passcount;
		boolean enabled;
		String actions;
		boolean isFastTp;
		
		public TracepointData(String file, int line, String cond, int pass, boolean isEnabled, String acts, boolean fast) {
			sourceFile = file;
			lineNumber = line;
			condition = cond;
			passcount = pass;
			enabled = isEnabled;
			actions = acts;
			if (fastTracepointsSupported()) {
				isFastTp = fast;
			} else {
				isFastTp = false;
			}
		}
	}
	
	private void checkTracepoints(TracepointData[] dataArray) throws Throwable {
		int numTracepoints = dataArray.length;
		
		// Fetch the tp list from the backend
		IBreakpointDMContext[] tracepoints = getBreakpoints(fBreakpointsDmc);
		assertTrue("expected " + numTracepoints + " breakpoint(s), received "
				+ tracepoints.length, tracepoints.length == numTracepoints);

		for (int i=0; i<numTracepoints; i++) {
			TracepointData data = dataArray[i];
			
			// Ensure that the tracepoints were correctly installed
			MIBreakpointDMData tp = (MIBreakpointDMData) getBreakpoint(fTracepoints[i]);
			assertTrue("tracepoint "+i+" is not a tracepoint but a " + tp.getBreakpointType(),
					tp.getBreakpointType().equals(MIBreakpoints.TRACEPOINT));			
			assertTrue("tracepoint "+i+" should be a " + (data.isFastTp?"fast":"slow")+" tracepoint but is not",
					tp.getType().equals("fast tracepoint") == data.isFastTp);			
			assertTrue("tracepoint "+i+" mismatch (wrong file name) got " + tp.getFileName(),
					tp.getFileName().equals(data.sourceFile));
			assertTrue("tracepoint "+i+" mismatch (wrong line number) got " + tp.getLineNumber(),
					tp.getLineNumber() == data.lineNumber);
			assertTrue("tracepoint "+i+" mismatch (wrong condition) got " + tp.getCondition(),
					tp.getCondition().equals(data.condition));
			assertTrue("tracepoint "+i+" mismatch (wrong pass count) got " + tp.getPassCount(),
					tp.getPassCount() == data.passcount);
			assertTrue("tracepoint "+i+" mismatch (wrong state) got " + tp.isEnabled(),
					tp.isEnabled() == data.enabled);
			assertTrue("tracepoint mismatch (wrong actions) got " + tp.getCommands(),
					tp.getCommands().equals(data.actions));

			assertTrue("tracepoint "+i+" mismatch",
					tp.equals((MIBreakpointDMData)getBreakpoint(tracepoints[i])));
		}
	}
	
	/**
	 * This test makes sure that the tracing status is correct when we start.
	 * It also stores the total buffer size to be used by other tests.
	 * This test is being run before every other test by being called
	 * by the @Before method; this allows to verify every launch of GDB. 
	 */
	@Test
	public void checkTraceInitialStatus() throws Throwable {
//		checkTraceStatus(true, false, 0);
	}
	
	/**
	 * This test sets different tracepoints in the program:
	 * - using a method address
	 * - using a method name
	 * - using a filename and line number
	 *
	 * It also set a fast tracepoint by 
	 */
	@Test
	public void testCreateTracepoints() throws Throwable {

		Map<String, Object> attributes = null;
		int index = 0;
		
		// First tracepoint (will be a slow tracepoint)
		attributes = new HashMap<String, Object>();
		attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
		attributes.put(MIBreakpoints.FUNCTION, "*"+METHOD_NAME);
		fTracepoints[index++] = insertBreakpoint(fBreakpointsDmc, attributes);

		waitForBreakpointEvent();
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
				+ fBreakpointEventCount, fBreakpointEventCount == 1);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT_ADDED event(s), received "
				+ getBreakpointEventCount(BP_ADDED), getBreakpointEventCount(BP_ADDED) == 1);
		clearEventCounters();
		
		// Second tracepoint (will be a fast tracepoint)
		attributes = new HashMap<String, Object>();
		attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
		attributes.put(MIBreakpoints.FILE_NAME, SOURCE_FILE);
		attributes.put(MIBreakpoints.FUNCTION, METHOD_NAME);
		fTracepoints[index++] = insertBreakpoint(fBreakpointsDmc, attributes);

		waitForBreakpointEvent();
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
				+ fBreakpointEventCount, fBreakpointEventCount == 1);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT_ADDED event(s), received "
				+ getBreakpointEventCount(BP_ADDED), getBreakpointEventCount(BP_ADDED) == 1);
		clearEventCounters();

		// Third tracepoint (will be a slow tracepoint)
		attributes = new HashMap<String, Object>();
		attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
		attributes.put(MIBreakpoints.FILE_NAME, SOURCE_FILE);
		attributes.put(MIBreakpoints.LINE_NUMBER, LINE_LOOP_1);
		fTracepoints[index++] = insertBreakpoint(fBreakpointsDmc, attributes);

		waitForBreakpointEvent();
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
				+ fBreakpointEventCount, fBreakpointEventCount == 1);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT_ADDED event(s), received "
				+ getBreakpointEventCount(BP_ADDED), getBreakpointEventCount(BP_ADDED) == 1);
		clearEventCounters();

		// Forth tracepoint (will be a fast tracepoint)
		attributes = new HashMap<String, Object>();
		attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
		attributes.put(MIBreakpoints.FILE_NAME, SOURCE_FILE);
		attributes.put(MIBreakpoints.LINE_NUMBER, LINE_NUMBER_1);
		fTracepoints[index++] = insertBreakpoint(fBreakpointsDmc, attributes);

		waitForBreakpointEvent();
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
				+ fBreakpointEventCount, fBreakpointEventCount == 1);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT_ADDED event(s), received "
				+ getBreakpointEventCount(BP_ADDED), getBreakpointEventCount(BP_ADDED) == 1);
		clearEventCounters();		

		// Fifth tracepoint (will be a slow tracepoint)
		attributes = new HashMap<String, Object>();
		attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
		attributes.put(MIBreakpoints.FILE_NAME, SOURCE_FILE);
		attributes.put(MIBreakpoints.LINE_NUMBER, LINE_LOOP_2);
		fTracepoints[index++] = insertBreakpoint(fBreakpointsDmc, attributes);

		waitForBreakpointEvent();
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
				+ fBreakpointEventCount, fBreakpointEventCount == 1);
		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT_ADDED event(s), received "
				+ getBreakpointEventCount(BP_ADDED), getBreakpointEventCount(BP_ADDED) == 1);
		clearEventCounters();		
		
		ArrayList<TracepointData> dataArray = new ArrayList<TracepointData>();
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_2, NO_CONDITION, 0, true, "", false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_3, NO_CONDITION, 0, true, "", true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_LOOP_1, NO_CONDITION, 0, true, "", false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_1, NO_CONDITION, 0, true, "", true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_LOOP_2, NO_CONDITION, 0, true, "", false));
		
		checkTracepoints(dataArray.toArray(new TracepointData[dataArray.size()]));
	}
	
	/**
	 * This test sets the different types of tracepoints and then deletes them
	 */
	@Test
	public void testDeleteTracepoints() throws Throwable {
		testCreateTracepoints();
		// Delete all tracepoints
		for (IBreakpointDMContext tp : fTracepoints) {
			if (tp == null) break;
			removeBreakpoint(tp);
		}

		// Fetch the bp list from the backend
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertTrue("BreakpointService problem: expected " + 0 + " breakpoints, received "
				+ breakpoints.length, breakpoints.length == 0);
	}

	/**
	 * This test sets the different types of tracepoints and then disables them
	 */
	@Test
	public void testDisableTracepoints() throws Throwable {
		testCreateTracepoints();
		
		Map<String, Object> delta = new HashMap<String, Object>();
		delta.put(MIBreakpoints.IS_ENABLED, false);
		// Disable all tracepoints
		for (IBreakpointDMContext tp : fTracepoints) {
			if (tp == null) break;
			updateBreakpoint(tp, delta);
		}

		ArrayList<TracepointData> dataArray = new ArrayList<TracepointData>();
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_2, NO_CONDITION, 0, false, "", false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_3, NO_CONDITION, 0, false, "", true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_LOOP_1, NO_CONDITION, 0, false, "", false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_1, NO_CONDITION, 0, false, "", true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_LOOP_2, NO_CONDITION, 0, false, "", false));
		
		checkTracepoints(dataArray.toArray(new TracepointData[dataArray.size()]));
	}

	/**
	 * This test sets, disables the different types of tracepoints and then enables them
	 */
	@Test
	public void testEnableTracepoints() throws Throwable {
		testDisableTracepoints();
		
		Map<String, Object> delta = new HashMap<String, Object>();
		delta.put(MIBreakpoints.IS_ENABLED, true);
		// Enable all tracepoints
		for (IBreakpointDMContext tp : fTracepoints) {
			if (tp == null) break;
			updateBreakpoint(tp, delta);
		}

		ArrayList<TracepointData> dataArray = new ArrayList<TracepointData>();
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_2, NO_CONDITION, 0, true, "", false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_3, NO_CONDITION, 0, true, "", true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_LOOP_1, NO_CONDITION, 0, true, "", false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_1, NO_CONDITION, 0, true, "", true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_LOOP_2, NO_CONDITION, 0, true, "", false));

		checkTracepoints(dataArray.toArray(new TracepointData[dataArray.size()]));
	}

	/**
	 * This test sets the different types of tracepoints and then sets their passcount
	 */
	@Test
	public void testTracepointPasscount() throws Throwable {
		testCreateTracepoints();
		
		Map<String, Object> delta = new HashMap<String, Object>();
		// Set passcount for all tracepoints
		for (int i=0; i<fTracepoints.length; i++) {
			if (fTracepoints[i] == null) break;
			if (PASS_COUNTS[i] == 0) continue;
			delta.put(MIBreakpoints.PASS_COUNT, PASS_COUNTS[i]);
			updateBreakpoint(fTracepoints[i], delta);
		}

		ArrayList<TracepointData> dataArray = new ArrayList<TracepointData>();
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_2, NO_CONDITION, PASS_COUNTS[0], true, "", false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_3, NO_CONDITION, PASS_COUNTS[1], true, "", true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_LOOP_1, NO_CONDITION, PASS_COUNTS[2], true, "", false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_1, NO_CONDITION, PASS_COUNTS[3], true, "", true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_LOOP_2, NO_CONDITION, PASS_COUNTS[4], true, "", false));
		
		checkTracepoints(dataArray.toArray(new TracepointData[dataArray.size()]));
	}

	/**
	 * This test sets a tracepoint and then gives it a condition
	 */
	//@Test
	public void testTracepointCondition() throws Throwable {
		// Use trace state variables and stuff
	}
	

//	private void testActions(String[] actions) throws Throwable {
//		Map<String, Object> delta = new HashMap<String, Object>();
//		ArrayList<TracepointData> dataArray = new ArrayList<TracepointData>();
//		
//		for (int i=0; i<actions.length; i++) {
//			delta.put(MIBreakpoints.COMMANDS, actions[i]);
//			updateBreakpoint(fTracepoints[i], delta);
//			dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_1, NO_CONDITION, 0, true, actions[i], false));
//		}
//		
//		checkTracepoints(dataArray.toArray(new TracepointData[dataArray.size()]));
//	}
//	
//	/**
//	 * This test sets the different types of tracepoints and then sets some collect actions
//	 */	
//	@Test
//	public void testCollectActions() throws Throwable {
//		final String ACTIONS1 = COLLECT_ACTION_1.getName()+","+COLLECT_ACTION_2.getName()+","+COLLECT_ACTION_3.getName();
//		final String ACTIONS2 = COLLECT_ACTION_1.getName()+","+COLLECT_ACTION_3.getName();
//		final String ACTIONS3 = COLLECT_ACTION_3.getName();
//
//		testCreateTracepoints();
//		testActions(new String[] {ACTIONS1, ACTIONS2, ACTIONS3, ""});
//	}
//	
//	/**
//	 * This test sets the different types of tracepoints and then sets some eval actions
//	 */
//	@Test
//	public void testEvalActions() throws Throwable {
//		final String ACTIONS1 = EVAL_ACTION_1.getName()+","+EVAL_ACTION_2.getName()+","+EVAL_ACTION_3.getName();
//		final String ACTIONS2 = EVAL_ACTION_1.getName()+","+EVAL_ACTION_3.getName();
//		final String ACTIONS3 = EVAL_ACTION_3.getName();
//
//		testCreateTracepoints();
//		testActions(new String[] {ACTIONS1, ACTIONS2, ACTIONS3, ""});
//	}
//	
//	/**
//	 * This test sets the different types of tracepoints and then sets some while-stepping actions
//	 */
//	//@Test
//	public void testSteppingActions() throws Throwable {
////		final String ACTIONS1 = STEPPING_ACTION_1.getName()+","+STEPPING_ACTION_2.getName()+","+STEPPING_ACTION_3.getName();
////		final String ACTIONS2 = STEPPING_ACTION_1.getName()+","+STEPPING_ACTION_3.getName();
////		final String ACTIONS3 = STEPPING_ACTION_3.getName();
////
////		testCreateTracepoints();
////		testActions(new String[] {ACTIONS1, ACTIONS2, ACTIONS3, ""});
//	}
//
//	/**
//	 * This test sets the different types of tracepoints and then sets a mix of different 
//     * tracepoint actions
//	 */
//	//@Test
//	public void testMixedActions() throws Throwable {
////		final String ACTIONS1 = COLLECT_ACTION_1.getName() + "," +
////		                        EVAL_ACTION_2.getName() + "," +
////		                        STEPPING_ACTION_3.getName();
////		final String ACTIONS2 = STEPPING_ACTION_1.getName() + "," +
////								COLLECT_ACTION_2.getName() + "," +
////		                        EVAL_ACTION_1.getName() + "," +
////								COLLECT_ACTION_3.getName() + "," +
////		                        EVAL_ACTION_2.getName() + "," +
////		                        EVAL_ACTION_3.getName();
////		final String ACTIONS3 = EVAL_ACTION_3.getName() + "," +
////								COLLECT_ACTION_1.getName() + "," +
////								EVAL_ACTION_2.getName() + "," +
////								STEPPING_ACTION_3.getName();
////
////		testCreateTracepoints();
////		testActions(new String[] {ACTIONS1, ACTIONS2, ACTIONS3, ""});
//	}
//
//	/**
//	 * This test sets the different types of tracepoints and then sets some default collect actions
//	 */
//	//@Test
//	public void testDefaultCollectAction() throws Throwable {
//		testCreateTracepoints();
//	}
//
//	// *********************************************************************
//	// Below are the tests for the control of tracing
//	// *********************************************************************
//
//	/**
//	 * This test sets different tracepoints in the program:
//	 * - using a filename and line number
//	 * - using a method name
//	 * - using a method address
//	 * 
//	 * and confirms they are installed when tracing starts
//	 */
//	@Test
//	public void testCreateAndRunTracepoints() throws Throwable {
//		testCreateTracepoints();
//		startTracing();
//		SyncUtil.SyncRunToLocation(Integer.toString(LAST_LINE_NUMBER));
//		checkTraceStatus(true, true, TOTAL_FRAMES_TO_BE_COLLECTED);
//		stopTracing();
//		checkTraceStatus(true, false, TOTAL_FRAMES_TO_BE_COLLECTED);
//	}
//	
//	/**
//	 * This test sets the different types of tracepoints and then deletes them
// 	 * and confirms they are not installed when tracing starts
//	 */
//	@Test
//	public void testDeleteAndRunTracepoints() throws Throwable {
//		testDeleteTracepoints();
//		startTracing("No tracepoints available to download");
//		SyncUtil.SyncRunToLocation(Integer.toString(LAST_LINE_NUMBER));
//		checkTraceStatus(true, false, 0);
//	}
//
//	/**
//	 * This test sets the different types of tracepoints and then disables them
// 	 * and confirms they are not hit when tracing starts
//	 */
//	@Test
//	public void testDisableAndRunTracepoints() throws Throwable {
//		testDisableTracepoints();
//		startTracing("None of the downloadable tracepoints enabled");
//		SyncUtil.SyncRunToLocation(Integer.toString(LAST_LINE_NUMBER));
//		checkTraceStatus(true, false, 0);
//	}
//
//	/**
//	 * This test sets, disables the different types of tracepoints and then enables them
//	 * and confirms they are hit when tracing starts
//	 */
//	@Test
//	public void testEnableAndRunTracepoints() throws Throwable {
//		testEnableTracepoints();
//		startTracing();
//		SyncUtil.SyncRunToLocation(Integer.toString(LAST_LINE_NUMBER));
//		checkTraceStatus(true, true, TOTAL_FRAMES_TO_BE_COLLECTED);
//		stopTracing();
//		checkTraceStatus(true, false, TOTAL_FRAMES_TO_BE_COLLECTED);
//	}
//
//	/**
//	 * This test sets the different types of tracepoints and then sets their passcount
//	 * and confirms the passcount is respected
//	 */
//	@Test
//	public void testTracepointPasscountAndRun1() throws Throwable {
//		testTracepointPasscount();
//		startTracing();
//		SyncUtil.SyncRunToLocation(Integer.toString(LAST_LINE_NUMBER));
//		
//		checkTraceStatus(true, false,
//				         1 + 1 + 10 + 1 + PASS_COUNTS[4],
//				         STOP_REASON_ENUM.PASSCOUNT, 6);
//	}
//
//	/**
//	 * This test sets the passcount of the a tracepoint that is hit before some
//	 * other tracepoints are hit, to confirm tracing really stops.
//	 */
//	@Test
//	public void testTracepointPasscountAndRun2() throws Throwable {
//		testTracepointPasscount();
//
//		// Set the passcount of the forth tp to make it stop the tracing
//		Map<String, Object> delta = new HashMap<String, Object>();
//		delta.put(MIBreakpoints.IGNORE_COUNT, 1);
//		updateBreakpoint(fTracepoints[3], delta);
//
//		startTracing();
//		SyncUtil.SyncRunToLocation(Integer.toString(LAST_LINE_NUMBER));
//		
//		checkTraceStatus(true, false,
//						 1 + 1 + 10 + 1,
//				         STOP_REASON_ENUM.PASSCOUNT, 5);
//	}
//
//	/**
//	 * This test sets a tracepoint and then gives it a condition
// 	 * and confirms the condition is respected
//	 */
//	//@Test
//	public void testTracepointConditionAndRun() throws Throwable {
//		// Use trace state variables and stuff
//	}
//	
//	/**
//	 * This test sets the different types of tracepoints and then sets some collect actions
//	 * and confirms the proper information is collected
//	 */
//	//@Test
//	public void testCollectActionsAndRun() throws Throwable {
//		testCreateTracepoints();
//	}
//
//	/**
//	 * This test sets the different types of tracepoints and then sets some eval actions
//	 * and confirms the trace variables are properly updated
//	 */
//	//@Test
//	public void testEvalActionsAndRun() throws Throwable {
//		testCreateTracepoints();
//	}
//
//	/**
//	 * This test sets the different types of tracepoints and then sets some while-stepping actions
//	 * and confirms the proper information is collected
//	 */
//	//@Test
//	public void testSteppingActionsAndRun() throws Throwable {
//		testCreateTracepoints();
//	}
//
//	/**
//	 * This test sets the different types of tracepoints and then sets a mix of different 
//     * tracepoint actions and confirms the proper information is collected
//	 */
//	//@Test
//	public void testMixedActionsAndRun() throws Throwable {
//		testCreateTracepoints();
//	}
//
//	/**
//	 * This test sets the different types of tracepoints and then sets some default collect actions
//	 * and confirms the proper information is collected
//	 */
//	//@Test
//	public void testDefaultCollectActionAndRun() throws Throwable {
//		testCreateTracepoints();
//	}
//
	

}
