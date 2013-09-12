package org.eclipse.remote.core.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class ProcessTests extends TestCase {
	private static final String USERNAME = "test"; //$NON-NLS-1$
	private static final String PASSWORD = ""; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$
	private static int NUM_THREADS = 2;

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;

	public void testConcurrentProcess() {
		Thread[] threads = new Thread[NUM_THREADS];
		final Set<String> results = Collections.synchronizedSet(new HashSet<String>());

		for (int t = 0; t < NUM_THREADS; t++) {
			final String threadNum = Integer.toString(t);
			Thread thread = new Thread("test thread " + t) {
				@Override
				public void run() {
					IRemoteProcessBuilder builder = fRemoteConnection.getProcessBuilder("perl", "-v", threadNum); //$NON-NLS-1$
					assertNotNull(builder);
					builder.redirectErrorStream(true);
					for (int i = 0; i < 10; i++) {
						try {
							IRemoteProcess proc = builder.start();
							BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
							String line;
							while ((line = stdout.readLine()) != null) {
								results.add(line);
								results.add("\n");
							}
							try {
								proc.waitFor();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							assertTrue(results.toString().contains("Larry Wall"));
						} catch (IOException e) {
							e.printStackTrace();
							fail(e.getLocalizedMessage());
						}
					}
				}

			};
			thread.start();
			threads[t] = thread;
		}
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
			}
		}
		assertTrue(results.size() == 1);
	}

	public void testEnv() {
		IRemoteProcessBuilder builder = fRemoteConnection.getProcessBuilder("printenv"); //$NON-NLS-1$
		assertNotNull(builder);
		builder.redirectErrorStream(true);
		String path = builder.environment().get("PATH");
		builder.environment().clear();
		builder.environment().put("PATH", path);
		try {
			IRemoteProcess proc = builder.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			String result = null;
			while ((line = stdout.readLine()) != null) {
				assertTrue(result == null);
				result = line;
			}
			assertEquals(result, "PATH=" + path);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	public void testEcho() {
		IRemoteProcessBuilder builder = fRemoteConnection.getProcessBuilder("cat"); //$NON-NLS-1$
		assertNotNull(builder);
		builder.redirectErrorStream(true);
		final StringBuffer result = new StringBuffer();
		try {
			final IRemoteProcess proc = builder.start();
			Thread readerThread = new Thread("echo reader thread") {
				@Override
				public void run() {
					try {
						BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
						String line;
						while ((line = stdout.readLine()) != null) {
							result.append(line);
						}
						try {
							proc.waitFor();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
						fail(e.getLocalizedMessage());
					}
				}

			};
			Thread writerThread = new Thread("echo writer thread") {
				@Override
				public void run() {
					try {
						BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
						for (int i = 0; i < 10; i++) {
							String line = i + "\n";
							stdin.append(line);
							stdin.flush();
						}
						proc.getOutputStream().close();
						try {
							proc.waitFor();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
						fail(e.getLocalizedMessage());
					}
				}

			};
			writerThread.start();
			readerThread.start();
			writerThread.join();
			readerThread.join();
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}

		assertEquals("0123456789", result.toString());
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
			fail(e.getLocalizedMessage());
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
			fail(e.getLocalizedMessage());
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
