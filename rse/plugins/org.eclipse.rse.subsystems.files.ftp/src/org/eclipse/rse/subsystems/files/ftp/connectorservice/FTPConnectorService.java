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
 * Javier Montalvo Orus (Symbian) - Bug 140348 - FTP did not use port number
 * Javier Montalvo Orus (Symbian) - Bug 161209 - Need a Log of ftp commands
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.ftp.connectorservice;

import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.ftp.FTPService;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;



public class FTPConnectorService extends AbstractConnectorService 
{
	protected FTPService _ftpService;
	
	public FTPConnectorService(IHost host, int port)
	{		
		super(SystemFileResources.RESID_FTP_CONNECTORSERVICE_NAME,SystemFileResources.RESID_FTP_CONNECTORSERVICE_DESCRIPTION, host, port);
		_ftpService = new FTPService();
	} 
	
	public void internalConnect(IProgressMonitor monitor) throws Exception
	{
		internalConnect();
	}

	private void internalConnect() throws Exception
	{
		
		SystemSignonInformation info = getPasswordInformation();
		_ftpService.setHostName(info.getHostname());
		_ftpService.setUserId(info.getUserid());
		_ftpService.setPassword(info.getPassword());
		_ftpService.setPortNumber(getPort());
		_ftpService.setLoggingStream(getLoggingStream(info.getHostname(),getPort()));
		_ftpService.connect();	
	}
	
	private OutputStream getLoggingStream(String hostName,int portNumber)
	{
		MessageConsole messageConsole=null;
		
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (int i = 0; i < consoles.length; i++) {
			if(consoles[i].getName().equals("FTP log: "+hostName+":"+portNumber)) { //$NON-NLS-1$ //$NON-NLS-2$
				messageConsole = (MessageConsole)consoles[i];
				break;
			}	
		}
		
		if(messageConsole==null){
			messageConsole = new MessageConsole("FTP log: "+hostName+":"+portNumber, null); //$NON-NLS-1$ //$NON-NLS-2$
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ messageConsole });
		}
		
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(messageConsole);
		
		return messageConsole.newOutputStream();
	}
	
	public IFileService getFileService()
	{
		return _ftpService;
	}
	
	public void internalDisconnect(IProgressMonitor monitor)
	{
		_ftpService.disconnect();
	}
	
	
	public boolean hasRemoteServerLauncherProperties()
	{
		return false;
	}

	public boolean supportsRemoteServerLaunching()
	{
		return false;
	}

	public boolean isConnected() 
	{
		return (_ftpService != null && _ftpService.isConnected());
	}

	public boolean supportsServerLaunchProperties()
	{
		return false;
	}
	
	
}