/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.c;

import org.eclipse.cdt.core.dom.parser.GNUScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * @author jcamelon
 */
public class GCCScannerExtensionConfiguration extends GNUScannerExtensionConfiguration {

	private static IMacro[] sAdditionalMacros;
	static {
		final IMacro[] macros = GNUScannerExtensionConfiguration.getAdditionalGNUMacros();
		sAdditionalMacros= new IMacro[macros.length+1];
		System.arraycopy(macros, 0, sAdditionalMacros, 0, macros.length);
		sAdditionalMacros[macros.length]= createMacro("_Pragma(arg)", "");  //$NON-NLS-1$//$NON-NLS-2$
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#supportMinAndMaxOperators()
     */
    public boolean supportMinAndMaxOperators() {
        return false;
    }
    

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#getAdditionalMacros()
     */
    public IMacro[] getAdditionalMacros() {
    	return sAdditionalMacros;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#getAdditionalKeywords()
     */
    public CharArrayIntMap getAdditionalKeywords() {
        CharArrayIntMap result = new CharArrayIntMap( 4, -1 );
		result.put( GCCKeywords.cp__ALIGNOF__, IGCCToken.t___alignof__ );
		result.put( GCCKeywords.cpTYPEOF, IGCCToken.t_typeof );
		result.put( GCCKeywords.cp__ATTRIBUTE__, IGCCToken.t__attribute__ );
		result.put( GCCKeywords.cp__DECLSPEC, IGCCToken.t__declspec );
        return result;
    }

}
