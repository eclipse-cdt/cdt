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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.utils.spawner.ProcessFactory;

/*
 * Objdump
 */
public class Objdump {
	String[] args;

	public Objdump(String command, String param, String file) throws IOException {
		String[] params;
		if (param == null || param.length() == 0) {
			params = new String[0];
		} else {
			// FIXME: This is wrong we have to check for quoted strings.
			List list = new ArrayList();
			StringTokenizer st = new StringTokenizer(param);
			while (st.hasMoreTokens()) {
				list.add(st.nextToken());
			}
			params = new String[list.size()];
			list.toArray(params);
		}
		init(command, params, file);
	}

	public Objdump(String command, String[] params, String file) throws IOException {
		init(command, params, file);
	}
	
	public Objdump(String file) throws IOException {
		this("objdump", new String[0], file); //$NON-NLS-1$
	}

	protected void init(String command, String[] params, String file) throws IOException {
		if (params == null || params.length == 0) {
			args = new String[] { command, "-C", "-x", "-S", file }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			args = new String[params.length + 1];
			args[0] = command;
			System.arraycopy(params, 0, args, 1, params.length);
		}
	}

	public byte[] getOutput() throws IOException {
		Process objdump = ProcessFactory.getFactory().exec(args);
		StringBuffer buffer = new StringBuffer();
		BufferedReader stdout = new BufferedReader(new InputStreamReader(objdump.getInputStream()));
		char[] buf = new char[512];
		int len;
		while ((len = stdout.read(buf, 0, buf.length)) != -1) {
			buffer.append(buf, 0, len);
		}
		stdout.close();
		objdump.destroy();
		return buffer.toString().getBytes();
	}

	public void dispose() {
	}

}
