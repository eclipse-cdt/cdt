/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
			List<String> list = new ArrayList<>();
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

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(args[0]);
		for (int i = 1; i < args.length; i++) {
			b.append(" "); //$NON-NLS-1$
			b.append(args[i]);
		}
		return b.toString();
	}

	/**
	 * Limit output to number of bytes
	 *  @since 5.8
	 */
	public byte[] getOutput(int limitBytes) throws IOException {
		Process objdump = ProcessFactory.getFactory().exec(args);
		try {
			StringBuilder buffer = new StringBuilder();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(objdump.getInputStream()));
			char[] buf = new char[4096];
			int len;
			while ((len = stdout.read(buf, 0, buf.length)) != -1) {
				if (limitBytes > 0 && buffer.length() + len >= limitBytes) {
					buffer.append(buf, 0, Math.min(len, limitBytes - buffer.length()));
					break;
				}
				buffer.append(buf, 0, len);
			}
			try {
				stdout.close();
			} catch (IOException e) {
				// ignore that
			}
			return buffer.toString().getBytes();
		} finally {
			objdump.destroy();
		}
	}

	public byte[] getOutput() throws IOException {
		return getOutput(0);
	}

	/** @since 5.8 */
	public InputStream getInputStream() throws IOException {
		Process objdump = ProcessFactory.getFactory().exec(args);
		objdump.getOutputStream().close();
		objdump.getErrorStream().close();
		return objdump.getInputStream();
	}

	public void dispose() {
	}

}
