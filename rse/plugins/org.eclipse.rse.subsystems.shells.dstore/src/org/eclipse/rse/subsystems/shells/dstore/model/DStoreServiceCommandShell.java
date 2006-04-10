package org.eclipse.rse.subsystems.shells.dstore.model;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.internal.services.dstore.shell.DStoreShellOutputReader;
import org.eclipse.rse.internal.subsystems.shells.servicesubsystem.OutputRefreshJob;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteError;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteOutput;
import org.eclipse.rse.services.dstore.shells.DStoreHostShell;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ServiceCommandShell;

public class DStoreServiceCommandShell extends ServiceCommandShell
{

	public DStoreServiceCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{
		super(cmdSS, hostShell);
	}

	public Object getContext()
	{
		DStoreHostShell shell = (DStoreHostShell)getHostShell();
		DStoreShellOutputReader reader = (DStoreShellOutputReader)shell.getStandardOutputReader();
		String workingDir = reader.getWorkingDirectory();
		if (workingDir != null && workingDir.length() > 0)
		{
			try
			{
				return getFileSubSystem().getRemoteFileObject(workingDir);
			}
			catch (Exception e)
			{			
			}
		}
		return null;

	}

	public void shellOutputChanged(IHostShellChangeEvent event)
	{
		Object[] lines = event.getLines();
		IRemoteOutput[] outputs = new IRemoteOutput[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			RemoteOutput output = null;
			Object lineObj = lines[i];
			if (lineObj instanceof DataElement)
			{
				DataElement line = (DataElement)lineObj;
				if (line != null)
				{
					String type = line.getType();
					if (event.isError())
					{
						output = new RemoteError(this, type);									
					}
					else
					{
						output = new RemoteOutput(this, type);
					}
					output.setText(line.getName());
					output.setAbsolutePath(line.getSource());	
		
					addOutput(output);
					outputs[i] = output;
				}
			}
		}
		if (_lastRefreshJob == null || _lastRefreshJob.isComplete())
		{
			_lastRefreshJob = new OutputRefreshJob(this, outputs, false);
			_lastRefreshJob.schedule();
		}
	}
	
}
