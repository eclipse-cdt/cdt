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

package org.eclipse.rse.ui.validators;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * For editable integer numbers that must be within a certain range to be valid
 */
public class ValidatorIntegerRangeInput extends ValidatorIntegerInput 
{
	private int minRange, maxRange;
	private int orgMinRange, orgMaxRange;
	private Integer minRangeInt, maxRangeInt;
	private SystemMessage msg_InvalidRange;
	
    /**
     * Constructor when an empty field is not allowed (will result in an error message)
     * @param minRange - the lowest valid number
     * @param maxRange - the highest valid number
     */
	public ValidatorIntegerRangeInput(int minRange, int maxRange)
	{
		this(minRange, maxRange, false);
	}
    /**
     * Constructor when an empty field is allowed.
     * @param minRange - the lowest valid number
     * @param maxRange - the highest valid number
     * @param allowBlank - true if blanks allowed, false if not
     */
	public ValidatorIntegerRangeInput(int minRange, int maxRange, boolean allowBlank)
	{
		super(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_NUMBER_EMPTY),
		       RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_NUMBER_NOTVALID));
		msg_InvalidRange = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_NUMBER_OUTOFRANGE);
		this.orgMinRange = minRange;
		this.orgMaxRange = maxRange;
		super.setBlankAllowed(allowBlank);
		setRange(minRange, maxRange);
	}

	/**
	 * Reset the range
	 */
	public void setRange(int minRange, int maxRange)
	{
		this.minRange = minRange;
		this.maxRange = maxRange;
		minRangeInt = new Integer(minRange);
		maxRangeInt = new Integer(maxRange);		
	}
	/**
	 * Restore the range originally specified in the constructor
	 */
	public void restoreRange()
	{
		setRange(orgMinRange, orgMaxRange);
	}
	/**
	 * Set the error messages, overriding the defaults
	 */
	public void setErrorMessages(SystemMessage emptyMsg, SystemMessage invalidMsg, SystemMessage outOfRangeMsg)
	{
		super.setErrorMessages(emptyMsg, invalidMsg);
		msg_InvalidRange = outOfRangeMsg;
	}

    /**
     * Return the max length for this name, or -1 if no max.
     * We return a max length that just allows the largest number in the range to be set, plus the sign if negative
     */
    public int getMaximumNameLength()
    {
    	int maxlen = Integer.toString(Math.abs(maxRange)).length();
    	if (maxRange < 0)
    	  ++maxlen;
    	return maxlen;
    }
    
	/**
	 * Intercept of parent to also add range checking
	 */
	public String isValid(String input) 
	{		
		String msg = super.isValid(input);
		if ((msg == null) && (input != null) && (input.length()>0))
		{
		  if ((number < minRange) || (number > maxRange))
		  {
		    currentMessage = msg_InvalidRange;
		    currentMessage.makeSubstitution(input, minRangeInt, maxRangeInt);
		    msg = currentMessage.getLevelOneText();
		  }
		}
		return msg;
	}

}