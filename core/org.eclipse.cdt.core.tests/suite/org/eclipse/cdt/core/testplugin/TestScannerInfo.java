/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.IExtendedScannerInfo;

public class TestScannerInfo implements IExtendedScannerInfo {
	private static final String[] EMPTY = new String[0];
	private static final Map EMPTY_MAP = new HashMap(0);
	private String[] fIncludes;
	private String[] fIncludeFiles;
	private String[] fMacroFiles;

	public TestScannerInfo(String[] includes, String[] includeFiles, String[] macroFiles) {
		fIncludes= includes;
		fIncludeFiles= includeFiles;
		fMacroFiles= macroFiles;
	}
	@Override
	public Map getDefinedSymbols() {
		return EMPTY_MAP;
	}

	@Override
	public String[] getIncludePaths() {
		return fIncludes == null ? EMPTY : fIncludes;
	}
	@Override
	public String[] getIncludeFiles() {
		return fIncludeFiles == null ? EMPTY: fIncludeFiles;
	}
	@Override
	public String[] getLocalIncludePath() {
		return null;
	}
	@Override
	public String[] getMacroFiles() {
		return fMacroFiles == null ? EMPTY: fMacroFiles;
	}
}
