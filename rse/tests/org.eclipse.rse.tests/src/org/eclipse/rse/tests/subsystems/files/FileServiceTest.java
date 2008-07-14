/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 * Martin Oberhuber (Wind River) - [235360][ftp][ssh] Return proper "Root" IHostFile
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.io.File;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;

public class FileServiceTest extends RSEBaseConnectionTestCase {

	private String fPropertiesFileName;
	// For testing the test: verify methods on Local
	public static String fDefaultPropertiesFile = "local.properties";

	private IFileServiceSubSystem fss;
	private IFileService fs;
	private IRemoteFile fHomeDirectory;
	private IRemoteFile remoteTempDir;
	private String tempDirPath;
	private IProgressMonitor mon = new NullProgressMonitor();

	/**
	 * Constructor with specific test name.
	 *
	 * @param name test to execute
	 */
	public FileServiceTest(String name) {
		this(name, fDefaultPropertiesFile);
	}

	/**
	 * Constructor with connection type and specific test name.
	 *
	 * @param name test to execute
	 * @param propertiesFileName file with connection properties to use
	 */
	public FileServiceTest(String name, String propertiesFileName) {
		super(name);
		fPropertiesFileName = propertiesFileName;
		if (propertiesFileName != null) {
			int idx = propertiesFileName.indexOf("Connection.properties");
			String targetName = propertiesFileName.substring(0, idx);
			setTargetName(targetName);
		}
	}

	public static Test suite() {
		String baseName = FileServiceTest.class.getName();
		TestSuite suite = new TestSuite(baseName);

		// // Add a test suite for each connection type
		String[] connTypes = { "local", "ssh", "ftpWindows", "ftp", "linux", "windows" };
		//String[] connTypes = { "local" };
		// String[] connTypes = { "ssh" };

		for (int i = 0; i < connTypes.length; i++) {
			String suiteName = connTypes[i] == null ? "EFS" : connTypes[i];
			String propFileName = connTypes[i] == null ? null : connTypes[i] + "Connection.properties";
			TestSuite subSuite = new TestSuite(baseName + "." + suiteName);
			Method[] m = FileServiceTest.class.getMethods();
			for (int j = 0; j < m.length; j++) {
				String testName = m[j].getName();
				if (testName.startsWith("test")) {
					subSuite.addTest(new FileServiceTest(testName, propFileName));
				}
			}
			suite.addTest(subSuite);
		}
		return suite;
	}


	public void setUp() throws Exception {
		super.setUp();
		IHost host = getHost(fPropertiesFileName);
		fss = (IFileServiceSubSystem) RemoteFileUtility.getFileSubSystem(host);
		fs = fss.getFileService();
		fss.checkIsConnected(getDefaultProgressMonitor());
		fHomeDirectory = fss.getRemoteFileObject(".", getDefaultProgressMonitor());
		remoteTempDir = fss.getRemoteFileObject(fHomeDirectory, "rsetest" + System.currentTimeMillis(), getDefaultProgressMonitor());
		fss.createFolder(remoteTempDir, getDefaultProgressMonitor());
		tempDirPath = remoteTempDir.getAbsolutePath();
	}

	public void tearDown() throws Exception {
		fss.delete(remoteTempDir, getDefaultProgressMonitor());
		super.tearDown();
	}

	public boolean isWindows() {
		return fss.getHost().getSystemType().isWindows();
	}

	public String getTestFileName() {
		//Return a filename for testing that exposes all characters valid on the file system
		if (!isWindows()) {
			//UNIX TODO: test embedded newlines
			return "a !@#${a}\"\' fi\tle\b\\%^&*()?_ =[]~+-'`;:,.|<>"; //$NON-NLS-1$
		}
		//Fallback: Windows TODO: test unicode
		//Note: The trailing dot ('.') is really unfair on Windows because the file
		//system doesn't seem to ever store trailing dots
		//return "a !@#${a}'` file%^&()_ =[]~+-;,."; //$NON-NLS-1$
		return "a !@#${a}'` file%^&()_ =[]~+-;.,"; //$NON-NLS-1$
	}

	public void testGetRootProperties() throws Exception {
		//-test-author-:MartinOberhuber
		if (isTestDisabled()) return;
		IHostFile[] roots = fs.getRoots(new NullProgressMonitor());
		assertNotNull(roots);
		assertTrue(roots.length > 0);
		for (int i = 0; i < roots.length; i++) {
			assertTrue(roots[i].isRoot());
			assertTrue(roots[i].exists());
			assertNull(roots[i].getParentPath()); //dstore: bug 235471
			String rootName = roots[i].getName();
			assertNotNull(rootName);
			System.out.println(rootName);
			// DStore: NPE, bug 240710
			IHostFile newHf = fs.getFile(null, rootName, new NullProgressMonitor());
			assertTrue(newHf.isRoot());
			assertTrue(newHf.exists());
			assertEquals(newHf.getName(), rootName);
			newHf = fs.getFile("", rootName, new NullProgressMonitor());
			assertTrue(newHf.isRoot());
			assertTrue(newHf.exists());
			assertEquals(newHf.getName(), rootName);
		}
	}

	public void testCaseSensitive() {
		//-test-author-:MartinOberhuber
		if (isTestDisabled()) return;

		if (isWindows()) {
			assertFalse(fs.isCaseSensitive());
			assertFalse(fss.isCaseSensitive());
			assertFalse(fss.getSubSystemConfiguration().isCaseSensitive());
		} else {
			assertTrue(fs.isCaseSensitive());
			assertTrue(fss.isCaseSensitive());
			assertTrue(fss.getSubSystemConfiguration().isCaseSensitive());
		}
	}

	public void testCreateFile() throws SystemMessageException {
		//-test-author-:MartinOberhuber
		if (isTestDisabled()) return;

		String testName = getTestFileName();
		IHostFile hf = fs.createFile(tempDirPath, testName, mon); //dstore-linux: bug 235492
		assertTrue(hf.exists());
		assertTrue(hf.canRead());
		assertTrue(hf.canWrite());
		assertEquals(hf.getName(), testName);
		assertEquals(hf.getParentPath(), tempDirPath);
		assertEquals(hf.getSize(), 0);
		long modDate = hf.getModifiedDate();
		assertTrue(modDate > 0);
		if (fss.getHost().getSystemType().isLocal()) {
			File theFile = new File(remoteTempDir.getAbsolutePath(), testName);
			assertTrue(theFile.exists());
			assertTrue(modDate == theFile.lastModified());
		}
	}

	public void testCreateCaseSensitive() throws SystemMessageException {
		//-test-author-:MartinOberhuber
		if (isTestDisabled()) return;

		String testName = getTestFileName();
		String testName2 = testName.toUpperCase();
		IHostFile hf = fs.createFile(tempDirPath, testName, mon);
		if (fss.isCaseSensitive()) {
			//UNIX: uppercase version must be distinct
			IHostFile hf2 = fs.getFile(tempDirPath, testName2, mon);
			assertFalse(hf2.exists());
			hf2 = fs.createFolder(tempDirPath, testName2, mon);
			assertTrue(hf2.exists());
			assertTrue(hf2.isDirectory());
			assertFalse(hf.equals(hf2));
		} else {
			//Windows: uppercase version must be the same
			IHostFile hf2 = fs.getFile(tempDirPath, testName2, mon);
			assertTrue(hf2.exists());
			try {
				hf2 = fs.createFolder(tempDirPath, testName2, mon);
			} catch(SystemMessageException e) {
				//Windows cannot create a folder when the file is already there
				assertNotNull(e);
			}
			assertTrue(hf2.exists());
			assertFalse(hf2.isDirectory());
			assertEquals(hf.getModifiedDate(), hf2.getModifiedDate());
			assertEquals(hf.getSize(), hf2.getSize());
			//Different abstract path names but denote the same file
			//Should be equal since java.io.File treats them as equal
			assertEquals(new File(tempDirPath, testName), new File(tempDirPath, testName2));
			////While the file handles were created with different names,
			////resolving them should return the same name (the one that's on disk)
			////But Local and java.io.File do not work that way
			//assertEquals(hf.getName(), hf2.getName());
			assertEquals(hf, hf2); //bug 168591, bug 235489: no equals() for IHostFile
		}
	}

}
