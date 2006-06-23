/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;


import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.pty.PTY;

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
			e.printStackTrace();
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

	public Process exec(String cmd, String[] envp, File dir)
		throws IOException {
		if (hasSpawner)
			return new Spawner(cmd, envp, dir);
		return runtime.exec(cmd, envp, dir);
	}

	public Process exec(String cmdarray[], String[] envp, File dir)
		throws IOException {
		if (hasSpawner)
			return new Spawner(cmdarray, envp, dir);
		return runtime.exec(cmdarray, envp, dir);
	}

	public Process exec(String cmdarray[], String[] envp, File dir, PTY pty)
		throws IOException {
		if (hasSpawner)
			return new Spawner(cmdarray, envp, dir, pty);
		throw new UnsupportedOperationException(CCorePlugin.getResourceString("Util.exception.cannotCreatePty")); //$NON-NLS-1$
	}
}
