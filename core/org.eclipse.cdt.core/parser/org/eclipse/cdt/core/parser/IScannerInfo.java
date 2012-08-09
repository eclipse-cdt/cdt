/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

/**
 * Interface for providing a configuration for the preprocessor. 
 * @see IExtendedScannerInfo
 */
public interface IScannerInfo {
	/**
	 * Returns a {@link Map} containing all the defined preprocessor symbols and their values.
	 * Symbols defined without values have an empty string for a value. For 
	 * example, -Dsymbol=value would have a map entry (symbol, value). A symbol
	 * defined as -Dsymbol= would have a map entry of (symbol, "").
	 */
	public Map<String, String> getDefinedSymbols();

	/**
	 * Returns an array of paths that are searched when processing an include directive.
	 * see {@link IExtendedScannerInfo#getLocalIncludePath()}
	 * <p>
	 * In order to suppress the use of the directory of the current file (side effect of gcc option
	 * -I-) you can pass '-' as one of the include paths. Other than that, the '-' will not have an
	 * effect, in particular it will not split the include path as the -I- option would do. To achieve
	 * that, use {@link IExtendedScannerInfo#getLocalIncludePath()}.
	 * <p>
	 * In order to handle framework includes used on Apple Computers you can make use of
	 * the two variables: '__framework__' and '__header__'. 
	 * <br> E.g.:  /System/Library/Frameworks/__framework__.framework/Headers/__header__,
	 * /System/Library/Frameworks/__framework__.framework/PrivateHeaders/__header__
	 * would handle the framework search for '/System/Library/Frameworks'
	 * <br> The variables are handled only, if a search path element makes use of both of
	 * the variables. The __framework__ variable will receive the first segment of the include,
	 * the __header__ variable the rest. Such a search path element is not used for directives
	 * with a single segment (e.g. 'header.h')
	 */
	public String[] getIncludePaths();
}
