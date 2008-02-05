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
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.tests.RSETestsPlugin;

public class FileServiceArchiveTest extends FileServiceBaseTest {

	protected String folderToCopyName1 = "RemoteSystemsConnections";
	protected String folderToCopyName2 = "6YLT5Xa";
	protected String folderToCopyName3 = "folderToCopy";
	
	protected String zipSourceFileName1 = "closedBefore.zip";
	protected String zipSourceFileName2 = "mynewzip.zip";
	
	protected String tarSourceFileName1 = "source.tar";
	protected String tarSourceFileName2 = "mynewtar.tar";
	
	protected String tarSourceFolderName1 = "META-INF";
	protected String tarSourceFolderName2 = "org";
	
	protected String tarSourceForOpenTest = "tarSourceForOpen.tar";
	protected String tarSourceForOpenFolderName1 = "META-INF";
	protected String tarSourceForOpenFolderName2 = "org";
	
	protected String fileContentString1 = "this is just some dummy content \n to a remote file \n to test an open operation";
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
		UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)tempObjects3, tempDir, mon, true);
		
		//Then, we need to retrieve children of the tempDir to cache their information.
		fss.resolveFilterString(tempDir, null, mon);
		
		//Then, delete the temp folder in the junit workspace.
		temp.delete(EFS.NONE, mon);
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//Create the zip file first.
		String testName = "dummy.tar";
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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

	public void testCopyToTarVirtualFileLevelOne() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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

	public void testCopyTarVirtualFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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

	public void testCopyTarVirtualFileLevelFour() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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

	public void testMoveToTarArchiveFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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

	public void testMoveToTarVirtualFileLevelOne() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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

	public void testMoveTarVirtualFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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

	public void testMoveTarVirtualFileLevelFour() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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

	public void testRenameTarVirtualFile() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
		//Create the zip file first.
		String testName = "source.tar";
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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



	public void testCopyBatchToTarVirtualFileLevelFour() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
	



	
	
	public void testOpenFileFromTarArchive() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("FileServiceTest.testCreateFile")) return; //$NON-NLS-1$
		
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
		String localPath = tempPath + "\\" + fileContentToVerifyName1;
		fss.download(thisVirtualFile, localPath, thisVirtualFile.getEncoding(), mon);
		
		//now, verify the content of the local file
		IFileStore localFile = temp.getChild(fileContentToVerifyName1);
		//Check the content of the download file:
		boolean sameContent = compareContent(getContents(fileContentString1), localFile.openInputStream(EFS.NONE, null));
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
		localPath = tempPath + "\\" + fileContentToVerifyName2;
		fss.download(thisVirtualFile, localPath, thisVirtualFile.getEncoding(), mon);
		
		//now, verify the content of the local file
		localFile = temp.getChild(fileContentToVerifyName2);
		//Check the content of the download file:
		sameContent = compareContent(getContents(fileContentString1), localFile.openInputStream(EFS.NONE, null));
		assertTrue(sameContent);
	}
	



}
