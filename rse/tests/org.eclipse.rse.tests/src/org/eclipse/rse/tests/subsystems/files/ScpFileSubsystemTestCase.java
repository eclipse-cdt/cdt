/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David McKnight   (IBM)        - [186363] get rid of obsolete calls to SubSystem.connect()
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 * Anna Dushistova  (MontaVista) - adapted from FTPFileSubSystemTestCase
 *******************************************************************************/

package org.eclipse.rse.tests.subsystems.files;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.tests.core.RSEWaitAndDispatchUtil;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

public class ScpFileSubsystemTestCase extends RSEBaseConnectionTestCase {

	private ISubSystem subSystem;
	private IHost connection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		if (subSystem != null && subSystem.isConnected()) {
			subSystem.disconnect(true);
		}
		if (connection != null) {
			getConnectionManager().removeConnection(
					connection.getSystemProfileName(), connection.getName());
		}

		subSystem = null;
		connection = null;

		super.tearDown();
	}

	/**
	 * Test the FTP read access to a real remote FTP host.
	 */
	public void testScpAccessToRemoteHost() {
		// -test-author-:UweStieber
		if (isTestDisabled())
			return;

		String propFileName = "scpConnection.properties";

		Exception exception = null;
		String cause = null;

		subSystem = null;
		try {
			subSystem = getScpSubSystem(propFileName);
		} catch (Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull(
				"Failed to get scp.files subsystem! Possible cause: " + cause, exception); //$NON-NLS-1$
		assertNotNull("No scp.files subystem", subSystem); //$NON-NLS-1$

		ISubSystemConfiguration configuration = subSystem
				.getSubSystemConfiguration();
		assertNotNull(
				"Failed to get scp.files subsystem configuration instance!", configuration); //$NON-NLS-1$

		try {
			subSystem.connect(false, null);
		} catch (Exception e) {
			exception = e;
			cause = e.getLocalizedMessage();
		}
		assertNull(
				"Failed to connect scp.files subsystem to host " + "! Possible cause: " + cause, exception); //$NON-NLS-1$ //$NON-NLS-2$

		// Wait hard-coded 10 seconds to get around asynchronous connection
		// problems.
		RSEWaitAndDispatchUtil.waitAndDispatch(10000);

		// if we could not connect in 10 sec. we give up here. The server might
		// be not reachable
		// or exceeded the max number of connection or ... or ... or ... Just do
		// not fail in this case.
		if (!subSystem.isConnected())
			return;
		FileServiceSubSystem inputFss = (FileServiceSubSystem) subSystem;
		IRemoteFile homeDirectory;
		try {
			homeDirectory = inputFss.getRemoteFileObject(".",
					new NullProgressMonitor());
			String baseFolderName = "rsetest";
			String homeFolderName = homeDirectory.getAbsolutePath();
			String testFolderName = FileServiceHelper.getRandomLocation(
					inputFss, homeFolderName, baseFolderName,
					new NullProgressMonitor());
			IHostFile testDir = inputFss.getFileService()
					.createFolder(
							homeFolderName,
							testFolderName.substring(testFolderName
									.lastIndexOf("/") + 1),
							new NullProgressMonitor());
			assertTrue(testDir != null);
			IHostFile hostfile = inputFss.getFileService().createFile(
					testFolderName, "test-scp.txt", new NullProgressMonitor());
			assertTrue(hostfile != null);
			inputFss.getFileService().delete(testFolderName, "test-scp.txt",
					new NullProgressMonitor());
			inputFss.getFileService()
					.delete(testFolderName,
							testFolderName.substring(testFolderName
									.lastIndexOf("/") + 1),
							new NullProgressMonitor());
		} catch (SystemMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected ISubSystem getScpSubSystem(String propertiesFileName)
			throws Exception {
		if (propertiesFileName == null) {
			return null;
		}
		IHost host = getHost(propertiesFileName);

		return getConnectionManager().getFileSubSystem(host, "scp.files"); //$NON-NLS-1$
	}
}