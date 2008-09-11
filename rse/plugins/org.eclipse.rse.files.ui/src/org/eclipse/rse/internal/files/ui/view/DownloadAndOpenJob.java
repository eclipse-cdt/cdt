/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Kevin Doyle (IBM) - [194463] Use the result of _editable.download() to decide if file is to be opened
 * David McKnight (IBM)  - [189873] Improve remote shell editor open action with background jobs
 * David McKnight (IBM)  - [246651] FTP subsystem doesn't handle disconnected situation well
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.SystemUniversalTempFileListener;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.actions.SystemRemoteFileLineOpenWithMenu;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.widgets.Display;

public class DownloadAndOpenJob extends Job
{
	private static class OpenEditorRunnable implements Runnable
	{
		private ISystemEditableRemoteObject _editable;
		private boolean _systemEditor;
		private int _line;
		private int _charStart;
		private int _charEnd;
		
		public OpenEditorRunnable(ISystemEditableRemoteObject editable, boolean systemEditor)
		{
			_editable = editable;
			_systemEditor = systemEditor;
			_line = -1;
			_charStart = -1;
			_charEnd = -1;
		}
		
		public OpenEditorRunnable(ISystemEditableRemoteObject editable, boolean systemEditor, int line, int charStart, int charEnd)
		{
			_editable = editable;
			_systemEditor = systemEditor;
			_line = line;
			_charStart = charStart;
			_charEnd = charEnd;
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
				
				if (_line > -1 || (_charStart > -1 && _charEnd > -1)){						
					SystemRemoteFileLineOpenWithMenu.handleGotoLine((IRemoteFile)_editable.getRemoteObject(), _line, _charStart, _charEnd);
				}
			}
			catch (Exception e)
			{				
			}			
		}
		
	}

	private ISystemEditableRemoteObject _editable;
	private boolean _systemEditor;
	private int _line;
	private int _charStart;
	private int _charEnd;
	
	/**
	 * Download job.
	 * @param editable the editable remote object.
	 * @param systemEditor whether to use the system editor.
	 */
	public DownloadAndOpenJob(ISystemEditableRemoteObject editable, boolean systemEditor)
	{
		super(FileResources.RESID_FILES_DOWNLOAD);
		_editable = editable;
		_systemEditor = systemEditor;
		_line = -1;
		_charStart = -1;
		_charEnd = -1;
	}
	
	/**
	 * Download job
	 * @param editable the editable remote object
	 * @param systemEditor whether to use the system editor
	 * @param line the line to jump to after opening
	 */
	public DownloadAndOpenJob(ISystemEditableRemoteObject editable, boolean systemEditor, int line, int charStart, int charEnd)
	{
		super(FileResources.RESID_FILES_DOWNLOAD);
		_editable = editable;
		_systemEditor = systemEditor;
		_line = line;
		_charStart = charStart;
		_charEnd = charEnd;
	}

	public IStatus run(IProgressMonitor monitor) 
	{
		boolean downloadSuccessful = false;
		try
		{
			IFile localFile = _editable.getLocalResource();
			ISubSystem ss = _editable.getSubSystem();

			
			// if we're not connected, connect
			if (!ss.isConnected()){
				ss.connect(monitor, false);
			}
			if (ss.isConnected()){
				SystemUniversalTempFileListener listener = SystemUniversalTempFileListener.getListener();
				listener.addIgnoreFile(localFile);
				downloadSuccessful = _editable.download(monitor);
				listener.removeIgnoreFile(localFile);
			}
			else {
				return Status.CANCEL_STATUS;
			}
		}
		catch (Exception e)
		{				
		}
		if (downloadSuccessful)
		{
			OpenEditorRunnable oe = null;
			if (_line > -1 || (_charStart > -1 && _charEnd > -1)){	
				oe = new OpenEditorRunnable(_editable, _systemEditor, _line, _charStart, _charEnd);
			}
			else{
				oe = new OpenEditorRunnable(_editable, _systemEditor);
			}
			Display.getDefault().asyncExec(oe);
		}

		return Status.OK_STATUS;
	}
}
