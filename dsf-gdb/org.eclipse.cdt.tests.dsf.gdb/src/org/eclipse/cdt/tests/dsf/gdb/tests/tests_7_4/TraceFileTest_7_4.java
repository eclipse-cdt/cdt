/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.CollectAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.EvaluateAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.ITracepointAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.MIBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.junit.After;
import org.junit.Test;

public class TraceFileTest_7_4 extends BaseTestCase {

	private final static String FILE_NAME = "TracepointTestApp.cc";
	private final static int LINE_NUMBER_1 = 97;
	private final static int LINE_NUMBER_2 = 102;
	private final static String TEVAL_STRING = "lBoolPtr2";
	private final static String COLLECT_STRING1 = "counter";
	private final static String COLLECT_STRING2 = "$regs";
	private final static String TRACE_FILE = "data/launch/bin/trace";

	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	private IBreakpoints fBreakpointService;
	private IGDBTraceControl fTraceService;
	private IBreakpointsTargetDMContext fBreakpointsDmc;
	private ITraceTargetDMContext fTraceTargetDmc;

	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_4);		
	}

    @Override
	public void doBeforeTest() throws Exception {
    	// Suppress settings of the launch attributes and launching. 
    	// Each test sets its own launch attributes
	}

	@Override
	@After
	public void doAfterTest() throws Exception {
		super.doAfterTest();
		fBreakpointService = null;
		fTraceService = null;
		fBreakpointsDmc = null;
		fTraceTargetDmc = null;
		if (fServicesTracker != null) {
			fServicesTracker.dispose();
			fServicesTracker = null;
		}
	}

	/**
	 * This test implements the following steps.
	 * 1. Starts a remote session
	 * 2. Sets two tracepoints at data/launch/src/TracepointTestApp.cc:97 
	 *    and data/launch/src/TracepointTestApp.cc:102.
     *    The first tracepoint's command is "teval lBoolPtr2".
     *    The second tracepoint's commands are "collect counter" and "collect $regs".
     * 3. Sets a regular breakpoint at the end of the source file.
	 * 4. Starts tracing
	 * 5. Resumes and runs until the breakpoint is hit
	 * 6. Stops tracing
	 * 7. Saves the trace data into a file (data/launch/bin/trace).
	 */
	@Test
	public void createTraceFile() throws Throwable {
		deleteOldTraceFile();
		startRemoteSession();
		setTracepoints();
		MIBreakpointDMContext bptDMC = setBreakpointAtEndLine();
		startTracing();
		MIStoppedEvent stoppedEvent = SyncUtil.resumeUntilStopped();
		assertTrue(stoppedEvent instanceof MIBreakpointHitEvent 
				&& ((MIBreakpointHitEvent)stoppedEvent).getNumber() == bptDMC.getReference());
		stopTracing();
		saveTraceData();
	}

    /**
     * This test removes all existing tracepoint actions and tracepoints 
     * and verifies that corresponding platform tracepoints with the proper 
     * actions are created.
     */
    @Test
    public void testTraceFile() throws Throwable {
    	// Make sure that there is no tracepoint actions and platform breakpoints 
    	// are in the workspace.
    	deleteActionsAndBreakpoints();
    	startTraceFileSession();
    	// Verify that required tracepoints and new tracepoint actions are created.
    	checkActionsAndTracepoints();
    }

    /**
     * This test verifies that the tracepoint actions and platform tracepoints 
     * created by 'testTraceFile()' are associated with the corresponding target 
     * tracepoints.
     */
    @Test
    public void testTraceFileWithExistingTracepoints() throws Throwable {
    	// Verify that actions and tracepoints required for this test are in place.
    	checkActionsAndTracepoints();
    	startTraceFileSession();
    	// Verify that no new platform tracepoints or new tracepoint actions are created.
    	checkActionsAndTracepoints();
    }

    private void startTraceFileSession() throws Throwable {
    	// Set launch attributes
    	super.setLaunchAttributes();
		// Set a working directory for GDB that is different than eclipse's directory.
		// This allows us to make sure we properly handle finding the core file,
		// especially in the case of a relative path
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "${workspace_loc}");
		// Because we just set a different working directory, we must use an absolute path for the program
    	String absoluteProgram = new Path("data/launch/bin/TracepointTestApp.exe").toFile().getAbsolutePath();
        setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, absoluteProgram);

        // Set post-mortem launch
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				           ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
		// Set post-mortem type to trace file
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_POST_MORTEM_TYPE,
		                   IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE);
		// Set core file path
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, TRACE_FILE);

		doLaunch();

    	// Allow time to create tracepoint actions
    	Thread.sleep(1000);
    }

    /**
     * Deletes all tracepoint actions and all existing platform breakpoints.
     */
    private void deleteActionsAndBreakpoints() throws Throwable {
    	TracepointActionManager tam = TracepointActionManager.getInstance();
    	IBreakpointManager bm = DebugPlugin.getDefault().getBreakpointManager();
    	
    	// Delete all existing actions
    	@SuppressWarnings( "unchecked" )
		ArrayList<ITracepointAction> actions = (ArrayList<ITracepointAction>)tam.getActions().clone();
    	for (ITracepointAction a : actions) {
    		tam.deleteAction(a);
    	}

    	IBreakpoint[] bpts = bm.getBreakpoints();
    	for (IBreakpoint b : bpts) {
    		bm.removeBreakpoint(b, true);
    	}
    }

    /**
     * Checks whether there are only two platform tracepoints and three tracepoint actions.
     */
    private void checkActionsAndTracepoints() throws Throwable {
    	TracepointActionManager tam = TracepointActionManager.getInstance();
    	IBreakpointManager bm = DebugPlugin.getDefault().getBreakpointManager();
    	ArrayList<ITracepointAction> actions = tam.getActions();
    	IBreakpoint[] bpts = bm.getBreakpoints();
    	actions = tam.getActions();
    	assertTrue(String.format("Unexpected count of tracepoint actions: %d", actions.size()), actions.size() == 3);
    	bpts = bm.getBreakpoints();
    	assertTrue(String.format("Unexpected count of breakpoints: %d", bpts.length), bpts.length == 2);
    	for (IBreakpoint b : bpts) {
    		assertTrue(b instanceof ICTracepoint);
    		checkTracepoint((ICTracepoint)b);
    	}
    }

    private void checkTracepoint(ICTracepoint tracepoint) throws Throwable {
    	TracepointActionManager tam = TracepointActionManager.getInstance();
		assertTrue(FILE_NAME.equals(new Path(tracepoint.getFileName()).lastSegment()));
		assertTrue(LINE_NUMBER_1 == tracepoint.getLineNumber() || LINE_NUMBER_2 == tracepoint.getLineNumber());
		String[] actionNames = 
			((String)tracepoint.getMarker().getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE)).split(TracepointActionManager.TRACEPOINT_ACTION_DELIMITER);
		for (String name : actionNames) { 
			ITracepointAction a = tam.findAction(name);
			assertNotNull(a);
			if (a instanceof EvaluateAction) {
				assertTrue(TEVAL_STRING.equals(((EvaluateAction)a).getEvalString()));
			}
			if (a instanceof CollectAction) {
				assertTrue(COLLECT_STRING1.equals(((CollectAction)a).getCollectString()) 
							|| COLLECT_STRING2.equals(((CollectAction)a).getCollectString()));
			}
		}
    }

    private void startRemoteSession() throws Throwable {
    	// Set launch attributes
		super.setLaunchAttributes();		
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "data/launch/bin/TracepointTestApp.exe");
		// GDB tracepoints are only supported on a remote target (e.g., using gdbserver)
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);		
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_TRACEPOINT_MODE,
				IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT);
		
		// Start the session
		doLaunch();
		
		// Initialize
		fSession = getGDBLaunch().getSession();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
				fBreakpointService = fServicesTracker.getService(IBreakpoints.class);
				fTraceService = fServicesTracker.getService(IGDBTraceControl.class);
			}
		};
		fSession.getExecutor().submit(runnable).get();

		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		fBreakpointsDmc = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);
		assertNotNull(fBreakpointsDmc);
		fTraceTargetDmc = DMContexts.getAncestorOfType(containerDmc, ITraceTargetDMContext.class);
		assertNotNull(fTraceTargetDmc);
    }

    private void startTracing() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		fSession.getExecutor().submit(new Runnable() {			
			@Override
			public void run() {
				fTraceService.getTraceStatus(
					fTraceTargetDmc, 
					new DataRequestMonitor<ITraceStatusDMData>(fSession.getExecutor(), null) {
						@Override
						@ConfinedToDsfExecutor("fExecutor")
						protected void handleCompleted() {
							if (isSuccess() && getData().isTracingSupported()) {
								fTraceService.startTracing( 
										fTraceTargetDmc, 
										new RequestMonitor(fSession.getExecutor(), null) {
											@Override
											@ConfinedToDsfExecutor("fExecutor")
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
				fTraceService.stopTracing( 
					fTraceTargetDmc, 
					new RequestMonitor(fSession.getExecutor(), null) {
						@Override
						@ConfinedToDsfExecutor("fExecutor")
						protected void handleCompleted() {
							wait.waitFinished(getStatus());
						}
					});
			}
		});		
		wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());
	}

	private MIBreakpointDMContext setBreakpointAtEndLine() throws Throwable {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
		attributes.put(MIBreakpoints.FILE_NAME, FILE_NAME);
		attributes.put(MIBreakpoints.LINE_NUMBER, 152);
		IBreakpointDMContext bptDMC = insertBreakpoint(fBreakpointsDmc, attributes);
		assertTrue(bptDMC instanceof MIBreakpointDMContext);
		return (MIBreakpointDMContext)bptDMC;
	}

	private void setTracepoints() throws Throwable {
		TracepointActionManager tam = TracepointActionManager.getInstance();
		
		CollectAction collectAction1 = new CollectAction();
		collectAction1.setCollectString(COLLECT_STRING1);
		collectAction1.setName(String.format("Collect %s", COLLECT_STRING1));
		tam.addAction(collectAction1);
		
		
		CollectAction collectAction2 = new CollectAction();
		collectAction2.setCollectString(COLLECT_STRING2);
		collectAction2.setName(String.format("Collect %s", COLLECT_STRING2));
		tam.addAction(collectAction2);
		
		EvaluateAction evalAction = new EvaluateAction();
		evalAction.setEvalString(TEVAL_STRING);
		evalAction.setName(String.format("Evaluate %s", TEVAL_STRING));
		tam.addAction(evalAction);

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
		attributes.put(MIBreakpoints.FILE_NAME, FILE_NAME);
		attributes.put(MIBreakpoints.LINE_NUMBER, LINE_NUMBER_1);
		attributes.put(MIBreakpoints.COMMANDS, evalAction.getName());
		insertBreakpoint(fBreakpointsDmc, attributes);
		
		attributes.put(MIBreakpoints.LINE_NUMBER, LINE_NUMBER_2);
		attributes.put(MIBreakpoints.COMMANDS, 
			String.format("%s%s%s", collectAction1.getName(), 
					TracepointActionManager.TRACEPOINT_ACTION_DELIMITER, collectAction2.getName()));
		insertBreakpoint(fBreakpointsDmc, attributes);
	}

	private IBreakpointDMContext insertBreakpoint(
			final IBreakpointsTargetDMContext context,
			final Map<String,Object> attributes) throws InterruptedException {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		fSession.getExecutor().submit(new Runnable() {
			@Override
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

		// Wait for the result and return the breakpoint context
		wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());

		return (IBreakpointDMContext)wait.getReturnInfo();
	}

	private void saveTraceData() throws Throwable {
    	final File traceFile = new Path(TRACE_FILE).toFile();
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		fSession.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				fTraceService.saveTraceData(
					fTraceTargetDmc, 
					traceFile.getAbsolutePath(), 
					false, 
					new RequestMonitor(fSession.getExecutor(), null) {
						@Override
						protected void handleCompleted() {
							wait.waitFinished(getStatus());
						}
					});
			}
		});

		// Wait for the result and verify the trace file is created
		wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());
		assertTrue(traceFile.exists());
	}

	private void deleteOldTraceFile() throws Throwable {
    	File traceFile = new Path(TRACE_FILE).toFile();
		traceFile.delete();
		assertFalse(traceFile.exists());
	}
}
