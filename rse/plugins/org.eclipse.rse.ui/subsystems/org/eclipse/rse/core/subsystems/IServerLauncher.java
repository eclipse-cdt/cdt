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


package org.eclipse.rse.core.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.model.SystemSignonInformation;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;


/**
 * This interface captures the abstract lifecyle for launching the remote server,
 * and subsequently connecting to it.
 * @see org.eclipse.rse.core.subsystems.IServerLauncherProperties
 */
public interface IServerLauncher
{
	
	/**
	 * Set the remote system signon information
	 */
	public void setSignonInformation(SystemSignonInformation info);
	
	/**
	 * Get the remote system signon information, as set in
	 *  {@link #setSignonInformation(SystemSignonInformation)}
	 */
	public SystemSignonInformation getSignonInformation();
	
	/**
	 * Set the object which contains the user-specified properties that 
	 *  are used by this launcher
	 */
	public void setServerLauncherProperties(IServerLauncherProperties propertyInfo);
	
	/**
	 * Get the object which contians the user-specified properties that are
	 *  used by this launcher. As set in {@link #setServerLauncherProperties(IServerLauncherProperties)}. 
	 */
	public IServerLauncherProperties getServerLauncherProperties();
	
	/**
	 * Determine if the remote server needs to be launched or not.
	 * Generally is always false.
	 * @return true if the remote server is already launched, false if it needs to be.
	 */
	public boolean isLaunched();
		
	/**
	 * Launch the remote server. Some subclasses may not need this step,
	 *  if the server is already running.
	 * @see #getErrorMessage()
	 * @param monitor - a monitor for showing progress
	 * @return an object. Up to each implementor how to interpret.
	 */
	public Object launch(IProgressMonitor monitor) throws Exception;

	/**
	 * Determine if we are connected to the remote server or not.  
	 * @return true if we are connected, false otherwise.
	 */
	public boolean isConnected();

	/**
	 * Connect to the remote server. 
	 * @see #getErrorMessage()
	 * @param monitor a monitor for showing progress
	 * @param connectPort the port to use for launching the server
	 * @return Anything you want.
	 */
	public Object connect(IProgressMonitor monitor, int connectPort) throws Exception;
	
	/**
	 * Disconnect from the remote server
	 * @see #getErrorMessage()
	 */
	public void disconnect() throws Exception;
	
	/**
	 * Returns the host error message if there was a problem connecting to the host.
	 * If there was no problem, this returns null
	 * 
	 * @return the error message.
	 */		
	public SystemMessage getErrorMessage();
}