/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class CSearchPattern
	implements ICSearchConstants, ICSearchPattern {

	/**
	 * @param matchMode
	 * @param caseSensitive
	 */
	public CSearchPattern(int matchMode, boolean caseSensitive) {
		_matchMode = matchMode;
		_caseSensitive = caseSensitive;
	}

	/**
	 * 
	 */
	public CSearchPattern() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static CSearchPattern createPattern( String patternString, int searchFor, int limitTo, int matchMode, boolean caseSensitive ){
		if( patternString == null || patternString.length() == 0 ){
			return null;
		}
		
		CSearchPattern pattern = null;
		switch( searchFor ){
			case ICSearchConstants.TYPE:
				pattern = createTypePattern( patternString, limitTo, matchMode, caseSensitive );
				break;
			case ICSearchConstants.METHOD:
				pattern = createMethodPattern( patternString, limitTo, matchMode, caseSensitive );
				break;
			case ICSearchConstants.CONSTRUCTOR:
				pattern = createConstructorPattern( patternString, limitTo, matchMode, caseSensitive );
				break;
			case ICSearchConstants.FIELD:
				pattern = createFieldPattern( patternString, limitTo, matchMode, caseSensitive );
				break;
		}
		
		return pattern;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createFieldPattern(String patternString, int limitTo, int matchMode, boolean caseSensitive) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createMethodPattern(String patternString, int limitTo, int matchMode, boolean caseSensitive) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createConstructorPattern(String patternString, int limitTo, int matchMode, boolean caseSensitive) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createTypePattern(String patternString, int limitTo, int matchMode, boolean caseSensitive) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected boolean matchesName( char[] pattern, char[] name ){
		if( pattern == null ){
			return true;  //treat null as "*"
		}
		
		if( name != null ){
			switch( _matchMode ){
				case EXACT_MATCH:
					//return CharOperation.equals( pattern, name, _caseSensitive );
				case PREFIX_MATCH:
					//return CharOperation.prefixEquals( pattern, name, _caseSensitive );
				case PATTERN_MATCH:
					if( !_caseSensitive ){
						//pattern = CharOperation.toLowerCase( pattern );
					}
					//return CharOperation.match( pattern, name, _caseSensitive );
			}
		}
		return false;
	}
	
	
	protected int 		_matchMode;
	protected boolean 	_caseSensitive;
}
