/*******************************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
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
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextBuildMacroValues;
import org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues;
import org.eclipse.cdt.managedbuilder.macros.IReservedMacroNameSupplier;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Builder extends BuildObject implements IBuilder {

	private static final String EMPTY_STRING = new String();

	//  Superclass
	private IBuilder superClass;
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
	
	//  Miscellaneous
	private boolean isExtensionBuilder = false;
	private boolean isDirty = false;
	private boolean resolved = true;

	private IConfigurationElement previousMbsVersionConversionElement = null;
	private IConfigurationElement currentMbsVersionConversionElement = null;
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
	 * @param ToolChain The parent of the builder, if any
	 * @param Builder The superClass, if any
	 * @param String The id for the new Builder
	 * @param String The name for the new Builder
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	public Builder(ToolChain parent, IBuilder superClass, String Id, String name, boolean isExtensionElement) {
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
	public Builder(IToolChain parent, Element element, String managedBuildRevision) {
		this.parent = parent;
		isExtensionBuilder = false;
		
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
		this.parent = parent;
		superClass = builder.superClass;
		if (superClass != null) {
			if (builder.superClassId != null) {
				superClassId = new String(builder.superClassId);
			}
		}
		setId(Id);
		setName(name);
		
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
		buildFileGeneratorElement = builder.buildFileGeneratorElement; 
		
		if(builder.fileContextBuildMacroValues != null){
			fileContextBuildMacroValues = (FileContextBuildMacroValues)builder.fileContextBuildMacroValues.clone();
			fileContextBuildMacroValues.setBuilder(this);
		}
		
		builderVariablePattern = builder.builderVariablePattern;
		
		if(builder.isVariableCaseSensitive != null)
			isVariableCaseSensitive = new Boolean(builder.isVariableCaseSensitive.booleanValue());

		if(builder.reservedMacroNames != null)
			reservedMacroNames = (String[])builder.reservedMacroNames.clone();

		reservedMacroNameSupplierElement = builder.reservedMacroNameSupplierElement;
		reservedMacroNameSupplier = builder.reservedMacroNameSupplier;

		setDirty(true);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
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
        
		// Get the semicolon separated list of IDs of the error parsers
		errorParserIds = element.getAttribute(IToolChain.ERROR_PARSERS);
		
		// Store the configuration element IFF there is a build file generator defined 
		String buildfileGenerator = element.getAttribute(BUILDFILEGEN_ID); 
		if (buildfileGenerator != null && element instanceof DefaultManagedConfigElement) {
			buildFileGeneratorElement = ((DefaultManagedConfigElement)element).getConfigurationElement();			
		}
		
		//load the File Context Build Macro Values
		fileContextBuildMacroValues = new FileContextBuildMacroValues(this,element);
	}
	
	/* (non-Javadoc)
	 * Initialize the builder information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the builder information 
	 */
	protected void loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.hasAttribute(IBuildObject.NAME)) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// Set the version after extracting from 'id' attribute
		setVersion(getVersionFromId());

		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		if (superClassId != null && superClassId.length() > 0) {
			superClass = ManagedBuildManager.getExtensionBuilder(superClassId);
			// Check for migration support
			checkForMigrationSupport();
		}

		// Get the 'versionSupported' attribute
		if (element.hasAttribute(VERSIONS_SUPPORTED)) {
			versionsSupported = element.getAttribute(VERSIONS_SUPPORTED);
		}
		
		// Get the 'convertToId' id
		if (element.hasAttribute(CONVERT_TO_ID)) {
			convertToId = element.getAttribute(CONVERT_TO_ID);
		}
		
		// Get the unused children, if any
		if (element.hasAttribute(IProjectType.UNUSED_CHILDREN)) {
				unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		}
		
		// isAbstract
		if (element.hasAttribute(IProjectType.IS_ABSTRACT)) {
			String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
			if (isAbs != null){
				isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
			}
		}

        // command
		if (element.hasAttribute(IBuilder.COMMAND)) {
			command = element.getAttribute(IBuilder.COMMAND); 
		}
        
        // arguments
		if (element.hasAttribute(IBuilder.ARGUMENTS)) {
			args = element.getAttribute(IBuilder.ARGUMENTS);
		}
		
		// Get the semicolon separated list of IDs of the error parsers
		if (element.hasAttribute(IToolChain.ERROR_PARSERS)) {
			errorParserIds = element.getAttribute(IToolChain.ERROR_PARSERS);
		}
		
		// Note: build file generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (element.hasAttribute(IBuilder.BUILDFILEGEN_ID)) {
			// TODO:  Issue warning?
		}
	}

	/**
	 * Persist the builder to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
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

		// Note: build file generator cannot be specified in a project file because
		//       an IConfigurationElement is needed to load it!
		if (buildFileGeneratorElement != null) {
			//  TODO:  issue warning?
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getParent()
	 */
	public IToolChain getParent() {
		return parent;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getSuperClass()
	 */
	public IBuilder getSuperClass() {
		return superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getName()
	 */
	public String getName() {
		return (name == null && superClass != null) ? superClass.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#isAbstract()
	 */
	public boolean isAbstract() {
		if (isAbstract != null) {
			return isAbstract.booleanValue();
		} else {
			return false;	// Note: no inheritance from superClass
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getUnusedChildren()
	 */
	public String getUnusedChildren() {
		if (unusedChildren != null) {
			return unusedChildren;
		} else
			return EMPTY_STRING;	// Note: no inheritance from superClass
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getCommand()
	 */
	public String getCommand() {
		if (command == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getCommand();
			} else {
				return new String("make"); //$NON-NLS-1$
			}
		}
		return command;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#getArguments()
	 */
	public String getArguments() {
		if (args == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getArguments();
			} else {
				return new String("-k"); //$NON-NLS-1$
			}
		}
		return args;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getErrorParserIds()
	 */
	public String getErrorParserIds() {
		String ids = errorParserIds;
		if (ids == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				ids = superClass.getErrorParserIds();
			}
		}
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getErrorParserList()
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
			errorParsers = new String[0];
		}
		return errorParsers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder.setCommand(String)
	 */
	public void setCommand(String cmd) {
		if (cmd == null && command == null) return;
		if (command == null || cmd == null || !cmd.equals(command)) {
			command = cmd;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuilder#setArguments(String)
	 */
	public void setArguments(String newArgs) {
		if (newArgs == null && args == null) return;
		if (args == null || newArgs == null || !newArgs.equals(args)) {
			args = newArgs;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#setErrorParserIds()
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
	 * Sets the isAbstract attribute
	 */
	public void setIsAbstract(boolean b) {
		isAbstract = new Boolean(b);
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getBuildFileGeneratorElement()
	 */
	public IConfigurationElement getBuildFileGeneratorElement() {
		if (buildFileGeneratorElement == null) {
			if (superClass != null) {
				return ((Builder)superClass).getBuildFileGeneratorElement();
			}
		}
		return buildFileGeneratorElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getBuildFileGenerator()
	 */
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

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#setBuildFileGeneratorElement(String)
	 */
	public void setBuildFileGeneratorElement(IConfigurationElement element) {
		buildFileGeneratorElement = element;
		setDirty(true);
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionBuilder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension Builder
 		if (isExtensionBuilder) return false;
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Resolve superClass
			if (superClassId != null && superClassId.length() > 0) {
				superClass = ManagedBuildManager.getExtensionBuilder(superClassId);
				if (superClass == null) {
					// Report error
					ManagedBuildManager.OutputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"builder",	//$NON-NLS-1$
							getId());
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getConvertToId()
	 */
	public String getConvertToId() {
		if (convertToId == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getConvertToId();
			} else {
				return EMPTY_STRING;
			}
		}
		return convertToId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#setConvertToId(String)
	 */
	public void setConvertToId(String convertToId) {
		if (convertToId == null && this.convertToId == null) return;
		if (convertToId == null || this.convertToId == null || !convertToId.equals(this.convertToId)) {
			this.convertToId = convertToId;
			setDirty(true);
		}
		return;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getVersionsSupported()
	 */
	public String getVersionsSupported() {
		if (versionsSupported == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getVersionsSupported();
			} else {
				return EMPTY_STRING;
			}
		}
		return versionsSupported;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#setVersionsSupported(String)
	 */
	
	public void setVersionsSupported(String versionsSupported) {
		if (versionsSupported == null && this.versionsSupported == null) return;
		if (versionsSupported == null || this.versionsSupported == null || !versionsSupported.equals(this.versionsSupported)) {
			this.versionsSupported = versionsSupported;
			setDirty(true);
		}
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getFileContextBuildMacroValues()
	 */
	public IFileContextBuildMacroValues getFileContextBuildMacroValues(){
		if(fileContextBuildMacroValues == null && superClass != null)
			return superClass.getFileContextBuildMacroValues();
		return fileContextBuildMacroValues;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getBuilderVariablePattern()
	 */
	public String getBuilderVariablePattern(){
		if(builderVariablePattern == null && superClass != null)
			return superClass.getBuilderVariablePattern();
		return builderVariablePattern;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#isVariableCaseSensitive()
	 */
	public boolean isVariableCaseSensitive(){
		if(isVariableCaseSensitive == null){
			if(superClass != null)
				return superClass.isVariableCaseSensitive();
			return true;
		}
		return isVariableCaseSensitive.booleanValue();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getReservedMacroNames()
	 */
	public String[] getReservedMacroNames(){
		if(reservedMacroNames == null && superClass != null)
			return superClass.getReservedMacroNames();
		return reservedMacroNames;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuilder#getReservedMacroNameSupplier()
	 */
	public IReservedMacroNameSupplier getReservedMacroNameSupplier(){
		if(reservedMacroNameSupplier == null && reservedMacroNameSupplierElement != null){
			try{
				reservedMacroNameSupplier = (IReservedMacroNameSupplier)reservedMacroNameSupplierElement.createExecutableExtension(RESERVED_MACRO_NAME_SUPPLIER);
			}catch(CoreException e){
			}
		}
		if(reservedMacroNameSupplier == null && superClass != null)
			return superClass.getReservedMacroNameSupplier();
		return reservedMacroNameSupplier;
	}
	
	/*
	 * This function checks for migration support for the builder, while
	 * loading. If migration support is needed, looks for the available
	 * converters and stores them.
	 */

	public void checkForMigrationSupport() {

		String tmpId = null;
		boolean isExists = false;

		if (getSuperClass() == null) {
			// If 'superClass' is null, then there is no builder available in
			// plugin manifest file with the same 'id' & version.
			// Look for the 'versionsSupported' attribute
			String high = (String) ManagedBuildManager
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

			IBuilder[] builderElements = (IBuilder[]) subMap.values()
					.toArray();
			
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
							if (new PluginVersionIdentifier(version)
									.equals(new PluginVersionIdentifier(
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
							PluginVersionIdentifier currentMbsVersion = ManagedBuildManager
									.getBuildInfoVersion();

							// set the converter element based on the MbsVersion
							if (currentMbsVersion
									.isGreaterThan(new PluginVersionIdentifier(
											mbsVersion))) {
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
}
