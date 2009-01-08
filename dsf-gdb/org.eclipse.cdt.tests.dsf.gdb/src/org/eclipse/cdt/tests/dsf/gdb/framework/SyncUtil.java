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
package org.eclipse.cdt.tests.dsf.gdb.framework;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIStack;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakDelete;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakInsert;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakList;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecContinue;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecFinish;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecNext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecStep;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIExecUntil;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class SyncUtil {
    
    private static ICommandControlService fCommandControl;
    private static MIRunControl fRunControl;
    private static MIStack fStack;
    private static IExpressions fExpressions;
    private static DsfSession fSession;
	
    private static IContainerDMContext fGdbContainerDmc;
    private static IBreakpointsTargetDMContext fBreakpointsDmc;
    
    // Initialize some common things, once the session has been established
    public static void initialize(DsfSession session) {
    	fSession = session;
    	
    	DsfServicesTracker tracker = 
    		new DsfServicesTracker(TestsPlugin.getBundleContext(), 
    				fSession.getId());
    	
    	fCommandControl = tracker.getService(ICommandControlService.class);
    	IMIProcesses procService = tracker.getService(IMIProcesses.class);
   		IProcessDMContext procDmc = procService.createProcessContext(fCommandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID);
   		fGdbContainerDmc = procService.createContainerContext(procDmc, MIProcesses.UNIQUE_GROUP_ID);

   		fBreakpointsDmc = (IBreakpointsTargetDMContext)fCommandControl.getContext();
   		
		fRunControl = tracker.getService(MIRunControl.class);
		fStack = tracker.getService(MIStack.class);
		fExpressions = tracker.getService(IExpressions.class);
		
		tracker.dispose();
	}

	public static MIStoppedEvent SyncStep(final StepType stepType, int numSteps) throws Throwable {
	    MIStoppedEvent retVal = null;
		for (int i=0; i<numSteps; i++) {
		    retVal = SyncStep(stepType);
		}
		return retVal;
	}

	public static MIStoppedEvent SyncStep(final StepType stepType) throws Throwable {
		return SyncStep(fGdbContainerDmc, stepType);
	}
		
	public static MIStoppedEvent SyncStep(final IExecutionDMContext dmc, final StepType stepType) throws Throwable {
		
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
					fCommandControl.queueCommand(new MIExecStep(dmc), null);
					break;
				case STEP_OVER:
					fCommandControl.queueCommand(new MIExecNext(dmc), null);
					break;
				case STEP_RETURN:
					fCommandControl.queueCommand(new MIExecFinish(fStack.createFrameDMContext(dmc, 0)), null);
					break;
				default:
					Assert.assertTrue("Unsupported step type; " + stepType.toString(), false);
				}
			}
		});

		// Wait for the execution to suspend after the step
		return eventWaitor.waitForEvent(ServiceEventWaitor.WAIT_FOREVER);
	}
	
	public static MIStoppedEvent SyncRunToLine(final IExecutionDMContext dmc, final String fileName, final String lineNo, 
			                         final boolean skipBreakpoints) throws Throwable {
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
            new ServiceEventWaitor<MIStoppedEvent>(
                    fSession,
                    MIStoppedEvent.class);
		
		fRunControl.getExecutor().submit(new Runnable() {
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				
				fCommandControl.queueCommand(
						new MIExecUntil(dmc, fileName + ":" + lineNo), //$NON-NLS-1$
						null);
			}
		});

		// Wait for the execution to suspend after the step
    	return eventWaitor.waitForEvent(ServiceEventWaitor.WAIT_FOREVER);	
	}

	public static MIStoppedEvent SyncRunToLine(final String fileName, final String lineNo, 
            final boolean skipBreakpoints) throws Throwable {
		return SyncRunToLine(fGdbContainerDmc, fileName, lineNo, skipBreakpoints);
	}
	
	public static MIStoppedEvent SyncRunToLine(final String fileName, final String lineNo) throws Throwable {
		return SyncRunToLine(fGdbContainerDmc, fileName, lineNo, false);
	}

	
	public static int SyncAddBreakpoint(final String location) throws Throwable {
		return SyncAddBreakpoint(location, true);
	}

	public static int SyncAddBreakpoint(final String location, boolean temporary)
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
				new MIBreakInsert(fBreakpointsDmc, temporary, false, null, 0, location, 0),
			    addBreakDone);
		
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        MIBreakInsertInfo info = (MIBreakInsertInfo) wait.getReturnInfo();
        return info.getMIBreakpoints()[0].getNumber();
	}

	
	public static int[] SyncGetBreakpointList() throws Throwable {

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

		fCommandControl.queueCommand(new MIBreakList(fBreakpointsDmc), listDRM);
		
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

		MIBreakpoint[] breakpoints = listDRM.getData().getMIBreakpoints();
		int[] result = new int[breakpoints.length];
		for (int i = 0; i < breakpoints.length; i++) {
			result[i] = breakpoints[i].getNumber();
		}
		return result;
	}
	
	public static void SyncDeleteBreakpoint(int breakpointIndex) throws Throwable {
		SyncDeleteBreakpoint(new int[] {breakpointIndex});
	}
	
	public static void SyncDeleteBreakpoint(int[] breakpointIndices) throws Throwable {

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
				new MIBreakDelete(fBreakpointsDmc, breakpointIndices), //$NON-NLS-1$
				deleteBreakDone);
		
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
	}

	
	public static MIStoppedEvent SyncResumeUntilStopped(final IExecutionDMContext dmc) throws Throwable {
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
            new ServiceEventWaitor<MIStoppedEvent>(
                    fSession,
                    MIStoppedEvent.class);

		fRunControl.getExecutor().submit(new Runnable() {
			public void run() {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				fCommandControl.queueCommand(
						new MIExecContinue(dmc),
						null);
			}
		});

		// Wait for the execution to suspend after the step
    	return eventWaitor.waitForEvent(ServiceEventWaitor.WAIT_FOREVER);			
	}
	
	public static MIStoppedEvent SyncResumeUntilStopped() throws Throwable {
		return SyncResumeUntilStopped(fGdbContainerDmc);
	}

	public static MIStoppedEvent SyncRunToLocation(final String location) throws Throwable {
		// Set a temporary breakpoint and run to it.
		// Note that if there were other breakpoints set ahead of this one,
		// they will stop execution earlier than planned
		SyncAddBreakpoint(location, true);
		return SyncResumeUntilStopped();
	}
	
    public static IFrameDMContext SyncGetStackFrame(final IExecutionDMContext execCtx, final int level) throws Throwable {
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

    public static IExpressionDMContext SyncCreateExpression(final IDMContext parentCtx, final String expression)
        throws Throwable {
        Callable<IExpressionDMContext> callable = new Callable<IExpressionDMContext>() {
            public IExpressionDMContext call() throws Exception {
                return fExpressions.createExpression(parentCtx, expression);
            }
        };
        return fSession.getExecutor().submit(callable).get();
    }

    public static FormattedValueDMContext SyncGetFormattedValue(
        final IFormattedValues service, final IFormattedDataDMContext dmc, final String formatId) throws Throwable 
    {
        Callable<FormattedValueDMContext> callable = new Callable<FormattedValueDMContext>() {
            public FormattedValueDMContext call() throws Exception {
                return service.getFormattedValueContext(dmc, formatId);
            }
        };
        return fSession.getExecutor().submit(callable).get();
    }
    
    public static IMIExecutionDMContext SyncCreateExecutionContext(final IContainerDMContext parentCtx, final int threadId)
    throws Throwable {
    Callable<IMIExecutionDMContext> callable = new Callable<IMIExecutionDMContext>() {
        public IMIExecutionDMContext call() throws Exception {
            return fRunControl.createMIExecutionContext(parentCtx, threadId);
        }
    };
    return fSession.getExecutor().submit(callable).get();
}

}
