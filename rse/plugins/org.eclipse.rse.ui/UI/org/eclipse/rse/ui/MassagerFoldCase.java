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
 * depending on the value pass to the constructor or setter.
 * <p>
 * Note by default this also trims the 
 */
public class MassagerFoldCase implements ISystemMassager
{
   
    private boolean uppercase;
    private boolean trim;
    
	/**
	 * Constructor using uppercase as the case direction
	 */
	public MassagerFoldCase()
	{
		this(true);
	}
	/**
	 * Constructor using given case direction
	 * @param foldToUpperCase - whether to fold to uppercase (true) or lowercase (false).
	 */
	public MassagerFoldCase(boolean foldToUpperCase)
	{
		super();
		setFoldToUpperCase(foldToUpperCase);
		setTrimBlanks(true);
	}
	
	/**
	 * Toggle whether to fold to uppercase or lowercase
	 * @param foldToUpperCase - whether to fold to uppercase (true) or lowercase (false).
	 */
	public void setFoldToUpperCase(boolean foldToUpperCase)
	{
		this.uppercase = foldToUpperCase;
	}
	/**
	 * Toggle whether to trim blanks for not
	 * @param trimBlanks - whether to trim blanks (true) or leave them (false).
	 */
	public void setTrimBlanks(boolean trimBlanks)
	{
		this.trim = trimBlanks;
	}

	/**
	 * Return property about whether to fold to uppercase or lowercase
	 * @return true if folder to uppercase, false if folded to lowercaese
	 */
	public boolean getFoldToUpperCase()
	{
		return uppercase;
	}
	/**
	 * Return property about whether to trim blanks for not
	 * @return true if blanks are trimmed
	 */
	public boolean getTrimBlanks()
	{
		return trim;
	}	
	/**
	 * @see org.eclipse.rse.ui.ISystemMassager#massage(String)
	 */
	public String massage(String text)
	{
		if (text == null)
		  return null;
		if (trim)
		  text = text.trim();
		if (uppercase)
		  return toUpperCase(text);
		else
		  return toLowerCase(text);
	}

    /**
     * Overrridable method that actually does the uppercasing
     */
    protected String toUpperCase(String input)
    {
    	return input.toUpperCase();
    }
    /**
     * Overrridable method that actually does the lowercasing
     */
    protected String toLowerCase(String input)
    {
    	return input.toLowerCase();
    }
}