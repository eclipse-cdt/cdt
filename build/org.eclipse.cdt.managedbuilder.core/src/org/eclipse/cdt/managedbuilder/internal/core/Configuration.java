/**********************************************************************
 * Copyright (c) 2003,2004 IBM Rational Software and others.
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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolReference;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Configuration extends BuildObject implements IConfiguration {
	private boolean isDirty = false;
	private IConfiguration parent;
	private boolean rebuildNeeded = false;
	private boolean resolved = true;
	private ITarget target;
	private List toolReferences;

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
			if (configElement.getNodeName().equals(IConfiguration.TOOLREF_ELEMENT_NAME)) {
				new ToolReference(this, (Element)configElement);
			}
		}
	
	}

	/**
	 * Create a new configuration based on one already defined.
	 * 
	 * @param target The <code>Target</code> the receiver will be added to.
	 * @param parentConfig The <code>IConfiguration</code> to copy the settings from.
	 * @param id A unique ID for the configuration.
	 */
	public Configuration(Target target, IConfiguration parentConfig, String id) {
		this.id = id;
		this.name = parentConfig.getName();
		this.target = target;

		// If this contructor is called to clone an existing 
		// configuration, the parent of the parent should be stored. 
		// As of 2.0, there is still one single level of inheritence to
		// worry about
		parent = parentConfig.getParent() == null ? parentConfig : parentConfig.getParent();
		
		// Check that the tool and the project match
		IProject project = (IProject) target.getOwner();
		
		// Get the tool references from the parent
		List parentToolRefs = ((Configuration)parentConfig).getLocalToolReferences();
		Iterator iter = parentToolRefs.listIterator();
		while (iter.hasNext()) {
			ToolReference toolRef = (ToolReference)iter.next();

			// Make a new ToolReference based on the tool in the ref
			ITool parentTool = toolRef.getTool();
			ToolReference newRef = new ToolReference(this, parentTool);
			
			// The reference may have a different command than the parent tool
			String refCmd = toolRef.getToolCommand(); 
			if (!refCmd.equals(parentTool.getToolCommand())) {
				newRef.setToolCommand(refCmd);
			}
			
			List optRefs = toolRef.getOptionReferenceList();
			Iterator optIter = optRefs.listIterator();
			while (optIter.hasNext()) {
				OptionReference optRef = (OptionReference)optIter.next();
				IOption opt = optRef.getOption();
				try {
					switch (opt.getValueType()) {
						case IOption.BOOLEAN:
							new OptionReference(newRef, opt).setValue(optRef.getBooleanValue());
							break;
						case IOption.STRING:
							new OptionReference(newRef, opt).setValue(optRef.getStringValue());
							break;
						case IOption.ENUMERATED:
							new OptionReference(newRef, opt).setValue(optRef.getSelectedEnum());
							break;
						case IOption.STRING_LIST :
							new OptionReference(newRef, opt).setValue(optRef.getStringListValue());
							break;
						case IOption.INCLUDE_PATH :
							new OptionReference(newRef, opt).setValue(optRef.getIncludePaths());
							break;
						case IOption.PREPROCESSOR_SYMBOLS :
							new OptionReference(newRef, opt).setValue(optRef.getDefinedSymbols());
							break;
						case IOption.LIBRARIES :
						new OptionReference(newRef, opt).setValue(optRef.getLibraries());
							break;
						case IOption.OBJECTS :
						new OptionReference(newRef, opt).setValue(optRef.getUserObjects());
							break;
					}
				} catch (BuildException e) {
					continue;
				}
			}
		}
		
		target.addConfiguration(this);
	}

	/**
	 * Create a new <code>Configuration</code> based on the specification in the plugin manifest.
	 * 
	 * @param target The <code>Target</code> the receiver will be added to.
	 * @param element The element from the manifest that contains the default configuration settings.
	 */
	public Configuration(Target target, IManagedConfigElement element) {
		this.target = target;
		
		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		resolved = false;
		
		// id
		setId(element.getAttribute(IConfiguration.ID));
		
		// hook me up
		target.addConfiguration(this);
		
		// name
		setName(element.getAttribute(IConfiguration.NAME));

		IManagedConfigElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IManagedConfigElement configElement = configElements[l];
			if (configElement.getName().equals(IConfiguration.TOOLREF_ELEMENT_NAME)) {
				new ToolReference(this, configElement);
			}
		}
	}
	
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

	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
//			IManagedConfigElement element = ManagedBuildManager.getConfigElement(this);
			Iterator refIter = getLocalToolReferences().iterator();
			while (refIter.hasNext()) {
				ToolReference ref = (ToolReference)refIter.next();
				ref.resolveReferences();
			}
		}
	}
	
	/**
	 * Adds a tool reference to the receiver.
	 * 
	 * @param toolRef
	 */
	public void addToolReference(ToolReference toolRef) {
		getLocalToolReferences().add(toolRef);
	}
	
	/* (non-Javadoc)
	 * @param option
	 * @return
	 */
	private OptionReference createOptionReference(IOption option) {
		// The option may already be a reference created to hold user settings
		if (option instanceof OptionReference) {
			// The option reference belongs to an existing tool reference
			OptionReference optionRef = (OptionReference)option;
			ToolReference toolRef = optionRef.getToolReference();
			// That tool reference may belong to a target or to the configuration
			if (toolRef.ownedByConfiguration(this))
				return optionRef;
			else {
				// Make a copy so the settings can be saved
				toolRef = new ToolReference(this, toolRef);
				return toolRef.createOptionReference(option);
			}
		} else {
			// Find out if a tool reference already exists
			ToolReference toolRef = (ToolReference) getToolReference(option.getTool());
			if (toolRef == null) {
				toolRef = new ToolReference(this, option.getTool());
			} else {
				// The reference may belong to the target
				if (!toolRef.ownedByConfiguration(this)) {
					toolRef = new ToolReference(this, toolRef);
				}
			}
		
			return toolRef.createOptionReference(option);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getFilteredTools(org.eclipse.core.resources.IProject)
	 */
	public ITool[] getFilteredTools(IProject project) {
		ITool[] localTools = getTools();
		Vector tools = new Vector(localTools.length);
		for (int i = 0; i < localTools.length; i++) {
			ITool tool = localTools[i];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							tools.add(tool);
						}
						break;
					case ITool.FILTER_CC:
						if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							tools.add(tool);
						}
						break;
					case ITool.FILTER_BOTH:
						tools.add(tool);
						break;
					default:
						break;
				}
			} catch (CoreException e) {
				continue;
			}
		}
		
		// Answer the filtered tools as an array
		return (ITool[])tools.toArray(new ITool[tools.size()]);
	}

	/* (non-javadoc)
	 * A safety method to avoid NPEs. It answers the tool reference list in the 
	 * receiver. It does not look at the tool references defined in the parent.
	 * 
	 * @return List
	 */
	protected List getLocalToolReferences() {
		if (toolReferences == null) {
			toolReferences = new ArrayList();
		}
		return toolReferences;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getName()
	 */
	public String getName() {
		return (name == null && parent != null) ? parent.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getTools()
	 */
	public ITool[] getTools() {
		ITool[] tools = parent != null
			? parent.getTools()
			: target.getTools();
		
		// Validate that the tools correspond to the nature
		IProject project = (IProject)target.getOwner();
		if (project != null) {
			List validTools = new ArrayList();
			
			// The target is associated with a real project
			for (int i = 0; i < tools.length; ++i) {
				ITool tool = tools[i];
				// Make sure the tool filter and project nature agree
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						try {
							if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								validTools.add(tool);
							}
						} catch (CoreException e) {
							continue;
						}
						break;
					case ITool.FILTER_CC:
						try {
							if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								validTools.add(tool);
							}
						} catch (CoreException e) {
							continue;
						}
						break;
					case ITool.FILTER_BOTH:
						validTools.add(tool);
						break;
				} 
			}
			// Now put the valid tools back into the array
			tools = (ITool[]) validTools.toArray(new ITool[validTools.size()]);			
		}
		
		// Replace tools with local overrides
		for (int i = 0; i < tools.length; ++i) {
			IToolReference ref = getToolReference(tools[i]);
			if (ref != null)
				tools[i] = ref;
		}
		
		return tools;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isDirty()
	 */
	public boolean isDirty() {
		// If I need saving, just say yes
		if (isDirty) return true;
		
		// Otherwise see if any tool references need saving
		Iterator iter = getLocalToolReferences().listIterator();
		while (iter.hasNext()) {
			IToolReference ref = (IToolReference) iter.next();
			if (ref.isDirty()) return true;
		}
		
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#needsRebuild()
	 */
	public boolean needsRebuild() {
		return rebuildNeeded;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getParent()
	 */
	public IConfiguration getParent() {
		return parent;
	}
	
	/* (non-javadoc)
	 * 
	 * @param tool
	 * @return List
	 */
	protected List getOptionReferences(ITool tool) {
		List references = new ArrayList();
		
		// Get all the option references I add for this tool
		IToolReference toolRef = getToolReference(tool);
		if (toolRef != null) {
			references.addAll(toolRef.getOptionReferenceList());
		}
		
		// See if there is anything that my parents add that I don't
		if (parent != null) {
			List temp = ((Configuration)parent).getOptionReferences(tool);
			Iterator iter = temp.listIterator();
			while (iter.hasNext()) {
				OptionReference ref = (OptionReference) iter.next();
				if (!references.contains(ref)) {
					references.add(ref);
				}
			}
		}
		
		return references;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getToolById(java.lang.String)
	 */
	public ITool getToolById(String id) {
		ITool[] tools = parent != null
		? parent.getTools()
		: target.getTools();

		// Replace tools with local overrides
		for (int i = 0; i < tools.length; ++i) {
			IToolReference ref = getToolReference(tools[i]);
			if (ref != null)
				tools[i] = ref;
		}

		// Search the result for the ID
		for (int index = tools.length - 1; index >= 0; --index) {
			if (tools[index].getId().equals(id)) {
				return tools[index];
			}
		}

		return null;
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

	/* (non-Javadoc)
	 * Returns the reference for a given tool or <code>null</code> if one is not
	 * found.
	 * 
	 * @param tool
	 * @return ToolReference
	 */
	private IToolReference getToolReference(ITool tool) {
		// Sanity
		if (tool == null) return null;

		// See if the receiver has a reference to the tool
		Iterator iter = getLocalToolReferences().listIterator();
		while (iter.hasNext()) {
			ToolReference temp = (ToolReference)iter.next(); 
			if (temp.references(tool)) {
				return temp;
			}
		}
		
		// See if the target that the receiver belongs to has a reference to the tool
		ITool[] targetTools = target.getTools();
		for (int index = targetTools.length - 1; index >= 0; --index) {
			ITool targetTool = targetTools[index];
			if (targetTool instanceof ToolReference) {
				if (((ToolReference)targetTool).references(tool)) {
					return (ToolReference)targetTool;
				}
			}
		}
		return null;
	}
	
	/**
	 * @param targetElement
	 */
	public void reset(IManagedConfigElement element) {
		// I just need to reset the tool references
		getLocalToolReferences().clear();
		IManagedConfigElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IManagedConfigElement configElement = configElements[l];
			if (configElement.getName().equals(IConfiguration.TOOLREF_ELEMENT_NAME)) {
				ToolReference ref = new ToolReference(this, configElement);
				ref.resolveReferences();
			}
		}
		isDirty = true;
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
		
		// Serialize only the tool references defined in the configuration
		Iterator iter = getLocalToolReferences().listIterator();
		while (iter.hasNext()) {
			ToolReference toolRef = (ToolReference) iter.next();
			Element toolRefElement = doc.createElement(IConfiguration.TOOLREF_ELEMENT_NAME);
			element.appendChild(toolRefElement);
			toolRef.serialize(doc, toolRefElement);
		}
		
		// I am clean now
		isDirty = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		// Override the dirty flag
		this.isDirty = isDirty;
		// And do the same for the tool references
		Iterator iter = getLocalToolReferences().listIterator();
		while (iter.hasNext()) {
			((ToolReference)iter.next()).setDirty(isDirty);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, boolean)
	 */
	public void setOption(IOption option, boolean value) throws BuildException {
		// Is there a delta
		if (option.getBooleanValue() != value) {
			createOptionReference(option).setValue(value);
			isDirty = true;
			rebuildNeeded = true;
		}
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
		if (oldValue != null && !oldValue.equals(value)) {
			createOptionReference(option).setValue(value);
			isDirty = true;
			rebuildNeeded = true;
		}
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
			case IOption.OBJECTS :
				oldValue = option.getUserObjects();
				break;
			default :
				oldValue = new String[0];
				break;
		}
		if(!Arrays.equals(value, oldValue)) {
			createOptionReference(option).setValue(value);
			isDirty = true;
			rebuildNeeded = true;
		} 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setRebuildState(boolean)
	 */
	public void setRebuildState(boolean rebuild) {
		rebuildNeeded = rebuild;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setToolCommand(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String)
	 */
	public void setToolCommand(ITool tool, String command) {
		// Make sure the command is different
		if (command != null) {
			// Does this config have a ref to the tool
			IToolReference ref = getToolReference(tool);
			if (ref == null) {
				// Then make one
				ref = new ToolReference(this, tool);
			}
			// Set the ref's command
			if (ref != null) {
				isDirty = ref.setToolCommand(command);
				rebuildNeeded = isDirty;
			}
		}
	}

}
