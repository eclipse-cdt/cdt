/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * Scanner extension configuration interface.
 * 
 * <p>
 * This interface is not intended to be implemented directly. Clients should
 * subclass {@link AbstractScannerExtensionConfiguration} instead.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @author jcamelon
 */
public interface IScannerExtensionConfiguration {

	/**
	 * @return <code>true</code>, if macros should be initialized to 1
	 */
	public boolean initializeMacroValuesTo1();

	/**
	 * Support for GNU extension "Dollar Signs in Identifier Names".
	 * 
	 * @see http://gcc.gnu.org/onlinedocs/gcc/Dollar-Signs.html
	 * @return <code>true</code>, if $ should be supported in identifiers
	 */
	public boolean support$InIdentifiers();

	/**
	 * Support for (deprecated) GNU minimum and maximum operators (<code>&lt;?</code>
	 * and <code>&gt;?</code>).
	 * 
	 * @see http://gcc.gnu.org/onlinedocs/gcc/Deprecated-Features.html
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportMinAndMaxOperators();

	/**
	 * Support for additional numeric literal suffix characters, like e.g. 'i'
	 * and 'j' for GNU Complex number literals.
	 * 
	 * @see http://gcc.gnu.org/onlinedocs/gcc/Complex.html
	 * @return an array of chars or <code>null</code>, if no additional
	 *         suffixes should be allowed
	 */
	public char[] supportAdditionalNumericLiteralSuffixes();

	/**
	 * Support for additional keywords.
	 * 
	 * @return a mapping of keyword name to one of the constants defined in
	 *         {@link org.eclipse.cdt.core.parser.IToken IToken} or
	 *         <code>null</code> for no additional keywords.
	 */
	public CharArrayIntMap getAdditionalKeywords();

	/**
	 * Support for additional macros.
	 * 
	 * @return a mapping of macro name to
	 *         {@link org.eclipse.cdt.core.parser.IMacro IMacro} or
	 *         <code>null</code> for no additional macros.
	 */
	public CharArrayObjectMap getAdditionalMacros();

	/**
	 * Support for additional preprocessor directives.
	 * 
	 * @return a mapping of preprocessor directive keyword to one of the
	 *         constants defined in
	 *         {@link org.eclipse.cdt.core.parser.IPreprocessorDirective IPreprocessorDirective}
	 *         or <code>null</code> for no additional keywords.
	 */
	public CharArrayIntMap getAdditionalPreprocessorKeywords();
}
