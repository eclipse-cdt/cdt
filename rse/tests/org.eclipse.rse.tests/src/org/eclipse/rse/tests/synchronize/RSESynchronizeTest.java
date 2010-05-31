/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - Adapted from org.eclipse.rse.tests / FileServiceTest
 * Martin Oberhuber (Wind River) - [314547] NPE in RSESynchronizeTest
 *******************************************************************************/
package org.eclipse.rse.tests.synchronize;

import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.rse.internal.importexport.files.Utilities;
import org.eclipse.rse.internal.synchronize.SynchronizeData;
import org.eclipse.rse.internal.synchronize.provisional.ISynchronizeConnectionManager;
import org.eclipse.rse.internal.synchronize.provisional.ISynchronizeOperation;
import org.eclipse.rse.internal.synchronize.provisional.ISynchronizer;
import org.eclipse.rse.internal.synchronize.provisional.SynchronizeConnectionManager;
import org.eclipse.rse.internal.synchronize.provisional.SynchronizeOperation;
import org.eclipse.rse.internal.synchronize.provisional.Synchronizer;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

public class RSESynchronizeTest extends SynchronizeTestBase {

	/**
	 * Constructor with specific test name.
	 *
	 * @param name test to execute
	 */
	public RSESynchronizeTest(String name) {
		super(name);
	}

	/**
	 * Constructor with connection type and specific test name.
	 *
	 * @param name test to execute
	 * @param propertiesFileName file with connection properties to use
	 */
	public RSESynchronizeTest(String name, String propertiesFileName) {
		super(name, propertiesFileName);
	}

	public static Test suite() {
		String baseName = RSESynchronizeTest.class.getName();
		TestSuite suite = new TestSuite(baseName);

		// // Add a test suite for each connection type
		// String[] connTypes = { "local", "ssh", "ftpWindows", "ftp", "linux",
		// "windows" };
		String[] connTypes = { "local" };
		// String[] connTypes = { "ssh" };

		for (int i = 0; i < connTypes.length; i++) {
			String suiteName = connTypes[i] == null ? "EFS" : connTypes[i];
			String propFileName = connTypes[i] == null ? null : connTypes[i] + "Connection.properties";
			TestSuite subSuite = new TestSuite(baseName + "." + suiteName);
			Method[] m = RSESynchronizeTest.class.getMethods();
			for (int j = 0; j < m.length; j++) {
				String testName = m[j].getName();
				if (testName.startsWith("test")) {
					subSuite.addTest(new RSESynchronizeTest(testName, propFileName));
				}
			}
			suite.addTest(subSuite);
		}
		return suite;
	}

	public void testConnectDisconnect() throws Exception {
		// -test-author-:MartinOberhuber
		if (isTestDisabled())
			return;

		// Create a local test project with some files and folders in it
		IProject project = getProject(getUniqueString());
		IResource[] resources = new IResource[] { project };
		ensureExistsInWorkspace(resources, true);

		ISynchronizeConnectionManager connection = new SynchronizeConnectionManager();
		assertTrue("1.1", !connection.isConnected(project));
		// FIXME not a good idea to throw TeamException here -- this is the only
		// reason why I need to import org.eclipse.team.core here
		connection.connect(project);
		assertTrue("1.2", connection.isConnected(project));

		connection.disconnect(project);
		assertTrue("1.3", !connection.isConnected(project));

		// What happens if a connected project is deleted?
		ensureDoesNotExistInWorkspace(project);
		assertTrue("2.1", !connection.isConnected(project));
	}


	public void testExport() throws Exception {
		// -test-author-:MartinOberhuber
		if (isTestDisabled())
			return;

		// Create a local test project with some files and folders in it
		IProject project = getProject(getUniqueString());
		IFolder folder = project.getFolder("folder");
		IFile file = project.getFile("file.txt");
		IFile subFile = folder.getFile("subfile.txt");
		IFile subFile2 = folder.getFile("subfile2.txt");
		IResource[] resources = new IResource[] { project, folder, file, subFile, subFile2 };
		ensureExistsInWorkspace(resources, true);

		// Initial export of test project to remote folder
		SynchronizeData sd = new SynchronizeData();

		// sync only folder and subFile
		IResource[] resourcesToSync = new IResource[] { folder, subFile };
		sd.setElements(Arrays.asList(resourcesToSync));
		String remoteLocation = Utilities.getAsString(remoteTempDir);
		sd.setRemoteLocation(remoteLocation);
		sd.setSynchronizeType(ISynchronizeOperation.SYNC_MODE_OVERRIDE_DEST);
		
		ISynchronizer synchronizer = new Synchronizer(sd);
		synchronizer.run(new SynchronizeOperation());

		// Check file files and folders exist on the remote
		IRemoteFile rProject = fss.getRemoteFileObject(remoteTempDir, project.getName(), getDefaultProgressMonitor());
		assertTrue("1.0", rProject.exists());
		IRemoteFile rFolder = fss.getRemoteFileObject(rProject, "folder", getDefaultProgressMonitor());
		assertTrue("1.1", rFolder.exists());
		assertTrue("1.2", rFolder.isDirectory());
		IRemoteFile rFile = fss.getRemoteFileObject(rProject, "file.txt", getDefaultProgressMonitor());
		assertFalse("2.1", rFile.exists());
		IRemoteFile rSubFile = fss.getRemoteFileObject(rFolder, "subfile.txt", getDefaultProgressMonitor());
		assertTrue("3.1", rSubFile.exists());
		assertTrue("3.2", rSubFile.isFile());
		//bug....
		//IRemoteFile rSubFile2 = fss.getRemoteFileObject(rFolder, "subfile2.txt", getDefaultProgressMonitor());
		//assertFalse("4.1", rSubFile2.exists());

		// TODO: Check file contents, timestamp etc

	}

}
