/*******************************************************************************
 * Copyright (c) 2007, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Richard Eames
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import java.util.ArrayList;

import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * Abstract scanner extension configuration to help model C/C++ dialects.
 * @since 4.0
 */
public abstract class AbstractScannerExtensionConfiguration implements IScannerExtensionConfiguration {
	private static final IMacro[] EMPTY_MACRO_ARRAY = new IMacro[0];
	private ArrayList<IMacro> fAddMacroList;
	private IMacro[] fAddMacros;
	private CharArrayIntMap fAddKeywords;
	private CharArrayIntMap fAddPreprocessorKeywords;

	protected static class MacroDefinition implements IMacro {
		private final char[] fSignature;
		private final char[] fExpansion;

		MacroDefinition(char[] signature, char[] expansion) {
			fSignature = signature;
			fExpansion = expansion;
		}

		@Override
		public char[] getSignature() {
			return fSignature;
		}

		@Override
		public char[] getExpansion() {
			return fExpansion;
		}
	}

	public AbstractScannerExtensionConfiguration() {
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#support$InIdentifiers()
	 */
	@Override
	public boolean support$InIdentifiers() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @since 5.1
	 */
	@Override
	public boolean supportAtSignInIdentifiers() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @since 5.1
	 */
	@Override
	public boolean supportUTFLiterals() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * @since 5.1
	 */
	@Override
	public boolean supportSlashPercentComments() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#supportAdditionalNumericLiteralSuffixes()
	 */
	@Override
	public char[] supportAdditionalNumericLiteralSuffixes() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration#supportMinAndMaxOperators()
	 */
	@Override
	public boolean supportMinAndMaxOperators() {
		return false;
	}

	/**
	 * @since 5.5
	 */
	@Override
	public boolean supportRawStringLiterals() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @since 5.11
	 */
	@Override
	public boolean supportUserDefinedLiterals() {
		return false;
	}

	@Override
	public boolean supportDigitSeparators() {
		return false;
	}

	@Override
	public CharArrayIntMap getAdditionalPreprocessorKeywords() {
		return fAddPreprocessorKeywords;
	}

	@Override
	public CharArrayIntMap getAdditionalKeywords() {
		return fAddKeywords;
	}

	@Override
	public IMacro[] getAdditionalMacros() {
		if (fAddMacros == null) {
			if (fAddMacroList == null) {
				fAddMacros = EMPTY_MACRO_ARRAY;
			} else {
				fAddMacros = fAddMacroList.toArray(new IMacro[fAddMacroList.size()]);
			}
		}
		return fAddMacros;
	}

	/**
	 * Adds a macro to the list of additional macros.
	 * The macro can either be of object- or of function-style.
	 * <pre>
	 * Example:
	 *    addMacro("max(a,b)", "(((a)>(b) ? (a) : (b))");
	 * </pre>
	 * @param signature the signature of the macro, see {@link IMacro#getSignature()}.
	 * @param value the macro value
	 * @since 5.1
	 */
	protected void addMacro(String signature, String value) {
		if (fAddMacroList == null) {
			fAddMacroList = new ArrayList<>();
		}
		fAddMacroList.add(new MacroDefinition(signature.toCharArray(), value.toCharArray()));
		fAddMacros = null;
	}

	/**
	 * Adds a preprocessor keyword to the map of additional preprocessor keywords.
	 * @param name the name of the keyword
	 * @param tokenKind the kind of token the keyword is mapped to. See {@link IToken}.
	 * @since 5.1
	 */
	protected void addPreprocessorKeyword(char[] name, int tokenKind) {
		if (fAddPreprocessorKeywords == null) {
			fAddPreprocessorKeywords = new CharArrayIntMap(10, -1);
		}
		fAddPreprocessorKeywords.put(name, tokenKind);
	}

	/**
	 * Adds a  keyword to the map of additional keywords.
	 * @param name the name of the keyword
	 * @param tokenKind the kind of token the keyword is mapped to. See {@link IToken}.
	 * @since 5.1
	 */
	protected void addKeyword(char[] name, int tokenKind) {
		if (fAddKeywords == null) {
			fAddKeywords = new CharArrayIntMap(10, -1);
		}
		fAddKeywords.put(name, tokenKind);
	}

	/**
	 * @deprecated use {@link #addMacro(String, String)}
	 */
	@Deprecated
	protected static IMacro createMacro(String signature, String value) {
		return new MacroDefinition(signature.toCharArray(), value.toCharArray());
	}

	/**
	 * @deprecated use {@link #addMacro(String, String)}
	 */
	@Deprecated
	protected static IMacro createFunctionStyleMacro(String name, String value, String[] arguments) {
		StringBuilder buf = new StringBuilder();
		buf.append(name);
		buf.append('(');
		for (int i = 0; i < arguments.length; i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append(arguments[i]);
		}
		buf.append(')');
		char[] signature = new char[buf.length()];
		buf.getChars(0, signature.length, signature, 0);
		return new MacroDefinition(signature, value.toCharArray());
	}

	/**
	 * @deprecated see {@link IScannerExtensionConfiguration#initializeMacroValuesTo1()}
	 */
	@Override
	@Deprecated
	public boolean initializeMacroValuesTo1() {
		return false;
	}

}
