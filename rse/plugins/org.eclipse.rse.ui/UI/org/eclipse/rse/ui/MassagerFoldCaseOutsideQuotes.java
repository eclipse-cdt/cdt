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

package org.eclipse.rse.ui;
/**
 * This massager folds the input text into either uppercase or lowercase,
 *  but ONLY for those portions of the string that are not inside delimiters. 
 * <p>
 * The default delimiter characters checked for are single or double quote characters, but this 
 *  can be changed by a setter method. When any of the delimiter characters are
 *  first found we enter delimited (non-folding) mode, until the same 
 *  non-escaped delimiter character is found. 
 * <p>
 * This massager assumes an imbedded delimiter is denoted by a doubled up 
 *  delimiter. If this is not the case, a setter can be used for the escape
 *  character.
 * <p>
 * This massager takes more time than the MassageFoldCaseUnlessQuoted massager,
 *  as that one just checks if the entire string is delimited, while this one
 *  attempts to check for ranges of delimiting.
 */
public class MassagerFoldCaseOutsideQuotes extends MassagerFoldCase
{
   
    private static final char[] DEFAULT_DELIMITERS = {'\"', '\''};
    private char[] delimiters;
    private char escape = ' ';
        
	/**
	 * Constructor using uppercase and using single and double quotes as delimiters
	 */
	public MassagerFoldCaseOutsideQuotes()
	{
		this(true, DEFAULT_DELIMITERS);
	}
	/**
	 * Constructor using given case direction, using single and double quotes as delimiters
	 * @param foldToUpperCase - whether to fold to uppercase (true) or lowercase (false).
	 */
	public MassagerFoldCaseOutsideQuotes(boolean foldToUpperCase)
	{
		this(foldToUpperCase, DEFAULT_DELIMITERS);
	}
	/**
	 * Constructor using given case direction, using given delimiters
	 * @param foldToUpperCase - whether to fold to uppercase (true) or lowercase (false).
	 * @param delimiters - chars to trigger delimited mode. Delimited sections are not folded.
	 */
	public MassagerFoldCaseOutsideQuotes(boolean foldToUpperCase, char[] delimiters)
	{
		super(foldToUpperCase);
		setDelimiters(delimiters);
	}
	
	/**
	 * Set the delimiter characters
	 * @param delimiters - chars to trigger delimited mode. Delimited sections are not folded.
	 */
	public void setDelimiters(char[] delimiters)
	{
		this.delimiters = delimiters;
	}
	/**
	 * Set the escape character used for denoted an imbedded delimiter. By default, it is assumed
	 * a doubled up delimiter is used for this.
	 * @param escapeChar - char that escapes the delimiter. Eg '\'
	 */
	public void setEscapeCharacter(char escapeChar)
	{
		this.escape = escapeChar;
	}
	
	/**
	 * Get the delimiter characters
	 */
	public char[] getDelimiters()
	{
		return delimiters;
	}
	/**
	 * Get the escape character
	 */
	public char getEscapeCharacter()
	{
		return escape;
	}
	
    /**
     * Overrridable method that actually does the uppercasing
     */
    protected String toUpperCase(String input)
    {
    	if ((input==null) || (input.length() == 0))
    	  return input;
    	else if (!hasAnyDelimiters(input)) // no delimit characters?
    	  return input.toUpperCase(); // fold it all!
    	else
    	  return doFolding(input, true);
    }
    /**
     * Overrridable method that actually does the lowercasing
     */
    protected String toLowerCase(String input)
    {
    	if ((input==null) || (input.length() == 0))
    	  return input;    
    	else if (!hasAnyDelimiters(input)) // no delimit characters?
    	  return input.toLowerCase(); // fold it all!
    	else
    	  return doFolding(input, false);
    }
    /**
     * Check for existence of any delimiters
     */
    protected boolean hasAnyDelimiters(String input)
    {
    	boolean hasAny = false;
    	for (int idx=0; !hasAny && (idx<delimiters.length); idx++)
    	   if (input.indexOf(delimiters[idx]) != -1)
    	     hasAny = true;
    	return hasAny;
    }
    
    /**
     * Method that actually walks the given string, character by character,
     *  folding all those which are not inside delimiters
     */
    protected String doFolding(String input, boolean upperCase)
    {
    	StringBuffer buffer = new StringBuffer(input.length());
    	boolean insideDelimiters = false;
    	boolean checkForEscape = (escape != ' ');
    	char currDelimiter = ' ';
    	char prevChar = ' ';
    	char currChar = ' ';
    	for (int idx=0; idx<input.length(); idx++)
    	{
    		prevChar = currChar;
    		currChar = input.charAt(idx);
    		// ------------------------
    		// outside of delimiters...
    		// ------------------------
    		if (!insideDelimiters)
    		{
    			// append folded
    	    	if (upperCase)
    	          buffer.append(Character.toUpperCase(currChar));
    	        else 
    	          buffer.append(Character.toLowerCase(currChar));
    			// check if this is the start of delimiting...
    			if (isDelimiter(currChar))
    			{
    				currDelimiter = currChar;
    				insideDelimiters = true; // enter delimited mode
    			}
    		}
    		// -----------------------
    		// inside of delimiters...
    		// -----------------------
    		else
    		{
    			buffer.append(currChar); // append as is    			
    			// check if this is the end of delimiting...
    			if (currChar == currDelimiter)
    			{
    				// ensure this isn't an escaped delimiter
    				if (!checkForEscape || (prevChar != escape))
    				  insideDelimiters = false; // exit delimited mode
    			}
    		}
    	}
    	return buffer.toString();
    }
    /**
     * Check if given character is one of the delimiters
     */
    protected boolean isDelimiter(char currChar)
    {
    	for (int idx=0; idx<delimiters.length; idx++)
    	   if (currChar == delimiters[idx])
    	      return true;
    	return false;
    }
}