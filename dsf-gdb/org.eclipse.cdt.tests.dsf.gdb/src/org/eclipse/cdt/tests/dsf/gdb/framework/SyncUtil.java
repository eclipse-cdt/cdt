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
package org.eclipse.cdt.tests.dsf.gdb.framework;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil.DefaultTimeouts.ETimeout;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Timeout wait values are in milliseconds, or WAIT_FOREVER.
 */
public class SyncUtil {
    
    private static IGDBControl fGdbControl;
    private static IMIRunControl fRunControl;
    private static MIStack fStack;
    private static IExpressions fExpressions;
    private static DsfSession fSession;
	
    private static CommandFactory fCommandFactory;
	private static IGDBProcesses fProcessesService;
    
    // Initialize some common things, once the session has been established
    public static void initialize(DsfSession session) throws Exception {
    	fSession = session;
    	
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
	        	DsfServicesTracker tracker = 
	        		new DsfServicesTracker(TestsPlugin.getBundleContext(), 
	        				fSession.getId());
	        	
	        	fGdbControl = tracker.getService(IGDBControl.class);		   		
	        	fRunControl = tracker.getService(IMIRunControl.class);
	        	fStack = tracker.getService(MIStack.class);
	        	fExpressions = tracker.getService(IExpressions.class);
	        	fProcessesService = tracker.getService(IGDBProcesses.class);
	        	fCommandFactory = fGdbControl.getCommandFactory();
	        		        		        	
	        	tracker.dispose();
            }
	    };
	    fSession.getExecutor().submit(runnable).get();
	}

	public static MIStoppedEvent step(int numSteps, StepType stepType) throws Throwable {
	    MIStoppedEvent retVal = null;
		for (int i=0; i<numSteps; i++) {
		    retVal = step(stepType, DefaultTimeouts.get(ETimeout.step));
		}
		return retVal;
	}

	public static MIStoppedEvent step(final StepType stepType, int numSteps, int timeout) throws Throwable {
	    MIStoppedEvent retVal = null;
		for (int i=0; i<numSteps; i++) {
		    retVal = step(stepType, timeout);
		}
		return retVal;
	}

	public static MIStoppedEvent step(StepType stepType) throws Throwable {
		return step(stepType, DefaultTimeouts.get(ETimeout.step));
	}

	public static MIStoppedEvent step(StepType stepType, int timeout) throws Throwable {
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return step(containerDmc, stepType, timeout);
	}
	
	public static MIStoppedEvent step(IExecutionDMContext dmc, StepType stepType) throws Throwable {
		return step(dmc, stepType, DefaultTimeouts.get(ETimeout.step));		
	}
	
	public static MIStoppedEvent step(final IExecutionDMContext dmc, final StepType stepType, int timeout) throws Throwable {
		
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
			new ServiceEventWaitor<MIStoppedEvent>(
					fSession,
					MIStoppedEvent.class);

		fRunControl.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				switch(stepType) {
				case STEP_INTO:
					fGdbControl.queueCommand(fCommandFactory.createMIExecStep(dmc), null);
					break;
				case STEP_OVER:
					fGdbControl.queueCommand(fCommandFactory.createMIExecNext(dmc), null);
					break;
				case STEP_RETURN:
					fGdbControl.queueCommand(fCommandFactory.createMIExecFinish(fStack.createFrameDMContext(dmc, 0)), null);
					break;
				default:
					fail("Unsupported step type; " + stepType.toString());
				}
			}
		});

		// Wait for the execution to suspend after the step
		return eventWaitor.waitForEvent(timeout);
	}

	public static MIStoppedEvent runToLine(IExecutionDMContext dmc, String fileName, String lineNo, 
            boolean skipBreakpoints) throws Throwable {
		return runToLine(dmc, fileName, lineNo, skipBreakpoints, DefaultTimeouts.get(ETimeout.runToLine));
	}

	public static MIStoppedEvent runToLine(final IExecutionDMContext dmc, final String fileName, final String lineNo, 
			                               boolean skipBreakpoints, int timeout) throws Throwable {
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
            new ServiceEventWaitor<MIStoppedEvent>(
                    fSession,
                    MIStoppedEvent.class);
		
		fRunControl.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				
				fGdbControl.queueCommand(
						fCommandFactory.createMIExecUntil(dmc, fileName + ":" + lineNo), //$NON-NLS-1$
						null);
			}
		});

		// Wait for the execution to suspend after the step
    	return eventWaitor.waitForEvent(timeout);	
	}

	public static MIStoppedEvent runToLine(String fileName, String lineNo, 
            boolean skipBreakpoints) throws Throwable {
		return runToLine(fileName, lineNo, skipBreakpoints, DefaultTimeouts.get(ETimeout.runToLine));
	}

	public static MIStoppedEvent runToLine(String fileName, String lineNo, 
            boolean skipBreakpoints, int timeout) throws Throwable {
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return runToLine(containerDmc, fileName, lineNo, skipBreakpoints, timeout);
	}

	public static MIStoppedEvent runToLine(String fileName, String lineNo) throws Throwable {
		return runToLine(fileName, lineNo, DefaultTimeouts.get(ETimeout.runToLine));
	}

	public static MIStoppedEvent runToLine(String fileName, String lineNo, int timeout) throws Throwable {
		return runToLine(fileName, lineNo, false, timeout);
	}

	public static int addBreakpoint(String location) throws Throwable {
		return addBreakpoint(location, DefaultTimeouts.get(ETimeout.addBreakpoint));
	}

	public static int addBreakpoint(String location, int timeout) throws Throwable {
		return addBreakpoint(location, true, timeout);
	}

	public static int addBreakpoint(String location, boolean temporary) throws Throwable {
		return addBreakpoint(location, temporary, DefaultTimeouts.get(ETimeout.addBreakpoint));
	}
	
	public static int addBreakpoint(final String location, final boolean temporary, int timeout)
							throws Throwable {

        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
        final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);
        
		Query<MIBreakInsertInfo> query = new Query<MIBreakInsertInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIBreakInsertInfo> rm) {
				fGdbControl.queueCommand(
						fCommandFactory.createMIBreakInsert(bpTargetDmc, temporary, false, null, 0, location, 0),
						rm);
			}
		};
		
		fGdbControl.getExecutor().execute(query);
		MIBreakInsertInfo info = query.get(timeout, TimeUnit.MILLISECONDS);
        return info.getMIBreakpoints()[0].getNumber();
	}

	
	public static int[] getBreakpointList(int timeout) throws Throwable {
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
        final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);

        Query<MIBreakListInfo> query = new Query<MIBreakListInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIBreakListInfo> rm) {
				fGdbControl.queueCommand(fCommandFactory.createMIBreakList(bpTargetDmc), rm);
			}
		};
		
		fGdbControl.getExecutor().execute(query);
		MIBreakListInfo info = query.get(timeout, TimeUnit.MILLISECONDS);
		MIBreakpoint[] breakpoints = info.getMIBreakpoints();
		
		int[] result = new int[breakpoints.length];
		for (int i = 0; i < breakpoints.length; i++) {
			result[i] = breakpoints[i].getNumber();
		}
		return result;
	}
	
	public static void deleteBreakpoint(int breakpointIndex, int timeout) throws Throwable {
		deleteBreakpoint(new int[] {breakpointIndex}, timeout);
	}
	
	public static void deleteBreakpoint(final int[] breakpointIndices, int timeout) throws Throwable {
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
        final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);

        Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fGdbControl.queueCommand(
						fCommandFactory.createMIBreakDelete(bpTargetDmc, breakpointIndices),
						rm);
			}
		};
		
		fGdbControl.getExecutor().execute(query);
		query.get(timeout, TimeUnit.MILLISECONDS);
	}

	
	public static MIStoppedEvent resumeUntilStopped(final IExecutionDMContext dmc, int timeout) throws Throwable {
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
            new ServiceEventWaitor<MIStoppedEvent>(
                    fSession,
                    MIStoppedEvent.class);

		fRunControl.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				fGdbControl.queueCommand(
						fCommandFactory.createMIExecContinue(dmc),
						null);
			}
		});

		// Wait for the execution to suspend after the step
    	return eventWaitor.waitForEvent(timeout);			
	}

	public static MIStoppedEvent resumeUntilStopped() throws Throwable {
		return resumeUntilStopped(DefaultTimeouts.get(ETimeout.resumeUntilStopped));
	}

	public static MIStoppedEvent resumeUntilStopped(int timeout) throws Throwable {
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return resumeUntilStopped(containerDmc, timeout);
	}

	public static MIRunningEvent resume(final IExecutionDMContext dmc, int timeout) throws Throwable {
        final ServiceEventWaitor<MIRunningEvent> eventWaitor =
            new ServiceEventWaitor<MIRunningEvent>(
                    fSession,
                    MIRunningEvent.class);

		fRunControl.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been resumed
				fGdbControl.queueCommand(
						fCommandFactory.createMIExecContinue(dmc),
						null);
			}
		});

		// Wait for the execution to start after the step
    	return eventWaitor.waitForEvent(timeout);			
	}

	public static MIRunningEvent resume() throws Throwable {
		return resume(DefaultTimeouts.get(ETimeout.resume));
	}

	public static MIRunningEvent resume(int timeout) throws Throwable {
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return resume(containerDmc, timeout);
	}

	public static MIStoppedEvent waitForStop() throws Throwable {
		return waitForStop(DefaultTimeouts.get(ETimeout.waitForStop));
	}
	
	// This method is risky.  If the command to resume/step execution
	// is sent and the stopped event is received before we call this method
	// here, then we will miss the stopped event.
	// Normally, one shoudl initialize the ServiveEventWaitor before
	// triggering the resume to make sure not to miss the stopped event.
	// However, in some case this method will still work, for instance
	// if there is a sleep in the code between the resume and the time
	// it stops; this will give us plenty of time to call this method.
	public static MIStoppedEvent waitForStop(int timeout) throws Throwable {
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
            new ServiceEventWaitor<MIStoppedEvent>(
                    fSession,
                    MIStoppedEvent.class);

		// Wait for the execution to suspend
    	return eventWaitor.waitForEvent(timeout);			
	}
	
	public static MIStoppedEvent runToLocation(String location) throws Throwable {
		return runToLocation(location, DefaultTimeouts.get(ETimeout.runToLocation));
	}
	
	public static MIStoppedEvent runToLocation(String location, int timeout) throws Throwable {
		// Set a temporary breakpoint and run to it.
		// Note that if there were other breakpoints set ahead of this one,
		// they will stop execution earlier than planned
		addBreakpoint(location, true, timeout);
		return resumeUntilStopped(timeout);
	}
	
    public static IFrameDMContext getStackFrame(final IExecutionDMContext execCtx, final int level) throws Throwable {
    	Query<IFrameDMContext> query = new Query<IFrameDMContext>() {
            @Override
            protected void execute(final DataRequestMonitor<IFrameDMContext> rm) {
                fStack.getFrames(execCtx, new ImmediateDataRequestMonitor<IFrameDMContext[]>(rm) {
                    @Override
                    protected void handleSuccess() {
                        if (getData().length > level) {
                            rm.setData(getData()[level]);
                        } else {
                            rm.setStatus(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, "Frame not available"));
                        }
                        rm.done();
                    }
                });
            }
        };

        fSession.getExecutor().execute(query);
        return query.get(500, TimeUnit.MILLISECONDS);
    }
    
    public static IFrameDMData getFrameData(final IExecutionDMContext execCtx, final int level) throws Throwable {
      	Query<IFrameDMData> query = new Query<IFrameDMData>() {
    		@Override
    		protected void execute(final DataRequestMonitor<IFrameDMData> rm) {
    			fStack.getFrames(execCtx, level, level, new ImmediateDataRequestMonitor<IFrameDMContext[]>(rm) {
    				@Override
    				protected void handleSuccess() {
    					IFrameDMContext[] frameDmcs = getData();
    					assert frameDmcs != null;
    					assert frameDmcs.length == 1;
    					fStack.getFrameData(frameDmcs[0], rm);
    				}
    			});
    		}
    	};

    	fSession.getExecutor().execute(query);
    	return query.get(500, TimeUnit.MILLISECONDS);
    }

    public static IExpressionDMContext createExpression(final IDMContext parentCtx, final String expression)
        throws Throwable {
        Callable<IExpressionDMContext> callable = new Callable<IExpressionDMContext>() {
            @Override
			public IExpressionDMContext call() throws Exception {
                return fExpressions.createExpression(parentCtx, expression);
            }
        };
        return fSession.getExecutor().submit(callable).get();
    }

    public static FormattedValueDMContext getFormattedValue(
        final IFormattedValues service, final IFormattedDataDMContext dmc, final String formatId) throws Throwable 
    {
        Callable<FormattedValueDMContext> callable = new Callable<FormattedValueDMContext>() {
            @Override
			public FormattedValueDMContext call() throws Exception {
                return service.getFormattedValueContext(dmc, formatId);
            }
        };
        return fSession.getExecutor().submit(callable).get();
    }
    
    public static IMIExecutionDMContext createExecutionContext(final IContainerDMContext parentCtx, final int threadId) throws Throwable {
	    Callable<IMIExecutionDMContext> callable = new Callable<IMIExecutionDMContext>() {
	        @Override
			public IMIExecutionDMContext call() throws Exception {
	        	String threadIdStr = Integer.toString(threadId);
	        	IProcessDMContext processDmc = DMContexts.getAncestorOfType(parentCtx, IProcessDMContext.class);
	        	IThreadDMContext threadDmc = fProcessesService.createThreadContext(processDmc, threadIdStr);
	            return fProcessesService.createExecutionContext(parentCtx, threadDmc, threadIdStr);
	        }
	    };
	    return fSession.getExecutor().submit(callable).get();
    }
    
    static class DefaultTimeouts {

		/**
		 * Overridable default timeout values. An override is specified using a
		 * system property that is "dsf.gdb.tests.timeout.default." plus the
		 * name of the enum below.
		 */
    	enum ETimeout {
    		addBreakpoint,
    		deleteBreakpoint,
    		getBreakpointList,
    		createExecutionContext,
    		createExpression,
    		getFormattedValue,
    		getStackFrame,
    		resume,
    		resumeUntilStopped,
    		runToLine,
    		runToLocation,
    		step,
    		waitForStop
    	}

		/**
		 * Map of timeout enums to their <b>harcoded</b> default value )in
		 * milliseconds). These can be individually overridden with a system
		 * property.
		 * 
		 * <p>
		 * In practice, these operations are either very quick or the amount of
		 * time is hard to predict (depends on what the test is doing). For ones
		 * that are quick, we allot 1 second, which is ample. For the unknowns
		 * we allows 10 seconds, which is probably ample in most cases. Tests
		 * can provide larger values as needed in specific SyncUtil calls.
		 */
    	private static Map<ETimeout,Integer> sTimeouts = new HashMap<ETimeout, Integer>();
    	static {
    		sTimeouts.put(ETimeout.addBreakpoint, 1000);
    		sTimeouts.put(ETimeout.deleteBreakpoint, 1000);
    		sTimeouts.put(ETimeout.getBreakpointList, 1000);
    		sTimeouts.put(ETimeout.createExecutionContext, 1000);
    		sTimeouts.put(ETimeout.createExpression, 1000);
    		sTimeouts.put(ETimeout.getFormattedValue, 1000);
    		sTimeouts.put(ETimeout.getStackFrame, 1000);
    		sTimeouts.put(ETimeout.resume, 1000);
    		sTimeouts.put(ETimeout.resumeUntilStopped, 10000); // 10 seconds
    		sTimeouts.put(ETimeout.runToLine, 10000);	// 10 seconds
    		sTimeouts.put(ETimeout.runToLocation, 10000);	// 10 seconds    		
    		sTimeouts.put(ETimeout.step, 1000);
    		sTimeouts.put(ETimeout.waitForStop, 10000);	// 10 seconds
    	}

		/**
		 * Get the default timeout to use when the caller of a SyncUtil method
		 * doesn't specify one. We honor overrides specified via system
		 * properties, as well as apply the multiplier that can also be
		 * specified via a system property.
		 * 
		 * @param timeout
		 *            the timeout enum
		 * @return the default value
		 */
    	static int get(ETimeout timeout) {
    		int value = -1;
    		final String propname = "dsf.gdb.tests.timeout.default." + timeout.toString();
    		final String prop = System.getProperty(propname);
    		if (prop != null) {
	    		try {
	    			value = Integer.valueOf(value);
	    			if (value < 0) {
	    				TestsPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "\"" + propname + "\" property incorrectly specified. Should be an integer value or not specified at all.")); //$NON-NLS-1$
	    				value = -1;
	    			}
	    		}
	    		catch (NumberFormatException exc) {
	    			TestsPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "\"" + propname + "\" property incorrectly specified. Should be an integer value or not specified at all.")); //$NON-NLS-1$
	    			value = -1;
	    		}
    		}
    		
    		if (value == -1) {
    			value = sTimeouts.get(timeout);
    		}
    		assert value >= 0;
    		return TestsPlugin.massageTimeout(value);
    	}
    }

	/**
	 * Utility method to return the container DM context. This can be used only by
	 * tests that deal with a single heavyweight process. If more than one
	 * process is available, this method will fail. 
	 * 
	 * <p>
	 * This must NOT be called from the DSF executor.
	 * 
	 * @return the process context
	 * @throws InterruptedException
	 */
	@ThreadSafeAndProhibitedFromDsfExecutor("fSession.getExecutor()")
	public static IContainerDMContext getContainerContext() throws InterruptedException {
		assert !fProcessesService.getExecutor().isInExecutorThread();

		Query<IContainerDMContext> query = new Query<IContainerDMContext>() {
			@Override
			protected void execute(final DataRequestMonitor<IContainerDMContext> rm) {
				fProcessesService.getProcessesBeingDebugged(
            			fGdbControl.getContext(), 
            			new ImmediateDataRequestMonitor<IDMContext[]>() {
                    @Override
                    protected void handleCompleted() {
                    	if (isSuccess()) {
                    		IDMContext[] contexts = getData();
                    		Assert.assertNotNull("invalid return value from service", contexts);
                    		Assert.assertEquals("unexpected number of processes", 1, contexts.length);
                    		IDMContext context = contexts[0];    
                    		Assert.assertNotNull("unexpected process context type ", context);
                    		rm.done((IContainerDMContext)context);
                    	} else {
                            rm.done(getStatus());
                    	}
                    }
            	});
			}
		};
		
		fGdbControl.getExecutor().execute(query);
		try {
			return query.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		return null;
	}

	/**
	 * Utility method to return the execution DM context.
	 */
	@ThreadSafeAndProhibitedFromDsfExecutor("fSession.getExecutor()")
	public static IMIExecutionDMContext getExecutionContext(final int threadIndex) throws InterruptedException {
		assert !fProcessesService.getExecutor().isInExecutorThread();

        final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		Query<IMIExecutionDMContext> query = new Query<IMIExecutionDMContext>() {
			@Override
			protected void execute(final DataRequestMonitor<IMIExecutionDMContext> rm) {
				fProcessesService.getProcessesBeingDebugged(
            			containerDmc, 
            			new ImmediateDataRequestMonitor<IDMContext[]>() {
                    @Override
                    protected void handleCompleted() {
                    	if (isSuccess()) {
                    		IDMContext[] threads = getData();
                    		Assert.assertNotNull("invalid return value from service", threads);
                    		Assert.assertTrue("unexpected number of threads", threadIndex < threads.length);
                    		IDMContext thread = threads[threadIndex];    
                    		Assert.assertNotNull("unexpected thread context type ", thread);
                    		rm.setData((IMIExecutionDMContext)thread);
                    	} else {
                            rm.setStatus(getStatus());
                    	}
                    	rm.done();
                    }
            	});
			}
		};
		
		fGdbControl.getExecutor().execute(query);
		try {
			return query.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		return null;
	}

    /**
     * Restart the program.
     */
	public static MIStoppedEvent restart(final GdbLaunch launch) throws Throwable {	
		final IContainerDMContext containerDmc = getContainerContext();

		// Check if restart is allowed
        Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				fProcessesService.canRestart(
            			containerDmc,
            			new ImmediateDataRequestMonitor<Boolean>(rm) {
            				@Override
            				protected void handleSuccess() {
            					rm.setData(getData());
            					rm.done();
            				}
            			});
            	
			}
        };

        fGdbControl.getExecutor().execute(query);
        boolean canRestart = query.get(500, TimeUnit.MILLISECONDS);
        if (!canRestart) {
        	throw new CoreException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, "Unable to restart"));
        }

        // Now wait for the stopped event of the restart
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
			new ServiceEventWaitor<MIStoppedEvent>(
					fSession,
					MIStoppedEvent.class);
			
        // Perform the restart
        Query<IContainerDMContext> query2 = new Query<IContainerDMContext>() {
			@SuppressWarnings("unchecked")
			@Override
			protected void execute(final DataRequestMonitor<IContainerDMContext> rm) {
				Map<String, Object> attributes = null;
				try {
					attributes = launch.getLaunchConfiguration().getAttributes();
				} catch (CoreException e) {}

				fProcessesService.restart(containerDmc, attributes, rm);
			}
        };

        fGdbControl.getExecutor().execute(query2);
        query2.get(500, TimeUnit.MILLISECONDS);
        
        
 		MIStoppedEvent event = eventWaitor.waitForEvent(DefaultTimeouts.get(ETimeout.waitForStop));
 		if (event instanceof MISignalEvent) {
 			// This is not the stopped event we were waiting for.  Get the next one.
 	 		event = eventWaitor.waitForEvent(DefaultTimeouts.get(ETimeout.waitForStop));
 		}
 		return event;
    }
}
