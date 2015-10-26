/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

public class Addr2line {
	private String[] args;
	private Process addr2line;
	private BufferedReader stdout;
	private BufferedWriter stdin;
	private String lastaddr;
	private List<String> lastlines, lastsymbols;
	private static final Pattern OUTPUT_PATTERN = Pattern.compile("(.*)( \\(discriminator.*\\))");  //$NON-NLS-1$
	//private boolean isDisposed = false;

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
			args = new String[] {command, "-C", "-i", "-f", "-e", file}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
				List<String> lastsymbols = new ArrayList<>();
				List<String> lastlines = new ArrayList<>();
				String line = stdout.readLine();
				Iterator<String> lines = stdout.lines().collect(Collectors.toList()).iterator();
				while (lines.hasNext()) {
					lastsymbols.add(lines.next());
					lastlines.add(lines.next());
//					if (stdout.lines()
//					line = stdout.readLine();
				}
				lastaddr = address;
		}
	}

	public String getLine(IAddress address) throws IOException {
		return (String) firstStringOrNull(getLines(address));
	}
	
	/**
	 * @since 5.12
	 */
	public String[] getLines(IAddress address) throws IOException {
		getOutput(address.toString(16));
		return lastlines.toArray(new String[] {});
	}

	public String getFunction(IAddress address) throws IOException {
		return (String) firstStringOrNull(getFunctions(address));
	}
	
	private Object firstStringOrNull(Object[] strings) {
		return strings.length > 0 ? strings[0] : null;
	}

	/**
	 * @since 5.12
	 */
	public String[] getFunctions(IAddress address) throws IOException {
		getOutput(address.toString(16));
		return lastsymbols.toArray(new String[] {});
	}

	/**
	 * The format of the output:
	 *  addr2line -C -f -e hello
	 *  08048442
	 *  main
	 *  hello.c:39
	 */
	public String getFileName(IAddress address) throws IOException {
		String[] fileNames = getFileNames(address);
		return fileNames.length > 0 ? fileNames[0] : null;
	}
	
	/**
	 * @since 5.12
	 */
	public String[] getFileNames(IAddress address) throws IOException {
		String filename = null;
		String lines[] = getLines(address);
		List<String> filenames = new ArrayList<>();
		int index1, index2;
		for (String line : lines) {
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
				filenames.add(filename);
			}
		}

		return filenames.toArray(new String[] {});
	}

	/**
	 * The format of the output:
	 *  addr2line -C -f -e hello
	 *  08048442
	 *  main
	 *  hello.c:39
	 * @since 5.12
	 */
	public int[] getLineNumbers(IAddress address) throws IOException {
		// We try to get the nearest match
		// since the symbol may not exactly align with debug info.
		String[] lines = getLines(address);
		boolean validAddress = false;
		for (int i = 0; i <= 20; i += 4, address = address.add(i)) {
			lines = getLines(address);
			if (lines.length > 0) {
				String line = parserOutput(lines[0]);
				// We have match for this address
				if (line != null) {
					validAddress = true;
					break;
				}
			}
		}

		// In C line number 0 is invalid, line starts at 1 for file, we use
		// this for validation. 
		List<Integer> lineNumbers = new ArrayList<>();
		if (validAddress) {
			for (String line : lines) {
				line = parserOutput(line);
				int colon = line.lastIndexOf(':');
				String number = line.substring(colon + 1);
				if (!number.startsWith("0")) { //$NON-NLS-1$
					try {
						lineNumbers.add(Integer.parseInt(number));
					} catch (Exception ex) {
						lineNumbers.add(-1);
					}
				}
			}
		}

		return lineNumbers.stream().mapToInt(a->a).toArray();
	}
	
	public int getLineNumber(IAddress address) throws IOException {
		int[] lineNumbers = getLineNumbers(address);
		return lineNumbers.length > 0 ? lineNumbers[0] : -1;
	}

	public void dispose() {
		try {
			stdout.close();
			stdin.close();
			addr2line.getErrorStream().close();		
		} catch (IOException e) {
		}
		addr2line.destroy();
		//isDisposed = true;
	}
	
	private String parserOutput(String line) {
		Matcher matcher = OUTPUT_PATTERN.matcher(line);
		if (matcher.matches() && matcher.groupCount() > 1) {
			line = matcher.group(1);
		}
		return line;
	}
}


