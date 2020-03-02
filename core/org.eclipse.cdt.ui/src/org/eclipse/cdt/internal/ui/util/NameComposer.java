/*******************************************************************************
 * Copyright (c) 2011, 2015 Google, Inc and others.
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
package org.eclipse.cdt.internal.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.internal.ui.text.CBreakIterator;
import org.eclipse.cdt.ui.PreferenceConstants;

import com.ibm.icu.text.BreakIterator;

/**
 * Composes names according to a particular style. A seed name is split into
 * words at non-alphanumeric characters and camel case boundaries. The resulting
 * words are capitalized according to the given capitalization style, joined
 * using the given delimiter and combined with the given prefix and suffix.
 */
public class NameComposer {
	private static final int CAPITALIZATION_ORIGINAL = PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL;
	private static final int CAPITALIZATION_UPPER_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_UPPER_CASE;
	private static final int CAPITALIZATION_LOWER_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CASE;
	private static final int CAPITALIZATION_CAMEL_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE;
	private static final int CAPITALIZATION_LOWER_CAMEL_CASE = PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE;
	private static final int[] ALL_CAPITALIZATIONS = { CAPITALIZATION_ORIGINAL, CAPITALIZATION_UPPER_CASE,
			CAPITALIZATION_LOWER_CASE, CAPITALIZATION_CAMEL_CASE, CAPITALIZATION_LOWER_CAMEL_CASE, };

	private int capitalization;
	private final String wordDelimiter;
	private final String prefix;
	private String suffix;

	/**
	 * Creates a name composer for a given style.
	 *
	 * @param capitalization capitalization transformation applied to a name. Possible values: <ul>
	 * <li>PreferenceConstants.NAME_STYLE_CAPITALIZATION_ORIGINAL,</li>
	 * <li>PreferenceConstants.NAME_STYLE_CAPITALIZATION_UPPER_CASE,</li>
	 * <li>PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CASE,</li>
	 * <li>PreferenceConstants.NAME_STYLE_CAPITALIZATION_CAMEL_CASE,</li>
	 * <li>PreferenceConstants.NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE.</li>
	 * </ul>
	 * @param wordDelimiter delimiter inserted between words
	 * @param prefix prefix prepended to the name
	 * @param suffix suffix appended to the name
	 */
	public NameComposer(int capitalization, String wordDelimiter, String prefix, String suffix) {
		this.capitalization = capitalization;
		this.wordDelimiter = wordDelimiter;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	/**
	 * Composes a name according to the composer's style based on a seed name.
	 *
	 * @param seedName the name used as an inspiration
	 * @return the composed name
	 */
	public String compose(CharSequence seedName) {
		List<CharSequence> words = splitIntoWords(seedName);
		return compose(words);
	}

	/**
	 * Composes a name according to the composer's style based on a seed words.
	 *
	 * @param words the words that that should be combined to form the name
	 * @return the composed name
	 */
	public String compose(List<CharSequence> words) {
		StringBuilder buf = new StringBuilder();
		buf.append(prefix);
		for (int i = 0; i < words.size(); i++) {
			if (i > 0) {
				buf.append(wordDelimiter);
			}
			CharSequence word = words.get(i);
			switch (capitalization) {
			case CAPITALIZATION_ORIGINAL:
				buf.append(word);
				break;
			case CAPITALIZATION_UPPER_CASE:
				appendUpperCase(buf, word);
				break;
			case CAPITALIZATION_LOWER_CASE:
				appendLowerCase(buf, word);
				break;
			case CAPITALIZATION_CAMEL_CASE:
				appendTitleCase(buf, word);
				break;
			case CAPITALIZATION_LOWER_CAMEL_CASE:
				if (i == 0) {
					appendLowerCase(buf, word);
				} else {
					appendTitleCase(buf, word);
				}
				break;
			}
		}
		buf.append(suffix);
		return buf.toString();
	}

	/**
	 * Splits a name into words at non-alphanumeric characters and camel case boundaries.
	 */
	public static List<CharSequence> splitIntoWords(CharSequence name) {
		List<CharSequence> words = new ArrayList<>();
		CBreakIterator iterator = new CBreakIterator();
		iterator.setText(name);
		int end;
		for (int start = iterator.first(); (end = iterator.next()) != BreakIterator.DONE; start = end) {
			if (Character.isLetterOrDigit(name.charAt(start))) {
				int pos = end;
				while (--pos >= start && !Character.isLetterOrDigit(name.charAt(pos))) {
				}
				words.add(name.subSequence(start, pos + 1));
			}
		}
		return words;
	}

	private void appendUpperCase(StringBuilder buf, CharSequence word) {
		for (int i = 0; i < word.length(); i++) {
			buf.append(Character.toUpperCase(word.charAt(i)));
		}
	}

	private void appendLowerCase(StringBuilder buf, CharSequence word) {
		for (int i = 0; i < word.length(); i++) {
			buf.append(Character.toLowerCase(word.charAt(i)));
		}
	}

	private void appendTitleCase(StringBuilder buf, CharSequence word) {
		for (int i = 0; i < word.length(); i++) {
			buf.append(i == 0 ? Character.toUpperCase(word.charAt(i)) : Character.toLowerCase(word.charAt(i)));
		}
	}

	/**
	 * Creates a NameComposer such that it would compose {@code composedName} given {@code seedName}
	 * as a seed.
	 *
	 * @param seedName the seed name
	 * @param composedName the composed name
	 * @param defaultCapitalization used to disambiguate capitalization if it cannot be uniquely
	 *     determined from the composed name
	 * @param defaultWordDelimiter used to disambiguate word delimiter if it cannot be uniquely
	 *     determined from the composed name
	 * @return a name composer based on the composed name, or {@code null} if such name composer
	 *     does not exist
	 */
	public static NameComposer createByExample(String seedName, String composedName, int defaultCapitalization,
			String defaultWordDelimiter) {
		List<CharSequence> seedWords = splitIntoWords(seedName);
		if (seedWords.isEmpty())
			return null;
		List<CharSequence> composedWords = splitIntoWords(composedName);
		String delimiter = defaultWordDelimiter;
		int numPrefixWords = indexOfSublistIgnoreCase(composedWords, seedWords);
		if (numPrefixWords < 0) {
			delimiter = ""; //$NON-NLS-1$
			seedWords = Collections.<CharSequence>singletonList(seedName);
			numPrefixWords = indexOfSublistIgnoreCase(composedWords, seedWords);
			if (numPrefixWords < 0)
				return null;
		}
		String prefix = deducePrefix(composedName, numPrefixWords);
		if (seedWords.size() > 1) {
			delimiter = ""; //$NON-NLS-1$
			int start = prefix.length() + composedWords.get(numPrefixWords).length();
			for (int i = start; i < composedName.length(); i++) {
				if (Character.isLetterOrDigit(composedName.charAt(i))) {
					delimiter = composedName.substring(start, i);
					break;
				}
			}
		}
		NameComposer composer = new NameComposer(defaultCapitalization, delimiter, prefix, ""); //$NON-NLS-1$
		for (int i = -1; i < ALL_CAPITALIZATIONS.length; i++) {
			if (i >= 0)
				composer.capitalization = ALL_CAPITALIZATIONS[i];
			String name = composer.compose(seedWords);
			if (composedName.startsWith(name)) {
				composer.suffix = composedName.substring(name.length());
				return composer;
			}
		}
		return null;
	}

	private static int indexOfSublistIgnoreCase(List<CharSequence> list, List<CharSequence> subList) {
		int subListSize = subList.size();
		int limit = list.size() - subListSize;

		outer: for (int k = 0; k <= limit; k++) {
			for (int i = 0, j = k; i < subListSize; i++, j++) {
				if (!subList.get(i).toString().equalsIgnoreCase(list.get(j).toString()))
					continue outer;
			}
			return k;
		}
		return -1;
	}

	private static String deducePrefix(CharSequence name, int numPrefixWords) {
		CBreakIterator iterator = new CBreakIterator();
		iterator.setText(name);
		int end;
		int wordCount = 0;
		for (int start = iterator.first(); (end = iterator.next()) != BreakIterator.DONE; start = end) {
			if (Character.isLetterOrDigit(name.charAt(start))) {
				if (wordCount == numPrefixWords)
					return name.subSequence(0, start).toString();
				wordCount++;
			}
		}
		if (wordCount == numPrefixWords)
			return name.toString();
		throw new IllegalArgumentException(numPrefixWords + " is larger than the number of words in \"" + name + "\""); //$NON-NLS-1$//$NON-NLS-2$
	}
}
