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
import org.eclipse.core.boot.BootLoader;
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
	private String makeArguments;
	private String makeCommand;
	private IResource owner;
	private ITarget parent;
	private List targetOSList;
	private Map toolMap;
	private List toolList;
	private List toolReferences;

	private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	private static final String EMPTY_STRING = new String();
	
	/* (non-Javadoc)
	 * Set the resource that owns the target.
	 * 
	 * @param owner
	 */
	protected Target(IResource owner) {
		this.owner = owner;
	}
	
	/**
	 * Create a copy of the target specified in the argument, 
	 * that is owned by the owned by the specified resource.
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
		setId(owner.getName() + "." + parent.getId() + "." + id);		 //$NON-NLS-1$ //$NON-NLS-2$
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
		isAbstract = ("true".equals(element.getAttribute(IS_ABSTRACT))); //$NON-NLS-1$

		// Is this a test target
		isTest = ("true".equals(element.getAttribute(IS_TEST))); //$NON-NLS-1$
		
		// Get the clean command
		cleanCommand = element.getAttribute(CLEAN_COMMAND);

		// Get the make command
		makeCommand = element.getAttribute(MAKE_COMMAND);

		// Get the comma-separated list of valid OS
		String os = element.getAttribute(OS_LIST);
		if (os != null) {
			targetOSList = new ArrayList();
			StringTokenizer tokens = new StringTokenizer(os, ","); //$NON-NLS-1$
			while (tokens.hasMoreTokens()) {
				targetOSList.add(tokens.nextToken().trim());
			}
		}
		
		// Load any tool references we might have
		IConfigurationElement[] toolRefs = element.getChildren(IConfiguration.TOOLREF_ELEMENT_NAME);
		for (int i=0; i < toolRefs.length; ++i) {
			new ToolReference(this, toolRefs[i]);
		}
		// Then load any tools defined for the target
		IConfigurationElement[] tools = element.getChildren(ITool.TOOL_ELEMENT_NAME);
		for (int j = 0; j < tools.length; ++j) {
			new Tool(this, tools[j]);
		}
		// Then load the configurations which may have tool references
		IConfigurationElement[] configs = element.getChildren(IConfiguration.CONFIGURATION_ELEMENT_NAME);
		for (int k = 0; k < configs.length; ++k) {
			new Configuration(this, configs[k]);
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
		if ("true".equals(element.getAttribute(IS_ABSTRACT))) //$NON-NLS-1$
			isAbstract = true;
			
		// Is this a test target
		isTest = ("true".equals(element.getAttribute(IS_TEST))); //$NON-NLS-1$
		
		// Get the clean command
		cleanCommand = element.getAttribute(CLEAN_COMMAND);
		
		// Get the make command and arguments
		if (element.hasAttribute(MAKE_COMMAND)) {
			makeCommand = element.getAttribute(MAKE_COMMAND);
		}
		if(element.hasAttribute(MAKE_ARGS)) {
			makeArguments = element.getAttribute(MAKE_ARGS);
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
		makeArguments = null;
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
		element.setAttribute(IS_ABSTRACT, isAbstract ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		element.setAttribute(ARTIFACT_NAME, getArtifactName());
		if (extension != null) {
			element.setAttribute(EXTENSION, extension);
		}
		element.setAttribute(IS_TEST, isTest ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		element.setAttribute(CLEAN_COMMAND, getCleanCommand());
		if (makeCommand != null) {
			element.setAttribute(MAKE_COMMAND, makeCommand);
		}
		if (makeArguments != null) {
			element.setAttribute(MAKE_ARGS, makeArguments);
		}
				
		if (configurations != null)
			for (int i = 0; i < configurations.size(); ++i) {
				Configuration config = (Configuration)configurations.get(i);
				Element configElement = doc.createElement(IConfiguration.CONFIGURATION_ELEMENT_NAME);
				element.appendChild(configElement);
				config.serialize(doc, configElement);
			}
	}

	/* (non-javadoc)
	 * A safe accesor method. It answers the tool reference list in the 
	 * receiver.
	 * 
	 * @return List
	 */
	protected List getLocalToolReferences() {
		if (toolReferences == null) {
			toolReferences = new ArrayList();
			toolReferences.clear();
		}
		return toolReferences;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getMakeArguments()
	 */
	public String getMakeArguments() {
		if (makeArguments == null) {
			// See if it is defined in my parent
			if (parent != null) {
				return parent.getMakeArguments();
			} else { 
				// No parent and no user setting
				return new String(""); //$NON-NLS-1$
			}
		}
		return makeArguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getMakeCommand()
	 */
	public String getMakeCommand() {
		// Return the name of the make utility
		if (makeCommand == null) {
			// If I have a parent, ask it
			if (parent != null) {
				return parent.getMakeCommand();
			} else {
				// The user has forgotten to specify a command in the plugin manifets.
				return new String("make"); //$NON-NLS-1$
			}
		} else {
			return makeCommand;
		}
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

	private int getToolCount() {
		// Count the tools that belong to the target
		int n = getToolList().size();
		// Count the references the target has
		n += getLocalToolReferences().size();
		// Count the tools in the parent
		if (parent != null)
			n += ((Target)parent).getToolCount();
		return n;
	}
	

	/* (non-Javadoc)
	 * Tail-recursion method that creates a lits of tools and tool reference 
	 * walking the receiver's parent hierarchy. 
	 *  
	 * @param toolArray
	 * @param start
	 * @return
	 */
	private int addToolsToArray(ITool[] toolArray, int start) {
		int n = start;
		if (parent != null)
			n = ((Target)parent).addToolsToArray(toolArray, start);

		for (int i = 0; i < getToolList().size(); ++i) {
			toolArray[n++] = (ITool)getToolList().get(i); 
		}

		// Add local tool references
		for (int j = 0; j < getLocalToolReferences().size(); ++j) {
			toolArray[n++] = (ITool)getLocalToolReferences().get(j);
		}
		
		return n;
	}
	
	/* (non-Javadoc)
	 * A safe accessor method for the list of tools maintained by the 
	 * target
	 * 
	 */
	private List getToolList() {
		if (toolList == null) {
			toolList = new ArrayList();
			toolList.clear();
		}
		return toolList;
	}

	/* (non-Javadoc)
	 * A safe accessor for the tool map
	 * 
	 */
	private Map getToolMap() {
		if (toolMap == null) {
			toolMap = new HashMap();
			toolMap.clear();
		}
		return toolMap;
	}

	/* (non-Javadoc)
	 * Returns the reference for a given tool or <code>null</code> if one is not
	 * found.
	 * 
	 * @param tool
	 * @return ToolReference
	 */
	private ToolReference getToolReference(ITool tool) {
		// See if the receiver has a reference to the tool
		ToolReference ref = null;
		if (tool == null) return ref;
		Iterator iter = getLocalToolReferences().listIterator();
		while (iter.hasNext()) {
			ToolReference temp = (ToolReference)iter.next(); 
			if (temp.references(tool)) {
				ref = temp;
				break;
			}
		}
		return ref;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getTools()
	 */
	public ITool[] getTools() {
		ITool[] toolArray = new ITool[getToolCount()];
		addToolsToArray(toolArray, 0);
		return toolArray;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#hasMakeCommandOverride()
	 */
	public boolean hasOverridenMakeCommand() {
		// We answer true if the make command or the flags are different
		return ((makeCommand != null && !makeCommand.equals(parent.getMakeCommand())) 
			|| (makeArguments != null && !makeArguments.equals(parent.getMakeArguments())));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getTool(java.lang.String)
	 */
	public ITool getTool(String id) {
		ITool result = null;

		// See if receiver has it in list
		result = (ITool) getToolMap().get(id);

		// If not, check if parent has it
		if (result == null && parent != null) {
			result = ((Target)parent).getTool(id);
		}
		
		// If not defined in parents, check if defined at all
		if (result == null) {
			result = ManagedBuildManager.getTool(id);
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
		if (cleanCommand == null) {
			if (parent != null) {
				return parent.getCleanCommand();
			} else {
				// User forgot to specify it. Guess based on OS.
				if (BootLoader.getOS().equals("OS_WIN32")) { //$NON-NLS-1$
					return new String("del"); //$NON-NLS-1$
				} else {
					return new String("rm"); //$NON-NLS-1$
				}
			}
		} else {
			// This was spec'd in the manifest
			return cleanCommand;
		}
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

	/**
	 * Adds a tool reference to the receiver.
	 * 
	 * @param toolRef
	 */
	public void addToolReference(ToolReference toolRef) {
		getLocalToolReferences().add(toolRef);
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#setMakeArguments(java.lang.String)
	 */
	public void setMakeArguments(String makeArgs) {
		if (makeArgs != null && !getMakeArguments().equals(makeArgs)) {
			makeArguments = makeArgs;
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
