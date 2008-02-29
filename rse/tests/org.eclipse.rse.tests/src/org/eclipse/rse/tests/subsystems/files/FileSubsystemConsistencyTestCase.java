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
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.connection.IRSEConnectionProperties;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * Test cases for comparing various file subsystem operations
 */
public class FileSubsystemConsistencyTestCase extends RSEBaseConnectionTestCase {
	
	private List _subSystems;
	private List _connections;
	private List _samplePaths;
	
	private String LOCALTEMPDIR = "C:\\temp";
	
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
		}
		
		if (_connections != null)
		{
			for (int j = 0; j < _connections.size(); j++) {
				IHost host = (IHost)_connections.get(j);
				if (host != null) {
					getConnectionManager().removeConnection(host.getSystemProfileName(), host.getName());
				}
			}
		}

		_subSystems.clear();
		_connections.clear();
		_subSystems = null;
		_connections = null;
		
		super.tearDown();
	}
	

	
	protected void setupConnections() {
		if (_connections == null)
		{
			_connections = new ArrayList();
			_subSystems = new ArrayList();
			
			// setup dstore connection
			addSystem(getDStoreHost());				
			
			// setup ssh connection
			addSystem(getSSHHost());
			
			// setup ftp connection
			addSystem(getFTPHost());		
		
			
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

	protected IHost getSSHHost()
	{
		IHost sshHost = null;

		// Calculate the location of the test connection properties
		IPath location = getTestDataLocation("", false); //$NON-NLS-1$
		assertNotNull("Cannot locate test data! Missing test data location?", location); //$NON-NLS-1$
		location = location.append("sshConnection.properties"); //$NON-NLS-1$
		assertNotNull("Failed to construct location to 'connection.properties' test data file!", location); //$NON-NLS-1$
		assertTrue("Required test data file seems to be not a file!", location.toFile().isFile()); //$NON-NLS-1$
		assertTrue("Required test data file is not readable!", location.toFile().canRead()); //$NON-NLS-1$
		
		// Load the properties from the calculated location without backing up defaults
		IRSEConnectionProperties properties = getConnectionManager().loadConnectionProperties(location, false);
		assertNotNull("Failed to load test connection properties from location " + location.toOSString(), properties); //$NON-NLS-1$
		
		// Lookup and create the connection now if necessary
		sshHost = getConnectionManager().findOrCreateConnection(properties);
		assertNotNull("Failed to create connection " + properties.getProperty(IRSEConnectionProperties.ATTR_NAME), sshHost); //$NON-NLS-1$

		return sshHost;
	}
	
	protected IHost getFTPHost()
	{
		IHost ftpHost = null;

		// Calculate the location of the test connection properties
		IPath location = getTestDataLocation("", false); //$NON-NLS-1$
		assertNotNull("Cannot locate test data! Missing test data location?", location); //$NON-NLS-1$
		location = location.append("ftpConnection.properties"); //$NON-NLS-1$
		assertNotNull("Failed to construct location to 'connection.properties' test data file!", location); //$NON-NLS-1$
		assertTrue("Required test data file seems to be not a file!", location.toFile().isFile()); //$NON-NLS-1$
		assertTrue("Required test data file is not readable!", location.toFile().canRead()); //$NON-NLS-1$
		
		// Load the properties from the calculated location without backing up defaults
		IRSEConnectionProperties properties = getConnectionManager().loadConnectionProperties(location, false);
		assertNotNull("Failed to load test connection properties from location " + location.toOSString(), properties); //$NON-NLS-1$
		
		// Lookup and create the connection now if necessary
		ftpHost = getConnectionManager().findOrCreateConnection(properties);
		assertNotNull("Failed to create connection " + properties.getProperty(IRSEConnectionProperties.ATTR_NAME), ftpHost); //$NON-NLS-1$
		
		return ftpHost;
	}
	
	protected IHost getDStoreHost()
	{
		IHost dstoreHost = null;
		
		//Ensure that the SSL acknowledge dialog does not show up. 
		//We need to setDefault first in order to set the value of a preference.  
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(ISystemPreferencesConstants.ALERT_SSL, ISystemPreferencesConstants.DEFAULT_ALERT_SSL);
		store.setDefault(ISystemPreferencesConstants.ALERT_NONSSL, ISystemPreferencesConstants.DEFAULT_ALERT_NON_SSL);

		store.setValue(ISystemPreferencesConstants.ALERT_SSL, false);
		store.setValue(ISystemPreferencesConstants.ALERT_NONSSL, false);

		// Calculate the location of the test connection properties
		IPath location = getTestDataLocation("", false); //$NON-NLS-1$
		assertNotNull("Cannot locate test data! Missing test data location?", location); //$NON-NLS-1$
		location = location.append("linuxConnection.properties"); //$NON-NLS-1$
		assertNotNull("Failed to construct location to 'connection.properties' test data file!", location); //$NON-NLS-1$
		assertTrue("Required test data file seems to be not a file!", location.toFile().isFile()); //$NON-NLS-1$
		assertTrue("Required test data file is not readable!", location.toFile().canRead()); //$NON-NLS-1$
		
		// Load the properties from the calculated location without backing up defaults
		IRSEConnectionProperties properties = getConnectionManager().loadConnectionProperties(location, false);
		assertNotNull("Failed to load test connection properties from location " + location.toOSString(), properties); //$NON-NLS-1$
		
		// Lookup and create the connection now if necessary
		dstoreHost = getConnectionManager().findOrCreateConnection(properties);
		assertNotNull("Failed to create connection " + properties.getProperty(IRSEConnectionProperties.ATTR_NAME), dstoreHost); //$NON-NLS-1$
		
		return dstoreHost;
	}
	


	/**
	 * Test the implicit connect of each connection when calling getRemoteFileObject().
	 */
	public void testImplicitConnectViaFileSubSystem() {
		//-test-author-:DaveMcKnight
		if (!RSETestsPlugin.isTestCaseEnabled("FileSubsystemConsistencyTestCase.testImplicitConnectViaFileSubSystem")) return; //$NON-NLS-1$
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileSubsystemConsistencyTestCase.testSingleFileQuery")) return; //$NON-NLS-1$
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileSubsystemConsistencyTestCase.testMultiFileQuery")) return; //$NON-NLS-1$
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
		if (!RSETestsPlugin.isTestCaseEnabled("FileSubsystemConsistencyTestCase.testSingleFileDownload")) return; //$NON-NLS-1$
		setupConnections();
		internalFileDownload(false);
	}
	
	/**
	 * Test the multi file download
	 */
	public void testMultiFileDownload() {
		//-test-author-:DaveMcKnight
		if (!RSETestsPlugin.isTestCaseEnabled("FileSubsystemConsistencyTestCase.testMultiFileDownload")) return; //$NON-NLS-1$
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
	
}




