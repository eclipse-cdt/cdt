/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * A listener, which gets notified when the contents of a specific buffer
 * have changed, or when the buffer is closed.
 * When a buffer is closed, the listener is notified <em>after</em> the buffer has been closed.
 * A listener is not notified when a buffer is saved.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * This interface is similar to the JDT IBufferChangedListener interface
 */
public interface IBufferChangedListener {
	/**
	 * Notifies that the given event has occurred.
	 *
	 * @param event the change event
	 */
	public void bufferChanged(BufferChangedEvent event);

}
