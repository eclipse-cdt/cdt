/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.build.managed;

/**
 * 
 */
public interface IOption {

	// Type for the value of the option
	public static final int STRING = 0;
	public static final int STRING_LIST = 1;
	
	/**
	 * Returns the tool defining this option.
	 * 
	 * @return
	 */
	public ITool getTool();
	
	/**
	 * Returns the category for this option.
	 * 
	 * @return
	 */
	public IOptionCategory getCategory();
	
	/**
	 * Returns the name of this option.
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Get the type for the value of the option.
	 * 
	 * @return
	 */
	public int getValueType();
	
	/**
	 * If this option is defined as an enumeration, this function returns
	 * the list of possible values for that enum.
	 * 
	 * If this option is not defined as an enumeration, it returns null.
	 * @return
	 */
	public String [] getApplicableValues();

	/**
	 * Returns the current value for this option if it is a String
	 * 
	 * @return
	 */
	public String getStringValue();
	
	/**
	 * Returns the current value for this option if it is a List of Strings.
	 * 
	 * @return
	 */
	public String [] getStringListValue();
	
	/**
	 * Sets the value for this option in a given configuration.
	 * A new instance of the option for the configuration may be created.
	 * The appropriate new option is returned.
	 * 
	 * @param config
	 * @param value
	 */
	public IOption setStringValue(IConfiguration config, String value);

	/**
	 * Sets the value for this option in a given configuration.
	 * A new instance of the option for the configuration may be created.
	 * The appropriate new option is returned.
	 * 
	 * @param config
	 * @param value
	 */
	public IOption setStringValue(IConfiguration config, String[] value);

}
