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
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_0;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IMultiRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Tests IMultiRunControl class for Non-stop multi-threaded application. 
 */
@RunWith(BackgroundRunner.class)
public class GDBMultiNonStopRunControlTest_7_0 extends BaseTestCase {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_0);
	}

	private DsfServicesTracker fServicesTracker;    

	private IMultiRunControl fMultiRun;
		
	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";
	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThreadRunControl.exe";
	
	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();
			
		final DsfSession session = getGDBLaunch().getSession();
		
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
           	fServicesTracker = 
            		new DsfServicesTracker(TestsPlugin.getBundleContext(), 
            				session.getId());
            	fMultiRun = fServicesTracker.getService(IMultiRunControl.class);
            }
        };
        session.getExecutor().submit(runnable).get();
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           EXEC_PATH + EXEC_NAME);

		// Multi run control only makes sense for non-stop mode
    	setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();
		
		fServicesTracker.dispose();
	}
	
	private abstract class AsyncRunnable<V> {
	    public abstract void run(DataRequestMonitor<V> drm);
	};

	private <V> V runAsyncCall(final AsyncRunnable<V> runnable) {
		return runAsyncCall(runnable, false);
	}

	private <V> V runAsyncCall(final AsyncRunnable<V> runnable, boolean expectExecutionException) {
		Query<V> query = new Query<V>() {
    		@Override
    		protected void execute(DataRequestMonitor<V> rm) {
    			runnable.run(rm);
    		}
    	};
    	
    	V result = null;
    	try {
    		fMultiRun.getExecutor().execute(query);
    		result = query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		if (expectExecutionException) {
    			return null;
    		}
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    	
    	if (expectExecutionException) {
    		fail("Didn't get the expected execution exception");
    	}
    	
    	return result;
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the run-state of multiple threads
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * with one thread which is stopped.
	 */
	@Test
	public void testStateOneThreadStopped() throws Throwable {
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);
		
		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(threads, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(threads, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);

		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(threads, drm);
			}
		});
		assertFalse("expected not to be able to suspend all, but can", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(threads, drm);
			}
		});
		assertFalse("expected not to be able to suspend some, but can", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected to find all threads suspended but didn't", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertTrue("expected to find some threads suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step all, but cannot", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but cannot", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}
	
	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * with one thread which is running.
	 */
	@Test
	public void testStateOneThreadRunning() throws Throwable {
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);

		// Resume the program to check thread while it is running
		SyncUtil.resume();
		
		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(threads, drm);
			}
		});
		assertFalse("expected not to be able to resume all, but can", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(threads, drm);
			}
		});
		assertFalse("expected not to be able to resume some, but can", result);
	
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(threads, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(threads, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertFalse("expected to find no thread suspended but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertFalse("expected to find no thread suspended but did", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected to not be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step some, but can", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * with two threads which are both stopped.
	 */
	@Test
	public void testStateTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

		Boolean result;

		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(threads, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(threads, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(threads, drm);
			}
		});
		assertFalse("expected not to be able to suspend all, but can", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(threads, drm);
			}
		});
		assertFalse("expected not to be able to suspend some, but can", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected to find all threads suspended but didn't", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertTrue("expected to find some threads suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step all, but can't", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but can't", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * with two threads, one of which is stopped and the other running.
	 */
	@Test
	public void testStateTwoThreadsStoppedAndRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(threads, drm);
			}
		});
		assertFalse("expected to not be able to resume all, but can", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(threads, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(threads, drm);
			}
		});
		assertFalse("expected not to be able to suspend all, but can", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(threads, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertFalse("expected that not all threads are suspended but they are", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertTrue("expected to find some threads suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but can't", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}
	
	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * with two threads which are both running.
	 */
	@Test
	public void testStateTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(threads, drm);
			}
		});
		assertFalse("expected not to be able to resume all, but can", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(threads, drm);
			}
		});
		assertFalse("expected not to be able to resume some, but can", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(threads, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(threads, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertFalse("expected that no threads are suspended but they are", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertFalse("expected to find no threads suspended but did", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step some, but can", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(threads, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the resume operation on multiple threads
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test resume of multiple contexts with one thread which is stopped.
	 */
	@Test
	public void testResumeOneThreadStopped() throws Throwable {
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);
		
        final ServiceEventWaitor<MIRunningEvent> eventWaitor =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(threads, drm);
			}
		});

		eventWaitor.waitForEvent(100); // Wait for confirmation thread resumed
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);
	}
	
	/**
	 * Test resume of multiple contexts with one thread which is running.
	 */
	@Test
	public void testResumeOneThreadRunning() throws Throwable {
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);

		// Resume the program to get thread running
		SyncUtil.resume();

		// Confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(threads, drm);
			}
		});

		// Confirm that all threads are still running
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);

	}

	/**
	 * Test resume of multiple contexts with two stopped threads.  Only one thread
	 * is resumed.
	 */
	@Test
	public void testResumeTwoThreadsStoppedResumeOne() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
		eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(threads[0], drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation that one thread resumed
		
		// Also confirm that only one threads is running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertTrue("expected some threads to be suspended, but found none", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertFalse("expected not to find all threads suspended, but did", result);
		
		// Make sure no other running event arrives
		try {
			eventWaitorRunning.waitForEvent(500); 
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}

	/**
	 * Test resume of multiple contexts with two stopped threads.  Both threads
	 * are resumed.
	 */
	@Test
	public void testResumeTwoThreadsStoppedResumeTwo() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
		eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(threads, drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation first thread resumed
		eventWaitorRunning.waitForEvent(100); // Wait for confirmation second thread resumed
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);
	}

	/**
	 * Test resume of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 */
	@Test
	public void testResumeTwoThreadsStoppedAndRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(threads, drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation one thread resumed
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no other running event arrives
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	/**
	 * Test resume of multiple contexts with two running threads.
	 */
	@Test
	public void testResumeTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);
		
        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(threads, drm);
			}
		});
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no running events arrive
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the suspend operation on multiple threads
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test suspend of multiple contexts with one thread which is stopped.
	 */
	@Test
	public void testSuspendOneThreadStopped() throws Throwable {
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);
		
        // No error should be thrown, the already suspended threads should be ignored
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(threads, drm);
			}
		});

		// Also confirm that all threads are still suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}
	
	/**
	 * Test suspend of multiple contexts with one thread which is running.
	 */
	@Test
	public void testSuspendOneThreadRunning() throws Throwable {
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);

		// Resume the program to get thread running
		SyncUtil.resume();

		// Confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);

        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(threads, drm);
			}
		});

		eventWaitor.waitForEvent(100);  // Thread should interrupt
		
		// Confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two stopped threads.
	 */
	@Test
	public void testSuspendTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
		eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

        // No error should be thrown, the already suspended threads should be ignored
        runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(threads, drm);
			}
		});

		// Also confirm that all threads are still suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 */
	@Test
	public void testSuspendTwoThreadsStoppedAndRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

		// No error should be thrown, the call should ignore the suspended threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(threads, drm);
			}
		});

		eventWaitorStopped.waitForEvent(100); // Wait for confirmation one thread stopped
		
		// Also confirm that all threads are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);		
	}
	
	/**
	 * Test suspend of multiple contexts with two running threads. Only one
	 * thread will be suspended.
	 */
	@Test
	public void testSuspendTwoThreadsRunningSuspendOne() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);
		
		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(threads[0], drm);
			}
		});
		
		eventWaitor.waitForEvent(100);  // confirm one thread was suspended
		
		// Also confirm that some but not all threads are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(threads, drm);
			}
		});
		assertTrue("expected some threads to be suspended, but found none", result);
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertFalse("expected that not all threads are suspended, but they are", result);
		
		try {
			eventWaitor.waitForEvent(500); // Make sure no other stopped event arrives
			fail("Got an unexpected stopped event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}

	/**
	 * Test suspend of multiple contexts with two running threads.  Both threads
	 * will be suspended
	 */
	@Test
	public void testSuspendTwoThreadsRunningSuspendTwo() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);
		
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(threads, drm);
			}
		});
		
		eventWaitor.waitForEvent(100);  // confirm one thread was suspended
		eventWaitor.waitForEvent(100);  // confirm the other thread was suspended
		
		// Also confirm that all threads are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected that all threads are suspended, but they are not", result);		
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the step operation on multiple threads
    //////////////////////////////////////////////////////////////////////

	/**
	 * Test that the feature is not implemented.  Once this fails, we will
	 * know we have new tests to write to test the feature.
	 */
	@Test
	public void testStepNotImplemented() throws Throwable {
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);
		
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			runAsyncCall(new AsyncRunnable<Object>() { 
				@Override public void run(DataRequestMonitor<Object> drm) {
					fMultiRun.step(threads, type, drm);
				}
			}, true /* Not implemented yet*/);
		}

	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the run-state of the process.
	// This should be done with multi-process but we are not quite setup for it.
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with one thread which is stopped.
	 */
	@Test
	public void testStateProcessOneThreadStopped() throws Throwable {		
		final IContainerDMContext[] processes = 
				new IContainerDMContext[] { SyncUtil.getContainerContext() };
		
		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(processes, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(processes, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);

		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(processes, drm);
			}
		});
		assertFalse("expected not to be able to suspend all, but can", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(processes, drm);
			}
		});
		assertFalse("expected not to be able to suspend some, but can", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertTrue("expected to find all processes suspended but didn't", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertTrue("expected to find some processes suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step all, but cannot", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but cannot", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);
	}
	
	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with one thread which is running.
	 */
	@Test
	public void testStateProcessOneThreadRunning() throws Throwable {
		final IContainerDMContext[] processes = 
				new IContainerDMContext[] { SyncUtil.getContainerContext() };

		// Resume the program to check thread while it is running
		SyncUtil.resume();
		
		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(processes, drm);
			}
		});
		assertFalse("expected not to be able to resume all, but can", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(processes, drm);
			}
		});
		assertFalse("expected not to be able to resume some, but can", result);
	
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(processes, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(processes, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertFalse("expected to find no thread suspended but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertFalse("expected to find no thread suspended but did", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected to not be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step some, but can", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads which are both stopped.
	 */
	@Test
	public void testStateProcessTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		final IContainerDMContext[] processes = 
				new IContainerDMContext[] { SyncUtil.getContainerContext() };

		Boolean result;

		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(processes, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(processes, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(processes, drm);
			}
		});
		assertFalse("expected not to be able to suspend all, but can", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(processes, drm);
			}
		});
		assertFalse("expected not to be able to suspend some, but can", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertTrue("expected to find all threads suspended but didn't", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertTrue("expected to find some threads suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step all, but can't", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but can't", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads, one of which is stopped and the other running.
	 */
	@Test
	public void testStateProcessTwoThreadsStoppedAndRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		final IContainerDMContext[] processes = 
				new IContainerDMContext[] { SyncUtil.getContainerContext() };

		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(processes, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(processes, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(processes, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(processes, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but can't", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertTrue("expected that all processes are suspended but they are not", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertTrue("expected to find some processes suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but can't", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);
	}
	
	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads which are both running.
	 */
	@Test
	public void testStateProcessTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IContainerDMContext[] processes = 
				new IContainerDMContext[] { SyncUtil.getContainerContext() };

		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(processes, drm);
			}
		});
		assertFalse("expected not to be able to resume all, but can", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(processes, drm);
			}
		});
		assertFalse("expected not to be able to resume some, but can", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(processes, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(processes, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertFalse("expected that no threads are suspended but they are", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertFalse("expected to find no threads suspended but did", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step some, but can", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(processes, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);
	}

	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the run-state of the process and a thread.
	// Because the thread is part of the process, it should be ignored,
	// and the results should be as if only the process was selected.
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with one thread which is stopped.
	 * We also select the thread.
	 */
	@Test
	public void testStateProcessThreadOneThreadStopped() throws Throwable {		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);

		final IExecutionDMContext[] execDmcs = 
				new IExecutionDMContext[] { SyncUtil.getContainerContext(), threads[0] };
		
		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);

		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to suspend all, but can", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to suspend some, but can", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected to find all processes suspended but didn't", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertTrue("expected to find some processes suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step all, but cannot", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but cannot", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);
	}
	
	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with one thread which is running.
	 * We also select the thread.
	 */
	@Test
	public void testStateProcessThreadOneThreadRunning() throws Throwable {
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);

		final IExecutionDMContext[] execDmcs = 
				new IExecutionDMContext[] { SyncUtil.getContainerContext(), threads[0] };

		// Resume the program to check thread while it is running
		SyncUtil.resume();
		
		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to resume all, but can", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to resume some, but can", result);
	
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertFalse("expected to find no thread suspended but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected to find no thread suspended but did", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected to not be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step some, but can", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads which are both stopped.
	 * We also select the thread.
	 */
	@Test
	public void testStateProcessThreadTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 2);

		final IExecutionDMContext[] execDmcs = 
				new IExecutionDMContext[] { SyncUtil.getContainerContext(), threads[0] };

		Boolean result;

		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to suspend all, but can", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to suspend some, but can", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected to find all threads suspended but didn't", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertTrue("expected to find some threads suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step all, but can't", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but can't", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads, one of which is stopped and the other running.
	 * We also select the first thread.
	 */
	@Test
	public void testStateProcessThreadTwoThreadsStoppedAndRunning_1() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 2);

		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };

		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected that all processes are suspended but they are not", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertTrue("expected to find some processes suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but can't", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads, one of which is stopped and the other running.
	 * We also select the second thread.
	 */
	@Test
	public void testStateProcessThreadTwoThreadsStoppedAndRunning_2() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 2);

		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(1) };

		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected that all processes are suspended but they are not", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertTrue("expected to find some processes suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but can't", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads which are both running.
 	 * We also select both threads
	 */
	@Test
	public void testStateProcessThreadTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 2);

		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), threads[0] };

		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to resume all, but can", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to resume some, but can", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertFalse("expected that no threads are suspended but they are", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected to find no threads suspended but did", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step some, but can", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the run-state of the process and both threads.
	// Because the threads are part of the process, they should be ignored,
	// and the results should be as if only the process was selected.
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads which are both stopped.
	 * We also select both threads.
	 */
	@Test
	public void testStateProcessThreadsTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 2);

		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), threads[0], threads[1] };

		Boolean result;

		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to suspend all, but can", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to suspend some, but can", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected to find all threads suspended but didn't", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertTrue("expected to find some threads suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step all, but can't", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but can't", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no thread stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads, one of which is stopped and the other running.
	 * We also select both threads.
	 */
	@Test
	public void testStateProcessThreadsTwoThreadsStoppedAndRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 2);

		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), threads[0], threads[1] };

		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume all, but cannot", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to resume some, but cannot", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected that all processes are suspended but they are not", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertTrue("expected to find some processes suspended but didn't", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertTrue("expected to be able to step some, but can't", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);
	}

	/**
	 * Test canResume*, canSuspend*, isSuspended*, canStep*, isStepping*
	 * on the process with two threads which are both running.
	 * We also select both threads
	 */
	@Test
	public void testStateProcessThreadsTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 2);

		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), threads[0], threads[1] };

		Boolean result;
		
		// canResume calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeAll(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to resume all, but can", result);
		
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canResumeSome(execDmcs, drm);
			}
		});
		assertFalse("expected not to be able to resume some, but can", result);
		
		// canSuspend calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendAll(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend all, but cannot", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.canSuspendSome(execDmcs, drm);
			}
		});
		assertTrue("expected to be able to suspend some, but cannot", result);

		// isSuspended calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertFalse("expected that no threads are suspended but they are", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected to find no threads suspended but did", result);
		
		// canStep calls
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepAll(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step all, but can", result);

			result = runAsyncCall(new AsyncRunnable<Boolean>() { 
				@Override public void run(DataRequestMonitor<Boolean> drm) {
					fMultiRun.canStepSome(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
			// assertFalse("expected not to be able to step some, but can", result);
		}

		// isStepping calls
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingAll(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);

		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSteppingSome(execDmcs, drm);
			}
		}, true /* Not implemented yet*/);
		// assertFalse("expected to find no process stepping but did", result);
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the multi resume operation on processes
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test resume of multiple contexts with one thread which is stopped.
	 */
	@Test
	public void testResumeProcessOneThreadStopped() throws Throwable {
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };
		
        final ServiceEventWaitor<MIRunningEvent> eventWaitor =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(processes, drm);
			}
		});

		eventWaitor.waitForEvent(100); // Wait for confirmation process resumed
		
		// Also confirm that process is running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);
	}
	
	/**
	 * Test resume of multiple contexts with one thread which is running.
	 */
	@Test
	public void testResumeProcessOneThreadRunning() throws Throwable {
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };

		// Resume the program to get thread running
		SyncUtil.resume();

		// Confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);

		// No error should be thrown, the call should ignore running processes
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(processes, drm);
			}
		});

		// Confirm that all threads are still running
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);

	}

	/**
	 * Test resume of multiple contexts with two stopped threads.  Only one thread
	 * is resumed.
	 */
	@Test
	public void testResumeProcessTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
		eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(processes, drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation that one thread resumed
		eventWaitorRunning.waitForEvent(100); // Wait for confirmation that second thread resumed

		// Also confirm that all processes are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);
	}

	/**
	 * Test resume of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 */
	@Test
	public void testResumeProcessTwoThreadsStoppedAndRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(processes, drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation one thread resumed
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no other running event arrives
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	/**
	 * Test resume of multiple contexts with two running threads.
	 */
	@Test
	public void testResumeProcessTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };
		
        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(processes, drm);
			}
		});
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no running events arrive
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the suspend operation on processes
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test suspend of multiple contexts with one thread which is stopped.
	 */
	@Test
	public void testSuspendProcessOneThreadStopped() throws Throwable {
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);

        // No error should be thrown, the already suspended processes should be ignored
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(processes, drm);
			}
		});

		// Also confirm that all threads are still suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);

		// Also confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
}
	
	/**
	 * Test suspend of multiple contexts with one thread which is running.
	 */
	@Test
	public void testSuspendProcessOneThreadRunning() throws Throwable {
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };

		// Resume the program to get thread running
		SyncUtil.resume();

		// Confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(processes, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);

        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(processes, drm);
			}
		});

		eventWaitor.waitForEvent(100);  // Thread should interrupt
		
		// Confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two stopped threads.
	 */
	@Test
	public void testSuspendProcessTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
		eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

        // No error should be thrown, the already suspended threads should be ignored
        runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(processes, drm);
			}
		});

		// Also confirm that all processes are still suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);
		
		// Also confirm that all threads are still suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 */
	@Test
	public void testSuspendProcessTwoThreadsStoppedAndRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

		// No error should be thrown, the call should ignore the suspended threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(processes, drm);
			}
		});

		eventWaitorStopped.waitForEvent(100); // Wait for confirmation one thread stopped
		
		// Also confirm that all processes are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);	
		
		// Also confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two running threads.  Both threads
	 * should be suspended by suspending the process.
	 */
	@Test
	public void testSuspendProcessTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);
		
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(processes, drm);
			}
		});
		
		eventWaitor.waitForEvent(100);  // confirm one thread was suspended
		eventWaitor.waitForEvent(100);  // confirm the other thread was suspended
		
		// Also confirm that all processes are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(processes, drm);
			}
		});
		assertTrue("expected that all processes are suspended, but they are not", result);
		
		// Also confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected that all threads are suspended, but they are not", result);
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the step operation on multiple threads
    //////////////////////////////////////////////////////////////////////

	/**
	 * Test that the feature is not implemented.  Once this fails, we will
	 * know we have new tests to write to test the feature.
	 */
	@Test
	public void testStepProcessNotImplemented() throws Throwable {
		final IExecutionDMContext[] processes = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext() };
		
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			runAsyncCall(new AsyncRunnable<Object>() { 
				@Override public void run(DataRequestMonitor<Object> drm) {
					fMultiRun.step(processes, type, drm);
				}
			}, true /* Not implemented yet*/);
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the different operations on the process and a thread.
	// Because the thread is part of the process, it should be ignored,
	// and the results should be as if only the process was selected.
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test resume of multiple contexts with one thread which is stopped.
	 * We select the process and the first thread.
	 */
	@Test
	public void testResumeProcessThreadOneThreadStopped() throws Throwable {
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };
		
        final ServiceEventWaitor<MIRunningEvent> eventWaitor =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(execDmcs, drm);
			}
		});

		eventWaitor.waitForEvent(100); // Wait for confirmation process resumed
		
		// Also confirm that process is running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no contexts to be suspended, but found some", result);
		
		try {
			eventWaitor.waitForEvent(500); // Make sure no running events arrive
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	/**
	 * Test resume of multiple contexts with one thread which is running.
	 * We select the process and the first thread.
	 */
	@Test
	public void testResumeProcessThreadOneThreadRunning() throws Throwable {
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };

		// Resume the program to get thread running
		SyncUtil.resume();
		
		// Confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no contexts to be suspended, but found some", result);

		final ServiceEventWaitor<MIRunningEvent> eventWaitor =
				new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running processes
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(execDmcs, drm);
			}
		});

		// Confirm that all threads are still running
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no contexts to be suspended, but found some", result);

		try {
			eventWaitor.waitForEvent(500); // Make sure no running events arrive
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}

	/**
	 * Test resume of multiple contexts with two stopped threads.  Only one thread
	 * is resumed.
	 * We select the process and the first thread.
	 */
	@Test
	public void testResumeProcessThreadTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
		eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(execDmcs, drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation that one thread resumed
		eventWaitorRunning.waitForEvent(100); // Wait for confirmation that second thread resumed

		// Also confirm that all processes are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no running events arrive
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}

	/**
	 * Test resume of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 * We select the process and the first thread.
	 */
	@Test
	public void testResumeProcessThreadTwoThreadsStoppedAndRunning_1() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(execDmcs, drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation one thread resumed
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no other running event arrives
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	/**
	 * Test resume of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 * We select the process and the second thread.
	 */
	@Test
	public void testResumeProcessThreadTwoThreadsStoppedAndRunning_2() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(1) };

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(execDmcs, drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation one thread resumed
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no other running event arrives
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	/**
	 * Test resume of multiple contexts with two running threads.
	 * We select the process and the first thread.
	 */
	@Test
	public void testResumeProcessThreadTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };
		
        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(execDmcs, drm);
			}
		});
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no running events arrive
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the suspend operation on processes
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test suspend of multiple contexts with one thread which is stopped.
	 * We select the process and the first thread.
	 */
	@Test
	public void testSuspendProcessThreadOneThreadStopped() throws Throwable {
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };
		
		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected a single thread but got " + threads.length, threads.length == 1);

        // No error should be thrown, the already suspended processes should be ignored
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(execDmcs, drm);
			}
		});

		// Also confirm that all threads are still suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);

		// Also confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}
	
	/**
	 * Test suspend of multiple contexts with one thread which is running.
	 * We select the process and the first thread.
	 */
	@Test
	public void testSuspendProcessThreadOneThreadRunning() throws Throwable {
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };

		// Resume the program to get thread running
		SyncUtil.resume();

		// Confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);

        final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(execDmcs, drm);
			}
		});

		eventWaitor.waitForEvent(100);  // Thread should interrupt
		
		// Confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two stopped threads.
	 * We select the process and the first thread.
	 */
	@Test
	public void testSuspendProcessThreadTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
		eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

        // No error should be thrown, the already suspended threads should be ignored
        runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(execDmcs, drm);
			}
		});

		// Also confirm that all processes are still suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);
		
		// Also confirm that all threads are still suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 * We select the process and the first thread.
	 */
	@Test
	public void testSuspendProcessThreadTwoThreadsStoppedAndRunning_1() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

		// No error should be thrown, the call should ignore the suspended threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(execDmcs, drm);
			}
		});

		eventWaitorStopped.waitForEvent(100); // Wait for confirmation one thread stopped
		
		// Also confirm that all processes are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);	
		
		// Also confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}
	
	/**
	 * Test suspend of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 * We select the process and the second thread.
	 */
	@Test
	public void testSuspendProcessThreadTwoThreadsStoppedAndRunning_2() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(1) };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

		// No error should be thrown, the call should ignore the suspended threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(execDmcs, drm);
			}
		});

		eventWaitorStopped.waitForEvent(100); // Wait for confirmation one thread stopped
		
		// Also confirm that all processes are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);	
		
		// Also confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two running threads.  Both threads
	 * should be suspended by suspending the process.
	 * We select the process and the first thread.
	 */
	@Test
	public void testSuspendProcessThreadTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);
		
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(execDmcs, drm);
			}
		});
		
		eventWaitor.waitForEvent(100);  // confirm one thread was suspended
		eventWaitor.waitForEvent(100);  // confirm the other thread was suspended
		
		// Also confirm that all processes are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected that all processes are suspended, but they are not", result);
		
		// Also confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected that all threads are suspended, but they are not", result);
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the step operation on multiple threads
    //////////////////////////////////////////////////////////////////////

	/**
	 * Test that the feature is not implemented.  Once this fails, we will
	 * know we have new tests to write to test the feature.
	 * We select the process and the first thread.
	 */
	@Test
	public void testStepProcessThreadNotImplemented() throws Throwable {
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0) };
		
		for (final IRunControl.StepType type : IRunControl.StepType.values()) {
			runAsyncCall(new AsyncRunnable<Object>() { 
				@Override public void run(DataRequestMonitor<Object> drm) {
					fMultiRun.step(execDmcs, type, drm);
				}
			}, true /* Not implemented yet*/);
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the different operations on the process and both threads.
	// Because the threads are part of the process, they should be ignored,
	// and the results should be as if only the process was selected.
    //////////////////////////////////////////////////////////////////////
	
	/**
	 * Test resume of multiple contexts with two stopped threads.  Only one thread
	 * is resumed.
	 * We select the process and both threads.
	 */
	@Test
	public void testResumeProcessThreadsTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
		eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0), SyncUtil.getExecutionContext(1) };

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(execDmcs, drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation that one thread resumed
		eventWaitorRunning.waitForEvent(100); // Wait for confirmation that second thread resumed

		// Also confirm that all processes are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no running events arrive
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}

	/**
	 * Test resume of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 * We select the process and both threads.
	 */
	@Test
	public void testResumeProcessThreadsTwoThreadsStoppedAndRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0), SyncUtil.getExecutionContext(1) };

        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(execDmcs, drm);
			}
		});

		eventWaitorRunning.waitForEvent(100); // Wait for confirmation one thread resumed
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no process to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no other running event arrives
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	/**
	 * Test resume of multiple contexts with two running threads.
	 * We select the process and both threads.
	 */
	@Test
	public void testResumeProcessThreadsTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0), SyncUtil.getExecutionContext(1) };
		
        final ServiceEventWaitor<MIRunningEvent> eventWaitorRunning =
                new ServiceEventWaitor<MIRunningEvent>(fMultiRun.getSession(), MIRunningEvent.class);

		// No error should be thrown, the call should ignore running threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.resume(execDmcs, drm);
			}
		});
		
		// Also confirm that all threads are running
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedSome(execDmcs, drm);
			}
		});
		assertFalse("expected no threads to be suspended, but found some", result);
		
		try {
			eventWaitorRunning.waitForEvent(500); // Make sure no running events arrive
			fail("Got an unexpected running event");
		} catch (Exception e) {
			// Timeout expected.  Success.
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Tests for verifying the suspend operation on processes
    //////////////////////////////////////////////////////////////////////

	/**
	 * Test suspend of multiple contexts with two stopped threads.
	 * We select the process and both threads.
	 */
	@Test
	public void testSuspendProcessThreadsTwoThreadsStopped() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);
		
        final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
		eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0), SyncUtil.getExecutionContext(1) };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

        // No error should be thrown, the already suspended threads should be ignored
        runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(execDmcs, drm);
			}
		});

		// Also confirm that all processes are still suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);
		
		// Also confirm that all threads are still suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two threads, one which is stopped
	 * while the other is running.
	 * We select the process and both threads.
	 */
	@Test
	public void testSuspendProcessThreadsTwoThreadsStoppedAndRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitorStopped =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
        eventWaitorStopped.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitorStopped.waitForEvent(2000); // Wait for first thread to stop
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0), SyncUtil.getExecutionContext(1) };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);

		// No error should be thrown, the call should ignore the suspended threads
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(execDmcs, drm);
			}
		});

		eventWaitorStopped.waitForEvent(100); // Wait for confirmation one thread stopped
		
		// Also confirm that all processes are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected all processes to be suspended, but they are not", result);	
		
		// Also confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected all threads to be suspended, but they are not", result);
	}

	/**
	 * Test suspend of multiple contexts with two running threads.  Both threads
	 * should be suspended by suspending the process.
	 * We select the process and the first thread.
	 */
	@Test
	public void testSuspendProcessThreadsTwoThreadsRunning() throws Throwable {

		// Run program until both threads are stopped
		SyncUtil.addBreakpoint("firstBreakpoint", false);

		final ServiceEventWaitor<MIStoppedEvent> eventWaitor =
                new ServiceEventWaitor<MIStoppedEvent>(fMultiRun.getSession(), MIStoppedEvent.class);

		SyncUtil.resume();		
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
        eventWaitor.waitForEvent(2000); // Wait for second thread to stop
		
		// Now resume program again and wait for one of the two threads to stop
		SyncUtil.resume();
        eventWaitor.waitForEvent(2000); // Wait for first thread to stop
		
		// Now resume the thread again to have both running
		SyncUtil.resume();
		
		final IExecutionDMContext[] execDmcs = new IExecutionDMContext[] { 
				SyncUtil.getContainerContext(), SyncUtil.getExecutionContext(0), SyncUtil.getExecutionContext(1) };

		final IMIExecutionDMContext[] threads = SyncUtil.getExecutionContexts();
		assertTrue("Expected two threads but got " + threads.length, threads.length == 2);
		
		runAsyncCall(new AsyncRunnable<Object>() { 
			@Override public void run(DataRequestMonitor<Object> drm) {
				fMultiRun.suspend(execDmcs, drm);
			}
		});
		
		eventWaitor.waitForEvent(100);  // confirm one thread was suspended
		eventWaitor.waitForEvent(100);  // confirm the other thread was suspended
		
		// Also confirm that all processes are suspended
		Boolean result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(execDmcs, drm);
			}
		});
		assertTrue("expected that all processes are suspended, but they are not", result);
		
		// Also confirm that all threads are suspended
		result = runAsyncCall(new AsyncRunnable<Boolean>() { 
			@Override public void run(DataRequestMonitor<Boolean> drm) {
				fMultiRun.isSuspendedAll(threads, drm);
			}
		});
		assertTrue("expected that all threads are suspended, but they are not", result);
	}
}
