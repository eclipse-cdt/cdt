package org.eclipse.remote.core.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;

import junit.framework.TestCase;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class FileStoreTests extends TestCase {
	private static final String CONNECTION_NAME = "test_connection";
	private static final String USERNAME = "user";
	private static final String PASSWORD = "password";
	private static final String HOST = "localhost";
	private static final String PATH1 = "/home/user/sftp_test";
	private static final String PATH2 = PATH1 + "/.file1";
	private static final String TEST_STRING = "a string containing fairly *()(*&^$%## random text";

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;
	private IRemoteFileManager fRemoteFileManager;

	public void testFileStore() {
		URI path1Uri = fRemoteFileManager.toURI(PATH1);
		URI path2Uri = fRemoteFileManager.toURI(PATH2);
		assertNotNull(path1Uri);
		assertNotNull(path2Uri);

		IFileStore store1 = null;
		IFileStore store2 = null;

		try {
			store1 = EFS.getStore(path1Uri);
			store2 = EFS.getStore(path2Uri);
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		for (int i = 0; i < 5; i++) {
			assertFalse(store1.fetchInfo().exists());
			try {
				store1.mkdir(EFS.NONE, null);
			} catch (CoreException e) {
				e.getLocalizedMessage();
			}
			assertTrue(store1.fetchInfo().exists());

			assertFalse(store2.fetchInfo().exists());
			try {
				OutputStream stream = store2.openOutputStream(EFS.NONE, null);
				assertNotNull(stream);
				BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(stream));
				buf.write(TEST_STRING);
				buf.close();
			} catch (Exception e) {
				e.getLocalizedMessage();
			}
			assertTrue(store2.fetchInfo().exists());

			try {
				InputStream stream = store2.openInputStream(EFS.NONE, null);
				assertNotNull(stream);
				BufferedReader buf = new BufferedReader(new InputStreamReader(stream));
				String line = buf.readLine().trim();
				assertTrue(line.equals(TEST_STRING));
				buf.close();
			} catch (Exception e) {
				e.getLocalizedMessage();
			}

			try {
				store2.delete(EFS.NONE, null);
			} catch (CoreException e) {
				e.getLocalizedMessage();
			}
			assertFalse(store2.fetchInfo().exists());

			try {
				store1.delete(EFS.NONE, null);
			} catch (CoreException e) {
				e.getLocalizedMessage();
			}
			assertFalse(store1.fetchInfo().exists());
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

		IRemoteConnectionManager connMgr = fRemoteServices.getConnectionManager();
		assertNotNull(connMgr);

		try {
			fRemoteConnection = connMgr.newConnection(CONNECTION_NAME);
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(fRemoteConnection);
		fRemoteConnection.setAddress(HOST);
		fRemoteConnection.setUsername(USERNAME);
		fRemoteConnection.setPassword(PASSWORD);

		fRemoteFileManager = fRemoteServices.getFileManager(fRemoteConnection);
		assertNotNull(fRemoteFileManager);
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
