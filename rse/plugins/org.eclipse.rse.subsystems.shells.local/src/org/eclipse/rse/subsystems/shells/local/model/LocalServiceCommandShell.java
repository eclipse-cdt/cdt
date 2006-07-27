package org.eclipse.rse.subsystems.shells.local.model;


import java.io.File;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.services.local.shells.ParsedOutput;
import org.eclipse.rse.internal.services.local.shells.Patterns;
import org.eclipse.rse.internal.subsystems.shells.servicesubsystem.OutputRefreshJob;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteError;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ServiceCommandShell;

public class LocalServiceCommandShell extends ServiceCommandShell
{
	private Patterns _patterns;
	private String _workingDir;
	private IRemoteFileSubSystem _fs;
	
	public LocalServiceCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{
		super(cmdSS, hostShell);
		_patterns = new Patterns();
		_patterns.update("cmd");
		ISubSystem[] sses = cmdSS.getHost().getSubSystems();
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteFileSubSystem)
			{
				_fs = (IRemoteFileSubSystem)sses[i];
			}
		}
	}

	public Object getContext()
	{
		String workingDir = _workingDir;
		if (workingDir != null && workingDir.length() > 0)
		{
			try
			{
				return _fs.getRemoteFileObject(workingDir);
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
			String line = (String)lines[i];
			ParsedOutput parsedMsg = null;
			
			try
			{
				parsedMsg = _patterns.matchLine(line);
			}
			catch (Throwable e) 
			{
				e.printStackTrace();
			}
			
			RemoteOutput output = null;
			String type = "stdout";
			if (parsedMsg != null)
			{
				type = parsedMsg.type;
			}
			else
			{
				System.out.println("parsedMsg = null");
			}
			if (event.isError())
			{
				output = new RemoteError(this, type);
			}		
			else
			{
				output = new RemoteOutput(this, type);
			}

			output.setText(line);
			if (parsedMsg != null)
			{		
				String file = parsedMsg.file;
				if (type.equals("prompt"))
				{
					_workingDir = file;
					output.setAbsolutePath(_workingDir);
				}
				/*
				else if (type.equals("file") || type.equals("directory"))
				{
					output.setAbsolutePath(parsedMsg.file);
				}
				*/
				else
				{
					output.setAbsolutePath(_workingDir + File.separatorChar + file);
				}
			}
		
			addOutput(output);
			outputs[i] = output;
		}
		if (_lastRefreshJob == null || _lastRefreshJob.isComplete())
		{
			_lastRefreshJob = new OutputRefreshJob(this, outputs, false);
			_lastRefreshJob.schedule();
		}
	}
	
	public void writeToShell(String cmd)
	{
		_patterns.update(cmd);
		super.writeToShell(cmd);

	}
	
}
