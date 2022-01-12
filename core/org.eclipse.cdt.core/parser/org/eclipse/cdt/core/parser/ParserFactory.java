/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Set;

import org.eclipse.cdt.internal.core.parser.token.KeywordSets;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ParserFactory {
	private static IParserLogService defaultLogService = new DefaultLogService();

	public static IParserLogService createDefaultLogService() {
		return defaultLogService;
	}

	public static Set<String> getKeywordSet(KeywordSetKey key, ParserLanguage language) {
		return KeywordSets.getKeywords(key, language);
	}
}
