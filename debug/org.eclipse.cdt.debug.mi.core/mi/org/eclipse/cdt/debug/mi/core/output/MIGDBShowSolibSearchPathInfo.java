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
package org.eclipse.cdt.debug.mi.core.output;

import java.util.StringTokenizer;


/**
 * GDB/MI show parsing.
 * -gdb-show solib-search-path
 * ^done,value=""
 * (gdb) 
 * -gdb-set solib-search-path /tmp:/lib
 * ^done
 * (gdb) 
 * -gdb-show solib-search-path
 * ^done,value="/tmp:/lib"
 */
public class MIGDBShowSolibSearchPathInfo extends MIGDBShowInfo {

	String[] dirs = null;

	public MIGDBShowSolibSearchPathInfo(MIOutput o) {
		super(o);
	}

	public String[] getDirectories() {
		if (dirs == null) {
			String val = getValue();
			parseDirectories(val);
		}
		return dirs;
	}

	void parseDirectories(String d) {
		String sep = System.getProperty("path.separator", ":"); //$NON-NLS-1$ //$NON-NLS-2$
		StringTokenizer st = new StringTokenizer(d, sep);
		int count = st.countTokens();
		dirs = new String[count];
		for (int i = 0; st.hasMoreTokens() && i < count; i++) {
			dirs[i] = st.nextToken();
		}
	}
}
