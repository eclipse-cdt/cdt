/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.parser.CLanguageKeywords;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.internal.core.lrparser.xlc.c.XlcCParsersym;
import org.eclipse.cdt.internal.core.lrparser.xlc.cpp.XlcCPPParsersym;

public class XlcKeywords extends CLanguageKeywords {
	
	public static final XlcKeywords ALL_C_KEYWORDS = new XlcKeywords(ParserLanguage.C, true, true);
	public static final XlcKeywords ALL_CPP_KEYWORDS = new XlcKeywords(ParserLanguage.CPP, true, true);
	
	
	private final CharArrayMap<Integer> map = new CharArrayMap<Integer>();
	private final ParserLanguage language;
	private String[] allKeywords = null;
	
	public XlcKeywords(ParserLanguage language, boolean supportVectors, boolean supportDeclimalFloatingPoint) {
		super(language, XlcScannerExtensionConfiguration.getInstance());
		this.language = language;
		
		if(language.isCPP()) {
			if(supportVectors) {
				map.put("vector".toCharArray(),   XlcCPPParsersym.TK_vector);
				map.put("__vector".toCharArray(), XlcCPPParsersym.TK_vector);
				map.put("pixel".toCharArray(),    XlcCPPParsersym.TK_pixel);
				map.put("__pixel".toCharArray(),  XlcCPPParsersym.TK_pixel);
			}
			if(supportDeclimalFloatingPoint) {
				map.put("_Decimal32".toCharArray(),  XlcCPPParsersym.TK__Decimal32);
				map.put("_Decimal64".toCharArray(),  XlcCPPParsersym.TK__Decimal64);
				map.put("_Decimal128".toCharArray(), XlcCPPParsersym.TK__Decimal128);
			}
		}
		else {
			if(supportVectors) {
				map.put("vector".toCharArray(),   XlcCParsersym.TK_vector);
				map.put("__vector".toCharArray(), XlcCParsersym.TK_vector);
				map.put("pixel".toCharArray(),    XlcCParsersym.TK_pixel);
				map.put("__pixel".toCharArray(),  XlcCParsersym.TK_pixel);
				map.put("bool".toCharArray(),     XlcCParsersym.TK_bool);
			}
			if(supportDeclimalFloatingPoint) {
				map.put("_Decimal32".toCharArray(),  XlcCParsersym.TK__Decimal32);
				map.put("_Decimal64".toCharArray(),  XlcCParsersym.TK__Decimal64);
				map.put("_Decimal128".toCharArray(), XlcCParsersym.TK__Decimal128);
			}
		}
		
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
		if(allKeywords == null) {
			ICLanguageKeywords base = new CLanguageKeywords(language, XlcScannerExtensionConfiguration.getInstance());
			String[] baseKeywords = base.getKeywords();
			
			List<String> keywords = new ArrayList<String>();
			keywords.addAll(Arrays.asList(baseKeywords));
			
			for(char[] keyword : map.keys()) {
				keywords.add(String.valueOf(keyword));
			}
			
			allKeywords = keywords.toArray(new String[keywords.size()]);
		}

		return allKeywords;
	}

	
}
