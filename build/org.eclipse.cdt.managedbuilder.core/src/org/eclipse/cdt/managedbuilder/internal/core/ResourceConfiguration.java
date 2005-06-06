/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ResourceConfiguration extends BuildObject implements IResourceConfiguration {

	private static final String EMPTY_STRING = new String();

	//  Parent and children
	private IConfiguration parent;
	private List toolList;
	private Map toolMap;
	//  Managed Build model attributes
	private String resPath;
	private Boolean isExcluded;
	//  Miscellaneous
	private boolean isExtensionResourceConfig = false;
	private boolean isDirty = false;
	private boolean resolved = true;
	
	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create a resource configuration defined by an 
	 * extension point in a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The IConfiguration parent of this resource configuration
	 * @param element The resource configuration definition from the manifest file 
	 *                or a dynamic element provider
	 * @param managedBuildRevision
	 */
	public ResourceConfiguration(IConfiguration parent, IManagedConfigElement element, String managedBuildRevision) {
		this.parent = parent;
		isExtensionResourceConfig = true;
		
		// setup for resolving
		resolved = false;

		setManagedBuildRevision(managedBuildRevision);
		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionResourceConfiguration(this);

		// Load the tool children
		IManagedConfigElement[] tools = element.getChildren(ITool.TOOL_ELEMENT_NAME);
		for (int n = 0; n < tools.length; ++n) {
			Tool toolChild = new Tool(this, tools[n], getManagedBuildRevision());
			toolList.add(toolChild);
		}
	}

	/**
	 * Create a <code>ResourceConfiguration</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>IConfiguration</code> the resource configuration will be added to. 
	 * @param element The XML element that contains the resource configuration settings.
	 * @param managedBuildRevision
	 */
	public ResourceConfiguration(IConfiguration parent, Element element, String managedBuildRevision) {
		this.parent = parent;
		isExtensionResourceConfig = false;
		
		setManagedBuildRevision(managedBuildRevision);
		// Initialize from the XML attributes
		loadFromProject(element);

		// Load children
		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(ITool.TOOL_ELEMENT_NAME)) {
				Tool tool = new Tool((IBuildObject)this, (Element)configElement, getManagedBuildRevision());
				addTool(tool);
			}
		}
	}
	
	public ResourceConfiguration(IConfiguration parent, String id, String resourceName, String path){
		this.parent = parent;
	
		setId(id);
		setName(resourceName);
		
		if ( parent != null)
			setManagedBuildRevision(parent.getManagedBuildRevision());
		
		resPath = path;
		isDirty = false;
		isExcluded = new Boolean(false);
	}

	/**
	 * Create a new resource configuration based on one already defined.
	 * 
	 * @param managedProject The <code>ManagedProject</code> the configuration will be added to. 
	 * @param parentConfig The <code>IConfiguration</code> to copy the settings from.
	 * @param id A unique ID for the new configuration.
	 */
	public ResourceConfiguration(IConfiguration parent, ResourceConfiguration cloneConfig, String id) {
		setId(id);
		setName(cloneConfig.getName());
		this.parent = parent;
		isExtensionResourceConfig = false;

		setManagedBuildRevision(cloneConfig.getManagedBuildRevision());
		
		//  Copy the remaining attributes
		if (cloneConfig.resPath != null) {
			resPath = new String(cloneConfig.resPath);
		}
		if (cloneConfig.isExcluded != null) {
			isExcluded = new Boolean(cloneConfig.isExcluded.booleanValue());
		}
		
		// Clone the resource configuration's tool children
		if (cloneConfig.toolList != null) {
			Iterator iter = cloneConfig.getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId;
				String tmpId;
				String subName;
				String version;
				
				if (toolChild.getSuperClass() != null) {
					tmpId = toolChild.getSuperClass().getId();
					subName = toolChild.getSuperClass().getName();
				} else {
					tmpId = toolChild.getId();
					subName = toolChild.getName();
				}				
				version = ManagedBuildManager.getVersionFromIdAndVersion(tmpId);
				if ( version != null) {   // If the 'tmpId' contains version information
					subId = ManagedBuildManager.getIdFromIdAndVersion(tmpId) + "." + nnn + "_" + version;		//$NON-NLS-1$ //$NON-NLS-2$
				} else {
					subId = tmpId + "." + nnn;		//$NON-NLS-1$
				}
				
				//  The superclass for the cloned tool is not the same as the one from the tool being cloned.
				//  The superclasses reside in different configurations. 
				ITool toolSuperClass = null;
				//  Search for the tool in this configuration that has the same grand-superClass as the 
				//  tool being cloned
				ITool[] tools = parent.getTools();
				for (int i=0; i<tools.length; i++) {
				    ITool configTool = tools[i];
				    if (configTool.getSuperClass() == toolChild.getSuperClass().getSuperClass())
				    {
				        toolSuperClass = configTool;
				        break;
				    }
				}
				if (toolSuperClass == null) {
				    // TODO: report an error
				}
				Tool newTool = new Tool(this, toolSuperClass, subId, subName, toolChild);
				addTool(newTool);
			}
		}

		setDirty(true);
	}
	
	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the resource configuration information from the ManagedConfigElement 
	 * specified in the argument.
	 * 
	 * @param element Contains the resource configuration information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(IBuildObject.ID));
		
		// Get the name
		setName(element.getAttribute(IBuildObject.NAME));
		
		// resourcePath
		resPath = element.getAttribute(IResourceConfiguration.RESOURCE_PATH);

		// exclude
        String excludeStr = element.getAttribute(IResourceConfiguration.EXCLUDE);
        if (excludeStr != null){
    		isExcluded = new Boolean("true".equals(excludeStr)); //$NON-NLS-1$
        }
	}
	
	/* (non-Javadoc)
	 * Initialize the resource configuration information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the resource configuration information 
	 */
	protected void loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.hasAttribute(IBuildObject.NAME)) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// exclude
		if (element.hasAttribute(IResourceConfiguration.EXCLUDE)) {
			String excludeStr = element.getAttribute(IResourceConfiguration.EXCLUDE);
			if (excludeStr != null){
				isExcluded = new Boolean("true".equals(excludeStr)); //$NON-NLS-1$
			}
		}
		
		// resourcePath
		if (element.hasAttribute(IResourceConfiguration.RESOURCE_PATH)) {
			resPath = element.getAttribute(IResourceConfiguration.RESOURCE_PATH);
		}
	}

	/**
	 * Persist the resource configuration to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}
		
		if (isExcluded != null) {
			element.setAttribute(IResourceConfiguration.EXCLUDE, isExcluded.toString());
		}

		if (resPath != null) {
			element.setAttribute(IResourceConfiguration.RESOURCE_PATH, resPath);
		}
		
		// Serialize my children
		List toolElements = getToolList();
		Iterator iter = toolElements.listIterator();
		while (iter.hasNext()) {
			Tool tool = (Tool) iter.next();
			Element toolElement = doc.createElement(ITool.TOOL_ELEMENT_NAME);
			element.appendChild(toolElement);
			tool.serialize(doc, toolElement);
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceConfiguration#getParent()
	 */
	public IConfiguration getParent() {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceConfiguration#getTools()
	 */
	public ITool[] getTools() {
		ITool[] tools = new ITool[getToolList().size()];
		Iterator iter = getToolList().listIterator();
		int i = 0;
		while (iter.hasNext()) {
			Tool tool = (Tool)iter.next();
			tools[i++] = (ITool)tool; 
		}
		return tools;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getTool(java.lang.String)
	 */
	public ITool getTool(String id) {
		Tool tool = (Tool)getToolMap().get(id);
		return (ITool)tool;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the list of tools.
	 * 
	 * @return List containing the tools
	 */
	private List getToolList() {
		if (toolList == null) {
			toolList = new ArrayList();
		}
		return toolList;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the map of tool ids to tools
	 * 
	 * @return
	 */
	private Map getToolMap() {
		if (toolMap == null) {
			toolMap = new HashMap();
		}
		return toolMap;
	}

	/* (non-Javadoc)
	 * Adds the Tool to the Tool list and map
	 * 
	 * @param Tool
	 */
	public void addTool(Tool tool) {
		getToolList().add(tool);
		getToolMap().put(tool.getId(), tool);
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceConfiguration#isExcluded()
	 */
	public boolean isExcluded() {
		if (isExcluded != null) {
			return isExcluded.booleanValue();
		} else {
			return false;
		}
	}

		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getResourcePath()
	 */
	public String getResourcePath() {
		String path = resPath;
		if (path == null) return EMPTY_STRING;
		return path;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceConfiguration#setExclude()
	 */
	public void setExclude(boolean excluded) {
		if (isExcluded == null || excluded != isExcluded.booleanValue()) {
			isExcluded = new Boolean(excluded);
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setResourcePath()
	 */
	public void setResourcePath(String path) {
		if( path == null)
			return;
		if (resPath == null || !path.equals(resPath)) {
			resPath = path;
			setDirty(true);
		}
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#isExtensionElement()
	 */
	public boolean isExtensionResourceConfiguration() {
		return isExtensionResourceConfig;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension tool-chain
 		if (isExtensionResourceConfig) return false;
		
		// If I need saving, just say yes
		if (isDirty) return true;
		
		// Otherwise see if any tools need saving
		Iterator iter = getToolList().listIterator();
		while (iter.hasNext()) {
			Tool toolChild = (Tool) iter.next();
			if (toolChild.isDirty()) return true;
		}
		
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
		// Propagate "false" to the children
		if (!isDirty) {
			Iterator iter = getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				toolChild.setDirty(false);
			}		    
		}
	}
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;

			//  Call resolveReferences on our children
			Iterator iter = getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				toolChild.resolveReferences();
			}
		}
	}
	
	public ITool createTool(ITool superClass, String id, String name, boolean isExtensionElement) {
		Tool tool = new Tool(this, superClass, id, name, isExtensionElement);
		addTool(tool);
		setDirty(true);
		return (ITool)tool;
	}
	
	public void reset() {
		// We just need to remove all Options
		ITool[] tools = getTools();
		// Send out the event to notify the options that they are about to be removed
		ManagedBuildManager.performValueHandlerEvent(this, IManagedOptionValueHandler.EVENT_CLOSE);
		// Remove the configurations		
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			IOption[] opts = tool.getOptions();
			for (int j = 0; j < opts.length; j++) {
				tool.removeOption(opts[j]);
			}
		}
		isExcluded = new Boolean(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setToolCommand(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String)
	 */
	public void setToolCommand(ITool tool, String command) {
		// TODO:  Do we need to verify that the tool is part of the configuration?
			tool.setToolCommand(command);
	}
	
	private IBuildObject getHoldersParent(IOption option) {
		IHoldsOptions holder = option.getOptionHolder();
		if (holder instanceof ITool) {
			return ((ITool)holder).getParent();
		} else if (holder instanceof IToolChain) {
			return ((IToolChain)holder).getParent();
		}
		return null;
	}
	
	public IOption setOption(IHoldsOptions holder, IOption option, boolean value) throws BuildException {
		// Is there a change?
		IOption retOpt = option;
		if (option.getBooleanValue() != value) {
		    //  If this resource config does not already override this option, then we need to
		    //  create a new option
		    if (getHoldersParent(option) != this) {
				IOption newSuperClass = option;
				if (!newSuperClass.isExtensionElement()) {
					newSuperClass = newSuperClass.getSuperClass();
				}
				if (newSuperClass.isExtensionElement()) {
					//  If the extension element is only overriding the "value" of its superclass, hook the
					//  new option up to its superclass directly.  This is to avoid references to oddly id'ed
					//  elements that are automatically generated from V2.0 model optionReferences.  If these
					//  end up in the project file, then the project could have a problem when the integration
					//  provider switches to providing the new model.
					if (newSuperClass.overridesOnlyValue()) {
						newSuperClass = newSuperClass.getSuperClass();
					}
				}
				//  Create an Option element for the managed build project file (.CDTBUILD)
				String subId;
				int nnn = ManagedBuildManager.getRandomNumber();
				subId = newSuperClass.getId() + "." + nnn; //$NON-NLS-1$
				retOpt = holder.createOption(newSuperClass, subId, null, false); 
				retOpt.setValueType(option.getValueType());
				retOpt.setValue(value);
				setDirty(true);
			} else {
				option.setValue(value);
			}
		    // TODO: This causes the entire project to be rebuilt.  Is there a way to only have this 
		    //       file rebuilt?  "Clean" its output?  Change its modification date?
			parent.setRebuildState(true);
		}
		return retOpt;
	}
	
	public IOption setOption(IHoldsOptions holder, IOption option, String value) throws BuildException {
		IOption retOpt = option;
		String oldValue;
		oldValue = option.getStringValue(); 
		if (oldValue != null && !oldValue.equals(value)) {
		    //  If this resource config does not already override this option, then we need to
		    //  create a new option
		    if (getHoldersParent(option) != this) {
				IOption newSuperClass = option;
				if (!newSuperClass.isExtensionElement()) {
					newSuperClass = newSuperClass.getSuperClass();
				}
				if (newSuperClass.isExtensionElement()) {
					//  If the extension element is only overriding the "value" of its superclass, hook the
					//  new option up to its superclass directly.  This is to avoid references to oddly id'ed
					//  elements that are automatically generated from V2.0 model optionReferences.  If these
					//  end up in the project file, then the project could have a problem when the integration
					//  provider switches to providing the new model.
					if (newSuperClass.overridesOnlyValue()) {
						newSuperClass = newSuperClass.getSuperClass();
					}
				}
				//  Create an Option element for the managed build project file (.CDTBUILD)
				String subId;
				int nnn = ManagedBuildManager.getRandomNumber();
				subId = newSuperClass.getId() + "." + nnn; //$NON-NLS-1$
				retOpt = holder.createOption(newSuperClass, subId, null, false); 
				retOpt.setValueType(option.getValueType());
				retOpt.setValue(value);
				setDirty(true);
			} else {
				option.setValue(value);
			}
		    // TODO: This causes the entire project to be rebuilt.  Is there a way to only have this 
		    //       file rebuilt?  "Clean" its output?  Change its modification date?
			parent.setRebuildState(true);
		}
		return retOpt;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, java.lang.String[])
	 */
	public IOption setOption(IHoldsOptions holder, IOption option, String[] value) throws BuildException {
		IOption retOpt = option;
		// Is there a change?
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
		    //  If this resource config does not already override this option, then we need to
		    //  create a new option
		    if (getHoldersParent(option) != this) {
				IOption newSuperClass = option;
				if (!newSuperClass.isExtensionElement()) {
					newSuperClass = newSuperClass.getSuperClass();
				}
				if (newSuperClass.isExtensionElement()) {
					//  If the extension element is only overriding the "value" of its superclass, hook the
					//  new option up to its superclass directly.  This is to avoid references to oddly id'ed
					//  elements that are automatically generated from V2.0 model optionReferences.  If these
					//  end up in the project file, then the project could have a problem when the integration
					//  provider switches to providing the new model.
					if (newSuperClass.overridesOnlyValue()) {
						newSuperClass = newSuperClass.getSuperClass();
					}
				}
				//  Create an Option element for the managed build project file (.CDTBUILD)
				String subId;
				int nnn = ManagedBuildManager.getRandomNumber();
				subId = newSuperClass.getId() + "." + nnn; //$NON-NLS-1$
				retOpt = holder.createOption(newSuperClass, subId, null, false); 
				retOpt.setValueType(option.getValueType());
				retOpt.setValue(value);
				setDirty(true);
			} else {
				option.setValue(value);
			}
		    // TODO: This causes the entire project to be rebuilt.  Is there a way to only have this 
		    //       file rebuilt?  "Clean" its output?  Change its modification date?
			parent.setRebuildState(true);
		} 
		return retOpt;
	}
	
	public IResource getOwner() {
		return getParent().getOwner();
	}
	
	/**
	 * @return Returns the version.
	 */
	public PluginVersionIdentifier getVersion() {
		if ( version == null) {
			if ( getParent() != null) {
				return getParent().getVersion();
			}
		}
		return version;
	}
	
	public void setVersion(PluginVersionIdentifier version) {
		// Do nothing
	}

}
