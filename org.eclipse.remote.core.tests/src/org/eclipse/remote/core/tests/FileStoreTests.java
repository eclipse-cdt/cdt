package org.eclipse.remote.core.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;

import junit.framework.TestCase;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class FileStoreTests extends TestCase {
	private static final String CONNECTION_NAME = "test_connection";
	private static final String USERNAME = "test";
	private static final String PASSWORD = "";
	private static final String HOST = "localhost";
	private static final String LOCAL_DIR = "/tmp/ptp_" + System.getProperty("user.name") + "/filestore_tests";
	private static final String REMOTE_DIR = "/tmp/ptp_" + USERNAME + "/filestore_tests";
	private static final String LOCAL_FILE = "local_file";
	private static final String REMOTE_FILE = "remote_file";
	private static final String TEST_STRING = "a string containing fairly *()(*&^$%## random text";

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;
	private IRemoteConnectionManager fRemoteConnectionManager;
	private IRemoteFileManager fRemoteFileManager;
	private IFileStore fRemoteDir;
	private IFileStore fLocalDir;

	public void testStreams() {
		IFileStore remoteFileStore = fRemoteDir.getChild(REMOTE_FILE);

		for (int i = 0; i < 5; i++) {
			try {
				remoteFileStore.delete(EFS.NONE, null);
			} catch (CoreException e) {
				fail(e.getMessage());
			}

			assertFalse(remoteFileStore.fetchInfo().exists());

			try {
				OutputStream stream = remoteFileStore.openOutputStream(EFS.NONE, null);
				assertNotNull(stream);
				BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(stream));
				buf.write(TEST_STRING);
				buf.close();
			} catch (Exception e) {
				fail(e.getMessage());
			}

			assertTrue(remoteFileStore.fetchInfo().exists());

			try {
				InputStream stream = remoteFileStore.openInputStream(EFS.NONE, null);
				assertNotNull(stream);
				BufferedReader buf = new BufferedReader(new InputStreamReader(stream));
				String line = buf.readLine().trim();
				assertTrue(line.equals(TEST_STRING));
				buf.close();
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}
	}

	public void testCopy() {
		final IFileStore localFileStore = fLocalDir.getChild(LOCAL_FILE);
		final IFileStore remoteFileStore = fRemoteDir.getChild(REMOTE_FILE);
		try {
			localFileStore.delete(EFS.NONE, new NullProgressMonitor());
			remoteFileStore.delete(EFS.NONE, new NullProgressMonitor());
			OutputStream stream = localFileStore.openOutputStream(EFS.NONE, new NullProgressMonitor());
			stream.write(new byte[] { 'f', 'o', 'o', '\n' });
			stream.close();
			localFileStore.copy(remoteFileStore, EFS.NONE, new NullProgressMonitor());
		} catch (CoreException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		IFileInfo srcInfo = localFileStore.fetchInfo();
		IFileInfo dstInfo = remoteFileStore.fetchInfo();
		assertTrue(dstInfo.exists());
		assertTrue(srcInfo.getLength() == dstInfo.getLength());
		try {
			InputStream stream = remoteFileStore.openInputStream(EFS.NONE, new NullProgressMonitor());
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		fRemoteServices = RemoteServices.getRemoteServices("org.eclipse.remote.JSch");
		assertNotNull(fRemoteServices);

		fRemoteConnectionManager = fRemoteServices.getConnectionManager();
		assertNotNull(fRemoteConnectionManager);

		try {
			fRemoteConnection = fRemoteConnectionManager.newConnection(CONNECTION_NAME);
		} catch (RemoteConnectionException e) {
			fail(e.getMessage());
		}
		assertNotNull(fRemoteConnection);
		IRemoteConnectionWorkingCopy wc = fRemoteConnection.getWorkingCopy();
		wc.setAddress(HOST);
		wc.setUsername(USERNAME);
		wc.setPassword(PASSWORD);
		wc.save();

		try {
			fRemoteConnection.open(new NullProgressMonitor());
		} catch (RemoteConnectionException e) {
			fail(e.getMessage());
		}
		assertTrue(fRemoteConnection.isOpen());

		fRemoteFileManager = fRemoteConnection.getFileManager();
		assertNotNull(fRemoteFileManager);

		URI remoteDirUri = fRemoteFileManager.toURI(REMOTE_DIR);
		URI localDirUri = fRemoteFileManager.toURI(LOCAL_DIR);
		assertNotNull(remoteDirUri);
		assertNotNull(localDirUri);

		try {
			fRemoteDir = EFS.getStore(fRemoteFileManager.toURI(REMOTE_DIR));
			fLocalDir = EFS.getLocalFileSystem().getStore(new Path(LOCAL_DIR));
		} catch (CoreException e) {
			fail(e.getMessage());
		}

		try {
			fRemoteDir.mkdir(EFS.NONE, null);
			fLocalDir.mkdir(EFS.NONE, null);
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		fRemoteDir.delete(EFS.NONE, new NullProgressMonitor());
		fLocalDir.delete(EFS.NONE, new NullProgressMonitor());
		fRemoteConnection.close();
		fRemoteConnectionManager.removeConnection(fRemoteConnection);
	}

}
