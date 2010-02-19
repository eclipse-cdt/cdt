package org.eclipse.cdt.debug.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IRequest;

/**
 * Base class for request objects used in asynchronous calls in base CDT
 * (non-DSF). This is used in base features that delegate a task to a backend
 * that is either DSF or CDI. Since DSF is highly asynchronous, the base logic
 * has to use asynchronous APIs.
 */
public class CRequest implements IRequest {
	private IStatus fStatus;
	private boolean fCanceled;
	/*
	 * @see org.eclipse.debug.core.IRequest#cancel()
	 */
	public void cancel() {
		fCanceled= true;
	}

	/*
	 * @see org.eclipse.debug.core.IRequest#done()
	 */
	public void done() {
	}

	/*
	 * @see org.eclipse.debug.core.IRequest#getStatus()
	 */
	public IStatus getStatus() {
		return fStatus;
	}

	/*
	 * @see org.eclipse.debug.core.IRequest#isCanceled()
	 */
	public boolean isCanceled() {
		return fCanceled;
	}

	/*
	 * @see org.eclipse.debug.core.IRequest#setStatus(org.eclipse.core.runtime.IStatus)
	 */
	public void setStatus(IStatus status) {
		fStatus= status;
	}
}
