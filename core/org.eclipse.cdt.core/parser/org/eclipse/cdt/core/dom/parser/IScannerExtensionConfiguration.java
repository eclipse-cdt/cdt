/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * Scanner extension configuration interface.
 *
 * @noimplement This interface is not intended to be implemented by clients. Clients can subclass
 * {@link AbstractScannerExtensionConfiguration}, instead.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IScannerExtensionConfiguration {

	/**
	 * @return <code>true</code>, if macros should be initialized to 1
	 * @deprecated empty macros are taken as they are. It is the task of configuration to provide the correct values.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=215789
	 */
	@Deprecated
	public boolean initializeMacroValuesTo1();

	/**
	 * Support for GNU extension "Dollar Signs in Identifier Names".
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Dollar-Signs.html"
	 * @return <code>true</code>, if $ should be supported in identifiers
	 */
	public boolean support$InIdentifiers();

	/**
	 * Support for extension "At Signs in Identifier Names". If enabled, the '@' sign is treated as part of
	 * identifiers.
	 * @return <code>true</code>, if @ should be supported in identifiers
	 * @since 5.1
	 */
	public boolean supportAtSignInIdentifiers();

	/** 
	 * Support for block-comments comments using /% %/.
	 * @return <code>true</code>, if /% should be interpreted as the start of a block-comment which is
	 * ended by %/
	 * @since 5.1
	 */
	public boolean supportSlashPercentComments();

	/**
	 * Support for (deprecated) GNU minimum and maximum operators (<code>&lt;?</code>
	 * and <code>&gt;?</code>).
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Deprecated-Features.html"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportMinAndMaxOperators();

	/**
	 * Support for additional numeric literal suffix characters, like e.g. 'i'
	 * and 'j' for GNU Complex number literals.
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Complex.html"
	 * @return an array of chars or <code>null</code>, if no additional
	 *         suffixes should be allowed
	 */
	public char[] supportAdditionalNumericLiteralSuffixes();

	/**
	 * Support for additional keywords.
	 * 
	 * @return a mapping of keyword name to one of the constants defined in
	 *         {@link IToken} or <code>null</code> for no additional keywords.
	 */
	public CharArrayIntMap getAdditionalKeywords();

	/**
	 * Support for additional macros.
	 * @return an array of macros or <code>null</code> for no additional macros.
	 */
	public IMacro[] getAdditionalMacros();

	/**
	 * Support for additional preprocessor directives.
	 * 
	 * @return a mapping of preprocessor directive keyword to one of the
	 *         constants defined in
	 *         {@link org.eclipse.cdt.core.parser.IPreprocessorDirective IPreprocessorDirective}
	 *         or <code>null</code> for no additional keywords.
	 */
	public CharArrayIntMap getAdditionalPreprocessorKeywords();
	
	
	/**
     * Support for UTF string literals.
     *
	 * @since 5.1
     * @see "http://publib.boulder.ibm.com/infocenter/comphelp/v101v121/index.jsp?topic=/com.ibm.xlcpp101.aix.doc/language_ref/unicode_standard.html"
	 */
	public boolean supportUTFLiterals();
	
	/**
     * Support for C++ raw string literals.
	 * @since 5.5
	 */
	public boolean supportRawStringLiterals();
		
	/**
	 * Support for User Defined Literals such as 123_suffix
	 * @since 5.11
	 */
	public boolean supportUserDefinedLiterals();
}
