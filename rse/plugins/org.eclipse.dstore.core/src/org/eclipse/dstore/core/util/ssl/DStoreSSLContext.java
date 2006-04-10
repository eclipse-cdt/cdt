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

package org.eclipse.dstore.core.util.ssl;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;


public class DStoreSSLContext
{

	public static SSLContext getServerSSLContext(String filePath, String password)
	{
		SSLContext serverContext = null;

		try
		{
			KeyStore ks = DStoreKeyStore.getKeyStore(filePath, password);
			String keymgrAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(keymgrAlgorithm);
			kmf.init(ks, password.toCharArray());				

			serverContext = SSLContext.getInstance("SSL");
			serverContext.init(kmf.getKeyManagers(), null, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return serverContext;
	}
	
	public static SSLContext getClientSSLContext(String filePath, String password, DataStoreTrustManager trustManager)
	{
		SSLContext clientContext = null;

		try
		{
			trustManager.setKeystore(filePath, password);			
			clientContext = SSLContext.getInstance("SSL");
			TrustManager[] mgrs = new TrustManager[1];
			mgrs[0] = trustManager;
			
			
			clientContext.init(null, mgrs, null);
			}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	
		return clientContext;		
	}
	

}