package org.eclipse.cdt.managedbuilder.internal.core;

/**********************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Target extends BuildObject implements ITarget {

	// Build model elements that come from the plugin or project files	
	private String artifactName;
	private String binaryParserId;
	private String cleanCommand;
	private Map configMap;
	private List configurations;
	private String defaultExtension;
	private String extension;
	private boolean isAbstract = false;
	private boolean isTest = false;
	private String makeCommand;
	private IResource owner;
	private ITarget parent;
	private List targetOSList;
	private Map toolMap;
	private List toolList;

	private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	private static final String EMPTY_STRING = new String();
	
	public Target(IResource owner) {
		this.owner = owner;
	}
	
	/**
	 * Create a target owned by a resource based on a parent target
	 * 
	 * @param owner 
	 * @param parent
	 */
	public Target(IResource owner, ITarget parent) {
		// Make the owner of the target the project resource
		this(owner);
		
		// Copy the parent's identity
		this.parent = parent;
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		int id = r.nextInt();
		if (id < 0) {
			id *= -1;
		}
		setId(owner.getName() + "." + parent.getId() + "." + id);		
		setName(parent.getName());
		this.artifactName = parent.getArtifactName();
		this.binaryParserId = parent.getBinaryParserId();
		this.defaultExtension = parent.getArtifactExtension();
		this.isTest = parent.isTestTarget();
		this.cleanCommand = parent.getCleanCommand();

		// Hook me up
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(owner, true);
		buildInfo.addTarget(this);
	}

	/**
	 * This constructor is called to create a target defined by an extension point in 
	 * a plugin manifest file.
	 * 
	 * @param element
	 */
	public Target(IConfigurationElement element) {
		// id
		setId(element.getAttribute(ID));
		
		// hook me up
		ManagedBuildManager.addExtensionTarget(this);
		
		// Get the target name
		setName(element.getAttribute(NAME));

		// Get the name of the build artifact associated with target (usually 
		// in the plugin specification).
		artifactName = element.getAttribute(ARTIFACT_NAME);
		
		// Get the ID of the binary parser
		binaryParserId = element.getAttribute(BINARY_PARSER);

		// Get the default extension
		defaultExtension = element.getAttribute(DEFAULT_EXTENSION);

		// parent
		String parentId = element.getAttribute(PARENT);
		if (parentId != null) {
			parent = ManagedBuildManager.getTarget(null, parentId);
			// copy over the parents configs
			IConfiguration[] parentConfigs = parent.getConfigurations();
			for (int i = 0; i < parentConfigs.length; ++i)
				addConfiguration(parentConfigs[i]);
		}

		// isAbstract
		isAbstract = ("true".equals(element.getAttribute(IS_ABSTRACT)));

		// Is this a test target
		isTest = ("true".equals(element.getAttribute(IS_TEST)));
		
		// Get the clean command
		cleanCommand = element.getAttribute(CLEAN_COMMAND);
		if (cleanCommand == null) {
			// See if it defined in the parent
			cleanCommand = parent.getCleanCommand();
		}

		// Get the make command
		makeCommand = element.getAttribute(MAKE_COMMAND);
		if (makeCommand == null) {
			// See if it defined in the parent
			makeCommand = parent.getMakeCommand();
		}

		// Get the comma-separated list of valid OS
		String os = element.getAttribute(OS_LIST);
		if (os != null) {
			targetOSList = new ArrayList();
			StringTokenizer tokens = new StringTokenizer(os, ",");
			while (tokens.hasMoreTokens()) {
				targetOSList.add(tokens.nextToken().trim());
			}
		}
		
		IConfigurationElement[] targetElements = element.getChildren();
		int k;
		// Load the tools first
		for (k = 0; k < targetElements.length; ++k) {
			IConfigurationElement targetElement = targetElements[k];
			if (targetElement.getName().equals(ITool.TOOL_ELEMENT_NAME)) {
				new Tool(this, targetElement);
			}
		}
		// Then load the configurations which may have tool references
		for (k = 0; k < targetElements.length; ++k) {
			IConfigurationElement targetElement = targetElements[k];
			if (targetElement.getName().equals(IConfiguration.CONFIGURATION_ELEMENT_NAME)) {
				new Configuration(this, targetElement);
			}
		}

	}

	/**
	 * Create target from project file.
	 * 
	 * @param buildInfo
	 * @param element
	 */
	public Target(ManagedBuildInfo buildInfo, Element element) {
		this(buildInfo.getOwner());
		
		// id
		setId(element.getAttribute(ID));
		
		// hook me up
		buildInfo.addTarget(this);
		
		// name
		setName(element.getAttribute(NAME));

		// Get the name of the build artifact associated with target (should
		// contain what the user entered in the UI).
		artifactName = element.getAttribute(ARTIFACT_NAME);

		// Get the overridden extension
		if (element.hasAttribute(EXTENSION)) {
			extension = element.getAttribute(EXTENSION);
		}

		// parent
		String parentId = element.getAttribute(PARENT);
		if (parentId != null)
			parent = ManagedBuildManager.getTarget(null, parentId);

		// isAbstract
		if ("true".equals(element.getAttribute(IS_ABSTRACT)))
			isAbstract = true;
			
		// Is this a test target
		isTest = ("true".equals(element.getAttribute(IS_TEST)));
		
		// Get the clean command
		cleanCommand = element.getAttribute(CLEAN_COMMAND);
		
		// Get the make command
		if (element.hasAttribute(MAKE_COMMAND)) {
			makeCommand = element.getAttribute(MAKE_COMMAND);
		}
	
		Node child = element.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals(IConfiguration.CONFIGURATION_ELEMENT_NAME)) {
				new Configuration(this, (Element)child);
			}
			child = child.getNextSibling();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#removeConfiguration(java.lang.String)
	 */
	public void removeConfiguration(String id) {
		// Remove the specified configuration from the list and map
		Iterator iter = configurations.listIterator();
		while (iter.hasNext()) {
			 IConfiguration config = (IConfiguration)iter.next();
			 if (config.getId().equals(id)) {
			 	configurations.remove(config);
				configMap.remove(id);
			 	break;
			 }
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#resetMakeCommand()
	 */
	public void resetMakeCommand() {
		makeCommand = null;
	}
	
	/**
	 * Persist receiver to project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(ID, getId());
		element.setAttribute(NAME, getName());
		if (parent != null)
			element.setAttribute(PARENT, parent.getId());
		element.setAttribute(IS_ABSTRACT, isAbstract ? "true" : "false");
		element.setAttribute(ARTIFACT_NAME, getArtifactName());
		if (extension != null) {
			element.setAttribute(EXTENSION, extension);
		}
		element.setAttribute(IS_TEST, isTest ? "true" : "false");
		element.setAttribute(CLEAN_COMMAND, getCleanCommand());
		if (makeCommand != null) {
			element.setAttribute(MAKE_COMMAND, makeCommand);
		}
				
		if (configurations != null)
			for (int i = 0; i < configurations.size(); ++i) {
				Configuration config = (Configuration)configurations.get(i);
				Element configElement = doc.createElement(IConfiguration.CONFIGURATION_ELEMENT_NAME);
				element.appendChild(configElement);
				config.serialize(doc, configElement);
			}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getMakeCommand()
	 */
	public String getMakeCommand() {
		// Return the name of the make utility
		return (makeCommand == null) ? parent.getMakeCommand() : makeCommand;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getName()
	 */
	public String getName() {
		return (name == null && parent != null) ? parent.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getParent()
	 */
	public ITarget getParent() {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getTargetOSList()
	 */
	public String[] getTargetOSList() {
		if (targetOSList == null) {
			// Ask parent for its list
			if (parent != null) {
				return parent.getTargetOSList();
			} else {
				// I have no parent and no defined list but never return null
				return new String[0];
			}
		}
		return (String[]) targetOSList.toArray(new String[targetOSList.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getOwner()
	 */
	public IResource getOwner() {
		return owner;
	}

	private int getNumTools() {
		int n = getToolList().size();
		if (parent != null)
			n += ((Target)parent).getNumTools();
		return n;
	}
	

	private int addToolsToArray(ITool[] toolArray, int start) {
		int n = start;
		if (parent != null)
			n = ((Target)parent).addToolsToArray(toolArray, start);

		for (int i = 0; i < getToolList().size(); ++i) {
			toolArray[n++] = (ITool)getToolList().get(i); 
		}
		
		return n;
	}
	
	private List getToolList() {
		if (toolList == null) {
			toolList = new ArrayList();
			toolList.clear();
		}
		return toolList;
	}

	private Map getToolMap() {
		if (toolMap == null) {
			toolMap = new HashMap();
			toolMap.clear();
		}
		return toolMap;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getTools()
	 */
	public ITool[] getTools() {
		ITool[] toolArray = new ITool[getNumTools()];
		addToolsToArray(toolArray, 0);
		return toolArray;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#hasMakeCommandOverride()
	 */
	public boolean hasOverridenMakeCommand() {
		return (makeCommand != null && !makeCommand.equals(parent.getMakeCommand()));
	}

	/**
	 * @param id
	 * @return ITool
	 */
	public ITool getTool(String id) {
		ITool result = null;

		// See if receiver has it in list
			result = (ITool) getToolMap().get(id);

		// If not, check if parent has it
		if (result == null && parent != null) {
			result = ((Target)parent).getTool(id);
		}

		return result;
	}

	/**
	 * @param tool
	 */
	public void addTool(ITool tool) {
		getToolList().add(tool);
		getToolMap().put(tool.getId(), tool);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getConfigurations()
	 */
	public IConfiguration[] getConfigurations() {
		if (configurations != null)
			return (IConfiguration[])configurations.toArray(new IConfiguration[configurations.size()]);
		else
			return emptyConfigs;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getDefaultExtension()
	 */
	public String getDefaultExtension() {
		return defaultExtension == null ? EMPTY_STRING : defaultExtension;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getCleanCommand()
	 */
	public String getCleanCommand() {
		// Return the command used to remove files
		return cleanCommand == null ? EMPTY_STRING : cleanCommand;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getArtifactName()
	 */
	public String getArtifactName() {
		if (artifactName == null) {
			// If I have a parent, ask it
			if (parent != null) {
				return parent.getArtifactName();
			} else {
				// I'm it and this is not good!
				return EMPTY_STRING;
			}
		} else {
			return artifactName;
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getArtifactExtension()
	 */
	public String getArtifactExtension() {
		// Has the user changed the extension for this target
		if (extension != null) {
			return extension;
		}
		// If not, then go through the default extension lookup
		if (defaultExtension == null) {
			// Ask my parent first
			if (parent != null) {
				return parent.getArtifactExtension();
			} else {
				return EMPTY_STRING;
			}
		} else {
			return defaultExtension;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getBinaryParserId()
	 */
	public String getBinaryParserId() {
		if (binaryParserId == null) {
			// If I have a parent, ask it
			if (parent != null) {
				return parent.getBinaryParserId();
			} else {
				// I'm it and this is not good!
				return EMPTY_STRING;
			}
		}
		return binaryParserId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getConfiguration()
	 */
	public IConfiguration getConfiguration(String id) {
		return (IConfiguration)configMap.get(id);
	}

	/**
	 * @param configuration
	 */
	public void addConfiguration(IConfiguration configuration) {
		if (configurations == null) {
			configurations = new ArrayList();
			configMap = new HashMap();
		}
		configurations.add(configuration);
		configMap.put(configuration.getId(), configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#isAbstract()
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#isTestTarget()
	 */
	public boolean isTestTarget() {
		return isTest;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#createConfiguration()
	 */
	public IConfiguration createConfiguration(String id) {
		return new Configuration(this, id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#createConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public IConfiguration createConfiguration(IConfiguration parent, String id) {
		return new Configuration(this, parent, id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#setArtifactExtension(java.lang.String)
	 */
	public void setArtifactExtension(String extension) {
		if (extension != null) {
			this.extension = extension;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#setArtifactName(java.lang.String)
	 */
	public void setArtifactName(String name) {
		if (name != null) {
			artifactName = name;		
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#setMakeCommand(java.lang.String)
	 */
	public void setMakeCommand(String command) {
		if (command != null && !getMakeCommand().equals(command)) {
			makeCommand = command;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#updateOwner(org.eclipse.core.resources.IResource)
	 */
	public void updateOwner(IResource resource) {
		if (!resource.equals(owner)) {
			// Set the owner correctly
			owner = resource;
		}
	}

}
