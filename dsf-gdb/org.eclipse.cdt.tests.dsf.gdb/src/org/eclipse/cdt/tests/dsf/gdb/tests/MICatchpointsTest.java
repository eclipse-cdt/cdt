/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMData;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsAddedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsRemovedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsUpdatedEvent;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.MIBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.gdb.eventbkpts.IEventBreakpointConstants;
import org.eclipse.cdt.gdb.internal.eventbkpts.GdbCatchpoints;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This is the test suite for the catchpoint support in DSF-GDB.
 * 
 * It is meant to be a regression suite to be executed automatically against the
 * DSF nightly builds.
 * 
 * It is also meant to be augmented with a proper test case(s) every time a
 * feature is added or in the event (unlikely :-) that a bug is found in the
 * Breakpoint Service.
 * 
 * Refer to the JUnit4 documentation for an explanation of the annotations.
 * 
 */

@RunWith(BackgroundRunner.class)
public class MICatchpointsTest extends BaseTestCase {

    private static final String TEST_APPL   = "data/launch/bin/CatchpointTestApp.exe"; //$NON-NLS-1$

    public static final String SOURCE_FILE    = "CatchpointTestApp.cc"; //$NON-NLS-1$
    
    public static final int LINE_NUMBER_SLEEP_CALL = 17;

    // Asynchronous Completion
    private final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    
    // Services references
    private DsfSession          fSession;
	private IBreakpointsTargetDMContext fBreakpointsDmc;
    private DsfServicesTracker  fServicesTracker;
    private MIRunControl        fRunControl;
    private IBreakpoints        fBreakpointService;
    private IExpressions        fExpressionService;
    
    // Event Management
    private static Boolean fEventHandlerLock = true;
    private enum Events { BP_ADDED, BP_UPDATED, BP_REMOVED, BP_HIT }
    private final int BP_ADDED   = Events.BP_ADDED.ordinal();
    private final int BP_UPDATED = Events.BP_UPDATED.ordinal();
    private final int BP_REMOVED = Events.BP_REMOVED.ordinal();
    private final int BP_HIT     = Events.BP_HIT.ordinal();
    
    /** number of times a breakpoint event was received, broken down by event type */ 
    private int[]   fBreakpointEvents = new int[Events.values().length];
    
    /** total number of breakpoint events received */
    private int totalBreakpointEventsCount() {
    	synchronized (fEventHandlerLock) {
	    	int total = 0;
	    	for (int count : fBreakpointEvents) {
	    		total += count;
	    	}
	    	return total;
    	}
    }
    
    
    /**
     * The gdb breakpoint number associated with the most recent breakpoint event
     */
    private int fBreakpointRef;

    // NOTE: The back-end can reformat the condition. In order for the
    // comparison to work, better specify the condition as the back-end
    // would have it.
    private final String CONDITION_VAR     = "g_i";
    private final String CONDITION_NONE    = "";
    private final String CONDITION_1     = CONDITION_VAR + " == 2";
    private final String CONDITION_2     = CONDITION_VAR + " == 4";
    private final String CONDITION_NEVER_MET = CONDITION_VAR + " == 10000";
    private final String CONDITION_ALWAYS_MET = CONDITION_VAR + " >= 0";

    // Error messages
    private final String UNKNOWN_EXECUTION_CONTEXT    = "Unknown execution context";
    private final String UNKNOWN_BREAKPOINT           = "Unknown breakpoint";
    
    // ========================================================================
    // Housekeeping stuff
    // ========================================================================

    @BeforeClass
    public static void testSuiteInitialization() {
        // Select the binary to run the tests against
        setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, TEST_APPL);
    }

    @AfterClass
    public static void testSuiteCleanup() {
    }

    @Before
    public void testCaseInitialization() throws Exception {

        // Get a reference to the breakpoint service
        fSession = getGDBLaunch().getSession();
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
                fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
                assertNotNull(fServicesTracker);
        		    
                fRunControl = fServicesTracker.getService(MIRunControl.class);
                assertNotNull(fRunControl);

                fBreakpointService = fServicesTracker.getService(IBreakpoints.class);
                assertNotNull(fBreakpointService);

                fExpressionService = fServicesTracker.getService(IExpressions.class);
                assertNotNull(fExpressionService);


                // Register to receive breakpoint events
                fRunControl.getSession().addServiceEventListener(MICatchpointsTest.this, null);

                clearEventCounters();
            }
        };
        fSession.getExecutor().submit(runnable).get();
        
        IContainerDMContext containerDmc = SyncUtil.getContainerContext();
        fBreakpointsDmc = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);
        assertNotNull(fBreakpointsDmc);

    }

    @After
    public void testCaseCleanup() throws Exception {
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
            	fRunControl.getSession().removeServiceEventListener(MICatchpointsTest.this);
            }
        };
        fSession.getExecutor().submit(runnable).get();

		// Clear the references (not strictly necessary)
        fBreakpointService = null;
        fRunControl = null;
        fServicesTracker.dispose();
        fServicesTracker = null;

        clearEventCounters();
    }

    // ========================================================================
    // Event Management Functions
    // ========================================================================

    /* -----------------------------------------------------------------------
     * eventDispatched
     * ------------------------------------------------------------------------
     * Processes BreakpointHitEvent.
     * ------------------------------------------------------------------------
     * @param e The BreakpointEvent
     * ------------------------------------------------------------------------
     */
  	@DsfServiceEventHandler
	public void eventDispatched(IBreakpointsAddedEvent e) {
  		synchronized (fEventHandlerLock) {
  			fBreakpointEvents[BP_ADDED]++;
  			fBreakpointRef = ((MIBreakpointDMContext) e.getBreakpoints()[0]).getReference();
  			System.out.println(DsfPlugin.getDebugTime() + " Got bp added event (#" + fBreakpointRef + ")");
  			fEventHandlerLock.notifyAll();
  		}
	}

  	@DsfServiceEventHandler
	public void eventDispatched(IBreakpointsUpdatedEvent e) {
  		synchronized (fEventHandlerLock) {
  			fBreakpointEvents[BP_UPDATED]++;
  			fBreakpointRef = ((MIBreakpointDMContext) e.getBreakpoints()[0]).getReference();
  			System.out.println(DsfPlugin.getDebugTime() + " Got bp updated event (#" + fBreakpointRef + ")");
  			fEventHandlerLock.notifyAll();
  		}
	}

  	@DsfServiceEventHandler
	public void eventDispatched(IBreakpointsRemovedEvent e) {
  		synchronized (fEventHandlerLock) {
  			fBreakpointEvents[BP_REMOVED]++;
  			fBreakpointRef = ((MIBreakpointDMContext) e.getBreakpoints()[0]).getReference();
  			System.out.println(DsfPlugin.getDebugTime() + " Got bp removed event (#" + fBreakpointRef + ")");
  			fEventHandlerLock.notifyAll();
  		}
	}

  	@DsfServiceEventHandler
	public void eventDispatched(MIBreakpointHitEvent e) {
  		synchronized (fEventHandlerLock) {
  			fBreakpointEvents[BP_HIT]++;
  			fBreakpointRef = e.getNumber();
  			System.out.println(DsfPlugin.getDebugTime() + " Got bp hit event (#" + fBreakpointRef + ")");
  			fEventHandlerLock.notifyAll();
  		}
	}

	// Clears the counters
	private void clearEventCounters() {
		synchronized (fEventHandlerLock) {
			for (int i = 0; i < fBreakpointEvents.length; i++) {
				fBreakpointEvents[i] = 0;
			}
		}
	}

	// Get the breakpoint hit count
	private int getBreakpointEventCount(int event) {
		int count = 0;
  		synchronized (fEventHandlerLock) {
  			count = fBreakpointEvents[event];
  		}
		return count;
	}

	/**
	 * Suspends the calling thread until [count] number of breakpoint events
	 * have been received in the current test. NOTE: too simple for real life
	 * but good enough for this test suite
	 * 
	 * @param count
	 *            the number breakpoint events to wait for
	 * @param timeout
	 *            max wait time, in milliseconds
	 */
	private void waitForBreakpointEvent(int count, int timeout) throws Exception {
		long startMs = System.currentTimeMillis();
		synchronized (fEventHandlerLock) {
			// Make sure we don't wait forever, in case an event never
			// arrives.  The test will check if everything was received
			int receivedCount;
			while ((receivedCount = totalBreakpointEventsCount()) < count) {
				try {
					fEventHandlerLock.wait(30);
				} catch (InterruptedException ex) {
				}
				if (System.currentTimeMillis() - startMs > timeout) {
					throw new Exception("Timed out waiting for " + count + " breakpoint events to occur. Only " + receivedCount + " occurred.");
				}
			}
		}
	}
	
	/**
	 * Simplified variant that just waits up to two seconds
	 */
	private void waitForBreakpointEvent(int count) throws Exception {
		waitForBreakpointEvent(count, TestsPlugin.massageTimeout(2000));
	}

    // ========================================================================
    // Helper Functions
    // ========================================================================

    /* ------------------------------------------------------------------------
     * evaluateExpression
     * ------------------------------------------------------------------------
     * Invokes the ExpressionService to evaluate an expression. In theory,
     * we shouldn't rely on another service to test this one but we need a
     * way to access a variable from the test application in order verify
     * that the memory operations (read/write) are working properly.
     * ------------------------------------------------------------------------
     * @param expression Expression to resolve @return Resolved expression
     * @throws InterruptedException
     * ------------------------------------------------------------------------
     */
    private BigInteger evaluateExpression(IDMContext ctx, String expression) throws Throwable {

        // Get a stack context (temporary - should be an MIcontainerDMC)
		final IExpressionDMContext expressionDMC = SyncUtil.createExpression(ctx, expression);
		final FormattedValueDMContext formattedValueDMC = SyncUtil.getFormattedValue(fExpressionService,
				expressionDMC, IFormattedValues.DECIMAL_FORMAT);

		// Create the DataRequestMonitor which will store the operation result in the wait object
		final DataRequestMonitor<FormattedValueDMData> drm =
			new DataRequestMonitor<FormattedValueDMData>(fSession.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					fWait.setReturnInfo(getData());
				}
				fWait.waitFinished(getStatus());
			}
		};

		// Evaluate the expression (asynchronously)
		fWait.waitReset();
		fSession.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				fExpressionService.getFormattedExpressionValue(formattedValueDMC, drm);
			}
		});

		// Wait for completion
		fWait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		assertTrue(fWait.getMessage(), fWait.isOK());

		// Return the string formatted by the back-end
		String result = "";
		Object returnInfo = fWait.getReturnInfo();
		if (returnInfo instanceof FormattedValueDMData)
			result = ((FormattedValueDMData) returnInfo).getFormattedValue();
		return new BigInteger(result);
    }

    /* ------------------------------------------------------------------------
     * getBreakpoints
     * ------------------------------------------------------------------------
     * Retrieves the installed breakpoints list
     * ------------------------------------------------------------------------
     * Typical usage:
     *    IBreakpointDMContext[] breakpoints = getBreakpoints(context);
     * ------------------------------------------------------------------------
     * @param context       the execution context
     * ------------------------------------------------------------------------
     */
    private IBreakpointDMContext[] getBreakpoints(final IBreakpointsTargetDMContext context) throws InterruptedException
    {
    	// Clear the completion waiter
		fWait.waitReset();

        // Set the Request Monitor
        final DataRequestMonitor<IBreakpointDMContext[]> drm =
            new DataRequestMonitor<IBreakpointDMContext[]>(fBreakpointService.getExecutor(), null) {
                @Override
                protected void handleCompleted() {
                    fWait.waitFinished(getStatus());
                }
            };

        // Issue the breakpoint request
        fWait.waitReset();
        fBreakpointService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fBreakpointService.getBreakpoints(context, drm);
            }
        });

        // Wait for completion
        fWait.waitUntilDone(TestsPlugin.massageTimeout(5000));
        assertTrue(fWait.getMessage(), fWait.isOK());

        // Return the string formatted by the back-end
        return drm.getData();
    }

    /* ------------------------------------------------------------------------
     * getBreakpoint
     * ------------------------------------------------------------------------
     * Retrieves the installed breakpoint
     * ------------------------------------------------------------------------
     * Typical usage:
     *    IBreakpointDMContext breakpoint = ...;
     *    IBreakpointDMData bp = getBreakpoint(breakpoint);
     * ------------------------------------------------------------------------
     * @param breakpoint    the breakpoint to retrieve
     * ------------------------------------------------------------------------
     */
    private IBreakpointDMData getBreakpoint(final IBreakpointDMContext breakpoint) throws InterruptedException
    {
    	// Clear the completion waiter
		fWait.waitReset();

        // Set the Request Monitor
        final DataRequestMonitor<IBreakpointDMData> drm =
            new DataRequestMonitor<IBreakpointDMData>(fBreakpointService.getExecutor(), null) {
                @Override
                protected void handleCompleted() {
                    fWait.waitFinished(getStatus());
                }
            };

        // Issue the breakpoint request
        fWait.waitReset();
        fBreakpointService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fBreakpointService.getBreakpointDMData(breakpoint, drm);
            }
        });

        // Wait for completion
        fWait.waitUntilDone(TestsPlugin.massageTimeout(5000));
        assertTrue(fWait.getMessage(), fWait.isOK());

        // Return the string formatted by the back-end
        return drm.getData();
    }

    /* ------------------------------------------------------------------------
     * insertBreakpoint
     * ------------------------------------------------------------------------
     * Issues an add breakpoint request.
     * ------------------------------------------------------------------------
     * Typical usage:
     *    bp = insertBreakpoint(context, attributes);
     *    assertTrue(fWait.getMessage(), fWait.isOK());
     * ------------------------------------------------------------------------
     * @param context       the execution context
     * @param attributes    the breakpoint attributes
     * ------------------------------------------------------------------------
     */
    private IBreakpointDMContext insertBreakpoint(final IBreakpointsTargetDMContext context,
    		final Map<String,Object> attributes) throws InterruptedException
    {
    	// Clear the completion waiter
		fWait.waitReset();

		// Set the Request Monitor
        final DataRequestMonitor<IBreakpointDMContext> drm =
            new DataRequestMonitor<IBreakpointDMContext>(fBreakpointService.getExecutor(), null) {
                @Override
                protected void handleCompleted() {
                    fWait.waitFinished(getStatus());
                }
            };

        // Issue the remove breakpoint request
        fBreakpointService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fBreakpointService.insertBreakpoint(context, attributes, drm);
            }
        });

        // Wait for the result and return the breakpoint id
        fWait.waitUntilDone(TestsPlugin.massageTimeout(5000));
        return drm.getData();
    }

    /* ------------------------------------------------------------------------
     * removeBreakpoint
     * ------------------------------------------------------------------------
     * Issues a remove breakpoint request.
     * ------------------------------------------------------------------------
     * Typical usage:
     *    IBreakpointDMContext breakpoint = ...;
     *    removeBreakpoint(context, breakpoint);
     *    assertTrue(fWait.getMessage(), fWait.isOK());
     * ------------------------------------------------------------------------
     * @param breakpoint the breakpoint to remove
     * ------------------------------------------------------------------------
     */
    private void removeBreakpoint(final IBreakpointDMContext breakpoint) throws InterruptedException
    {
    	// Clear the completion waiter
		fWait.waitReset();

        // Set the Request Monitor
        final RequestMonitor rm =
            new RequestMonitor(fBreakpointService.getExecutor(), null) {
                @Override
                protected void handleCompleted() {
                    fWait.waitFinished(getStatus());
                }
            };

        // Issue the add breakpoint request
        fBreakpointService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fBreakpointService.removeBreakpoint(breakpoint, rm);
            }
        });

        // Wait for the result
        fWait.waitUntilDone(TestsPlugin.massageTimeout(5000));
    }

    /* ------------------------------------------------------------------------
     * updateBreakpoint
     * ------------------------------------------------------------------------
     * Issues an update breakpoint request.
     * ------------------------------------------------------------------------
     * Typical usage:
     *    updateBreakpoint(context, breakpoint, properties);
     *    assertTrue(fWait.getMessage(), fWait.isOK());
     * ------------------------------------------------------------------------
     * @param breakpoint the breakpoint to update
     * @param delta      the delta properties
     * ------------------------------------------------------------------------
     */
    private void updateBreakpoint(final IBreakpointDMContext breakpoint,
    		final Map<String, Object> delta) throws InterruptedException
    {
    	// Clear the completion waiter
		fWait.waitReset();

        // Set the Request Monitor
        final RequestMonitor rm =
            new RequestMonitor(fBreakpointService.getExecutor(), null) {
                @Override
                protected void handleCompleted() {
                    fWait.waitFinished(getStatus());
                }
            };

        // Issue the update breakpoint request
        fBreakpointService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fBreakpointService.updateBreakpoint(breakpoint, delta, rm);
            }
        });

        // Wait for the result
        fWait.waitUntilDone(TestsPlugin.massageTimeout(5000));
    }

    // ========================================================================
    // Test Cases
    // ========================================================================

	///////////////////////////////////////////////////////////////////////////
    // Add Catchpoint tests
    ///////////////////////////////////////////////////////////////////////////

    @Test
	public void insertCatchpoint_InvalidContext() throws Throwable {

		// Attempt to create a catchpoint with an invalid execution context (should fail)
		Map<String, Object> breakpoint = new HashMap<String, Object>();
		breakpoint.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.CATCHPOINT);
		breakpoint.put(MIBreakpoints.CATCHPOINT_TYPE, "throw");
		insertBreakpoint(null, breakpoint);

		// Ensure it failed
		String expected = UNKNOWN_EXECUTION_CONTEXT;
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected message to contain: '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure that no breakpoint events were received
		assertEquals("Unexpected number of breakpoint events", 0, totalBreakpointEventsCount());		
    }

	// Long story. There's really no way for the user to set a disabled
	// catchpoint/breakpoint/tracepoint, so this test is invalid. If a
	// catchpoint is disabled prior to launching a session, then we simply defer
	// telling gdb about it until the user enables it. It was done this way
	// because until recently, gdb did not support indicating the enable state
	// at creation time, and changing the enable state after creation is
	// susceptible to race condition problems (a non-stopped thread could hit it
	// during the small window where it's enabled). At some point, we should
	// change the implementation to use the new gdb capability to create a
	// disabled breakpoint. When we do, this test will become relevant.
	//	@Test
//	public void insertCatchpoint_Disabled() throws Throwable {
//		// Create a catchpoint		
//		Map<String, Object> breakpoint = new HashMap<String, Object>();
//		breakpoint.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.CATCHPOINT);
//		breakpoint.put(MIBreakpoints.CATCHPOINT_TYPE, "throw");
//		breakpoint.put(MIBreakpoints.IS_ENABLED, false);
//
//		// Perform the test
//		IBreakpointDMContext ref = insertBreakpoint(fBreakpointsDmc, breakpoint);
//		assertTrue(fWait.getMessage(), fWait.isOK());
//
//		// Ensure that right BreakpointEvents were received
//		waitForBreakpointEvent(1);
//		int count = totalBreakpointEventsCount();
//		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT event(s), received "
//				+ count, count == 1);
//		assertTrue("BreakpointEvent problem: expected " + 1 + " BREAKPOINT_ADDED event(s), received "
//				+ getBreakpointEventCount(BP_ADDED), getBreakpointEventCount(BP_ADDED) == 1);
//		clearEventCounters();
//
//		// Ensure that the breakpoint was correctly installed
//		MIBreakpointDMData breakpoint1 = (MIBreakpointDMData) getBreakpoint(ref);
//		assertTrue("BreakpointService problem: breakpoint mismatch (wrong condition)",
//				breakpoint1.getCondition().equals(NO_CONDITION));
//		assertTrue("BreakpointService problem: breakpoint mismatch (wrong ignore count)",
//				breakpoint1.getIgnoreCount() == 0);
//		assertTrue("BreakpointService problem: breakpoint mismatch (wrong state)",
//				!breakpoint1.isEnabled());
//
//		// Ensure the BreakpointService holds only the right breakpoints
//		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
//		assertTrue("BreakpointService problem: expected " + 1 + " breakpoint(s), received "
//				+ breakpoints.length, breakpoints.length == 1);
//		MIBreakpointDMData breakpoint2 = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
//		assertEquals(breakpoint1.getNumber(), breakpoint2.getNumber());
//		assertFalse(breakpoint2.isEnabled());
//    }

	@Test
	public void insertCatchpoint_Simple() throws Throwable {
		IBreakpointDMContext ref = setCatchpoint("throw", null, null);
		resumeAndExpectBkptHit(((MIBreakpointDMData)getBreakpoint(ref)).getNumber(), 0);
	}

	/**
	 * Set a conditional catchpoint. Ensure that it is set correctly in the
	 * back-end. This doesn't actually run the target to see if the catchpoint
	 * behaves correctly.
	 */
	@Test
	public void insertCatchpoint_Condition() throws Throwable {
		IBreakpointDMContext ref = setCatchpoint("throw", CONDITION_1, null);
		resumeAndExpectBkptHit(((MIBreakpointDMData)getBreakpoint(ref)).getNumber(), 2);
	}

	/**
	 * Set a catchpoint with an ignore count. Ensure that it is set correctly in
	 * the back-end. This doesn't actually run the target to see if the
	 * catchpoint behaves correctly.
	 */
	@Test
	public void insertCatchpoint_IgnoreCnt() throws Throwable {
		IBreakpointDMContext ref = setCatchpoint("throw", null, 3);
		resumeAndExpectBkptHit(((MIBreakpointDMData)getBreakpoint(ref)).getNumber(), 3);
 	}

	/**
	 * Set two different catchpoints and ensure they are set correctly in the back-end.
	 * This doesn't actually run the target to see if the catchpoints behaves
	 * correctly.
	 */
	@Test
	public void insertCatchpoint_MultipleCatchpoints() throws Throwable {
		// Set a throw catchpoint
		IBreakpointDMContext ref = setCatchpoint("throw", null, null);
		MIBreakpointDMData bkpt1_set = (MIBreakpointDMData) getBreakpoint(ref);

		// Set a catch catchpoint
		ref = setCatchpoint("catch", null, null);
		MIBreakpointDMData bkpt2_set = (MIBreakpointDMData) getBreakpoint(ref);

		// Ensure the breakpoint service sees what we expect
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoint service reports unexpected number of breakpoints", 2, breakpoints.length);
		MIBreakpointDMData bkpt1_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		MIBreakpointDMData bkpt2_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[1]);

		// The breakpoint references are not necessarily retrieved in the order the
		// breakpoints were initially set...
		if (bkpt1_svc.getNumber() == bkpt1_set.getNumber()) {
			assertEquals(bkpt2_svc.getNumber(), bkpt2_set.getNumber());
		}
		else {
			assertEquals(bkpt1_svc.getNumber(), bkpt2_set.getNumber());
			assertEquals(bkpt2_svc.getNumber(), bkpt1_set.getNumber());
		}
	}

	/**
	 * Set two identical catchpoints and ensure they are set correctly in the
	 * back-end. GDB has no problem with this. This doesn't actually run the
	 * target to see if the catchpoints behaves correctly.
	 */
	@Test
	public void insertCatchpoint_Duplicate() throws Throwable {
		// Set a throw catchpoint
		IBreakpointDMContext ref = setCatchpoint("throw", null, null);
		MIBreakpointDMData bkpt1_set = (MIBreakpointDMData) getBreakpoint(ref);

		// Tell gdb to set a throw catchpoint AGAIN
		ref = setCatchpoint("throw", null, null);
		MIBreakpointDMData bkpt2_set = (MIBreakpointDMData) getBreakpoint(ref);

		// Ensure the breakpoint service sees what we expect
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoint service reports unexpected number of breakpoints", 2, breakpoints.length);
		MIBreakpointDMData bkpt1_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		MIBreakpointDMData bkpt2_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[1]);

		// The breakpoint references are not necessarily retrieved in the order the
		// breakpoints were initially set...
		if (bkpt1_svc.getNumber() == bkpt1_set.getNumber()) {
			assertEquals(bkpt2_svc.getNumber(), bkpt2_set.getNumber());
		}
		else {
			assertEquals(bkpt1_svc.getNumber(), bkpt2_set.getNumber());
			assertEquals(bkpt2_svc.getNumber(), bkpt1_set.getNumber());
		}
	}

	/**
	 * Set a catchpoint while the target is running and ensure it gets hit. 
	 */
	@Test
	public void insertCatchpoint_WhileTargetRunning() throws Throwable {
		// Interrupting the target on Windows is susceptible to an additional,
		// unwanted suspension. That means that silently interrupting the target
		// to set/modify/remove a breakpoint then resuming it can leave the
		// target in a suspended state. Unfortunately, there is nothing
		// practical CDT can do to address this issue except wait for the gdb
		// folks to resolve it. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=304096#c27
	    if (Platform.getOS().equals(Platform.OS_WIN32)) {
	    	return;
	    }
		
		// Run the program. It will make a two second sleep() call, during which time... 
		SyncUtil.resume();
		
		// Set a throw catchpoint; don't use the utility method since it assumes
		// the target is running
		Map<String, Object> bkptsProps = new HashMap<String, Object>();
		bkptsProps.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.CATCHPOINT);
		bkptsProps.put(MIBreakpoints.CATCHPOINT_TYPE, "throw");
		insertBreakpoint(fBreakpointsDmc, bkptsProps);
		assertTrue(fWait.getMessage(), fWait.isOK());
		
		// After the sleep, the test app throws a C++ exception. Wait for the
		// catchpoint to hit and for the expected number of breakpoint events to
		// have occurred
		MIStoppedEvent event = SyncUtil.waitForStop(3000);
		waitForBreakpointEvent(2);
		
		// Ensure that right breakpoint events were received. One indicating the
		// catchpoint was created, another indicating it was hit
		waitForBreakpointEvent(1);
		assertEquals("Unexpected number of breakpoint events", 2, totalBreakpointEventsCount());
		assertEquals("Unexpected number of breakpoint-added events", 1, getBreakpointEventCount(BP_ADDED));
		assertEquals("Unexpected number of breakpoint-hit events", 1, getBreakpointEventCount(BP_HIT));		
		clearEventCounters();
		
		assertTrue("Did not stop because of catchpoint, but stopped because of: " +
				event.getClass().getCanonicalName(), event instanceof MIBreakpointHitEvent);
	}

	/**
	 * Set a catchpoint and remove it. This doesn't actually run the target to
	 * see if the removed catchpoint has no effect.
	 */
	@Test
	public void removeCatchpoint_SimpleCase() throws Throwable {
		// Set a throw catchpoint
		IBreakpointDMContext ref = setCatchpoint("throw", null, null);

		// Remove the cachpoint
		clearEventCounters();
		removeBreakpoint(ref);
		assertTrue(fWait.getMessage(), fWait.isOK());

		// Ensure that right breakpoint events were received
		waitForBreakpointEvent(1);
		assertEquals("Unexpected number of breakpoint events", 1, totalBreakpointEventsCount());
		assertEquals("Unexpected number of breakpoint-added events", 1, getBreakpointEventCount(BP_REMOVED));
		clearEventCounters();

		// Ensure the breakpoint was effectively removed
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 0, breakpoints.length);
	}

	/**
	 * Set a catchpoint, remove it, then try to remove it again; that should
	 * fail. Set a second catchpoint, try removing the first one (again), which
	 * should again fail, but this time make sure the second catchpoint is
	 * unaffected. This doesn't actually run the target to see if the
	 * insalled/removed catchpoints behave correctly.
	 */
	@Test
	public void removeCatchpoint_InvalidBreakpoint() throws Throwable {
		// set a catchpoint
		IBreakpointDMContext bkptRef1 = setCatchpoint("throw", null, null);
		
		// Remove the installed breakpoint
		clearEventCounters();
		removeBreakpoint(bkptRef1);
		assertTrue(fWait.getMessage(), fWait.isOK());

		// Ensure that right breakpoints events were received
		waitForBreakpointEvent(1);
		assertEquals("Unexpected number of breakpoint events", 1, totalBreakpointEventsCount());
		assertEquals("Unexpected number of breakpoint-added events", 1, getBreakpointEventCount(BP_REMOVED));
		clearEventCounters();

		// Ensure the breakpoint service sees what we expect 
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 0, breakpoints.length);

		// Try removing the catchpoint again; should fail
		removeBreakpoint(bkptRef1);
		assertFalse(fWait.getMessage(), fWait.isOK());
		String expected = UNKNOWN_BREAKPOINT;
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no breakpoint events were received
		assertEquals("Unexpected number of breakpoint events", 0, totalBreakpointEventsCount());

		// Ensure the breakpoint service sees what we expect 
		breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 0, breakpoints.length);

		// Re-install the catchpoint
		IBreakpointDMContext bkptRef2 = setCatchpoint("throw", null, null);
		clearEventCounters();

		// Try removing the un-installed breakpoint again; should fail
		removeBreakpoint(bkptRef1);
		assertFalse(fWait.getMessage(), fWait.isOK());
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no breakpoint events were received
		assertEquals("Unexpected number of breakpoint events", 0, totalBreakpointEventsCount());

		// Ensure that the recently set breakpoint is unaffected
		breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 1, breakpoints.length);
		MIBreakpointDMData bkpt2_set = (MIBreakpointDMData) getBreakpoint(bkptRef2);
		MIBreakpointDMData bkpt2_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		assertEquals(bkpt2_set.getNumber(), bkpt2_svc.getNumber());		
	}

	/**
	 * Set a series of distinct catchpoints then remove them in a different
	 * order. This doesn't actually run the target to see if the
	 * installed/removed catchpoints behave correctly.
	 */
	@Test
	public void removeCatchpoint_MixedOrder() throws Throwable {
		final String[] events = new String[] { "throw", "catch", "exec", "fork" };

		// Set the catchpoints
		for (String event : events) {
			setCatchpoint(event, null, null);
		}

		// Get the list of breakpoints
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoint service reports unexpected number of breakpoints", events.length, breakpoints.length);		

		// Remove the catchpoints one at a time but in an order different than how they were added
		int[] whichOne = { 0, 2, 1, 3 };
		int breakpoints_left = 4;
		for (int i = 0; i < whichOne.length; i++) {
			clearEventCounters();
			
			// Remove one of the catchpoints
			IBreakpointDMContext removeThisBreakpoint = breakpoints[whichOne[i]];
			removeBreakpoint(removeThisBreakpoint);
			fWait.waitUntilDone(TestsPlugin.massageTimeout(5000));
			assertTrue(fWait.getMessage(), fWait.isOK());

			// Ensure that right breakpoint events were received
			waitForBreakpointEvent(1);
			assertEquals("Unexpected number of breakpoint events", 1, totalBreakpointEventsCount());
			assertEquals("Unexpected number of breakpoint-added events", 1, getBreakpointEventCount(BP_REMOVED));
			
			// Ensure the breakpoint service sees what we expect 
			IBreakpointDMContext[] remaining_breakpoints = getBreakpoints(fBreakpointsDmc);
			assertEquals("Breakpoints service reports unexpected number of breakpoints", --breakpoints_left, remaining_breakpoints.length);
			for (int j = 0; j < breakpoints_left; j++) {
				assertTrue("BreakpointService problem: removed breakpoint still present (" + removeThisBreakpoint + ")",
						!remaining_breakpoints[j].equals(removeThisBreakpoint));
			}
		}
	}

	/**
	 * Set a throw and a catch catchpoint while the target is
	 * stopped, then remove the throw catchpoint while the target is running and
	 * ensure the catch catchpoint is hit.
	 */
	@Test
	public void removeCatchpoint_WhileTargetRunning1() throws Throwable {
		removeCatchpoint_WhileTargetRunning(true);
	}

	/**
	 * Variant that removes the catch catchpoint instead of the throw one. See
	 * {@link #removeCatchpoint_WhileTargetRunning1()}
	 */
	@Test
	public void removeCatchpoint_WhileTargetRunning2() throws Throwable {
		removeCatchpoint_WhileTargetRunning(false);
	}

	/**
	 * See {@link #removeCatchpoint_WhileTargetRunning1()}
	 * @param removeThrow
	 *            if true, we remove the throw catchpoint, otherwise the catch
	 *            one.
	 */
	private void removeCatchpoint_WhileTargetRunning(boolean removeThrow) throws Throwable {
		// Interrupting the target on Windows is susceptible to an additional,
		// unwanted suspension. That means that silently interrupting the target
		// to set/modify/remove a breakpoint then resuming it can leave the
		// target in a suspended state. Unfortunately, there is nothing
		// practical CDT can do to address this issue except wait for the gdb
		// folks to resolve it. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=304096#c27
	    if (Platform.getOS().equals(Platform.OS_WIN32)) {
	    	return;
	    }
		
		// Set a line breakpoint at the sleep() call. We need to get the program
		// past the initial loop that throws and catches C++ exceptions.
		IBreakpointDMContext refLineBkpt = setLineBreakpoint(LINE_NUMBER_SLEEP_CALL);

		// Run to the breakpoint
		resumeAndExpectBkptHit(((MIBreakpointDMData) getBreakpoint(refLineBkpt)).getNumber(), null);
		
		// Set the two catchpoints
		IBreakpointDMContext refThrow = setCatchpoint("throw", null, null);
		IBreakpointDMContext refCatch = setCatchpoint("catch", null, null);

		// Run the program. It will make a two second sleep() call, during which time...
		clearEventCounters();		
		SyncUtil.resume();

		// ...we remove one of the catchpoints
		removeBreakpoint(removeThrow ? refThrow : refCatch);
		assertTrue(fWait.getMessage(), fWait.isOK());

		// After the sleep, the test app throws a C++ exception and catches it.
		// The catchpoint we DIDN'T remove should stop the program
		// Wait for catchpoint to hit and for the expected number of breakpoint
		// events to have occurred
		MIStoppedEvent event = SyncUtil.waitForStop(3000);
		waitForBreakpointEvent(2);
		assertTrue("stopped event is of an unexpected type: " + event.getClass().getName(), event instanceof MIBreakpointHitEvent);
		MIBreakpointHitEvent bkptHitEvent = (MIBreakpointHitEvent)event; 
		MIBreakpointDMData bkptNotRemoved = (MIBreakpointDMData) getBreakpoint(removeThrow ? refCatch : refThrow);
		assertEquals("Target stopped as expected, but the responsible breakpoint was not the expected one", bkptNotRemoved.getNumber(), bkptHitEvent.getNumber());
		
		// If we removed the catch exception, we don't know at this point that
		// it won't get hit; we're stopped at the throw catchpoint. So resume
		// the target and make sure it doesn't get hit.
		if (!removeThrow) {
			clearEventCounters();
			SyncUtil.resume();
			Thread.sleep(1000); // give the program a second to run to completion
			assertEquals("Unexpected number of breakpoint events", 0, totalBreakpointEventsCount());			
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Catchpoint Update tests
	///////////////////////////////////////////////////////////////////////////


	/**
	 * Add a catchpoint with no condition then modify the condition.
	 */
	@Test
	public void updateCatchpoint_AddCondition() throws Throwable {
		// Set a catchpoint with no condition
		IBreakpointDMContext ref = setCatchpoint("throw", null, null);
		
		// Update the catchpoint to have a condition
		modifyBkptProperty(ref, MIBreakpoints.CONDITION, CONDITION_1);		

		// Ensure the breakpoint service sees what we expect 
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 1, breakpoints.length);
		MIBreakpointDMData bkpt_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		assertEquals("Incorrect breakpoint condition", CONDITION_1, bkpt_svc.getCondition());
		
		resumeAndExpectBkptHit(bkpt_svc.getNumber(), 2);
	}
	
	/**
	 * Add a catchpoint with a condition then remove the condition
	 */
	@Test
	public void updateCatchpoint_RemoveCondition() throws Throwable {
		// Set a catchpoint with a condition
		IBreakpointDMContext ref = setCatchpoint("throw", CONDITION_1, null);
		
		// Remove the condition
		modifyBkptProperty(ref, MIBreakpoints.CONDITION, null);

		// Ensure the breakpoint service sees what we expect 
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 1, breakpoints.length);
		MIBreakpointDMData bkpt_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		assertEquals("Incorrect breakpoint condition", CONDITION_NONE, bkpt_svc.getCondition());
		
		resumeAndExpectBkptHit(bkpt_svc.getNumber(), 0);
	}

	/**
	 * Add a catchpoint with a condition then modify the condition
	 */
	@Test
	public void updateCatchpoint_ModifyCondition() throws Throwable {
		// Set the catchpoint with a particular condition
		IBreakpointDMContext ref = setCatchpoint("throw", CONDITION_1, null);

		// Modify the catchpoint to have a different condition
		modifyBkptProperty(ref, MIBreakpoints.CONDITION, CONDITION_2);

		// Ensure the breakpoint service sees what we expect 
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 1, breakpoints.length);
		MIBreakpointDMData bkpt_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		assertEquals("Incorrect breakpoint condition", CONDITION_2, bkpt_svc.getCondition());

		resumeAndExpectBkptHit(bkpt_svc.getNumber(), 4);
	}

	/**
	 * Set a throw and a catch catchpoint while the target is stopped, with a
	 * condition that will never resolve to true. The program should breeze
	 * through the loop that throws and catches C++ exceptions and then enter a
	 * sleep call. During the sleep call, Then remove the throw catchpoint while
	 * the target is running and ensure the catch catchpoint is hit.
	 * 
	 */
	@Test
	public void updateCatchpoint_WhileTargetRunning1() throws Throwable {
		updateCatchpoint_WhileTargetRunning(true);
	}

	/**
	 * Variant that removes the catch catchpoint instead of the throw one. See
	 * {@link #removeCatchpoint_WhileTargetRunning1()}
	 */
	@Test
	public void updateCatchpoint_WhileTargetRunning2() throws Throwable {
		updateCatchpoint_WhileTargetRunning(false);
	}

	/**
	 * Set catch and throw catchpoints with a condition that will never be true,
	 * and also a line breakpoint at the sleep call, then resume the target. The
	 * initial part of the program has a loop that throws and catches C++
	 * exceptions. We should breeze on past that loop because of the invalid
	 * catchpoint conditions and end up stopped at the line breakpoint. We
	 * resume the target. The program makes a sleep call (two seconds), during
	 * which time we attempt to update the condition of one of the catchpooints
	 * to something that will resolve to true. After the sleep, the program does
	 * one more round of throwing and catching. Ensure that the target stops and
	 * that it's because of the catchpoint we updated.
	 * 
	 * @param removeThrow
	 *            if true, we update the throw catchpoint, otherwise the catch
	 *            one.
	 */
	private void updateCatchpoint_WhileTargetRunning(boolean modifyThrow) throws Throwable {
		// Interrupting the target on Windows is susceptible to an additional,
		// unwanted suspension. That means that silently interrupting the target
		// to set/modify/remove a breakpoint then resuming it can leave the
		// target in a suspended state. Unfortunately, there is nothing
		// practical CDT can do to address this issue except wait for the gdb
		// folks to resolve it. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=304096#c27
	    if (Platform.getOS().equals(Platform.OS_WIN32)) {
	    	return;
	    }
		
		// Set a line breakpoint at the sleep() call. 
		IBreakpointDMContext refLineBkpt = setLineBreakpoint(LINE_NUMBER_SLEEP_CALL);

		// Set the two catchpoints
		IBreakpointDMContext refThrow = setCatchpoint("throw", CONDITION_NEVER_MET, null);
		IBreakpointDMContext refCatch = setCatchpoint("catch", CONDITION_NEVER_MET, null);

		// Run the program. The catchpoints should not get hit, but the line
		// breakpoint should
		clearEventCounters();
		SyncUtil.resume();
		waitForBreakpointEvent(1);
		assertEquals("Unexpected number of breakpoint events", 1, totalBreakpointEventsCount());
		assertEquals("Unexpected number of breakpoint-added events", 1, getBreakpointEventCount(BP_HIT));
		MIBreakpointDMData lineBkpt = (MIBreakpointDMData) getBreakpoint(refLineBkpt);
		assertEquals("Target stopped as expected, but the responsible breakpoint was not the expected one", lineBkpt.getNumber(), fBreakpointRef);
		clearEventCounters();
		
		// Resume the program. It will make a one second sleep() call, during which time...
		SyncUtil.resume();
		
		// ...we modify one of the catchpoints's condition
		modifyBkptProperty(modifyThrow ? refThrow : refCatch, MIBreakpoints.CONDITION, CONDITION_ALWAYS_MET);

		// After the sleep, the test app throws a C++ exception and catches it.
		// So, the catchpoint whose condition we modified should get hit
		// Wait for breakpoint to hit and for the expected number of breakpoint events to have occurred 
		MIStoppedEvent event = SyncUtil.waitForStop(3000);
		waitForBreakpointEvent(2);
		assertTrue("stopped event is of an unexpected type: " + event.getClass().getName(), event instanceof MIBreakpointHitEvent);
		MIBreakpointHitEvent bkptHitEvent = (MIBreakpointHitEvent)event; 
		MIBreakpointDMData bkptUpdated = (MIBreakpointDMData) getBreakpoint(modifyThrow ? refThrow : refCatch);
		assertEquals("Target stopped as expected, but the responsible breakpoint was not the expected one", bkptUpdated.getNumber(), bkptHitEvent.getNumber());
	}

	@Test
	public void updateCatchpoint_AddCount() throws Throwable {
		// Set the catchpoint with a particular condition
		IBreakpointDMContext ref = setCatchpoint("throw", null, null);

		// Modify the catchpoint to have a different condition
		modifyBkptProperty(ref, MIBreakpoints.IGNORE_COUNT, 3);

		// Ensure the breakpoint service sees what we expect 
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 1, breakpoints.length);
		MIBreakpointDMData bkpt_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		assertEquals("Incorrect breakpoint condition", 3, bkpt_svc.getIgnoreCount());
		
		// Resume and validate catchpoint hit
		resumeAndExpectBkptHit(bkpt_svc.getNumber(), 3);
	}

	/**
	 * Set a catchpoint with an ignore count, then remove the ignore count.
	 */
	@Test
	public void updateCatchpoint_RemoveCount() throws Throwable {
		// Set the catchpoint with a particular condition
		IBreakpointDMContext ref = setCatchpoint("throw", null, 3);

		// Modify the catchpoint to not have an ignore count
		modifyBkptProperty(ref, MIBreakpoints.IGNORE_COUNT, null);
		
		// Ensure the breakpoint service sees what we expect 
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 1, breakpoints.length);
		MIBreakpointDMData bkpt_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		assertEquals("Incorrect breakpoint ignore count", 0, bkpt_svc.getIgnoreCount());
		
		// Resume and validate catchpoint hit
		resumeAndExpectBkptHit(bkpt_svc.getNumber(), 0);
	}

	/**
	 * Set a catchpoint with a particular ignore count and then update the
	 * catchpoint to have a different ignore count
	 */
	@Test
	public void updateCatchpoint_ModifyCount() throws Throwable {
		// Set the catchpoint with a particular ignore  count
		IBreakpointDMContext ref = setCatchpoint("throw", null, 3);

		// Modify the catchpoint to have a different ignore count
		modifyBkptProperty(ref, MIBreakpoints.IGNORE_COUNT, 5);

		// Ensure the breakpoint service sees what we expect 
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 1, breakpoints.length);
		MIBreakpointDMData bkpt_svc = (MIBreakpointDMData) getBreakpoint(breakpoints[0]);
		assertEquals("Incorrect breakpoint ignore count", 5, bkpt_svc.getIgnoreCount());

		// Resume and validate catchpoint hit
		resumeAndExpectBkptHit(bkpt_svc.getNumber(), 5);
	}

	/**
	 * Set two catchpoints. Disable one and ensure it isn't hit. Enable it and
	 * ensure it is hit.
	 */
	@Test
	public void updateCatchpoint_Disable() throws Throwable {

		// Set the catchpoints
		IBreakpointDMContext refThrow = setCatchpoint("throw", null, null);
		IBreakpointDMContext refCatch = setCatchpoint("catch", null, null);
		
		// Disable the throw catchpoint
		modifyBkptProperty(refThrow, MIBreakpoints.IS_ENABLED, false);

		// Ensure the breakpoint service sees what we expect 
		IBreakpointDMContext[] breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 2, breakpoints.length);
		int throwCatchpointNumber = ((MIBreakpointDMData)getBreakpoint(refThrow)).getNumber();
		for (IBreakpointDMContext bkpt : breakpoints) {
			MIBreakpointDMData bkpt_svc = (MIBreakpointDMData) getBreakpoint(bkpt);
			assertEquals("Incorrect breakpoint condition", throwCatchpointNumber != bkpt_svc.getNumber(), bkpt_svc.isEnabled());
		}

		// Resume the target. Should miss the throw catchpoint and stop at the catch one
		int catchCatchpointNumber = ((MIBreakpointDMData)getBreakpoint(refCatch)).getNumber();
		resumeAndExpectBkptHit(catchCatchpointNumber, null);
		
		// Ee-enable the throw catchpoint
		modifyBkptProperty(refThrow, MIBreakpoints.IS_ENABLED, true);

		// Ensure the breakpoint service sees what we expect 
		breakpoints = getBreakpoints(fBreakpointsDmc);
		assertEquals("Breakpoints service reports unexpected number of breakpoints", 2, breakpoints.length);
		for (IBreakpointDMContext bkpt : breakpoints) {
			MIBreakpointDMData bkpt_svc = (MIBreakpointDMData) getBreakpoint(bkpt);
			assertEquals("Incorrect breakpoint condition", true, bkpt_svc.isEnabled());
		}

		// Resume the target. Should miss the throw catchpoint and stop at the catch one
		resumeAndExpectBkptHit(throwCatchpointNumber, null);
	}

	/**
	 * Test some utiility methods we use to convert between event breakpoint ids
	 * and gdb catchpoint keywords
	 */
	@Test
	public void catchpointConversions() throws Throwable {
		assertEquals("catch", GdbCatchpoints.eventToGdbCatchpointKeyword(IEventBreakpointConstants.EVENT_TYPE_CATCH));
		assertEquals("syscall", GdbCatchpoints.eventToGdbCatchpointKeyword(IEventBreakpointConstants.EVENT_TYPE_SYSCALL));
		assertEquals(IEventBreakpointConstants.EVENT_TYPE_CATCH, GdbCatchpoints.gdbCatchpointKeywordToEvent("catch"));
		assertEquals(IEventBreakpointConstants.EVENT_TYPE_SYSCALL, GdbCatchpoints.gdbCatchpointKeywordToEvent("syscall"));
		assertNull(GdbCatchpoints.gdbCatchpointKeywordToEvent("signa"));
		assertNull(GdbCatchpoints.gdbCatchpointKeywordToEvent("signals"));
	}

	/**
	 * Set a line breakpoint and validate it was set correctly.
	 * 
	 * @param lineNumber
	 *            the line where to set the breakpoint
	 * @return the breakpoint context
	 */
	private IBreakpointDMContext setLineBreakpoint(int lineNumber) throws Exception {
		clearEventCounters();

		IBreakpointDMContext[] bkptsBefore = getBreakpoints(fBreakpointsDmc);
		
		// Set the breakpoint
		Map<String, Object> breakpoint = new HashMap<String, Object>();
		breakpoint.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
		breakpoint.put(MIBreakpoints.FILE_NAME, SOURCE_FILE);
		breakpoint.put(MIBreakpoints.LINE_NUMBER, lineNumber);
		IBreakpointDMContext refLineBkpt = insertBreakpoint(fBreakpointsDmc, breakpoint);
		assertTrue(fWait.getMessage(), fWait.isOK());

		// Ensure that right breakpoint events were received.
		waitForBreakpointEvent(1);
		assertEquals("Unexpected number of breakpoint events", 1, totalBreakpointEventsCount());
		assertEquals("Unexpected number of breakpoint-added events", 1, getBreakpointEventCount(BP_ADDED));

		// Ensure the breakpoint service sees what we expect
		List<IBreakpointDMContext> bkptsAfter = new LinkedList<IBreakpointDMContext>(Arrays.asList(getBreakpoints(fBreakpointsDmc)));
		assertEquals("Breakpoints service reports unexpected number of breakpoints", bkptsBefore.length + 1, bkptsAfter.size());
		
		ListIterator<IBreakpointDMContext> iter = bkptsAfter.listIterator();
		while (iter.hasNext()) {
			IBreakpointDMContext bkptAfter = iter.next();
			boolean found = false;
			for (IBreakpointDMContext bkptBefore : bkptsBefore) {
				if (bkptAfter.equals(bkptBefore)) {
					assertFalse("shouldn't have been more than one match", found);
					iter.remove();
					found = true;
				}
			}
		}
		assertEquals("All but the new bkpt should have been removed from bkptsAfter", bkptsAfter.size(), 1);

		return refLineBkpt;
	}

	/**
	 * Set a catchpoint for the given event and validate it was set correctly
	 * 
	 * @param event
	 *            the event; the gdb keyword for it (e.g., "catch", "throw")
	 * @param condition
	 *            an optional condition, or null to indicate not condition
	 * @param ignoreCount
	 *            an optional ignore count, or null to indicate no ignore count
	 * @return the breakpoint context
	 */
	private IBreakpointDMContext setCatchpoint(String event, String condition, Integer ignoreCount) throws Exception {
		clearEventCounters();

		IBreakpointDMContext[] bkptsBefore = getBreakpoints(fBreakpointsDmc);
		
		// set the catchpoint
		Map<String, Object> bkptsProps = new HashMap<String, Object>();
		bkptsProps.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.CATCHPOINT);
		bkptsProps.put(MIBreakpoints.CATCHPOINT_TYPE, event);
		if (condition != null) {
			bkptsProps.put(MIBreakpoints.CONDITION, condition);
		}
		if (ignoreCount != null) {
			bkptsProps.put(MIBreakpoints.IGNORE_COUNT, ignoreCount);
		}
		IBreakpointDMContext refCatchpoint = insertBreakpoint(fBreakpointsDmc, bkptsProps);
		assertTrue(fWait.getMessage(), fWait.isOK());
		
		// Ensure that right breakpoint events were received.
		waitForBreakpointEvent(1);
		assertEquals("Unexpected number of breakpoint events", 1, totalBreakpointEventsCount());
		assertEquals("Unexpected number of breakpoint-added events", 1, getBreakpointEventCount(BP_ADDED));

		// Ensure the breakpoint service sees what we expect. Ask the breakpoint
		// service for the list of breakpoint against and make sure it differs
		// only by the newly added one
		List<IBreakpointDMContext> bkptsAfter = new LinkedList<IBreakpointDMContext>(Arrays.asList(getBreakpoints(fBreakpointsDmc)));
		assertEquals("Breakpoints service reports unexpected number of breakpoints", bkptsBefore.length + 1, bkptsAfter.size());
		ListIterator<IBreakpointDMContext> iter = bkptsAfter.listIterator();
		while (iter.hasNext()) {
			IBreakpointDMContext bkptAfter = iter.next();
			boolean found = false;
			for (IBreakpointDMContext bkptBefore : bkptsBefore) {
				if (bkptAfter.equals(bkptBefore)) {
					assertFalse("shouldn't have been more than one match", found);
					iter.remove();
					found = true;
				}
			}
		}
		assertEquals("All but the new bkpt should have been removed from bkptsAfter", bkptsAfter.size(), 1);
		
		MIBreakpointDMData bkpt_set = (MIBreakpointDMData) getBreakpoint(refCatchpoint);
		MIBreakpointDMData bkpt_svc = (MIBreakpointDMData) getBreakpoint(bkptsAfter.get(0));
		
		assertEquals(bkpt_set.getNumber(), bkpt_svc.getNumber());		
		assertEquals("Incorrect breakpoint condition", condition != null ? condition : CONDITION_NONE, bkpt_svc.getCondition());
		assertEquals("Incorrect breakpoint ignore count", ignoreCount != null ? ignoreCount : 0, bkpt_svc.getIgnoreCount());

		return refCatchpoint;
	}

	/**
	 * Resume the target and expect it to be stopped by the given breakpoint.
	 * Optionally, check that the program's single global int variable has the
	 * given value.
	 * 
	 * @param bkptNumber
	 *            the GDB breakpoint number
	 * @param expectedVarValue
	 *            the expected value of the program variable; can be null to
	 *            indicate a check isn't wanted
	 * @return the stoppped event
	 */
	private MIStoppedEvent resumeAndExpectBkptHit(int bkptNumber, Integer expectedVarValue) throws Throwable {
		// Resume the target. The throw catchpoint should get hit. 
		clearEventCounters();
		MIStoppedEvent event = SyncUtil.resumeUntilStopped();

		// Ensure the right breakpoint events were received
		waitForBreakpointEvent(1);
		assertEquals("Unexpected number of breakpoint events", 1, totalBreakpointEventsCount());
		assertEquals("Unexpected type of breakpoint event", 1, getBreakpointEventCount(BP_HIT));
		
		// Ensure the target stopped because of the throw catchpoint
		assertEquals("Target stopped as expected, but the responsible breakpoint was not the expected one", bkptNumber, fBreakpointRef);
		
		if (expectedVarValue != null) {
	        IFrameDMContext frameDmc = SyncUtil.getStackFrame(event.getDMContext(), 0);
			assertEquals("program variable has unexpected value", expectedVarValue.intValue(), evaluateExpression(frameDmc, CONDITION_VAR).intValue());
		}
		return event;
	}

	/**
	 * Modify a single property of a single breakpoint and validate that a
	 * breakpoint updated event occurs
	 */
	private void modifyBkptProperty(IBreakpointDMContext bkptRef, String property, Object value) throws Throwable {
		// Modify the catchpoint to not have an ignore count
		clearEventCounters();
		Map<String, Object> bkptProps = new HashMap<String, Object>();
		bkptProps.put(property, value);
		updateBreakpoint(bkptRef, bkptProps);
		assertTrue(fWait.getMessage(), fWait.isOK());
	
		// Ensure that right breakpoint events were received
		waitForBreakpointEvent(1);
		assertEquals("Unexpected number of breakpoint events", 1, totalBreakpointEventsCount());
		assertEquals("Unexpected number of breakpoint added events", 1, getBreakpointEventCount(BP_UPDATED));
	}
	
}
