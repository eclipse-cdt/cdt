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
******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.GNUScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * @author jcamelon
 */
public class GPPScannerExtensionConfiguration extends GNUScannerExtensionConfiguration {

	private static IMacro[] sAdditionalMacros;
	private static CharArrayIntMap sAdditionalKeywords;
	static {
		final IMacro[] macros = GNUScannerExtensionConfiguration.getAdditionalGNUMacros();
		sAdditionalMacros= new IMacro[macros.length + 1];
		System.arraycopy(macros, 0, sAdditionalMacros, 0, macros.length);
		sAdditionalMacros[macros.length]= createMacro("__null", "0"); //$NON-NLS-1$ //$NON-NLS-2$

		sAdditionalKeywords= new CharArrayIntMap(10, -1);
		GNUScannerExtensionConfiguration.addAdditionalGNUKeywords(sAdditionalKeywords);
		sAdditionalKeywords.put( Keywords.c_COMPLEX, IToken.t__Complex );
		sAdditionalKeywords.put( Keywords.c_IMAGINARY, IToken.t__Imaginary );
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#supportMinAndMaxOperators()
     */
    @Override
	public boolean supportMinAndMaxOperators() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#getAdditionalMacros()
     */
    @Override
	public IMacro[] getAdditionalMacros() {
    	return sAdditionalMacros;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#getAdditionalKeywords()
     */
    @Override
	public CharArrayIntMap getAdditionalKeywords() {
		return sAdditionalKeywords;
    }
}
