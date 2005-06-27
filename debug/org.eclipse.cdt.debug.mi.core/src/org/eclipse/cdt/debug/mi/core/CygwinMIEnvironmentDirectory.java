/*
 *(c) Copyright Rational Software Corporation, 2002
 * All Rights Reserved.
 *
 */

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
