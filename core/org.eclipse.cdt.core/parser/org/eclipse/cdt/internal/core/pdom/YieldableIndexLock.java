/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.internal.core.index.IWritableIndex;

/**
 * Write lock on the index that can be yielded temporarily to unblock threads that need
 * read access to the index. 
 * @since 5.2
 */
public class YieldableIndexLock {
	private final IWritableIndex index;
	private final boolean flushIndex;
	private long lastLockTime;
	private long cumulativeLockTime;

	public YieldableIndexLock(IWritableIndex index, boolean flushIndex) {
		this.index = index;
		this.flushIndex = flushIndex;
	}

	/**
	 * Acquires the lock.
	 * 
	 * @throws InterruptedException
	 */
	public void acquire() throws InterruptedException {
		index.acquireWriteLock();
		lastLockTime = System.currentTimeMillis();
	}

	/**
	 * Releases the lock.
	 */
	public void release() {
		if (lastLockTime != 0) {
			index.releaseWriteLock(flushIndex);
			cumulativeLockTime += System.currentTimeMillis() - lastLockTime;
			lastLockTime = 0;
		}
	}

	/**
	 * Yields the lock temporarily if it was held for YIELD_INTERVAL or more, and somebody is waiting
	 * for a read lock. 
	 * @throws InterruptedException
	 */
	public void yield() throws InterruptedException {
		if (index.hasWaitingReaders()) {
			index.releaseWriteLock(false);
			cumulativeLockTime += System.currentTimeMillis() - lastLockTime;
			lastLockTime = 0;
			acquire();
		}
	}

	/**
	 * @return Total time the lock was held in milliseconds. 
	 */
	public long getCumulativeLockTime() {
		return cumulativeLockTime;
	}
}
