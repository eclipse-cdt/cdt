/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;

public class Utilities {

	private static Utilities instance;

	private Utilities() {
	}

	public static Utilities getDefault() {
		if (instance == null)
			instance = new Utilities();
		return instance;
	}

	public IPath getProjectPath(String name) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath rootLocation = root.getLocation();

		IPath destProjectDirPath = rootLocation.append(name);

		return destProjectDirPath;
	}

	public boolean buildProject(String name) {
		File destProjectDir = getProjectPath(name).toFile();

		String resourceDirPath = "/projects/" + name; //$NON-NLS-1$
		try {
			String path = FileLocator.toFileURL(this.getClass().getResource(resourceDirPath)).getPath();

			File projectDir = new Path(path).toFile();
			copy(projectDir, destProjectDir);

			Process process = execute(new String[] { "make", "all", "2>&1" }, null, destProjectDir, false);

			boolean done = false;
			while (!done) {
				try {
					process.exitValue();
					done = true;
					byte b[] = new byte[1000];
					int numread = 0;
					InputStream stream = process.getInputStream();
					do {
						numread = stream.read(b);
						String x = new String(b);
						System.out.print(x);
					} while (numread >= 0);
				} catch (IllegalThreadStateException e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
					}
				}
			}
			process.destroy();
			process = execute(new String[] { "ls", "-l" }, null, destProjectDir, false);

			done = false;
			while (!done) {
				try {
					process.exitValue();
					done = true;
					byte b[] = new byte[1000];
					int numread = 0;
					while (numread >= 0) {
						numread = process.getInputStream().read(b);
						String x = new String(b);
						System.out.println(x);
					}
				} catch (IllegalThreadStateException e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
					}
				}
			}
			process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean cleanProject(String name) {
		IPath destProjectDirPath = getProjectPath(name);
		File destProjectDir = destProjectDirPath.toFile();

		try {

			Process process = execute(new String[] { "make", "clean" }, null, destProjectDir, false);

			boolean done = false;
			while (!done) {
				try {
					process.exitValue();
					done = true;
					byte b[] = new byte[1000];
					process.getInputStream().read(b);
					System.out.println(b);
				} catch (IllegalThreadStateException e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
					}
				}
			}
			process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static Process execute(String[] commandArray, String[] env, File wd, boolean usePty) throws IOException {
		Process process = null;
		if (env == null) {
			Map<String, String> envMap = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironment();
			env = new String[envMap.size()];
			int i = 0;
			for (Entry<String, String> entry : envMap.entrySet()) {
				env[i++] = entry.getKey() + "=" + entry.getValue();
			}
		}
		try {
			process = startProcess(commandArray, env, wd, false);
			return process;
		} catch (IOException e) {
			throw e;
		}
	}

	private static Process startProcess(String[] commandArray, String[] envp, File workDir, boolean usePty)
			throws IOException {
		if (workDir == null) {
			return ProcessFactory.getFactory().exec(commandArray, envp);
		}
		if (PTY.isSupported() && usePty) {
			return ProcessFactory.getFactory().exec(commandArray, envp, workDir, new PTY());
		} else {
			return ProcessFactory.getFactory().exec(commandArray, envp, workDir);
		}
	}

	public static void copy(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// ensure destination directory exists
			if (!dest.exists()) {
				dest.mkdir();
			}

			// copy all children files recursively
			String files[] = src.list();

			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				copy(srcFile, destFile);
			}

		} else { // is a file, manually copy the contents over
			Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
