/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
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
 * Patrick Tassé    (Ericsson)   - [285226] Empty directory shown as an error message
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import java.io.File;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.messages.SystemElementNotFoundException;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;
import org.eclipse.rse.tests.subsystems.shells.ShellOutputListener;

public class FileServiceTest extends RSEBaseConnectionTestCase {

	private String fPropertiesFileName;
	// For testing the test: verify methods on Local
	public static String fDefaultPropertiesFile = "localConnection.properties";

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
		String[] connTypes = { "local", "ssh", "ftpWindows", "ftpSsh", "linux", "windows", "unix" };
		//String[] connTypes = { "unix" };
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
			String testName = "a !@#${a}\"\' fi\tle\b\\%^&*()?_ =[]~+-'`;:,.|<>"; //$NON-NLS-1$
			// Bug 235492: DStore is designed to treat '\' and '/' the same way, so do not
			// test this.
			if (fss.getSubSystemConfiguration().getServiceImplType().getName().equals("org.eclipse.rse.services.dstore.IDStoreService")) { //$NON-NLS-1$
				testName.replace('\\', ' ');
			}
		}
		//Fallback: Windows TODO: test unicode
		//Note: The trailing dot ('.') is really unfair on Windows because the file
		//system doesn't seem to ever store trailing dots
		//return "a !@#${a}'` file%^&()_ =[]~+-;,."; //$NON-NLS-1$
		return "a !@#${a}'` file%^&()_ =[]~+-;.,"; //$NON-NLS-1$
	}

	/**
	 * Find the first IShellServiceSubSystem service associated with the host.
	 *
	 * @return shell service subsystem, or <code>null</code> if not found.
	 */
	public IShellService getRelatedShellService() {
		IHost host = fss.getHost();
		ISubSystem[] subSystems = host.getSubSystems();
		IShellService ssvc = null;
		for (int i = 0; subSystems != null && i < subSystems.length; i++) {
			IService svc = subSystems[i].getSubSystemConfiguration().getService(host);
			if (svc != null) {
				ssvc = (IShellService) svc.getAdapter(IShellService.class);
				if (ssvc != null) {
					try {
						subSystems[i].checkIsConnected(getDefaultProgressMonitor());
						return ssvc;
					} catch (SystemMessageException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
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
			assertNull(roots[i].getParentPath());
			String rootName = roots[i].getName();
			assertNotNull(rootName);
			System.out.println(rootName);
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
		IHostFile hf = fs.createFile(tempDirPath, testName, mon);
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

	public String[] runRemoteCommand(IShellService shellService, String workingDirectory, String cmd) throws SystemMessageException, InterruptedException {
		IHostShell hostShell = null;
		hostShell = shellService.runCommand(workingDirectory, cmd, null, mon);
		ShellOutputListener outputListener = new ShellOutputListener();
		hostShell.addOutputListener(outputListener);
		hostShell.writeToShell("exit");
		assertNotNull(hostShell);
		assertNotNull(hostShell.getStandardOutputReader());
		while (hostShell.isActive()) {
			Thread.sleep(1000);
		}
		Object[] allOutput = outputListener.getAllOutput();
		if (allOutput!=null) {
			String[] sOutput = new String[allOutput.length];
			for (int i = 0; i < allOutput.length; i++) {
				if (allOutput[i] instanceof IHostOutput) {
					sOutput[i] = ((IHostOutput) allOutput[i]).getString();
				} else {
					sOutput[i] = allOutput[i].toString();
				}
			}
			return sOutput;
		}
		return null;
	}

	/**
	 * Create a symbolic link in the context of tempDirPath.
	 *
	 * @param source source file to link from
	 * @param target target file to link to
	 * @return <code>true</code> if link was successfully created.
	 */
	public boolean mkSymLink(String source, String target) {
		if (!fss.getHost().getSystemType().isWindows()) {
			IShellService ss = getRelatedShellService();
			if (ss != null) {
				String[] allOutput;
				try {
					String src = PathUtility.enQuoteUnix(source);
					String tgt = PathUtility.enQuoteUnix(target);
					String cmd = "ln -s " + src + " " + tgt;
					allOutput = runRemoteCommand(ss, tempDirPath, cmd);
					IHostFile hf = fs.getFile(tempDirPath, target, mon);
					if (hf.exists()) {
						return true;
					}
					allOutput = new String[] { "Failed to symlink: " + cmd };
				} catch (Exception e) {
					allOutput = new String[] { "Exception thrown: " + e };
				}
				System.out.println("WARNING: Could not create symlink");
				if (allOutput != null) {
					for (int i = 0; i < allOutput.length; i++) {
						System.out.println(allOutput[i]);
					}
				}
			}
		}
		return false;
	}

	public void testListEmptyFolder() throws SystemMessageException {
		// -test-author-:PatrickTassé
		if (isTestDisabled()) return;

		String testName = "empty";
		IHostFile hf = fs.createFolder(tempDirPath, testName, mon);
		assertTrue(hf.exists());
		assertTrue(hf.isDirectory());
		assertTrue(hf.canRead());
		assertTrue(hf.canWrite());
		assertEquals(testName, hf.getName());
		assertEquals(tempDirPath, hf.getParentPath());
		long modDate = hf.getModifiedDate();
		assertTrue(modDate > 0);
		if (fss.getHost().getSystemType().isLocal()) {
			File theFile = new File(remoteTempDir.getAbsolutePath(), testName);
			assertTrue(theFile.exists());
			assertTrue(modDate == theFile.lastModified());
		}
		IHostFile[] hfa = fs.list(hf.getAbsolutePath(), "*", IFileService.FILE_TYPE_FILES_AND_FOLDERS, mon);
		assertEquals(0, hfa.length);
		// check for symlink-to-empty-folder case
		if (mkSymLink(testName, "lto" + testName)) {
			IHostFile hf2 = fs.getFile(tempDirPath, "lto" + testName, mon);
			assertTrue(hf2.isDirectory());
			hfa = fs.list(hf2.getAbsolutePath(), "*", IFileService.FILE_TYPE_FILES_AND_FOLDERS, mon);
			assertEquals(0, hfa.length);
		}
	}

	public void testListNonExistentFolder() throws SystemMessageException, InterruptedException {
		// -test-author-:PatrickTassé
		if (isTestDisabled()) return;

		String testPath = tempDirPath + "/non/existent";
		try {
			IHostFile[] hfa = fs.list(testPath, "*", IFileService.FILE_TYPE_FILES_AND_FOLDERS, mon);
			// Bug 285942: LocalFileService and DStoreFileService return empty array today
			// Assert something impossible since an exception is expected
			assertEquals("Exception expected on list nonexistent", -1, hfa.length);
		} catch (SystemMessageException e) {
			assertTrue(e instanceof SystemElementNotFoundException);
		}
		// check for symlink-to-non-existent case
		if (mkSymLink("non/existent", "ltononex")) {
			IHostFile hf2 = fs.getFile(tempDirPath, "ltononex", mon);
			try {
				IHostFile[] hfa = fs.list(hf2.getAbsolutePath(), "*", IFileService.FILE_TYPE_FILES_AND_FOLDERS, mon);
				assertEquals("Exception expected on list broken symlink", -1, hfa.length);
			} catch (SystemMessageException e) {
				assertTrue(e instanceof SystemElementNotFoundException);
			}
		}
	}

	public void testListNotAFolder() throws SystemMessageException {
		// -test-author-:PatrickTassé
		if (isTestDisabled()) return;

		String testName = getTestFileName();
		IHostFile hf = fs.createFile(tempDirPath, testName, mon);
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
		try {
			IHostFile[] hfa = fs.list(hf.getAbsolutePath(), "*", IFileService.FILE_TYPE_FILES_AND_FOLDERS, mon);
			// Bug 285942: LocalFileService and DStoreFileService return empty array today
			// Assert something impossible since an exception is expected
			assertEquals("Exception expected on list not-a-folder", -1, hfa.length);
		} catch (SystemMessageException e) {
			assertTrue(e instanceof RemoteFileException);
		}
		// check for symlink-to-not-a-folder case
		if (mkSymLink(testName, "lto" + testName)) {
			try {
				IHostFile hf2 = fs.getFile(tempDirPath, "lto" + testName, mon);
				assertTrue(hf2.isFile());
				IHostFile[] hfa = fs.list(hf2.getAbsolutePath(), "*", IFileService.FILE_TYPE_FILES_AND_FOLDERS, mon);
				// Assert something impossible since an exception is expected
				assertEquals("Exception expected on list symlink-to-folder", -1, hfa.length);
			} catch (SystemMessageException e) {
				assertTrue(e instanceof RemoteFileException);
			}
		}
	}

}
