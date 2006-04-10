/********************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation. All rights reserved.
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
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;


/**
 * This class encapsulates all the criteria required to identify a match on a remote
 * system object, and the methods to determine if a given input meets that criteria.
 * <ol>
 * <li>subsystemconfigurationid. For scoping to remote objects for a given subsystem factory
 * <li>subsystemconfigurationCategory. For scoping to remote objects for a given subsystem factory category.
 * <li>systemtypes. For scoping to remote objects from systems of a given type, or comma-separated types.
 * <li>category. For scoping to remote objects of a given type category
 * <li>namefilter. For scoping to remote objects of a given name
 * <li>typefilter. For scoping to remote objects of a given type
 * <li>subtypefilter. For scoping to remote objects of a given subtype
 * <li>subsubtypefilter. For scoping to remote objects of a given sub-subtype
 * </ol>
 * <p>
 * The names given can be scalar or very simple generic (begin or end with an asterisk).
 * Occasionally, as with iSeries objects types, an asterisk is a valid part of the 
 *  name, as in *PGM. If "*PGM" is specified for the type filter, then it would match
 *  on other types too, like *SRVPGM. To solve this special case, users can specify
 *  the following to tell us that the asterisk is to be taken literally, versus as a
 *  leading or trailing wild card:
 * <pre><code>
 *  "%%ast.PGM" or "%%ast;PGM"
 * </code></pre>
 * <p>
 * The special symbol "%%ast." is resolved into an asterisk as part of the literal name.
 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter
 */
public class SystemRemoteObjectMatcher 
{
	public static final String STAR_SYMBOL = "%ast.";
	public static final String STAR_SYMBOL2 = "%ast;"; // really should have been this I think.
	public static final int    STAR_SYMBOL_LEN = 5;
    private String categoryfilter, subsystemfilter,subsystemCategoryFilter, systypesfilter, namefilter,typefilter,subtypefilter,subsubtypefilter;
    private String categoryfilterpart, subsystemfilterpart,subsystemCategoryFilterpart,namefilterpart,typefilterpart,subtypefilterpart,subsubtypefilterpart;
    private boolean allSSFCategories = false;
    private boolean allCategories = false;
    private boolean allNames = false;
    private boolean allTypes = false;    
    private boolean allSubTypes = false;
    private boolean allSubSubTypes = false;
    private boolean allSubSystems = false;    
    private boolean allSystemTypes = false;

    private boolean genericSSFCategoriesStart = false;
    private boolean genericCategoriesStart = false;
    private boolean genericNamesStart = false;
    private boolean genericTypesStart = false;    
    private boolean genericSubTypesStart = false;
    private boolean genericSubSubTypesStart = false;
    private boolean genericSubSystemStart = false;    
    
    private boolean genericSSFCategoriesEnd = false;
    private boolean genericCategoriesEnd = false;
    private boolean genericNamesEnd = false;
    private boolean genericTypesEnd = false;    
    private boolean genericSubTypesEnd = false;
    private boolean genericSubSubTypesEnd = false;
    private boolean genericSubSystemEnd = false;
    
    /**
     * Historical constructor that doesn't support "subsystemFactoryCategory" or "systemtypes".
     */
    public SystemRemoteObjectMatcher(String subsystemFactoryId, String categoryFilter,
                                     String nameFilter, String typeFilter,
                                     String subtypeFilter, String subsubtypeFilter)
    {
    	this(subsystemFactoryId, null, categoryFilter, null, nameFilter, typeFilter, subtypeFilter, subsubtypeFilter);
    }
    /**
     * Constructor that supports "subsystemFactoryCategory" and "systemtypes".
     */
    public SystemRemoteObjectMatcher(String subsystemFactoryId, String subsystemFactoryCategoryFilter, String categoryFilter,
                                     String systemTypes, String nameFilter, String typeFilter,
                                     String subtypeFilter, String subsubtypeFilter)
    {
    	this.subsystemCategoryFilter = subsystemFactoryCategoryFilter;
    	this.subsystemfilter = subsystemFactoryId;
    	this.systypesfilter = systemTypes;
        this.categoryfilter = categoryFilter;
    	this.namefilter = nameFilter;
    	this.typefilter = typeFilter;
    	this.subtypefilter = subtypeFilter;
    	this.subsubtypefilter = subsubtypeFilter;

    	if ((systypesfilter == null) || (systypesfilter.length()==0))
    	  systypesfilter = "*";
    	if ((subsystemCategoryFilter == null) || (subsystemCategoryFilter.length()==0))
    	  subsystemCategoryFilter = "*";
    	if (categoryfilter == null)
    	  categoryfilter = "*";
    	if (namefilter == null)
    	  namefilter = "*";
    	if (typefilter == null)
    	  typefilter = "*";
    	if (subtypefilter == null)
    	  subtypefilter = "*";
    	if (subsubtypefilter == null)
    	  subsubtypefilter = "*";
    	if (subsystemfilter == null)
    	  subsystemfilter = "*";

    	this.allSSFCategories  = subsystemCategoryFilter.equals("*");
    	this.allCategories  = categoryfilter.equals("*");
    	this.allSystemTypes = systypesfilter.equals("*");    	
    	this.allNames       = namefilter.equals("*");
    	this.allTypes       = typefilter.equals("*");
    	this.allSubTypes    = subtypefilter.equals("*");
    	this.allSubSubTypes = subsubtypefilter.equals("*");
    	this.allSubSystems  = subsystemfilter.equals("*");
    	
    	// --------------------------------------------------
    	// determine if the name starts or ends with asterisk
    	// --------------------------------------------------
    	this.genericSSFCategoriesStart = !allSSFCategories  && startsWithAsterisk(subsystemCategoryFilter);    	
    	this.genericCategoriesStart    = !allCategories     && startsWithAsterisk(categoryfilter);    	
    	this.genericNamesStart         = !allNames          && startsWithAsterisk(namefilter);
    	this.genericTypesStart         = !allTypes          && startsWithAsterisk(typefilter);
    	this.genericSubTypesStart      = !allSubTypes       && startsWithAsterisk(subtypefilter);
    	this.genericSubSubTypesStart   = !allSubSubTypes    && startsWithAsterisk(subsubtypefilter);
    	this.genericSubSystemStart     = !allSubSystems     && startsWithAsterisk(subsystemfilter);

    	this.genericSSFCategoriesEnd = !allSSFCategories  && endsWithAsterisk(subsystemCategoryFilter);    	
    	this.genericCategoriesEnd    = !allCategories     && endsWithAsterisk(categoryfilter);
    	this.genericNamesEnd         = !allNames          && endsWithAsterisk(namefilter);
    	this.genericTypesEnd         = !allTypes          && endsWithAsterisk(typefilter);
    	this.genericSubTypesEnd      = !allSubTypes       && endsWithAsterisk(subtypefilter);
    	this.genericSubSubTypesEnd   = !allSubSubTypes    && endsWithAsterisk(subsubtypefilter);
    	this.genericSubSystemEnd     = !allSubSystems     && endsWithAsterisk(subsystemfilter);

    	if (genericSSFCategoriesStart)
    	  subsystemCategoryFilterpart   = stripLeadingAsterisk(subsystemCategoryFilter);   // strip off asterisk
    	if (genericCategoriesStart)
    	  categoryfilterpart   = stripLeadingAsterisk(categoryfilter);   // strip off asterisk
    	if (genericNamesStart)
    	  namefilterpart       = stripLeadingAsterisk(namefilter);       // strip off asterisk
    	if (genericTypesStart)
    	  typefilterpart       = stripLeadingAsterisk(typefilter);       // strip off asterisk
    	if (genericSubTypesStart)
    	  subtypefilterpart    = stripLeadingAsterisk(subtypefilter);    // strip off asterisk
    	if (genericSubSubTypesStart)
    	  subsubtypefilterpart = stripLeadingAsterisk(subsubtypefilter); // strip off asterisk
    	if (genericSubSystemStart)
    	  subsystemfilterpart  = stripLeadingAsterisk(subsystemfilter);  // strip off asterisk

    	if (genericSSFCategoriesEnd)
    	  subsystemCategoryFilterpart   = stripTrailingAsterisk(subsystemCategoryFilter);  // strip off asterisk
    	if (genericCategoriesEnd)
    	  categoryfilterpart   = stripTrailingAsterisk(categoryfilter);  // strip off asterisk
    	if (genericNamesEnd)
    	  namefilterpart       = stripTrailingAsterisk(namefilter);      // strip off asterisk
    	if (genericTypesEnd)
    	  typefilterpart       = stripTrailingAsterisk(typefilter);      // strip off asterisk
    	if (genericSubTypesEnd)
    	  subtypefilterpart    = stripTrailingAsterisk(subtypefilter);   // strip off asterisk
    	if (genericSubSubTypesEnd)
    	  subsubtypefilterpart = stripTrailingAsterisk(subsubtypefilter);// strip off asterisk
    	if (genericSubSystemEnd)
    	  subsystemfilterpart  = stripTrailingAsterisk(subsystemfilter); // strip off asterisk

    	// --------------------------------------------------
    	// resolve '\*' escape characters
    	// --------------------------------------------------
        subsystemCategoryFilter   = resolveSymbols(subsystemCategoryFilter);
        categoryfilter   = resolveSymbols(categoryfilter);
        namefilter       = resolveSymbols(namefilter);
        typefilter       = resolveSymbols(typefilter);
        subtypefilter    = resolveSymbols(subtypefilter);
        subsubtypefilter = resolveSymbols(subsubtypefilter);
        subsystemfilter  = resolveSymbols(subsystemfilter);
    }
    
    /**
     * Helper method.
     * Returns true if given name starts with an asterisk.
     */
    protected boolean startsWithAsterisk(String name)
    {
    	return name.startsWith("*");
    }
    /**
     * Helper method.
     * Returns true if given name ends with an asterisk.
     */
    protected boolean endsWithAsterisk(String name)
    {
    	return name.endsWith("*");
    }
    /**
     * Helper method.
     * Strips off the leading asterisk.
     */
    protected String stripLeadingAsterisk(String name)
    {
    	return resolveSymbols(name.substring(1));
    }
    /**
     * Helper method.
     * Strips off the trailing asterisk.
     */
    protected String stripTrailingAsterisk(String name)
    {
    	return resolveSymbols(name.substring(0, name.length()-1));
    }
    /**
     * Occasionally, as with iSeries objects types, an asterisk is a valid part of the 
     *  name, as in *PGM. If "*PGM" is specified for the type filter, then it would match
     *  on other types too, like *SRVPGM. To solve this special case, users can specify
     *  the following to tell us that the asterisk is to be taken literally, versus as a
     *  leading or trailing wild card:<br>
     * <pre><code>
     *  "%%ast.PGM" or "%%ast;PGM"
     * </code></pre>
     * <p>
     * The special symbol "%%ast." is resolved into an asterisk as part of the literal name.
     */
    protected String resolveSymbols(String name)
    {
    	// yantzi:5.1.2 workaround for eclipse bug 49312
    	if (name.startsWith("%%ast.") || name.startsWith("%%ast;"))
    	{
    		// eclipse should have stripped the leading % off but doesn't because of this bug
    		name = name.substring(1);
    	}
    	
    	int symbolLength = STAR_SYMBOL_LEN;
    	int symbolIndex = name.indexOf(STAR_SYMBOL);
    	if (symbolIndex == -1)
    	   symbolIndex = name.indexOf(STAR_SYMBOL2);
    	boolean hasSymbols = (symbolIndex != -1);
    	boolean hadSymbols = hasSymbols;
    	boolean debug = false;
    	if (hasSymbols && debug)
          System.out.println("Before: " + name);
    	while (hasSymbols)
    	{
    		if (symbolIndex == 0)
    		{
              // "&amp.abc"
    		  if (name.length() > symbolLength)
    		    name = "*" + name.substring(symbolLength);
              // "&amp."
    		  else
    		    name = "*";
    		}
    		else if ((symbolIndex+symbolLength) < name.length())
    		{
                // "abc&amp.def"
                // "01234567890"    			
    			String part1 = name.substring(0,symbolIndex); // up to symbol
    			String part2 = name.substring(symbolIndex+symbolLength); // after symbol 
    			name = part1 + "*" + part2;
    		}
    		else
    		{
                // "abc&amp."
                // "01234567"    			
    			String part1 = name.substring(0,symbolIndex); // up to symbol
    			name = part1 + "*";
    		}
    		symbolIndex = name.indexOf(STAR_SYMBOL);
    		if (symbolIndex == -1)
    		  symbolIndex = name.indexOf(STAR_SYMBOL2);    		
    		hasSymbols = (symbolIndex != -1);
    	}
    	if (hadSymbols && debug)
          System.out.println("After: " + name);
    	return name;
    }
    
    /**
     * Getter method.
     * Return what was specified for the <samp>subsystemconfigurationCategory</samp> xml attribute.
     */
    public String getSubSystemFactoryCategoryFilter()
    {
    	return subsystemCategoryFilter;
    }        
    /**
     * Getter method.
     * Return what was specified for the <samp>systemtypes</samp> xml attribute.
     */
    public String getSystemTypesFilter()
    {
    	return systypesfilter;
    }        
    
    /**
     * Getter method.
     * Return what was specified for the <samp>typecategoryfilter</samp> xml attribute.
     */
    public String getCategoryFilter()
    {
    	return categoryfilter;
    }        
    /**
     * Getter method.
     * Return what was specified for the <samp>namefilter</samp> xml attribute.
     */
    public String getNameFilter()
    {
    	return namefilter;
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>typefilter</samp> xml attribute.
     */
    public String getTypeFilter()
    {
    	return typefilter;
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>subtypefilter</samp> xml attribute.
     */
    public String getSubTypeFilter()
    {
    	return subtypefilter;
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>subsubtypefilter</samp> xml attribute.
     */
    public String getSubSubTypeFilter()
    {
    	return subsubtypefilter;
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>subsystemconfigurationid</samp> xml attribute.
     */
    public String getSubSystemFactoryId()
    {
    	return subsystemfilter;
    }    

    /**
     * Given an ISystemRemoteElement, return true if that element
     * meets this criteria.
     */
    public boolean appliesTo(ISystemRemoteElementAdapter adapter, Object element)
    {
        boolean applies = true;
        // must match on all attributes to apply

        // -----------------------------------
        // check for match on subsystem filter
        // -----------------------------------
        boolean subsystemMatch = true;
        if (!allSubSystems)
        {
        	String subsystem = adapter.getSubSystemFactoryId(element);
        	if (subsystem == null)
        	  subsystemMatch = false;
            else if (!genericSubSystemStart && !genericSubSystemEnd)
              subsystemMatch = subsystem.equals(subsystemfilter);
            else if (genericSubSystemStart)
              subsystemMatch = subsystem.endsWith(subsystemfilterpart);
            else if (genericSubSystemEnd)
              subsystemMatch = subsystem.startsWith(subsystemfilterpart);
        }
        if (!subsystemMatch)
          return false;

        // ----------------------------------------------------
        // check for match on subsystem factory category filter
        // ----------------------------------------------------
        boolean ssfCategoryMatch = true;
        if (!allSSFCategories)
        {
        	ISubSystem subsystem = adapter.getSubSystem(element);
        	if (subsystem == null)
        	  ssfCategoryMatch = true; // should be false, but this was added late and I don't to regress anyting.
        	else
        	{
        		String ssfCategory = subsystem.getSubSystemConfiguration().getCategory();
        		if (ssfCategory == null)
        		  ssfCategory = "";        		
        		if (!genericSSFCategoriesStart && !genericSSFCategoriesEnd)        		
        		   ssfCategoryMatch = ssfCategory.equals(subsystemCategoryFilter);
                else if (genericSSFCategoriesStart)
                   ssfCategoryMatch = ssfCategory.endsWith(subsystemCategoryFilterpart);
                else if (genericSSFCategoriesEnd)
                   ssfCategoryMatch = ssfCategory.startsWith(subsystemCategoryFilterpart);
        	}
        }
        if (!ssfCategoryMatch)
          return false;

        // ----------------------------------------------------
        // check for match on system types filter
        // ----------------------------------------------------
        boolean systemTypesMatch = true;
        if (!allSystemTypes)
        {        	
        	ISubSystem subsystem = adapter.getSubSystem(element);
			String[] values = tokenize(systypesfilter);
			if (subsystem == null)
			{
				if (!(element instanceof IHost)) // should never happen for remote objects!
			      systemTypesMatch = false;	
			    else
			    {
			       String connSysType = ((IHost)element).getSystemType();		
			       systemTypesMatch = false;
			       for (int idx=0; !systemTypesMatch && (idx<values.length); idx++)			
			       {
			    	  if (connSysType.equals(values[idx]))
			    	     systemTypesMatch = true;
			       }
			    }
			}
			else
			{
			   systemTypesMatch = false;
			   for (int idx=0; !systemTypesMatch && (idx<values.length); idx++)			
			   {
			      if (subsystem.getHost().getSystemType().equals(values[idx]))
		             systemTypesMatch = true;
			   }
			}
        }
        if (!systemTypesMatch)
          return false;
                    
        // ------------------------------        
        // check for match on name filter
        // ------------------------------
        boolean nameMatch = true;
        
        // default is case insensitive
        boolean caseSensitive = false;
        
        // get subsystem
    	ISubSystem subsystem = adapter.getSubSystem(element);
    	
    	// find out whether system is case sensitive or not
    	if (subsystem != null) {
    		caseSensitive = subsystem.getSubSystemConfiguration().isCaseSensitive();
    	}
        
        if (!allNames)
        {
        	String name = adapter.getName(element);
        	String theNameFilter = namefilter;
        	String theNameFilterPart = namefilterpart;
        	
        	if (name == null) {
        		nameMatch = false;
        	}
        	
        	else {
        		
        		if (!caseSensitive) {
        			name = name.toLowerCase();
        			theNameFilter = theNameFilter.toLowerCase();
        			theNameFilterPart = theNameFilterPart.toLowerCase();
        		}
        		
        		if (!genericNamesStart && !genericNamesEnd)
        			nameMatch = name.equals(theNameFilter);
        		else if (genericNamesStart)
        			nameMatch = name.endsWith(theNameFilterPart);
        		else if (genericNamesEnd)
        			nameMatch = name.startsWith(theNameFilterPart);
        	}
        }
        if (!nameMatch)
          return false;

        // ---------------------------------------        
        // check for match on type category filter
        // ---------------------------------------
        boolean catMatch = true;
        if (!allCategories)
        {
        	String cat = adapter.getRemoteTypeCategory(element);
        	if (cat == null)
        	  catMatch = false;
            else if (!genericCategoriesStart && !genericCategoriesEnd)
              catMatch = cat.equals(categoryfilter);
            else if (genericCategoriesStart)
              catMatch = cat.endsWith(categoryfilterpart);
            else if (genericCategoriesEnd)
              catMatch = cat.startsWith(categoryfilterpart);
        }
        if (!catMatch)
          return false;

        // ------------------------------        
        // check for match on type filter
        // ------------------------------
        boolean typeMatch = true;
        if (!allTypes)
        {
        	String type = adapter.getRemoteType(element);
        	if (type == null)
        	  typeMatch = false;
            else if (!genericTypesStart && !genericTypesEnd)
              typeMatch = type.equals(typefilter);
            else if (genericTypesStart)
              typeMatch = type.endsWith(typefilterpart);
            else if (genericTypesEnd)
              typeMatch = type.startsWith(typefilterpart);
        }
        if (!typeMatch)
          return false;

        // ---------------------------------        
        // check for match on subtype filter
        // ---------------------------------
        boolean subtypeMatch = true;
        if (!allSubTypes)
        {
        	String subtype = adapter.getRemoteSubType(element);
        	if (subtype == null)
        	  subtypeMatch = false;
            else if (!genericSubTypesStart && !genericSubTypesEnd)
              subtypeMatch = subtype.equals(subtypefilter);
            else if (genericSubTypesStart)
              subtypeMatch = subtype.endsWith(subtypefilterpart);
            else if (genericSubTypesEnd)
              subtypeMatch = subtype.startsWith(subtypefilterpart);
        }
        if (!subtypeMatch)
          return false;

        // -------------------------------------        
        // check for match on sub-subtype filter
        // -------------------------------------
        boolean subsubtypeMatch = true;
        if (!allSubSubTypes)
        {
        	String subsubtype = adapter.getRemoteSubSubType(element);
        	if (subsubtype == null)
        	  subsubtypeMatch = false;        	  
            else if (!genericSubSubTypesStart && !genericSubSubTypesEnd)
              subsubtypeMatch = subsubtype.equals(subsubtypefilter);
            else if (genericSubSubTypesStart)
              subsubtypeMatch = subsubtype.endsWith(subsubtypefilterpart);
            else if (genericSubSubTypesEnd)
              subsubtypeMatch = subsubtype.startsWith(subsubtypefilterpart);
        }
        if (!subsubtypeMatch)
          return false;

        return applies;
    }

	/**
	 * Break given comma-delimited string into tokens
	 */
	private String[] tokenize(String input)
	{
          	StringTokenizer tokens = new StringTokenizer(input,";");
            Vector v = new Vector();
            while (tokens.hasMoreTokens())
              v.addElement(tokens.nextToken());
            String[] stringArray = new String[v.size()];
            for (int idx=0; idx<v.size(); idx++)
               stringArray[idx] = (String)v.elementAt(idx);
            return stringArray;
	}    
}