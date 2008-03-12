/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;

/**
 * @author Thomas Corbat
 *
 */
public class IdentifierHelper {

	private static final String QUOTE = Messages.IdentifierHelper_quote; 

	public static IdentifierResult checkIdentifierName(String identifier){

		if(identifier == null){
			return null;
		}
		if(isCorrect(identifier)){
			if(isKeyword(identifier)){
				return new IdentifierResult(IdentifierResult.KEYWORD, QUOTE + identifier + Messages.IdentifierHelper_isKeyword); 
			}
			else{
				return new IdentifierResult(IdentifierResult.VALID, QUOTE + identifier + Messages.IdentifierHelper_isValid ); 
			}
		} else if(isLeadingADigit(identifier)){
			return new IdentifierResult(IdentifierResult.DIGIT_FIRST, QUOTE + identifier + Messages.IdentifierHelper_leadingDigit); 
		} else if(identifier.length() == 0){
			return new IdentifierResult(IdentifierResult.EMPTY, Messages.IdentifierHelper_emptyIdentifier ); 
		} else if(hasIllegalCharacters(identifier)){
			return new IdentifierResult(IdentifierResult.ILLEGAL_CHARACTER, Messages.IdentifierHelper_illegalCharacter + identifier + QUOTE); 
		}
		
		return new IdentifierResult(IdentifierResult.UNKNOWN, QUOTE + identifier + Messages.IdentifierHelper_unidentifiedMistake); 
	}

	private static boolean isKeyword(String identifier) {
		
		for(String currentKeyword : getKeywords()){
			if(identifier.equals(currentKeyword)){
				return true;
			}
		}
		return false;
	}

	private static boolean hasIllegalCharacters(String identifier) {
		Pattern p = Pattern.compile("\\W"); //$NON-NLS-1$
		Matcher m = p.matcher(identifier);
		return m.find();
	}

	private static boolean isLeadingADigit(String identifier) {
		Pattern p = Pattern.compile("\\d.*"); //$NON-NLS-1$
		Matcher m = p.matcher(identifier);
		return m.matches();
	}

	private static boolean isCorrect(String identifier) {
		Pattern p = Pattern.compile("[a-zA-Z_]\\w*"); //$NON-NLS-1$
		Matcher m = p.matcher(identifier);
		return m.matches();
	}
	
	public static String[] getKeywords() {
		Set<String> keywords= KeywordSets.getKeywords(KeywordSetKey.KEYWORDS, ParserLanguage.CPP);
		return keywords.toArray(new String[keywords.size()]);
	}
}
