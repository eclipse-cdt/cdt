/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.utils.spawner.ProcessFactory;

/**
 */
public class NM {

	public class AddressNamePair {
		public String name;

		public long address;

		public AddressNamePair(String n, long a) {
			name = n;
			address = a;
		}

		public String toString() {
			return (name + "@" + Long.toHexString(address)); //$NON-NLS-1$
		}

	}

	private static Pattern undef_pattern = null;
	private static Pattern normal_pattern = null;

	private List undef_symbols;
	private List text_symbols;
	private List bss_symbols;
	private List data_symbols;

	private void parseOutput(InputStream stream) throws IOException {

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		String line;

		// See matcher.java for regular expression string data definitions.

		if (undef_pattern == null) {
			undef_pattern = Pattern.compile("^\\s+U\\s+(\\S+)"); //$NON-NLS-1$
		}

		if (normal_pattern == null) {
			normal_pattern = Pattern.compile("^(\\S+)\\s+([AaTtBbDd])\\s+(\\S+)"); //$NON-NLS-1$
		}
		while ((line = reader.readLine()) != null) {
			Matcher undef_matcher = undef_pattern.matcher(line);
			Matcher normal_matcher = normal_pattern.matcher(line);
			try {
				if (undef_matcher.matches()) {
					undef_symbols.add(undef_matcher.group(1));
				} else if (normal_matcher.matches()) {
					char type = normal_matcher.group(2).charAt(0);
					String name = normal_matcher.group(3);
					long address = Long.parseLong(normal_matcher.group(1), 16);
					AddressNamePair val = new AddressNamePair(name, address);
					
					switch (type) {
					case 'T':
					case 't':
						text_symbols.add(val);
						break;
					case 'B':
					case 'b':
						bss_symbols.add(val);
						break;
					case 'D':
					case 'd':
						data_symbols.add(val);
						break;
					}
				}
			} catch (NumberFormatException e) {
				// ignore.
			} catch (IndexOutOfBoundsException e) {
				// ignore
			}
		}

	}

	public NM(String file, boolean dynamic_only) throws IOException {
		this ("nm", file, dynamic_only); //$NON-NLS-1$
	}

	public NM(String command, String file, boolean dynamic_only) throws IOException {
		this(command, (dynamic_only) ? new String[] {"-C", "-D"}: null, file); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public NM(String command, String param, String file) throws IOException {
		String[] params;
		if (param == null || param.length() == 0) {
			params = new String[0];
		} else {
			// FIXME: This is wrong we have to check for quoted strings.
			params = param.split("\\s"); //$NON-NLS-1$
		}
		init(command, params, file);
	}

	public NM(String command, String[] params, String file) throws IOException {
		init(command, params, file);
	}
	
	protected void init(String command, String[] params, String file) throws IOException {
		String[] args = null;
		if (params == null || params.length == 0) {
			args = new String[] {command, "-C", file}; //$NON-NLS-1$
		} else {
			args = new String[params.length + 1];
			args[0] = command;
			System.arraycopy(params, 0, args, 1, params.length);
		}

		undef_symbols = new ArrayList();
		text_symbols = new ArrayList();
		data_symbols = new ArrayList();
		bss_symbols = new ArrayList();
		Process process = ProcessFactory.getFactory().exec(args);
		parseOutput(process.getInputStream());
		process.destroy();
	}

	public String[] getUndefSymbols() {
		return (String[]) undef_symbols.toArray(new String[0]);
	}

	public AddressNamePair[] getTextSymbols() {
		return (AddressNamePair[]) text_symbols.toArray(new AddressNamePair[0]);
	}

	public AddressNamePair[] getDataSymbols() {
		return (AddressNamePair[]) data_symbols.toArray(new AddressNamePair[0]);
	}
	
	public AddressNamePair[] getBSSSymbols() {
		return (AddressNamePair[]) bss_symbols.toArray(new AddressNamePair[0]);
	}

}
