/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
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
package org.eclipse.cdt.core.resources;

import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.parser.IExtendedScannerInfo;

/**
 * #@noextend This class is not intended to be subclassed by clients.
 * #@noinstantiate This class is not intended to be instantiated by clients. Note
 *     that protected constructor won't allow to instantiate this class
 *     outside the package anyway, so it is not really API.
 *
 * @deprecated Since CDT 4.0 not used for the "new style" projects.
 */
@Deprecated
public class ScannerInfo implements IExtendedScannerInfo {

	private final Map<String, String> fMacroMap;
	private final String[] fSystemIncludePaths;
	private final String[] fMacroFiles;
	private final String[] fIncludeFiles;
	private final String[] fLocalIncludePaths;
	final static String[] EMPTY_ARRAY_STRING = new String[0];

	protected ScannerInfo(String[] systemIncludePaths, String[] localIncludePaths, String[] includeFiles,
			Map<String, String> macroMap, String[] macroFiles) {
		fSystemIncludePaths = (systemIncludePaths == null) ? EMPTY_ARRAY_STRING : systemIncludePaths;
		fLocalIncludePaths = (localIncludePaths == null) ? EMPTY_ARRAY_STRING : localIncludePaths;
		fIncludeFiles = (includeFiles == null) ? EMPTY_ARRAY_STRING : includeFiles;
		fMacroFiles = (macroFiles == null) ? EMPTY_ARRAY_STRING : macroFiles;
		fMacroMap = nonNullMap(macroMap);
	}

	private Map<String, String> nonNullMap(Map<String, String> macroMap) {
		if (macroMap == null) {
			return Collections.emptyMap();
		}
		return macroMap;
	}

	@Override
	public synchronized String[] getIncludePaths() {
		return fSystemIncludePaths;
	}

	@Override
	public synchronized Map<String, String> getDefinedSymbols() {
		return fMacroMap;
	}

	@Override
	public String[] getMacroFiles() {
		return fMacroFiles;
	}

	@Override
	public String[] getIncludeFiles() {
		return fIncludeFiles;
	}

	@Override
	public String[] getLocalIncludePath() {
		return fLocalIncludePaths;
	}

}
