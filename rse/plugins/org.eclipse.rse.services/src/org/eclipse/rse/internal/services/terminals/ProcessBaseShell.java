/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - [258631][api] ITerminalService should be public API
 *******************************************************************************/

package org.eclipse.rse.internal.services.terminals;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.rse.services.terminals.IBaseShell;

/**
 * A wrapper for Java {@link Process} objects to give more convenient access to
 * them through the {@link IBaseShell} interface.
 *
 * @since org.eclipse.rse.services 3.0
 */
public class ProcessBaseShell extends PlatformObject implements IBaseShell {

	/**
	 * The underlying Process instance.
	 */
	protected final Process fProcess;

	/**
	 * Constructor.
	 *
	 * @param p the Process to wrap with this IBaseShell interface.
	 */
	public ProcessBaseShell(Process p) {
		fProcess = p;
	}

	/**
	 * Forcefully terminate the underlying Process through
	 * {@link Process#destroy()}. Subclasses may want to override this behavior
	 * by trying to terminate the underlying Process in a cleaner way.
	 *
	 * @see IBaseShell#exit()
	 */
	public void exit() {
		fProcess.destroy();
	}

	public int exitValue() {
		return fProcess.exitValue();
	}

	public InputStream getErrorStream() {
		return fProcess.getErrorStream();
	}

	public InputStream getInputStream() {
		return fProcess.getInputStream();
	}

	public OutputStream getOutputStream() {
		return fProcess.getOutputStream();
	}

	/**
	 * Check if the underlying Process is still active. Does not check whether
	 * the Streams for the Process have been closed by the client, since this
	 * does not influence the process active state anyways.
	 *
	 * @see IBaseShell#isActive()
	 */
	public boolean isActive() {
		try {
			fProcess.exitValue();
		} catch (IllegalThreadStateException e) {
			return true;
		}
		return false;
	}

	/**
	 * A Watchdog Thread, to interrupt other Threads after a given time unless a
	 * specified condition is met.
	 *
	 * Sleeps for a given time, and upon wakeup checks if a condition is met. If
	 * not, the specified Thread is interrupted.
	 */
	private abstract static class Watchdog extends Thread {
		private final Thread fThreadToWatch;
		private long fTimeout;

		public Watchdog(Thread threadToWatch, long timeout) {
			fThreadToWatch = threadToWatch;
			fTimeout = timeout;
		}

		protected abstract boolean conditionDone();

		public void run() {
			try {
				sleep(fTimeout);
			} catch (InterruptedException e) {
				/* ignore */
			} finally {
				if (!conditionDone()) {
					fThreadToWatch.interrupt();
				}
			}
		}
	}

	public boolean waitFor(long timeout) throws InterruptedException {
		boolean active = isActive();
		if (active) {
			Thread watchdog = null;
			if (timeout > 0) {
				// TODO Check if using java.util.Timer would be more efficient
				// than our own Watchdog
				watchdog = new Watchdog(Thread.currentThread(), timeout) {
					protected boolean conditionDone() {
						return !isActive();
					}
				};
				watchdog.start();
			}
			try {
				fProcess.waitFor();
			} catch (InterruptedException e) {
				/* ignore */
			}
			if (watchdog != null) {
				watchdog.interrupt();
			}
			active = isActive();
		}
		return active;
	}

}
