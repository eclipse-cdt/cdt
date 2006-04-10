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

package org.eclipse.dstore.core.util;

import java.util.ArrayList;

/**
 * This class is used to define a wildcard string pattern.  Strings 
 * can be compared against a pattern to determine whether they match
 * or not.
 */ 
public class Pattern
{

	private String _pattern;
	private ArrayList _subMatches;
	private ArrayList _matchSchema;

	/**
	 * Constructor
	 * @param pattern a wildcard string
	 * @param matchSchema 
	 */
	public Pattern(String pattern, ArrayList matchSchema)
	{
		_pattern = pattern;

		_matchSchema = matchSchema;
	}

	/**
	 * Returns a list of submatches
	 * @return a list of submatches
	 */
	public ArrayList getSubMatches()
	{
		return _subMatches;
	}

	/**
	 * Returns a submatch
	 * @param attribute an attribute to match
	 * @return a submatch
	 */
	public String getSubMatch(String attribute)
	{
		// find attribute index in match schema
		int index = _matchSchema.indexOf(attribute);
		if ((index >= 0) && (index < _subMatches.size()))
		{
			Object match = _subMatches.get(index);
			return new String((String) match);
		}
		else
		{
			return new String("null");
		}
	}

	/**
	 * Checks whther a compare string matches the pattern
	 * @param compareStr to string to compare
	 * @return true if there is a match
	 */
	public boolean matches(String compareStr)
	{
		String currentMatch = new String("");
		_subMatches = new ArrayList();

		int iText = 0;
		int iPattern = 0;
		int lastStar = 0;
		int len = compareStr.length();

		int patternLen = _pattern.length();

		while (iPattern < patternLen)
		{
			char p = _pattern.charAt(iPattern++);
			if (p == '*')
			{
				if (currentMatch.length() > 0)
				{
					_subMatches.add(new String(currentMatch));
				}
				currentMatch = new String("");

				if (iPattern >= patternLen)
				{
					while (iText < len)
					{
						currentMatch += compareStr.charAt(iText++);
					}
					_subMatches.add(new String(currentMatch));

					return true;
				}
				else
				{
					lastStar = iPattern;
				}
			}
			else
			{
				if (iText >= len)
				{
					return false;
				}
				else
				{
					char t = compareStr.charAt(iText++);
					if (p == t)
					{
						if ((lastStar > 0) && (iPattern >= patternLen) && (iText < len))
						{
						}
						else
						{
							continue;
						}

					}
					else
					{
						currentMatch += t;
						if (lastStar == 0)
						{
							return false;
						}
					}

					int matched = iPattern - lastStar - 1;
					iPattern = lastStar;

					iText -= matched;
				}
			}
		}

		if (iText >= len)
		{
			_subMatches.add(new String(currentMatch));
			return true;
		}
		else
		{
			return false;
		}
	}

}