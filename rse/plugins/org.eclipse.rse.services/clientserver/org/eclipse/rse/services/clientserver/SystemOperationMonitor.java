/********************************************************************************
 * Copyright (c) 2007, 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 * 
 * Xuan Chen (IBM) - initial API and implementation 
 ********************************************************************************/


package org.eclipse.rse.services.clientserver;

public class SystemOperationMonitor implements ISystemOperationMonitor 
{
	private boolean canceled = false;
	private boolean done = false;
	
	
	public boolean isDone()
	{
		return done;
	}
	
	public void setDone(boolean value)
	{
		done = value;
	}
	
	public boolean isCanceled()
	{
		return canceled;
	}
	
	
	
	public void setCanceled(boolean value)
	{
		canceled = value;
	}
}
