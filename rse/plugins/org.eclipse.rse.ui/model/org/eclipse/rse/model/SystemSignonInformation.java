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

package org.eclipse.rse.model;


/**
 * This class encapsulates the signon information required for a remote system.  This class 
 * must be secure and never disclose the password for the remote system in its unencrypted form.
 * However the encrypted form of the password is not considered secret information and can be
 * accessed by anyone.
 * 
 * @author yantzi
 */
public final class SystemSignonInformation {

	
	private String _hostname, _userid, _systemType, _password;
	
	/**
	 * Default no-arg constructor
	 */
	public SystemSignonInformation()
	{
	}

	/**
	 * Constructor for SystemSignonInformation.
	 */
	public SystemSignonInformation(String hostname, String userid, String systemType) {
		_hostname = hostname;//RSEUIPlugin.getQualifiedHostName(hostname).toUpperCase();
		_userid = userid;
		_systemType = systemType;	
	}
	
	/**
	 * Constructor for SystemSignonInformation.
	 */
	public SystemSignonInformation(String hostname, String userid, String password, String systemType) {
		_hostname = hostname;//RSEUIPlugin.getQualifiedHostName(hostname).toUpperCase();
		_userid = userid;
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
	 * Returns the systemType of the remote system
	 * @return String
	 */
	public String getSystemType() {
		return _systemType;
	}

	/**
	 * Returns the userid for the remote system
	 * @return String
	 */
	public String getUserid() {
		return _userid;
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
	public void setSystemType(String systemType) {
		_systemType = systemType;
	}

	/**
	 * Sets the userid.
	 * @param userid The userid to set
	 */
	public void setUserid(String userid) {
		_userid = userid;
	}

}