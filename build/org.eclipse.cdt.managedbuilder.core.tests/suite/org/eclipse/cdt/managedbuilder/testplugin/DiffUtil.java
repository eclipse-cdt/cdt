/*******************************************************************************
 * Copyright (c) 2007, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.testplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class DiffUtil {
	public static String diff(String location1, String location2) {
		StringBuilder buf = new StringBuilder();
		try {
			String[] command = new String[] { "diff", "-ub", location1, location2 };
			Process p = Runtime.getRuntime().exec(command);
			InputStream in = p.getInputStream();
			if (in == null) {
				return null;
			}

			try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
				String line;
				while ((line = br.readLine()) != null) {
					buf.append("\n");
					buf.append(line);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return buf.toString();
	}
}
