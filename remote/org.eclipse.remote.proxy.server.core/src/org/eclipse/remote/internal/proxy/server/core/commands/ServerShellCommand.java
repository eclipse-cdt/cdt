/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.server.core.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.pty.PTY.Mode;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;

/**
 * TODO: Fix hang if command fails...
 *
 */
public class ServerShellCommand extends AbstractServerExecCommand {
	private class ShellProcess extends Process {
		private final Process proc;
		private final PTY pty;

		public ShellProcess(Process proc, PTY pty) {
			this.proc = proc;
			this.pty = pty;
		}

		@Override
		public OutputStream getOutputStream() {
			if (pty != null) {
				return pty.getOutputStream();
			}
			return proc.getOutputStream();
		}

		@Override
		public InputStream getInputStream() {
			if (pty != null) {
				return pty.getInputStream();
			}
			return proc.getInputStream();
		}

		@Override
		public InputStream getErrorStream() {
			if (pty != null) {
				return pty.getInputStream();
			}
			return proc.getErrorStream();
		}

		@Override
		public int waitFor() throws InterruptedException {
			return proc.waitFor();
		}

		@Override
		public int exitValue() {
			return proc.exitValue();
		}

		@Override
		public void destroy() {
			proc.destroy();
		}

		public void setTerminalSize(int cols, int rows) {
			if (pty != null) {
				pty.setTerminalSize(cols, rows);
			}
		}
	}

	public ServerShellCommand(StreamChannel cmdChan, StreamChannel ioChan) {
		super(null, null, null, true, false, cmdChan, ioChan, null);
	}

	public Process doRun() throws IOException {
		String shell = findLoginShell();

		if (PTY.isSupported(Mode.TERMINAL)) {
			PTY pty = new PTY(Mode.TERMINAL);
			Process p = ProcessFactory.getFactory().exec(new String[] { shell, "-l" }, null, null, pty); //$NON-NLS-1$
			return new ShellProcess(p, pty);
		}

		return ProcessFactory.getFactory().exec(new String[] { shell, "-l" }, null, null); //$NON-NLS-1$
	}

	protected void doKill(Process proc) {
		if (proc.isAlive()) {
			proc.destroyForcibly();
		}
	}

	protected void doSetTerminalSize(Process proc, int cols, int rows) {
		if (proc.isAlive() && proc instanceof ShellProcess) {
			ShellProcess shell = (ShellProcess) proc;
			shell.setTerminalSize(cols, rows);
		}
	}

	/**
	 * Find the login shell.
	 *
	 * On Linux, use `getent passwd $USER`
	 * On Mac OSX, use `dscl . -read /Users/$USER UserShell`
	 *
	 * @return
	 */
	private String findLoginShell() throws IOException {
		String res;

		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		String userName = System.getProperty("user.name"); //$NON-NLS-1$
		if (osName == null || userName == null) {
			throw new IOException("Unable to obtain information needed to find login shell"); //$NON-NLS-1$
		}
		switch (osName) {
		case "Mac OS X": //$NON-NLS-1$
			res = executeCommand("dscl . -read /Users/" + userName + " UserShell"); //$NON-NLS-1$ //$NON-NLS-2$
			if (res != null) {
				String[] vals = res.split(" "); //$NON-NLS-1$
				if (vals.length == 2) {
					return vals[1];
				}
			}
			break;
		case "Linux": //$NON-NLS-1$
			res = executeCommand("getent passwd " + userName); //$NON-NLS-1$
			if (res != null) {
				String[] vals = res.split(":"); //$NON-NLS-1$
				if (vals.length == 7) {
					return vals[6];
				}
			}
			break;
		default:
			break;
		}
		throw new IOException("Unable to find login shell for os=" + osName + " user=" + userName); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private String executeCommand(String command) throws IOException {
		String line;
		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage());
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		line = reader.readLine();
		while (line != null) {
			output.append(line);
			line = reader.readLine();
			if (line != null) {
				output.append("\n"); //$NON-NLS-1$
			}
		}

		return output.toString();
	}
}
