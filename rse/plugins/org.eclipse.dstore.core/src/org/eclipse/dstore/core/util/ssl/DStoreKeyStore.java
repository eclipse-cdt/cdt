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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;



public class DStoreKeyStore
{
	public static DStoreKeyStore _instance = null;

	public DStoreKeyStore()
	{
		_instance = this;
	}

	public static DStoreKeyStore getInstance()
	{
		if (_instance == null)
		{
			new DStoreKeyStore();
		}
		return _instance;
	}

	public static KeyStore getKeyStore(String filePath, String password)
	throws KeyStoreException, NoSuchAlgorithmException,
	CertificateException, IOException, NoSuchProviderException

	{
		KeyStore keyStore= null;


		if (filePath != null)
		{
			File keyStoreFile = new File(filePath);
			
			/* Do not stomp an existing file */
			if(!keyStoreFile.exists()) 
			{
				keyStore =  KeyStore.getInstance("JKS");
				keyStore.load(null, password.toCharArray());
				persistKeyStore(keyStore, filePath, password);
			}
			else {
				keyStore = loadKeyStore(filePath, password);
			}		
		}
		
		return keyStore;
	}


	public static KeyStore loadKeyStore(String pathname, String password)
	   throws KeyStoreException,
	           NoSuchAlgorithmException,
	           CertificateException,
	           IOException,
	           NoSuchProviderException  {
		
		KeyStore ks=null;
		File file=new File(pathname);
		
		/* Do not stomp an existing file */
		if(file.exists()) {
			ks=KeyStore.getInstance("JKS");
			/* Initialize the keystore with no information */
			FileInputStream is=new FileInputStream(file);
			ks.load(is, password.toCharArray());
			is.close();
		}
		return ks;
	}
	
	public static Certificate loadCertificate(String certFilename) 
	   throws CertificateException,
	           FileNotFoundException {
		
		CertificateFactory factory=CertificateFactory.getInstance("X.509");
		
		return factory.generateCertificate(new FileInputStream(certFilename));
	}
	
	public static void addCertificateToKeyStore(KeyStore ks, Certificate cert, String alias)
		      throws KeyStoreException {
		ks.setCertificateEntry(alias, cert);
			
	}
	
	public static void persistKeyStore(KeyStore ks, String pathname, String password) 
		throws KeyStoreException,
		        FileNotFoundException,
		        NoSuchAlgorithmException,
		        CertificateException,
		        IOException {
		FileOutputStream os=new FileOutputStream(pathname);
		ks.store(os, password.toCharArray());
		os.close();
		
		
	}
}