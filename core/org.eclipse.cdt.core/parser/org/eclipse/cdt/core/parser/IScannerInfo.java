/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

/**
 * Interface for providing a configuration for the preprocessor. 
 * @see IExtendedScannerInfo
 */
public interface IScannerInfo {
	/**
	 * Returns a <code>Map</code> containing all the defined preprocessor 
	 * symbols and their values.
	 * Symbols defined without values have an empty string for a value. For 
	 * example,-Dsymbol=value would have a map entry (symbol,value). A symbol
	 * defined as -Dsymbol= would have a map entry of (symbol,"").
	 */
	public Map<String, String> getDefinedSymbols();

	/**
	 * Returns an array of paths that are searched when processing an include directive.
	 * see {@link IExtendedScannerInfo#getLocalIncludePath()}
	 */
	public String[] getIncludePaths();
}
