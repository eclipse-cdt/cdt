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
 *     徐持恒 Xu Chiheng - refactor to help debugging
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.eclipse.cdt.internal.core.natives.CNativePlugin;
import org.eclipse.cdt.internal.core.natives.Messages;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ProcessFactory {

	static private ProcessFactory instance;
	private boolean hasSpawner;
	private Runtime runtime;

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

	private static TreeMap<String, String> newEmptyEnvironment() {
		TreeMap<String, String> environment;
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			environment = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		} else {
			environment = new TreeMap<>();
		}
		return environment;
	}

	private static TreeMap<String, String> getDefaultEnvironment() {
		TreeMap<String, String> environment = newEmptyEnvironment();
		Map<String, String> env = new ProcessBuilder().environment();
		environment.putAll(env);
		return environment;
	}

	private static TreeMap<String, String> envpToEnvMap(String[] envp) {
		TreeMap<String, String> environment;
		if (envp != null) {
			environment = newEmptyEnvironment();
			for (String envstring : envp) {
				int eqlsign = envstring.indexOf('=');
				if (eqlsign != -1) {
					environment.put(envstring.substring(0, eqlsign), envstring.substring(eqlsign + 1));
				} else {
					// Silently ignore envstrings lacking the required `='.
				}
			}
		} else {
			environment = getDefaultEnvironment();
		}
		return environment;
	}

	private static void appendEnvMapComparison(StringBuilder sb, TreeMap<String, String> environmentA,
			TreeMap<String, String> environmentB) {
		TreeMap<String, String> environmentC = newEmptyEnvironment();
		environmentC.putAll(environmentA);
		environmentC.putAll(environmentB);
		Iterator<Entry<String, String>> iterator = environmentC.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String valueA, valueB;
			if (environmentA.containsKey(key)) {
				valueA = environmentA.get(key);
				if (environmentB.containsKey(key)) {
					valueB = environmentB.get(key);
					int result = valueA.compareTo(valueB);
					if (result == 0) {
						// not changed
						sb.append(' ');
						sb.append(key);
						sb.append('=');
						sb.append(valueA);
						sb.append('\n');
					} else {
						// changed
						sb.append('-');
						sb.append(key);
						sb.append('=');
						sb.append(valueA);
						sb.append('\n');
						sb.append('+');
						sb.append(key);
						sb.append('=');
						sb.append(valueB);
						sb.append('\n');
					}
				} else {
					// removed
					sb.append('-');
					sb.append(key);
					sb.append('=');
					sb.append(valueA);
					sb.append('\n');
				}
			} else {
				// environmentB contains the key
				valueB = environmentB.get(key);
				// added
				sb.append('+');
				sb.append(key);
				sb.append('=');
				sb.append(valueB);
				sb.append('\n');
			}
		}
	}

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
				throw new IllegalArgumentException("Empty command"); //$NON-NLS-1$
			}
			StringTokenizer st = new StringTokenizer(cmd);
			this.cmdarray = new String[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++)
				this.cmdarray[i] = st.nextToken();
			this.cmdarray = modifyCmdArrayIfFlatpak(this.cmdarray);
		}

		public Builder(String[] cmdarray) throws IOException {
			if (cmdarray.length == 0 || cmdarray[0].isEmpty()) {
				throw new IllegalArgumentException("Empty command"); //$NON-NLS-1$
			}
			this.cmdarray = cmdarray;
			this.cmdarray = modifyCmdArrayIfFlatpak(this.cmdarray);
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

		private StringBuilder debug() {
			// for debug purpose
			StringBuilder sb = new StringBuilder();

			sb.append("command :\n"); //$NON-NLS-1$
			for (int i = 0; i < cmdarray.length; i++) {
				sb.append(i);
				sb.append(" : \""); //$NON-NLS-1$
				sb.append(cmdarray[i]);
				sb.append("\"\n"); //$NON-NLS-1$
			}
			sb.append("\n\n"); //$NON-NLS-1$

			sb.append("directory :\n"); //$NON-NLS-1$
			if (dir != null) {
				String pathString = dir.toString();
				sb.append(pathString);
				sb.append('\n');
				Path path = new Path(pathString);
				String pathDevice = path.getDevice();
				String[] pathSegments = path.segments();
				if (pathDevice != null) {
					sb.append("device : "); //$NON-NLS-1$
					sb.append(pathDevice);
					sb.append('\n');
				}
				sb.append("segments : \n"); //$NON-NLS-1$
				for (int i = 0; i < pathSegments.length; i++) {
					sb.append(i);
					sb.append(" : "); //$NON-NLS-1$
					sb.append(pathSegments[i]);
					sb.append('\n');
				}
			} else {
				sb.append("not specified\n"); //$NON-NLS-1$
			}
			sb.append("\n\n"); //$NON-NLS-1$

			{
				TreeMap<String, String> environmentA = getDefaultEnvironment();
				TreeMap<String, String> environmentB = envpToEnvMap(envp);

				sb.append("environment :\n"); //$NON-NLS-1$
				appendEnvMapComparison(sb, environmentA, environmentB);
				sb.append("\n\n"); //$NON-NLS-1$
			}
			if (use_pty) {
				sb.append("use pty : "); //$NON-NLS-1$
				sb.append(pty.toString());
				sb.append("\n\n"); //$NON-NLS-1$
			}
			if (has_gracefulExitTimeMs) {
				sb.append("has gracefulExitTimeMs : "); //$NON-NLS-1$
				sb.append(gracefulExitTimeMs);
				sb.append("\n\n"); //$NON-NLS-1$
			}
			// set breakpoint on next line to inspect sb when debugging, to see the
			// ultimate parameters of ProcessBuilder
			return sb;
		}

		public Process start() throws IOException {
			// Uncomment the next line, set a breakpoint in the last line of debug() method,
			// when the breakpoint is triggered, inspect the sb variable to see detailed info on what is being launched.
			// debug();
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
}
