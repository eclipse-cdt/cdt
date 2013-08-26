package org.eclipse.remote.core.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class ConnectionTests extends TestCase {
	private static final String USERNAME = "test"; //$NON-NLS-1$
	private static final String PASSWORD = ""; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;
	private IRemoteConnectionManager fRemoteConnectionManager;

	public void testEnv() {
		String var = fRemoteConnection.getEnv("SHELL"); //$NON-NLS-1$
		assertNotNull(var);

		var = fRemoteConnection.getEnv("FOO_VAR_SHOULD_NOT_BE_DEFINED"); //$NON-NLS-1$
		assertNull(var);

		assertNotNull(fRemoteConnection.getProperty("os.name")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("os.arch")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("os.version")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("file.separator")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("path.separator")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("line.separator")); //$NON-NLS-1$

		IRemoteProcessBuilder builder = fRemoteConnection.getProcessBuilder("env"); //$NON-NLS-1$
		assertNotNull(builder);
		builder.environment().put("FOO", "BAR"); //$NON-NLS-1$ //$NON-NLS-2$
		builder.environment().put("USER", "FOO"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			IRemoteProcess proc = builder.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			while ((line = stdout.readLine()) != null) {
				String[] kv = line.trim().split("="); //$NON-NLS-1$
				if (kv.length == 2) {
					if (kv[0].equals("FOO")) {
						assertTrue(kv[1].equals("BAR")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (kv[0].equals("USER")) {
						assertTrue(kv[1].equals("FOO")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	public void testWd() {
		/*
		 * Test connection wd
		 */
		String oldWd = fRemoteConnection.getWorkingDirectory();
		assertTrue(oldWd.startsWith("/"));
		String newWd = "/tmp";
		fRemoteConnection.setWorkingDirectory(newWd);
		assertTrue(fRemoteConnection.getWorkingDirectory().equals(newWd));

		/*
		 * Test process builder inherits wd from connection
		 */
		IRemoteProcessBuilder builder = fRemoteConnection.getProcessBuilder("pwd"); //$NON-NLS-1$
		assertNotNull(builder);
		try {
			IRemoteProcess proc = builder.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = stdout.readLine();
			proc.destroy();
			assertTrue(line != null && line.equals(newWd));
		} catch (IOException e) {
			fail(e.getMessage());
		}

		/*
		 * Test process builder wd
		 */
		final IRemoteFileManager fileManager = fRemoteConnection.getFileManager();
		assertNotNull(fileManager);
		builder = fRemoteConnection.getProcessBuilder("pwd"); //$NON-NLS-1$
		assertNotNull(builder);
		builder.directory(fileManager.getResource("/bin"));
		try {
			IRemoteProcess proc = builder.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = stdout.readLine();
			proc.destroy();
			assertTrue(line != null && line.equals("/bin"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		fRemoteServices = RemoteServices.getRemoteServices("org.eclipse.remote.JSch"); //$NON-NLS-1$
		assertNotNull(fRemoteServices);

		fRemoteConnectionManager = fRemoteServices.getConnectionManager();
		assertNotNull(fRemoteConnectionManager);

		try {
			fRemoteConnection = fRemoteConnectionManager.newConnection("test_connection"); //$NON-NLS-1$
		} catch (RemoteConnectionException e) {
			fail(e.getMessage());
		}
		assertNotNull(fRemoteConnection);
		fRemoteConnection.setAddress(HOST);
		fRemoteConnection.setUsername(USERNAME);
		fRemoteConnection.setPassword(PASSWORD);

		try {
			fRemoteConnection.open(new NullProgressMonitor());
		} catch (RemoteConnectionException e) {
			fail(e.getMessage());
		}
		assertTrue(fRemoteConnection.isOpen());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		fRemoteConnection.close();
		fRemoteConnectionManager.removeConnection(fRemoteConnection);
	}

}
