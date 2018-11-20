/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.osgi.util.NLS;

/**
 * Class to verify that an identifier meets the C++ rules for valid names.
 *
 * @author Thomas Corbat
 */
public class IdentifierHelper {
	private static Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z_]\\w*"); //$NON-NLS-1$
	private static Pattern INVALID_CHARACTER_PATTERN = Pattern.compile("\\W"); //$NON-NLS-1$

	/**
	 * @param identifier to check
	 * @return an instance of IdentifierResult that holds the outcome of the validation
	 */
	public static IdentifierResult checkIdentifierName(String identifier) {
		if (identifier == null) {
			return null;
		}
		if (isValidIdentifier(identifier)) {
			if (isKeyword(identifier)) {
				return new IdentifierResult(IdentifierResult.KEYWORD,
						NLS.bind(Messages.IdentifierHelper_isKeyword, identifier));
			}
			return new IdentifierResult(IdentifierResult.VALID,
					NLS.bind(Messages.IdentifierHelper_isValid, identifier));
		} else if (isLeadingADigit(identifier)) {
			return new IdentifierResult(IdentifierResult.DIGIT_FIRST,
					NLS.bind(Messages.IdentifierHelper_leadingDigit, identifier));
		} else if (identifier.length() == 0) {
			return new IdentifierResult(IdentifierResult.EMPTY, Messages.IdentifierHelper_emptyIdentifier);
		} else if (hasIllegalCharacters(identifier)) {
			return new IdentifierResult(IdentifierResult.ILLEGAL_CHARACTER,
					NLS.bind(Messages.IdentifierHelper_illegalCharacter, identifier));
		}

		return new IdentifierResult(IdentifierResult.UNKNOWN,
				NLS.bind(Messages.IdentifierHelper_unidentifiedMistake, identifier));
	}

	private static boolean isKeyword(String identifier) {
		Set<String> keywords = KeywordSets.getKeywords(KeywordSetKey.KEYWORDS, ParserLanguage.CPP);
		return keywords.contains(identifier);
	}

	private static boolean hasIllegalCharacters(String identifier) {
		Matcher m = INVALID_CHARACTER_PATTERN.matcher(identifier);
		return m.find();
	}

	public static boolean isLeadingADigit(String identifier) {
		return identifier.length() > 0 && Character.isDigit(identifier.charAt(0));
	}

	private static boolean isValidIdentifier(String identifier) {
		Matcher m = IDENTIFIER_PATTERN.matcher(identifier);
		return m.matches();
	}
}
