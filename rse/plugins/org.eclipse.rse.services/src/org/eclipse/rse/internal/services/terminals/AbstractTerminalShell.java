/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.services.terminals;

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
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a>
 * team.
 * </p>
 *
 * @since org.eclipse.rse.services 3.0
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
		if (isActive())
			throw new IllegalThreadStateException();
		return 0;
	}

	public boolean waitFor(long timeout) throws InterruptedException {
		boolean active = isActive();
		if (active) {
			synchronized (this) {
				wait(timeout);
			}
			active = isActive();
		}
		return active;
	}

}
