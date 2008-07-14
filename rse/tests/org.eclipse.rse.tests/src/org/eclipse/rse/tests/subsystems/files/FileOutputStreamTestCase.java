/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: Kevin Doyle
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [cleanup] Avoid using SystemStartHere in production code
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 * Martin Oberhuber (Wind River) - [195402] Add constructor with test name
 ********************************************************************************/

package org.eclipse.rse.tests.subsystems.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.efs.RSEFileStore;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

public class FileOutputStreamTestCase extends FileServiceBaseTest {

	private IHost host = null;
	private IRemoteFile tempDirectory;

	/**
	 * Constructor with specific test name.
	 * @param name test to execute
	 */
	public FileOutputStreamTestCase(String name) {
		super(name);
	}

	private IRemoteFileSubSystem getRemoteFileSubSystem(IHost host) {
		IRemoteFileSubSystem fss = null;
		ISystemRegistry sr = SystemStartHere.getSystemRegistry();
		ISubSystem[] ss = sr.getServiceSubSystems(host, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof FileServiceSubSystem) {
				fss = (IRemoteFileSubSystem)ss[i];
				return fss;
			}
		}
		return null;
	}

	public void testRSEFileStoreAppendOutputStreamLocal() throws Exception {
		//-test-author-:KevinDoyle
		host = getLocalSystemConnection();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.APPEND);
	}

	public void testRSEFileStoreAppendOutputStreamFTP() throws Exception {
		//-test-author-:KevinDoyle
		host = getFTPHost();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.APPEND);
	}

	public void testRSEFileStoreAppendOutputStreamLinux() throws Exception {
		//-test-author-:KevinDoyle
		host = getLinuxHost();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.APPEND);
	}

	public void testRSEFileStoreAppendOutputStreamWindows() throws Exception {
		//-test-author-:KevinDoyle
		host = getWindowsHost();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.APPEND);
	}

	public void testRSEFileStoreAppendOutputStreamSSH() throws Exception {
		//-test-author-:KevinDoyle
		host = getSSHHost();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.APPEND);
	}

	public void testRSEFileStoreOverwriteOutputStreamLocal() throws Exception {
		//-test-author-:KevinDoyle
		host = getLocalSystemConnection();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.NONE);
	}

	public void testRSEFileStoreOverwriteOutputStreamFTP() throws Exception {
		//-test-author-:KevinDoyle
		host = getFTPHost();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.NONE);
	}

	public void testRSEFileStoreOverwriteOutputStreamLinux() throws Exception {
		//-test-author-:KevinDoyle
		host = getLinuxHost();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.NONE);
	}

	public void testRSEFileStoreOverwriteOutputStreamWindows() throws Exception {
		//-test-author-:KevinDoyle
		host = getWindowsHost();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.NONE);
	}

	public void testRSEFileStoreOverwriteOutputStreamSSH() throws Exception {
		//-test-author-:KevinDoyle
		host = getSSHHost();
		if (isTestDisabled())
			return;
		outputStreamFileWriting(EFS.NONE);
	}

	public void outputStreamFileWriting(int options) throws Exception {
		// RSE URI: rse://SYSTEM_ADDRESS/PATH_TO_FIlE
		OutputStream outputStream = null;
		InputStream inputStream = null;

		// Create temporary folder
		FileServiceSubSystem inputFss = (FileServiceSubSystem) getRemoteFileSubSystem(host);
		inputFss.connect(new NullProgressMonitor(), false);
		IRemoteFile homeDirectory = inputFss.getRemoteFileObject(".", new NullProgressMonitor());
		String baseFolderName = "rsetest";
		String homeFolderName = homeDirectory.getAbsolutePath();
		String testFolderName = FileServiceHelper.getRandomLocation(inputFss, homeFolderName, baseFolderName, new NullProgressMonitor());
		IRemoteFile targetDir = createFileOrFolder(inputFss, homeFolderName, testFolderName, true);
		tempDirectory = targetDir;

		String path = targetDir.getAbsolutePath();

		String systemType = host.getSystemType().getLabel();
		if (host.getSystemType().isWindows()) {
			path = path.replace('\\', '/');
		}
		path = fixPathForURI(path);
		URI uri = new URI("rse", host.getHostName(), path, null);

		IFileStore parentFS = RSEFileStore.getInstance(uri);
		createDir(parentFS, true);

		IFileStore childFS = parentFS.getChild("append.txt");

		outputStream = childFS.openOutputStream(options, new NullProgressMonitor());

		String contents = getRandomString();
		byte[] readBytes = new byte[contents.length()];
		outputStream.write(contents.getBytes());
		outputStream.close();

		inputStream = childFS.openInputStream(EFS.NONE, new NullProgressMonitor());
		inputStream.read(readBytes);

		String input = new String(readBytes);
		inputStream.close();
		assertTrue(systemType + ": Contents incorrect writing to an empty file.  Expected Contents: " + contents + " Actual Contents: " + input, contents.equals(input));

		outputStream = childFS.openOutputStream(options, new NullProgressMonitor());

		String write = " " + getRandomString();
		if ((options & EFS.APPEND) != 0) {
			contents += write;
		} else {
			contents = write;
		}
		outputStream.write(write.getBytes());
		outputStream.close();

		readBytes = new byte[contents.length()];
		inputStream = childFS.openInputStream(EFS.NONE, new NullProgressMonitor());
		inputStream.read(readBytes);

		input = new String(readBytes);
		inputStream.close();
		assertTrue(systemType + ": Contents incorrect writing to a non-empty file.  Expected Contents: " + contents + " Actual Contents: " + input, contents.equals(input));
		// Cleanup, so IFileStore uses the correct connection next time.
		cleanup();
	}

	/**
	 * Adapt a local file system path such that it can be used as
	 * path in an URI. Converts path delimiters do '/' default
	 * delimiter, and adds a slash in front if necessary.
	 *
	 * Copied from RSEFileSystemContributor as it's private
	 * @param path the path to adapt
	 * @return adapted path
	 */
	private String fixPathForURI(String path) {
		String sep = PathUtility.getSeparator(path);
		if (!sep.equals("/")) { //$NON-NLS-1$
			path = path.replace(sep.charAt(0), '/');
		}
		//<adapted from org.eclipse.core.filesystem.URIUtil.toURI() Copyright(c) 2005, 2006 IBM>
		final int length = path.length();
		StringBuffer pathBuf = new StringBuffer(length + 3);
		//There must be a leading slash in a hierarchical URI
		if (length > 0 && (path.charAt(0) != '/'))
			pathBuf.append('/');
		//additional double-slash for UNC paths to distinguish from host separator
		if (path.startsWith("//")) //$NON-NLS-1$
			pathBuf.append('/').append('/');
		pathBuf.append(path);
		//</adapted from org.eclipse.core.filesystem.URIUtil.toURI() Copyright(c) 2005, 2006 IBM>
		return pathBuf.toString();
	}

	public void cleanup() throws Exception {
		if (host != null) {
			if (tempDirectory != null) {
				IRemoteFileSubSystem fss = getRemoteFileSubSystem(host);
				fss.delete(tempDirectory, new NullProgressMonitor());
				fss.disconnect();
				tempDirectory = null;
			}
			getConnectionManager().removeConnection(host.getSystemProfile().getName(), host.getName());
			host = null;
		}
	}

	public void tearDown() throws Exception {
		cleanup();
		super.tearDown();
	}
}