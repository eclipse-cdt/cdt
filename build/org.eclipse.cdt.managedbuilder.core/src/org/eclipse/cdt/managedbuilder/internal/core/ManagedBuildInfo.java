/**********************************************************************
 * Copyright (c) 2002,2004 IBM Software Corporation and others.
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
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.ManagedBuildCPathEntryContainer;
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
	 */
	public ManagedBuildInfo(IResource owner, Element element) {
		this(owner);
		
		// Recreate the managed build project element and its children
		NodeList projNodes = element.getElementsByTagName(IManagedProject.MANAGED_PROJECT_ELEMENT_NAME);
		// TODO:  There should only be 1?
		for (int projIndex = projNodes.getLength() - 1; projIndex >= 0; --projIndex) {
			ManagedProject proj = new ManagedProject(this, (Element)projNodes.item(projIndex));
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
			if (defaultConfigId != null && managedProject != null) {
				defaultConfig = managedProject.getConfiguration(defaultConfigId);
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
		IConfiguration config = getDefaultConfiguration();
		if(config == null)
			return symbols;
		ITool[] tools = config.getFilteredTools();
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			// Now extract the valid tool's options
			IOption[] opts = tool.getOptions();
			for (int j = 0; j < opts.length; j++) {
				IOption option = opts[j];
				try {
					if (option.getValueType() == IOption.PREPROCESSOR_SYMBOLS) {
						ArrayList symbolList = new ArrayList();
						symbolList.addAll(Arrays.asList(option.getDefinedSymbols()));
						Iterator iter = symbolList.listIterator();
						while (iter.hasNext()) {
							String symbol = (String) iter.next();
							if (symbol.length() == 0){
								continue;
							}
							String[] tokens = symbol.split("="); //$NON-NLS-1$
							String key = tokens[0].trim();
							String value = (tokens.length > 1) ? tokens[1].trim() : new String();
							symbols.put(key, value);
						}
					}
				} catch (BuildException e) {
					// TODO: report error
					continue;
				}
			}
		}
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
						return tools[index].getDependencyGenerator();
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
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool != null && tool.buildsFileType(extension)) {
				try {
					return tool.getToolFlags();
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
		// Treat null extensions as an empty string
		String ext = extension == null ? new String() : extension;
		
		// Get all the tools for the current config
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(ext)) {
				try {
					return tool.getToolFlags();
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
		IConfiguration config = getDefaultConfiguration();
		IPath location = owner.getLocation();
		// If the build info is out of date this might be null
		if (location == null) {
			location = new Path("."); //$NON-NLS-1$
		}
		if (config != null) {
			IPath root = location.addTrailingSeparator().append(config.getName());
			ITool[] tools = config.getFilteredTools();
			for (int i = 0; i < tools.length; i++) {
				ITool tool = tools[i];
				// The tool checks out for this project, get its options
				IOption[] opts = tool.getOptions();
				for (int j = 0; j < opts.length; j++) {
					IOption option = opts[j];
					try {
						if (option.getValueType() == IOption.INCLUDE_PATH) {
							// Get all the user-defined paths from the option as absolute paths
							String[] userPaths = option.getIncludePaths();
							for (int index = 0; index < userPaths.length; ++index) {
								IPath userPath = new Path(userPaths[index]);
								if (userPath.isAbsolute()) {
									paths.add(userPath.toOSString());
								} else {
									IPath absPath = root.addTrailingSeparator().append(userPath);
									paths.add(absPath.makeAbsolute().toOSString());
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
		
		// Answer the results as an array
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
							String command = option.getCommand();
							String[] allLibs = option.getLibraries();
							for (int j = 0; j < allLibs.length; j++) {
								String string = allLibs[j];
								libs.add(command + string);
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

	/* (non-Javadoc)
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

	/* (non-Javadoc)
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
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getToolInvocation(java.lang.String)
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
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#generateCommandLineInfo(java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	public IManagedCommandLineInfo generateCommandLineInfo(
			String sourceExtension, String[] flags, String outputFlag,
			String outputPrefix, String outputName, String[] inputResources) {
		ITool[] tools = getFilteredTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.buildsFileType(sourceExtension)) {
				IManagedCommandLineGenerator gen = tool.getCommandLineGenerator();
				return gen.generateCommandLineInfo( tool, tool.getToolCommand(), 
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
	private void initializePathEntries() {
		try {
			IPathEntryContainer container = new ManagedBuildCPathEntryContainer(getOwner().getProject());
			CoreModel.getDefault().setPathEntryContainer(new ICProject[]{cProject}, container, new NullProgressMonitor());
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
}
