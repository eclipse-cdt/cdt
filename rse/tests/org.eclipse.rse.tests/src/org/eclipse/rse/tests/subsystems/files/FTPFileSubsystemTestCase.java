/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.services.files.ftp.FTPService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.ftp.FTPFileSubSystemConfiguration;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.connection.IRSEConnectionProperties;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * Test cases for FTP based remote host access.
 */
public class FTPFileSubsystemTestCase extends RSEBaseConnectionTestCase {
	private ISubSystem subSystem;
	private IHost connection;
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		if (subSystem != null && subSystem.isConnected()) {
			subSystem.disconnect(true);
		}
		if (connection != null) {
			getConnectionManager().removeConnection(connection.getSystemProfileName(), connection.getName());
		}

		subSystem = null;
		connection = null;
		
		super.tearDown();
	}

	/**
	 * Test the FTP read access via ftp://ftp.suse.com
	 */
	public void testFTPAccessToHost_ftp_suse_com() {
		if (!RSETestsPlugin.isTestCaseEnabled("FTPFileSubsystemTestCase.testFTPAccessToHost_ftp_suse_com")) return; //$NON-NLS-1$
		
		ISystemRegistry systemRegistry = RSEUIPlugin.getTheSystemRegistry();
		assertNotNull("Failed to get RSE system registry instance!", systemRegistry); //$NON-NLS-1$
		
		// Calculate the location of the test connection properties
		IPath location = getTestDataLocation("testFTPAccessToHost_ftp_suse_com", false); //$NON-NLS-1$
		assertNotNull("Cannot locate test data! Missing test data location?", location); //$NON-NLS-1$
		location = location.append("connection.properties"); //$NON-NLS-1$
		assertNotNull("Failed to construct location to 'connection.properties' test data file!", location); //$NON-NLS-1$
		assertTrue("Required test data file seems to be not a file!", location.toFile().isFile()); //$NON-NLS-1$
		assertTrue("Required test data file is not readable!", location.toFile().canRead()); //$NON-NLS-1$
		
		// Load the properties from the calculated location without backing up defaults
		IRSEConnectionProperties properties = getConnectionManager().loadConnectionProperties(location, false);
		assertNotNull("Failed to load test connection properties from location " + location.toOSString(), properties); //$NON-NLS-1$
		
		// Lookup and create the connection now if necessary
		connection = getConnectionManager().findOrCreateConnection(properties);
		assertNotNull("Failed to create connection " + properties.getProperty(IRSEConnectionProperties.ATTR_NAME), connection); //$NON-NLS-1$
		// expand the connection in the UI
		systemRegistry.expandHost(connection);
		
		Exception exception = null;
		String cause = null;
		
		subSystem = null;
		try {
			subSystem = getConnectionManager().getFileSubSystem(connection, "ftp.files"); //$NON-NLS-1$
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to get ftp.files subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertNotNull("No ftp.files subystem", subSystem); //$NON-NLS-1$

		// we expect that the subsystem is not connected yet
		assertFalse("ftp.files subsystem is unexpectedly connected!", subSystem.isConnected()); //$NON-NLS-1$
		try {
			subSystem.connect();
		} catch(Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to connect ftp.files subsystem to host " + properties.getProperty(IRSEConnectionProperties.ATTR_NAME) + "! Possible cause: " + cause, exception); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Failed to connect ftp.files subsystem to host " + properties.getProperty(IRSEConnectionProperties.ATTR_NAME) + "! Unknown cause!", subSystem.isConnected()); //$NON-NLS-1$ //$NON-NLS-2$
		// expand the subsystem
		systemRegistry.expandSubSystem(subSystem);
		
		ISubSystemConfiguration configuration = systemRegistry.getSubSystemConfiguration(subSystem);
		assertNotNull("Failed to get ftp.files subsystem configuration instance!", configuration); //$NON-NLS-1$
		
		// The ftp.files subsystem supports filtering, therefor ISubSystem.getChildren() is expected
		// to return a non null value.
		assertTrue("Unexpected return value false for ftp.files subsystem configuration supportFilters()!", configuration.supportsFilters()); //$NON-NLS-1$
		assertNotNull("Unexpected return value null for ftp.files subsystem getChildren()!", subSystem.getChildren()); //$NON-NLS-1$
		
		// get access to the services
		assertTrue("ftp.files subsystem configuration instance is not of expected type FileServiceSubSystemConfiguration!", configuration instanceof FTPFileSubSystemConfiguration); //$NON-NLS-1$
		FTPFileSubSystemConfiguration ftpConfiguration = (FTPFileSubSystemConfiguration)configuration;
		IFileService service = ftpConfiguration.getFileService(connection);
		assertNotNull("Failed to get IFileService instance from ftp.files subsystem configuration!", service); //$NON-NLS-1$
		assertTrue("IFileService instance is not of expected type FTPService!", service instanceof FTPService); //$NON-NLS-1$
		FTPService ftpService = (FTPService)service;
		
		// now we have the service reference and can start reading things from the server
		IHostFile[] roots = ftpService.getRoots(new NullProgressMonitor());
		assertNotNull("Failed to get root nodes from ftp.files service!", roots); //$NON-NLS-1$
		
		FTPClient ftpClient = ftpService.getFTPClient();
		assertNotNull("Failed to get FTPClient instance!", ftpClient); //$NON-NLS-1$
		
		exception = null;
		cause = null;
		
		FTPFile[] files = null;
		try {
			files = ftpClient.listFiles();
		} catch (IOException e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull("Failed to list the files from ftp server " + properties.getProperty(IRSEConnectionProperties.ATTR_NAME) + "! Possible cause: " + cause, exception); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull("Unexpected return value null for FTPClient.listFiles()!", files); //$NON-NLS-1$
	}
}
