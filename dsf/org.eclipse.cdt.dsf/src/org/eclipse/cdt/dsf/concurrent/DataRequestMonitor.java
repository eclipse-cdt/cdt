/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
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
 *     Eugene Ostroukhov (NVIDIA) - new done(V) method
 *******************************************************************************/
package org.eclipse.cdt.dsf.concurrent;

import java.util.concurrent.Executor;

/**
 * Request monitor that allows data to be returned to the request initiator.
 *
 * @param V The type of the data object that this monitor handles.
 *
 * @since 1.0
 */
public class DataRequestMonitor<V> extends RequestMonitor {

	/** Data object reference */
	private V fData;

	public DataRequestMonitor(Executor executor, RequestMonitor parentRequestMonitor) {
		super(executor, parentRequestMonitor);
	}

	/**
	 * Sets the data object to specified value.  To be called by the
	 * asynchronous method implementor.
	 * @param data Data value to set.
	 *
	 * @see #done(Object)
	 */
	public synchronized void setData(V data) {
		fData = data;
	}

	/**
	 * Returns the data value, null if not set.
	 */
	public synchronized V getData() {
		return fData;
	}

	/**
	 * Completes the monitor setting data object to specified value. To be
	 * called by asynchronous method implementor.
	 *
	 * <p>
	 * Note: Only one <code>done</code> method should be called and only once,
	 * for every request issued. Even if the request was canceled.
	 * </p>
	 *
	 * @param data Data value to set
	 * @see #setData(Object)
	 * @see #done()
	 * @see #done(org.eclipse.core.runtime.IStatus)
	 * @since 2.3
	 */
	public synchronized void done(V data) {
		setData(data);
		done();
	}

	@Override
	public String toString() {
		if (getData() != null) {
			return getData().toString();
		} else {
			return super.toString();
		}
	}
}
