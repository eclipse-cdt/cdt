/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;
import org.eclipse.rse.ui.SystemPropertyResources;
/**
 * This class captures the data needed to populate a
 * InheritableTextCellEditor.
 */
public class SystemInheritablePropertyData
{
	private String localValue="";
	private String inheritedValue="";
	private boolean isLocal;
	private boolean notApplicable = false;

	private String inheritedXlatedString;
	
	public SystemInheritablePropertyData()
	{
		super();
		setInheritedDisplayString(SystemPropertyResources.RESID_PROPERTY_INHERITED);
	}
	
	/**
	 * Identify this value as "not applicable". This causes
	 * this string to be displayed, and prevents users from editing this property.
	 */
	public void setNotApplicable(boolean set)
	{
		notApplicable = set;
	}
	/**
	 * Get the notApplicable flag. Default is false.
	 */
	public boolean getNotApplicable()
	{
		return notApplicable;
	}
	
	/**
	 * Gets the localValue
	 * @return Returns a String
	 */
	public String getLocalValue() 
	{
		return localValue;
	}
	/**
	 * Sets the localValue
	 * @param localValue The localValue to set
	 */
	public void setLocalValue(String localValue)
	{
		if (localValue == null)
		  localValue = ""; // to prevent equals() from crashing
		this.localValue = localValue;
	}

	/**
	 * Gets the inheritedValue
	 * @return Returns a String
	 */
	public String getInheritedValue() 
	{
		return inheritedValue;
	}
	/**
	 * Sets the inheritedValue
	 * @param inheritedValue The inheritedValue to set
	 */
	public void setInheritedValue(String inheritedValue)
	{
		if (inheritedValue == null)
		  inheritedValue = ""; // to prevent equals() from crashing
		this.inheritedValue = inheritedValue;
	}


	/**
	 * Gets the isLocal
	 * @return Returns a boolean
	 */
	public boolean getIsLocal() 
	{
		return isLocal;
	}
	/**
	 * Sets the isLocal
	 * @param isLocal The isLocal to set
	 */
	public void setIsLocal(boolean isLocal)
	{
		this.isLocal = isLocal;
	}
	
	/**
	 * Set the string to append to the inherited value in display-only mode
	 */
	public void setInheritedDisplayString(String s)
	{
        inheritedXlatedString = s;
	}
 
    /**
     * Convert to string for readonly-property sheet value
     */
    public String toString()
    {
    	if (notApplicable)
    	  return SystemPropertyResources.RESID_TERM_NOTAPPLICABLE;
    	String value = null;
    	if (isLocal)
    	  value = localValue;
    	else
    	  //value = " (*INHERITED)";
    	  value = inheritedValue + " " + inheritedXlatedString;
    	return value;
    }
    
    /**
     * The property sheet viewer will decide to call the adapter back when Enter is pressed,
     *  only if the result of calling equals() on the previous and current versions of this
     *  object returns false. If we did not have this method, they'd always return true.
     */
    public boolean equals(Object other)
    {
    	if (other instanceof SystemInheritablePropertyData)
    	{
    	  SystemInheritablePropertyData otherData = (SystemInheritablePropertyData)other;
    	  boolean equal = 
                  ((isLocal == otherData.getIsLocal()) &&
                   (localValue.equals(otherData.getLocalValue())) &&
                   (inheritedValue.equals(otherData.getInheritedValue())) );    	  
         /*
         System.out.println("inside equals. Result? " + equal + " Local value: " + localValue);
         if (!equal)
         {
           System.out.println("... isLocal.......: " + isLocal + " vs " + otherData.getIsLocal());
           System.out.println("... localValue....: '" + localValue + "' vs '" + otherData.getLocalValue() + "'");
           System.out.println("... inheritedValue: '" + inheritedValue + "' vs " + otherData.getInheritedValue() + "'");
         }
         */
          return equal;
    	}
    	else
    	  return super.equals(other);
    }
    
    /**
     * For debugging
     */
    public void printDetails()
    {
    	System.out.println("SystemInheritablePropertyData: ");
    	System.out.println("...localValue = "+localValue);
    	System.out.println("...inheritedValue = "+inheritedValue);
    	System.out.println("...isLocal = "+isLocal);    	
    }
}