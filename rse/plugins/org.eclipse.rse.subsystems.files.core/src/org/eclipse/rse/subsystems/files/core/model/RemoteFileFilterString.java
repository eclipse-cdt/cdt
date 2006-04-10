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

package org.eclipse.rse.subsystems.files.core.model;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;


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
public class RemoteFileFilterString implements Cloneable
{
	protected String path, file;
	protected String[] types;
	protected boolean subdirs, files, filterByTypes;
	//private RemoteFileSubSystemFactory subsysFactory;
	protected String PATH_SEP = java.io.File.separator;
	public static final char TYPE_SEP = ',';
	public static final String TYPE_SEP_STRING = ",";
	public static final String SWITCH_NOSUBDIRS = " /ns";
	public static final String SWITCH_NOFILES = " /nf";    
	
	/**
	 * Constructor to use for a filter to list roots when used absolutely, or list all contents
	 * when used relatively.
	 */
	public RemoteFileFilterString()
	{
		file = "*";
		subdirs = true;
		files = true;
	}
	/**
	 * Constructor to use when there is no existing filter string.
	 * <p>
	 * This constructor is better that the default constructor, because it takes a remote file subsystem
	 *  factory, from which we can query the folder and path separator characters, which are needed
	 *  to parse and re-build filter strings.
	 * @see #setPath(String)
	 * @see #setFile(String)
	 * @see #setTypes(String[])
	 */
	public RemoteFileFilterString(IRemoteFileSubSystemConfiguration subsysFactory)
	{
		PATH_SEP = subsysFactory.getSeparator();
		file = "*";    	
		subdirs = true;
		files = true;
	}
	/**
	 * Constructor to use when an absolute filter string already exists.
	 */    
	public RemoteFileFilterString(IRemoteFileSubSystemConfiguration subsysFactory, String input)
	{
		this(subsysFactory);
		parse(null, input);    	
	}
	/**
	 * Constructor to use when you have a path and filename filter or comma-separated file types list.
	 * In the latter case, the last char must be a TYPE_SEP or comma, even for a single type.
	 */    
	public RemoteFileFilterString(IRemoteFileSubSystemConfiguration subsysFactory, String path, String input)
	{
		this(subsysFactory);
		parse(path, input);
	}
	
	public void setSubSystemFactory(IRemoteFileSubSystemConfiguration subsysFactory)
	{
		PATH_SEP = subsysFactory.getSeparator();
	}
    /**
     * Set the file name filter. You either call this or setTypes!
     */	
	public void setFile(String obj)
	{
		file = obj;
	}
    /**
     * Set the path to list files and/or folders in
     */	
	public void setPath(String path)
	{
		this.path = path;
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
	 * Allow files?
	 */
	public void setShowFiles(boolean set)
	{
		files = set;
	}
	/**
	 * Allow subdirs?
	 */
	public void setShowSubDirs(boolean set)
	{
		subdirs = set;
	}
	
    /**
     * Get the file name filter
     */		
	public String getFile()
	{
		return file;
	}
	/**
	 * Return the file part of the filter string, without the path.
	 * This is either the file name filter or a comma-separated list of types if this
	 * is a file type filter string.
	 */
	public String getFileOrTypes()
	{
		if (!filterByTypes || (types==null))
		  return file;
		else
		{
			return getTypesString(types);
		}
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
     * Get the path to list files and/or folders in
     */	
	public String getPath()
	{
		return path;
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
	 * Subdirs allowed?
	 */
	public boolean getShowFiles()
	{
		return files;
	}
	/**
	 * Subdirs allowed?
	 */
	public boolean getShowSubDirs()
	{
		return subdirs;
	}
	/**
	 * Return true if this filter string filters by file types versus by file name
	 */
	public boolean getFilterByTypes()
	{
		return filterByTypes;
	}
	
	/**
	 * This filter string represent a list-roots filter string?
	 */
	public boolean listRoots()
	{
		//return toStringNoSwitches().equals("*");
		return (path==null || path.equals("/"));
	}

	/**
	 * This filter string represent a list-files-in-root filter string?
	 */
	public boolean listRoot()
	{
		return toStringNoSwitches().equals("/*");
	}
	
	
	/**
	 *
	 */
	protected void parse(String inputPath, String input)
	{
		int idx = input.indexOf(SWITCH_NOSUBDIRS);
		if (idx >= 0)
		{
		    subdirs = false;
		    input = input.substring(0,idx);
		}
		else
		  subdirs = true;    	
		idx = input.indexOf(SWITCH_NOFILES);
		if (idx >= 0)
		{
		    files = false;
		    input = input.substring(0,idx);
		}
		else
		  files = true;
		if (inputPath != null)
		{
		  path = inputPath;
		  parseFileOrTypes(input); // file = input;
		}
		else
		{
          int pathidx = input.lastIndexOf(PATH_SEP);
          if (pathidx == 0)
          {
          	// hmm, we have been given say \*.java -> what does this mean? On Windows, it
          	// would mean "*.java" in the current drive, whereas on Unix/Linux it would
          	// mean "*.java" in the root directory. 
          	// For now, we are going with the Unix interpretation, and see if that leads
          	// to any problems on Windows, which should not allow such a filter anyway, unless
          	// it is absolute.
          	//path = null;
          	path = PATH_SEP;
          	if (input.length()>1)
          	  //file = input.substring(1); // from the 2nd char on
          	  parseFileOrTypes(input.substring(1)); // from the 2nd char on
          	else
          	  file = "*";
          }
          else if (pathidx>0)
          {
          	path = input.substring(0,pathidx);
          	if (path.endsWith(":")) // special case: eg, given e:
          	  path = path + PATH_SEP; // need it to be e:\ !!
          	if (pathidx == (input.length()-1))
          	  file = "*";
          	else
          	  //file = input.substring(pathidx+1);
          	  parseFileOrTypes(input.substring(pathidx+1));
          } 			
          else
          {
          	path = null;
          	//file = input;
          	parseFileOrTypes(input);
          }
		}
		///File fileObj = (path==null)? new File(input) : new File(path, input);
		//this.path = fileObj.getParent();
		//if (this.path == null)
		 //this.path = fileObj.getAbsolutePath(); // happens for root drives
		//this.file = fileObj.getName();    	
	}
    /**
     * Parse the non-folder part of the filter string. Will either be a 
     *  generic name or comma-separated list of types.
     */
    protected void parseFileOrTypes(String filter)
    {
        filterByTypes = false;
        types = null;        
    	if ((filter == null) || (filter.length()==0))
    	{
    		file = "*";    		
    	}
    	else if (filter.endsWith(TYPE_SEP_STRING))
    	{
    		types = parseTypes(filter);
    		filterByTypes = true;
    	}
    	else
    	  file = filter;
    }
	/**
	 * Parse a comma-separated list of strings into an array of strings
	 */
	public static String[] parseTypes(String typeList)
	{
		StringTokenizer tokens = new StringTokenizer(typeList,TYPE_SEP_STRING);
		Vector v = new Vector();
		while (tokens.hasMoreTokens())
		{
			String token = tokens.nextToken().trim();
			if (token.length()>0)
			  v.addElement(token);
		}
		String[] types = new String[v.size()];
		for (int idx=0; idx<v.size(); idx++)
		   types[idx] = (String)v.elementAt(idx);
		return types;
	}

    /**
     * De-hydrate into a string capturing all the attributes
     */
	public String toString()
	{
		String fs = toStringNoSwitches();
		if (!getShowSubDirs())
		  fs += SWITCH_NOSUBDIRS;
		if (!getShowFiles())
		  fs += SWITCH_NOFILES;    	  
		return fs;
	}
	/**
	 * Return the filter as a string, without the switches for no-files, no-folders
	 */
	public String toStringNoSwitches()
	{
		String fs = null;
		
		// KM: defect 53009. Check where path is empty, so we don't add path separator to it
		if (path == null || path.length() == 0)
		  fs = getFileOrTypes();
		else if (!path.endsWith(PATH_SEP))
		  fs = path+PATH_SEP+getFileOrTypes();
		else
		  fs = path+getFileOrTypes();
		return fs;
	}	
    /**
     * Clone this into another filter string object with the same attributes.
     */
    public Object clone()
    {
   	    RemoteFileFilterString copy = new RemoteFileFilterString();
   	    copy.path = path;
   	    copy.file = file;
   	    copy.subdirs = subdirs;
   	    copy.files = files;
   	    copy.PATH_SEP = PATH_SEP;
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