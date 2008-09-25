/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 ********************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.subsystems.ICredentials;

/**
 * This class encapsulates the signon information required for a remote system.  This class 
 * must be secure and never disclose the password for the remote system in its unencrypted form.
 * However the encrypted form of the password is not considered secret information and can be
 * accessed by anyone.
 */
public final class SystemSignonInformation implements ICredentials {

	private IRSESystemType _systemType;
	private String _hostname;
	private String _userId;
	private String _password;

	/**
	 * Default no-arg constructor
	 */
	public SystemSignonInformation() {
	}

	/**
	 * Constructor for SystemSignonInformation.
	 */
	public SystemSignonInformation(String hostname, String userid, IRSESystemType systemType) {
		_hostname = hostname;//RSEUIPlugin.getQualifiedHostName(hostname).toUpperCase();
		_userId = userid;
		_systemType = systemType;
	}

	/**
	 * Constructor for SystemSignonInformation.
	 */
	public SystemSignonInformation(String hostname, String userid, String password, IRSESystemType systemType) {
		_hostname = hostname;//RSEUIPlugin.getQualifiedHostName(hostname).toUpperCase();
		_userId = userid;
		_password = password;
		_systemType = systemType;
	}

	/**
	 * Returns the hostname of the remote system
	 * @return String 
	 */
	public String getHostname() {
		return _hostname;
	}

	/**
	 * Returns the systemType of the remote system.
	 * @return the systemType object.
	 */
	public IRSESystemType getSystemType() {
		return _systemType;
	}

	/**
	 * Returns the userid for the remote system
	 * @return the user ID.
	 */
	public String getUserId() {
		return _userId;
	}

	/**
	 * Return the password for the remote system
	 */
	public String getPassword() {
		return _password;
	}

	/**
	 * Sets the password for the remote system
	 */
	public void setPassword(String string) {
		_password = string;
	}

	/**
	 * Sets the hostname.
	 * @param hostname The hostname to set
	 */
	public void setHostname(String hostname) {
		_hostname = hostname;//RSEUIPlugin.getQualifiedHostName(hostname).toUpperCase();
	}

	/**
	 * Sets the systemType.
	 * @param systemType The systemType to set
	 */
	public void setSystemType(IRSESystemType systemType) {
		_systemType = systemType;
	}

	/**
	 * Sets the userid.
	 * @param userId The userid to set
	 */
	public void setUserId(String userId) {
		_userId = userId;
	}

}