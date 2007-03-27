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
 * Javier Montalvo Orus (Symbian) - Bug 140348 - FTP did not use port number
 * Javier Montalvo Orus (Symbian) - Bug 161209 - Need a Log of ftp commands
 * Javier Montalvo Orus (Symbian) - Bug 169680 - [ftp] FTP files subsystem and service should use passive mode
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ftp.connectorservice;

import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.PropertyType;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.AbstractConnectorService;
import org.eclipse.rse.core.subsystems.ICredentialsProvider;
import org.eclipse.rse.internal.services.files.ftp.FTPService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.ui.subsystems.StandardCredentialsProvider;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;



public class FTPConnectorService extends AbstractConnectorService 
{
	protected FTPService _ftpService;
	private IPropertySet _propertySet;
	private ICredentialsProvider credentialsProvider = null;
	
	public FTPConnectorService(IHost host, int port)
	{		
		super(SystemFileResources.RESID_FTP_CONNECTORSERVICE_NAME,SystemFileResources.RESID_FTP_CONNECTORSERVICE_DESCRIPTION, host, port);
		_ftpService = new FTPService();
	} 
	
	protected void internalConnect(IProgressMonitor monitor) throws Exception
	{
		internalConnect();
	}

	private void internalConnect() throws Exception
	{
		_propertySet = getPropertySet("FTP Settings"); //$NON-NLS-1$
		
		if(_propertySet==null)
		{
			_propertySet = createPropertySet("FTP Settings"); //$NON-NLS-1$
			_propertySet.addProperty("passive","false",PropertyType.getEnumPropertyType(new String[]{"true","false"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}	
		
		SystemSignonInformation info = getSignonInformation();
		_ftpService.setHostName(info.getHostname());
		_ftpService.setUserId(info.getUserId());
		_ftpService.setPassword(info.getPassword());
		_ftpService.setPortNumber(getPort());
		_ftpService.setLoggingStream(getLoggingStream(info.getHostname(),getPort()));
		_ftpService.setPropertySet(_propertySet);
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
	
	protected void internalDisconnect(IProgressMonitor monitor)
	{
		_ftpService.disconnect();
	}
	
	protected ICredentialsProvider getCredentialsProvider() {
		if (credentialsProvider == null) {
			credentialsProvider = new StandardCredentialsProvider(this);
		}
		return credentialsProvider;
	}
	
		public boolean isConnected() 
	{
		return (_ftpService != null && _ftpService.isConnected());
	}

}