/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.parser.CLanguageKeywords;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.internal.core.lrparser.xlc.c.XlcCParsersym;
import org.eclipse.cdt.internal.core.lrparser.xlc.cpp.XlcCPPParsersym;

public class XlcKeywords extends CLanguageKeywords {

	public static final XlcKeywords ALL_C_KEYWORDS = createC(true, true);
	public static final XlcKeywords ALL_CPP_KEYWORDS = createCPP(true, true, true, true, true);

	private final CharArrayMap<Integer> map = new CharArrayMap<>();
	private final ParserLanguage language;
	private String[] allKeywords = null;

	public static XlcKeywords createC(boolean supportVectors, boolean supportDecimalFloatingPoint) {
		XlcKeywords keywords = new XlcKeywords(ParserLanguage.C);
		CharArrayMap<Integer> map = keywords.map;
		if (supportVectors) {
			map.put("vector".toCharArray(), XlcCParsersym.TK_vector);
			map.put("__vector".toCharArray(), XlcCParsersym.TK_vector);
			map.put("pixel".toCharArray(), XlcCParsersym.TK_pixel);
			map.put("__pixel".toCharArray(), XlcCParsersym.TK_pixel);
			map.put("bool".toCharArray(), XlcCParsersym.TK_bool);
		}
		if (supportDecimalFloatingPoint) {
			map.put("_Decimal32".toCharArray(), XlcCParsersym.TK__Decimal32);
			map.put("_Decimal64".toCharArray(), XlcCParsersym.TK__Decimal64);
			map.put("_Decimal128".toCharArray(), XlcCParsersym.TK__Decimal128);
		}
		return keywords;
	}

	public static XlcKeywords createCPP(boolean supportVectors, boolean supportDecimalFloatingPoint,
			boolean supportComplex, boolean supportRestrict, boolean supportStaticAssert) {
		XlcKeywords keywords = new XlcKeywords(ParserLanguage.CPP);
		CharArrayMap<Integer> map = keywords.map;
		if (supportVectors) {
			map.put("vector".toCharArray(), XlcCPPParsersym.TK_vector);
			map.put("__vector".toCharArray(), XlcCPPParsersym.TK_vector);
			map.put("pixel".toCharArray(), XlcCPPParsersym.TK_pixel);
			map.put("__pixel".toCharArray(), XlcCPPParsersym.TK_pixel);
		}
		if (supportDecimalFloatingPoint) {
			map.put("_Decimal32".toCharArray(), XlcCPPParsersym.TK__Decimal32);
			map.put("_Decimal64".toCharArray(), XlcCPPParsersym.TK__Decimal64);
			map.put("_Decimal128".toCharArray(), XlcCPPParsersym.TK__Decimal128);
		}
		if (supportComplex) {
			map.put("_Complex".toCharArray(), XlcCPPParsersym.TK__Complex);
		}
		if (supportRestrict) {
			map.put("restrict".toCharArray(), XlcCPPParsersym.TK_restrict);
			map.put("__restrict".toCharArray(), XlcCPPParsersym.TK_restrict);
			map.put("__restrict__".toCharArray(), XlcCPPParsersym.TK_restrict);
		}

		if (supportStaticAssert) {
			map.put("__static_assert".toCharArray(), XlcCPPParsersym.TK___static_assert);
		}

		return keywords;
	}

	private XlcKeywords(ParserLanguage language) {
		super(language, getConfig(language));
		this.language = language;
	}

	private static IScannerExtensionConfiguration getConfig(ParserLanguage lang) {
		return lang.isCPP() ? XlcCPPScannerExtensionConfiguration.getInstance()
				: XlcCScannerExtensionConfiguration.getInstance();
	}

	/**
	 * Returns the LPG token kind for additional keywords defined by
	 * the XLC extensions, null otherwise.
	 */
	public Integer getTokenKind(char[] keyword) {
		return map.get(keyword);
	}

	@Override
	public synchronized String[] getKeywords() {
		if (allKeywords == null) {
			ICLanguageKeywords base = new CLanguageKeywords(language, getConfig(language));
			String[] baseKeywords = base.getKeywords();

			List<String> keywords = new ArrayList<>();
			keywords.addAll(Arrays.asList(baseKeywords));

			for (char[] keyword : map.keys()) {
				keywords.add(String.valueOf(keyword));
			}

			allKeywords = keywords.toArray(new String[keywords.size()]);
		}

		return allKeywords;
	}

}
