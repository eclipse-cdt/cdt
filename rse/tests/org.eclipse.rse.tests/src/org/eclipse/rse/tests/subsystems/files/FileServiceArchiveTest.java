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

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class FileServiceArchiveTest extends FileServiceBaseTest {

	protected String ZIP_SOURCE_DIR = "";
	protected String TMP_DIR_PARENT = "";
	protected String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_LOCAL_ID;
	protected String SYSTEM_ADDRESS = "";
	protected String SYSTEM_NAME = "";
	protected String USER_ID = "";
	protected String PASSWORD = "";
	
	public void setUp() {
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
		IHost localHost = getLocalSystemConnection();
		ISystemRegistry sr = SystemStartHere.getSystemRegistry(); 
		ISubSystem[] ss = sr.getServiceSubSystems(localHost, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				fss = (IFileServiceSubSystem)ss[i];
				fs = fss.getFileService();
			}
		}
		try {
			 File tempDirFile = File.createTempFile("rsetest","dir"); //$NON-NLS-1$ //$NON-NLS-2$
			 assertTrue(tempDirFile.delete());
			 assertTrue(tempDirFile.mkdir());
			 tempDirPath = tempDirFile.getAbsolutePath();
			 tempDir = fss.getRemoteFileObject(tempDirPath, mon);
		} catch(Exception e) {
			assertTrue("Exception creating temp dir", false); //$NON-NLS-1$
		}
		ZIP_SOURCE_DIR = "d:\\tmp\\junit_source\\";
		
	}
	
	public void tearDown() {
		try {
			fss.delete(tempDir, mon);
		} catch(SystemMessageException msg) {
			assertFalse("Exception: "+msg.getLocalizedMessage(), true); //$NON-NLS-1$
		}
	}
	
	public void testCreateZipFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//Create the zip file first.
		String testName = "dummy.zip";
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
	
	public void testRenameVirtualFile() throws SystemMessageException {
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
	
	public void testRenameVirtualFileBigZip() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String sourceFileName = "eclipse-SDK-3.3M6-win32.zip";
		IRemoteFile sourceZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFileName, sourceFileName, tempDirPath);
		assertTrue(sourceZipFile != null);
		
		//Get the "features" directory under "eclipse"
		String directoryName ="eclipse";
		IRemoteFile parentDirectory = (IRemoteFile)getChildFromFolder(sourceZipFile, directoryName);
		assertTrue(parentDirectory != null);
		directoryName = "features";
		parentDirectory = (IRemoteFile)getChildFromFolder(parentDirectory, directoryName);
		//Now, we want to rename a folder (with really long name) inside this "feature" directory.
		directoryName = "org.eclipse.cvs.source_1.0.0.v20070202-7B79_71CI99g_LDV2411";
		IRemoteFile directoryToRename = (IRemoteFile)getChildFromFolder(parentDirectory, directoryName);
		assertTrue(directoryToRename != null);
		//Now, rename this directory
		String newDirectoryName = "org.eclipse.cvs.source_1.0.0.v20070202-7B79_71CI99g_LDV2411 aaa#@";
		fss.rename(directoryToRename, newDirectoryName, mon);
		
		//check result of this operation
		String[] namesToCheck = {"org.eclipse.cvs.source_1.0.0.v20070202-7B79_71CI99g_LDV2411 aaa#@", 
				                 "org.eclipse.cvs_1.0.0.v20070202-7B79_71CI99g_LDV2411", 
				                 "org.eclipse.jdt.source_3.3.0.v20070319-1800-7n7dEAWEgDfeRYHuyAaFTR",
				                 "org.eclipse.jdt_3.3.0.v20070319-1800-7n7dEAWEgDfeRYHuyAaFTR",
				                 "org.eclipse.pde.source_3.3.0.v20061114-0800-7M7M-6NUEF6EbRV6EWGC",
				                 "org.eclipse.pde_3.3.0.v20061114-0800-7M7M-6NUEF6EbRV6EWGC",
				                 "org.eclipse.platform.source_3.3.0.v20070316b-_-9MEhFxiXZ2gosKM6M-3_Hu9MqL",
				                 "org.eclipse.platform_3.3.0.v20070316b-_-9MEhFxiXZ2gosKM6M-3_Hu9MqL",
				                 "org.eclipse.rcp.source_3.3.0.v20070316b-8l8VCEYXUPdNkSgt8LpKG",
				                 "org.eclipse.rcp_3.3.0.v20070316b-8l8VCEYXUPdNkSgt8LpKG",
				                 "org.eclipse.sdk_3.3.0.v20070319-7L7J-4DjllPLcCcxrZDgaddnelet"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER,TYPE_FOLDER,TYPE_FOLDER,TYPE_FOLDER, 
				              TYPE_FOLDER, TYPE_FOLDER,TYPE_FOLDER,TYPE_FOLDER,TYPE_FOLDER, TYPE_FOLDER};
		checkFolderContents(parentDirectory, namesToCheck, typesToCheck);
		
		//Now, rename some files and folder inside the renamed folder.
		directoryToRename = (IRemoteFile)getChildFromFolder(parentDirectory, newDirectoryName);
		assertTrue(directoryToRename != null);
		
		String fileToRenameName = "eclipse_update_120.jpg";
		IRemoteFile levelThreeFileToRename = (IRemoteFile)getChildFromFolder(directoryToRename, fileToRenameName);
		assertTrue(levelThreeFileToRename != null);
		
		directoryName = "META-INF";
		IRemoteFile levelThreeDirectoryToRename = (IRemoteFile)getChildFromFolder(directoryToRename, directoryName);
		assertTrue(levelThreeDirectoryToRename != null);
		
		//Rename file first
		String newFileName = "eclipse_update_120aaa.jpg";
		fss.rename(levelThreeFileToRename, newFileName, mon);
		
		newDirectoryName = "META!@#$%^&*INF";
		fss.rename(levelThreeDirectoryToRename, newDirectoryName, mon);
		
		//check the result
		String[] level3NamesToCheck = {"META!@#$%^&*INF", 
                "eclipse_update_120aaa.jpg", 
                "epl-v10.html",
                "feature.properties",
                "feature.xml",
                "license.html"};
		int[] level3TypesToCheck = {TYPE_FOLDER, TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
		checkFolderContents(directoryToRename, level3NamesToCheck, level3TypesToCheck);
		
		return;
		
	}
	
	public void testMoveVirtualFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String sourceFileName = "closedBefore.zip";
		IRemoteFile sourceZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFileName, sourceFileName, tempDirPath);
		assertTrue(sourceZipFile != null);
		
		//then, create a folder inside the tempDir
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		//Now, copy one of the folder from the zip file into folder1
		try
		{
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
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
		
	}
	
	public void testMoveVirtualFileLevelTwo() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String sourceFileName = "closedBefore.zip";
		IRemoteFile sourceZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFileName, sourceFileName, tempDirPath);
		assertTrue(sourceZipFile != null);
		
		//then, create a folder inside the tempDir
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		//Now, move one of the level two folder from the zip file into folder1
		try
		{
			//Now, copy one of the level two folder from the zip file into folder1
			Object[] children = fss.resolveFilterString(sourceZipFile, null, mon); //level one chidren
			Object[] levelTwoChildren = fss.resolveFilterString(children[0], null, mon);
			IRemoteFile originalVirtualFolder = (IRemoteFile)levelTwoChildren[0];
			String movedFolderName = originalVirtualFolder.getName();
			
			//copy this level two childer into folder1
			fss.move(originalVirtualFolder, folder1, originalVirtualFolder.getName(), mon);
			
			Object movedVirtualFolder = getChildFromFolder(folder1, movedFolderName);
			
			assertTrue(movedVirtualFolder != null);
			
			String[] contents = {"Connections", "Filters", "profile.xmi"};
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
			checkFolderContents((IRemoteFile)movedVirtualFolder, contents, typesToCheck);
			
			//Now, make sure the moved virtual folder is gone from its original zip file
			children = fss.resolveFilterString(sourceZipFile, null, mon);
			Object result = getChildFromFolder((IRemoteFile)children[0], movedFolderName);
			assertTrue(result == null);  //we should not be able to find it.
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
		
	}
	
	public void testMoveToArchiveFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String zipTargetFileName = "closedBefore.zip";
		
		IRemoteFile targetZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + zipTargetFileName, zipTargetFileName, tempDirPath);
		assertTrue(targetZipFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = "folderToCopy";
		IRemoteFile sourceFolder = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFolderName, sourceFolderName, tempDirPath);
		assertTrue(sourceFolder != null);
		
		//Now, copy one of the folder from the sourceFolder into copiedTargetZipFile
		try
		{
			//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.move(sourceFolder, targetZipFile, sourceFolder.getName(), mon);
			//IRemoteFile copiedVirtualFolder = fss.getRemoteFileObject(folderAbsName+"\\" + ((IRemoteFile)children[0]).getName(), mon);
			
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
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}
	
	public void testMoveToVirtualFileLevelOne() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String zipTargetFileName = "closedBefore.zip";
		
		IRemoteFile targetZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + zipTargetFileName, zipTargetFileName, tempDirPath);
		assertTrue(targetZipFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = "folderToCopy";
		IRemoteFile sourceFolder = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFolderName, sourceFolderName, tempDirPath);
		assertTrue(sourceFolder != null);
		
		
		try
		{
			//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
			//Get one of its first level children, and copy the folder to there.
			Object[] childrenOfTargetZipFile = fss.resolveFilterString(targetZipFile, null, mon);
			
			//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.move(sourceFolder, ((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolderName, mon);
			
			Object theMovedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolderName);
			
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
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}
	
	public void testMoveToVirtualFileLevelTwo() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String zipTargetFileName = "closedBefore.zip";
		
		IRemoteFile targetZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + zipTargetFileName, zipTargetFileName, tempDirPath);
		assertTrue(targetZipFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = "folderToCopy";
		IRemoteFile sourceFolder = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFolderName, sourceFolderName, tempDirPath);
		assertTrue(sourceFolder != null);
		
		
		try
		{
			//Get one of its second level children, and copy the folder to there.
			Object[] childrenOfTargetZipFile = fss.resolveFilterString(targetZipFile, null, mon);
			Object[] childrenOfTargetZipFileFirstLevel = fss.resolveFilterString(childrenOfTargetZipFile[0], null, mon);
			
			//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.move(sourceFolder, ((IRemoteFile)childrenOfTargetZipFileFirstLevel[1]), sourceFolder.getName(), mon);
			
			Object theMovedChild = getChildFromFolder((IRemoteFile)childrenOfTargetZipFileFirstLevel[1], sourceFolderName);
			
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
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
		
	}
	
	
	public void testCopyVirtualFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String sourceFileName = "closedBefore.zip";
		IRemoteFile sourceZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFileName, sourceFileName, tempDirPath);
		assertTrue(sourceZipFile != null);
		
		//then, create a folder inside the tempDir
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		//Now, copy one of the folder from the zip file into folder1
		try
		{
			Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.copy(((IRemoteFile)children[0]), folder1, ((IRemoteFile)children[0]).getName(), mon);
			
			Object copiedVirtualFolder = getChildFromFolder(folder1, (((IRemoteFile)children[0]).getName()));
			
			assertTrue(copiedVirtualFolder != null);
			
			String[] contents = {"Team", "TypeFilters", "xuanchentp", ".compatibility", ".project"};
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE, TYPE_FILE};
			checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
	}
	
	public void testCopyVirtualFileLevelTwo() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String sourceFileName = "closedBefore.zip";
		IRemoteFile sourceZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFileName, sourceFileName, tempDirPath);
		assertTrue(sourceZipFile != null);
		
		//then, create a folder inside the tempDir
		String folderName = "folder1";
		IRemoteFile folder1 = createFileOrFolder(tempDirPath, folderName, true);
		assertTrue(folder1 != null);
		
		try
		{
			//Now, copy one of the level two folder from the zip file into folder1
			Object[] children = fss.resolveFilterString(sourceZipFile, null, mon); //level one chidren
			Object[] levelTwoChildren = fss.resolveFilterString(children[0], null, mon);
			//copy this level two childer into folder1
			fss.copy(((IRemoteFile)levelTwoChildren[0]), folder1, ((IRemoteFile)levelTwoChildren[0]).getName(), mon);
			
			Object copiedVirtualFolder = getChildFromFolder(folder1, (((IRemoteFile)levelTwoChildren[0]).getName()));
			
			assertTrue(copiedVirtualFolder != null);
			
			String[] contents = {"Connections", "Filters", "profile.xmi"};
			int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER, TYPE_FILE};
			checkFolderContents((IRemoteFile)copiedVirtualFolder, contents, typesToCheck);
		}
		catch (Exception e)
		{
			fail("Problem encountered: " + e.getStackTrace().toString());
		}
		
		return;
		
	}
	
	
	public void testCopyToArchiveFile() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String zipTargetFileName = "closedBefore.zip";
		
		IRemoteFile targetZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + zipTargetFileName, zipTargetFileName, tempDirPath);
		assertTrue(targetZipFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = "folderToCopy";
		IRemoteFile sourceFolder = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFolderName, sourceFolderName, tempDirPath);
		assertTrue(sourceFolder != null);
		
		//Now, copy one of the folder from the sourceFolder into copiedTargetZipFile
		try
		{
			//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.copy(sourceFolder, targetZipFile, sourceFolder.getName(), mon);
			//IRemoteFile copiedVirtualFolder = fss.getRemoteFileObject(folderAbsName+"\\" + ((IRemoteFile)children[0]).getName(), mon);
			
			Object theCopiedChild = getChildFromFolder(targetZipFile, sourceFolderName);
			
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
	
	
	
	public void testCopyToVirtualFileLevelOne() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String zipTargetFileName = "closedBefore.zip";
		
		IRemoteFile targetZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + zipTargetFileName, zipTargetFileName, tempDirPath);
		assertTrue(targetZipFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = "folderToCopy";
		IRemoteFile sourceFolder = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFolderName, sourceFolderName, tempDirPath);
		assertTrue(sourceFolder != null);
		
		
		try
		{
			//Now, copy one of the folder from the sourceFolder into a first level virtual file in targetZipFile
			//Get one of its first level children, and copy the folder to there.
			Object[] childrenOfTargetZipFile = fss.resolveFilterString(targetZipFile, null, mon);
			
			//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.copy(sourceFolder, ((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolder.getName(), mon);
			
			Object theCopiedChild = getChildFromFolder(((IRemoteFile)childrenOfTargetZipFile[0]), sourceFolderName);
			
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
	

	public void testCopyToVirtualFileLevelTwo() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String zipTargetFileName = "closedBefore.zip";
		
		IRemoteFile targetZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + zipTargetFileName, zipTargetFileName, tempDirPath);
		assertTrue(targetZipFile != null);
		
		//Now, copy the source folder.
		String sourceFolderName = "folderToCopy";
		IRemoteFile sourceFolder = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFolderName, sourceFolderName, tempDirPath);
		assertTrue(sourceFolder != null);
		
		
		try
		{
			//Get one of its second level children, and copy the folder to there.
			Object[] childrenOfTargetZipFile = fss.resolveFilterString(targetZipFile, null, mon);
			Object[] childrenOfTargetZipFileFirstLevel = fss.resolveFilterString(childrenOfTargetZipFile[0], null, mon);
			
			//Object[] children = fss.resolveFilterString(sourceZipFile, null, mon);
			fss.copy(sourceFolder, ((IRemoteFile)childrenOfTargetZipFileFirstLevel[1]), sourceFolder.getName(), mon);
			
			Object theCopiedChild = getChildFromFolder((IRemoteFile)childrenOfTargetZipFileFirstLevel[1], sourceFolderName);
			
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
	
	public void testDeleteVirtualFileBigZip() throws SystemMessageException {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//copy the zip file first.
		String sourceFileName = "eclipse-SDK-3.3M6-win32.zip";
		IRemoteFile sourceZipFile = copySourceFileOrFolder(ZIP_SOURCE_DIR + sourceFileName, sourceFileName, tempDirPath);
		assertTrue(sourceZipFile != null);
		
		//Get the "features" directory under "eclipse"
		String directoryName ="eclipse";
		IRemoteFile parentDirectory = (IRemoteFile)getChildFromFolder(sourceZipFile, directoryName);
		assertTrue(parentDirectory != null);
		directoryName = "features";
		parentDirectory = (IRemoteFile)getChildFromFolder(parentDirectory, directoryName);
		//Now, we want to delete a folder (with really long name) inside this "feature" directory.
		directoryName = "org.eclipse.platform.source_3.3.0.v20070316b-_-9MEhFxiXZ2gosKM6M-3_Hu9MqL";
		IRemoteFile directoryToDelete = (IRemoteFile)getChildFromFolder(parentDirectory, directoryName);
		assertTrue(directoryToDelete != null);
		//Now, delete this directory
		fss.delete(directoryToDelete, mon);
		directoryToDelete = (IRemoteFile)getChildFromFolder(parentDirectory, directoryName);
		assertTrue(directoryToDelete == null);
		
		//check result of this operation
		String[] namesToCheck = {"org.eclipse.cvs.source_1.0.0.v20070202-7B79_71CI99g_LDV2411", 
				                 "org.eclipse.cvs_1.0.0.v20070202-7B79_71CI99g_LDV2411", 
				                 "org.eclipse.jdt.source_3.3.0.v20070319-1800-7n7dEAWEgDfeRYHuyAaFTR",
				                 "org.eclipse.jdt_3.3.0.v20070319-1800-7n7dEAWEgDfeRYHuyAaFTR",
				                 "org.eclipse.pde.source_3.3.0.v20061114-0800-7M7M-6NUEF6EbRV6EWGC",
				                 "org.eclipse.pde_3.3.0.v20061114-0800-7M7M-6NUEF6EbRV6EWGC",
				                 "org.eclipse.platform_3.3.0.v20070316b-_-9MEhFxiXZ2gosKM6M-3_Hu9MqL",
				                 "org.eclipse.rcp.source_3.3.0.v20070316b-8l8VCEYXUPdNkSgt8LpKG",
				                 "org.eclipse.rcp_3.3.0.v20070316b-8l8VCEYXUPdNkSgt8LpKG",
				                 "org.eclipse.sdk_3.3.0.v20070319-7L7J-4DjllPLcCcxrZDgaddnelet"};
		int[] typesToCheck = {TYPE_FOLDER, TYPE_FOLDER,TYPE_FOLDER,TYPE_FOLDER,TYPE_FOLDER, 
				              TYPE_FOLDER, TYPE_FOLDER,TYPE_FOLDER,TYPE_FOLDER,TYPE_FOLDER};
		checkFolderContents(parentDirectory, namesToCheck, typesToCheck);
		
		//Now, delete some files and folder inside the a virtual folder.
		parentDirectory = (IRemoteFile)getChildFromFolder(parentDirectory, "org.eclipse.rcp_3.3.0.v20070316b-8l8VCEYXUPdNkSgt8LpKG");
		assertTrue(parentDirectory != null);
		
		String fileToDelete = "eclipse_update_120.jpg";
		IRemoteFile levelThreeFileToDelete = (IRemoteFile)getChildFromFolder(parentDirectory, fileToDelete);
		assertTrue(levelThreeFileToDelete != null);
		
		String directoryNameToDelete = "META-INF";
		IRemoteFile levelThreeDirectoryToDelete = (IRemoteFile)getChildFromFolder(parentDirectory, directoryNameToDelete);
		assertTrue(levelThreeDirectoryToDelete != null);
		
		//delete file first
		fss.delete(levelThreeFileToDelete, mon);
		
		fss.delete(levelThreeDirectoryToDelete, mon);
		
		//check the result
		String[] level3NamesToCheck = {"epl-v10.html",
                						"feature.properties",
                						"feature.xml",
                						"license.html"};
		int[] level3TypesToCheck = {TYPE_FILE, TYPE_FILE, TYPE_FILE, TYPE_FILE};
		checkFolderContents(parentDirectory, level3NamesToCheck, level3TypesToCheck);
		
		return;
		
	}

}
