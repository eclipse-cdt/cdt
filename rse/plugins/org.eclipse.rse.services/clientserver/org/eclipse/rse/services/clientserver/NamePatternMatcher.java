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

import java.io.PrintWriter;

/**
 * This class offers generic name pattern matching.
 * <p>
 * This supports one wildcard character ('*") anywhere in
 * the name, or one at the beginning and end of the name.
 * <ol>
 *     <li>ABC </li>
 *     <li>* </li>
 *     <li>ABC* </li>
 *     <li>*ABC </li>
 *     <li>AB*C </li>
 *     <li>*ABC* </li>
 *  </ol> 
 * <p>
 * This pattern matching class also <i>optionally</i> supports additional
 *  advanced patterns beyond the stricter PDM style. These allow
 *  for two '*'s anywhere in the name.
 * <ol>
 *     <li>AB*C* </li>
 *     <li>*A*C </li>  
 *     <li>A*B*C </li> 
 *  </ol>
 * <p>
 * Quoted names are supported.
 * <p>
 * All matching is case-sensitive!
 * <p>
 * Instantiate this class for a given generic name, and then
 *   call matches(String input) for each input name to see if it
 *   matches the pattern.
 * <p>
 * To enable the advanced patterns, pass <code>true</code> to the constructor.
 */
public class NamePatternMatcher implements IMatcher
{
	
	/**
	 * Wildcard character: *
	 */
	public static final char WILDCARD = '*';
	private static final String WILDCARD_DOUBLED = "**";    
	/**
	 * Example: Quoted name delimiter: "
	 */	    
	public static final char QUOTE = '"';
	/**
	 * Example: ABC
	 */	
	public static final int SCALAR = 0;
	/**
	 * Example: *
	 */
	public static final int ALL = 1;    
	/**
	 * Example: ABC*
	 */
	public static final int WILDCARD_END = 2;
	/**
	 * Example: *ABC
	 */
	public static final int WILDCARD_START = 4;
	/**
	 * Example: A*C
	 */
	public static final int WILDCARD_MIDDLE = 8;
	/**
	 * Example: *ABC*
	 */
	public static final int WILDCARD_START_END = 16;
	/*
	 * Example: A*C*
	 */
	public static final int WILDCARD_MIDDLE_END = 32;
	/**
	 * Example: *A*F
	 */
	public static final int WILDCARD_START_MIDDLE = 64;    
	/**
	 * Example: A*C*F
	 */
	public static final int WILDCARD_MIDDLE_MIDDLE = 128;
    
    
	// used in writeInfo debugging method
	private static final int[] TYPES_IDX = {SCALAR,ALL,WILDCARD_END,WILDCARD_START,WILDCARD_MIDDLE,
				   WILDCARD_START_END,WILDCARD_MIDDLE_END,WILDCARD_START_MIDDLE,WILDCARD_MIDDLE_MIDDLE};
	private static final String[] TYPES = {"SCALAR","ALL","END","START","MIDDLE","START_END","MIDDLE_END","START_MIDDLE","MIDDLE_MIDDLE"};
    
	private String genericName, part1, part2, part3;
	private int part1len,part2len,part3len,part12len,part123len;
	private int patternType;
	private boolean quotedName, validName;
	private boolean caseSensitive = true;
	/**
	 * Constructor for traditional-style only patterns, which allows
	 *  for one asterisk anywhere in the name,or one asterisk each at the
	 *  beginning or end of the name.
	 * <p>
	 * If you don't know for sure the input is generic or valid, after
	 *   constructing call:
	 *   <sl>
	 *    <li> {@link #isValid()} to determine if given generic name was valid. </li>
	 *    <li> {@link #isGeneric()} to determine if given generic name had a wildcard. </li>
	 *   </sl>
	 * <p>
	 * If curious, you can also subsequently call:
	 *   <sl>
	 *     <li> {@link #getPatternType()} to determine which type of pattern the generic name follows. </li>
	 *   </sl>
	 * <p>
	 * Once constructed for a valid name, you can call 
	 *   <sl>
	 *     <li> {@link #matches(String)} for each name to see if it matches this generic name pattern.
	 *   </sl>
     * <p>
     * Quoted names are supported.
     * <p>
     * All matching is case-sensitive!
     * 
	 * @param genericName generic name to do pattern matching for (ie, ABC*DEF)
	 */
	public NamePatternMatcher(String genericName)    
	{
		  this(genericName, false, true);
	}
	/**
	 * Constructor for traditional-style patterns PLUS advanced
	 *  patterns ABC*DEF* and A*C*F.
	 * <p>
	 * If you don't know for sure the input is generic or valid, after
	 *   constructing call:
	 *   <sl>
	 *    <li> {@link #isValid()} to determine if given generic name was valid. </li>
	 *    <li> {@link #isGeneric()} to determine if given generic name had a wildcard. </li>
	 *   </sl>
	 * <p>
	 * If curious, you can also subsequently call:
	 *   <sl>
	 *     <li> {@link #getPatternType()} to determine which type of pattern the generic name follows. </li>
	 *   </sl>
	 * <p>
	 * Once constructed for a valid name, you can call 
	 *   <sl>
	 *     <li> {@link #matches(String)} for each name to see if it matches this generic name pattern.
	 *   </sl>
     * <p>
     * Quoted names are supported.
	 *
	 * @param genericName generic name to do pattern matching for (ie, ABC*DEF)
	 * @param advanced true if you want to support the advanced patterns.
	 * @param caseSensitive true if the names are case-sensitive, false if case insensitive
	 */
	public NamePatternMatcher(String genericName, boolean advanced, boolean caseSensitive)    
	{
		  this.caseSensitive = caseSensitive;
		  if (genericName == null)
			  genericName = "*";
		  int len = 0;
		  // determine if given a null name
		  if ((genericName == null) || (genericName.length()==0))
		    validName = false;
		  else
		    validName = true; // for now
		  if (validName)
		  {
		  	 len = genericName.length();
		     // determine if given generic name is a quoted name
		     quotedName = genericName.charAt(0) == QUOTE;
		     if (quotedName && ((len==1) || (genericName.charAt(len-1)!=QUOTE)))
		       validName = false;
		     if (!quotedName && !caseSensitive)
		       genericName = genericName.toLowerCase();
		  }
		  if (validName)
		  {
		     // change *BLANK into 10 blanks
		     if (genericName.equals("*BLANK"))
		       genericName = "          ";
		     // away we go...
		     int firstCharPos = quotedName ? 1 : 0;
		     int lastCharPos = quotedName ? len-2 : len-1;
		     char firstChar = genericName.charAt(firstCharPos);
		     char lastChar = genericName.charAt(lastCharPos);    	     
		     // determine type of generic name. 6 flavors including SCALAR...
		     int wildcardOccurrences = countOccurrencesOf(genericName, WILDCARD);
		     if (wildcardOccurrences==0)
		     {
		        patternType = SCALAR;
		     }
		     else if (wildcardOccurrences==1)
		     {
		  	     if ((!quotedName && (len == 1)) || (quotedName && (len ==3)) || genericName.equals("*ALL") || genericName.equals("\"*ALL\"") || genericName.equals("*LIBL"))
		  	       patternType = ALL;
		  	     else if (firstChar == WILDCARD)
		  	     {
		  	       patternType = WILDCARD_START;
		  	       part2 = genericName.substring(firstCharPos+1,lastCharPos+1);
		  	     }
		  	     else if (lastChar == WILDCARD)
		  	     {
		  	       patternType = WILDCARD_END;
		  	       part1 = genericName.substring(firstCharPos,lastCharPos);
		  	     }
		  	     else 
		  	     {
		  	       patternType = WILDCARD_MIDDLE;
		  	       int wcPos = genericName.indexOf(WILDCARD);
		  	       part1 = genericName.substring(firstCharPos,wcPos);
		  	       part2 = genericName.substring(wcPos+1,lastCharPos+1);
		  	     }
		     }
		     else if (wildcardOccurrences==2)
		     {
		     	 if (!advanced && (lastChar != WILDCARD) && (firstChar != WILDCARD))
		     	   validName = false;
		     	 else if (genericName.indexOf(WILDCARD_DOUBLED) >= 0)
		     	   validName = false;
		     	 else if ((firstChar == WILDCARD) && (lastChar == WILDCARD)) // pdm-style
		     	 {
		     	   patternType = WILDCARD_START_END;
		     	   part1 = genericName.substring(firstCharPos+1,lastCharPos);
		     	 }
		     	 else if (lastChar == WILDCARD) // advanced: A*C*
		     	 {
		     	    patternType = WILDCARD_MIDDLE_END;
		     	    int wcPos = genericName.indexOf(WILDCARD);
		     	    part1 = genericName.substring(firstCharPos,wcPos);
		     	    part2 = genericName.substring(wcPos+1,lastCharPos);
		     	    part1len = part1.length();
		     	    part2len = part2.length();    	     	    
		     	    part12len = part1len + part2len;
		     	 }
		     	 else if (firstChar == WILDCARD) // advanced: *B*C
		     	 {
		     	    patternType = WILDCARD_START_MIDDLE;
		     	    int wcPos = genericName.lastIndexOf(WILDCARD);
		     	    part1 = genericName.substring(firstCharPos+1,wcPos);
		     	    part2 = genericName.substring(wcPos+1,lastCharPos+1);
		     	    part1len = part1.length();
		     	    part2len = part2.length();    	  
		     	    part12len = part1len + part2len;
		     	 }
		     	 else // advanced: A*C*F
		     	 {
		     	    patternType = WILDCARD_MIDDLE_MIDDLE;
		     	    int wcPos1 = genericName.indexOf(WILDCARD);
		     	    int wcPos2 = genericName.lastIndexOf(WILDCARD);    	     	    
		     	    part1 = genericName.substring(firstCharPos,wcPos1);
		     	    part2 = genericName.substring(wcPos1+1,wcPos2);
		     	    part3 = genericName.substring(wcPos2+1,lastCharPos+1);
		     	    part1len = part1.length();
		     	    part2len = part2.length();    	  
		     	    part12len = part1len + part2len;
		     	    part123len = part12len + part3.length();    	     	    
		     	 }    	     	     	     	 
		     }  
		     else
		       validName = false;
		  }
		  this.genericName = genericName;
	}
	/**
	 * Test if a host name matches the pattern of this generic name.
	 * @param input Scalar name like ABCDEF
	 * @return true if given name matches this generic name pattern.
	 */
   public boolean matches(String input)
   {
	   boolean matches = false;
	   if (validName)
	   {
	   	 if ((input.length()>2) && 
	   	     (input.charAt(0) == QUOTE))
	   	   input = input.substring(1,input.length()-1);
	   	 else if (!caseSensitive)
	   	   input = input.toLowerCase();
		 switch (patternType)
		 { 
	   	    case SCALAR:
	   	          matches = input.equals(genericName);
	   	          break;
	   	    case ALL:
	   	          matches = true;
	   	          break;
	   	    case WILDCARD_END:
	   	          matches = input.startsWith(part1);
	   	          break;
	   	    case WILDCARD_START:
	   	          matches = input.endsWith(part2);
	   	          break;
	   	    case WILDCARD_MIDDLE:
	   	          matches = input.startsWith(part1) && input.endsWith(part2);
	   	          break;
	   	    case WILDCARD_START_END:
	   	          matches = (input.indexOf(part1) >= 0);
	   	          break;
	   	    case WILDCARD_MIDDLE_END:
	   	          if (input.startsWith(part1) && (input.length()>=part12len))
	   	            matches = (input.indexOf(part2,part1len) >= 0);
	   	          else
	   	            matches = false;
	   	          break;
	   	    case WILDCARD_START_MIDDLE:  // *B*F
	   	          if (input.endsWith(part2) && (input.length()>=part12len))
	   	          {
	   	          	int idx = input.indexOf(part1);       	   
	   	          	int startOfEndPart = input.length() - part2len;       	
	   	            matches = ((idx >= 0) && (idx+part1len <= startOfEndPart));
	   	          }
	   	          else
	   	            matches = false;
	   	          break;
	   	    case WILDCARD_MIDDLE_MIDDLE: // A*C*D
	   	          if (input.startsWith(part1) && input.endsWith(part3) && (input.length()>=part123len))
	   	          {
	   	          	int idx = input.indexOf(part2);       	          	          	
	   	          	int startOfEndPart = input.length() - part3len;              	          	
	   	            matches = ((idx >= 0) && (idx >= part1len) && (idx <= startOfEndPart));
	   	          }
	   	          else
	   	            matches = false;
	   	          break;
		 }
	   }
	   return matches;
   }      

	/**
	 * Was generic name given in the constructor a valid scalar or generic name?
	 * @return true if name contained 0, 1 or 2 wildcards.
	 */
	public boolean isValid()
	{
		  return validName;
	}
	/**
	 * Was generic name given in the constructor a valid generic name (one or 2 '*'s)?
	 */
	public boolean isGeneric()
	{
		  return patternType != SCALAR;
	}
	/**
	 * Was quoted name given in the constructor a quoted name like "abcDEF"?
	 */
	public boolean isQuoted()
	{
		  return quotedName;
	}    
	/**
	 * What type of pattern is it? One of:
	 *  {@link #SCALAR}, {@link #ALL}, {@link #WILDCARD_END}, {@link #WILDCARD_START}, {@link #WILDCARD_MIDDLE},
	 *  {@link #WILDCARD_START_END}, or {@link #WILDCARD_MIDDLE_END}
	 */
	public int getPatternType()
	{
		  return patternType;
	}
	/**
	 * Helper method.
	 * Count occurrences of given character in given string.
	 * Does NOT take into account quoted names.
	 */
	private static int countOccurrencesOf(String haystack, char needle)
	{
		int count  = 0;
		for (int idx=0; idx<haystack.length(); idx++)
		{
			if (haystack.charAt(idx) == needle)
			 ++count;
		}
		return count;
	}            
    
	/** 
	 * For writing this object out.
	 * Writes out the original generic name specified in the constructor.
	 */
   public String toString()
   {
	   return genericName;
   }      
	/** 
	 * For debugging/testing purposes.
	 * Writes out information about this generic name to the given stream file.
	 */
   public void writeInfo(PrintWriter stream)
   {
		 stream.println("GENERIC NAME: " + genericName);
		 stream.println(" isValid: " + isValid());
		 stream.println(" isGeneric: " + isGeneric());
		 stream.println(" isQuoted: " + isQuoted());
		 int type = getPatternType();
		 int typeidx = 0;
		 boolean match=false;
		 for (int idx=0; !match && (idx<TYPES_IDX.length); idx++)
			if (type == TYPES_IDX[idx])
			{
				typeidx = idx;
				match = true;
			}
		 stream.println(" patternType: " + TYPES[typeidx]);
		 if (part1 != null)
		   stream.println(" part1: " + part1);         
		 if (part2 != null)
		   stream.println(" part2: " + part2);   
		 if (part3 != null)
		   stream.println(" part3: " + part3);
		 stream.println();
		 stream.flush();
   }         
   
   /**
    * Static method to test if a given pattern is a valid generic name
    * @param genericName pattern to test
    * @param advanced true if advanced pattern allowed: ABC*DEF* and A*C*F
    * @true if there are no obvious syntactical errors, like two asterisks together.
    */
   public static boolean verifyPattern(String genericName, boolean advanced)
   {
   	   if (genericName != null)
	     genericName = genericName.trim();
	   else 
	     return false;
	   // determine if given a null name
	   if (genericName.length()==0)
		  return false;
	   int len = genericName.length();
	   // determine if given generic name is a quoted name
	   boolean quotedName = (genericName.charAt(0) == QUOTE);   
	   if (quotedName && ((len==1) || (genericName.charAt(len-1)!=QUOTE)))
		 return false;
	   int wildcardOccurrences = countOccurrencesOf(genericName, WILDCARD);
	   if (wildcardOccurrences==2)
	   {
		  char firstChar = genericName.charAt((quotedName ? 1 : 0));
		  char lastChar = genericName.charAt((quotedName ? (len-2) : (len-1)));    	     
	 	  if (!advanced && (lastChar != WILDCARD) && (firstChar != WILDCARD))
	 	    return false;
	  	  else if (genericName.indexOf(WILDCARD_DOUBLED) >= 0)
	 	    return false;
	   }  
	   else if (wildcardOccurrences > 2)
	     return false;
	   return true;
   }
}