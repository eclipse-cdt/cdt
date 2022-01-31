package org.eclipse.remote.proxy.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Base64;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.jsch.ui.UserInfoPrompter;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.exception.RemoteConnectionException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import junit.framework.TestCase;

public class ConnectionTests extends TestCase {

	private IRemoteConnectionType connType;

	class Context {
		private String line;
		private State state;
		private BufferedReader reader;
		private BufferedWriter writer;

		public Context(BufferedReader reader, BufferedWriter writer) {
			this.reader = reader;
			this.writer = writer;
			setState(States.INIT);
		}

		String getLine() {
			return line;
		}

		void setLine(String line) {
			this.line = line;
		}

		State getState() {
			return state;
		}

		void setState(State state) {
			this.state = state;
		}
	}

	interface State {
		/**
		   * @return true to keep processing, false to read more data.
		 */
		boolean process(Context context) throws IOException;
	}

	enum States implements State {
		INIT {
			@Override
			public boolean process(Context context) throws IOException {
				System.out.println("state=" + INIT);
				String line = context.reader.readLine();
				System.out.println("got " + line);
				if (line.equals("running")) {
					context.setState(States.CHECK);
					return true;
				}
				return false;
			}
		},
		CHECK {
			@Override
			public boolean process(Context context) throws IOException {
				System.out.println("state=" + CHECK);
				context.writer.write("check\n");
				context.writer.flush();
				String line = context.reader.readLine();
				String[] parts = line.split(":");
				switch (parts[0]) {
				case "ok":
					context.setState(States.START);
					return true;
				case "warning":
					context.setState(States.DOWNLOAD);
					return true;
				}
				System.out.println("fail:" + parts[1]);
				return false;
			}
		},
		DOWNLOAD {
			@Override
			public boolean process(Context context) throws IOException {
				System.out.println("state=" + DOWNLOAD);
				File file = new File("proxy.server-linux.gtk.x86_64.tar.gz");
				long count = file.length() / 510;
				System.out.println("download " + count);
				context.writer.write("download " + count + "\n");
				context.writer.flush();
				if (downloadFile(file, context.writer)) {
					String line = context.reader.readLine();
					String[] parts = line.split(":");
					switch (parts[0]) {
					case "ok":
						context.setState(States.START);
						return true;
					case "fail":
						System.out.println("fail:" + parts[1]);
						return false;
					}
				}
				return false;
			}

			private boolean downloadFile(File file, BufferedWriter writer) {
				try {
					Base64.Encoder encoder = Base64.getEncoder();
					FileInputStream in = new FileInputStream(file);
					byte[] buf = new byte[510]; // Multiple of 3
					int n;
					while ((n = in.read(buf)) >= 0) {
						if (n < 510) {
							writer.write(encoder.encodeToString(Arrays.copyOf(buf, n)) + "\n");
						} else {
							writer.write(encoder.encodeToString(buf));
						}
					}
					writer.flush();
					in.close();
					return true;
				} catch (IOException e) {
					return false;
				}
			}
		},
		START {
			@Override
			public boolean process(Context context) throws IOException {
				System.out.println("state=" + START);
				context.writer.write("start\n");
				context.writer.flush();
				return false;
			}
		}
	}

	public void testProxyConnection() {
		try {
			IJSchService jService = Activator.getService(IJSchService.class);
			Session session = jService.createSession("titan.ccs.ornl.gov", 22, "gw6");
			session.setConfig("PreferredAuthentications", "password,keyboard-interactive,gssapi-with-mic,publickey"); //$NON-NLS-1$ //$NON-NLS-2$
			new UserInfoPrompter(session);
			jService.connect(session, 0, new NullProgressMonitor());
			ChannelExec server = (ChannelExec) session.openChannel("exec");
			server.setCommand("/bin/sh");
			server.connect();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
			FileReader script = new FileReader("bootstrap.sh");
			BufferedReader scriptReader = new BufferedReader(script);
			String line;
			while ((line = scriptReader.readLine()) != null) {
				writer.write(line + "\n");
			}
			scriptReader.close();
			writer.flush();
			Context context = new Context(reader, writer);
			while (context.getState().process(context)) {
				// do state machine
			}
		} catch (JSchException | IOException e) {
			fail(e.getMessage());
		}

		//		try {
		//			final Process proc = Runtime.getRuntime().exec("java"
		//					+ " -cp /Users/gw6/Work/git/org.eclipse.remote/releng/org.eclipse.remote.proxy.server.product/target/products/proxy.server/macosx/cocoa/x86_64/Proxy.app/Contents/Eclipse/plugins/org.eclipse.equinox.launcher_1.3.200.v20160318-1642.jar"
		//					+ " org.eclipse.equinox.launcher.Main"
		//					+ " -application org.eclipse.remote.proxy.server.core.application"
		//					+ " -noExit");
		//			assertTrue(proc.isAlive());
		//
		//			new Thread("stderr") {
		//				private byte[] buf = new byte[1024];
		//				@Override
		//				public void run() {
		//					int n;
		//					BufferedInputStream err = new BufferedInputStream(proc.getErrorStream());
		//					try {
		//						while ((n = err.read(buf)) >= 0) {
		//							if (n > 0) {
		//								System.err.println("server: " + new String(buf, 0, n));
		//							}
		//						}
		//					} catch (IOException e) {
		//						// TODO Auto-generated catch block
		//						e.printStackTrace();
		//					}
		//				}
		//
		//			}.start();
		//
		//			IRemoteConnection conn = connType.newConnection("test");
		//			assertNotNull(conn);
		//			IRemoteProxyService proxy = conn.getService(IRemoteProxyService.class);
		//			assertNotNull(proxy);
		//			proxy.setStreams(proc.getInputStream(), proc.getOutputStream());
		//			conn.open(new NullProgressMonitor());
		//			conn.close();
		//
		//			proc.destroy();
		//			proc.waitFor();
		//			assertEquals(false, proc.isAlive());
		//		} catch (IOException | RemoteConnectionException | InterruptedException e) {
		//			fail(e.getMessage());
		//		}
	}

	private String executeSshCommand(ChannelShell shell, String command) throws RemoteConnectionException {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			shell.setOutputStream(stream);
			shell.setExtOutputStream(err);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(shell.getOutputStream()));
			writer.write(command);
			writer.flush();
			if (err.size() > 0) {
				throw new RemoteConnectionException(err.toString());
			}
			return stream.toString();
		} catch (IOException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}

	@Override
	protected void setUp() throws Exception {
		//		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		//		connType = manager.getConnectionType("org.eclipse.remote.Proxy"); //$NON-NLS-1$
		//		assertNotNull(connType);
	}

	@Override
	protected void tearDown() throws Exception {
	}
}
