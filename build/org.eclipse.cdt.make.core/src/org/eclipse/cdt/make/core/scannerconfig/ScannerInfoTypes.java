/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

/**
 * Typesafe enum pattern
 * 
 * @author vhirsl
 */
public class ScannerInfoTypes {
    public static final ScannerInfoTypes COMPILER_COMMAND = new ScannerInfoTypes(1); // CCommandDSC
    public static final ScannerInfoTypes INCLUDE_PATHS = new ScannerInfoTypes(2);
    public static final ScannerInfoTypes QUOTE_INCLUDE_PATHS = new ScannerInfoTypes(3);
    public static final ScannerInfoTypes SYMBOL_DEFINITIONS = new ScannerInfoTypes(4);
    public static final ScannerInfoTypes TARGET_SPECIFIC_OPTION = new ScannerInfoTypes(5) ; 
    public static final ScannerInfoTypes COMPILER_VERSION_INFO = new ScannerInfoTypes(6);

    private final int _enum;

    private ScannerInfoTypes(int val) {
        _enum = val;
    }
    public final int toInt() {
        return _enum;
    }
}
