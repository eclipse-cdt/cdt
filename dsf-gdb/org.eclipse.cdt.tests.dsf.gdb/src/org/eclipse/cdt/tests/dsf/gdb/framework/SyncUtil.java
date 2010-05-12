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

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil.DefaultTimeouts.ETimeout;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Timeout wait values are in milliseconds, or WAIT_FOREVER.
 */
public class SyncUtil {
    
    private static ICommandControlService fCommandControl;
    private static MIRunControl fRunControl;
    private static MIStack fStack;
    private static IExpressions fExpressions;
    private static DsfSession fSession;
	
    private static CommandFactory fCommandFactory;

    private static IBreakpointsTargetDMContext fBreakpointsDmc;
	private static IProcesses fProcessesService;
    
    public static final int WAIT_FOREVER = ServiceEventWaitor.WAIT_FOREVER;
    
    // Initialize some common things, once the session has been established
    public static void initialize(DsfSession session) {
    	fSession = session;
    	
    	DsfServicesTracker tracker = 
    		new DsfServicesTracker(TestsPlugin.getBundleContext(), 
    				fSession.getId());
    	
    	fCommandControl = tracker.getService(ICommandControlService.class);

   		fBreakpointsDmc = (IBreakpointsTargetDMContext)fCommandControl.getContext();
   		
		fRunControl = tracker.getService(MIRunControl.class);
		fStack = tracker.getService(MIStack.class);
		fExpressions = tracker.getService(IExpressions.class);
		fProcessesService = tracker.getService(IProcesses.class);
		
		fCommandFactory = tracker.getService(IMICommandControl.class).getCommandFactory();

		tracker.dispose();
	}

	public static MIStoppedEvent step(int numSteps, final StepType stepType) throws Throwable {
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

	public static MIStoppedEvent step(final StepType stepType) throws Throwable {
		return step(stepType, DefaultTimeouts.get(ETimeout.step));
	}

	public static MIStoppedEvent step(final StepType stepType, int timeout) throws Throwable {
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return step(containerDmc, stepType, timeout);
	}
	
	public static MIStoppedEvent step(final IExecutionDMContext dmc, final StepType stepType) throws Throwable {
		return step(dmc, stepType, DefaultTimeouts.get(ETimeout.step));		
	}
	
	public static MIStoppedEvent step(final IExecutionDMContext dmc, final StepType stepType, int timeout) throws Throwable {
		
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
			new ServiceEventWaitor<MIStoppedEvent>(
					fSession,
					MIStoppedEvent.class);

		fRunControl.getExecutor().submit(new Runnable() {
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				switch(stepType) {
				case STEP_INTO:
					fCommandControl.queueCommand(fCommandFactory.createMIExecStep(dmc), null);
					break;
				case STEP_OVER:
					fCommandControl.queueCommand(fCommandFactory.createMIExecNext(dmc), null);
					break;
				case STEP_RETURN:
					fCommandControl.queueCommand(fCommandFactory.createMIExecFinish(fStack.createFrameDMContext(dmc, 0)), null);
					break;
				default:
					Assert.assertTrue("Unsupported step type; " + stepType.toString(), false);
				}
			}
		});

		// Wait for the execution to suspend after the step
		return eventWaitor.waitForEvent(timeout);
	}

	public static MIStoppedEvent runToLine(final IExecutionDMContext dmc, final String fileName, final String lineNo, 
            final boolean skipBreakpoints) throws Throwable {
		return runToLine(dmc, fileName, lineNo, skipBreakpoints, DefaultTimeouts.get(ETimeout.runToLine));
	}

	public static MIStoppedEvent runToLine(final IExecutionDMContext dmc, final String fileName, final String lineNo, 
			                         final boolean skipBreakpoints, int timeout) throws Throwable {
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
            new ServiceEventWaitor<MIStoppedEvent>(
                    fSession,
                    MIStoppedEvent.class);
		
		fRunControl.getExecutor().submit(new Runnable() {
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				
				fCommandControl.queueCommand(
						fCommandFactory.createMIExecUntil(dmc, fileName + ":" + lineNo), //$NON-NLS-1$
						null);
			}
		});

		// Wait for the execution to suspend after the step
    	return eventWaitor.waitForEvent(timeout);	
	}

	public static MIStoppedEvent runToLine(final String fileName, final String lineNo, 
            final boolean skipBreakpoints) throws Throwable {
		return runToLine(fileName, lineNo, skipBreakpoints, DefaultTimeouts.get(ETimeout.runToLine));
	}

	public static MIStoppedEvent runToLine(final String fileName, final String lineNo, 
            final boolean skipBreakpoints, int timeout) throws Throwable {
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return runToLine(containerDmc, fileName, lineNo, skipBreakpoints, timeout);
	}

	public static MIStoppedEvent runToLine(final String fileName, final String lineNo) throws Throwable {
		return runToLine(fileName, lineNo, DefaultTimeouts.get(ETimeout.runToLine));
	}

	public static MIStoppedEvent runToLine(final String fileName, final String lineNo, int timeout) throws Throwable {
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return runToLine(containerDmc, fileName, lineNo, false, timeout);
	}

	public static int addBreakpoint(final String location) throws Throwable {
		return addBreakpoint(location, DefaultTimeouts.get(ETimeout.addBreakpoint));
	}

	public static int addBreakpoint(final String location, int timeout) throws Throwable {
		return addBreakpoint(location, true, timeout);
	}

	public static int addBreakpoint(final String location, boolean temporary) throws Throwable {
		return addBreakpoint(location, temporary, DefaultTimeouts.get(ETimeout.addBreakpoint));
	}
	
	public static int addBreakpoint(final String location, boolean temporary, int timeout)
							throws Throwable {

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		DataRequestMonitor<MIBreakInsertInfo> addBreakDone = 
			new DataRequestMonitor<MIBreakInsertInfo>(fRunControl.getExecutor(), null) { 
			@Override
			protected void handleCompleted() {
                if (isSuccess()) {
                    wait.setReturnInfo(getData());
                }
                
                wait.waitFinished(getStatus());
			}
		};

		fCommandControl.queueCommand(
				fCommandFactory.createMIBreakInsert(fBreakpointsDmc, temporary, false, null, 0, location, 0),
			    addBreakDone);
		
        wait.waitUntilDone(timeout);
        assertTrue(wait.getMessage(), wait.isOK());
        MIBreakInsertInfo info = (MIBreakInsertInfo) wait.getReturnInfo();
        return info.getMIBreakpoints()[0].getNumber();
	}

	
	public static int[] getBreakpointList(int timeout) throws Throwable {

		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		DataRequestMonitor<MIBreakListInfo> listDRM = 
			new DataRequestMonitor<MIBreakListInfo>(fRunControl.getExecutor(), null) { 
			@Override
			protected void handleCompleted() {
                if (isSuccess()) {
                    wait.setReturnInfo(getData());
                }               
                wait.waitFinished(getStatus());
			}
		};

		fCommandControl.queueCommand(fCommandFactory.createMIBreakList(fBreakpointsDmc), listDRM);
		
        wait.waitUntilDone(timeout);
        assertTrue(wait.getMessage(), wait.isOK());

		MIBreakpoint[] breakpoints = listDRM.getData().getMIBreakpoints();
		int[] result = new int[breakpoints.length];
		for (int i = 0; i < breakpoints.length; i++) {
			result[i] = breakpoints[i].getNumber();
		}
		return result;
	}
	
	public static void deleteBreakpoint(int breakpointIndex, int timeout) throws Throwable {
		deleteBreakpoint(new int[] {breakpointIndex}, timeout);
	}
	
	public static void deleteBreakpoint(int[] breakpointIndices, int timeout) throws Throwable {

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		DataRequestMonitor<MIInfo> deleteBreakDone = 
			new DataRequestMonitor<MIInfo>(fRunControl.getExecutor(), null) { 
			@Override
			protected void handleCompleted() {
                if (isSuccess()) {
                    wait.setReturnInfo(getData());
                }
                
                wait.waitFinished(getStatus());
			}
		};

		fCommandControl.queueCommand(
				fCommandFactory.createMIBreakDelete(fBreakpointsDmc, breakpointIndices), //$NON-NLS-1$
				deleteBreakDone);
		
        wait.waitUntilDone(timeout);
        assertTrue(wait.getMessage(), wait.isOK());
	}

	
	public static MIStoppedEvent resumeUntilStopped(final IExecutionDMContext dmc, int timeout) throws Throwable {
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
            new ServiceEventWaitor<MIStoppedEvent>(
                    fSession,
                    MIStoppedEvent.class);

		fRunControl.getExecutor().submit(new Runnable() {
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				fCommandControl.queueCommand(
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
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been resumed
				fCommandControl.queueCommand(
						fCommandFactory.createMIExecContinue(dmc),
						null);
			}
		});

		// Wait for the execution to suspend after the step
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
	
	public static MIStoppedEvent waitForStop(int timeout) throws Throwable {
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
            new ServiceEventWaitor<MIStoppedEvent>(
                    fSession,
                    MIStoppedEvent.class);

		// Wait for the execution to suspend
    	return eventWaitor.waitForEvent(timeout);			
	}
	
	public static MIStoppedEvent runToLocation(final String location) throws Throwable {
		return runToLocation(location, DefaultTimeouts.get(ETimeout.runToLocation));
	}
	
	public static MIStoppedEvent runToLocation(final String location, int timeout) throws Throwable {
		// Set a temporary breakpoint and run to it.
		// Note that if there were other breakpoints set ahead of this one,
		// they will stop execution earlier than planned
		addBreakpoint(location, true, timeout);
		return resumeUntilStopped(timeout);
	}
	
    public static IFrameDMContext getStackFrame(final IExecutionDMContext execCtx, final int level) throws Throwable {
        class StackFrameQuery extends Query<IFrameDMContext> {
            @Override
            protected void execute(final DataRequestMonitor<IFrameDMContext> rm) {
                fStack.getFrames(execCtx, new DataRequestMonitor<IFrameDMContext[]>(fSession.getExecutor(), rm) {
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
        }

        StackFrameQuery sfQuery = new StackFrameQuery();
        fSession.getExecutor().execute(sfQuery);
        return sfQuery.get();
    }

    public static IExpressionDMContext createExpression(final IDMContext parentCtx, final String expression)
        throws Throwable {
        Callable<IExpressionDMContext> callable = new Callable<IExpressionDMContext>() {
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
            public FormattedValueDMContext call() throws Exception {
                return service.getFormattedValueContext(dmc, formatId);
            }
        };
        return fSession.getExecutor().submit(callable).get();
    }
    
    public static IMIExecutionDMContext createExecutionContext(final IContainerDMContext parentCtx, final int threadId) throws Throwable {
	    Callable<IMIExecutionDMContext> callable = new Callable<IMIExecutionDMContext>() {
	        public IMIExecutionDMContext call() throws Exception {
	            return fRunControl.createMIExecutionContext(parentCtx, threadId);
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
	    				TestsPlugin.log(new Status(IStatus.ERROR, TestsPlugin.getUniqueIdentifier(), "\"" + propname + "\" property incorrectly specified. Should be an integer value or not specified at all.")); //$NON-NLS-1$
	    				value = -1;
	    			}
	    		}
	    		catch (NumberFormatException exc) {
	    			TestsPlugin.log(new Status(IStatus.ERROR, TestsPlugin.getUniqueIdentifier(), "\"" + propname + "\" property incorrectly specified. Should be an integer value or not specified at all.")); //$NON-NLS-1$
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

		final AsyncCompletionWaitor waitor = new AsyncCompletionWaitor();
		
		fProcessesService.getExecutor().submit(new Runnable() {
            public void run() {
            	fProcessesService.getProcessesBeingDebugged(
            			fCommandControl.getContext(), 
            			new DataRequestMonitor<IDMContext[]>(fProcessesService.getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                       if (isSuccess()) {
                    	   IDMContext[] contexts = getData();
                    	   Assert.assertNotNull("invalid return value from service", contexts);
                    	   Assert.assertEquals("unexpected number of processes", contexts.length, 1);
                    	   IDMContext context = contexts[0];    
                           Assert.assertNotNull("unexpected process context type ", context);
                    	   waitor.setReturnInfo(context);
                    	   waitor.waitFinished();
                        } else {
                           waitor.waitFinished(getStatus());
                        }
                    }
            		
            	});
            }
        });
		
    	waitor.waitUntilDone(TestsPlugin.massageTimeout(2000));
    	Assert.assertTrue(waitor.getMessage(), waitor.isOK());
    	return (IContainerDMContext)waitor.getReturnInfo();
	}
    
}
