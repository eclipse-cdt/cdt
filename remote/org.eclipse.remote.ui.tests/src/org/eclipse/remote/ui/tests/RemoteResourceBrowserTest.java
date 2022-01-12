/*
 * Copyright (c) 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   IBM Corporation - initial API and implementation
 */
package org.eclipse.remote.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.remote.ui.dialogs.RemoteResourceBrowser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/*
 * Provides tests to several scenarios but they should be
 *  executed manually (i.e. browse and click OK)
 */
@RunWith(JUnit4.class)
public class RemoteResourceBrowserTest {
	private static final String USERNAME = "test"; //$NON-NLS-1$
	private static final String PASSWORD = ""; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$
	private static IRemoteConnectionType fConnectionType;
	private static IRemoteConnection fRemoteConnection;
	private static Shell shell = null;
	private RemoteResourceBrowser browser;
	private IFileStore expectedResource;

	@BeforeClass
	public static void setUp() {
		IRemoteServicesManager manager = RemoteUIPlugin.getService(IRemoteServicesManager.class);
		fConnectionType = manager.getConnectionType("org.eclipse.remote.JSch"); //$NON-NLS-1$
		assertNotNull(fConnectionType);
		IRemoteConnectionWorkingCopy wc = null;
		try {
			wc = fConnectionType.newConnection("test_connection");//$NON-NLS-1$
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}

		IRemoteConnectionHostService hostService = wc.getService(IRemoteConnectionHostService.class);
		assertNotNull(hostService);

		String host = System.getenv("TEST_HOST");
		if (host == null) {
			host = HOST;
		}
		hostService.setHostname(host);

		String username = System.getenv("TEST_USERNAME");
		if (username == null) {
			username = USERNAME;
		}
		hostService.setUsername(username);

		String password = System.getenv("TEST_PASSWORD");
		if (password == null) {
			password = PASSWORD;
		}
		hostService.setPassword(password);

		try {
			fRemoteConnection = wc.save();
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(fRemoteConnection);

		try {
			fRemoteConnection.open(new NullProgressMonitor());
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertTrue(fRemoteConnection.isOpen());

		shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		assertNotNull(shell);
	}

	@AfterClass
	public static void tearDown() throws RemoteConnectionException {
		fConnectionType.removeConnection(fRemoteConnection);
	}

	/*
	 * Select any file.
	 */
	@Test
	public void browseFileTest() {
		browser = new RemoteResourceBrowser(shell, SWT.SINGLE);
		browser.setConnection(fRemoteConnection);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setTitle("Allows to select file only");
		browser.open();
		expectedResource = browser.getResource();
		assertNotNull(expectedResource);
		assertTrue(!expectedResource.fetchInfo().isDirectory());
	}

	/*
	 * Select any directory.
	 */
	@Test
	public void browseDirectoryTest() {
		browser = new RemoteResourceBrowser(shell, SWT.SINGLE);
		browser.setConnection(fRemoteConnection);
		browser.setType(RemoteResourceBrowser.DIRECTORY_BROWSER);
		browser.setTitle("Allows to select directory only");
		browser.open();
		expectedResource = browser.getResource();
		assertNotNull(expectedResource);
		assertTrue(expectedResource.fetchInfo().isDirectory());
	}

	/*
	 * Select either file or directory.
	 */
	@Test
	public void browseResourceTest() {
		browser = new RemoteResourceBrowser(shell, SWT.SINGLE);
		browser.setConnection(fRemoteConnection);
		browser.setTitle("Allows to select either file or directory");
		browser.open();
		expectedResource = browser.getResource();
		assertNotNull(expectedResource);
	}

	/*
	 * Select more than one resource.
	 */
	@Test
	public void browseResourcesTest() {
		browser = new RemoteResourceBrowser(shell, SWT.MULTI);
		browser.setConnection(fRemoteConnection);
		browser.setTitle("Allows to select either multiple resources");
		browser.open();
		List<IFileStore> expectedResources = browser.getResources();
		assertNotNull(expectedResources);
		assertTrue(expectedResources.size() > 0);
	}

	/*
	 * Select to local connection and select a directory.
	 */
	@Test
	public void changeLocalConnectionTest() {
		browser = new RemoteResourceBrowser(shell, SWT.SINGLE);
		browser.setConnection(fRemoteConnection);
		browser.setTitle("Allows to switch to local browsing");
		browser.setType(RemoteResourceBrowser.DIRECTORY_BROWSER);
		browser.showConnections(true);
		browser.showLocalSelection(true);
		browser.open();
		expectedResource = browser.getResource();
		assertNotNull(expectedResource);
		assertEquals(expectedResource.getFileSystem().getScheme(), "file");
	}

	/*
	 * Initial path set.
	 */
	@Test
	public void setInitialPathTest() {
		String initialPath = "/tmp";
		browser = new RemoteResourceBrowser(shell, SWT.SINGLE);
		browser.setConnection(fRemoteConnection);
		browser.setTitle("Initial path set to " + initialPath);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(initialPath);
		browser.open();
	}

	/*
	 * Show connections.
	 * Don't show hidden check box and new folder button.
	 */
	@Test
	public void changeDefaultSettingsTest() {
		browser = new RemoteResourceBrowser(shell, SWT.SINGLE);
		browser.setConnection(fRemoteConnection);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.showConnections(true);
		browser.showHiddenCheckbox(false);
		browser.showNewFolderButton(false);
		browser.open();
	}
}
