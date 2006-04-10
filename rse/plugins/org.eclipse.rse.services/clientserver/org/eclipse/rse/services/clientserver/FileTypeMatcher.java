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

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class offers file type matching. A file type is the extension part of a file
 * name, minus the dot. For example, "java" and "class" are types.
 * <p>
 * Recently added is support for full file names, such as "manifest.mf", and even 
 * extension-less names which use the placeholder ".null" for the extension, as in
 * "makefile.null" so as to dis-ambiguate from extensions like "cpp".
 * <p>
 * This supports being given a list of file types, via the constructor, to match on. It
 * will return true from the {@link #matches(String)} method if the given file name ends 
 * in an extension that matches one of the given types.
 * <p>
 * By default, all matching is done case-insensitive, but this can be overridden in the 
 * constructor or by calling {@link #setCaseSensitive(boolean)}.
 * <p>
 * For file name matching, use {@link org.eclipse.rse.services.clientserver.NamePatternMatcher} instead.
 */
public class FileTypeMatcher implements IMatcher
{
	
	/**
	 * The delimiter that starts the extension part
	 */	    
	public static final char SEP_EXTENSION = '.';    
    private String[] orgTypes, types;
    private String[] orgNames, names;
    private boolean  caseSensitive = false;
    
	/**
	 * Constructor for case-insensitive matching
     * 
	 * @param types Array of file types to match on. These should not include the dot. Eg, "java"
	 */
	public FileTypeMatcher(String[] types)    
	{
		  this(types, false);
	}
	/**
	 * Constructor when specifying if matching is case-sensitive or not
     * 
	 * @param types Array of file types to match on. These should not include the dot. Eg, "java"
	 * @param caseSensitive true if to consider case when matching
	 */
	public FileTypeMatcher(String[] types, boolean caseSensitive)    
	{
		setTypes(types);
		setCaseSensitive(caseSensitive);
	}

    /** 
     * Reset the types used to match on
     */
    public void setTypes(String[] types)
    {
    	this.types = types;
    	this.orgTypes = types;
    	setCaseSensitive(caseSensitive);
    }
    /** 
     * Reset the types and names used to match on
     */
    public void setTypesAndNames(String[] types, String[] names)
    {
    	this.types = types;
    	this.orgTypes = types;
    	this.names = names;
    	this.orgNames = names;
    	setCaseSensitive(caseSensitive);
    }
    /** 
     * Reset the types and names used to match on
     */
    public void setTypesAndNames(String[] typesAndNames)
    {
    	Vector typesVector = new Vector();
    	Vector namesVector = new Vector();
    	for (int idx=0; idx<typesAndNames.length; idx++)
    	{
    	    if (typesAndNames[idx].indexOf('.')!=-1) // this is a full name!
    	      namesVector.add(typesAndNames[idx]);
    	    else
    	      typesVector.add(typesAndNames[idx]);
    	}    	
    	this.orgTypes = new String[typesVector.size()];
    	this.orgNames = new String[namesVector.size()];
    	for (int idx=0; idx<orgTypes.length; idx++)
    	  orgTypes[idx] = (String)typesVector.elementAt(idx);
    	for (int idx=0; idx<orgNames.length; idx++)
    	  orgNames[idx] = (String)namesVector.elementAt(idx);
    	
    	this.types = orgTypes;
    	this.names = orgNames;

    	setCaseSensitive(caseSensitive);
    }

    /**
     * Return the types used to match on.
     * @see #toString()
     */
    public String[] getTypes()
    {
    	return orgTypes;
    }
    /**
     * Return the names used to match on.
     * @see #toString()
     */
    public String[] getNames()
    {
    	return orgNames;
    }

    /**
     * Set whether case should be considered when matching
     */
    public void setCaseSensitive(boolean caseSensitive)
    {
    	this.caseSensitive = caseSensitive;
    	if (!caseSensitive && (types!=null))
    	{
    		types = new String[orgTypes.length];
    		for (int idx=0; idx<types.length; idx++)
    		   types[idx] = orgTypes[idx].toLowerCase();
    	}
    	else
    	  types = orgTypes;
    	if (!caseSensitive && (names!=null))
    	{
    		names = new String[orgNames.length];
    		for (int idx=0; idx<names.length; idx++)
    		   names[idx] = orgNames[idx].toLowerCase();
    	}
    	else
    	  names = orgNames;
    }
    /**
     * Return whether case should be considered when matching
     */
    public boolean isCaseSensitive()
    {
    	return caseSensitive;
    }

	/**
	 * Test if a host name matches the pattern of this generic name.
	 * @param fileName file name such as MyClass.java
	 * @return true if given extension part of name matches any of the given types
     * @see #setCaseSensitive(boolean)
	 */
   public boolean matches(String fileName)
   {
	   boolean matches = false;
	   String type = null;
	   if (fileName != null)
	   {
	   	 // check for a match of the given file's name with any of the input fullnames
	   	 if (names != null)
	   	 {
	        String name = null;
	   	    if (!caseSensitive)
	   	      name = fileName.toLowerCase(); 
	   	    else
	   	      name = fileName;
	   	    for (int idx=0; !matches && (idx<names.length); idx++)
	   	    {
	   	 	   if (name.equals(names[idx]))	   	 	   
	   	 	      matches = true;	   	 	
	   	 	   else if (names[idx].endsWith(".null") &&
	   	 	             name.equals(names[idx].substring(0,names[idx].indexOf(".null"))) )
	   	 	      matches = true;
	   	    }
	   	    if (matches)
	   	      return true;
	   	 }
	   	 
	   	 // check for a match of the given file's extension with any of the input extensions
	   	 int dotIdx = fileName.lastIndexOf(SEP_EXTENSION);
	   	 int lastPos = fileName.length() - 1;
	   	 if ((dotIdx >= 0) && (dotIdx < lastPos))
	   	 {
	   	 	if (!caseSensitive)
	   	 	  type = fileName.substring(dotIdx+1).toLowerCase(); // strip off
	   	 	else
	   	 	  type = fileName.substring(dotIdx+1);
	   	 	for (int idx=0; !matches && (idx<types.length); idx++)
	   	 	   if (type.equals(types[idx]))
	   	 	     matches = true;	   	 	
	   	 }
	   	 // check for an extenion-less name, and if so see if one of the types is the special-case "null" type...
	   	 else if (dotIdx == -1)
	   	 {
	   	 	for (int idx=0; !matches && (idx<types.length); idx++)
	   	 	   if ("null".equals(types[idx]))
	   	 	     matches = true;	   	 		   	    	
	   	 }
	   }  
	   return matches;
   }      

	/** 
	 * For writing this object out.
	 * Writes out the list of types given in the constructor, as a comma-separated list.
	 * This is in the format directly usable in org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString
	 */
   public String toString()
   {
   	   StringBuffer typesBuffer = new StringBuffer("");
   	   if (orgTypes != null)
   	   {
   	     for (int idx=0; idx<orgTypes.length; idx++)
   	        typesBuffer.append(orgTypes[idx]+","); 
   	   }
   	   if (orgNames != null)
   	   {
   	     for (int idx=0; idx<orgNames.length; idx++)
   	        typesBuffer.append(orgNames[idx]+","); 
   	   }
	   return typesBuffer.toString();
   }      

	/**
	 * Parse a comma-separated list of strings into an array of strings representing
	 *  extensions. This ignores any full-names in the input list, and only parses
	 *  out the extensions...
	 */
	public static String[] parseTypes(String typeList)
	{
		StringTokenizer tokens = new StringTokenizer(typeList,",");
		Vector v = new Vector();
		while (tokens.hasMoreTokens())
		{
			String token = tokens.nextToken().trim();
			if ((token.length()>0) &&
			    (token.indexOf('.') == -1))
			  v.addElement(token);
		}
		String[] types = new String[v.size()];
		for (int idx=0; idx<v.size(); idx++)
		   types[idx] = (String)v.elementAt(idx);
		return types;
	}
	
	/**
	 * Parse a comma-separated list of strings into an array of full-names. This ignores
	 * the extensions and only parses out the full names.
	 */
	public static String[] parseNames(String typeList)
	{
		StringTokenizer tokens = new StringTokenizer(typeList,",");
		Vector v = new Vector();
		while (tokens.hasMoreTokens())
		{
			String token = tokens.nextToken().trim();
			if ((token.length()>0) &&
			    (token.indexOf('.') != -1))
			  v.addElement(token);
		}
		String[] names = new String[v.size()];
		for (int idx=0; idx<v.size(); idx++)
		   names[idx] = (String)v.elementAt(idx);
		return names;
	}

}