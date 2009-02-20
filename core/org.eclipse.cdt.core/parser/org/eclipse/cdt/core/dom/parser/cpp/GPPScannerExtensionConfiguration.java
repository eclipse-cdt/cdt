/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Ed Swartz (Nokia)
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.GNUScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;

/**
 * Configures the preprocessor for c++-sources as accepted by g++.
 */
public class GPPScannerExtensionConfiguration extends GNUScannerExtensionConfiguration {

	private static GPPScannerExtensionConfiguration sInstance= new GPPScannerExtensionConfiguration();
	/**
	 * @since 5.1
	 */
	public static GPPScannerExtensionConfiguration getInstance() {
		return sInstance;
	}
	
	@SuppressWarnings("nls")
	public GPPScannerExtensionConfiguration() {
		addMacro("__null", "0");  
		addMacro("__builtin_offsetof(T,m)", "(reinterpret_cast <size_t>(&reinterpret_cast <const volatile char &>(static_cast<T*> (0)->m)))");
		addKeyword(Keywords.c_COMPLEX, IToken.t__Complex);
		addKeyword(Keywords.c_IMAGINARY, IToken.t__Imaginary);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#supportMinAndMaxOperators()
     */
    @Override
	public boolean supportMinAndMaxOperators() {
        return true;
    }
}
