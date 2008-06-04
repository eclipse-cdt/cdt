/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM) - initial API and implementation
 * Martin Oberhuber (Wind River) - [216252] canceled --> cancelled in comments and locals
 *******************************************************************************/


package org.eclipse.rse.services.clientserver;

/**
 * A monitor to support cancellation of operations in an environment
 * where Eclipse IProgressMonitor is not available.
 * 
 * @since 3.0
 */
public class SystemOperationMonitor implements ISystemOperationMonitor
{
	private boolean cancelled = false;
	private boolean done = false;


	public boolean isDone()
	{
		return done;
	}

	public void setDone(boolean value)
	{
		done = value;
	}

	public boolean isCancelled()
	{
		return cancelled;
	}



	public void setCancelled(boolean value)
	{
		cancelled = value;
	}
}
