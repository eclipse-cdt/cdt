/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

/**
 * Implementation for the {@link IExtendedScannerInfo} interface. Allows to configure
 * the preprocessor.
 * @since 5.5
 */
public class ExtendedScannerInfo extends ScannerInfo implements IExtendedScannerInfo {
	private static final String[] EMPTY_STRING_ARRAY = {};
	private String[] macroFiles;
	private String[] includeFiles;
	private String[] localIncludePaths;
	private IncludeExportPatterns includeExportPatterns;
	private IParserSettings parserSettings;

	public ExtendedScannerInfo() {
	}

	public ExtendedScannerInfo(Map<String, String> definedSymbols, String[] includePaths) {
		super(definedSymbols, includePaths);
	}

	public ExtendedScannerInfo(Map<String, String> definedSymbols, String[] includePaths, String[] macroFiles,
			String[] includeFiles) {
		super(definedSymbols, includePaths);
		this.macroFiles = macroFiles;
		this.includeFiles = includeFiles;
	}

	/**
	 * @since 5.3
	 */
	public ExtendedScannerInfo(Map<String, String> definedSymbols, String[] includePaths, String[] macroFiles,
			String[] includeFiles, String[] localIncludePaths) {
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
		if (info instanceof ExtendedScannerInfo) {
			ExtendedScannerInfo extendedScannerInfo = (ExtendedScannerInfo) info;
			includeExportPatterns = extendedScannerInfo.includeExportPatterns;
			parserSettings = extendedScannerInfo.parserSettings;
		}
	}

	@Override
	public String[] getMacroFiles() {
		if (macroFiles == null)
			return EMPTY_STRING_ARRAY;
		return macroFiles;
	}

	@Override
	public String[] getIncludeFiles() {
		if (includeFiles == null)
			return EMPTY_STRING_ARRAY;
		return includeFiles;
	}

	@Override
	public String[] getLocalIncludePath() {
		if (localIncludePaths == null)
			return EMPTY_STRING_ARRAY;
		return localIncludePaths;
	}

	/**
	 * Returns the regular expression patterns matching export directives for included files.
	 * @see IncludeExportPatterns
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.5
	 */
	public IncludeExportPatterns getIncludeExportPatterns() {
		return includeExportPatterns;
	}

	/**
	 * Sets the regular expression patterns matching export directives for included files.
	 * @see IncludeExportPatterns
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.5
	 */
	public void setIncludeExportPatterns(IncludeExportPatterns patterns) {
		includeExportPatterns = patterns;
	}

	/**
	 * Returns additional settings for the parser.
	 * @since 5.6
	 */
	public IParserSettings getParserSettings() {
		return parserSettings;
	}

	/**
	 * Sets additional settings for configuring the parser.
	 * @since 5.6
	 */
	public void setParserSettings(IParserSettings parserSettings) {
		this.parserSettings = parserSettings;
	}
}
