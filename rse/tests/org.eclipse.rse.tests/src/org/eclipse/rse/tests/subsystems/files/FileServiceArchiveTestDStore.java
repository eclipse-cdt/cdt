/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM)               - initial API and implementation
 * Martin Oberhuber (Wind River) - Fix Javadoc warnings
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 * Martin Oberhuber (Wind River) - [195402] Add constructor with test name
 * Xuan Chen (IBM)               - [333874] [testing] Spurious NPE during testOpenFileFromTarArchive on hudson.eclipse.org
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import junit.framework.TestSuite;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class FileServiceArchiveTestDStore extends FileServiceArchiveTest {

	private boolean fPreference_ALERT_SSL;
	private boolean fPreference_ALERT_NONSSL;

	/**
	 * Constructor with specific test name.
	 * @param name test to execute
	 */
	public FileServiceArchiveTestDStore(String name) {
		super(name);
		setTargetName("linux");
	}

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
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testSuperTransferDStoreToLocal"));
		//open a virtual file in tar archive
		//suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testOpenFileFromTarArchive")); //$NON-NLS-1$

		//copy the virtual folder across connections
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVirtualFileFromDStoreToLocal")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVirtualFileLevelTwoFromDStoreToLocal")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVirtualFileFromLocalToDStore")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVirtualFileLevelTwoFromLocalToDStore")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVFLevelTwoToArchiveFromDStoreToLocal")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVFLevelTwoToArchiveFromLocalToDStore")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVFToArchiveFromDStoreToLocal")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVFToArchiveFromLocalToDStore")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVFToVFFromDStoreToLocal")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVFToVFLevelTwoFromDStoreToLocal")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVFToVFFromLocalToDStore")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testCopyVFToVFLevelTwoFromLocalToDStore")); //$NON-NLS-1$

		return suite;
	}



	protected void setupFileSubSystem() {

		IHost dstoreHost = getLinuxHost();
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

			//If you want to connect to a running server, uncomment the following statements
			/*
			IServerLauncherProperties properties = connectionService.getRemoteServerLauncherProperties();

			if (properties instanceof IRemoteServerLauncher)
			{
				IRemoteServerLauncher sl = (IRemoteServerLauncher)properties;
				sl.setServerLaunchType(ServerLaunchType.get(ServerLaunchType.RUNNING));
				fss.getSubSystemConfiguration().updateSubSystem(fss, false, "tester", true, 4033);
			}
			*/
			//end here

			connectionService.acquireCredentials(false);
			connectionService.connect(mon);

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

	public void testSuperTransferLocalToRemote() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;
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
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet3.addResource(sourceFolderToCopy1);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, tempDir, mon, true);

		//Then, we need to retrieve children of the tempDir to cache their information.
		Object[] children = fss.resolveFilterString(tempDir, null, mon);
		//Make sure there is no temp archive file left
		assertTrue(children.length == 1);

		Object theCopiedFolder = getChildFromFolder(tempDir, folderToCopyName3);
		assertNotNull(theCopiedFolder);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};

		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedFolder, childrenToCheck, typesToCheck);

		//Then, set the preference back to its original value
		store.setValue(ISystemFilePreferencesConstants.DOSUPERTRANSFER, preference_DOSUPERTRANSFER);

		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);
	}

	public void testSuperTransferDStoreWindowsAndDStore() throws Exception {
		//-test-author-:XuanChen
		getWindowsHost();
		if (isTestDisabled())
			return;
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
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects1, tempDir, mon, true);

		//Then, we need to retrieve children of the tempDir to cache their information.
		fss.resolveFilterString(tempDir, null, mon);

		IHost dstoreHost = getWindowsHost();
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
		dstoreWindowsConnectionService.acquireCredentials(false);
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
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects2, dstoreWindowsTempDir, mon, true);

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

		//delete the windows dstore temp file just created
		try {
			dstoreWindowsFss.delete(dstoreWindowsTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);
	}

	public void testSuperTransferDStoreToLocal() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;
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
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects1, tempDir, mon, true);

		//Then, we need to retrieve children of the tempDir to cache their information.
		fss.resolveFilterString(tempDir, null, mon);


		//Then, create a temparory directory the My Home of the Local connection
		//Create a temparory directory in My Home
		IRemoteFile localTempDir = null;
		try
		{
			IRemoteFile homeDirectory = localFss.getRemoteFileObject(".", mon);
			String baseFolderName = "rsetest";
			String homeFolderName = homeDirectory.getAbsolutePath();
			String testFolderName = FileServiceHelper.getRandomLocation(localFss, homeFolderName, baseFolderName, mon);
			localTempDir = createFileOrFolder(localFss, homeFolderName, testFolderName, true);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}

		//now, copy that folder in the Dstore connection into the folder in local connection
		IRemoteFile sourceFolderToCopy2 = (IRemoteFile)getChildFromFolder(tempDir, folderToCopyName3);
		ISystemDragDropAdapter srcAdapter2 = (ISystemDragDropAdapter) ((IAdaptable) sourceFolderToCopy2).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet2 = new SystemRemoteResourceSet(fss, srcAdapter2);
		fromSet2.addResource(sourceFolderToCopy2);
		ISystemResourceSet tempObjects2 = srcAdapter2.doDrag(fromSet2, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects2, localTempDir, mon, true);

		Object[] localChildren = localFss.resolveFilterString(localTempDir, null, mon);
		//Make sure there is no temp archive file left
		assertTrue(localChildren.length == 1);

		//then verify the result of copy
		Object theCopiedFolderLocal = getChildFromFolder(localFss, localTempDir, folderToCopyName3);
		assertNotNull(theCopiedFolderLocal);
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};

		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents(localFss, (IRemoteFile)theCopiedFolderLocal, childrenToCheck, typesToCheck);

		//Then, set the preference back to its original value
		store.setValue(ISystemFilePreferencesConstants.DOSUPERTRANSFER, preference_DOSUPERTRANSFER);

		//delete the windows dstore temp file just created
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);
	}

	public void testCopyVirtualFileFromDStoreToLocal() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//Create the tempDir inside the Local connection first.
		IRemoteFile localTempDir = null;
		try
		{
			IRemoteFile homeDirectory = localFss.getRemoteFileObject(".", mon);
			String baseFolderName = "rsetest";
			String homeFolderName = homeDirectory.getAbsolutePath();
			String testFolderName = FileServiceHelper.getRandomLocation(localFss, homeFolderName, baseFolderName, mon);
			localTempDir = createFileOrFolder(localFss, homeFolderName, testFolderName, true);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		//then, create a folder inside the tempDir inside the Local connection
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(localFss, localTempDir.getAbsolutePath(), folderName, true);
		assertNotNull(folder1);


		//Now, copy one of the folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) firstLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(fss, srcAdapter1);
		fromSet3.addResource(firstLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, folder1, mon, true);

		//make sure some delay before checking the result
		Thread.sleep(50);
		Object copiedVirtualFolder = getChildFromFolder(localFss, folder1, folderToCopyName1);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents(localFss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}



	public void testCopyVirtualFileLevelTwoFromDStoreToLocal() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		//copy the zip file first.
		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//Create the tempDir inside the Local connection first.
		IRemoteFile localTempDir = null;
		try
		{
			IRemoteFile homeDirectory = localFss.getRemoteFileObject(".", mon);
			String baseFolderName = "rsetest";
			String homeFolderName = homeDirectory.getAbsolutePath();
			String testFolderName = FileServiceHelper.getRandomLocation(localFss, homeFolderName, baseFolderName, mon);
			localTempDir = createFileOrFolder(localFss, homeFolderName, testFolderName, true);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		//then, create a folder inside the tempDir inside the Local connection
		String folderName = "folder1";
		String secondLeveChildName = "Team";
		IRemoteFile folder1 = createFileOrFolder(localFss, localTempDir.getAbsolutePath(), folderName, true);
		assertNotNull(folder1);

		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "Team");

		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) secondLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(fss, srcAdapter1);
		fromSet3.addResource(secondLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, folder1, mon, true);

		Thread.sleep(50);

		Object copiedVirtualFolder = getChildFromFolder(localFss, folder1, secondLeveChildName);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents(localFss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public void testCopyVirtualFileFromLocalToDStore() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		IRemoteFile sourceZipLocation = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(localFss, sourceZipLocation, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//then, create a folder inside the tempDir inside the DStore connection
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(fss, tempDir.getAbsolutePath(), folderName, true);
		assertNotNull(folder1);


		//Now, copy one of the folder from the zip file in Local connection into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(localFss, sourceZipFile, folderToCopyName1);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) firstLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet3.addResource(firstLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, folder1, mon, true);

		Object copiedVirtualFolder = getChildFromFolder(fss, folder1, folderToCopyName1);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents(fss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(sourceZipLocation, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public void testCopyVirtualFileLevelTwoFromLocalToDStore() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		IRemoteFile sourceZipLocation = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(localFss, sourceZipLocation, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//then, create a folder inside the tempDir inside the DStore connection
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(fss, tempDir.getAbsolutePath(), folderName, true);
		assertNotNull(folder1);


		//Now, copy one of the folder from the zip file in Local connection into folder1
		//Now, copy one of the level two folder from the zip file into folder1
		String secondLeveChildName = "Team";
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(localFss, sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(localFss, firstLevelChild, secondLeveChildName);

		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) secondLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet3.addResource(secondLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, folder1, mon, true);

		Object copiedVirtualFolder = getChildFromFolder(fss, folder1, secondLeveChildName);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents(fss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(sourceZipLocation, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}

	}

	public void testCopyVFToArchiveFromDStoreToLocal() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip files in dstore connection.
		createSourceZipFiles();

		//Create the zip files in local connection
		IRemoteFile localTempDir = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//The destination is the first level virtual child of a zip file
		//in local temp dir
		IRemoteFile destinationArchiveFile = (IRemoteFile)getChildFromFolder(localFss, localTempDir, zipSourceFileName2);
		//IRemoteFile destinationVirtualFolder = (IRemoteFile)getChildFromFolder(localFss, destinationArchiveFile, folderToCopyName2);

		//Now, copy one of the folder from the zip file in dstore into destinationVirtualFolder
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) firstLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(fss, srcAdapter1);
		fromSet3.addResource(firstLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, destinationArchiveFile, mon, true);

		Thread.sleep(50);
		Object copiedVirtualFolder = getChildFromFolder(localFss, destinationArchiveFile, folderToCopyName1);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents(localFss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public void testCopyVFLevelTwoToArchiveFromDStoreToLocal() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip files in dstore connection.
		createSourceZipFiles();

		//Create the zip files in local connection
		IRemoteFile localTempDir = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//The destination is the second level virtual child of a zip file
		//in local temp dir
		IRemoteFile destinationArchiveFile = (IRemoteFile)getChildFromFolder(localFss, localTempDir, zipSourceFileName2);
		//IRemoteFile firstChild = (IRemoteFile)getChildFromFolder(localFss, destinationArchiveFile, folderToCopyName2);
		//IRemoteFile destinationVirtualFolder = (IRemoteFile)getChildFromFolder(localFss, firstChild, "20070319");

		//the source is a second level child of a zip file in dstore connection temp dir
		String secondLeveChildName = "Team";
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(fss, sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(fss, firstLevelChild, secondLeveChildName);


		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) secondLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(fss, srcAdapter1);
		fromSet3.addResource(secondLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, destinationArchiveFile, mon, true);

		Thread.sleep(50);

		Object copiedVirtualFolder = getChildFromFolder(localFss, destinationArchiveFile, secondLeveChildName);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents(localFss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public void testCopyVFToArchiveFromLocalToDStore() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip files in dstore connection.
		createSourceZipFiles();

		//Create the zip files in local connection
		IRemoteFile localTempDir = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		//Source zip file is from Local connection
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(localFss, localTempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//The destination is the first level virtual child of a zip file
		//in dstore temp dir
		IRemoteFile destinationArchiveFile = (IRemoteFile)getChildFromFolder(fss, tempDir, zipSourceFileName2);
		//IRemoteFile destinationVirtualFolder = (IRemoteFile)getChildFromFolder(fss, destinationArchiveFile, folderToCopyName2);

		//Now, copy one of the folder from the zip file in local into destinationVirtualFolder
		//First, drag the virtual folder from the local zip file
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(localFss, sourceZipFile, folderToCopyName1);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) firstLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet3.addResource(firstLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		//The drop to the destination virtual folder in dstore connection.
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, destinationArchiveFile, mon, true);

		//The result is in the dstore connection
		Thread.sleep(50);
		Object copiedVirtualFolder = getChildFromFolder(fss, destinationArchiveFile, folderToCopyName1);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents(fss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public void testCopyVFLevelTwoToArchiveFromLocalToDStore() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip files in dstore connection.
		createSourceZipFiles();

		//Create the zip files in local connection
		IRemoteFile localTempDir = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		//Source zip file is in local connection
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(localFss, localTempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//The destination is the second level virtual child of a zip file
		//in dstore temp dir
		IRemoteFile destinationArchiveFile = (IRemoteFile)getChildFromFolder(fss, tempDir, zipSourceFileName2);
		//IRemoteFile firstChild = (IRemoteFile)getChildFromFolder(fss, destinationArchiveFile, folderToCopyName2);
		//IRemoteFile destinationVirtualFolder = (IRemoteFile)getChildFromFolder(fss, firstChild, "20070319");

		//the source is a second level child of a zip file in local connection temp dir
		String secondLeveChildName = "Team";
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(localFss, sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(localFss, firstLevelChild, secondLeveChildName);

		//Now, copy one of the folder from the zip file in local into destinationVirtualFolder
		//First, drag the virtual folder from the local zip file
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) secondLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet3.addResource(secondLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, destinationArchiveFile, mon, true);

		//The result is in the dstore connection
		Thread.sleep(50);
		Object copiedVirtualFolder = getChildFromFolder(fss, destinationArchiveFile, secondLeveChildName);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents(fss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public void testCopyVFToVFFromDStoreToLocal() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip files in dstore connection.
		createSourceZipFiles();

		//Create the zip files in local connection
		IRemoteFile localTempDir = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//The destination is the first level virtual child of a zip file
		//in local temp dir
		IRemoteFile destinationArchiveFile = (IRemoteFile)getChildFromFolder(localFss, localTempDir, zipSourceFileName2);
		IRemoteFile destinationVirtualFolder = (IRemoteFile)getChildFromFolder(localFss, destinationArchiveFile, folderToCopyName2);

		//Now, copy one of the folder from the zip file in dstore into destinationVirtualFolder
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) firstLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(fss, srcAdapter1);
		fromSet3.addResource(firstLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, destinationVirtualFolder, mon, true);

		Thread.sleep(50);
		Object copiedVirtualFolder = getChildFromFolder(localFss, destinationVirtualFolder, folderToCopyName1);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents(localFss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public void testCopyVFToVFLevelTwoFromDStoreToLocal() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip files in dstore connection.
		createSourceZipFiles();

		//Create the zip files in local connection
		IRemoteFile localTempDir = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//The destination is the second level virtual child of a zip file
		//in local temp dir
		IRemoteFile destinationArchiveFile = (IRemoteFile)getChildFromFolder(localFss, localTempDir, zipSourceFileName2);
		IRemoteFile firstChild = (IRemoteFile)getChildFromFolder(localFss, destinationArchiveFile, folderToCopyName2);
		IRemoteFile destinationVirtualFolder = (IRemoteFile)getChildFromFolder(localFss, firstChild, "20070319");

		//the source is a second level child of a zip file in dstore connection temp dir
		String secondLeveChildName = "Team";
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(fss, sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(fss, firstLevelChild, secondLeveChildName);


		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) secondLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(fss, srcAdapter1);
		fromSet3.addResource(secondLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, destinationVirtualFolder, mon, true);

		Thread.sleep(50);
		Object copiedVirtualFolder = getChildFromFolder(localFss, destinationVirtualFolder, secondLeveChildName);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents(localFss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public void testCopyVFToVFFromLocalToDStore() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip files in dstore connection.
		createSourceZipFiles();

		//Create the zip files in local connection
		IRemoteFile localTempDir = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		//Source zip file is from Local connection
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(localFss, localTempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//The destination is the first level virtual child of a zip file
		//in dstore temp dir
		IRemoteFile destinationArchiveFile = (IRemoteFile)getChildFromFolder(fss, tempDir, zipSourceFileName2);
		IRemoteFile destinationVirtualFolder = (IRemoteFile)getChildFromFolder(fss, destinationArchiveFile, folderToCopyName2);

		//Now, copy one of the folder from the zip file in local into destinationVirtualFolder
		//First, drag the virtual folder from the local zip file
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(localFss, sourceZipFile, folderToCopyName1);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) firstLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet3.addResource(firstLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		//The drop to the destination virtual folder in dstore connection.
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, destinationVirtualFolder, mon, true);

		//The result is in the dstore connection
		Thread.sleep(50);
		Object copiedVirtualFolder = getChildFromFolder(fss, destinationVirtualFolder, folderToCopyName1);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents(fss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public void testCopyVFToVFLevelTwoFromLocalToDStore() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip files in dstore connection.
		createSourceZipFiles();

		//Create the zip files in local connection
		IRemoteFile localTempDir = createSourceZipFiles(localFss);

		String sourceZipFileName = zipSourceFileName1;
		//Source zip file is in local connection
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(localFss, localTempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//The destination is the second level virtual child of a zip file
		//in dstore temp dir
		IRemoteFile destinationArchiveFile = (IRemoteFile)getChildFromFolder(fss, tempDir, zipSourceFileName2);
		IRemoteFile firstChild = (IRemoteFile)getChildFromFolder(fss, destinationArchiveFile, folderToCopyName2);
		IRemoteFile destinationVirtualFolder = (IRemoteFile)getChildFromFolder(fss, firstChild, "20070319");

		//the source is a second level child of a zip file in local connection temp dir
		String secondLeveChildName = "Team";
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(localFss, sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(localFss, firstLevelChild, secondLeveChildName);

		//Now, copy one of the folder from the zip file in local into destinationVirtualFolder
		//First, drag the virtual folder from the local zip file
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) secondLevelChild).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet3.addResource(secondLevelChild);
		ISystemResourceSet tempObjects3 = srcAdapter1.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, destinationVirtualFolder, mon, true);

		//The result is in the dstore connection
		Thread.sleep(50);
		Object copiedVirtualFolder = getChildFromFolder(fss, destinationVirtualFolder, secondLeveChildName);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents(fss, (IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, need to delete the temp dir in the Local connection
		try {
			localFss.delete(localTempDir, mon);
		} catch(SystemMessageException msg) {
			//ensure that super.tearDown() can run
			System.err.println("Exception on deleting local temp dir: "+msg.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

}
