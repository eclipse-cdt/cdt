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
package org.eclipse.cdt.core.resources;

import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;

public class ScannerInfo implements IScannerInfo {

	private final Map macroMap;
	private final String[] includePaths;
	final static String[] EMPTY_ARRAY_STRING = new String[0];

	protected ScannerInfo(String[] includePaths, Map macroMap) {
		this.includePaths = includePaths;
		this.macroMap = macroMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
	 */
	public synchronized String[] getIncludePaths() {
		if (includePaths == null) {
			return EMPTY_ARRAY_STRING;
		}
		return includePaths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
	 */
	public synchronized Map getDefinedSymbols() {
		if (macroMap == null) {
			return Collections.EMPTY_MAP;
		}
		return macroMap;
	}

}
