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
import org.eclipse.rse.core.subsystems.IRemoteServerLauncher;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class FileServiceArchiveTestDStore extends FileServiceArchiveTest {

	protected String tarSourceFileName1 = "source.tar";
	protected String tarSourceFileName2 = "mynewtar.tar";
	
	protected String tarSourceFolderName1 = "META-INF";
	protected String tarSourceFolderName2 = "org";
	
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
		
		return suite;
	}
	
	
	
	public void setUp() {
		//We need to delay if it is first case run after a workspace startup
		SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_LINUX_ID;
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
			
			IServerLauncherProperties properties = connectionService.getRemoteServerLauncherProperties();
			
			if (properties instanceof IRemoteServerLauncher)
			{
				IRemoteServerLauncher sl = (IRemoteServerLauncher)properties;
				sl.setDaemonPort(8008);
				
			}
			
			
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
	
	public void tearDown() {
		try {
			fss.delete(tempDir, mon);
		} catch(SystemMessageException msg) {
			assertFalse("Exception: "+msg.getLocalizedMessage(), true); //$NON-NLS-1$
		}
	}
	
	public void createSourceTarFiles()
	{
		try
		{
			String tempPath = getWorkspace().getRoot().getLocation().append("temp").toString();
			IFileStore temp = createDir(tempPath, true);
			String content = getRandomString();
			
			//Now, we need to construct a "source.tar" archive file
			//We will construct the content of the tar file in folders "META-INF" and "org"
			//Then we copy this folder into a tar file by RSE API.
			IFileStore META_INF = temp.getChild(tarSourceFolderName1);
			createDir(META_INF, true);
			//Now, populate the contents in the folder.
			IFileStore MANIFEST_MF = META_INF.getChild("MANIFEST.MF");
			content = getRandomString();
			createFile(MANIFEST_MF, content);
			//create folder "org"
			IFileStore org = temp.getChild(tarSourceFolderName2);
			createDir(org, true);
			IFileStore eclipse = org.getChild("eclipse");
			createDir(eclipse, true);
			//create "dstore" folder inside "org"
			IFileStore dstore = eclipse.getChild("dstore");
			createDir(dstore, true);
			//Now, create a few folders inside the dstore folder.
			IFileStore core = dstore.getChild("core");
			createDir(core, true);
			IFileStore internal = dstore.getChild("internal");
			createDir(internal, true);
			
			//now create directory inside "core":
			IFileStore client = core.getChild("client");
			createDir(client, true);
			IFileStore java = core.getChild("java");
			createDir(java, true);
			IFileStore miners = core.getChild("miners");
			createDir(miners, true);
			IFileStore model = core.getChild("model");
			createDir(model, true);
			IFileStore server = core.getChild("server");
			createDir(server, true);
			IFileStore util = core.getChild("util");
			createDir(util, true);
			IFileStore Activator_java = core.getChild("Activator.java");
			content = getRandomString();
			createFile(Activator_java, content);
			
			//now, some contents on client folder
			IFileStore ClientConnection_java = client.getChild("ClientConnection.java");
			content = getRandomString();
			createFile(ClientConnection_java, content);
			IFileStore ConnectionStatus_java = client.getChild("ConnectionStatus.java");
			content = getRandomString();
			createFile(ConnectionStatus_java, content);
			
			//now, some contents in java folder
			IFileStore ClassByteStreamHandler$ReceiveClassInstanceThread_java = java.getChild("ClassByteStreamHandler$ReceiveClassInstanceThread.java");
			content = getRandomString();
			createFile(ClassByteStreamHandler$ReceiveClassInstanceThread_java, content);
			
			//now, some contents in miners folder
			IFileStore Miner_java = miners.getChild("Miner.java");
			content = getRandomString();
			createFile(Miner_java, content);
			IFileStore MinerThread_java = miners.getChild("MinerThread.java");
			content = getRandomString();
			createFile(MinerThread_java, content);
			
			//now, some contents in model folder
			IFileStore ByteStreamHandler_java = model.getChild("ByteStreamHandler.java");
			content = getRandomString();
			createFile(ByteStreamHandler_java, content);
			IFileStore DE_java = model.getChild("DE.java");
			content = getRandomString();
			createFile(DE_java, content);
			IFileStore Handler_java = model.getChild("Handler.java");
			content = getRandomString();
			createFile(Handler_java, content);
			
			//now, some contents in server folder
			IFileStore Server_java = server.getChild("Server.java");
			content = getRandomString();
			createFile(Server_java, content);
			
			//now, some contents in util folder
			IFileStore StringCompare_java = util.getChild("StringCompare.java");
			content = getRandomString();
			createFile(StringCompare_java, content);
			
			//now, create the contents in "internal" folder
			IFileStore core1 = internal.getChild("core");
			createDir(core1, true);
			
			//then create some folder in this "core" folder
			IFileStore client1 = core1.getChild("client");
			createDir(client1, true);
			IFileStore model1 = core1.getChild("model");
			createDir(model1, true);
			IFileStore server1 = core1.getChild("server");
			createDir(server1, true);
			IFileStore util1 = core1.getChild("util");
			createDir(util1, true);
			
			//now, some contents on client folder
			IFileStore ClientConnection_java1 = client1.getChild("ClientConnection.java");
			content = getRandomString();
			createFile(ClientConnection_java1, content);
			IFileStore ConnectionStatus_java1 = client1.getChild("ConnectionStatus.java");
			content = getRandomString();
			createFile(ConnectionStatus_java1, content);
			
			
			//now, some contents in model folder
			IFileStore ByteStreamHandler_java1 = model1.getChild("ByteStreamHandler.java");
			content = getRandomString();
			createFile(ByteStreamHandler_java1, content);
			IFileStore DE_java1 = model1.getChild("DE.java");
			content = getRandomString();
			createFile(DE_java1, content);
			IFileStore Handler_java1 = model1.getChild("Handler.java");
			content = getRandomString();
			createFile(Handler_java1, content);
			
			//now, some contents in server folder
			IFileStore Server_java1 = server1.getChild("Server.java");
			content = getRandomString();
			createFile(Server_java1, content);
			
			//now, some contents in util folder
			IFileStore StringCompare_java1 = util1.getChild("StringCompare.java");
			content = getRandomString();
			createFile(StringCompare_java1, content);
			
			//now, copy META_INF into the folder in the remote system
			IRemoteFile META_INF_folder = localFss.getRemoteFileObject(tempPath + '\\' + tarSourceFolderName1, mon);
			assertTrue(META_INF_folder != null);
			ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) META_INF_folder).getAdapter(ISystemDragDropAdapter.class);
			SystemRemoteResourceSet fromSet = new SystemRemoteResourceSet(localFss, srcAdapter1);
			fromSet.addResource(META_INF_folder);
			ISystemResourceSet tempObjects1 = srcAdapter1.doDrag(fromSet, mon);
			UniversalFileTransferUtility.copyWorkspaceResourcesToRemote((SystemWorkspaceResourceSet)tempObjects1, tempDir, mon, true);
			
			//now, copy org into the folder in the remote system
			IRemoteFile org_folder = localFss.getRemoteFileObject(tempPath + '\\' + tarSourceFolderName2, mon);
			assertTrue(org_folder != null);
			ISystemDragDropAdapter srcAdapter2 = (ISystemDragDropAdapter) ((IAdaptable) org_folder).getAdapter(ISystemDragDropAdapter.class);
			SystemRemoteResourceSet fromSet2 = new SystemRemoteResourceSet(localFss, srcAdapter2);
			fromSet2.addResource(org_folder);
			ISystemResourceSet tempObjects2 = srcAdapter2.doDrag(fromSet2, mon);
			UniversalFileTransferUtility.copyWorkspaceResourcesToRemote((SystemWorkspaceResourceSet)tempObjects2, tempDir, mon, true);
			
			//now, create tar file in the host
			IRemoteFile tarSource = createFileOrFolder(tempDir.getAbsolutePath(), tarSourceFileName1, false);
			assertTrue(tarSource != null);
			IRemoteFile tarSourceFolder1 = (IRemoteFile)getChildFromFolder(tempDir, tarSourceFolderName1); 
			assertTrue(tarSourceFolder1 != null);
			IRemoteFile tarSourceFolder2 = (IRemoteFile)getChildFromFolder(tempDir, tarSourceFolderName2);
			fss.copy(tarSourceFolder1, tarSource, tarSourceFolderName1, mon);
			fss.copy(tarSourceFolder2, tarSource, tarSourceFolderName2, mon);
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
	
	public void testCopyToTarArchiveFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		createSourceFolders();
		
		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertTrue(targetTarFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Now, copy one of the folder from the sourceFolder into copiedTargetZipFile
		try
		{
			fss.copy(sourceFolder, targetTarFile, sourceFolder.getName(), mon);
			
			Object theCopiedChild = getChildFromFolder(targetTarFile, sourceFolderName);
			
			assertTrue(theCopiedChild != null);
			
			//Also make sure the copied child has the right contents.
			String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
			
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}

	public void testCopyToTarVirtualFileLevelOne() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		createSourceFolders();
		
		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertTrue(targetTarFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		
		try
		{
			//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
			//Get one of its first level children, and copy the folder to there.
			IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetTarFile, "org");
			assertTrue(firstLevelChild != null);
			
			fss.copy(sourceFolder, firstLevelChild, sourceFolder.getName(), mon);
			
			Object theCopiedChild = getChildFromFolder(firstLevelChild, sourceFolderName);
			
			assertTrue(theCopiedChild != null);
			
			//Also make sure the copied child has the right contents.
			String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}

	public void testCopyToTarVirtualFileLevelFour() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		createSourceFolders();
		
		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertTrue(targetTarFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		
		try
		{
			//Get one of its fourth level children, and copy the folder to there.
			IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetTarFile, "org");
			assertTrue(firstLevelChild != null);
			IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
			assertTrue(secondLevelChild != null);
			IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
			assertTrue(thirdLevelChild != null);
			IRemoteFile fourLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
			assertTrue(fourLevelChild != null);
			
			//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.copy(sourceFolder, fourLevelChild, sourceFolder.getName(), mon);
			
			Object theCopiedChild = getChildFromFolder(fourLevelChild, sourceFolderName);
			assertTrue(theCopiedChild != null);
			
			//Also make sure the copied child has the right contents.
			String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}

	public void testCopyTarVirtualFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		
		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertTrue(sourceTarFile != null);
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		//Now, copy one of the folders from the tar file into folder1
		try
		{
			//Get one of its fourth level children, and copy the folder to there.
			IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceTarFile, tarSourceFolderName1);
			assertTrue(firstLevelChild != null);
			
			fss.copy(firstLevelChild, folder1, tarSourceFolderName1, mon);
			
			Object copiedVirtualFolder = getChildFromFolder(folder1, tarSourceFolderName1);
			assertTrue(copiedVirtualFolder != null);
			
			String[] contents = {"MANIFEST.MF"};
			int[] typesToCheck = {TYPE_FILE};
			checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}

	public void testCopyTarVirtualFileLevelFour() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		
		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertTrue(sourceTarFile != null);
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		try
		{
			//Now, copy one of the level four folder from the zip file into folder1
			//The folder is org/eclipse/dstore/core
			//then, get directory "java" under org/eclipse/dstore/core
			String parentForDirectoryToCopyName ="org";
			IRemoteFile parentForDirectoryToCopy = (IRemoteFile)getChildFromFolder(sourceTarFile, parentForDirectoryToCopyName);
			assertTrue(parentForDirectoryToCopy != null);
			
			parentForDirectoryToCopyName ="eclipse";
			parentForDirectoryToCopy = (IRemoteFile)getChildFromFolder(parentForDirectoryToCopy, parentForDirectoryToCopyName);
			assertTrue(parentForDirectoryToCopy != null);
			
			parentForDirectoryToCopyName ="dstore";
			parentForDirectoryToCopy = (IRemoteFile)getChildFromFolder(parentForDirectoryToCopy, parentForDirectoryToCopyName);
			assertTrue(parentForDirectoryToCopy != null);
			
			String directoryToCopyName ="core";
			IRemoteFile directoryToCopy = (IRemoteFile)getChildFromFolder(parentForDirectoryToCopy, directoryToCopyName);
			assertTrue(directoryToCopy != null);
			
			
			//copy this level four children into folder1
			fss.copy(directoryToCopy, folder1, directoryToCopyName, mon);
			
			Object copiedVirtualFolder = getChildFromFolder(folder1, directoryToCopyName);
			
			assertTrue(copiedVirtualFolder != null);
			
			String[] contents = {"client", "java", "miners", "model", "server", "util", "Activator.java"};
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
			checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
		
	}

	public void testMoveToTarArchiveFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		createSourceFolders();
		
		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertTrue(targetTarFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		
		//Now, move sourceFolder into targetTarFile
		try
		{
			fss.move(sourceFolder, targetTarFile, sourceFolder.getName(), mon);
			
			Object theMovedChild = getChildFromFolder(targetTarFile, sourceFolderName);
			
			assertTrue(theMovedChild != null);
			
			//Also make sure the copied child has the right contents.
			String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
			
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);
			
			//make sure the original folder is gone.
			Object originalSource = getChildFromFolder(tempDir, sourceFolderName);
			assertFalse(originalSource != null);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}

	public void testMoveToTarVirtualFileLevelOne() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		createSourceFolders();
		
		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertTrue(targetTarFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		
		try
		{
			//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
			//Get one of its first level children, and copy the folder to there.
			Object[] childrenOfTargetZipFile = fss.resolveFilterString(targetTarFile, null, mon);
			
			//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.move(sourceFolder, ((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolderName, mon);
			
			Object theMovedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolderName);
			
			assertTrue(theMovedChild != null);
			
			//Also make sure the copied child has the right contents.
			String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);
			
			//make sure the original folder is gone.
			Object originalSource = getChildFromFolder(tempDir, sourceFolderName);
			assertFalse(originalSource != null);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}

	public void testMoveToVirtualFileLevelFour() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		createSourceFolders();
		
		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertTrue(targetTarFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		
		try
		{
			//Get one of its fourth level children, and copy the folder to there.
			IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetTarFile, "org");
			assertTrue(firstLevelChild != null);
			IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
			assertTrue(secondLevelChild != null);
			IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
			assertTrue(thirdLevelChild != null);
			IRemoteFile fourLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
			assertTrue(fourLevelChild != null);
			
			//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.move(sourceFolder, fourLevelChild, sourceFolder.getName(), mon);
			
			Object theCopiedChild = getChildFromFolder(fourLevelChild, sourceFolderName);
			
			assertTrue(theCopiedChild != null);
			
			//Also make sure the moved child has the right contents.
			String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
			
			//make sure the original folder is gone.
			Object originalSource = getChildFromFolder(tempDir, sourceFolderName);
			assertFalse(originalSource != null);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}

	public void testMoveTarVirtualFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		
		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertTrue(sourceTarFile != null);
		
		//then, create a folder inside the tempDir
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		//Now, copy one of the folder from the zip file into folder1
		try
		{
			String movedFolderName = tarSourceFolderName1;
			IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceTarFile, tarSourceFolderName1);
			assertTrue(firstLevelChild != null);
			fss.move(firstLevelChild, folder1, movedFolderName, mon);
			
			Object movedVirtualFolder = getChildFromFolder(folder1, movedFolderName);
			
			assertTrue(movedVirtualFolder != null);
			
			String[] contents = {"MANIFEST.MF"};
			int[] typesToCheck = {TYPE_FILE};
			checkFolderContents((IRemoteFile)movedVirtualFolder, contents, typesToCheck);
			
			//Now, make sure the moved virtual folder is gone from its original zip file
			IRemoteFile tmp = (IRemoteFile)getChildFromFolder(sourceTarFile, tarSourceFolderName1);
			assertTrue(tmp == null);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
		
	}

	public void testMoveTarVirtualFileLevelFour() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		
		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertTrue(sourceTarFile != null);
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		try
		{
			//Get one of its fourth level children, and move it to the folder
			IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceTarFile, "org");
			assertTrue(firstLevelChild != null);
			IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
			assertTrue(secondLevelChild != null);
			IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
			assertTrue(thirdLevelChild != null);
			IRemoteFile fourthLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
			assertTrue(fourthLevelChild != null);
			String movedFolderName = fourthLevelChild.getName();
			
			//copy this level four children into folder1
			fss.move(fourthLevelChild, folder1, movedFolderName, mon);
			
			Object copiedVirtualFolder = getChildFromFolder(folder1, movedFolderName);
			
			assertTrue(copiedVirtualFolder != null);
			
			String[] contents = {"client", "java", "miners", "model", "server", "util", "Activator.java"};
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
			checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
			
			//Now, make sure the moved virtual folder is gone from its original zip file
			//children = fss.resolveFilterString(sourceTarFile, null, mon);
			Object result = getChildFromFolder(thirdLevelChild, movedFolderName);
			assertTrue(result == null);  //we should not be able to find it.
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
		
	}

	public void testRenameTarVirtualFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//Create the zip file first.
		String testName = "source.tar";
		IRemoteFile newArchiveFile = createFileOrFolder(tempDirPath, testName, false);
		
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
		String[] levelOneNamesToCheck = {"aaa.txt", "bbb.txt", "folder1"};
		int[] levalOneTypesToCheck = {TYPE_FILE, TYPE_FILE, TYPE_FOLDER};
		checkFolderContents(newArchiveFile, levelOneNamesToCheck, levalOneTypesToCheck);
		
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
		String[] levelTwoNamesToCheck = {"ccc.exe", "ddd.bat", "another Folder"};
		int[] levalTwoTypesToCheck = {TYPE_FILE, TYPE_FILE, TYPE_FOLDER};
		checkFolderContents(folder1, levelTwoNamesToCheck, levalTwoTypesToCheck);
		
		//Now rename one of the text file in the first level:
		IRemoteFile childToRename = (IRemoteFile)getChildFromFolder(newArchiveFile, "aaa.txt");
		fss.rename(childToRename, "aaa1.txt", mon);
		//Now rename one of the folder in the first level
		childToRename = (IRemoteFile)getChildFromFolder(newArchiveFile, "folder1");
		fss.rename(childToRename, "folder2", mon);
		
		//Check the result of rename
		String[] newLevelOneNamesToCheck = {"aaa1.txt", "bbb.txt", "folder2"};
		checkFolderContents(newArchiveFile, newLevelOneNamesToCheck, levalOneTypesToCheck);
		
		//Now rename one of the text file in the second level:
		IRemoteFile thisFolder = (IRemoteFile)getChildFromFolder(newArchiveFile, "folder2");
		childToRename = (IRemoteFile)getChildFromFolder(thisFolder, "ddd.bat");
		fss.rename(childToRename, "ddd1.bat", mon);
		//Now rename one of the folder in the second level
		childToRename = (IRemoteFile)getChildFromFolder(thisFolder, "another Folder");
		fss.rename(childToRename, "some folder$", mon);
		
		//Check the result of rename
		String[] newLevelTwoNamesToCheck = {"ccc.exe", "ddd1.bat", "some folder$"};
		checkFolderContents(thisFolder, newLevelTwoNamesToCheck, levalTwoTypesToCheck);
		
		return;
		
	}

	public void testDeleteTarVirtualFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//create the source for testing first
		createSourceTarFiles();
		
		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertTrue(sourceTarFile != null);
		
		//delete a file from level 2
		String parentForFileToDeleteName ="META-INF";
		IRemoteFile parentForFileToDelete = (IRemoteFile)getChildFromFolder(sourceTarFile, parentForFileToDeleteName);
		assertTrue(parentForFileToDelete != null);
		String deleteFileName = "MANIFEST.MF";
		IRemoteFile fileToToDelete = (IRemoteFile)getChildFromFolder(parentForFileToDelete, deleteFileName);
		assertTrue(fileToToDelete != null);
		//Now, delete this file
		fss.delete(fileToToDelete, mon);
		fileToToDelete = (IRemoteFile)getChildFromFolder(parentForFileToDelete, deleteFileName);
		assertTrue(fileToToDelete == null);
		
		//then, get directory "java" under org/eclipse/dstore/core
		String parentForDirectoryToDeleteName ="org";
		IRemoteFile parentForDirectoryToDelete = (IRemoteFile)getChildFromFolder(sourceTarFile, parentForDirectoryToDeleteName);
		assertTrue(parentForDirectoryToDelete != null);
		
		parentForDirectoryToDeleteName ="eclipse";
		parentForDirectoryToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, parentForDirectoryToDeleteName);
		assertTrue(parentForDirectoryToDelete != null);
		
		parentForDirectoryToDeleteName ="dstore";
		parentForDirectoryToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, parentForDirectoryToDeleteName);
		assertTrue(parentForDirectoryToDelete != null);
		
		parentForDirectoryToDeleteName ="core";
		parentForDirectoryToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, parentForDirectoryToDeleteName);
		assertTrue(parentForDirectoryToDelete != null);
		
		String directoryToDeleteName = "java";
		IRemoteFile directoryToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, directoryToDeleteName);
		//Now, delete this directory
		fss.delete(directoryToDelete, mon);
		directoryToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, directoryToDeleteName);
		
		//check result of this operation
		String[] contents = {"client", "miners", "model", "server", "util", "Activator.java"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents(parentForDirectoryToDelete, contents, typesToCheck);
		
		//And check this directory is not there any more.
		directoryToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, directoryToDeleteName);
		assertTrue(directoryToDelete == null);
		
		//Now, delete some files and folder inside the a virtual folder.
		parentForFileToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, "model");
		deleteFileName = "DE.java";
		fileToToDelete = (IRemoteFile)getChildFromFolder(parentForFileToDelete, deleteFileName);
		assertTrue(fileToToDelete != null);
		
		fss.delete(fileToToDelete, mon);
		
		//check the result
		fileToToDelete = (IRemoteFile)getChildFromFolder(parentForFileToDelete, deleteFileName);
		
		assertTrue(fileToToDelete == null);
		
		return;
		
	}



	public void testCopyBatchToTarArchiveFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceTarFiles();
		createSourceFolders();
		
		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertTrue(targetTarFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir,sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Now, copy one of the folder from the sourceFolder into copiedTargetZipFile
		try
		{
			IRemoteFile[] sourceFiles = new IRemoteFile[3];
			//Also add some of its children into the batch.
			String childToCopyName1 = "aaaaaaaa";
			sourceFiles[0] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName1);
			String childToCopyName2 = "aaaab";
			sourceFiles[1] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName2);
			String childToCopyName3 = "epdcdump01.hex12a";
			sourceFiles[2] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName3);
			fss.copyBatch(sourceFiles, targetTarFile, mon);
			
			//Checking the first copied folder
			Object theCopiedChild = getChildFromFolder(targetTarFile, childToCopyName1);
			
			assertTrue(theCopiedChild != null);
			
			//Also make sure the copied child has the right contents.
			String[] childrenToCheck1 = {"adsf", "eclipse-SDK-3.3M6-win32.zip", "epdcdump01.hex12", "epdcdump01.hex12aaaa"};
			
			int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);
			
			//Checking the second copied folder
			theCopiedChild = getChildFromFolder(targetTarFile, childToCopyName2);
			
			assertTrue(theCopiedChild != null);
			
			//Also make sure the copied child has the right contents.
			String[] childrenToCheck2 = {"features"};
			
			int[] typesToCheck2 = {TYPE_FOLDER};
			checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);
			
			//Checking the third copied file
			theCopiedChild = getChildFromFolder(targetTarFile, childToCopyName3);
			assertTrue(theCopiedChild != null);
			assertTrue(((IRemoteFile)theCopiedChild).isDirectory() != true);
			
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}



	public void testCopyBatchToTarVirtualFileLevelFour() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceTarFiles();
		createSourceFolders();
		
		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertTrue(targetTarFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		try
		{
			//Get one of its fourth level children, and copy the folder to there.
			IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetTarFile, "org");
			assertTrue(firstLevelChild != null);
			IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
			assertTrue(secondLevelChild != null);
			IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
			assertTrue(thirdLevelChild != null);
			IRemoteFile fourthLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
			assertTrue(fourthLevelChild != null);
			
			IRemoteFile[] sourceFiles = new IRemoteFile[3];
			//Also add some of its children into the batch.
			String childToCopyName1 = "aaaaaaaa";
			sourceFiles[0] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName1);
			String childToCopyName2 = "aaaab";
			sourceFiles[1] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName2);
			String childToCopyName3 = "epdcdump01.hex12a";
			sourceFiles[2] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName3);
			fss.copyBatch(sourceFiles, fourthLevelChild, mon);
			
			
			//Checking the first copied folder
			Object theCopiedChild = getChildFromFolder(fourthLevelChild, childToCopyName1);
			
			assertTrue(theCopiedChild != null);
			
			//Also make sure the copied child has the right contents.
			String[] childrenToCheck1 = {"adsf", "eclipse-SDK-3.3M6-win32.zip", "epdcdump01.hex12", "epdcdump01.hex12aaaa"};
			
			int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);
			
			//Checking the second copied folder
			theCopiedChild = getChildFromFolder(fourthLevelChild, childToCopyName2);
			
			assertTrue(theCopiedChild != null);
			
			//Also make sure the copied child has the right contents.
			String[] childrenToCheck2 = {"features"};
			
			int[] typesToCheck2 = {TYPE_FOLDER};
			checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);
			
			//Checking the third copied file
			theCopiedChild = getChildFromFolder(fourthLevelChild, childToCopyName3);
			assertTrue(theCopiedChild != null);
			assertTrue(((IRemoteFile)theCopiedChild).isDirectory() != true);
			
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}



	public void testCopyBatchTarVirtualFileLevelFive() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceTarFiles();
		
		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertTrue(sourceTarFile != null);
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		try
		{
			//Get several of its fifth level children, and them into the folder.
			IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceTarFile, "org");
			assertTrue(firstLevelChild != null);
			IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
			assertTrue(secondLevelChild != null);
			IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
			assertTrue(thirdLevelChild != null);
			IRemoteFile fourthLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
			assertTrue(fourthLevelChild != null);
			
			IRemoteFile[] fifLevelChildrenToCopy = new IRemoteFile[3];
			
			String firstToCopyName = "client"; 
			fifLevelChildrenToCopy[0] = (IRemoteFile)getChildFromFolder(fourthLevelChild, firstToCopyName);
			assertTrue(fifLevelChildrenToCopy[0] != null);
			String secondToCopyName = "miners"; 
			fifLevelChildrenToCopy[1] = (IRemoteFile)getChildFromFolder(fourthLevelChild, secondToCopyName);
			assertTrue(fifLevelChildrenToCopy[1] != null);
			String thirdToCopyName = "Activator.java"; 
			fifLevelChildrenToCopy[2] = (IRemoteFile)getChildFromFolder(fourthLevelChild, thirdToCopyName);
			assertTrue(fifLevelChildrenToCopy[2] != null);
			
			
			fss.copyBatch(fifLevelChildrenToCopy, folder1, mon);
			
			Object copiedVirtualFolder1 = getChildFromFolder(folder1, firstToCopyName);
			assertTrue(copiedVirtualFolder1 != null);
			String[] contents1 = {"ClientConnection.java", "ConnectionStatus.java"};
			int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)copiedVirtualFolder1, contents1, typesToCheck1);
			
			Object copiedVirtualFolder2 = getChildFromFolder(folder1, secondToCopyName);
			assertTrue(copiedVirtualFolder2 != null);
			String[] contents2 = {"Miner.java", "MinerThread.java"};
			int[] typesToCheck2 = {TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)copiedVirtualFolder2, contents2, typesToCheck2);
			
			Object copiedVirtualFolder3 = getChildFromFolder(folder1, thirdToCopyName);
			assertTrue(copiedVirtualFolder3 != null);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
		
	}

}
