/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *******************************************************************************/
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
	@Override
	public void cancel() {
		fCanceled= true;
	}

	/*
	 * @see org.eclipse.debug.core.IRequest#done()
	 */
	@Override
	public void done() {
	}

	/*
	 * @see org.eclipse.debug.core.IRequest#getStatus()
	 */
	@Override
	public IStatus getStatus() {
		return fStatus;
	}

	/*
	 * @see org.eclipse.debug.core.IRequest#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		return fCanceled;
	}

	/*
	 * @see org.eclipse.debug.core.IRequest#setStatus(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	public void setStatus(IStatus status) {
		fStatus= status;
	}

	/**
	 * Checks whether the request completed successfully or not. If the request
	 * monitor was canceled it is considered a failure, regardless of the
	 * status. If the status has a severity higher than INFO (i.e., WARNING,
	 * ERROR or CANCEL), it is considered a failure. Note that as per IRequest
	 * documentation, a null status object is equivalent to IStatu#OK.
	 */
    public boolean isSuccess() {
    	IStatus status = getStatus();
        return !isCanceled() && (status == null || status.getSeverity() <= IStatus.INFO); 
    }
}
