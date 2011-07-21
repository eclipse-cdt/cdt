/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * <p>
 * An interface for objects that own resources that have to be explicitly
 * released. A disposable object is guaranteed to receive a {@link #dispose()} call
 * when it is not longer needed. At this point, the object must release all resources
 * and detach all listeners. A disposable object can only be disposed once; it cannot
 * be reused.
 * </p>
 * <p>
 * This interface can be extended or implemented by clients.
 * </p>
 * @since 2.0
 */
public interface ICodanDisposable {
	/**
	 * Disposes of the object. This method has to be called exactly once during
	 * the life cycle of the object.
	 */
	public void dispose();
}
