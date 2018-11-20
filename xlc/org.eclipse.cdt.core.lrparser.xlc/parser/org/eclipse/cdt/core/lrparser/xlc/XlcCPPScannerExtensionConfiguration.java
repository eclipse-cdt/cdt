/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc;

import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;

public class XlcCPPScannerExtensionConfiguration extends GPPScannerExtensionConfiguration {

	private static XlcCPPScannerExtensionConfiguration instance;

	private XlcCPPScannerExtensionConfiguration() {
	}

	public static synchronized XlcCPPScannerExtensionConfiguration getInstance() {
		if (instance == null)
			instance = new XlcCPPScannerExtensionConfiguration();
		return instance;
	}

	@Override
	public boolean supportUTFLiterals() {
		return true;
	}

	@Override
	public char[] supportAdditionalNumericLiteralSuffixes() {
		return "dflij".toCharArray(); //$NON-NLS-1$
	}
}
