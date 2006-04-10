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

/**
 * Utility class for comparing a wildcard string to another string
 */
public class StringCompare
{

	/**
	 * Constructor
	 */
	public StringCompare()
	{
	}

	/**
	 * Compare two strings
	 * 
	 * @param pattern the pattern to match
	 * @param compareStr the string to compare against the pattern
	 * @param noCase indicates whether the strings should be compared based on case
	 * @return true if the compare string matches the pattern
	 */
	public static boolean compare(String pattern, String compareStr, boolean noCase)
	{
		if ((pattern == null) || (compareStr == null))
			return false;

		if (noCase)
		{
			pattern = pattern.toUpperCase();
			compareStr = compareStr.toUpperCase();
		}

		String currentMatch = new String("");

		int iText = 0;
		int iPattern = 0;
		int lastStar = 0;
		int len = compareStr.length();

		int patternLen = pattern.length();

		while (iPattern < patternLen)
		{
			char p = pattern.charAt(iPattern++);
			if (p == '*')
			{

				if (iPattern >= patternLen)
				{
					while (iText < len)
					{
						iText++;
					}
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
			return true;
		}
		else
		{
			return false;
		}
	}

}