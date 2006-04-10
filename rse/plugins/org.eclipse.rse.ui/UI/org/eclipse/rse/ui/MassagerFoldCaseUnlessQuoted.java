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
 *  but ONLY if the given input is not delimited. The default delimiter checked
 *  for is the double quote character, but this can be changed by a setter method.
 */
public class MassagerFoldCaseUnlessQuoted extends MassagerFoldCase
{
   
    private char delimiter;
    
	/**
	 * Constructor using uppercase and using a double quote as delimiter
	 */
	public MassagerFoldCaseUnlessQuoted()
	{
		this(true, '\"');
	}
	/**
	 * Constructor using given case direction, using a double quote as delimiter
	 * @param foldToUpperCase - whether to fold to uppercase (true) or lowercase (false).
	 */
	public MassagerFoldCaseUnlessQuoted(boolean foldToUpperCase)
	{
		this(foldToUpperCase, '\"');
	}
	/**
	 * Constructor using given case direction, using given delimiter
	 * @param foldToUpperCase - whether to fold to uppercase (true) or lowercase (false).
	 * @param delimiter - char to check if text is enclosed with. If it is, then no folding is done.
	 */
	public MassagerFoldCaseUnlessQuoted(boolean foldToUpperCase, char delimiter)
	{
		super(foldToUpperCase);
		setDelimiter(delimiter);
	}
	
	/**
	 * Set the delimiter character
	 * @param delimiter - char to check if text is enclosed with. If it is, then no folding is done.
	 */
	public void setDelimiter(char delimiter)
	{
		this.delimiter = delimiter;
	}
	
	/**
	 * Get the delimiter character
	 */
	public char getDelimiter()
	{
		return delimiter;
	}

    /**
     * Overrridable method that actually does the uppercasing
     */
    protected String toUpperCase(String input)
    {
    	if ((input.length() >= 2) &&
    	     (input.charAt(0) == delimiter) &&  // start with delimiter?
    	     (input.charAt(input.length()-1) == delimiter)) // end with delimiter?
    	  return input; // do nothing
    	else
    	  return input.toUpperCase(); // fold
    }
    /**
     * Overrridable method that actually does the lowercasing
     */
    protected String toLowerCase(String input)
    {
    	if ((input.length() >= 2) &&
    	     (input.charAt(0) == delimiter) &&  // start with delimiter?
    	     (input.charAt(input.length()-1) == delimiter)) // end with delimiter?
    	  return input; // do nothing
    	else
    	  return input.toLowerCase(); // fold
    }
}