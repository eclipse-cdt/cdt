/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Johnson Ma (Wind River) - [195402] Extracted from FileServiceArchiveTest
 * Xuan Chen (IBM) [333874] - Added more logging code to track junit failure
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

public abstract class FileServiceArchiveBaseTest extends FileServiceBaseTest {

	protected String folderToCopyName1 = "RemoteSystemsConnections";
	protected String folderToCopyName2 = "6YLT5Xa";
	protected String folderToCopyName3 = "folderToCopy";

	protected String tarSourceFileName1;
	protected String tarSourceFileName2;

	protected String tarSourceFolderName1 = "META-INF";
	protected String tarSourceFolderName2 = "org";

	protected String tarSourceForOpenTest;
	protected String tarSourceForOpenFolderName1 = "META-INF";
	protected String tarSourceForOpenFolderName2 = "org";

	protected String testName;

	protected String fileContentString1 = "this is just some dummy content \n to a remote file \n to test an open operation";

	/**
	 * Constructor with specific test name.
	 * @param name test to execute
	 */
	public FileServiceArchiveBaseTest(String name) {
		super(name);
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public void createSourceFolders() throws Exception
	{
		String tempPath = getWorkspace().getRoot().getLocation().append("temp").toString();
		IFileStore temp = createDir(tempPath, true);
		String content = getRandomString();

		// create the source folder used for copy or move
		IFileStore folderToCopy = temp.getChild(folderToCopyName3);
		createDir(folderToCopy, true);
		//Now, populate the contents in the folderToCopy.
		IFileStore aaaaaaaa = folderToCopy.getChild("aaaaaaaa");
		createDir(aaaaaaaa, true);
		//create file inside the aaaaaaaa folder.
		IFileStore adsf = aaaaaaaa.getChild("adsf");
		content = getRandomString();
		createFile(adsf, content);
		IFileStore eclipse_SDK_3_3M6_win32_zip = aaaaaaaa.getChild("eclipse-SDK-3.3M6-win32.zip");
		createFile(eclipse_SDK_3_3M6_win32_zip, "");
		IFileStore epdcdump01_hex12 = aaaaaaaa.getChild("epdcdump01.hex12");
		content = getRandomString();
		createFile(epdcdump01_hex12, content);
		IFileStore epdcdump01_hex12aaaa = aaaaaaaa.getChild("epdcdump01.hex12aaaa");
		content = getRandomString();
		createFile(epdcdump01_hex12aaaa, content);

		IFileStore aaaab = folderToCopy.getChild("aaaab");
		createDir(aaaab, true);
		IFileStore features = aaaab.getChild("features");
		createDir(features, true);
		IFileStore dummyFile = features.getChild("dummy.txt");
		content = getRandomString();
		createFile(dummyFile, content);
		//create file inside the aaaab folder.
		content = "this is just a simple content \n to a simple file \n to test a 'simple' copy";
		IFileStore epdcdump01_hex12a = aaaab.getChild("epdcdump01.hex12a");
		content = getRandomString();
		createFile(epdcdump01_hex12a, content);

		IFileStore epdcdump01_hex12a1 = folderToCopy.getChild("epdcdump01.hex12a");
		content = getRandomString();
		createFile(epdcdump01_hex12a1, content);

		IFileStore RSE_SDK_2_0RC1_zip = folderToCopy.getChild("RSE-SDK-2.0RC1.zip");
		content = getRandomString();
		createFile(RSE_SDK_2_0RC1_zip, content);

		//now, copy folderToCopy into the folder in the remote system
		IRemoteFile sourceFolderToCopy3 = localFss.getRemoteFileObject(tempPath + '\\' + folderToCopyName3, mon);
		ISystemDragDropAdapter srcAdapter3 = (ISystemDragDropAdapter) ((IAdaptable) sourceFolderToCopy3).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet3 = new SystemRemoteResourceSet(localFss, srcAdapter3);
		fromSet3.addResource(sourceFolderToCopy3);
		ISystemResourceSet tempObjects3 = srcAdapter3.doDrag(fromSet3, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, tempDir, mon, true);

		//Then, we need to retrieve children of the tempDir to cache their information.
		fss.resolveFilterString(tempDir, null, mon);

		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);
	}


	protected void createSuperTransferFolder(IFileStore temp) throws Exception
	{

		String content = getRandomString();

		// create the source folder used for copy or move
		IFileStore folderToCopy = temp.getChild(folderToCopyName3);
		createDir(folderToCopy, true);
		//Now, populate the contents in the folderToCopy.
		IFileStore aaaaaaaa = folderToCopy.getChild("aaaaaaaa");
		createDir(aaaaaaaa, true);
		//create file inside the aaaaaaaa folder.
		IFileStore adsf = aaaaaaaa.getChild("adsf");
		content = getRandomString();
		createFile(adsf, content);
		IFileStore eclipse_SDK_3_3M6_win32_zip = aaaaaaaa.getChild("eclipse-SDK-3.3M6-win32.zip");
		createFile(eclipse_SDK_3_3M6_win32_zip, "");
		IFileStore epdcdump01_hex12 = aaaaaaaa.getChild("epdcdump01.hex12");
		content = getRandomString();
		createFile(epdcdump01_hex12, content);
		IFileStore epdcdump01_hex12aaaa = aaaaaaaa.getChild("epdcdump01.hex12aaaa");
		content = getRandomString();
		createFile(epdcdump01_hex12aaaa, content);

		IFileStore aaaab = folderToCopy.getChild("aaaab");
		createDir(aaaab, true);
		IFileStore features = aaaab.getChild("features");
		createDir(features, true);
		IFileStore dummyFile = features.getChild("dummy.txt");
		content = getRandomString();
		createFile(dummyFile, content);
		//create file inside the aaaab folder.
		content = "this is just a simple content \n to a simple file \n to test a 'simple' copy";
		IFileStore epdcdump01_hex12a = aaaab.getChild("epdcdump01.hex12a");
		content = getRandomString();
		createFile(epdcdump01_hex12a, content);

		IFileStore epdcdump01_hex12a1 = folderToCopy.getChild("epdcdump01.hex12a");
		content = getRandomString();
		createFile(epdcdump01_hex12a1, content);

		IFileStore RSE_SDK_2_0RC1_zip = folderToCopy.getChild("RSE-SDK-2.0RC1.zip");
		content = getRandomString();
		createFile(RSE_SDK_2_0RC1_zip, content);

		return;

	}

	public void createTarSourceForOpen() throws Exception
	{
		String tempPath = getWorkspace().getRoot().getLocation().append("temp").toString();
		IFileStore temp = createDir(tempPath, true);
		String content = getRandomString();

		//Now, we need to construct a "source.tar" archive file
		//We will construct the content of the tar file in folders "META-INF" and "org"
		//Then we copy this folder into a tar file by RSE API.
		IFileStore META_INF = temp.getChild(tarSourceForOpenFolderName1);
		createDir(META_INF, true);
		//Now, populate the contents in the folder.
		IFileStore MANIFEST_MF = META_INF.getChild("MANIFEST.MF");
		content = fileContentString1;
		createFile(MANIFEST_MF, content);

		//create folder "org"
		IFileStore org = temp.getChild("org");
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
		content = fileContentString1;
		createFile(Activator_java, content);

		//now, some contents on client folder
		IFileStore ClientConnection_java = client.getChild("ClientConnection.java");
		content = getRandomString();
		createFile(ClientConnection_java, content);
		IFileStore ConnectionStatus_java = client.getChild("ConnectionStatus.java");
		content = fileContentString1;
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
		content = fileContentString1;
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
		IRemoteFile META_INF_folder = localFss.getRemoteFileObject(tempPath + '\\' + tarSourceForOpenFolderName1, mon);
		assertNotNull(META_INF_folder);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) META_INF_folder).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet.addResource(META_INF_folder);
		ISystemResourceSet tempObjects1 = srcAdapter1.doDrag(fromSet, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects1, tempDir, mon, true);

		//now, copy org into the folder in the remote system
		IRemoteFile org_folder = localFss.getRemoteFileObject(tempPath + '\\' + tarSourceForOpenFolderName2, mon);
		assertNotNull(org_folder);
		ISystemDragDropAdapter srcAdapter2 = (ISystemDragDropAdapter) ((IAdaptable) org_folder).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet2 = new SystemRemoteResourceSet(localFss, srcAdapter2);
		fromSet2.addResource(org_folder);
		ISystemResourceSet tempObjects2 = srcAdapter2.doDrag(fromSet2, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects2, tempDir, mon, true);

		//now, create tar file in the host
		IRemoteFile tarSource = createFileOrFolder(tempDir.getAbsolutePath(), tarSourceForOpenTest, false);
		assertNotNull(tarSource);
		IRemoteFile tarSourceFolder1 = (IRemoteFile)getChildFromFolder(tempDir, tarSourceForOpenFolderName1);
		assertNotNull(tarSourceFolder1);
		IRemoteFile tarSourceFolder2 = (IRemoteFile)getChildFromFolder(tempDir, tarSourceForOpenFolderName2);
		fss.copy(tarSourceFolder1, tarSource, tarSourceForOpenFolderName1, mon);
		fss.copy(tarSourceFolder2, tarSource, tarSourceForOpenFolderName2, mon);

		//Then, we need to retrieve children of the tempDir to cache their information.
		fss.resolveFilterString(tempDir, null, mon);

		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);
	}



	public void createSourceTarFiles() throws Exception
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
		assertNotNull(META_INF_folder);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) META_INF_folder).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet.addResource(META_INF_folder);
		ISystemResourceSet tempObjects1 = srcAdapter1.doDrag(fromSet, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects1, tempDir, mon, true);

		//now, copy org into the folder in the remote system
		IRemoteFile org_folder = localFss.getRemoteFileObject(tempPath + '\\' + tarSourceFolderName2, mon);
		assertNotNull(org_folder);
		ISystemDragDropAdapter srcAdapter2 = (ISystemDragDropAdapter) ((IAdaptable) org_folder).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet2 = new SystemRemoteResourceSet(localFss, srcAdapter2);
		fromSet2.addResource(org_folder);
		ISystemResourceSet tempObjects2 = srcAdapter2.doDrag(fromSet2, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects2, tempDir, mon, true);

		//now, create tar file in the host
		IRemoteFile tarSource = createFileOrFolder(tempDir.getAbsolutePath(), tarSourceFileName1, false);
		assertNotNull(tarSource);
		IRemoteFile tarSourceFolder1 = (IRemoteFile)getChildFromFolder(tempDir, tarSourceFolderName1);
		assertNotNull(tarSourceFolder1);
		IRemoteFile tarSourceFolder2 = (IRemoteFile)getChildFromFolder(tempDir, tarSourceFolderName2);
		fss.copy(tarSourceFolder1, tarSource, tarSourceFolderName1, mon);
		fss.copy(tarSourceFolder2, tarSource, tarSourceFolderName2, mon);
	}

	public void testCreateTarFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip file first.
		IRemoteFile newArchiveFile = createFileOrFolder(tempDirPath, testName, false);
		assertNotNull(newArchiveFile);
		assertTrue(newArchiveFile.exists());
		assertTrue(newArchiveFile.canRead());
		assertTrue(newArchiveFile.canWrite());
		assertEquals(newArchiveFile.getName(), testName);
		assertEquals(newArchiveFile.getParentPath(), tempDirPath);

		//fss.resolveFilterString(filterString, monitor)

		//Now, we want to create a text file inside.
		String childName = "aaa.txt";
		IRemoteFile file1 = createFileOrFolder(newArchiveFile.getAbsolutePath(), childName, false);
		assertNotNull(file1);

		childName = "bbb.txt";
		IRemoteFile file2 = createFileOrFolder(newArchiveFile.getAbsolutePath(), childName, false);
		assertNotNull(file2);

		//Create a folder
		childName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(newArchiveFile.getAbsolutePath(), childName, true);
		assertNotNull(folder1);

		//Now, check the contents
		String[] namesToCheck = {"aaa.txt", "bbb.txt", "folder1"};
		int[] typesToCheck = {TYPE_FILE, TYPE_FILE, TYPE_FOLDER};
		checkFolderContents(newArchiveFile, namesToCheck, typesToCheck);

		//Now, create some files inside the folder.
		String secondLevelChildName = "ccc.exe";
		IRemoteFile levelTwoChild1 = createFileOrFolder(folder1.getAbsolutePath(), secondLevelChildName, false);
		assertNotNull(levelTwoChild1);

		secondLevelChildName = "ddd.bat";
		IRemoteFile levelTwoChild2 = createFileOrFolder(folder1.getAbsolutePath(), secondLevelChildName, false);
		assertNotNull(levelTwoChild2);

		secondLevelChildName = "another Folder"; //folder with space
		IRemoteFile levelTwoChild3 = createFileOrFolder(folder1.getAbsolutePath(), secondLevelChildName, true);
		assertNotNull(levelTwoChild3);

		//Now, check the contents
		String[] namesToCheck1 = {"ccc.exe", "ddd.bat", "another Folder"};
		int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FOLDER};
		checkFolderContents(folder1, namesToCheck1, typesToCheck1);
	}

	public void testCopyToTarArchiveFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//create the source for testing first
		createSourceTarFiles();
		createSourceFolders();

		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertNotNull(targetTarFile);

		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertNotNull(sourceFolder);

		//Now, copy one of the folder from the sourceFolder into copiedTargetZipFile
		fss.copy(sourceFolder, targetTarFile, sourceFolder.getName(), mon);

		Object theCopiedChild = getChildFromFolder(targetTarFile, sourceFolderName);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};

		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}



	public void testCopyTarVirtualFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//create the source for testing first
		createSourceTarFiles();

		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertNotNull(sourceTarFile);

		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertNotNull(folder1);

		//Get one of its fourth level children, and copy the folder to there.
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceTarFile, tarSourceFolderName1);
		assertNotNull(firstLevelChild);

		fss.copy(firstLevelChild, folder1, tarSourceFolderName1, mon);

		Object copiedVirtualFolder = getChildFromFolder(folder1, tarSourceFolderName1);
		assertNotNull(copiedVirtualFolder);

		String[] contents = {"MANIFEST.MF"};
		int[] typesToCheck = {TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
	}


	public void testMoveToTarArchiveFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//create the source for testing first
		createSourceTarFiles();
		createSourceFolders();

		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertNotNull(targetTarFile);

		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertNotNull(sourceFolder);

		fss.move(sourceFolder, targetTarFile, sourceFolder.getName(), mon);

		Object theMovedChild = getChildFromFolder(targetTarFile, sourceFolderName);

		assertNotNull(theMovedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};

		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);

		//make sure the original folder is gone.
		Object originalSource = getChildFromFolder(tempDir, sourceFolderName);
		assertNull(originalSource);
	}



	public void testMoveTarVirtualFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//create the source for testing first
		createSourceTarFiles();

		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertNotNull(sourceTarFile);

		//then, create a folder inside the tempDir
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertNotNull(folder1);

		//Now, copy one of the folder from the zip file into folder1
		String movedFolderName = tarSourceFolderName1;
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceTarFile, tarSourceFolderName1);
		assertNotNull(firstLevelChild);
		fss.move(firstLevelChild, folder1, movedFolderName, mon);

		Object movedVirtualFolder = getChildFromFolder(folder1, movedFolderName);

		assertNotNull(movedVirtualFolder);

		String[] contents = {"MANIFEST.MF"};
		int[] typesToCheck = {TYPE_FILE};
		checkFolderContents((IRemoteFile)movedVirtualFolder, contents, typesToCheck);

		//Now, make sure the moved virtual folder is gone from its original zip file
		IRemoteFile tmp = (IRemoteFile)getChildFromFolder(sourceTarFile, tarSourceFolderName1);
		assertNull(tmp);
	}


	public void testRenameTarVirtualFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip file first.
		IRemoteFile newArchiveFile = createFileOrFolder(tempDirPath, testName, false);

		//Now, we want to create a text file inside.
		String childName = "aaa.txt";
		IRemoteFile file1 = createFileOrFolder(newArchiveFile.getAbsolutePath(), childName, false);
		assertNotNull(file1);

		childName = "bbb.txt";
		IRemoteFile file2 = createFileOrFolder(newArchiveFile.getAbsolutePath(), childName, false);
		assertNotNull(file2);

		//Create a folder
		childName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(newArchiveFile.getAbsolutePath(), childName, true);
		assertNotNull(folder1);

		//Now, check the contents
		String[] levelOneNamesToCheck = {"aaa.txt", "bbb.txt", "folder1"};
		int[] levalOneTypesToCheck = {TYPE_FILE, TYPE_FILE, TYPE_FOLDER};
		checkFolderContents(newArchiveFile, levelOneNamesToCheck, levalOneTypesToCheck);

		//Now, create some files inside the folder.
		String secondLevelChildName = "ccc.exe";
		IRemoteFile levelTwoChild1 = createFileOrFolder(folder1.getAbsolutePath(), secondLevelChildName, false);
		assertNotNull(levelTwoChild1);

		secondLevelChildName = "ddd.bat";
		IRemoteFile levelTwoChild2 = createFileOrFolder(folder1.getAbsolutePath(), secondLevelChildName, false);
		assertNotNull(levelTwoChild2);

		secondLevelChildName = "another Folder"; //folder with space
		IRemoteFile levelTwoChild3 = createFileOrFolder(folder1.getAbsolutePath(), secondLevelChildName, true);
		assertNotNull(levelTwoChild3);

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
	}

	public void testDeleteTarVirtualFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//create the source for testing first
		createSourceTarFiles();

		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertNotNull(sourceTarFile);

		//delete a file from level 2
		String parentForFileToDeleteName ="META-INF";
		IRemoteFile parentForFileToDelete = (IRemoteFile)getChildFromFolder(sourceTarFile, parentForFileToDeleteName);
		assertNotNull(parentForFileToDelete);
		String deleteFileName = "MANIFEST.MF";
		IRemoteFile fileToToDelete = (IRemoteFile)getChildFromFolder(parentForFileToDelete, deleteFileName);
		assertNotNull(fileToToDelete);
		//Now, delete this file
		fss.delete(fileToToDelete, mon);
		fileToToDelete = (IRemoteFile)getChildFromFolder(parentForFileToDelete, deleteFileName);
		assertNull(fileToToDelete);

		//then, get directory "java" under org/eclipse/dstore/core
		String parentForDirectoryToDeleteName ="org";
		IRemoteFile parentForDirectoryToDelete = (IRemoteFile)getChildFromFolder(sourceTarFile, parentForDirectoryToDeleteName);
		assertNotNull(parentForDirectoryToDelete);

		parentForDirectoryToDeleteName ="eclipse";
		parentForDirectoryToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, parentForDirectoryToDeleteName);
		assertNotNull(parentForDirectoryToDelete);

		parentForDirectoryToDeleteName ="dstore";
		parentForDirectoryToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, parentForDirectoryToDeleteName);
		assertNotNull(parentForDirectoryToDelete);

		parentForDirectoryToDeleteName ="core";
		parentForDirectoryToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, parentForDirectoryToDeleteName);
		assertNotNull(parentForDirectoryToDelete);

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
		assertNull(directoryToDelete);

		//Now, delete some files and folder inside the a virtual folder.
		parentForFileToDelete = (IRemoteFile)getChildFromFolder(parentForDirectoryToDelete, "model");
		deleteFileName = "DE.java";
		fileToToDelete = (IRemoteFile)getChildFromFolder(parentForFileToDelete, deleteFileName);
		assertNotNull(fileToToDelete);

		fss.delete(fileToToDelete, mon);

		//check the result
		fileToToDelete = (IRemoteFile)getChildFromFolder(parentForFileToDelete, deleteFileName);

		assertNull(fileToToDelete);
	}



	public void testCopyBatchToTarArchiveFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceTarFiles();
		createSourceFolders();

		String tarTargetFileName = tarSourceFileName1;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertNotNull(targetTarFile);

		//Now, copy the source folder.
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir,sourceFolderName);
		assertNotNull(sourceFolder);

		//Now, copy one of the folder from the sourceFolder into copiedTargetZipFile
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

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck1 = {"adsf", "eclipse-SDK-3.3M6-win32.zip", "epdcdump01.hex12", "epdcdump01.hex12aaaa"};

		int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);

		//Checking the second copied folder
		theCopiedChild = getChildFromFolder(targetTarFile, childToCopyName2);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck2 = {"features"};

		int[] typesToCheck2 = {TYPE_FOLDER};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);

		//Checking the third copied file
		theCopiedChild = getChildFromFolder(targetTarFile, childToCopyName3);
		assertNotNull(theCopiedChild);
		assertTrue(((IRemoteFile)theCopiedChild).isDirectory() != true);
	}



	/*
	public void testOpenFileFromTarArchive() throws Exception {
		if (isTestDisabled())
			return;

		//create the source for testing first
		createTarSourceForOpen();

		String tarTargetFileName = tarSourceForOpenTest;
		IRemoteFile targetTarFile = (IRemoteFile)getChildFromFolder(tempDir, tarTargetFileName);
		assertNotNull(targetTarFile);

		//Now get the contents of the virtual file we want to download:
		String fileContentToVerifyName1 = "MANIFEST.MF";

		//Get its parent first.
		IRemoteFile itsParentFolder = (IRemoteFile)getChildFromFolder(tempDir,tarSourceForOpenFolderName1);
		assertNotNull(itsParentFolder);

		//Then get this file:
		IRemoteFile thisVirtualFile = (IRemoteFile)getChildFromFolder(itsParentFolder, fileContentToVerifyName1);
		assertNotNull(thisVirtualFile);

		//Now, we want to download the content of this file
		//We could just construct a dummy localpath for it.
		String tempPath = getWorkspace().getRoot().getLocation().append("temp").toString();
		IFileStore temp = createDir(tempPath, true);
		String localPath = tempPath + File.separator + fileContentToVerifyName1;
		fss.download(thisVirtualFile, localPath, thisVirtualFile.getEncoding(), mon);

		//now, verify the content of the local file
		IFileStore localFile = temp.getChild(fileContentToVerifyName1);
		File actualFile = localFile.toLocalFile(EFS.NONE, new NullProgressMonitor());
		assertTrue("The file does not exist", actualFile.exists());
		//Check the content of the download file:
		boolean sameContent = compareContent1(getContents(fileContentString1), localFile.openInputStream(EFS.NONE, null));
		assertTrue(sameContent);


		//now, we got the contents of another virtual file we want to download:
		String fileContentToVerifyName2 = "Activator.java";
		itsParentFolder = (IRemoteFile)getChildFromFolder(tempDir,tarSourceForOpenFolderName2);
		assertNotNull(itsParentFolder);
		itsParentFolder = (IRemoteFile)getChildFromFolder(itsParentFolder,"eclipse");
		assertNotNull(itsParentFolder);
		itsParentFolder = (IRemoteFile)getChildFromFolder(itsParentFolder,"dstore");
		assertNotNull(itsParentFolder);
		itsParentFolder = (IRemoteFile)getChildFromFolder(itsParentFolder,"core");
		assertNotNull(itsParentFolder);
		thisVirtualFile = (IRemoteFile)getChildFromFolder(itsParentFolder, fileContentToVerifyName2);
		assertNotNull(thisVirtualFile);
		localPath = tempPath + File.separator + fileContentToVerifyName2;
		fss.download(thisVirtualFile, localPath, thisVirtualFile.getEncoding(), mon);

		//now, verify the content of the local file
		localFile = temp.getChild(fileContentToVerifyName2);
		//Check the content of the download file:
		sameContent = compareContent(getContents(fileContentString1), localFile.openInputStream(EFS.NONE, null));
		assertTrue(sameContent);
	}

	*/


}
