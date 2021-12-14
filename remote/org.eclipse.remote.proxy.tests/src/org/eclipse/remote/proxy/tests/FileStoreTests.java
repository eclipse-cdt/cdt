package org.eclipse.remote.proxy.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.UUID;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteServicesManager;

import junit.framework.TestCase;

public class FileStoreTests extends TestCase {
	private static final String CONNECTION_NAME = "test_connection";
	private static final String LOCAL_DIR = "/tmp/ptp_" + UUID.randomUUID();
	private static final String REMOTE_DIR = "/tmp/ptp_" + UUID.randomUUID();
	private static final String DIR_NAME = "filestore_tests";
	private static final String LOCAL_FILE = "local_file";
	private static final String REMOTE_FILE = "remote_file";
	private static final String REMOTE_FILE2 = "remote_file2";
	private static final String TEST_CONTENTS = "a string containing fairly *()(*&^$%##\n random text\n with some newlines\n";
	private static final String TEST_CONTENTS2 = "a different string containing \nfairly *()(*&^$%## random text\n with some newlines\n";

	private IFileStore remoteParent;
	private IFileStore localParent;
	private IFileStore remoteDir;
	private IFileStore localDir;

	private static IRemoteFileService fileService;
	private static IRemoteConnection connection;

	private void createFile(IFileStore fileStore, String contents) throws CoreException, IOException {
		OutputStream stream = fileStore.openOutputStream(EFS.NONE, new NullProgressMonitor());
		assertNotNull(stream);
		BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(stream));
		buf.write(contents);
		buf.close();
	}

	public void testStreams() {
		IFileStore remoteFileStore = remoteDir.getChild(REMOTE_FILE);

		for (int i = 0; i < 5; i++) {
			try {
				remoteFileStore.delete(EFS.NONE, null);
			} catch (CoreException e) {
				fail(e.getMessage());
			}

			assertFalse(remoteFileStore.fetchInfo().exists());

			try {
				createFile(remoteFileStore, TEST_CONTENTS);
			} catch (Exception e) {
				fail(e.getMessage());
			}

			assertTrue(remoteFileStore.fetchInfo().exists());

			try {
				InputStream stream = remoteFileStore.openInputStream(EFS.NONE, null);
				assertNotNull(stream);
				BufferedReader buf = new BufferedReader(new InputStreamReader(stream));
				String line = buf.readLine();
				assertEquals(line, TEST_CONTENTS.split("\n")[0]);

				buf.close();
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}
	}

	public void testMultiStreams() {
		IFileStore remoteFileStore = remoteDir.getChild(REMOTE_FILE);
		IFileStore remoteFileStore2 = remoteDir.getChild(REMOTE_FILE2);

		try {
			createFile(remoteFileStore, TEST_CONTENTS);
			createFile(remoteFileStore2, TEST_CONTENTS2);
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertTrue(remoteFileStore.fetchInfo().exists());
		assertTrue(remoteFileStore2.fetchInfo().exists());

		/*
		 * Check how many streams we can open
		 */
		InputStream streams[] = new InputStream[10];
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
				String line = buf.readLine();
				assertEquals(line, TEST_CONTENTS.split("\n")[0]);

				InputStream stream2 = remoteFileStore2.openInputStream(EFS.NONE, null);
				assertNotNull(stream2);
				BufferedReader buf2 = new BufferedReader(new InputStreamReader(stream2));
				String line2 = buf2.readLine();
				assertEquals(line2, TEST_CONTENTS2.split("\n")[0]);

				stream.close();
				stream2.close();
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}
	}

	public void testCopy() {
		final IFileStore localFileStore = localDir.getChild(LOCAL_FILE);
		final IFileStore remoteFileStore = remoteDir.getChild(REMOTE_FILE);
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
		IFileStore fs = remoteDir.getChild(REMOTE_FILE);
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
		fs = remoteDir.getChild(REMOTE_FILE);
		fi = fs.fetchInfo();
		assertEquals(expected, fi.getAttribute(EFS.ATTRIBUTE_EXECUTABLE));
	}

	public void xtestLargeFile() {
		IFileStore local = EFS.getLocalFileSystem().getStore(new Path("/usr/bin/php"));
		IFileStore remote = remoteDir.getChild("php.xxx");
		try {
			remote.delete(0, new NullProgressMonitor());
			InputStream inp = local.openInputStream(0, new NullProgressMonitor());
			OutputStream out = remote.openOutputStream(0, new NullProgressMonitor());
			byte[] b = new byte[1024];
			int len;
			while ((len = inp.read(b)) > 0) {
				out.write(b, 0, len);
			}
			inp.close();
			out.close();
			IFileInfo linfo = local.fetchInfo();
			IFileInfo rinfo = remote.fetchInfo();
			assertEquals(linfo.getLength(), rinfo.getLength());
		} catch (CoreException | IOException e) {
			fail(e.getMessage());
		}
	}

	public void testCopyLargeFile() {
		IFileStore local = EFS.getLocalFileSystem().getStore(new Path("/usr/bin/php"));
		IFileStore remote = remoteDir.getChild("php.xxx");
		try {
			local.copy(remote, EFS.OVERWRITE, new NullProgressMonitor());
			IFileInfo linfo = local.fetchInfo();
			IFileInfo rinfo = remote.fetchInfo();
			assertEquals(linfo.getLength(), rinfo.getLength());
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	@Override
	protected void setUp() throws Exception {
		if (connection == null) {
			IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
			IRemoteConnectionType connType = manager.getConnectionType("org.eclipse.remote.Proxy"); //$NON-NLS-1$
			assertNotNull(connType);
			IRemoteConnectionWorkingCopy wc = connType.newConnection(CONNECTION_NAME);
			IRemoteConnectionHostService host = wc.getService(IRemoteConnectionHostService.class);
			host.setHostname("titan-ext1.ccs.ornl.gov");
			//			host.setHostname("localhost");
			host.setUsername("gw6");
			connection = wc.save();
			assertNotNull(connection);
			connection.open(new NullProgressMonitor());
			assertTrue(connection.isOpen());
			fileService = connection.getService(IRemoteFileService.class);
			assertNotNull(fileService);
		}

		URI remoteDirUri = fileService.toURI(REMOTE_DIR);
		URI localDirUri = fileService.toURI(LOCAL_DIR);
		assertNotNull(remoteDirUri);
		assertNotNull(localDirUri);

		remoteParent = EFS.getStore(fileService.toURI(REMOTE_DIR));
		remoteDir = remoteParent.getChild(DIR_NAME);
		localParent = EFS.getLocalFileSystem().getStore(new Path(LOCAL_DIR));
		localDir = localParent.getChild(DIR_NAME);

		remoteDir.mkdir(EFS.NONE, null);
		localDir.mkdir(EFS.NONE, null);
	}

	@Override
	protected void tearDown() throws Exception {
		remoteParent.delete(EFS.NONE, new NullProgressMonitor());
		localParent.delete(EFS.NONE, new NullProgressMonitor());
	}

}
