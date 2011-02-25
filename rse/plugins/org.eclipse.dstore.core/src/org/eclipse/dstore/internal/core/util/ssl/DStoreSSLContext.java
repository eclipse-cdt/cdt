/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
 * David McKnight  (IBM) - [259905][api] provide public API for getting/setting key managers for SSLContext
 * David McKnight  (IBM)  - [264858][dstore] OpenRSE always picks the first trusted certificate
 * David McKnight   (IBM) - [283613] [dstore] Create a Constants File for all System Properties we support
 *******************************************************************************/

package org.eclipse.dstore.internal.core.util.ssl;

import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import org.eclipse.dstore.core.util.ssl.BaseSSLContext;
import org.eclipse.dstore.core.util.ssl.DStoreKeyStore;
import org.eclipse.dstore.core.util.ssl.IDataStoreTrustManager;
import org.eclipse.dstore.internal.core.model.IDataStoreSystemProperties;


public class DStoreSSLContext
{
	
	public static SSLContext getServerSSLContext(String filePath, String password)
	{
		SSLContext serverContext = null;

		try
		{
			KeyManager[] keyManagers = BaseSSLContext.getKeyManagers();
			if (keyManagers == null)
			{
				KeyStore ks = DStoreKeyStore.getKeyStore(filePath, password);
				String keymgrAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(keymgrAlgorithm);
				kmf.init(ks, password.toCharArray());								
	
				serverContext = SSLContext.getInstance("SSL"); //$NON-NLS-1$
				
				keyManagers = kmf.getKeyManagers();
				
				// read optional system property that indicates a default certificate alias
				String defaultAlias = System.getProperty(IDataStoreSystemProperties.DSTORE_DEFAULT_CERTIFICATE_ALIAS); 
				if (defaultAlias != null){
					KeyManager[] x509KeyManagers = new X509KeyManager[10];
				
					for(int i=0;i<keyManagers.length; i++){
						if(keyManagers[i] instanceof X509KeyManager){						
							x509KeyManagers[i] = new DStoreKeyManager((X509KeyManager)keyManagers[i], defaultAlias);
						}
					}								
					serverContext.init(x509KeyManagers, null, null);
				}
				else {
					serverContext.init(keyManagers, null, null);
				}
			}
			else
			{
				serverContext = SSLContext.getInstance("SSL"); //$NON-NLS-1$
				serverContext.init(keyManagers, null, null);
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
			
			
			KeyManager[] keyManagers = BaseSSLContext.getKeyManagers();
			clientContext.init(keyManagers, mgrs, null);
			}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	
		return clientContext;		
	}
	

}
