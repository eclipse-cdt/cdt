/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [240729] More flexible disabling of testcases
 * Martin Oberhuber (Wind River) - [314439] testDeleteSpecialCases fails on Linux
 *******************************************************************************/

package org.eclipse.rse.tests.subsystems.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.efs.RSEFileSystem;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.model.SimpleCommandOperation;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;

/**
 * Testcase for RSEFileStore
 */
public class RSEFileStoreTest extends FileServiceBaseTest {

	private String fPropertiesFileName;
	//For testing the test: verify methods on Eclipse Local Filesystem
	public static String fDefaultPropertiesFile = null;
	//public static String fDefaultPropertiesFile = "sshConnection.properties"; //$NON-NLS-1$

	private IRemoteFile fHomeDirectory;
	private String fTestStorePath;
	private IFileStore fTestStore;
	private InputStream fIS;
	private OutputStream fOS;

	/**
	 * Constructor with specific test name.
	 *
	 * @param name test to execute
	 */
	public RSEFileStoreTest(String name) {
		this(name, fDefaultPropertiesFile);
	}

	/**
	 * Constructor with connection type and specific test name.
	 *
	 * @param name test to execute
	 * @param propertiesFileName file with connection properties to use
	 */
	public RSEFileStoreTest(String name, String propertiesFileName) {
		super(name);
		fPropertiesFileName = propertiesFileName;
		if (propertiesFileName != null) {
			int idx = propertiesFileName.indexOf("Connection.properties");
			String targetName = propertiesFileName.substring(0, idx);
			setTargetName(targetName);
		}
	}

	public static Test suite() {
		String baseName = RSEFileStoreTest.class.getName();
		TestSuite suite = new TestSuite(baseName);

		//// Add a test suite for each connection type
		//String[] connTypes = { null, "local", "ssh", "ftp", "linux", "windows" };
		String[] connTypes = { null, "local" };
		//String[] connTypes = { "ssh" };

		for (int i = 0; i < connTypes.length; i++) {
			String suiteName = connTypes[i] == null ? "EFS" : connTypes[i];
			String propFileName = connTypes[i] == null ? null : connTypes[i] + "Connection.properties";
			TestSuite subSuite = new TestSuite(baseName + "." + suiteName);
			Method[] m = RSEFileStoreTest.class.getMethods();
			for (int j = 0; j < m.length; j++) {
				String testName = m[j].getName();
				if (testName.startsWith("test")) {
					subSuite.addTest(new RSEFileStoreTest(testName, propFileName));
				}
			}
			suite.addTest(subSuite);
		}
		return suite;
	}


	protected IShellServiceSubSystem getShellServiceSubSystem() {
		if (fPropertiesFileName == null) {
			return null;
		}
		IHost host = getHost(fPropertiesFileName);
		ISubSystem[] ss = RSECorePlugin.getTheSystemRegistry().getServiceSubSystems(host, IShellService.class);
		for (int i = 0; i < ss.length; i++) {
			if (ss[i] instanceof ShellServiceSubSystem) {
				return (ShellServiceSubSystem) ss[i];
			}
		}
		return null;
	}

	public void setUp() throws Exception {
		super.setUp();
		if (fPropertiesFileName == null) {
			//For testing the test: Use Eclipse EFS.getLocalFileSystem()
			String homePath = System.getProperty("user.home");
			IPath testPath = new Path(homePath + "/rseTest" + System.currentTimeMillis());
			fTestStorePath = testPath.toOSString();
			fTestStore = EFS.getLocalFileSystem().getStore(testPath);
			fTestStore.mkdir(EFS.NONE, getDefaultProgressMonitor());
		} else {
			//RSE method
			IHost host = getHost(fPropertiesFileName);
			IRemoteFileSubSystem fss = RemoteFileUtility.getFileSubSystem(host);
			fss.checkIsConnected(getDefaultProgressMonitor());
			fHomeDirectory = fss.getRemoteFileObject(".", getDefaultProgressMonitor());
			IPath testPath = new Path(fHomeDirectory.getAbsolutePath() + "/rseTest" + System.currentTimeMillis());
			fTestStorePath = testPath.toString();
			URI testURI = RSEFileSystem.getURIFor(host.getHostName(), fTestStorePath);
			fTestStore = RSEFileSystem.getInstance().getStore(testURI);
			fTestStore.mkdir(EFS.NONE, getDefaultProgressMonitor());
		}
	}

	public void tearDown() throws Exception {
		try {
			if (fIS != null) {
				try {
					fIS.close();
				} catch (IOException e) {
					System.err.println("Exception in tearDown.closeInputStream:");
					e.printStackTrace();
				}
			}
			if (fOS != null) {
				try {
					fOS.close();
				} catch (IOException e) {
					System.err.println("Exception in tearDown.closeOutputStream:");
					e.printStackTrace();
				}
			}
			//Try..catch to allow super.tearDown() to run
			try {
				IFileInfo info = fTestStore.fetchInfo();
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
				info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
				fTestStore.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());
			} finally {
				try {
					fTestStore.delete(EFS.NONE, getDefaultProgressMonitor());
				} catch (CoreException ce) {
					/* might be expected if fTestStore had no permissions */
				} finally {
					IRemoteCmdSubSystem rcmd = getShellServiceSubSystem();
					if (rcmd!=null) {
						SimpleCommandOperation op = new SimpleCommandOperation(rcmd, fHomeDirectory, true);
						op.runCommand("chmod 777 \"" + fTestStorePath + "\"", true);
						while (op.isActive()) {
							Thread.sleep(200);
						}
						//no more exception expected.
						fTestStore.delete(EFS.NONE, getDefaultProgressMonitor());
					}
				}
			}
		} finally {
			super.tearDown();
		}
	}

	protected IFileStore createFile(String name) throws Exception {
		IFileStore store = fTestStore.getChild(name);
		fOS = store.openOutputStream(EFS.NONE, getDefaultProgressMonitor());
		fOS.write(name.getBytes());
		fOS.flush();
		fOS.close();
		fOS = null;
		return store;
	}

	public void testRecursiveGetParent() {
		//-test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		IFileStore store = fTestStore;
		String homePath = store.toURI().getPath();
		assertTrue("exists: " + store, store.fetchInfo().exists());
		IFileStore newStore = store.getParent();
		while (newStore != null && newStore.isParentOf(store)) {
			assertTrue("exists: " + newStore, newStore.fetchInfo().exists());
			store = newStore;
		}
		String newPath = store.toURI().getPath();
		assertTrue("newPath not empty: " + newPath, newPath.length() > 0);
		assertTrue("newPath < homePath " + homePath, newPath.length() < homePath.length());
	}

	public void testAppendOutputStream() throws Exception {
		//-test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		IFileStore f = createFile("foo");
		fOS = f.openOutputStream(EFS.APPEND, getDefaultProgressMonitor());
		fOS.write("bar".getBytes());
		fOS.flush();
		fOS.close();
		fOS = null;
		fIS = f.openInputStream(EFS.NONE, getDefaultProgressMonitor());
		byte[] b = "foobar".getBytes();
		for (int i = 0; i < b.length; i++) {
			assertTrue("Reading byte " + i, b[i] == fIS.read());
		}
		assertTrue("EOF", fIS.read() == -1);
		fIS.close();
		fIS = null;
	}

	public void testPutInfo() throws Exception {
		//-test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		IFileInfo testInfo = fTestStore.fetchInfo();
		assertTrue("1.1", testInfo.exists());
		assertTrue("1.2", testInfo.isDirectory());
		// if (fPropertiesFileName != null) {
		// // bug 249316: fails on EFS localstore linux nfs
		// assertTrue("1.3", testInfo.getLength() == 0);
		// }
		long parentModified = testInfo.getLastModified();

		IFileStore f = createFile("testReadOnly.txt");
		IFileInfo info = f.fetchInfo();
		assertFalse("2.1", info.isDirectory());
		assertFalse("2.2", info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		assertTrue("2.3", info.getLastModified() >= parentModified);

		info.setLastModified(parentModified - 120000); // 2 minutes earlier
		f.putInfo(info, EFS.SET_LAST_MODIFIED, getDefaultProgressMonitor());
		info = f.fetchInfo();
		assertTrue("2.4", info.getLastModified() < parentModified);

		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		f.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());
		info = f.fetchInfo();
		assertTrue("2.5", info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));

		//Modifying modtime of read-only file: expected to fail
		info.setLastModified(parentModified + 120000); // 2 minutes later
		assertTrue("2.6.1", info.getLastModified() > parentModified);
		try {
			f.putInfo(info, EFS.SET_LAST_MODIFIED, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			//TODO: Not specified by EFS whether putInfo modtime of a read-only file should throw an exception
			//EFS.getLocalFileSystem() does not throw an exception, but also doesn't change the modtime
			System.out.println("OK: Exception on putInfo to read-only: " + ce.getLocalizedMessage());
		}
		info = f.fetchInfo();
		//SSH is capable of modifying modtime of read-only files
		//assertTrue("2.6.2", info.getLastModified() <= parentModified); //not actually changed
	}

	public void testDeleteSpecialCases() throws Exception {
		//-test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		String testFileName = "noPerm.txt"; //$NON-NLS-1$
		boolean exceptionThrown = false;

		//delete file without read permissions on parent
		IFileStore store = createFile(testFileName);
		IFileInfo info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		store.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());
		info = fTestStore.fetchInfo();
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, false);
		fTestStore.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());
		try {
			store.delete(EFS.NONE, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			exceptionThrown = true;
			System.out.println("Good! " + ce);
			assertTrue("1.1.1", ce.getStatus().getCode() == EFS.ERROR_DELETE);
		}
		// restore deletable
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
		info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
		fTestStore.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());
		if (fPropertiesFileName != null && File.separatorChar != '\\') {
			// Do not check Eclipse EFS due to bug 314448
			// Do not check RSE-EFS on Windows (read-only stuff can be deleted)
			if (fHomeDirectory == null || fHomeDirectory.getSeparatorChar() != '\\') {
				assertTrue("1.1", exceptionThrown);
				IFileInfo info2 = store.fetchInfo();
				assertTrue(info2.exists());
			}
		}

		store.delete(EFS.NONE, getDefaultProgressMonitor());
		info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
		assertTrue("1.2", !info.exists());

		if (fHomeDirectory != null && fHomeDirectory.getSeparatorChar() == '/' && fHomeDirectory.getParentRemoteFileSubSystem().isCaseSensitive()) {
			//IRemoteFileSubSystem rfss = fHomeDirectory.getParentRemoteFileSubSystem();
			IRemoteCmdSubSystem rcmd = getShellServiceSubSystem();
			//SimpleCommandOperation op = new SimpleCommandOperation(rcmd, fHomeDirectory, false);
			SimpleCommandOperation op = new SimpleCommandOperation(rcmd, fHomeDirectory, true);
			op.runCommand("ln -s notExisting2.txt \"" + fTestStorePath + "/" + testFileName + "\"", true);
			while (op.isActive()) {
				Thread.sleep(200);
			}
			//delete symbolic link pointing to nowhere
			store.delete(EFS.NONE, getDefaultProgressMonitor());
			info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
			assertTrue("1.3", !info.exists());

			SimpleCommandOperation op2 = new SimpleCommandOperation(rcmd, fHomeDirectory, true);
			op2.runCommand("ln -s . \"" + fTestStorePath + "/" + testFileName + "\"", true);
			while (op2.isActive()) {
				Thread.sleep(200);
			}
			// delete symbolic link pointing to current folder
			store.delete(EFS.NONE, getDefaultProgressMonitor());
			info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
			assertTrue("1.4", !info.exists());

			//Delete without even read permission on parent folder
			store = createFile(testFileName);
			SimpleCommandOperation op3 = new SimpleCommandOperation(rcmd, fHomeDirectory, true);
			op3.runCommand("chmod 000 \"" + fTestStorePath + "\"", true);
			while (op3.isActive()) {
				Thread.sleep(200);
			}
			exceptionThrown = false;
			try {
				store.delete(EFS.NONE, getDefaultProgressMonitor());
			} catch (CoreException ce) {
				exceptionThrown = true;
				System.out.println("Good! " + ce);
				assertTrue("1.5.1", ce.getStatus().getCode() == EFS.ERROR_DELETE);
			}
			if (!"localConnection.properties".equals(fPropertiesFileName)) {
				//bug 314439: java.io.File cannot tell between no-permission and not-exists
				assertTrue("1.5", exceptionThrown);
			}

			exceptionThrown = false;
			try {
				info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
			} catch (CoreException ce) {
				exceptionThrown = true;
				System.out.println("Good! " + ce);
				assertTrue("1.6.1", ce.getStatus().getCode() == EFS.ERROR_READ);
			}
			if (!"localConnection.properties".equals(fPropertiesFileName)) {
				//bug 314439: java.io.File cannot tell between no-permission and not-exists
				assertTrue("1.6", exceptionThrown);
			}
			SimpleCommandOperation op4 = new SimpleCommandOperation(rcmd, fHomeDirectory, true);
			op4.runCommand("chmod 777 \"" + fTestStorePath + "\"", true);
			while (op4.isActive()) {
				Thread.sleep(200);
			}
			//Experience shows that we need to wait a little longer until the filesystem calms down
			Thread.sleep(500);
			info = store.fetchInfo();
			assertTrue(info.exists());
		}
	}

	public void testModifyNonExisting() throws Exception {
		// -test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		IFileStore store = fTestStore.getChild("nonExisting.txt");
		IFileInfo info;
		boolean exceptionThrown = false;

		// fetchInfo on non-Existing
		info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
		assertTrue("1.1", !info.exists());

		// delete non-Existing
		store.delete(EFS.NONE, getDefaultProgressMonitor());
		// TODO IFileStore.delete() does not specify whether deleting a
		// non-existing file should throw an Exception.
		// EFS.getLocalFileSystem() does not throw the exception.
		info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
		assertTrue("1.2", !info.exists());

		// putInfo on non-Existing
		exceptionThrown = false;
		try {
			info = new FileInfo();
			store.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			System.out.println("Good! putInfo attrib non-existing: " + ce.getLocalizedMessage());
			exceptionThrown = true;
			assertTrue("1.3.1", ce.getStatus().getCode() == EFS.ERROR_NOT_EXISTS);
		}
		assertTrue("1.3", exceptionThrown);

		// putInfo on non-Existing
		exceptionThrown = false;
		try {
			info = new FileInfo();
			store.putInfo(info, EFS.SET_LAST_MODIFIED, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			System.out.println("Good! putInfo lastMod non-existing: " + ce.getLocalizedMessage());
			exceptionThrown = true;
			assertTrue("1.4.1", ce.getStatus().getCode() == EFS.ERROR_NOT_EXISTS);
		}
		assertTrue("1.4", exceptionThrown);

		// fetchInfo on non-Existing
		exceptionThrown = false;
		info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
		assertTrue("1.5", !info.exists());

		// openInputStream on non-Existing
		exceptionThrown = false;
		try {
			fIS = store.openInputStream(EFS.NONE, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			System.out.println("Good! openInputStream non-existing: " + ce.getLocalizedMessage());
			exceptionThrown = true;
			//FIXME EFS.getLocalFileSystem() uses EFS.ERROR_READ but should ERROR_NOT_EXISTS
			int code = ce.getStatus().getCode();
			assertTrue("1.6.1", code == EFS.ERROR_NOT_EXISTS || code == EFS.ERROR_READ);
		}
		assertTrue("1.6", exceptionThrown);

		// fetchInfo on non-Existing
		exceptionThrown = false;
		info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
		assertTrue("1.7", !info.exists());

		// openOutputStream append on non-Existing
		// IFileStore specifies that this method succeeds in case of non-existing files.
		fOS = store.openOutputStream(EFS.APPEND, getDefaultProgressMonitor());
		fOS.write('a');
		fOS.close();
		fOS = null;
		info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
		assertTrue("1.8.1", info.exists());
		assertTrue("1.8.2", info.getLength() == 1);

		// openOutputStream overwrite
		fOS = store.openOutputStream(EFS.NONE, getDefaultProgressMonitor());
		fOS.write('b');
		fOS.close();
		fOS = null;
		info = store.fetchInfo(EFS.NONE, getDefaultProgressMonitor());
		assertTrue("1.9.1", info.exists());
		assertTrue("1.9.2", info.getLength() == 1);
	}

	public void testModifyReadOnly() throws Exception {
		//-test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		IFileStore store = createFile("readOnly.txt");
		IFileInfo info = store.fetchInfo();
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		store.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());

		boolean exceptionThrown = false;
		try {
			fOS = store.openOutputStream(EFS.APPEND, getDefaultProgressMonitor());
			fOS.write('a');
			fOS.close();
			fOS = null;
		} catch (CoreException ce) {
			System.out.println("Good! appendReadOnly: " + ce.getLocalizedMessage());
			exceptionThrown = true;
			int code = ce.getStatus().getCode();
			//assertTrue("1.1.1", ce.getStatus().getCode() == EFS.ERROR_READ_ONLY);
			assertTrue("1.1.1", code == EFS.ERROR_WRITE);
			assertTrue(fOS==null);
		}
		assertTrue("1.1", exceptionThrown);

		//set writable again
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
		store.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());

		//append, but KEEP STORE OPEN
		fOS = store.openOutputStream(EFS.APPEND, getDefaultProgressMonitor());
		fOS.write('a');
		fOS.flush();

		// set read-only WHILE FILE IS OPEN
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		exceptionThrown = false;
		try {
			store.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			System.out.println("Good! setOpenFileReadOnly: " + ce.getLocalizedMessage());
			int code = ce.getStatus().getCode();
			assertTrue("2.1", code == EFS.ERROR_WRITE);
		}
		//FIXME Platform EFS.getLocalFileSystem() doesn't throw exception here
		//assertTrue("2.2", exceptionThrown);
		if (!exceptionThrown) {
			info = store.fetchInfo();
			assertTrue("2.2", info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		}

		// continue appending now that the file is read-only
		fOS.write('b');
		fOS.flush();

		// set read-only AFTER CLOSING
		fOS.close();
		store.putInfo(info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());
	}

	public void testMakeDeleteTree() throws Exception {
		// -test-author-:MartinOberhuber
		// Create folder
		if (isTestDisabled())
			return;
		IFileStore treeStore = fTestStore.getChild("treeTest");
		treeStore.mkdir(EFS.SHALLOW, getDefaultProgressMonitor());

		// Neg: Create folder where file already exists
		boolean exceptionThrown = false;
		IFileStore treeNegStore = createFile("treeNegTest");
		treeNegStore = fTestStore.getChild("treeNegTest");
		try {
			treeNegStore.mkdir(EFS.SHALLOW, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			System.out.println("Good! treeNegStore.mkdir: " + ce.getLocalizedMessage());
			exceptionThrown = true;
			int code = ce.getStatus().getCode();
			assertTrue("1.1", code == EFS.ERROR_WRONG_TYPE);
		}
		assertTrue("1.2", exceptionThrown);

		// Neg: Create deep folder with EFS.SHALLOW
		exceptionThrown = false;
		//IFileStore store = treeStore.getFileStore(new Path("foo/bar/baz"));
		IFileStore store = treeStore.getChild("foo").getChild("bar").getChild("baz");
		try {
			store.mkdir(EFS.SHALLOW, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			System.out.println("Good! mkdirsShallow: " + ce.getLocalizedMessage());
			exceptionThrown = true;
			int code = ce.getStatus().getCode();
			//assertTrue("2.1", code == EFS.ERROR_NOT_EXISTS);
			assertTrue("2.1", code == EFS.ERROR_WRITE);
		}
		assertTrue("2.2", exceptionThrown);

		// Create deep folder
		store.mkdir(EFS.NONE, getDefaultProgressMonitor());

		// Neg: openInputStream on a folder
		exceptionThrown = false;
		try {
			fIS = store.openInputStream(EFS.NONE, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			System.out.println("Good! openInputStream on folder: " + ce.getLocalizedMessage());
			exceptionThrown = true;
			int code = ce.getStatus().getCode();
			//FIXME would expect EFS.ERROR_WRONG_TYPE, but EFS.getLocalFileSystem() throws EFS.ERROR_READ
			assertTrue("3.1", code == EFS.ERROR_READ || code == EFS.ERROR_WRONG_TYPE);
		}
		assertTrue("3.2", exceptionThrown);

		// Neg: openOutputStream on a folder
		exceptionThrown = false;
		try {
			fOS = store.openOutputStream(EFS.NONE, getDefaultProgressMonitor());
		} catch (CoreException ce) {
			System.out.println("Good! openOutputStream on folder: " + ce.getLocalizedMessage());
			exceptionThrown = true;
			int code = ce.getStatus().getCode();
			//FIXME expected ERROR_WRONG_TYPE, but EFS.getLocalFileSystem() throws EFS.ERROR_WRITE
			assertTrue("4.1", code == EFS.ERROR_WRITE || code == EFS.ERROR_WRONG_TYPE);
		}
		assertTrue("4.2", exceptionThrown);

		// Create file in deep folder but READ-ONLY
		IFileStore child1 = store.getChild("readOnlyFile.txt");
		fOS = child1.openOutputStream(EFS.NONE, getDefaultProgressMonitor());
		fOS.write("foobar\nbaz\n".getBytes());
		fOS.flush();
		fOS.close();
		fOS = null;
		IFileInfo child1info = child1.fetchInfo();
		child1info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		child1.putInfo(child1info, EFS.SET_ATTRIBUTES, getDefaultProgressMonitor());

		// Create file in deep folder but LEAVE IT OPEN
		IFileStore child2 = store.getChild("openFile.txt");
		fOS = child2.openOutputStream(EFS.NONE, getDefaultProgressMonitor());
		fOS.write("foobar\nbaz\n".getBytes());
		fOS.flush();

		// Delete tree
		exceptionThrown = false;
		try {
			treeStore.delete(EFS.NONE, getDefaultProgressMonitor());
			fOS.close();
			fOS = null;
		} catch (CoreException ce) {
			System.out.println("Good! Exception while deleting tree with open File: " + ce.getLocalizedMessage());
			int code = ce.getStatus().getCode();
			assertTrue(code == EFS.ERROR_DELETE);
			exceptionThrown = true;
			fOS.close();
			fOS = null;
			treeStore.delete(EFS.NONE, getDefaultProgressMonitor());
		}
		//We don't care about exception thrown or not, as long as tree is gone
		//assertTrue("5", exceptionThrown);

		IFileInfo treeInfo = treeStore.fetchInfo();
		assertFalse("5.1", treeInfo.exists());
		assertFalse("5.2", child1.fetchInfo().exists());
		assertFalse("5.3", child2.fetchInfo().exists());
	}

	public static Test suite255files() {
		String baseName = RSEFileStoreTest.class.getName();
		TestSuite suite = new TestSuite(baseName);
		suite.addTest(new RSEFileStoreTest("test255files", "sshConnection.properties"));
		suite.addTest(new RSEFileStoreTest("test255files", "linuxConnection.properties"));
		suite.addTest(new RSEFileStoreTest("test255files", "sshConnection.properties"));
		suite.addTest(new RSEFileStoreTest("test255files", "linuxConnection.properties"));
		return suite;
	}

	public void test255files() throws Exception {
		if (isTestDisabled())
			return;
		IFileStore f = fTestStore.getChild("f");
		f.mkdir(EFS.SHALLOW, getDefaultProgressMonitor());
		for (int i = 0; i < 255; i++) {
			IFileStore store = f.getChild("f" + i);
			fOS = store.openOutputStream(EFS.NONE, getDefaultProgressMonitor());
			fOS.write(String.valueOf(i).getBytes());
			fOS.flush();
			fOS.close();
			fOS = null;
		}
		IFileInfo[] infos = f.childInfos(EFS.NONE, getDefaultProgressMonitor());
		assertTrue("1", infos.length == 255);
		for (int i = 0; i < 255; i++) {
			assertTrue("1.1", infos[i].exists());
			assertFalse("1.2", infos[i].isDirectory());
		}
		f.delete(EFS.NONE, getDefaultProgressMonitor());
	}

}
