/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.util.StringTokenizer;

import org.eclipse.cdt.make.internal.core.makefile.Statement;

public class Include extends Statement {

	String[] filenames;

	public Include(String line) {
		parse(line);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("include");
		for (int i = 0; i < filenames.length; i++) {
			sb.append(' ').append(filenames[i]);
		}
		return sb.toString();
	}

	public String[] getFilenames() {
		return filenames;
	}

	/**
	 *  Format of the include directive:
	 * 	include filename1 filename2 ...
	 */
	protected void parse(String line) {
		StringTokenizer st = new StringTokenizer(line);
		int count = st.countTokens();
		if (count > 0) {
			filenames = new String[count - 1];
			for (int i = 0; i < count; i++) {
				if (i == 0) {
					// ignore the "include" keyword.
					continue;
				}
				filenames[i] = st.nextToken();
			}
		} else {
			filenames = new String[0];
		}
	}
}
