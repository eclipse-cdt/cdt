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
package org.eclipse.cdt.internal.core.build.managed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.core.build.managed.BuildException;
import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class OptionReference implements IOption {

	// Used for all option references that override the command
	private String command;
	// The option this reference overrides
	private IOption option;
	// The owner of the reference
	private ToolReference owner;
	// The actual value of the reference
	private Object value;

	/**
	 * Created internally.
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
	 * Created from extension.
	 * 
	 * @param owner
	 * @param element
	 */
	public OptionReference(ToolReference owner, IConfigurationElement element) {
		this.owner = owner;
		option = owner.getTool().getOption(element.getAttribute("id"));
		
		owner.addOptionReference(this);

		// value
		switch (option.getValueType()) {
			case IOption.BOOLEAN:
				value = new Boolean(element.getAttribute("defaultValue"));
				break;
			case IOption.STRING:
				value = element.getAttribute("defaultValue");
				break;
			case IOption.ENUMERATED:
				value = option.getSelectedEnum();
				break;
			case IOption.STRING_LIST:
				List valueList = new ArrayList();
				IConfigurationElement[] valueElements = element.getChildren("optionValue");
				for (int i = 0; i < valueElements.length; ++i) {
					valueList.add(valueElements[i].getAttribute("value"));
				}
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
		option = owner.getTool().getOption(element.getAttribute("id"));
		
		owner.addOptionReference(this);

		// value
		switch (option.getValueType()) {
			case IOption.BOOLEAN:
				value = new Boolean(element.getAttribute("defaultValue"));
				break;
			case IOption.STRING:
			case IOption.ENUMERATED:
				value = (String) element.getAttribute("defaultValue");
				break;
			case IOption.STRING_LIST:
				List valueList = new ArrayList();
				NodeList nodes = element.getElementsByTagName("optionValue");
				for (int i = 0; i < nodes.getLength(); ++i) {
					Node node = nodes.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						valueList.add(((Element)node).getAttribute("value"));
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
		element.setAttribute("id", option.getId());
		
		// value
		switch (option.getValueType()) {
			case IOption.BOOLEAN:
				element.setAttribute("defaultValue", ((Boolean)value).toString());
				break;
			case IOption.STRING:
			case IOption.ENUMERATED:
				element.setAttribute("defaultValue", (String)value);
				break;
			case IOption.STRING_LIST:
				ArrayList stringList = (ArrayList)value;
				ListIterator iter = stringList.listIterator();
				while (iter.hasNext()) {
					Element valueElement = doc.createElement("optionValue");
					valueElement.setAttribute("value", (String)iter.next());
					element.appendChild(valueElement);
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
	 * @see org.eclipse.cdt.core.build.managed.IOption#getEnumCommand(java.lang.String)
	 */
	public String getEnumCommand(String name) {
		return option.getEnumCommand(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getId()
	 */
	public String getId() {
		return option.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getName()
	 */
	public String getName() {
		return option.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getBooleanValue()
	 */
	public boolean getBooleanValue() throws BuildException {
		if (value == null){
			return option.getBooleanValue();
		} 
		else if (getValueType() == IOption.BOOLEAN) {
			Boolean bool = (Boolean) value;
			return bool.booleanValue();
		} else {
			throw new BuildException("bad value type");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getDefaultEnumValue()
	 */
	public String getSelectedEnum() {
		if (value == null) {
			// Return the default defined for the enumeration in the manifest.
			return option.getSelectedEnum();
		} else {
			// Value will contain the human-readable name of the enum 
			return (String) value;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringListValue()
	 */
	public String[] getStringListValue() throws BuildException {
		if (value == null)
			return option.getStringListValue();
		else if (getValueType() == IOption.STRING_LIST) {
			ArrayList list = (ArrayList)value;
			return (String[]) list.toArray(new String[list.size()]);
		}
		else
			throw new BuildException("bad value type");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringValue()
	 */
	public String getStringValue() throws BuildException {
		if (value == null)
			return option.getStringValue();
		else if (getValueType() == IOption.STRING)
			return (String)value;
		else
			throw new BuildException("bad value type");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getTool()
	 */
	public ITool getTool() {
		return owner;
	}

	public ToolReference getToolReference() {
		return owner;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getValueType()
	 */
	public int getValueType() {
		return option.getValueType();
	}

	public boolean references(IOption target) {
		if (equals(target))
			// we are the target
			return true;
		else if (option instanceof OptionReference)
			// check the reference we are overriding
			return ((OptionReference)option).references(target);
		else
			// the real reference
			return option.equals(target);
	}

	/**
	 * @param value
	 */
	public void setValue(boolean value) throws BuildException {
		if (getValueType() == IOption.BOOLEAN)
			this.value = new Boolean(value);
		else
			throw new BuildException("bad value type");
	}

	public void setValue(String value) throws BuildException {
		if (getValueType() == IOption.STRING || getValueType() == IOption.ENUMERATED)
			this.value = value;
		else
			throw new BuildException("bad value type");
	}
	
	/**
	 * Sets the value of the receiver to be an array of items.
	 * 
	 * @param value An array of strings to place in the option reference.
	 * @throws BuildException
	 */
	public void setValue(String [] value) throws BuildException {
		if (getValueType() == IOption.STRING_LIST) {
			// Just replace what the option reference is holding onto 
			this.value = new ArrayList(Arrays.asList(value));
		}
		else
			throw new BuildException("bad value type");
	}
}
