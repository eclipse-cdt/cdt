/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

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
	protected static class MacroDefinition implements IMacro {
		private char[] fSignature;
		private char[] fExpansion;
		
		MacroDefinition(char[] signature, char[] expansion) {
			fSignature= signature;
			fExpansion= expansion;
		}
		
		public char[] getSignature() {
			return fSignature;
		}
		public char[] getExpansion() {
			return fExpansion;
		}
	}

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


	public IMacro[] getAdditionalMacros() {
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
	 * @param signature the signature of the macro, see {@link IMacro#getSignature()}.
	 * @param value the macro value
	 */
	protected static IMacro createMacro(String signature, String value) {
		return new MacroDefinition(signature.toCharArray(), value.toCharArray());
	}

	/**
	 * Helper method to add a function style macro to the given map.
	 * 
	 * @param macros the macro map
	 * @param name the macro name
	 * @param value the macro value
	 * @param arguments the macro arguments
	 */
	protected static IMacro createFunctionStyleMacro(String name, String value, String[] arguments) {
		StringBuffer buf= new StringBuffer();
		buf.append(name);
		buf.append('(');
		for (int i = 0; i < arguments.length; i++) {
			if (i>0) {
				buf.append(',');
			}
			buf.append(arguments[i]);
		}
		buf.append(')');
		char[] signature= new char[buf.length()];
		buf.getChars(0, signature.length, signature, 0);
		return new MacroDefinition(signature, value.toCharArray());
	}
}
