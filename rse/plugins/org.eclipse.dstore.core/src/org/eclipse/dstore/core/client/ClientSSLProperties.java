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

package org.eclipse.dstore.core.client;

import org.eclipse.dstore.core.model.ISSLProperties;
public class ClientSSLProperties implements ISSLProperties
{
	private boolean _enableSSL = false;
	private boolean _disableServerSSL = false;
	private String _daemonKeyStorePath;
	private String _daemonKeyStorePassword;
	
	private String _serverKeyStorePath;
	private String _serverKeyStorePassword;
	
	public ClientSSLProperties(boolean enableSSL, 
							String daemonKeystore, String daemonPassword,
								String serverKeystore, String serverPassword)
	{
		_enableSSL = enableSSL;
		_daemonKeyStorePath = daemonKeystore;
		_daemonKeyStorePassword = daemonPassword;
		_serverKeyStorePath = serverKeystore;
		_serverKeyStorePassword = serverPassword;
	}
	
	public ClientSSLProperties(boolean enableSSL, boolean disableServerSSL,
			String daemonKeystore, String daemonPassword,
				String serverKeystore, String serverPassword)
	{
		_enableSSL = enableSSL;
		_disableServerSSL = disableServerSSL;
		_daemonKeyStorePath = daemonKeystore;
		_daemonKeyStorePassword = daemonPassword;
		_serverKeyStorePath = serverKeystore;
		_serverKeyStorePassword = serverPassword;
	}
	
	public ClientSSLProperties(boolean enableSSL, String keystore, String password)
	{
		_enableSSL = enableSSL;
		_daemonKeyStorePath = keystore;
		_daemonKeyStorePassword = password;

		_serverKeyStorePath = keystore;
		_serverKeyStorePassword = password;
	}
	
	public ClientSSLProperties(boolean enableSSL, boolean disableServerSSL, String keystore, String password)
	{
		_enableSSL = enableSSL;
		_disableServerSSL = disableServerSSL;
		_daemonKeyStorePath = keystore;
		_daemonKeyStorePassword = password;

		_serverKeyStorePath = keystore;
		_serverKeyStorePassword = password;
	}
	
	
	public boolean usingSSL()
	{
		return _enableSSL;
	}
	
	public boolean usingServerSSL()
	{
		return !_disableServerSSL;
	}


	public String getDaemonKeyStorePassword()
	{
		return _daemonKeyStorePassword;
	}

	public String getDaemonKeyStorePath()
	{
		return _daemonKeyStorePath;
	}

	public String getServerKeyStorePassword()
	{
		return _serverKeyStorePassword;
	}

	public String getServerKeyStorePath()
	{
		return _serverKeyStorePath;
	}

}