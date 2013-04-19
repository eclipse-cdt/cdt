/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.naming.OperationNotSupportedException;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl3;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MILocationReachedEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.internal.core.model.FunctionDeclaration;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests Non Stop GDB RunControl "Step into Selection feature"
 * 
 */
@SuppressWarnings("restriction")
@RunWith(BackgroundRunner.class)
public class StepIntoSelectionTest extends BaseTestCase {

	private DsfServicesTracker fServicesTracker;

	private IGDBControl fGDBCtrl;
	private IRunControl3 fRunCtrl;

	private IContainerDMContext fContainerDmc;
	private IExecutionDMContext fThreadExecDmc;

	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";

	/*
	 * Name of the executable
	 */
	private static final String BIN_COMPOSITE = "Composite.exe";

	// Composite Locations
	private static final String SRC_COMPOSITE = "Composite.cc";
	private static final int COMPOSITE_GETARTIFACTSIZE_LINE_1 = 97;
	private static final int COMPOSITE_GETARTIFACT_LINE_1 = 101;
	private static final int COMPOSITE_MAIN_LINE_M1 = 190;
	private static final int COMPOSITE_MAIN_LINE_M2 = 191;
	private static final int COMPOSITE_MAIN_LINE_L1 = 192;
	private static final int COMPOSITE_MAIN_LINE_L2 = 197;
	private static final int COMPOSITE_MAIN_LINE_L3 = 201;
	private static final int COMPOSITE_MAIN_LINE_L4 = 204;
	private static final int COMPOSITE_TOSTRING_LINE_1 = 72;
	private static final int COMPOSITE_TOSTRING_C_LINE_1 = 84;
	private static final String COMPOSITE_GETARTIFACTSIZE = "getArtifactsSize";
	private static final String COMPOSITE_GETARTIFACT = "getArtifact";
	private static final String COMPOSITE_TOSTRING = "toString";

	// Artifact Locations
	private static final String ARTIFACT_GETLOCATION = "getLocation";
	private static final int ARTIFACT_GETLOCATION_LINE_1 = 26;

	// Leaf Locations
	private static final String SRC_LEAF = "Leaf.cc";
	private static final int LEAF_PRINT_LINE_1 = 14;
	
	//Target Functions
	private final static FunctionDeclaration funcCompGetArtifactSize = new FunctionDeclaration(null, COMPOSITE_GETARTIFACTSIZE);
	private final static FunctionDeclaration funcCompGetArtifact_i = new FunctionDeclaration(null, COMPOSITE_GETARTIFACT);
	private final static FunctionDeclaration funcArtifactGetLocation = new FunctionDeclaration(null, ARTIFACT_GETLOCATION);
	private final static FunctionDeclaration funcCompToString = new FunctionDeclaration(null, COMPOSITE_TOSTRING);
	private final static FunctionDeclaration funcCompToString_c = new FunctionDeclaration(null, COMPOSITE_TOSTRING);

	static {
		funcCompGetArtifact_i.setParameterTypes(new String[]{"int"});
		funcCompToString_c.setParameterTypes(new String[]{"Char&"});	
	}

	class ResultContext {
		MIStoppedEvent fEvent = null;
		IExecutionDMContext fContext = null;
	
		public ResultContext(MIStoppedEvent event, IExecutionDMContext context) {
			this.fEvent = event;
			this.fContext = context;
		}
		
		public MIStoppedEvent getEvent() {
			return fEvent;
		}
	
		public IExecutionDMContext getContext() {
			return fContext;
		}
	}

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		final DsfSession session = getGDBLaunch().getSession();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), session.getId());
				fGDBCtrl = fServicesTracker.getService(IGDBControl.class);

				IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
				IProcessDMContext procDmc = procService.createProcessContext(fGDBCtrl.getContext(), MIProcesses.UNIQUE_GROUP_ID);
				fContainerDmc = procService.createContainerContext(procDmc, MIProcesses.UNIQUE_GROUP_ID);
				IThreadDMContext threadDmc = procService.createThreadContext(procDmc, "1");
				fThreadExecDmc = procService.createExecutionContext(fContainerDmc, threadDmc, "1");

				fRunCtrl = fServicesTracker.getService(IRunControl3.class);
			}
		};
		session.getExecutor().submit(runnable).get();
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		fServicesTracker.dispose();
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + BIN_COMPOSITE);
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
	}

	private IExecutionDMContext gdbRunToStartLine(String sourceName, int targetLine, ServiceEventWaitor<MIStoppedEvent> waitor) throws Throwable {
		// run gdb to the specified line an resolve the execution context where the MI signal events are being processed
		SyncUtil.runToLine(fThreadExecDmc, sourceName, Integer.toString(targetLine), true);
		MILocationReachedEvent locEvent = waitor.waitForEvent(MILocationReachedEvent.class, TestsPlugin.massageTimeout(500));
		return locEvent.getDMContext();
	}

	private MIStoppedEvent getLastEvent(ServiceEventWaitor<MIStoppedEvent> gdbStopListener) {
		// Fetch the last stopped event as stepping into selection needs to step several times.
		MIStoppedEvent event = null;
		// Run until Timeout exception i.e. no more events in the queue
		try {
			while (true) {
				// Wait or fetch the next stopped event in the queue
				event = gdbStopListener.waitForEvent(MIStoppedEvent.class, TestsPlugin.massageTimeout(500));
			}
		} catch (Exception e) {
			assertTrue("Exception: " + e.getMessage(), e.getMessage().contains("Timed out"));
		}

		return event;
	}

	private void validateLocation(IExecutionDMContext exeContext, MIFrame frame, String funcName) throws Throwable {
		// Validate that the frame received is at the specified location
		assertTrue(frame.getFunction().endsWith(funcName));

		// Validate that GDB is in sync at the specified location
		IFrameDMData gdbFrame = SyncUtil.getFrameData(exeContext, 0);
		assertTrue(gdbFrame.getFunction().endsWith(funcName));
	}

	private void checkGdbIsSuspended() throws Throwable {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		final IContainerDMContext containerDmc = SyncUtil.getContainerContext();

		// Execution shall be suspended
		fRunCtrl.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				wait.setReturnInfo(fRunCtrl.isSuspended(containerDmc));
				wait.waitFinished();
			}
		});

		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		assertTrue("Target is running. It should have been suspended", (Boolean) wait.getReturnInfo());

		wait.waitReset();
	}

	private void triggerRunToLine(final IExecutionDMContext exeContext, final String sourceName, final int targetLine) throws InterruptedException {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		
		fRunCtrl.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				fRunCtrl.runToLine(exeContext, sourceName, targetLine, true, new RequestMonitor(fRunCtrl.getExecutor(), null) {
					@Override
					protected void handleCompleted() {
						wait.waitFinished(getStatus());
					}
				});	
			}
		});
	
		wait.waitUntilDone(TestsPlugin.massageTimeout(10000));
		wait.waitReset();
	}

	private void triggerStepIntoSelection(final IExecutionDMContext exeContext, final String sourceName, final int targetLine, final IFunctionDeclaration function, final boolean skipBreakPoints) throws InterruptedException {
		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		final OperationNotSupportedException[] exception = new OperationNotSupportedException[1];
		// Trigger Stepping into a specified 'function' on the current line
		fRunCtrl.getExecutor().submit(new Runnable() {
			@Override
			public void run() {				
				fRunCtrl.stepIntoSelection(exeContext, sourceName, targetLine, skipBreakPoints, function, new RequestMonitor(fRunCtrl.getExecutor(), null) {
					@Override
					protected void handleCompleted() {
						wait.waitFinished(getStatus());
					}
				});
			}
		});
	
		wait.waitUntilDone(TestsPlugin.massageTimeout(10000));
		wait.waitReset();
		
		if (exception[0] != null) {
			fail("Step into selection failed: " + exception[0].getMessage());
		}
		
	}

	private ResultContext runToLine(IExecutionDMContext exeContext, String sourceName, int runToLine) throws Throwable {
		DsfSession session = getGDBLaunch().getSession();
	
		ServiceEventWaitor<MIStoppedEvent> gdbStopListener = new ServiceEventWaitor<MIStoppedEvent>(session, MIStoppedEvent.class);
	
		// Trigger Run to line
		triggerRunToLine(exeContext, sourceName, runToLine);
	
		// Fetch the last stopped event as stepping into selection needs to step several times.
		MIBreakpointHitEvent event = gdbStopListener.waitForEvent(MIBreakpointHitEvent.class, TestsPlugin.massageTimeout(500));
	
		assertNotNull(event);
	
		// Validate that the last stopped frame received is at the specified location
		MIFrame frame = event.getFrame();
		assertTrue(frame.getLine() == runToLine);
		return new ResultContext(event, exeContext);
	}

	private ResultContext stepIntoSelectionBase(String sourceName, int runToLine, IFunctionDeclaration targetFunction) throws Throwable {
		return stepIntoSelectionBase(sourceName, runToLine, targetFunction, true, true);
	}

	private ResultContext stepIntoSelectionBase(String sourceName, int runToLine, IFunctionDeclaration targetFunction, boolean validateLocation, boolean skipBreakPoints) throws Throwable {
		DsfSession session = getGDBLaunch().getSession();

		ServiceEventWaitor<MIStoppedEvent> gdbStopListener = new ServiceEventWaitor<MIStoppedEvent>(session, MIStoppedEvent.class);

		// Run to an initial line an resolve the execution context where the MI signal events are being processed
		final IExecutionDMContext exeContext = gdbRunToStartLine(SRC_COMPOSITE, COMPOSITE_MAIN_LINE_M1, gdbStopListener);
		assertNotNull(exeContext);
		
		// Trigger Stepping into a specified 'function' and several lines below the current one
		triggerStepIntoSelection(exeContext, sourceName, runToLine, targetFunction, skipBreakPoints);

		// Fetch the last stopped event as stepping into selection needs to step several times.
		MIStoppedEvent event = getLastEvent(gdbStopListener);
		assertNotNull(event);

		// Validate that the last stopped frame received is at the specified location
		MIFrame frame = event.getFrame();

		if (validateLocation) {
			validateLocation(exeContext, frame, targetFunction.getElementName());
		}

		checkGdbIsSuspended();
		
		return new ResultContext(event, exeContext);
	}

	@Test
	public void stepIntoSelection() throws Throwable {
		ResultContext result = stepIntoSelectionBase(SRC_COMPOSITE, COMPOSITE_MAIN_LINE_M1, funcCompGetArtifactSize);
		int currentLine = result.getEvent().getFrame().getLine(); 
		assertTrue(currentLine == COMPOSITE_GETARTIFACTSIZE_LINE_1);
	}
	
	@Test
	public void stepIntoSelectionWithRunToLine() throws Throwable {
		ResultContext result  = stepIntoSelectionBase(SRC_COMPOSITE, COMPOSITE_MAIN_LINE_M2, funcCompGetArtifact_i);
		int currentLine = result.getEvent().getFrame().getLine(); 
		assertTrue(currentLine == COMPOSITE_GETARTIFACT_LINE_1);
	}

	@Test
	public void withSelectedLineOnDifferentFile() throws Throwable {
		ResultContext result  = stepIntoSelectionBase(SRC_LEAF, LEAF_PRINT_LINE_1, funcArtifactGetLocation);
		int currentLine = result.getEvent().getFrame().getLine(); 
		assertTrue(currentLine == ARTIFACT_GETLOCATION_LINE_1);
	}

	/**
	 * A break point is found before reaching search line
	 * 
	 * @throws Throwable
	 */
	@Test
	public void doNotSkipBreakPoints() throws Throwable {
		// insert a break point before the run to line
		SyncUtil.addBreakpoint(SRC_COMPOSITE + ":" + COMPOSITE_MAIN_LINE_L2);
		//trigger step into selection skip break points is set to false
		ResultContext result = stepIntoSelectionBase(SRC_COMPOSITE, COMPOSITE_MAIN_LINE_L4, funcCompToString_c, false, false);
		MIStoppedEvent event = result.getEvent();
		int currentLine = event.getFrame().getLine();
		//validate location, it shall not reach the step to selection line but the break point line instead.
		assertTrue(currentLine == COMPOSITE_MAIN_LINE_L2);
		//validate the last event received is a breakpoint
		assertTrue(event instanceof MIBreakpointHitEvent);
		//Make sure the step to selection operation is no longer active by triggering a second run to line before the step into selection line
		result = runToLine(result.getContext(), SRC_COMPOSITE, COMPOSITE_MAIN_LINE_L3);
		event = result.getEvent();
		currentLine = event.getFrame().getLine();
		//validate location, did not reached the step to selection line but the break point
		assertTrue(currentLine == COMPOSITE_MAIN_LINE_L3);
	}
	
	@Test
	public void diffMethodByArgsNumber() throws Throwable {
		ResultContext result = stepIntoSelectionBase(SRC_COMPOSITE, COMPOSITE_MAIN_LINE_L1, funcCompToString_c);
		int currentLine = result.getEvent().getFrame().getLine(); 
		assertTrue(currentLine == COMPOSITE_TOSTRING_C_LINE_1);  //first line of toString(char& c)
	}

	@Test
	public void diffMethodByArgsNumber2() throws Throwable {
		ResultContext result = stepIntoSelectionBase(SRC_COMPOSITE, COMPOSITE_MAIN_LINE_L1, funcCompToString);
		int currentLine = result.getEvent().getFrame().getLine(); 
		assertTrue(currentLine == COMPOSITE_TOSTRING_LINE_1);  //first line of toString()
	}
	
	@Test
	public void stepIntoRecursiveMethod() throws Throwable {
		fail();
	}
}
