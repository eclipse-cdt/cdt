package org.eclipse.cdt.utils;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

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
		String[] args = {command, "--windows", "--file", "-"};
		cygpath = ProcessFactory.getFactory().exec(args);
		//cppfilt = new Spawner(args);
		stdin = new BufferedWriter(new OutputStreamWriter(cygpath.getOutputStream()));
		stdout = new BufferedReader(new InputStreamReader(cygpath.getInputStream()));
	}

	public CygPath() throws IOException {
		this("cygpath");
	}

	public String getFileName(String name) throws IOException {
		stdin.write(name + "\n");
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
