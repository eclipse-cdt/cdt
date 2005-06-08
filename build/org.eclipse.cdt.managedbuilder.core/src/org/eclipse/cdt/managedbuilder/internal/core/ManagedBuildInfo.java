/**********************************************************************
 * Copyright (c) 2002,2005 IBM Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.ManagedBuildCPathEntryContainer;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @since 1.2
 */
public class ManagedBuildInfo implements IManagedBuildInfo, IScannerInfo {
	// The path container used for all managed projects
	public static final IContainerEntry containerEntry = CoreModel.newContainerEntry(new Path("org.eclipse.cdt.managedbuilder.MANAGED_CONTAINER"));	//$NON-NLS-1$
	private static final QualifiedName defaultConfigProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), DEFAULT_CONFIGURATION);
	private static final QualifiedName defaultTargetProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), DEFAULT_TARGET);
	public static final String MAJOR_SEPERATOR = ";"; //$NON-NLS-1$
	public static final String MINOR_SEPERATOR = "::"; //$NON-NLS-1$
	private static final String EMPTY_STRING = new String();

	private IManagedProject managedProject;
	private ICProject cProject;
	private IConfiguration defaultConfig;
	private String defaultConfigId;
	private boolean isDirty;
	private boolean isValid = false;
	private IResource owner;
	private boolean rebuildNeeded;
	private String version;
	private IConfiguration selectedConfig;

	private List targetList;
	private Map targetMap;
	
	private boolean isReadOnly = false;
	private boolean bIsContainerInited = false;
	

	/**
	 * Basic contructor used when the project is brand new.
	 * 
	 * @param owner
	 */
	public ManagedBuildInfo(IResource owner) {
		this.owner = owner;
		cProject = CoreModel.getDefault().create(owner.getProject());

		// Does not need a save but should be rebuilt
		isDirty = false;
		rebuildNeeded = true;

		// Get the default configs
		IProject project = owner.getProject();
		defaultConfigId = null;
		try {
			defaultConfigId = project.getPersistentProperty(defaultConfigProperty);
		} catch (CoreException e) {
			// Hitting this error just means the default config is not set
			return;
		}
	}
	
	/**
	 * Reads the build information from the project file and creates the 
	 * internal representation of the build settings for the project.
	 * 
	 * @param owner
	 * @param element
	 * @param managedBuildRevision
	 */
	public ManagedBuildInfo(IResource owner, Element element, String managedBuildRevision) {
		this(owner);
		
		// Recreate the managed build project element and its children
		NodeList projNodes = element.getElementsByTagName(IManagedProject.MANAGED_PROJECT_ELEMENT_NAME);
		// TODO:  There should only be 1?
		for (int projIndex = projNodes.getLength() - 1; projIndex >= 0; --projIndex) {
			ManagedProject proj = new ManagedProject(this, (Element)projNodes.item(projIndex), managedBuildRevision);
			if (!proj.resolveReferences())
				proj.setValid(false);
		}

		// Switch the rebuild off since this is an existing project
		rebuildNeeded = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#setManagedProject(IManagedProject)
	 */
	public void setManagedProject(IManagedProject managedProject) {
		this.managedProject = managedProject;
		//setDirty(true);  - It is primarily up to the ManagedProject to maintain the dirty state
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getManagedProject()
	 */
	public IManagedProject getManagedProject() {
		return managedProject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#buildsFileType(java.lang.String)
	 */
	public boolean buildsFileType(String srcExt) {
		// Check to see if there is a rule to build a file with this extension
		IConfiguration config = getDefaultConfiguration();
		ITool[] tools = config.getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool != null && tool.buildsFileType(srcExt)) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#getBuildArtifactExtension()
	 */
	public String getBuildArtifactExtension() {
		String ext = new String();
		IConfiguration config = getDefaultConfiguration();
		if (config != null) {
			ext = config.getArtifactExtension();
		} 
		return ext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getBuildArtifactName()
	 */
	public String getBuildArtifactName() {
		// Get the default configuration and use its value
		String name = new String();
		IConfiguration config = getDefaultConfiguration();
		if (config != null) {
			name = config.getArtifactName();
		}
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getCleanCommand()
	 */
	public String getCleanCommand() {
		// Get from the model
		String command = new String();
		IConfiguration config = getDefaultConfiguration();
		if (config != null) {
			command = config.getCleanCommand();
		}
		return command;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getConfigurationName()
	 */
	public String getConfigurationName() {
		// Return the human-readable name of the default configuration
		IConfiguration config = getDefaultConfiguration();
		return config == null ? new String() : config.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getConfigurationNames()
	 */
	public String[] getConfigurationNames() {
		ArrayList configNames = new ArrayList();
		IConfiguration[] configs = managedProject.getConfigurations();
		for (int i = 0; i < configs.length; i++) {
			IConfiguration configuration = configs[i];
			configNames.add(configuration.getName());
		}
		configNames.trimToSize();
		return (String[])configNames.toArray(new String[configNames.size()]);
	}

	public ICProject getCProject() {
		return cProject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getDefaultConfiguration()
	 */
	public IConfiguration getDefaultConfiguration() {
		// Get the default config associated with the project
		if (defaultConfig == null) {
			if (managedProject != null) {
				if (defaultConfigId != null) {
					defaultConfig = managedProject.getConfiguration(defaultConfigId);
				}
				if (defaultConfig == null) {
					IConfiguration[] configs = managedProject.getConfigurations();
					for (int i = 0; i < configs.length; i++){
						if (configs[i].isSupported()){
							defaultConfig = configs[i];
							defaultConfigId = defaultConfig.getId();
							break;
						}
					}
					if (defaultConfig == null && configs.length > 0) {
						defaultConfig = configs[0];
						defaultConfigId = defaultConfig.getId();
					}
				}
			}
		}
		return defaultConfig;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getDefinedSymbols()
	 */
	public Map getDefinedSymbols() {
		// Return the defined symbols for the default configuration
		HashMap symbols = getMacroPathEntries();
		return symbols; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#getDependencyGenerator(java.lang.String)
	 */
	public IManagedDependencyGenerator getDependencyGenerator(String sourceExtension) {
		// Find the tool and ask the Managed Build Manager for its dep generator
		try {
			if (getDefaultConfiguration() != null) {
				ITool[] tools = getDefaultConfiguration().getFilteredTools();
				for (int index = 0; index < tools.length; ++index) {
					if(tools[index].buildsFileType(sourceExtension)) {
						return tools[index].getDependencyGeneratorForExtension(sourceExtension);
					}
				}
			}
		} catch (NullPointerException e) {
			return null;
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * Helper method to extract a list of valid tools that are filtered by the 
	 * project nature.
	 * 
	 * @return
	 */
	private ITool[] getFilteredTools() {
		// Get all the tools for the current config filtered by the project nature
		IConfiguration config = getDefaultConfiguration();
		return config.getFilteredTools();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getFlagsForSource(java.lang.String)
	 */
	public String getFlagsForSource(String extension) {
		return getToolFlagsForSource(extension,null,null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#getToolFlagsForSource(java.lang.String, org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath)
	 */
	public String getToolFlagsForSource(String extension, IPath inputLocation, IPath outputLocation){
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool != null && tool.buildsFileType(extension)) {
				try {
					return tool.getToolCommandFlagsString(inputLocation,outputLocation);
				} catch (BuildException e) {
					return null;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getFlagsForConfiguration(java.lang.String)
	 */
	public String getFlagsForConfiguration(String extension) {
		return getToolFlagsForConfiguration(extension, null, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#getToolFlagsForConfiguration(java.lang.String, org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath)
	 */
	public String getToolFlagsForConfiguration(String extension, IPath inputLocation, IPath outputLocation){
		// Treat null extensions as an empty string
		String ext = extension == null ? new String() : extension;
		
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(ext)) {
				try {
					return tool.getToolCommandFlagsString(inputLocation,outputLocation);
				} catch (BuildException e) {
					return null;
				}
			}
		}
		return null;
	}

	private ArrayList getIncludePathEntries() {
		// Extract the resolved paths from the project (if any)
		ArrayList paths = new ArrayList();
		if (cProject != null) {
			try {
				IPathEntry[] entries = cProject.getResolvedPathEntries();
				for (int index = 0; index < entries.length; ++index) {
					int kind = entries[index].getEntryKind();
					if (kind == IPathEntry.CDT_INCLUDE) {
						IIncludeEntry include = (IIncludeEntry) entries[index];
						if (include.isSystemInclude()) {
							IPath entryPath = include.getFullIncludePath();
							paths.add(entryPath.toString());
						}						
					}
				}
			} catch (CModelException e) {
				// Just return an empty array 
				paths.clear();
				return paths;
			}
		}
		return paths;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
	 */
	public String[] getIncludePaths() {
		// Return the include paths for the default configuration
		ArrayList paths = getIncludePathEntries();
		return (String[])paths.toArray(new String[paths.size()]); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getLibsForConfiguration(java.lang.String)
	 */
	public String[] getLibsForConfiguration(String extension) {
		Vector libs = new Vector();
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(extension)) {
				IOption[] opts = tool.getOptions();
				// Look for the lib option type
				for (int i = 0; i < opts.length; i++) {
					IOption option = opts[i];
					try {
						if (option.getValueType() == IOption.LIBRARIES) {
							
							// check to see if the option has an applicability calculator
							IOptionApplicability applicabilitytCalculator = option.getApplicabilityCalculator();
							
							if (applicabilitytCalculator == null
									|| applicabilitytCalculator.isOptionUsedInCommandLine(getDefaultConfiguration(), tool, option)) {
								String command = option.getCommand();
								String[] allLibs = option.getLibraries();
								for (int j = 0; j < allLibs.length; j++)
								{
									String string = allLibs[j];
									libs.add(command + string);
								}
							}
						}
					} catch (BuildException e) {
						// TODO: report error
						continue;
					}
				}
			}
		}
		return (String[])libs.toArray(new String[libs.size()]);
	}

	private HashMap getMacroPathEntries() {
		HashMap macros = new HashMap();
		if (cProject != null) {
			try {
				IPathEntry[] entries = cProject.getResolvedPathEntries();
				for (int index = 0; index < entries.length; ++index) {
					if (entries[index].getEntryKind() == IPathEntry.CDT_MACRO) {
						IMacroEntry macro = (IMacroEntry) entries[index];
						macros.put(macro.getMacroName(), macro.getMacroValue());
					}
				}
			} catch (CModelException e) {
				// return an empty map
				macros.clear();
				return macros;
			}
		
		}
		return macros;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getMakeArguments()
	 */
	public String getBuildArguments() {
		if (getDefaultConfiguration() != null) {
			IToolChain toolChain = getDefaultConfiguration().getToolChain();
			IBuilder builder = toolChain.getBuilder();
			return builder.getArguments();
		}
		return EMPTY_STRING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getMakeCommand()
	 */
	public String getBuildCommand() {
		if (getDefaultConfiguration() != null) {
			IToolChain toolChain = getDefaultConfiguration().getToolChain();
			IBuilder builder = toolChain.getBuilder();
			return builder.getCommand();
		}
		return EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getPrebuildStep()
	 */
	public String getPrebuildStep() {
		// Get the default configuration and use its value
		String name = new String();
		IConfiguration config = getDefaultConfiguration();
		if (config != null) {
			name = config.getPrebuildStep();
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getPostbuildStep()
	 */
	public String getPostbuildStep() {
		// Get the default configuration and use its value
		String name = new String();
		IConfiguration config = getDefaultConfiguration();
		if (config != null) {
			name = config.getPostbuildStep();
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getPreannouncebuildStep()
	 */
	public String getPreannouncebuildStep() {
		// Get the default configuration and use its value
		String name = new String();
		IConfiguration config = getDefaultConfiguration();
		if (config != null) {
			name = config.getPreannouncebuildStep();
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getPostannouncebuildStep()
	 */
	public String getPostannouncebuildStep() {
		// Get the default configuration and use its value
		String name = new String();
		IConfiguration config = getDefaultConfiguration();
		if (config != null) {
			name = config.getPostannouncebuildStep();
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getOutputExtension(java.lang.String)
	 */
	public String getOutputExtension(String resourceExtension) {
		String outputExtension = null;
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			outputExtension = tool.getOutputExtension(resourceExtension);
			if (outputExtension != null) {
				return outputExtension;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getOutputFlag()
	 */
	public String getOutputFlag(String outputExt) {
		// Treat null extension as an empty string
		String ext = outputExt == null ? new String() : outputExt;
		
		// Get all the tools for the current config
		String flags = new String();
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			// It's OK
			if (tool.producesFileType(ext)) {
				flags = tool.getOutputFlag();
			}
		}
		return flags;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getOutputPrefix(java.lang.String)
	 */
	public String getOutputPrefix(String outputExtension) {
		// Treat null extensions as empty string
		String ext = outputExtension == null ? new String() : outputExtension;
		
		// Get all the tools for the current config
		String flags = new String();
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(ext)) {
				flags = tool.getOutputPrefix();
			}
		}
		return flags;
	}

	/**
	 * @return
	 */
	public IResource getOwner() {
		return owner;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getToolForSource(java.lang.String)
	 */
	public String getToolForSource(String sourceExtension) {
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.buildsFileType(sourceExtension)) {
				return tool.getToolCommand();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getToolForConfiguration(java.lang.String)
	 */
	public String getToolForConfiguration(String extension) {
		// Treat a null argument as an empty string
		String ext = extension == null ? new String() : extension;
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(ext)) {
				return tool.getToolCommand();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getToolFromInputExtension(java.lang.String)
	 */
	public ITool getToolFromInputExtension(String sourceExtension) {
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.buildsFileType(sourceExtension)) {
				return tool;
			}
		}
		return null;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getToolFromOutputExtension(java.lang.String)
	 */
	public ITool getToolFromOutputExtension(String extension) {
		// Treat a null argument as an empty string
		String ext = extension == null ? new String() : extension;
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(ext)) {
				return tool;
			}
		}
		return null;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#generateCommandLineInfo(java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	public IManagedCommandLineInfo generateCommandLineInfo(
			String sourceExtension, String[] flags, String outputFlag,
			String outputPrefix, String outputName, String[] inputResources) {
		return generateToolCommandLineInfo( sourceExtension, flags, 
				outputFlag, outputPrefix, outputName, inputResources, null, null );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#generateToolCommandLineInfo(java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String[], org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath)
	 */
	public IManagedCommandLineInfo generateToolCommandLineInfo( String sourceExtension, String[] flags, 
			String outputFlag, String outputPrefix, String outputName, String[] inputResources, IPath inputLocation, IPath outputLocation ){
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.buildsFileType(sourceExtension)) {
				String cmd = tool.getToolCommand();
				//try to resolve the build macros in the tool command
				try{
					String resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(cmd,
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_FILE,
							new FileContextData(inputLocation,outputLocation,null,getDefaultConfiguration().getToolChain()));
					if((resolvedCommand = resolvedCommand.trim()).length() > 0)
						cmd = resolvedCommand;
						
				} catch (BuildMacroException e){
				}

				IManagedCommandLineGenerator gen = tool.getCommandLineGenerator();
				return gen.generateCommandLineInfo( tool, cmd, 
						flags, outputFlag, outputPrefix, outputName, inputResources, 
						tool.getCommandLinePattern() );
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#getUserObjectsForConfiguration(java.lang.String)
	 */
	public String[] getUserObjectsForConfiguration(String extension) {
		Vector objs = new Vector();
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			// The tool is OK for this project nature
			if (tool.producesFileType(extension)) {
				IOption[] opts = tool.getOptions();
				// Look for the user object option type
				for (int i = 0; i < opts.length; i++) {
					IOption option = opts[i];
					try {
						if (option.getValueType() == IOption.OBJECTS) {
							objs.addAll(Arrays.asList(option.getUserObjects()));
						}
					} catch (BuildException e) {
						// TODO: report error
						continue;
					}
				}
			}
		}
		return (String[])objs.toArray(new String[objs.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#getVersion()
	 */
	public String getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * 
	 */
	public void initializePathEntries() {
		try {
			IPathEntryContainer container = new ManagedBuildCPathEntryContainer(getOwner().getProject());
			CoreModel.setPathEntryContainer(new ICProject[]{cProject}, container, new NullProgressMonitor());
		} catch (CModelException e) {
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#isDirty()
	 */
	public boolean isDirty() {
		// If the info has been flagged dirty, answer true
		if (isDirty) {
			return true;
		}
		
		// Check if the project is dirty
		if (managedProject != null) {
			return managedProject.isDirty();
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#isValid()
	 */
	public boolean isValid() {
		// If the info has been flagged as valid, answer true
		return isValid;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#isReadOnly()
	 */
	public boolean isReadOnly(){
		return isReadOnly;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#isHeaderFile(java.lang.String)
	 */
	public boolean isHeaderFile(String ext) {
		IProject project = (IProject)owner;

		// Check to see if there is a rule to build a file with this extension
		IConfiguration config = getDefaultConfiguration();
		ITool[] tools = config.getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							return tool.isHeaderFile(ext);
						}
						break;
					case ITool.FILTER_CC:
						if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							return tool.isHeaderFile(ext);
						}
						break;
					case ITool.FILTER_BOTH:
						return tool.isHeaderFile(ext);
				}
			} catch (CoreException e) {
				continue;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isContainerInited() {
		return bIsContainerInited;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#needsRebuild()
	 */
	public boolean needsRebuild() {
		if (rebuildNeeded) return true;

		if (getDefaultConfiguration() != null) {			
			return getDefaultConfiguration().needsRebuild();
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * 
	 */
	private void persistDefaultConfiguration() {
		// Persist the default configuration
		IProject project = owner.getProject();
		try {
			if(defaultConfigId != null)
				project.setPersistentProperty(defaultConfigProperty, defaultConfigId.toString().trim());
		} catch (CoreException e) {
			// Too bad
		}
	}
	
	/**
	 * Write the contents of the build model to the persistent store 
	 * specified in the argument.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		// Write out the managed build project

		if(managedProject != null){
			Element projElement = doc.createElement(IManagedProject.MANAGED_PROJECT_ELEMENT_NAME);
			element.appendChild(projElement);
			managedProject.serialize(doc, projElement);
		}
		else{
			Iterator iter = getTargets().listIterator();
			while (iter.hasNext()) {
				// Get the target
				Target targ = (Target)iter.next();
				// Create an XML element to hold the target settings
				Element targetElement = doc.createElement(ITarget.TARGET_ELEMENT_NAME);
				element.appendChild(targetElement);
				targ.serialize(doc, targetElement);
			}
//			persistDefaultTarget();
		}
			
		
		// Remember the default configuration
		persistDefaultConfiguration();

		// I'm clean now
		setDirty(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#setDefaultConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public void setDefaultConfiguration(IConfiguration configuration) {
		// TODO:  This is probably wrong.  I'll bet we don't handle the case where all configs are deleted...
		//        But, at least, our UI does not allow the last config to be deleted.		
		// Sanity
		if (configuration == null) return;

		if (!configuration.equals(getDefaultConfiguration())) {
			// Save it
			defaultConfig = configuration;
			defaultConfigId = configuration.getId();
			// TODO: is this appropriate?
			persistDefaultConfiguration();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#setDefaultConfiguration(java.lang.String)
	 */
	public boolean setDefaultConfiguration(String configName) {
		if (configName != null) {
			// Look for the configuration with the same name as the argument
			IConfiguration[] configs = managedProject.getConfigurations();
			for (int index = configs.length - 1; index >= 0; --index) {
				IConfiguration config = configs[index];
				if (configName.equalsIgnoreCase(config.getName())) {
					setDefaultConfiguration(config);
					return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		// Reset the dirty status here
		this.isDirty = isDirty;
		// and in the managed project
		if (managedProject != null) {
			managedProject.setDirty(isDirty);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#setValid(boolean)
	 */
	public void setValid(boolean isValid) {
		// Reset the valid status
		this.isValid = isValid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly){
		if(!readOnly && isReadOnly)
			setDirty(true);
		isReadOnly = readOnly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#setRebuildState(boolean)
	 */
	public void setRebuildState(boolean rebuild) {
		// Reset the status here
		rebuildNeeded = rebuild;
		// TODO:  Is the appropriate?  Should the rebuild state be stored in the project file?
		// and in the managed project
		if (getDefaultConfiguration() != null) {
			getDefaultConfiguration().setRebuildState(rebuild);
		}
	}

	/**
	 * @param version
	 */
	public void setVersion(String version) {
		if (version != null && !version.equals(this.version)) {
			this.version = version;
			//setDirty(true);  - It is primarily up to the ManagedProject to maintain the dirty state
		}
	}

	/**
	 * @param boolean
	 */
	public void setContainerInited(boolean bInited) {
		 bIsContainerInited = bInited;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// Just print out the name of the project
		return "Managed build information for " + owner.getName();	//$NON-NLS-1$
	}
	
	/**
	 * Sets the owner of the receiver to be the <code>IResource</code> specified
	 * in the argument.
	 * 
	 * @param resource
	 */
	public void updateOwner(IResource resource) {
		// Check to see if the owner is the same as the argument
		if (resource != null) {
			if (!owner.equals(resource)) {
				owner = resource;
				// Do the same for the managed project
				managedProject.updateOwner(resource);
				// And finally update the cModelElement
				cProject = CoreModel.getDefault().create(owner.getProject());

				// Save everything
				setDirty(true);
				setRebuildState(true);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getSelectedConfiguration()
	 */
	public IConfiguration getSelectedConfiguration() {
		return selectedConfig;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#setSelectedConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public void setSelectedConfiguration(IConfiguration config) {
		selectedConfig = config;
	}

	/*
	 * Note:  "Target" routines are only currently applicable when loading a CDT 2.0
	 *        or earlier managed build project file (.cdtbuild)
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#addTarget(org.eclipse.cdt.core.build.managed.ITarget)
	 */
	public void addTarget(ITarget target) {
		getTargetMap().put(target.getId(), target);
		getTargets().add(target);
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#removeTarget(java.lang.String)
	 */
	public void removeTarget(String id) {
		getTargets().remove(getTarget(id));
		getTargetMap().remove(id);
		setDirty(true);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getTarget(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public ITarget getTarget(String id) {
		return (ITarget) getTargetMap().get(id);
	}

	/* (non-Javadoc)
	 * Safe accessor.
	 * 
	 * @return Returns the map of IDs to ITargets.
	 */
	private Map getTargetMap() {
		if (targetMap == null) {
			targetMap = new HashMap();
		}
		return targetMap;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getTargets(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public List getTargets() {
		if (targetList == null) {
			targetList = new ArrayList();
		}
		return targetList;	
	}
	
	/**
	 * 
	 * @return
	 */
	private String getCWD() {
		String cwd = ""; //$NON-NLS-1$
		IBuildEnvironmentVariable cwdvar = ManagedBuildManager.getEnvironmentVariableProvider().getVariable("CWD", getDefaultConfiguration(), false, true); //$NON-NLS-1$
		if (cwdvar != null) { cwd = cwdvar.getValue().replace('\\','/'); }     //$NON-NLS-1$  //$NON-NLS-2$
		return cwd;
	}
	
	/**
	 */
	private String processPath(String path, int context, Object obj) {
		final String EMPTY = "";   //$NON-NLS-1$
		final String QUOTE = "\""; //$NON-NLS-1$
		
		if (path == null) { return EMPTY; }
		String s = path;
		if (context != 0) {
			try {
				s = ManagedBuildManager.getBuildMacroProvider().resolveValue(s, EMPTY, " ", context, obj); //$NON-NLS-1$
			} catch (BuildMacroException e) { return EMPTY; }
		}
		if (s == null) { s = path; }

		if (s.length()> 1 && s.startsWith(QUOTE) && s.endsWith(QUOTE)) {
			s = s.substring(1, s.length()-1);
		}
		
		if ( ".".equals(s) ) { //$NON-NLS-1$
			String cwd = getCWD();
			if (cwd.length()>0) { s = cwd; }
		}
		if (!(new Path(s)).isAbsolute()) {
			String cwd = getCWD();
			if (cwd.length()>0) { s = cwd + "/" + s; } //$NON-NLS-1$
		}
		return s;
	}

	/**
	 * Obtain all possible Managed build values
	 * @return
	 */
	public IPathEntry[] getManagedBuildValues() {
		List entries = new ArrayList();
		int i=0;
		IPathEntry[] a = getManagedBuildValues(IPathEntry.CDT_INCLUDE_FILE);
		if (a != null) { for (i=0; i<a.length; i++) entries.add(a[i]); } 
		a = getManagedBuildValues(IPathEntry.CDT_LIBRARY);
		if (a != null) { for (i=0; i<a.length; i++) entries.add(a[i]); } 
		a = getManagedBuildValues(IPathEntry.CDT_MACRO);
		if (a != null) { for (i=0; i<a.length; i++) entries.add(a[i]); } 
		return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
	}

	/**
	 * Obtain all possible Managed build built-ins
	 * @return
	 */
	public IPathEntry[] getManagedBuildBuiltIns() {
		List entries = new ArrayList();
		int i=0;
		IPathEntry[] a = getManagedBuildBuiltIns(IPathEntry.CDT_INCLUDE_FILE);
		if (a != null) { for (i=0; i<a.length; i++) entries.add(a[i]); } 
		a = getManagedBuildBuiltIns(IPathEntry.CDT_LIBRARY);
		if (a != null) { for (i=0; i<a.length; i++) entries.add(a[i]); } 
		a = getManagedBuildBuiltIns(IPathEntry.CDT_MACRO);
		if (a != null) { for (i=0; i<a.length; i++) entries.add(a[i]); } 
		return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
	}
	
	/**
	 * 
	 * @param entryType
	 * @return
	 */
	public IPathEntry[] getManagedBuildValues(int entryType) {
		// obtain option values
		List entries = getOptionValues(entryType, false);
	
		// for includes, get env variables values; useless for other entry types  
		if (entryType == IPathEntry.CDT_INCLUDE_FILE) {
			IEnvironmentVariableProvider env = ManagedBuildManager.getEnvironmentVariableProvider();
			entries = addIncludes(entries, env.getBuildPaths(getDefaultConfiguration(), IEnvVarBuildPath.BUILDPATH_INCLUDE), Path.EMPTY, null);
		}	
		return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
	}
	
	/**
	 * @param entryType
	 * @return
	 */
	public IPathEntry[] getManagedBuildBuiltIns(int entryType) {
		List entries = getOptionValues(entryType, true);
		return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);		
	}
	
	/**
	 * 
	 * @param entryType  - data type to be scanned for 
	 * @param builtIns   - return either values or built-in's 
	 * @return list of strings which contains all found values 
	 */
	private List getOptionValues(int entryType, boolean builtIns) {
		List entries = new ArrayList(); 
		IConfiguration cfg = getDefaultConfiguration();
		
		// process config toolchain's options
		entries = readToolsOptions(
				entryType, 
				entries, 
				builtIns, 
				cfg);
		
		
		// code below (obtaining of resource config values)
		// is now commented because resource-related include
		// paths are displayed by UI together with config-
		// related includes, so paths are duplicated in
		// project's "includes" folder.
		// 
		// Uncomment following code after UI problem fix.     
/* 
		// process resource configurations
        IResourceConfiguration[] rescfgs = cfg.getResourceConfigurations();
		if (rescfgs != null) {
			for (int i=0; i<rescfgs.length; i++) {
				entries = readToolsOptions(
							entryType, 
							entries, 
							builtIns, 
							rescfgs[i]);
			}
		}
//		*/
		return entries;
	}

	/**
	 * 
	 * @param optionType - data type: include | library | symbols 
	 * @param entries    - list to be affected
	 * @param builtIns   - whether get actual values or builtins 
	 * @param obj        - object to be processed (ResCfg | Cfg) 
	 */
	private List readToolsOptions(int entryType, List entries, boolean builtIns, IBuildObject obj) {
		ITool[] t = null;
		Path resPath = Path.EMPTY;

		// check that entryType is correct
		if (entryType != IPathEntry.CDT_INCLUDE_FILE &&
			entryType != IPathEntry.CDT_LIBRARY &&
			entryType != IPathEntry.CDT_MACRO) { return entries; }
		
		// calculate parameters depending of object type
		if (obj instanceof IResourceConfiguration) {
			resPath = new Path(((IResourceConfiguration)obj).getResourcePath());
			t = ((IResourceConfiguration)obj).getTools();
		} else if (obj instanceof IConfiguration) {
			t  = ((IConfiguration)obj).getFilteredTools();
		} else { return entries; } // wrong object passed 
		if (t == null) { return entries; }
		
		// process all tools and all their options
		for (int i=0; i<t.length; i++) {
			IOption[] op = t[i].getOptions();
			for (int j=0; j<op.length; j++) {
				
				// check to see if the option has an applicability calculator
				IOptionApplicability applicabilityCalculator = op[j].getApplicabilityCalculator();
				if (applicabilityCalculator != null &&
				   !applicabilityCalculator.isOptionUsedInCommandLine(obj, t[i], op[j])) continue;
				
				try {
					if (entryType == IPathEntry.CDT_INCLUDE_FILE && 
							op[j].getValueType() == IOption.INCLUDE_PATH) 
					{
						OptionContextData ocd = new OptionContextData(op[j], obj);				
						addIncludes(entries, builtIns ? op[j].getBuiltIns() : op[j].getIncludePaths(), resPath, ocd);
					} else if (entryType == IPathEntry.CDT_LIBRARY && 
							op[j].getValueType() == IOption.LIBRARIES) 
					{
						OptionContextData ocd = new OptionContextData(op[j], obj);				
						addLibraries(entries, builtIns ? op[j].getBuiltIns() : op[j].getLibraries(), resPath, ocd);
					} else if (entryType == IPathEntry.CDT_MACRO && 
							op[j].getValueType() == IOption.PREPROCESSOR_SYMBOLS) 
					{
						addSymbols(entries, builtIns ? op[j].getBuiltIns() : op[j].getDefinedSymbols(), resPath);
					} else { continue; }
				} catch (BuildException e) {}
			}
		}
		return entries;
	}
	
	/**
	 * 
	 * @param entries
	 * @param values
	 * @param resPath
	 * @param ocd       
	 */
	protected List addIncludes(List entries, String[] values, Path resPath, OptionContextData ocd) {
		if (values != null) {
			for (int k=0; k<values.length; k++) {
				if (ocd != null) {
				   values[k] = processPath(values[k], IBuildMacroProvider.CONTEXT_OPTION, ocd);
				}
				IPathEntry entry = CoreModel.newIncludeEntry(resPath, Path.EMPTY, new Path(processPath(values[k], 0, null)), true);
				if (!entries.contains(entry)) {	entries.add(entry);	}
			}
		}
		return entries;
	}
	
	/**
	 * 
	 * @param entries
	 * @param values
	 * @param resPath
	 * @param ocd
	 */
	protected List addLibraries(List entries, String[] values, Path resPath, OptionContextData ocd) {
		if (values != null) {
			for (int k=0; k<values.length; k++) {
				if (ocd != null) {
					values[k] = processPath(values[k], IBuildMacroProvider.CONTEXT_OPTION, ocd);
				}
				IPathEntry entry = CoreModel.newLibraryEntry(resPath, Path.EMPTY, new Path(processPath(values[k], 0, null)), null, null, null, true);
				if (!entries.contains(entry)) {	entries.add(entry); }
			}
		}
		return entries;
	}
	
	/**
	 * 
	 * @param entries
	 * @param values
	 * @param resPath
	 */
	protected List addSymbols(List entries, String[] values, Path resPath) {
		if (values == null) return entries;
		for (int i=0; i<values.length; i++) {
			if (values[i].length() == 0) continue;
			String[] tokens = values[i].split("="); //$NON-NLS-1$
			String key = tokens[0].trim();
			String value = (tokens.length > 1) ? tokens[1].trim() : new String();
			// Make sure the current entries do not contain a duplicate
			boolean add = true;
			Iterator entryIter = entries.listIterator();
			while (entryIter.hasNext()) {
				IPathEntry entry = (IPathEntry) entryIter.next();
				if (entry.getEntryKind() == IPathEntry.CDT_MACRO) {	
					if (((IMacroEntry)entry).getMacroName().equals(key) && 
						((IMacroEntry)entry).getMacroValue().equals(value)) {
						add = false;
						break;
					}
				}
			}
			if (add) { entries.add(CoreModel.newMacroEntry(resPath, key, value)); }
		}
		return entries;
	}

}
