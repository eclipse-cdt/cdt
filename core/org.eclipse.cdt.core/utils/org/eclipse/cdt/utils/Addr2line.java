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

public class Addr2line {
	private Process addr2line;
	private BufferedReader stdout;
	private BufferedWriter stdin;
	private String lastaddr, lastsymbol, lastline;

	public Addr2line(String command, String file) throws IOException {
		String[] args = {command, "-C", "-f", "-e", file};
		addr2line = ProcessFactory.getFactory().exec(args);
		stdin = new BufferedWriter(new OutputStreamWriter(addr2line.getOutputStream()));
		stdout = new BufferedReader(new InputStreamReader(addr2line.getInputStream()));			
	}

	public Addr2line(String file) throws IOException {
		this("addr2line", file);
	}

	private void getOutput(String address) throws IOException {
		if ( address.equals(lastaddr) == false ) {
				stdin.write(address + "\n");
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

	public void dispose() {
		try {
			//stdin.write(-1);
			stdout.close();
			stdin.close();
			addr2line.getErrorStream().close();		
		}
		catch (IOException e) {
		}
		addr2line.destroy();
	}
}


