package org.eclipse.remote.jsch.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.internal.jsch.core.JSchConnection;

import junit.framework.TestCase;

public class FileStoreTests extends TestCase {
	private static final String CONNECTION_NAME = "test_connection";
	private static final String USERNAME = "test";
	private static final String PASSWORD = "";
	private static final String HOST = "localhost";
	private static final String LOCAL_DIR = "/tmp/ptp_" + System.getProperty("user.name") + "/filestore_tests";
	private static final String REMOTE_DIR = "/tmp/ptp_" + USERNAME + "/filestore_tests";
	private static final String LOCAL_FILE = "local_file";
	private static final String REMOTE_FILE = "remote_file";
	private static final String REMOTE_FILE2 = "remote_file2";
	private static final String TEST_STRING = "a string containing fairly *()(*&^$%## random text";
	private static final String TEST_STRING2 = "a different string containing fairly *()(*&^$%## random text";

	private IRemoteConnectionType fConnectionType;
	private IRemoteConnection fRemoteConnection;
	private IRemoteFileService fRemoteFileManager;
	private IFileStore fRemoteDir;
	private IFileStore fLocalDir;

	private void createFile(IFileStore fileStore, String contents) throws CoreException, IOException {
		OutputStream stream = fileStore.openOutputStream(EFS.NONE, new NullProgressMonitor());
		assertNotNull(stream);
		BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(stream));
		buf.write(contents);
		buf.close();
	}

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
				createFile(remoteFileStore, TEST_STRING);
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

	public void testMultiStreams() {
		IFileStore remoteFileStore = fRemoteDir.getChild(REMOTE_FILE);
		IFileStore remoteFileStore2 = fRemoteDir.getChild(REMOTE_FILE2);

		try {
			createFile(remoteFileStore, TEST_STRING);
			createFile(remoteFileStore2, TEST_STRING2);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(remoteFileStore.fetchInfo().exists());
		assertTrue(remoteFileStore2.fetchInfo().exists());

		/*
		 * Check how many streams we can open
		 */
		InputStream streams[] = new InputStream[100];
		int streamCount = 0;

		for (; streamCount < streams.length; streamCount++) {
			try {
				streams[streamCount] = remoteFileStore.openInputStream(EFS.NONE, null);
			} catch (Exception e) {
				if (!e.getMessage().endsWith("channel is not opened.")) {
					fail(e.getMessage());
				}
				break;
			}
		}

		for (int i = 0; i < streamCount; i++) {
			try {
				streams[i].close();
			} catch (IOException e) {
				// No need to deal with this
			}
		}

		for (int i = 0; i < streamCount / 2; i++) {
			try {
				InputStream stream = remoteFileStore.openInputStream(EFS.NONE, null);
				assertNotNull(stream);
				BufferedReader buf = new BufferedReader(new InputStreamReader(stream));
				String line = buf.readLine().trim();
				assertTrue(line.equals(TEST_STRING));

				InputStream stream2 = remoteFileStore2.openInputStream(EFS.NONE, null);
				assertNotNull(stream2);
				BufferedReader buf2 = new BufferedReader(new InputStreamReader(stream2));
				String line2 = buf2.readLine().trim();
				assertTrue(line2.equals(TEST_STRING2));

				stream.close();
				stream2.close();
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
			createFile(localFileStore, "foo\n");
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

	public void testExecutable() {
		IFileStore fs = fRemoteDir.getChild(REMOTE_FILE);
		try {
			fs.delete(EFS.NONE, new NullProgressMonitor());
			createFile(fs, "contents");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		IFileInfo fi = fs.fetchInfo();
		boolean current = fi.getAttribute(EFS.ATTRIBUTE_EXECUTABLE);
		boolean expected = !current;
		fi.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, expected);
		try {
			fs.putInfo(fi, EFS.SET_ATTRIBUTES, new NullProgressMonitor());
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		fs = fRemoteDir.getChild(REMOTE_FILE);
		fi = fs.fetchInfo();
		assertEquals(expected, fi.getAttribute(EFS.ATTRIBUTE_EXECUTABLE));
	}

	@Override
	protected void setUp() throws Exception {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		fConnectionType = manager.getConnectionType("org.eclipse.remote.JSch");
		assertNotNull(fConnectionType);

		IRemoteConnectionWorkingCopy wc = fConnectionType.newConnection(CONNECTION_NAME);

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

		fRemoteFileManager = fRemoteConnection.getService(IRemoteFileService.class);
		assertNotNull(fRemoteFileManager);

		URI remoteDirUri = fRemoteFileManager.toURI(REMOTE_DIR);
		URI localDirUri = fRemoteFileManager.toURI(LOCAL_DIR);
		assertNotNull(remoteDirUri);
		assertNotNull(localDirUri);

		fRemoteDir = EFS.getStore(fRemoteFileManager.toURI(REMOTE_DIR));
		fLocalDir = EFS.getLocalFileSystem().getStore(new Path(LOCAL_DIR));

		fRemoteDir.mkdir(EFS.NONE, null);
		fLocalDir.mkdir(EFS.NONE, null);
	}

	@Override
	protected void tearDown() throws Exception {
		fRemoteDir.delete(EFS.NONE, new NullProgressMonitor());
		fLocalDir.delete(EFS.NONE, new NullProgressMonitor());
		fConnectionType.removeConnection(fRemoteConnection);
	}

}
