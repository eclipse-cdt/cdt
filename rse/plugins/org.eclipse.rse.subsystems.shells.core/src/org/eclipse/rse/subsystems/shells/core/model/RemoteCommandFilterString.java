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

package org.eclipse.rse.subsystems.shells.core.model;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystemConfiguration;


/**
 * A string representing a filter sent to remote file subsystems.
 * <p>
 * Filters can be absolute or relative. Absolute contains a folder path, while relative do not.
 * <p>
 * The files can be filtered by name, or by a list of file types. If by name, it can have
 *  up to 2 asterisks anywhere in the name for wildcarding. If by types, multiple types can
 *  by specified. The types are the file extensions without the dot, as in "java" or "class".
 * <p>
 * Examples:
 * <ul>
 *  <li>Absolute file name filter: <code>d:\mystuff\phil*.abc</code>
 *  <li>Relative file name filter: <code>phil*.abc</code>
 *  <li>Absolute file type filter: <code>d:\mystuff\java,class,</code>
 *  <li>Relative file type filter: <code>java,class,</code>
 * <p>
 * Syntactically, file type filter strings have at least one comma.
 * Note that the file name filter string "*.java" is semantically the same
 *  as a type filter string "java,". Either one can be used and will get
 *  the same results. However, if you specify "java" you will be in trouble,
 *  as it will mean look for a file explicitly named "java".
 * <p>
 * It is invalid to have both a comma and an asterisk in the same filter string.
 * It is also invalid to have both a comma and a period in the same filter string.
 */
public class RemoteCommandFilterString implements Cloneable
{
	protected String shellStr;
	protected String[] types;
	protected boolean filterByTypes;
	
	/**
	 * Constructor to use for a filter to list roots when used absolutely, or list all contents
	 * when used relatively.
	 */
	public RemoteCommandFilterString()
	{
		shellStr = "*";
	}
	/**
	 * Constructor to use when there is no existing filter string.
	 * <p>
	 * This constructor is better that the default constructor, because it takes a remote file subsystem
	 *  factory, from which we can query the folder and path separator characters, which are needed
	 *  to parse and re-build filter strings.
	 * @see #setTypes(String[])
	 */
	public RemoteCommandFilterString(IRemoteCmdSubSystemConfiguration subsysFactory)
	{
		shellStr = "*";    	
	}
	/**
	 * Constructor to use when an absolute filter string already exists.
	 */    
	public RemoteCommandFilterString(IRemoteCmdSubSystemConfiguration subsysFactory, String input)
	{
		this(subsysFactory); 	
	}
	/**
	 * Constructor to use when you have a path and filename filter or comma-separated file types list.
	 * In the latter case, the last char must be a TYPE_SEP or comma, even for a single type.
	 */    
	public RemoteCommandFilterString(IRemoteCmdSubSystemConfiguration subsysFactory, String path, String input)
	{
		this(subsysFactory);
	}
	
	public void setSubSystemConfiguration(IRemoteCmdSubSystemConfiguration subsysFactory)
	{
	}
 
	/**
	 * Set the file types to subset by. These are extensions, without the dot, as
	 *  in java, class, gif, etc.
	 * You either call this or setFile!
	 */
	public void setTypes(String[] types)
	{
		this.types = types;		
		filterByTypes = (types != null);		
	}


    /**
     * Concatenate the given file types as a single string, each type comma-separated
     */
    public static String getTypesString(String[] typesArray)
    {
    	StringBuffer typesBuffer = new StringBuffer("");
    	for (int idx=0; idx<typesArray.length; idx++)
    	   typesBuffer.append(typesArray[idx]+",");
    	return typesBuffer.toString();
    }
    /**
     * For file types filters, returns the types as a string of concatenated types,
     * comma-delimited. For file name filters, returns null;
     */
    public String getTypesAsString()
    {
		if (!filterByTypes || (types==null))
		  return null;
		return getTypesString(types);
    }

	/**
	 * Get the types to subset by, if specified.
	 * Will be null if this is not a by-type filter string.
	 */
	public String[] getTypes()
	{
		return types;
	}

	/**
	 * Return true if this filter string filters by file types versus by file name
	 */
	public boolean getFilterByTypes()
	{
		return filterByTypes;
	}
	
	 /**
     * De-hydrate into a string capturing all the attributes
     */
	public String toString()
	{	 
		return shellStr;
	}
	
	 /**
     * Clone this into another filter string object with the same attributes.
     */
    public Object clone()
    {
   	    RemoteCommandFilterString copy = new RemoteCommandFilterString();
   	    copy.shellStr = shellStr;
   	    copy.filterByTypes = filterByTypes;
   	    if (types!=null)
   	    {
   	    	copy.types = new String[types.length];
   	    	for (int idx=0; idx<types.length; idx++)
   	    	   copy.types[idx] = types[idx]; // don't think we need to clone strings as they are immutable
   	    }
   	    return copy;
    }	
}