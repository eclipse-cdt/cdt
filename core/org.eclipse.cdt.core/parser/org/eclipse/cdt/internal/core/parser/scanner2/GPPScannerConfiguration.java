/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * @author jcamelon
 */
public class GPPScannerConfiguration extends GNUScannerConfiguration implements
        IScannerConfiguration {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#supportMinAndMaxOperators()
     */
    public boolean supportMinAndMaxOperators() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#getAdditionalKeywords()
     */
    public CharArrayIntMap getAdditionalKeywords() {
        CharArrayIntMap additionalCPPKeywords = new CharArrayIntMap( 8, -1 );
		additionalCPPKeywords.put( GCCKeywords.cp__ALIGNOF__, IGCCToken.t___alignof__ );
		additionalCPPKeywords.put( GCCKeywords.cpTYPEOF, IGCCToken.t_typeof );		
		additionalCPPKeywords.put( Keywords.cRESTRICT, IToken.t_restrict );
		additionalCPPKeywords.put( Keywords.c_COMPLEX, IToken.t__Complex );
		additionalCPPKeywords.put( Keywords.c_IMAGINARY, IToken.t__Imaginary );
		return additionalCPPKeywords;
    }

}
