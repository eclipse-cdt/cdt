/*******************************************************************************
 * Copyright (c) 2007, 2018 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 437562 - Split the dsf-gdb tests to a plug-in and fragment pair
 *     Simon Marchi (Ericsson) - Make canRestart and restart throw Exception instead of Throwable.
 *     Simon Marchi (Ericsson) - Add getThreadData.
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Make tests run with different values of addressable size (Bug 460241)
 *     Jonah Graham (Kichwa Coders) - Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IDebugSourceFiles;
import org.eclipse.cdt.dsf.gdb.service.IDebugSourceFiles.IDebugSourceFileInfo;
import org.eclipse.cdt.dsf.gdb.service.IGDBMemory2;
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
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterNamesInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil.DefaultTimeouts.ETimeout;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * Timeout wait values are in milliseconds, or WAIT_FOREVER.
 */
public class SyncUtil {

	private static IGDBControl fGdbControl;
	private static IMIRunControl fRunControl;
	private static MIStack fStack;
	private static IExpressions fExpressions;
	private static DsfSession fSession;
	private static IMemory fMemory;

	private static CommandFactory fCommandFactory;
	private static IGDBProcesses fProcessesService;

	private static ISourceLookup fSourceLookup;

	private static IDebugSourceFiles fDebugSourceFiles;

	// Static list of register names as obtained directly from GDB.
	// We make it static, key'ed on each version of gdb, so it does not
	// get re-set for every test.
	// Each version of GDB can expose the set of register differently
	private static Map<String, List<String>> fRegisterNames = new HashMap<>();

	// Initialize some common things, once the session has been established
	public static void initialize(DsfSession session) throws Exception {
		fSession = session;

		Runnable runnable = () -> {
			DsfServicesTracker tracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

			fGdbControl = tracker.getService(IGDBControl.class);
			fRunControl = tracker.getService(IMIRunControl.class);
			fStack = tracker.getService(MIStack.class);
			fExpressions = tracker.getService(IExpressions.class);
			fProcessesService = tracker.getService(IGDBProcesses.class);
			fMemory = tracker.getService(IMemory.class);
			fCommandFactory = fGdbControl.getCommandFactory();
			fSourceLookup = tracker.getService(ISourceLookup.class);
			fDebugSourceFiles = tracker.getService(IDebugSourceFiles.class);
			tracker.dispose();
		};
		fSession.getExecutor().submit(runnable).get();
	}

	public static MIStoppedEvent step(int numSteps, StepType stepType) throws Throwable {
		return step(numSteps, stepType, false);
	}

	public static MIStoppedEvent step(int numSteps, StepType stepType, boolean reverse) throws Throwable {
		MIStoppedEvent retVal = null;
		for (int i = 0; i < numSteps; i++) {
			retVal = step(stepType, reverse, DefaultTimeouts.get(ETimeout.step));
		}
		return retVal;
	}

	public static MIStoppedEvent step(StepType stepType) throws Throwable {
		return step(stepType, false, DefaultTimeouts.get(ETimeout.step));
	}

	public static MIStoppedEvent step(StepType stepType, boolean reverse, int massagedTimeout) throws Throwable {
		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return step(containerDmc, stepType, reverse, massagedTimeout);
	}

	public static MIStoppedEvent step(IExecutionDMContext dmc, StepType stepType) throws Throwable {
		return step(dmc, stepType, DefaultTimeouts.get(ETimeout.step));
	}

	public static MIStoppedEvent step(final IExecutionDMContext dmc, final StepType stepType, int massagedTimeout)
			throws Throwable {
		return step(dmc, stepType, false, massagedTimeout);
	}

	public static MIStoppedEvent step(final IExecutionDMContext dmc, final StepType stepType, boolean reverse,
			int massagedTimeout) throws Throwable {
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fSession, MIStoppedEvent.class);

		if (!reverse) {
			fRunControl.getExecutor().submit(() -> {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				switch (stepType) {
				case STEP_INTO:
					fGdbControl.queueCommand(fCommandFactory.createMIExecStep(dmc), null);
					break;
				case STEP_OVER:
					fGdbControl.queueCommand(fCommandFactory.createMIExecNext(dmc), null);
					break;
				case STEP_RETURN:
					fGdbControl.queueCommand(fCommandFactory.createMIExecFinish(fStack.createFrameDMContext(dmc, 0)),
							null);
					break;
				default:
					fail("Unsupported step type; " + stepType.toString());
				}
			});
		} else {
			fRunControl.getExecutor().submit(() -> {
				// No need for a RequestMonitor since we will wait for the
				// ServiceEvent telling us the program has been suspended again
				switch (stepType) {
				case STEP_INTO:
					fGdbControl.queueCommand(fCommandFactory.createMIExecReverseStep(dmc), null);
					break;
				case STEP_OVER:
					fGdbControl.queueCommand(fCommandFactory.createMIExecReverseNext(dmc), null);
					break;
				case STEP_RETURN:
					fGdbControl.queueCommand(fCommandFactory.createMIExecUncall(fStack.createFrameDMContext(dmc, 0)),
							null);
					break;
				default:
					fail("Unsupported step type; " + stepType.toString());
				}
			});
		}

		// Wait for the execution to suspend after the step
		return eventWaitor.waitForEvent(massagedTimeout);
	}

	public static String addBreakpoint(String location) throws Throwable {
		return addBreakpoint(location, DefaultTimeouts.get(ETimeout.addBreakpoint));
	}

	public static String addBreakpoint(String location, int massagedTimeout) throws Throwable {
		return addBreakpoint(location, true, massagedTimeout);
	}

	public static String addBreakpoint(String location, boolean temporary) throws Throwable {
		return addBreakpoint(location, temporary, DefaultTimeouts.get(ETimeout.addBreakpoint));
	}

	private static String addBreakpoint(final String location, final boolean temporary, int massagedTimeout)
			throws Throwable {

		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(containerDmc,
				IBreakpointsTargetDMContext.class);

		Query<MIBreakInsertInfo> query = new Query<MIBreakInsertInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIBreakInsertInfo> rm) {
				fGdbControl.queueCommand(
						fCommandFactory.createMIBreakInsert(bpTargetDmc, temporary, false, null, 0, location, "0"), rm);
			}
		};

		fGdbControl.getExecutor().execute(query);
		MIBreakInsertInfo info = query.get(massagedTimeout, TimeUnit.MILLISECONDS);
		return info.getMIBreakpoints()[0].getNumber();
	}

	public static String[] getBreakpointList(int timeout) throws Throwable {
		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		final IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(containerDmc,
				IBreakpointsTargetDMContext.class);

		Query<MIBreakListInfo> query = new Query<MIBreakListInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIBreakListInfo> rm) {
				fGdbControl.queueCommand(fCommandFactory.createMIBreakList(bpTargetDmc), rm);
			}
		};

		fGdbControl.getExecutor().execute(query);
		MIBreakListInfo info = query.get(TestsPlugin.massageTimeout(timeout), TimeUnit.MILLISECONDS);
		MIBreakpoint[] breakpoints = info.getMIBreakpoints();

		String[] result = new String[breakpoints.length];
		for (int i = 0; i < breakpoints.length; i++) {
			result[i] = breakpoints[i].getNumber();
		}
		return result;
	}

	private static MIStoppedEvent resumeUntilStopped(final IExecutionDMContext dmc, int massagedTimeout)
			throws Throwable {
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fSession, MIStoppedEvent.class);

		// No need for a RequestMonitor since we will wait for the ServiceEvent telling us the program has been suspended again
		fRunControl.getExecutor()
				.submit(() -> fGdbControl.queueCommand(fCommandFactory.createMIExecContinue(dmc), null));

		// Wait for the execution to suspend after the step
		return eventWaitor.waitForEvent(massagedTimeout);
	}

	public static MIStoppedEvent resumeUntilStopped() throws Throwable {
		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		// Don't call resumeUtilStopped(int timeout) as this will duplicate the timeout massage
		return resumeUntilStopped(containerDmc, DefaultTimeouts.get(ETimeout.resumeUntilStopped));
	}

	public static MIStoppedEvent resumeUntilStopped(int timeout) throws Throwable {
		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return resumeUntilStopped(containerDmc, TestsPlugin.massageTimeout(timeout));
	}

	public static MIRunningEvent resume(final IExecutionDMContext dmc, int massagedTimeout) throws Throwable {
		final ServiceEventWaitor<MIRunningEvent> eventWaitor = new ServiceEventWaitor<>(fSession, MIRunningEvent.class);

		// No need for a RequestMonitor since we will wait for the ServiceEvent telling us the program has been suspended again
		fRunControl.getExecutor()
				.submit(() -> fGdbControl.queueCommand(fCommandFactory.createMIExecContinue(dmc), null));

		// Wait for the execution to start after the step
		return eventWaitor.waitForEvent(massagedTimeout);
	}

	public static boolean canResume(final IExecutionDMContext execDmc) throws Throwable {
		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				fRunControl.canResume(execDmc, new ImmediateDataRequestMonitor<Boolean>(rm) {
					@Override
					protected void handleSuccess() {
						rm.done(getData());
					}
				});
			}
		};

		fRunControl.getExecutor().execute(query);
		boolean canResume = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		return canResume;
	}

	public static MIRunningEvent resume() throws Throwable {
		return resume(DefaultTimeouts.get(ETimeout.resume));
	}

	public static MIRunningEvent resume(int massagedTimeout) throws Throwable {
		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		return resume(containerDmc, massagedTimeout);
	}

	public static void resumeAll() throws Throwable {
		resumeAll(DefaultTimeouts.get(ETimeout.resume));
	}

	public static void resumeAll(int massagedTimeout) throws Throwable {
		IMIExecutionDMContext[] threadDmcs = SyncUtil.getExecutionContexts();
		for (IMIExecutionDMContext thread : threadDmcs) {
			if (canResume(thread)) {
				resume(thread, massagedTimeout);
			}
		}
	}

	public static MIStoppedEvent waitForStop() throws Throwable {
		// Use a direct value to avoid double call to TestsPlugin.massageTimeout
		return waitForStop(10000);
	}

	// This method is risky.  If the command to resume/step execution
	// is sent and the stopped event is received before we call this method
	// here, then we will miss the stopped event.
	// Normally, one should initialize the ServiveEventWaitor before
	// triggering the resume to make sure not to miss the stopped event.
	// However, in some case this method will still work, for instance
	// if there is a sleep in the code between the resume and the time
	// it stops; this will give us plenty of time to call this method.
	public static MIStoppedEvent waitForStop(int timeout) throws Throwable {
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fSession, MIStoppedEvent.class);

		// Wait for the execution to suspend
		return eventWaitor.waitForEvent(TestsPlugin.massageTimeout(timeout));
	}

	public static MIStoppedEvent runToLocation(String location) throws Throwable {
		return runToLocation(location, DefaultTimeouts.get(ETimeout.runToLocation));
	}

	public static MIStoppedEvent runToLocation(String location, int timeout) throws Throwable {
		// Set a temporary breakpoint and run to it.
		// Note that if there were other breakpoints set ahead of this one,
		// they will stop execution earlier than planned
		addBreakpoint(location, true, timeout);
		// Don't provide a timeout so we use the resume default timeout for this step
		// if a timeout value is provided via DefaultTimeouts the value will be massaged twice
		return resumeUntilStopped();
	}

	public static IFrameDMContext getStackFrame(final IExecutionDMContext execCtx, final int level) throws Exception {
		Query<IFrameDMContext> query = new Query<IFrameDMContext>() {
			@Override
			protected void execute(final DataRequestMonitor<IFrameDMContext> rm) {
				fStack.getFrames(execCtx, level, level, new ImmediateDataRequestMonitor<IFrameDMContext[]>(rm) {
					@Override
					protected void handleSuccess() {
						IFrameDMContext[] frameDmcs = getData();
						assert frameDmcs != null;
						assert frameDmcs.length == 1;
						rm.setData(frameDmcs[0]);
						rm.done();
					}
				});
			}
		};

		fSession.getExecutor().execute(query);
		return query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	}

	/**
	 * Utility method to return a specific frame DM context.
	 */
	@ThreadSafeAndProhibitedFromDsfExecutor("fSession.getExecutor()")
	public static IFrameDMContext getStackFrame(int threadIndex, final int level) throws Exception {
		return getStackFrame(getExecutionContext(threadIndex), level);
	}

	public static Integer getStackDepth(final IExecutionDMContext execCtx) throws Throwable {
		return getStackDepth(execCtx, 0);
	}

	public static Integer getStackDepth(final IExecutionDMContext execCtx, final int maxDepth) throws Throwable {
		Query<Integer> query = new Query<Integer>() {
			@Override
			protected void execute(final DataRequestMonitor<Integer> rm) {
				fStack.getStackDepth(execCtx, maxDepth, rm);
			}
		};

		fSession.getExecutor().execute(query);
		return query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
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
		return query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	}

	public static IFrameDMData getFrameData(final int threadId, final int level) throws Throwable {
		return getFrameData(getExecutionContext(threadId), level);
	}

	public static IThreadDMData getThreadData(final int threadId)
			throws InterruptedException, ExecutionException, TimeoutException {
		final IProcessDMContext processContext = DMContexts.getAncestorOfType(SyncUtil.getContainerContext(),
				IProcessDMContext.class);

		Query<IThreadDMData> query = new Query<IThreadDMData>() {
			@Override
			protected void execute(DataRequestMonitor<IThreadDMData> rm) {
				IThreadDMContext threadDmc = fProcessesService.createThreadContext(processContext,
						Integer.toString(threadId));
				fProcessesService.getExecutionData(threadDmc, rm);

			}
		};

		fSession.getExecutor().execute(query);
		return query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	}

	public static IExpressionDMContext createExpression(final IDMContext parentCtx, final String expression)
			throws Throwable {
		Callable<IExpressionDMContext> callable = () -> fExpressions.createExpression(parentCtx, expression);
		return fSession.getExecutor().submit(callable).get();
	}

	public static IExpressionDMContext[] getSubExpressions(final IExpressionDMContext dmc)
			throws InterruptedException, ExecutionException {
		Query<IExpressionDMContext[]> query = new Query<IExpressionDMContext[]>() {
			@Override
			protected void execute(DataRequestMonitor<IExpressionDMContext[]> rm) {
				fExpressions.getSubExpressions(dmc, rm);
			}
		};

		fSession.getExecutor().execute(query);
		return query.get();
	}

	/*
	 * Like getSubExpressions, but for cases where we know there will be only
	 * one child.
	 */
	public static IExpressionDMContext getSubExpression(final IExpressionDMContext dmc)
			throws InterruptedException, ExecutionException {
		IExpressionDMContext[] subExpressions = SyncUtil.getSubExpressions(dmc);

		assertEquals(1, subExpressions.length);

		return subExpressions[0];
	}

	public static String getExpressionValue(final IExpressionDMContext exprDmc, final String format) throws Throwable {
		Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				FormattedValueDMContext valueDmc = fExpressions.getFormattedValueContext(exprDmc, format);
				fExpressions.getFormattedExpressionValue(valueDmc,
						new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
							@Override
							protected void handleSuccess() {
								rm.done(getData().getFormattedValue());
							}
						});
			}
		};

		fSession.getExecutor().execute(query);
		return query.get();
	}

	public static FormattedValueDMContext getFormattedValue(final IFormattedValues service,
			final IFormattedDataDMContext dmc, final String formatId) throws Throwable {
		Callable<FormattedValueDMContext> callable = () -> service.getFormattedValueContext(dmc, formatId);
		return fSession.getExecutor().submit(callable).get();
	}

	public static IMIExecutionDMContext createExecutionContext(final IContainerDMContext parentCtx, final int threadId)
			throws Throwable {
		Callable<IMIExecutionDMContext> callable = () -> {
			String threadIdStr = Integer.toString(threadId);
			IProcessDMContext processDmc = DMContexts.getAncestorOfType(parentCtx, IProcessDMContext.class);
			IThreadDMContext threadDmc = fProcessesService.createThreadContext(processDmc, threadIdStr);
			return fProcessesService.createExecutionContext(parentCtx, threadDmc, threadIdStr);
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
			addBreakpoint, deleteBreakpoint, getBreakpointList, createExecutionContext, createExpression,
			getFormattedValue, getStackFrame, resume, resumeUntilStopped, runToLine, runToLocation, step, waitForStop
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
		private static Map<ETimeout, Integer> sTimeouts = new HashMap<>();
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
			sTimeouts.put(ETimeout.runToLine, 10000); // 10 seconds
			sTimeouts.put(ETimeout.runToLocation, 10000); // 10 seconds
			sTimeouts.put(ETimeout.step, 1000);
			sTimeouts.put(ETimeout.waitForStop, 10000); // 10 seconds
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
						TestsPlugin.log(new Status(IStatus.ERROR, TestsPlugin.getUniqueIdentifier(), "\"" + propname //$NON-NLS-1$
								+ "\" property incorrectly specified. Should be an integer value or not specified at all."));
						value = -1;
					}
				} catch (NumberFormatException exc) {
					TestsPlugin.log(new Status(IStatus.ERROR, TestsPlugin.getUniqueIdentifier(), "\"" + propname //$NON-NLS-1$
							+ "\" property incorrectly specified. Should be an integer value or not specified at all."));
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
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@ThreadSafeAndProhibitedFromDsfExecutor("fSession.getExecutor()")
	public static IContainerDMContext getContainerContext()
			throws InterruptedException, ExecutionException, TimeoutException {
		assert !fProcessesService.getExecutor().isInExecutorThread();

		Query<IContainerDMContext> query = new Query<IContainerDMContext>() {
			@Override
			protected void execute(final DataRequestMonitor<IContainerDMContext> rm) {
				fProcessesService.getProcessesBeingDebugged(fGdbControl.getContext(),
						new ImmediateDataRequestMonitor<IDMContext[]>() {
							@Override
							protected void handleCompleted() {
								if (isSuccess()) {
									IDMContext[] contexts = getData();
									assertNotNull("invalid return value from service", contexts);
									assertEquals("unexpected number of processes", 1, contexts.length);
									IDMContext context = contexts[0];
									assertNotNull("unexpected process context type ", context);
									rm.done((IContainerDMContext) context);
								} else {
									rm.done(getStatus());
								}
							}
						});
			}
		};

		fGdbControl.getExecutor().execute(query);
		return query.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
	}

	/**
	 * Utility method to return all thread execution contexts.
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@ThreadSafeAndProhibitedFromDsfExecutor("fSession.getExecutor()")
	public static IMIExecutionDMContext[] getExecutionContexts()
			throws InterruptedException, ExecutionException, TimeoutException {
		assert !fProcessesService.getExecutor().isInExecutorThread();

		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		Query<IMIExecutionDMContext[]> query = new Query<IMIExecutionDMContext[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IMIExecutionDMContext[]> rm) {
				fProcessesService.getProcessesBeingDebugged(containerDmc,
						new ImmediateDataRequestMonitor<IDMContext[]>() {
							@Override
							protected void handleCompleted() {
								if (isSuccess()) {
									IDMContext[] threads = getData();
									assertNotNull("invalid return value from service", threads);
									rm.setData((IMIExecutionDMContext[]) threads);
								} else {
									rm.setStatus(getStatus());
								}
								rm.done();
							}
						});
			}
		};

		fGdbControl.getExecutor().execute(query);
		return query.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
	}

	/**
	 * Utility method to return a specific execution DM context.
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@ThreadSafeAndProhibitedFromDsfExecutor("fSession.getExecutor()")
	public static IMIExecutionDMContext getExecutionContext(int threadIndex)
			throws InterruptedException, ExecutionException, TimeoutException {
		IMIExecutionDMContext[] threads = getExecutionContexts();
		assertTrue("unexpected number of threads", threadIndex < threads.length);
		assertNotNull("unexpected thread context type ", threads[threadIndex]);
		return threads[threadIndex];
	}

	/**
	 * Check if the restart operation is supported
	 */
	public static boolean canRestart() throws Exception {
		final IContainerDMContext containerDmc = getContainerContext();

		// Check if restart is allowed
		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				fProcessesService.canRestart(containerDmc, new ImmediateDataRequestMonitor<Boolean>(rm) {
					@Override
					protected void handleSuccess() {
						rm.setData(getData());
						rm.done();
					}
				});

			}
		};

		fGdbControl.getExecutor().execute(query);
		boolean canRestart = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		return canRestart;
	}

	/**
	 * Restart the program.
	 */
	public static MIStoppedEvent restart(final GdbLaunch launch) throws Exception {
		final IContainerDMContext containerDmc = getContainerContext();

		// If we are calling this method, the restart operation should be allowed
		if (!canRestart()) {
			throw new CoreException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, "Unable to restart"));
		}

		// Now wait for the stopped event of the restart
		final ServiceEventWaitor<MIStoppedEvent> eventWaitor = new ServiceEventWaitor<>(fSession, MIStoppedEvent.class);

		// Perform the restart
		Query<IContainerDMContext> query2 = new Query<IContainerDMContext>() {
			@Override
			protected void execute(final DataRequestMonitor<IContainerDMContext> rm) {
				Map<String, Object> attributes = null;
				try {
					attributes = launch.getLaunchConfiguration().getAttributes();
				} catch (CoreException e) {
				}

				fProcessesService.restart(containerDmc, attributes, rm);
			}
		};

		fGdbControl.getExecutor().execute(query2);
		query2.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);

		MIStoppedEvent event = eventWaitor.waitForEvent(DefaultTimeouts.get(ETimeout.waitForStop));
		if (event instanceof MISignalEvent) {
			// This is not the stopped event we were waiting for.  Get the next one.
			event = eventWaitor.waitForEvent(DefaultTimeouts.get(ETimeout.waitForStop));
		}
		return event;
	}

	public static IVariableDMData[] getLocals(final IFrameDMContext frameDmc) throws Throwable {
		Query<IVariableDMData[]> query = new Query<IVariableDMData[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IVariableDMData[]> rm) {
				fStack.getLocals(frameDmc, new ImmediateDataRequestMonitor<IVariableDMContext[]>() {
					@Override
					protected void handleCompleted() {
						if (isSuccess()) {
							IVariableDMContext[] varDmcs = getData();
							final List<IVariableDMData> localsDMData = new ArrayList<>();
							final CountingRequestMonitor crm = new CountingRequestMonitor(
									ImmediateExecutor.getInstance(), rm) {
								@Override
								protected void handleSuccess() {
									rm.done(localsDMData.toArray(new IVariableDMData[localsDMData.size()]));
								}
							};

							for (IVariableDMContext varDmc : varDmcs) {
								fStack.getVariableData(varDmc, new ImmediateDataRequestMonitor<IVariableDMData>(crm) {
									@Override
									public void handleSuccess() {
										localsDMData.add(getData());
										crm.done();
									}
								});
							}
							crm.setDoneCount(varDmcs.length);
						} else {
							rm.done();
						}
					}
				});
			}
		};

		fSession.getExecutor().execute(query);
		IVariableDMData[] result = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		return result;
	}

	/**
	 * Get the registers directly from GDB (without using the registers service)
	 * @param gdbVersion
	 * @param context
	 * @return
	 * @throws Throwable
	 */
	public static List<String> getRegistersFromGdb(String gdbVersion, IDMContext context) throws Throwable {
		if (!fRegisterNames.containsKey(gdbVersion)) {
			// The tests must run on different machines, so the set of registers can change.
			// To deal with this we ask GDB for the list of registers.
			// Note that we send an MI Command in this code and do not use the IRegister service;
			// this is because we want to test the service later, comparing it to what we find
			// by asking GDB directly.
			Query<MIDataListRegisterNamesInfo> query = new Query<MIDataListRegisterNamesInfo>() {
				@Override
				protected void execute(DataRequestMonitor<MIDataListRegisterNamesInfo> rm) {
					IContainerDMContext containerDmc = DMContexts.getAncestorOfType(context, IContainerDMContext.class);
					fGdbControl.queueCommand(
							fGdbControl.getCommandFactory().createMIDataListRegisterNames(containerDmc), rm);
				}
			};
			fSession.getExecutor().execute(query);

			MIDataListRegisterNamesInfo data = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
			String[] names = data.getRegisterNames();

			// Remove registers with empty names since the service also
			// remove them. I don't know why GDB returns such empty names.
			List<String> registerNames = new LinkedList<>();
			for (String name : names) {
				if (!name.isEmpty()) {
					registerNames.add(name);
				}
			}
			assertNotEquals(
					"Test does not make sense, and has probably completely failed, as there are no register names",
					Collections.emptyList(), registerNames);
			fRegisterNames.put(gdbVersion, registerNames);
		}
		return fRegisterNames.get(gdbVersion);
	}

	/**
	 * Read data from memory.
	 *
	 * @param dmc		the data model context
	 * @param address	the memory block address
	 * @param offset	the offset in the buffer
	 * @param wordSize	the size of a word, in octets
	 * @param count		the number of bytes to read
	 * @return			the memory content
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static MemoryByte[] readMemory(final IMemoryDMContext dmc, final IAddress address, final long offset,
			final int wordSize, final int count) throws InterruptedException, ExecutionException {
		Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(DataRequestMonitor<MemoryByte[]> rm) {
				fMemory.getMemory(dmc, address, offset, wordSize, count, rm);
			}
		};

		fMemory.getExecutor().execute(query);

		return query.get();
	}

	/**
	 * Write data to memory.
	 *
	 * @param dmc		the data model context
	 * @param address	the memory block address (could be an expression)
	 * @param offset	the offset from address
	 * @param wordSize	the word size, in octets
	 * @param count		the number of bytes to write
	 * @param buffer	the byte buffer to write from
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void writeMemory(final IMemoryDMContext dmc, final IAddress address, final long offset,
			final int wordSize, final int count, final byte[] buffer) throws InterruptedException, ExecutionException {
		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(DataRequestMonitor<Void> rm) {
				fMemory.setMemory(dmc, address, offset, wordSize, count, buffer, rm);
			}
		};

		fMemory.getExecutor().execute(query);

		query.get();
	}

	/**
	 * Fill memory with a pattern.
	 *
	 * @param dmc		the data model context
	 * @param address	the memory block address (could be an expression)
	 * @param offset	the offset from address
	 * @param wordSize	the word size, in octets
	 * @param count		the number of times the pattern is to be written
	 * @param pattern	the pattern to write
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void fillMemory(final IMemoryDMContext dmc, final IAddress address, final long offset,
			final int wordSize, final int count, final byte[] pattern) throws InterruptedException, ExecutionException {
		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(DataRequestMonitor<Void> rm) {
				fMemory.fillMemory(dmc, address, offset, wordSize, count, pattern, rm);
			}
		};

		fMemory.getExecutor().execute(query);

		query.get();
	}

	/**
	 * Get the addressable size of a memory context, in octets. The addressable
	 * size is the number of octets in each memory "cell".
	 *
	 * @param dmc
	 *            the memory data model context
	 * @return the addressable size, in octets.
	 */
	public static int readAddressableSize(final IMemoryDMContext dmc) {
		assert (fMemory instanceof IGDBMemory2);
		final IGDBMemory2 memoryService = (IGDBMemory2) fMemory;

		return memoryService.getAddressableSize(dmc);
	}

	/**
	 * Get the byte order of a memory context.
	 *
	 * @param dmc
	 *            the memory data model context
	 * @return the byte order
	 */
	public static ByteOrder getMemoryByteOrder(final IMemoryDMContext dmc) {
		assert (fMemory instanceof IGDBMemory2);
		final IGDBMemory2 memoryService = (IGDBMemory2) fMemory;

		return memoryService.isBigEndian(dmc) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
	}

	/**
	 * Get the source using the {@link ISourceLookup} service.
	 *
	 * Wrapper around
	 * {@link ISourceLookup#getSource(ISourceLookupDMContext, String, DataRequestMonitor)}
	 */
	public static Object getSource(final String debuggerPath) throws Exception {
		Query<Object> query = new Query<Object>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				final ISourceLookupDMContext ctx = DMContexts.getAncestorOfType(fGdbControl.getContext(),
						ISourceLookupDMContext.class);
				fSourceLookup.getSource(ctx, debuggerPath, rm);
			}
		};

		fSourceLookup.getExecutor().execute(query);

		return query.get();
	}

	/**
	 * Get the sources from the debugger.
	 *
	 * Wrapper around
	 * {@link IDebugSourceFiles#getSources(IDMContext, DataRequestMonitor)}
	 */
	public static IDebugSourceFileInfo[] getSources(IDMContext ctx) throws Exception {
		Query<IDebugSourceFileInfo[]> query = new Query<IDebugSourceFileInfo[]>() {
			@Override
			protected void execute(DataRequestMonitor<IDebugSourceFileInfo[]> rm) {
				fDebugSourceFiles.getSources(ctx, rm);
			}
		};
		fDebugSourceFiles.getExecutor().execute(query);
		return query.get();
	}
}
