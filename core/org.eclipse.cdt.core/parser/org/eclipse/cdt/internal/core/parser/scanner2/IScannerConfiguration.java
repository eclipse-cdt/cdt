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
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * @author jcamelon
 */
public interface IScannerConfiguration {

    public boolean initializeMacroValuesTo1();
    public boolean support$InIdentifiers();
    public boolean supportMinAndMaxOperators();
    
    public CharArrayIntMap getAdditionalKeywords();

    public char [] supportAdditionalNumericLiteralSuffixes();
    public CharArrayObjectMap getAdditionalMacros();
}
