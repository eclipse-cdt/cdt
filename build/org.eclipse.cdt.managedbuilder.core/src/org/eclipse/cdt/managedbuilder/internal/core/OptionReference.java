/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     ARM Ltd. - basic tooltip support
 *     Petri Tuononen - [321040] Get Library Search Paths
 *     Baltasar Belyavsky (Texas Instruments) - [279633] Custom command-generator support
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.internal.core.SafeStringInterner;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IOptionCommandGenerator;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.cdt.managedbuilder.macros.IOptionContextData;
import org.eclipse.core.runtime.IConfigurationElement;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An <code>OptionReference</code> plays two roles in the managed build model.
 * It is used to store overridden option values in a toolchain specification at
 * the level of a <code>Configuration</code> and it stores user option settings
 * between sessions.
 */
public class OptionReference implements IOption {

	// List of built-in values a tool defines
	private List<String> builtIns;
	// Used for all option references that override the command
	// Note: This is not currently used - don't start using it because
	//       it is not handled in converting from the CDT 2.0 object model
	private String command;
	// The option this reference overrides
	private IOption option;
	// The owner of the reference
	private ToolReference owner;
	// The actual value of the reference
	private Object value;
	private boolean resolved = true;

	/**
	 * This constructor will be called when the receiver is created from
	 * the settings found in an extension point.
	 */
	public OptionReference(ToolReference owner, IManagedConfigElement element) {
		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		resolved = false;

		this.owner = owner;

		owner.addOptionReference(this);

	}

	/**
	 * Constructor called when the option reference is created from an
	 * existing <code>IOption</code>
	 */
	public OptionReference(ToolReference owner, IOption option) {
		this.owner = owner;
		this.option = option;

		// Until the option reference is changed, all values will be extracted from original option
		owner.addOptionReference(this);
	}

	/**
	 * Created from project file.
	 */
	public OptionReference(ToolReference owner, Element element) {
		this.owner = owner;
		try {
			option = owner.getTool().getOptionById(element.getAttribute(ID));
		} catch (NullPointerException e) {
			// Something bad happened
			option = null;
		}

		// Bail now if there's no option for the reference
		if (option == null) {
			return;
		}

		int optValType;
		try {
			optValType = option.getValueType();
		} catch (BuildException e) {return;}

		// Hook the reference up
		owner.addOptionReference(this);

		// value
		switch (optValType) {
			case BOOLEAN:
				value = new Boolean(element.getAttribute(DEFAULT_VALUE));
				break;
			case STRING:
			case ENUMERATED:
				// Pre-2.0 the value was the string for the UI
				// Post-2.0 it is the ID of the enumerated option
				value = element.getAttribute(DEFAULT_VALUE);
				break;
			case STRING_LIST:
			case INCLUDE_PATH:
			case PREPROCESSOR_SYMBOLS:
			case LIBRARIES:
			case OBJECTS:
			case INCLUDE_FILES:
			case LIBRARY_PATHS:
			case LIBRARY_FILES:
			case MACRO_FILES:
			case UNDEF_INCLUDE_PATH:
			case UNDEF_PREPROCESSOR_SYMBOLS:
			case UNDEF_INCLUDE_FILES:
			case UNDEF_LIBRARY_PATHS:
			case UNDEF_LIBRARY_FILES:
			case UNDEF_MACRO_FILES:
				List<String> valueList = new ArrayList<String>();
				NodeList nodes = element.getElementsByTagName(LIST_VALUE);
				for (int i = 0; i < nodes.getLength(); ++i) {
					Node node = nodes.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Boolean isBuiltIn = new Boolean(((Element)node).getAttribute(LIST_ITEM_BUILTIN));
						if (isBuiltIn.booleanValue()) {
							getBuiltInList().add(((Element)node).getAttribute(LIST_ITEM_VALUE));
						} else {
							valueList.add(((Element)node).getAttribute(LIST_ITEM_VALUE));
						}
					}
				}
				value = valueList;
				break;
		}

	}

	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			IManagedConfigElement element = ManagedBuildManager.getConfigElement(this);

			// resolve parent (recursively) before calling methods on it.
			option = owner.getTool().getOptionById(element.getAttribute(ID));
			if (option == null) {
				// error condition probably the result of a misidentified option ID
				resolved = false;
				return;
			}
			if (option instanceof Option) {
				((Option)option).resolveReferences();
			} else if (option instanceof OptionReference) {
				((OptionReference)option).resolveReferences();
			}

			// Note:  The "value" loaded here when the optionReference is read from the manifest file.
			//        This because the "valueType" is only known once the option reference is resolved.
			int optValType;
			try {
				optValType = option.getValueType();
			} catch (BuildException e) {return;}

			// value
			switch (optValType) {
				case BOOLEAN:
					value = new Boolean(element.getAttribute(DEFAULT_VALUE));
					break;
				case STRING:
					value = element.getAttribute(DEFAULT_VALUE);
					break;
				case ENUMERATED:
					String temp = element.getAttribute(DEFAULT_VALUE);
					if (temp != null) {
						value = temp;
					}
					break;
				case STRING_LIST:
				case INCLUDE_PATH:
				case PREPROCESSOR_SYMBOLS:
				case LIBRARIES:
				case OBJECTS:
				case INCLUDE_FILES:
				case LIBRARY_PATHS:
				case LIBRARY_FILES:
				case MACRO_FILES:
				case UNDEF_INCLUDE_PATH:
				case UNDEF_PREPROCESSOR_SYMBOLS:
				case UNDEF_INCLUDE_FILES:
				case UNDEF_LIBRARY_PATHS:
				case UNDEF_LIBRARY_FILES:
				case UNDEF_MACRO_FILES:
					List<String> valueList = new ArrayList<String>();
					IManagedConfigElement[] valueElements = element.getChildren(LIST_VALUE);
					for (int i = 0; i < valueElements.length; ++i) {
						IManagedConfigElement valueElement = valueElements[i];
						Boolean isBuiltIn = new Boolean(valueElement.getAttribute(LIST_ITEM_BUILTIN));
						if (isBuiltIn.booleanValue()) {
							getBuiltInList().add(SafeStringInterner.safeIntern(valueElement.getAttribute(LIST_ITEM_VALUE)));
						}
						else {
							valueList.add(SafeStringInterner.safeIntern(valueElement.getAttribute(LIST_ITEM_VALUE)));
						}
					}
					value = valueList;
					break;
			}
		}
	}

	/**
	 * Persist receiver to project file.
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(ID, option.getId());

		int optValType;
		try {
			optValType = option.getValueType();
		} catch (BuildException e) {
			// TODO: Issue an error message
			return;
		}

		// value
		switch (optValType) {
			case BOOLEAN:
				element.setAttribute(DEFAULT_VALUE, ((Boolean)value).toString());
				break;
			case STRING:
			case ENUMERATED:
				element.setAttribute(DEFAULT_VALUE, (String)value);
				break;
			case STRING_LIST:
			case INCLUDE_PATH:
			case PREPROCESSOR_SYMBOLS:
			case LIBRARIES:
			case OBJECTS:
			case INCLUDE_FILES:
			case LIBRARY_PATHS:
			case LIBRARY_FILES:
			case MACRO_FILES:
			case UNDEF_INCLUDE_PATH:
			case UNDEF_PREPROCESSOR_SYMBOLS:
			case UNDEF_INCLUDE_FILES:
			case UNDEF_LIBRARY_PATHS:
			case UNDEF_LIBRARY_FILES:
			case UNDEF_MACRO_FILES:
				@SuppressWarnings("unchecked")
				ArrayList<String> stringList = (ArrayList<String>)value;
				for (String val : stringList) {
					Element valueElement = doc.createElement(LIST_VALUE);
					valueElement.setAttribute(LIST_ITEM_VALUE, val);
					valueElement.setAttribute(LIST_ITEM_BUILTIN, "false"); //$NON-NLS-1$
					element.appendChild(valueElement);
				}
				// Serialize the built-ins that have been overridden
				if (builtIns != null) {
					for (String builtIn : builtIns) {
						Element valueElement = doc.createElement(LIST_VALUE);
						valueElement.setAttribute(LIST_ITEM_VALUE, builtIn);
						valueElement.setAttribute(LIST_ITEM_BUILTIN, "true"); //$NON-NLS-1$
						element.appendChild(valueElement);
					}
				}
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getOptionContextData(org.eclipse.cdt.managedbuilder.core.IHoldsOptions)
	 */
	@Override
	public IOptionContextData getOptionContextData(IHoldsOptions holder) {
		return option.getOptionContextData(holder);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getApplicableValues()
	 */
	@Override
	public String[] getApplicableValues() {
		return option.getApplicableValues();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCategory()
	 */
	@Override
	public IOptionCategory getCategory() {
		return option.getCategory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCommand()
	 */
	@Override
	public String getCommand() {
		return option.getCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getCommandGenerator()
	 */
	@Override
	public IOptionCommandGenerator getCommandGenerator() {
		return option.getCommandGenerator();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCommandFalse()
	 */
	@Override
	public String getCommandFalse() {
		return option.getCommandFalse();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getToolTip()
	 */
	@Override
	public String getToolTip() {
		return option.getToolTip();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getContextID()
	 */
	@Override
	public String getContextId() {
		return option.getContextId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getDefinedSymbols()
	 */
	@Override
	public String[] getDefinedSymbols() throws BuildException {
		if (value == null)
			return option.getDefinedSymbols();
		else if (getValueType() == PREPROCESSOR_SYMBOLS) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			return list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getEnumCommand(java.lang.String)
	 */
	@Override
	public String getEnumCommand(String id) {
		if (!resolved) {
			resolveReferences();
		}
		if (option != null) {
			try {
				String command = option.getEnumCommand(id);
				return command;
			} catch (BuildException e) {}
		}
		return new String();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getEnumName(java.lang.String)
	 */
	@Override
	public String getEnumName(String id) {
		if (!resolved) {
			resolveReferences();
		}
		if (option != null) {
			try {
				String name = option.getEnumName(id);
				return name;
			} catch (BuildException e) {}
		}
		return new String();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getEnumeratedId(java.lang.String)
	 */
	@Override
	public String getEnumeratedId(String name) {
		if (!resolved) {
			resolveReferences();
		}
		if (option != null) {
			try {
				String id = option.getEnumeratedId(name);
				return id;
			} catch (BuildException e) {}
		}
		return new String();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getId()
	 */
	@Override
	public String getId() {
		// A reference has the same id as the option it references
		return option.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getBaseId()
	 */
	@Override
	public String getBaseId() {
		// A reference has the same id as the option it references
		return option.getBaseId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getIncludePaths()
	 */
	@Override
	public String[] getIncludePaths() throws BuildException {
		if (value == null)
			return option.getIncludePaths();
		else if (getValueType() == INCLUDE_PATH) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			return list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getLibraries()
	 */
	@Override
	public String[] getLibraries() throws BuildException {
		if (value == null)
			return option.getLibraries();
		else if (getValueType() == LIBRARIES) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			return list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getLibraryFiles()
	 */
	@Override
	public String[] getLibraryFiles() throws BuildException {
		if (value == null)
			return option.getLibraryFiles();
		else if (getValueType() == LIBRARY_FILES) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			return list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getLibraryPaths()
	 */
	@Override
	public String[] getLibraryPaths() throws BuildException {
		if (value == null)
			return option.getLibraryPaths();
		else if (getValueType() == LIBRARY_PATHS) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			return list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getName()
	 */
	@Override
	public String getName() {
		// A reference has the same name as the option it references
		return option.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getBooleanValue()
	 */
	@Override
	public boolean getBooleanValue() throws BuildException {
		if (value == null){
			return option.getBooleanValue();
		}
		else if (getValueType() == BOOLEAN) {
			Boolean bool = (Boolean) value;
			return bool.booleanValue();
		} else {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getBrowseType()
	 */
	@Override
	public int getBrowseType() {
		return option.getBrowseType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getBrowseFilterPath()
	 */
	@Override
	public String getBrowseFilterPath() {
		return option.getBrowseFilterPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getBrowseFilterExtensions()
	 */
	@Override
	public String[] getBrowseFilterExtensions() {
		return option.getBrowseFilterExtensions();
	}

	private List<String> getBuiltInList() {
		if (builtIns == null) {
			builtIns = new ArrayList<String>();
		}
		return builtIns;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getBuiltIns()
	 */
	@Override
	public String[] getBuiltIns() {
		List<String> answer = new ArrayList<String>();
		if (builtIns != null) {
			answer.addAll(builtIns);
		}

		// Add the built-ins from the referenced option to the list
		if (option != null) {
			String[] optionBuiltIns = option.getBuiltIns();
			for (int index = 0; index < optionBuiltIns.length; ++index) {
				if (!answer.contains(optionBuiltIns[index])) {
					answer.add(optionBuiltIns[index]);
				}
			}
		}
		return answer.toArray(new String[answer.size()]);
	}

	/**
	 * @return the <code>IOption the reference is for</code>
	 */
	public IOption getOption() {
		// This is an operation that requires the reference to be resolved
		if (!resolved) {
			resolveReferences();
		}
		return option;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getSelectedEnum()
	 */
	@Override
	public String getSelectedEnum() throws BuildException {
		// A reference to an enumerated option stores the ID of the selected enum in its value
		if (value == null) {
			// Return the default defined for the enumeration in the manifest.
			return option.getSelectedEnum();
		} else if (getValueType() == ENUMERATED) {
			// This is a valid ID
			return (String) value;
		} else {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringListValue()
	 */
	@Override
	public String[] getStringListValue() throws BuildException {
		if (value == null)
			return option.getStringListValue();
		else if (getValueType() == STRING_LIST) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			return list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringValue()
	 */
	@Override
	public String getStringValue() throws BuildException {
		if (value == null)
			return option.getStringValue();
		else if (getValueType() == STRING)
			return (String)value;
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getParent()
	 */
	@Override
	public IBuildObject getParent() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getOptionHolder()
	 */
	@Override
	public IHoldsOptions getOptionHolder() {
		return owner;
	}

	/**
	 * Answers the tool reference that contains the receiver.
	 *
	 * @return ToolReference
	 */
	public ToolReference getToolReference() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getUserObjects()
	 */
	@Override
	public String[] getUserObjects() throws BuildException {
		if (value == null)
			return option.getDefinedSymbols();
		else if (getValueType() == OBJECTS) {
			@SuppressWarnings("unchecked")
			ArrayList<String> list = (ArrayList<String>)value;
			return list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getValueType()
	 */
	@Override
	public int getValueType() {
		int optValType;
		try {
			optValType = option.getValueType();
		} catch (BuildException e) {return -1;}

		return optValType;
	}

	/* (non-Javadoc)
	 * Returns the raw value.
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * Answers <code>true</code> if the receiver is a reference to the
	 * <code>IOption</code> specified in the argument, esle answers <code>false</code>.
	 */
	public boolean references(IOption target) {
		if (equals(target)) {
			// we are the target
			return true;
		} else if (option instanceof OptionReference) {
			// check the reference we are overriding
			return ((OptionReference)option).references(target);
		} else {
			// the real reference
			return option.equals(target);
		}
	}

	/**
	 * Sets the boolean value of the receiver to the value specified in the argument.
	 * If the receive is not a reference to a boolean option, method will throw an
	 * exception.
	 */
	@Override
	public void setValue(boolean value) throws BuildException {
		if (getValueType() == BOOLEAN)
			this.value = new Boolean(value);
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	@Override
	public void setValue(String value) throws BuildException {
		// Note that we can still set the human-readable value here
		if (getValueType() == STRING || getValueType() == ENUMERATED) {
			this.value = value;
		} else {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
	}

	/**
	 * Sets the value of the receiver to be an array of items.
	 *
	 * @param value An array of strings to place in the option reference.
	 */
	@Override
	public void setValue(String [] value) throws BuildException {
		if (getValueType() == STRING_LIST
			|| getValueType() == INCLUDE_PATH
			|| getValueType() == PREPROCESSOR_SYMBOLS
			|| getValueType() == LIBRARIES
			|| getValueType() == OBJECTS
			|| getValueType() == INCLUDE_FILES
			|| getValueType() == LIBRARY_PATHS
			|| getValueType() == LIBRARY_FILES
			|| getValueType() == MACRO_FILES
			|| getValueType() == UNDEF_INCLUDE_PATH
			|| getValueType() == UNDEF_PREPROCESSOR_SYMBOLS
			|| getValueType() == UNDEF_INCLUDE_FILES
			|| getValueType() == UNDEF_LIBRARY_PATHS
			|| getValueType() == UNDEF_LIBRARY_FILES
			|| getValueType() == UNDEF_MACRO_FILES
			) {
			// Just replace what the option reference is holding onto
			this.value = new ArrayList<String>(Arrays.asList(value));
		}
		else
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String answer = new String();
		if (option != null) {
			answer += "Reference to " + option.getName();	//$NON-NLS-1$
		}

		if (answer.length() > 0) {
			return answer;
		} else {
			return super.toString();
		}
	}

	/*
	 * The following methods are here in order to implement the new ITool methods.
	 * They should never be called.
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#isExtensionElement()
	 */
	@Override
	public boolean isExtensionElement() {
		return false;
	}

	/* (non-Javadoc)
	 * Sets the raw value.
	 */
	@Override
	public void setValue(Object v) {
		value = v;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setValueType()
	 */
	@Override
	public void setValueType(int type) {
	}

	/* (non-Javadoc)
	 * Returns the raw default value.
	 */
	@Override
	public Object getDefaultValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setValue(Object)
	 */
	@Override
	public void setDefaultValue(Object v) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getSuperClass()
	 */
	@Override
	public IOption getSuperClass() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getResourceFilter()
	 */
	@Override
	public int getResourceFilter() {
		return FILTER_ALL;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getApplicabilityCalculator()
	 */
	@Override
	public IOptionApplicability getApplicabilityCalculator() {
		return option.getApplicabilityCalculator();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#setResourceFilter(int)
	 */
	@Override
	public void setResourceFilter(int filter) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#setBrowseType(int)
	 */
	@Override
	public void setBrowseType(int type) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#setBrowseFilterPath(java.lang.String)
	 */
	@Override
	public void setBrowseFilterPath(String path) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#setBrowseFilterExtensions(java.lang.String[])
	 */
	@Override
	public void setBrowseFilterExtensions(String[] extensions) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setCategory(org.eclipse.cdt.core.build.managed.IOptionCategory)
	 */
	@Override
	public void setCategory(IOptionCategory category) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setCommand(String)
	 */
	@Override
	public void setCommand(String cmd) {
		if (cmd == null && command == null) return;
		if (cmd == null || command == null || !cmd.equals(command)) {
			command = cmd;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setCommandFalse(String)
	 */
	@Override
	public void setCommandFalse(String cmd) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setToolTip(String)
	 */
	@Override
	public void setToolTip(String tooltip) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setContextId(String)
	 */
	@Override
	public void setContextId(String contextId) {
	}

	@Override
	public Version getVersion() {
		return option.getVersion();
	}

	@Override
	public void setVersion(Version version) {
		option.setVersion(version);
	}

	@Override
	public String getManagedBuildRevision() {
		return option.getManagedBuildRevision();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getValueHandlerElement()
	 */
	public IConfigurationElement getValueHandlerElement() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setValueHandlerElement(IConfigurationElement)
	 */
	public void setValueHandlerElement(IConfigurationElement element) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getValueHandler()
	 */
	@Override
	public IManagedOptionValueHandler getValueHandler() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getValueHandlerExtraArgument())
	 */
	@Override
	public String getValueHandlerExtraArgument() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#setValueHandlerExtraArgument(String))
	 */
	@Override
	public void setValueHandlerExtraArgument(String extraArgument) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getFieldEditorId()
	 */
	@Override
	public String getFieldEditorId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#getFieldEditorExtraArgument()
	 */
	@Override
	public String getFieldEditorExtraArgument() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#setFieldEditorExtraArgument(java.lang.String)
	 */
	@Override
	public void setFieldEditorExtraArgument(String extraArgument) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOption#isValid()
	 */
	@Override
	public boolean isValid() {
		return option.isValid();
	}

	@Override
	public String[] getBasicStringListValue() throws BuildException {
		if (getBasicValueType() != STRING_LIST) {
			throw new BuildException(ManagedMakeMessages.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
		@SuppressWarnings("unchecked")
		ArrayList<String> v = (ArrayList<String>)getValue();
		if (v == null) {
			return new String[0];
		}

		return v.toArray(new String[v.size()]);
	}

	@Override
	public int getBasicValueType() throws BuildException {
		switch(getValueType()){
		case IOption.BOOLEAN:
			return IOption.BOOLEAN;
		case IOption.STRING:
			return IOption.STRING;
		case IOption.ENUMERATED:
			return IOption.ENUMERATED;
		default:
			return IOption.STRING_LIST;
		}
	}

	@Override
	public OptionStringValue[] getBasicStringListValueElements()
			throws BuildException {
		String[] str = getBasicStringListValue();
		OptionStringValue[] ve = new OptionStringValue[str.length];
		for(int i = 0; i < str.length; i++){
			ve[i] = new OptionStringValue(str[i]);
		}
		return ve;
	}
}
