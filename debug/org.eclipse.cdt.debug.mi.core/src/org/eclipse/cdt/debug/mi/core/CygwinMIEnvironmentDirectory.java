/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.io.ByteArrayOutputStream;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;
import org.eclipse.core.runtime.Path;

/**
 * Cygwin implementation of the MIEnvironmentDirectory command.  In the cygwin
 * environment, the paths are DOS paths and need to be converted to cygwin
 * style paths before passing them to gdb.
 */
public class CygwinMIEnvironmentDirectory extends MIEnvironmentDirectory {

	CygwinMIEnvironmentDirectory(String miVersion, boolean reset, String[] paths) {
		super(miVersion, reset, paths);

		String[] newpaths = new String[paths.length];
		for (int i = 0; i < paths.length; i++) {
			// Use the cygpath utility to convert the path
			CommandLauncher launcher = new CommandLauncher();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			launcher.execute(
				new Path("cygpath"), //$NON-NLS-1$
				new String[] { "-u", paths[i] }, //$NON-NLS-1$
				new String[0],
				new Path(".")); //$NON-NLS-1$
			if (launcher.waitAndRead(out, out) != CommandLauncher.OK)
				newpaths[i] = paths[i];
			else
				newpaths[i] = out.toString().trim();
		}

		setParameters(newpaths);
	}
}
