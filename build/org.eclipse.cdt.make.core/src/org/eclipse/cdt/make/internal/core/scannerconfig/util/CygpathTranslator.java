/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Executes external command 'cygpath' to translate cygpaths to absolute paths.
 * 
 * @author vhirsl
 */
public class CygpathTranslator {
	IProject project;
	String orgPath;
	String transPath;

	public CygpathTranslator(IProject project, String path) {
		this.project = project;
		orgPath = path;
	}
	
	public String run() {
		ISafeRunnable runnable = new ISafeRunnable() {
			public void run() throws Exception {
				transPath = platformRun();
				if (transPath.startsWith("cygpath:")) {	//$NON-NLS-1$
					transPath = null;
				}
			}

			public void handleException(Throwable exception) {
				transPath = orgPath;
				MakeCorePlugin.log(exception);
			}
		};
		Platform.run(runnable);
		return transPath;
	}

	/**
	 * @return
	 */
	String platformRun() {
		CommandLauncher launcher = new CommandLauncher();
		launcher.showCommand(false);

		OutputStream output = new ByteArrayOutputStream();

		Process p = launcher.execute(
			new Path("cygpath"),			//$NON-NLS-1$
			new String[] {"-m", orgPath},	//$NON-NLS-1$
			new String[0],//setEnvironment(launcher, "c:/"),//$NON-NLS-1$
			project.getLocation());				//$NON-NLS-1$
		if (p != null) {
			try {
				// Close the input of the Process explicitely.
				// We will never write to it.
				p.getOutputStream().close();
			}
			catch (IOException e) {
			}
			if (launcher.waitAndRead(output, output) != CommandLauncher.OK) {
				//String errMsg = launcher.getErrorMessage();
			}
			else
				return output.toString().trim();
		}
		return orgPath;
	}

	/**
	 * @param launcher
	 * @return
	 */
	private String[] setEnvironment(CommandLauncher launcher, String dir) {
		// Set the environmennt, some scripts may need the CWD var to be set.
		IPath workingDirectory = new Path(dir);
		Properties props = launcher.getEnvironment();
		props.put("CWD", workingDirectory.toOSString()); //$NON-NLS-1$
		props.put("PWD", workingDirectory.toOSString()); //$NON-NLS-1$
		String[] env = null;
		ArrayList envList = new ArrayList();
		Enumeration names = props.propertyNames();
		if (names != null) {
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				envList.add(key + "=" + props.getProperty(key)); //$NON-NLS-1$
			}
			env = (String[]) envList.toArray(new String[envList.size()]);
		}
		return env;
	}
}
