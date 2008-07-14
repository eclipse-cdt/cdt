/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Xuan Chen (IBM) - initial API and implementation
 * Martin Oberhuber (Wind River) - Fix Javadoc warnings
 * Martin Oberhuber (Wind River) - [195402] Add constructor with test name
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import junit.framework.TestSuite;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

public class FileServiceArchiveTestDStoreWindows extends FileServiceArchiveTest {

	private boolean fPreference_ALERT_SSL;
	private boolean fPreference_ALERT_NONSSL;

	/**
	 * Constructor with specific test name.
	 * @param name test to execute
	 */
	public FileServiceArchiveTestDStoreWindows(String name) {
		super(name);
		setTargetName("windows");
	}

	public static junit.framework.Test suite() {

		TestSuite suite = new TestSuite("FileServiceArchiveTestDStoreWindows");

		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyBatchToArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyBatchToVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyBatchToVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyBatchVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyBatchVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyToArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyToVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyToVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyVirtualBatchToArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyVirtualBatchToVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyVirtualBatchToVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCopyVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testCreateZipFile")); //$NON-NLS-1$
		//suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testDeleteVirtualFileBigZip")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testMoveToArchiveFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testMoveToVirtualFileLevelOne")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testMoveToVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testMoveVirtualFile")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testMoveVirtualFileLevelTwo")); //$NON-NLS-1$
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testRenameVirtualFile")); //$NON-NLS-1$
		//suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testRenameVirtualFileBigZip")); //$NON-NLS-1$

		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStoreWindows.class, "testSuperTransferLocalToRemote"));
		suite.addTest(TestSuite.createTest(FileServiceArchiveTestDStore.class, "testSuperTransferDStoreToLocal"));
		return suite;
	}

	protected void setupFileSubSystem() {

		IHost dstoreHost = getWindowsHost();
		assertTrue(dstoreHost != null);
		ISystemRegistry sr = SystemStartHere.getSystemRegistry();
		ISubSystem[] ss = sr.getServiceSubSystems(dstoreHost, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				fss = (IFileServiceSubSystem)ss[i];
				fs = fss.getFileService();
			}
		}

		IHost localHost = getLocalSystemConnection();
		sr = SystemStartHere.getSystemRegistry();
		ss = sr.getServiceSubSystems(localHost, IFileService.class);
		for (int i=0; i<ss.length; i++) {
			if (ss[i] instanceof IFileServiceSubSystem) {
				localFss = (IFileServiceSubSystem)ss[i];
			}
		}

		try
		{
			IConnectorService connectionService = fss.getConnectorService();

			//If you want to connect to a running server, uncomment the following statements
			/*
			IServerLauncherProperties properties = connectionService.getRemoteServerLauncherProperties();

			if (properties instanceof IRemoteServerLauncher)
			{
				IRemoteServerLauncher sl = (IRemoteServerLauncher)properties;
				sl.setServerLaunchType(ServerLaunchType.get(ServerLaunchType.RUNNING));
				fss.getSubSystemConfiguration().updateSubSystem(fss, false, "xuanchen", true, 4033);
			}
			*/
			//End here.

			connectionService.acquireCredentials(false);
			connectionService.connect(mon);

		} catch(Exception e) {
			assertTrue("Exception creating temp dir " + e.getStackTrace().toString(), false); //$NON-NLS-1$
		}
	}

	public void tearDown() throws Exception {
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.ALERT_SSL, fPreference_ALERT_SSL);
		store.setValue(ISystemPreferencesConstants.ALERT_NONSSL, fPreference_ALERT_NONSSL);
		super.tearDown();
	}

}
