/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.UserDefinedEnvironmentSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.UserDefinedMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Configuration extends BuildObject implements IConfiguration {
	
	private static final String EMPTY_STRING = new String();
	
	//  Parent and children
	private IConfiguration parent;
	private ProjectType projectType;
	private ManagedProject managedProject;
	private ToolChain toolChain;
	private List resourceConfigurationList;
	private Map resourceConfigurationMap;
	//  Managed Build model attributes
	private String artifactName;
	private String cleanCommand;
	private String artifactExtension;
	private String errorParserIds;
    private String prebuildStep; 
    private String postbuildStep; 
    private String preannouncebuildStep; 
    private String postannouncebuildStep;   
	private String description;
	//  Miscellaneous
	private boolean isExtensionConfig = false;
	private boolean isDirty = false;
	private boolean rebuildNeeded = false;
	private boolean resolved = true;
	private boolean isTemporary = false;

	
	/*
	 *  C O N S T R U C T O R S
	 */

	/**
	 * Create an extension configuration from the project manifest file element.
	 * 
	 * @param projectType The <code>ProjectType</code> the configuration will be added to. 
	 * @param element The element from the manifest that contains the configuration information.
	 * @param managedBuildRevision 
	 */
	public Configuration(ProjectType projectType, IManagedConfigElement element, String managedBuildRevision) {
		this.projectType = projectType;
		isExtensionConfig = true;
		
		// setup for resolving
		resolved = false;
		
		setManagedBuildRevision(managedBuildRevision);
		
		// Initialize from the XML attributes
		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionConfiguration(this);
		
		// Hook me up to the ProjectType
		if (projectType != null) {
			projectType.addConfiguration(this);
		}
		
		// Load the children
		IManagedConfigElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IManagedConfigElement configElement = configElements[l];
			if (configElement.getName().equals(IToolChain.TOOL_CHAIN_ELEMENT_NAME)) {
				toolChain = new ToolChain(this, configElement, managedBuildRevision);
			}else if (configElement.getName().equals(IResourceConfiguration.RESOURCE_CONFIGURATION_ELEMENT_NAME)) {
				ResourceConfiguration resConfig = new ResourceConfiguration(this, configElement, managedBuildRevision);
				addResourceConfiguration(resConfig);
			}
		}
	}

	/**
	 * Create a new extension configuration based on one already defined.
	 * 
	 * @param projectType The <code>ProjectType</code> the configuration will be added to. 
	 * @param parentConfig The <code>IConfiguration</code> that is the parent configuration of this configuration
	 * @param id A unique ID for the new configuration.
	 */
	public Configuration(ProjectType projectType, IConfiguration parentConfig, String id) {
		setId(id);
		this.projectType = projectType;
		isExtensionConfig = true;
		
		// setup for resolving
		resolved = false;

		if (parentConfig != null) {
			name = parentConfig.getName();
			// If this contructor is called to clone an existing 
			// configuration, the parent of the parent should be stored. 
			// As of 2.1, there is still one single level of inheritence to
			// worry about
			parent = parentConfig.getParent() == null ? parentConfig : parentConfig.getParent();
		}
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionConfiguration(this);
		
		// Hook me up to the ProjectType
		if (projectType != null) {
			projectType.addConfiguration(this);			
			// set managedBuildRevision
			setManagedBuildRevision(projectType.getManagedBuildRevision());
		}
	}

	/**
	 * Create a new extension configuration and fill in the attributes and childen later.
	 * 
	 * @param projectType The <code>ProjectType</code> the configuration will be added to. 
	 * @param parentConfig The <code>IConfiguration</code> that is the parent configuration of this configuration
	 * @param id A unique ID for the new configuration.
	 * @param name A name for the new configuration.
	 */
	public Configuration(ProjectType projectType, IConfiguration parentConfig, String id, String name) {
		setId(id);
		setName(name);
		this.projectType = projectType;
		parent = parentConfig;
		isExtensionConfig = true;
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionConfiguration(this);
		
		// Hook me up to the ProjectType
		if (projectType != null) {
			projectType.addConfiguration(this);
			setManagedBuildRevision(projectType.getManagedBuildRevision());
		}
	}

	/**
	 * Create a <code>Configuration</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param managedProject The <code>ManagedProject</code> the configuration will be added to. 
	 * @param element The XML element that contains the configuration settings.
	 * 
	 */
	public Configuration(ManagedProject managedProject, Element element, String managedBuildRevision) {
		this.managedProject = managedProject;
		isExtensionConfig = false;
		
		setManagedBuildRevision(managedBuildRevision);
		
		// Initialize from the XML attributes
		loadFromProject(element);

		// Hook me up
		managedProject.addConfiguration(this);

		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(IToolChain.TOOL_CHAIN_ELEMENT_NAME)) {
				toolChain = new ToolChain(this, (Element)configElement, managedBuildRevision);
			}else if (configElement.getNodeName().equals(IResourceConfiguration.RESOURCE_CONFIGURATION_ELEMENT_NAME)) {
				ResourceConfiguration resConfig = new ResourceConfiguration(this, (Element)configElement, managedBuildRevision);
				addResourceConfiguration(resConfig);
			}
		}
	}

	/**
	 * Create a new project, non-extension, configuration based on one already defined.
	 * 
	 * @param managedProject The <code>ManagedProject</code> the configuration will be added to. 
	 * @param cloneConfig The <code>IConfiguration</code> to copy the settings from.
	 * @param id A unique ID for the new configuration.
	 * @param cloneChildren If <code>true</code>, the configuration's tools are cloned 
	 */
	public Configuration(ManagedProject managedProject, Configuration cloneConfig, String id, boolean cloneChildren, boolean temporary) {
		setId(id);
		setName(cloneConfig.getName());
		this.description = cloneConfig.getDescription();
		this.managedProject = managedProject;
		isExtensionConfig = false;
		this.isTemporary = temporary;

		// set managedBuildRevision
		setManagedBuildRevision(cloneConfig.getManagedBuildRevision());
		
		// If this contructor is called to clone an existing 
		// configuration, the parent of the cloning config should be stored. 
		parent = cloneConfig.getParent() == null ? cloneConfig : cloneConfig.getParent();
	
		//  Copy the remaining attributes
		projectType = cloneConfig.projectType;
		if (cloneConfig.artifactName != null) {
			artifactName = new String(cloneConfig.artifactName);
		}
		if (cloneConfig.cleanCommand != null) {
			cleanCommand = new String(cloneConfig.cleanCommand);
		}
		if (cloneConfig.artifactExtension != null) {
			artifactExtension = new String(cloneConfig.artifactExtension);
		}
		if (cloneConfig.errorParserIds != null) {
			errorParserIds = new String(cloneConfig.errorParserIds);
		}
        if (cloneConfig.prebuildStep != null) {
			prebuildStep = new String(cloneConfig.prebuildStep);
		}
		if (cloneConfig.postbuildStep != null) {
			postbuildStep = new String(cloneConfig.postbuildStep);
		}
		if (cloneConfig.preannouncebuildStep != null) {
			preannouncebuildStep = new String(cloneConfig.preannouncebuildStep);
		}
		if (cloneConfig.postannouncebuildStep != null) {
			postannouncebuildStep = new String(cloneConfig.postannouncebuildStep);
		} 
		
		// Clone the configuration's children
		// Tool Chain
		String subId;
		String subName;
		if (cloneConfig.parent != null) {
			subId = ManagedBuildManager.calculateChildId(
						cloneConfig.parent.getToolChain().getId(),
						null);
			subName = cloneConfig.parent.getToolChain().getName();
			
		} else {
			subId = ManagedBuildManager.calculateChildId(
						cloneConfig.getToolChain().getId(),
						null);
			subName = cloneConfig.getToolChain().getName();
		}
		
		if (cloneChildren) {
		    toolChain = new ToolChain(this, subId, subName, (ToolChain)cloneConfig.getToolChain());
		    
			//copy expand build macros setting
			BuildMacroProvider macroProvider = (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
			macroProvider.expandMacrosInBuildfile(this,
						macroProvider.areMacrosExpandedInBuildfile(cloneConfig));

			//copy user-defined build macros
			UserDefinedMacroSupplier userMacros = BuildMacroProvider.fUserDefinedMacroSupplier;
			userMacros.setMacros(
					userMacros.getMacros(BuildMacroProvider.CONTEXT_CONFIGURATION,cloneConfig),
					BuildMacroProvider.CONTEXT_CONFIGURATION,
					this);
			
			//copy user-defined environment
			UserDefinedEnvironmentSupplier userEnv = EnvironmentVariableProvider.fUserSupplier;
			userEnv.setVariables(
					userEnv.getVariables(cloneConfig), this);

		} else {
			// Add a tool-chain element that specifies as its superClass the 
			// tool-chain that is the child of the configuration.
			ToolChain superChain = (ToolChain)cloneConfig.getToolChain();
			subId = ManagedBuildManager.calculateChildId(
						superChain.getId(),
						null);
			IToolChain newChain = createToolChain(superChain, subId, superChain.getName(), false);
			
			// For each option/option category child of the tool-chain that is
			// the child of the selected configuration element, create an option/
			// option category child of the cloned configuration's tool-chain element
			// that specifies the original tool element as its superClass.
			newChain.createOptions(superChain);

			// For each tool element child of the tool-chain that is the child of 
			// the selected configuration element, create a tool element child of 
			// the cloned configuration's tool-chain element that specifies the 
			// original tool element as its superClass.
			ITool[] tools = superChain.getTools();
			for (int i=0; i<tools.length; i++) {
			    Tool toolChild = (Tool)tools[i];
			    subId = ManagedBuildManager.calculateChildId(toolChild.getId(),null);
			    newChain.createTool(toolChild, subId, toolChild.getName(), false);
			}
		}

		//  Resource Configurations
		if (cloneConfig.resourceConfigurationList != null) {
			List resElements = cloneConfig.getResourceConfigurationList();
			Iterator iter = resElements.listIterator();
			while (iter.hasNext()) {
				ResourceConfiguration resConfig = (ResourceConfiguration) iter.next();
				subId = getId() + "." + ManagedBuildManager.getRandomNumber(); //$NON-NLS-1$
				ResourceConfiguration newResConfig = new ResourceConfiguration(this, resConfig, subId);
				addResourceConfiguration(newResConfig);
			}
		}
		
		// Hook me up
		managedProject.addConfiguration(this);
		setDirty(true);
		rebuildNeeded = true;
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Initialize the configuration information from an element in the 
	 * manifest file or provided by a dynamicElementProvider
	 * 
	 * @param element An obejct implementing IManagedConfigElement 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(IConfiguration.ID));

		// name
		name = element.getAttribute(IConfiguration.NAME);
		
		// description
		description = element.getAttribute(IConfiguration.DESCRIPTION);
		
		// parent
		String parentID = element.getAttribute(IConfiguration.PARENT);
		if (parentID != null) {
			// Lookup the parent configuration by ID
			parent = ManagedBuildManager.getExtensionConfiguration(parentID);
		}

		// Get the name of the build artifact associated with configuration
		artifactName = element.getAttribute(ARTIFACT_NAME);
		
		// Get the semicolon separated list of IDs of the error parsers
		errorParserIds = element.getAttribute(ERROR_PARSERS);

		// Get the artifact extension
		artifactExtension = element.getAttribute(EXTENSION);
		
		// Get the clean command
		cleanCommand = element.getAttribute(CLEAN_COMMAND);
               
        // Get the pre-build and post-build commands            
        prebuildStep = element.getAttribute(PREBUILD_STEP);     
        postbuildStep = element.getAttribute(POSTBUILD_STEP);           
               
        // Get the pre-build and post-build announcements               
        preannouncebuildStep = element.getAttribute(PREANNOUNCEBUILD_STEP); 
        postannouncebuildStep = element.getAttribute(POSTANNOUNCEBUILD_STEP); 
	}
	
	/* (non-Javadoc)
	 * Initialize the configuration information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the configuration information 
	 */
	protected void loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IConfiguration.ID));

		// name
		if (element.hasAttribute(IConfiguration.NAME))
			setName(element.getAttribute(IConfiguration.NAME));
		
		// description
		if (element.hasAttribute(IConfiguration.DESCRIPTION))
			this.description = element.getAttribute(IConfiguration.DESCRIPTION);
		
		if (element.hasAttribute(IConfiguration.PARENT)) {
			// See if the parent belongs to the same project
			parent = managedProject.getConfiguration(element.getAttribute(IConfiguration.PARENT));
			// If not, then try the extension configurations
			if (parent == null) {
				parent = ManagedBuildManager.getExtensionConfiguration(element.getAttribute(IConfiguration.PARENT));
			}
		}

		// Get the name of the build artifact associated with target (usually 
		// in the plugin specification).
		if (element.hasAttribute(ARTIFACT_NAME)) {
			artifactName = element.getAttribute(ARTIFACT_NAME);
		}
		
		// Get the semicolon separated list of IDs of the error parsers
		if (element.hasAttribute(ERROR_PARSERS)) {
			errorParserIds = element.getAttribute(ERROR_PARSERS);
		}

		// Get the artifact extension
		if (element.hasAttribute(EXTENSION)) {
			artifactExtension = element.getAttribute(EXTENSION);
		}
		
		// Get the clean command
		if (element.hasAttribute(CLEAN_COMMAND)) {
			cleanCommand = element.getAttribute(CLEAN_COMMAND);
		}
               
        // Get the pre-build and post-build commands
		if (element.hasAttribute(PREBUILD_STEP)) {
			prebuildStep = element.getAttribute(PREBUILD_STEP);
		}

		if (element.hasAttribute(POSTBUILD_STEP)) {
			postbuildStep = element.getAttribute(POSTBUILD_STEP);
		}

		// Get the pre-build and post-build announcements
		if (element.hasAttribute(PREANNOUNCEBUILD_STEP)) {
			preannouncebuildStep = element.getAttribute(PREANNOUNCEBUILD_STEP);
		}

		if (element.hasAttribute(POSTANNOUNCEBUILD_STEP)) {
			postannouncebuildStep = element
					.getAttribute(POSTANNOUNCEBUILD_STEP);
		}               
	}

	/**
	 * Persist this configuration to project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(IConfiguration.ID, id);
		
		if (name != null)
			element.setAttribute(IConfiguration.NAME, name);
			
		if (description != null)
			element.setAttribute(IConfiguration.DESCRIPTION, description);
		
		if (parent != null)
			element.setAttribute(IConfiguration.PARENT, parent.getId());
		
		if (artifactName != null)
			element.setAttribute(ARTIFACT_NAME, artifactName);
		
		if (errorParserIds != null)
			element.setAttribute(ERROR_PARSERS, errorParserIds);

		if (artifactExtension != null)
			element.setAttribute(EXTENSION, artifactExtension);

		if (cleanCommand != null)
			element.setAttribute(CLEAN_COMMAND, cleanCommand);

		if (prebuildStep != null)
			element.setAttribute(PREBUILD_STEP, prebuildStep);

		if (postbuildStep != null)
			element.setAttribute(POSTBUILD_STEP, postbuildStep);

		if (preannouncebuildStep != null)
			element.setAttribute(PREANNOUNCEBUILD_STEP, preannouncebuildStep);

		if (postannouncebuildStep != null)
			element.setAttribute(POSTANNOUNCEBUILD_STEP, postannouncebuildStep);

		// Serialize my children
		Element toolChainElement = doc.createElement(IToolChain.TOOL_CHAIN_ELEMENT_NAME);
		element.appendChild(toolChainElement);
		toolChain.serialize(doc, toolChainElement);
		List resElements = getResourceConfigurationList();
		Iterator iter = resElements.listIterator();
		while (iter.hasNext()) {
			ResourceConfiguration resConfig = (ResourceConfiguration) iter.next();
			Element resElement = doc.createElement(IResourceConfiguration.RESOURCE_CONFIGURATION_ELEMENT_NAME);
			element.appendChild(resElement);
			resConfig.serialize(doc, resElement);
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getParent()
	 */
	public IConfiguration getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getOwner()
	 */
	public IResource getOwner() {
		if (managedProject != null)
			return managedProject.getOwner();
		else {
			return null;	// Extension configurations don't have an "owner"
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getProjectType()
	 */
	public IProjectType getProjectType() {
		return (IProjectType)projectType;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getManagedProject()
	 */
	public IManagedProject getManagedProject() {
		return (IManagedProject)managedProject;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getToolChain(IToolChain, String, String, boolean)
	 */
	public IToolChain createToolChain(IToolChain superClass, String Id, String name, boolean isExtensionElement) {
		toolChain = new ToolChain(this, superClass, Id, name, isExtensionElement);
		setDirty(true);
		return (IToolChain)toolChain;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getToolChain()
	 */
	public IToolChain getToolChain() {
		return (IToolChain)toolChain;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getResourceConfigurations()
	 */
	public IResourceConfiguration[] getResourceConfigurations() {
		IResourceConfiguration[] resConfigs = new IResourceConfiguration[getResourceConfigurationList().size()];
		Iterator iter = getResourceConfigurationList().listIterator();
		int i = 0;
		while (iter.hasNext()) {
			ResourceConfiguration resConfig = (ResourceConfiguration)iter.next();
			resConfigs[i++] = (IResourceConfiguration)resConfig; 
		}
		return resConfigs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getResourceConfiguration(java.lang.String)
	 */
	public IResourceConfiguration getResourceConfiguration(String resPath) {
		ResourceConfiguration resConfig = (ResourceConfiguration)getResourceConfigurationMap().get(resPath);
		return (IResourceConfiguration)resConfig;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getFilteredTools()
	 */
	public ITool[] getFilteredTools() {
		if (toolChain == null) {
			return new ITool[0];
		}
		ITool[] localTools = toolChain.getTools();
		IManagedProject manProj = getManagedProject();
		if (manProj == null) {
			//  If this is not associated with a project, then there is nothing to filter with
			return localTools;
		}
		IProject project = (IProject)manProj.getOwner();
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getTools()
	 */
	public ITool[] getTools() {
		return toolChain.getTools();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getTool(java.lang.String)
	 */
	public ITool getTool(String id) {
		return toolChain.getTool(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getTargetTool()
	 */
	public ITool getTargetTool() {
		String[] targetToolIds = toolChain.getTargetToolList();
		if (targetToolIds == null || targetToolIds.length == 0) return null;
		
		//  For each target tool id, in list order,
		//  look for a tool with this ID, or a tool with a superclass with this id.
		//  Stop when we find a match
		ITool[] tools = getFilteredTools();
		for (int i=0; i<targetToolIds.length; i++) {
			String targetToolId = targetToolIds[i];
			for (int j=0; j<tools.length; j++) {
				ITool targetTool = tools[j];
				ITool tool = targetTool;
				do {
					if (targetToolId.equals(tool.getId())) {
						return targetTool;
					}		
					tool = tool.getSuperClass();
				} while (tool != null);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setToolCommand(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String)
	 */
	public String getToolCommand(ITool tool) {
		// TODO:  Do we need to verify that the tool is part of the configuration?
		return tool.getToolCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setToolCommand(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String)
	 */
	public void setToolCommand(ITool tool, String command) {
		// TODO:  Do we need to verify that the tool is part of the configuration?
		tool.setToolCommand(command);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, boolean)
	 */
	public IOption setOption(IHoldsOptions holder, IOption option, boolean value) throws BuildException {
		// Is there a change?
		IOption retOpt = option;
		if (option.getBooleanValue() != value) {
			if (option.isExtensionElement()) {
				//  If the extension element was created from an MBS 2.0 model OptionReference element, hook the
				//  new option up to its superclass directly.  This is to avoid references to oddly id'ed
				//  elements that are automatically generated from V2.0 model optionReferences.  If these
				//  end up in the project file, then the project could have a problem when the integration
				//  provider switches to providing the new model.
				IOption newSuperClass = option;
				if (((Option)option).wasOptRef()) {
					newSuperClass = option.getSuperClass();
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
			rebuildNeeded = true;
		}
		return retOpt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, java.lang.String)
	 */
	public IOption setOption(IHoldsOptions holder, IOption option, String value) throws BuildException {
		IOption retOpt = option;
		String oldValue;
		oldValue = option.getStringValue(); 
		if (oldValue != null && !oldValue.equals(value)) {
			if (option.isExtensionElement()) {
				//  If the extension element was created from an MBS 2.0 model OptionReference element, hook the
				//  new option up to its superclass directly.  This is to avoid references to oddly id'ed
				//  elements that are automatically generated from V2.0 model optionReferences.  If these
				//  end up in the project file, then the project could have a problem when the integration
				//  provider switches to providing the new model.
				IOption newSuperClass = option;
				if (((Option)option).wasOptRef()) {
					newSuperClass = option.getSuperClass();
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
			rebuildNeeded = true;
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
			if (option.isExtensionElement()) {
				//  If the extension element was created from an MBS 2.0 model OptionReference element, hook the
				//  new option up to its superclass directly.  This is to avoid references to oddly id'ed
				//  elements that are automatically generated from V2.0 model optionReferences.  If these
				//  end up in the project file, then the project could have a problem when the integration
				//  provider switches to providing the new model.
				IOption newSuperClass = option;
				if (((Option)option).wasOptRef()) {
					newSuperClass = option.getSuperClass();
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
			rebuildNeeded = true;
		} 
		return retOpt;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the list of resource configs.
	 * 
	 * @return List containing the tools
	 */
	private List getResourceConfigurationList() {
		if (resourceConfigurationList == null) {
			resourceConfigurationList = new ArrayList();
		}
		return resourceConfigurationList;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the map of resource paths to resource configs
	 * 
	 * @return
	 */
	private Map getResourceConfigurationMap() {
		if (resourceConfigurationMap == null) {
			resourceConfigurationMap = new HashMap();
		}
		return resourceConfigurationMap;
	}

	/* (non-Javadoc)
	 * Adds the Resource Configuration to the Resource Configuration list and map
	 * 
	 * @param resConfig
	 */
	public void addResourceConfiguration(ResourceConfiguration resConfig) {
		getResourceConfigurationList().add(resConfig);
		getResourceConfigurationMap().put(resConfig.getResourcePath(), resConfig);
		isDirty = true;
		rebuildNeeded = true;
	}

	public void removeResourceConfiguration(IResourceConfiguration resConfig) {
		getResourceConfigurationList().remove(resConfig);
		getResourceConfigurationMap().remove(resConfig.getResourcePath());
		isDirty = true;
		rebuildNeeded = true;
	}
	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getName()
	 */
	public String getName() {
		return (name == null && parent != null) ? parent.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getArtifactExtension()
	 */
	public String getArtifactExtension() {
		if (artifactExtension == null) {
			// Ask my parent first
			if (parent != null) {
				return parent.getArtifactExtension();
			} else {
				return EMPTY_STRING;
			}
		} else {
			return artifactExtension;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getArtifactName()
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
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getBuildArguments()
	 */
	public String getBuildArguments() {
		IToolChain tc = getToolChain();
		IBuilder builder = tc.getBuilder();
		if (builder != null) {
		    return builder.getArguments();
		}
		return new String("-k"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getBuildCommand()
	 */
	public String getBuildCommand() {
		IToolChain tc = getToolChain();
		IBuilder builder = tc.getBuilder();
		if (builder != null) {
		    return builder.getCommand();		
		}
		return new String("make"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getPrebuildStep()
	 */
	public String getPrebuildStep() {
		if (prebuildStep == null) {
			// If I have a parent, ask it
			if (parent != null) {
				return parent.getPrebuildStep();
			} else {
				// I'm it
				return EMPTY_STRING;
			}
		} else {
			return prebuildStep;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getPostbuildStep()
	 */
	public String getPostbuildStep() {
		if (postbuildStep == null) {
			// If I have a parent, ask it
			if (parent != null) {
				return parent.getPostbuildStep();
			} else {
				// I'm it
				return EMPTY_STRING;
			}
		} else {
			return postbuildStep;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getPreannouncebuildStep()
	 */
	public String getPreannouncebuildStep() {
		if (preannouncebuildStep == null) {
			// If I have a parent, ask it
			if (parent != null) {
				return parent.getPreannouncebuildStep();
			} else {
				// I'm it
				return EMPTY_STRING;
			}
		} else {
			return preannouncebuildStep;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getPostannouncebuildStep()
	 */
	public String getPostannouncebuildStep() {
		if (postannouncebuildStep == null) {
			// If I have a parent, ask it
			if (parent != null) {
				return parent.getPostannouncebuildStep();
			} else {
				// I'm it
				return EMPTY_STRING;
			}
		} else {
			return postannouncebuildStep;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getCleanCommand()
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
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getDescription()
	 */
	public String getDescription() {
		if (description == null) {
			// If I have a parent, ask it
			if (parent != null) {
				return parent.getDescription();
			} else {
				// I'm it
				return EMPTY_STRING;
			}
		} else {
			return description;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getErrorParserIds()
	 */
	public String getErrorParserIds() {
		if (errorParserIds != null) {
			return errorParserIds;
		}			
		// If I have a parent, ask it
		String errorParsers = null;
		if (parent != null) {
			errorParsers = parent.getErrorParserIds();
		}
		// If no error parsers are specified by the configuration, the default
		// is
		// the error parsers from the tool-chain
		if (errorParsers == null && toolChain != null) {
			errorParsers = toolChain.getErrorParserIds(this);
		}
		return errorParsers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getErrorParserList()
	 */
	public String[] getErrorParserList() {
		String parserIDs = getErrorParserIds();
		String[] errorParsers;
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
			// If no error parsers are specified, the default is 
			// all error parsers
			errorParsers = CCorePlugin.getDefault().getAllErrorParsersIDs();
		}
		return errorParsers;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setArtifactExtension(java.lang.String)
	 */
	public void setArtifactExtension(String extension) {
		if (extension == null && artifactExtension == null) return;
		if (artifactExtension == null || extension == null || !artifactExtension.equals(extension)) {
			artifactExtension = extension;
			rebuildNeeded = true;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setArtifactName(java.lang.String)
	 */
	public void setArtifactName(String name) {
		if (name == null && artifactName == null) return;
		if (artifactName == null || name == null || !artifactName.equals(name)) {
			artifactName = name;
			rebuildNeeded = true;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setErrorParserIds()
	 */
	public void setErrorParserIds(String ids) {
		String currentIds = getErrorParserIds();
		if (ids == null && currentIds == null) return;
		if (currentIds == null || ids == null || !(currentIds.equals(ids))) {
			errorParserIds = ids;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setCleanCommand()
	 */
	public void setCleanCommand(String command) {
		if (command == null && cleanCommand == null) return;
		if (cleanCommand == null || command == null || !cleanCommand.equals(command)) {
			cleanCommand = command;
			isDirty = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		 if (description == null && this.description == null) return; 
	        if (this.description == null || description == null || !description.equals(this.description)) { 
				this.description = description; 
	            isDirty = true; 
	        }       
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setBuildArguments()
	 */
	public void setBuildArguments(String makeArgs) {
		IToolChain tc = getToolChain();
		IBuilder builder = tc.getBuilder();
		if(makeArgs == null){ //resetting the build arguments
			if(!builder.isExtensionElement()){
				builder.setArguments(makeArgs);
				rebuildNeeded = true;
			}
		}else if(!makeArgs.equals(builder.getArguments())){
			if (builder.isExtensionElement()) {
				String subId = ManagedBuildManager.calculateChildId(builder.getId(), null);
				String builderName = builder.getName() + "." + getName(); 	//$NON-NLS-1$
				builder = toolChain.createBuilder(builder, subId, builderName, false);
			}
			builder.setArguments(makeArgs);
			rebuildNeeded = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setBuildCommand()
	 */
	public void setBuildCommand(String command) {
		IToolChain tc = getToolChain();
		IBuilder builder = tc.getBuilder();
		if(command == null){ //resetting the build command
			if(!builder.isExtensionElement()){
				builder.setCommand(command);
				rebuildNeeded = true;
			}
		} else if(!command.equals(builder.getCommand())){
			if (builder.isExtensionElement()) {
				String subId = ManagedBuildManager.calculateChildId(builder.getId(), null);
				String builderName = builder.getName() + "." + getName(); 	//$NON-NLS-1$
				builder = toolChain.createBuilder(builder, subId, builderName, false);
			}
			builder.setCommand(command);
			rebuildNeeded = true;
		}
	}
 
    /* (non-Javadoc) 
     * @see org.eclipse.cdt.core.build.managed.IConfiguration#setPrebuildStep(java.lang.String) 
     */ 
    public void setPrebuildStep(String step) { 
        if (step == null && prebuildStep == null) return; 
        if (prebuildStep == null || step == null || !prebuildStep.equals(step)) { 
            prebuildStep = step; 
            rebuildNeeded = true;
            isDirty = true; 
        } 
    } 
	
 
    /* (non-Javadoc) 
     * @see org.eclipse.cdt.core.build.managed.IConfiguration#setPostbuildStep(java.lang.String) 
     */ 
    public void setPostbuildStep(String step) { 
        if (step == null && postbuildStep == null) return; 
        if (postbuildStep == null || step == null || !postbuildStep.equals(step)) { 
            postbuildStep = step; 
    		rebuildNeeded = true;
            isDirty = true; 
        }       
    } 
	
    /* (non-Javadoc) 
     * @see org.eclipse.cdt.core.build.managed.IConfiguration#setPreannouncebuildStep(java.lang.String) 
     */ 
    public void setPreannouncebuildStep(String announceStep) { 
        if (announceStep == null && preannouncebuildStep == null) return; 
        if (preannouncebuildStep == null || announceStep == null || !preannouncebuildStep.equals(announceStep)) {
            preannouncebuildStep = announceStep; 
    		rebuildNeeded = true;
            isDirty = true; 
        } 
    } 
 
    /* (non-Javadoc) 
     * @see org.eclipse.cdt.core.build.managed.IConfiguration#setPostannouncebuildStep(java.lang.String) 
     */ 
    public void setPostannouncebuildStep(String announceStep) { 
        if (announceStep == null && postannouncebuildStep == null) return; 
        if (postannouncebuildStep == null || announceStep == null || !postannouncebuildStep.equals(announceStep)) {
            postannouncebuildStep = announceStep; 
    		rebuildNeeded = true;
            isDirty = true; 
        } 
    } 
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isSupported()
	 */
	public boolean isSupported(){
		IToolChain toolChain = getToolChain();
		if(toolChain != null)
			return toolChain.isSupported();
		return false;
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionConfig;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension configuration
 		if (isExtensionConfig) return false;
		
		// If I need saving, just say yes
		if (isDirty) return true;
		
		// Otherwise see if any children need saving
		if (toolChain.isDirty()) return true;
		Iterator iter = getResourceConfigurationList().listIterator();
		while (iter.hasNext()) {
			ResourceConfiguration current = (ResourceConfiguration) iter.next();
			if (current.isDirty()) return true;
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
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		// Override the dirty flag
		this.isDirty = isDirty;
		// Propagate "false" to the children
		if (!isDirty) {
			toolChain.setDirty(false);
			Iterator iter = getResourceConfigurationList().listIterator();
			while (iter.hasNext()) {
				ResourceConfiguration current = (ResourceConfiguration) iter.next();
				current.setDirty(false);
			}		    
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setRebuildState(boolean)
	 */
	public void setRebuildState(boolean rebuild) {
		rebuildNeeded = rebuild;
		if(rebuild && !isTemporary())
			((EnvironmentVariableProvider)ManagedBuildManager.getEnvironmentVariableProvider()).checkBuildPathVariables(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#hasOverriddenBuildCommand()
	 */
	public boolean hasOverriddenBuildCommand() {
		IBuilder builder = getToolChain().getBuilder();
		if (builder != null) {
			IBuilder superB = builder.getSuperClass();
			if (superB != null) {
				String command = builder.getCommand();
				if (command != null) {
					String superC = superB.getCommand();
					if (superC != null) {
						if (!command.equals(superC)) {
							return true;
						}
					}
				}			
				String args = builder.getArguments();
				if (args != null) {
					String superA = superB.getArguments();
					if (superA != null) {
						if (!args.equals(superA)) {
							return true;
						}
					}
				}			
			}
		}
		return false;
	}
	
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			
			// call resolve references on any children
			toolChain.resolveReferences();
			Iterator resConfigIter = getResourceConfigurationList().iterator();
			while (resConfigIter.hasNext()) {
				ResourceConfiguration current = (ResourceConfiguration)resConfigIter.next();
				current.resolveReferences();
			}
		}
	}
	
	/**
	 * Reset the configuration's, tools', options
	 */
	public void reset() {
		// We just need to remove all Options
		ITool[] tools = getTools();
		IToolChain toolChain = getToolChain();
		IOption[] opts;
		
		// Send out the event to notify the options that they are about to be removed.
		// Do not do this for the child resource configurations as they are handled when
		// the configuration itself is destroyed.
		ManagedBuildManager.performValueHandlerEvent(this, IManagedOptionValueHandler.EVENT_CLOSE, false);
		// Remove the configurations		
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			opts = tool.getOptions();
			for (int j = 0; j < opts.length; j++) {
				tool.removeOption(opts[j]);
			}
		}
		opts = toolChain.getOptions();
		for (int j = 0; j < opts.length; j++) {
			toolChain.removeOption(opts[j]);
		}
		
		rebuildNeeded = true;
	}

	/*
	 *  Create a resource configuration object for the passed-in file
	 */
	public IResourceConfiguration createResourceConfiguration(IFile file)
	{	
		String path = file.getFullPath().toString();
		String resourceName = file.getName();
		String id = getId() + "." + ManagedBuildManager.getRandomNumber(); //$NON-NLS-1$
		ResourceConfiguration resConfig = new ResourceConfiguration( (IConfiguration) this, id, resourceName, path);
		
		//	Get file extension.
		String extString = file.getFileExtension();
		
		// Add the resource specific tools to this resource.
		ITool tools[] = getFilteredTools();
		String subId = new String();
		for (int i = 0; i < tools.length; i++) {
			if( tools[i].buildsFileType(extString) ) {
				subId = tools[i].getId() + "." + path; //$NON-NLS-1$
				resConfig.createTool(tools[i], subId, tools[i].getName(), false);
			}
		}
		 
		// Add this resource to the list.
		addResourceConfiguration(resConfig);
		ManagedBuildManager.performValueHandlerEvent(resConfig, IManagedOptionValueHandler.EVENT_OPEN);
		
		return resConfig;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getEnvironmentVariableSupplier()
	 */
	public IConfigurationEnvironmentVariableSupplier getEnvironmentVariableSupplier(){
		IToolChain toolChain = getToolChain();
		if(toolChain != null)
			return toolChain.getEnvironmentVariableSupplier();
		return null;
	}

	/**
	 * @return Returns the version.
	 */
	public PluginVersionIdentifier getVersion() {
		if ( version == null) {
			if ( toolChain != null) {
				return toolChain.getVersion();
			}
		}
		return version;
	}
	
	public void setVersion(PluginVersionIdentifier version) {
		// Do nothing
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#getBuildMacroSupplier()
	 */
	public IConfigurationBuildMacroSupplier getBuildMacroSupplier(){
		IToolChain toolChain = getToolChain();
		if(toolChain != null)
			return toolChain.getBuildMacroSupplier();
		return null;
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#isTemporary()
	 */
	public boolean isTemporary(){
		return isTemporary;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.core.BuildObject#updateManagedBuildRevision(java.lang.String)
	 */
	public void updateManagedBuildRevision(String revision){
		super.updateManagedBuildRevision(revision);
		toolChain.updateManagedBuildRevision(revision);
		
		for(Iterator iter = getResourceConfigurationList().iterator(); iter.hasNext();){
			((ResourceConfiguration)iter.next()).updateManagedBuildRevision(revision);
		}
	}
}
