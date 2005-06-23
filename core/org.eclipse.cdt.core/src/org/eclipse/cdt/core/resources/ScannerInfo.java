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
package org.eclipse.cdt.core.resources;

import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.parser.IExtendedScannerInfo;

public class ScannerInfo implements IExtendedScannerInfo {

	private final Map fMacroMap;
	private final String[] fSystemIncludePaths;
	private final String[] fMacroFiles;
	private final String[] fIncludeFiles;
	private final String[] fLocalIncludePaths;
	final static String[] EMPTY_ARRAY_STRING = new String[0];

	protected ScannerInfo(String[] systemIncludePaths, String[] localIncludePaths, String[] includeFiles,
			Map macroMap, String[] macroFiles) {
		fSystemIncludePaths = (systemIncludePaths == null) ? EMPTY_ARRAY_STRING : systemIncludePaths;
		fLocalIncludePaths = (localIncludePaths == null) ? EMPTY_ARRAY_STRING : localIncludePaths;
		fIncludeFiles = (includeFiles == null) ? EMPTY_ARRAY_STRING : includeFiles;
		fMacroFiles = (macroFiles == null) ? EMPTY_ARRAY_STRING : macroFiles;
		fMacroMap = (macroMap == null) ? Collections.EMPTY_MAP : macroMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
	 */
	public synchronized String[] getIncludePaths() {
		return fSystemIncludePaths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
	 */
	public synchronized Map getDefinedSymbols() {
		return fMacroMap;
	}

	public String[] getMacroFiles() {
		return fMacroFiles;
	}

	public String[] getIncludeFiles() {
		return fIncludeFiles;
	}

	public String[] getLocalIncludePath() {
		return fLocalIncludePaths;
	}

}
