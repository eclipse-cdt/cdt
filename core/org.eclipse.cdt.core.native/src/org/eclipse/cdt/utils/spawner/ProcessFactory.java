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
	private final static String FLATPAK_CMD = "flatpak-spawn --host --watch-bus "; //$NON-NLS-1$

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
		cmd = modifyCmdIfFlatpak(cmd);
		if (hasSpawner)
			return new Spawner(cmd);
		return runtime.exec(cmd);
	}

	public Process exec(String[] cmdarray) throws IOException {
		cmdarray = modifyCmdArrayIfFlatpak(cmdarray);
		if (hasSpawner)
			return new Spawner(cmdarray);
		return runtime.exec(cmdarray);
	}

	/**
	 * @since 6.2
	 */
	public Process exec(String[] cmdarray, int gracefulExitTimeMs) throws IOException {
		cmdarray = modifyCmdArrayIfFlatpak(cmdarray);
		if (hasSpawner)
			return new Spawner(cmdarray, gracefulExitTimeMs);
		return runtime.exec(cmdarray);
	}

	public Process exec(String[] cmdarray, String[] envp) throws IOException {
		cmdarray = modifyCmdArrayIfFlatpak(cmdarray);
		if (hasSpawner)
			return new Spawner(cmdarray, envp);
		return runtime.exec(cmdarray, envp);
	}

	/**
	 * @since 6.2
	 */
	public Process exec(String[] cmdarray, String[] envp, int gracefulExitTimeMs) throws IOException {
		cmdarray = modifyCmdArrayIfFlatpak(cmdarray);
		if (hasSpawner)
			return new Spawner(cmdarray, envp, gracefulExitTimeMs);
		return runtime.exec(cmdarray, envp);
	}

	/**
	 * @deprecated Do not use this method it splits command line arguments on whitespace with no regard to quoting rules. See Bug 573677
	 */
	@Deprecated
	public Process exec(String cmd, String[] envp) throws IOException {
		cmd = modifyCmdIfFlatpak(cmd);
		if (hasSpawner)
			return new Spawner(cmd, envp);
		return runtime.exec(cmd, envp);
	}

	/**
	 * @deprecated Do not use this method it splits command line arguments on whitespace with no regard to quoting rules. See Bug 573677
	 */
	@Deprecated
	public Process exec(String cmd, String[] envp, File dir) throws IOException {
		cmd = modifyCmdIfFlatpak(cmd);
		if (hasSpawner)
			return new Spawner(cmd, envp, dir);
		return runtime.exec(cmd, envp, dir);
	}

	public Process exec(String cmdarray[], String[] envp, File dir) throws IOException {
		cmdarray = modifyCmdArrayIfFlatpak(cmdarray);
		if (hasSpawner)
			return new Spawner(cmdarray, envp, dir);
		return runtime.exec(cmdarray, envp, dir);
	}

	/**
	 * @since 6.2
	 */
	public Process exec(String cmdarray[], String[] envp, File dir, int gracefulExitTimeMs) throws IOException {
		cmdarray = modifyCmdArrayIfFlatpak(cmdarray);
		if (hasSpawner)
			return new Spawner(cmdarray, envp, dir, gracefulExitTimeMs);
		return runtime.exec(cmdarray, envp, dir);
	}

	public Process exec(String cmdarray[], String[] envp, File dir, PTY pty) throws IOException {
		cmdarray = modifyCmdArrayIfFlatpak(cmdarray);
		if (hasSpawner)
			return new Spawner(cmdarray, envp, dir, pty);
		throw new UnsupportedOperationException(Messages.Util_exception_cannotCreatePty);
	}

	/**
	 * @since 6.2
	 */
	public Process exec(String cmdarray[], String[] envp, File dir, PTY pty, int gracefulExitTimeMs)
			throws IOException {
		cmdarray = modifyCmdArrayIfFlatpak(cmdarray);
		if (hasSpawner)
			return new Spawner(cmdarray, envp, dir, pty, gracefulExitTimeMs);
		throw new UnsupportedOperationException(Messages.Util_exception_cannotCreatePty);
	}

	private String modifyCmdIfFlatpak(String cmd) {
		if (System.getenv("FLATPAK_SANDBOX_DIR") != null) { //$NON-NLS-1$
			cmd = FLATPAK_CMD + cmd;
		}
		return cmd;
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
