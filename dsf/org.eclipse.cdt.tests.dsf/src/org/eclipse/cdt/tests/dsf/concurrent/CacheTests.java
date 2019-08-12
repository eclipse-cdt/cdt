/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems and others.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestCache;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that exercise the DataCache object.
 */
public class CacheTests {

	TestDsfExecutor fExecutor;
	TestCache fTestCache;
	DataRequestMonitor<Integer> fRetrieveRm;

	class TestCache extends RequestCache<Integer> {

		public TestCache() {
			super(new ImmediateInDsfExecutor(fExecutor));
		}

		@Override
		protected void retrieve(DataRequestMonitor<Integer> rm) {
			synchronized (CacheTests.this) {
				fRetrieveRm = rm;
				CacheTests.this.notifyAll();
			}
		}

		@Override
		protected void reset() {
			super.reset();
		}

		@Override
		public void set(Integer data, IStatus status) {
			super.set(data, status);
		}

	}

	class TestQuery extends Query<Integer> {
		@Override
		protected void execute(final DataRequestMonitor<Integer> rm) {
			fTestCache.update(new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
				@Override
				protected void handleSuccess() {
					rm.setData(fTestCache.getData());
					rm.done();
				}
			});
		}
	}

	/**
	 * There's no rule on how quickly the cache has to start data retrieval
	 * after it has been requested.  It could do it immediately, or it could
	 * wait a dispatch cycle, etc..
	 */
	private void waitForRetrieveRm() {
		synchronized (this) {
			while (fRetrieveRm == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	@Before
	public void startExecutor() throws ExecutionException, InterruptedException {
		fExecutor = new TestDsfExecutor();
		fTestCache = new TestCache();
	}

	@After
	public void shutdownExecutor() throws ExecutionException, InterruptedException {
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
		fRetrieveRm = null;
		fTestCache = null;
		fExecutor = null;
	}

	private void assertCacheValidWithData(Object data) {
		assertTrue(fTestCache.isValid());
		assertEquals(data, fTestCache.getData());
		assertTrue(fTestCache.getStatus().isOK());
	}

	private void assertCacheResetWithoutData() {
		assertFalse(fTestCache.isValid());
		try {
			fTestCache.getData();
			fail("Expected an IllegalStateException");
		} catch (IllegalStateException e) {
		}
		try {
			fTestCache.getStatus();
			fail("Expected an IllegalStateException");
		} catch (IllegalStateException e) {
		}
	}

	private void assertCacheValidWithoutData() {
		assertTrue(fTestCache.isValid());
		assertEquals(null, fTestCache.getData());
		assertFalse(fTestCache.getStatus().isOK());
		assertEquals(fTestCache.getStatus().getCode(), ERRCODE_TARGET_RUNNING);
	}

	private void assertCacheWaiting() {
		assertFalse(fTestCache.isValid());
		try {
			fTestCache.getData();
			fail("Expected an IllegalStateException");
		} catch (IllegalStateException e) {
		}
		try {
			fTestCache.getStatus();
			fail("Expected an IllegalStateException");
		} catch (IllegalStateException e) {
		}
		assertFalse(fRetrieveRm.isCanceled());
	}

	private void assertCacheInvalidAndWithCanceledRM() {
		assertFalse(fTestCache.isValid());
		try {
			fTestCache.getData();
			fail("Expected an IllegalStateException");
		} catch (IllegalStateException e) {
		}
		try {
			fTestCache.getStatus();
			fail("Expected an IllegalStateException");
		} catch (IllegalStateException e) {
		}
		assertTrue(fRetrieveRm.isCanceled());
	}

	@Test
	public void getWithCompletionInDsfThreadTest() throws InterruptedException, ExecutionException {
		// Request data from cache
		Query<Integer> q = new TestQuery();

		// Check initial state
		assertFalse(fTestCache.isValid());

		fExecutor.execute(q);

		// Wait until the cache requests the data.
		waitForRetrieveRm();

		// Check state while waiting for data
		assertFalse(fTestCache.isValid());

		// Complete the cache's retrieve data request.
		fExecutor.submit(() -> {
			fRetrieveRm.setData(1);
			fRetrieveRm.done();

			// Check that the data is available in the cache immediately
			// (in the same dispatch cycle).
			assertEquals(1, (int) fTestCache.getData());
			assertTrue(fTestCache.isValid());

			return null;
		}).get();

		assertEquals(1, (int) q.get());

		// Re-check final state
		assertCacheValidWithData(1);
	}

	@Test
	public void getTest() throws InterruptedException, ExecutionException {
		// Check initial state
		assertFalse(fTestCache.isValid());

		// Request data from cache
		Query<Integer> q = new TestQuery();
		fExecutor.execute(q);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Check state while waiting for data
		assertFalse(fTestCache.isValid());

		// Set the data without using an executor.
		fRetrieveRm.setData(1);
		fRetrieveRm.done();

		assertEquals(1, (int) q.get());

		// Check final state
		assertCacheValidWithData(1);
	}

	@Test
	public void getTestWithTwoClients() throws InterruptedException, ExecutionException {
		// Check initial state
		assertFalse(fTestCache.isValid());

		// Request data from cache
		Query<Integer> q1 = new TestQuery();
		fExecutor.execute(q1);

		// Request data from cache again
		Query<Integer> q2 = new TestQuery();
		fExecutor.execute(q2);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Check state while waiting for data
		assertFalse(fTestCache.isValid());

		// Set the data without using an executor.
		fRetrieveRm.setData(1);
		fRetrieveRm.done();

		assertEquals(1, (int) q1.get());
		assertEquals(1, (int) q2.get());

		// Check final state
		assertCacheValidWithData(1);
	}

	@Test
	public void getTestWithManyClients() throws InterruptedException, ExecutionException {
		// Check initial state
		assertFalse(fTestCache.isValid());

		// Request data from cache
		List<Query<Integer>> qList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Query<Integer> q = new TestQuery();
			fExecutor.execute(q);
			qList.add(q);
		}
		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Check state while waiting for data
		assertFalse(fTestCache.isValid());

		// Set the data without using an executor.
		fRetrieveRm.setData(1);
		fRetrieveRm.done();

		for (Query<Integer> q : qList) {
			assertEquals(1, (int) q.get());
		}

		// Check final state
		assertCacheValidWithData(1);
	}

	private static final int ERRCODE_TARGET_RUNNING = 1234;
	private static final Status STATUS_TARGET_RUNNING = new Status(Status.ERROR, DsfTestPlugin.PLUGIN_ID,
			ERRCODE_TARGET_RUNNING, "Target is running", null);

	// DISABLE TESTS
	//
	// We say a cache is "disabled" when its most recent attempt to update from
	// the source failed. Also, a cache may make itself disabled as a reaction
	// to a state change notification from its source (e.g., the target
	// resumed). In either case, the cache is in the valid state but it has no
	// data and the status reflects an error. Keep in mind that the 'valid'
	// state is not a reflection of the quality of the data, but merely whether
	// the cache object's representation of the data is stale or
	// not. A transaction that uses a "disabled" cache object will simply fail;
	// it will not ask the cache to update its data from the source. Only a
	// change in the source's state would cause the cache to put itself back in
	// the invalid state, thus opening the door to another update.

	/**
	 * Test behavior when a cache object is asked to update itself after it has
	 * become "disabled". Since a "disabled" cache is in the valid state, a
	 * request for it to update from the source should be ignored.
	 */
	@Test
	public void disableBeforeRequestTest() throws InterruptedException, ExecutionException {
		// Disable the cache
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fTestCache.set(null, STATUS_TARGET_RUNNING);
			}
		}).get();

		assertCacheValidWithoutData();

		// Try to request data from cache
		Query<Integer> q = new TestQuery();
		fExecutor.execute(q);

		Thread.sleep(100);

		// Retrieval should never have been made.
		assertEquals(null, fRetrieveRm);

		// The cache has no data so the query should have failed
		try {
			q.get();
			fail("expected an exeption");
		} catch (ExecutionException e) {
			// expected the exception
		}
	}

	/**
	 * Test behavior when a cache object goes into the "disabled" state while an
	 * update request is ongoing. The subsequent completion of the request should
	 * have no effect on the  cache
	 */
	@Test
	public void disableWhilePendingTest() throws InterruptedException, ExecutionException {
		// Request data from cache
		Query<Integer> q = new TestQuery();
		fExecutor.execute(q);

		// Disable the cache
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fTestCache.set(null, STATUS_TARGET_RUNNING);
			}
		}).get();

		assertCacheValidWithoutData();

		// Complete the retrieve RM. Note that the disabling of the cache above
		// disassociates it from its retrieval RM. Thus regardless of how that
		// request completes, it does not affect the cache.
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fRetrieveRm.setData(1);
				fRetrieveRm.done();
			}
		}).get();

		// Validate that cache is still disabled without data.
		assertCacheValidWithoutData();
	}

	/**
	 * Test behavior when a cache object goes into the "disabled" state while
	 * it's in the valid state. The cache remains in the valid state but it
	 * loses its data and obtains an error status.
	 */
	@Test
	public void disableWhileValidTest() throws InterruptedException, ExecutionException {
		// Request data from cache
		Query<Integer> q = new TestQuery();
		fExecutor.execute(q);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Complete the request
		fRetrieveRm.setData(1);
		fRetrieveRm.done();

		assertEquals(Integer.valueOf(1), q.get());

		// Disable the cache
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fTestCache.set(null, STATUS_TARGET_RUNNING);
			}
		}).get();

		// Check final state
		assertCacheValidWithoutData();
	}

	@Test
	public void setWithValueTest() throws InterruptedException, ExecutionException {
		// Disable the cache
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fTestCache.set(2, Status.OK_STATUS);
			}
		}).get();

		// Validate that cache is disabled without data.
		assertCacheValidWithData(2);
	}

	@Test
	public void cancelWhilePendingTest() throws InterruptedException, ExecutionException {
		// Request data from cache
		Query<Integer> q = new TestQuery();
		fExecutor.execute(q);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Cancel the client request
		q.cancel(true);
		try {
			q.get();
			fail("Expected a cancellation exception");
		} catch (CancellationException e) {
		} // Expected exception;

		assertCacheInvalidAndWithCanceledRM();

		// Simulate the retrieval completing successfully despite the cancel
		// request. Perhaps the retrieval logic isn't checking the RM status.
		// Even if it is checking, it may have gotten passed its last checkpoint
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fRetrieveRm.setData(1);
				fRetrieveRm.done();
			}
		}).get();

		// Validate that cache didn't accept the result after its RM was canceled
		assertCacheInvalidAndWithCanceledRM();
	}

	@Test
	public void cancelWhilePendingTest2() throws InterruptedException, ExecutionException {
		// Request data from cache
		Query<Integer> q = new TestQuery();
		fExecutor.execute(q);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Cancel the client request
		q.cancel(true);
		try {
			q.get();
			fail("Expected a cancellation exception");
		} catch (CancellationException e) {
		} // Expected exception;

		assertCacheInvalidAndWithCanceledRM();

		// Simulate retrieval logic that is regularly checking the RM's cancel
		// status and has discovered that the request has been canceled. It
		// technically does not need to explicitly set a cancel status object in
		// the RM, thanks to RequestMonitor.getStatus() automatically returning
		// Status.CANCEL_STATUS when its in the cancel state. So here we
		// simulate the retrieval logic just aborting its operations and
		// completing the RM. Note that it hasn't provided the data to the
		// cache.
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fRetrieveRm.done();
			}
		}).get();

		assertCacheInvalidAndWithCanceledRM();
	}

	@Test
	public void cancelWhilePendingTest3() throws InterruptedException, ExecutionException {
		// Request data from cache
		Query<Integer> q = new TestQuery();
		fExecutor.execute(q);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Cancel the client request
		q.cancel(true);
		try {
			q.get();
			fail("Expected a cancellation exception");
		} catch (CancellationException e) {
		} // Expected exception;

		assertCacheInvalidAndWithCanceledRM();

		// Simulate retrieval logic that is regularly checking the RM's cancel
		// status and has discovered that the request has been canceled. It
		// aborts its processing, sets STATUS.CANCEL_STATUS in the RM and
		// completes it.
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fRetrieveRm.setStatus(Status.CANCEL_STATUS);
				fRetrieveRm.done();
			}
		}).get();

		// Validate that cache didn't accept the result after its RM was canceled
		assertCacheInvalidAndWithCanceledRM();
	}

	@Test
	public void cancelWhilePendingWithoutClientNotificationTest() throws InterruptedException, ExecutionException {
		final boolean canceledCalled[] = new boolean[] { false };

		fTestCache = new TestCache() {
			@Override
			protected synchronized void canceled() {
				canceledCalled[0] = true;
			}
		};

		// Request data from cache
		Query<Integer> q = new Query<Integer>() {
			@Override
			protected void execute(final DataRequestMonitor<Integer> rm) {

				fTestCache.update(new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
					@Override
					public synchronized void addCancelListener(ICanceledListener listener) {
						// Do not add the cancel listener so that the cancel request is not
						// propagated to the cache.
					}

					@Override
					protected void handleSuccess() {
						rm.setData(fTestCache.getData());
						rm.done();
					}
				});
			}
		};
		fExecutor.execute(q);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Cancel the client request
		q.cancel(true);

		assertCacheInvalidAndWithCanceledRM();

		// AbstractCache.canceled() should be called after isCanceled()
		// discovers that the client has canceled its request.  The canceled() method is
		// called in a separate dispatch cycle, so we have to wait one cycle of the executor
		// after is canceled is called.
		fRetrieveRm.isCanceled();
		fExecutor.submit(() -> {
		}).get();
		assertTrue(canceledCalled[0]);

		try {
			q.get();
			fail("Expected a cancellation exception");
		} catch (CancellationException e) {
		} // Expected exception;

		// Completed the retrieve RM
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fRetrieveRm.setData(1);
				fRetrieveRm.done();
			}
		}).get();

		// Validate that cache didn't accept the result after its RM was canceled
		assertCacheInvalidAndWithCanceledRM();
	}

	/**
	 * This test forces a race condition where a client that requested data
	 * cancels.  While shortly after a second client starts a new request.
	 * The first request's cancel should not interfere with the second
	 * request.
	 */
	@Test
	public void cancelAfterCompletedRaceCondition() throws InterruptedException, ExecutionException {

		// Create a client request with a badly behaved cancel implementation.
		final RequestMonitor[] rmBad = new RequestMonitor[1];
		final boolean qBadCanceled[] = new boolean[] { false };
		Query<Integer> qBad = new Query<Integer>() {
			@Override
			protected void execute(final DataRequestMonitor<Integer> rm) {
				rmBad[0] = new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
					@Override
					public synchronized void removeCancelListener(ICanceledListener listener) {
						// Do not add the cancel listener so that the cancel request is not
						// propagated to the cache.
					}

					@Override
					public void cancel() {
						if (qBadCanceled[0]) {
							super.cancel();
						}
					}

					@Override
					public synchronized boolean isCanceled() {
						return qBadCanceled[0];
					}

					@Override
					public synchronized void done() {
						// Avoid clearing cancel listeners list
					}

					@Override
					protected void handleSuccess() {
						rm.setData(fTestCache.getData());
						rm.done();
					}
				};

				fTestCache.update(rmBad[0]);
			}
		};
		fExecutor.execute(qBad);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Reset the cache
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fRetrieveRm = null;
				fTestCache.set(null, Status.OK_STATUS);
				fTestCache.reset();
			}
		}).get();

		Query<Integer> qGood = new TestQuery();
		fExecutor.execute(qGood);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		qBadCanceled[0] = true;
		rmBad[0].cancel();

		assertFalse(fRetrieveRm.isCanceled());

		// Completed the retrieve RM
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fRetrieveRm.setData(1);
				fRetrieveRm.done();
			}
		}).get();

		qGood.get();

		assertCacheValidWithData(1);
	}

	@Test
	public void cancelWhilePendingWithTwoClientsTest() throws InterruptedException, ExecutionException {

		// Request data from cache. Use submit/get to ensure both update
		// requests are initiated before we wait for retrieval to start
		Query<Integer> q1 = new TestQuery();
		fExecutor.submit(q1).get();

		// Request data from cache again
		Query<Integer> q2 = new TestQuery();
		fExecutor.submit(q2).get();

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Cancel the first client request
		q1.cancel(true);
		try {
			q1.get();
			fail("Expected a cancellation exception");
		} catch (CancellationException e) {
		} // Expected exception;
		assertCacheWaiting();

		// Cancel the second request
		q2.cancel(true);
		try {
			q2.get();
			fail("Expected a cancellation exception");
		} catch (CancellationException e) {
		} // Expected exception;

		assertCacheInvalidAndWithCanceledRM();

		// Completed the retrieve RM
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fRetrieveRm.setData(1);
				fRetrieveRm.done();
			}
		}).get();

		// Validate that cache didn't accept the result after its RM was canceled
		assertCacheInvalidAndWithCanceledRM();
	}

	@Test
	public void cancelWhilePendingWithManyClientsTest() throws InterruptedException, ExecutionException {
		// Request data from cache
		List<Query<Integer>> qList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Query<Integer> q = new TestQuery();
			fExecutor.submit(q).get();
			qList.add(q);
		}

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Cancel some client requests
		int[] toCancel = new int[] { 0, 2, 5, 9 };
		for (int i = 0; i < toCancel.length; i++) {

			// Cancel request and verify that its canceled
			Query<Integer> q = qList.get(toCancel[i]);
			q.cancel(true);
			try {
				q.get();
				fail("Expected a cancellation exception");
			} catch (CancellationException e) {
			} // Expected exception;
			qList.set(toCancel[i], null);

			assertCacheWaiting();
		}

		// Replace canceled requests with new ones
		for (int i = 0; i < toCancel.length; i++) {
			Query<Integer> q = new TestQuery();
			fExecutor.submit(q).get();
			qList.set(toCancel[i], q);
			assertCacheWaiting();
		}

		// Now cancel all requests
		for (int i = 0; i < (qList.size() - 1); i++) {
			// Validate that cache is still waiting and is not canceled
			assertCacheWaiting();
			qList.get(i).cancel(true);
		}
		qList.get(qList.size() - 1).cancel(true);
		assertCacheInvalidAndWithCanceledRM();

		// Completed the retrieve RM
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fRetrieveRm.setData(1);
				fRetrieveRm.done();
			}
		}).get();

		// Validate that cache didn't accept the result after its RM was canceled
		assertCacheInvalidAndWithCanceledRM();
	}

	@Test
	public void resetWhileValidTest() throws InterruptedException, ExecutionException {
		// Request data from cache
		Query<Integer> q = new TestQuery();
		fExecutor.execute(q);

		// Wait until the cache starts data retrieval.
		waitForRetrieveRm();

		// Complete the request
		fRetrieveRm.setData(1);
		fRetrieveRm.done();

		q.get();

		// Disable cache
		fExecutor.submit(new DsfRunnable() {
			@Override
			public void run() {
				fTestCache.reset();
			}
		}).get();

		// Check final state
		assertCacheResetWithoutData();
	}
}