/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.dsf.concurrent;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A base implementation of a general purpose cache. Sub classes must implement
 * {@link #retrieve(DataRequestMonitor)} to fetch data from the data source.
 * Sub-classes are also responsible for calling {@link #set(Object, IStatus)}
 * and {@link #reset()} to manage the state of the cache in response to events
 * from the data source.
 * <p>
 * This cache requires an executor to use. The executor is used to synchronize
 * access to the cache state and data.
 * </p>
 * @since 2.2
 */
@ConfinedToDsfExecutor("fExecutor")
public abstract class AbstractCache<V> implements ICache<V> {

	private static final IStatus INVALID_STATUS = new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID,
			IDsfStatusConstants.INVALID_STATE, "Cache invalid", null); //$NON-NLS-1$

	private class RequestCanceledListener implements RequestMonitor.ICanceledListener {
		@Override
		public void requestCanceled(final RequestMonitor canceledRm) {
			fExecutor.getDsfExecutor().execute(() -> handleCanceledRm(canceledRm));
		}
	}

	private RequestCanceledListener fRequestCanceledListener = new RequestCanceledListener();

	private boolean fValid;

	private V fData;
	private IStatus fStatus = INVALID_STATUS;

	@ThreadSafe
	private Object fWaitingList;

	private final ImmediateInDsfExecutor fExecutor;

	public AbstractCache(ImmediateInDsfExecutor executor) {
		fExecutor = executor;
	}

	@Override
	public DsfExecutor getExecutor() {
		return fExecutor.getDsfExecutor();
	}

	protected ImmediateInDsfExecutor getImmediateInDsfExecutor() {
		return fExecutor;
	}

	/**
	 * Sub-classes should override this method to retrieve the cache data from
	 * its source. The implementation should call {@link #set(Object, IStatus)}
	 * to store the newly retrieved data when it arrives (or an error, if one
	 * occurred retrieving the data)
	 *
	 * @param rm
	 *            Request monitor for completion of data retrieval.
	 */
	abstract protected void retrieve();

	/**
	 * Called to cancel a retrieve request.  This method is called when
	 * clients of the cache no longer need data that was requested. <br>
	 * Sub-classes should cancel and clean up requests to the asynchronous
	 * data source.
	 *
	 * <p>
	 * Note: Called while holding a lock to "this".  No new request will start until
	 * this call returns.
	 * </p>
	 */
	@ThreadSafe
	abstract protected void canceled();

	@Override
	public boolean isValid() {
		return fValid;
	}

	@Override
	public V getData() {
		if (!fValid) {
			throw new IllegalStateException("Cache is not valid.  Cache data can be read only when cache is valid."); //$NON-NLS-1$
		}
		return fData;
	}

	@Override
	public IStatus getStatus() {
		if (!fValid) {
			throw new IllegalStateException("Cache is not valid.  Cache status can be read only when cache is valid."); //$NON-NLS-1$
		}
		return fStatus;
	}

	@Override
	public void update(RequestMonitor rm) {
		assert fExecutor.getDsfExecutor().isInExecutorThread();

		if (!fValid) {
			boolean first = false;
			synchronized (this) {
				if (fWaitingList == null) {
					first = true;
					fWaitingList = rm;
				} else if (fWaitingList instanceof RequestMonitor[]) {
					RequestMonitor[] waitingList = (RequestMonitor[]) fWaitingList;
					int waitingListLength = waitingList.length;
					int i;
					for (i = 0; i < waitingListLength; i++) {
						if (waitingList[i] == null) {
							waitingList[i] = rm;
							break;
						}
					}
					if (i == waitingListLength) {
						RequestMonitor[] newWaitingList = new RequestMonitor[waitingListLength + 1];
						System.arraycopy(waitingList, 0, newWaitingList, 0, waitingListLength);
						newWaitingList[waitingListLength] = rm;
						fWaitingList = newWaitingList;
					}
				} else {
					RequestMonitor[] newWaitingList = new RequestMonitor[2];
					newWaitingList[0] = (RequestMonitor) fWaitingList;
					newWaitingList[1] = rm;
					fWaitingList = newWaitingList;
				}
			}
			rm.addCancelListener(fRequestCanceledListener);
			if (first) {
				retrieve();
			}
		} else {
			rm.setStatus(fStatus);
			rm.done();
		}
	}

	private void completeWaitingRms() {
		Object waiting = null;
		synchronized (this) {
			waiting = fWaitingList;
			fWaitingList = null;
		}
		if (waiting != null) {
			if (waiting instanceof RequestMonitor) {
				completeWaitingRm((RequestMonitor) waiting);
			} else if (waiting instanceof RequestMonitor[]) {
				RequestMonitor[] waitingList = (RequestMonitor[]) waiting;
				for (int i = 0; i < waitingList.length; i++) {
					if (waitingList[i] != null) {
						completeWaitingRm(waitingList[i]);
					}
				}
			}
			waiting = null;
		}
	}

	private void completeWaitingRm(RequestMonitor rm) {
		rm.setStatus(fStatus);
		rm.removeCancelListener(fRequestCanceledListener);
		rm.done();
	}

	private void handleCanceledRm(final RequestMonitor rm) {

		boolean found = false;
		boolean waiting = false;
		synchronized (this) {
			if (rm.equals(fWaitingList)) {
				found = true;
				waiting = false;
				fWaitingList = null;
			} else if (fWaitingList instanceof RequestMonitor[]) {
				RequestMonitor[] waitingList = (RequestMonitor[]) fWaitingList;
				for (int i = 0; i < waitingList.length; i++) {
					if (!found && rm.equals(waitingList[i])) {
						waitingList[i] = null;
						found = true;
					}
					waiting = waiting || waitingList[i] != null;
				}
			}
			if (found && !waiting) {
				canceled();
			}
		}

		// If we have no clients waiting anymore, cancel the request
		if (found) {
			// We no longer need to listen to cancellations.
			rm.removeCancelListener(fRequestCanceledListener);
			rm.setStatus(Status.CANCEL_STATUS);
			rm.done();
		}

	}

	/**
	 * Returns true if there are no clients waiting for this cache or if the
	 * clients that are waiting, have already canceled their requests.
	 * <p>
	 * Note: Calling this method may cause the client request monitors that were
	 * canceled to be completed with a cancel status.  If all the client request
	 * monitors were canceled, this method will also cause the {@link #canceled()}
	 * method to be called.  Both of these side effects will only happen
	 * asynchronously after <code>isCanceled()</code> returns.
	 * </p>
	 *
	 * @return <code>true</code> if all clients waiting on this cache have been
	 * canceled, or if there are no clients waiting at all.
	 */
	@ThreadSafe
	protected boolean isCanceled() {
		boolean canceled;
		List<RequestMonitor> canceledRms = null;
		synchronized (this) {
			if (fWaitingList instanceof RequestMonitor) {
				if (((RequestMonitor) fWaitingList).isCanceled()) {
					canceledRms = new ArrayList<>(1);
					canceledRms.add((RequestMonitor) fWaitingList);
					canceled = true;
				} else {
					canceled = false;
				}
			} else if (fWaitingList instanceof RequestMonitor[]) {
				canceled = true;
				RequestMonitor[] waitingList = (RequestMonitor[]) fWaitingList;
				for (int i = 0; i < waitingList.length; i++) {
					if (waitingList[i] != null) {
						if (waitingList[i].isCanceled()) {
							if (canceledRms == null) {
								canceledRms = new ArrayList<>(1);
							}
							canceledRms.add(waitingList[i]);
						} else {
							canceled = false;
						}
					}
				}
			} else {
				assert fWaitingList == null;
				canceled = true;
			}
		}
		if (canceledRms != null) {
			final List<RequestMonitor> _canceledRms = canceledRms;
			fExecutor.getDsfExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					for (RequestMonitor canceledRm : _canceledRms) {
						handleCanceledRm(canceledRm);
					}
				}
			});
		}

		return canceled;
	}

	/**
	 * Resets the cache, setting the data to null and the status to
	 * INVALID_STATUS. When in the invalid state, neither the data nor the
	 * status can be queried.
	 */
	protected void reset() {
		if (!fValid) {
			throw new IllegalStateException("Cache is not valid.  Cache can be reset only when it's in a valid state"); //$NON-NLS-1$
		}
		fValid = false;
		fData = null;
		fStatus = INVALID_STATUS;
	}

	/**
	 * Moves the cache to the valid state with the given data and status. Note
	 * that data may be null and status may be an error status. 'Valid' simply
	 * means that our data is not stale. In other words, if the request to the
	 * source encounters an error, the cache object becomes valid all the same.
	 * The status indicates what error was encountered.
	 *
	 * <p>
	 * This method is called internally, typically in response to having
	 * obtained the result from the asynchronous request to the source. The
	 * data/status will remain valid until the cache object receives an event
	 * notification from the source indicating otherwise.
	 *
	 * @param data
	 *            The data that should be returned to any clients waiting for
	 *            cache data and for clients requesting data until the cache is
	 *            invalidated.
	 * @status The status that should be returned to any clients waiting for
	 *         cache data and for clients requesting data until the cache is
	 *         invalidated
	 *
	 * @see #reset(Object, IStatus)
	 */
	protected void set(V data, IStatus status) {
		assert fExecutor.getDsfExecutor().isInExecutorThread();

		fData = data;
		fStatus = status;
		fValid = true;

		completeWaitingRms();
	}

	/**
	 * Performs the set and reset operations in one step  This allows the cache to
	 * remain in invalid state, but to notify any waiting listeners that the state of
	 * the cache has changed.
	 *
	 * @param data
	 *            The data that should be returned to any clients waiting for
	 *            cache data and for clients requesting data until the cache is
	 *            invalidated.
	 * @status The status that should be returned to any clients waiting for
	 *         cache data and for clients requesting data until the cache is
	 *         invalidated
	 *
	 * @see #reset(Object, IStatus)
	 * @since 2.3
	 */
	protected void setAndReset(V data, IStatus status) {
		assert fExecutor.getDsfExecutor().isInExecutorThread();

		fData = data;
		fStatus = status;
		fValid = false;

		completeWaitingRms();
	}

}
