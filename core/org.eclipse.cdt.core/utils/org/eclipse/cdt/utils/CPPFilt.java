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

public class CPPFilt {
	private String[] args;
	private Process cppfilt;
	private BufferedReader stdout;
	private BufferedWriter stdin;
	//private boolean isDisposed = false;

	public CPPFilt(String command, String[] params) throws IOException {
		init(command, params);
	}
	
	public CPPFilt(String command) throws IOException {
		this(command, new String[0]);
	}
	
	public CPPFilt() throws IOException {
		this("c++filt"); //$NON-NLS-1$
	}

	protected void init(String command, String[] params) throws IOException {
		if (params == null || params.length == 0) {
			args = new String[] {command};
		} else {
			args = new String[params.length + 1];
			args[0] = command;
			System.arraycopy(params, 0, args, 1, params.length);
		}
		cppfilt = ProcessFactory.getFactory().exec(args);
		stdin = new BufferedWriter(new OutputStreamWriter(cppfilt.getOutputStream()));
		stdout = new BufferedReader(new InputStreamReader(cppfilt.getInputStream()));			
	}

	public String getFunction(String symbol) throws IOException {
		stdin.write(symbol + "\n"); //$NON-NLS-1$
		stdin.flush();
		String str = stdout.readLine();
		if ( str != null ) {
			return str.trim();
		}
		throw new IOException();
	}

	public void dispose() {
		try {
			//stdin.write(-1);
			stdout.close();
			stdin.close();
			cppfilt.getErrorStream().close();		
		}
		catch (IOException e) {
		}
		cppfilt.destroy();
		//isDisposed = true;
	}
}
