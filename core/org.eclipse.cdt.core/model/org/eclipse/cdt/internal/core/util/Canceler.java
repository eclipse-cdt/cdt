package org.eclipse.cdt.internal.core.util;

/**
 * A thread-safe implementation of {@link ICanceler} interface.
 */
public class Canceler implements ICanceler {
	private ICancelable fCancelable;
	private volatile boolean canceled;

	@Override
	public void setCancelable(ICancelable cancelable) {
		synchronized (this) {
			fCancelable = cancelable;
			cancelable = getCancelableToCancel();
		}
		if (cancelable != null)
			cancelable.cancel();
	}

	@Override
	public void setCanceled(boolean canceled) {
		ICancelable cancelable;
		synchronized (this) {
			this.canceled = canceled;
			cancelable = getCancelableToCancel();
		}
		if (cancelable != null)
			cancelable.cancel();
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Returns the cancelable to cancel, or {@code null} if there is nothing to cancel.
	 * Sets {@link #fCancelable} to {@code null}. Has to be called from a synchronized block.
	 */
	private ICancelable getCancelableToCancel() {
		ICancelable cancelable = null;
		if (canceled) {
			cancelable = fCancelable;
			fCancelable = null;
		}
		return cancelable;
	}
}