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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.tests.RSETestsPlugin;

public class FileServiceArchiveTest extends FileServiceBaseTest {

	protected String folderToCopyName1 = "RemoteSystemsConnections";
	protected String folderToCopyName2 = "6YLT5Xa";
	protected String folderToCopyName3 = "folderToCopy";
	
	protected String zipSourceFileName1 = "closedBefore.zip";
	protected String zipSourceFileName2 = "mynewzip.zip";
	protected String ZIP_SOURCE_DIR = "";
	protected String TEST_DIR = "";
	protected String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_LOCAL_ID;
	protected String SYSTEM_ADDRESS = "";
	protected String SYSTEM_NAME = "";
	protected String USER_ID = "";
	protected String PASSWORD = "";
	
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
		UniversalFileTransferUtility.copyWorkspaceResourcesToRemote((SystemWorkspaceResourceSet)tempObjects3, tempDir, mon, true);
		
		//Then, we need to retrieve children of the tempDir to cache their information.
		fss.resolveFilterString(tempDir, null, mon);
		
		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);
	}
	
	public void createSourceZipFiles() throws Exception
	{
		/* build scenario */
		String tempPath = getWorkspace().getRoot().getLocation().append("temp").toString();
		IFileStore temp = createDir(tempPath, true);
		String content = getRandomString();
		
		//Now, we need to construct a "closeBefore.zip" archive file
		//We will construct the content of the zip file in folder "RemoteSystemsConnections"
		//Then we copy this folder into a zip file by RSE API.
		IFileStore RemoteSystemsConnections = temp.getChild(folderToCopyName1);
		createDir(RemoteSystemsConnections, true);
		//Now, populate the contents in the folderToCopy.
		IFileStore Team = RemoteSystemsConnections.getChild("Team");
		createDir(Team, true);
		//create folder inside the Team folder.
		IFileStore Connections = Team.getChild("Connections");
		createDir(Connections, true);
		IFileStore dummyFile = Connections.getChild("dummy.txt");
		content = getRandomString();
		createFile(dummyFile, content);
		IFileStore Filters = Team.getChild("Filters");
		createDir(Filters, true);
		//Now, create a few folders inside the Filters folder.
		IFileStore ibm_cmds = Filters.getChild("ibm.cmds");
		createDir(ibm_cmds, true);
		IFileStore ibm_cmds400 = Filters.getChild("ibm.cmds400");
		createDir(ibm_cmds400, true);
		//create another directory inside this one
		IFileStore filter_pool1 = ibm_cmds400.getChild("FilterPool_Team Filter Pool");
		createDir(filter_pool1, true);
		//create a file inside this folder
		IFileStore filter_pool1_xml = filter_pool1.getChild("filterPool_Team Filter Pool.xmi");
		content = getRandomString();
		createFile(filter_pool1_xml, content);
		
		IFileStore ibm_cmdsIFS = Filters.getChild("ibm.cmdsIFS");
		createDir(ibm_cmdsIFS, true);
		IFileStore ibm_cmdsLocal = Filters.getChild("ibm.cmdsLocal");
		createDir(ibm_cmdsLocal, true);
		
		IFileStore ibm_files = Filters.getChild("ibm.files");
		createDir(ibm_files, true);
		//create another directory inside this one
		IFileStore filter_pool2 = ibm_files.getChild("FilterPool_Team Filter Pool");
		createDir(filter_pool2, true);
		//create a file inside this folder
		IFileStore filter_pool2_xml = filter_pool2.getChild("filterPool_Team Filter Pool.xmi");
		content = getRandomString();
		createFile(filter_pool2_xml, content);
		
		IFileStore ibm_files_aix = Filters.getChild("ibm.files.aix");
		createDir(ibm_files_aix, true);
		//create another directory inside this one
		IFileStore filter_pool3 = ibm_files_aix.getChild("FilterPool_Team Filter Pool");
		createDir(filter_pool3, true);
		//create a file inside this folder
		IFileStore filter_pool3_xml = filter_pool3.getChild("filterPool_Team Filter Pool.xmi");
		content = getRandomString();
		createFile(filter_pool3_xml, content);
		
		IFileStore ibm_iles_powerlinux = Filters.getChild("ibm.files.powerlinux");
		createDir(ibm_iles_powerlinux, true);
		//create another directory inside this one
		IFileStore filter_pool4 = ibm_iles_powerlinux.getChild("FilterPool_Team Filter Pool");
		createDir(filter_pool4, true);
		//create a file inside this folder
		IFileStore filter_pool4_xml = filter_pool4.getChild("filterPool_Team Filter Pool.xmi");
		content = getRandomString();
		createFile(filter_pool4_xml, content);
		
		IFileStore ibm_files400 = Filters.getChild("ibm.files400");
		createDir(ibm_files400, true);
		//create another directory inside this one
		IFileStore filter_pool5 = ibm_files400.getChild("FilterPool_Team Filter Pool");
		createDir(filter_pool5, true);
		//create a file inside this folder
		IFileStore filter_pool5_xml = filter_pool5.getChild("filterPool_Team Filter Pool.xmi");
		content = getRandomString();
		createFile(filter_pool5_xml, content);
		
		IFileStore ibm_filesLocal = Filters.getChild("ibm.filesLocal");
		createDir(ibm_filesLocal, true);
		//create another directory inside this one
		IFileStore filter_pool6 = ibm_filesLocal.getChild("FilterPool_Team Filter Pool");
		createDir(filter_pool6, true);
		//create a file inside this folder
		IFileStore filter_pool6_xml = filter_pool6.getChild("filterPool_Team Filter Pool.xmi");
		content = getRandomString();
		createFile(filter_pool6_xml, content);
		
		IFileStore ibm_filesIFS = Filters.getChild("ibm.filesIFS");
		createDir(ibm_filesIFS, true);
		//create another directory inside this one
		IFileStore filter_pool7 = ibm_filesIFS.getChild("FilterPool_Team Filter Pool");
		createDir(filter_pool7, true);
		//create a file inside this folder
		IFileStore filter_pool7_xml = filter_pool7.getChild("filterPool_Team Filter Pool.xmi");
		content = getRandomString();
		createFile(filter_pool7_xml, content);
		
		IFileStore ibm_filesWindows = Filters.getChild("ibm.filesWindows");
		createDir(ibm_filesWindows, true);
		//create another directory inside this one
		IFileStore filter_pool8 = ibm_filesWindows.getChild("FilterPool_Team Filter Pool");
		createDir(filter_pool8, true);
		//create a file inside this folder
		IFileStore filter_pool8_xml = filter_pool8.getChild("filterPool_Team Filter Pool.xmi");
		content = getRandomString();
		createFile(filter_pool8_xml, content);
		
		IFileStore ibm_jobs400 = Filters.getChild("ibm.jobs400");
		createDir(ibm_jobs400, true);
		//create another directory inside this one
		IFileStore filter_pool9 = ibm_jobs400.getChild("FilterPool_Team Filter Pool");
		createDir(filter_pool9, true);
		//create a file inside this folder
		IFileStore filter_pool9_xml = filter_pool9.getChild("filterPool_Team Filter Pool.xmi");
		content = getRandomString();
		createFile(filter_pool9_xml, content);
		
		//create file inside the Team folder.
		IFileStore profile_xml = Team.getChild("profile.xmi");
		content = getRandomString();
		createFile(profile_xml, content);
		
		//Now create another folder TypeFilters in the RemoteSystemsConnections folder
		IFileStore TypeFilters = RemoteSystemsConnections.getChild("TypeFilters");
		createDir(TypeFilters, true);
		dummyFile = TypeFilters.getChild("dummy.txt");
		content = getRandomString();
		createFile(dummyFile, content);
		//Now create another folder xuanchentp in the RemoteSystemsConnections folder
		IFileStore xuanchentp = RemoteSystemsConnections.getChild("xuanchentp");
		createDir(xuanchentp, true);
		//Create some files and folder inside the xuanchentp folder
		IFileStore Connections1 = xuanchentp.getChild("Connections");
		createDir(Connections1, true);
		dummyFile = Connections1.getChild("dummy.txt");
		content = getRandomString();
		createFile(dummyFile, content);
		IFileStore Filters1 = xuanchentp.getChild("Filters");
		createDir(Filters1, true);
		dummyFile = Filters1.getChild("dummy.txt");
		content = getRandomString();
		createFile(dummyFile, content);
		IFileStore profile_xml1 = xuanchentp.getChild("profile.xmi");
		content = getRandomString();
		createFile(profile_xml1, content);
		//now create two other files inside folder RemoteSystemConnections
		IFileStore compatibility = RemoteSystemsConnections.getChild(".compatibility");
		content = getRandomString();
		createFile(compatibility, content);
		IFileStore project = RemoteSystemsConnections.getChild(".project");
		createFile(project, content);
		
		//Now, we need to create the content of mynewzip.zip file
		IFileStore folder_6YLT5Xa = temp.getChild(folderToCopyName2);
		createDir(folder_6YLT5Xa, true);
		IFileStore folder_20070315a = folder_6YLT5Xa.getChild("20070315a");
		createDir(folder_20070315a, true);
		IFileStore QB5ROUTaadd = folder_20070315a.getChild("QB5ROUTaadd");
		content = getRandomString();
		createFile(QB5ROUTaadd, content);
		IFileStore folder_20070319 = folder_6YLT5Xa.getChild("20070319");
		createDir(folder_20070319, true);
		content = getRandomString();
		IFileStore QB5ROUTERa = folder_20070319.getChild("QB5ROUTERa");
		content = getRandomString();
		createFile(QB5ROUTERa, content);
		IFileStore folder_20070320a = folder_6YLT5Xa.getChild("20070320a");
		createDir(folder_20070320a, true);
		IFileStore QB5ROupdfasd = folder_20070320a.getChild("QB5ROupdfasd");
		content = getRandomString();
		createFile(QB5ROupdfasd, content);
		IFileStore folder_20070404a = folder_6YLT5Xa.getChild("20070404a");
		createDir(folder_20070404a, true);
		IFileStore qb5routeraad = folder_20070404a.getChild("qb5routeraad");
		content = getRandomString();
		createFile(qb5routeraad, content);
		IFileStore dummyFolder = folder_6YLT5Xa.getChild("dummyFolder");
		createDir(dummyFolder, true);
		IFileStore folder_20070404a1 = dummyFolder.getChild("20070404a");
		createDir(folder_20070404a1, true);
		IFileStore qb5routeraa = folder_20070404a1.getChild("qb5routeraa");
		content = getRandomString();
		createFile(qb5routeraa, content);
		IFileStore epdcdump01_hex12ab = dummyFolder.getChild("epdcdump01.hex12ab");
		content = getRandomString();
		createFile(epdcdump01_hex12ab, content);
		
		//now, copy folderToCopy into the folder in the remote system
		IRemoteFile sourceFolderToCopy1 = localFss.getRemoteFileObject(tempPath + '\\' + folderToCopyName1, mon);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) sourceFolderToCopy1).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet.addResource(sourceFolderToCopy1);
		ISystemResourceSet tempObjects1 = srcAdapter1.doDrag(fromSet, mon);
		UniversalFileTransferUtility.copyWorkspaceResourcesToRemote((SystemWorkspaceResourceSet)tempObjects1, tempDir, mon, true);
		
		IRemoteFile sourceFolderToCopy2 = localFss.getRemoteFileObject(tempPath + '\\' + folderToCopyName2, mon);
		ISystemDragDropAdapter srcAdapter2 = (ISystemDragDropAdapter) ((IAdaptable) sourceFolderToCopy2).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet2 = new SystemRemoteResourceSet(localFss, srcAdapter2);
		fromSet2.addResource(sourceFolderToCopy2);
		ISystemResourceSet tempObjects2 = srcAdapter2.doDrag(fromSet2, mon);
		UniversalFileTransferUtility.copyWorkspaceResourcesToRemote((SystemWorkspaceResourceSet)tempObjects2, tempDir, mon, true);
		
		IRemoteFile zipSource1 = createFileOrFolder(tempDir.getAbsolutePath(), zipSourceFileName1, false);
		assertTrue(zipSource1 != null);
		IRemoteFile zipSourceFolder = (IRemoteFile)getChildFromFolder(tempDir, folderToCopyName1);
		fss.copy(zipSourceFolder, zipSource1, folderToCopyName1, mon);
		
		IRemoteFile zipSource2 = createFileOrFolder(tempDir.getAbsolutePath(), zipSourceFileName2, false);
		assertTrue(zipSource2 != null);
		IRemoteFile zipSourceFolder2 = (IRemoteFile)getChildFromFolder(tempDir, folderToCopyName2);
		fss.copy(zipSourceFolder2, zipSource2, folderToCopyName2, mon);
		
		//Then, we need to retrieve children of the tempDir to cache their information.
		fss.resolveFilterString(tempDir, null, mon);
		
		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);
	}

	public void testCreateZipFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//Create the zip file first.
		String testName = "dummy.zip";
		IRemoteFile newArchiveFile = createFileOrFolder(tempDirPath, testName, false);
		assertNotNull(newArchiveFile);
		assertTrue(newArchiveFile.exists());
		assertTrue(newArchiveFile.canRead());
		assertTrue(newArchiveFile.canWrite());
		assertEquals(newArchiveFile.getName(), testName);
		
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
	
	public void testRenameVirtualFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//Create the zip file first.
		String testName = "dummy.zip";
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
	
	public void testMoveVirtualFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//Create the source data needed for testing
		createSourceZipFiles();
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		
		//Now, copy one of the folder from the zip file into folder1
		Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
		IRemoteFile originalVirtualFolder = (IRemoteFile)children[0];
		String movedFolderName = originalVirtualFolder.getName();
		fss.move(originalVirtualFolder, folder1, movedFolderName, mon);
		
		
		Object movedVirtualFolder = getChildFromFolder(folder1, movedFolderName);
		
		assertTrue(movedVirtualFolder != null);
		
		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)movedVirtualFolder, contents, typesToCheck);
		
		//Now, make sure the moved virtual folder is gone from its original zip file
		children = fss.resolveFilterString(sourceZipFile, null, mon);
		assertTrue(children.length == 0);
	}
	
	public void testMoveVirtualFileLevelTwo() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertTrue(sourceZipFile != null);
		
		//Now, move one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "Team");
		String movedFolderName = secondLevelChild.getName();
		
		//copy this level two childer into folder1
		fss.move(secondLevelChild, folder1, movedFolderName, mon);
		
		Object movedVirtualFolder = getChildFromFolder(folder1, movedFolderName);
		
		assertTrue(movedVirtualFolder != null);
		
		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)movedVirtualFolder, contents, typesToCheck);
		
		//Now, make sure the moved virtual folder is gone from its original zip file
		Object originalVirtualFolder = getChildFromFolder(firstLevelChild, movedFolderName);
		
		assertTrue(originalVirtualFolder == null);  //we should not be able to find it.
	}
	
	public void testMoveToArchiveFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		createSourceFolders();
		
		String targetZipFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, targetZipFileName);
		assertTrue(targetZipFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Now, move one of the folder from the sourceFolder into copiedTargetZipFile
		fss.move(sourceFolder, targetZipFile, sourceFolder.getName(), mon);
		
		Object theMovedChild = getChildFromFolder(targetZipFile, sourceFolderName);
		assertTrue(theMovedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);
		
		//make sure the original folder is gone.
		//make sure the original folder is gone.
		IRemoteFile tempDirRemoteFile = fss.getRemoteFileObject(tempDirPath, mon);
		Object originalSource = getChildFromFolder(tempDirRemoteFile, sourceFolderName);
		assertFalse(originalSource != null);
	}
	
	public void testMoveToVirtualFileLevelOne() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		createSourceFolders();
		
		String targetZipFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, targetZipFileName);
		assertTrue(targetZipFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Now, move one of the folder from the sourceFolder into a first level virtual file in targetZipFile
		//Get one of its first level children, and move the folder to there.
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetZipFile, folderToCopyName1);
		
		fss.move(sourceFolder, firstLevelChild, sourceFolderName, mon);
		
		Object theMovedChild = getChildFromFolder(firstLevelChild, sourceFolderName);
		
		assertTrue(theMovedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);
		
		//make sure the original folder is gone.
		IRemoteFile tempDirRemoteFile = fss.getRemoteFileObject(tempDirPath, mon);
		Object originalSource = getChildFromFolder(tempDirRemoteFile, sourceFolderName);
		assertFalse(originalSource != null);
	}
	
	public void testMoveToVirtualFileLevelTwo() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		createSourceFolders();
		
		String targetZipFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, targetZipFileName);
		assertTrue(targetZipFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Get one of its second level children, and move the folder to there.
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "Team");
		
		fss.move(sourceFolder, secondLevelChild, sourceFolder.getName(), mon);
		
		Object theMovedChild = getChildFromFolder(secondLevelChild, sourceFolderName);
		
		assertTrue(theMovedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);
		
		//make sure the original folder is gone.
		IRemoteFile tempDirRemoteFile = fss.getRemoteFileObject(tempDirPath, mon);
		Object originalSource = getChildFromFolder(tempDirRemoteFile, sourceFolderName);
		assertFalse(originalSource != null);
	}
	
	
	public void testCopyVirtualFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		
		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertTrue(sourceZipFile != null);
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		//Now, copy one of the folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		fss.copy(firstLevelChild, folder1, folderToCopyName1, mon);
		
		Object copiedVirtualFolder = getChildFromFolder(folder1, folderToCopyName1);
		
		assertTrue(copiedVirtualFolder != null);
		
		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
	}
	
	public void testCopyVirtualFileLevelTwo() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		
		//copy the zip file first.
		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertTrue(sourceZipFile != null);
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "Team");
		//copy this level two children into folder1
		fss.copy(secondLevelChild, folder1, secondLevelChild.getName(), mon);
		
		Object copiedVirtualFolder = getChildFromFolder(folder1, secondLevelChild.getName());
		
		assertTrue(copiedVirtualFolder != null);
		
		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
	}
	
	
	public void testCopyToArchiveFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		createSourceFolders();
		
		String targetZipFileName = zipSourceFileName1;
		IRemoteFile targetZipFile =  (IRemoteFile)getChildFromFolder(tempDir, targetZipFileName);
		assertTrue(targetZipFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Now, copy one of the folder from the sourceFolder into copiedTargetZipFile
		fss.copy(sourceFolder, targetZipFile, sourceFolder.getName(), mon);
		
		Object theCopiedChild = getChildFromFolder(targetZipFile, sourceFolderName);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}
	
	
	
	public void testCopyToVirtualFileLevelOne() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		createSourceFolders();
		
		String targetZipFileName = zipSourceFileName1; 
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, targetZipFileName);
		assertTrue(targetZipFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
		//Get one of its first level children, and copy the folder to there.
		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetZipFile, folderToCopyName1);
		
		fss.copy(sourceFolder, firstLevelChild, sourceFolder.getName(), mon);
		
		Object theCopiedChild = getChildFromFolder(firstLevelChild, sourceFolderName);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}
	

	public void testCopyToVirtualFileLevelTwo() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		createSourceFolders();
		
		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertTrue(targetZipFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, folderToCopyName3);
		assertTrue(sourceFolder != null);
		
		//Get one of its second level children, and copy the folder to there.
		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "TypeFilters");
		
		fss.copy(sourceFolder, secondLevelChild, sourceFolder.getName(), mon);
		
		Object theCopiedChild = getChildFromFolder(secondLevelChild, sourceFolderName);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}
	
	public void testCopyBatchToArchiveFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		createSourceFolders();
		
		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir,zipTargetFileName);
		assertTrue(targetZipFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir,sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Now, copy one of the folder from the sourceFolder into copiedTargetZipFile
		IRemoteFile[] sourceFiles = new IRemoteFile[3];
		//Also add some of its children into the batch.
		String childToCopyName1 = "aaaaaaaa";
		sourceFiles[0] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName1);
		String childToCopyName2 = "aaaab";
		sourceFiles[1] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName2);
		String childToCopyName3 = "epdcdump01.hex12a";
		sourceFiles[2] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName3);
		fss.copyBatch(sourceFiles, targetZipFile, mon);
		
		//Checking the first copied folder
		Object theCopiedChild = getChildFromFolder(targetZipFile, childToCopyName1);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck1 = {"adsf", "eclipse-SDK-3.3M6-win32.zip", "epdcdump01.hex12", "epdcdump01.hex12aaaa"};
		
		int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);
		
		//Checking the second copied folder
		theCopiedChild = getChildFromFolder(targetZipFile, childToCopyName2);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck2 = {"features"};
		
		int[] typesToCheck2 = {TYPE_FOLDER};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);
		
		//Checking the third copied file
		theCopiedChild = getChildFromFolder(targetZipFile, childToCopyName3);
		assertTrue(theCopiedChild != null);
		assertTrue(((IRemoteFile)theCopiedChild).isDirectory() != true);
	}

	public void testCopyBatchToVirtualFileLevelOne() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		createSourceFolders();
		
		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertTrue(targetZipFile != null);
		
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
		//Get one of its first level children, and copy the folder to there.
		Object[] childrenOfTargetZipFile = fss.resolveFilterString(targetZipFile, null, mon);
		
		IRemoteFile[] sourceFiles = new IRemoteFile[1];
		sourceFiles[0] = sourceFolder;
		fss.copyBatch(sourceFiles, ((IRemoteFile)childrenOfTargetZipFile[0]), mon);

		Object theCopiedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolderName);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}

	public void testCopyBatchToVirtualFileLevelTwo() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		createSourceFolders();
		
		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertTrue(targetZipFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertTrue(sourceFolder != null);
		
		//Get one of its second level children, and copy the folder to there.
		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "TypeFilters");
		
		IRemoteFile[] sourceFiles = new IRemoteFile[3];
		//Also add some of its children into the batch.
		String childToCopyName1 = "aaaaaaaa";
		sourceFiles[0] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName1);
		String childToCopyName2 = "aaaab";
		sourceFiles[1] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName2);
		String childToCopyName3 = "epdcdump01.hex12a";
		sourceFiles[2] = (IRemoteFile)getChildFromFolder(sourceFolder, childToCopyName3);
		fss.copyBatch(sourceFiles, secondLevelChild, mon);
		
		
		//Checking the first copied folder
		Object theCopiedChild = getChildFromFolder(secondLevelChild, childToCopyName1);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck1 = {"adsf", "eclipse-SDK-3.3M6-win32.zip", "epdcdump01.hex12", "epdcdump01.hex12aaaa"};
		
		int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);
		
		//Checking the second copied folder
		theCopiedChild = getChildFromFolder(secondLevelChild, childToCopyName2);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck2 = {"features"};
		
		int[] typesToCheck2 = {TYPE_FOLDER};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);
		
		//Checking the third copied file
		theCopiedChild = getChildFromFolder(secondLevelChild, childToCopyName3);
		assertTrue(theCopiedChild != null);
		assertTrue(((IRemoteFile)theCopiedChild).isDirectory() != true);
	}

	public void testCopyBatchVirtualFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		
		String sourceFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir,sourceFileName);
		assertTrue(sourceZipFile != null);
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		//Now, copy one of the folder from the zip file into folder1
		Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
		IRemoteFile[] sourceFiles = new IRemoteFile[3];
		String childToCopyName1 = "Team";
		sourceFiles[0] = (IRemoteFile)getChildFromFolder((IRemoteFile)children[0], childToCopyName1);
		assertTrue(sourceFiles[0] != null);
		String childToCopyName2 = "xuanchentp";
		sourceFiles[1] = (IRemoteFile)getChildFromFolder((IRemoteFile)children[0], childToCopyName2);
		assertTrue(sourceFiles[1] != null);
		String childToCopyName3 = ".compatibility";
		sourceFiles[2] = (IRemoteFile)getChildFromFolder((IRemoteFile)children[0], childToCopyName3);
		assertTrue(sourceFiles[2] != null);
		fss.copyBatch(sourceFiles, folder1, mon);
		
		Object copiedVirtualFolder = getChildFromFolder(folder1, childToCopyName1);
		assertTrue(copiedVirtualFolder != null);
		String[] contents1 = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck1 = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents1, typesToCheck1);
		
		copiedVirtualFolder = getChildFromFolder(folder1, childToCopyName2);
		assertTrue(copiedVirtualFolder != null);
		String[] contents2 = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck2 = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents2, typesToCheck2);
		
		Object copiedVirtualFile = getChildFromFolder(folder1, childToCopyName3);
		assertTrue(copiedVirtualFile != null);
		assertTrue(((IRemoteFile)copiedVirtualFile).isDirectory() != true);
	}

	public void testCopyBatchVirtualFileLevelTwo() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		
		String sourceFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertTrue(sourceZipFile != null);
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "Team");
		
		IRemoteFile[] sourceFiles = new IRemoteFile[1];
		sourceFiles[0] = secondLevelChild;
		//copy this level two childer into folder1
		fss.copyBatch(sourceFiles, folder1, mon);
		
		Object copiedVirtualFolder = getChildFromFolder(folder1, secondLevelChild.getName());
		
		assertTrue(copiedVirtualFolder != null);
		
		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
	}

	public void testCopyVirtualBatchToArchiveFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		
		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertTrue(targetZipFile != null);
		
		String sourcefileName = zipSourceFileName2;
		IRemoteFile sourceFile = (IRemoteFile)getChildFromFolder(tempDir, sourcefileName);
		assertTrue(sourceFile != null);
		
		//Now, copy one of the folder from the sourceFile into copiedTargetZipFile
		IRemoteFile[] sourceFiles = new IRemoteFile[1];
		String virutalFolderToCopyName = "6YLT5Xa";
		IRemoteFile virtualFolderToCopy = (IRemoteFile)getChildFromFolder(sourceFile, virutalFolderToCopyName);
		sourceFiles[0] = virtualFolderToCopy;
		fss.copyBatch(sourceFiles, targetZipFile, mon);
		
		Object theCopiedChild = getChildFromFolder(targetZipFile, virutalFolderToCopyName);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"20070315a", "20070319", "20070320a", "20070404a", "dummyFolder"};
		
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}

	public void testCopyVirtualBatchToVirtualFileLevelOne() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		
		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertTrue(targetZipFile != null);
		
		String sourcefileName = zipSourceFileName2;
		IRemoteFile sourceFile = (IRemoteFile)getChildFromFolder(tempDir, sourcefileName);
		assertTrue(sourceFile != null);
		
		//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
		//Get one of its first level children, and copy the folder to there.
		Object[] childrenOfTargetZipFile = fss.resolveFilterString(targetZipFile, null, mon);
		
		IRemoteFile[] sourceFiles = new IRemoteFile[3];
		
		String parentOfVirutalFolderToCopyName = "6YLT5Xa";
		IRemoteFile parentOfVirtualFolderToCopy = (IRemoteFile)getChildFromFolder(sourceFile, parentOfVirutalFolderToCopyName);
		String virtualChildToCopyName1 = "20070315a";
		String virtualChildToCopyName2 = "20070319";
		String virtualChildToCopyName3 = "dummyFolder";
		
		sourceFiles[0] = (IRemoteFile)getChildFromFolder(parentOfVirtualFolderToCopy, virtualChildToCopyName1);
		assertTrue(sourceFiles[0] != null);
		sourceFiles[1] = (IRemoteFile)getChildFromFolder(parentOfVirtualFolderToCopy, virtualChildToCopyName2);
		assertTrue(sourceFiles[1] != null);
		sourceFiles[2] = (IRemoteFile)getChildFromFolder(parentOfVirtualFolderToCopy, virtualChildToCopyName3);
		assertTrue(sourceFiles[2] != null);
		fss.copyBatch(sourceFiles, ((IRemoteFile)childrenOfTargetZipFile[0]), mon);

		
		Object theCopiedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), virtualChildToCopyName1);
		assertTrue(theCopiedChild != null);
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck1 = {"QB5ROUTaadd"};
		int[] typesToCheck1 = {TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);
		
		theCopiedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), virtualChildToCopyName2);
		assertTrue(theCopiedChild != null);
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck2 = {"QB5ROUTERa"};
		int[] typesToCheck2 = {TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);
		
		theCopiedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), virtualChildToCopyName3);
		assertTrue(theCopiedChild != null);
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck3 = {"20070404a", "epdcdump01.hex12ab"};
		int[] typesToCheck3 = {TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck3, typesToCheck3);
	}

	public void testCopyVirtualBatchToVirtualFileLevelTwo() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		createSourceZipFiles();
		
		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertTrue(targetZipFile != null);
		
		String sourcefileName = zipSourceFileName2;
		IRemoteFile sourceFile = (IRemoteFile)getChildFromFolder(tempDir,  sourcefileName);
		assertTrue(sourceFile != null);
		
		//Get one of its second level children, and copy the folder to there.
		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "TypeFilters");
		
		IRemoteFile[] sourceFiles = new IRemoteFile[1];
		
		String parentOfVirutalFolderToCopyName = "6YLT5Xa";
		IRemoteFile parentOfVirtualFolderToCopy = (IRemoteFile)getChildFromFolder(sourceFile, parentOfVirutalFolderToCopyName);
		String virtualFolderToCopyName = "dummyFolder";
		IRemoteFile virtualFolderToCopy = (IRemoteFile)getChildFromFolder(parentOfVirtualFolderToCopy, virtualFolderToCopyName);
		
		sourceFiles[0] = virtualFolderToCopy;
		
		fss.copyBatch(sourceFiles, secondLevelChild, mon);
		
		Object theCopiedChild = getChildFromFolder(parentOfVirtualFolderToCopy, virtualFolderToCopyName);
		
		assertTrue(theCopiedChild != null);
		
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"20070404a", "epdcdump01.hex12ab"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}

}
