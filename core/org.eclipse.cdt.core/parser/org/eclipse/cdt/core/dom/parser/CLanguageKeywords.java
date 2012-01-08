/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;

/**
 * This class allows provides a reusable implementation of ICLanguageKeywords
 * for use by ILanguage implementations.
 * 
 * @author Mike Kucera
 * @since 5.1
 */
public class CLanguageKeywords implements ICLanguageKeywords {
	private final ParserLanguage language;
	private final IScannerExtensionConfiguration config;
	
	// lazily initialized
	private String[] keywords = null;
	private String[] builtinTypes = null;
	private String[] preprocessorKeywords = null;

	/**
	 * @throws NullPointerException if either parameter is null
	 */
	public CLanguageKeywords(ParserLanguage language, IScannerExtensionConfiguration config) {
		if (language == null)
			throw new NullPointerException("language is null"); //$NON-NLS-1$
		if (config == null)
			throw new NullPointerException("config is null"); //$NON-NLS-1$
		
		this.language = language;
		this.config = config;
	}
	
	@Override
	public String[] getKeywords() {
		if (keywords == null) {
			Set<String> keywordSet = new HashSet<String>(KeywordSets.getKeywords(KeywordSetKey.KEYWORDS, language));
			CharArrayIntMap additionalKeywords = config.getAdditionalKeywords();
			if (additionalKeywords != null) {
				for (Iterator<char[]> iterator = additionalKeywords.toList().iterator(); iterator.hasNext(); ) {
					char[] name = iterator.next();
					keywordSet.add(new String(name));
				}
			}
			keywords = keywordSet.toArray(new String[keywordSet.size()]);
		}
		return keywords;
	}
	
	@Override
	public String[] getBuiltinTypes() {
		if (builtinTypes == null) {
			Set<String> types = KeywordSets.getKeywords(KeywordSetKey.TYPES, language);
			builtinTypes = types.toArray(new String[types.size()]);
		}
		return builtinTypes;
	}

	@Override
	public String[] getPreprocessorKeywords() {
		if (preprocessorKeywords == null) {
			Set<String> keywords = new HashSet<String>(KeywordSets.getKeywords(KeywordSetKey.PP_DIRECTIVE, language));
			CharArrayIntMap additionalKeywords= config.getAdditionalPreprocessorKeywords();
			if (additionalKeywords != null) {
				for (Iterator<char[]> iterator = additionalKeywords.toList().iterator(); iterator.hasNext(); ) {
					char[] name = iterator.next();
					keywords.add(new String(name));
				}
			}
			preprocessorKeywords = keywords.toArray(new String[keywords.size()]);
		}
		return preprocessorKeywords;
	}
}
