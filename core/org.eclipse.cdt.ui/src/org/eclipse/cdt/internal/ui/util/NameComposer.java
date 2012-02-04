/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.text.BreakIterator;

import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.text.CBreakIterator;

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

	private final int capitalization;
	private final String wordDelimiter;
	private final String prefix;
	private final String suffix;

	/**
	 * Creates a name composer for a given style.
	 * 
	 * @param capitalization capitalization transformation applied to name. Possible values: <ul>
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
	public String compose(String seedName) {
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
	public List<CharSequence> splitIntoWords(CharSequence name) {
		List<CharSequence> words = new ArrayList<CharSequence>();
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
			buf.append(i == 0 ?
					Character.toUpperCase(word.charAt(i)) : Character.toLowerCase(word.charAt(i)));
		}
	}
}
