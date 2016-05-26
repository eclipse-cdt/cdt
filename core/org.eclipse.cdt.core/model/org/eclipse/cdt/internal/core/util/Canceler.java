package org.eclipse.cdt.internal.core.util;

/**
 * A thread-safe implementation of {@link ICanceler} interface.
 */
public class Canceler implements ICanceler {
	private ICancelable fCancelable;
	private boolean canceled;

	@Override
	public synchronized void setCancelable(ICancelable cancelable) {
		fCancelable= cancelable;
		checkCanceled();
	}

	@Override
	public synchronized void setCanceled(boolean canceled) {
		this.canceled = canceled;
		checkCanceled();
	}

	@Override
	public synchronized boolean isCanceled() {
		return canceled;
	}

	private synchronized void checkCanceled() {
		if (fCancelable != null && canceled) {
			fCancelable.cancel();
			fCancelable= null;
		}
	}
}