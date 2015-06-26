/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.remote.core.IRemoteCommandShellService;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessTerminalService;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

@SuppressWarnings("restriction")
public class TerminalConsoleConnector {

	private final IRemoteConnection connection;
	private IRemoteProcess remoteProcess;
	private PageConnector[] pageConnectors = new PageConnector[0];
	private int width, height;
	private TerminalState state = TerminalState.CLOSED;

	private class OutThread extends Thread {
		public OutThread() {
			super("Terminal Output"); //$NON-NLS-1$
		}

		@Override
		public void run() {
			try {
				byte[] buff = new byte[1024];
				if (remoteProcess != null) {
					InputStream in = remoteProcess.getInputStream();
					for (int n = in.read(buff); n >= 0; n = in.read(buff)) {
						for (PageConnector connector : pageConnectors) {
							ITerminalControl control = connector.control;
							if (control != null) {
								control.getRemoteToTerminalOutputStream().write(buff, 0, n);
							}
						}
					}
				}
				setState(TerminalState.CLOSED);
				synchronized (TerminalConsoleConnector.this) {
					outThread = null;
				}
			} catch (IOException e) {
				Activator.log(e);
			}
		}
	}

	private OutThread outThread;

	public TerminalConsoleConnector(IRemoteConnection connection) {
		this.connection = connection;
	}

	public IRemoteConnection getConnection() {
		return connection;
	}

	public ITerminalConnector newPageConnector() {
		PageConnector connector = new PageConnector();
		List<PageConnector> list = new ArrayList<>(Arrays.asList(pageConnectors));
		list.add(connector);
		pageConnectors = list.toArray(new PageConnector[list.size()]);
		return connector;
	}

	private void disposePageConnector(PageConnector connector) {
		List<PageConnector> list = new ArrayList<>(Arrays.asList(pageConnectors));
		list.remove(connector);
		pageConnectors = list.toArray(new PageConnector[list.size()]);
		if (list.isEmpty()) {
			// All gone, disconnect
			disconnect();
		}
	}

	private synchronized void setState(TerminalState state) {
		this.state = state;
		for (PageConnector connector : pageConnectors) {
			ITerminalControl control = connector.control;
			if (control != null) {
				connector.control.setState(state);
			}
		}
	}

	public synchronized void connect() {
		if (state != TerminalState.CLOSED) {
			return;
		}

		new Job(ConsoleMessages.MAKING_CONNECTION) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// make sure we're only doing this one at a time
				// second and further controls will inherit much of this
				synchronized (TerminalConsoleConnector.this) {
					setState(TerminalState.CONNECTING);

					if (remoteProcess == null || remoteProcess.isCompleted()) {
						try {
							// We'll need a new one
							if (!connection.isOpen()) {
								try {
									connection.open(monitor);
								} catch (RemoteConnectionException e) {
									return e.getStatus();
								}
							}
							remoteProcess = connection.getService(IRemoteCommandShellService.class)
									.getCommandShell(IRemoteProcessBuilder.ALLOCATE_PTY);
						} catch (IOException e) {
							Activator.log(e);
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
						}
					}

					if (outThread == null) {
						outThread = new OutThread();
						outThread.start();
					}

					if (width > 0 || height > 0) {
						TerminalConsoleConnector.this.setTerminalSize();
					}

					setState(TerminalState.CONNECTED);
					return Status.OK_STATUS;
				}
			}
		}.schedule();
	}

	public void disconnect() {
		if (remoteProcess != null && !remoteProcess.isCompleted()) {
			new Job(ConsoleMessages.DISCONNECTING) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					remoteProcess.destroy();
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	private void setTerminalSize() {
		int minWidth = Integer.MAX_VALUE;
		int minHeight = Integer.MAX_VALUE;

		for (PageConnector connector : pageConnectors) {
			if (connector.myWidth < minWidth) {
				minWidth = connector.myWidth;
			}
			if (connector.myHeight < minHeight) {
				minHeight = connector.myHeight;
			}
		}

		// Weird but the terminal has wrapping issues at this width, need to reduce it by 4.
		minWidth -= 4;

		if (minWidth != width || minHeight != height) {
			width = minWidth;
			height = minHeight;
			synchronized (this) {
				if (remoteProcess != null) {
					IRemoteProcessTerminalService termService = remoteProcess.getService(IRemoteProcessTerminalService.class);
					if (termService != null) {
						termService.setTerminalSize(width, height, 8 * width, 8 * height);
					}
				}
			}
		}
	}

	private class PageConnector extends PlatformObject implements ITerminalConnector {
		private int myWidth, myHeight;
		private ITerminalControl control;

		@Override
		public OutputStream getTerminalToRemoteStream() {
			return remoteProcess != null ? remoteProcess.getOutputStream() : null;
		}

		@Override
		public void connect(final ITerminalControl control) {
			this.control = control;
			control.setVT100LineWrapping(true);
			TerminalConsoleConnector.this.connect();
			if (!control.getState().equals(state)) {
				control.setState(state);
			}
		}

		@Override
		public synchronized void disconnect() {
			disposePageConnector(this);
		}

		@Override
		public void setTerminalSize(int newWidth, int newHeight) {
			if (newWidth != myWidth || newHeight != myHeight) {
				myWidth = newWidth;
				myHeight = newHeight;
				TerminalConsoleConnector.this.setTerminalSize();
			}
		}

		@Override
		public String getId() {
			// No id, we're magic
			return null;
		}

		@Override
		public String getName() {
			// No name
			return null;
		}

		@Override
		public boolean isHidden() {
			// in case we do leak into the TM world, we shouldn't be visible
			return true;
		}

		@Override
		public boolean isInitialized() {
			return true;
		}

		@Override
		public String getInitializationErrorMessage() {
			return null;
		}

		@Override
		public boolean isLocalEcho() {
			// TODO should the be a property of the connection?
			return false;
		}

		@Override
		public void setDefaultSettings() {
			// we don't do settings
		}

		@Override
		public String getSettingsSummary() {
			// we don't do settings
			return null;
		}

		@Override
		public void load(ISettingsStore arg0) {
			// we don't do settings
		}

		@Override
		public void save(ISettingsStore arg0) {
			// we don't do settings
		}
	}
}
