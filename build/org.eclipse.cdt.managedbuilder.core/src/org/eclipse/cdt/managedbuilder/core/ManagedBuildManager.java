/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentBuildPathsChangeListener;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.DefaultManagedConfigElement;
import org.eclipse.cdt.managedbuilder.internal.core.InputType;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.Option;
import org.eclipse.cdt.managedbuilder.internal.core.OptionCategory;
import org.eclipse.cdt.managedbuilder.internal.core.OutputType;
import org.eclipse.cdt.managedbuilder.internal.core.ProjectType;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Target;
import org.eclipse.cdt.managedbuilder.internal.core.TargetPlatform;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator;
import org.eclipse.cdt.managedbuilder.projectconverter.UpdateManagedProjectManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * This is the main entry point for getting at the build information
 * for the managed build system. 
 */
public class ManagedBuildManager extends AbstractCExtension implements IScannerInfoProvider {

	private static final QualifiedName buildInfoProperty = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "managedBuildInfo");	//$NON-NLS-1$
	private static final String ROOT_NODE_NAME = "ManagedProjectBuildInfo";	//$NON-NLS-1$
	public  static final String SETTINGS_FILE_NAME = ".cdtbuild";	//$NON-NLS-1$
	private static final ITarget[] emptyTargets = new ITarget[0];
	public  static final String INTERFACE_IDENTITY = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".ManagedBuildManager";	//$NON-NLS-1$
	public  static final String EXTENSION_POINT_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".buildDefinitions";		//$NON-NLS-1$
	public  static final String EXTENSION_POINT_ID_V2 = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".ManagedBuildInfo";	//$NON-NLS-1$
	private static final String REVISION_ELEMENT_NAME = "managedBuildRevision";	//$NON-NLS-1$
	private static final String VERSION_ELEMENT_NAME = "fileVersion";	//$NON-NLS-1$
	private static final String MANIFEST_VERSION_ERROR ="ManagedBuildManager.error.manifest.version.error";	//$NON-NLS-1$
	private static final String PROJECT_VERSION_ERROR ="ManagedBuildManager.error.project.version.error";	//$NON-NLS-1$
	private static final String MANIFEST_ERROR_HEADER = "ManagedBuildManager.error.manifest.header";	//$NON-NLS-1$
	public  static final String MANIFEST_ERROR_RESOLVING = "ManagedBuildManager.error.manifest.resolving";	//$NON-NLS-1$
	public  static final String MANIFEST_ERROR_DUPLICATE = "ManagedBuildManager.error.manifest.duplicate";	//$NON-NLS-1$
	public  static final String MANIFEST_ERROR_ICON = "ManagedBuildManager.error.manifest.icon";	//$NON-NLS-1$
	private static final String MANIFEST_ERROR_OPTION_CATEGORY = "ManagedBuildManager.error.manifest.option.category";	//$NON-NLS-1$
	private static final String MANIFEST_ERROR_OPTION_FILTER = "ManagedBuildManager.error.manifest.option.filter";	//$NON-NLS-1$
	private static final String MANIFEST_ERROR_OPTION_VALUEHANDLER = "ManagedBuildManager.error.manifest.option.valuehandler";	//$NON-NLS-1$
	// Error ID's for OptionValidError()
	public static final int ERROR_CATEGORY = 0;
	public static final int ERROR_FILTER = 1;
	
	private static final String NEWLINE = System.getProperty("line.separator");	//$NON-NLS-1$
	
	// This is the version of the manifest and project files
	private static final PluginVersionIdentifier buildInfoVersion = new PluginVersionIdentifier(3, 0, 0);
	private static Map depCalculatorsMap;
	private static boolean projectTypesLoaded = false;
	// Project types defined in the manifest files
	private static Map projectTypeMap;
	private static List projectTypes;
	// Configurations defined in the manifest files
	private static Map extensionConfigurationMap;
	// Resource configurations defined in the manifest files
	private static Map extensionResourceConfigurationMap;
	// Tool-chains defined in the manifest files
	private static SortedMap extensionToolChainMap;
	// Tools defined in the manifest files
	private static SortedMap extensionToolMap;
	// Target Platforms defined in the manifest files
	private static Map extensionTargetPlatformMap;
	// Builders defined in the manifest files
	private static SortedMap extensionBuilderMap;
	// Options defined in the manifest files
	private static Map extensionOptionMap;
	// Option Categories defined in the manifest files
	private static Map extensionOptionCategoryMap;
	// Input types defined in the manifest files
	private static Map extensionInputTypeMap;
	// Output types defined in the manifest files
	private static Map extensionOutputTypeMap;
	// Targets defined in the manifest files (CDT V2.0 object model)
	private static Map extensionTargetMap;
	// "Selected configuraton" elements defined in the manifest files.
	// These are configuration elements that map to objects in the internal
	// representation of the manifest files.  For example, ListOptionValues
	// and enumeratedOptionValues are NOT included.
	// Note that these "configuration elements" are not related to the
	// managed build system "configurations".  
	// From the PDE Guide:
	//  A configuration element, with its attributes and children, directly 
	//  reflects the content and structure of the extension section within the 
	//  declaring plug-in's manifest (plugin.xml) file. 
	private static Map configElementMap;
	// Listeners interested in build model changes
	private static Map buildModelListeners;
	// Random number for derived object model elements
	private static Random randomNumber;
	// Environment Build Paths Change Listener
	private static IEnvironmentBuildPathsChangeListener fEnvironmentBuildPathsChangeListener;
	
	static {
		getEnvironmentVariableProvider().subscribe(
				fEnvironmentBuildPathsChangeListener = new IEnvironmentBuildPathsChangeListener(){
					public void buildPathsChanged(IConfiguration configuration, int buildPathType){
						if(buildPathType == IEnvVarBuildPath.BUILDPATH_INCLUDE){
							initializePathEntries(configuration,null);
							notifyListeners(configuration,null);
						}
					}
				});
	}
	/**
	 * Returns the next random number.
	 * 
	 * @return int A positive integer
	 */
	public static int getRandomNumber() {
		if (randomNumber == null) {
			// Set the random number seed
			randomNumber = new Random();
			randomNumber.setSeed(System.currentTimeMillis());
		}
		int i = randomNumber.nextInt();
		if (i < 0) {
			i *= -1;
		}
		return i;
	}
	
	/**
	 * Returns the list of project types that are defined by this project,
	 * projects referenced by this project, and by the extensions. 
	 * 
	 * @return IProjectType[]
	 */
	public static IProjectType[] getDefinedProjectTypes() {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Get the project types for this project and all referenced projects
		List definedTypes = null;
		// To Do

		// Create the array and copy the elements over
		int size = projectTypes != null ? 
				projectTypes.size()	+ (definedTypes != null ? definedTypes.size() : 0) :
				0;

		IProjectType[] types = new IProjectType[size];
		
		int n = 0;
		for (int i = 0; i < projectTypes.size(); ++i)
			types[n++] = (IProjectType)projectTypes.get(i);
		
		if (definedTypes != null)
			for (int i = 0; i < definedTypes.size(); ++i)
				types[n++] = (IProjectType)definedTypes.get(i);
				
		return types;
	}
	
	/**
	 * Returns the project type with the passed in ID
	 * 
	 * @param String 
	 * @return IProjectType
	 */
	public static IProjectType getProjectType(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (IProjectType)getExtensionProjectTypeMap().get(id);
	}
	
	protected static Map getExtensionDepCalcMap() {
		if (depCalculatorsMap == null) {
			depCalculatorsMap = new HashMap();
		}
		return depCalculatorsMap;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to ProjectTypes
	 * 
	 * @return Map
	 */
	protected static Map getExtensionProjectTypeMap() {
		if (projectTypeMap == null) {
			projectTypeMap = new HashMap();
		}
		return projectTypeMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to Configurations
	 * 
	 * @return Map
	 */
	protected static Map getExtensionConfigurationMap() {
		if (extensionConfigurationMap == null) {
			extensionConfigurationMap = new HashMap();
		}
		return extensionConfigurationMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to Resource Configurations
	 * 
	 * @return Map
	 */
	protected static Map getExtensionResourceConfigurationMap() {
		if (extensionResourceConfigurationMap == null) {
			extensionResourceConfigurationMap = new HashMap();
		}
		return extensionResourceConfigurationMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to ToolChains
	 * 
	 * @return Map
	 */
	public static SortedMap getExtensionToolChainMap() {
		if (extensionToolChainMap == null) {
			extensionToolChainMap =  new TreeMap();
		}
		return extensionToolChainMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to Tools
	 * 
	 * @return Map
	 */
	public static SortedMap getExtensionToolMap() {
		if (extensionToolMap == null) {
			extensionToolMap = new TreeMap();
		}
		return extensionToolMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to TargetPlatforms
	 * 
	 * @return Map
	 */
	protected static Map getExtensionTargetPlatformMap() {
		if (extensionTargetPlatformMap == null) {
			extensionTargetPlatformMap = new HashMap();
		}
		return extensionTargetPlatformMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to Builders
	 * 
	 * @return Map
	 */
	public static SortedMap getExtensionBuilderMap() {
		if (extensionBuilderMap == null) {
			extensionBuilderMap = new TreeMap();
		}
		return extensionBuilderMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to Options
	 * 
	 * @return Map
	 */
	protected static Map getExtensionOptionMap() {
		if (extensionOptionMap == null) {
			extensionOptionMap = new HashMap();
		}
		return extensionOptionMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to Option Categories
	 * 
	 * @return Map
	 */
	protected static Map getExtensionOptionCategoryMap() {
		if (extensionOptionCategoryMap == null) {
			extensionOptionCategoryMap = new HashMap();
		}
		return extensionOptionCategoryMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to InputTypes
	 * 
	 * @return Map
	 */
	protected static Map getExtensionInputTypeMap() {
		if (extensionInputTypeMap == null) {
			extensionInputTypeMap = new HashMap();
		}
		return extensionInputTypeMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to OutputTypes
	 * 
	 * @return Map
	 */
	protected static Map getExtensionOutputTypeMap() {
		if (extensionOutputTypeMap == null) {
			extensionOutputTypeMap = new HashMap();
		}
		return extensionOutputTypeMap;
	}

	/* (non-Javadoc)
	 * Safe accessor for the map of IDs to Targets (CDT V2.0 object model)
	 * 
	 * @return Map
	 */
	protected static Map getExtensionTargetMap() {
		if (extensionTargetMap == null) {
			extensionTargetMap = new HashMap();
		}
		return extensionTargetMap;
	}

	/**
	 * Returns the targets owned by this project.  If none are owned,
	 * an empty array is returned.
	 * 
	 * @param project
	 * @return
	 */
	public static ITarget[] getTargets(IResource resource) {
		IManagedBuildInfo buildInfo = getBuildInfo(resource);
		
		if (buildInfo != null) {
			List targets = buildInfo.getTargets();
			return (ITarget[])targets.toArray(new ITarget[targets.size()]);
		}
		return emptyTargets;
	}

	/**
	 * Returns the project type from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return IProjectType
	 */
	public static IProjectType getExtensionProjectType(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (IProjectType) getExtensionProjectTypeMap().get(id);
	}

	/**
	 * Returns the configuration from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return IConfiguration
	 */
	public static IConfiguration getExtensionConfiguration(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (IConfiguration) getExtensionConfigurationMap().get(id);
	}

	/**
	 * Returns the resource configuration from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return IResourceConfiguration
	 */
	public static IResourceConfiguration getExtensionResourceConfiguration(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (IResourceConfiguration) getExtensionResourceConfigurationMap().get(id);
	}

	/**
	 * Returns the tool-chain from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return IToolChain
	 */
	public static IToolChain getExtensionToolChain(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (IToolChain) getExtensionToolChainMap().get(id);
	}

	/**
	 * Returns the tool from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return ITool
	 */
	public static ITool getExtensionTool(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (ITool) getExtensionToolMap().get(id);
	}

	/**
	 * Returns the target platform from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return ITargetPlatform
	 */
	public static ITargetPlatform getExtensionTargetPlatform(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (ITargetPlatform) getExtensionTargetPlatformMap().get(id);
	}

	/**
	 * Returns the builder from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return IBuilder
	 */
	public static IBuilder getExtensionBuilder(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (IBuilder) getExtensionBuilderMap().get(id);
	}

	/**
	 * Returns the option from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return IOption
	 */
	public static IOption getExtensionOption(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (IOption) getExtensionOptionMap().get(id);
	}

	/**
	 * Returns the InputType from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return IInputType
	 */
	public static IInputType getExtensionInputType(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (IInputType) getExtensionInputTypeMap().get(id);
	}

	/**
	 * Returns the OutputType from the manifest with the ID specified in the argument
	 *  or <code>null</code>.
	 * 
	 * @param id
	 * @return IOutputType
	 */
	public static IOutputType getExtensionOutputType(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (IOutputType) getExtensionOutputTypeMap().get(id);
	}

	/**
	 * Returns the target from the manifest with the ID specified in the argument
	 *  or <code>null</code> - CDT V2.0 object model.
	 * 
	 * @param id
	 * @return ITarget
	 */
	public static ITarget getExtensionTarget(String id) {
		try {
			// Make sure the extensions are loaded
			loadExtensions();
		} catch (BuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (ITarget) getExtensionTargetMap().get(id);
	}
	
	/**
	 * Answers the result of a best-effort search to find a target with the 
	 * specified ID, or <code>null</code> if one is not found.
	 * 
	 * @param resource
	 * @param id
	 * @return
	 */
	public static ITarget getTarget(IResource resource, String id) {
		ITarget target = null;
		// Check if the target is spec'd in the build info for the resource
		if (resource != null) {
			IManagedBuildInfo buildInfo = getBuildInfo(resource);
			if (buildInfo != null)
				target = buildInfo.getTarget(id);
		}
		// OK, check the extension map
		if (target == null) {
			target = (ITarget)getExtensionTargetMap().get(id);
		}
		return target;
	}

	/**
	 * Sets the default configuration for the project. Note that this will also
	 * update the default target if needed.
	 *  
	 * @param project
	 * @param newDefault
	 */
	public static void setDefaultConfiguration(IProject project, IConfiguration newDefault) {
		if (project == null || newDefault == null) {
			return;
		}
		// Set the default in build information for the project 
		IManagedBuildInfo info = getBuildInfo(project);
		if (info != null) {
			info.setDefaultConfiguration(newDefault);
		}
	}
	
	/**
	 * Sets the currently selected configuration.  This is used while the project
	 * property pages are displayed
	 * 
	 * @param project
	 * @param target
	 */
	public static void setSelectedConfiguration(IProject project, IConfiguration config) {
		if (project == null) {
			return;
		}
		// Set the default in build information for the project 
		IManagedBuildInfo info = getBuildInfo(project);
		if (info != null) {
			info.setSelectedConfiguration(config);
		}
	}

	/**
	 * @param targetId
	 * @return
	 */
	public static IManagedBuilderMakefileGenerator getBuildfileGenerator(IConfiguration config) {
		IToolChain toolChain = config.getToolChain();
		if(toolChain != null){
			IBuilder builder = toolChain.getBuilder();
			if(builder != null)
				return builder.getBuildFileGenerator();
		} 
		// If no generator is defined, return the default GNU generator
		return new GnuMakefileGenerator();
	}
	
	/**
	 * load tool provider defined or default (if not found) command line generator special for selected tool
	 * @param toolId - id selected id
	 * @return IManagedCommandLineGenerator
	 */
	public static IManagedCommandLineGenerator getCommandLineGenerator(IConfiguration config, String toolId) {
		ITool tool = config.getTool(toolId);
		if (tool != null) {
			return tool.getCommandLineGenerator();
		}
		return ManagedCommandLineGenerator.getCommandLineGenerator();
	}

    /** 
     * Targets may have a scanner config discovery profile defined that knows  
     * how to discover built-in compiler defines and includes search paths. 
     * Find the profile for the target specified.
     * 
     * @param string the unique id of the target to search for
     * @return scanner configuration discovery profile id
     */
    public static String getScannerInfoProfileId(IConfiguration config) {
        IToolChain toolChain = config.getToolChain();
        return toolChain.getScannerConfigDiscoveryProfileId();
    }
    
	/**
	 * Gets the currently selected target.  This is used while the project
	 * property pages are displayed
	 * 
	 * @param project
	 * @return target
	 */
	public static IConfiguration getSelectedConfiguration(IProject project) {
		if (project == null) {
			return null;
		}
		// Set the default in build information for the project 
		IManagedBuildInfo info = getBuildInfo(project);
		if (info != null) {
			return info.getSelectedConfiguration();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * 
	 * @param config
	 * @param option
	 */
	private static void notifyListeners(IConfiguration config, IOption option) {
		// Continue if change is something that effect the scanner
		try {
			//an option can be null in the case of calling this method from the environment
			//build path change listener
			if (option != null && !(option.getValueType() == IOption.INCLUDE_PATH 
				|| option.getValueType() == IOption.PREPROCESSOR_SYMBOLS)) {
				return;
			}
		} catch (BuildException e) {return;}
		
		// Figure out if there is a listener for this change
		IResource resource = config.getOwner();
		List listeners = (List) getBuildModelListeners().get(resource);
		if (listeners == null) {
			return;
		}
		ListIterator iter = listeners.listIterator();
		while (iter.hasNext()) {
			((IScannerInfoChangeListener)iter.next()).changeNotification(resource, (IScannerInfo)getBuildInfo(resource));
		}
	}

	public static void initializePathEntries(IConfiguration config, IOption option){
		try{
			if(option != null
					&& option.getValueType() != IOption.INCLUDE_PATH
					&& option.getValueType() != IOption.PREPROCESSOR_SYMBOLS
					&& option.getValueType() != IOption.LIBRARIES
					)
				return;
		} catch (BuildException e){
			return;
		}
		IResource rc = config.getOwner();
		if(rc != null){
			IManagedBuildInfo info = getBuildInfo(rc);
			if(info instanceof ManagedBuildInfo && config.equals(info.getDefaultConfiguration()))
				((ManagedBuildInfo)info).initializePathEntries();
		}
			
	}
	
	public static void initializePathEntries(IResourceConfiguration resConfig, IOption option){
		IConfiguration cfg = resConfig.getParent();
		if(cfg != null)
			initializePathEntries(cfg,option);
	}
	
	private static void notifyListeners(IResourceConfiguration resConfig, IOption option) {
		// Continue if change is something that effect the scanreser
		try {
			if (!(option.getValueType() == IOption.INCLUDE_PATH 
				|| option.getValueType() == IOption.PREPROCESSOR_SYMBOLS)) {
				return;
			}
		} catch (BuildException e) {return;}
		
		// Figure out if there is a listener for this change
		IResource resource = resConfig.getOwner();
		List listeners = (List) getBuildModelListeners().get(resource);
		if (listeners == null) {
			return;
		}
		ListIterator iter = listeners.listIterator();
		while (iter.hasNext()) {
			((IScannerInfoChangeListener)iter.next()).changeNotification(resource, (IScannerInfo)getBuildInfo(resource));
		}
	}

	/**
	 * Adds the version of the managed build system to the project 
	 * specified in the argument.
	 * 
	 * @param newProject the project to version
	 */
	public static void setNewProjectVersion(IProject newProject) {
		// Get the build info for the argument
		ManagedBuildInfo info = findBuildInfo(newProject);
		info.setVersion(buildInfoVersion.toString());		
	}

	/**
	 * Set the boolean value for an option for a given config.
	 * 
	 * @param config The configuration the option belongs to.
	 * @param holder The holder/parent of the option.
	 * @param option The option to set the value for.
	 * @param value The boolean that the option should contain after the change.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @since 3.0 - The type and name of the <code>ITool tool</code> parameter
	 *        has changed to <code>IHoldsOptions holder</code>. Client code
	 *        assuming <code>ITool</code> as type, will continue to work unchanged.
	 */
	public static IOption setOption(IConfiguration config, IHoldsOptions holder, IOption option, boolean value) {
		IOption retOpt;
		try {
			// Request a value change and set dirty if real change results
			retOpt = config.setOption(holder, option, value);
			initializePathEntries(config,option);
			notifyListeners(config, option);
		} catch (BuildException e) {
			return null;
		}
		return retOpt;
	}

	/**
	 * Set the boolean value for an option for a given config.
	 * 
	 * @param resConfig The resource configuration the option belongs to.
	 * @param holder The holder/parent of the option.
	 * @param option The option to set the value for.
	 * @param value The boolean that the option should contain after the change.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @since 3.0 - The type and name of the <code>ITool tool</code> parameter
	 *        has changed to <code>IHoldsOptions holder</code>. Client code
	 *        assuming <code>ITool</code> as type, will continue to work unchanged.
	 */
	public static IOption setOption(IResourceConfiguration resConfig, IHoldsOptions holder, IOption option, boolean value) {
		IOption retOpt;
		try {
			// Request a value change and set dirty if real change results
			retOpt = resConfig.setOption(holder, option, value);
			initializePathEntries(resConfig,option);
			notifyListeners(resConfig, option);
		} catch (BuildException e) {
			return null;
		}
		return retOpt;
	}
	/**
	 * Set the string value for an option for a given config.
	 * 
	 * @param config The configuration the option belongs to.
	 * @param holder The holder/parent of the option.
	 * @param option The option to set the value for.
	 * @param value The value that the option should contain after the change.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @since 3.0 - The type and name of the <code>ITool tool</code> parameter
	 *        has changed to <code>IHoldsOptions holder</code>. Client code
	 *        assuming <code>ITool</code> as type, will continue to work unchanged.
	 */
	public static IOption setOption(IConfiguration config, IHoldsOptions holder, IOption option, String value) {
		IOption retOpt;
		try {
			retOpt = config.setOption(holder, option, value);
			initializePathEntries(config,option);
			notifyListeners(config, option);
		} catch (BuildException e) {
			return null;
		}
		return retOpt;
	}
	
	/**
	 * Set the string value for an option for a given  resource config.
	 * 
	 * @param resConfig The resource configuration the option belongs to.
	 * @param holder The holder/parent of the option.
	 * @param option The option to set the value for.
	 * @param value The value that the option should contain after the change.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @since 3.0 - The type and name of the <code>ITool tool</code> parameter
	 *        has changed to <code>IHoldsOptions holder</code>. Client code
	 *        assuming <code>ITool</code> as type, will continue to work unchanged.
	 */
	public static IOption setOption(IResourceConfiguration resConfig, IHoldsOptions holder, IOption option, String value) {
		IOption retOpt;
		try {
			retOpt = resConfig.setOption(holder, option, value);
			initializePathEntries(resConfig,option);
			notifyListeners(resConfig, option);
		} catch (BuildException e) {
			return null;
		}
		return retOpt;
	}
/**
	 * Set the string array value for an option for a given config.
	 * 
	 * @param config The configuration the option belongs to.
	 * @param holder The holder/parent of the option.
	 * @param option The option to set the value for.
	 * @param value The values the option should contain after the change.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @since 3.0 - The type and name of the <code>ITool tool</code> parameter
	 *        has changed to <code>IHoldsOptions holder</code>. Client code
	 *        assuming <code>ITool</code> as type, will continue to work unchanged.
	 */
	public static IOption setOption(IConfiguration config, IHoldsOptions holder, IOption option, String[] value) {
		IOption retOpt;
		try {
			retOpt = config.setOption(holder, option, value);
			initializePathEntries(config,option);
			notifyListeners(config, option);				
		} catch (BuildException e) {
			return null;
		}
		return retOpt;
	}

	/**
	 * Set the string array value for an option for a given resource config.
	 * 
	 * @param resConfig The resource configuration the option belongs to.
	 * @param holder The holder/parent of the option.
	 * @param option The option to set the value for.
	 * @param value The values the option should contain after the change.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @since 3.0 - The type and name of the <code>ITool tool</code> parameter
	 *        has changed to <code>IHoldsOptions holder</code>. Client code
	 *        assuming <code>ITool</code> as type, will continue to work unchanged.
	 */
	public static IOption setOption(IResourceConfiguration resConfig, IHoldsOptions holder, IOption option, String[] value) {
		IOption retOpt;
		try {
			retOpt = resConfig.setOption(holder, option, value);
			initializePathEntries(resConfig,option);
			notifyListeners(resConfig, option);				
		} catch (BuildException e) {
			return null;
		}
		return retOpt;
	}

	/**
	 * @param config
	 * @param tool
	 * @param command
	 */
	public static void setToolCommand(IConfiguration config, ITool tool, String command) {
		// The tool may be a reference.
		if (tool instanceof IToolReference) {
			// If so, just set the command in the reference
			((IToolReference)tool).setToolCommand(command);
		} else {
			config.setToolCommand(tool, command);
		}
	}

	public static void setToolCommand(IResourceConfiguration resConfig, ITool tool, String command) {
		// The tool may be a reference.
		if (tool instanceof IToolReference) {
			// If so, just set the command in the reference
			((IToolReference)tool).setToolCommand(command);
		} else {
			resConfig.setToolCommand(tool, command);
		}
	}
	/**
	 * Saves the build information associated with a project and all resources
	 * in the project to the build info file.
	 * 
	 * @param project
	 * @param force 
	 */
	public static void saveBuildInfo(IProject project, boolean force) {
		// Create document
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			
			// Get the build information for the project
			ManagedBuildInfo buildInfo = (ManagedBuildInfo) getBuildInfo(project);

			// Save the build info
			if (buildInfo != null && 
					!buildInfo.isReadOnly() &&
					buildInfo.isValid() &&
					(force == true || buildInfo.isDirty())) {
				// For post-2.0 projects, there will be a version
				String projectVersion = buildInfo.getVersion();
				if (projectVersion != null) {
					ProcessingInstruction instruction = doc.createProcessingInstruction(VERSION_ELEMENT_NAME, projectVersion);
					doc.appendChild(instruction);
				}
				Element rootElement = doc.createElement(ROOT_NODE_NAME);	
				doc.appendChild(rootElement);
				buildInfo.serialize(doc, rootElement);
		
				// Transform the document to something we can save in a file
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");	//$NON-NLS-1$
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//$NON-NLS-1$
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(stream);
				transformer.transform(source, result);
				
				// Save the document
				IFile projectFile = project.getFile(SETTINGS_FILE_NAME);
				String utfString = stream.toString("UTF-8");	//$NON-NLS-1$

				if (projectFile.exists()) {
					projectFile.setContents(new ByteArrayInputStream(utfString.getBytes("UTF-8")), IResource.FORCE, new NullProgressMonitor());	//$NON-NLS-1$
				} else {
					projectFile.create(new ByteArrayInputStream(utfString.getBytes("UTF-8")), IResource.FORCE, new NullProgressMonitor());	//$NON-NLS-1$
				}

				// Close the streams
				stream.close();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerFactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// The save failed
			e.printStackTrace();
		} catch (CoreException e) {
			// Save to IFile failed
			e.printStackTrace();
		}
	}

	/**
	 * @param resource
	 */
	public static void removeBuildInfo(IResource resource) {
		try {
			resource.setSessionProperty(buildInfoProperty, null);
		} catch (CoreException e) {
		}
	}

	/**
	 * Resets the build information for the project and configuration specified in the arguments. 
	 * The build information will contain the settings defined in the plugin manifest. 
	 * 
	 * @param project
	 * @param configuration
	 */
	public static void resetConfiguration(IProject project, IConfiguration configuration) {
		// reset the configuration
		((Configuration)configuration).reset();
	}

	public static void resetResourceConfiguration(IProject project, IResourceConfiguration resConfig) {
		// reset the configuration
		((ResourceConfiguration) resConfig).reset();
	}
	/**
	 * Adds a ProjectType that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param projectType 
	 */
	public static void addExtensionProjectType(ProjectType projectType) {
		if (projectTypes == null) {
			projectTypes = new ArrayList();
		}
		
		projectTypes.add(projectType);
		Object previous = getExtensionProjectTypeMap().put(projectType.getId(), projectType);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"ProjectType",	//$NON-NLS-1$
					projectType.getId());			
		}
	}
	
	/**
	 * Adds a Configuration that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param configuration 
	 */
	public static void addExtensionConfiguration(Configuration configuration) {
		Object previous = getExtensionConfigurationMap().put(configuration.getId(), configuration);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"Configuration",	//$NON-NLS-1$
					configuration.getId());			
		}
	}
	
	/**
	 * Adds a Resource Configuration that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param resourceConfiguration 
	 */
	public static void addExtensionResourceConfiguration(ResourceConfiguration resourceConfiguration) {
		Object previous = getExtensionResourceConfigurationMap().put(resourceConfiguration.getId(), resourceConfiguration);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"ResourceConfiguration",	//$NON-NLS-1$
					resourceConfiguration.getId());			
		}
	}
	
	/**
	 * Adds a ToolChain that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param toolChain 
	 */
	public static void addExtensionToolChain(ToolChain toolChain) {
		Object previous = getExtensionToolChainMap().put(toolChain.getId(), toolChain);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"ToolChain",	//$NON-NLS-1$
					toolChain.getId());			
		}
	}
		
	/**
	 * Adds a tool that is is specified in the manifest to the 
	 * build system. This tool is available to any target that 
	 * has a reference to it as part of its description. This 
	 * permits a tool that is common to many targets to be defined 
	 * only once.   
	 *  
	 * @param tool
	 */
	public static void addExtensionTool(Tool tool) {
		Object previous = getExtensionToolMap().put(tool.getId(), tool);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"Tool",	//$NON-NLS-1$
					tool.getId());			
		}
	}
	
	/**
	 * Adds a TargetPlatform that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param targetPlatform 
	 */
	public static void addExtensionTargetPlatform(TargetPlatform targetPlatform) {
		Object previous = getExtensionTargetPlatformMap().put(targetPlatform.getId(), targetPlatform);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"TargetPlatform",	//$NON-NLS-1$
					targetPlatform.getId());			
		}
	}
	
	/**
	 * Adds a Builder that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param Builder 
	 */
	public static void addExtensionBuilder(Builder builder) {
		Object previous = getExtensionBuilderMap().put(builder.getId(), builder);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"Builder",	//$NON-NLS-1$
					builder.getId());			
		}
	}
	
	/**
	 * Adds a Option that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param option 
	 */
	public static void addExtensionOption(Option option) {
		Object previous = getExtensionOptionMap().put(option.getId(), option);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"Option",	//$NON-NLS-1$
					option.getId());			
		}
	}
	
	/**
	 * Adds a OptionCategory that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param optionCategory 
	 */
	public static void addExtensionOptionCategory(OptionCategory optionCategory) {
		Object previous = getExtensionOptionCategoryMap().put(optionCategory.getId(), optionCategory);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"OptionCategory",	//$NON-NLS-1$
					optionCategory.getId());			
		}
	}
	
	/**
	 * Adds an InputType that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param inputType 
	 */
	public static void addExtensionInputType(InputType inputType) {
		Object previous = getExtensionInputTypeMap().put(inputType.getId(), inputType);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"InputType",	//$NON-NLS-1$
					inputType.getId());			
		}
	}
	
	/**
	 * Adds an OutputType that is is specified in the manifest to the 
	 * build system. It is available to any element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param outputType 
	 */
	public static void addExtensionOutputType(OutputType outputType) {
		Object previous = getExtensionOutputTypeMap().put(outputType.getId(), outputType);
		if (previous != null) {
			// Report error
			ManagedBuildManager.OutputDuplicateIdError(
					"OutputType",	//$NON-NLS-1$
					outputType.getId());			
		}
	}
	
	/**
	 * Adds a Target that is is specified in the manifest to the 
	 * build system. It is available to any CDT 2.0 object model element that 
	 * has a reference to it as part of its description.
	 *  
	 * @param target
	 */
	public static void addExtensionTarget(Target target) {
		getExtensionTargetMap().put(target.getId(), target);
	}
	
	/**
	 * Creates a new project instance for the resource based on the parent project type.
	 * 
	 * @param resource
	 * @param parentTarget
	 * @return new <code>ITarget</code> with settings based on the parent passed in the arguments
	 * @throws BuildException
	 */
	public static IManagedProject createManagedProject(IResource resource, IProjectType parent)
		throws BuildException
	{
		return new ManagedProject(resource, parent);
	}
	
	/**
	 * Creates a new target for the resource based on the parentTarget.
	 * 
	 * @param resource
	 * @param parentTarget
	 * @return new <code>ITarget</code> with settings based on the parent passed in the arguments
	 * @throws BuildException
	 */
	public static ITarget createTarget(IResource resource, ITarget parentTarget)
		throws BuildException
	{
		IResource owner = parentTarget.getOwner();
		
		if (owner != null && owner.equals(resource))
			// Already added
			return parentTarget; 
			
		if (resource instanceof IProject) {
			// Must be an extension target
			if (owner != null)
				throw new BuildException(ManagedMakeMessages.getResourceString("ManagedBuildManager.error.owner_not_null")); //$NON-NLS-1$
		} else {
			// Owner must be owned by the project containing this resource
			if (owner == null)
				throw new BuildException(ManagedMakeMessages.getResourceString("ManagedBuildManager.error.null_owner")); //$NON-NLS-1$
			if (!owner.equals(resource.getProject()))
				throw new BuildException(ManagedMakeMessages.getResourceString("ManagedBuildManager.error.owner_not_project")); //$NON-NLS-1$
		}
		
		// Passed validation so create the target.
		return new Target(resource, parentTarget);
	}
	
	/**
	 * @param resource
	 * @return
	 */
	public static IStatus initBuildInfoContainer(IResource resource) {
		ManagedBuildInfo buildInfo = null;

		// Get the build info associated with this project for this session
		try {
			buildInfo = findBuildInfo(resource.getProject());
			initBuildInfoContainer(buildInfo);
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, 
				ManagedBuilderCorePlugin.getUniqueIdentifier(), 
				IStatus.ERROR, 
				e.getLocalizedMessage(), 
				e);
		}
		return new Status(IStatus.OK, 
			ManagedBuilderCorePlugin.getUniqueIdentifier(), 
			IStatus.OK, 
			ManagedMakeMessages.getFormattedString("ManagedBuildInfo.message.init.ok", resource.getName()),	//$NON-NLS-1$ 
			null);
	}
	
	/* (non-Javadoc)
	 * Private helper method to intialize the path entry container once and 
	 * only once when the build info is first loaded or created.
	 * 
	 * @param info
	 * @throws CoreException
	 */
	private static void initBuildInfoContainer(ManagedBuildInfo info) throws CoreException {
		if (info == null) {
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(), 
					IStatus.ERROR, 
					new String(), 
					null));
		}
					
		if (info.isContainerInited()) return;
		// Now associate the path entry container with the project
		ICProject cProject = info.getCProject();

		synchronized (cProject) {

			// This does not block the workspace or trigger delta events
		IPathEntry[] entries = cProject.getRawPathEntries();
		// Make sure the container for this project is in the path entries
		List newEntries = new ArrayList(Arrays.asList(entries));
		if (!newEntries.contains(ManagedBuildInfo.containerEntry)) {
			// In this case we should trigger an init and deltas
			newEntries.add(ManagedBuildInfo.containerEntry);
			cProject.setRawPathEntries((IPathEntry[])newEntries.toArray(new IPathEntry[newEntries.size()]), new NullProgressMonitor());
		}
		info.setContainerInited(true);
		
		}  //  end synchronized
	}
	
	private static boolean isVersionCompatible(IExtension extension) {
		// We can ignore the qualifier
		PluginVersionIdentifier version = null;

		// Get the version of the manifest
		IConfigurationElement[] elements = extension.getConfigurationElements();
		
		// Find the version string in the manifest
		for (int index = 0; index < elements.length; ++index) {
			IConfigurationElement element = elements[index];
			if (element.getName().equals(REVISION_ELEMENT_NAME)) {
				version = new PluginVersionIdentifier(element.getAttribute(VERSION_ELEMENT_NAME));
				break;
			}
		}
		
		if (version == null) {
			// This is a 1.2 manifest and we are compatible for now
			return true;
		}
		return(buildInfoVersion.isGreaterOrEqualTo(version));
	}
	
	/* (non-Javadoc)
	 * Load the build information for the specified resource from its project
	 * file. Pay attention to the version number too.
	 */
	private static ManagedBuildInfo loadBuildInfo(final IProject project) throws Exception {
		ManagedBuildInfo buildInfo = null;
		IFile file = project.getFile(SETTINGS_FILE_NAME);
		File cdtbuild = file.getLocation().toFile();
		if (!cdtbuild.exists())
			return null;
	
		// So there is a project file, load the information there
		InputStream stream = new FileInputStream(cdtbuild);
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(stream);
			String fileVersion = null;
			
			// Get the first element in the project file
			Node rootElement = document.getFirstChild();
			
			// Since 2.0 this will be a processing instruction containing version
			if (rootElement.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) {
				// This is a 1.2 project and it must be updated
			} else {
				// Make sure that the version is compatible with the manager
				fileVersion = rootElement.getNodeValue();
				PluginVersionIdentifier version = new PluginVersionIdentifier(fileVersion);
				if (buildInfoVersion.isGreaterThan(version)) {
					// This is >= 2.0 project, but earlier than the current MBS version - it may need to be updated
				} else {
					// This is a 
					//  isCompatibleWith will return FALSE, if:
					//   o  The major versions are not equal
					//   o  The major versions are equal, but the remainder of the .cdtbuild version # is
					//      greater than the MBS version #
					if (!buildInfoVersion.isCompatibleWith(version)) {
						throw new BuildException(ManagedMakeMessages.getFormattedString(PROJECT_VERSION_ERROR, project.getName())); 
					}
				}
			}
			
			// Now get the project root element (there should be only one)
			NodeList nodes = document.getElementsByTagName(ROOT_NODE_NAME);
			if (nodes.getLength() > 0) {
				Node node = nodes.item(0);
				
				//  Create the internal representation of the project's MBS information
				buildInfo = new ManagedBuildInfo(project, (Element)node, fileVersion);
				if (fileVersion != null) {
					buildInfo.setVersion(fileVersion);
					PluginVersionIdentifier version = new PluginVersionIdentifier(fileVersion);
					PluginVersionIdentifier version21 = new PluginVersionIdentifier("2.1");		//$NON-NLS-1$
					//  CDT 2.1 is the first version using the new MBS model
					if (version.isGreaterOrEqualTo(version21)) {
						//  Check to see if all elements could be loaded correctly - for example, 
						//  if references in the project file could not be resolved to extension
						//  elements
						if (buildInfo.getManagedProject() == null ||
							(!buildInfo.getManagedProject().isValid())) {
							//  The load failed
							throw  new Exception(ManagedMakeMessages.getFormattedString("ManagedBuildManager.error.id.nomatch", project.getName())); //$NON-NLS-1$
						}
						
						// Each ToolChain/Tool/Builder element maintain two separate
						// converters if available
						// 0ne for previous Mbs versions and one for current Mbs version
						// walk through the project hierarchy and call the converters
						// written for previous mbs versions
						if ( checkForMigrationSupport(buildInfo, false) != true ) {
							// display an error message that the project is not loadable
							if (buildInfo.getManagedProject() == null ||
									(!buildInfo.getManagedProject().isValid())) {
									//  The load failed
									throw  new Exception(ManagedMakeMessages.getFormattedString("ManagedBuildManager.error.id.nomatch", project.getName())); //$NON-NLS-1$
							}
						}
					}
				}

				//  Upgrade the project's CDT version if necessary
				if (!UpdateManagedProjectManager.isCompatibleProject(buildInfo)) {
					UpdateManagedProjectManager.updateProject(project, buildInfo);
				}
				//  Check to see if the upgrade (if required) succeeded 
				if (buildInfo.getManagedProject() == null ||
					(!buildInfo.getManagedProject().isValid())) {
					//  The load failed
					throw  new Exception(ManagedMakeMessages.getFormattedString("ManagedBuildManager.error.id.nomatch", project.getName())); //$NON-NLS-1$
				}

				//  Walk through the project hierarchy and call the converters
				//  written for current mbs version
				if ( checkForMigrationSupport(buildInfo, true) != true ) {
					// display an error message.that the project is no loadable
					if (buildInfo.getManagedProject() == null ||
							(!buildInfo.getManagedProject().isValid())) {
							//  The load failed
							throw  new Exception(ManagedMakeMessages.getFormattedString("ManagedBuildManager.error.id.nomatch", project.getName())); //$NON-NLS-1$
						}
				}
				
				//  Finish up
				project.setSessionProperty(buildInfoProperty, buildInfo);
				IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
				//  Send an event to each configuration and if they exist, its resource configurations
				for (int i=0; i < configs.length; ++i) {
					ManagedBuildManager.performValueHandlerEvent(configs[i], IManagedOptionValueHandler.EVENT_OPEN);
				}
			}
		} catch (Exception e) {
			throw e;
		}
		
		buildInfo.setValid(true);
		return buildInfo;
	}

	/* (non-Javadoc)
	 * This method loads all of the managed build system manifest files
	 * that have been installed with CDT.  An internal hierarchy of
	 * objects is created that contains the information from the manifest
	 * files.  The information is then accessed through the ManagedBuildManager.
	 * 
	 * Since the class does not have a constructor but all public methods
	 * call this method first, it is effectively a startup method
	 */
	private static void loadExtensions() throws BuildException {
		if (projectTypesLoaded)
			return;
		
		loadExtensionsSynchronized();
	}
	
	private synchronized static void loadExtensionsSynchronized() throws BuildException {
		// Do this once
		if (projectTypesLoaded)
				return;
		projectTypesLoaded = true;
		
		// Get the extensions that use the current CDT managed build model
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		if( extensionPoint != null) {
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions != null) {
				
				// First call the constructors of the internal classes that correspond to the
				// build model elements
				for (int i = 0; i < extensions.length; ++i) {
					IExtension extension = extensions[i];
					// Can we read this manifest
					if (!isVersionCompatible(extension)) {
						//  The version of the Plug-in is greater than what the manager thinks it understands
						//  Display error message
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						if(window == null){
							IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
							window = windows[0];
						}

						final Shell shell = window.getShell();
						final String errMsg = ManagedMakeMessages.getFormattedString(MANIFEST_VERSION_ERROR, extension.getUniqueIdentifier());
						shell.getDisplay().asyncExec( new Runnable() {
							public void run() {
								MessageDialog.openError(shell, 
										ManagedMakeMessages.getResourceString("ManagedBuildManager.error.manifest_load_failed_title"),	//$NON-NLS-1$
										errMsg);
							}
						} );
					} else {			
						// Get the "configuraton elements" defined in the plugin.xml file.
						// Note that these "configuration elements" are not related to the
						// managed build system "configurations".  
						// From the PDE Guide:
						//  A configuration element, with its attributes and children, directly 
						//  reflects the content and structure of the extension section within the 
						//  declaring plug-in's manifest (plugin.xml) file. 
						IConfigurationElement[] elements = extension.getConfigurationElements();
						String revision = null;
						
						// Get the managedBuildRevsion of the extension.
						for (int j = 0; j < elements.length; j++) {
							IConfigurationElement element = elements[j];
							
							if( element.getName().equals(REVISION_ELEMENT_NAME) ) {	
								revision = element.getAttribute(VERSION_ELEMENT_NAME);
								break;
							}
						}
						
						// Get the value of 'ManagedBuildRevision' attribute
						loadConfigElements(DefaultManagedConfigElement.convertArray(elements, extension), revision);
					}
				}
				// Then call resolve.
				//
				// Here are notes on "references" within the managed build system.
				// References are "pointers" from one model element to another.
				// These are encoded in manifest and managed build system project files (.cdtbuild)
				// using unique string IDs (e.g. "cdt.managedbuild.tool.gnu.c.linker").
				// These string IDs are "resolved" to pointers to interfaces in model 
				// elements in the in-memory represent of the managed build system information.
				//
				// Here are the current "rules" for references:
				//  1.  A reference is always resolved to an interface pointer in the
				//      referenced object.
				//  2.  A reference is always TO an extension object - that is, an object
				//      loaded from a manifest file or a dynamic element provider.  It cannot
				//      be to an object loaded from a managed build system project file (.cdtbuild).
				//
		
				Iterator projectTypeIter = getExtensionProjectTypeMap().values().iterator();
				while (projectTypeIter.hasNext()) {
					try {
						ProjectType projectType = (ProjectType)projectTypeIter.next();
						projectType.resolveReferences();
					} catch (Exception ex) {
						// TODO: log
						ex.printStackTrace();
					}
				}
				Iterator configurationIter = getExtensionConfigurationMap().values().iterator();
				while (configurationIter.hasNext()) {
					try {
						Configuration configuration = (Configuration)configurationIter.next();
						configuration.resolveReferences();
					} catch (Exception ex) {
						// TODO: log
						ex.printStackTrace();
					}
				}
				Iterator resConfigIter = getExtensionResourceConfigurationMap().values().iterator();
				while (resConfigIter.hasNext()) {
					try {
						ResourceConfiguration resConfig = (ResourceConfiguration)resConfigIter.next();
						resConfig.resolveReferences();
					} catch (Exception ex) {
						// TODO: log
						ex.printStackTrace();
					}
				}
				Iterator toolChainIter = getExtensionToolChainMap().values().iterator();
				while (toolChainIter.hasNext()) {
					try {
						ToolChain toolChain = (ToolChain)toolChainIter.next();
						toolChain.resolveReferences();
					} catch (Exception ex) {
						// TODO: log
						ex.printStackTrace();
					}
				}
				Iterator toolIter = getExtensionToolMap().values().iterator();
				while (toolIter.hasNext()) {
					try {
						Tool tool = (Tool)toolIter.next();
						tool.resolveReferences();
					} catch (Exception ex) {
						// TODO: log
						ex.printStackTrace();
					}
				}
				Iterator targetPlatformIter = getExtensionTargetPlatformMap().values().iterator();
				while (targetPlatformIter.hasNext()) {
					try {
						TargetPlatform targetPlatform = (TargetPlatform)targetPlatformIter.next();
						targetPlatform.resolveReferences();
					} catch (Exception ex) {
						// TODO: log
						ex.printStackTrace();
					}
				}
				Iterator builderIter = getExtensionBuilderMap().values().iterator();
				while (builderIter.hasNext()) {
					try {
						Builder builder = (Builder)builderIter.next();
						builder.resolveReferences();
					} catch (Exception ex) {
						// TODO: log
						ex.printStackTrace();
					}
				}
				Iterator optionIter = getExtensionOptionMap().values().iterator();
				while (optionIter.hasNext()) {
					try {
						Option option = (Option)optionIter.next();
						option.resolveReferences();
					} catch (Exception ex) {
						// TODO: log
						ex.printStackTrace();
					}
				}
				Iterator optionCatIter = getExtensionOptionCategoryMap().values().iterator();
				while (optionCatIter.hasNext()) {
					try {
						OptionCategory optionCat = (OptionCategory)optionCatIter.next();
						optionCat.resolveReferences();
					} catch (Exception ex) {
						// TODO: log
						ex.printStackTrace();
					}
				}
			}
		}

		// Get the extensions that use the CDT 2.0 build model
		extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID_V2);
		if( extensionPoint != null) {
			IExtension[] extensions = extensionPoint.getExtensions();
			String revision = null;
			
			if (extensions != null) {
				if (extensions.length > 0) {
					
					// Call the constructors of the internal classes that correspond to the
					// V2.0 build model elements.  Some of these objects are converted to new model objects.
					// Others can use the same classes.
					for (int i = 0; i < extensions.length; ++i) {
						IExtension extension = extensions[i];
						// Can we read this manifest
						if (!isVersionCompatible(extension)) {
							//The version of the Plug-in is greater than what the manager thinks it understands
							throw new BuildException(ManagedMakeMessages.getResourceString(MANIFEST_VERSION_ERROR));
						}			
						IConfigurationElement[] elements = extension.getConfigurationElements();					
						
						// Get the managedBuildRevsion of the extension.
						for (int j = 0; j < elements.length; j++) {
							IConfigurationElement element = elements[j];
							if(element.getName().equals(REVISION_ELEMENT_NAME)) {
								revision = element.getAttribute(VERSION_ELEMENT_NAME);		
								break;
							}
						}
						// If the "fileVersion" attribute is missing, then default revision is "1.2.0"
						if (revision == null)
							revision = "1.2.0"; 	//$NON-NLS-1$
						loadConfigElementsV2(DefaultManagedConfigElement.convertArray(elements, extension), revision);
					}
					// Resolve references
					Iterator targetIter = getExtensionTargetMap().values().iterator();
					while (targetIter.hasNext()) {
						try {
							Target target = (Target)targetIter.next();
							target.resolveReferences();
						} catch (Exception ex) {
							// TODO: log
							ex.printStackTrace();
						}
					}
					// The V2 model can also add top-level Tools - they need to be "resolved" 
					Iterator toolIter = getExtensionToolMap().values().iterator();
					while (toolIter.hasNext()) {
						try {
							Tool tool = (Tool)toolIter.next();
							tool.resolveReferences();
						} catch (Exception ex) {
							// TODO: log
							ex.printStackTrace();
						}
					}
					// Convert the targets to the new model
					targetIter = getExtensionTargetMap().values().iterator();
					while (targetIter.hasNext()) {
						try {
							Target target = (Target)targetIter.next();
							//  Check to see if it has already been converted - if not, do it
							if (target.getCreatedProjectType() == null) {
								target.convertToProjectType(revision);
							}
						} catch (Exception ex) {
							// TODO: log
							ex.printStackTrace();
						}
					}
					// Resolve references for new ProjectTypes
					Iterator projTypeIter = getExtensionProjectTypeMap().values().iterator();
					while (projTypeIter.hasNext()) {
						try {
							ProjectType projType = (ProjectType)projTypeIter.next();
							projType.resolveReferences();
						} catch (Exception ex) {
							// TODO: log
							ex.printStackTrace();
						}
					}

					// TODO:  Clear the target and configurationV2 maps so that the object can be garbage collected
					//        We can't do this yet, because the UpdateManagedProjectAction class may need these elements later
					//        Can we change UpdateManagedProjectAction to see the converted model elements?
					//targetIter = getExtensionTargetMap().values().iterator();
					//while (targetIter.hasNext()) {
					//	try {
					//		Target target = (Target)targetIter.next();
					//		ManagedBuildManager.removeConfigElement(target);
					//		getExtensionTargetMap().remove(target);
					//	} catch (Exception ex) {
					//		// TODO: log
					//		ex.printStackTrace();
					//	}
					//}
					//getExtensionConfigurationV2Map().clear();
				}
			}
		}
	}

	private static void loadConfigElements(IManagedConfigElement[] elements, String revision) {
		for (int toolIndex = 0; toolIndex < elements.length; ++toolIndex) {
			try {
				IManagedConfigElement element = elements[toolIndex];
				// Load the top level elements, which in turn load their children
				if (element.getName().equals(IProjectType.PROJECTTYPE_ELEMENT_NAME)) {
					new ProjectType(element, revision);
				} else if (element.getName().equals(IConfiguration.CONFIGURATION_ELEMENT_NAME)) {
					new Configuration((ProjectType)null, element, revision);
				} else if (element.getName().equals(IToolChain.TOOL_CHAIN_ELEMENT_NAME)) {
					new ToolChain((Configuration)null, element, revision);
				} else if (element.getName().equals(ITool.TOOL_ELEMENT_NAME)) {
					new Tool((ProjectType)null, element, revision);
				} else if (element.getName().equals(ITargetPlatform.TARGET_PLATFORM_ELEMENT_NAME)) {
					new TargetPlatform((ToolChain)null, element, revision);
				} else if (element.getName().equals(IBuilder.BUILDER_ELEMENT_NAME)) {
					new Builder((ToolChain)null, element, revision);
				} else if (element.getName().equals(IManagedConfigElementProvider.ELEMENT_NAME)) {
					// don't allow nested config providers.
					if (element instanceof DefaultManagedConfigElement) {
						IManagedConfigElement[] providedConfigs;
						IManagedConfigElementProvider provider = createConfigProvider(
								(DefaultManagedConfigElement)element);
						providedConfigs = provider.getConfigElements();
						loadConfigElements(providedConfigs, revision);	// This must use the current build model
					}
				} else {
					// TODO: Report an error (log?)
				}
			} catch (Exception ex) {
				// TODO: log
				ex.printStackTrace();
			}
		}
	}

	private static void loadConfigElementsV2(IManagedConfigElement[] elements, String revision) {
		for (int toolIndex = 0; toolIndex < elements.length; ++toolIndex) {
			try {
				IManagedConfigElement element = elements[toolIndex];
				// Load the top level elements, which in turn load their children
				if (element.getName().equals(ITool.TOOL_ELEMENT_NAME)) {
					new Tool(element, revision);
				} else if (element.getName().equals(ITarget.TARGET_ELEMENT_NAME)) {
					new Target(element,revision);
				} else if (element.getName().equals(IManagedConfigElementProvider.ELEMENT_NAME)) {
					// don't allow nested config providers.
					if (element instanceof DefaultManagedConfigElement) {
						IManagedConfigElement[] providedConfigs;
						IManagedConfigElementProvider provider = createConfigProvider(
								(DefaultManagedConfigElement)element);
						providedConfigs = provider.getConfigElements();
						loadConfigElementsV2(providedConfigs, revision);	// This must use the 2.0 build model
					}
				}
			} catch (Exception ex) {
				// TODO: log
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Creates a new build information object and associates it with the 
	 * resource in the argument. Note that the information contains no 
	 * build target or configuation information. It is the respoinsibility 
	 * of the caller to populate it. It is also important to note that the 
	 * caller is responsible for associating an IPathEntryContainer with the 
	 * build information after it has been populated. 
	 * <p>
	 * The typical sequence of calls to add a new build information object to 
	 * a managed build project is
	 * <p><pre>
	 * ManagedBuildManager.createBuildInfo(project);
	 * &#047;&#047; Do whatever initialization you need here
	 * ManagedBuildManager.createTarget(project);
	 * ManagedBuildManager.initBuildInfoContainer(project);
	 *   
	 * @param resource The resource the build information is associated with
	 */
	public static ManagedBuildInfo createBuildInfo(IResource resource) {
		ManagedBuildInfo buildInfo = new ManagedBuildInfo(resource);
		try {
			// Associate the build info with the project for the duration of the session
			resource.setSessionProperty(buildInfoProperty, buildInfo);
		} catch (CoreException e) {
			// There is no point in keeping the info around if it isn't associated with the project
			buildInfo = null;
		}
		return buildInfo;
	}
	
	private static IManagedConfigElementProvider createConfigProvider(
		DefaultManagedConfigElement element) throws CoreException {

		return (IManagedConfigElementProvider)element.getConfigurationElement().
			createExecutableExtension(IManagedConfigElementProvider.CLASS_ATTRIBUTE);
	}

	
	/**
	 * @param project
	 * @return
	 */
	public static boolean manages(IResource resource) {
		// The managed build manager manages build information for the 
		// resource IFF it it is a project and has a build file with the proper
		// root element
		IProject project = null;
		if (resource instanceof IProject){
			project = (IProject)resource;
		} else if (resource instanceof IFile) {
			project = ((IFile)resource).getProject();
		} else {
			return false;
		}
		IFile file = project.getFile(SETTINGS_FILE_NAME);
		if (file.exists()) {
			try {
				InputStream stream = file.getContents();
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = parser.parse(stream);
				NodeList nodes = document.getElementsByTagName(ROOT_NODE_NAME);
				return (nodes.getLength() > 0);
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * Provate helper method that first checks to see if a build information 
	 * object has been associated with the project for the current workspace 
	 * session. If one cannot be found, one is created from the project file 
	 * associated with the argument. If there is no prject file or the load 
	 * fails for some reason, the method will return <code>null</code> 
	 *  
	 * @param resource
	 * @return
	 */
	private static ManagedBuildInfo findBuildInfo(IResource resource/*, boolean create*/) {
		// I am sick of NPEs
		if (resource == null) return null;

		// Make sure the extension information is loaded first
		try {
			loadExtensions();
		} catch (BuildException e) {
			e.printStackTrace();
			return null;
		}
		
		ManagedBuildInfo buildInfo = null;

		// Check if there is any build info associated with this project for this session
		try {
			buildInfo = (ManagedBuildInfo)resource.getSessionProperty(buildInfoProperty);
			// Make sure that if a project has build info, that the info is not corrupted
			if (buildInfo != null) {
				buildInfo.updateOwner(resource);
			}
		} catch (CoreException e) {
			return null;
		}
		
		if(buildInfo == null && resource instanceof IProject)
			buildInfo = findBuildInfoSynchronized((IProject)resource);
/*		
		// Nothing in session store, so see if we can load it from cdtbuild
		if (buildInfo == null && resource instanceof IProject) {
			try {
				buildInfo = loadBuildInfo((IProject)resource);
			} catch (Exception e) {
				// TODO:  Issue error reagarding not being able to load the project file (.cdtbuild)
			}
			
			try {
				// Check if the project needs its container initialized
				initBuildInfoContainer(buildInfo);
			} catch (CoreException e) {
				// We can live without a path entry container if the build information is valid
			}
		}
*/
		return buildInfo;
	}

	/* (non-Javadoc)
	 * this method is called if managed build info session property
	 * was not set. The caller will use the project rule 
	 * to synchronize with other callers
	 * findBuildInfoSynchronized could also be called from project converter
	 * in this case the ManagedBuildInfo saved in the converter would be returned
	 *  
	 * @param resource
	 * @return
	 */
	private static ManagedBuildInfo findBuildInfoSynchronized(IProject project/*, boolean create*/) {
		ManagedBuildInfo buildInfo = null;
		final ISchedulingRule rule = project; 
		IJobManager jobManager = Platform.getJobManager();

		// Check if there is any build info associated with this project for this session
		try{
			try {
				jobManager.beginRule(rule, null);
			} catch (IllegalArgumentException e) {
				//  An IllegalArgumentException means that this thread job currently has a scheduling
				//  rule active, and it doesn't match the rule that we are requesting.  
				//  So, we ignore the exception and assume the outer scheduling rule
				//  is protecting us from concurrent access
			}
			
			try {
				buildInfo = (ManagedBuildInfo)project.getSessionProperty(buildInfoProperty);
				// Make sure that if a project has build info, that the info is not corrupted
				if (buildInfo != null) {
					buildInfo.updateOwner(project);
				}
			} catch (CoreException e) {
	//			return null;
			}
			
			// Check weather getBuildInfo is called from converter
			if(buildInfo == null) {
				buildInfo = UpdateManagedProjectManager.getConvertedManagedBuildInfo(project);
				if (buildInfo != null) return buildInfo;
			}
			
			// Nothing in session store, so see if we can load it from cdtbuild
			if (buildInfo == null) {
				try {
					buildInfo = loadBuildInfo(project);
				} catch (Exception e) {
					// Issue error regarding not being able to load the project file (.cdtbuild)
					if (buildInfo == null) {
						buildInfo = createBuildInfo(project); 
					}
					buildInfo.setValid(false);
					//  Display error message
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if(window == null){
						IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
						window = windows[0];
					}

					final Shell shell = window.getShell();
					final String exceptionMsg = e.getMessage(); 
					shell.getDisplay().syncExec( new Runnable() {
						public void run() {
							MessageDialog.openError(shell, 
									ManagedMakeMessages.getResourceString("ManagedBuildManager.error.open_failed_title"),	//$NON-NLS-1$
									ManagedMakeMessages.getFormattedString("ManagedBuildManager.error.open_failed",			//$NON-NLS-1$
											exceptionMsg));
						}
					} );
				}
			}
		}
		finally{
			jobManager.endRule(rule);
		}

		if (buildInfo != null && !buildInfo.isContainerInited()) {
			//  NOTE:  If this is called inside the above rule, then an IllegalArgumentException can
			//         occur when the CDT project file is saved - it uses the Workspace Root as the scheduling rule.
			//         
			try {
				// Check if the project needs its container initialized
				initBuildInfoContainer(buildInfo);
			} catch (CoreException e) {
				// We can live without a path entry container if the build information is valid
			}
		}

		return buildInfo;
	}
	
	/**
	 * Finds, but does not create, the managed build information for the 
	 * argument.
	 * 
	 * @see ManagedBuildManager#initBuildInfo(IResource)
	 * @param resource The resource to search for managed build information on.
	 * @return IManagedBuildInfo The build information object for the resource.
	 */
	public static IManagedBuildInfo getBuildInfo(IResource resource) {
		return findBuildInfo(resource.getProject());
	}

	/**
	 * Answers the current version of the managed builder plugin.
	 * 
	 * @return the current version of the managed builder plugin
	 */
	public static PluginVersionIdentifier getBuildInfoVersion() {
		return buildInfoVersion;
	}

	/**
	 * Get the full URL for a path that is relative to the plug-in 
	 * in which .buildDefinitions are defined
	 * 
	 * @return the full URL for a path relative to the .buildDefinitions
	 *         plugin
	 */
	public static URL getURLInBuildDefinitions(DefaultManagedConfigElement element, IPath path) {
		
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		if( extensionPoint != null) {
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions != null) {
				
				// Iterate over all extensions that contribute to .buildDefinitions
				for (int i = 0; i < extensions.length; ++i) {
					IExtension extension = extensions[i];
					
					// Determine whether the configuration element that is
					// associated with the path, is valid for the extension that
					// we are currently processing.
					//
					// Note: If not done, icon file names would have to be unique
					// across several plug-ins.
					if (element.getExtension().getExtensionPointUniqueIdentifier() 
						 == extension.getExtensionPointUniqueIdentifier())
					{
						// Get the path-name
						Bundle bundle = Platform.getBundle( extension.getNamespace() );
						URL url = Platform.find(bundle, path);
						if ( url != null )
						{
							try {
								return Platform.asLocalURL(url);
							} catch (IOException e) {
								// Ignore the exception
								return null;
							}
						}
						else
						{
							// Print a warning
							OutputIconError(path.toString());
						}
					}
				}
			}			
		}		
		return null;
	}	
	
	/*
	 * @return
	 */
	private static Map getBuildModelListeners() {
		if (buildModelListeners == null) {
			buildModelListeners = new HashMap();
		}
		return buildModelListeners;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#getScannerInformation(org.eclipse.core.resources.IResource)
	 */
	public IScannerInfo getScannerInformation(IResource resource) {
		return (IScannerInfo) getBuildInfo(resource.getProject());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#subscribe(org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		IResource project = null;
		if (resource instanceof IProject) {
			project = resource;
		} else if (resource instanceof IFile) {
			project = ((IFile)resource).getProject();
		} else {
			return;
		}
		// Get listeners for this resource
		Map map = getBuildModelListeners();
		List list = (List) map.get(project);
		if (list == null) {
			// Create a new list
			list = new ArrayList();
		}
		if (!list.contains(listener)) {
			// Add the new listener for the resource
			list.add(listener);
			map.put(project, list);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfoProvider#unsubscribe(org.eclipse.cdt.core.parser.IScannerInfoChangeListener)
	 */
	public synchronized void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		IResource project = null;
		if (resource instanceof IProject) {
			project = resource;
		} else if (resource instanceof IFile) {
			project = ((IFile)resource).getProject();
		} else {
			return;
		}
		// Remove the listener
		Map map = getBuildModelListeners();
		List list = (List) map.get(project);
		if (list != null && !list.isEmpty()) {
			// The list is not empty so try to remove listener
			list.remove(listener);
			map.put(project, list);
		}
	}
	
	private static Map getConfigElementMap() {
		if (configElementMap == null) {
			configElementMap = new HashMap();
		}
		return configElementMap;
	}
	
	/**
	 * This method public for implementation reasons.  Not intended for use 
	 * by clients.
	 */
	public static void putConfigElement(IBuildObject buildObj, IManagedConfigElement configElement) {
		getConfigElementMap().put(buildObj, configElement);
	}
	
	/**
	 * Removes an item from the map
	 */
	private static void removeConfigElement(IBuildObject buildObj) {
		getConfigElementMap().remove(buildObj);
	}

	/**
	 * This method public for implementation reasons.  Not intended for use 
	 * by clients.
	 */
	public static IManagedConfigElement getConfigElement(IBuildObject buildObj) {
		return (IManagedConfigElement)getConfigElementMap().get(buildObj);
	}
	
	public static void OptionValidError(int errorId, String id) {
		String[] msgs = new String[1];
		msgs[0] = id;
		switch (errorId) {
		case ERROR_CATEGORY:
			ManagedBuildManager.OutputManifestError(
					ManagedMakeMessages.getFormattedString(ManagedBuildManager.MANIFEST_ERROR_OPTION_CATEGORY, msgs));
			break;
		case ERROR_FILTER:
			ManagedBuildManager.OutputManifestError(
					ManagedMakeMessages.getFormattedString(ManagedBuildManager.MANIFEST_ERROR_OPTION_FILTER, msgs));
			break;
		}
	}
	
	public static void OptionValueHandlerError(String attribute, String id) {
		String[] msgs = new String[2];
		msgs[0] = attribute;
		msgs[1] = id;
		ManagedBuildManager.OutputManifestError(
			ManagedMakeMessages.getFormattedString(ManagedBuildManager.MANIFEST_ERROR_OPTION_VALUEHANDLER, msgs));
	}
	
	public static void OutputResolveError(String attribute, String lookupId, String type, String id) {
		String[] msgs = new String[4];
		msgs[0] = attribute;
		msgs[1] = lookupId;
		msgs[2] = type;
		msgs[3] = id;
		ManagedBuildManager.OutputManifestError(
			ManagedMakeMessages.getFormattedString(ManagedBuildManager.MANIFEST_ERROR_RESOLVING, msgs));
	}
	
	public static void OutputDuplicateIdError(String type, String id) {
		String[] msgs = new String[2];
		msgs[0] = type;
		msgs[1] = id;
		ManagedBuildManager.OutputManifestError(
			ManagedMakeMessages.getFormattedString(ManagedBuildManager.MANIFEST_ERROR_DUPLICATE, msgs));
	}
	
	public static void OutputManifestError(String message) {
		System.err.println(ManagedMakeMessages.getResourceString(MANIFEST_ERROR_HEADER) + message + NEWLINE);
	}
	
	public static void OutputIconError(String iconLocation) {
		String[] msgs = new String[1];
		msgs[0]= iconLocation;
		ManagedBuildManager.OutputManifestError(
			ManagedMakeMessages.getFormattedString(ManagedBuildManager.MANIFEST_ERROR_ICON, msgs));
	}
	
	/**
	 * Returns the instance of the Environment Variable Provider
	 * 
	 * @return IEnvironmentVariableProvider
	 */
	public static IEnvironmentVariableProvider getEnvironmentVariableProvider(){
		return EnvironmentVariableProvider.getDefault();
	}
	
	/**
	 * Returns the version, if 'id' contains a valid version
	 * Returns null if 'id' does not contain a valid version 
	 * Returns null if 'id' does not contain a version
	 * 
	 * @param idAndVersion
	 * @return String
	 */
	
	public static String getVersionFromIdAndVersion(String idAndVersion) {
				
//		 Get the index of the separator '_' in tool id.
		int index = idAndVersion.lastIndexOf('_');

		//Validate the version number if exists.		
		if ( index != -1) {
			// Get the version number from tool id.
			String version = idAndVersion.substring(index+1);
			IStatus status = PluginVersionIdentifier.validateVersion(version);
			
			// If there is a valid version then return 'version'
			if ( status.isOK())
				return version;
		}
		// If there is no version information or not a valid version, return null
		return null;
	}
	
	/**
	 * If the input to this function contains 'id & a valid version', it returns only the 'id' part
	 * Otherwise it returns the received input back.
	 * 
	 * @param idAndVersion
	 * @return String
	 */
	public static String getIdFromIdAndVersion(String idAndVersion) {
		// If there is a valid version return only 'id' part
		if ( getVersionFromIdAndVersion(idAndVersion) != null) {
			// Get the index of the separator '_' in tool id.
			int index = idAndVersion.lastIndexOf('_');
			return idAndVersion.substring(0,index);
		}
		else {
			// if there is no version or no valid version
			return idAndVersion;
		}
	}

	/**
	 * Returns the instance of the Build Macro Provider
	 * 
	 * @return IBuildMacroProvider
	 */
	public static IBuildMacroProvider getBuildMacroProvider(){
		return BuildMacroProvider.getDefault();
	}
	
	/**
	 * Send event to value handlers of relevant configuration including
	 * all its child resource configurations, if they exist.
	 * 
	 * @param IConfiguration configuration for which to send the event
	 * @param event to be sent
	 * 
	 * @since 3.0
	 */
	public static void performValueHandlerEvent(IConfiguration config, int event) {
		performValueHandlerEvent(config, event, true);		
	}
	
	/**
	 * Send event to value handlers of relevant configuration.
	 * 
	 * @param IConfiguration configuration for which to send the event
	 * @param event to be sent
	 * @param doChildren - if true, also perform the event for all 
	 *        resource configurations that are children if this configuration. 
	 * 
	 * @since 3.0
	 */
	public static void performValueHandlerEvent(IConfiguration config, int event, boolean doChildren) {

		IToolChain toolChain = config.getToolChain();
		if (toolChain == null)
			return;
		
		IOption[] options = toolChain.getOptions();
		// Get global options directly under Toolchain (not associated with a particular tool)
		// This has to be sent to all the Options associated with this configuration.
		for (int i = 0; i < options.length; ++i) {
			// Ignore invalid options
			if (options[i].isValid()) {
				// Call the handler
				if (options[i].getValueHandler().handleValue(
						config, 
						options[i].getOptionHolder(), 
						options[i], 
						options[i].getValueHandlerExtraArgument(), 
						event)) {
					// TODO : Event is handled successfully and returned true.
					// May need to do something here say logging a message.
				} else {
					// Event handling Failed. 
				}
			}
		}

		// Get options associated with tools under toolChain
		ITool[] tools = config.getFilteredTools();
		for (int i = 0; i < tools.length; ++i) {
			IOption[] toolOptions = tools[i].getOptions();
			for (int j = 0; j < toolOptions.length; ++j) {
				// Ignore invalid options
				if (toolOptions[j].isValid()) {
					// Call the handler
					if (toolOptions[j].getValueHandler().handleValue(
							config, 
							toolOptions[j].getOptionHolder(), 
							toolOptions[j], 
							toolOptions[j].getValueHandlerExtraArgument(), 
							event)) {
						// TODO : Event is handled successfully and returned true.
						// May need to do something here say logging a message.
					} else {
						// Event handling Failed. 
					}
				}
			}
		}
		
		// Call backs for Resource Configurations associated with this config.
		if (doChildren == true) {
			IResourceConfiguration[] resConfigs = config.getResourceConfigurations();
			for (int j=0; j < resConfigs.length; ++j) {
				ManagedBuildManager.performValueHandlerEvent(resConfigs[j], event);
			}			
		}
	}
	
	/**
	 * Send event to value handlers of relevant configuration.
	 * 
	 * @param IResourceConfiguration configuration for which to send the event
	 * @param event to be sent
	 * 
	 * @since 3.0
	 */
	public static void performValueHandlerEvent(IResourceConfiguration config, int event) {

		// Note: Resource configurations have no toolchain options
		
		// Get options associated with the resource configuration
		ITool[] tools = config.getToolsToInvoke();
		for (int i = 0; i < tools.length; ++i) {
			IOption[] toolOptions = tools[i].getOptions();
			for (int j = 0; j < toolOptions.length; ++j) {
				// Ignore invalid options
				if (toolOptions[j].isValid()) {
					// Call the handler
					if (toolOptions[j].getValueHandler().handleValue(
							config, 
							toolOptions[j].getOptionHolder(), 
							toolOptions[j], 
							toolOptions[j].getValueHandlerExtraArgument(), 
							event)) {
						// TODO : Event is handled successfully and returned true.
						// May need to do something here say logging a message.
					} else {
						// Event handling Failed. 
					}
				}
			}
		}
	}
	
	private static boolean checkForMigrationSupport(ManagedBuildInfo buildInfo,
			boolean forCurrentMbsVersion) {
		
		IProjectType projectType = null;
		IConfigurationElement element = null;

		// Get the projectType from buildInfo
		IManagedProject managedProject = buildInfo.getManagedProject();
		projectType = managedProject.getProjectType();

		// walk through the hierarchy of the projectType and 
		// call the converters if available for each configuration
		IConfiguration[] configs = projectType.getConfigurations();
		for (int i = 0; i < configs.length; i++) {
			IConfiguration configuration = configs[i];
			IToolChain toolChain = configuration.getToolChain();

			if (forCurrentMbsVersion) {
				element = ((ToolChain)toolChain).getCurrentMbsVersionConversionElement();
			} else {
				element = ((ToolChain)toolChain).getPreviousMbsVersionConversionElement();
			}
			
			if (element != null) {
				// If there is a converter element for toolChain, invoke it
				// toolChain converter should take care of invoking converters of it's children
				if ( invokeConverter(toolChain, buildInfo, element) != true ) { 
					buildInfo.getManagedProject().setValid(false);
					return false;
				}
			} else {
				// If there are no converters for toolChain, walk through it's children
				ITool[] tools = toolChain.getTools();
				for (int j = 0; j < tools.length; j++) {
					ITool tool = tools[j];
					if (forCurrentMbsVersion) {
						element = ((Tool)tool).getCurrentMbsVersionConversionElement();
					} else {
						element = ((Tool)tool).getPreviousMbsVersionConversionElement();
					}
					if (element != null) {
						if ( invokeConverter(tool, buildInfo, element) != true ) {
							buildInfo.getManagedProject().setValid(false);
							return false;
						}
					}
				}
				IBuilder builder = toolChain.getBuilder();
				if (forCurrentMbsVersion) {
					element = ((Builder)builder).getCurrentMbsVersionConversionElement();
				} else {
					element = ((Builder)builder).getPreviousMbsVersionConversionElement();
				}

				if (element != null) {
					if ( invokeConverter(builder, buildInfo, element) != true ) {
						buildInfo.getManagedProject().setValid(false);
						return false;
					}
				}
			}
		}		
		// If control comes here, it means either there is no converter element
		// or converters are invoked successfully
		return true;
	}

	private static boolean invokeConverter(IBuildObject buildObject,
			IManagedBuildInfo buildInfo, IConfigurationElement element) {
		String toId = null;
		String fromId = null;
		IConvertManagedBuildObject convertBuildObject = null;

		if (element != null) {
			toId = element.getAttribute("toId"); //$NON-NLS-1$
			fromId = element.getAttribute("fromId"); //$NON-NLS-1$

			try {
				convertBuildObject = (IConvertManagedBuildObject) element
						.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (convertBuildObject != null) {
				// invoke the converter
				if (convertBuildObject
						.convert(buildObject, fromId, toId, false) != null) {
					// If it is successful, return 'true'
					return true;
				}
			}
		}
		// if control comes here, it means that either 'convertBuildObject' is null or 
		// converter did not convert the object successfully
		return false;
	}
		
}
