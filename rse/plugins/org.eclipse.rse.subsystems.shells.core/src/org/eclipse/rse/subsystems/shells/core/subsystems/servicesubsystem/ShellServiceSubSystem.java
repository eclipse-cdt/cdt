/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * David McKnight   (IBM)        - [191599] Need to pass in shell encoding
 * David Dykstal (IBM) - [197036] refactored switch configuration
 ********************************************************************************/

package org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServiceSubSystemConfiguration;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystemConfiguration;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.RemoteCmdSubSystem;

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
			IRSESystemType type = getHost().getSystemType();
			if (type.isLocal())
			{
				_userHome = System.getProperty("user.home"); //$NON-NLS-1$
			}
			else if (type.isWindows())
			{
				_userHome = "c:\\"; //$NON-NLS-1$
			}
			else
			{
				// Assume UNIX compatible file system
				_userHome = "/home/" + getUserId(); //$NON-NLS-1$
			}
		}
		return _userHome;
	}

	
	protected Object[] internalRunCommand(String cmd, Object context, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, SystemMessageException
	{
		return internalRunCommand(cmd, context, false, monitor);
	}

	protected Object[] internalRunCommand(String cmd, Object context, boolean interpretOutput, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, SystemMessageException
	{
		String cwd = ""; //$NON-NLS-1$
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
		if (cwd == null || cwd.equals("null")) //$NON-NLS-1$
		{
			cwd = getUserHome();
		}


		IShellService service = getShellService();	
		IHostShell hostShell = service.runCommand(cwd, cmd, getUserAndHostEnvVarsAsStringArray(), monitor);
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

	protected IRemoteCommandShell internalRunShell(Object context, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, SystemMessageException
	{
		String cwd = ""; //$NON-NLS-1$
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
		if (cwd == null || cwd.equals("null")) //$NON-NLS-1$
		{
			cwd = getUserHome();
		}


		IShellService service = getShellService();	
		String encoding = getHost().getDefaultEncoding(true);
		IHostShell hostShell = service.launchShell(cwd, encoding, getUserAndHostEnvVarsAsStringArray(), monitor);
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
	

	protected void internalCancelShell(Object command, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		if (command instanceof IServiceCommandShell)
		{
			IServiceCommandShell cmd = (IServiceCommandShell)command;
			cmd.getHostShell().exit();
		}
	}

	protected void internalSendCommandToShell(String cmd, Object command, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
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
		IShellServiceSubSystemConfiguration config = (IShellServiceSubSystemConfiguration)getParentRemoteCmdSubSystemConfiguration();
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

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#canSwitchTo(org.eclipse.rse.core.subsystems.IServiceSubSystemConfiguration)
	 */
	public boolean canSwitchTo(IServiceSubSystemConfiguration configuration) {
		return configuration instanceof IShellServiceSubSystemConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalSwitchServiceSubSystemConfiguration(org.eclipse.rse.core.subsystems.IServiceSubSystemConfiguration)
	 */
	protected void internalSwitchServiceSubSystemConfiguration(IServiceSubSystemConfiguration newConfiguration) {
		IShellServiceSubSystemConfiguration configuration = (IShellServiceSubSystemConfiguration) newConfiguration;
		IHost host = getHost();
		setShellService(configuration.getShellService(host));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#getServiceType()
	 */
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