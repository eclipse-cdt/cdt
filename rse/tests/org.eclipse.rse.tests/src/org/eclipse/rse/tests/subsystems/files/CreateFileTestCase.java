/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: Kevin Doyle.
 *  
 * Contributors:
 * Martin Oberhuber (Wind River) - [cleanup] Avoid using SystemStartHere in production code
 ********************************************************************************/

package org.eclipse.rse.tests.subsystems.files;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.core.model.SystemRegistry;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class CreateFileTestCase extends FileServiceBaseTest {

	private String SYSTEM_ADDRESS = "sles8rm";//"SLES8RM";
	private String USER_ID = "xxxxxx";
	private String PASSWORD = "xxxxxx"; //"xxxxxx";
	private IHost host;
	//TODO: See if additional characters in the name should work.
	// Also make sure if there are that they can be entered in the New
	// File Dialog.  This string can be so using this for base test.
	private String fileName = "a !@#${a}'%^&()_ =[]~+-'`;,.txt"; //$NON-NLS-1$
	private IRemoteFile tempDirectory = null;
	
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
	
	public void testCreateFile() throws Exception {
		host = getFTPHost();
		createFileAndAssertProperties();
		
		host = getDStoreHost();
		createFileAndAssertProperties();
		
		host = getSSHHost();
		createFileAndAssertProperties();
	}
	
	public void createFileAndAssertProperties() throws Exception {
		String SYSTEM_TYPE = host.getSystemType().getLabel();
		FileServiceSubSystem inputFss = (FileServiceSubSystem) getRemoteFileSubSystem(host);
		
		// Need to create a temporary directory for the new file to be created in.
		// this is to ensure we don't overwrite any previous files.
		inputFss.connect(new NullProgressMonitor(), false);
		IRemoteFile homeDirectory = inputFss.getRemoteFileObject(".", new NullProgressMonitor());
		String baseFolderName = "rsetest";
		String homeFolderName = homeDirectory.getAbsolutePath();
		String testFolderName = FileServiceHelper.getRandomLocation(inputFss, homeFolderName, baseFolderName, new NullProgressMonitor());
		tempDirectory = createFileOrFolder(inputFss, homeFolderName, testFolderName, true);
		
		tempDirPath = tempDirectory.getAbsolutePath();
		IHostFile hostfile = inputFss.getFileService().createFile(tempDirPath, fileName, new NullProgressMonitor());
		assertTrue(SYSTEM_TYPE + ": hostfile doesn't exist.", hostfile.exists());
		assertTrue(SYSTEM_TYPE + ": hostfile canRead returns false", hostfile.canRead());
		assertTrue(SYSTEM_TYPE + ": hostfile canWrite returns false", hostfile.canWrite());
		assertEquals(SYSTEM_TYPE + ": filename does not match.", fileName, hostfile.getName());
		assertEquals(SYSTEM_TYPE + ": path's to file do not match.", tempDirPath, hostfile.getParentPath());
		// Make sure the file is empty
		assertEquals(SYSTEM_TYPE + ": file size's do not match.", 0, hostfile.getSize());
		long modDate = hostfile.getModifiedDate();
		assertTrue(SYSTEM_TYPE + ": modification date is not greater than 0.", modDate > 0);
		
		// perform cleanup, so EFS uses the right file service next time
		cleanup();
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
 
