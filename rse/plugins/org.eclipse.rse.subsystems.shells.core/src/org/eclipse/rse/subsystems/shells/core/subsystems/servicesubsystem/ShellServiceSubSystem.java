/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.servicesubsystem.IServiceSubSystemConfiguration;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteCmdSubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.swt.widgets.Shell;



public final class ShellServiceSubSystem extends RemoteCmdSubSystem implements IShellServiceSubSystem 
{
	protected String _userHome = null;
	protected IShellService _hostService;


	public ShellServiceSubSystem(IHost host, IConnectorService connectorService, IShellService hostService)
	{
		super(host, connectorService);
		_hostService = hostService;
	}
	
	public IShellService getShellService()
	{
		return _hostService;
	}
	
	public void setShellService(IShellService service)
	{
		_hostService = service;
	}
	
	protected String getUserHome()
	{
		if (_userHome == null)
		{
			if (getSystemType() == IRSESystemType.SYSTEMTYPE_WINDOWS)
			{
				_userHome = "c:\\";
			}
			else if (getSystemType() == IRSESystemType.SYSTEMTYPE_LOCAL)
			{
				_userHome = System.getProperty("user.home");
			}
			else
			{
				// Assume UNIX compatible file system
				_userHome = "/home/" + getUserId();
			}
		}
		return _userHome;
	}

	
	protected Object[] internalRunCommand(IProgressMonitor monitor, String cmd, Object context) throws InvocationTargetException, InterruptedException, SystemMessageException
	{
		return internalRunCommand(monitor, cmd, context, false);
	}

	protected Object[] internalRunCommand(IProgressMonitor monitor, String cmd, Object context, boolean interpretOutput) throws InvocationTargetException, InterruptedException, SystemMessageException
	{
		String cwd = "";
		if (context instanceof IRemoteFile)
		{
			IRemoteFile file = (IRemoteFile) context;
			cwd = file.getAbsolutePath();
		}
		else if (context instanceof String)
		{
			// assume the string is a remote path
			cwd = (String)context;
		}
		if (cwd == null || cwd.equals("null"))
		{
			cwd = getUserHome();
		}


		IShellService service = getShellService();	
		IHostShell hostShell = service.runCommand(monitor, cwd, cmd, getUserAndHostEnvVarsAsStringArray());
		IServiceCommandShell cmdShell = createRemoteCommandShell(this, hostShell);
		hostShell.addOutputListener(cmdShell);
	
	
		if (_cmdShells.size() == 0)
		{
			// if this is first shell, start listening so that on disconnect, we persist
			getConnectorService().addCommunicationsListener(this);
		}
		_cmdShells.add(cmdShell);



		return new Object[] {cmdShell};
	}

	protected IRemoteCommandShell internalRunShell(IProgressMonitor monitor, Object context) throws InvocationTargetException, InterruptedException, SystemMessageException
	{
		String cwd = "";
		if (context instanceof IRemoteFile)
		{
			IRemoteFile file = (IRemoteFile) context;
			cwd = file.getAbsolutePath();
		}
		else if (context instanceof String)
		{
			// assume the string is a remote path
			cwd = (String)context;
		}
		if (cwd == null || cwd.equals("null"))
		{
			cwd = getUserHome();
		}


		IShellService service = getShellService();	
		IHostShell hostShell = service.launchShell(monitor, cwd, getUserAndHostEnvVarsAsStringArray());
		IServiceCommandShell cmdShell = createRemoteCommandShell(this, hostShell);
		if (cmdShell != null)
		{
			hostShell.addOutputListener(cmdShell);
		
		
			if (_cmdShells.size() == 0)
			{
				// if this is first shell, start listening so that on disconnect, we persist
				getConnectorService().addCommunicationsListener(this);
			}
			_cmdShells.add(cmdShell);

		}

		return cmdShell;
	}
	

	protected void internalCancelShell(IProgressMonitor monitor, Object command) throws InvocationTargetException, InterruptedException
	{
		if (command instanceof IServiceCommandShell)
		{
			IServiceCommandShell cmd = (IServiceCommandShell)command;
			cmd.getHostShell().exit();
		}
	}

	protected void internalSendCommandToShell(IProgressMonitor monitor, String cmd, Object command) throws InvocationTargetException, InterruptedException
	{
		if (command instanceof IServiceCommandShell)
		{
			IServiceCommandShell cmdWrapper = (IServiceCommandShell)command;
			cmdWrapper.writeToShell(cmd);
			cmdWrapper.updateHistory(cmd);
		}			
	}
	
	protected IServiceCommandShell createRemoteCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell)
	{
		IShellServiceSubSystemConfiguration config = (IShellServiceSubSystemConfiguration)getParentRemoteCmdSubSystemFactory();
		return config.createRemoteCommandShell(cmdSS, hostShell);
	}
	
	public String[] getHostEnvironment()
	{
		return getShellService().getHostEnvironment();
	}
	
	public List getHostEnvironmentVariables()
	{
		List l = new ArrayList();
		String[] vars = getHostEnvironment();
		for (int i = 0; i < vars.length; i++)
		{
			l.add(vars[i]);
		}
		return l;
	}

	/**
	 * swtich from one protocol to another
	 */
	public void switchServiceFactory(IServiceSubSystemConfiguration fact)
	{		
		if (fact != getSubSystemConfiguration() && fact instanceof IShellServiceSubSystemConfiguration)
		{
			IShellServiceSubSystemConfiguration factory = (IShellServiceSubSystemConfiguration)fact;
			try
			{
				disconnect(SystemBasePlugin.getActiveWorkbenchShell());
			}
			catch (Exception e)
			{	
			}
			
			IHost host = getHost();
			setSubSystemConfiguration(factory);
			setName(factory.getName());
			IConnectorService oldConnectorService = getConnectorService();			
			oldConnectorService.deregisterSubSystem(this);
			
			IConnectorService newConnectorService = factory.getConnectorService(host);
			setConnectorService(newConnectorService);
			
			oldConnectorService.commit();
			newConnectorService.commit();
			
			setShellService(factory.getShellService(host));
		}
	}
	
	public Class getServiceType()
	{
		return IShellService.class;
	}

	public void initializeSubSystem(IProgressMonitor monitor)
	{ 
		getShellService().initService(monitor);
	}

	public void uninitializeSubSystem(IProgressMonitor monitor)
	{
		cancelAllShells();
		getShellService().uninitService(monitor);
	}
	
} 