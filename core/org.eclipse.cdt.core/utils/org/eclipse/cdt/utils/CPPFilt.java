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

public class CPPFilt {
	private Process cppfilt;
	private BufferedReader stdout;
	private BufferedWriter stdin;
	private String function;
			
	public CPPFilt() throws IOException {
		String[] args = {"c++filt"};
		cppfilt = ProcessFactory.getFactory().exec(args);
		//cppfilt = new Spawner(args);
		stdin = new BufferedWriter(new OutputStreamWriter(cppfilt.getOutputStream()));
		stdout = new BufferedReader(new InputStreamReader(cppfilt.getInputStream()));
	}

	public String getFunction(String symbol) throws IOException {
		stdin.write(symbol + "\n");
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
	}
}
