/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     ARM Ltd. - basic tooltip support
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;


/**
 * 
 */
public interface IOption extends IBuildObject {
	// Type for the value of the option
	public static final int BOOLEAN = 0;
	public static final int ENUMERATED = 1;
	public static final int STRING = 2;
	public static final int STRING_LIST = 3;
	public static final int INCLUDE_PATH = 4;
	public static final int PREPROCESSOR_SYMBOLS = 5;
	public static final int LIBRARIES = 6;
	public static final int OBJECTS = 7;

	// Browse type
	public static final int BROWSE_NONE = 0;
	public static final String NONE = "none";	//$NON-NLS-1$
	public static final int BROWSE_FILE = 1;
	public static final String FILE = "file";	//$NON-NLS-1$
	public static final int BROWSE_DIR = 2;
	public static final String DIR = "directory";	//$NON-NLS-1$

	// Resource Filter type
	public static final int FILTER_ALL = 0;
	public static final String ALL = "all";	//$NON-NLS-1$
	public static final int FILTER_FILE = 1;
	public static final int FILTER_PROJECT = 2;
	public static final String PROJECT = "project";	//$NON-NLS-1$
	
	// Schema attribute names for option elements
	public static final String BROWSE_TYPE = "browseType";	//$NON-NLS-1$
	public static final String CATEGORY = "category"; //$NON-NLS-1$
	public static final String COMMAND = "command"; //$NON-NLS-1$
	public static final String COMMAND_FALSE = "commandFalse"; //$NON-NLS-1$
	public static final String TOOL_TIP = "tip"; //$NON-NLS-1$
	public static final String DEFAULT_VALUE = "defaultValue"; //$NON-NLS-1$
	public static final String ENUM_VALUE = "enumeratedOptionValue"; //$NON-NLS-1$
	public static final String IS_DEFAULT = "isDefault"; //$NON-NLS-1$
	public static final String LIST_VALUE = "listOptionValue"; //$NON-NLS-1$
	public static final String RESOURCE_FILTER = "resourceFilter"; //$NON-NLS-1$
	public static final String APPLICABILITY_CALCULATOR = "applicabilityCalculator"; //$NON-NLS-1$
	public static final String TYPE_BOOL = "boolean"; //$NON-NLS-1$
	public static final String TYPE_ENUM = "enumerated"; //$NON-NLS-1$
	public static final String TYPE_INC_PATH = "includePath"; //$NON-NLS-1$
	public static final String TYPE_LIB = "libs"; //$NON-NLS-1$
	public static final String TYPE_STRING = "string"; //$NON-NLS-1$
	public static final String TYPE_STR_LIST = "stringList"; //$NON-NLS-1$
	public static final String TYPE_USER_OBJS = "userObjs"; //$NON-NLS-1$
	public static final String TYPE_DEFINED_SYMBOLS = "definedSymbols"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String VALUE_TYPE = "valueType"; //$NON-NLS-1$
	public static final String VALUE_HANDLER = "valueHandler"; //$NON-NLS-1$
	public static final String VALUE_HANDLER_EXTRA_ARGUMENT = "valueHandlerExtraArgument"; //$NON-NLS-1$

	// Schema attribute names for listOptionValue elements
	public static final String LIST_ITEM_VALUE = "value"; //$NON-NLS-1$
	public static final String LIST_ITEM_BUILTIN = "builtIn"; //$NON-NLS-1$

	/**
	 * Returns the parent of this option. This is an object implementing ITool
	 * or IToolChain.
	 *
	 * @return IBuildObject
	 * @since 3.0 - changed return type from ITool to IBuildObject. The method returns
	 * the same object as getOptionHolder().  It is included as a convenience for clients.
	 */
	public IBuildObject getParent();

	/**
	 * Returns the holder (parent) of this option. This may be an object
	 * implenting ITool or IToolChain, which both extend IHoldsOptions
	 *
	 * @return IHoldsOptions
	 * @since 3.0
	 */
	public IHoldsOptions getOptionHolder();

	/**
	 * Returns the <code>IOption</code> that is the superclass of this
	 * option, or <code>null</code> if the attribute was not specified.
	 * 
	 * @return IOption
	 */
	public IOption getSuperClass();
	
	/**
	 * If this option is defined as an enumeration, this function returns
	 * the list of possible values for that enum.
	 * 
	 * If this option is not defined as an enumeration, it returns <code>null</code>.
	 * 
	 * @return String []
	 */
	public String [] getApplicableValues();

	/**
	 * Answers the value for a boolean option.
	 * 
	 * @return boolean
	 * @throws BuildException
	 */
	public boolean getBooleanValue() throws BuildException;
	
	/**
	 * Returns the setting of the browseType attribute
	 * 
	 * @return int
	 */
	public int getBrowseType();
	
	/**
	 * Sets the browseType attribute.
	 * 
	 * @param int
	 */
	public void setBrowseType(int type);
	
	/**
	 * Returns the setting of the resourceFilter attribute
	 * 
	 * @return int
	 */
	public int getResourceFilter();

	/**
	 * Sets the resourceFilter attribute.
	 * 
	 * @param int
	 */
	public void setResourceFilter(int filter);
	
	/**
	 * @return an instance of the class that calculates whether the option is visible,
	 * enabled, and used in command line generation
	 */
	public IOptionApplicability getApplicabilityCalculator();
	
	/**
	 * Answers an array of strings containing the built-in values 
	 * defined for a stringList, includePaths, definedSymbols, or libs
	 * option. If none have been defined, the array will be empty but
	 * never <code>null</code>.
	 * 
	 * @return String[]
	 */
	public String[] getBuiltIns();
		
	/**
	 * Returns the category for this option.
	 * 
	 * @return IOptionCategory
	 */
	public IOptionCategory getCategory();
	
	/**
	 * Sets the category for this option.
	 * 
	 * @param IOptionCategory
	 */
	public void setCategory(IOptionCategory category);
	
	/**
	 * Answers a <code>String</code> containing the actual command line 
	 * option associated with the option
	 * 
	 * @return String
	 */
	public String getCommand();
	
	/**
	 * Sets a <code>String</code> containing the actual command line 
	 * option associated with the option
	 * 
	 * @param String
	 */
	public void setCommand(String command);
	
	/**
	 * Answers a <code>String</code> containing the actual command line
	 * option associated with a Boolean option when the value is False
	 * @return String
	 */
	public String getCommandFalse();
	
	/**
	 * Sets a <code>String</code> containing the actual command line
	 * option associated with a Boolean option when the value is False
	 * 
	 * @param String
	 */
	public void setCommandFalse(String commandFalse);

	/**
	 * Answers a <code>String</code> containing the tooltip
	 * associated with the option
	 * @return String
	 */
	public String getToolTip();
	
	/**
	 * Sets a <code>String</code> containing the tooltip
	 * associated with the option
	 * 
	 * @param String
	 */
	public void setToolTip(String tooltip);

	/**
	 * Answers the user-defined preprocessor symbols. 
	 * 
	 * @return String[]
	 * @throws BuildException
	 */
	public String[] getDefinedSymbols() throws BuildException;

	/**
	 * Answers the command associated with the enumeration id. For
	 * example, if the enumeration id was <code>gnu.debug.level.default</code> 
	 * for the debug level option of the Gnu compiler, and the plugin 
	 * manifest defined that as -g, then the return value would be the 
	 * String "-g"  
	 *  
	 * @return 
	 */
	public String getEnumCommand (String id) throws BuildException;

	/**
	 * Answers the "name" associated with the enumeration id.
	 *  
	 * @return 
	 */
	public String getEnumName (String id) throws BuildException;

	/**
	 * @param name
	 * @return
	 */
	public String getEnumeratedId(String name) throws BuildException;

	/**
	 * Answers an array of <code>String</code> containing the includes paths
	 * defined in the build model.
	 * 
	 * @return String[]
	 * @throws BuildException
	 */
	public String[] getIncludePaths() throws BuildException;
		
	/**
	 * Answers an array or <code>String</code>s containing the libraries
	 * that must be linked into the project.
	 * 
	 * @return String[]
	 * @throws BuildException
	 */
	public String[] getLibraries() throws BuildException ;

	/**
	 * Answers a <code>String</code> containing the unique ID of the selected 
	 * enumeration in an enumerated option. For an option that has not been 
	 * changed by the user, the receiver will answer with the default defined 
	 * in the plugin manifest. If the user has modified the selection, the 
	 * receiver will answer with the overridden selection.
	 * 
	 * @return String
	 * @throws BuildException if the option type is not an enumeration
	 */
	public String getSelectedEnum () throws BuildException;	

	/**
	 * Returns the current value for this option if it is a List of Strings.
	 * 
	 * @return String []
	 * @throws BuildException
	 */
	public String [] getStringListValue() throws BuildException;
	
	/**
	 * Returns the current value for this option if it is a String
	 * 
	 * @return String
	 * @throws BuildException
	 */
	public String getStringValue() throws BuildException;
		
	/**
	 * Answers all of the user-defined object files that must be linked with
	 * the final build target. 
	 * 
	 * @return
	 * @throws BuildException
	 */
	public String [] getUserObjects() throws BuildException;
	
	/**
	 * Returns the raw value of this option.
	 * 
	 * @return Object The Object that contains the raw value of the option.  The type
	 *          of Object is specific to the option type.
	 */
	public Object getValue();
	
	/**
	 * Returns the raw default value of this option.
	 * 
	 * @return Object The Object that contains the raw default value of the option.  The type
	 *          of Object is specific to the option type.
	 */
	public Object getDefaultValue();
	
	/**
	 * Get the type for the value of the option.
	 * 
	 * @return int
	 */
	public int getValueType() throws BuildException;

	/**
	 * Sets the boolean value of the receiver to the value specified in the argument. 
	 * If the receiver is not a reference to a boolean option, method will throw an
	 * exception.
	 * 
	 * @param value
	 * @throws BuildException
	 */
	public void setValue(boolean value) throws BuildException;

	/**
	 * Sets the string value of the receiver to the value specified in the argument.
	 *  
	 * @param value
	 * @throws BuildException
	 */
	public void setValue(String value) throws BuildException;
	
	/**
	 * Sets the value of the receiver to be an array of strings.
	 * 
	 * @param value An array of strings to place in the option reference.
	 * @throws BuildException
	 */
	public void setValue(String [] value) throws BuildException;
	
	/**
	 * Sets the raw value of this option.
	 * 
	 * @param v The Object that contains the raw value of the option.  The type
	 *          of Object is specific to the option type.
	 */
	public void setValue(Object v);
	
	/**
	 * Sets the default value of this option.
	 * 
	 * @param v The Object that contains the default value of the option.  The type
	 *          of Object is specific to the option type.
	 */
	public void setDefaultValue(Object v);

	/**
	 * Sets the value-type of this option.  Use with care.
	 * 
	 * @param type
	 */
	public void setValueType(int type);
	
	/**
	 * Returns the value handler specified for this tool.
	 * @return IManagedOptionValueHandler
	 * @since 3.0
	 */
	public IManagedOptionValueHandler getValueHandler();
	
	/**
	 * Returns the value handlers extra argument specified for this tool
	 * @return String
	 * @since 3.0
	 */
	public String getValueHandlerExtraArgument();
		
	/**
	 * Sets the value handlers extra argument specified for this tool
	 * @param extraArgument
	 * @since 3.0
	 */
	public void setValueHandlerExtraArgument(String extraArgument);

	/**
	 * Returns <code>true</code> if this option was loaded from a manifest file,
	 * and <code>false</code> if it was loaded from a project (.cdtbuild) file.
	 * 
	 * @return boolean
	 */
	public boolean isExtensionElement();

	/**
	 * Returns <code>true</code> if this option only oveerides the value attribute
	 * of its superclass and <code>false</code> if it overrides other attributes.
	 * 
	 * @return boolean
	 * @deprecated
	 */
	public boolean overridesOnlyValue();
	
	/**
	 * Returns <code>true</code> if this option is valid and <code>false</code>
	 * if the option cannot be safely used due to an error in the MBS grammar.
	 * 
	 * @return boolean
	 * @since 3.0
	 * 
	 * @pre Can only be used after Ids in MBS grammar have been resolved by pointers.
	 */
	public boolean isValid();
}
