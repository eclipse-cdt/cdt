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
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class FileServiceArchiveTestDStore extends FileServiceArchiveTest {

	/*
	public static junit.framework.Test suite() {
		TestSuite suite = new TestSuite("FileServiceArchiveTestDStore");
		suite.addTest(new FileServiceArchiveTestDStore("testMoveToVirtualFileLevelOne"));
		return suite;
	}
	*/
	
	public void setUp() {
		//We need to delay if it is first case run after a workspace startup
		SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_LINUX_ID;
		TMP_DIR_PARENT = "/home2/xuanchen/test/";
		ZIP_SOURCE_DIR = "/home2/xuanchen/test/junit_source/";
		SYSTEM_ADDRESS = "SLES8RM";
		SYSTEM_NAME = "sles8rm_ds";
		USER_ID = "xuanchen";
		PASSWORD = "xxxxxx";
		
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
		IHost dstoreHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, SYSTEM_ADDRESS, SYSTEM_NAME, USER_ID, PASSWORD);
		assertTrue(dstoreHost != null);
		ISystemRegistry sr = SystemStartHere.getSystemRegistry(); 
		ISubSystem[] ss = sr.getServiceSubSystems(dstoreHost, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				fss = (IFileServiceSubSystem)ss[i];
				fs = fss.getFileService();
			}
		}
		try 
		{
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
	
	public void testCreateTarFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//Create the zip file first.
		String testName = "dummy.tar";
		IRemoteFile newArchiveFile = createFileOrFolder(tempDirPath, testName, false);
		assertTrue(newArchiveFile != null);
		assertTrue(newArchiveFile.exists());
		assertTrue(newArchiveFile.canRead());
		assertTrue(newArchiveFile.canWrite());
		assertEquals(newArchiveFile.getName(), testName);
		assertEquals(newArchiveFile.getParentPath(), tempDirPath);
		
		//fss.resolveFilterString(filterString, monitor)
		
		//Now, we want to create a text file inside.
		String childName = "aaa.txt";
		IRemoteFile file1 = createFileOrFolder(newArchiveFile.getAbsolutePath(), childName, false);
		assertTrue(file1 != null);
		
		childName = "bbb.txt";
		IRemoteFile file2 = createFileOrFolder(newArchiveFile.getAbsolutePath(), childName, false);
		assertTrue(file2 != null);
		
		//Create a folder
		childName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(newArchiveFile.getAbsolutePath(), childName, true);
		assertTrue(folder1 != null);
		
		//Now, check the contents
		String[] namesToCheck = {"aaa.txt", "bbb.txt", "folder1"};
		int[] typesToCheck = {TYPE_FILE, TYPE_FILE, TYPE_FOLDER};
		checkFolderContents(newArchiveFile, namesToCheck, typesToCheck);
		
		//Now, create some files inside the folder.
		String secondLevelChildName = "ccc.exe";
		IRemoteFile levelTwoChild1 = createFileOrFolder(folder1.getAbsolutePath(), secondLevelChildName, false);
		assertTrue(levelTwoChild1 != null);
		
		secondLevelChildName = "ddd.bat";
		IRemoteFile levelTwoChild2 = createFileOrFolder(folder1.getAbsolutePath(), secondLevelChildName, false);
		assertTrue(levelTwoChild2 != null);
		
		secondLevelChildName = "another Folder"; //folder with space
		IRemoteFile levelTwoChild3 = createFileOrFolder(folder1.getAbsolutePath(), secondLevelChildName, true);
		assertTrue(levelTwoChild3 != null);
		
		//Now, check the contents
		String[] namesToCheck1 = {"ccc.exe", "ddd.bat", "another Folder"};
		int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FOLDER};
		checkFolderContents(folder1, namesToCheck1, typesToCheck1);
		
		return;
		
	}

}
