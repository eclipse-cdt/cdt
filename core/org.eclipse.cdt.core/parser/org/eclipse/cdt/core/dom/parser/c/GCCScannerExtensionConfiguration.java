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
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.c;

import org.eclipse.cdt.core.dom.parser.GNUScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * @author jcamelon
 */
public class GCCScannerExtensionConfiguration extends GNUScannerExtensionConfiguration {

	private static IMacro[] sAdditionalMacros;
	private static CharArrayIntMap sAdditionalKeywords;
	static {
		final IMacro[] macros = GNUScannerExtensionConfiguration.getAdditionalGNUMacros();
		sAdditionalMacros= new IMacro[macros.length + 2];
		System.arraycopy(macros, 0, sAdditionalMacros, 0, macros.length);
		sAdditionalMacros[macros.length]= createMacro("__null", "(void*)0");  //$NON-NLS-1$ //$NON-NLS-2$
		sAdditionalMacros[macros.length + 1]= createMacro("_Pragma(arg)", "");  //$NON-NLS-1$//$NON-NLS-2$
		
		sAdditionalKeywords= new CharArrayIntMap(10, -1);
		GNUScannerExtensionConfiguration.addAdditionalGNUKeywords(sAdditionalKeywords);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#supportMinAndMaxOperators()
     */
    @Override
	public boolean supportMinAndMaxOperators() {
        return false;
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
