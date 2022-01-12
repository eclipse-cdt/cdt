/*******************************************************************************
 * Copyright (c) 2007 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.spelling;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * This class encapsulates spelling preferences.
 * If the source of spelling preferences were to move from CDT to the platform,
 * this class would make refactoring easier.
 */
public class SpellingPreferences {
	private static IPreferenceStore preferenceStore = PreferenceConstants.getPreferenceStore();
	static final String SPELLING_LOCALE = PreferenceConstants.SPELLING_LOCALE;
	static final String SPELLING_USER_DICTIONARY = PreferenceConstants.SPELLING_USER_DICTIONARY;
	static final String SPELLING_USER_DICTIONARY_ENCODING = PreferenceConstants.SPELLING_USER_DICTIONARY_ENCODING;
	private static final String SPELLING_PROPOSAL_THRESHOLD = PreferenceConstants.SPELLING_PROPOSAL_THRESHOLD;
	private static final String SPELLING_ENABLE_CONTENTASSIST = PreferenceConstants.SPELLING_ENABLE_CONTENTASSIST;
	private static final String SPELLING_IGNORE_DIGITS = PreferenceConstants.SPELLING_IGNORE_DIGITS;
	private static final String SPELLING_IGNORE_MIXED = PreferenceConstants.SPELLING_IGNORE_MIXED;
	private static final String SPELLING_IGNORE_NON_LETTERS = PreferenceConstants.SPELLING_IGNORE_NON_LETTERS;
	private static final String SPELLING_IGNORE_SENTENCE = PreferenceConstants.SPELLING_IGNORE_SENTENCE;
	private static final String SPELLING_IGNORE_SINGLE_LETTERS = PreferenceConstants.SPELLING_IGNORE_SINGLE_LETTERS;
	private static final String SPELLING_IGNORE_STRING_LITERALS = PreferenceConstants.SPELLING_IGNORE_STRING_LITERALS;
	private static final String SPELLING_IGNORE_UPPER = PreferenceConstants.SPELLING_IGNORE_UPPER;
	private static final String SPELLING_IGNORE_URLS = PreferenceConstants.SPELLING_IGNORE_URLS;

	/**
	 * @see IPreferenceStore#addPropertyChangeListener(IPropertyChangeListener)
	 */
	public static void addPropertyChangeListener(IPropertyChangeListener listener) {
		preferenceStore.addPropertyChangeListener(listener);
	}

	/**
	 * @see IPreferenceStore#removePropertyChangeListener(IPropertyChangeListener)
	 */
	public static void removePropertyChangeListener(IPropertyChangeListener listener) {
		preferenceStore.removePropertyChangeListener(listener);
	}

	/**
	 * The locale used for spell checking.
	 */
	public static String getSpellingLocale() {
		return preferenceStore.getString(SPELLING_LOCALE);
	}

	/**
	 * The workspace user dictionary.
	 */
	public static String getSpellingUserDictionary() {
		return preferenceStore.getString(SPELLING_USER_DICTIONARY);
	}

	/**
	 * The encoding of the workspace user dictionary.
	 */
	public static String getSpellingUserDictionaryEncoding() {
		return preferenceStore.getString(SPELLING_USER_DICTIONARY_ENCODING);
	}

	/**
	 * Returns the number of proposals offered during spell checking.
	 */
	public static int spellingProposalThreshold() {
		return preferenceStore.getInt(SPELLING_PROPOSAL_THRESHOLD);
	}

	/**
	 * Returns <code>true</code> if spelling content assist is enabled.
	 */
	public static boolean isEnabledSpellingContentAssist() {
		return preferenceStore.getBoolean(SPELLING_ENABLE_CONTENTASSIST);
	}

	/**
	 * Returns <code>true</code> if words containing digits should
	 * be skipped during spell checking.
	 */
	public static boolean isIgnoreDigits() {
		return preferenceStore.getBoolean(SPELLING_IGNORE_DIGITS);
	}

	/**
	 * Returns <code>true</code> if mixed case words should be
	 * skipped during spell checking.
	 */
	public static boolean isIgnoreMixed() {
		return preferenceStore.getBoolean(SPELLING_IGNORE_MIXED);
	}

	/**
	 * Returns <code>true</code> if non-letters at word boundaries
	 * should be ignored during spell checking.
	 */
	public static boolean isIgnoreNonLetters() {
		return preferenceStore.getBoolean(SPELLING_IGNORE_NON_LETTERS);
	}

	/**
	 * Returns <code>true</code> if sentence capitalization should
	 * be ignored during spell checking.
	 */
	public static boolean isIgnoreSentence() {
		return preferenceStore.getBoolean(SPELLING_IGNORE_SENTENCE);
	}

	/**
	 * Returns <code>true</code> if single letters
	 * should be ignored during spell checking.
	 */
	public static boolean isIgnoreSingleLetters() {
		return preferenceStore.getBoolean(SPELLING_IGNORE_SINGLE_LETTERS);
	}

	/**
	 * Returns <code>true</code> if string literals
	 * should be ignored during spell checking.
	 */
	public static boolean isIgnoreStringLiterals() {
		return preferenceStore.getBoolean(SPELLING_IGNORE_STRING_LITERALS);
	}

	/**
	 * Returns <code>true</code> if upper case words should be
	 * skipped during spell checking.
	 */
	public static boolean isIgnoreUpper() {
		return preferenceStore.getBoolean(SPELLING_IGNORE_UPPER);
	}

	/**
	 * Returns <code>true</code> if URLs should be ignored during
	 * spell checking.
	 */
	public static boolean isIgnoreUrls() {
		return preferenceStore.getBoolean(SPELLING_IGNORE_URLS);
	}
}
