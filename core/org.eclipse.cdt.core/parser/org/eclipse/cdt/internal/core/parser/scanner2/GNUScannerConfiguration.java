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

/**
 * @author jcamelon
 */
public class GNUScannerConfiguration {

    public boolean initializeMacroValuesTo1() {
        return true;
    }

    public boolean support$InIdentifiers() {
        return true;
    }

    public char[] supportAdditionalNumericLiteralSuffixes() {
        return "ij".toCharArray(); //$NON-NLS-1$
    }

}
