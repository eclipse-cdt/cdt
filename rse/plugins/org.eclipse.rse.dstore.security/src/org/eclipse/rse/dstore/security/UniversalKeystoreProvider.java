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

package org.eclipse.rse.dstore.security;


import java.util.List;

import org.eclipse.rse.core.comm.ISystemKeystoreProvider;
import org.eclipse.rse.dstore.security.wizards.SystemImportCertAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public class UniversalKeystoreProvider implements ISystemKeystoreProvider
{
	public class ImportCertificateRunnable implements Runnable
	{
		private List _certificates;
		private ISystemKeystoreProvider _provider;
		private boolean _wasCancelled = false;
		private String _systemName;
		
		public ImportCertificateRunnable(ISystemKeystoreProvider provider, List certs, String systemName)
		{			
			_certificates = certs;
			_provider = provider;
			_systemName = systemName;
		}
				
		public boolean wasCancelled()
		{
			return _wasCancelled;
		}
		
		public void run()
		{
			Shell shell = Display.getDefault().getActiveShell();
			SystemImportCertAction importAction = new SystemImportCertAction(_provider, _certificates, _systemName);
			importAction.run();
			_wasCancelled = importAction.wasCancelled();
		}	
	}
	
	public String getKeyStorePassword()
	{
		return UniversalSecurityPlugin.getKeyStorePassword();
	}

	public String getKeyStorePath()
	{
		return UniversalSecurityPlugin.getKeyStoreLocation();
	}

	public boolean importCertificates(List certs, String systemName)
	{
		Display display = Display.getDefault(); 
		ImportCertificateRunnable impRun = new ImportCertificateRunnable(this, certs, systemName);
		display.syncExec(impRun);
		
		return !impRun.wasCancelled();
	}
}