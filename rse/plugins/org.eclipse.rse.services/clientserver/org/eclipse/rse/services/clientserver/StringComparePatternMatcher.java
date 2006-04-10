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

/*
 * A pattern matcher for search that uses wildcard based compares
 */
public class StringComparePatternMatcher implements ISearchPatternMatcher
{
	private String _pattern;
	
	public StringComparePatternMatcher(String pattern)
	{
		_pattern = pattern;	
	}
	
	public boolean stringMatches(String compareString)
	{
		return StringCompare.compare(_pattern, compareString, true);
	}
}