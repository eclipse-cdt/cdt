/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.SystemUniversalTempFileListener;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.widgets.Display;

public class DownloadJob extends Job
{
	public static class OpenEditorRunnable implements Runnable
	{
		private ISystemEditableRemoteObject _editable;
		private boolean _systemEditor;
		public OpenEditorRunnable(ISystemEditableRemoteObject editable, boolean systemEditor)
		{
			_editable = editable;
			_systemEditor = systemEditor;
		}
		
		public void run()
		{
			try
			{
				_editable.addAsListener();
				_editable.setLocalResourceProperties();
				if (_systemEditor)
				{
					((SystemEditableRemoteFile)_editable).openSystemEditor();
				}
				else
				{
					_editable.openEditor();
				}
			}
			catch (Exception e)
			{
				
			}			
		}
		
	}

	private ISystemEditableRemoteObject _editable;
	private boolean _systemEditor;
	public DownloadJob(ISystemEditableRemoteObject editable, boolean systemEditor)
	{
		super("Download");
		_editable = editable;
		_systemEditor = systemEditor;
	}

	public IStatus run(IProgressMonitor monitor) 
	{
		try
		{
			IFile localFile = _editable.getLocalResource();
			SystemUniversalTempFileListener listener = SystemUniversalTempFileListener.getListener();
			listener.addIgnoreFile(localFile);
			_editable.download(monitor);
			listener.removeIgnoreFile(localFile);
		}
		catch (Exception e)
		{				
		}
		if (!monitor.isCanceled())
		{
			OpenEditorRunnable oe = new OpenEditorRunnable(_editable, _systemEditor);
			Display.getDefault().asyncExec(oe);
		}

		return Status.OK_STATUS;
	}
}