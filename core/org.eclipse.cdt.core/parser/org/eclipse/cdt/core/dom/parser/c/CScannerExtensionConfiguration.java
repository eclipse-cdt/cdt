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

package org.eclipse.cdt.core.dom.parser.c;

import org.eclipse.cdt.core.dom.parser.AbstractScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.Keywords;

public class CScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {
	private static CScannerExtensionConfiguration config = null;

	public CScannerExtensionConfiguration() {
		addPreprocessorKeyword(Keywords.cWARNING, IPreprocessorDirective.ppWarning); // C23
	}

	public static CScannerExtensionConfiguration getConfiguration(IScannerInfo info) {
		if (config == null) {
			config = new CScannerExtensionConfiguration();
		}
		return config;
	}

}
