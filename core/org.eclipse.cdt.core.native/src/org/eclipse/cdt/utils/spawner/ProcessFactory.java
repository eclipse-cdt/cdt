/*******************************************************************************
 * Copyright (c) 2000, 2020 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Martin Oberhuber (Wind River) - [303083] Split out the Spawner
 *     Red Hat Inc. - add flatpak support
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.eclipse.cdt.internal.core.natives.CNativePlugin;
import org.eclipse.cdt.internal.core.natives.Messages;
import org.eclipse.cdt.utils.pty.PTY;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ProcessFactory {

	static private ProcessFactory instance;
	private boolean hasSpawner;
	private Runtime runtime;

	private class Builder {
		String[] cmdarray;
		String[] envp;
		File dir;
		boolean use_pty;
		PTY pty;
		boolean has_gracefulExitTimeMs;
		int gracefulExitTimeMs;

		public Builder(String cmd) throws IOException {
			if (cmd.isEmpty()) {
				throw new IllegalArgumentException("Empty command");
			}
			StringTokenizer st = new StringTokenizer(cmd);
			this.cmdarray = new String[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++)
				this.cmdarray[i] = st.nextToken();
		}

		public Builder(String[] cmdarray) throws IOException {
			if (cmdarray.length == 0 || cmdarray[0].isEmpty()) {
				throw new IllegalArgumentException("Empty command");
			}
			this.cmdarray = cmdarray;
		}

		public Builder environment(String[] envp) {
			this.envp = envp;
			return this;
		}

		public Builder directory(File directory) {
			this.dir = directory;
			return this;
		}

		public Builder pty(PTY pty) {
			this.use_pty = true;
			this.pty = pty;
			return this;
		}

		public Builder gracefulExitTimeMs(int gracefulExitTimeMs) {
			this.has_gracefulExitTimeMs = true;
			this.gracefulExitTimeMs = gracefulExitTimeMs;
			return this;
		}

		public Process start() throws IOException {
			cmdarray = modifyCmdArrayIfFlatpak(cmdarray);
			Process p;
			if (hasSpawner) {
				if (use_pty) {
					if (has_gracefulExitTimeMs) {
						p = new Spawner(cmdarray, envp, dir, pty, gracefulExitTimeMs);
					} else {
						p = new Spawner(cmdarray, envp, dir, pty);
					}
				} else {
					if (has_gracefulExitTimeMs) {
						p = new Spawner(cmdarray, envp, dir, gracefulExitTimeMs);
					} else {
						p = new Spawner(cmdarray, envp, dir);
					}
				}
			} else {
				if (use_pty || has_gracefulExitTimeMs) {
					throw new UnsupportedOperationException(Messages.Util_exception_cannotCreatePty);
				} else {
					p = runtime.exec(cmdarray, envp, dir);
				}
			}
			return p;
		}
	}

	private ProcessFactory() {
		hasSpawner = false;
		String OS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		runtime = Runtime.getRuntime();
		try {
			// Spawner does not work for Windows 98 fallback
			if (OS != null && OS.equals("windows 98")) { //$NON-NLS-1$
				hasSpawner = false;
			} else {
				System.loadLibrary("spawner"); //$NON-NLS-1$
				hasSpawner = true;
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (UnsatisfiedLinkError e) {
			CNativePlugin.log(e.getMessage());
		}
	}

	public static ProcessFactory getFactory() {
		if (instance == null)
			instance = new ProcessFactory();
		return instance;
	}

	/**
	 * @deprecated Do not use this method it splits command line arguments on whitespace with no regard to quoting rules. See Bug 573677
	 */
	@Deprecated
	public Process exec(String cmd) throws IOException {
		Process p = new Builder(cmd).start();
		return p;
	}

	public Process exec(String[] cmdarray) throws IOException {
		Process p = new Builder(cmdarray).start();
		return p;
	}

	/**
	 * @since 6.2
	 */
	public Process exec(String[] cmdarray, int gracefulExitTimeMs) throws IOException {
		Process p = new Builder(cmdarray).gracefulExitTimeMs(gracefulExitTimeMs).start();
		return p;
	}

	public Process exec(String[] cmdarray, String[] envp) throws IOException {
		Process p = new Builder(cmdarray).environment(envp).start();
		return p;
	}

	/**
	 * @since 6.2
	 */
	public Process exec(String[] cmdarray, String[] envp, int gracefulExitTimeMs) throws IOException {
		Process p = new Builder(cmdarray).environment(envp).gracefulExitTimeMs(gracefulExitTimeMs).start();
		return p;
	}

	/**
	 * @deprecated Do not use this method it splits command line arguments on whitespace with no regard to quoting rules. See Bug 573677
	 */
	@Deprecated
	public Process exec(String cmd, String[] envp) throws IOException {
		Process p = new Builder(cmd).environment(envp).start();
		return p;
	}

	/**
	 * @deprecated Do not use this method it splits command line arguments on whitespace with no regard to quoting rules. See Bug 573677
	 */
	@Deprecated
	public Process exec(String cmd, String[] envp, File dir) throws IOException {
		Process p = new Builder(cmd).environment(envp).directory(dir).start();
		return p;
	}

	public Process exec(String cmdarray[], String[] envp, File dir) throws IOException {
		Process p = new Builder(cmdarray).environment(envp).directory(dir).start();
		return p;
	}

	/**
	 * @since 6.2
	 */
	public Process exec(String cmdarray[], String[] envp, File dir, int gracefulExitTimeMs) throws IOException {
		Process p = new Builder(cmdarray).environment(envp).directory(dir).gracefulExitTimeMs(gracefulExitTimeMs)
				.start();
		return p;
	}

	public Process exec(String cmdarray[], String[] envp, File dir, PTY pty) throws IOException {
		Process p = new Builder(cmdarray).environment(envp).directory(dir).pty(pty).start();
		return p;
	}

	/**
	 * @since 6.2
	 */
	public Process exec(String cmdarray[], String[] envp, File dir, PTY pty, int gracefulExitTimeMs)
			throws IOException {
		Process p = new Builder(cmdarray).environment(envp).directory(dir).pty(pty)
				.gracefulExitTimeMs(gracefulExitTimeMs).start();
		return p;
	}

	private String[] modifyCmdArrayIfFlatpak(String[] cmdarray) {
		if (System.getenv("FLATPAK_SANDBOX_DIR") != null) { //$NON-NLS-1$
			String[] newArray = new String[cmdarray.length + 3];
			System.arraycopy(cmdarray, 0, newArray, 3, cmdarray.length);
			newArray[0] = "flatpak-spawn"; //$NON-NLS-1$
			newArray[1] = "--host"; //$NON-NLS-1$
			newArray[2] = "--watch-bus"; //$NON-NLS-1$
			cmdarray = newArray;
		}
		return cmdarray;
	}
}
