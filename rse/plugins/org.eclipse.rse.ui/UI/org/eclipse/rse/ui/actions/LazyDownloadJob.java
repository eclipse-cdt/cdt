/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;

public class LazyDownloadJob extends Job
{
	private ISystemEditableRemoteObject _editable;
	public LazyDownloadJob(ISystemEditableRemoteObject editable) 
	{
		// TODO Auto-generated constructor stub
		super("Downloading " + editable.getAbsolutePath());
		_editable = editable;
	}
	
	public IStatus run(IProgressMonitor monitor)
	{	
		try
		{
			_editable.download(monitor);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}
}