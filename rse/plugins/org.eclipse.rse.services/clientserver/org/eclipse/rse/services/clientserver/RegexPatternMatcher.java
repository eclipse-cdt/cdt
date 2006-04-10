/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.services.clientserver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A pattern matcher used for search that uses regex
 */
public class RegexPatternMatcher implements ISearchPatternMatcher
{
	private Pattern _regexPattern;
		
	public RegexPatternMatcher(String pattern)
	{
		_regexPattern = Pattern.compile(pattern);
	}
	
	public boolean stringMatches(String compareString)
	{		
		Matcher matcher = _regexPattern.matcher(compareString);
		return matcher.matches();

	}
}