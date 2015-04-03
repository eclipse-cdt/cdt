package org.eclipse.remote.core.tests.internal;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;

import junit.framework.TestCase;

public class ServiceTests extends TestCase {
	private static final String USERNAME = "greg"; //$NON-NLS-1$
	private static final String PASSWORD = ""; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$

	private IRemoteConnectionType fConnectionType;
	private IRemoteConnection fRemoteConnection;

	public void testHostService() {
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

		hostService = fRemoteConnection.getService(IRemoteConnectionHostService.class);
		assertNotNull(hostService);

		assertEquals(hostService.getHostname(), host);
		assertEquals(hostService.getUsername(), username);
	}

	@Override
	protected void setUp() throws Exception {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		fConnectionType = manager.getConnectionType("org.eclipse.remote.JSch"); //$NON-NLS-1$
		assertNotNull(fConnectionType);
	}

	@Override
	protected void tearDown() throws Exception {
		fConnectionType.removeConnection(fRemoteConnection);
	}

}
