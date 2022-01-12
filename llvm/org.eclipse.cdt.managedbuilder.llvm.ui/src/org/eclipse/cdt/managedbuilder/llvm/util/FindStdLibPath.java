/*******************************************************************************
 * Copyright (c) 2011-2013 Nokia Siemens Networks Oyj, Finland.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The purpose is to find a path where stdc++ library is located.
 * Currently the shell script is only for Linux.
 *
 * TODO: It might not be able to execute scripts therefore place the command into a variable.
 */
public class FindStdLibPath {

	private static final String WIN_SCRIPT = "scripts/find_path.bat"; //$NON-NLS-1$
	private static final String STD_LIB = "libstdc++.a"; //$NON-NLS-1$
	private static final String UNIX_SCRIPT = " echo `locate libstdc++.a | sort -r | head -1 | sed \"s/libstdc++.a$//\"` "; //$NON-NLS-1$
	private static final String MAC_SCRIPT = " echo `locate libstdc++.dylib | sort -r | head -1 | sed \"s/libstdc++.dylib$//\"` "; //$NON-NLS-1$

	/**
	 * Find stdc++ library path.
	 *
	 * @return Stdc++ library path.
	 */
	public static String find() {
		ProcessBuilder pb = null;
		String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		if (os.indexOf("win") >= 0) { //$NON-NLS-1$
			pb = new ProcessBuilder("cmd", "/c", WIN_SCRIPT + " " + STD_LIB); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$
			pb = new ProcessBuilder("bash", "-c", UNIX_SCRIPT); //$NON-NLS-1$//$NON-NLS-2$
		} else if (os.indexOf("mac") >= 0) { //$NON-NLS-1$
			pb = new ProcessBuilder("bash", "-c", MAC_SCRIPT); //$NON-NLS-1$//$NON-NLS-2$
		} else {
			return null;
		}
		try {
			Process p = pb.start();
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			line = input.readLine();
			input.close();
			if (line != null) {
				return line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
