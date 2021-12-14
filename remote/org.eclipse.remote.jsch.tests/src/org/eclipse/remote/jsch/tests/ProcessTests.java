package org.eclipse.remote.jsch.tests;

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
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteProcessAdapter;
import org.eclipse.remote.internal.jsch.core.JSchConnection;

public class ProcessTests extends TestCase {
	private static final String USERNAME = "test"; //$NON-NLS-1$
	private static final String PASSWORD = ""; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$
	private static int NUM_THREADS = 1; // Test currently fails for more than one thread

	private IRemoteConnectionType fConnectionType;
	private IRemoteConnection fRemoteConnection;

	public void testConcurrentProcess() {
		Thread[] threads = new Thread[NUM_THREADS];

		for (int t = 0; t < NUM_THREADS; t++) {
			final String threadNum = Integer.toString(t);
			Thread thread = new Thread("test thread " + t) {
				@Override
				public void run() {
					final Set<String> results = Collections.synchronizedSet(new HashSet<String>());
					IRemoteProcessService processService = fRemoteConnection.getService(IRemoteProcessService.class);
					assertNotNull(processService);
					IRemoteProcessBuilder builder = processService.getProcessBuilder("perl", "-v", threadNum); //$NON-NLS-1$
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
	}

	public void testEnv() {
		IRemoteProcessService processService = fRemoteConnection.getService(IRemoteProcessService.class);
		assertNotNull(processService);
		IRemoteProcessBuilder builder = processService.getProcessBuilder("printenv"); //$NON-NLS-1$
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
				assertNull(result);
				result = line;
			}
			assertEquals(result, "PATH=" + path);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	public void testEcho() {
		IRemoteProcessService processService = fRemoteConnection.getService(IRemoteProcessService.class);
		assertNotNull(processService);
		IRemoteProcessBuilder builder = processService.getProcessBuilder("cat"); //$NON-NLS-1$
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

	public void testExitValue() {
		IRemoteProcessService processService = fRemoteConnection.getService(IRemoteProcessService.class);
		assertNotNull(processService);
		IRemoteProcessBuilder builder = processService.getProcessBuilder(new String[] { "sleep", "60" }); //$NON-NLS-1$
		assertNotNull(builder);
		IRemoteProcess rp = null;
		try {
			rp = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
		assertNotNull(rp);
		Process p = new RemoteProcessAdapter(rp);
		try {
			p.exitValue();
			fail("Process has not exited. Should throws an IllegalThreadStateException exception");
		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
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
