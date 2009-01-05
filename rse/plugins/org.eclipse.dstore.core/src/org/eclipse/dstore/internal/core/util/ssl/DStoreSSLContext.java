/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM) - [225507][api][breaking] RSE dstore API leaks non-API types
 * Noriaki Takatsu  (IBM) - [259905][api] Provide a facility to use its own keystore
 *******************************************************************************/

package org.eclipse.dstore.internal.core.util.ssl;

import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.dstore.core.util.ssl.DStoreKeyStore;
import org.eclipse.dstore.core.util.ssl.IDataStoreTrustManager;


public class DStoreSSLContext
{
	private static KeyManager[] _keyManagers;

	public static void setKeyManager(KeyManager[] keyManagers)
	{
		_keyManagers = keyManagers;
	}
	
	public static SSLContext getServerSSLContext(String filePath, String password)
	{
		SSLContext serverContext = null;

		try
		{
			if (_keyManagers == null)
			{
				KeyStore ks = DStoreKeyStore.getKeyStore(filePath, password);
				String keymgrAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(keymgrAlgorithm);
				kmf.init(ks, password.toCharArray());				

				serverContext = SSLContext.getInstance("SSL"); //$NON-NLS-1$
				serverContext.init(kmf.getKeyManagers(), null, null);
			}
			else
			{
				serverContext = SSLContext.getInstance("SSL"); //$NON-NLS-1$
				serverContext.init(_keyManagers, null, null);
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return serverContext;
	}
	
	public static SSLContext getClientSSLContext(String filePath, String password, IDataStoreTrustManager trustManager)
	{
		SSLContext clientContext = null;

		try
		{
			trustManager.setKeystore(filePath, password);			
			clientContext = SSLContext.getInstance("SSL"); //$NON-NLS-1$
			TrustManager[] mgrs = new TrustManager[1];
			mgrs[0] = trustManager;
			
			
			clientContext.init(_keyManagers, mgrs, null);
			}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	
		return clientContext;		
	}
	

}
