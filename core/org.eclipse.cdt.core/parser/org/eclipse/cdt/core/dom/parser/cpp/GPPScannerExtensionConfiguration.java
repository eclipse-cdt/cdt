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
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.GNUScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * @author jcamelon
 */
public class GPPScannerExtensionConfiguration extends GNUScannerExtensionConfiguration {

	private static CharArrayIntMap sAdditionalKeywords;
	static {
		sAdditionalKeywords= new CharArrayIntMap(10, -1);
		GNUScannerExtensionConfiguration.addAdditionalGNUKeywords(sAdditionalKeywords);
		sAdditionalKeywords.put( Keywords.cRESTRICT, IToken.t_restrict );
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
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#getAdditionalKeywords()
     */
    @Override
	public CharArrayIntMap getAdditionalKeywords() {
		return sAdditionalKeywords;
    }
}
