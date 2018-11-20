/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Collections;
import java.util.Map;

/**
 * Implementation of the {@link IScannerInfo} interface. Allows to configure the preprocessor.
 */
public class ScannerInfo implements IScannerInfo {
	private final Map<String, String> definedSymbols;
	private final String[] includePaths;

	public ScannerInfo() {
		this(null, null);
	}

	public ScannerInfo(Map<String, String> macroDefinitions) {
		this(macroDefinitions, null);
	}

	public ScannerInfo(Map<String, String> macroDefinitions, String[] includeSearchPath) {
		definedSymbols = macroDefinitions != null ? macroDefinitions : Collections.<String, String>emptyMap();
		includePaths = includeSearchPath != null ? includeSearchPath : new String[] {};
	}

	@Override
	public Map<String, String> getDefinedSymbols() {
		return definedSymbols;
	}

	@Override
	public String[] getIncludePaths() {
		return includePaths;
	}
}
