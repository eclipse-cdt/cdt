package org.eclipse.remote.jsch.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.internal.jsch.core.JSchConnection;

public class ConnectionTests extends TestCase {
	private static final String USERNAME = "test"; //$NON-NLS-1$
	private static final String PASSWORD = ""; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$

	private IRemoteConnectionType fConnectionType;
	private IRemoteConnection fRemoteConnection;

	public void testEnv() {
		IRemoteProcessService processService = fRemoteConnection.getService(IRemoteProcessService.class);
		assertNotNull(processService);
		String var = processService.getEnv("SHELL"); //$NON-NLS-1$
		assertNotNull(var);

		var = processService.getEnv("FOO_VAR_SHOULD_NOT_BE_DEFINED"); //$NON-NLS-1$
		assertNull(var);

		assertNotNull(fRemoteConnection.getProperty("os.name")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("os.arch")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("os.version")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("file.separator")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("path.separator")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("line.separator")); //$NON-NLS-1$

		IRemoteProcessBuilder builder = processService.getProcessBuilder("env"); //$NON-NLS-1$
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
		IRemoteProcessService processService = fRemoteConnection.getService(IRemoteProcessService.class);
		assertNotNull(processService);
		/*
		 * Test connection wd
		 */
		String oldWd = processService.getWorkingDirectory();
		assertTrue(oldWd.startsWith("/"));
		String newWd = "/tmp";
		processService.setWorkingDirectory(newWd);
		assertTrue(processService.getWorkingDirectory().equals(newWd));

		/*
		 * Test process builder inherits wd from connection
		 */
		IRemoteProcessBuilder builder = processService.getProcessBuilder("pwd"); //$NON-NLS-1$
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
		final IRemoteFileService fileManager = fRemoteConnection.getService(IRemoteFileService.class);
		assertNotNull(fileManager);
		builder = processService.getProcessBuilder("pwd"); //$NON-NLS-1$
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

	@Override
	protected void setUp() throws Exception {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		fConnectionType = manager.getConnectionType("org.eclipse.remote.JSch"); //$NON-NLS-1$
		assertNotNull(fConnectionType);

		IRemoteConnectionWorkingCopy wc = fConnectionType.newConnection("test_connection"); //$NON-NLS-1$

		String host = System.getenv("TEST_HOST");
		if (host == null) {
			host = HOST;
		}
		wc.setAttribute(JSchConnection.ADDRESS_ATTR, host);

		String username = System.getenv("TEST_USERNAME");
		if (username == null) {
			username = USERNAME;
		}
		wc.setAttribute(JSchConnection.USERNAME_ATTR, username);

		String password = System.getenv("TEST_PASSWORD");
		if (password == null) {
			password = PASSWORD;
		}
		wc.setSecureAttribute(JSchConnection.PASSWORD_ATTR, password);

		fRemoteConnection = wc.save();
		assertNotNull(fRemoteConnection);

		fRemoteConnection.open(new NullProgressMonitor());
		assertTrue(fRemoteConnection.isOpen());
	}

	@Override
	protected void tearDown() throws Exception {
		fConnectionType.removeConnection(fRemoteConnection);
	}

}
