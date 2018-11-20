/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

/**
 * Typesafe enum pattern
 *
 * @author vhirsl
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ScannerInfoTypes {
	public static final ScannerInfoTypes COMPILER_COMMAND = new ScannerInfoTypes(1); // CCommandDSC
	public static final ScannerInfoTypes UNDISCOVERED_COMPILER_COMMAND = new ScannerInfoTypes(2); // CCommandDSC whose SI has not been resolved
	public static final ScannerInfoTypes INCLUDE_PATHS = new ScannerInfoTypes(10);
	public static final ScannerInfoTypes QUOTE_INCLUDE_PATHS = new ScannerInfoTypes(11);
	public static final ScannerInfoTypes SYMBOL_DEFINITIONS = new ScannerInfoTypes(12);
	public static final ScannerInfoTypes TARGET_SPECIFIC_OPTION = new ScannerInfoTypes(13);
	public static final ScannerInfoTypes COMPILER_VERSION_INFO = new ScannerInfoTypes(14);

	private final int _enum;

	private ScannerInfoTypes(int val) {
		_enum = val;
	}

	public final int toInt() {
		return _enum;
	}
}
