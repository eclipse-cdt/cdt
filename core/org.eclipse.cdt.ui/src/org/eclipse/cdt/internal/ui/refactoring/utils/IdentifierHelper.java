/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import com.ibm.icu.text.MessageFormat;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;

import org.eclipse.cdt.internal.core.parser.token.KeywordSets;

/**
 * Class to verify that an identifier meets the C++ rules for valid names.
 * 
 * @author Thomas Corbat
 *
 */
public class IdentifierHelper {

	/**
	 * @param identifier to check
	 * @return an instance of IdentifierResult that holds the outcome of the validation
	 */
	public static IdentifierResult checkIdentifierName(String identifier){

		if(identifier == null){
			return null;
		}
		if(isCorrect(identifier)){
			if(isKeyword(identifier)){
				return new IdentifierResult(IdentifierResult.KEYWORD, MessageFormat.format(Messages.IdentifierHelper_isKeyword, new Object[] {identifier})); 
			}
			return new IdentifierResult(IdentifierResult.VALID, MessageFormat.format(Messages.IdentifierHelper_isValid, new Object[] {identifier}));
		} else if(isLeadingADigit(identifier)){
			return new IdentifierResult(IdentifierResult.DIGIT_FIRST, MessageFormat.format(Messages.IdentifierHelper_leadingDigit, new Object[] {identifier})); 
		} else if(identifier.length() == 0){
			return new IdentifierResult(IdentifierResult.EMPTY, Messages.IdentifierHelper_emptyIdentifier ); 
		} else if(hasIllegalCharacters(identifier)){
			return new IdentifierResult(IdentifierResult.ILLEGAL_CHARACTER, MessageFormat.format(Messages.IdentifierHelper_illegalCharacter, new Object[] {identifier})); 
		}
		
		return new IdentifierResult(IdentifierResult.UNKNOWN, MessageFormat.format(Messages.IdentifierHelper_unidentifiedMistake, new Object[] {identifier})); 
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
