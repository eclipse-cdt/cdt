/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.debug.mi.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentCD;
import org.eclipse.core.runtime.Path;

/**
 * CygwinMIEnvironmentCD
 */
public class CygwinMIEnvironmentCD extends MIEnvironmentCD {

	/**
	 * @param path
	 */
	public CygwinMIEnvironmentCD(String path) {
		super(path);

		// Use the cygpath utility to convert the path
		CommandLauncher launcher = new CommandLauncher();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		String newPath = null;
		launcher.execute(
			new Path("cygpath"), //$NON-NLS-1$
			new String[] { "-u", path }, //$NON-NLS-1$
			new String[0],
			new Path(".")); //$NON-NLS-1$
		if (launcher.waitAndRead(out, err) == CommandLauncher.OK) {
			newPath = out.toString();
			if (newPath != null) {
				newPath = newPath.trim();
				if (newPath.length() > 0) {
					path = newPath;
				}
			}
		}
		try {
			out.close();
			err.close();
		} catch (IOException e) {
			// ignore.
		}

		setParameters(new String[]{path});

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#parametersToString()
	 */
	protected String parametersToString() {
		String[] params = getParameters();
		if (params != null && params.length == 1) {
			StringBuffer sb = new StringBuffer();
			// We need to escape the double quotes and the backslash.
			String param = params[0];
			for (int j = 0; j < param.length(); j++) {
				char c = param.charAt(j);
				if (c == '"' || c == '\\') {
					sb.append('\\');
				}
				sb.append(c);
			}

			// If the string contains spaces instead of escaping
			// surround the parameter with double quotes.
			if (containsWhitespace(param)) {
				sb.insert(0, '"');
				sb.append('"');
			}
			return sb.toString().trim();
		}
		return super.parametersToString();
	}
}
