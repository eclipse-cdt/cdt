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
		int lineno = -1;
		String line = getLine(address);
		int colon;
		if (line != null && (colon = line.lastIndexOf(':')) != -1) {
			try {
				lineno = Integer.parseInt(line.substring(colon + 1));
				lineno = (lineno == 0) ? -1 : lineno;
			} catch(Exception e) {
			}
		}
		return lineno;
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


