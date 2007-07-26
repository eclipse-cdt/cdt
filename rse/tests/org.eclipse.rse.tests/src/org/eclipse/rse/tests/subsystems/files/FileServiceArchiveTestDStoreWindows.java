/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Xuan Chen (IBM)               - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class FileServiceArchiveTestDStoreWindows extends FileServiceArchiveTest {

	
	public void setUp() {
		
		//We need to delay if it is first case run after a workspace startup
		SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_WINDOWS_ID;
		TMP_DIR_PARENT = "D:\\tmp\\junit_test\\";
		ZIP_SOURCE_DIR = "D:\\tmp\\junit_source\\";
		SYSTEM_ADDRESS = "LOCALHOST";
		SYSTEM_NAME = "LOCALHOST_ds";
		
		
		//We need to delay if it is first case run after a workspace startup
		if (!classBeenRunBefore)
		{
			IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
			
			//We need to setDefault first in order to set the value of a preference.  
			store.setDefault(ISystemPreferencesConstants.ALERT_SSL, ISystemPreferencesConstants.DEFAULT_ALERT_SSL);
			store.setDefault(ISystemPreferencesConstants.ALERT_NONSSL, ISystemPreferencesConstants.DEFAULT_ALERT_NON_SSL);
			store.setValue(ISystemPreferencesConstants.ALERT_SSL, false);
			store.setValue(ISystemPreferencesConstants.ALERT_NONSSL, false);
			try
			{
				System.out.println("need to sleep");
				Thread.sleep(500);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			classBeenRunBefore = true;
		}
		IHost dstoreHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, SYSTEM_ADDRESS, SYSTEM_NAME, "", "");
		assertTrue(dstoreHost != null);
		ISystemRegistry sr = SystemStartHere.getSystemRegistry(); 
		ISubSystem[] ss = sr.getServiceSubSystems(dstoreHost, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				fss = (IFileServiceSubSystem)ss[i];
				fs = fss.getFileService();
			}
		}
		try {
			IConnectorService connectionService = fss.getConnectorService();
			//If you want to change the daemon to another port, uncomment following statements
			/*
			IServerLauncherProperties properties = connectionService.getRemoteServerLauncherProperties();
			
			if (properties instanceof IRemoteServerLauncher)
			{
				IRemoteServerLauncher sl = (IRemoteServerLauncher)properties;
				sl.setDaemonPort(4075);
				
			}
			*/
			
			//If you want to connect to a running server, uncomment the following statements
			/*
			IServerLauncherProperties properties = connectionService.getRemoteServerLauncherProperties();
			
			if (properties instanceof IRemoteServerLauncher)
			{
				IRemoteServerLauncher sl = (IRemoteServerLauncher)properties;
				sl.setServerLaunchType(ServerLaunchType.get(ServerLaunchType.RUNNING));
				fss.getSubSystemConfiguration().updateSubSystem(fss, false, "xuanchen", true, 4033);
			}
			*/

			connectionService.connect(mon);
			 tempDirPath = TMP_DIR_PARENT;
			 long currentTime = System.currentTimeMillis();
			 tempDir = createFileOrFolder(tempDirPath, "rsetest" + currentTime, true);
			 assertTrue(tempDir != null);
			 assertTrue(tempDir.exists());
			 assertTrue(tempDir.canRead());
			 assertTrue(tempDir.canWrite());
			 assertTrue(tempDir.isDirectory());
			 tempDirPath = tempDir.getAbsolutePath();
			 
		} catch(Exception e) {
			assertTrue("Exception creating temp dir " + e.getStackTrace().toString(), false); //$NON-NLS-1$
		}
	}
	
	public void tearDown() {
		try {
			fss.delete(tempDir, mon);
		} catch(SystemMessageException msg) {
			assertFalse("Exception: "+msg.getLocalizedMessage(), true); //$NON-NLS-1$
		}
	}

}
