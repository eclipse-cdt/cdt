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
        // TODO Auto-generated method stub
        return null;
    }

}
