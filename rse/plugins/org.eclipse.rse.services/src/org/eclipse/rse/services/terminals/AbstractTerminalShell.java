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

package org.eclipse.rse.services.terminals;

import java.io.InputStream;

import org.eclipse.core.runtime.PlatformObject;


/**
 * Abstract base class for clients to create an ITerminalShell instance.
 *
 * This abstract base class provides valid default implementations for all
 * {@link ITerminalShell} methods where possible. Clients should extend this
 * base class rather than implementing ITerminalShell directly, in order to
 * remain compatible when the ITerminalShell interface is evolved in the future.
 *
 * @since org.eclipse.rse.services 3.1
 */
public abstract class AbstractTerminalShell extends PlatformObject implements ITerminalShell {

	public String getDefaultEncoding() {
		return null;
	}

	public String getPtyType() {
		return null;
	}

	public boolean isLocalEcho() {
		return false;
	}

	public void setTerminalSize(int newWidth, int newHeight) {
		// do nothing
	}

	public InputStream getErrorStream() {
		return null;
	}

	public int exitValue() {
		// exit values are not supported by default, but we need to observe the
		// API by throwing IllegalThreadStateException
		if (isActive()) {
			throw new IllegalThreadStateException();
		}
		return 0;
	}

	/**
	 * Return the interval (in milliseconds) for polling the {@ink #isActive()}
	 * method during the {@link #waitFor(long)} method. Subclasses may override
	 * to return different poll intervals.
	 *
	 * The interval may be changed dynamically as appropriate for the current
	 * state of this shell. That way, wait polling mechanisms such as
	 * exponential backoff can be implemented.
	 *
	 * Or, a concrete implementation that supports a notification mechanism for
	 * knowing when the shell terminates, can use this to tweak the waitFor()
	 * method by returning Long.MAX_VALUE here (wait forever), but calling
	 * {@link #notifyAll()} when the shell is dead.
	 *
	 * @return interval (in milliseconds) for polling active state
	 */
	protected long getWaitForPollInterval() {
		return 500L;
	}

	/**
	 * Wait for the shell to terminate. This uses a polling mechanism by
	 * default, which can be tweaked by overriding
	 * {@link #getWaitForPollInterval()}.
	 *
	 * @see IBaseShell#waitFor(long)
	 */
	public boolean waitFor(long timeout) throws InterruptedException {
		boolean active = isActive();
		if (active) {
			long endTime = (timeout <= 0) ? Long.MAX_VALUE : System.currentTimeMillis() + timeout - getWaitForPollInterval();
			do {
				synchronized (this) {
					wait(getWaitForPollInterval());
				}
				active = isActive();
			} while (active && (timeout <= 0 || System.currentTimeMillis() < endTime));
		}
		return active;
	}

}
