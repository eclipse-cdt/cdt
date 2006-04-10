/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.clientserver.search;

import java.util.regex.Pattern;


/**
 * A string matcher for search.
 */
public class SystemSearchStringMatcher implements ISystemSearchMatcher {
	
	private String searchString;
	private boolean isRegex;
	
	// for a regular expression
	private Pattern regexPattern;
	
	// for non regular expression
	private SystemNonRegexMatcher nonRegexMatcher;

	/**
	 * Constructor to create a string matcher.
	 * @param searchString the search string.
	 * @param isCaseSensitive <code>true</code> if the matching should be done in a case sensitive manner, <code>false</code> otherwise.
	 * @param isRegex <code>true</code> if the search string is a regular expression, <code>false</code> otherwise.
	 * @throws
	 */
	public SystemSearchStringMatcher(String searchString, boolean isCaseSensitive, boolean isRegex) {
		
		if (searchString == null) {
			throw new NullPointerException();
		}
		
		this.searchString = searchString;
		this.isRegex = isRegex;
		
		// if the string pattern is a regular expression
		if (isRegex) {
			
		
			try {

				if (isCaseSensitive) {
					regexPattern = Pattern.compile(searchString);
				}
				else {
					regexPattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
				}
			}
			catch (Exception e) {
				// TODO: log it
			}
		}
		else {
			nonRegexMatcher = new SystemNonRegexMatcher(searchString, !isCaseSensitive, false);
		}
	}
	
	/**
	 * Returns whether the search string is empty.
	 * @return <code>true</code> if the search string is empty, <code>false</code> otherwise.
	 */
	public boolean isSearchStringEmpty() {
		return searchString.length() == 0;
	}
	
	/**
	 * Returns whether the search string is "*".
	 * @return <code>true</code> if the search string is "*", <code>false</code> otherwise.
	 */
	public boolean isSearchStringAsterisk() {
		return searchString.equals("*");
	}

	/**
	 * Returns whether there is a match for the given input. Returns <code>true</code> if the search string
	 * is an empty string.
	 * @see org.eclipse.rse.services.clientserver.ISystemSearchMatcher#matches(java.lang.String)
	 */
	public boolean matches(String input) {
		
		if (isSearchStringEmpty()) {
			return true;
		}
		
		if (isRegex) {
			return regexPattern.matcher(input).find();
		}
		else {
			// TODO (KM): can't use nonRegexMatcher.match(input) here because it returns
			// false even when there is a match inside the input. Investigate what is wrong.
			return nonRegexMatcher.find(input, 0, input.length()) != null;
		}
	}
}