/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import java.util.concurrent.Callable;


/**
 * Runnable object that returns a result object.
 * 
 * This is like Future<V> from the concurrent package,
 * but with a few less bells and whistles.
 * 
 * Intended to be used, for example, as follows:
 * 
 * RunnableWithResult<X> runnable = new RunnableWithResult<X>() {
 *   public X call() {
 *     ... do work, return an X object ...
 *   }
 * }
 * 
 * Thread thread = new Thread(runnable);
 * thread.start();
 * X result = runnable.getResult(0);
 * 
 * or, to run it on the UI thread...
 * 
 * GUIUtils.execAndWait(runnable);
 * X result = runnable.getResult(0);
 * 
 * Note: if you're invoking this from the UI thread,
 * it's important to use execAndWait(), so that the runnable
 * gets a turn on the event loop, otherwise you'll hang the UI!
 * 
 */
public class RunnableWithResult<V>
	implements Runnable, Callable<V>
{
	// --- members ---

	/** Result to return */
	protected V m_result = null;
	
	/** Whether run() has completed */
	protected boolean m_done = false;
	
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public RunnableWithResult() {
	}
	
	/** Dispose method. */
	public void dispose() {
		m_result = null;
	}

	
	// --- accessors ---

	/** Sets result value */
	public void setResult(V result) {
		m_result = result;
	}
	
	/** Gets result value. */
	public V getResult() {
		return m_result;
	}

	
	// --- Runnable implementation ---

	/** Run method.
	 *  Derived types should override call() rather than this method.
	 */
	public void run() {
		m_done = false;
		setResult(call());
		m_done = true;
		synchronized (this) {
			notifyAll();
		}
	}
	
	
	// --- Callable implementation ---
	
	/** Method that returns the value.
	 *  Default implementation returns null.
	 */
	public V call() {
		return null;
	}

	
	// --- methods ---
	
	/** Waits for result and returns it. */
	public V waitForResult() {
		return waitForResult(0, null);
	}
	
	/** Waits for result and returns it. */
	public V waitForResult(long timeout) {
		return waitForResult(timeout, null);
	}
	
	/** Waits for result and returns it.
	 *  Returns null if specified timeout is exceeded.
	 */
	public V waitForResult(long timeout, V defaultValue) {
		V result = defaultValue;
		try {
			if (timeout == 0) {
				// wait forever
				// (guard against spurious thread wakeup -- see wait() Javadoc)
				while (! m_done) {
					synchronized (this) {
						this.wait(0);
					}
				}
			}
			else {
				// wait until specified timeout
				// (guard against spurious thread wakeup -- see wait() Javadoc)
				long then = now();
				long waitstep = timeout / 10;
				if (waitstep == 0) waitstep = 1;
				do {
					synchronized (this) {
						this.wait(waitstep);
					}
				}
				while (! m_done && ((now() - then) < timeout));
			}
		}
		catch (InterruptedException e) {
		}
		if (m_done) result = getResult();
		return result;
	}
	
	/** Returns current time in milliseconds. */
	protected static long now() {
		return System.currentTimeMillis();
	}
}

