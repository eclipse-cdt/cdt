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
	
	public static final XlcKeywords C = new XlcKeywords(ParserLanguage.C);
	static {
		C.map.put("vector".toCharArray(),   XlcCParsersym.TK_vector);
		C.map.put("__vector".toCharArray(), XlcCParsersym.TK_vector);
		C.map.put("pixel".toCharArray(),    XlcCParsersym.TK_pixel);
		C.map.put("__pixel".toCharArray(),  XlcCParsersym.TK_pixel);
		C.map.put("bool".toCharArray(),     XlcCParsersym.TK_bool);
	}
	
	public static final XlcKeywords CPP = new XlcKeywords(ParserLanguage.CPP);
	static {
		CPP.map.put("vector".toCharArray(),   XlcCPPParsersym.TK_vector);
		CPP.map.put("__vector".toCharArray(), XlcCPPParsersym.TK_vector);
		CPP.map.put("pixel".toCharArray(),    XlcCPPParsersym.TK_pixel);
		CPP.map.put("__pixel".toCharArray(),  XlcCPPParsersym.TK_pixel);
		// bool is already a C++ keyword
	}
	
	
	private final CharArrayMap<Integer> map = new CharArrayMap<Integer>();
	private final ParserLanguage language;
	private String[] allKeywords = null;
	
	private XlcKeywords(ParserLanguage language) {
		super(language, XlcScannerExtensionConfiguration.getInstance());
		this.language = language;
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
