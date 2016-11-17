/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IUserAuthenticatorService;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.JSchUserInfo;
import org.eclipse.remote.proxy.protocol.core.StreamChannelManager;
import org.osgi.framework.Bundle;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ProxyConnectionBootstrap {
	private final IJSchService jSchService;
	private Session session;
	private ChannelExec exec;
	
	private class Context {
		private State state;
		private String osName;
		private String osArch;
		
		private final SubMonitor monitor;
		private final BufferedReader reader;
		private final BufferedWriter writer;
		
		public Context(BufferedReader reader, BufferedWriter writer, IProgressMonitor monitor) {
			this.reader = reader;
			this.writer = writer;
			this.monitor = SubMonitor.convert(monitor);
			setState(States.INIT);
		}
		
	    State getState() {
	    	return state;
	    }
	    
	    SubMonitor getMonitor() {
	    	return monitor;
	    }
	    
	    void setState(State state) {
	    	this.state = state;
	    }
	    
	    String getOSName() {
	    	return osName;
	    }
	    
	    void setOSName(String osName) {
	    	this.osName = osName;
	    }
	    
	    String getOSArch() {
	    	return osArch;
	    }
	    
	    void setOSArch(String osArch) {
	    	this.osArch = osArch;
	    }
	}
	
	private interface State {
	    /**
	       * @return true to keep processing, false to read more data.
	     */
	    boolean process(Context context) throws IOException;
	}
	
	private enum States implements State {
		INIT {
			@Override
			public boolean process(Context context) throws IOException {
				context.getMonitor().subTask("Initializing");
				String line = context.reader.readLine();
				context.getMonitor().worked(1);
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
				context.getMonitor().subTask("Validating environment");
				String bundleName = "org.eclipse.remote.proxy.server.core";
				Bundle serverBundle = Platform.getBundle(bundleName);
				if (serverBundle == null) {
					throw new IOException("Unable to locate server bundle " + bundleName);
				}
				context.writer.write("check " + serverBundle.getVersion() + "\n");
				context.writer.flush();
				String line = context.reader.readLine();
				while (line != null) {
					context.getMonitor().worked(2);
					String[] parts = line.split(":");
					switch (parts[0]) {
					case "ok":
						String[] status = parts[1].split("/");
						context.setOSName(status[1]);
						context.setOSArch(status[2]);
						context.setState(status[0].equals("proxy") ? States.START : States.DOWNLOAD);
						return true;
					case "fail":
						System.out.println("fail:"+parts[1]);
						return false;
					case "debug":
						System.err.println(line);
						break;
					default:
						System.err.println("Invalid response from bootstrap script: " + line);
						return false;
					}
					line = context.reader.readLine();
				}
				return false;
			}
		},
		DOWNLOAD {
			@Override
			public boolean process(Context context) throws IOException {
				context.getMonitor().subTask("Updating server proxy");
				String bundleName = "org.eclipse.remote.proxy.server." + context.getOSName() + "." + context.getOSArch();
				Bundle serverBundle = Platform.getBundle(bundleName);
				if (serverBundle == null) {
					throw new IOException("Unable to locate server bundle " + bundleName);
				}
				URL fileURL = FileLocator.find(serverBundle, new Path("proxy.server.tar.gz"), null);
				if (fileURL == null) {
					return false;
				}
				File file = new File(FileLocator.toFileURL(fileURL).getFile());
				long count = file.length() / 510;
				context.writer.write("download " + count + "\n");
				context.writer.flush();
				context.getMonitor().worked(2);
				if (downloadFile(file, context.writer, context.getMonitor().newChild(5))) {
					String line = context.reader.readLine();
					while (line != null) {
						String[] parts = line.split(":");
						switch (parts[0]) {
						case "ok":
							context.setState(States.START);
							return true;
						case "fail":
							System.out.println("fail:"+parts[1]);
							return false;
						case "debug":
							System.err.println(line);
							break;
						default:
							System.err.println("Invalid response from bootstrap script: " + line);
							return false;
						}
						line = context.reader.readLine();
					}
				}
				return false;
			}
			
			private boolean downloadFile(File file, BufferedWriter writer, IProgressMonitor monitor) {
				SubMonitor subMon = SubMonitor.convert(monitor, 10);
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
						subMon.setWorkRemaining(8);
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
				context.getMonitor().subTask("Starting server");
				context.writer.write("start\n");
				context.writer.flush();
				return false; // Finished
			}
		}
	}
	
	public ProxyConnectionBootstrap() {
		jSchService = Activator.getService(IJSchService.class);
	}

	public StreamChannelManager run(IRemoteConnection connection, IProgressMonitor monitor) throws RemoteConnectionException {
		SubMonitor subMon = SubMonitor.convert(monitor, 20);
		try {
			final Channel chan = openChannel(connection, subMon.newChild(10));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(chan.getOutputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(chan.getInputStream()));
			subMon.beginTask("Checking server installation", 10);
			subMon.subTask("Loading bootstrap shell");
			URL fileURL = FileLocator.find(Activator.getDefault().getBundle(), new Path("bootstrap.sh"), null);
			if (fileURL == null) {
				throw new RemoteConnectionException("Unable to locate bootstrap shell");
			}
			File file = new File(FileLocator.toFileURL(fileURL).getFile());
			BufferedReader scriptReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = scriptReader.readLine()) != null) {
				writer.write(line + "\n");
			}
			scriptReader.close();
			writer.flush();
			subMon.worked(2);
			Context context = new Context(reader, writer, subMon.newChild(8));
			while (context.getState().process(context)) {
				// do state machine
			}
			if (context.getState() != States.START) {
				context.writer.write("exit\n");
				context.writer.flush();
				throw new RemoteConnectionException("Unable to start server");
			}
			new Thread("server error stream") {
				@Override
				public void run() {
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(chan.getExtInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							System.err.println("server: "+ line);
						}
					} catch (IOException e) {
						// Ignore and terminate thread
					}
				}
			}.start();
			return new StreamChannelManager(chan.getInputStream(), chan.getOutputStream());
		} catch (IOException | CoreException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}
	
	private Channel openChannel(IRemoteConnection connection, IProgressMonitor monitor) throws RemoteConnectionException {
		IRemoteConnectionWorkingCopy wc = connection.getWorkingCopy();
		IRemoteConnectionHostService hostService = wc.getService(IRemoteConnectionHostService.class);
		IUserAuthenticatorService authService = wc.getService(IUserAuthenticatorService.class);
		try {
			session = jSchService.createSession(hostService.getHostname(), hostService.getPort(), hostService.getUsername());
			session.setUserInfo(new JSchUserInfo(hostService, authService));
			if (hostService.usePassword()) {
				session.setConfig("PreferredAuthentications", "password,keyboard-interactive,gssapi-with-mic,publickey"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				session.setConfig("PreferredAuthentications", "publickey,gssapi-with-mic,password,keyboard-interactive"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			String password = hostService.getPassword();
			if (!password.isEmpty()) {
				session.setPassword(password);
			}
			jSchService.connect(session, hostService.getTimeout() * 1000, monitor);
			if (monitor.isCanceled()) {
				throw new RemoteConnectionException("User canceled connection open");
			}
			exec = (ChannelExec) session.openChannel("exec"); //$NON-NLS-1$
			exec.setCommand("/bin/sh -l");
			exec.connect();
			return exec;
		} catch (JSchException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}
}
