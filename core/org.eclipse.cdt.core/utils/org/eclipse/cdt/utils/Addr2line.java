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

public class Addr2line {
	private String[] args;
	private Process addr2line;
	private BufferedReader stdout;
	private BufferedWriter stdin;
	private String lastaddr, lastsymbol, lastline;

	public Addr2line(String command, String[] params, String file) throws IOException {
		init(command, params, file);
	}

	public Addr2line(String command, String file) throws IOException {
		this(command, new String[0], file);
	}
	
	public Addr2line(String file) throws IOException {
		this("addr2line", file); //$NON-NLS-1$
	}

	protected void init(String command, String[] params, String file) throws IOException {
		if (params == null || params.length == 0) {
			args = new String[] {command, "-C", "-f", "-e", file}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			args = new String[params.length + 1];
			args[0] = command;
			System.arraycopy(params, 0, args, 1, params.length);
		}
		addr2line = ProcessFactory.getFactory().exec(args);
		stdin = new BufferedWriter(new OutputStreamWriter(addr2line.getOutputStream()));
		stdout = new BufferedReader(new InputStreamReader(addr2line.getInputStream()));			
	}
	
	protected void getOutput(String address) throws IOException {
		if ( address.equals(lastaddr) == false ) {
				stdin.write(address + "\n"); //$NON-NLS-1$
				stdin.flush();
				lastsymbol = stdout.readLine();
				lastline = stdout.readLine();
				lastaddr = address;
		}
	}

	public String getLine(long address) throws IOException {
		getOutput(Integer.toHexString((int)address));
		return lastline;
	}

	public String getFunction(long address) throws IOException {
		getOutput(Integer.toHexString((int)address));
		return lastsymbol;
	}	

	/**
	 * The format of the output:
	 *  addr2line -C -f -e hello
	 *  08048442
	 *  main
	 *  hello.c:39
	 */
	public String getFileName(long address) throws IOException {
		String filename = null;
		String line = getLine(address);
		int index1, index2;
		if (line != null && (index1 = line.lastIndexOf(':')) != -1) {
			// we do this because addr2line on win produces
			// <cygdrive/pathtoexc/C:/pathtofile:##>
			index2 = line.indexOf(':');
			if (index1 == index2) {
				index2 = 0;
			} else {
				index2--;
			}
			filename = line.substring(index2, index1);
		}
		return filename;
	}

	/**
	 * The format of the output:
	 *  addr2line -C -f -e hello
	 *  08048442
	 *  main
	 *  hello.c:39
	 */
	public int getLineNumber(long address) throws IOException {
		// We try to get the nearest match
		// since the symbol may not exactly align with debug info.
		// In C line number 0 is invalid, line starts at 1 for file, we use
		// this for validation.
		for (int i = 0; i <= 20; i += 4, address += i) {
			String line = getLine(address);
			if (line != null) {
				int colon = line.lastIndexOf(':');
				String number = line.substring(colon + 1);
				if (!number.startsWith("0")) { //$NON-NLS-1$
					return Integer.parseInt(number);
				}
			}
		}
		return -1;
	}

	public void dispose() {
		try {
			stdout.close();
			stdin.close();
			addr2line.getErrorStream().close();		
		} catch (IOException e) {
		}
		addr2line.destroy();
	}
}


