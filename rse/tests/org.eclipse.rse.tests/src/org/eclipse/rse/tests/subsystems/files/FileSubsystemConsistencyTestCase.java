/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * Test cases for comparing various file subsystem operations
 */
public class FileSubsystemConsistencyTestCase extends RSEBaseConnectionTestCase {
	
	private List _subSystems;
	private List _connections;
	
	private String SYSTEM_ADDRESS = "SLES8RM";
	private String USER_ID = "dmcknigh";
	private String PASSWORD = "xxxxxx";
	
	/*
	private SYSTEM_ADDRESS = "dmcknigh3";
	private USER_ID = "tester";
	private PASSWORD = "xxxxxx";
	*/
		
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		if (_subSystems != null) {
			for (int i = 0; i < _subSystems.size(); i++) {
				IRemoteFileSubSystem ss = (IRemoteFileSubSystem)_subSystems.get(i);
				if (ss != null && ss.isConnected()) {
					ss.disconnect(true);
				}
					
			}
		}
		
		if (_connections != null)
		{
			for (int j = 0; j < _connections.size(); j++) {
				IHost host = (IHost)_connections.get(j);
				if (host != null) {
					getConnectionManager().removeConnection(host.getSystemProfileName(), host.getName());
				}
			}
		}

		_subSystems.clear();
		_connections.clear();
		_subSystems = null;
		_connections = null;
		
		super.tearDown();
	}
	
	protected void setupConnections() {
		_connections = new ArrayList();
		_subSystems = new ArrayList();
		
		
		// setup dstore connection
		addSystem(getDStoreHost());
		
		// setup ssh connection
		addSystem(getSSHHost());

		// setup ftp connection
		addSystem(getFTPHost());						
	}
	
	private void addSystem(IHost host) {
		IRemoteFileSubSystem fss = null;
		ISystemRegistry sr = SystemStartHere.getSystemRegistry(); 
		ISubSystem[] ss = sr.getServiceSubSystems(host, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IRemoteFileSubSystem) {
				fss = (IRemoteFileSubSystem)ss[i];
			}
		}		
		_subSystems.add(fss);
		_connections.add(host);
	}

	protected IHost getSSHHost()
	{
		IHost sshHost = null;

		String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID;
		String SYSTEM_NAME = "sles8rm_ssh";

		sshHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, SYSTEM_ADDRESS, SYSTEM_NAME, USER_ID, PASSWORD);
		assertNotNull(sshHost);
		return sshHost;
	}
	
	protected IHost getFTPHost()
	{
		IHost ftpHost = null;

		String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_FTP_ONLY_ID;
		String SYSTEM_NAME = "sles8rm_ftp";

		ftpHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, SYSTEM_ADDRESS, SYSTEM_NAME, USER_ID, PASSWORD);
		assertNotNull(ftpHost);
		return ftpHost;
	}
	
	protected IHost getDStoreHost()
	{
		IHost dstoreHost = null;

		String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_LINUX_ID;
		String SYSTEM_NAME = "sles8rm_dstore";

		//Ensure that the SSL acknowledge dialog does not show up. 
		//We need to setDefault first in order to set the value of a preference.  
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(ISystemPreferencesConstants.ALERT_SSL, ISystemPreferencesConstants.DEFAULT_ALERT_SSL);
		store.setDefault(ISystemPreferencesConstants.ALERT_NONSSL, ISystemPreferencesConstants.DEFAULT_ALERT_NON_SSL);

		store.setValue(ISystemPreferencesConstants.ALERT_SSL, false);
		store.setValue(ISystemPreferencesConstants.ALERT_NONSSL, false);

		dstoreHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, SYSTEM_ADDRESS, SYSTEM_NAME, USER_ID, PASSWORD);
		assertNotNull(dstoreHost);
		
		return dstoreHost;
	}
	


	/**
	 * Test the implicit connect of each connection when calling getRemoteFileObject().
	 */
	public void testImplicitConnectViaFileSubSystem() {
		if (!RSETestsPlugin.isTestCaseEnabled("FileSubsystemConsistencyTestCase.testImplicitConnectViaFileSubSystem")) return; //$NON-NLS-1$
		setupConnections();
		
		String testPath = "/usr/lib";

		for (int i = 0; i < _subSystems.size(); i++) {
			IRemoteFileSubSystem ss = (IRemoteFileSubSystem)_subSystems.get(i);
			
			// ensure that the system is NOT connected
			if (ss.isConnected()) {
				try {
					ss.disconnect(true);
				}
				catch (Exception e) {
					// disconnect failed
				}				
			}
			
			String systemType = ss.getHost().getSystemType().getLabel();
			
			Exception exception = null;
			String cause = null;
			IRemoteFile remoteFile = null;
			
			try {
				remoteFile = ss.getRemoteFileObject(testPath, new NullProgressMonitor());
			}
			catch (Exception e){
				exception = e;
				cause = e.getLocalizedMessage();
			}

			assertNull(systemType + ":Exception getting remote file! Possible cause: " + cause, exception); //$NON-NLS-1$
			assertTrue(ss.isConnected());
			assertNotNull(systemType + ":Unexpected return value for getRemoteFile().  Remote file is null!", remoteFile);						
		}
	}
	
	
}




