/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMData;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsAddedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsRemovedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsUpdatedEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.CollectAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.EvaluateAction;
import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.Platform;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GDBRemoteTracepointsTest extends BaseParametrizedTestCase {
	protected DsfSession fSession;
	protected DsfServicesTracker fServicesTracker;
	protected IBreakpoints fBreakpointService;
	//	private ITraceControl fTraceService;
	protected IBreakpointsTargetDMContext fBreakpointsDmc;
	//	private ITraceTargetDMContext fTraceTargetDmc;
	protected IGDBControl fGdbControl;
	protected CommandFactory fCommandFactory;

	//	private int fTotalTracingBufferSize = 0;

	protected static final String EXEC_NAME = "TracepointTestApp.exe";
	protected static final String SOURCE_NAME = "TracepointTestApp.cc";

	// Breakpoint tags in TracepointTestApp.cc
	public static final String[] LINE_TAGS = new String[] { "1_BYTE", "2_BYTE", "3_BYTE", "4_BYTE", "5_BYTE" };

	protected static final String NO_CONDITION = "";
	protected static final String NO_COMMANDS = "";
	//    private static final int    LAST_LINE_NUMBER   = 94;
	//
	// private static final int TOTAL_FRAMES_TO_BE_COLLECTED = 1 + 1 + 10 + 1 + 10000;

	protected final static int[] PASS_COUNTS = { 12, 2, 32, 6, 128, 0, 0, 0, 0, 0, 0, 0 };
	protected final static String[] CONDITIONS = { "gIntVar == 543", "gBoolVar == false", "x == 3", "x > 4",
			"x > 2 && gIntVar == 12345" };

	protected static CollectAction[] COLLECT_ACTIONS = new CollectAction[10];
	protected static EvaluateAction[] EVAL_ACTIONS = new EvaluateAction[10];
	// private static WhileSteppingAction[] STEPPING_ACTION_1 = new WhileSteppingAction[3];

	@BeforeClass
	public static void initializeActions() {
		TracepointActionManager tracepointActionMgr = TracepointActionManager.getInstance();

		int index = 0;
		COLLECT_ACTIONS[index] = new CollectAction();
		COLLECT_ACTIONS[index].setCollectString("$locals");
		COLLECT_ACTIONS[index].setName("Collect locals");
		tracepointActionMgr.addAction(COLLECT_ACTIONS[index]);
		index++;

		COLLECT_ACTIONS[index] = new CollectAction();
		COLLECT_ACTIONS[index].setCollectString("gIntVar");
		COLLECT_ACTIONS[index].setName("Collect gIntVar");
		tracepointActionMgr.addAction(COLLECT_ACTIONS[index]);
		index++;

		COLLECT_ACTIONS[index] = new CollectAction();
		COLLECT_ACTIONS[index].setCollectString("$locals, x, $reg");
		COLLECT_ACTIONS[index].setName("Collect locals, x and reg");
		tracepointActionMgr.addAction(COLLECT_ACTIONS[index]);
		index++;

		COLLECT_ACTIONS[index] = new CollectAction();
		COLLECT_ACTIONS[index].setCollectString("$reg");
		COLLECT_ACTIONS[index].setName("Collect reg");
		tracepointActionMgr.addAction(COLLECT_ACTIONS[index]);
		index++;

		COLLECT_ACTIONS[index] = new CollectAction();
		COLLECT_ACTIONS[index].setCollectString("x, $locals");
		COLLECT_ACTIONS[index].setName("Collect x, locals");
		tracepointActionMgr.addAction(COLLECT_ACTIONS[index]);
		index++;

		COLLECT_ACTIONS[index] = new CollectAction();
		COLLECT_ACTIONS[index].setCollectString("$myTraceVariable");
		COLLECT_ACTIONS[index].setName("Collect myTraceVariable");
		tracepointActionMgr.addAction(COLLECT_ACTIONS[index]);
		index++;

		index = 0;
		EVAL_ACTIONS[index] = new EvaluateAction();
		EVAL_ACTIONS[index].setEvalString("$count=$count+1");
		EVAL_ACTIONS[index].setName("Eval increment count");
		tracepointActionMgr.addAction(EVAL_ACTIONS[index]);
		index++;

		EVAL_ACTIONS[index] = new EvaluateAction();
		EVAL_ACTIONS[index].setEvalString("$count2=$count2+2");
		EVAL_ACTIONS[index].setName("Eval increment count2 by 2");
		tracepointActionMgr.addAction(EVAL_ACTIONS[index]);
		index++;

		EVAL_ACTIONS[index] = new EvaluateAction();
		EVAL_ACTIONS[index].setEvalString("$count3=$count3+3");
		EVAL_ACTIONS[index].setName("Eval increment count3 by 3");
		tracepointActionMgr.addAction(EVAL_ACTIONS[index]);
		index++;

		//TODO do while stepping actions
		index = 0;

	}

	@Override
	public void doBeforeTest() throws Exception {
		// GDB tracepoints are only supported on a remote target (e.g., using gdbserver)
		assumeRemoteSession();
		super.doBeforeTest();
		resolveLineTagLocations(SOURCE_NAME, LINE_TAGS);

		fSession = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

			fBreakpointService = fServicesTracker.getService(IBreakpoints.class);
			fGdbControl = fServicesTracker.getService(IGDBControl.class);
			fCommandFactory = fGdbControl.getCommandFactory();

			//        		fTraceService = fServicesTracker.getService(ITraceControl.class);
			fSession.addServiceEventListener(GDBRemoteTracepointsTest.this, null);

			// Create a large array to make sure we don't run out
			fTracepoints = new IBreakpointDMContext[100];

			// Run an initial test to check that everything is ok with GDB
			checkTraceInitialStatus();
		};
		fSession.getExecutor().submit(runnable).get();

		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		fBreakpointsDmc = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);
		assert (fBreakpointsDmc != null);
		//                fTraceTargetDmc = DMContexts.getAncestorOfType(containerDmc, ITraceTargetDMContext.class);

	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);

		// To test both fast and normal tracepoints, we use the FAST_THEN_NORMAL setting
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_TRACEPOINT_MODE,
				IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_FAST_THEN_NORMAL);
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();
		if (fSession != null)
			fSession.getExecutor().submit(() -> fSession.removeServiceEventListener(GDBRemoteTracepointsTest.this))
					.get();
		fBreakpointService = null;
		if (fServicesTracker != null)
			fServicesTracker.dispose();
	}

	// *********************************************************************
	// Below are utility methods.
	// *********************************************************************
	private static Boolean lock = true;

	enum Events {
		BP_ADDED, BP_UPDATED, BP_REMOVED, BP_HIT
	}

	final int BP_ADDED = Events.BP_ADDED.ordinal();
	final int BP_UPDATED = Events.BP_UPDATED.ordinal();
	final int BP_REMOVED = Events.BP_REMOVED.ordinal();
	final int BP_HIT = Events.BP_HIT.ordinal();
	private int[] fBreakpointEvents = new int[Events.values().length];
	private boolean fBreakpointEvent;
	private int fBreakpointEventCount;

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
	protected void clearEventCounters() {
		synchronized (lock) {
			for (int i = 0; i < fBreakpointEvents.length; i++) {
				fBreakpointEvents[i] = 0;
			}
			fBreakpointEvent = false;
			fBreakpointEventCount = 0;
		}
	}

	// Get the breakpoint hit count
	protected int getBreakpointEventCount(int event) {
		int count = 0;
		synchronized (lock) {
			count = fBreakpointEvents[event];
		}
		return count;
	}

	// Suspends the thread until an event is flagged
	// NOTE: too simple for real life but good enough for this test suite
	protected void waitForBreakpointEvent() {
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

	protected IBreakpointDMContext insertBreakpoint(final IBreakpointsTargetDMContext context,
			final Map<String, Object> attributes) throws Throwable {
		Query<IBreakpointDMContext> query = new Query<IBreakpointDMContext>() {
			@Override
			protected void execute(final DataRequestMonitor<IBreakpointDMContext> rm) {
				fBreakpointService.insertBreakpoint(context, attributes, rm);
			}
		};

		fBreakpointService.getExecutor().execute(query);
		try {
			return query.get(1, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			assert false : e.getCause().getMessage();
		}
		return null;
	}

	protected void removeBreakpoint(final IBreakpointDMContext breakpoint) throws Throwable {
		Query<Object> query = new Query<Object>() {
			@Override
			protected void execute(final DataRequestMonitor<Object> rm) {
				fBreakpointService.removeBreakpoint(breakpoint, rm);
			}
		};

		fBreakpointService.getExecutor().execute(query);
		try {
			query.get(1, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			assert false : e.getCause().getMessage();
		}
	}

	protected void updateBreakpoint(final IBreakpointDMContext breakpoint, final Map<String, Object> delta)
			throws Throwable {
		Query<Object> query = new Query<Object>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				fBreakpointService.updateBreakpoint(breakpoint, delta, rm);
			}
		};

		fBreakpointService.getExecutor().execute(query);
		try {
			query.get(1, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			assert false : e.getCause().getMessage();
		}
	}

	protected IBreakpointDMData getBreakpoint(final IBreakpointDMContext breakpoint) throws Throwable {
		Query<IBreakpointDMData> query = new Query<IBreakpointDMData>() {
			@Override
			protected void execute(DataRequestMonitor<IBreakpointDMData> rm) {
				fBreakpointService.getBreakpointDMData(breakpoint, rm);
			}
		};

		fBreakpointService.getExecutor().execute(query);
		try {
			return query.get(1, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			assert false : e.getCause().getMessage();
		}
		return null;
	}

	protected IBreakpointDMContext[] getBreakpoints(final IBreakpointsTargetDMContext context) throws Throwable {
		Query<IBreakpointDMContext[]> query = new Query<IBreakpointDMContext[]>() {
			@Override
			protected void execute(DataRequestMonitor<IBreakpointDMContext[]> rm) {
				fBreakpointService.getBreakpoints(context, rm);
			}
		};

		fBreakpointService.getExecutor().execute(query);
		try {
			return query.get(1, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			assert false : e.getCause().getMessage();
		}
		return null;
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

	protected IBreakpointDMContext[] fTracepoints = null;

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

	protected class TracepointData {
		String sourceFile;
		int lineNumber;
		String condition;
		int passcount;
		boolean enabled;
		String commands;
		boolean isFastTp;

		public TracepointData(int line, String cond, int pass, boolean isEnabled, String cmds, boolean fast) {
			this(SOURCE_NAME, line, cond, pass, isEnabled, cmds, fast);
		}

		public TracepointData(String file, int line, String cond, int pass, boolean isEnabled, String cmds,
				boolean fast) {
			sourceFile = file;
			lineNumber = line;
			condition = cond;
			passcount = pass;
			enabled = isEnabled;
			commands = cmds;
			isFastTp = fast;
			assertTrue(!fast || fastTracepointsSupported());
		}
	}

	protected void checkTracepoints(TracepointData[] dataArray) throws Throwable {
		int numTracepoints = dataArray.length;

		// Fetch the tp list from the backend
		IBreakpointDMContext[] tracepoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("expected " + numTracepoints + " breakpoint(s), received " + tracepoints.length, numTracepoints,
				tracepoints.length);

		for (int i = 0; i < numTracepoints; i++) {
			TracepointData data = dataArray[i];

			// Ensure that the tracepoints were correctly installed
			MIBreakpointDMData tp = (MIBreakpointDMData) getBreakpoint(fTracepoints[i]);
			assertEquals("tracepoint " + i + " is not a tracepoint but a " + tp.getBreakpointType(),
					MIBreakpoints.TRACEPOINT, tp.getBreakpointType());
			assertEquals("tracepoint " + i + " should be a " + (data.isFastTp ? "fast" : "normal")
					+ " tracepoint but is not", data.isFastTp, tp.getType().equals("fast tracepoint"));
			assertEquals("tracepoint " + i + " mismatch (wrong file name) got " + tp.getFileName(), data.sourceFile,
					tp.getFileName());
			assertEquals("tracepoint " + i + " mismatch (wrong line number) got " + tp.getLineNumber(), data.lineNumber,
					tp.getLineNumber());
			assertEquals("tracepoint " + i + " mismatch (wrong condition) got " + tp.getCondition(), data.condition,
					tp.getCondition());
			assertEquals("tracepoint " + i + " mismatch (wrong pass count) got " + tp.getPassCount(), data.passcount,
					tp.getPassCount());
			assertEquals("tracepoint " + i + " mismatch (wrong enablement) got " + tp.isEnabled(), data.enabled,
					tp.isEnabled());
			assertEquals("tracepoint " + i + " mismatch (wrong actions) got " + tp.getCommands(), data.commands,
					tp.getCommands());

			assertEquals("tracepoint " + i + " mismatch", getBreakpoint(tracepoints[i]), tp);
		}
	}

	protected void checkTracepoints(boolean useCond, boolean useCount, boolean enabled, boolean useActions)
			throws Throwable {
		TracepointData[] dataArray = new TracepointData[] {
				new TracepointData(getLineForTag("1_BYTE"), useCond ? CONDITIONS[0] : NO_CONDITION,
						useCount ? PASS_COUNTS[0] : 0, enabled,
						useActions ? COLLECT_ACTIONS[0].toString() : NO_COMMANDS, false),
				new TracepointData(getLineForTag("2_BYTE"), useCond ? CONDITIONS[1] : NO_CONDITION,
						useCount ? PASS_COUNTS[1] : 0, enabled,
						useActions ? COLLECT_ACTIONS[1].toString() : NO_COMMANDS, false),
				new TracepointData(getLineForTag("3_BYTE"), useCond ? CONDITIONS[2] : NO_CONDITION,
						useCount ? PASS_COUNTS[2] : 0, enabled,
						useActions ? COLLECT_ACTIONS[2].toString() : NO_COMMANDS, false),
				new TracepointData(getLineForTag("4_BYTE"), useCond ? CONDITIONS[3] : NO_CONDITION,
						useCount ? PASS_COUNTS[3] : 0, enabled,
						useActions ? COLLECT_ACTIONS[3].toString() : NO_COMMANDS, acceptsFastTpOnFourBytes()),
				new TracepointData(getLineForTag("5_BYTE"), useCond ? CONDITIONS[4] : NO_CONDITION,
						useCount ? PASS_COUNTS[4] : 0, enabled,
						useActions ? COLLECT_ACTIONS[4].toString() : NO_COMMANDS, fastTracepointsSupported()), };

		checkTracepoints(dataArray);
	}

	protected void createTracepoints(boolean useCond, boolean useCount, boolean enabled, boolean useActions)
			throws Throwable {
		Map<String, Object> attributes = null;

		int[] lineNumbers = { getLineForTag("1_BYTE"), getLineForTag("2_BYTE"), getLineForTag("3_BYTE"),
				getLineForTag("4_BYTE"), getLineForTag("5_BYTE") };

		for (int i = 0; i < lineNumbers.length; i++) {
			attributes = new HashMap<>();
			attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
			attributes.put(MIBreakpoints.FILE_NAME, SOURCE_NAME);
			attributes.put(MIBreakpoints.LINE_NUMBER, lineNumbers[i]);
			if (!enabled)
				attributes.put(MIBreakpoints.IS_ENABLED, enabled);
			if (useCount)
				attributes.put(MIBreakpoints.PASS_COUNT, PASS_COUNTS[i]);
			if (useCond)
				attributes.put(MIBreakpoints.CONDITION, CONDITIONS[i]);
			if (useActions)
				attributes.put(MIBreakpoints.COMMANDS, COLLECT_ACTIONS[i].getName());

			fTracepoints[i] = insertBreakpoint(fBreakpointsDmc, attributes);

			waitForBreakpointEvent();
			assertEquals("Incorrect number of breakpoint events", 1, fBreakpointEventCount);
			assertEquals("Incorrect number of breakpoint added events", 1, getBreakpointEventCount(BP_ADDED));
			clearEventCounters();
		}

		checkTracepoints(useCond, useCount, enabled, useActions);
	}

	/**
	 * This test makes sure that the tracing status is correct when we start.
	 * It also stores the total buffer size to be used by other tests.
	 * This test is being run before every other test by being called
	 * by the @Before method; this allows to verify every launch of GDB.
	 */
	@Test
	@Ignore
	public void checkTraceInitialStatus() {
		//		checkTraceStatus(true, false, 0);
	}

	/**
	 * This test relies on knowing that instructions in the compiled
	 * code are of the expected number of bytes long. See the {@value #SOURCE_NAME}
	 * for details of how to resolve this test failing.
	 */
	@Test
	public void checkInstructionsAreExpectedLength() throws Throwable {
		// GDB helpfully returns an error if you try to insert a fast tracepoint
		// on an instruction < 4 bytes long. We can use that to verify how big
		// each instruction is.

		// The disassembly does not give the required information on older GDBs.
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_5);

		IContainerDMContext containerDmc = SyncUtil.getContainerContext();
		Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fGdbControl.queueCommand(
						fCommandFactory.createMIInterpreterExecConsole(containerDmc, "disassemble /rm foo"), rm);
			}
		};
		fGdbControl.getExecutor().execute(query);
		MIInfo miInfo = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		try {
			MIOOBRecord[] mioobRecords = miInfo.getMIOutput().getMIOOBRecords();
			Set<Integer> passed = new HashSet<>();
			for (int i = 0; i < mioobRecords.length; i++) {
				String sourceLineWithComment = ((MIStreamRecord) mioobRecords[i]).getString();
				int index;
				if ((index = sourceLineWithComment.indexOf("_BYTE")) >= 0) {
					int byteCount = Integer.parseInt(sourceLineWithComment.substring(index - 1, index));
					String disassembledInstruction = ((MIStreamRecord) mioobRecords[i + 1]).getString();
					String[] split = disassembledInstruction.split("\\t", 3);
					@SuppressWarnings("unused")
					String addr = split[0];
					String bytes = split[1];
					@SuppressWarnings("unused")
					String mnemonic = split[2];
					String[] bytes2 = bytes.split(" ");
					assertEquals(byteCount, bytes2.length);
					passed.add(byteCount);
				}
			}
			assertEquals("Some byte length were not seen", new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)), passed);
		} catch (AssertionError | Exception e) {
			throw new AssertionError(
					"Failed to verify instruction lengths. Output from GDB's disassemble:\n" + miInfo.toString(), e);
		}
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
	public void createTracepoints() throws Throwable {
		createTracepoints(false, false, true, false);
	}

	/**
	 * This test sets the different types of tracepoints and then deletes them
	 */
	@Test
	public void deleteTracepoints() throws Throwable {
		createTracepoints();
		// Delete all tracepoints
		for (IBreakpointDMContext tp : fTracepoints) {
			if (tp == null)
				break;
			removeBreakpoint(tp);
		}

		// Fetch the bp list from the backend
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertTrue("BreakpointService problem: expected " + 0 + " breakpoints, received " + breakpoints.length,
				breakpoints.length == 0);
	}

	/**
	 * This test sets the different types of tracepoints and then disables them
	 */
	@Test
	public void disableTracepoints() throws Throwable {
		createTracepoints();

		Map<String, Object> delta = new HashMap<>();
		delta.put(MIBreakpoints.IS_ENABLED, false);
		// Disable all tracepoints
		for (IBreakpointDMContext tp : fTracepoints) {
			if (tp == null)
				break;
			updateBreakpoint(tp, delta);
		}

		checkTracepoints(false, false, false, false);
	}

	/**
	 * This test sets, disables the different types of tracepoints and then enables them
	 */
	@Test
	public void enableTracepoints() throws Throwable {
		disableTracepoints();

		Map<String, Object> delta = new HashMap<>();
		delta.put(MIBreakpoints.IS_ENABLED, true);
		// Enable all tracepoints
		for (IBreakpointDMContext tp : fTracepoints) {
			if (tp == null)
				break;
			updateBreakpoint(tp, delta);
		}

		checkTracepoints(false, false, true, false);
	}

	/**
	 * This test sets the different types of tracepoints and then sets their passcount
	 */
	@Test
	public void tracepointPasscount() throws Throwable {
		createTracepoints();

		Map<String, Object> delta = new HashMap<>();
		// Set passcount for all tracepoints
		for (int i = 0; i < fTracepoints.length; i++) {
			if (fTracepoints[i] == null)
				break;
			if (PASS_COUNTS[i] == 0)
				continue;
			delta.put(MIBreakpoints.PASS_COUNT, PASS_COUNTS[i]);
			updateBreakpoint(fTracepoints[i], delta);
		}

		checkTracepoints(false, true, true, false);
	}

	/**
	 * This test sets the different types of tracepoints and then sets some conditions
	 */
	@Test
	public void tracepointCondition() throws Throwable {
		createTracepoints();

		Map<String, Object> delta = new HashMap<>();
		// Set conditions for all tracepoints
		for (int i = 0; i < fTracepoints.length; i++) {
			if (fTracepoints[i] == null)
				break;
			if (CONDITIONS[i].equals(NO_CONDITION))
				continue;
			delta.put(MIBreakpoints.CONDITION, CONDITIONS[i]);
			updateBreakpoint(fTracepoints[i], delta);
		}

		checkTracepoints(true, false, true, false);
	}

	/**
	 * This test sets the different types of tracepoints and then sets some actions
	 */
	@Test
	public void tracepointActions() throws Throwable {
		createTracepoints();

		Map<String, Object> delta = new HashMap<>();
		// Set conditions for all tracepoints
		for (int i = 0; i < fTracepoints.length; i++) {
			if (fTracepoints[i] == null)
				break;
			if (COLLECT_ACTIONS[i].equals(NO_COMMANDS))
				continue;
			delta.put(MIBreakpoints.COMMANDS, COLLECT_ACTIONS[i].getName());
			updateBreakpoint(fTracepoints[i], delta);
		}

		checkTracepoints(false, false, true, true);
	}

	/**
	 * This test creates a tracepoint that starts disabled
	 */
	@Test
	public void createTracepointDisabled() throws Throwable {
		createTracepoints(false, false, false, false);
	}

	/**
	 * This test creates a tracepoint that starts with a passcount
	 */
	@Test
	public void createTracepointWithPasscount() throws Throwable {
		createTracepoints(false, true, true, false);
	}

	/**
	 * This test creates a tracepoint that starts with a condition
	 */
	@Test
	public void createTracepointWithCondition() throws Throwable {
		createTracepoints(true, false, true, false);
	}

	/**
	 * This test creates tracepoints that start a command
	 */
	@Test
	public void createTracepointWithCommand() throws Throwable {
		createTracepoints(false, false, true, true);
	}

	/**
	 * This test creates tracepoints that start with more than one command
	 */
	@Test
	public void createTracepointWithMultipleCommands() throws Throwable {

		String commandsNames1 = COLLECT_ACTIONS[0].getName() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[1].getName() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[3].getName();
		String commandsResult1 = COLLECT_ACTIONS[0].toString() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[1].toString() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[3].toString();

		String commandsNames2 = COLLECT_ACTIONS[2].getName() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[2].getName() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[1].getName();
		String commandsResult2 = COLLECT_ACTIONS[2].toString() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[2].toString() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[1].toString();

		String commandsNames3 = COLLECT_ACTIONS[4].getName() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[0].getName() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[1].getName();
		String commandsResult3 = COLLECT_ACTIONS[4].toString() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[0].toString() + TracepointActionManager.TRACEPOINT_ACTION_DELIMITER
				+ COLLECT_ACTIONS[1].toString();

		String cmdNames[] = new String[] { commandsNames1, COLLECT_ACTIONS[0].getName(), commandsNames2,
				COLLECT_ACTIONS[2].getName(), commandsNames3 };
		String cmdResults[] = new String[] { commandsResult1, COLLECT_ACTIONS[0].toString(), commandsResult2,
				COLLECT_ACTIONS[2].toString(), commandsResult3 };

		Map<String, Object> attributes = null;

		int[] lineNumbers = { getLineForTag("1_BYTE"), getLineForTag("2_BYTE"), getLineForTag("3_BYTE"),
				getLineForTag("4_BYTE"), getLineForTag("5_BYTE") };

		for (int i = 0; i < lineNumbers.length; i++) {
			attributes = new HashMap<>();
			attributes.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
			attributes.put(MIBreakpoints.FILE_NAME, SOURCE_NAME);
			attributes.put(MIBreakpoints.LINE_NUMBER, lineNumbers[i]);
			attributes.put(MIBreakpoints.COMMANDS, cmdNames[i]);

			fTracepoints[i] = insertBreakpoint(fBreakpointsDmc, attributes);

			waitForBreakpointEvent();
			assertEquals("Incorrect number of breakpoint events", 1, fBreakpointEventCount);
			assertEquals("Incorrect number of breakpoint added events", 1, getBreakpointEventCount(BP_ADDED));
			clearEventCounters();
		}

		TracepointData[] dataArray = new TracepointData[] {
				new TracepointData(getLineForTag("1_BYTE"), NO_CONDITION, 0, true, cmdResults[0], false),
				new TracepointData(getLineForTag("2_BYTE"), NO_CONDITION, 0, true, cmdResults[1], false),
				new TracepointData(getLineForTag("3_BYTE"), NO_CONDITION, 0, true, cmdResults[2], false),
				new TracepointData(getLineForTag("4_BYTE"), NO_CONDITION, 0, true, cmdResults[3],
						acceptsFastTpOnFourBytes()),
				new TracepointData(getLineForTag("5_BYTE"), NO_CONDITION, 0, true, cmdResults[4],
						fastTracepointsSupported()), };

		checkTracepoints(dataArray);
	}

	/**
	 * This test creates an enabled tracepoint that starts with commands, condition and passcount
	 */
	@Test
	public void createTracepointEnabledWithCommandsConditionPasscount() throws Throwable {
		createTracepoints(true, true, true, true);
	}

	/**
	 * This test creates a disabled tracepoint that starts with commands, condition and passcount
	 */
	@Test
	public void createTracepointDisabledWithCommandsConditionPasscount() throws Throwable {
		createTracepoints(true, true, false, true);
	}

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

	protected boolean acceptsFastTpOnFourBytes() {
		String gdbVersion = getGdbVersion();
		boolean isLower = LaunchUtils.compareVersions(ITestConstants.SUFFIX_GDB_7_4, gdbVersion) > 0;
		if (isLower)
			return false;
		// With GDB 7.4, fast tracepoints only need an
		// instruction of 4 bytes or more when on a 32bit architecture, instead of 5.
		if (Platform.getOS().equals(Platform.ARCH_X86)) {
			return true;
		}
		return false;
	}

	protected boolean fastTracepointsSupported() {
		return LaunchUtils.compareVersions(getGdbVersion(), ITestConstants.SUFFIX_GDB_7_2) >= 0;
	}

	/**
	 * This test sets the different types of tracepoints and then sets some string collection actions
	 */
	@Test
	public void tracepointActionsWithCollectStrings() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_4);
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

		Map<String, Object> delta = new HashMap<>();
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

		TracepointData[] dataArray = new TracepointData[] {
				new TracepointData(getLineForTag("1_BYTE"), NO_CONDITION, 0, true, action1.toString(), false),
				new TracepointData(getLineForTag("2_BYTE"), NO_CONDITION, 0, true, action2.toString(), false),
				new TracepointData(getLineForTag("3_BYTE"), NO_CONDITION, 0, true, action1.toString(), false),
				new TracepointData(getLineForTag("4_BYTE"), NO_CONDITION, 0, true, action1.toString(),
						acceptsFastTpOnFourBytes()),
				new TracepointData(getLineForTag("5_BYTE"), NO_CONDITION, 0, true, action2.toString(),
						fastTracepointsSupported()), };

		checkTracepoints(dataArray);
	}
}
