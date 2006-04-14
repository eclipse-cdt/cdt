/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
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
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
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

	//property name for holding the rebuild state
	private static final String REBUILD_STATE = "rebuildState";  //$NON-NLS-1$

	//  Parent and children
	private IConfiguration parent;
	private List toolList;
	private Map toolMap;
	//  Managed Build model attributes
	private String resPath;
	private Boolean isExcluded;
	private Integer rcbsApplicability;
	private String toolsToInvoke;
	//  Miscellaneous
	private boolean isExtensionResourceConfig = false;
	private boolean isDirty = false;
	private boolean resolved = true;
	private boolean rebuildState;
	
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
		
		String rebuild = PropertyManager.getInstance().getProperty(this, REBUILD_STATE);
		if(rebuild == null || Boolean.parseBoolean(rebuild))
			rebuildState = true;
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
		toolsToInvoke = EMPTY_STRING;
		rcbsApplicability = new Integer(KIND_DISABLE_RCBS_TOOL);
		setRebuildState(true);
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
		if (cloneConfig.toolsToInvoke != null) {
			toolsToInvoke = new String(cloneConfig.toolsToInvoke);
		}
		if (cloneConfig.rcbsApplicability != null) {
			rcbsApplicability = new Integer(cloneConfig.rcbsApplicability.intValue());
		}
				
		// Clone the resource configuration's tool children
		if (cloneConfig.toolList != null) {
			Iterator iter = cloneConfig.getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				String subId;
				String subName;
				
				if (toolChild.getSuperClass() != null) {
					subId = ManagedBuildManager.calculateChildId(
								toolChild.getSuperClass().getId(),
								null);
					subName = toolChild.getSuperClass().getName();
				} else {
					subId = ManagedBuildManager.calculateChildId(
								toolChild.getId(),
								null);
					subName = toolChild.getName();
				}				

				//  The superclass for the cloned tool is not the same as the one from the tool being cloned.
				//  The superclasses reside in different configurations. 
				ITool toolSuperClass = null;
				//  Search for the tool in this configuration that has the same grand-superClass as the 
				//  tool being cloned
				ITool[] tools = parent.getTools();
				for (int i=0; i<tools.length; i++) {
				    ITool configTool = tools[i];
				    if (toolChild.getSuperClass() != null 
				    		&& configTool.getSuperClass() == toolChild.getSuperClass().getSuperClass())
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
		setRebuildState(true);
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
		// toolsToInvoke
		toolsToInvoke = element.getAttribute(IResourceConfiguration.TOOLS_TO_INVOKE);

		// rcbsApplicability
		String rcbsApplicabilityStr = element.getAttribute(IResourceConfiguration.RCBS_APPLICABILITY);
		if (rcbsApplicabilityStr == null || rcbsApplicabilityStr.equals(DISABLE_RCBS_TOOL)) {
			rcbsApplicability = new Integer(KIND_DISABLE_RCBS_TOOL);
		} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_BEFORE)) {
			rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_BEFORE);
		} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_AFTER)) {
			rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_AFTER);
		} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_AS_OVERRIDE)) {
			rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_AS_OVERRIDE);
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

		// toolsToInvoke
		if (element.hasAttribute(IResourceConfiguration.TOOLS_TO_INVOKE)) {
			toolsToInvoke = element.getAttribute(IResourceConfiguration.TOOLS_TO_INVOKE);
		}

		// rcbsApplicability
		if (element.hasAttribute(IResourceConfiguration.RCBS_APPLICABILITY)) {
			String rcbsApplicabilityStr = element.getAttribute(IResourceConfiguration.RCBS_APPLICABILITY);
			if (rcbsApplicabilityStr == null || rcbsApplicabilityStr.equals(DISABLE_RCBS_TOOL)) {
				rcbsApplicability = new Integer(KIND_DISABLE_RCBS_TOOL);
			} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_BEFORE)) {
				rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_BEFORE);
			} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_AFTER)) {
				rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_AFTER);
			} else if (rcbsApplicabilityStr.equals(APPLY_RCBS_TOOL_AS_OVERRIDE)) {
				rcbsApplicability = new Integer(KIND_APPLY_RCBS_TOOL_AS_OVERRIDE);
			}
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
		
		if (toolsToInvoke != null) {
			element.setAttribute(IResourceConfiguration.TOOLS_TO_INVOKE, toolsToInvoke);
		}

		if (rcbsApplicability != null) {
			String str;
			switch (getRcbsApplicability()) {
				case KIND_APPLY_RCBS_TOOL_BEFORE:
					str = APPLY_RCBS_TOOL_BEFORE;
					break;
				case KIND_APPLY_RCBS_TOOL_AFTER:
					str = APPLY_RCBS_TOOL_AFTER;
					break;
				case KIND_APPLY_RCBS_TOOL_AS_OVERRIDE:
					str = APPLY_RCBS_TOOL_AS_OVERRIDE;
					break;
				case KIND_DISABLE_RCBS_TOOL:
					str = DISABLE_RCBS_TOOL;
					break;
				default:
					str = DISABLE_RCBS_TOOL; 
					break;
			}
			element.setAttribute(IResourceConfiguration.RCBS_APPLICABILITY, str);
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
		setRebuildState(true);
	}

	/* (non-Javadoc)
	 * Removes the Tool from the Tool list and map
	 * 
	 * @param Tool
	 */
	public void removeTool(ITool tool) {
		getToolList().remove(tool);
		getToolMap().remove(tool);
		setRebuildState(true);
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
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getRcbsApplicability()
	 */
	public int getRcbsApplicability() {
		/*
		 * rcbsApplicability is an integer constant that represents how the user wants to
		 * order the application of a resource custom build step tool.
		 * Defaults to disable rcbs tool.
		 * Choices are before, after, or override other tools, or disable rcbs tool.
		 */
		if (rcbsApplicability == null) {
			return KIND_DISABLE_RCBS_TOOL;
		}
		return rcbsApplicability.intValue();
		}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getToolsToInvoke()
	 */
	public ITool[] getToolsToInvoke() {
		/*
		 * toolsToInvoke is an ordered list of tool ids for the currently defined tools in
		 * the resource configuration.
		 * Defaults to all tools in the order found.
		 * Modified by the presence of an rcbs tool and the currently assigned applicability of that tool.
		 * The attribute is implemented as a String of a semicolon separated list of tool ids.
		 * An empty string implies treat as if no resource configuration, i.e., use project level tool.
		 * This getter routine returns an ITool[] to consumers (i.e., the makefile generator).
		 */
		String t_ToolsToInvoke = EMPTY_STRING;
		ITool[] resConfigTools;
		ITool[] tools;
		String rcbsToolId = EMPTY_STRING;
		int len;
		int j;
		int rcbsToolIdx=-1;
		resConfigTools = getTools();

		/*
		 * Evaluate the tools currently defined in the resource configuration.
		 * Update the current state of the toolsToInvoke attribute.
		 * Build and return an ITool[] for consumers.
		 */
		
		/*
		 * If no tools are currently defined, return a zero lengh array of ITool.
		 */
		if (resConfigTools.length == 0) {
			toolsToInvoke = EMPTY_STRING;
			tools = new ITool[0];
			return tools;
		}
		
		/*
		 * See if there is an rcbs tool defined.  There should only be one at most.
		 */
		for ( int i = 0; i < resConfigTools.length; i++ ){
			if (resConfigTools[i].getCustomBuildStep() && !resConfigTools[i].isExtensionElement()) {
				rcbsToolId = resConfigTools[i].getId();
				rcbsToolIdx = i;
				break;
			}
		}
		if (!rcbsToolId.equals(EMPTY_STRING)){
			/*
			 * Here if an rcbs tool is defined.
			 * Apply the tools according to the current rcbsApplicability setting.
			 */
			switch(rcbsApplicability.intValue()){
			case KIND_APPLY_RCBS_TOOL_AS_OVERRIDE:
				toolsToInvoke = rcbsToolId;
				tools = new ITool[1];
				tools[0] = resConfigTools[rcbsToolIdx];
				break;
			case KIND_APPLY_RCBS_TOOL_AFTER:
				j = 0;
				tools = new ITool[resConfigTools.length];
				for ( int i = 0; i < resConfigTools.length; i++ ){
					if (resConfigTools[i].getId() != rcbsToolId) {
						t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
						tools[j++] = resConfigTools[i];
					}
				}
				t_ToolsToInvoke += rcbsToolId;
				tools[j++] = resConfigTools[rcbsToolIdx];
				toolsToInvoke = t_ToolsToInvoke;
				break;
			case KIND_APPLY_RCBS_TOOL_BEFORE:
				j = 0;
				tools = new ITool[resConfigTools.length];
				t_ToolsToInvoke = rcbsToolId + ";";	//$NON-NLS-1$
				tools[j++] = resConfigTools[rcbsToolIdx];
				for ( int i = 0; i < resConfigTools.length; i++ ){
					if (resConfigTools[i].getId() != rcbsToolId) {
						t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
						tools[j++] = resConfigTools[i];
					}
				}
				len = t_ToolsToInvoke.length();
				t_ToolsToInvoke = t_ToolsToInvoke.substring(0,len-1);
				toolsToInvoke = t_ToolsToInvoke;
				break;
			case KIND_DISABLE_RCBS_TOOL:
				/*
				 * If the rcbs tool is the only tool and the user has disabled it,
				 * there are no tools to invoke in the resource configuration.
				 */
				if(resConfigTools.length == 1){
					tools = new ITool[0];
					toolsToInvoke = EMPTY_STRING;
					break;
				}
				j = 0;
				tools = new ITool[resConfigTools.length-1];
				for ( int i = 0; i < resConfigTools.length; i++ ){
					if (resConfigTools[i].getId() != rcbsToolId) {
						t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
						tools[j++] = resConfigTools[i];
					}
				}
				len = t_ToolsToInvoke.length();
				t_ToolsToInvoke = t_ToolsToInvoke.substring(0,len-1);
				toolsToInvoke = t_ToolsToInvoke;
				break;
			default:
				/*
				 * If we get an unexpected value, apply all tools in the order found.
				 */
				tools = new ITool[resConfigTools.length];
				for ( int i = 0; i < resConfigTools.length; i++ ){
					t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
					tools[i] = resConfigTools[i];
				}
				len = t_ToolsToInvoke.length();
				t_ToolsToInvoke = t_ToolsToInvoke.substring(0,len-1);
				toolsToInvoke = t_ToolsToInvoke;
				break;
			}
		}
		else {
			/*
			 * Here if no rcbs tool is defined, but there are other tools in the resource configuration.
			 * Specify all tools in the order found.
			 */
			tools = new ITool[resConfigTools.length];
			for ( int i = 0; i < resConfigTools.length; i++ ){
				t_ToolsToInvoke += resConfigTools[i].getId() + ";";	//$NON-NLS-1$
				tools[i] = resConfigTools[i];
			}
			len = t_ToolsToInvoke.length();
			t_ToolsToInvoke = t_ToolsToInvoke.substring(0,len-1);
			toolsToInvoke = t_ToolsToInvoke;
		}
		return tools;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#getRcbsApplicability()
	 */
	public void setRcbsApplicability(int newValue) {
		/*
		 * rcbsApplicability is an integer constant that represents how the user wants to
		 * order the application of a resource custom build step tool.
		 * Defaults to override all other tools.
		 * Choices are before, after, or override other tools, or disable rcbs tool.
		 */
		if (rcbsApplicability == null || !(rcbsApplicability.intValue() == newValue)) {
			rcbsApplicability = new Integer(newValue);
			isDirty = true;
			setRebuildState(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IResourceConfiguration#setExclude()
	 */
	public void setExclude(boolean excluded) {
		if (isExcluded == null || excluded != isExcluded.booleanValue()) {
			isExcluded = new Boolean(excluded);
			setDirty(true);
			setRebuildState(true);
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
			setRebuildState(true);
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
//		ManagedBuildManager.performValueHandlerEvent(this, IManagedOptionValueHandler.EVENT_CLOSE);
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
			retOpt = holder.getOptionToSet(option, false);
			retOpt.setValue(value);
		    // TODO: This causes the entire project to be rebuilt.  Is there a way to only have this 
		    //       file rebuilt?  "Clean" its output?  Change its modification date?
//			parent.setRebuildState(true);
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
			retOpt = holder.getOptionToSet(option, false);
			retOpt.setValue(value);
		    // TODO: This causes the entire project to be rebuilt.  Is there a way to only have this 
		    //       file rebuilt?  "Clean" its output?  Change its modification date?
//			parent.setRebuildState(true);
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
			retOpt = holder.getOptionToSet(option, false);
			retOpt.setValue(value);
		    // TODO: This causes the entire project to be rebuilt.  Is there a way to only have this 
		    //       file rebuilt?  "Clean" its output?  Change its modification date?
//			parent.setRebuildState(true);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.core.BuildObject#updateManagedBuildRevision(java.lang.String)
	 */
	public void updateManagedBuildRevision(String revision){
		super.updateManagedBuildRevision(revision);
		
		for(Iterator iter = getToolList().iterator(); iter.hasNext();){
			((Tool)iter.next()).updateManagedBuildRevision(revision);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#needsRebuild()
	 */
	public boolean needsRebuild() {
		if(rebuildState)
			return true;
		
		ITool tools[] = getToolsToInvoke();
		for(int i = 0; i < tools.length; i++){
			if(tools[i].needsRebuild())
				return true;
		}

		return rebuildState;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IResourceConfiguration#setRebuildState(boolean)
	 */
	public void setRebuildState(boolean rebuild) {
		if(isExtensionResourceConfiguration() && rebuild)
			return;
		
		if(rebuildState != rebuild){
			rebuildState = rebuild;
			saveRebuildState();
		}
		
		if(!rebuildState){
			ITool tools[] = getToolsToInvoke();
			for(int i = 0; i < tools.length; i++){
				tools[i].setRebuildState(false);
			}
		}

	}
	
	private void saveRebuildState(){
		PropertyManager.getInstance().setProperty(this, REBUILD_STATE, Boolean.toString(rebuildState));
	}

}
