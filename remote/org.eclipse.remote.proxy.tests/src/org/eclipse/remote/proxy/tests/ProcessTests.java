package org.eclipse.remote.proxy.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteProcessAdapter;

import junit.framework.TestCase;

public class ProcessTests extends TestCase {
	private static final String CONNECTION_NAME = "test_connection";
	private static final int NUM_THREADS = 1;

	private static IRemoteConnection connection;
	private static IRemoteProcessService processService;

	private boolean threadFailed = false;

	public void testStreamHalfClose() {
		IRemoteProcessBuilder builder = processService.getProcessBuilder("perl", "-v"); //$NON-NLS-1$
		try {
			final Set<String> results = new HashSet<String>();
			IRemoteProcess proc = builder.start();
			proc.getOutputStream().close(); // close stdin to make sure half closed channel works
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
			fail(e.getMessage());
		}

	}

	public void testConcurrentProcess() {
		Thread[] threads = new Thread[NUM_THREADS];

		for (int t = 0; t < NUM_THREADS; t++) {
			final String threadNum = Integer.toString(t);
			Thread thread = new Thread("test thread " + t) {
				@Override
				public void run() {
					final Set<String> results = Collections.synchronizedSet(new HashSet<String>());
					IRemoteProcessBuilder builder = processService.getProcessBuilder("perl", "-v"); //$NON-NLS-1$
					assertNotNull(builder);
					//					builder.redirectErrorStream(true);
					for (int i = 0; i < 1; i++) {
						try {
							IRemoteProcess proc = builder.start();
							BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
							String line;
							while ((line = stdout.readLine()) != null) {
								System.out.println("read " + line);
								results.add(line);
								results.add("\n");
							}
							try {
								proc.waitFor();
							} catch (InterruptedException e) {
								fail(e.getMessage());
							}
							if (!results.toString().contains("Larry Wall")) {
								threadFailed = true;
							}
							assertTrue(results.toString().contains("Larry Wall"));
						} catch (IOException e) {
							fail(e.getMessage());
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
		assertFalse(threadFailed);
	}

	public void testEnv() {
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
				break;
			}
			assertEquals("PATH=" + path, result);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	public void testEcho() {
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
						String line = stdout.readLine();
						int count = Integer.parseInt(line);
						for (int i = 0; i < count; i++) {
							line = stdout.readLine();
							if (line == null) {
								break;
							}
							result.append(line);
							System.out.println(line);
						}
						try {
							proc.destroy();
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
						int count = 10;
						String line = count + "\n";
						stdin.write(line);
						stdin.flush();
						for (int i = 0; i < count; i++) {
							line = i + "\n";
							stdin.write(line);
							stdin.flush();
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
		IRemoteProcessBuilder builder = processService.getProcessBuilder(new String[] { "sleep", "50" }); //$NON-NLS-1$
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
			// Ok
		}
		try {
			p.destroyForcibly();
			p.waitFor();
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		assertFalse(p.isAlive());
	}

	@Override
	protected void setUp() throws Exception {
		if (connection == null) {
			IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
			IRemoteConnectionType connType = manager.getConnectionType("org.eclipse.remote.Proxy"); //$NON-NLS-1$
			assertNotNull(connType);
			IRemoteConnectionWorkingCopy wc = connType.newConnection(CONNECTION_NAME);
			IRemoteConnectionHostService host = wc.getService(IRemoteConnectionHostService.class);
			host.setHostname("localhost");
			//			host.setHostname("titan-ext1.ccs.ornl.gov");
			host.setUsername("gw6");
			connection = wc.save();
			assertNotNull(connection);
			connection.open(new NullProgressMonitor());
			assertTrue(connection.isOpen());
			processService = connection.getService(IRemoteProcessService.class);
			assertNotNull(processService);
		}
	}

	@Override
	protected void tearDown() throws Exception {
	}

}
