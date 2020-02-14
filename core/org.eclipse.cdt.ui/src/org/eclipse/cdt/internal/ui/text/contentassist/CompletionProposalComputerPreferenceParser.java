/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.ui.PreferenceConstants;

/**
 * Parses the Completion Proposal Computer Preferences.
 * <p>
 * See org.eclipse.cdt.internal.ui.preferences.CodeAssistAdvancedConfigurationBlock.PreferenceModel
 * for the write side of the preferences.
 */
public class CompletionProposalComputerPreferenceParser {
	/**
	 * Parses the {@link PreferenceConstants#CODEASSIST_EXCLUDED_CATEGORIES} value and
	 * converts to a set of categories that are excluded.
	 * @param preferenceValue as stored in {@link PreferenceConstants#CODEASSIST_EXCLUDED_CATEGORIES}
	 * @return set of excluded categories
	 * @throws ParseException if the value cannot be parsed
	 */
	public static Set<String> parseExcludedCategories(String preferenceValue) throws ParseException {
		Set<String> disabled = new HashSet<>();
		String[] disabledArray = splitOnNulls(preferenceValue);
		disabled.addAll(Arrays.asList(disabledArray));
		return disabled;
	}

	/**
	 * Parses the {@link PreferenceConstants#CODEASSIST_CATEGORY_ORDER} value and
	 * converts to a map of category ids to sort rank number
	 * @param preferenceValue as stored in {@link PreferenceConstants#CODEASSIST_CATEGORY_ORDER}
	 * @return map of category id to rank order
	 * @throws ParseException if the value cannot be parsed
	 */
	public static Map<String, Integer> parseCategoryOrder(String preferenceValue) throws ParseException {
		Map<String, Integer> ordered = new HashMap<>();
		String[] orderedArray = splitOnNulls(preferenceValue);
		for (String entry : orderedArray) {
			String[] idRank = entry.split(":"); //$NON-NLS-1$
			if (idRank.length != 2) {
				throw new ParseException(entry, 0);
			}
			String id = idRank[0];
			int rank;
			try {
				rank = Integer.parseInt(idRank[1]);
			} catch (NumberFormatException e) {
				throw new ParseException(entry, 0);
			}
			ordered.put(id, Integer.valueOf(rank));
		}
		return ordered;
	}

	/**
	 * See Bug 558809. Oomph seems to have failed at times to encode/decode nul character '\0'
	 * from the format it is stored in Oomph setup files. We can have ${0x0} instead of \0, infact
	 * there can be multiple $, so $$$$${0x0} is a valid split too.
	 */
	private static String[] splitOnNulls(String preference) {
		return preference.split("\\000|(\\$+\\{0x0\\})"); //$NON-NLS-1$
	}
}
