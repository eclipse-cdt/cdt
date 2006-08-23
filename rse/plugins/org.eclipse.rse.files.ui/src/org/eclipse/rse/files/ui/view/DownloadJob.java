/**
 * 
 */
package org.eclipse.rse.files.ui.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.files.ui.resources.SystemUniversalTempFileListener;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.widgets.Display;

public class DownloadJob extends Job
{
	public class OpenEditorRunnable implements Runnable
	{
		private ISystemEditableRemoteObject _editable;
		public OpenEditorRunnable(ISystemEditableRemoteObject editable)
		{
			_editable = editable;
		}
		
		public void run()
		{
			try
			{
				_editable.addAsListener();
				_editable.setLocalResourceProperties();
				_editable.openEditor();
			}
			catch (Exception e)
			{
				
			}			
		}
		
	}

	private ISystemEditableRemoteObject _editable;
	public DownloadJob(ISystemEditableRemoteObject editable)
	{
		super("Download");
		_editable = editable;
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
		OpenEditorRunnable oe = new OpenEditorRunnable(_editable);
		Display.getDefault().asyncExec(oe);

		return Status.OK_STATUS;
	}
}