/*******************************************************************************
 * Copyright (c) 2023 Julian Waters.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.AbstractScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.Keywords;

public class CPPScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {
	private static CPPScannerExtensionConfiguration config = null;

	@SuppressWarnings("nls")
	public CPPScannerExtensionConfiguration() {
		addMacro("__has_include", ""); // C++17

		addPreprocessorKeyword(Keywords.cWARNING, IPreprocessorDirective.ppWarning); // C++23
	}

	public static CPPScannerExtensionConfiguration getConfiguration(IScannerInfo info) {
		if (config == null) {
			config = new CPPScannerExtensionConfiguration();
		}
		return config;
	}

	/**
	 * @since 5.5
	 */
	@Override
	public boolean supportRawStringLiterals() {
		return true;
	}

	/**
	 * User Defined Literals
	 * @since 5.10
	 */
	@Override
	public boolean supportUserDefinedLiterals() {
		return true;
	}

	@Override
	public boolean supportDigitSeparators() {
		return true;
	}
}
