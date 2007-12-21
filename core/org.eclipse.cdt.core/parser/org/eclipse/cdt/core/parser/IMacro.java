/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core.parser;

/**
 * Interface to provide macro definitions in an IScannerExtensionConfiguration.
 */
public interface IMacro {
	/**
	 * Return the signature of a macro, which is the name for object style macros and 
	 * the name followed by the comma-separated parameters put in parenthesis. For 
	 * example: 'funcStyleMacro(par1, par2)'. 
	 */
    public char[] getSignature();

    /**
     * Returns the expansion for this macro.
     */
    public char[] getExpansion();
}
