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
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Target extends BuildObject implements ITarget {
	private static final String EMPTY_STRING = new String();
	private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	private String artifactName;
	private String binaryParserId;
	private String cleanCommand;
	private List configList;
	private Map configMap;
	private String defaultExtension;
	private Map depCalculatorsMap;
	private String errorParserIds;
	private String extension;
	private boolean isAbstract = false;
	private boolean isDirty = false;
	private boolean isTest = false;
	private String makeArguments;
	private String makeCommand;
	private IResource owner;
	private ITarget parent;
	private boolean resolved = true;
	private List targetArchList;
	private List targetOSList;
	private List toolList;
	private Map toolMap;
	private List toolReferences;

	/**
	 * This constructor is called to create a target defined by an extension point in 
	 * a plugin manifest file.
	 * 
	 * @param element
	 */
	public Target(IManagedConfigElement element) {
		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		resolved = false;
		
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
		
		// Get the semicolon separated list of IDs of the error parsers
		errorParserIds = element.getAttribute(ERROR_PARSERS);

		// Get the default extension
		defaultExtension = element.getAttribute(DEFAULT_EXTENSION);

		// isAbstract
		isAbstract = ("true".equals(element.getAttribute(IS_ABSTRACT))); //$NON-NLS-1$

		// Is this a test target
		isTest = ("true".equals(element.getAttribute(IS_TEST))); //$NON-NLS-1$
		
		// Get the clean command
		cleanCommand = element.getAttribute(CLEAN_COMMAND);

		// Get the make command
		makeCommand = element.getAttribute(MAKE_COMMAND);

		// Get the make arguments
		makeArguments = element.getAttribute(MAKE_ARGS);
		
		// Get the comma-separated list of valid OS
		String os = element.getAttribute(OS_LIST);
		if (os != null) {
			targetOSList = new ArrayList();
			String[] osTokens = os.split(","); //$NON-NLS-1$
			for (int i = 0; i < osTokens.length; ++i) {
				targetOSList.add(osTokens[i].trim());
			}
		}
		
		// Get the comma-separated list of valid Architectures
		String arch = element.getAttribute(ARCH_LIST);
		if (arch != null) {
			targetArchList = new ArrayList();
			String[] archTokens = arch.split(","); //$NON-NLS-1$
			for (int j = 0; j < archTokens.length; ++j) {
				targetArchList.add(archTokens[j].trim());
			}
		}
		// Load any tool references we might have
		IManagedConfigElement[] toolRefs = element.getChildren(IConfiguration.TOOLREF_ELEMENT_NAME);
		for (int k = 0; k < toolRefs.length; ++k) {
			new ToolReference(this, toolRefs[k]);
		}
		// Then load any tools defined for the target
		IManagedConfigElement[] tools = element.getChildren(ITool.TOOL_ELEMENT_NAME);
		for (int m = 0; m < tools.length; ++m) {
			new Tool(this, tools[m]);
		}
		// Then load the configurations which may have tool references
		IManagedConfigElement[] configs = element.getChildren(IConfiguration.CONFIGURATION_ELEMENT_NAME);
		for (int n = 0; n < configs.length; ++n) {
			new Configuration(this, configs[n]);
		}
	}
	
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
		setArtifactName(parent.getArtifactName());
		this.binaryParserId = parent.getBinaryParserId();
		this.errorParserIds = parent.getErrorParserIds();
		this.defaultExtension = parent.getArtifactExtension();
		this.isTest = parent.isTestTarget();
		this.cleanCommand = parent.getCleanCommand();

		// Hook me up
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(owner);
		buildInfo.addTarget(this);
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
		if (element.hasAttribute(CLEAN_COMMAND)) {
			cleanCommand = element.getAttribute(CLEAN_COMMAND);
		}
		
		// Get the semicolon separated list of IDs of the error parsers
		if (element.hasAttribute(ERROR_PARSERS)) {
			errorParserIds = element.getAttribute(ERROR_PARSERS);
		}
		
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

	/**
	 * @param configuration
	 */
	public void addConfiguration(IConfiguration configuration) {
		getConfigurationList().add(configuration);
		getConfigurationMap().put(configuration.getId(), configuration);
	}

	/**
	 * Adds a tool specification to the receiver. This tool is defined 
	 * only for the receiver, and cannot be shared by other targets.
	 *  
	 * @param tool
	 */
	public void addTool(ITool tool) {
		getToolList().add(tool);
		getToolMap().put(tool.getId(), tool);
		getDepCalcMap().put(tool.getId(), ManagedBuildManager.createDependencyGenerator(tool.getId()));
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
	 * Tail-recursion method that creates a lits of tools and tool reference 
	 * walking the receiver's parent hierarchy. 
	 *  
	 * @param toolArray
	 */
	private void addToolsToArray(Vector toolArray) {
		if (parent != null) {
			((Target)parent).addToolsToArray(toolArray);
		}
		
		//	Add the tools from out own list
		toolArray.addAll(getToolList());

		// Add local tool references
		toolArray.addAll(getLocalToolReferences());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#createConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public IConfiguration createConfiguration(IConfiguration parent, String id) {
		isDirty = true;
		return new Configuration(this, parent, id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#createConfiguration()
	 */
	public IConfiguration createConfiguration(String id) {
		return new Configuration(this, id);
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
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getCleanCommand()
	 */
	public String getCleanCommand() {
		// Return the command used to remove files
		if (cleanCommand == null) {
			if (parent != null) {
				return parent.getCleanCommand();
			} else {
				// User forgot to specify it. Guess based on OS.
				if (Platform.getOS().equals("OS_WIN32")) { //$NON-NLS-1$
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
	 * @see org.eclipse.cdt.core.build.managed.ITarget#getConfiguration()
	 */
	public IConfiguration getConfiguration(String id) {
		return (IConfiguration)getConfigurationMap().get(id);
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the list of configurations.
	 * 
	 * @return List containing the configurations
	 */
	private List getConfigurationList() {
		if (configList == null) {
			configList = new ArrayList();
		}
		return configList;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the map of configuration ids to configurations
	 * 
	 * @return
	 */
	private Map getConfigurationMap() {
		if (configMap == null) {
			configMap = new HashMap();
		}
		return configMap;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getConfigurations()
	 */
	public IConfiguration[] getConfigurations() {
		return (IConfiguration[])getConfigurationList().toArray(new IConfiguration[getConfigurationList().size()]);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getDefaultExtension()
	 */
	public String getDefaultExtension() {
		return defaultExtension == null ? EMPTY_STRING : defaultExtension;
	}
	
	private Map getDepCalcMap() {
		if (depCalculatorsMap == null) {
			depCalculatorsMap = new HashMap();
		}
		return depCalculatorsMap;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getDependencyCalculator(java.lang.String)
	 */
	public IManagedDependencyGenerator getDependencyGenerator(String toolId) {
		// If I have this tool defined locally, answer its dependency calculator
		IManagedDependencyGenerator answer = (IManagedDependencyGenerator) getDepCalcMap().get(toolId);
		
		// I do not have a local tool definition
		if (answer == null && parent != null) {
			answer = parent.getDependencyGenerator(toolId);
		}
		
		// Perhaps this is a reference, in which case the build manager is cacheing its generator
		if (answer == null && parent == null) {
			answer = ManagedBuildManager.getDependencyGenerator(toolId);
		}
		
		return answer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getErrorParserIds()
	 */
	public String getErrorParserIds() {
		if (errorParserIds == null) {
			// If I have a parent, ask it
			if (parent != null) {
				return parent.getErrorParserIds();
			}
		}
		return errorParserIds;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getErrorParserList()
	 */
	public String[] getErrorParserList() {
		String parserIDs = getErrorParserIds();
		String[] errorParsers = null;
		if (parserIDs != null) {
			// Check for an empty string
			if (parserIDs.length() == 0) {
				errorParsers = new String[0];
			} else {
				StringTokenizer tok = new StringTokenizer(parserIDs, ";"); //$NON-NLS-1$
				List list = new ArrayList(tok.countTokens());
				while (tok.hasMoreElements()) {
					list.add(tok.nextToken());
				}
				String[] strArr = {""};	//$NON-NLS-1$
				errorParsers = (String[]) list.toArray(strArr);
			}
		} else {
			// If no error parsers are specified by the target, the default is 
			// all error parsers
			errorParsers = CCorePlugin.getDefault().getAllErrorParsersIDs();
		}
		return errorParsers;
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
				// The user has forgotten to specify a command in the plugin manifest
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
		// If I am unnamed, see if I can inherit one from my parent
		if (name == null) {
			if (parent != null) {
				return parent.getName();
			} else {
				return new String(""); //$NON-NLS-1$
			}
		} else {
			return name;
		}
	}

	/* (non-javadoc)
	 * 
	 * @param tool
	 * @return List
	 */
	protected List getOptionReferences(ITool tool) {
		List references = new ArrayList();
		
		// Get all the option references I add for this tool
		ToolReference toolRef = getToolReference(tool);
		if (toolRef != null) {
			references.addAll(toolRef.getOptionReferenceList());
		}
		
		// See if there is anything that my parents add that I don't
		if (parent != null) {
			List temp = ((Target)parent).getOptionReferences(tool);
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getOwner()
	 */
	public IResource getOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getParent()
	 */
	public ITarget getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#getTargetArchList()
	 */
	public String[] getTargetArchList() {
		if (targetArchList == null) {
			// Ask parent for its list
			if (parent != null) {
				return parent.getTargetArchList();
			} else {
				// I have no parent and no defined list
				return new String[] {"all"}; //$NON-NLS-1$
			}
		}
		return (String[]) targetArchList.toArray(new String[targetArchList.size()]);
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
				// I have no parent and no defined filter list
				return new String[] {"all"};	//$NON-NLS-1$
			}
		}
		return (String[]) targetOSList.toArray(new String[targetOSList.size()]);
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

	/* (non-Javadoc)
	 * A safe accessor method for the list of tools maintained by the 
	 * target
	 * 
	 */
	private List getToolList() {
		if (toolList == null) {
			toolList = new ArrayList();
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
		Vector toolArray = new Vector();
		addToolsToArray(toolArray);
		return (ITool[]) toolArray.toArray(new ITool[toolArray.size()]);
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
	 * @see org.eclipse.cdt.core.build.managed.ITarget#isAbstract()
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#isDirty()
	 */
	public boolean isDirty() {
		// If I need saving, just say yes
		if (isDirty) {
			return true;
		}
		
		// Iterate over the configurations and ask them if they need saving
		Iterator iter = getConfigurationList().listIterator();
		while (iter.hasNext()) {
			if (((IConfiguration)iter.next()).isDirty()) {
				return true;
			}
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#isTestTarget()
	 */
	public boolean isTestTarget() {
		return isTest;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#needsRebuild()
	 */
	public boolean needsRebuild(){
		// Iterate over the configurations and ask them if they need saving
		Iterator iter = getConfigurationList().listIterator();
		while (iter.hasNext()) {
			if (((IConfiguration)iter.next()).needsRebuild()) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#removeConfiguration(java.lang.String)
	 */
	public void removeConfiguration(String id) {
		// Remove the specified configuration from the list and map
		Iterator iter = getConfigurationList().listIterator();
		while (iter.hasNext()) {
			 IConfiguration config = (IConfiguration)iter.next();
			 if (config.getId().equals(id)) {
			 	getConfigurationList().remove(config);
				getConfigurationMap().remove(id);
				isDirty = true;
			 	break;
			 }
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#resetMakeCommand()
	 */
	public void resetMakeCommand() {
		// Flag target as dirty if the reset actually changes something
		if (makeCommand != null) {
			setDirty(true);
		}
		makeCommand = null;
		makeArguments = null;
	}
	
	/**
	 * 
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			IManagedConfigElement element = ManagedBuildManager.getConfigElement(this);
			// parent
			String parentId = element.getAttribute(PARENT);
			if (parentId != null) {
				parent = ManagedBuildManager.getTarget(null, parentId);
				// should resolve before calling methods on it
				((Target)parent).resolveReferences();
				// copy over the parents configs
				IConfiguration[] parentConfigs = parent.getConfigurations();
				for (int i = 0; i < parentConfigs.length; ++i)
					addConfiguration(parentConfigs[i]);
			}

			// call resolve references on any children
			Iterator toolIter = getToolList().iterator();
			while (toolIter.hasNext()) {
				Tool current = (Tool)toolIter.next();
				current.resolveReferences();
			}
			Iterator refIter = getLocalToolReferences().iterator();
			while (refIter.hasNext()) {
				ToolReference current = (ToolReference)refIter.next();
				current.resolveReferences();
			}
			Iterator configIter = getConfigurationList().iterator();
			while (configIter.hasNext()) {
				Configuration current = (Configuration)configIter.next();
				current.resolveReferences();
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

		if (makeCommand != null) {
			element.setAttribute(MAKE_COMMAND, makeCommand);
		} else {
			// Make sure we use the default
		}
		
		if (makeArguments != null) {
			element.setAttribute(MAKE_ARGS, makeArguments);
		}
		if (errorParserIds != null) {
			element.setAttribute(ERROR_PARSERS, errorParserIds);
		}

		// Serialize the configuration settings
		Iterator iter = getConfigurationList().listIterator();
		while (iter.hasNext()) {
			Configuration config = (Configuration) iter.next();
			Element configElement = doc.createElement(IConfiguration.CONFIGURATION_ELEMENT_NAME);
			element.appendChild(configElement);
			config.serialize(doc, configElement);
		}
		
		// I am clean now
		isDirty = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#setArtifactExtension(java.lang.String)
	 */
	public void setArtifactExtension(String extension) {
		if (extension != null) {
			this.extension = extension;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITarget#setArtifactName(java.lang.String)
	 */
	public void setArtifactName(String name) {
		if (name != null) {
			artifactName = name;
			setRebuildState(true);
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		// Override the dirty flag here
		this.isDirty = isDirty;
		// and in the configurations
		Iterator iter = getConfigurationList().listIterator();
		while (iter.hasNext()) {
			IConfiguration config = (IConfiguration)iter.next();
			config.setDirty(isDirty);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#setErrorParserIds()
	 */
	public void setErrorParserIds(String ids) {
		if (ids == null) return;
		String currentIds = getErrorParserIds();
		if (currentIds == null || !(currentIds.equals(ids))) {
			errorParserIds = ids;
			isDirty = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#setMakeArguments(java.lang.String)
	 */
	public void setMakeArguments(String makeArgs) {
		if (makeArgs != null && !getMakeArguments().equals(makeArgs)) {
			makeArguments = makeArgs;
			setRebuildState(true);
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#setMakeCommand(java.lang.String)
	 */
	public void setMakeCommand(String command) {
		if (command != null && !getMakeCommand().equals(command)) {
			makeCommand = command;
			setRebuildState(true);
			isDirty = true;
		}
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITarget#setRebuildState(boolean)
	 */
	public void setRebuildState(boolean rebuild) {
		Iterator iter = getConfigurationList().listIterator();
		while (iter.hasNext()) {
			((IConfiguration)iter.next()).setRebuildState(rebuild);
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
