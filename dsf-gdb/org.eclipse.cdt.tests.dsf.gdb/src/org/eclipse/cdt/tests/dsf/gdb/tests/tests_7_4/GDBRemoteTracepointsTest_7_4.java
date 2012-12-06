/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.CollectAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3.GDBRemoteTracepointsTest_7_3;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBRemoteTracepointsTest_7_4 extends GDBRemoteTracepointsTest_7_3 {
	
	protected IGDBTraceControl fTraceControl;
	protected ITraceTargetDMContext fTraceTargetDMC;

	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_4);		
	}

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();
	
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				fTraceControl = fServicesTracker.getService(IGDBTraceControl.class);
			}
		};
		fSession.getExecutor().submit(runnable).get();
		fTraceTargetDMC = DMContexts.getAncestorOfType(SyncUtil.getContainerContext(), ITraceTargetDMContext.class);
		Assert.assertNotNull(fTraceTargetDMC);
	}
	
	@Override
	protected boolean acceptsFastTpOnFourBytes() {
		// With GDB 7.4, fast tracepoints only need an
		// instruction of 4 bytes or more, instead of 5.
		return true;
	}
	
	/**
	 * This test sets the different types of tracepoints and then sets some string collection actions
	 */
	@Test
	public void tracepointActionsWithCollectStrings() throws Throwable {
		TracepointActionManager tracepointActionMgr = TracepointActionManager.getInstance();

		CollectAction action1 = new CollectAction();
		action1.setCollectString("/s $locals");
		action1.setName("Collect string locals");
		tracepointActionMgr.addAction(action1);
		
		CollectAction action2 = new CollectAction();
		action2.setCollectString("/s3 $locals, $reg");
		action2.setName("Collect string locals, reg");
		tracepointActionMgr.addAction(action2);
		
		createTracepoints();

		Map<String, Object> delta = new HashMap<String, Object>();
		// Set conditions for all tracepoints
		delta.put(MIBreakpoints.COMMANDS, action1.getName());
		updateBreakpoint(fTracepoints[0], delta);
		delta.put(MIBreakpoints.COMMANDS, action2.getName());
		updateBreakpoint(fTracepoints[1], delta);
		delta.put(MIBreakpoints.COMMANDS, action1.getName());
		updateBreakpoint(fTracepoints[2], delta);
		delta.put(MIBreakpoints.COMMANDS, action1.getName());
		updateBreakpoint(fTracepoints[3], delta);
		delta.put(MIBreakpoints.COMMANDS, action2.getName());
		updateBreakpoint(fTracepoints[4], delta);

		ArrayList<TracepointData> dataArray = new ArrayList<TracepointData>();
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_2, NO_CONDITION, 0, true, action1.toString(), false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_3, NO_CONDITION, 0, true, action2.toString(), false));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_4, NO_CONDITION, 0, true, action1.toString(), true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_NUMBER_1, NO_CONDITION, 0, true, action1.toString(), true));
		dataArray.add(new TracepointData(SOURCE_FILE, LINE_LOOP_2, NO_CONDITION, 0, true, action2.toString(), acceptsFastTpOnFourBytes()));

		checkTracepoints(dataArray.toArray(new TracepointData[dataArray.size()]));
	}

	@Test
	public void createTraceFile() throws Throwable {
		TracepointActionManager tam = TracepointActionManager.getInstance();
		ArrayList<ITracepointAction> actions = tam.getActions();

		createTracepointWithMultipleCommands();


		// Set a regular breakpoint at the end of file. 
		// We need to stop to store the trace data into a file.
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
		attributes.put(MIBreakpoints.FILE_NAME, SOURCE_FILE);
		attributes.put(MIBreakpoints.LINE_NUMBER, 152);
		insertBreakpoint(fBreakpointsDmc, attributes);
		waitForBreakpointEvent();
		clearEventCounters();

		startTracing();
		
		MIStoppedEvent stoppedEvent = SyncUtil.resumeUntilStopped();
		
		stopTracing();
		
		SyncUtil.resume();
	}

	private void startTracing() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		fSession.getExecutor().submit(new Runnable() {			
			@Override
			public void run() {
				fTraceControl.getTraceStatus(
					fTraceTargetDMC, 
					new DataRequestMonitor<ITraceStatusDMData>(fSession.getExecutor(), null) {
						@Override
						@ConfinedToDsfExecutor( "fExecutor" )
						protected void handleCompleted() {
							if (getData().isTracingSupported()) {
								fTraceControl.startTracing( 
										fTraceTargetDMC, 
										new RequestMonitor(fSession.getExecutor(), null) {
											@Override
											@ConfinedToDsfExecutor( "fExecutor" )
											protected void handleCompleted() {
												wait.waitFinished(getStatus());
											}
										});
							}
							else {
								wait.waitFinished(getStatus());
							}
						}
					});
			}
		});		
		wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());
	}

	private void stopTracing() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		fSession.getExecutor().submit(new Runnable() {			
			@Override
			public void run() {
				fTraceControl.stopTracing( 
					fTraceTargetDMC, 
					new RequestMonitor( fSession.getExecutor(), null ) {
						@Override
						@ConfinedToDsfExecutor( "fExecutor" )
						protected void handleCompleted() {
							wait.waitFinished(getStatus());
						}
					});
			}
		});		
		wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());
	}
}
