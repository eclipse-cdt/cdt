/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
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

	public Process exec(String cmd) throws IOException {
		if (hasSpawner)
			return new Spawner(cmd);
		return runtime.exec(cmd);
	}

	public Process exec(String[] cmdarray) throws IOException {
		if (hasSpawner)
			return new Spawner(cmdarray);
		return runtime.exec(cmdarray);
	}

	public Process exec(String[] cmdarray, String[] envp) throws IOException {
		if (hasSpawner)
			return new Spawner(cmdarray, envp);
		return runtime.exec(cmdarray, envp);
	}

	public Process exec(String cmd, String[] envp) throws IOException {
		if (hasSpawner)
			return new Spawner(cmd, envp);
		return runtime.exec(cmd, envp);
	}

	public Process exec(String cmd, String[] envp, File dir) throws IOException {
		if (hasSpawner)
			return new Spawner(cmd, envp, dir);
		return runtime.exec(cmd, envp, dir);
	}

	public Process exec(String cmdarray[], String[] envp, File dir) throws IOException {
		if (hasSpawner)
			return new Spawner(cmdarray, envp, dir);
		return runtime.exec(cmdarray, envp, dir);
	}

	public Process exec(String cmdarray[], String[] envp, File dir, PTY pty) throws IOException {
		if (hasSpawner)
			return new Spawner(cmdarray, envp, dir, pty);
		throw new UnsupportedOperationException(Messages.Util_exception_cannotCreatePty);
	}
}
