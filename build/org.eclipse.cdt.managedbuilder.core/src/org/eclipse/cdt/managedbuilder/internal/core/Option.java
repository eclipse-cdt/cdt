/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

public class Option extends BuildObject implements IOption {
	// Static default return values
	private static final String EMPTY_STRING = new String();
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	// Private bookeeping attributes
	private int browseType;
	private List builtIns;
	private IOptionCategory category;
	private String command;
	private String commandFalse;
	private String defaultEnumId;
	private Map enumCommands;
	private Map enumNames;
	private ITool tool;
	private Object value;
	private int valueType;
	private boolean resolved = true;
	 
	public Option(ITool tool) {
		this.tool = tool;
	}
	
	public Option(Tool tool, IManagedConfigElement element) {
		this(tool);
		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		resolved = false;
		
		// Get the unique id of the option
		setId(element.getAttribute(ID));
		
		// Hook me up to a tool
		tool.addOption(this);
		
		// Get the option Name (this is what the user will see in the UI)
		setName(element.getAttribute(NAME));

		// Get the command defined for the option
		command = element.getAttribute(COMMAND);
		
		// Get the command defined for a Boolean option when the value is False
		commandFalse = element.getAttribute(COMMAND_FALSE);
		
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
				IManagedConfigElement[] enumElements = element.getChildren(ENUM_VALUE);
				for (int i = 0; i < enumElements.length; ++i) {
					String optId = enumElements[i].getAttribute(ID);
					enumList.add(optId);
					getEnumCommandMap().put(optId, enumElements[i].getAttribute(COMMAND));
					getEnumNameMap().put(optId, enumElements[i].getAttribute(NAME));
					Boolean isDefault = new Boolean(enumElements[i].getAttribute(IS_DEFAULT));
					if (isDefault.booleanValue()) {
						defaultEnumId = optId; 
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
				IManagedConfigElement[] valueElements = element.getChildren(LIST_VALUE);
				for (int i = 0; i < valueElements.length; ++i) {
					IManagedConfigElement valueElement = valueElements[i];
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
		
		// Determine if there needs to be a browse button
		String browseTypeStr = element.getAttribute(BROSWE_TYPE);
		if (browseTypeStr == null || browseTypeStr.equals(NONE)) {
			browseType = BROWSE_NONE;
		} else if (browseTypeStr.equals(FILE)) {
			browseType = BROWSE_FILE;
		} else if (browseTypeStr.equals(DIR)) {
			browseType = BROWSE_DIR;
		}
		
	}

	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			IManagedConfigElement element = ManagedBuildManager.getConfigElement(this);
			// Options can be grouped into categories
			String categoryId = element.getAttribute(CATEGORY);
			if (categoryId != null)
				setCategory(((Tool)tool).getOptionCategory(categoryId));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getApplicableValues()
	 */
	public String[] getApplicableValues() {
		// Get all of the enumerated names from the option
		List ids = (List) value;
		if (ids == null || ids.size() == 0) {
			return EMPTY_STRING_ARRAY;
		} else {
			// Return the elements in the order they are specified in the manifest
			String[] enumNames = new String[ids.size()];
			for (int index = 0; index < ids.size(); ++ index) {
				enumNames[index] = (String) getEnumNameMap().get(ids.get(index));
			}
			return enumNames;
		}
	}

	public boolean getBooleanValue() {
		Boolean bool = (Boolean) value;
		return bool.booleanValue();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getBrowseType()
	 */
	public int getBrowseType() {
		return browseType;
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
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCommandFalse()
	 */
	public String getCommandFalse() {
		return commandFalse;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getDefinedSymbols()
	 */
	public String[] getDefinedSymbols() throws BuildException {
		if (valueType != PREPROCESSOR_SYMBOLS) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		ArrayList v = (ArrayList)value;
		if (v == null) {
			return EMPTY_STRING_ARRAY;
		} else {
			v.trimToSize();
			return (String[]) v.toArray(new String[v.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getEnumCommand(java.lang.String)
	 */
	public String getEnumCommand(String id) {
		// Sanity
		if (id == null) return EMPTY_STRING;
		
		// First check for the command in ID->command map
		String cmd = (String) getEnumCommandMap().get(id);
		if (cmd == null) {
			// This may be a 1.2 project or plugin manifest. If so, the argument is the human readable
			// name of the enumeration. Search for the ID that maps to the name and use that to find the
			// command.
			List ids = (List) value;
			ListIterator iter = ids.listIterator();
			while (iter.hasNext()) {
				String realID = (String) iter.next();
				String name = (String) getEnumNameMap().get(realID);
				if (id.equals(name)) {
					cmd = (String) getEnumCommandMap().get(realID);
					break;
				}
			}
		}
		return cmd == null ? EMPTY_STRING : cmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getEnumName(java.lang.String)
	 */
	public String getEnumName(String id) {
		// Sanity
		if (id == null) return EMPTY_STRING;
		
		// First check for the command in ID->name map
		String name = (String) getEnumNameMap().get(id);
		if (name == null) {
			// This may be a 1.2 project or plugin manifest. If so, the argument is the human readable
			// name of the enumeration.
			name = id;
		}
		return name;
	}

	/* (non-Javadoc)
	 * A memory-safe accessor to the map of enumerated option value IDs to the commands
	 * that a tool understands.
	 * 
	 * @return a Map of enumerated option value IDs to actual commands that are passed 
	 * to a tool on the command line.
	 */
	private Map getEnumCommandMap() {
		if (enumCommands == null) {
			enumCommands = new HashMap();
		}
		return enumCommands;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getEnumeratedId(java.lang.String)
	 */
	public String getEnumeratedId(String name) {
		if (name == null) return null;
		Set idSet = getEnumNameMap().keySet();
		Iterator iter = idSet.iterator();
		while (iter.hasNext()) {
			String id = (String) iter.next();
			String enumName = (String) getEnumNameMap().get(id);
			if (name.equals(enumName)) {
				return id;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * 
	 * @return a Map of enumerated option value IDs to the selection displayed to the user.
	 */
	private Map getEnumNameMap() {
		if (enumNames == null) {
			enumNames = new HashMap();
		}
		return enumNames;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getIncludePaths()
	 */
	public String[] getIncludePaths() throws BuildException {
		if (valueType != INCLUDE_PATH) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		ArrayList v = (ArrayList)value;
		if (v == null) {
			return EMPTY_STRING_ARRAY;
		} else {
			v.trimToSize();
			return (String[]) v.toArray(new String[v.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getLibraries()
	 */
	public String[] getLibraries() throws BuildException {
		if (valueType != LIBRARIES) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		ArrayList v = (ArrayList)value;
		if (v == null) {
			return EMPTY_STRING_ARRAY;
		} else {
			v.trimToSize();
			return (String[]) v.toArray(new String[v.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getDefaultEnumValue()
	 */
	public String getSelectedEnum() throws BuildException {
		if (valueType != ENUMERATED) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		return defaultEnumId == null ? EMPTY_STRING : defaultEnumId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringListValue()
	 */
	public String[] getStringListValue() throws BuildException {
		if (valueType != STRING_LIST) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		ArrayList v = (ArrayList)value;
		if (v == null) {
			return EMPTY_STRING_ARRAY;
		} else {
			v.trimToSize();
			return (String[]) v.toArray(new String[v.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringValue()
	 */
	public String getStringValue() throws BuildException {
		if (valueType != STRING) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
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
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		// This is the right puppy, so return its list value
		ArrayList v = (ArrayList)value;
		if (v == null) {
			return EMPTY_STRING_ARRAY;
		} else {
			v.trimToSize();
			return (String[]) v.toArray(new String[v.size()]);
		}
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
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$

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
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		
		if (config == null) {
			this.value = value;
			return this;
		} else {
			// More magic
			return null;
		}
	}

}
