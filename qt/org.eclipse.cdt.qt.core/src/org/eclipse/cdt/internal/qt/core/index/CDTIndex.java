/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.core.runtime.CoreException;

/**
 * A wrapper around the CDT index that manages the read lock.
 */
public class CDTIndex {

	private final IIndex index;

	public CDTIndex(IIndex index) {
		this.index = index;
	}

	/**
	 * An object used for reading from the CDT index.  The {@link #access(IIndex)} method
	 * will only be invoked when the index's read lock has been properly acquired.
	 */
	public static interface Accessor<T> {
		/**
		 * A method that performs the lookup within the CDT index.  The read-lock will
		 * be acquired before invoking this method.
		 * <p>
		 * <strong>The implementation of access must not make calls to {@link CDTIndex#get(Accessor)}.
		 * If other objects are needed, then have the accessor return a qualified name and
		 * lookup the object after the implementation of #access completes.</strong>
		 */
		public T access(IIndex index) throws CoreException;
	}

	/**
	 * Use the given accessor to find and return a value from the index.  This method ensures
	 * that the read-lock has been acquired.
	 */
	public <T> T get(Accessor<T> accessor) {
		try {
			index.acquireReadLock();
		} catch (InterruptedException e) {
			return null;
		}

		try {
			return accessor.access(index);
		} catch (CoreException e) {
			Activator.log(e);
		} finally {
			index.releaseReadLock();
		}

		return null;
	}
}
