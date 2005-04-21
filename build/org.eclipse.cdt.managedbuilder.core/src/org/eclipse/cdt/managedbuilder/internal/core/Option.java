/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Option extends BuildObject implements IOption {
	// Static default return values
	private static final String EMPTY_STRING = new String();
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	//  Superclass
	private IOption superClass;
	private String superClassId;
	//  Parent and children
	private ITool tool;
	//  Managed Build model attributes
	private String unusedChildren;
	private Integer browseType;
	private List builtIns;
	private IOptionCategory category;
	private String categoryId;
	private String command;
	private String commandFalse;
	private List enumList;
	private Map enumCommands;
	private Map enumNames;
	private Object value;
	private Object defaultValue;
	private Integer valueType;
	private Boolean isAbstract;
	private Integer resourceFilter;
	//  Miscellaneous
	private boolean isExtensionOption = false;
	private boolean isDirty = false;
	private boolean resolved = true;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create an option defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The ITool parent of this option, or <code>null</code> if
	 *                defined at the top level
	 * @param element The option definition from the manifest file or a dynamic element
	 *                provider
	 */
	public Option(Tool parent, IManagedConfigElement element) {
		this.tool = parent;
		isExtensionOption = true;
		
		// setup for resolving
		resolved = false;

		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionOption(this);
	}

	/**
	 * This constructor is called to create an Option whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param Tool The parent of the tool, if any
	 * @param Option The superClass, if any
	 * @param String The id for the new option 
	 * @param String The name for the new option
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	public Option(Tool parent, IOption superClass, String Id, String name, boolean isExtensionElement) {
		this.tool = parent;
		this.superClass = superClass;
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
		isExtensionOption = isExtensionElement;
		if (isExtensionElement) {
			// Hook me up to the Managed Build Manager
			ManagedBuildManager.addExtensionOption(this);
		} else {
			setDirty(true);
		}
	}

	/**
	 * Create an <code>Option</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>ITool</code> the option will be added to. 
	 * @param element The XML element that contains the option settings.
	 */
	public Option(Tool parent, Element element) {
		this.tool = parent;
		isExtensionOption = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);
	}

	/**
	 * Create an <code>Option</code> based upon an existing option.
	 * 
	 * @param parent The <code>ITool</code> the option will be added to. 
	 * @param tool The existing option to clone.
	 */
	public Option(ITool parent, String Id, String name, Option option){
		this.tool = parent;
		superClass = option.superClass;
		if (superClass != null) {
			superClassId = option.superClass.getId();
		}
		setId(Id);
		setName(name);
		isExtensionOption = false;
		
		//  Copy the remaining attributes
		if (option.unusedChildren != null) {
			unusedChildren = new String(option.unusedChildren);
		}
		if (option.isAbstract != null) {
			isAbstract = new Boolean(option.isAbstract.booleanValue());
		}
		if (option.command != null) {
			command = new String(option.command);
		}
		if (option.commandFalse != null) {
			commandFalse = new String(option.commandFalse);
		}
		if (option.categoryId != null) {
			categoryId = new String(option.categoryId);
		}
		if (option.builtIns != null) {
			builtIns = new ArrayList(option.builtIns);
		}
		if (option.browseType != null) {
			browseType = new Integer(option.browseType.intValue());
		}
		if (option.resourceFilter != null) {
			resourceFilter = new Integer(option.resourceFilter.intValue());
		}
		if (option.enumList != null) {
			enumList = new ArrayList(option.enumList);
			enumCommands = new HashMap(option.enumCommands);
			enumNames = new HashMap(option.enumNames);
		}
		if (option.valueType != null) {
			valueType = new Integer(option.valueType.intValue());
			switch (valueType.intValue()) {
				case BOOLEAN:
					if (option.value != null) {
						value = new Boolean(((Boolean)option.value).booleanValue());
					}
					if (option.defaultValue != null) {
						defaultValue = new Boolean(((Boolean)option.defaultValue).booleanValue());
					}
					break;
				case STRING:
				case ENUMERATED:
					if (option.value != null) {
						value = new String((String)option.value);
					}
					if (option.defaultValue != null) {
						defaultValue = new String((String)option.defaultValue);
					}
					break;
				case STRING_LIST:
				case INCLUDE_PATH:
				case PREPROCESSOR_SYMBOLS:
				case LIBRARIES:
				case OBJECTS:
					if (option.value != null) {
						value = new ArrayList((ArrayList)option.value);
					}
					if (option.defaultValue != null) {
						defaultValue = new ArrayList((ArrayList)option.defaultValue);
					}
					break;
			}
		}
		category = option.category;

        setDirty(true);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the option information from the ManagedConfigElement specified in the 
	 * argument.
	 * 
	 * @param element Contains the option information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(IBuildObject.ID));
		
		// Get the name
		setName(element.getAttribute(IBuildObject.NAME));
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);

		// Get the unused children, if any
		unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		
		// isAbstract
        String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
        if (isAbs != null){
    		isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
        }

		// Get the command defined for the option
		command = element.getAttribute(COMMAND);
		
		// Get the command defined for a Boolean option when the value is False
		commandFalse = element.getAttribute(COMMAND_FALSE);
		
		// Options hold different types of values
		String valueTypeStr = element.getAttribute(VALUE_TYPE);
		if (valueTypeStr != null) {
			valueType = new Integer(ValueTypeStrToInt(valueTypeStr));
		}

		// Note: The value and defaultValue attributes are loaded in the resolveReferences routine.
		//       This is because we need to have the value-type, and this may be defined in a 
		//       superClass that is not yet loaded.
		
		// Determine if there needs to be a browse button
		String browseTypeStr = element.getAttribute(BROWSE_TYPE);
		if (browseTypeStr == null || browseTypeStr.equals(NONE)) {
			browseType = new Integer(BROWSE_NONE);
		} else if (browseTypeStr.equals(FILE)) {
			browseType = new Integer(BROWSE_FILE);
		} else if (browseTypeStr.equals(DIR)) {
			browseType = new Integer(BROWSE_DIR);
		}

		categoryId = element.getAttribute(CATEGORY);
		
		// Get the resourceFilter attribute
		String resFilterStr = element.getAttribute(RESOURCE_FILTER);
		if (resFilterStr == null || resFilterStr.equals(ALL)) {
			resourceFilter = new Integer(FILTER_ALL);
		} else if (resFilterStr.equals(FILE)) {
			resourceFilter = new Integer(FILTER_FILE);
		} else if (resFilterStr.equals(PROJECT)) {
			resourceFilter = new Integer(FILTER_PROJECT);
		}
	}
	
	/* (non-Javadoc)
	 * Initialize the option information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the option information 
	 */
	protected void loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.hasAttribute(IBuildObject.NAME)) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		if (superClassId != null && superClassId.length() > 0) {
			superClass = ManagedBuildManager.getExtensionOption(superClassId);
			if (superClass == null) {
				// TODO:  Report error
			}
		}

		// Get the unused children, if any
		if (element.hasAttribute(IProjectType.UNUSED_CHILDREN)) {
				unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		}
		
		// isAbstract
		if (element.hasAttribute(IProjectType.IS_ABSTRACT)) {
			String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
			if (isAbs != null){
				isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
			}
		}

		// Get the command defined for the option
		if (element.hasAttribute(COMMAND)) {
			command = element.getAttribute(COMMAND);
		}
		
		// Get the command defined for a Boolean option when the value is False
		if (element.hasAttribute(COMMAND_FALSE)) {
			commandFalse = element.getAttribute(COMMAND_FALSE);
		}
		
		// Options hold different types of values
		if (element.hasAttribute(VALUE_TYPE)) {
			String valueTypeStr = element.getAttribute(VALUE_TYPE);
			valueType = new Integer(ValueTypeStrToInt(valueTypeStr));
		}
		
		// Now get the actual value based upon value-type
		try {
			int valType = getValueType(); 
			switch (valType) {
				case BOOLEAN:
					// Convert the string to a boolean
					if (element.hasAttribute(VALUE)) {
						value = new Boolean(element.getAttribute(VALUE));
					}
					if (element.hasAttribute(DEFAULT_VALUE)) {
						defaultValue = new Boolean(element.getAttribute(DEFAULT_VALUE));
					}
					break;
				case STRING:
					// Just get the value out of the option directly
					if (element.hasAttribute(VALUE)) {
						value = element.getAttribute(VALUE);
					}
					if (element.hasAttribute(DEFAULT_VALUE)) {
						defaultValue = element.getAttribute(DEFAULT_VALUE);
					}
					break;
				case ENUMERATED:
					if (element.hasAttribute(VALUE)) {
						value = element.getAttribute(VALUE);
					}
					if (element.hasAttribute(DEFAULT_VALUE)) {
						defaultValue = element.getAttribute(DEFAULT_VALUE);
					}
	
					//  Do we have enumeratedOptionValue children?  If so, load them
					//  to define the valid values and the default value.
					NodeList configElements = element.getChildNodes();
					for (int i = 0; i < configElements.getLength(); ++i) {
						Node configNode = configElements.item(i);
						if (configNode.getNodeName().equals(ENUM_VALUE)) {
							Element configElement = (Element)configNode;
							String optId = configElement.getAttribute(ID);
							if (i == 0) {
								enumList = new ArrayList();
								if (defaultValue == null) {
									defaultValue = optId;		//  Default value to be overridden is default is specified
								}
							}
							enumList.add(optId);
							if (configElement.hasAttribute(COMMAND)) {
								getEnumCommandMap().put(optId, configElement.getAttribute(COMMAND));
							} else {
								getEnumCommandMap().put(optId, EMPTY_STRING);
							}
							getEnumNameMap().put(optId, configElement.getAttribute(NAME));
							if (configElement.hasAttribute(IS_DEFAULT)) {
								Boolean isDefault = new Boolean(configElement.getAttribute(IS_DEFAULT));
								if (isDefault.booleanValue()) {
									defaultValue = optId;
								}
							}
						}
					}
					break;
				case STRING_LIST:
				case INCLUDE_PATH:
				case PREPROCESSOR_SYMBOLS:
				case LIBRARIES:
				case OBJECTS:
					//  Note:  These string-list options do not load either the "value" or 
					//         "defaultValue" attributes.  Instead, the ListOptionValue children
					//         are loaded in the value field.
					List valueList = null;
					configElements = element.getChildNodes();
					for (int i = 0; i < configElements.getLength(); ++i) {
						if (i == 0) {
							valueList = new ArrayList();
							builtIns = new ArrayList();
						}
						Node configNode = configElements.item(i);
						if (configNode.getNodeName().equals(LIST_VALUE)) {
							Element valueElement = (Element)configNode;
							Boolean isBuiltIn;
							if (valueElement.hasAttribute(IS_DEFAULT)) {
								isBuiltIn = new Boolean(valueElement.getAttribute(LIST_ITEM_BUILTIN));
							} else {
								isBuiltIn = new Boolean(false);
							}
							if (isBuiltIn.booleanValue()) {
								builtIns.add(valueElement.getAttribute(LIST_ITEM_VALUE));
							}
							else {
								valueList.add(valueElement.getAttribute(LIST_ITEM_VALUE));
							}
						}
					}
					value = valueList;
					break;
				default :
					break;
			}
		} catch (BuildException e) {
			// TODO: report error
		}

		// Determine if there needs to be a browse button
		if (element.hasAttribute(BROWSE_TYPE)) {
			String browseTypeStr = element.getAttribute(BROWSE_TYPE);
			if (browseTypeStr == null || browseTypeStr.equals(NONE)) {
				browseType = new Integer(BROWSE_NONE);
			} else if (browseTypeStr.equals(FILE)) {
				browseType = new Integer(BROWSE_FILE);
			} else if (browseTypeStr.equals(DIR)) {
				browseType = new Integer(BROWSE_DIR);
			}
		}

		if (element.hasAttribute(CATEGORY)) {
			categoryId = element.getAttribute(CATEGORY);
			if (categoryId != null) {
				category = ((Tool)tool).getOptionCategory(categoryId);
			}
		}
		
		// Get the resourceFilter attribute
		if (element.hasAttribute(RESOURCE_FILTER)) {
			String resFilterStr = element.getAttribute(RESOURCE_FILTER);
			if (resFilterStr == null || resFilterStr.equals(ALL)) {
				resourceFilter = new Integer(FILTER_ALL);
			} else if (resFilterStr.equals(FILE)) {
				resourceFilter = new Integer(FILTER_FILE);
			} else if (resFilterStr.equals(PROJECT)) {
				resourceFilter = new Integer(FILTER_PROJECT);
			}
		}
	}

	private int ValueTypeStrToInt(String valueTypeStr) {
		if (valueTypeStr == null) return -1;
		if (valueTypeStr.equals(TYPE_STRING))
			return STRING;
		else if (valueTypeStr.equals(TYPE_STR_LIST))
			return STRING_LIST;
		else if (valueTypeStr.equals(TYPE_BOOL))
			return BOOLEAN;
		else if (valueTypeStr.equals(TYPE_ENUM))
			return ENUMERATED;
		else if (valueTypeStr.equals(TYPE_INC_PATH))
			return INCLUDE_PATH;
		else if (valueTypeStr.equals(TYPE_LIB))
			return LIBRARIES;
		else if (valueTypeStr.equals(TYPE_USER_OBJS))
			return OBJECTS;
		else if (valueTypeStr.equals(TYPE_DEFINED_SYMBOLS))
			return PREPROCESSOR_SYMBOLS;
		else {
			// TODO:  This was the CDT 2.0 default - should we keep it?
			return PREPROCESSOR_SYMBOLS;
		}
	}

	/**
	 * Persist the option to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) throws BuildException {
		if (superClass != null)
			element.setAttribute(IProjectType.SUPERCLASS, superClass.getId());
		
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}

		if (unusedChildren != null) {
			element.setAttribute(IProjectType.UNUSED_CHILDREN, unusedChildren);
		}
		
		if (isAbstract != null) {
			element.setAttribute(IProjectType.IS_ABSTRACT, isAbstract.toString());
		}
		
		if (command != null) {
			element.setAttribute(COMMAND, command);
		}
		
		if (commandFalse != null) {
			element.setAttribute(COMMAND_FALSE, commandFalse);
		}
		
		/*
		 * Note:  We store value & value-type as a pair, so we know what type of value we are
		 *        dealing with when we read it back in.
		 *        This is also true of defaultValue.
		 */
		boolean storeValueType = false;

		// value
		if (value != null) {
			storeValueType = true;
			switch (getValueType()) {
				case BOOLEAN:
					element.setAttribute(VALUE, ((Boolean)value).toString());
					break;
				case STRING:
				case ENUMERATED:
					element.setAttribute(VALUE, (String)value);
					break;
				case STRING_LIST:
				case INCLUDE_PATH:
				case PREPROCESSOR_SYMBOLS:
				case LIBRARIES:
				case OBJECTS:
					if (value != null) {
						ArrayList stringList = (ArrayList)value;
						ListIterator iter = stringList.listIterator();
						while (iter.hasNext()) {
							Element valueElement = doc.createElement(LIST_VALUE);
							valueElement.setAttribute(LIST_ITEM_VALUE, (String)iter.next());
							valueElement.setAttribute(LIST_ITEM_BUILTIN, "false"); //$NON-NLS-1$
							element.appendChild(valueElement);
						}
					}
					// Serialize the built-ins that have been overridden
					if (builtIns != null) {
						ListIterator iter = builtIns.listIterator();
						while (iter.hasNext()) {
							Element valueElement = doc.createElement(LIST_VALUE);
							valueElement.setAttribute(LIST_ITEM_VALUE, (String)iter.next());
							valueElement.setAttribute(LIST_ITEM_BUILTIN, "true"); //$NON-NLS-1$
							element.appendChild(valueElement);
						}
					}
					break;
			}
		}

		// defaultValue
		if (defaultValue != null) {
			storeValueType = true;
			switch (getValueType()) {
				case BOOLEAN:
					element.setAttribute(DEFAULT_VALUE, ((Boolean)defaultValue).toString());
					break;
				case STRING:
				case ENUMERATED:
					element.setAttribute(DEFAULT_VALUE, (String)defaultValue);
					break;
				default:
					break;
			}
		}

		if (storeValueType) {
			String str;
			switch (getValueType()) {
				case BOOLEAN:
					str = TYPE_BOOL;
					break;
				case STRING:
					str = TYPE_STRING;
					break;
				case ENUMERATED:
					str = TYPE_ENUM;
					break;
				case STRING_LIST:
					str = TYPE_STR_LIST;
					break;
				case INCLUDE_PATH:
					str = TYPE_INC_PATH;
					break;
				case LIBRARIES:
					str = TYPE_LIB;
					break;
				case OBJECTS:
					str = TYPE_USER_OBJS;
					break;
				case PREPROCESSOR_SYMBOLS:
					str = TYPE_DEFINED_SYMBOLS;
					break;
				default:
					//  TODO; is this a problem...
					str = EMPTY_STRING; 
					break;
			}
			element.setAttribute(VALUE_TYPE, str);
		}
		
		// browse type
		if (browseType != null) {
			String str;
			switch (getBrowseType()) {
				case BROWSE_NONE:
					str = NONE;
					break;
				case BROWSE_FILE:
					str = FILE;
					break;
				case BROWSE_DIR:
					str = DIR;
					break;
				default:
					str = EMPTY_STRING; 
					break;
			}
			element.setAttribute(BROWSE_TYPE, str);
		}
		
		if (categoryId != null) {
			element.setAttribute(CATEGORY, categoryId);
		}
		
		// resource filter
		if (resourceFilter != null) {
			String str;
			switch (getResourceFilter()) {
				case FILTER_ALL:
					str = ALL;
					break;
				case FILTER_FILE:
					str = FILE;
					break;
				case FILTER_PROJECT:
					str = PROJECT;
					break;
				default:
					str = EMPTY_STRING; 
					break;
			}
			element.setAttribute(RESOURCE_FILTER, str);
		}
		
		// I am clean now
		isDirty = false;
	}
	
	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getParent()
	 */
	public ITool getParent() {
		return tool;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getSuperClass()
	 */
	public IOption getSuperClass() {
		return superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getName()
	 */
	public String getName() {
		return (name == null && superClass != null) ? superClass.getName() : name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getApplicableValues()
	 */
	public String[] getApplicableValues() {
		// Does this option instance have the list of values?
		if (enumList == null) {
			if (superClass != null) {
				return superClass.getApplicableValues();
			} else {
				return EMPTY_STRING_ARRAY;
			}
		}
		// Get all of the enumerated names from the option
		if (enumList.size() == 0) {
			return EMPTY_STRING_ARRAY;
		} else {
			// Return the elements in the order they are specified in the manifest
			String[] enumNames = new String[enumList.size()];
			for (int index = 0; index < enumList.size(); ++ index) {
				enumNames[index] = (String) getEnumNameMap().get(enumList.get(index));
			}
			return enumNames;
		}
	}
	
	public boolean getBooleanValue() {
		return ((Boolean)getValue()).booleanValue();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getBrowseType()
	 */
	public int getBrowseType() {
		if (browseType == null) {
			if (superClass != null) {
				return superClass.getBrowseType();
			} else {
				return BROWSE_NONE;
			}
		}
		return browseType.intValue();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getResourceFilter()
	 */
	public int getResourceFilter() {
		if (resourceFilter == null) {
			if (superClass != null) {
				return superClass.getResourceFilter();
			} else {
				return FILTER_ALL;
			}
		}
		return resourceFilter.intValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getBuiltIns()
	 */
	public String[] getBuiltIns() {
		// Return the list of built-ins as an array
		if (builtIns == null) {
			if (superClass != null) {
				return superClass.getBuiltIns();
			} else {
				return EMPTY_STRING_ARRAY; 
			}
		}			   
		return (String[])builtIns.toArray(new String[builtIns.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCategory()
	 */
	public IOptionCategory getCategory() {
		if (category == null) {
			if (superClass != null) {
				return superClass.getCategory();
			} else {
				return getParent().getTopOptionCategory();
			}			
		}
		return category; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCommand()
	 */
	public String getCommand() {
		if (command == null) {
			if (superClass != null) {
				return superClass.getCommand();
			} else {
				return EMPTY_STRING;
			}			
		}
		return command;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCommandFalse()
	 */
	public String getCommandFalse() {
		if (commandFalse == null) {
			if (superClass != null) {
				return superClass.getCommandFalse();
			} else {
				return EMPTY_STRING;
			}			
		}
		return commandFalse;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getDefinedSymbols()
	 */
	public String[] getDefinedSymbols() throws BuildException {
		if (getValueType() != PREPROCESSOR_SYMBOLS) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		ArrayList v = (ArrayList)getValue();
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
	public String getEnumCommand(String id) throws BuildException {
		// Sanity
		if (id == null) return EMPTY_STRING;

		// Does this option instance have the list of values?
		if (enumList == null) {
			if (superClass != null) {
				return superClass.getEnumCommand(id);
			} else {
				return EMPTY_STRING;
			}			
		}
		if (getValueType() != ENUMERATED) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
	
		// First check for the command in ID->command map
		String cmd = (String) getEnumCommandMap().get(id);
		if (cmd == null) {
			// This may be a 1.2 project or plugin manifest. If so, the argument is the human readable
			// name of the enumeration. Search for the ID that maps to the name and use that to find the
			// command.
			ListIterator iter = enumList.listIterator();
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
	public String getEnumName(String id) throws BuildException {
		// Sanity
		if (id == null) return EMPTY_STRING;

		// Does this option instance have the list of values?
		if (enumList == null) {
			if (superClass != null) {
				return superClass.getEnumName(id);
			} else {
				return EMPTY_STRING;
			}			
		}
		if (getValueType() != ENUMERATED) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		
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
	public String getEnumeratedId(String name) throws BuildException {
		if (name == null) return null;

		// Does this option instance have the list of values?
		if (enumList == null) {
			if (superClass != null) {
				return superClass.getEnumeratedId(name);
			} else {
				return EMPTY_STRING;
			}			
		}
		if (getValueType() != ENUMERATED) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}

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
		if (getValueType() != INCLUDE_PATH) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		ArrayList v = (ArrayList)getValue();
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
		if (getValueType() != LIBRARIES) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		ArrayList v = (ArrayList)getValue();
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
		if (getValueType() != ENUMERATED) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		return getStringValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringListValue()
	 */
	public String[] getStringListValue() throws BuildException {
		if (getValueType() != STRING_LIST) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		ArrayList v = (ArrayList)getValue();
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
		if (getValueType() != STRING && getValueType() != ENUMERATED) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		return getValue() == null ? EMPTY_STRING : (String)getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getUserObjects()
	 */
	public String[] getUserObjects() throws BuildException {
		if (getValueType() != OBJECTS) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		// This is the right puppy, so return its list value
		ArrayList v = (ArrayList)getValue();
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
	public int getValueType() throws BuildException {
		if (valueType == null) {
			if (superClass != null) {
				return superClass.getValueType();
			} else {
				throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$;
			}			
		}
		return valueType.intValue();
	}

	/* (non-Javadoc)
	 * Gets the value, applying appropriate defaults if necessary.
	 */
	public Object getValue() {
		/*
		 *  In order to determine the current value of an option, perform the following steps until a value is found:
		 *   1.	Examine the value attribute of the option.
		 *   2.	Examine the value attribute of the option’s superClass recursively.
		 *   3.	Examine the dynamicDefaultValue attribute of the option and invoke it if specified. (not yet implemented)
		 *   4.	Examine the defaultValue attribute of the option.
		 *   5.	Examine the dynamicDefaultValue attribute of the option’s superClass and invoke it if specified. (not yet implemented)
		 *   6.	Examine the defaultValue attribute of the option’s superClass.
		 *   7.	Go to step 5 recursively until no more super classes.
		 *   8.	Use the default value for the option type.
		 */

		Object val = getRawValue();
		if (val == null) {
			val = getDefaultValue();
			if (val == null) {
				int valType;
				try {
					valType = getValueType(); 
				} catch (BuildException e) {
					return EMPTY_STRING;
				}
				switch (valType) {
					case BOOLEAN:
						val = new Boolean(false);
						break;
					case STRING:
						val = EMPTY_STRING;
						break;
					case ENUMERATED:
						// TODO: Can we default to the first enumerated id?
						val = EMPTY_STRING;
						break;
					case STRING_LIST:
					case INCLUDE_PATH:
					case PREPROCESSOR_SYMBOLS:
					case LIBRARIES:
					case OBJECTS:
						val = new ArrayList();
						break;
					default:
						val = EMPTY_STRING; 
						break;
				}
			}
		}
		return val;
	}

	/* (non-Javadoc)
	 * Gets the raw value, applying appropriate defauls if necessary.
	 */
	public Object getRawValue() {
		if (value == null) {
			if (superClass != null) {
				Option mySuperClass = (Option)superClass;
				return mySuperClass.getRawValue();
			}
		}
		return value;
	}

	/* (non-Javadoc)
	 * Gets the raw default value.
	 */
	public Object getDefaultValue() {
		// Note: string-list options do not have a default value
		if (defaultValue == null) {
			if (superClass != null) {
				return superClass.getDefaultValue();
			}
		}
		return defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setValue(Object)
	 */
	public void setDefaultValue(Object v) {
		defaultValue = v;
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setCategory(org.eclipse.cdt.core.build.managed.IOptionCategory)
	 */
	public void setCategory(IOptionCategory category) {
		if (this.category != category) {
			this.category = category;
			if (category != null) {
				categoryId = category.getId();
			} else {
				categoryId = null;
			}
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setCommand(String)
	 */
	public void setCommand(String cmd) {
		if (cmd == null && command == null) return;
		if (cmd == null || command == null || !cmd.equals(command)) {
			command = cmd;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setCommandFalse(String)
	 */
	public void setCommandFalse(String cmd) {
		if (cmd == null && commandFalse == null) return;
		if (cmd == null || commandFalse == null || !cmd.equals(commandFalse)) {
			commandFalse = cmd;
			isDirty = true;		
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#setResourceFilter(int)
	 */
	public void setResourceFilter(int filter) {
		if (resourceFilter == null || !(filter == resourceFilter.intValue())) {
			resourceFilter = new Integer(filter);
			isDirty = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#setBrowseType(int)
	 */
	public void setBrowseType(int type) {
		if (browseType == null || !(type == browseType.intValue())) {
			browseType = new Integer(type);
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setValue(boolean)
	 */
	public void setValue(boolean value) throws BuildException {
		if (!isExtensionElement() && getValueType() == BOOLEAN)
			this.value = new Boolean(value);
		else {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		setDirty(true);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setValue(String)
	 */
	public void setValue(String value) throws BuildException {
		// Note that we can still set the human-readable value here 
		if (!isExtensionElement() && (getValueType() == STRING || getValueType() == ENUMERATED)) {
			this.value = value;
		} else {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		setDirty(true);
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setValue(String [])
	 */
	public void setValue(String [] value) throws BuildException {
		if (!isExtensionElement() && 
			  (getValueType() == STRING_LIST
			|| getValueType() == INCLUDE_PATH
			|| getValueType() == PREPROCESSOR_SYMBOLS
			|| getValueType() == LIBRARIES
			|| getValueType() == OBJECTS)) {
			// Just replace what the option reference is holding onto 
			this.value = new ArrayList(Arrays.asList(value));
		}
		else {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setValue(Object)
	 */
	public void setValue(Object v) {
		value = v;
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setValueType()
	 */
	public void setValueType(int type) {
		// TODO:  Verify that this is a valid type
		if (valueType == null || valueType.intValue() != type) {
			valueType = new Integer(type);
			setDirty(true);
		}
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionOption;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#overridesOnlyValue()
	 */
	public boolean overridesOnlyValue() {
		if (superClass != null &&
			unusedChildren == null &&
		    browseType == null &&
		    (builtIns == null || builtIns.size() == 0) &&
		    category == null &&
			categoryId == null &&
			command == null &&
			commandFalse == null &&
			enumList == null &&
			enumCommands == null &&
			enumNames == null &&
			defaultValue == null) {
			return true;
		} else {
			return false;
		}			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension option
 		if (isExtensionOption) return false;
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IToolChain#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public void resolveReferences() {

		if (!resolved) {
			resolved = true;
			// Resolve superClass
			if (superClassId != null && superClassId.length() > 0) {
				superClass = ManagedBuildManager.getExtensionOption(superClassId);
				if (superClass == null) {
					// Report error
					ManagedBuildManager.OutputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"option",	//$NON-NLS-1$
							getId());
				}
			}
			if (categoryId != null) {
				category = ((Tool)tool).getOptionCategory(categoryId);
				if (category == null) {
					// Report error
					ManagedBuildManager.OutputResolveError(
							"category",	//$NON-NLS-1$
							categoryId,
							"option",	//$NON-NLS-1$
							getId());
					}
			}
			// Process the value and default value attributes.  This is delayed until now
			// because we may not know the valueType until after we have resolved the superClass above
			// Now get the actual value
			try {
				IManagedConfigElement element = ManagedBuildManager.getConfigElement(this);
				switch (getValueType()) {
					case BOOLEAN:
						// Convert the string to a boolean
						String val = element.getAttribute(VALUE);
						if (val != null) {
							value = new Boolean(val);
						}
						val = element.getAttribute(DEFAULT_VALUE);
						if (val != null) {
							defaultValue = new Boolean(val);
						}
						break;
					case STRING:
						// Just get the value out of the option directly
						value = element.getAttribute(VALUE);
						defaultValue = element.getAttribute(DEFAULT_VALUE);
						break;
					case ENUMERATED:
						value = element.getAttribute(VALUE);
						defaultValue = element.getAttribute(DEFAULT_VALUE);
	
						//  Do we have enumeratedOptionValue children?  If so, load them
						//  to define the valid values and the default value.
						IManagedConfigElement[] enumElements = element.getChildren(ENUM_VALUE);
						for (int i = 0; i < enumElements.length; ++i) {
							String optId = enumElements[i].getAttribute(ID);
							if (i == 0) {
								enumList = new ArrayList();
								if (defaultValue == null) {
									defaultValue = optId;		//  Default value to be overridden if default is specified
								}
							}
							enumList.add(optId);
							getEnumCommandMap().put(optId, enumElements[i].getAttribute(COMMAND));
							getEnumNameMap().put(optId, enumElements[i].getAttribute(NAME));
							Boolean isDefault = new Boolean(enumElements[i].getAttribute(IS_DEFAULT));
							if (isDefault.booleanValue()) {
								defaultValue = optId; 
							}
						}
						break;
					case STRING_LIST:
					case INCLUDE_PATH:
					case PREPROCESSOR_SYMBOLS:
					case LIBRARIES:
					case OBJECTS:
						//  Note:  These string-list options do not load either the "value" or 
						//         "defaultValue" attributes.  Instead, the ListOptionValue children
						//         are loaded in the value field.
						List valueList = null;
						IManagedConfigElement[] valueElements = element.getChildren(LIST_VALUE);
						for (int i = 0; i < valueElements.length; ++i) {
							if (i == 0) {
								valueList = new ArrayList();
								builtIns = new ArrayList();
							}
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
			} catch (BuildException e) {
				// TODO: report error
			}
		}
	}

}
