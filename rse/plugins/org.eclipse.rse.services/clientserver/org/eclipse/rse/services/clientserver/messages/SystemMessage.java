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

package org.eclipse.rse.services.clientserver.messages;

// To dos:
// 1) can remove the following import when deprecated stuff is removed....
// 2) remove deprecated classes....
// ... done. Phil.


import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Error message window class.
 */
public class SystemMessage
       //implements ISystemMessages
{
        
   // start of the new stuff!!!  Violaine Batthish
   // Indicators
	/**
	 * Completion message indicator
	 */
	public static final char COMPLETION='C';
	/**
	 * Inquiry message indicator
	 */
	public static final char INQUIRY='Q';
	/**
	 * Information message indicator
	 */
	public static final char INFORMATION='I';
	/**
	 * Error message indicator
	 */
	public static final char ERROR='E';
	/**
	 * Warning message indicator
	 */
	public static final char WARNING='W';
	/**
	 * Unexpected message indicator (same as warning but will log exception & stack in message log)
	 */
	public static final char UNEXPECTED='U';
	
	// Private variables
	private char subPrefix='%';
	private char indicator;
	private String level1NS, level2NS;  // level 1 & 2 with NO substitution made
	private String level1WS, level2WS;	// level 1 & 2 with substitutions made.
	private String component, subComponent;
	private String messageNumber;
	private int numSubs=-1;
	private Object[] subs=null;

	protected static final String NESTING_INDENT = "  "; //$NON-NLS-1$


	/** SystemMesssage constructor
	 * <p>Throws:
	 * <li>IndicatorException:  the indicator specified is not a valid indicator
	 * @param comp:	Component
	 * @param sub:		Subcomponent
	 * @param ind:		Indicator
	 * @param number:  message number
	 * @param l1:		Level 1 text
	 * @param l2:		Level 2 text 
	 */
	public SystemMessage(String comp, String sub, String number, char ind, String l1, String l2) throws IndicatorException {
		component=comp.toUpperCase();
		subComponent=sub.toUpperCase();
		messageNumber=number.toUpperCase();
		setIndicator(ind);
		level1NS=l1.trim();
		level2NS=l2.trim();
		level1WS=l1.trim();
		level2WS=l2.trim();
		
	}
	
	/**
	 * Use this method to override the default severity of a message
	 * Throws SeverityException if the severity is not valid
	 */
	public void setIndicator(char ind) throws IndicatorException {
		// check that a valid indicator was specified
		if (ind != INQUIRY  &&
		    ind != INFORMATION &&
		    ind != ERROR &&
		    ind != WARNING &&
		    ind != UNEXPECTED &&
			ind != COMPLETION)
		   throw(new IndicatorException("Indicator specified not valid. Unable to set Indicator."));
	
		// to do: decide if there are other indicator change violations....
		
		// set indicator
		indicator=ind;
	}
	
	/**
	 * Use this method to get the severity. Will be one of:
	 * <ul>
	 *   <li>{@link #INQUIRY} for a question message.
	 *   <li>{@link #COMPLETION} for a completion message.
	 * 	 <li>{@link #INFORMATION} for an informational message.
	 *   <li>{@link #ERROR} for an error message.
	 *   <li>{@link #WARNING} for a warning message.
	 *   <li>{@link #UNEXPECTED} for an unexpected-situation-encountered message.
	 * </ul>
	 **/
	public char getIndicator() {
		return indicator;
	}

	/**
	 * @return the message number of this message.
	 */
	public String getMessageNumber() {
		return messageNumber;
	}
	
	/**
	 * @return the component of this message.
	 */
	public String getComponent() {
		return component;
	}
	
	/**
	 * @return the subcomponent of this message.
	 */
	public String getSubComponent() {
		return subComponent;
	}
	
	/**
	 * Use this method to retrieve the unique number of substitution variables 
	 * in this message (this would include level 1 and 2 text
	 * @return (int) number of unique substitution variables variables
	 **/
	public int getNumSubstitutionVariables() {
		// see if we already have this information stored
		if (numSubs>=0)		
			return numSubs;
		// otherwise we need to count the substitution variables.
		else {
			numSubs=0;	// initial value
			String allText=level1NS+" "+level2NS;
			String subVar=subPrefix+new Integer(numSubs+1).toString();
			int subLoc=allText.indexOf(subVar);
			while (subLoc>=0) {
				    // in first position
				if ((subLoc==0 && 
				     !Character.isDigit(allText.substring(subVar.length()).toCharArray()[0])) ||
				    // in last position 
					(subLoc==allText.length()-subVar.length() &&
					 allText.substring(subLoc-1).toCharArray()[0]!=subPrefix) ||
					// somewhere in the middle
					(!Character.isDigit(allText.substring(subLoc+subVar.length()).toCharArray()[0]) &&
					 allText.substring(subLoc-1).toCharArray()[0]!=subPrefix))
						 numSubs++;
				subVar=subPrefix+new Integer(numSubs+1).toString();	 
				subLoc=allText.indexOf(subVar);
			}
			return numSubs;	
		}
	}
	
	/**
	 * Use this method to retrieve level one text
	 * @return String - level one text with subsitutions made.
	 **/
	public String getLevelOneText() {
		return level1WS;
	}
	
	/**
	 * Use this method to retrieve level two text
	 * @return String - level two text with subsitutions made.
	 **/
	public String getLevelTwoText() {
		return level2WS;
	}
		
	/**
	 * Use this method to retrieve the full message ID:
	 *   Component + SubComponent + Number + indicator 
	 * @return String - the full message ID 
	 **/
	public String getFullMessageID() {
		return component+subComponent+messageNumber+indicator;
	}


	/**
	 * Use this method to retrieve 'long' message ID format:
	 * 		Component + SubComponent + Number.
	 * The long message ID is used for retrieving messages from a message file.
	 * @return String - the long message ID 
	 **/
	public String getLongMessageID() {
		return component + subComponent + messageNumber;
	} 
	
	/**
	 * Use this method to retrieve 'standard' message ID format:
	 * 		Component + Number
	 * @return String - the full message ID 
	 **/
	public String getMessageID() {
		return component + messageNumber;
	} 
	
	/**
	 * Tests if this message has a long id equal to the one supplied in the argument.
	 * @param messageId the long message id to compare against.
	 * @return a boolean indicating if the message ids are equal.
	 */
	public boolean hasLongID(String messageId) {
		return getLongMessageID().equals(messageId);
	}

	/**
	 * Use this method to set substitution value %1.
	 * <br>Generally toString() is used on the substitution objects, but there is 
	 *   special case handling for exception objects and IStatus objects.
	 */
	public SystemMessage makeSubstitution(Object sub1) {
		level1WS=level1NS;
		level2WS=level2NS;
		makeSub(1, sub1);
		// save subs
		subs=new Object[1];
		subs[0]=sub1;
		return this;
	}
	/**
	 * Use this method to set substitution value %1 %2
	 * <br>Generally toString() is used on the substitution objects, but there is 
	 *   special case handling for exception objects and IStatus objects.
	 */
	public SystemMessage makeSubstitution(Object sub1, Object sub2) {
		level1WS=level1NS;
		level2WS=level2NS;
		makeSub(1, sub1);
		makeSub(2, sub2);
		// save subs
		subs=new Object[2];
		subs[0]=sub1;
		subs[1]=sub2;
		return this;
	}
	/**
	 * Use this method to set substitution value %1 %2 %3
	 * <br>Generally toString() is used on the substitution objects, but there is 
	 *   special case handling for exception objects and IStatus objects.
	 */
	public SystemMessage makeSubstitution(Object sub1, Object sub2, Object sub3) {
		level1WS=level1NS;
		level2WS=level2NS;
		makeSub(1, sub1);
		makeSub(2, sub2);
		makeSub(3, sub3);
		// save subs
		subs=new Object[3];
		subs[0]=sub1;
		subs[1]=sub2;
		subs[2]=sub3;
		return this;
	}
	/**
	 * Use this method to set substitution value %1 %2 %3 %4
	 * <br>Generally toString() is used on the substitution objects, but there is 
	 *   special case handling for exception objects and IStatus objects.
	 */
	public SystemMessage makeSubstitution(Object sub1, Object sub2, Object sub3, Object sub4) {
		level1WS=level1NS;
		level2WS=level2NS;
		makeSub(1, sub1);
		makeSub(2, sub2);
		makeSub(3, sub3);
		makeSub(4, sub4);
		// save subs
		subs=new Object[4];
		subs[0]=sub1;
		subs[1]=sub2;
		subs[2]=sub3;
		subs[3]=sub4;
		return this;
	}
	
	/**
	 * Use this method to set substitution values 
	 * <br>Generally toString() is used on the substitution objects, but there is 
	 *   special case handling for exception objects and IStatus objects.
	 */
	public SystemMessage makeSubstitution(Object [] subsList) 
	{ 
		level1WS=level1NS;
		level2WS=level2NS;
		
		if ((subsList!=null) && (subsList.length>0))
		  for (int i=0; i<subsList.length; i++)
			makeSub(i+1, subsList[i]);
		// save subs
		subs=subsList;
		return this;
	}
	
	/**
	 * retrieves an array of substitution variables
	 * @return Object[] array of substitution variables used
	 */
	public Object[] getSubVariables() {
		return subs;
	}
	/**
	 * private method to make a substitution
	 */
	private void makeSub(int subNumber, Object sub) 
	{
		if (sub == null)
		  return;

		String subValue = getSubValue(sub);
				  
		// check that substitution is needed
		if (subNumber>getNumSubstitutionVariables())
			return;

		String subVar=subPrefix+new Integer(subNumber).toString();
		int subLoc = -1;
		
		// set level 1 
		// - quick test added by Phil
		if (level1WS.equals(subVar))
		  level1WS = subValue;
		else
		{
		  subLoc=level1WS.indexOf(subVar);
		
		  // FIXES BY PHIL
		  // 1. 
		  // in the following code we were doing toCharArray only to index and get the first char.
		  // this is not required! there is a charAt method in String for this. Phil. EG:
		    //!Character.isDigit(level1WS.substring(subVar.length()).toCharArray()[0])) || 
		  
		  // 2. compared subLoc to gt zero, vs gte zero
		  // 3. fixing that exposed a bug for the case when level one or two is only '%1'
		  //
		  // This code is hard to read and maintain and should be fixed up someday. 
		  // -- why do we check if the next or previous char is a digit? For double digit sub vars like %11?
		  
		  //while (subLoc>0) fixed. phil
		  while (subLoc>=0) 
		  {
		    // in first position
			if ((subLoc==0 && 
			    !Character.isDigit(level1WS.substring(subVar.length()).charAt(0))) ||
			   // in last position 
			(subLoc==level1WS.length()-subVar.length() &&
			 level1WS.substring(subLoc-1).charAt(0)!=subPrefix) ||
			// somewhere in the middle
			(!Character.isDigit(level1WS.substring(subLoc+subVar.length()).charAt(0)) &&
			 level1WS.substring(subLoc-1).charAt(0)!=subPrefix))
				 level1WS=level1WS.substring(0,subLoc)+subValue+level1WS.substring(subLoc+subVar.length());
			subLoc=level1WS.indexOf(subVar, subLoc + subValue.length() );
		  }
		}
		// set level 2
		// - quick test added by Phil
		if (level2WS.equals(subVar))
		  level2WS = subValue;
		else
		{
	  	  subLoc=level2WS.indexOf(subVar);
		  //while (subLoc>0) fixed. phil
		  while (subLoc>=0) 
		  {
		    // in first position
			if ((subLoc==0 && 
			     //!Character.isDigit(level2WS.substring(subVar.length()).charAt(0))) ||
			     !Character.isDigit(level2WS.charAt(subVar.length()))) ||
			    // in last position 
				(subLoc==level2WS.length()-subVar.length() &&
				 level2WS.substring(subLoc-1).charAt(0)!=subPrefix) ||
				// somewhere in the middle
				(!Character.isDigit(level2WS.substring(subLoc+subVar.length()).charAt(0)) &&
				 level2WS.substring(subLoc-1).charAt(0)!=subPrefix))
					 level2WS=level2WS.substring(0,subLoc)+subValue+level2WS.substring(subLoc+subVar.length());
			subLoc=level2WS.indexOf(subVar, subLoc + subValue.length() );
		  }
		}
	} 
        
	
 
    public void setPrefixChar(char prefixChar) {       	
		subPrefix = prefixChar;
    }
    
	/**
	 * Do message variable substitution. Using you are replacing &1 (say) with
	 *  a string.
	 * Still need this for non-message substitution capability. Phil.
	 * @param string - string containing substring to be substituted. 
	 * @param subOld - substitution variable. Eg "%1"
	 * @param subNew - substitution data. Eg "001"
	 * @return message with all occurrences of subOld substituted with subNew.
	 */
	public static String sub(String msg, String subOld, String subNew)
	{
		StringBuffer temp = new StringBuffer();
		int lastHit = 0;
		int newHit = 0;
		for (newHit = msg.indexOf(subOld,lastHit); newHit != -1;
			 lastHit = newHit, newHit = msg.indexOf(subOld,lastHit))
		   {
			 if (newHit >= 0)
			   temp.append(msg.substring(lastHit,newHit));
			 temp.append(subNew);
			 newHit += subOld.length();
		   }
		if (lastHit >= 0)
		  temp.append(msg.substring(lastHit));
		return temp.toString();
	}
	
	// Housekeeping...
	/**
	 * Convert this message to a string.
	 * Returns string of the form: msgId + severity + ":" + first-level-text 
	 */
	public String toString()
	{
		return getFullMessageID() + ": " + getLevelOneText();  		
	}

	

	/**
	 * used to determine the string value of the object 
	 * it calls toString for all object types except for Exceptions
	 * where the stack is also rendered
	 * @param sub  the substitution object
	 * @return the string value for the object
	 */
	public String getSubValue(Object sub) 
	{
		
		// the following code caused a crash so I changed it. Phil.
		/*
		Class subClass=sub.getClass();
		// loop through all the classes for the object
		try {
			while (subClass!=null) {
				if (subClass.equals(Class.forName("Exception"))) {
					Exception exc=(Exception)sub;
					StringWriter excWriter = new StringWriter();
					exc.printStackTrace(new PrintWriter(excWriter));
					return exc.toString()+"\n"+ excWriter.toString();
				}
				subClass=subClass.getSuperclass();
			}
		}
		catch(ClassNotFoundException e) {
			// class was not found
			SystemBasePlugin.logError("SystemMessge: getSubValue (Class not found) "+e.toString(), e);
			}
	    */
	
	    if (sub == null)
	      return "";
	      
	    if (sub instanceof Exception)
	    {
		  Exception exc=(Exception)sub;
	      StringWriter excWriter = new StringWriter();
		  exc.printStackTrace(new PrintWriter(excWriter));
		  String msg = exc.toString();
		  //String msg = exc.getMessage();
		  if ((msg == null) || (exc instanceof ClassCastException))
		    msg = exc.getClass().getName();
		  return msg+"\n"+ excWriter.toString();	    	
	    }
	   
	    
		return sub.toString();
	}

	
}