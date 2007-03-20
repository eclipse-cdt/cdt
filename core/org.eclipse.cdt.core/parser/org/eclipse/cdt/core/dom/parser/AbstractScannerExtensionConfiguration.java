/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;

/**
 * Abstract scanner extension configuration to help model C/C++ dialects.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public abstract class AbstractScannerExtensionConfiguration implements IScannerExtensionConfiguration {

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#initializeMacroValuesTo1()
	 */
	public boolean initializeMacroValuesTo1() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#support$InIdentifiers()
	 */
	public boolean support$InIdentifiers() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#supportAdditionalNumericLiteralSuffixes()
	 */
	public char[] supportAdditionalNumericLiteralSuffixes() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#supportMinAndMaxOperators()
	 */
	public boolean supportMinAndMaxOperators() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#getAdditionalKeywords()
	 */
	public CharArrayIntMap getAdditionalKeywords() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#getAdditionalMacros()
	 */
	public CharArrayObjectMap getAdditionalMacros() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#getAdditionalPreprocessorKeywords()
	 */
	public CharArrayIntMap getAdditionalPreprocessorKeywords() {
		return null;
	}

	/**
	 * Helper method to add an object style macro to the given map.
	 * 
	 * @param macros the macro map
	 * @param name the macro name
	 * @param value the macro value
	 */
	protected void addObjectStyleMacro(CharArrayObjectMap macros, String name, String value) {
		char[] nameChars= name.toCharArray();
		macros.put(nameChars, new ObjectStyleMacro(nameChars, value.toCharArray()));
	}

	/**
	 * Helper method to add a function style macro to the given map.
	 * 
	 * @param macros the macro map
	 * @param name the macro name
	 * @param value the macro value
	 * @param arguments the macro arguments
	 */
	protected void addFunctionStyleMacro(CharArrayObjectMap macros, String name, String value, String[] arguments) {
		char[] nameChars= name.toCharArray();
		char[][] argumentsArray= new char[arguments.length][];
		for (int i = 0; i < arguments.length; i++) {
			argumentsArray[i]= arguments[i].toCharArray();
		}
		macros.put(nameChars, new FunctionStyleMacro(nameChars, value.toCharArray(), argumentsArray));
	}

}
