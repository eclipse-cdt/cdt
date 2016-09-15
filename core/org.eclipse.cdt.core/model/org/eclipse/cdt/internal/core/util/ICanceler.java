/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.util;

/**
 * An interface for objects accepting an instance of {@link ICancelable}.
 * 
 * @since 5.0
 */
public interface ICanceler {
	/**
	 * Sets the cancelable object.
	 * 
	 * @param cancelable  the cancelable object
	 */
	public void setCancelable(ICancelable cancelable);

	/**
	 * Sets the cancel state to the given value. The state will be propagated to the cancelable object
	 * if it was set.
	 *
	 * @param value {@code true} indicates that cancellation has been requested,
	 *     {@code false} clears this flag
	 */
	public void setCanceled(boolean value);

	/**
	 * Checks if cancellation has been requested.
	 */
	public boolean isCanceled();
}
