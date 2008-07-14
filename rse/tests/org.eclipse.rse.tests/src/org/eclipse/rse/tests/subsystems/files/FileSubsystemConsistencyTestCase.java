/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David McKnight   (IBM)        - [207095] test case to compare same op between subsystems
 * David McKnight   (IBM)        - [162195] new APIs for upload multi and download multi
 * David McKnight   (IBM)        - [209552] API changes to use multiple and getting rid of deprecated
 * David McKnight   (IBM)        - [210109] store constants in IFileService rather than IFileServiceConstants
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 * Martin Oberhuber (Wind River) - [240729] More flexible disabling of testcases
 * Martin Oberhuber (Wind River) - [240704] Protect against illegal API use of getRemoteFileObject() with relative path as name
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

/**
 * Test cases for comparing various file subsystem operations
 */
public class FileSubsystemConsistencyTestCase extends RSEBaseConnectionTestCase {

	private List _subSystems;
	private List _connections;
	private List _samplePaths;

	private String LOCALTEMPDIR = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		if (_subSystems != null) {
			for (int i = 0; i < _subSystems.size(); i++) {
				IRemoteFileSubSystem ss = (IRemoteFileSubSystem)_subSystems.get(i);
				if (ss != null && ss.isConnected()) {
					ss.disconnect(true);
				}

			}
			_subSystems.clear();
		}

		if (_connections != null)
		{
			for (int j = 0; j < _connections.size(); j++) {
				IHost host = (IHost)_connections.get(j);
				if (host != null) {
					getConnectionManager().removeConnection(host.getSystemProfileName(), host.getName());
				}
			}
			_connections.clear();
		}

		_subSystems = null;
		_connections = null;

		super.tearDown();
	}



	protected void setupConnections() {
		if (_connections == null)
		{
			_connections = new ArrayList();
			_subSystems = new ArrayList();

			//TODO Support Windows style connections
			//String[] connTypes = { "local", "ssh", "ftpWindows", "ftp", "linux", "windows" };
			String[] connTypes = { "ssh", "ftp", "linux" };
			for (int i = 0; i < connTypes.length; i++) {
				setTargetName(connTypes[i]);
				if (!isTestDisabled()) {
					addSystem(getHost(connTypes[i] + "Connection.properties"));
				}
			}
			setTargetName(null);

			_samplePaths = new ArrayList();
			_samplePaths.add("/usr");
			_samplePaths.add("/usr/lib");
			_samplePaths.add("/usr/bin");
			_samplePaths.add("/bin");
			_samplePaths.add("/etc");
			_samplePaths.add("/home");
			_samplePaths.add("/sbin");

		}
	}

	private void addSystem(IHost host) {
		IRemoteFileSubSystem fss = null;
		ISystemRegistry sr = SystemStartHere.getSystemRegistry();
		ISubSystem[] ss = sr.getServiceSubSystems(host, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IRemoteFileSubSystem) {
				fss = (IRemoteFileSubSystem)ss[i];
			}
		}
		_subSystems.add(fss);
		_connections.add(host);
	}

	/**
	 * Test the implicit connect of each connection when calling getRemoteFileObject().
	 */
	public void testImplicitConnectViaFileSubSystem() {
		//-test-author-:DaveMcKnight
		if (isTestDisabled())
			return;
		setupConnections();

		String testPath = "/usr/lib";

		for (int i = 0; i < _subSystems.size(); i++) {
			IRemoteFileSubSystem ss = (IRemoteFileSubSystem)_subSystems.get(i);

			// ensure that the system is NOT connected
			if (ss.isConnected()) {
				try {
					ss.disconnect(true);
				}
				catch (Exception e) {
					// disconnect failed
				}
			}



			String systemType = ss.getConfigurationId();

			Exception exception = null;
			String cause = null;
			IRemoteFile remoteFile = null;

			try {
				remoteFile = ss.getRemoteFileObject(testPath, new NullProgressMonitor());
			}
			catch (Exception e){
				exception = e;
				cause = e.getLocalizedMessage();
			}

			assertNull(systemType + ":Exception getting remote file! Possible cause: " + cause, exception); //$NON-NLS-1$
			assertTrue(ss.isConnected());
			assertNotNull(systemType + ":Unexpected return value for getRemoteFile().  Remote file is null!", remoteFile);
		}
	}
	public void testSingleFileQuery() {
		//-test-author-:DaveMcKnight
		if (isTestDisabled())
			return;
		setupConnections();


		String[] testPaths = (String[])_samplePaths.toArray(new String[_samplePaths.size()]);

		for (int i = 0; i < _subSystems.size(); i++) {
			IRemoteFileSubSystem ss = (IRemoteFileSubSystem)_subSystems.get(i);

			// ensure that the system is connected
			if (!ss.isConnected()) {
				try {
					ss.connect(new NullProgressMonitor(), false);
				}
				catch (Exception e) {
					// connect failed
				}
			}

			String systemType = ss.getConfigurationId();

			Exception exception = null;
			String cause = null;
			IRemoteFile[] remoteFiles = new IRemoteFile[testPaths.length];

			long t1 = System.currentTimeMillis();
			for (int f = 0; f < testPaths.length; f++)
			{
				try
				{
					remoteFiles[f] = ss.getRemoteFileObject(testPaths[f], new NullProgressMonitor());
				}
				catch (Exception e){
					exception = e;
					cause = e.getLocalizedMessage();
				}
			}

			long t2 = System.currentTimeMillis();

			System.out.println(systemType + ": get files time = "+ (t2 - t1) + " milliseconds");

			// query folders
			IRemoteFile[] results = null;
			List consolidatedResults = new ArrayList();
			long t3 = System.currentTimeMillis();
			for (int q = 0; q < remoteFiles.length; q++)
			{
				try
				{
					IRemoteFile[] children = ss.list(remoteFiles[q], IFileService.FILE_TYPE_FILES_AND_FOLDERS, new NullProgressMonitor());
					for (int c = 0; c < children.length; c++)
					{
						consolidatedResults.add(children[c]);
					}
				}
				catch (Exception e){
					exception = e;
					cause = e.getLocalizedMessage();
				}
			}
			results = (IRemoteFile[])consolidatedResults.toArray(new IRemoteFile[consolidatedResults.size()]);
			long t4 = System.currentTimeMillis();

			System.out.println(systemType + ": query time = "+ (t4 - t3) + " milliseconds");

			assertNull(systemType + ":Exception getting remote files! Possible cause: " + cause, exception); //$NON-NLS-1$
			assertTrue(ss.isConnected());

			System.out.println(systemType + ": results size="+results.length);
			/*
			for (int r = 0; r < remoteFiles.length; r++)
			{
				IRemoteFile rfile = remoteFiles[r];
				boolean exists = rfile.exists();
				if (!exists){
					System.out.println(rfile.getAbsolutePath() + " doesn't exist!");
				}
				assertTrue(exists);
			}
			*/

		}
	}

	/**
	 * Test the multi file query
	 */
	public void testMultiFileQuery() {
		//-test-author-:DaveMcKnight
		if (isTestDisabled())
			return;
		setupConnections();



		String[] testPaths = (String[])_samplePaths.toArray(new String[_samplePaths.size()]);

		for (int i = 0; i < _subSystems.size(); i++) {
			IRemoteFileSubSystem ss = (IRemoteFileSubSystem)_subSystems.get(i);

			// ensure that the system is connected
			if (!ss.isConnected()) {
				try {
					ss.connect(new NullProgressMonitor(), false);
				}
				catch (Exception e) {
					// connect failed
				}
			}

			String systemType = ss.getConfigurationId();

			Exception exception = null;
			String cause = null;
			IRemoteFile[] remoteFiles = null;

			// get folders to query
			long t1 = System.currentTimeMillis();
			try
			{
				remoteFiles = ss.getRemoteFileObjects(testPaths, new NullProgressMonitor());
			}
			catch (Exception e){
				exception = e;
				e.printStackTrace();
				cause = e.getLocalizedMessage();
			}

			long t2 = System.currentTimeMillis();

			System.out.println(systemType + ": get files time = "+ (t2 - t1) + " milliseconds");

			// query folders
			IRemoteFile[] results = null;
			long t3 = System.currentTimeMillis();
			try
			{
				results = ss.listMultiple(remoteFiles, IFileService.FILE_TYPE_FILES_AND_FOLDERS, new NullProgressMonitor());
			}
			catch (Exception e){
				exception = e;
				e.printStackTrace();
				cause = e.getLocalizedMessage();
			}
			long t4 = System.currentTimeMillis();

			System.out.println(systemType + ": query time = "+ (t4 - t3) + " milliseconds");

			assertNull(systemType + ":Exception getting remote files! Possible cause: " + cause, exception); //$NON-NLS-1$
			assertTrue(ss.isConnected());

			System.out.println(systemType + ":results size="+results.length);
		}
	}

	/**
	 * Test the single file download
	 */
	public void testSingleFileDownload() {
		//-test-author-:DaveMcKnight
		if (isTestDisabled())
			return;
		setupConnections();
		internalFileDownload(false);
	}

	/**
	 * Test the multi file download
	 */
	public void testMultiFileDownload() {
		//-test-author-:DaveMcKnight
		if (isTestDisabled())
			return;
		setupConnections();
		internalFileDownload(true);
	}

	protected void internalFileDownload(boolean multi)
	{
		String remoteParentDir = "/usr/include";
		File tempDir = new File(LOCALTEMPDIR);
		if (!tempDir.exists())
		{
			tempDir.mkdirs();
		}

		for (int i = 0; i < _subSystems.size(); i++) {
			IRemoteFileSubSystem ss = (IRemoteFileSubSystem)_subSystems.get(i);

			// ensure that the system is connected
			if (!ss.isConnected()) {
				try {
					ss.connect(new NullProgressMonitor(), false);
				}
				catch (Exception e) {
					// connect failed
				}
			}

			String systemType = ss.getConfigurationId();

			File subTempDir = new File(tempDir, systemType + (multi ? "_multi" : "_single"));
			if (subTempDir.exists())
			{
				// delete old contents
				try
				{
					String[] children = subTempDir.list();
					for (int c = 0; c < children.length; c++)
					{
						new File(children[c]).delete();
					}
				}
				catch (Exception e)
				{
					/*ignore*/
				}
			}
			else
			{
				subTempDir.mkdirs();
			}

			Exception exception = null;
			String cause = null;
			//IRemoteFile[] remoteFiles = null;

			try
			{
				IProgressMonitor monitor = new NullProgressMonitor();
				IRemoteFile includeDir = ss.getRemoteFileObject(remoteParentDir, monitor);

				// get all the files
				IRemoteFile[] files = ss.list(includeDir, IFileService.FILE_TYPE_FILES, monitor);

				System.out.println(systemType + ": downloading "+files.length+ " files");


				// determine local locations for each
				String[] destinations = new String[files.length];
				String[] encodings = new String[files.length];
				long[] fileSizes = new long[files.length];



				for (int d = 0; d < files.length; d++)
				{
					IRemoteFile file = files[d];
					destinations[d] = subTempDir.getAbsolutePath() + File.separatorChar + file.getName();
					encodings[d] = file.getEncoding();
					fileSizes[d] = file.getLength();
				}

				long t1 = System.currentTimeMillis();
				if (multi) // multi file download
				{
					System.out.println(systemType + ":Starting multi-file Download");

					// transfer the files
					ss.downloadMultiple(files, destinations, encodings, monitor);
				}
				else // single file download
				{
					System.out.println(systemType + ":Starting single file Download");

					for (int s = 0; s < files.length; s++)
					{
						// transfer the files
						ss.download(files[s], destinations[s], encodings[s], monitor);
					}
				}
				long t2 = System.currentTimeMillis();
				System.out.println(systemType + ": download time = "+ (t2 - t1) + " milliseconds");


				assertNull(systemType + ":Exception getting remote files! Possible cause: " + cause, exception); //$NON-NLS-1$
				assertTrue(ss.isConnected());

				// examine results
				for (int r = 0; r < destinations.length; r++)
				{
					// check results and compare their sizes
					long expectedSize = fileSizes[r];

					File destination = new File(destinations[r]);
					long actualSize = destination.length();

					boolean goodDownload = expectedSize == actualSize;

					if (!goodDownload)
					{
						System.out.println("bad download of "+ destination.getAbsolutePath());
						System.out.println("expected size:"+expectedSize);
						System.out.println("actual size:"+actualSize);
					}
					assertTrue(goodDownload);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	public void testGetRemoteFileRelativePath() throws Exception {
		// for bug 240704
		// -test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		setupConnections();
		for (int i = 0; i < _subSystems.size(); i++) {
			IRemoteFileSubSystem ss = (IRemoteFileSubSystem) _subSystems.get(i);
			ss.checkIsConnected(getDefaultProgressMonitor());
			IRemoteFile homeDir = ss.getRemoteFileObject(".", getDefaultProgressMonitor());
			assertTrue(homeDir.exists());
			assertTrue(homeDir.isDirectory());
			String sep = homeDir.getSeparator();
			String relativePath = "rsetest" + System.currentTimeMillis() + sep + "foo" + sep + "bar";
			try {
				IRemoteFile subDir = ss.getRemoteFileObject(homeDir, relativePath, getDefaultProgressMonitor());
				assertTrue(subDir.isDescendantOf(homeDir));
				assertEquals("bar", subDir.getName());
				assertFalse(subDir.exists());
				assertFalse(subDir.isDirectory());
			} catch (IllegalArgumentException e) {
				// Expected here: IllegalArgumentException is OK
			}
		}
	}

}
