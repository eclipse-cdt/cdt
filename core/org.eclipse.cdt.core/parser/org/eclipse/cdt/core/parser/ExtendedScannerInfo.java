/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

/**
 * Implementation for the {@link IExtendedScannerInfo} interface. Allows to configure the preprocessor.
 */
public class ExtendedScannerInfo extends ScannerInfo implements IExtendedScannerInfo {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private String[] macroFiles;
	private String[] includeFiles;
	private String[] localIncludePaths;

	public ExtendedScannerInfo() {
	}

	public ExtendedScannerInfo(Map<String, String> definedSymbols, String[] includePaths) {
		super(definedSymbols, includePaths);
	}

	public ExtendedScannerInfo(Map<String, String> definedSymbols, String[] includePaths,
			String[] macroFiles, String[] includeFiles) {

		super(definedSymbols, includePaths);
		this.macroFiles = macroFiles;
		this.includeFiles = includeFiles;
	}

	/**
	 * @since 5.3
	 */
	public ExtendedScannerInfo(Map<String, String> definedSymbols, String[] includePaths,
			String[] macroFiles, String[] includeFiles, String[] localIncludePaths) {
		
		super(definedSymbols, includePaths);
		this.macroFiles = macroFiles;
		this.includeFiles = includeFiles;
		this.localIncludePaths = localIncludePaths;
	}

	public ExtendedScannerInfo(IScannerInfo info) {
		super(info.getDefinedSymbols(), info.getIncludePaths());
		if (info instanceof IExtendedScannerInfo) {
			IExtendedScannerInfo einfo = (IExtendedScannerInfo) info;
			macroFiles = einfo.getMacroFiles();
			includeFiles = einfo.getIncludeFiles();
			localIncludePaths = einfo.getLocalIncludePath();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IExtendedScannerInfo#getMacroFiles()
	 */
	@Override
	public String[] getMacroFiles() {
		if (macroFiles == null)
			return EMPTY_STRING_ARRAY;
		return macroFiles;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IExtendedScannerInfo#getIncludeFiles()
	 */
	@Override
	public String[] getIncludeFiles() {
		if (includeFiles == null)
			return EMPTY_STRING_ARRAY;
		return includeFiles;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IExtendedScannerInfo#getLocalIncludePath()
	 */
	@Override
	public String[] getLocalIncludePath() {
		if (localIncludePaths == null)
			return EMPTY_STRING_ARRAY;
		return localIncludePaths;
	}
}
