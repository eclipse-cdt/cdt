/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.services.clientserver;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An interface to support cancellation of operations on remote systems, where
 * the Eclipse {@link IProgressMonitor} is not avaialble.
 * 
 * @since org.eclipse.rse.services 3.0
 */
public interface ISystemOperationMonitor
{
	/**
	 * Notifies that the work is done; that is, either the main task is completed
	 * or the user cancelled it. This method may be called more than once
	 * (implementations should be prepared to handle this case).
	 */
	public boolean isDone();

	/**
	 * Sets the done state to the given value.
	 *
	 * @param value <code>true</code> indicates that this operation has finished
	 *     <code>false</code> clears this flag
	 * @see #isDone()
	 */
	public void setDone(boolean value);
	/**
	 * Returns whether cancelation of current operation has been requested.
	 * Long-running operations should poll to see if cancelation
	 * has been requested.
	 *
	 * @return <code>true</code> if cancellation has been requested,
	 *    and <code>false</code> otherwise
	 * @see #setCancelled(boolean)
	 */
	public boolean isCancelled();


	/**
	 * Sets the cancel state to the given value.
	 *
	 * @param value <code>true</code> indicates that cancelation has
	 *     been requested (but not necessarily acknowledged);
	 *     <code>false</code> clears this flag
	 * @see #isCancelled()
	 */
	public void setCancelled(boolean value);

}