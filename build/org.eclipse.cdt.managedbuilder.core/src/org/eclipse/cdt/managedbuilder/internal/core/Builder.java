/*******************************************************************************
 * Copyright (c) 2004, 2009 Intel Corporation and others.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.settings.model.COutputEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.ExternalBuildRunner;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuildRunner;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.InternalBuildRunner;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.BuildBuildData;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextBuildMacroValues;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues;
import org.eclipse.cdt.managedbuilder.macros.IReservedMacroNameSupplier;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator2;
import org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.osgi.framework.Version;

public class Builder extends HoldsOptions implements IBuilder, IMatchKeyProvider, IRealBuildObjectAssociation  {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	//  Superclass
	private String superClassId;
	//  Parent and children
	private IToolChain parent;
	//  Managed Build model attributes
	private String unusedChildren;
	private String errorParserIds;
	private Boolean isAbstract;
	private String command;
	private String args;
	private IConfigurationElement buildFileGeneratorElement;
	private String versionsSupported;
	private String convertToId;
	private FileContextBuildMacroValues fileContextBuildMacroValues;
	private String builderVariablePattern;
	private Boolean isVariableCaseSensitive;
	private String[] reservedMacroNames;
	private IReservedMacroNameSupplier reservedMacroNameSupplier;
	private IConfigurationElement reservedMacroNameSupplierElement;
	
	private String autoBuildTarget;
	private Boolean autoBuildEnabled;
	private String incrementalBuildTarget;
	private Boolean incrementalBuildEnabled;
	private String cleanBuildTarget;
	private Boolean cleanBuildEnabled;
	private Boolean managedBuildOn;
	private Boolean keepEnvVarInBuildfile;
	private Boolean supportsManagedBuild;
	//custom builder settings
	private String[] customizedErrorParserIds; 
	private HashMap customizedEnvironment;
	private Boolean appendEnvironment;// = Boolean.valueOf(true);
	private String buildPath;
	private HashMap customBuildProperties;
//	private Boolean isWorkspaceBuildPath;
	private String ignoreErrCmd;
	private String parallelBuildCmd;
	private Boolean stopOnErr;
	private Integer parallelNum;
	private Boolean parallelBuildOn;
	private boolean isTest;
	
	//  Miscellaneous
	private boolean isExtensionBuilder = false;
	private boolean isDirty = false;
	private boolean resolved = true;

	private IConfigurationElement previousMbsVersionConversionElement = null;
	private IConfigurationElement currentMbsVersionConversionElement = null;
	
	private BuildBuildData fBuildData;
	
	private Boolean fSupportsCustomizedBuild;

	private List identicalList;
	
	private ICOutputEntry[] outputEntries;
	
	private ICommandLauncher fCommandLauncher = null;
	private IConfigurationElement fCommandLauncherElement = null;

	private IBuildRunner fBuildRunner = null;
	private IConfigurationElement fBuildRunnerElement = null;

	/*
	 *  C O N S T R U C T O R S
	 */
	

	/**
	 * This constructor is called to create a builder defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  				The IToolChain parent of this builder, or <code>null</code> if
	 *                				defined at the top level
	 * @param element 				The builder definition from the manifest file or a dynamic element
	 *                				provider
	 * @param managedBuildRevision 	The fileVersion of Managed Buid System                 
	 */
	public Builder(IToolChain parent, IManagedConfigElement element, String managedBuildRevision) {
		super(true);
		this.parent = parent;
		isExtensionBuilder = true;
		
		// setup for resolving
		resolved = false;

		// Set the managedBuildRevision
		setManagedBuildRevision(managedBuildRevision);
		
		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionBuilder(this);
	}
	
	/**
	 * This constructor is called to create a Builder whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param parent The parent of the builder, if any
	 * @param superClass The superClass, if any
	 * @param Id The id for the new Builder
	 * @param name The name for the new Builder
	 * @param isExtensionElement Indicates whether this is an extension element or a managed project element
	 */
	public Builder(ToolChain parent, IBuilder superClass, String Id, String name, boolean isExtensionElement) {
		super(true);
		this.parent = parent;
		this.superClass = superClass;
		setManagedBuildRevision(parent.getManagedBuildRevision());
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
		setVersion(getVersionFromId());
		
		isExtensionBuilder = isExtensionElement;
		if (isExtensionElement) {
			// Hook me up to the Managed Build Manager
			ManagedBuildManager.addExtensionBuilder(this);
		} else {
			fBuildData = new BuildBuildData(this);
			setDirty(true);
		}
	}

	/**
	 * Create a <code>Builder</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>IToolChain</code> the Builder will be added to. 
	 * @param element The XML element that contains the Builder settings.
	 * @param managedBuildRevision 	The fileVersion of Managed Buid System
	 */
	public Builder(IToolChain parent, ICStorageElement element, String managedBuildRevision) {
		super(true);
		this.parent = parent;
		isExtensionBuilder = false;
		
		fBuildData = new BuildBuildData(this);
		
		// Set the managedBuildRevision
		setManagedBuildRevision(managedBuildRevision);
		
		// Initialize from the XML attributes
		loadFromProject(element);
	}

	/**
	 * Create a <code>Builder</code> based upon an existing builder.
	 * 
	 * @param parent The <code>IToolChain</code> the builder will be added to. 
	 * @param builder The existing builder to clone.
	 */
	public Builder(IToolChain parent, String Id, String name, Builder builder) {
		super(true);
		this.parent = parent;
		
		superClass = builder.superClass;
		if (superClass != null) {
			if (builder.superClassId != null) {
				superClassId = new String(builder.superClassId);
			}
		}

		setId(Id);
		setName(name);
		
		boolean copyIds = Id.equals(builder.getId());
		// Set the managedBuildRevision & the version
		setManagedBuildRevision(builder.getManagedBuildRevision());
		setVersion(getVersionFromId());

		isExtensionBuilder = false;
		
		//  Copy the remaining attributes
		if(builder.versionsSupported != null) {
			versionsSupported = new String(builder.versionsSupported);
		}
		if(builder.convertToId != null) {
			convertToId = new String(builder.convertToId);
		}
		if (builder.unusedChildren != null) {
			unusedChildren = new String(builder.unusedChildren);
		}
		if (builder.errorParserIds != null) {
			errorParserIds = new String(builder.errorParserIds);
		}
		if (builder.isAbstract != null) {
			isAbstract = new Boolean(builder.isAbstract.booleanValue());
		}
		if (builder.command != null) {
			command = new String(builder.command);
		}
		if (builder.args != null) {
			args = new String(builder.args);
		}
		autoBuildTarget = builder.autoBuildTarget;
		autoBuildEnabled = builder.autoBuildEnabled;
		incrementalBuildTarget = builder.incrementalBuildTarget;
		incrementalBuildEnabled = builder.incrementalBuildEnabled;
		cleanBuildTarget = builder.cleanBuildTarget;
		cleanBuildEnabled = builder.cleanBuildEnabled;
		managedBuildOn = builder.managedBuildOn;
		keepEnvVarInBuildfile = builder.keepEnvVarInBuildfile;
		supportsManagedBuild = builder.supportsManagedBuild;
		if(builder.customizedErrorParserIds != null)
			customizedErrorParserIds = builder.customizedErrorParserIds.clone();
		if(builder.customizedEnvironment != null)
			customizedEnvironment = (HashMap)builder.customizedEnvironment.clone();
		appendEnvironment = builder.appendEnvironment;
		buildPath = builder.buildPath;
		if(builder.customBuildProperties != null)
			customBuildProperties = (HashMap)builder.customBuildProperties.clone();

			
			
		buildFileGeneratorElement = builder.buildFileGeneratorElement; 
		
		if(builder.fileContextBuildMacroValues != null){
			fileContextBuildMacroValues = (FileContextBuildMacroValues)builder.fileContextBuildMacroValues.clone();
			fileContextBuildMacroValues.setBuilder(this);
		}
		
		builderVariablePattern = builder.builderVariablePattern;
		
		if(builder.isVariableCaseSensitive != null)
			isVariableCaseSensitive = new Boolean(builder.isVariableCaseSensitive.booleanValue());

		if(builder.reservedMacroNames != null)
			reservedMacroNames = builder.reservedMacroNames.clone();

		reservedMacroNameSupplierElement = builder.reservedMacroNameSupplierElement;
		reservedMacroNameSupplier = builder.reservedMacroNameSupplier;

		fBuildData = new BuildBuildData(this);
		
		stopOnErr = builder.stopOnErr;
		ignoreErrCmd = builder.ignoreErrCmd;
		parallelBuildCmd = builder.parallelBuildCmd;
		parallelNum = builder.parallelNum;
		parallelBuildOn = builder.parallelBuildOn;

		if(builder.outputEntries != null){
			outputEntries = builder.outputEntries.clone();
		}

		if(copyIds){
			isDirty = builder.isDirty;
		} else {
			setDirty(true);
		}
		
		fCommandLauncher = builder.fCommandLauncher;
		fCommandLauncherElement = builder.fCommandLauncherElement;
		
		fBuildRunner = builder.fBuildRunner;
		fBuildRunnerElement = builder.fBuildRunnerElement;
	}
	
	public void copySettings(Builder builder, boolean allBuildSettings){
		try {
			if(isAutoBuildEnable() != builder.isAutoBuildEnable())
				setAutoBuildEnable(builder.isAutoBuildEnable());
		} catch (CoreException e) {
		}
		try {
			if(isIncrementalBuildEnabled() != builder.isIncrementalBuildEnabled())
				setIncrementalBuildEnable(builder.isIncrementalBuildEnabled());
		} catch (CoreException e) {
		}
		try {
			if(isFullBuildEnabled() != builder.isFullBuildEnabled())
				setFullBuildEnable(builder.isFullBuildEnabled());
		} catch (CoreException e) {
		}
		try {
			if(isCleanBuildEnabled() != builder.isCleanBuildEnabled())
				setCleanBuildEnable(builder.isCleanBuildEnabled());
		} catch (CoreException e) {
		}
		if(isStopOnError() != builder.isStopOnError()
				&& supportsStopOnError(builder.isStopOnError())){
			try {
				setStopOnError(builder.isStopOnError());
			} catch (CoreException e) {
			}
		}
		if(getParallelizationNum() != builder.getParallelizationNum()
				&& supportsParallelBuild()){
			try {
				setParallelizationNum(builder.getParallelizationNum());
			} catch (CoreException e) {
			}
		}
		if(isParallelBuildOn() != builder.isParallelBuildOn()
				&& supportsParallelBuild()){
			try {
				setParallelBuildOn(builder.isParallelBuildOn());
			} catch (CoreException e) {
			}
		}
		if(builder.keepEnvironmentVariablesInBuildfile() && 
				canKeepEnvironmentVariablesInBuildfile()){
			setKeepEnvironmentVariablesInBuildfile(builder.keepEnvironmentVariablesInBuildfile());
		}
		if(isManagedBuildOn() != builder.isManagedBuildOn()
				&& supportsBuild(builder.isManagedBuildOn())){
			try {
				setManagedBuildOn(builder.isManagedBuildOn());
			} catch (CoreException e) {
			}
		}

		if(builder.customizedErrorParserIds != null)
			customizedErrorParserIds = builder.customizedErrorParserIds.clone();
		if(builder.customizedEnvironment != null)
			customizedEnvironment = (HashMap)builder.customizedEnvironment.clone();
		appendEnvironment = builder.appendEnvironment;
		if(isBuildPathEditable()){
			if(!getBuildPath().equals(builder.getBuildPath()))
				setBuildPath(builder.getBuildPath());
		}
		if(builder.customBuildProperties != null)
			customBuildProperties = (HashMap)builder.customBuildProperties.clone();

		if(allBuildSettings){
			if(!getCommand().equals(builder.getCommand()))
				setCommand(builder.getCommand());
			if(!getArgumentsAttribute().equals(builder.getArgumentsAttribute()))
				setArgumentsAttribute(builder.getArgumentsAttribute());
			if(!CDataUtil.objectsEqual(getAutoBuildTargetAttribute(), builder.getAutoBuildTargetAttribute())){
				autoBuildTarget = builder.getAutoBuildTargetAttribute();
			}
			if(!CDataUtil.objectsEqual(getIncrementalBuildTargetAttribute(), builder.getIncrementalBuildTargetAttribute())){
				incrementalBuildTarget = builder.getIncrementalBuildTargetAttribute();
			}
			if(!CDataUtil.objectsEqual(getCleanBuildTargetAttribute(), builder.getCleanBuildTargetAttribute())){
				cleanBuildTarget = builder.getCleanBuildTargetAttribute();
			}
		}

		setDirty(true);
	}
	
/*	public Builder(IToolChain parent, String Id, String name, Builder builder, ICStorageElement el) {
		this(parent, Id, name, builder);
		
		loadFromProject(el);
	}
*/

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/**
	 * Loads the builder information from the ManagedConfigElement specified in the 
	 * argument.
	 * 
	 * @param element Contains the Builder information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(IBuildObject.ID));
		
		// Get the name
		setName(element.getAttribute(IBuildObject.NAME));
		
		// Set the version after extracting from 'id' attribute
		setVersion(getVersionFromId());
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);

		// Get the unused children, if any
		unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		
		// Get the 'versionsSupported' attribute
		versionsSupported = element.getAttribute(VERSIONS_SUPPORTED);
		
		// Get the 'convertToId' attribute
		convertToId = element.getAttribute(CONVERT_TO_ID);

		// get the 'variableFormat' attribute
		builderVariablePattern = element.getAttribute(VARIABLE_FORMAT);
		
		// get the 'isVariableCaseSensitive' attribute
		String isCS = element.getAttribute(IS_VARIABLE_CASE_SENSITIVE);
		if(isCS != null)
			isVariableCaseSensitive = new Boolean("true".equals(isCS)); //$NON-NLS-1$

		// get the reserved macro names
		String reservedNames = element.getAttribute(RESERVED_MACRO_NAMES);
		if(reservedNames != null)
			reservedMacroNames = reservedNames.split(","); //$NON-NLS-1$

		// Get the reservedMacroNameSupplier configuration element
		String reservedMacroNameSupplier = element.getAttribute(RESERVED_MACRO_NAME_SUPPLIER); 
		if(reservedMacroNameSupplier != null && element instanceof DefaultManagedConfigElement){
			reservedMacroNameSupplierElement = ((DefaultManagedConfigElement)element).getConfigurationElement();
		}


		// isAbstract
        String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
        if (isAbs != null){
    		isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
        }

        // command
		command = element.getAttribute(IBuilder.COMMAND); 
        
        // arguments
		args = element.getAttribute(IBuilder.ARGUMENTS);
		
		autoBuildTarget = element.getAttribute(ATTRIBUTE_TARGET_AUTO);
		String tmp = element.getAttribute(ATTRIBUTE_AUTO_ENABLED);
		if(tmp != null)
			autoBuildEnabled = Boolean.valueOf(tmp);
		incrementalBuildTarget = element.getAttribute(ATTRIBUTE_TARGET_INCREMENTAL);
		tmp = element.getAttribute(ATTRIBUTE_AUTO_ENABLED);
		if(tmp != null)
			incrementalBuildEnabled = Boolean.valueOf(tmp);
		cleanBuildTarget = element.getAttribute(ATTRIBUTE_TARGET_CLEAN);
		tmp = element.getAttribute(ATTRIBUTE_CLEAN_ENABLED);
		if(tmp != null)
			cleanBuildEnabled = Boolean.valueOf(tmp);
		tmp = element.getAttribute(ATTRIBUTE_MANAGED_BUILD_ON);
		if(tmp != null)
			managedBuildOn = Boolean.valueOf(tmp);
		tmp = element.getAttribute(ATTRIBUTE_KEEP_ENV);
		if(tmp != null)
			keepEnvVarInBuildfile = Boolean.valueOf(tmp);
		tmp = element.getAttribute(ATTRIBUTE_SUPORTS_MANAGED_BUILD);
		if(tmp != null)
			supportsManagedBuild = Boolean.valueOf(tmp);
		tmp = element.getAttribute(ATTRIBUTE_CUSTOMIZED_ERROR_PARSERS);
		if(tmp != null)
			customizedErrorParserIds = CDataUtil.stringToArray(tmp, ";"); //$NON-NLS-1$
		tmp = element.getAttribute(ATTRIBUTE_ENVIRONMENT);
		if(tmp != null)
			customizedEnvironment = (HashMap)MapStorageElement.decodeMap(tmp);
		tmp = element.getAttribute(ATTRIBUTE_APPEND_ENVIRONMENT);
		if(tmp != null)
			appendEnvironment = Boolean.valueOf(tmp);
		buildPath = element.getAttribute(ATTRIBUTE_BUILD_PATH);
		tmp = element.getAttribute(ATTRIBUTE_CUSTOM_PROPS);
		if(tmp != null)
			customBuildProperties = (HashMap)MapStorageElement.decodeMap(tmp);

		ignoreErrCmd = element.getAttribute(ATTRIBUTE_IGNORE_ERR_CMD);
        tmp = element.getAttribute(ATTRIBUTE_STOP_ON_ERR);
        if(tmp != null)
        	stopOnErr = Boolean.valueOf(tmp);
        parallelBuildCmd = element.getAttribute(ATTRIBUTE_PARALLEL_BUILD_CMD);
        tmp = element.getAttribute(ATTRIBUTE_PARALLELIZATION_NUMBER);
        if(tmp != null){
        	try {
        		parallelNum = Integer.decode(tmp);
        	} catch (NumberFormatException e){
        	}
        }
        tmp = element.getAttribute(ATTRIBUTE_PARALLEL_BUILD_ON);
        if(tmp != null)
        	parallelBuildOn = Boolean.valueOf(tmp);
        
		// Get the semicolon separated list of IDs of the error parsers
		errorParserIds = element.getAttribute(IToolChain.ERROR_PARSERS);
		
		// Store the configuration element IFF there is a build file generator defined 
		String buildfileGenerator = element.getAttribute(BUILDFILEGEN_ID); 
		if (buildfileGenerator != null && element instanceof DefaultManagedConfigElement) {
			buildFileGeneratorElement = ((DefaultManagedConfigElement)element).getConfigurationElement();			
		}
		
		//load the File Context Build Macro Values
		fileContextBuildMacroValues = new FileContextBuildMacroValues(this,element);
		
        tmp = element.getAttribute(IS_SYSTEM);
        if(tmp != null)
        	isTest = Boolean.valueOf(tmp).booleanValue();
        
        IManagedConfigElement[] children = element.getChildren();
        for(int i = 0; i < children.length; i++){
        	IManagedConfigElement child = children[i];
        	if (loadChild(child)) {
        		// nothing
        	} else {
	        	String name = child.getName();
	        	if(OUTPUT_ENTRIES.equals(name)){
	        		ICSettingEntry entries[] = LanguageSettingEntriesSerializer.loadEntries(new ManagedConfigStorageElement(child));
	        		if(entries.length == 0){
	        			outputEntries = new ICOutputEntry[0];
	        		} else {
		        		List list = new ArrayList(entries.length);
		        		for(int k = 0; k < entries.length; k++){
		        			if(entries[k].getKind() == ICLanguageSettingEntry.OUTPUT_PATH)
		        				list.add(entries[k]);
		        		}
		        		outputEntries = (ICOutputEntry[])list.toArray(new ICOutputEntry[list.size()]);
	        		}
	        	}
        	}
        }
        
        String commandLauncher = element.getAttribute(ATTRIBUTE_COMMAND_LAUNCHER); 
		if(commandLauncher != null && element instanceof DefaultManagedConfigElement){
			fCommandLauncherElement = ((DefaultManagedConfigElement)element).getConfigurationElement();
		}
        
		String buildRunner = element.getAttribute(ATTRIBUTE_BUILD_RUNNER);
		if (buildRunner != null && element instanceof DefaultManagedConfigElement)
			fBuildRunnerElement = ((DefaultManagedConfigElement)element).getConfigurationElement();
	}
	
	/**
	 * Initialize the builder information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the builder information 
	 */
	protected void loadFromProject(ICStorageElement element) {
		
		// id
		if(element.getAttribute(IBuildObject.ID) != null)
			setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.getAttribute(IBuildObject.NAME) != null) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// Set the version after extracting from 'id' attribute
		setVersion(getVersionFromId());

		// superClass
		if(element.getAttribute(IProjectType.SUPERCLASS) != null){
			superClassId = element.getAttribute(IProjectType.SUPERCLASS);
			if (superClassId != null && superClassId.length() > 0) {
				superClass = ManagedBuildManager.getExtensionBuilder(superClassId);
				// Check for migration support
				checkForMigrationSupport();
			}
		}

		// Get the 'versionSupported' attribute
		if (element.getAttribute(VERSIONS_SUPPORTED) != null) {
			versionsSupported = element.getAttribute(VERSIONS_SUPPORTED);
		}
		
		// Get the 'convertToId' id
		if (element.getAttribute(CONVERT_TO_ID) != null) {
			convertToId = element.getAttribute(CONVERT_TO_ID);
		}
		
		// Get the unused children, if any
		if (element.getAttribute(IProjectType.UNUSED_CHILDREN) != null) {
				unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		}
		
		// isAbstract
		if (element.getAttribute(IProjectType.IS_ABSTRACT) != null) {
			String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
			if (isAbs != null){
				isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
			}
		}

        // command
		if (element.getAttribute(IBuilder.COMMAND) != null) {
			command = element.getAttribute(IBuilder.COMMAND); 
		}
        
        // arguments
		if (element.getAttribute(IBuilder.ARGUMENTS) != null) {
			args = element.getAttribute(IBuilder.ARGUMENTS);
		}
		
		if(element.getAttribute(ATTRIBUTE_TARGET_AUTO) != null)
			autoBuildTarget = element.getAttribute(ATTRIBUTE_TARGET_AUTO);
		
		String tmp = element.getAttribute(ATTRIBUTE_AUTO_ENABLED);
		if(tmp != null)
			autoBuildEnabled = Boolean.valueOf(tmp);
		
		if(element.getAttribute(ATTRIBUTE_TARGET_INCREMENTAL) != null)
			incrementalBuildTarget = element.getAttribute(ATTRIBUTE_TARGET_INCREMENTAL);
		
		tmp = element.getAttribute(ATTRIBUTE_INCREMENTAL_ENABLED);
		if(tmp != null)
			incrementalBuildEnabled = Boolean.valueOf(tmp);
		
		if(element.getAttribute(ATTRIBUTE_TARGET_CLEAN) != null)
			cleanBuildTarget = element.getAttribute(ATTRIBUTE_TARGET_CLEAN);
		
		tmp = element.getAttribute(ATTRIBUTE_CLEAN_ENABLED);
		if(tmp != null)
			cleanBuildEnabled = Boolean.valueOf(tmp);
		
		tmp = element.getAttribute(ATTRIBUTE_MANAGED_BUILD_ON);
		if(tmp != null)
			managedBuildOn = Boolean.valueOf(tmp);
		
		tmp = element.getAttribute(ATTRIBUTE_KEEP_ENV);
		if(tmp != null)
			keepEnvVarInBuildfile = Boolean.valueOf(tmp);
		
		tmp = element.getAttribute(ATTRIBUTE_SUPORTS_MANAGED_BUILD);
		if(tmp != null)
			supportsManagedBuild = Boolean.valueOf(tmp);
		
		tmp = element.getAttribute(ATTRIBUTE_CUSTOMIZED_ERROR_PARSERS);
		if(tmp != null)
			customizedErrorParserIds = CDataUtil.stringToArray(tmp, ";"); //$NON-NLS-1$
		
		tmp = element.getAttribute(ATTRIBUTE_ENVIRONMENT);
		if(tmp != null)
			customizedEnvironment = (HashMap)MapStorageElement.decodeMap(tmp);
		
		tmp = element.getAttribute(ATTRIBUTE_APPEND_ENVIRONMENT);
		if(tmp != null)
			appendEnvironment = Boolean.valueOf(tmp);
		
		if(element.getAttribute(ATTRIBUTE_BUILD_PATH) != null)
			buildPath = element.getAttribute(ATTRIBUTE_BUILD_PATH);
		
		tmp = element.getAttribute(ATTRIBUTE_CUSTOM_PROPS);
		if(tmp != null)
			customBuildProperties = (HashMap)MapStorageElement.decodeMap(tmp);

		// Get the semicolon separated list of IDs of the error parsers
		if (element.getAttribute(IToolChain.ERROR_PARSERS) != null) {
			errorParserIds = element.getAttribute(IToolChain.ERROR_PARSERS);
		}
		
		// Note: build file generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (element.getAttribute(IBuilder.BUILDFILEGEN_ID) != null) {
			// TODO:  Issue warning?
		}
		
		if(element.getAttribute(ATTRIBUTE_IGNORE_ERR_CMD) != null)
			ignoreErrCmd = element.getAttribute(ATTRIBUTE_IGNORE_ERR_CMD);
		
        tmp = element.getAttribute(ATTRIBUTE_STOP_ON_ERR);
        if(tmp != null)
        	stopOnErr = Boolean.valueOf(tmp);
        
        if(element.getAttribute(ATTRIBUTE_PARALLEL_BUILD_CMD) != null)
        	parallelBuildCmd = element.getAttribute(ATTRIBUTE_PARALLEL_BUILD_CMD);
        
        tmp = element.getAttribute(ATTRIBUTE_PARALLELIZATION_NUMBER);
        if(tmp != null){
        	try {
        		parallelNum = Integer.decode(tmp);
        	} catch (NumberFormatException e){
        	}
        }
        
        tmp = element.getAttribute(ATTRIBUTE_PARALLEL_BUILD_ON);
        if(tmp != null)
        	parallelBuildOn = Boolean.valueOf(tmp);

        ICStorageElement[] children = element.getChildren();
        for(int i = 0; i < children.length; i++){
        	ICStorageElement child = children[i];
        	if (loadChild(child)) {
        		// nothing
        	} else {
	        	String name = child.getName();
	        	if(OUTPUT_ENTRIES.equals(name)){
	        		ICSettingEntry entries[] = LanguageSettingEntriesSerializer.loadEntries(child);
	        		if(entries.length == 0){
	        			outputEntries = new ICOutputEntry[0];
	        		} else {
		        		List list = new ArrayList(entries.length);
		        		for(int k = 0; k < entries.length; k++){
		        			if(entries[k].getKind() == ICLanguageSettingEntry.OUTPUT_PATH)
		        				list.add(entries[k]);
		        		}
		        		outputEntries = (ICOutputEntry[])list.toArray(new ICOutputEntry[list.size()]);
	        		}
	        	}
        	}
        }

	}

	public void serialize(ICStorageElement element) {
		serialize(element, true);
	}

	/**
	 * Persist the builder to the project file.
	 */
	public void serialize(ICStorageElement element, boolean resetDirtyState) {
		if (superClass != null)
			element.setAttribute(IProjectType.SUPERCLASS, superClass.getId());
		
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}

		if (unusedChildren != null) {
			element.setAttribute(IProjectType.UNUSED_CHILDREN, unusedChildren);
		}
		
		if (isAbstract != null) {
			element.setAttribute(IProjectType.IS_ABSTRACT, isAbstract.toString());
		}

		// versionsSupported
		if (versionsSupported != null) {
			element.setAttribute(VERSIONS_SUPPORTED, versionsSupported);
		}
		
		// convertToId
		if (convertToId != null) {
			element.setAttribute(CONVERT_TO_ID, convertToId);
		}

		if (errorParserIds != null) {
			element.setAttribute(IToolChain.ERROR_PARSERS, errorParserIds);
		}
		
		if (command != null) {
			element.setAttribute(IBuilder.COMMAND, command);
		}
		
		if (args != null) {
			element.setAttribute(IBuilder.ARGUMENTS, args);
		}
		
		if(autoBuildTarget != null)
			element.setAttribute(ATTRIBUTE_TARGET_AUTO, autoBuildTarget);
		if(autoBuildEnabled != null)
			element.setAttribute(ATTRIBUTE_AUTO_ENABLED, autoBuildEnabled.toString());
		if(incrementalBuildTarget != null)
			element.setAttribute(ATTRIBUTE_TARGET_INCREMENTAL, incrementalBuildTarget);
		if(incrementalBuildEnabled != null)
			element.setAttribute(ATTRIBUTE_INCREMENTAL_ENABLED, incrementalBuildEnabled.toString());
		if(cleanBuildTarget != null)
			element.setAttribute(ATTRIBUTE_TARGET_CLEAN, cleanBuildTarget);
		if(cleanBuildEnabled != null)
			element.setAttribute(ATTRIBUTE_CLEAN_ENABLED, cleanBuildEnabled.toString());
		if(managedBuildOn != null)
			element.setAttribute(ATTRIBUTE_MANAGED_BUILD_ON, managedBuildOn.toString());
		if(keepEnvVarInBuildfile != null)
			element.setAttribute(ATTRIBUTE_KEEP_ENV, keepEnvVarInBuildfile.toString());
		if(supportsManagedBuild != null)
			element.setAttribute(ATTRIBUTE_SUPORTS_MANAGED_BUILD, supportsManagedBuild.toString());
		if(customizedErrorParserIds != null)
			element.setAttribute(ATTRIBUTE_CUSTOMIZED_ERROR_PARSERS, CDataUtil.arrayToString(customizedErrorParserIds, ";")); //$NON-NLS-1$
		if(customizedEnvironment != null)
			element.setAttribute(ATTRIBUTE_ENVIRONMENT, MapStorageElement.encodeMap(customizedEnvironment));
		if(appendEnvironment != null)
			element.setAttribute(ATTRIBUTE_APPEND_ENVIRONMENT, appendEnvironment.toString());
		if(buildPath != null)	
			element.setAttribute(ATTRIBUTE_BUILD_PATH, buildPath);
		if(customBuildProperties != null)
			element.setAttribute(ATTRIBUTE_CUSTOM_PROPS, MapStorageElement.encodeMap(customBuildProperties));

        if(ignoreErrCmd != null)
        	element.setAttribute(ATTRIBUTE_IGNORE_ERR_CMD, ignoreErrCmd);
        if(stopOnErr != null)
        	element.setAttribute(ATTRIBUTE_STOP_ON_ERR, stopOnErr.toString());
        if(parallelBuildCmd != null)
        	element.setAttribute(ATTRIBUTE_PARALLEL_BUILD_CMD, parallelBuildCmd);
        if(parallelNum != null)
        	element.setAttribute(ATTRIBUTE_PARALLELIZATION_NUMBER, parallelNum.toString());
        if(parallelBuildOn != null)
        	element.setAttribute(ATTRIBUTE_PARALLEL_BUILD_ON, parallelBuildOn.toString());
		// Note: build file generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (buildFileGeneratorElement != null) {
			//  TODO:  issue warning?
		}
		
		// options
		try {
			super.serialize(element);
		} catch (BuildException e) {
			ManagedBuilderCorePlugin.log(e);
		}
		
		if(outputEntries != null){
			ICStorageElement outEl = element.createChild(OUTPUT_ENTRIES);
			LanguageSettingEntriesSerializer.serializeEntries(outputEntries, outEl);
		}
		
		if(resetDirtyState){
			// I am clean now
			isDirty = false;
		}
	}
	
	public void serializeRawData(ICStorageElement element) {
		if (superClass != null)
			element.setAttribute(IProjectType.SUPERCLASS, superClass.getId());
		
		element.setAttribute(IBuildObject.ID, id);
		
		if (getName() != null) {
			element.setAttribute(IBuildObject.NAME, getName());
		}

		if (unusedChildren != null) {
			element.setAttribute(IProjectType.UNUSED_CHILDREN, unusedChildren);
		}
		
		if (isAbstract != null) {
			element.setAttribute(IProjectType.IS_ABSTRACT, isAbstract.toString());
		}

		// versionsSupported
		if (versionsSupported != null) {
			element.setAttribute(VERSIONS_SUPPORTED, versionsSupported);
		}
		
		// convertToId
		if (convertToId != null) {
			element.setAttribute(CONVERT_TO_ID, convertToId);
		}

		if (getErrorParserIds() != null) {
			element.setAttribute(IToolChain.ERROR_PARSERS, getErrorParserIds());
		}
		
		if (getCommand() != null) {
			element.setAttribute(IBuilder.COMMAND, getCommand());
		}
		
		if (getArgumentsAttribute() != null) {
			element.setAttribute(IBuilder.ARGUMENTS, getArguments/*Attribute*/());
		}
		
		if(getAutoBuildTargetAttribute() != null)
			element.setAttribute(ATTRIBUTE_TARGET_AUTO, getAutoBuildTargetAttribute());
		element.setAttribute(ATTRIBUTE_AUTO_ENABLED, Boolean.valueOf(isAutoBuildEnable()).toString());
		if(getIncrementalBuildTargetAttribute() != null)
			element.setAttribute(ATTRIBUTE_TARGET_INCREMENTAL, getIncrementalBuildTargetAttribute());
		element.setAttribute(ATTRIBUTE_INCREMENTAL_ENABLED, Boolean.valueOf(isIncrementalBuildEnabled()).toString());
		if(getCleanBuildTargetAttribute() != null)
			element.setAttribute(ATTRIBUTE_TARGET_CLEAN, getCleanBuildTargetAttribute());
		element.setAttribute(ATTRIBUTE_CLEAN_ENABLED, Boolean.valueOf(isCleanBuildEnabled()).toString());
		element.setAttribute(ATTRIBUTE_MANAGED_BUILD_ON, Boolean.valueOf(isManagedBuildOn()).toString());
		element.setAttribute(ATTRIBUTE_KEEP_ENV, Boolean.valueOf(keepEnvironmentVariablesInBuildfile()).toString());
		element.setAttribute(ATTRIBUTE_SUPORTS_MANAGED_BUILD, Boolean.valueOf(supportsBuild(true)).toString());
		if(customizedErrorParserIds != null)
			element.setAttribute(ATTRIBUTE_CUSTOMIZED_ERROR_PARSERS, CDataUtil.arrayToString(customizedErrorParserIds, ";")); //$NON-NLS-1$
		if(customizedEnvironment != null)
			element.setAttribute(ATTRIBUTE_ENVIRONMENT, MapStorageElement.encodeMap(customizedEnvironment));
		element.setAttribute(ATTRIBUTE_APPEND_ENVIRONMENT, Boolean.valueOf(appendEnvironment()).toString());
		if(getBuildPathAttribute() != null)	
			element.setAttribute(ATTRIBUTE_BUILD_PATH, getBuildPathAttribute());
		if(customBuildProperties != null)
			element.setAttribute(ATTRIBUTE_CUSTOM_PROPS, MapStorageElement.encodeMap(customBuildProperties));

        if(getIgnoreErrCmdAttribute() != null)
        	element.setAttribute(ATTRIBUTE_IGNORE_ERR_CMD, getIgnoreErrCmdAttribute());
       	element.setAttribute(ATTRIBUTE_STOP_ON_ERR, Boolean.valueOf(isStopOnError()).toString());
        if(getParrallelBuildCmd() != null)
        	element.setAttribute(ATTRIBUTE_PARALLEL_BUILD_CMD, getParrallelBuildCmd());
       	element.setAttribute(ATTRIBUTE_PARALLELIZATION_NUMBER, new Integer(getParallelizationNumAttribute()).toString());
       	element.setAttribute(ATTRIBUTE_PARALLEL_BUILD_ON, Boolean.valueOf(isParallelBuildOn()).toString());
		// Note: build file generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (buildFileGeneratorElement != null) {
			//  TODO:  issue warning?
		}
		
		// options
		try {
			super.serialize(element);
		} catch (BuildException e) {
			ManagedBuilderCorePlugin.log(e);
		}

		if(outputEntries != null){
			ICStorageElement outEl = element.createChild(OUTPUT_ENTRIES);
			LanguageSettingEntriesSerializer.serializeEntries(outputEntries, outEl);
		}
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	public IToolChain getParent() {
		return parent;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	public IBuilder getSuperClass() {
		return (IBuilder)superClass;
	}

	@Override
	public String getName() {
		return (name == null && superClass != null) ? superClass.getName() : name;
	}

	public boolean isAbstract() {
		if (isAbstract != null) {
			return isAbstract.booleanValue();
		} else {
			return false;	// Note: no inheritance from superClass
		}
	}

	public String getUnusedChildren() {
		if (unusedChildren != null) {
			return unusedChildren;
		} else
			return EMPTY_STRING;	// Note: no inheritance from superClass
	}

	public String getCommand() {
		if (command == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return getSuperClass().getCommand();
			} else {
				return "make"; //$NON-NLS-1$
			}
		}
		return command;
	}

	public String getArguments() {
		String args = getArgumentsAttribute(); 
		String stopOnErrCmd = getStopOnErrCmd(isStopOnError());
		String parallelBuildCmd = isParallelBuildOn() ? getParallelizationCmd(getParallelizationNum()) : EMPTY_STRING;
		
		String reversedStopOnErrCmd = getStopOnErrCmd(!isStopOnError());
		String reversedParallelBuildCmd = !isParallelBuildOn() ? getParallelizationCmd(getParallelizationNum()) : EMPTY_STRING;
		
		args = removeCmd(args, reversedStopOnErrCmd);
		args = removeCmd(args, reversedParallelBuildCmd);
		
		args = addCmd(args, stopOnErrCmd);
		args = addCmd(args, parallelBuildCmd);

		return args != null ? args.trim() : null;
	}
	
	private String addCmd(String args, String cmd){
		if(getCmdIndex(args, cmd) == -1){
			if(args.length() != 0){
				args += ' ';
			}
			args += cmd;
		}
		return args;
	}
	
	private String removeCmd(String args, String cmd){
		int index = getCmdIndex(args, cmd);
		if(index != -1){
			String prefix = args.substring(0, index).trim();
			String suffix = args.substring(index + cmd.length(), args.length()).trim();
			if(prefix.length() == 0){
				args = suffix;
			} else if (suffix.length() == 0){
				args = prefix;
			} else {
				args = prefix + ' ' + suffix;
			}
			
			args = args.trim();
		}
		return args;
	}
	
	private int getCmdIndex(String args, String cmd){
		if(cmd.length() == 0)
			return -1;

		String tmp = args;
		int index = -1;
		for(index = tmp.indexOf(cmd); index != -1; index = tmp.indexOf(cmd, index + 1)){
			if(index != 0){
				char c = tmp.charAt(index-1); 
				if(c != '\t' && c != ' ')
					continue;
			}
			int end = index + cmd.length();
			if(end < tmp.length()){
				char c = tmp.charAt(end);
				if(c != '\t' && c != ' ')
					continue;
			} 

			//found
			break;
		}
		return index;
	}
	
	public String getParallelizationCmd(int num){
		String pattern = getParrallelBuildCmd();
		if(pattern.length() == 0){
			return EMPTY_STRING;
		}if(num == 0){
			return EMPTY_STRING;
		}
		
		return processParallelPattern(pattern, num < 0, num);
	}
	
	private String processParallelPattern(String pattern, boolean empty, int num){
		int start = pattern.indexOf(PARALLEL_PATTERN_NUM_START);
		int end = -1;
		boolean hasStartChar = false;
		String result;
		if(start != -1){
			end = pattern.indexOf(PARALLEL_PATTERN_NUM_END);
			if(end != -1){
				hasStartChar = true;
			} else {
				start = -1;
			}
		}
		if(start == -1){
			start = pattern.indexOf(PARALLEL_PATTERN_NUM);
			if(start != -1){
				end = start + PARALLEL_PATTERN_NUM.length();
			}
		}
		if(start == -1){
			result = pattern;
		} else {
			String prefix;
			String suffix;
			String numStr;
			prefix = pattern.substring(0, start);
			suffix = pattern.substring(end);
			numStr = pattern.substring(start, end);
			if(empty){
				result = prefix + suffix;
			} else {
				String resolvedNum;
				if(hasStartChar){
					String numPrefix, numSuffix;
					numStr = numStr.substring(0, PARALLEL_PATTERN_NUM_START.length());
					numStr = numStr.substring(numStr.length() - PARALLEL_PATTERN_NUM_END.length());
					int numStart = pattern.indexOf(PARALLEL_PATTERN_NUM);
					if(numStart != -1){
						int numEnd = numStart + PARALLEL_PATTERN_NUM.length();
						numPrefix = numStr.substring(0, numStart);
						numSuffix = numStr.substring(numEnd);
						resolvedNum = numPrefix + new Integer(num).toString() + numSuffix;
					} else {
						resolvedNum = EMPTY_STRING;
					}
				} else {
					resolvedNum = new Integer(num).toString();
				}
				result = prefix + resolvedNum + suffix;
			}
		}
		return result;
	}
	
	public String getArgumentsAttribute() {
		if (args == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return ((Builder)superClass).getArgumentsAttribute();
			}
			return EMPTY_STRING;
		}
		return args;
	}

	public String getErrorParserIds() {
		String ids = errorParserIds;
		if (ids == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				ids = getSuperClass().getErrorParserIds();
			}
		}
		return ids;
	}

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
			errorParsers = new String[0];
		}
		return errorParsers;
	}

	public void setCommand(String cmd) {
		if(getCommand().equals(cmd)) return;
		if (cmd == null && command == null) return;
		if (command == null || cmd == null || !cmd.equals(command)) {
			command = cmd;
			setDirty(true);
		}
	}

	public void setArguments(String newArgs) {
		if(getArguments().equals(newArgs))
			return;
		
		if(newArgs != null){
			String stopOnErrCmd = getStopOnErrCmd(isStopOnError());
			String parallelBuildCmd = isParallelBuildOn() ? getParallelizationCmd(getParallelizationNum()) : EMPTY_STRING;

			newArgs = removeCmd(newArgs, stopOnErrCmd);
			newArgs = removeCmd(newArgs, parallelBuildCmd);
		}
		setArgumentsAttribute(newArgs);
	}

	public void setArgumentsAttribute(String newArgs) {
		if (newArgs == null && args == null) return;
		if (args == null || newArgs == null || !newArgs.equals(args)) {
			args = newArgs;
			setDirty(true);
		}
	}

	public void setErrorParserIds(String ids) {
		String currentIds = getErrorParserIds();
		if (ids == null && currentIds == null) return;
		if (currentIds == null || ids == null || !(currentIds.equals(ids))) {
			errorParserIds = ids;
			isDirty = true;
		}
	}

	public void setIsAbstract(boolean b) {
		isAbstract = new Boolean(b);
		setDirty(true);
	}
	
	public IConfigurationElement getBuildFileGeneratorElement() {
		if (buildFileGeneratorElement == null) {
			if (superClass != null) {
				return ((Builder)superClass).getBuildFileGeneratorElement();
			}
		}
		return buildFileGeneratorElement;
	}
	
	public IManagedBuilderMakefileGenerator getBuildFileGenerator(){
		IConfigurationElement element = getBuildFileGeneratorElement();
		if (element != null) {
			try {
				if (element.getName().equalsIgnoreCase("target")) {	//$NON-NLS-1$
					if (element.getAttribute(ManagedBuilderCorePlugin.MAKEGEN_ID) != null) {
						return (IManagedBuilderMakefileGenerator) element.createExecutableExtension(ManagedBuilderCorePlugin.MAKEGEN_ID);
					}
				} else {
					if (element.getAttribute(IBuilder.BUILDFILEGEN_ID) != null) {
						return (IManagedBuilderMakefileGenerator) element.createExecutableExtension(IBuilder.BUILDFILEGEN_ID);
					}
				}
			} catch (CoreException e) {
			} catch (ClassCastException e) {
			}
			
		}
		return new GnuMakefileGenerator();
	}

	
	public void setBuildFileGeneratorElement(IConfigurationElement element) {
		buildFileGeneratorElement = element;
		setDirty(true);
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	public boolean isExtensionElement() {
		return isExtensionBuilder;
	}

	public boolean isDirty() {
		// This shouldn't be called for an extension Builder
 		if (isExtensionBuilder) return false;
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Resolve superClass
			if (superClassId != null && superClassId.length() > 0) {
				superClass = ManagedBuildManager.getExtensionBuilder(superClassId);
				if (superClass == null) {
					// Report error
					ManagedBuildManager.outputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"builder",	//$NON-NLS-1$
							getId());
				}
			}
		}
	}

	public String getConvertToId() {
		if (convertToId == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return getSuperClass().getConvertToId();
			} else {
				return EMPTY_STRING;
			}
		}
		return convertToId;
	}

	public void setConvertToId(String convertToId) {
		if (convertToId == null && this.convertToId == null) return;
		if (convertToId == null || this.convertToId == null || !convertToId.equals(this.convertToId)) {
			this.convertToId = convertToId;
			setDirty(true);
		}
		return;
	}

	public String getVersionsSupported() {
		if (versionsSupported == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return getSuperClass().getVersionsSupported();
			} else {
				return EMPTY_STRING;
			}
		}
		return versionsSupported;
	}

	public void setVersionsSupported(String versionsSupported) {
		if (versionsSupported == null && this.versionsSupported == null) return;
		if (versionsSupported == null || this.versionsSupported == null || !versionsSupported.equals(this.versionsSupported)) {
			this.versionsSupported = versionsSupported;
			setDirty(true);
		}
		return;
	}
	
	public IFileContextBuildMacroValues getFileContextBuildMacroValues(){
		if(fileContextBuildMacroValues == null && superClass != null)
			return getSuperClass().getFileContextBuildMacroValues();
		return fileContextBuildMacroValues;
	}
	
	public String getBuilderVariablePattern(){
		if(builderVariablePattern == null && superClass != null)
			return getSuperClass().getBuilderVariablePattern();
		return builderVariablePattern;
	}
	
	public boolean isVariableCaseSensitive(){
		if(isVariableCaseSensitive == null){
			if(superClass != null)
				return getSuperClass().isVariableCaseSensitive();
			return true;
		}
		return isVariableCaseSensitive.booleanValue();
	}
	
	public String[] getReservedMacroNames(){
		if(reservedMacroNames == null && superClass != null)
			return getSuperClass().getReservedMacroNames();
		return reservedMacroNames;
	}
	
	public IReservedMacroNameSupplier getReservedMacroNameSupplier(){
		if(reservedMacroNameSupplier == null && reservedMacroNameSupplierElement != null){
			try{
				reservedMacroNameSupplier = (IReservedMacroNameSupplier)reservedMacroNameSupplierElement.createExecutableExtension(RESERVED_MACRO_NAME_SUPPLIER);
			}catch(CoreException e){
			}
		}
		if(reservedMacroNameSupplier == null && superClass != null)
			return getSuperClass().getReservedMacroNameSupplier();
		return reservedMacroNameSupplier;
	}
	
	/*
	 * This function checks for migration support for the builder, while
	 * loading. If migration support is needed, looks for the available
	 * converters and stores them.
	 */

	public void checkForMigrationSupport() {

//		String tmpId = null;
		boolean isExists = false;

		if (getSuperClass() == null) {
			// If 'superClass' is null, then there is no builder available in
			// plugin manifest file with the same 'id' & version.
			// Look for the 'versionsSupported' attribute
			String high = ManagedBuildManager
					.getExtensionBuilderMap().lastKey();
			
			SortedMap subMap = null;
			if (superClassId.compareTo(high) <= 0) {
				subMap = ManagedBuildManager.getExtensionBuilderMap().subMap(
						superClassId, high + "\0"); //$NON-NLS-1$
			} else {
				// It means there are no entries in the map for the given id.
				// make the project is invalid

				IToolChain parent = getParent();
				IConfiguration parentConfig = parent.getParent();
				IManagedProject managedProject = parentConfig.getManagedProject();
				if (managedProject != null) {
					managedProject.setValid(false);
				}
				return;
			}

			// for each element in the 'subMap',
			// check the 'versionsSupported' attribute whether the given
			// builder version is supported

			String baseId = ManagedBuildManager
					.getIdFromIdAndVersion(superClassId);
			String version = ManagedBuildManager
					.getVersionFromIdAndVersion(superClassId);

			Collection c = subMap.values();
			IBuilder[] builderElements = (IBuilder[])c.toArray(new IBuilder[c.size()]);
			
			for (int i = 0; i < builderElements.length; i++) {
				IBuilder builderElement = builderElements[i];

				if (ManagedBuildManager.getIdFromIdAndVersion(
						builderElement.getId()).compareTo(baseId) > 0)
					break;
				// First check if both base ids are equal
				if (ManagedBuildManager.getIdFromIdAndVersion(
						builderElement.getId()).equals(baseId)) {

					// Check if 'versionsSupported' attribute is available'
					String versionsSupported = builderElement
							.getVersionsSupported();

					if ((versionsSupported != null)
							&& (!versionsSupported.equals(""))) { //$NON-NLS-1$
						String[] tmpVersions = versionsSupported.split(","); //$NON-NLS-1$

						for (int j = 0; j < tmpVersions.length; j++) {
							if (new Version(version).equals(new Version(
											tmpVersions[j]))) {
								// version is supported.
								// Do the automatic conversion without
								// prompting the user.
								// Get the supported version
								String supportedVersion = ManagedBuildManager
										.getVersionFromIdAndVersion(builderElement
												.getId());
								setId(ManagedBuildManager
										.getIdFromIdAndVersion(getId())
										+ "_" + supportedVersion); //$NON-NLS-1$
								
								// If control comes here means that 'superClass' is null
								// So, set the superClass to this builder element
								superClass = builderElement;
								superClassId = superClass.getId();
								isExists = true;
								break;
							}
						}
						if(isExists)
							break;        // break the outer for loop if 'isExists' is true
					}					
				}
			}
		}
		if (getSuperClass() != null) {
			// If 'getSuperClass()' is not null, look for 'convertToId' attribute in plugin
			// manifest file for this builder.
			String convertToId = getSuperClass().getConvertToId();
			if ((convertToId == null) || (convertToId.equals(""))) { //$NON-NLS-1$
				// It means there is no 'convertToId' attribute available and
				// the version is still actively
				// supported by the tool integrator. So do nothing, just return
				return;
			} else {
				// Incase the 'convertToId' attribute is available,
				// it means that Tool integrator currently does not support this
				// version of builder.
				// Look for the converters available for this builder version.

				getConverter(convertToId);
			}

		} else {
			// make the project is invalid
			// 
			IToolChain parent = getParent();
			IConfiguration parentConfig = parent.getParent();
			IManagedProject managedProject = parentConfig.getManagedProject();
			if (managedProject != null) {
				managedProject.setValid(false);
			}		
		}
		return;
	}

	private void getConverter(String convertToId) {

		String fromId = null;
		String toId = null;

		// Get the Converter Extension Point
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.cdt.managedbuilder.core", //$NON-NLS-1$
						"projectConverter"); //$NON-NLS-1$
		if (extensionPoint != null) {
			// Get the extensions
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				// Get the configuration elements of each extension
				IConfigurationElement[] configElements = extensions[i]
						.getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {

					IConfigurationElement element = configElements[j];

					if (element.getName().equals("converter")) { //$NON-NLS-1$

						fromId = element.getAttribute("fromId"); //$NON-NLS-1$
						toId = element.getAttribute("toId"); //$NON-NLS-1$
						// Check whether the current converter can be used for
						// the selected builder

						if (fromId.equals(getSuperClass().getId())
								&& toId.equals(convertToId)) {
							// If it matches
							String mbsVersion = element
									.getAttribute("mbsVersion"); //$NON-NLS-1$
							Version currentMbsVersion = ManagedBuildManager
									.getBuildInfoVersion();

							// set the converter element based on the MbsVersion
							if (currentMbsVersion.compareTo(new Version(mbsVersion))>0) {
								previousMbsVersionConversionElement = element;
							} else {
								currentMbsVersionConversionElement = element;
							}
							return;
						}
					}
				}
			}
		}

		// If control comes here, it means 'Tool Integrator' specified
		// 'convertToId' attribute in toolchain definition file, but
		// has not provided any converter.
		// So, make the project is invalid

		IToolChain parent = getParent();
		IConfiguration parentConfig = parent.getParent();
		IManagedProject managedProject = parentConfig.getManagedProject();
		if (managedProject != null) {
			managedProject.setValid(false);
		}
	}
	
	public IConfigurationElement getPreviousMbsVersionConversionElement() {
		return previousMbsVersionConversionElement;
	}

	public IConfigurationElement getCurrentMbsVersionConversionElement() {
		return currentMbsVersionConversionElement;
	}

	public CBuildData getBuildData() {
		return fBuildData;
	}

//	public String[] getCustomizedErrorParserIds(){
//		if(customizedErrorParserIds != null)
//			return (String[])customizedErrorParserIds.clone();
//		return null;
//	}
	
	public String[] getErrorParsers() {
		if(isCustomBuilder() && customizedErrorParserIds != null)
			return customizedErrorParserIds.clone();
		
		IToolChain parent = getParent();
		IConfiguration parentConfig = parent.getParent();
		return parentConfig.getErrorParserList();
	}
	
	public String[] getCustomizedErrorParserIds(){
		if(customizedErrorParserIds != null)
			return customizedErrorParserIds.clone();
		return null;
	}
	
	public void setCustomizedErrorParserIds(String[] ids){
		customizedErrorParserIds = ids != null ? (String[])ids.clone() : ids;
	}

	public void setErrorParsers(String[] parsers) throws CoreException {
		if(isCustomBuilder()){
			customizedErrorParserIds = (parsers != null && parsers.length != 0) ? (String[])parsers.clone() : parsers;
		} else {
			IToolChain parent = getParent();
			IConfiguration parentConfig = parent.getParent();
			parentConfig.setErrorParserList(parsers);
		}
	}
	
	private Object getMacroContextData(){
		return this;//!isExtensionBuilder ? (Object)this : (Object)getParent().getParent();
	}

	public String getBuildArguments() {
		String args = getArguments();
		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();

		try {
			args = provider.resolveValue(args, "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, getMacroContextData()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (BuildMacroException e) {
		}
		
		return args;
	}

	public IPath getBuildCommand() {
		String command = getCommand();
		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();

		try {
			command = provider.resolveValue(command, "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, getMacroContextData()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (BuildMacroException e) {
		}

		return new Path(command);
	}
	
	public String getBuildPathAttribute(){
		return getBuildPathAttribute(true);
	}
	
	public String getBuildPathAttribute(boolean querySuperClass){
		if(buildPath == null){
			if(querySuperClass && superClass != null){
				return ((Builder)superClass).getBuildPathAttribute(true);
			}
		}
		return buildPath;
	}
	
	public void setBuildPath(String path){
		setBuildPathAttribute(path);
	}
	
	public void setBuildPathAttribute(String path){
		buildPath = path;
		setDirty(true);
	}
	
	public String getBuildPath(){
		if(isManagedBuildOn())
			return getDefaultBuildPath();
		
		String path = getBuildPathAttribute();
		if(path == null){
			path = getDefaultBuildPath();
//			if(isManagedBuildOn() && !isExtensionElement()) {
//				buildPath = path;
//			}
		}
		return path;
	}
	
	private boolean isBuildPathEditable(){
		return !isManagedBuildOn();
	}
	
	public String getDefaultBuildPath(){
		Configuration cfg = (Configuration)getConfguration();
		IPath buildPath;
		String result;
		
//		Builder extBuilder = (Builder)ManagedBuildManager.getExtensionBuilder(this);
//		String attr = extBuilder.getBuildPathAttribute();
		if(cfg != null){
			if(!isExtensionElement() && !cfg.isPreference()){
				IProject project = cfg.getOwner().getProject();
//				if(attr == null){
					if(isManagedBuildOn()){
						IManagedBuilderMakefileGenerator gen = getBuildFileGenerator();
						if(gen instanceof IManagedBuilderMakefileGenerator2){
							((IManagedBuilderMakefileGenerator2)gen).initialize(IncrementalProjectBuilder.FULL_BUILD, cfg, this, new NullProgressMonitor());
						} else {
							gen.initialize(project, ManagedBuildManager.getBuildInfo(project), new NullProgressMonitor());
						}
						
						buildPath = gen.getBuildWorkingDir();
						if(buildPath == null)
							buildPath = new Path(cfg.getName());
					} else {
						buildPath = Path.EMPTY;
					}
//				} else {
//					buildPath = new Path(attr);
//				}
				
				if(!buildPath.isAbsolute()){
					buildPath = project.getFullPath().append(buildPath);
					IStringVariableManager mngr = VariablesPlugin.getDefault().getStringVariableManager();
	
					result = buildPath.toString();
					result = mngr.generateVariableExpression("workspace_loc", result); //$NON-NLS-1$
				} else {
					result = buildPath.toString();
				}
			} else {
				if(isManagedBuildOn()){
					result = cfg.getName();
					if(result == null)
						result = ""; //$NON-NLS-1$
				} else {
					result = ""; //$NON-NLS-1$ 
				}
			}
		} else {
			result = ""; //$NON-NLS-1$ 
		}
		
		return result;
	}
	
/*	public boolean isWorkspaceBuildPath(){
		String path = getBuildPathAttribute();
		if(path == null)
			return true;
	
		if(isWorkspaceBuildPath == null){
			if(superClass != null)
				return superClass.isWorkspaceBuildPath();
			return true;
		}
		return isWorkspaceBuildPath.booleanValue();
	}
*/
	public IPath getBuildLocation() {
		String path = getBuildPath();
		
		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();

		try {
			path = provider.resolveValue(path, "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, getMacroContextData()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (BuildMacroException e) {
		}
		
		return new Path(path);
	}

	public boolean isDefaultBuildCmd() {
		return isExtensionBuilder || (command == null && args == null /*&& stopOnErr == null && parallelBuildOn == null && parallelNum == null */ &&  superClass != null);
	}

	public boolean isStopOnError() {
		if(stopOnErr == null){
			if(superClass != null){
				return getSuperClass().isStopOnError();
			}
			return true;
		}
		return stopOnErr.booleanValue();
	}

	public void setBuildArguments(String args) throws CoreException {
		setArguments(args);
	}

	public void setBuildCommand(IPath command) throws CoreException {
		String cmd = command != null ? command.toString() : null;
		setCommand(cmd);
	}

	public void setBuildLocation(IPath location) throws CoreException {
		String path = location != null ? location.toString() : null;
		setBuildPath(path);
	}

	public void setStopOnError(boolean on) throws CoreException {
		if(isStopOnError() == on)
			return;
		
		if(supportsStopOnError(on)){
			String curCmd = getStopOnErrCmd(isStopOnError());
			String args = getArgumentsAttribute();
			String updatedArgs = removeCmd(args, curCmd);
			if(!updatedArgs.equals(args))
				setArgumentsAttribute(updatedArgs);
			stopOnErr = Boolean.valueOf(on);
		}
		setDirty(true);
	}

	public void setUseDefaultBuildCmd(boolean on) throws CoreException {
		if(!isExtensionBuilder && superClass != null){
			if(on){
				command = null;
				args = null;
//				stopOnErr = null;
//				parallelBuildOn = null;
//				parallelNum = null;
			} else {
				command = getCommand();
			}
		}
	}

	public String getAutoBuildTargetAttribute() {
		if(autoBuildTarget == null){
			if(superClass != null)
				return ((Builder)superClass).getAutoBuildTargetAttribute();
			return null;
		}
		return autoBuildTarget;
	}
	
	public String getAutoBuildTarget() {
		String attr = getAutoBuildTargetAttribute();
		
		if(attr != null){
			IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
	
			try {
				attr = provider.resolveValue(attr, "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, getMacroContextData()); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (BuildMacroException e) {
			}
		}
		if(attr == null){
			attr = DEFAULT_TARGET_AUTO;
		}
		
		return attr;
	}


	public String getCleanBuildTargetAttribute() {
		if(cleanBuildTarget == null){
			if(superClass != null)
				return ((Builder)superClass).getCleanBuildTargetAttribute();
			return null;
		}
		return cleanBuildTarget;
	}

	public String getCleanBuildTarget() {
		String attr = getCleanBuildTargetAttribute();
		
		if(attr != null){
			IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
	
			try {
				attr = provider.resolveValue(attr, "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, getMacroContextData()); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (BuildMacroException e) {
			}
		}
		if(attr == null){
			attr = DEFAULT_TARGET_CLEAN;
		}
		
		return attr;
	}

	
	public String getFullBuildTarget() {
		return getIncrementalBuildTarget();
	}

	public String getIncrementalBuildTargetAttribute() {
		if(incrementalBuildTarget == null){
			if(superClass != null)
				return ((Builder)superClass).getIncrementalBuildTargetAttribute();
			return null;
		}
		return incrementalBuildTarget;
	}

	public String getIncrementalBuildTarget() {
		String attr = getIncrementalBuildTargetAttribute();
		
		if(attr != null){
			IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
	
			try {
				attr = provider.resolveValue(attr, "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, getMacroContextData()); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (BuildMacroException e) {
			}
		}
		if(attr == null){
			attr = DEFAULT_TARGET_INCREMENTAL;
		}
		
		return attr;
	}

	public boolean isAutoBuildEnable() {
		if(autoBuildEnabled == null){
			if(superClass != null)
				return getSuperClass().isAutoBuildEnable();
			return false;
		}
		return autoBuildEnabled.booleanValue();
	}

	public boolean isCleanBuildEnabled() {
		if(cleanBuildEnabled == null){
			if(superClass != null)
				return getSuperClass().isCleanBuildEnabled();
			return true;
		}
		return cleanBuildEnabled.booleanValue();
	}

	public boolean isFullBuildEnabled() {
		return isIncrementalBuildEnabled();
	}

	public boolean isIncrementalBuildEnabled() {
		if(incrementalBuildEnabled == null){
			if(superClass != null)
				return getSuperClass().isIncrementalBuildEnabled();
			return true;
		}
		return incrementalBuildEnabled.booleanValue();
	}

	public void setAutoBuildEnable(boolean enabled) throws CoreException {
		autoBuildEnabled = Boolean.valueOf(enabled);
	}

	public void setAutoBuildTarget(String target) throws CoreException {
		autoBuildTarget = target;
	}

	public void setCleanBuildEnable(boolean enabled) throws CoreException {
		cleanBuildEnabled = Boolean.valueOf(enabled);
	}

	public void setCleanBuildTarget(String target) throws CoreException {
		cleanBuildTarget = target;
	}

	public void setFullBuildEnable(boolean enabled) throws CoreException {
		setIncrementalBuildEnable(enabled);
	}

	public void setFullBuildTarget(String target) throws CoreException {
		setIncrementalBuildTarget(target);
	}

	public void setIncrementalBuildEnable(boolean enabled) throws CoreException {
		incrementalBuildEnabled = Boolean.valueOf(enabled);
	}

	public void setIncrementalBuildTarget(String target) throws CoreException {
		incrementalBuildTarget = target;
	}

	public boolean appendEnvironment() {
		if(appendEnvironment == null){
			if(superClass != null){
				return getSuperClass().appendEnvironment();
			}
			return true;
		}
		return appendEnvironment.booleanValue();
	}

	public String getBuildAttribute(String name, String defaultValue) {
		String result = null;
		if(BUILD_TARGET_INCREMENTAL.equals(name)){
			result = getIncrementalBuildTargetAttribute();
		} else if(BUILD_TARGET_AUTO.equals(name)){
			result =  getAutoBuildTargetAttribute();
		} else if(BUILD_TARGET_CLEAN.equals(name)){
			result = getCleanBuildTargetAttribute(); 
		} else if(BUILD_LOCATION.equals(name)){
			result = getBuildPathAttribute();
		} else if(BUILD_COMMAND.equals(name)){
			result = getCommand();
		} else if(BUILD_ARGUMENTS.equals(name)){
			result = getArguments();
		} else if(BuilderFactory.BUILD_COMMAND.equals(name)){
			result = getCommand();
		} else if(BuilderFactory.BUILD_LOCATION.equals(name)){
			result = getBuildPathAttribute();
		} else if(BuilderFactory.STOP_ON_ERROR.equals(name)){
			result = Boolean.valueOf(isStopOnError()).toString();
		} else if(BuilderFactory.USE_DEFAULT_BUILD_CMD.equals(name)){
			result = Boolean.valueOf(isDefaultBuildCmd()).toString();
		} else if(BuilderFactory.BUILD_TARGET_AUTO.equals(name)){
			result = getAutoBuildTargetAttribute();
		} else if(BuilderFactory.BUILD_TARGET_INCREMENTAL.equals(name)){
			result = getIncrementalBuildTargetAttribute();
		} else if(BuilderFactory.BUILD_TARGET_FULL.equals(name)){
			result = getIncrementalBuildTargetAttribute();
		} else if(BuilderFactory.BUILD_TARGET_CLEAN.equals(name)){
			result = getCleanBuildTargetAttribute();
		} else if(BuilderFactory.BUILD_FULL_ENABLED.equals(name)){
			result = Boolean.valueOf(isFullBuildEnabled()).toString();
		} else if(BuilderFactory.BUILD_CLEAN_ENABLED.equals(name)){
			result = Boolean.valueOf(isCleanBuildEnabled()).toString();
		} else if(BuilderFactory.BUILD_INCREMENTAL_ENABLED.equals(name)){
			result = Boolean.valueOf(isIncrementalBuildEnabled()).toString();
		} else if(BuilderFactory.BUILD_AUTO_ENABLED.equals(name)){
			result = Boolean.valueOf(isAutoBuildEnable()).toString();
		} else if(BuilderFactory.BUILD_ARGUMENTS.equals(name)){
			result = getArguments();
		} else if(BuilderFactory.ENVIRONMENT.equals(name)){
			result = customizedEnvironment != null ?
				MapStorageElement.encodeMap(customizedEnvironment) : null;
		} else if(BuilderFactory.BUILD_APPEND_ENVIRONMENT.equals(name)){
			result = Boolean.valueOf(appendEnvironment()).toString();
		} else if(customBuildProperties != null){
			result = (String)customBuildProperties.get(name);
		}
		
		if(result == null)
			return defaultValue;
		return result;
	}
	
	public static String[] toBuildAttributes(String name) {
		
		if(ATTRIBUTE_TARGET_INCREMENTAL.equals(name)){
			return new String[]{BUILD_TARGET_INCREMENTAL, BuilderFactory.BUILD_TARGET_INCREMENTAL, BUILD_TARGET_FULL, BuilderFactory.BUILD_TARGET_FULL};
		} else if(ATTRIBUTE_TARGET_AUTO.equals(name)){
			return new String[]{BUILD_TARGET_AUTO, BuilderFactory.BUILD_TARGET_AUTO};
		} else if(ATTRIBUTE_TARGET_CLEAN.equals(name)){
			return new String[]{BUILD_TARGET_CLEAN, BuilderFactory.BUILD_TARGET_CLEAN}; 
		} else if(ATTRIBUTE_BUILD_PATH.equals(name)){
			return new String[]{BUILD_LOCATION, BuilderFactory.BUILD_LOCATION};
		} else if(COMMAND.equals(name)){
			return new String[]{BUILD_COMMAND, BuilderFactory.BUILD_COMMAND};
		} else if(ARGUMENTS.equals(name)){
			return new String[]{BUILD_ARGUMENTS, BuilderFactory.BUILD_ARGUMENTS};
		} else if(ATTRIBUTE_STOP_ON_ERR.equals(name)){
			return new String[]{BuilderFactory.STOP_ON_ERROR};
		} //TODO else if(BuilderFactory.USE_DEFAULT_BUILD_CMD.equals(name)){
		//	return getCommand();
		//}
		else if(ATTRIBUTE_INCREMENTAL_ENABLED.equals(name)) {
			return new String[]{BuilderFactory.BUILD_INCREMENTAL_ENABLED, BuilderFactory.BUILD_FULL_ENABLED};
		} else if(ATTRIBUTE_CLEAN_ENABLED.equals(name)){
			return new String[]{BuilderFactory.BUILD_CLEAN_ENABLED};
		} else if(ATTRIBUTE_AUTO_ENABLED.equals(name)){
			return new String[]{BuilderFactory.BUILD_AUTO_ENABLED};
		} else if(ATTRIBUTE_ENVIRONMENT.equals(name)){
			return new String[]{BuilderFactory.ENVIRONMENT};
		} else if(ATTRIBUTE_APPEND_ENVIRONMENT.equals(name)){
			return new String[]{BuilderFactory.BUILD_APPEND_ENVIRONMENT};
		} else if(ATTRIBUTE_CUSTOMIZED_ERROR_PARSERS.equals(name)){
			return new String[]{ErrorParserManager.PREF_ERROR_PARSER};
		}
		
		return new String[0];
	}
	
	public static String toBuilderAttribute(String name) {

		if(BUILD_TARGET_INCREMENTAL.equals(name)
				|| BuilderFactory.BUILD_TARGET_INCREMENTAL.equals(name)
				|| BUILD_TARGET_FULL.equals(name)
				|| BuilderFactory.BUILD_TARGET_FULL.equals(name)){
			return ATTRIBUTE_TARGET_INCREMENTAL;
		} else if (BUILD_TARGET_AUTO.equals(name)
				|| BuilderFactory.BUILD_TARGET_AUTO.equals(name)) {
			return ATTRIBUTE_TARGET_AUTO;
		} else if (BUILD_TARGET_CLEAN.equals(name)
				|| BuilderFactory.BUILD_TARGET_CLEAN.equals(name)) {
			return ATTRIBUTE_TARGET_CLEAN;
		} else if (BUILD_LOCATION.equals(name)
				|| BuilderFactory.BUILD_LOCATION.equals(name)) {
			return ATTRIBUTE_BUILD_PATH;
		} else if (BUILD_COMMAND.equals(name)
				|| BuilderFactory.BUILD_COMMAND.equals(name)) {
			return COMMAND;
		} else if (BUILD_ARGUMENTS.equals(name)
				|| BuilderFactory.BUILD_ARGUMENTS.equals(name)) {
			return ARGUMENTS;
		} else if (BuilderFactory.STOP_ON_ERROR.equals(name)) {
			return ATTRIBUTE_STOP_ON_ERR;
		} //TODO else if(BuilderFactory.USE_DEFAULT_BUILD_CMD.equals(name)){
		//	return getCommand();
		//}
		else if (BuilderFactory.BUILD_INCREMENTAL_ENABLED.equals(name)
				||  BuilderFactory.BUILD_FULL_ENABLED.equals(name)) {
			return ATTRIBUTE_INCREMENTAL_ENABLED;
		} else if (BuilderFactory.BUILD_CLEAN_ENABLED.equals(name)){
			return ATTRIBUTE_CLEAN_ENABLED;
		} else if (BuilderFactory.BUILD_AUTO_ENABLED.equals(name)) {
			return ATTRIBUTE_AUTO_ENABLED;
		} else if (BuilderFactory.ENVIRONMENT.equals(name)) {
			return ATTRIBUTE_ENVIRONMENT;
		} else if (BuilderFactory.BUILD_APPEND_ENVIRONMENT.equals(name)){
			return ATTRIBUTE_APPEND_ENVIRONMENT;
		} else if (ErrorParserManager.PREF_ERROR_PARSER.equals(name)) {
			return ATTRIBUTE_CUSTOMIZED_ERROR_PARSERS;
		}
		return null;
	}
	

	public Map getEnvironment() {
		if(customizedEnvironment != null)
			return (HashMap)customizedEnvironment.clone();
		return null;
	}

	public Map getExpandedEnvironment() throws CoreException {
		if(customizedEnvironment != null){
			Map expanded = (HashMap)customizedEnvironment.clone();
			ICdtVariableManager mngr = CCorePlugin.getDefault().getCdtVariableManager();
			String separator = CCorePlugin.getDefault().getBuildEnvironmentManager().getDefaultDelimiter();
			ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(getParent().getParent());
			for(Iterator iter = expanded.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				String value = (String)entry.getValue();
				try {
					value = mngr.resolveValue(value, "", separator, cfgDes); //$NON-NLS-1$
					entry.setValue(value);
				} catch (CdtVariableException e){
				}
			}
			
			return expanded;
		}
		return null;
	}

	public void setAppendEnvironment(boolean append) throws CoreException {
		appendEnvironment = Boolean.valueOf(append);
	}

	public void setBuildAttribute(String name, String value)
			throws CoreException {
		if(BUILD_TARGET_INCREMENTAL.equals(name)){
			incrementalBuildTarget = value;
		} else if(BUILD_TARGET_AUTO.equals(name)){
			autoBuildTarget = value;
		} else if(BUILD_TARGET_CLEAN.equals(name)){
			cleanBuildTarget = value;
		} else if(BUILD_LOCATION.equals(name)){
			buildPath = value;
		} else if(BUILD_COMMAND.equals(name)){
			command = value;
		} else if(BUILD_ARGUMENTS.equals(name)){
			args = value;
		} else if(BuilderFactory.BUILD_COMMAND.equals(name)){
			command = value;
		} else if(BuilderFactory.BUILD_LOCATION.equals(name)){
			buildPath = value;
		} else if(BuilderFactory.STOP_ON_ERROR.equals(name)){
			stopOnErr = Boolean.valueOf(value);
		} else if(BuilderFactory.USE_DEFAULT_BUILD_CMD.equals(name)){
			if(value == null || Boolean.valueOf(value).booleanValue()){
				if(superClass != null)
					command = null;
			}
		} else if(BuilderFactory.BUILD_TARGET_AUTO.equals(name)){
			autoBuildTarget = value;
		} else if(BuilderFactory.BUILD_TARGET_INCREMENTAL.equals(name)){
			incrementalBuildTarget = value;
		} else if(BuilderFactory.BUILD_TARGET_FULL.equals(name)){
			autoBuildTarget = value;
		} else if(BuilderFactory.BUILD_TARGET_CLEAN.equals(name)){
			cleanBuildTarget = value;
		} else if(BuilderFactory.BUILD_FULL_ENABLED.equals(name)){
			autoBuildEnabled = value != null ?
					Boolean.valueOf(value) : null;
		} else if(BuilderFactory.BUILD_CLEAN_ENABLED.equals(name)){
			cleanBuildEnabled = value != null ?
					Boolean.valueOf(value) : null;	
		} else if(BuilderFactory.BUILD_INCREMENTAL_ENABLED.equals(name)){
			incrementalBuildEnabled = value != null ? 
					Boolean.valueOf(value) : null;
		} else if(BuilderFactory.BUILD_AUTO_ENABLED.equals(name)){
			autoBuildEnabled = value != null ?
					Boolean.valueOf(value) : null;
		} else if(BuilderFactory.BUILD_ARGUMENTS.equals(name)){
			args = value;
		} else if(BuilderFactory.ENVIRONMENT.equals(name)){
			if(value == null){
				customizedEnvironment = null;
			} else {
				customizedEnvironment = (HashMap)MapStorageElement.decodeMap(value);
			}
		} else if(BuilderFactory.BUILD_APPEND_ENVIRONMENT.equals(name)){
			appendEnvironment = value != null ?
					Boolean.valueOf(value) : null;
		} else {
			getCustomBuildPropertiesMap().put(name, value);
		}
	}
	
	private Map getCustomBuildPropertiesMap(){
		if(customBuildProperties == null){
			customBuildProperties = new HashMap();
		}
		return customBuildProperties;
	}

	public void setEnvironment(Map env) throws CoreException {
		customizedEnvironment = new HashMap(env);
	}

	public boolean isCustomBuilder() {
		if(!isExtensionBuilder && getParent().getBuilder() != this)
			return true;
		return false;
	}

	public IConfiguration getConfguration(){
		if(getParent() != null)
			return getParent().getParent();
		return null;
	}

	public boolean isManagedBuildOn() {
		IConfiguration cfg = getConfguration();
		if(cfg != null){
			if(!cfg.supportsBuild(true))
				return false;
			else if(!cfg.supportsBuild(false))
				return true;
		}
		
		Boolean attr = getManagedBuildOnAttribute();
		if(attr != null)
			return attr.booleanValue();
		return true;
	}
	
	public Boolean getManagedBuildOnAttribute(){
		if(managedBuildOn == null){
			if(superClass != null)
				return ((Builder)superClass).getManagedBuildOnAttribute();
			return null;
		}
		return managedBuildOn;
	}

	public void setManagedBuildOn(boolean on) throws CoreException {
		managedBuildOn = Boolean.valueOf(on);
	}

	public boolean canKeepEnvironmentVariablesInBuildfile() {
		return BuildMacroProvider.canKeepMacrosInBuildfile(this);
	}

	public boolean keepEnvironmentVariablesInBuildfile() {
		if(keepEnvVarInBuildfile == null){
			if(superClass != null)
				return getSuperClass().keepEnvironmentVariablesInBuildfile();
			return false;
		}
		return keepEnvVarInBuildfile.booleanValue();
	}

	public void setKeepEnvironmentVariablesInBuildfile(boolean keep) {
		keepEnvVarInBuildfile = Boolean.valueOf(keep);
	}

	public boolean supportsCustomizedBuild() {
		if(fSupportsCustomizedBuild == null){
			IManagedBuilderMakefileGenerator makeGen = getBuildFileGenerator();
			if(makeGen instanceof IManagedBuilderMakefileGenerator2)
				fSupportsCustomizedBuild = Boolean.valueOf(true);
			else
				fSupportsCustomizedBuild = Boolean.valueOf(false);
		}
		return fSupportsCustomizedBuild.booleanValue();
	}
	
	public boolean supportsBuild(boolean managed) {
		if(supportsManagedBuild == null){
			if(superClass != null)
				return getSuperClass().supportsBuild(managed);
			return managed || !isInternalBuilder();
		}
		return supportsManagedBuild.booleanValue();
	}
	
	public void setParent(IToolChain toolChain){
		parent = toolChain;
	}

	public boolean matches(IBuilder builder){
		if(builder == this)
			return true;
		
		IBuilder rBld = ManagedBuildManager.getRealBuilder(this);
		if(rBld == null)
			return false;
		
		return rBld == ManagedBuildManager.getRealBuilder(builder);
	}

	public boolean performMatchComparison(IBuilder builder){
		if(builder == null)
			return false;
		
		if(builder == this)
			return true;
		
//		if(tool.isReal() && isReal())
//			return false;
//		if(!tool.getToolCommand().equals(getToolCommand()))
//			return false;
		
		if(!builder.getName().equals(getName()))
			return false;

		String thisVersion = ManagedBuildManager.getVersionFromIdAndVersion(getId());
		String otherVersion = ManagedBuildManager.getVersionFromIdAndVersion(builder.getId());
		if(thisVersion == null || thisVersion.length() == 0){
			if(otherVersion != null && otherVersion.length() != 0)
				return false;
		} else {
			if(!thisVersion.equals(otherVersion))
				return false;
		}

		return true;
	}
	
	private class MatchKey {
		Builder builder;
		
		public MatchKey(Builder builder) {
			this.builder = builder;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			if(!(obj instanceof MatchKey))
				return false;
			MatchKey other = (MatchKey)obj;
			return builder.performMatchComparison(other.builder);
		}

		@Override
		public int hashCode() {
			String name = getName();
			if(name == null)
				name = getId();
			int code = name.hashCode();
			String version = ManagedBuildManager.getVersionFromIdAndVersion(getId());
			if(version != null)
				code += version.hashCode();
			return code;
		}
		
	}

	public Object getMatchKey() {
		if(isAbstract())
			return null;
		if(!isExtensionBuilder)
			return null;
		return new MatchKey(this);
	}

	public void setIdenticalList(List list) {
		identicalList = list;
	}

	public String getNameAndVersion(){
		String name = getName();
		String version = ManagedBuildManager.getVersionFromIdAndVersion(getId());
		if(version != null && version.length() != 0){
			return new StringBuffer().append(name).append(" (").append(version).append("").toString(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return name;
	}

	public List getIdenticalList() {
		return identicalList;
	}

	public boolean isInternalBuilder() {
		IBuilder internalBuilder = ManagedBuildManager.getInternalBuilder();
		for(IBuilder builder = this; builder != null; builder = builder.getSuperClass()){
			if(internalBuilder == builder)
				return true;
		}
		return false;
	}

	public int getParallelizationNum() {
		if(supportsParallelBuild())
			return getParallelizationNumAttribute();
		return 1;
	}
	
	public int getParallelizationNumAttribute(){
		if(parallelNum == null){
			if(superClass != null){
				return ((Builder)superClass).getParallelizationNumAttribute();
			}
			return 1;
		}
		return parallelNum.intValue();
	}

	public void setParallelizationNum(int num) throws CoreException {
//		if(num == 0 || supportsParallelBuild()){
			Integer newParallelNum = new Integer(num);
			String curCmd = getParallelizationCmd(getParallelizationNum());
			String args = getArgumentsAttribute();
			String updatedArgs = removeCmd(args, curCmd);
			if(!updatedArgs.equals(args)){
				setArgumentsAttribute(updatedArgs);
			}
			parallelNum = newParallelNum;
			setDirty(true);
//		}
	}

	public boolean supportsParallelBuild() {
		if(isInternalBuilder())
			return true;
		return getParrallelBuildCmd().length() != 0;
	}

	public boolean supportsStopOnError(boolean on) {
		if(isInternalBuilder())
			return true;
		
		if(!on)
			return getIgnoreErrCmdAttribute().length() != 0;
		return true;
	}
	
	public String getStopOnErrCmd(boolean stop){
		if(!stop)
			return getIgnoreErrCmdAttribute();
		return EMPTY_STRING;
	}
	
	public String getIgnoreErrCmdAttribute(){
		if(ignoreErrCmd == null){
			if(superClass != null){
				return ((Builder)superClass).getIgnoreErrCmdAttribute();
			}
			return EMPTY_STRING;
		}
		return ignoreErrCmd;
	}

	public String getParrallelBuildCmd(){
		if(parallelBuildCmd == null){
			if(superClass != null){
				return ((Builder)superClass).getParrallelBuildCmd();
			}
			return EMPTY_STRING;
		}
		return parallelBuildCmd;
	}

	public boolean isParallelBuildOn() {
		if(parallelBuildOn == null){
			if(superClass != null){
				return getSuperClass().isParallelBuildOn();
			}
			return false;
		}
		return parallelBuildOn.booleanValue();
	}

	public void setParallelBuildOn(boolean on) throws CoreException{
		if(isParallelBuildOn() == on)
			return;
		if(on && !supportsParallelBuild())
			return;
		
		String curCmd = getParallelizationCmd(getParallelizationNum());
		String args = getArgumentsAttribute();
		String updatedArgs = removeCmd(args, curCmd);
		if(!updatedArgs.equals(args)){
			setArgumentsAttribute(updatedArgs);
		}
		parallelBuildOn = Boolean.valueOf(on);
		setDirty(true);
	}
	
	public Set<String> contributeErrorParsers(Set<String> set){
		if(getErrorParserIds() != null){
			if(set == null)
				set = new HashSet<String>();
			
			String ids[] = getErrorParserList();
			if(ids.length != 0)
				set.addAll(Arrays.asList(ids));
		}
		return set;
	}

	public void resetErrorParsers(){
		errorParserIds = null;
	}
	
	void removeErrorParsers(Set set){
		Set oldSet = contributeErrorParsers(null);
		if(oldSet == null)
			oldSet = new HashSet();
		oldSet.removeAll(set);
		setErrorParserList((String[])oldSet.toArray(new String[oldSet.size()]));
	}
	
	public void setErrorParserList(String[] ids) {
		if(ids == null){
			errorParserIds = null;
		} else if(ids.length == 0){
			errorParserIds = EMPTY_STRING;
		} else {
			StringBuffer buf = new StringBuffer();
			buf.append(ids[0]);
			for(int i = 1; i < ids.length; i++){
				buf.append(";").append(ids[i]); //$NON-NLS-1$
			}
			errorParserIds = buf.toString();
		}
	}
	
	public boolean isSystemObject() {
		if(isTest)
			return true;
		
		if(getConvertToId().length() != 0)
			return true;
		
		if(getParent() != null)
			return getParent().isSystemObject();
		return false;
	}

	public String getUniqueRealName() {
		String name = getName();
		if(name == null){
			name = getId();
		} else {
			String version = ManagedBuildManager.getVersionFromIdAndVersion(getId());
			if(version != null){
			StringBuffer buf = new StringBuffer();
			buf.append(name);
			buf.append(" (v").append(version).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			name = buf.toString();
			}
		}
		return name;
	}
	
	public ICOutputEntry[] getOutputEntries(){
		if(isManagedBuildOn()){
			return getDefaultOutputSettings();
		}
		ICOutputEntry[] entries = getOutputEntrySettings();
		if(entries == null || entries.length == 0){
			entries = getDefaultOutputSettings();
		}
		return entries;
	}
	
	private ICOutputEntry[] getDefaultOutputSettings(){
		Configuration cfg = (Configuration)getConfguration();
		if(cfg == null || cfg.isPreference() || cfg.isExtensionElement()){
			return new ICOutputEntry[]{new COutputEntry(Path.EMPTY, null, ICLanguageSettingEntry.VALUE_WORKSPACE_PATH | ICLanguageSettingEntry.RESOLVED)};
		}
		
		IPath path = ManagedBuildManager.getBuildFullPath(cfg, this);
		IProject proj = cfg.getOwner().getProject();
		IPath projFullPath = proj.getFullPath(); 
		if(path != null && projFullPath.isPrefixOf(path)){
			path = path.removeFirstSegments(projFullPath.segmentCount()).makeRelative();
		} else {
			path = Path.EMPTY;
		}
			
		return new ICOutputEntry[]{new COutputEntry(path, null, ICLanguageSettingEntry.VALUE_WORKSPACE_PATH | ICLanguageSettingEntry.RESOLVED)};
	}

	public ICOutputEntry[] getOutputEntrySettings(){
		if(outputEntries == null){
			if(superClass != null){
				return ((Builder)superClass).getOutputEntrySettings();
			}
			return null;
				
		}
		return outputEntries.clone();
	}

	public void setOutputEntries(ICOutputEntry[] entries){
		if(entries != null)
			outputEntries = entries.clone();
		else
			outputEntries = null;
	}
	
	private int getSuperClassNum(){
		int num = 0;
		for(IBuilder superTool = getSuperClass(); superTool != null; superTool = superTool.getSuperClass()){
			num++;
		}
		return num;
	}

	public int compareTo(Object o) {
		Builder other = (Builder)o;
		if(other.isSystemObject() != isSystemObject())
			return isSystemObject() ? 1 : -1;
		
		return getSuperClassNum() - other.getSuperClassNum();
	}

	public IRealBuildObjectAssociation getExtensionObject() {
		return (Builder)ManagedBuildManager.getExtensionBuilder(this);
	}

	public IRealBuildObjectAssociation[] getIdenticBuildObjects() {
		return (IRealBuildObjectAssociation[])ManagedBuildManager.findIdenticalBuilders(this);
	}

	public IRealBuildObjectAssociation getRealBuildObject() {
		return (Builder)ManagedBuildManager.getRealBuilder(this);
	}

	public IRealBuildObjectAssociation getSuperClassObject() {
		return (Builder)getSuperClass();
	}

	public final int getType() {
		return OBJECT_BUILDER;
	}

	public boolean isRealBuildObject() {
		return ManagedBuildManager.getRealBuilder(this) == this;
	}

	public boolean isExtensionBuildObject() {
		return isExtensionElement();
	}
	@Override
	public String toString() {
		return getUniqueRealName();
	}

	public ICommandLauncher getCommandLauncher() {
		if(fCommandLauncher != null)
			return fCommandLauncher;
		
		if(fCommandLauncher == null && fCommandLauncherElement != null){
			try{
				fCommandLauncher = (ICommandLauncher)fCommandLauncherElement.createExecutableExtension(ATTRIBUTE_COMMAND_LAUNCHER);
				return fCommandLauncher;
			}catch(CoreException e){
				e.printStackTrace();
			}
		}
		if(fCommandLauncher == null && superClass != null)
			return getSuperClass().getCommandLauncher();
		
		else if(fCommandLauncher == null) // catch all for backwards compatibility
			fCommandLauncher = new CommandLauncher();
		
		return fCommandLauncher;
	}

	public IBuildRunner getBuildRunner() throws CoreException {
		// Already defined
		if (fBuildRunner != null)
			return fBuildRunner;
		
		// Instantiate from model
		if (fBuildRunnerElement != null) {
			fBuildRunner = (IBuildRunner)fBuildRunnerElement.createExecutableExtension(ATTRIBUTE_BUILD_RUNNER);
			return fBuildRunner;
		}
		
		// Check with superClass
		if (superClass != null)
			return getSuperClass().getBuildRunner();
		
		// Default internal or external builder
		if (isInternalBuilder())
			return new InternalBuildRunner();
		
		return new ExternalBuildRunner();
	}

	@Override
	protected IResourceInfo getParentResourceInfo() {
		// There are no resources associated with builders
		return null;
	}
}
