/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.cdt.utils.spawner.ProcessFactory;

public class CygPath {
	private Process cygpath;
	private BufferedReader stdout;
	private BufferedWriter stdin;

	public CygPath(String command) throws IOException {		
		String[] args = {command, "--windows", "--file", "-"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cygpath = ProcessFactory.getFactory().exec(args);
		//cppfilt = new Spawner(args);
		stdin = new BufferedWriter(new OutputStreamWriter(cygpath.getOutputStream()));
		stdout = new BufferedReader(new InputStreamReader(cygpath.getInputStream()));
	}

	public CygPath() throws IOException {
		this("cygpath"); //$NON-NLS-1$
	}

	public String getFileName(String name) throws IOException {
		stdin.write(name + "\n"); //$NON-NLS-1$
		stdin.flush();
		String str = stdout.readLine();
		if ( str != null ) {
			return str.trim();
		}
		throw new IOException();
	}

	public void dispose() {
		try {
			stdout.close();
			stdin.close();
			cygpath.getErrorStream().close();		
		}
		catch (IOException e) {
		}
		cygpath.destroy();
	}
}
