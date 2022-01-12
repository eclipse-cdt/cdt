/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor.ICanceledListener;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that exercise the Query object.
 */
public class DsfQueryTests {
	TestDsfExecutor fExecutor;

	@Before
	public void startServices() throws ExecutionException, InterruptedException {
		fExecutor = new TestDsfExecutor();
	}

	@After
	public void shutdownServices() throws ExecutionException, InterruptedException {
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fExecutor.shutdown();
			}
		}).get();
		if (fExecutor.exceptionsCaught()) {
			Throwable[] exceptions = fExecutor.getExceptions();
			throw new ExecutionException(exceptions[0]);
		}
		fExecutor = null;
	}

	@Test
	public void simpleGetTest() throws InterruptedException, ExecutionException {
		Query<Integer> q = new Query<Integer>() {
			@Override
			protected void execute(DataRequestMonitor<Integer> rm) {
				rm.setData(1);
				rm.done();
			}
		};
		// Check initial state
		assertTrue(!q.isDone());
		assertTrue(!q.isCancelled());

		fExecutor.execute(q);
		assertEquals(1, (int) q.get());

		// Check final state
		assertTrue(q.isDone());
		assertTrue(!q.isCancelled());

	}

	@Test
	public void getErrorTest() throws InterruptedException, ExecutionException {
		final String error_message = "Test Error";

		Query<Integer> q = new Query<Integer>() {
			@Override
			protected void execute(DataRequestMonitor<Integer> rm) {
				rm.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
						error_message, null)); //$NON-NLS-1$
				rm.done();
			}
		};

		// Check initial state
		assertTrue(!q.isDone());
		assertTrue(!q.isCancelled());

		fExecutor.execute(q);

		try {
			q.get();
			fail("Expected exception");
		} catch (ExecutionException e) {
			assertEquals(e.getCause().getMessage(), error_message);
		}

		// Check final state
		assertTrue(q.isDone());
		assertTrue(!q.isCancelled());

	}

	@Test
	public void doneExceptionTest() throws InterruptedException, ExecutionException {
		Query<Integer> q = new Query<Integer>() {
			@SuppressWarnings("deprecation")
			@Override
			protected void execute(DataRequestMonitor<Integer> rm) {
				doneException(new Throwable());
			}
		};

		// Check initial state
		assertTrue(!q.isDone());
		assertTrue(!q.isCancelled());

		fExecutor.execute(q);

		try {
			q.get();
			fail("Expected exception");
		} catch (ExecutionException e) {
		}

		// Check final state
		assertTrue(q.isDone());
		assertTrue(!q.isCancelled());

	}

	@Test
	public void getWithMultipleDispatchesTest() throws InterruptedException, ExecutionException {
		Query<Integer> q = new Query<Integer>() {
			@Override
			protected void execute(final DataRequestMonitor<Integer> rm) {
				fExecutor.execute(new DsfRunnable() {
					@Override
					public void run() {
						rm.setData(1);
						rm.done();
					}

					@Override
					public String toString() {
						return super.toString() + "\n       getWithMultipleDispatchesTest() second runnable"; //$NON-NLS-1$
					}
				});
			}

			@Override
			public String toString() {
				return super.toString() + "\n       getWithMultipleDispatchesTest() first runnable (query)"; //$NON-NLS-1$
			}
		};
		fExecutor.execute(q);
		assertEquals(1, (int) q.get());
	}

	@Test(expected = ExecutionException.class)
	public void exceptionOnGetTest() throws InterruptedException, ExecutionException {
		Query<Integer> q = new Query<Integer>() {
			@Override
			protected void execute(final DataRequestMonitor<Integer> rm) {
				rm.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, -1, "", null)); //$NON-NLS-1$
				rm.done();
			}
		};

		fExecutor.execute(q);

		try {
			q.get();
		} finally {
			assertTrue(q.isDone());
			assertTrue(!q.isCancelled());
		}
	}

	@Test
	public void cancelBeforeWaitingTest() throws InterruptedException, ExecutionException {
		final Query<Integer> q = new Query<Integer>() {
			@Override
			protected void execute(final DataRequestMonitor<Integer> rm) {
				fail("Query was cancelled, it should not be called."); //$NON-NLS-1$
				rm.done();
			}
		};

		// Cancel before invoking the query.
		q.cancel(false);

		assertTrue(q.isDone());
		assertTrue(q.isCancelled());

		// Start the query.
		fExecutor.execute(q);

		// Block to retrieve data
		try {
			q.get();
		} catch (CancellationException e) {
			return; // Success
		} finally {
			assertTrue(q.isDone());
			assertTrue(q.isCancelled());
		}
		assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
	}

	@Test
	public void cancelWhileWaitingTest() throws InterruptedException, ExecutionException {
		final DataRequestMonitor<?>[] rmHolder = new DataRequestMonitor<?>[1];
		final Boolean[] cancelCalled = new Boolean[] { Boolean.FALSE };

		final Query<Integer> q = new Query<Integer>() {
			@Override
			protected void execute(final DataRequestMonitor<Integer> rm) {
				synchronized (rmHolder) {
					rmHolder[0] = rm;
					rmHolder.notifyAll();
				}
			}
		};

		// Start the query.
		fExecutor.execute(q);

		// Wait until the query is started
		synchronized (rmHolder) {
			while (rmHolder[0] == null) {
				rmHolder.wait();
			}
		}

		// Add a cancel listener to the query RM
		rmHolder[0].addCancelListener(new ICanceledListener() {

			@Override
			public void requestCanceled(RequestMonitor rm) {
				cancelCalled[0] = Boolean.TRUE;
			}
		});

		// Cancel running request.
		q.cancel(false);

		assertTrue(cancelCalled[0]);
		assertTrue(rmHolder[0].isCanceled());
		assertTrue(q.isCancelled());
		assertTrue(q.isDone());

		// Retrieve data
		try {
			q.get();
		} catch (CancellationException e) {
			return; // Success
		} finally {
			assertTrue(q.isDone());
			assertTrue(q.isCancelled());
		}

		// Complete rm and query.
		@SuppressWarnings("unchecked")
		DataRequestMonitor<Integer> drm = (DataRequestMonitor<Integer>) rmHolder[0];
		drm.setData(Integer.valueOf(1));
		rmHolder[0].done();

		// Try to retrieve data again, it should still result in
		// cancellation exception.
		try {
			q.get();
		} catch (CancellationException e) {
			return; // Success
		} finally {
			assertTrue(q.isDone());
			assertTrue(q.isCancelled());
		}

		assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
	}

	@Test
	public void getTimeoutTest() throws InterruptedException, ExecutionException {
		final Query<Integer> q = new Query<Integer>() {
			@Override
			protected void execute(final DataRequestMonitor<Integer> rm) {
				// Call done with a delay of 1 second, to avoid stalling the tests.
				fExecutor.schedule(new DsfRunnable() {
					@Override
					public void run() {
						rm.done();
					}
				}, 60, TimeUnit.SECONDS);
			}
		};

		fExecutor.execute(q);

		// Note: no point in checking isDone() and isCancelled() here, because
		// the value could change on timing.

		try {
			q.get(1, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			return; // Success
		} finally {
			assertFalse("Query should not be done yet, it should have timed out first.", q.isDone()); //$NON-NLS-1$
		}
		assertTrue("TimeoutException should have been thrown", false); //$NON-NLS-1$
	}

}
