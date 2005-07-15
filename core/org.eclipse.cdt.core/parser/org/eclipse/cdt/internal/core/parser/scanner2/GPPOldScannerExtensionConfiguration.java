/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * The main purpose of this class is to provide a subclass of GPPScannerExtensionConfiguration
 * for exclusive use in the old parser (ParserFactory.createScanner()) so that as old
 * hacks are removed when fixed in the new parser, they can still apply to the old parser.  This
 * way the old parser will not have to be maintained as it's no longer being tested.
 * 
 * @author dsteffle
 */
public class GPPOldScannerExtensionConfiguration extends
		GPPScannerExtensionConfiguration {
	
    private static final ObjectStyleMacro __cdecl = new ObjectStyleMacro(
            "__cdecl".toCharArray(), emptyCharArray); //$NON-NLS-1$

    private static final FunctionStyleMacro __attribute__ = new FunctionStyleMacro(
            "__attribute__".toCharArray(), //$NON-NLS-1$
            emptyCharArray, new char[][] { "arg".toCharArray() }); //$NON-NLS-1$

    private static final FunctionStyleMacro __declspec = new FunctionStyleMacro(
            "__declspec".toCharArray(), //$NON-NLS-1$
            emptyCharArray, new char[][] { "arg".toCharArray() }); //$NON-NLS-1$
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#getAdditionalMacros()
     */
    public CharArrayObjectMap getAdditionalMacros() {
        CharArrayObjectMap result = super.getAdditionalMacros();
        
        result.put(__cdecl.name, __cdecl);
        result.put(__attribute__.name, __attribute__);
        result.put(__declspec.name, __declspec);
        
        return result;
    }

}

