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
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class OptionReference implements IOption {

	// List of built-in values a tool defines
	private List builtIns;
	// Used for all option references that override the command
	private String command;
	// The option this reference overrides
	private IOption option;
	// The owner of the reference
	private ToolReference owner;
	// The actual value of the reference
	private Object value;

	/**
	 * Constructor called when the option reference is created from an 
	 * existing <code>IOption</code>
	 * 
	 * @param owner
	 * @param option
	 */
	public OptionReference(ToolReference owner, IOption option) {
		this.owner = owner;
		this.option = option;
		
		// Until the option reference is changed, all values will be extracted from original option		
		owner.addOptionReference(this);
	}

	/**
	 * This constructor will be called when the receiver is created from 
	 * the settings found in an extension point.
	 * 
	 * @param owner
	 * @param element
	 */
	public OptionReference(ToolReference owner, IConfigurationElement element) {
		this.owner = owner;
		option = owner.getTool().getOption(element.getAttribute(ID));
		
		owner.addOptionReference(this);

		// value
		switch (option.getValueType()) {
			case BOOLEAN:
				value = new Boolean(element.getAttribute(DEFAULT_VALUE));
				break;
			case STRING:
				value = element.getAttribute(DEFAULT_VALUE);
				break;
			case ENUMERATED:
				String temp = element.getAttribute(DEFAULT_VALUE);
				if (temp == null) {
					try {
						temp = option.getSelectedEnum();
					} catch (BuildException e) {
						temp = new String();
					}
				}
				value = temp;
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
					}				}
				value = valueList;
				break;
		}
	}

	/**
	 * Created from project file.
	 * 
	 * @param owner
	 * @param element
	 */
	public OptionReference(ToolReference owner, Element element) {
		this.owner = owner;	
		option = owner.getTool().getOption(element.getAttribute(ID));
		
		// Bail now if there's no option for the reference
		if (option == null) {
			return;
		}
		
		// Hook the reference up		
		owner.addOptionReference(this);

		// value
		switch (option.getValueType()) {
			case BOOLEAN:
				value = new Boolean(element.getAttribute(DEFAULT_VALUE));
				break;
			case STRING:
			case ENUMERATED:
				value = (String) element.getAttribute(DEFAULT_VALUE);
				break;
			case STRING_LIST:
			case INCLUDE_PATH:
			case PREPROCESSOR_SYMBOLS:
			case LIBRARIES:
			case OBJECTS:
				List valueList = new ArrayList();
				builtIns = new ArrayList();
				NodeList nodes = element.getElementsByTagName(LIST_VALUE);
				for (int i = 0; i < nodes.getLength(); ++i) {
					Node node = nodes.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Boolean isBuiltIn = new Boolean(((Element)node).getAttribute(LIST_ITEM_BUILTIN));
						if (isBuiltIn.booleanValue()) {
							builtIns.add(((Element)node).getAttribute(LIST_ITEM_VALUE));
						} else {
							valueList.add(((Element)node).getAttribute(LIST_ITEM_VALUE));
						}
					}
				}
				value = valueList;
				break;
		}

	}
	
	/**
	 * Persist receiver to project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(ID, option.getId());
		
		// value
		switch (option.getValueType()) {
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
				ArrayList stringList = (ArrayList)value;
				ListIterator iter = stringList.listIterator();
				while (iter.hasNext()) {
					Element valueElement = doc.createElement(LIST_VALUE);
					valueElement.setAttribute(LIST_ITEM_VALUE, (String)iter.next());
					valueElement.setAttribute(LIST_ITEM_BUILTIN, "false"); //$NON-NLS-1$
					element.appendChild(valueElement);
				}
				// Serialize the built-ins that have been overridden
				if (builtIns != null) {
					iter = builtIns.listIterator();
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getApplicableValues()
	 */
	public String[] getApplicableValues() {
		return option.getApplicableValues();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCategory()
	 */
	public IOptionCategory getCategory() {
		return option.getCategory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCommand()
	 */
	public String getCommand() {
		return option.getCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getDefinedSymbols()
	 */
	public String[] getDefinedSymbols() throws BuildException {
		if (value == null)
			return option.getDefinedSymbols();
		else if (getValueType() == PREPROCESSOR_SYMBOLS) {
			ArrayList list = (ArrayList)value;
			return (String[]) list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getEnumCommand(java.lang.String)
	 */
	public String getEnumCommand(String name) {
		return option.getEnumCommand(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getId()
	 */
	public String getId() {
		// A reference has the same id as the option it references
		return option.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getIncludePaths()
	 */
	public String[] getIncludePaths() throws BuildException {
		if (value == null)
			return option.getIncludePaths();
		else if (getValueType() == INCLUDE_PATH) {
			ArrayList list = (ArrayList)value;
			return (String[]) list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getLibraries()
	 */
	public String[] getLibraries() throws BuildException {
		if (value == null)
			return option.getLibraries();
		else if (getValueType() == LIBRARIES) {
			ArrayList list = (ArrayList)value;
			return (String[]) list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getName()
	 */
	public String getName() {
		// A reference has the same name as the option it references
		return option.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getBooleanValue()
	 */
	public boolean getBooleanValue() throws BuildException {
		if (value == null){
			return option.getBooleanValue();
		} 
		else if (getValueType() == BOOLEAN) {
			Boolean bool = (Boolean) value;
			return bool.booleanValue();
		} else {
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getBuiltIns()
	 */
	public String[] getBuiltIns() {
		// Return any overridden built-ins here, or the default set 
		// from the option this is a reference to
		return builtIns == null ?
			   option.getBuiltIns():
			   (String[])builtIns.toArray(new String[builtIns.size()]);
	}

	public IOption getOption() {
		return option;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getDefaultEnumValue()
	 */
	public String getSelectedEnum() throws BuildException {
		if (value == null) {
			// Return the default defined for the enumeration in the manifest.
			return option.getSelectedEnum();
		} else if (getValueType() == ENUMERATED) {
			// Value will contain the human-readable name of the enum 
			return (String) value;
		} else {
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringListValue()
	 */
	public String[] getStringListValue() throws BuildException {
		if (value == null)
			return option.getStringListValue();
		else if (getValueType() == STRING_LIST) {
			ArrayList list = (ArrayList)value;
			return (String[]) list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringValue()
	 */
	public String getStringValue() throws BuildException {
		if (value == null)
			return option.getStringValue();
		else if (getValueType() == STRING)
			return (String)value;
		else
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getTool()
	 */
	public ITool getTool() {
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
	public String[] getUserObjects() throws BuildException {
		if (value == null)
			return option.getDefinedSymbols();
		else if (getValueType() == OBJECTS) {
			ArrayList list = (ArrayList)value;
			return (String[]) list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getValueType()
	 */
	public int getValueType() {
		return option.getValueType();
	}

	/**
	 * Answers <code>true</code> if the receiver is a reference to the 
	 * <code>IOption</code> specified in the argument, esle answers <code>false</code>.
	 * 
	 * @param target
	 * @return boolean
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
	 * 
	 * @param value
	 * @throws BuildException
	 */
	public void setValue(boolean value) throws BuildException {
		if (getValueType() == BOOLEAN)
			this.value = new Boolean(value);
		else
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

	/**
	 * @param value
	 * @throws BuildException
	 */
	public void setValue(String value) throws BuildException {
		if (getValueType() == STRING || getValueType() == ENUMERATED)
			this.value = value;
		else
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}
	
	/**
	 * Sets the value of the receiver to be an array of items.
	 * 
	 * @param value An array of strings to place in the option reference.
	 * @throws BuildException
	 */
	public void setValue(String [] value) throws BuildException {
		if (getValueType() == STRING_LIST
			|| getValueType() == INCLUDE_PATH
			|| getValueType() == PREPROCESSOR_SYMBOLS
			|| getValueType() == LIBRARIES
			|| getValueType() == OBJECTS) {
			// Just replace what the option reference is holding onto 
			this.value = new ArrayList(Arrays.asList(value));
		}
		else
			throw new BuildException(ManagedBuilderCorePlugin.getResourceString("Option.error.bad_value_type")); //$NON-NLS-1$
	}

}
