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

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class Configuration extends BuildObject implements IConfiguration {

	private ITarget target;
	private IConfiguration parent;
	private List toolReferences;

	/**
	 * A fresh new configuration for a target.
	 * 
	 * @param target
	 * @param id
	 */	
	public Configuration(Target target, String id) {
		this.id = id;
		this.target = target;
		
		target.addConfiguration(this);
	}

	/**
	 * Create a new configuration based on one already defined.
	 * 
	 * @param target The <code>Target</code> the receiver will be added to.
	 * @param parent The <code>IConfiguration</code> to copy the settings from.
	 * @param id A unique ID for the configuration.
	 */
	public Configuration(Target target, IConfiguration parent, String id) {
		this.id = id;
		this.name = parent.getName();
		this.target = target;
		this.parent = parent;
		
		target.addConfiguration(this);
	}

	/**
	 * Create a new <code>Configuration</code> based on the specification in the plugin manifest.
	 * 
	 * @param target The <code>Target</code> the receiver will be added to.
	 * @param element The element from the manifest that contains the default configuration settings.
	 */
	public Configuration(Target target, IConfigurationElement element) {
		this.target = target;
		
		// id
		setId(element.getAttribute(IConfiguration.ID));
		
		// hook me up
		target.addConfiguration(this);
		
		// name
		setName(element.getAttribute(IConfiguration.NAME));

		IConfigurationElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IConfigurationElement configElement = configElements[l];
			if (configElement.getName().equals(IConfiguration.TOOL_REF)) {
				new ToolReference(this, configElement);
			}
		}
	}
	
	/**
	 * Build a configuration from the project manifest file.
	 * 
	 * @param target The <code>Target</code> the configuration belongs to. 
	 * @param element The element from the manifest that contains the overridden configuration information.
	 */
	public Configuration(Target target, Element element) {
		this.target = target;
		
		// id
		setId(element.getAttribute(IConfiguration.ID));
		
		// hook me up
		target.addConfiguration(this);
		
		// name
		if (element.hasAttribute(IConfiguration.NAME))
			setName(element.getAttribute(IConfiguration.NAME));
		
		if (element.hasAttribute(IConfiguration.PARENT)) {
			// See if the target has a parent
			ITarget targetParent = target.getParent();
			// If so, then get my parent from it
			if (targetParent != null) {
				parent = targetParent.getConfiguration(element.getAttribute(IConfiguration.PARENT));
			}
			else {
				parent = null;
			}
		}
		
		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(IConfiguration.TOOL_REF)) {
				new ToolReference(this, (Element)configElement);
			}
		}
	
	}
	
	/**
	 * Persist receiver to project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(IConfiguration.ID, id);
		
		if (name != null)
			element.setAttribute(IConfiguration.NAME, name);
			
		if (parent != null)
			element.setAttribute(IConfiguration.PARENT, parent.getId());
		
		for (int i = 0; i < getToolReferences().size(); ++i) {
			ToolReference toolRef = (ToolReference)getToolReferences().get(i);
			Element toolRefElement = doc.createElement(IConfiguration.TOOL_REF);
			element.appendChild(toolRefElement);
			toolRef.serialize(doc, toolRefElement);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getName()
	 */
	public String getName() {
		return (name == null && parent != null) ? parent.getName() : name;
	}

	/*
	 * @return
	 */
	private List getToolReferences() {
		if (toolReferences == null) {
			toolReferences = new ArrayList();
		}
		return toolReferences;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getTools()
	 */
	public ITool[] getTools() {
		ITool[] tools = parent != null
			? parent.getTools()
			: target.getTools();
		
		// Replace tools with overrides
		for (int i = 0; i < tools.length; ++i) {
			ToolReference ref = getToolReference(tools[i]);
			if (ref != null)
				tools[i] = ref;
		}
		
		return tools;
	}

	/**
	 * @param targetElement
	 */
	public void reset(IConfigurationElement element) {
		// I just need to reset the tool references
		getToolReferences().clear();
		IConfigurationElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IConfigurationElement configElement = configElements[l];
			if (configElement.getName().equals(IConfiguration.TOOL_REF)) {
				new ToolReference(this, configElement);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getParent()
	 */
	public IConfiguration getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getTarget()
	 */
	public ITarget getTarget() {
		return (target == null && parent != null) ? parent.getTarget() : target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getOwner()
	 */
	public IResource getOwner() {
		return getTarget().getOwner();
	}

	/**
	 * Returns the reference for a given tool.
	 * 
	 * @param tool
	 * @return
	 */
	private ToolReference getToolReference(ITool tool) {
		for (int i = 0; i < getToolReferences().size(); ++i) {
			ToolReference toolRef = (ToolReference)getToolReferences().get(i);
			if (toolRef.references(tool))
				return toolRef;
		}
		return null;
	}
	
	public void addToolReference(ToolReference toolRef) {
		getToolReferences().add(toolRef);
	}
	
	public OptionReference createOptionReference(IOption option) {
		if (option instanceof OptionReference) {
			OptionReference optionRef = (OptionReference)option;
			ToolReference toolRef = optionRef.getToolReference();
			if (toolRef.getConfiguration().equals(this))
				return optionRef;
			else {
				toolRef = new ToolReference(this, toolRef);
				return toolRef.createOptionReference(option);
			}
		} else {
			ToolReference toolRef = getToolReference(option.getTool());
			if (toolRef == null)
				toolRef = new ToolReference(this, option.getTool());
			return toolRef.createOptionReference(option);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, boolean)
	 */
	public void setOption(IOption option, boolean value) throws BuildException {
		// Is there a delta
		if (option.getBooleanValue() != value)
			createOptionReference(option).setValue(value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, java.lang.String)
	 */
	public void setOption(IOption option, String value) throws BuildException {
		String oldValue;
		// Check whether this is an enumerated option
		if (option.getValueType() == IOption.ENUMERATED) {
			oldValue = option.getSelectedEnum();
		}
		else {
			oldValue = option.getStringValue(); 
		}
		if (!oldValue.equals(value))
			createOptionReference(option).setValue(value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, java.lang.String[])
	 */
	public void setOption(IOption option, String[] value) throws BuildException {
		// Is there a delta
		String[] oldValue;
		switch (option.getValueType()) {
			case IOption.STRING_LIST :
				oldValue = option.getStringListValue();
				break;
			case IOption.INCLUDE_PATH :
				oldValue = option.getIncludePaths();
				break;
			case IOption.PREPROCESSOR_SYMBOLS :
				oldValue = option.getDefinedSymbols();
				break;
			case IOption.LIBRARIES :
				oldValue = option.getLibraries();
				break;
			default :
				oldValue = new String[0];
				break;
		}
		if(!Arrays.equals(value, oldValue))
			createOptionReference(option).setValue(value);
	}


}
