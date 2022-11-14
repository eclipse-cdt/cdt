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
 * or an {@link OperationCanceledException} or some other type of runtime exception or
 * error (especially assertion errors) which will be the nested exception.
 */
public class FailedToReAcquireLockException extends Exception {

	public FailedToReAcquireLockException(Throwable t) {
		super(t);
		Assert.isNotNull(t);
	}

	public void reThrow() throws InterruptedException {
		if (getCause() instanceof InterruptedException ie) {
			throw ie;
		}
		if (getCause() instanceof RuntimeException re) {
			throw re;
		}
		if (getCause() instanceof Error er) {
			throw er;
		}
		throw new RuntimeException("Checked exception changed wrapped in runtime exception", getCause()); //$NON-NLS-1$
	}
}
