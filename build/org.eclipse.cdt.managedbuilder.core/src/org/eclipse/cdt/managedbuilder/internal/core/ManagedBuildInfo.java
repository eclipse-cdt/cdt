package org.eclipse.cdt.managedbuilder.internal.core;

/**********************************************************************
 * Copyright (c) 2002,2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ManagedBuildInfo implements IManagedBuildInfo, IScannerInfo {
	
	// Local variables
	public static final String MAJOR_SEPERATOR = ";"; //$NON-NLS-1$
	public static final String MINOR_SEPERATOR = "::"; //$NON-NLS-1$
	private static final QualifiedName defaultConfigProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "defaultConfig");	//$NON-NLS-1$
	private static final QualifiedName defaultTargetProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "defaultTarget");	//$NON-NLS-1$
	private String defaultConfigIds;
	private Map defaultConfigMap;
	private ITarget defaultTarget;
	private String defaultTargetId;
	private ITarget selectedTarget;
	private boolean isDirty;
	private IResource owner;
	private Map targetMap;
	private List targetList;
	private String version;	//$NON-NLS-1$
	
	/**
	 * Create a new managed build information for the IResource specified in the argument
	 * 
	 * @param owner
	 */
	public ManagedBuildInfo(IResource owner) {
		this.owner = owner;
		isDirty = false;

		// The id of the default target from the project persistent settings store
		IProject project = (IProject)owner;
		defaultTargetId = null;
		try {
			defaultTargetId = project.getPersistentProperty(defaultTargetProperty);
		} catch (CoreException e) {
			// We have all the build elements so we can stop if this occurs
			return;
		}

		// Get the default configs for every target out of the same store
		defaultConfigIds = null;
		try {
			defaultConfigIds = project.getPersistentProperty(defaultConfigProperty);
		} catch (CoreException e) {
			// Again, hitting this error just means the default config is not set
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
		
		// Inflate the targets
		NodeList targetNodes = element.getElementsByTagName(ITarget.TARGET_ELEMENT_NAME);
		for (int targIndex = targetNodes.getLength() - 1; targIndex >= 0; --targIndex) {
			new Target(this, (Element)targetNodes.item(targIndex));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#addTarget(org.eclipse.cdt.core.build.managed.ITarget)
	 */
	public void addTarget(ITarget target) {
		getTargetMap().put(target.getId(), target);
		getTargets().add(target);
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#buildsFileType(java.lang.String)
	 */
	public boolean buildsFileType(String srcExt) {
		// Make sure the owner is treated as a project for the duration
		IProject project = (IProject)owner;

		// Check to see if there is a rule to build a file with this extension
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							return tool.buildsFileType(srcExt);
						}
						break;
					case ITool.FILTER_CC:
						if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							return tool.buildsFileType(srcExt);
						}
						break;
					case ITool.FILTER_BOTH:
						return tool.buildsFileType(srcExt);
				}
			} catch (CoreException e) {
				continue;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#getBuildArtifactExtension()
	 */
	public String getBuildArtifactExtension() {
		String ext = new String();
		ITarget target = getDefaultTarget();
		if (target != null) {
			ext = target.getArtifactExtension();
		} 
		return ext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getBuildArtifactName()
	 */
	public String getBuildArtifactName() {
		// Get the default target and use its value
		String name = new String();
		ITarget target = getDefaultTarget();
		if (target != null) {
			name = target.getArtifactName();
		}
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getCleanCommand()
	 */
	public String getCleanCommand() {
		// Get from the model
		String command = new String();
		ITarget target = getDefaultTarget();
		if (target != null) {
			command = target.getCleanCommand();
		}
		return command;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getConfigurationName()
	 */
	public String getConfigurationName() {
		// Return the human-readable name of the default configuration
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		return config == null ? new String() : config.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getConfigurationNames()
	 */
	public String[] getConfigurationNames() {
		ArrayList configNames = new ArrayList();
		IConfiguration[] configs = getDefaultTarget().getConfigurations();
		for (int i = 0; i < configs.length; i++) {
			IConfiguration configuration = configs[i];
			configNames.add(configuration.getName());
		}
		configNames.trimToSize();
		return (String[])configNames.toArray(new String[configNames.size()]);
	}

	/* (non-Javadoc)
	 * 
	 * @return Returns the map of ITarget ids to IConfigurations.
	 */
	private Map getDefaultConfigMap() {
		if (defaultConfigMap == null) {
			defaultConfigMap = new HashMap();
			// We read this as part of the constructor
			if (defaultConfigIds != null) {
				String[] majorTokens = defaultConfigIds.split(MAJOR_SEPERATOR);
				for (int index = majorTokens.length - 1; index >= 0; --index) {
					// Now split each token into the target and config id component
					String idToken = majorTokens[index];
					if (idToken != null) {
						String[] minorTokens = idToken.split(MINOR_SEPERATOR);
						// The first token is the target ID
						ITarget target = getTarget(minorTokens[0]);
						if (target == null) continue;
						// The second is the configuration ID
						IConfiguration config = target.getConfiguration(minorTokens[1]);
						if (config != null) {
							defaultConfigMap.put(target.getId(), config);
						}
					}
				}
			}
		}
		return defaultConfigMap;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getDefaultConfiguration()
	 */
	public IConfiguration getDefaultConfiguration(ITarget target) {
		// Get the default config associated with the defalt target
		IConfiguration config = (IConfiguration) getDefaultConfigMap().get(target.getId());

		// If null, look up the first configuration associated with the target
		if (config == null) {
			IConfiguration[] configs = getDefaultTarget().getConfigurations();
			if (configs.length > 0) {
				config = configs[0];
			}
		}
		return config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getDefaultTarget()
	 */
	public ITarget getDefaultTarget() {
		if (defaultTarget == null) {
			// See if there is a target that was persisted
			if (defaultTargetId != null) {
				defaultTarget = (ITarget) getTargetMap().get(defaultTargetId);
			}
			// If that failed, look for anything
			if (defaultTarget == null) {
				defaultTarget = (ITarget) getTargets().get(0);
			}
		}
		return defaultTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getDefinedSymbols()
	 */
	public Map getDefinedSymbols() {
		IProject project = (IProject)owner;
		// Return the defined symbols for the default configuration
		HashMap symbols = new HashMap();
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (!project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_CC:
						if (!project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_BOTH:
						break;
				}
			} catch (CoreException e) {
				continue;
			}
			// Now extract the valid tool's options
			IOption[] opts = tool.getOptions();
			for (int j = 0; j < opts.length; j++) {
				IOption option = opts[j];
				if (option.getValueType() == IOption.PREPROCESSOR_SYMBOLS) {
					try {
						ArrayList symbolList = new ArrayList();
						symbolList.addAll(Arrays.asList(option.getBuiltIns()));
						symbolList.addAll(Arrays.asList(option.getDefinedSymbols()));
						Iterator iter = symbolList.listIterator();
						while (iter.hasNext()) {
							String symbol = (String) iter.next();
							if (symbol.length() == 0){
								continue;
							}
							String key = new String();
							String value = new String();
							int index = symbol.indexOf("="); //$NON-NLS-1$
							if (index != -1) {
								key = symbol.substring(0, index).trim();
								value = symbol.substring(index + 1).trim();
							} else {
								key = symbol.trim();
							}
							symbols.put(key, value);
						}

					} catch (BuildException e) {
						// we should never get here
						continue;
					}
				}
			}
		}
		return symbols; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getFlagsForSource(java.lang.String)
	 */
	public String getFlagsForSource(String extension) {
		IProject project = (IProject)owner;

		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.buildsFileType(extension)) {
				try {
					// Make sure the tool is right for the project
					switch (tool.getNatureFilter()) {
						case ITool.FILTER_C:
							if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.getToolFlags();
							}
							break;
						case ITool.FILTER_CC:
							if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.getToolFlags();
							}
							break;
						case ITool.FILTER_BOTH:
							return tool.getToolFlags();
					}
				} catch (CoreException e) {
					continue;
				} catch (BuildException e) {
					// Give it your best shot with the next tool
					continue;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getToolFlags(java.lang.String)
	 */
	public String getFlagsForTarget(String extension) {
		IProject project = (IProject)owner;
		// Treat null extensions as an empty string
		String ext = extension == null ? new String() : extension;
		
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(ext)) {
				try {
					// Make sure the tool is right for the project
					switch (tool.getNatureFilter()) {
						case ITool.FILTER_C:
							if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.getToolFlags();
							}
							break;
						case ITool.FILTER_CC:
							if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.getToolFlags();
							}
							break;
						case ITool.FILTER_BOTH:
							return tool.getToolFlags();
					}
				} catch (CoreException e) {
					continue;
				} catch (BuildException e) {
					// Give it your best shot with the next tool
					continue;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IScannerInfo#getIncludePaths()
	 */
	public String[] getIncludePaths() {
		IProject project = (IProject)owner;
		
		// Return the include paths for the default configuration
		ArrayList paths = new ArrayList();
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		IPath location = owner.getLocation();
		// If the build info is out of date this might be null
		if (location == null) {
			location = new Path("."); //$NON-NLS-1$
		}
		IPath root = location.addTrailingSeparator().append(config.getName());
		ITool[] tools = config.getTools();
		for (int i = 0; i < tools.length; i++) {
			ITool tool = tools[i];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (!project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_CC:
						if (!project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_BOTH:
						break;
				}
			} catch (CoreException e) {
				continue;
			}
			// The tool checks out for this project, get its options
			IOption[] opts = tool.getOptions();
			for (int j = 0; j < opts.length; j++) {
				IOption option = opts[j];
				if (option.getValueType() == IOption.INCLUDE_PATH) {
					try {
						// Get all the built-in paths from the option
						paths.addAll(Arrays.asList(option.getBuiltIns()));
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
					} catch (BuildException e) {
						// we should never get here, but continue anyway
						continue;
					}
				}
			}
		}
		paths.trimToSize();
		return (String[])paths.toArray(new String[paths.size()]); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getLibsForTarget(java.lang.String)
	 */
	public String[] getLibsForTarget(String extension) {
		IProject project = (IProject)owner;
		
		ArrayList libs = new ArrayList();
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (!project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_CC:
						if (!project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_BOTH:
						break;
				}
			} catch (CoreException e) {
				continue;
			}
			// The tool is OK for this project nature
			if (tool.producesFileType(extension)) {
				IOption[] opts = tool.getOptions();
				// Look for the lib option type
				for (int i = 0; i < opts.length; i++) {
					IOption option = opts[i];
					if (option.getValueType() == IOption.LIBRARIES) {
						try {
							String command = option.getCommand();
							String[] allLibs = option.getLibraries();
							for (int j = 0; j < allLibs.length; j++) {
								String string = allLibs[j];
								libs.add(command + string);
							}
						} catch (BuildException e) {
							continue;
						}
					}
				}
			}
		}
		libs.trimToSize();
		return (String[])libs.toArray(new String[libs.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getMakeArguments()
	 */
	public String getMakeArguments() {
		return getDefaultTarget().getMakeArguments();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getMakeCommand()
	 */
	public String getMakeCommand() {
		return getDefaultTarget().getMakeCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getOutputExtension(java.lang.String)
	 */
	public String getOutputExtension(String resourceExtension) {
		IProject project = (IProject)owner;
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							return tool.getOutputExtension(resourceExtension);
						}
						break;
					case ITool.FILTER_CC:
						if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							return tool.getOutputExtension(resourceExtension);
						}
						break;
					case ITool.FILTER_BOTH:
						return tool.getOutputExtension(resourceExtension);
				}
			} catch (CoreException e) {
				continue;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getOutputFlag()
	 */
	public String getOutputFlag(String outputExt) {
		IProject project = (IProject)owner;
		// Treat null extension as an empty string
		String ext = outputExt == null ? new String() : outputExt;
		
		// Get all the tools for the current config
		String flags = new String();
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (!project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_CC:
						if (!project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_BOTH:
						break;
				}
			} catch (CoreException e) {
				continue;
			}
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
		IProject project = (IProject)owner;
		// Treat null extensions as empty string
		String ext = outputExtension == null ? new String() : outputExtension;
		
		// Get all the tools for the current config
		String flags = new String();
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (!project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_CC:
						if (!project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_BOTH:
						break;
				}
			} catch (CoreException e) {
				continue;
			}
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getToolForSource(java.lang.String)
	 */
	public String getToolForSource(String extension) {
		IProject project = (IProject)owner;

		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.buildsFileType(extension)) {
				try {
					// Make sure the tool is right for the project
					switch (tool.getNatureFilter()) {
						case ITool.FILTER_C:
							if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.getToolCommand();
							}
							break;
						case ITool.FILTER_CC:
							if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.getToolCommand();
							}
							break;
						case ITool.FILTER_BOTH:
							return tool.getToolCommand();
					}
				} catch (CoreException e) {
					continue;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getToolInvocation(java.lang.String)
	 */
	public String getToolForTarget(String extension) {
		IProject project = (IProject)owner;

		// Treat a null argument as an empty string
		String ext = extension == null ? new String() : extension;
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			if (tool.producesFileType(ext)) {
				try {
					// Make sure the tool is right for the project
					switch (tool.getNatureFilter()) {
						case ITool.FILTER_C:
							if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.getToolCommand();
							}
							break;
						case ITool.FILTER_CC:
							if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								return tool.getToolCommand();
							}
							break;
						case ITool.FILTER_BOTH:
							return tool.getToolCommand();
					}
				} catch (CoreException e) {
					continue;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#getUserObjectsForTarget(java.lang.String)
	 */
	public String[] getUserObjectsForTarget(String extension) {
		IProject project = (IProject)owner;
		ArrayList objs = new ArrayList();
		// Get all the tools for the current config
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
		for (int index = 0; index < tools.length; index++) {
			ITool tool = tools[index];
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						if (!project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_CC:
						if (!project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							continue;
						}
						break;
					case ITool.FILTER_BOTH:
						break;
				}
			} catch (CoreException e) {
				continue;
			}
			// The tool is OK for this project nature
			if (tool.producesFileType(extension)) {
				IOption[] opts = tool.getOptions();
				// Look for the user object option type
				for (int i = 0; i < opts.length; i++) {
					IOption option = opts[i];
					if (option.getValueType() == IOption.OBJECTS) {
						try {
							objs.addAll(Arrays.asList(option.getUserObjects()));
						} catch (BuildException e) {
							continue;
						}
					}
				}
			}
		}
		objs.trimToSize();
		return (String[])objs.toArray(new String[objs.size()]);
	}

	/**
	 * @return
	 */
	public String getVersion() {
		return version;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#isDirty()
	 */
	public boolean isDirty() {
		// If the info has been flagged dirty, answer true
		if (isDirty) {
			return true;
		}
		
		// Check if any of the defined targets are dirty
		Iterator iter = getTargets().listIterator();
		while (iter.hasNext()) {
			if (((ITarget)iter.next()).isDirty()) {
				return true;
			}
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#isHeaderFile(java.lang.String)
	 */
	public boolean isHeaderFile(String ext) {
		IProject project = (IProject)owner;

		// Check to see if there is a rule to build a file with this extension
		IConfiguration config = getDefaultConfiguration(getDefaultTarget());
		ITool[] tools = config.getTools();
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
	 * 
	 */
	private void persistDefaultConfigurations() {
		// Create a buffer of the default configuration IDs
		StringBuffer defaultConfigs = new StringBuffer();
		Iterator iter = getTargets().listIterator();
		while (iter.hasNext()) {
			// Persist the default target configuration pair as <targ_ID>::<conf_ID>
			ITarget targ = (ITarget)iter.next();
			IConfiguration config = getDefaultConfiguration((ITarget)targ);
			if (config != null) {
				defaultConfigs.append(targ.getId());
				defaultConfigs.append(MINOR_SEPERATOR);
				defaultConfigs.append(config.getId());
				defaultConfigs.append(MAJOR_SEPERATOR);
			}
		}
		// Persist the default configurations
		IProject project = (IProject) getOwner();
		try {
			project.setPersistentProperty(defaultConfigProperty, defaultConfigs.toString().trim());
		} catch (CoreException e) {
			// Too bad
		}
	}

	/* (non-Javadoc)
	 * 
	 */
	private void persistDefaultTarget() {
		// Persist the default target as a project setting
		IProject project = (IProject) getOwner();
		ITarget defTarget = getDefaultTarget();
		if (defTarget != null){
			try {
				project.setPersistentProperty(defaultTargetProperty, defTarget.getId());
			} catch (CoreException e) {
				// Tough
			}
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
		// Write out each target
		Iterator iter = getTargets().listIterator();
		while (iter.hasNext()) {
			// Get the target
			Target targ = (Target)iter.next();
			// Create an XML element to hold the target settings
			Element targetElement = doc.createElement(ITarget.TARGET_ELEMENT_NAME);
			element.appendChild(targetElement);
			targ.serialize(doc, targetElement);
		}
		// Remember the default target and configurations
		persistDefaultTarget();
		persistDefaultConfigurations();

		// I'm clean now
		setDirty(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#setDefaultConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public void setDefaultConfiguration(IConfiguration configuration) {
		// Sanity
		if (configuration== null) return;
		
		// Get the target associated with the argument
		ITarget target = configuration.getTarget();
		
		// See if there is anything to be done
		IConfiguration oldDefault = getDefaultConfiguration(target);
		if (!configuration.equals(oldDefault)) {
			// Make sure it is the default
			setDefaultTarget(target);
			// Make the argument the 
			getDefaultConfigMap().put(target.getId(), configuration);
			// Save it
			persistDefaultConfigurations();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#setDefaultTarget(org.eclipse.cdt.core.build.managed.ITarget)
	 */
	public void setDefaultTarget(ITarget target) {
		// Sanity
		if (target == null) return;
		
		// Make sure there is something to change 
		if (!target.equals(defaultTarget)) {
			defaultTarget = target;
			defaultTargetId = target.getId();
			persistDefaultTarget();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#setSelectedTarget(org.eclipse.cdt.core.build.managed.ITarget)
	 */
	public void setSelectedTarget(ITarget target) {
		selectedTarget = target;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IManagedBuildInfo#getSelectedTarget()
	 */
	public ITarget getSelectedTarget() {
		return selectedTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		// Reset the dirty status here
		this.isDirty = isDirty;
		// and in the contained targets
		Iterator iter = getTargets().listIterator();
		while (iter.hasNext()) {
			ITarget target = (ITarget)iter.next();
			target.setDirty(isDirty);
		}
	}

	/**
	 * @param version
	 */
	public void setVersion(String version) {
		if (version != null && !version.equals(this.version)) {
			this.version = version;
			setDirty(true);
		}
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
				// Do the same for the targets
				Iterator iter = getTargets().listIterator();
				while(iter.hasNext()) {
					ITarget target = (ITarget) iter.next();
					target.updateOwner(resource);
				}
			}
		}
	}
}
