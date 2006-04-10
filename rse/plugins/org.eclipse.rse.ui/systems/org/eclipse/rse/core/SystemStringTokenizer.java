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

package org.eclipse.rse.core;
import java.util.Vector;

/**
 * Similar to java's StringTokenizer, but supports multi-character delimiter
 * versus just single character delimiters. Eg usage is:
 * <code>
 *   SystemStringTokenizer tokens = new SystemStringTokenizer(stringToParse, "___");
 *   while (tokens.hasMoreTokens())
 *      String nextToken = tokens.nextToken();
 * </code>
 */
public class SystemStringTokenizer 
{
	private Vector tokens;
	private int    nextToken;	
	
	/**
	 * Constructor
	 * @param inputString The string to be tokenized
	 * @param delimiter The multi-char string that delimits the tokens. Eg "___"
	 */
	public SystemStringTokenizer(String inputString, String delimiter)
	{
		tokens = tokenize(inputString, delimiter);
		nextToken = 0;
	}
	
	/**
	 * Return a count of the number of tokens in the input string
	 */
	public int countTokens()
	{
	     return tokens.size();	
	}
	
	/**
	 * Return true if there are more tokens
	 */
	public boolean hasMoreTokens()
	{
		return (nextToken < tokens.size());
	}
	
	/**
	 * Return the next token
	 */
	public String nextToken()
	{
		if (nextToken < tokens.size())
		  return (String)tokens.elementAt(nextToken++);
		else
		  return null;
	}
	
    /**
     * Parse a string into tokens. 
     */
    public static Vector tokenize(String inputString, String delimiter)
    {
    	Vector v = new Vector();
    	StringBuffer token = new StringBuffer();
    	String lastToken = null;
    	int inpLen = inputString.length();
    	int delimLen = delimiter.length();
    	char delimChar1 = delimiter.charAt(0);
    	for (int idx=0; idx<inpLen; idx++)
    	{
    		int remLen = inpLen - idx; 
    		char currChar = inputString.charAt(idx);
    		if ((currChar == delimChar1) && (remLen >= delimLen) &&
    		    inputString.substring(idx,idx+delimLen).equals(delimiter))
    		{
    		  lastToken = token.toString();
    		  v.addElement(lastToken);
    		  //System.out.println("...token: " + token);    		  
    		  token.setLength(0);
    		  idx += delimLen-1;
    		}
    		else
    		  token.append(currChar);
    	}
    	if (token.length() > 0)
    	{
    	  lastToken = token.toString();
    	  v.addElement(lastToken);
          //System.out.println("...token: " + token);
    	}    	
    	return v;
    }
	
}