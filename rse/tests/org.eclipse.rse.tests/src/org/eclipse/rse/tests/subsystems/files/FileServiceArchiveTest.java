/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM) - initial API and implementation
 * Martin Oberhuber (Wind River) - Fix Javadoc warnings
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 * Johnson Ma (Wind River) - [195402] Add tar.gz archive support
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

public class FileServiceArchiveTest extends FileServiceArchiveBaseTest {


	protected String zipSourceFileName1 = "closedBefore.zip";
	protected String zipSourceFileName2 = "mynewzip.zip";

	protected String ZIP_SOURCE_DIR = "";
	protected String TEST_DIR = "";
	protected String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_LOCAL_ID;
	protected String SYSTEM_ADDRESS = "";
	protected String SYSTEM_NAME = "";
	protected String USER_ID = "";
	protected String PASSWORD = "";

	/**
	 * Constructor with specific test name.
	 * @param name test to execute
	 */
	public FileServiceArchiveTest(String name) {
		super(name);
		tarSourceFileName1 = "source.tar";
		tarSourceFileName2 = "mynewtar.tar";
		tarSourceForOpenTest = "tarSourceForOpen.tar";
		testName = "dummy.tar";
	}

	public void createSourceZipFiles() throws Exception
	{
		createSourceZipFiles(fss);
	}
	public IRemoteFile createSourceZipFiles(IFileServiceSubSystem inputFss) throws Exception
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

		IRemoteFile targetDir = null;
		if (inputFss != fss)
		{
			//Create the tempDir inside the inputFss
			try
			{
				IRemoteFile homeDirectory = inputFss.getRemoteFileObject(".", mon);
				String baseFolderName = "rsetest";
				String homeFolderName = homeDirectory.getAbsolutePath();
				String testFolderName = FileServiceHelper.getRandomLocation(localFss, homeFolderName, baseFolderName, mon);
				targetDir = createFileOrFolder(localFss, homeFolderName, testFolderName, true);
			}
			catch (Exception e)
			{
				fail("Problem encountered: " + e.getStackTrace().toString());
			}
		}
		else
		{
			targetDir = tempDir;
		}
		//now, copy folderToCopy into the folder in the remote system
		IRemoteFile sourceFolderToCopy1 = localFss.getRemoteFileObject(tempPath + '\\' + folderToCopyName1, mon);
		ISystemDragDropAdapter srcAdapter1 = (ISystemDragDropAdapter) ((IAdaptable) sourceFolderToCopy1).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet = new SystemRemoteResourceSet(localFss, srcAdapter1);
		fromSet.addResource(sourceFolderToCopy1);
		ISystemResourceSet tempObjects1 = srcAdapter1.doDrag(fromSet, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects1, targetDir, mon, true);

		IRemoteFile sourceFolderToCopy2 = localFss.getRemoteFileObject(tempPath + '\\' + folderToCopyName2, mon);
		ISystemDragDropAdapter srcAdapter2 = (ISystemDragDropAdapter) ((IAdaptable) sourceFolderToCopy2).getAdapter(ISystemDragDropAdapter.class);
		SystemRemoteResourceSet fromSet2 = new SystemRemoteResourceSet(localFss, srcAdapter2);
		fromSet2.addResource(sourceFolderToCopy2);
		ISystemResourceSet tempObjects2 = srcAdapter2.doDrag(fromSet2, mon);
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects2, targetDir, mon, true);

		IRemoteFile zipSource1 = createFileOrFolder(inputFss, targetDir.getAbsolutePath(), zipSourceFileName1, false);
		assertNotNull(zipSource1);
		IRemoteFile zipSourceFolder = (IRemoteFile)getChildFromFolder(inputFss, targetDir, folderToCopyName1);
		inputFss.copy(zipSourceFolder, zipSource1, folderToCopyName1, mon);

		IRemoteFile zipSource2 = createFileOrFolder(inputFss, targetDir.getAbsolutePath(), zipSourceFileName2, false);
		assertNotNull(zipSource2);
		IRemoteFile zipSourceFolder2 = (IRemoteFile)getChildFromFolder(inputFss, targetDir, folderToCopyName2);
		inputFss.copy(zipSourceFolder2, zipSource2, folderToCopyName2, mon);

		//Then, we need to retrieve children of the tempDir to cache their information.
		inputFss.resolveFilterString(targetDir, null, mon);

		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);

		return targetDir;
	}



	public void testCreateZipFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

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

		return;

	}

	public void testRenameVirtualFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the zip file first.
		String testName = "dummy.zip";
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

		return;

	}

	public void testMoveVirtualFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		//Create the source data needed for testing
		createSourceZipFiles();

		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertNotNull(folder1);

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);

		//Now, copy one of the folder from the zip file into folder1
		Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
		IRemoteFile originalVirtualFolder = (IRemoteFile)children[0];
		String movedFolderName = originalVirtualFolder.getName();
		fss.move(originalVirtualFolder, folder1, movedFolderName, mon);


		Object movedVirtualFolder = getChildFromFolder(folder1, movedFolderName);

		assertNotNull(movedVirtualFolder);

		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)movedVirtualFolder, contents, typesToCheck);

		//Now, make sure the moved virtual folder is gone from its original zip file
		children = fss.resolveFilterString(sourceZipFile, null, mon);
		assertTrue(children.length == 0);
	}

	public void testMoveVirtualFileLevelTwo() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertNotNull(folder1);

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//Now, move one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "Team");
		String movedFolderName = secondLevelChild.getName();

		//copy this level two childer into folder1
		fss.move(secondLevelChild, folder1, movedFolderName, mon);

		Object movedVirtualFolder = getChildFromFolder(folder1, movedFolderName);

		assertNotNull(movedVirtualFolder);

		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)movedVirtualFolder, contents, typesToCheck);

		//Now, make sure the moved virtual folder is gone from its original zip file
		Object originalVirtualFolder = getChildFromFolder(firstLevelChild, movedFolderName);

		assertNull(originalVirtualFolder);  //we should not be able to find it.
	}

	public void testMoveToArchiveFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();
		createSourceFolders();

		String targetZipFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, targetZipFileName);
		assertNotNull(targetZipFile);

		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertNotNull(sourceFolder);

		//Now, move one of the folder from the sourceFolder into copiedTargetZipFile
		fss.move(sourceFolder, targetZipFile, sourceFolder.getName(), mon);

		Object theMovedChild = getChildFromFolder(targetZipFile, sourceFolderName);
		assertNotNull(theMovedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};

		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);

		//make sure the original folder is gone.
		//make sure the original folder is gone.
		IRemoteFile tempDirRemoteFile = fss.getRemoteFileObject(tempDirPath, mon);
		Object originalSource = getChildFromFolder(tempDirRemoteFile, sourceFolderName);
		assertNull(originalSource);
	}

	public void testMoveToVirtualFileLevelOne() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();
		createSourceFolders();

		String targetZipFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, targetZipFileName);
		assertNotNull(targetZipFile);

		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertNotNull(sourceFolder);

		//Now, move one of the folder from the sourceFolder into a first level virtual file in targetZipFile
		//Get one of its first level children, and move the folder to there.
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetZipFile, folderToCopyName1);

		fss.move(sourceFolder, firstLevelChild, sourceFolderName, mon);

		Object theMovedChild = getChildFromFolder(firstLevelChild, sourceFolderName);

		assertNotNull(theMovedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);

		//make sure the original folder is gone.
		IRemoteFile tempDirRemoteFile = fss.getRemoteFileObject(tempDirPath, mon);
		Object originalSource = getChildFromFolder(tempDirRemoteFile, sourceFolderName);
		assertNull(originalSource);
	}

	public void testMoveToVirtualFileLevelTwo() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

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

		assertNotNull(theMovedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);

		//make sure the original folder is gone.
		IRemoteFile tempDirRemoteFile = fss.getRemoteFileObject(tempDirPath, mon);
		Object originalSource = getChildFromFolder(tempDirRemoteFile, sourceFolderName);
		assertNull(originalSource);
	}


	public void testCopyVirtualFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertNotNull(folder1);

		//Now, copy one of the folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		fss.copy(firstLevelChild, folder1, folderToCopyName1, mon);

		Object copiedVirtualFolder = getChildFromFolder(folder1, folderToCopyName1);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
	}

	public void testCopyVirtualFileLevelTwo() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		//copy the zip file first.
		String sourceZipFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceZipFileName);
		assertNotNull(sourceZipFile);

		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertNotNull(folder1);

		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "Team");
		//copy this level two children into folder1
		fss.copy(secondLevelChild, folder1, secondLevelChild.getName(), mon);

		Object copiedVirtualFolder = getChildFromFolder(folder1, secondLevelChild.getName());

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
	}


	public void testCopyToArchiveFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();
		createSourceFolders();

		String targetZipFileName = zipSourceFileName1;
		IRemoteFile targetZipFile =  (IRemoteFile)getChildFromFolder(tempDir, targetZipFileName);
		assertNotNull(targetZipFile);

		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertNotNull(sourceFolder);

		//Now, copy one of the folder from the sourceFolder into copiedTargetZipFile
		fss.copy(sourceFolder, targetZipFile, sourceFolder.getName(), mon);

		Object theCopiedChild = getChildFromFolder(targetZipFile, sourceFolderName);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};

		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}



	public void testCopyToVirtualFileLevelOne() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();
		createSourceFolders();

		String targetZipFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, targetZipFileName);
		assertNotNull(targetZipFile);

		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertNotNull(sourceFolder);

		//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
		//Get one of its first level children, and copy the folder to there.
		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetZipFile, folderToCopyName1);

		fss.copy(sourceFolder, firstLevelChild, sourceFolder.getName(), mon);

		Object theCopiedChild = getChildFromFolder(firstLevelChild, sourceFolderName);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}


	public void testCopyToVirtualFileLevelTwo() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();
		createSourceFolders();

		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertNotNull(targetZipFile);

		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, folderToCopyName3);
		assertNotNull(sourceFolder);

		//Get one of its second level children, and copy the folder to there.
		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "TypeFilters");

		fss.copy(sourceFolder, secondLevelChild, sourceFolder.getName(), mon);

		Object theCopiedChild = getChildFromFolder(secondLevelChild, sourceFolderName);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}

	public void testCopyBatchToArchiveFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();
		createSourceFolders();

		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir,zipTargetFileName);
		assertNotNull(targetZipFile);

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
		fss.copyBatch(sourceFiles, targetZipFile, mon);

		//Checking the first copied folder
		Object theCopiedChild = getChildFromFolder(targetZipFile, childToCopyName1);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck1 = {"adsf", "eclipse-SDK-3.3M6-win32.zip", "epdcdump01.hex12", "epdcdump01.hex12aaaa"};

		int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);

		//Checking the second copied folder
		theCopiedChild = getChildFromFolder(targetZipFile, childToCopyName2);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck2 = {"features"};

		int[] typesToCheck2 = {TYPE_FOLDER};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);

		//Checking the third copied file
		theCopiedChild = getChildFromFolder(targetZipFile, childToCopyName3);
		assertNotNull(theCopiedChild);
		assertTrue(((IRemoteFile)theCopiedChild).isDirectory() != true);
	}

	public void testCopyBatchToVirtualFileLevelOne() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();
		createSourceFolders();

		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertNotNull(targetZipFile);

		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertNotNull(sourceFolder);

		//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
		//Get one of its first level children, and copy the folder to there.
		Object[] childrenOfTargetZipFile = fss.resolveFilterString(targetZipFile, null, mon);

		IRemoteFile[] sourceFiles = new IRemoteFile[1];
		sourceFiles[0] = sourceFolder;
		fss.copyBatch(sourceFiles, ((IRemoteFile)childrenOfTargetZipFile[0]), mon);

		Object theCopiedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolderName);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}

	public void testCopyBatchToVirtualFileLevelTwo() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();
		createSourceFolders();

		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertNotNull(targetZipFile);

		//Now, copy the source folder.
		String sourceFolderName = folderToCopyName3;
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertNotNull(sourceFolder);

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

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck1 = {"adsf", "eclipse-SDK-3.3M6-win32.zip", "epdcdump01.hex12", "epdcdump01.hex12aaaa"};

		int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);

		//Checking the second copied folder
		theCopiedChild = getChildFromFolder(secondLevelChild, childToCopyName2);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck2 = {"features"};

		int[] typesToCheck2 = {TYPE_FOLDER};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);

		//Checking the third copied file
		theCopiedChild = getChildFromFolder(secondLevelChild, childToCopyName3);
		assertNotNull(theCopiedChild);
		assertTrue(((IRemoteFile)theCopiedChild).isDirectory() != true);
	}

	public void testCopyBatchVirtualFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		String sourceFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir,sourceFileName);
		assertNotNull(sourceZipFile);

		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertNotNull(folder1);

		//Now, copy one of the folder from the zip file into folder1
		Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
		IRemoteFile[] sourceFiles = new IRemoteFile[3];
		String childToCopyName1 = "Team";
		sourceFiles[0] = (IRemoteFile)getChildFromFolder((IRemoteFile)children[0], childToCopyName1);
		assertNotNull(sourceFiles[0]);
		String childToCopyName2 = "xuanchentp";
		sourceFiles[1] = (IRemoteFile)getChildFromFolder((IRemoteFile)children[0], childToCopyName2);
		assertNotNull(sourceFiles[1]);
		String childToCopyName3 = ".compatibility";
		sourceFiles[2] = (IRemoteFile)getChildFromFolder((IRemoteFile)children[0], childToCopyName3);
		assertNotNull(sourceFiles[2]);
		fss.copyBatch(sourceFiles, folder1, mon);

		Object copiedVirtualFolder = getChildFromFolder(folder1, childToCopyName1);
		assertNotNull(copiedVirtualFolder);
		String[] contents1 = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck1 = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents1, typesToCheck1);

		copiedVirtualFolder = getChildFromFolder(folder1, childToCopyName2);
		assertNotNull(copiedVirtualFolder);
		String[] contents2 = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck2 = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents2, typesToCheck2);

		Object copiedVirtualFile = getChildFromFolder(folder1, childToCopyName3);
		assertNotNull(copiedVirtualFile);
		assertTrue(((IRemoteFile)copiedVirtualFile).isDirectory() != true);
	}

	public void testCopyBatchVirtualFileLevelTwo() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		String sourceFileName = zipSourceFileName1;
		IRemoteFile sourceZipFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertNotNull(sourceZipFile);

		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertNotNull(folder1);

		//Now, copy one of the level two folder from the zip file into folder1
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceZipFile, folderToCopyName1);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "Team");

		IRemoteFile[] sourceFiles = new IRemoteFile[1];
		sourceFiles[0] = secondLevelChild;
		//copy this level two childer into folder1
		fss.copyBatch(sourceFiles, folder1, mon);

		Object copiedVirtualFolder = getChildFromFolder(folder1, secondLevelChild.getName());

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"Connections", "Filters", "profile.xmi"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
	}

	public void testCopyVirtualBatchToArchiveFile() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertNotNull(targetZipFile);

		String sourcefileName = zipSourceFileName2;
		IRemoteFile sourceFile = (IRemoteFile)getChildFromFolder(tempDir, sourcefileName);
		assertNotNull(sourceFile);

		//Now, copy one of the folder from the sourceFile into copiedTargetZipFile
		IRemoteFile[] sourceFiles = new IRemoteFile[1];
		String virutalFolderToCopyName = "6YLT5Xa";
		IRemoteFile virtualFolderToCopy = (IRemoteFile)getChildFromFolder(sourceFile, virutalFolderToCopyName);
		sourceFiles[0] = virtualFolderToCopy;
		fss.copyBatch(sourceFiles, targetZipFile, mon);

		Object theCopiedChild = getChildFromFolder(targetZipFile, virutalFolderToCopyName);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"20070315a", "20070319", "20070320a", "20070404a", "dummyFolder"};

		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}

	public void testCopyVirtualBatchToVirtualFileLevelOne() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertNotNull(targetZipFile);

		String sourcefileName = zipSourceFileName2;
		IRemoteFile sourceFile = (IRemoteFile)getChildFromFolder(tempDir, sourcefileName);
		assertNotNull(sourceFile);

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
		assertNotNull(sourceFiles[0]);
		sourceFiles[1] = (IRemoteFile)getChildFromFolder(parentOfVirtualFolderToCopy, virtualChildToCopyName2);
		assertNotNull(sourceFiles[1]);
		sourceFiles[2] = (IRemoteFile)getChildFromFolder(parentOfVirtualFolderToCopy, virtualChildToCopyName3);
		assertNotNull(sourceFiles[2]);
		fss.copyBatch(sourceFiles, ((IRemoteFile)childrenOfTargetZipFile[0]), mon);


		Object theCopiedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), virtualChildToCopyName1);
		assertNotNull(theCopiedChild);
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck1 = {"QB5ROUTaadd"};
		int[] typesToCheck1 = {TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);

		theCopiedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), virtualChildToCopyName2);
		assertNotNull(theCopiedChild);
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck2 = {"QB5ROUTERa"};
		int[] typesToCheck2 = {TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);

		theCopiedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), virtualChildToCopyName3);
		assertNotNull(theCopiedChild);
		//Also make sure the copied child has the right contents.
		String[] childrenToCheck3 = {"20070404a", "epdcdump01.hex12ab"};
		int[] typesToCheck3 = {TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck3, typesToCheck3);
	}

	public void testCopyVirtualBatchToVirtualFileLevelTwo() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceZipFiles();

		String zipTargetFileName = zipSourceFileName1;
		IRemoteFile targetZipFile = (IRemoteFile)getChildFromFolder(tempDir, zipTargetFileName);
		assertNotNull(targetZipFile);

		String sourcefileName = zipSourceFileName2;
		IRemoteFile sourceFile = (IRemoteFile)getChildFromFolder(tempDir,  sourcefileName);
		assertNotNull(sourceFile);

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

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"20070404a", "epdcdump01.hex12ab"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}




	public void testCopyToTarVirtualFileLevelOne() throws Exception {
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

		//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
		//Get one of its first level children, and copy the folder to there.
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetTarFile, "org");
		assertNotNull(firstLevelChild);

		fss.copy(sourceFolder, firstLevelChild, sourceFolder.getName(), mon);

		Object theCopiedChild = getChildFromFolder(firstLevelChild, sourceFolderName);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}

	public void testCopyToTarVirtualFileLevelFour() throws Exception {
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

		//Get one of its fourth level children, and copy the folder to there.
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetTarFile, "org");
		assertNotNull(firstLevelChild);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
		assertNotNull(secondLevelChild);
		IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
		assertNotNull(thirdLevelChild);
		IRemoteFile fourLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
		assertNotNull(fourLevelChild);

		//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
		fss.copy(sourceFolder, fourLevelChild, sourceFolder.getName(), mon);

		Object theCopiedChild = getChildFromFolder(fourLevelChild, sourceFolderName);
		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);
	}


	public void testCopyTarVirtualFileLevelFour() throws Exception {
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

		//Now, copy one of the level four folder from the zip file into folder1
		//The folder is org/eclipse/dstore/core
		//then, get directory "java" under org/eclipse/dstore/core
		String parentForDirectoryToCopyName ="org";
		IRemoteFile parentForDirectoryToCopy = (IRemoteFile)getChildFromFolder(sourceTarFile, parentForDirectoryToCopyName);
		assertNotNull(parentForDirectoryToCopy);

		parentForDirectoryToCopyName ="eclipse";
		parentForDirectoryToCopy = (IRemoteFile)getChildFromFolder(parentForDirectoryToCopy, parentForDirectoryToCopyName);
		assertNotNull(parentForDirectoryToCopy);

		parentForDirectoryToCopyName ="dstore";
		parentForDirectoryToCopy = (IRemoteFile)getChildFromFolder(parentForDirectoryToCopy, parentForDirectoryToCopyName);
		assertNotNull(parentForDirectoryToCopy);

		String directoryToCopyName ="core";
		IRemoteFile directoryToCopy = (IRemoteFile)getChildFromFolder(parentForDirectoryToCopy, directoryToCopyName);
		assertNotNull(directoryToCopy);


		//copy this level four children into folder1
		fss.copy(directoryToCopy, folder1, directoryToCopyName, mon);

		Object copiedVirtualFolder = getChildFromFolder(folder1, directoryToCopyName);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"client", "java", "miners", "model", "server", "util", "Activator.java"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
	}


	public void testMoveToTarVirtualFileLevelOne() throws Exception {
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

		//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
		//Get one of its first level children, and copy the folder to there.
		Object[] childrenOfTargetZipFile = fss.resolveFilterString(targetTarFile, null, mon);

		//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
		fss.move(sourceFolder, ((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolderName, mon);

		Object theMovedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolderName);

		assertNotNull(theMovedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theMovedChild, childrenToCheck, typesToCheck);

		//make sure the original folder is gone.
		Object originalSource = getChildFromFolder(tempDir, sourceFolderName);
		assertNull(originalSource);
	}

	public void testMoveToVirtualFileLevelFour() throws Exception {
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

		//Get one of its fourth level children, and copy the folder to there.
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetTarFile, "org");
		assertNotNull(firstLevelChild);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
		assertNotNull(secondLevelChild);
		IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
		assertNotNull(thirdLevelChild);
		IRemoteFile fourLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
		assertNotNull(fourLevelChild);

		//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
		fss.move(sourceFolder, fourLevelChild, sourceFolder.getName(), mon);

		Object theCopiedChild = getChildFromFolder(fourLevelChild, sourceFolderName);

		assertNotNull(theCopiedChild);

		//Also make sure the moved child has the right contents.
		String[] childrenToCheck = {"aaaaaaaa", "aaaab", "epdcdump01.hex12a", "RSE-SDK-2.0RC1.zip"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck, typesToCheck);

		//make sure the original folder is gone.
		Object originalSource = getChildFromFolder(tempDir, sourceFolderName);
		assertNull(originalSource);
	}


	public void testMoveTarVirtualFileLevelFour() throws Exception {
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

		//Get one of its fourth level children, and move it to the folder
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceTarFile, "org");
		assertNotNull(firstLevelChild);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
		assertNotNull(secondLevelChild);
		IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
		assertNotNull(thirdLevelChild);
		IRemoteFile fourthLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
		assertNotNull(fourthLevelChild);
		String movedFolderName = fourthLevelChild.getName();

		//copy this level four children into folder1
		fss.move(fourthLevelChild, folder1, movedFolderName, mon);

		Object copiedVirtualFolder = getChildFromFolder(folder1, movedFolderName);

		assertNotNull(copiedVirtualFolder);

		String[] contents = {"client", "java", "miners", "model", "server", "util", "Activator.java"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);

		//Now, make sure the moved virtual folder is gone from its original zip file
		//children = fss.resolveFilterString(sourceTarFile, null, mon);
		Object result = getChildFromFolder(thirdLevelChild, movedFolderName);
		assertNull(result);  //we should not be able to find it.
	}



	public void testCopyBatchToTarVirtualFileLevelFour() throws Exception {
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
		IRemoteFile sourceFolder = (IRemoteFile)getChildFromFolder(tempDir, sourceFolderName);
		assertNotNull(sourceFolder);

		//Get one of its fourth level children, and copy the folder to there.
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(targetTarFile, "org");
		assertNotNull(firstLevelChild);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
		assertNotNull(secondLevelChild);
		IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
		assertNotNull(thirdLevelChild);
		IRemoteFile fourthLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
		assertNotNull(fourthLevelChild);

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

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck1 = {"adsf", "eclipse-SDK-3.3M6-win32.zip", "epdcdump01.hex12", "epdcdump01.hex12aaaa"};

		int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck1, typesToCheck1);

		//Checking the second copied folder
		theCopiedChild = getChildFromFolder(fourthLevelChild, childToCopyName2);

		assertNotNull(theCopiedChild);

		//Also make sure the copied child has the right contents.
		String[] childrenToCheck2 = {"features"};

		int[] typesToCheck2 = {TYPE_FOLDER};
		checkFolderContents((IRemoteFile)theCopiedChild, childrenToCheck2, typesToCheck2);

		//Checking the third copied file
		theCopiedChild = getChildFromFolder(fourthLevelChild, childToCopyName3);
		assertNotNull(theCopiedChild);
		assertTrue(((IRemoteFile)theCopiedChild).isDirectory() != true);
	}



	public void testCopyBatchTarVirtualFileLevelFive() throws Exception {
		//-test-author-:XuanChen
		if (isTestDisabled())
			return;

		createSourceTarFiles();

		String sourceFileName = tarSourceFileName1;
		IRemoteFile sourceTarFile = (IRemoteFile)getChildFromFolder(tempDir, sourceFileName);
		assertNotNull(sourceTarFile);

		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertNotNull(folder1);

		//Get several of its fifth level children, and them into the folder.
		IRemoteFile firstLevelChild = (IRemoteFile)getChildFromFolder(sourceTarFile, "org");
		assertNotNull(firstLevelChild);
		IRemoteFile secondLevelChild = (IRemoteFile)getChildFromFolder(firstLevelChild, "eclipse");
		assertNotNull(secondLevelChild);
		IRemoteFile thirdLevelChild = (IRemoteFile)getChildFromFolder(secondLevelChild, "dstore");
		assertNotNull(thirdLevelChild);
		IRemoteFile fourthLevelChild = (IRemoteFile)getChildFromFolder(thirdLevelChild, "core");
		assertNotNull(fourthLevelChild);

		IRemoteFile[] fifLevelChildrenToCopy = new IRemoteFile[3];

		String firstToCopyName = "client";
		fifLevelChildrenToCopy[0] = (IRemoteFile)getChildFromFolder(fourthLevelChild, firstToCopyName);
		assertNotNull(fifLevelChildrenToCopy[0]);
		String secondToCopyName = "miners";
		fifLevelChildrenToCopy[1] = (IRemoteFile)getChildFromFolder(fourthLevelChild, secondToCopyName);
		assertNotNull(fifLevelChildrenToCopy[1]);
		String thirdToCopyName = "Activator.java";
		fifLevelChildrenToCopy[2] = (IRemoteFile)getChildFromFolder(fourthLevelChild, thirdToCopyName);
		assertNotNull(fifLevelChildrenToCopy[2]);


		fss.copyBatch(fifLevelChildrenToCopy, folder1, mon);

		Object copiedVirtualFolder1 = getChildFromFolder(folder1, firstToCopyName);
		assertNotNull(copiedVirtualFolder1);
		String[] contents1 = {"ClientConnection.java", "ConnectionStatus.java"};
		int[] typesToCheck1 = {TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder1, contents1, typesToCheck1);

		Object copiedVirtualFolder2 = getChildFromFolder(folder1, secondToCopyName);
		assertNotNull(copiedVirtualFolder2);
		String[] contents2 = {"Miner.java", "MinerThread.java"};
		int[] typesToCheck2 = {TYPE_FILE, TYPE_FILE};
		checkFolderContents((IRemoteFile)copiedVirtualFolder2, contents2, typesToCheck2);

		Object copiedVirtualFolder3 = getChildFromFolder(folder1, thirdToCopyName);
		assertNotNull(copiedVirtualFolder3);
	}

}
