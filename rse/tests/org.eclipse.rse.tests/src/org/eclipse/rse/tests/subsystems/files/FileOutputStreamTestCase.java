/********************************************************************************
* Copyright (c) 2007 IBM Corporation. All rights reserved.
* This program and the accompanying materials are made available under the terms
* of the Eclipse Public License v1.0 which accompanies this distribution, and is 
* available at http://www.eclipse.org/legal/epl-v10.html 
* 
* Initial Contributors:
* The following IBM employees contributed to the Remote System Explorer
* component that contains this file: Kevin Doyle
* 
* Contributors:
* {Name} (company) - description of contribution.
********************************************************************************/

package org.eclipse.rse.tests.subsystems.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.IRSESystemType;
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
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.internal.model.SystemRegistry;

public class FileOutputStreamTestCase extends FileServiceBaseTest {

	private String SYSTEM_ADDRESS = "sles8rm";//"SLES8RM";
	private String USER_ID = "xxxxxx";
	private String PASSWORD = "xxxxxx"; //"xxxxxx";
	private IHost host = null;
	private IRemoteFile tempDirectory;
	
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
	
	protected IHost getSSHHost()
	{
		IHost sshHost = null;

		String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID;
		String SYSTEM_NAME = SYSTEM_ADDRESS + "_ssh";

		sshHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, SYSTEM_ADDRESS, SYSTEM_NAME, USER_ID, PASSWORD);
		assertNotNull(sshHost);
		return sshHost;
	}
	
	protected IHost getFTPHost()
	{
		IHost ftpHost = null;

		String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_FTP_ONLY_ID;
		String SYSTEM_NAME = SYSTEM_ADDRESS + "_ftp";

		ftpHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, SYSTEM_ADDRESS, SYSTEM_NAME, USER_ID, PASSWORD);
		assertNotNull(ftpHost);
		return ftpHost;
	}
	
	protected IHost getLocalHost() {
		IHost localHost = null;

		String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_LOCAL_ID;
		String SYSTEM_NAME = SYSTEM_ADDRESS + "_local";

		localHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, "localhost", SYSTEM_NAME, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertNotNull(localHost);
		return localHost;
	}
	
	protected IHost getDStoreHost()
	{
		IHost dstoreHost = null;

		String SYSTEM_TYPE_ID = IRSESystemType.SYSTEMTYPE_LINUX_ID;
		String SYSTEM_NAME = SYSTEM_ADDRESS + "_dstore";

		//Ensure that the SSL acknowledge dialog does not show up. 
		//We need to setDefault first in order to set the value of a preference.  
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(ISystemPreferencesConstants.ALERT_SSL, ISystemPreferencesConstants.DEFAULT_ALERT_SSL);
		store.setDefault(ISystemPreferencesConstants.ALERT_NONSSL, ISystemPreferencesConstants.DEFAULT_ALERT_NON_SSL);

		store.setValue(ISystemPreferencesConstants.ALERT_SSL, false);
		store.setValue(ISystemPreferencesConstants.ALERT_NONSSL, false);

		dstoreHost = getRemoteSystemConnection(SYSTEM_TYPE_ID, SYSTEM_ADDRESS, SYSTEM_NAME, USER_ID, PASSWORD);
		assertNotNull(dstoreHost);
		
		return dstoreHost;
	}
	
	public void testRSEFileStoreAppendOutputStream() throws Exception {
		host = getLocalHost();
		outputStreamFileWriting(EFS.APPEND);
		
		host = getFTPHost();
		outputStreamFileWriting(EFS.APPEND);
		
		host = getDStoreHost();
		outputStreamFileWriting(EFS.APPEND);
		
		host = getSSHHost();
		outputStreamFileWriting(EFS.APPEND);
	}
	
	public void testRSEFileStoreOverwriteOutputStream() throws Exception {
		host = getLocalHost();
		outputStreamFileWriting(EFS.NONE);
		
		host = getFTPHost();
		outputStreamFileWriting(EFS.NONE);
		
		host = getDStoreHost();
		outputStreamFileWriting(EFS.NONE);
		
		host = getSSHHost();
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
			SystemRegistry.getInstance().deleteHost(host);
			host = null;
		}
	}
	
	public void tearDown() throws Exception {
		cleanup();
		super.tearDown();
	}
}