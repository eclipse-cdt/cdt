package org.eclipse.remote.core.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class ConnectionTests extends TestCase {
	private static final String USERNAME = "user"; //$NON-NLS-1$
	private static final String PASSWORD = "password"; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;

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

		IRemoteProcessBuilder builder = fRemoteServices.getProcessBuilder(fRemoteConnection, "env"); //$NON-NLS-1$
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

	public void testCopy() {
		final IRemoteFileManager fileManager = fRemoteServices.getFileManager(fRemoteConnection);

		final IFileSystem fileSystem = EFS.getLocalFileSystem();
		final IFileStore srcFileStore = fileSystem.getStore(new Path("/tmp/log_src.txt"));
		final IFileStore dstFileStore = fileManager.getResource("/tmp").getChild("log_dst.txt");
		try {
			srcFileStore.delete(EFS.NONE, new NullProgressMonitor());
			dstFileStore.delete(EFS.NONE, new NullProgressMonitor());
			OutputStream stream = srcFileStore.openOutputStream(EFS.NONE, new NullProgressMonitor());
			stream.write(new byte[] { 'f', 'o', 'o', '\n' });
			stream.close();
			srcFileStore.copy(dstFileStore, EFS.NONE, new NullProgressMonitor());
		} catch (CoreException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		IFileInfo srcInfo = srcFileStore.fetchInfo();
		IFileInfo dstInfo = dstFileStore.fetchInfo();
		assertTrue(dstInfo.exists());
		assertTrue(srcInfo.getLength() == dstInfo.getLength());
		try {
			InputStream stream = dstFileStore.openInputStream(EFS.NONE, new NullProgressMonitor());
			byte[] b = new byte[4];
			stream.read(b);
			stream.close();
			assertTrue(b[0] == 'f');
			assertTrue(b[1] == 'o');
			assertTrue(b[2] == 'o');
			assertTrue(b[3] == '\n');
		} catch (CoreException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			srcFileStore.delete(EFS.NONE, new NullProgressMonitor());
			dstFileStore.delete(EFS.NONE, new NullProgressMonitor());
		} catch (CoreException e) {
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
		IRemoteProcessBuilder builder = fRemoteServices.getProcessBuilder(fRemoteConnection, "pwd"); //$NON-NLS-1$
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
		final IRemoteFileManager fileManager = fRemoteServices.getFileManager(fRemoteConnection);
		builder = fRemoteServices.getProcessBuilder(fRemoteConnection, "pwd"); //$NON-NLS-1$
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

		IRemoteConnectionManager connMgr = fRemoteServices.getConnectionManager();
		assertNotNull(connMgr);

		try {
			fRemoteConnection = connMgr.newConnection("test_connection"); //$NON-NLS-1$
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
		IRemoteConnectionManager connMgr = fRemoteServices.getConnectionManager();
		assertNotNull(connMgr);
		connMgr.removeConnection(fRemoteConnection);
	}

}
