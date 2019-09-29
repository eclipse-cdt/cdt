/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.cdt.core.dom.parser.AbstractScannerExtensionConfiguration;

/**
 * A minimalistic scanner configuration for the LR parser.
 *
 * @author Mike Kucera
 *
 */
@SuppressWarnings("nls")
public class ScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {

	private ScannerExtensionConfiguration() {
		addMacro("__STDC__", "1");
	}

	public static ScannerExtensionConfiguration createC() {
		ScannerExtensionConfiguration sec = new ScannerExtensionConfiguration();
		sec.addMacro("__null", "(void *)0");
		return sec;
	}

	public static ScannerExtensionConfiguration createCPP() {
		ScannerExtensionConfiguration sec = new ScannerExtensionConfiguration();
		sec.addMacro("__null", "0");
		return sec;
	}

	@Override
	public boolean support$InIdentifiers() {
		return true;
	}

	@Override
	public boolean supportUserDefinedLiterals() {
		return false;
	}
}
