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
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IConfigurationElement;

public class Option extends BuildObject implements IOption {
	// Static default return values
	private static final String EMPTY_STRING = new String();
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	// Private bookeeping attributes
	private List builtIns;
	private IOptionCategory category;
	private String command;
	private String defaultEnumName;
	private Map enumCommands;
	private ITool tool;
	private Object value;
	private int valueType;
	
	 
	public Option(ITool tool) {
		this.tool = tool;
	}
	
	public Option(Tool tool, IConfigurationElement element) {
		this(tool);
		
		// Get the unique id of the option
		setId(element.getAttribute(ID));
		
		// Hook me up to a tool
		tool.addOption(this);
		
		// Get the option Name (this is what the user will see in the UI)
		setName(element.getAttribute(NAME));

		// Options can be grouped into categories
		String categoryId = element.getAttribute(CATEGORY);
		if (categoryId != null)
			setCategory(tool.getOptionCategory(categoryId));
		
		// Get the command defined for the option
		command = element.getAttribute(COMMAND);
		
		// Options hold different types of values
		String valueTypeStr = element.getAttribute(VALUE_TYPE);
		if (valueTypeStr == null)
			valueType = -1;
		else if (valueTypeStr.equals(TYPE_STRING))
			valueType = STRING;
		else if (valueTypeStr.equals(TYPE_STR_LIST))
			valueType = STRING_LIST;
		else if (valueTypeStr.equals(TYPE_BOOL))
			valueType = BOOLEAN;
		else if (valueTypeStr.equals(TYPE_ENUM))
			valueType = ENUMERATED;
		else if (valueTypeStr.equals(TYPE_INC_PATH))
			valueType = INCLUDE_PATH;
		else if (valueTypeStr.equals(TYPE_LIB))
			valueType = LIBRARIES;
		else if (valueTypeStr.equals(TYPE_USER_OBJS))
			valueType = OBJECTS;
		else
			valueType = PREPROCESSOR_SYMBOLS;
		
		// Now get the actual value
		enumCommands = new HashMap();
		switch (valueType) {
			case BOOLEAN:
				// Convert the string to a boolean
				value = new Boolean(element.getAttribute(DEFAULT_VALUE));
				break;
			case STRING:
				// Just get the value out of the option directly
				value = element.getAttribute(DEFAULT_VALUE);
			break;
			case ENUMERATED:
				List enumList = new ArrayList();
				IConfigurationElement[] enumElements = element.getChildren(ENUM_VALUE);
				for (int i = 0; i < enumElements.length; ++i) {
					String optName = enumElements[i].getAttribute(NAME);
					String optCommand = enumElements[i].getAttribute(COMMAND); 
					enumList.add(optName);
					enumCommands.put(optName, optCommand);
					Boolean isDefault = new Boolean(enumElements[i].getAttribute(IS_DEFAULT));
					if (isDefault.booleanValue()) {
						defaultEnumName = optName; 
					}
				}
				value = enumList;
				break;
			case STRING_LIST:
			case INCLUDE_PATH:
			case PREPROCESSOR_SYMBOLS:
			case LIBRARIES:
			case OBJECTS:
				List valueList = new ArrayList();
				builtIns = new ArrayList();
				IConfigurationElement[] valueElements = element.getChildren(LIST_VALUE);
				for (int i = 0; i < valueElements.length; ++i) {
					IConfigurationElement valueElement = valueElements[i];
					Boolean isBuiltIn = new Boolean(valueElement.getAttribute(LIST_ITEM_BUILTIN));
					if (isBuiltIn.booleanValue()) {
						builtIns.add(valueElement.getAttribute(LIST_ITEM_VALUE));
					}
					else {
						valueList.add(valueElement.getAttribute(LIST_ITEM_VALUE));
					}
				}
				value = valueList;
				break;
			default :
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getApplicableValues()
	 */
	public String[] getApplicableValues() {
		List enumValues = (List)value;
		return enumValues != null
			? (String[])enumValues.toArray(new String[enumValues.size()])
			: EMPTY_STRING_ARRAY;
	}

	public boolean getBooleanValue() {
		Boolean bool = (Boolean) value;
		return bool.booleanValue();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getBuiltIns()
	 */
	public String[] getBuiltIns() {
		// Return the list of built-ins as an array
		return builtIns == null ?
			   EMPTY_STRING_ARRAY:
			   (String[])builtIns.toArray(new String[builtIns.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCategory()
	 */
	public IOptionCategory getCategory() {
		return category != null ? category : getTool().getTopOptionCategory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCommand()
	 */
	public String getCommand() {
		return command;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getDefinedSymbols()
	 */
	public String[] getDefinedSymbols() throws BuildException {
		if (valueType != PREPROCESSOR_SYMBOLS) {
			throw new BuildException("bad value type");
		}
		List v = (List)value;
		return v != null
			? (String[])v.toArray(new String[v.size()])
			: EMPTY_STRING_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getEnumCommand(java.lang.String)
	 */
	public String getEnumCommand(String name) {
		String cmd = (String) enumCommands.get(name); 
		return cmd == null ? EMPTY_STRING : cmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getIncludePaths()
	 */
	public String[] getIncludePaths() throws BuildException {
		if (valueType != INCLUDE_PATH) {
			throw new BuildException("bad value type");
		}
		List v = (List)value;
		return v != null
			? (String[])v.toArray(new String[v.size()])
			: EMPTY_STRING_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getLibraries()
	 */
	public String[] getLibraries() throws BuildException {
		if (valueType != LIBRARIES) {
			throw new BuildException("bad value type");
		}
		List v = (List)value;
		return v != null
			? (String[])v.toArray(new String[v.size()])
			: EMPTY_STRING_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getDefaultEnumValue()
	 */
	public String getSelectedEnum() throws BuildException {
		if (valueType != ENUMERATED) {
			throw new BuildException("bad value type");
		}
		return defaultEnumName == null ? EMPTY_STRING : defaultEnumName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringListValue()
	 */
	public String[] getStringListValue() throws BuildException {
		if (valueType != STRING_LIST) {
			throw new BuildException("bad value type");
		}
		List v = (List)value;
		return v != null
			? (String[])v.toArray(new String[v.size()])
			: EMPTY_STRING_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringValue()
	 */
	public String getStringValue() throws BuildException {
		if (valueType != STRING) {
			throw new BuildException("bad value type");
		}
		return value == null ? EMPTY_STRING : (String)value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getTool()
	 */
	public ITool getTool() {
		return tool;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getUserObjects()
	 */
	public String[] getUserObjects() throws BuildException {
		if (valueType != OBJECTS) {
			throw new BuildException("bad value type");
		}
		// This is the right puppy, so return its list value
		List v = (List)value;
		return v != null
			? (String[])v.toArray(new String[v.size()])
			: EMPTY_STRING_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getValueType()
	 */
	public int getValueType() {
		return valueType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setCategory(org.eclipse.cdt.core.build.managed.IOptionCategory)
	 */
	public void setCategory(IOptionCategory category) {
		this.category = category;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setStringValue(org.eclipse.cdt.core.build.managed.IConfiguration, java.lang.String)
	 */
	public IOption setValue(IConfiguration config, String value)
		throws BuildException
	{
		if (valueType != IOption.STRING
			|| valueType != ENUMERATED)
			throw new BuildException("Bad value for type");

		if (config == null) {
			this.value = value;
			return this;
		} else {
			// Magic time
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setStringValue(org.eclipse.cdt.core.build.managed.IConfiguration, java.lang.String[])
	 */
	public IOption setValue(IConfiguration config, String[] value)
		throws BuildException
	{
		if (valueType != STRING_LIST 
			|| valueType != INCLUDE_PATH
			|| valueType != PREPROCESSOR_SYMBOLS
			|| valueType != LIBRARIES
			|| valueType != OBJECTS)
			throw new BuildException("Bad value for type");
		
		if (config == null) {
			this.value = value;
			return this;
		} else {
			// More magic
			return null;
		}
	}

}
