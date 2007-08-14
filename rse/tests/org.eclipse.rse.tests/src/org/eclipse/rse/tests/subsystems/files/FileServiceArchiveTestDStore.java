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

import junit.framework.TestSuite;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class FileServiceArchiveTestDStore extends FileServiceArchiveTest {
	
	private boolean fPreference_ALERT_SSL;
	private boolean fPreference_ALERT_NONSSL;
	
	public static junit.framework.Test suite() {
		TestSuite suite = new TestSuite("FileServiceArchiveTestDStore");
		
		//zip file testing
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyBatchToArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyBatchToVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyBatchToVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyBatchVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyBatchVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyToArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyToVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyToVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVirtualBatchToArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVirtualBatchToVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVirtualBatchToVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCreateZipFile")); //$NON-NLS-1$
		//suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testDeleteVirtualFileBigZip")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveToArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveToVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveToVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testRenameVirtualFile")); //$NON-NLS-1$
		//suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testRenameVirtualFileBigZip")); //$NON-NLS-1$
		
		//tar file testing
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCreateTarFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyTarVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyTarVirtualFileLevelFour")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyToTarArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyToTarVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyToTarVirtualFileLevelFour")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testDeleteTarVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveTarVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveTarVirtualFileLevelFour")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveToTarArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveToTarVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testMoveToVirtualFileLevelFour")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testRenameTarVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyBatchTarVirtualFileLevelFive"));
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyBatchToTarArchiveFile"));
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyBatchToTarVirtualFileLevelFour"));
		
		//super transfer
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testSuperTransferLocalToRemote"));
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testSuperTransferDStoreWindowsAndDStore"));
		
		//open a virtual file in tar archive
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testOpenFileFromTarArchive")); //$NON-NLS-1$
		return suite;
	}
	
	
	
	public void setUp() throws Exception {
		super.setUp();
		
		//We need to delay if it is first case run after a workspace startup
		SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_LINUX_ID;
		SYSTEM_ADDRESS = "SLES8RM";
		SYSTEM_NAME = "sles8rm_ds";
		USER_ID = "xuanchen";
		PASSWORD = "xxxxxx";
		
		//Ensure that the SSL acknowledge dialog does not show up. 
		//We need to setDefault first in order to set the value of a preference.  
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(ISystemPreferencesConstants.ALERT_SSL, ISystemPreferencesConstants.DEFAULT_ALERT_SSL);
		store.setDefault(ISystemPreferencesConstants.ALERT_NONSSL, ISystemPreferencesConstants.DEFAULT_ALERT_NON_SSL);
		fPreference_ALERT_SSL = store.getBoolean(ISystemPreferencesConstants.ALERT_SSL);
		fPreference_ALERT_NONSSL = store.getBoolean(ISystemPreferencesConstants.ALERT_NONSSL);
		store.setValue(ISystemPreferencesConstants.ALERT_SSL, false);
		store.setValue(ISystemPreferencesConstants.ALERT_NONSSL, false);

		IHost dstoreHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, SYSTEM_ADDRESS, SYSTEM_NAME, USER_ID, PASSWORD);
		assertNotNull(dstoreHost);
		ISystemRegistry sr = SystemStartHere.getSystemRegistry(); 
		ISubSystem[] ss = sr.getServiceSubSystems(dstoreHost, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				fss = (IFileServiceSubSystem)ss[i];
				fs = fss.getFileService();
			}
		}
		
		IHost localHost = getLocalSystemConnection();
		sr = SystemStartHere.getSystemRegistry(); 
		ss = sr.getServiceSubSystems(localHost, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				localFss = (IFileServiceSubSystem)ss[i];
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
				sl.setDaemonPort(8008);
				
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
			 
			//Create a temparory directory in My Home
			try
			{
				IRemoteFile homeDirectory = fss.getRemoteFileObject(".", mon);
				String baseFolderName = "rsetest";
				String homeFolderName = homeDirectory.getAbsolutePath();
				String testFolderName = FileServiceHelper.getRandomLocation(fss, homeFolderName, baseFolderName, mon);
				tempDir = createFileOrFolder(homeFolderName, testFolderName, true);
				tempDirPath = tempDir.getAbsolutePath();
			}
			catch (Exception e)
			{
				fail("Problem encountered: " + e.getStackTrace().toString());
			}
			 
		} catch(Exception e) {
			assertTrue("Exception creating temp dir " + e.getStackTrace().toString(), false); //$NON-NLS-1$
		}
	}
	
	public void tearDown() throws Exception {
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.ALERT_SSL, fPreference_ALERT_SSL);
		store.setValue(ISystemPreferencesConstants.ALERT_NONSSL, fPreference_ALERT_NONSSL);
		super.tearDown();
	}
	
	public void testSuperTransferDStoreWindowsAndDStore() throws Exception {
		String tempPath = getWorkspace().getRoot().getLocation().append("temp").toString();
		IFileStore temp = createDir(tempPath, true);
		
		createSuperTransferFolder(temp);
		
		//Set the superTransfer preference on
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		boolean preference_DOSUPERTRANSFER = store.getBoolean(ISystemFilePreferencesConstants.DOSUPERTRANSFER);
		store.setValue(ISystemFilePreferencesConstants.DOSUPERTRANSFER, true);
		
		//now, copy folderToCopy into the folder in the remote system
		IRemoteFile sourceFolderToCopy1 = localFss.getRemoteFileObject(tempPath + '\\' + folderToCopyName3, mon);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) sourceFolderToCopy1).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet1 = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet1.addResource(sourceFolderToCopy1);
		ISystemResourceSet tempObjects1 = srcAdapter1.doDrag(fromSet1, mon);
		UniversalFileTransferUtility.copyWorkspaceResourcesToRemote((SystemWorkspaceResourceSet)tempObjects1, tempDir, mon, true);
		
		//Then, we need to retrieve children of the tempDir to cache their information.
		fss.resolveFilterString(tempDir, null, mon);
		
		//Now, we need to have a Windows DStore connection, so that we could use super transfer to copy
		//directory from DStore Unix/Linux to DStore Windows
		//We need to delay if it is first case run after a workspace startup
		String systemTypeID = IRSESystemType.SYSTEMTYPE_WINDOWS_ID;
		String systemAddress = "LOCALHOST";
		String systemName = "LOCALHOST_ds";
		

		IHost dstoreHost = getRemoteSystemConnection(systemTypeID, systemAddress, systemName, "", "");
		assertNotNull(dstoreHost);
		ISystemRegistry sr = SystemStartHere.getSystemRegistry(); 
		ISubSystem[] ss = sr.getServiceSubSystems(dstoreHost, IFileService.class);
		IFileServiceSubSystem dstoreWindowsFss = null;
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				dstoreWindowsFss = (IFileServiceSubSystem)ss[i];
			}
		}
		assertNotNull(dstoreWindowsFss);
		IConnectorService dstoreWindowsConnectionService = dstoreWindowsFss.getConnectorService();
		dstoreWindowsConnectionService.connect(mon);
		
		//Then, create a temparory directory the My Home of the DStore Windows
		//Create a temparory directory in My Home
		IRemoteFile dstoreWindowsTempDir = null;
		//String dstoreWindowsTempDirPath = null;
		try
		{
			IRemoteFile homeDirectory = dstoreWindowsFss.getRemoteFileObject(".", mon);
			String baseFolderName = "rsetest";
			String homeFolderName = homeDirectory.getAbsolutePath();
			String testFolderName = FileServiceHelper.getRandomLocation(dstoreWindowsFss, homeFolderName, baseFolderName, mon);
			dstoreWindowsTempDir = createFileOrFolder(dstoreWindowsFss, homeFolderName, testFolderName, true);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		//now, copy that folder in the Dstore connection into the folder in DStore Windows connection
		IRemoteFile sourceFolderToCopy2 = (IRemoteFile)getChildFromFolder(tempDir, folderToCopyName3);
		ISystemDragDropAdapter srcAdapter2 = (ISystemDragDropAdapter) ((IAdaptable) sourceFolderToCopy2).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet2 = new SystemRemoteResourceSet(fss, srcAdapter2);
		fromSet2.addResource(sourceFolderToCopy2);
		ISystemResourceSet tempObjects2 = srcAdapter2.doDrag(fromSet2, mon);
		UniversalFileTransferUtility.copyWorkspaceResourcesToRemote((SystemWorkspaceResourceSet)tempObjects2, dstoreWindowsTempDir, mon, true);
		
		Object[] children = dstoreWindowsFss.resolveFilterString(dstoreWindowsTempDir, null, mon);
		//Make sure there is no temp archive file left 
		assertTrue(children.length == 1);
		
		//then verify the result of copy
		Object theCopiedFolder = getChildFromFolder(dstoreWindowsFss, dstoreWindowsTempDir, folderToCopyName3);
		assertNotNull(theCopiedFolder);
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents(dstoreWindowsFss, (IRemoteFile)theCopiedFolder, childrenToCheck, typesToCheck);
		
		//Then, set the preference back to its original value
		store.setValue(ISystemFilePreferencesConstants.DOSUPERTRANSFER, preference_DOSUPERTRANSFER);
		
		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);
	}

}
