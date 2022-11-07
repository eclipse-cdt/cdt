/*******************************************************************************
 * Copyright (c) 2022 Kichwa Coders Canada, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * This exception is raised when {@link YieldableIndexLock#yield()} fails to
 * reacquire the lock after yielding, this may be due to an {@link InterruptedException}
 * or an {@link OperationCanceledException} which will be the nested exception.
 */
public class FailedToReAcquireLockException extends Exception {

	public FailedToReAcquireLockException(InterruptedException e) {
		super(e);
		Assert.isNotNull(e);
	}

	public FailedToReAcquireLockException(OperationCanceledException e) {
		super(e);
		Assert.isNotNull(e);
	}

	public void reThrow() throws InterruptedException, OperationCanceledException {
		if (getCause() instanceof InterruptedException ie) {
			throw ie;
		}
		if (getCause() instanceof OperationCanceledException oce) {
			throw oce;
		}
		throw new RuntimeException("Unexpectedly the exception cause was none of the allowed types", this); //$NON-NLS-1$
	}
}
