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
package org.eclipse.cdt.managedbuilder.internal.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildfileMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeSettings;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a tool that can be invoked during a build.
 * Note that this class implements IOptionCategory to represent the top
 * category.
 */
public class Tool extends HoldsOptions implements ITool, IOptionCategory {

	public static final String DEFAULT_PATTERN = "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}${OUTPUT} ${INPUTS}"; //$NON-NLS-1$
	public static final String DEFAULT_CBS_PATTERN = "${COMMAND}"; //$NON-NLS-1$

	private static final String DEFAULT_SEPARATOR = ","; //$NON-NLS-1$
	//private static final IOptionCategory[] EMPTY_CATEGORIES = new IOptionCategory[0];
	//private static final IOption[] EMPTY_OPTIONS = new IOption[0];
	private static final String EMPTY_STRING = new String();
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String DEFAULT_ANNOUNCEMENT_PREFIX = "Tool.default.announcement";	//$NON-NLS-1$
	private static final String WHITESPACE = " ";	//$NON-NLS-1$

	private static final boolean resolvedDefault = true;
	
	//  Superclass
	//  Note that superClass itself is defined in the base and that the methods
	//  getSuperClass() and setSuperClass(), defined in Tool must be used to 
	//  access it. This avoids widespread casts from IHoldsOptions to ITool.
	private String superClassId;
	//  Parent and children
	private IBuildObject parent;
	private Vector inputTypeList;
	private Map inputTypeMap;
	private Vector outputTypeList;
	private Map outputTypeMap;
	private List envVarBuildPathList;
	//  Managed Build model attributes
	private String unusedChildren;
	private Boolean isAbstract;
	private String command;
	private List inputExtensions;
	private List interfaceExtensions;
	private Integer natureFilter;
	private String outputExtensions;
	private String outputFlag;
	private String outputPrefix;
	private String errorParserIds;
	private String commandLinePattern;
	private String versionsSupported;
	private String convertToId;
	private Boolean advancedInputCategory;
	private Boolean customBuildStep;
	private String announcement;
	private IConfigurationElement commandLineGeneratorElement = null;
	private IManagedCommandLineGenerator commandLineGenerator = null;
	private IConfigurationElement dependencyGeneratorElement = null;
	private IManagedDependencyGenerator dependencyGenerator = null;
	private URL iconPathURL;	
	//  Miscellaneous
	private boolean isExtensionTool = false;
	private boolean isDirty = false;
	private boolean resolved = resolvedDefault;
	private IConfigurationElement previousMbsVersionConversionElement = null;
	private IConfigurationElement currentMbsVersionConversionElement = null;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * Constructor to create a tool based on an element from the plugin
	 * manifest. 
	 * 
	 * @param element The element containing the information about the tool.
	 * @param managedBuildRevision the fileVersion of Managed Build System
	 */
	public Tool(IManagedConfigElement element, String managedBuildRevision) {
		// setup for resolving
		super(false);
		resolved = false;

		isExtensionTool = true;
		
		// Set the managedBuildRevision
		setManagedBuildRevision(managedBuildRevision);
		
		loadFromManifest(element);

		// hook me up
		ManagedBuildManager.addExtensionTool(this);

		// set up the category map
		addOptionCategory(this);

		// Load children
		IManagedConfigElement[] toolElements = element.getChildren();
		for (int l = 0; l < toolElements.length; ++l) {
			IManagedConfigElement toolElement = toolElements[l];
			if (loadChild(toolElement)) {
				// do nothing
			} else if (toolElement.getName().equals(ITool.INPUT_TYPE)) {
				InputType inputType = new InputType(this, toolElement);
				addInputType(inputType);
			} else if (toolElement.getName().equals(ITool.OUTPUT_TYPE)) {
				OutputType outputType = new OutputType(this, toolElement);
				addOutputType(outputType);
			} else if (toolElement.getName().equals(IEnvVarBuildPath.BUILD_PATH_ELEMENT_NAME)){
				addEnvVarBuildPath(new EnvVarBuildPath(this,toolElement));
			}
		}
	}
	
	/**
	 * Constructor to create a new tool for a tool-chain based on the information
	 * defined in the plugin.xml manifest. 
	 * 
	 * @param parent  The parent of this tool.  This can be a ToolChain or a
	 *                ResourceConfiguration.
	 * @param element The element containing the information about the tool.
	 * @param managedBuildRevision the fileVersion of Managed Build System
	 */
	public Tool(IBuildObject parent, IManagedConfigElement element, String managedBuildRevision) {
		this(element, managedBuildRevision);
		this.parent = parent;
	}

	/**
	 * This constructor is called to create a Tool whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param ToolChain The parent of the tool, if any
	 * @param Tool The superClass, if any
	 * @param String The id for the new tool 
	 * @param String The name for the new tool
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	public Tool(ToolChain parent, ITool superClass, String Id, String name, boolean isExtensionElement) {
		super(resolvedDefault);
		this.parent = parent;
		setSuperClass(superClass);
		setManagedBuildRevision(parent.getManagedBuildRevision());
		if (getSuperClass() != null) {
			superClassId = getSuperClass().getId();
		}
		
		setId(Id);
		setName(name);
		setVersion(getVersionFromId());
		
		isExtensionTool = isExtensionElement;
		if (isExtensionElement) {
			// Hook me up to the Managed Build Manager
			ManagedBuildManager.addExtensionTool(this);
		} else {
			setDirty(true);
		}
	}

	/**
	 * This constructor is called to create a Tool whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param ResourceConfiguration, The parent of the tool, if any
	 * @param Tool The superClass, if any
	 * @param String The id for the new tool 
	 * @param String The name for the new tool
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	 
	public Tool(ResourceConfiguration parent, ITool superClass, String Id, String name, boolean isExtensionElement) {
		super(resolvedDefault);
		this.parent = parent;
		setSuperClass( superClass );
		setManagedBuildRevision(parent.getManagedBuildRevision());
		if (getSuperClass() != null) {
			superClassId = getSuperClass().getId();
		}
		setId(Id);
		setName(name);
		setVersion(getVersionFromId());
		
		isExtensionTool = isExtensionElement;
		if (isExtensionElement) {
			// Hook me up to the Managed Build Manager
			ManagedBuildManager.addExtensionTool(this);
		} else {
			setDirty(true);
		}
	}

	/**
	 * Create a <code>Tool</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>IToolChain</code> or <code>IResourceConfiguration</code>
	 *               the tool will be added to. 
	 * @param element The XML element that contains the tool settings.
	 * @param managedBuildRevision the fileVersion of Managed Build System
	 */
	public Tool(IBuildObject parent, Element element, String managedBuildRevision) {
		super(resolvedDefault);
		this.parent = parent;
		isExtensionTool = false;
		
		// Set the managedBuildRevsion
		setManagedBuildRevision(managedBuildRevision);
		
		// Initialize from the XML attributes
		loadFromProject(element);

		// set up the category map
		addOptionCategory(this);

		// Load children
		NodeList toolElements = element.getChildNodes();
		for (int i = 0; i < toolElements.getLength(); ++i) {
			Node toolElement = toolElements.item(i);
			if (loadChild(toolElement)) {
				// do nothing
			} else if (toolElement.getNodeName().equals(ITool.INPUT_TYPE)) {
				InputType inputType = new InputType(this, (Element)toolElement);
				addInputType(inputType);
			} else if (toolElement.getNodeName().equals(ITool.OUTPUT_TYPE)) {
				OutputType outputType = new OutputType(this, (Element)toolElement);
				addOutputType(outputType);
			}
		}
	}

	/**
	 * Create a <code>Tool</code> based upon an existing tool.
	 * 
	 * @param parent The <code>IToolChain</code> or <code>IResourceConfiguration</code>
	 *               the tool will be added to. 
	 * @param tool The existing tool to clone.
	 */
	public Tool(IBuildObject parent, ITool toolSuperClass, String Id, String name, Tool tool){
		super(resolvedDefault);
		this.parent = parent;
		if (toolSuperClass != null) {
			setSuperClass( toolSuperClass );
		} else {
			setSuperClass( tool.getSuperClass() );
		}
		if (getSuperClass() != null) {
			superClassId = getSuperClass().getId();
		}
		setId(Id);
		setName(name);
		
		// Set the managedBuildRevision & the version
		setManagedBuildRevision(tool.getManagedBuildRevision());
		setVersion(getVersionFromId());
		
		isExtensionTool = false;
		
		//  Copy the remaining attributes
		if(tool.versionsSupported != null) {
			versionsSupported = new String(tool.versionsSupported);
		}
		if(tool.convertToId != null) {
			convertToId = new String(tool.convertToId);
		}
		if (tool.unusedChildren != null) {
			unusedChildren = new String(tool.unusedChildren);
		}
		if (tool.errorParserIds != null) {
			errorParserIds = new String(tool.errorParserIds);
		}
		if (tool.isAbstract != null) {
			isAbstract = new Boolean(tool.isAbstract.booleanValue());
		}
		if (tool.command != null) {
			command = new String(tool.command);
		}
		if (tool.inputExtensions != null) {
			inputExtensions = new ArrayList(tool.inputExtensions);
		}
		if (tool.interfaceExtensions != null) {
			interfaceExtensions = new ArrayList(tool.interfaceExtensions);
		}
		if (tool.natureFilter != null) {
			natureFilter = new Integer(tool.natureFilter.intValue());
		}
		if (tool.outputExtensions != null) {
			outputExtensions = new String(tool.outputExtensions);
		}
		if (tool.outputFlag != null) {
			outputFlag = new String(tool.outputFlag);
		}
		if (tool.outputPrefix != null) {
			outputPrefix = new String(tool.outputPrefix);
		}
		if (tool.advancedInputCategory != null) {
			advancedInputCategory = new Boolean(tool.advancedInputCategory.booleanValue());
		}
		if (tool.customBuildStep != null) {
			customBuildStep = new Boolean(tool.customBuildStep.booleanValue());
		}
		if (tool.announcement != null) {
			announcement = new String(tool.announcement);
		}

		commandLineGeneratorElement = tool.commandLineGeneratorElement; 
		commandLineGenerator = tool.commandLineGenerator; 
		dependencyGeneratorElement = tool.dependencyGeneratorElement; 
		dependencyGenerator = tool.dependencyGenerator; 

		if(tool.envVarBuildPathList != null)
			envVarBuildPathList = new ArrayList(tool.envVarBuildPathList);
		
		//  Clone the children in superclass
		super.copyChildren(tool);
		//  Clone the children
		if (tool.inputTypeList != null) {
			Iterator iter = tool.getInputTypeList().listIterator();
			while (iter.hasNext()) {
				InputType inputType = (InputType) iter.next();
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId;
				String subName;
				if (inputType.getSuperClass() != null) {
					subId = inputType.getSuperClass().getId() + "." + nnn;		//$NON-NLS-1$
					subName = inputType.getSuperClass().getName();
				} else {
					subId = inputType.getId() + "." + nnn;		//$NON-NLS-1$
					subName = inputType.getName();
				}
				InputType newInputType = new InputType(this, subId, subName, inputType);
				addInputType(newInputType);
			}
		}
		if (tool.outputTypeList != null) {
			Iterator iter = tool.getOutputTypeList().listIterator();
			while (iter.hasNext()) {
				OutputType outputType = (OutputType) iter.next();
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId;
				String subName;
				if (outputType.getSuperClass() != null) {
					subId = outputType.getSuperClass().getId() + "." + nnn;		//$NON-NLS-1$
					subName = outputType.getSuperClass().getName();
				} else {
					subId = outputType.getId() + "." + nnn;		//$NON-NLS-1$
					subName = outputType.getName();
				}
				OutputType newOutputType = new OutputType(this, subId, subName, outputType);
				addOutputType(newOutputType);
			}
		}

		// icon
		if ( tool.iconPathURL != null ) {
			iconPathURL = tool.iconPathURL;
		}	
		
        setDirty(true);
	}
	
	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */

	/* (non-Javadoc)
	 * Load the tool information from the XML element specified in the 
	 * argument
	 * @param element An XML element containing the tool information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		
		// id		
		setId(element.getAttribute(ITool.ID));
		
		// name
		setName(element.getAttribute(ITool.NAME));

		// version
		setVersion(getVersionFromId());
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);

		// Get the unused children, if any
		unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		
		// Get the 'versionsSupported' attribute
		versionsSupported =element.getAttribute(VERSIONS_SUPPORTED);
		
		// Get the 'convertToId' attribute
		convertToId = element.getAttribute(CONVERT_TO_ID);

		// isAbstract
        String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
        if (isAbs != null){
    		isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
        }
		
		// Get the semicolon separated list of IDs of the error parsers
		errorParserIds = element.getAttribute(IToolChain.ERROR_PARSERS);
		
		// Get the nature filter
		String nature = element.getAttribute(NATURE);
		if (nature != null) {
			if ("both".equals(nature)) {	//$NON-NLS-1$
				natureFilter = new Integer(FILTER_BOTH);
			} else if ("cnature".equals(nature)) {	//$NON-NLS-1$
				natureFilter = new Integer(FILTER_C);
			} else if ("ccnature".equals(nature)) {	//$NON-NLS-1$
				natureFilter = new Integer(FILTER_CC);
			} else {
				natureFilter = new Integer(FILTER_BOTH);
			}
		}
		
		// Get the supported input file extensions
		String inputs = element.getAttribute(ITool.SOURCES);
		if (inputs != null) {
			StringTokenizer tokenizer = new StringTokenizer(inputs, DEFAULT_SEPARATOR);
			while (tokenizer.hasMoreElements()) {
				getInputExtensionsList().add(tokenizer.nextElement());
			}
		}
		
		// Get the interface (header file) extensions
		String headers = element.getAttribute(INTERFACE_EXTS);
		if (headers != null) {
			StringTokenizer tokenizer = new StringTokenizer(headers, DEFAULT_SEPARATOR);
			while (tokenizer.hasMoreElements()) {
				getInterfaceExtensionsList().add(tokenizer.nextElement());
			}
		}
		
		// Get the output extension
		outputExtensions = element.getAttribute(ITool.OUTPUTS); 
			
		// Get the tool invocation command
		command = element.getAttribute(ITool.COMMAND); 
			
		// Get the flag to control output
		outputFlag = element.getAttribute(ITool.OUTPUT_FLAG);
			
		// Get the output prefix
		outputPrefix = element.getAttribute(ITool.OUTPUT_PREFIX);
		
		// Get command line pattern
		commandLinePattern = element.getAttribute( ITool.COMMAND_LINE_PATTERN );
		
		// Get advancedInputCategory
        String advInput = element.getAttribute(ITool.ADVANCED_INPUT_CATEGORY);
        if (advInput != null){
			advancedInputCategory = new Boolean("true".equals(advInput)); //$NON-NLS-1$
        }
		
		// Get customBuildStep
        String cbs = element.getAttribute(ITool.CUSTOM_BUILD_STEP);
        if (cbs != null){
			customBuildStep = new Boolean("true".equals(cbs)); //$NON-NLS-1$
        }
		
		// Get the announcement text
		announcement = element.getAttribute(ITool.ANNOUNCEMENT);
		
		// Store the configuration element IFF there is a command line generator defined 
		String commandLineGenerator = element.getAttribute(COMMAND_LINE_GENERATOR); 
		if (commandLineGenerator != null && element instanceof DefaultManagedConfigElement) {
			commandLineGeneratorElement = ((DefaultManagedConfigElement)element).getConfigurationElement();			
		}
		
		// Store the configuration element IFF there is a dependency generator defined 
		String depGenerator = element.getAttribute(DEP_CALC_ID); 
		if (depGenerator != null && element instanceof DefaultManagedConfigElement) {
			dependencyGeneratorElement = ((DefaultManagedConfigElement)element).getConfigurationElement();			
		}
		
		// icon
		if ( element.getAttribute(IOptionCategory.ICON) != null && element instanceof DefaultManagedConfigElement)
		{
		    String icon = element.getAttribute(IOptionCategory.ICON);
			iconPathURL = ManagedBuildManager.getURLInBuildDefinitions( (DefaultManagedConfigElement)element, new Path(icon) );
		}		
	}
	
	/* (non-Javadoc)
	 * Initialize the tool information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the tool information 
	 */
	protected void loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.hasAttribute(IBuildObject.NAME)) {
			setName(element.getAttribute(IBuildObject.NAME));
		}

		// version
		setVersion(getVersionFromId());
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		if (superClassId != null && superClassId.length() > 0) {
			if( getParent() instanceof IResourceConfiguration ) {
				IResourceConfiguration resConfig = (IResourceConfiguration) getParent();
				setSuperClass( resConfig.getParent().getTool(superClassId) );
			} else {
				setSuperClass( ManagedBuildManager.getExtensionTool(superClassId) );
			
				// Check for migration support
				checkForMigrationSupport();
			}
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
		
		// Get the 'versionSupported' attribute
		if (element.hasAttribute(VERSIONS_SUPPORTED)) {
			versionsSupported = element.getAttribute(VERSIONS_SUPPORTED);
		}
		
		// Get the 'convertToId' id
		if (element.hasAttribute(CONVERT_TO_ID)) {
			convertToId = element.getAttribute(CONVERT_TO_ID);
		}
		
		// Get the semicolon separated list of IDs of the error parsers
		if (element.hasAttribute(IToolChain.ERROR_PARSERS)) {
			errorParserIds = element.getAttribute(IToolChain.ERROR_PARSERS);
		}
		
		// Get the nature filter
		if (element.hasAttribute(NATURE)) {
			String nature = element.getAttribute(NATURE);
			if (nature != null) {
				if ("both".equals(nature)) {	//$NON-NLS-1$
					natureFilter = new Integer(FILTER_BOTH);
				} else if ("cnature".equals(nature)) {	//$NON-NLS-1$
					natureFilter = new Integer(FILTER_C);
				} else if ("ccnature".equals(nature)) {	//$NON-NLS-1$
					natureFilter = new Integer(FILTER_CC);
				} else {
					natureFilter = new Integer(FILTER_BOTH);
				}
			}
		}
		
		// Get the supported input file extension
		if (element.hasAttribute(ITool.SOURCES)) {
			String inputs = element.getAttribute(ITool.SOURCES);
			if (inputs != null) {
				StringTokenizer tokenizer = new StringTokenizer(inputs, DEFAULT_SEPARATOR);
				while (tokenizer.hasMoreElements()) {
					getInputExtensionsList().add(tokenizer.nextElement());
				}
			}
		}
		
		// Get the interface (header file) extensions
		if (element.hasAttribute(INTERFACE_EXTS)) {
			String headers = element.getAttribute(INTERFACE_EXTS);
			if (headers != null) {
				StringTokenizer tokenizer = new StringTokenizer(headers, DEFAULT_SEPARATOR);
				while (tokenizer.hasMoreElements()) {
					getInterfaceExtensionsList().add(tokenizer.nextElement());
				}
			}
		}
		
		// Get the output extension
		if (element.hasAttribute(ITool.OUTPUTS)) {
			outputExtensions = element.getAttribute(ITool.OUTPUTS); 
		}
			
		// Get the tool invocation command
		if (element.hasAttribute(ITool.COMMAND)) {
			command = element.getAttribute(ITool.COMMAND); 
		}
			
		// Get the flag to control output
		if (element.hasAttribute(ITool.OUTPUT_FLAG)) {
			outputFlag = element.getAttribute(ITool.OUTPUT_FLAG);
		}
			
		// Get the output prefix
		if (element.hasAttribute(ITool.OUTPUT_PREFIX)) {
			outputPrefix = element.getAttribute(ITool.OUTPUT_PREFIX);
		}
		
		// Get command line pattern
		if( element.hasAttribute( ITool.COMMAND_LINE_PATTERN ) ) {
			commandLinePattern = element.getAttribute( ITool.COMMAND_LINE_PATTERN );
		}
		
		// advancedInputCategory
		if (element.hasAttribute(ITool.ADVANCED_INPUT_CATEGORY)) {
			String advInput = element.getAttribute(ITool.ADVANCED_INPUT_CATEGORY);
			if (advInput != null){
				advancedInputCategory = new Boolean("true".equals(advInput)); //$NON-NLS-1$
			}
		}
		
		// customBuildStep
		if (element.hasAttribute(ITool.CUSTOM_BUILD_STEP)) {
			String cbs = element.getAttribute(ITool.CUSTOM_BUILD_STEP);
			if (cbs != null){
				customBuildStep = new Boolean("true".equals(cbs)); //$NON-NLS-1$
			}
		}
		
		// Get the announcement text
		if (element.hasAttribute(ITool.ANNOUNCEMENT)) {
			announcement = element.getAttribute(ITool.ANNOUNCEMENT);
		}
		
		// icon - was saved as URL in string form
		if (element.hasAttribute(IOptionCategory.ICON)) {
			String iconPath = element.getAttribute(IOptionCategory.ICON);
			try {
				iconPathURL = new URL(iconPath);
			} catch (MalformedURLException e) {
				// Print a warning
				ManagedBuildManager.OutputIconError(iconPath);
				iconPathURL = null;
			}
		}
	}

	/**
	 * Persist the tool to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		try {
			if (getSuperClass() != null)
				element.setAttribute(IProjectType.SUPERCLASS, getSuperClass().getId());
			
			// id
			element.setAttribute(IBuildObject.ID, id);
			
			// name
			if (name != null) {
				element.setAttribute(IBuildObject.NAME, name);
			}
	
			// unused children
			if (unusedChildren != null) {
				element.setAttribute(IProjectType.UNUSED_CHILDREN, unusedChildren);
			}
			
			// isAbstract
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
			
			// error parsers
			if (errorParserIds != null) {
				element.setAttribute(IToolChain.ERROR_PARSERS, errorParserIds);
			}
			
			// nature filter
			if (natureFilter != null) {
				String nature;
				if (natureFilter.intValue() == FILTER_C) {
					nature = "cnature";	//$NON-NLS-1$
				} else if (natureFilter.intValue() == FILTER_CC) {
					nature = "ccnature";	//$NON-NLS-1$
				} else {
					nature = "both";	//$NON-NLS-1$
				}
				element.setAttribute(NATURE, nature);
			}
			
			// input file extensions
			if (getInputExtensionsList().size() > 0) {
				String inputs;
				List list = getInputExtensionsList();
				Iterator iter = list.listIterator();
				inputs = (String)iter.next();
				while (iter.hasNext()) {
					inputs += DEFAULT_SEPARATOR;
					inputs += iter.next();
				}
				element.setAttribute(ITool.SOURCES, inputs);
			}
			
			// interface (header file) extensions
			if (getInterfaceExtensionsList().size() > 0) {
				String headers;
				List list = getInterfaceExtensionsList();
				Iterator iter = list.listIterator();
				headers = (String)iter.next();
				while (iter.hasNext()) {
					headers += DEFAULT_SEPARATOR;
					headers += iter.next();
				}
				element.setAttribute(INTERFACE_EXTS, headers);
			}
			
			// output extension
			if (outputExtensions != null) {
				element.setAttribute(ITool.OUTPUTS, outputExtensions); 
			}
				
			// command
			if (command != null) {
				element.setAttribute(ITool.COMMAND, command); 
			}
				
			// flag to control output
			if (outputFlag != null) {
				element.setAttribute(ITool.OUTPUT_FLAG, outputFlag);
			}
				
			// output prefix
			if (outputPrefix != null) {
				element.setAttribute(ITool.OUTPUT_PREFIX, outputPrefix);
			}
			
			// command line pattern
			if (commandLinePattern != null) {
				element.setAttribute(ITool.COMMAND_LINE_PATTERN, commandLinePattern);
			}
			
			// advancedInputCategory
			if (advancedInputCategory != null) {
				element.setAttribute(ITool.ADVANCED_INPUT_CATEGORY, advancedInputCategory.toString());
			}
			
			// customBuildStep
			if (customBuildStep != null) {
				element.setAttribute(ITool.CUSTOM_BUILD_STEP, customBuildStep.toString());
			}
			
			// announcement text
			if (announcement != null) {
				element.setAttribute(ITool.ANNOUNCEMENT, announcement);
			}
			
			// Serialize elements from my super class
			super.serialize(doc, element);

			// Serialize my children
			Iterator iter;
			List typeElements = getInputTypeList();
			iter = typeElements.listIterator();
			while (iter.hasNext()) {
				InputType type = (InputType) iter.next();
				Element typeElement = doc.createElement(INPUT_TYPE);
				element.appendChild(typeElement);
				type.serialize(doc, typeElement);
			}
			typeElements = getOutputTypeList();
			iter = typeElements.listIterator();
			while (iter.hasNext()) {
				OutputType type = (OutputType) iter.next();
				Element typeElement = doc.createElement(OUTPUT_TYPE);
				element.appendChild(typeElement);
				type.serialize(doc, typeElement);
			}

			// Note: command line generator cannot be specified in a project file because
			//       an IConfigurationElement is needed to load it!
			if (commandLineGeneratorElement != null) {
				//  TODO:  issue warning?
			}

			// Note: dependency generator cannot be specified in a project file because
			//       an IConfigurationElement is needed to load it!
			if (dependencyGeneratorElement != null) {
				//  TODO:  issue warning?
			}
			
			if (iconPathURL != null) {
				// Save as URL in string form
				element.setAttribute(IOptionCategory.ICON, iconPathURL.toString());
			}
			
			// I am clean now
			isDirty = false;
		} catch (Exception e) {
			// TODO: issue an error message
		}
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getParent()
	 */
	public IBuildObject getParent() {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setParent(IBuildObject)
	 */
	public void setToolParent(IBuildObject newParent) {
		this.parent = newParent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getTopOptionCategory()
	 */
	public IOptionCategory getTopOptionCategory() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#createInputType(IInputType, String, String, boolean)
	 */
	public IInputType createInputType(IInputType superClass, String Id, String name, boolean isExtensionElement) {
		InputType type = new InputType(this, superClass, Id, name, isExtensionElement);
		addInputType(type);
		setDirty(true);
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#removeInputType(IInputType)
	 */
	public void removeInputType(IInputType type) {
		getInputTypeList().remove(type);
		getInputTypeMap().remove(type.getId());
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getInputTypes()
	 */
	public IInputType[] getInputTypes() {
		IInputType[] types = null;
		// Merge our input types with our superclass' input types.
		if (getSuperClass() != null) {
			types = getSuperClass().getInputTypes();
		}
		// Our options take precedence.
		Vector ourTypes = getInputTypeList();
		if (types != null) {
			for (int i = 0; i < ourTypes.size(); i++) {
				IInputType ourType = (IInputType)ourTypes.get(i);
				int j;
				for (j = 0; j < types.length; j++) {
					if (ourType.getSuperClass() != null &&
					    ourType.getSuperClass().getId().equals(types[j].getId())) {
						types[j] = ourType;
						break;
					}
				}
				//  No Match?  Add it.
				if (j == types.length) {
					IInputType[] newTypes = new IInputType[types.length + 1];
					for (int k = 0; k < types.length; k++) {
						newTypes[k] = types[k];
					}						 
					newTypes[j] = ourType;
					types = newTypes;
				}
			}
		} else {
			types = (IInputType[])ourTypes.toArray(new IInputType[ourTypes.size()]);
		}
		return types;
	}

	private boolean hasInputTypes() {
		Vector ourTypes = getInputTypeList();
		if (ourTypes.size() > 0) return true;
		return false;
	}
	
	public IInputType getInputTypeById(String id) {
		IInputType type = (IInputType)getInputTypeMap().get(id);
		if (type == null) {
			if (getSuperClass() != null) {
				return getSuperClass().getInputTypeById(id);
			}
		}
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#createOutputType(IOutputType, String, String, boolean)
	 */
	public IOutputType createOutputType(IOutputType superClass, String Id, String name, boolean isExtensionElement) {
		OutputType type = new OutputType(this, superClass, Id, name, isExtensionElement);
		addOutputType(type);
		setDirty(true);
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#removeOutputType(IOutputType)
	 */
	public void removeOutputType(IOutputType type) {
		getOutputTypeList().remove(type);
		getOutputTypeMap().remove(type.getId());
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOutputTypes()
	 */
	public IOutputType[] getOutputTypes() {
		IOutputType[] types = null;
		// Merge our output types with our superclass' output types.
		if (getSuperClass() != null) {
			types = getSuperClass().getOutputTypes();
		}
		// Our options take precedence.
		Vector ourTypes = getOutputTypeList();
		if (types != null) {
			for (int i = 0; i < ourTypes.size(); i++) {
				IOutputType ourType = (IOutputType)ourTypes.get(i);
				int j;
				for (j = 0; j < types.length; j++) {
					if (ourType.getSuperClass() != null &&
					    ourType.getSuperClass().getId().equals(types[j].getId())) {
						types[j] = ourType;
						break;
					}
				}
				//  No Match?  Add it.
				if (j == types.length) {
					IOutputType[] newTypes = new IOutputType[types.length + 1];
					for (int k = 0; k < types.length; k++) {
						newTypes[k] = types[k];
					}						 
					newTypes[j] = ourType;
					types = newTypes;
				}
			}
		} else {
			types = (IOutputType[])ourTypes.toArray(new IOutputType[ourTypes.size()]);
		}
		return types;
	}

	private boolean hasOutputTypes() {
		Vector ourTypes = getOutputTypeList();
		if (ourTypes.size() > 0) return true;
		return false;
	}

	public IOutputType getPrimaryOutputType() {
		IOutputType type = null;
		IOutputType[] types = getOutputTypes();
		if (types != null && types.length > 0) {
			for (int i=0; i<types.length; i++) {
				if (i == 0) type = types[0];
				if (types[i].getPrimaryOutput() == true) {
					type = types[i];
					break;
				}
			}
		}
		return type;
	}

	public IOutputType getOutputTypeById(String id) {
		IOutputType type = (IOutputType)getOutputTypeMap().get(id);
		if (type == null) {
			if (getSuperClass() != null) {
				return getSuperClass().getOutputTypeById(id);
			}
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOwner()
	 */
	public IOptionCategory getOwner() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getIconPath()
	 */
	public URL getIconPath() {
		if (iconPathURL == null  &&  getSuperClass() != null) {
			return getSuperClass().getTopOptionCategory().getIconPath();
		}
		return iconPathURL;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOptions(org.eclipse.cdt.core.build.managed.ITool)
	 */
	public Object[][] getOptions(IConfiguration configuration) {
		// Find the child of the configuration that represents the same tool.
		// It could the tool itself, or a "sub-class" of the tool.
		if (configuration != null) {
			ITool[] tools = configuration.getTools();
			return getOptions(tools);
		} else {
			return getAllOptions(this);
		}
	}
	
	public Object[][] getOptions(IResourceConfiguration resConfig) {
		ITool[] tools = resConfig.getTools();
		return getOptions(tools);
	}
	
	private Object[][] getOptions(ITool[] tools) {
		ITool catTool = this;
		ITool tool = null;
		for (int i = 0; i < tools.length; ++i) {
			ITool current = tools[i];
			do {
				if (catTool == current) {
					tool = tools[i];
					break;
				}
			} while ((current = current.getSuperClass()) != null);
			if (tool != null) break;
		}
		// Get all of the tool's options and see which ones are part of
		// this category.
		if( tool == null)
			return null;
		
		return getAllOptions(tool);
	}
	
	private Object[][] getAllOptions(ITool tool) {
		IOption[] allOptions = tool.getOptions();
		Object[][] myOptions = new Object[allOptions.length][2];
		int index = 0;
		for (int i = 0; i < allOptions.length; ++i) {
			IOption option = allOptions[i];
			IOptionCategory optCat = option.getCategory();
			if (optCat instanceof ITool) {
				//  Determine if the category is this tool or a superclass
				ITool current = this;
				boolean match = false;
				do {
					if (optCat == current) {
						match = true;
						break;
					}
				} while ((current = current.getSuperClass()) != null);
				if (match) {
					myOptions[index] = new Object[2];
					myOptions[index][0] = tool;
					myOptions[index][1] = option;
					index++;
				}
			}
		}

		return myOptions;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getTool()
	 */
	public ITool getTool() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOptionHolder()
	 */
	public IHoldsOptions getOptionHolder() {
		return this; 
	}

	/* (non-Javadoc)
	 * Memory-safe way to access the list of input types
	 */
	private Vector getInputTypeList() {
		if (inputTypeList == null) {
			inputTypeList = new Vector();
		}
		return inputTypeList;
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of IDs to input types
	 */
	private Map getInputTypeMap() {
		if (inputTypeMap == null) {
			inputTypeMap = new HashMap();
		}
		return inputTypeMap;
	}
	
	/**
	 * @param type
	 */
	public void addInputType(InputType type) {
		getInputTypeList().add(type);
		getInputTypeMap().put(type.getId(), type);
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of output types
	 */
	private Vector getOutputTypeList() {
		if (outputTypeList == null) {
			outputTypeList = new Vector();
		}
		return outputTypeList;
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of IDs to output types
	 */
	private Map getOutputTypeMap() {
		if (outputTypeMap == null) {
			outputTypeMap = new HashMap();
		}
		return outputTypeMap;
	}
	
	/**
	 * @param type
	 */
	public void addOutputType(OutputType type) {
		getOutputTypeList().add(type);
		getOutputTypeMap().put(type.getId(), type);
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getSuperClass()
	 */
	public ITool getSuperClass() {
		return (ITool)superClass;
	}

	/* (non-Javadoc)
	 * Access function to set the superclass element that is defined in
	 * the base class.
	 */
	private void setSuperClass(ITool superClass) {
		this.superClass = superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getName()
	 */
	public String getName() {
		return (name == null && getSuperClass() != null) ? getSuperClass().getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#isAbstract()
	 */
	public boolean isAbstract() {
		if (isAbstract != null) {
			return isAbstract.booleanValue();
		} else {
			return false;	// Note: no inheritance from superClass
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setIsAbstract(boolean)
	 */
	public void setIsAbstract(boolean b) {
		isAbstract = new Boolean(b);
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getUnusedChildren()
	 */
	public String getUnusedChildren() {
		if (unusedChildren != null) {
			return unusedChildren;
		} else
			return EMPTY_STRING;	// Note: no inheritance from superClass
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getErrorParserIds()
	 */
	public String getErrorParserIds() {
		String ids = errorParserIds;
		if (ids == null) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				ids = getSuperClass().getErrorParserIds();
			}
		}
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getErrorParserList()
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
			errorParsers = new String[0];
		}
		return errorParsers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getInputExtensions()
	 * @deprecated
	 */
	public List getInputExtensions() {
		String[] exts = getPrimaryInputExtensions();
		List extList = new ArrayList();
		for (int i=0; i<exts.length; i++) {
			extList.add(exts[i]);
		}
		return extList;
	}

	private List getInputExtensionsAttribute() {
		if( (inputExtensions == null) || ( inputExtensions.size() == 0) ) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				return ((Tool)getSuperClass()).getInputExtensionsAttribute();
			} else {
				inputExtensions = new ArrayList();
			}
		}
		return inputExtensions;
	}

	private List getInputExtensionsList() {
		if (inputExtensions == null) {
				inputExtensions = new ArrayList();
		}
		return inputExtensions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getDefaultInputExtension()
	 */
	public String getDefaultInputExtension() {
		// Find the primary input type
		IInputType type = getPrimaryInputType();
		if (type != null) {
			String[] exts = type.getSourceExtensions(this);
			// Use the first entry in the list
			if (exts.length > 0) return exts[0];
		}
		// If none, use the input extensions specified for the Tool (backwards compatibility)
		List extsList = getInputExtensionsAttribute();
		// Use the first entry in the list
		if (extsList != null && extsList.size() > 0) return (String)extsList.get(0);
		return EMPTY_STRING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getPrimaryInputExtensions()
	 */
	public String[] getPrimaryInputExtensions() {
		IInputType type = getPrimaryInputType();
		if (type != null) {
			String[] exts = type.getSourceExtensions(this);
			// Use the first entry in the list
			if (exts.length > 0) return exts;
		}
		// If none, use the input extensions specified for the Tool (backwards compatibility)
		List extsList = getInputExtensionsAttribute();
		// Use the first entry in the list
		if (extsList != null && extsList.size() > 0) {
			return (String[])extsList.toArray(new String[extsList.size()]);
		}
		return EMPTY_STRING_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getAllInputExtensions()
	 */
	public String[] getAllInputExtensions() {
		IInputType[] types = getInputTypes();
		if (types != null && types.length > 0) {
			List allExts = new ArrayList();
			for (int i=0; i<types.length; i++) {
				String[] exts = types[i].getSourceExtensions(this);
				for (int j=0; j<exts.length; j++) {
					allExts.add(exts[j]);
				}
			}
			if (allExts.size() > 0) {
				return (String[])allExts.toArray(new String[allExts.size()]);
			}
		}
		// If none, use the input extensions specified for the Tool (backwards compatibility)
		List extsList = getInputExtensionsAttribute();
		if (extsList != null && extsList.size() > 0) {
			return (String[])extsList.toArray(new String[extsList.size()]);
		}
		return EMPTY_STRING_ARRAY;
	}

	public IInputType getPrimaryInputType() {
		IInputType type = null;
		IInputType[] types = getInputTypes();
		if (types != null && types.length > 0) {
			for (int i=0; i<types.length; i++) {
				if (i == 0) type = types[0];
				if (types[i].getPrimaryInput() == true) {
					type = types[i];
					break;
				}
			}
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getInputInputType()
	 */
	public IInputType getInputType(String inputExtension) {
		IInputType type = null;
		IInputType[] types = getInputTypes();
		if (types != null && types.length > 0) {
			for (int i=0; i<types.length; i++) {
				if (types[i].isSourceExtension(this, inputExtension)) {
					type = types[i];
					break;
				}
			}
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getAdditionalDependencies()
	 */
	public IPath[] getAdditionalDependencies() {
		List allDeps = new ArrayList();
		IInputType[] types = getInputTypes();
		for (int i=0; i<types.length; i++) {
			IInputType type = types[i];
			//  Additional dependencies come from 2 places.
			//  1.  From AdditionalInput childen
			IPath[] deps = type.getAdditionalDependencies();
			for (int j=0; j<deps.length; j++) {
				allDeps.add(deps[j]);
			}
			//  2.  From InputTypes that other than the primary input type
			if (type != getPrimaryInputType()) {
				if (type.getOptionId() != null) {
					IOption option = getOptionBySuperClassId(type.getOptionId());
					if (option != null) {
						try {
							List inputs = new ArrayList();
							int optType = option.getValueType();
							if (optType == IOption.STRING) {
								inputs.add(Path.fromOSString(option.getStringValue()));
							} else if (
									optType == IOption.STRING_LIST ||
									optType == IOption.LIBRARIES ||
									optType == IOption.OBJECTS) {
								List inputNames = (List)option.getValue();
								for (int j=0; j<inputNames.size(); j++) {
									inputs.add(Path.fromOSString((String)inputNames.get(j)));
								}
							}
							allDeps.addAll(inputs);
						} catch( BuildException ex ) {
						}
					}
				} else if (type.getBuildVariable() != null && type.getBuildVariable().length() > 0) {
					allDeps.add(Path.fromOSString("$(" + type.getBuildVariable() + ")"));   //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		return (IPath[])allDeps.toArray(new IPath[allDeps.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getAdditionalResources()
	 */
	public IPath[] getAdditionalResources() {
		List allRes = new ArrayList();
		IInputType[] types = getInputTypes();
		for (int i=0; i<types.length; i++) {
			IInputType type = types[i];
			//  Additional dependencies come from 2 places.
			//  1.  From AdditionalInput childen
			IPath[] res = type.getAdditionalResources();
			for (int j=0; j<res.length; j++) {
				allRes.add(res[j]);
			}
			//  2.  From InputTypes that other than the primary input type
			if (type != getPrimaryInputType()) {
				String var = type.getBuildVariable();
				if (var != null && var.length() > 0) {
					allRes.add(Path.fromOSString("$(" + type.getBuildVariable() + ")"));   //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		return (IPath[])allRes.toArray(new IPath[allRes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getAllDependencyExtensions()
	 */
	public String[] getAllDependencyExtensions() {
		IInputType[] types = getInputTypes();
		if (types != null && types.length > 0) {
			List allExts = new ArrayList();
			for (int i=0; i<types.length; i++) {
				String[] exts = types[i].getDependencyExtensions(this);
				for (int j=0; j<exts.length; j++) {
					allExts.add(exts[j]);
				}
			}
			if (allExts.size() > 0) {
				return (String[])allExts.toArray(new String[allExts.size()]);
			}
		}
		// If none, use the header extensions specified for the Tool (backwards compatibility)
		List extsList = getHeaderExtensionsAttribute();
		if (extsList != null && extsList.size() > 0) {
			return (String[])extsList.toArray(new String[extsList.size()]);
		}
		return EMPTY_STRING_ARRAY;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getInterfaceExtension()
	 * @deprecated
	 */
	public List getInterfaceExtensions() {
		return getHeaderExtensionsAttribute();
	}

	private List getHeaderExtensionsAttribute() {
		if (interfaceExtensions == null || interfaceExtensions.size() == 0) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				return ((Tool)getSuperClass()).getHeaderExtensionsAttribute();
			} else {
			    if (interfaceExtensions == null) {
			        interfaceExtensions = new ArrayList();
			    }
			}
		}
		return interfaceExtensions;
	}

	private List getInterfaceExtensionsList() {
		if (interfaceExtensions == null) {
			interfaceExtensions = new ArrayList();
		}
		return interfaceExtensions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutputFlag()
	 */
	public String getOutputFlag() {
		if (outputFlag == null) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				return getSuperClass().getOutputFlag();
			} else {
				return EMPTY_STRING;
			}
		}
		return outputFlag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutputPrefix()
	 */
	public String getOutputPrefix() {
		// Get the outputPrefix from an OutputType, if any.
		IOutputType type = null;
		IOutputType[] types = getOutputTypes();
		if (types != null && types.length > 0) {
			for (int i=0; i<types.length; i++) {
				if (i == 0) type = types[0];
				if (types[i].getPrimaryOutput() == true) {
					type = types[i];
					break;
				}
			}
		}
		if (type != null) {
			return type.getOutputPrefix();
		}
		
		// If there are no OutputTypes, use the deprecated Tool attribute
		if (outputPrefix == null) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				return getSuperClass().getOutputPrefix();
			} else {
				return EMPTY_STRING;
			}
		}
		return outputPrefix;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getToolCommand()
	 */
	public String getToolCommand() {
		if (command == null) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				return getSuperClass().getToolCommand();
			} else {
				return EMPTY_STRING;
			}
		}
		return command;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getCommandLinePattern()
	 */
	public String getCommandLinePattern() {
		if (commandLinePattern == null) {
			if (getSuperClass() != null) {
				return getSuperClass().getCommandLinePattern();
			} else {
				if (getCustomBuildStep()) {
					return new String(DEFAULT_CBS_PATTERN);  // Default pattern
				} else {
					return new String(DEFAULT_PATTERN);  // Default pattern
				}
			}
		}
		return commandLinePattern;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getAdvancedInputCategory()
	 */
	public boolean getAdvancedInputCategory() {
		if (advancedInputCategory == null) {
			if (getSuperClass() != null) {
				return getSuperClass().getAdvancedInputCategory();
			} else {
				return false;	// default is false
			}
		}
		return advancedInputCategory.booleanValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getCustomBuildStep()
	 */
	public boolean getCustomBuildStep() {
		if (customBuildStep == null) {
			if (getSuperClass() != null) {
				return getSuperClass().getCustomBuildStep();
			} else {
				return false;	// default is false
			}
		}
		return customBuildStep.booleanValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getAnnouncement()
	 */
	public String getAnnouncement() {
		if (announcement == null) {
			if (getSuperClass() != null) {
				return getSuperClass().getAnnouncement();
			} else {
				//  Generate the default announcement string for the Tool
				String defaultAnnouncement = ManagedMakeMessages.getResourceString(DEFAULT_ANNOUNCEMENT_PREFIX) +
					WHITESPACE + getName();  // + "(" + getId() + ")";  
				return defaultAnnouncement;
			}
		}
		return announcement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getCommandLineGeneratorElement()
	 */
	public IConfigurationElement getCommandLineGeneratorElement() {
		if (commandLineGeneratorElement == null) {
			if (getSuperClass() != null) {
				return ((Tool)getSuperClass()).getCommandLineGeneratorElement();
			}
		}
		return commandLineGeneratorElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setCommandLineGeneratorElement(String)
	 */
	public void setCommandLineGeneratorElement(IConfigurationElement element) {
		commandLineGeneratorElement = element;
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getCommandLineGenerator()
	 */
	public IManagedCommandLineGenerator getCommandLineGenerator() {
		if (commandLineGenerator != null) {
			return commandLineGenerator;
		}
		IConfigurationElement element = getCommandLineGeneratorElement();
		if (element != null) {
			try {
				if (element.getAttribute(COMMAND_LINE_GENERATOR) != null) {
					commandLineGenerator = (IManagedCommandLineGenerator) element.createExecutableExtension(COMMAND_LINE_GENERATOR);
					return commandLineGenerator;
				}
			} catch (CoreException e) {}
		}
		return ManagedCommandLineGenerator.getCommandLineGenerator();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getDependencyGeneratorElement()
	 * @deprecated
	 */
	public IConfigurationElement getDependencyGeneratorElement() {
		//  First try the primary InputType
		IInputType type = getPrimaryInputType();
		if (type != null) {
			IConfigurationElement primary = ((InputType)type).getDependencyGeneratorElement();
			if (primary != null) return primary;
		}

		//  If not found, use the deprecated attribute
		return getToolDependencyGeneratorElement();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getDependencyGeneratorElementForExtension()
	 */
	public IConfigurationElement getDependencyGeneratorElementForExtension(String sourceExt) {
		IInputType[] types = getInputTypes();
		if (types != null) {
			for (int i=0; i<types.length; i++) {
				if (types[i].isSourceExtension(this, sourceExt)) {
					return ((InputType)types[i]).getDependencyGeneratorElement();
				}
			}
		}

		//  If not found, use the deprecated attribute
		return getToolDependencyGeneratorElement();
	}
	
	private IConfigurationElement getToolDependencyGeneratorElement() {
		if (dependencyGeneratorElement == null) {
			if (getSuperClass() != null) {
				return ((Tool)getSuperClass()).getToolDependencyGeneratorElement();
			}
		}
		return dependencyGeneratorElement;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setDependencyGeneratorElement(String)
	 * @deprecated
	 */
	public void setDependencyGeneratorElement(IConfigurationElement element) {
		dependencyGeneratorElement = element;
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getDependencyGenerator()
	 * @deprecated
	 */
	public IManagedDependencyGenerator getDependencyGenerator() {
		if (dependencyGenerator != null) {
			return dependencyGenerator;
		}
		IConfigurationElement element = getDependencyGeneratorElement();
		if (element != null) {
			try {
				if (element.getAttribute(DEP_CALC_ID) != null) {
					dependencyGenerator = (IManagedDependencyGenerator) element.createExecutableExtension(DEP_CALC_ID);
					return dependencyGenerator;
				}
			} catch (CoreException e) {}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getDependencyGeneratorForExtension()
	 */
	public IManagedDependencyGenerator getDependencyGeneratorForExtension(String sourceExt) {
		if (dependencyGenerator != null) {
			return dependencyGenerator;
		}
		IConfigurationElement element = getDependencyGeneratorElementForExtension(sourceExt);
		if (element != null) {
			try {
				if (element.getAttribute(DEP_CALC_ID) != null) {
					dependencyGenerator = (IManagedDependencyGenerator) element.createExecutableExtension(DEP_CALC_ID);
					return dependencyGenerator;
				}
			} catch (CoreException e) {}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getNatureFilter()
	 */
	public int getNatureFilter() {
		if (natureFilter == null) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				return getSuperClass().getNatureFilter();
			} else {
				return FILTER_BOTH;
			}
		}
		return natureFilter.intValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getAllOutputExtensions()
	 */
	public String[] getAllOutputExtensions() {
		IOutputType[] types = getOutputTypes();
		if (types != null && types.length > 0) {
			List allExts = new ArrayList();
			for (int i=0; i<types.length; i++) {
				String[] exts = types[i].getOutputExtensions(this);
				if (exts != null) {
					for (int j=0; j<exts.length; j++) {
						allExts.add(exts[j]);
					}
				}
			}
			if (allExts.size() > 0) {
				return (String[])allExts.toArray(new String[allExts.size()]);
			}
		}
		// If none, use the outputs specified for the Tool (backwards compatibility)
		String[] extsList = getOutputsAttribute();
		if (extsList != null && extsList.length > 0) {
			return extsList;
		}
		return EMPTY_STRING_ARRAY;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOutputExtensions()
	 * @deprecated
	 */
	public String[] getOutputExtensions() {
		return getOutputsAttribute();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOutputsAttribute()
	 */
	public String[] getOutputsAttribute() {
		// TODO:  Why is this treated differently than inputExtensions?
		if (outputExtensions == null) {
			if (getSuperClass() != null) {
				return getSuperClass().getOutputsAttribute();
			} else {
				return null;
			}
		}
		return outputExtensions.split(DEFAULT_SEPARATOR);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutputExtension(java.lang.String)
	 */
	public String getOutputExtension(String inputExtension) {
		// Search thru the output-types to find one that has a primary input type with this extension
		IOutputType[] types = getOutputTypes();
		int i;
		if (types != null) {
			for (i=0; i<types.length; i++) {
				IInputType inputType = types[i].getPrimaryInputType();
				if (inputType != null && inputType.isSourceExtension(this, inputExtension)) {
					String[] exts = types[i].getOutputExtensions(this);
					if (exts != null && exts.length > 0) {
						return exts[0]; 
					}
				}
			}
			// Does any input type produce this extension?
			if (getInputType(inputExtension) != null) {
				//  Return the first extension of the primary output type
				IOutputType outType = getPrimaryOutputType();
				String[] exts = outType.getOutputExtensions(this);
				if (exts != null && exts.length > 0) {
					return exts[0]; 
				}				
			}
		}
		// If no OutputTypes specified, examine the list of input extensions
		String[] inputExts = getAllInputExtensions();
		for (i=0; i<inputExts.length; i++) {
			if (inputExts[i].equals(inputExtension)) {
				String[] exts = getOutputsAttribute();
				if (exts != null) {
					if (i < exts.length) {
						return exts[i];
					} else {
						return exts[exts.length - 1];
					}
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutputType(java.lang.String)
	 */
	public IOutputType getOutputType(String outputExtension) {
		IOutputType type = null;
		IOutputType[] types = getOutputTypes();
		if (types != null && types.length > 0) {
			for (int i=0; i<types.length; i++) {
				if (types[i].isOutputExtension(this, outputExtension)) {
					type = types[i];
					break;
				}
			}
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setErrorParserIds()
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setToolCommand(java.lang.String)
	 */
	public boolean setToolCommand(String cmd) {
		if (cmd == null && command == null) return false;
		if (cmd == null || command == null || !cmd.equals(command)) {
			command = cmd;
			isDirty = true;
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setCommandLinePattern()
	 */
	public void setCommandLinePattern(String pattern) {
		if (pattern == null && commandLinePattern == null) return;
		if (pattern == null || commandLinePattern == null || !pattern.equals(commandLinePattern)) {
			commandLinePattern = pattern;
			isDirty = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setOutputFlag(java.lang.String)
	 */
	public void setOutputFlag(String flag) {
		if (flag == null && outputFlag == null) return;
		if (outputFlag == null || flag == null || !(flag.equals(outputFlag))) {
			outputFlag = flag;
			isDirty = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setOutputPrefix(java.lang.String)
	 */
	public void setOutputPrefix(String prefix) {
		if (prefix == null && outputPrefix == null) return;
		if (outputPrefix == null || prefix == null || !(prefix.equals(outputPrefix))) {
			outputPrefix = prefix;
			isDirty = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setOutputsAttribute(java.lang.String)
	 */
	public void setOutputsAttribute(String ext) {
		if (ext == null && outputExtensions == null) return;
		if (outputExtensions == null || ext == null || !(ext.equals(outputExtensions))) {
			outputExtensions = ext;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setAdvancedInputCategory(boolean)
	 */
	public void setAdvancedInputCategory(boolean b) {
		if (advancedInputCategory == null || !(b == advancedInputCategory.booleanValue())) {
			advancedInputCategory = new Boolean(b);
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setCustomBuildStep(boolean)
	 */
	public void setCustomBuildStep(boolean b) {
		if (customBuildStep == null || !(b == customBuildStep.booleanValue())) {
			customBuildStep = new Boolean(b);
			setDirty(true);
		}
	}

	public void setAnnouncement(String newText) {
		if (newText == null && announcement == null) return;
		if (announcement == null || newText == null || !(newText.equals(announcement))) {
			announcement = newText;
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getCommandFlags()
	 */
	public String[] getCommandFlags() throws BuildException {
		return getToolCommandFlags(null,null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getToolFlags()
	 */
	public String getToolFlags() throws BuildException {
		return getToolCommandFlagsString(null,null);
	}
	
	/**
	 * this method used internaly by the Tool to obtain the command flags with the build macros resolved,
	 * but could be also used by other MBS components to adjust the tool flags resolution 
	 * behavior by passing the method some custom macro substitutor
	 *  
	 * @param inputFileLocation
	 * @param outputFileLocation
	 * @param macroSubstitutor
	 * @return
	 * @throws BuildException
	 */
	public String[] getToolCommandFlags(IPath inputFileLocation, IPath outputFileLocation,
				IMacroSubstitutor macroSubstitutor) throws BuildException {
		IOption[] opts = getOptions();
		ArrayList flags = new ArrayList();
		StringBuffer sb = new StringBuffer();
		for (int index = 0; index < opts.length; index++) {
			IOption option = opts[index];
			sb.setLength( 0 );

			// check to see if the option has an applicability calculator
			IOptionApplicability applicabilityCalculator = option.getApplicabilityCalculator();
			IBuildObject config = null;
			IBuildObject parent = getParent();
			if ( parent instanceof IResourceConfiguration ) {
				config = parent;
			} else if ( parent instanceof IToolChain ){
				config = ((IToolChain)parent).getParent();
			}
			if (applicabilityCalculator == null || applicabilityCalculator.isOptionUsedInCommandLine(config, this, option)) {
				try{
				switch (option.getValueType()) {
				case IOption.BOOLEAN :
					String boolCmd;
					if (option.getBooleanValue()) {
						boolCmd = option.getCommand();
					} else {
						// Note: getCommandFalse is new with CDT 2.0
						boolCmd = option.getCommandFalse();
					}
					if (boolCmd != null && boolCmd.length() > 0) {
						sb.append(boolCmd);
					}
					break;
				
				case IOption.ENUMERATED :
					String enumVal = option.getEnumCommand(option.getSelectedEnum());
					if (enumVal.length() > 0) {
						sb.append(enumVal);
					}
					break;
				
				case IOption.STRING :
					String strCmd = option.getCommand();
					String val = option.getStringValue();
					macroSubstitutor.setMacroContextInfo(IBuildMacroProvider.CONTEXT_FILE,
						new FileContextData(inputFileLocation, outputFileLocation, option, getParent()));
					if (val.length() > 0
						&& (val = MacroResolver.resolveToString(val, macroSubstitutor)).length() > 0) {
						sb.append( evaluateCommand( strCmd, val ) );
					}
					break;
					
				case IOption.STRING_LIST :
					String listCmd = option.getCommand();
					macroSubstitutor.setMacroContextInfo(IBuildMacroProvider.CONTEXT_FILE,
						new FileContextData(inputFileLocation, outputFileLocation, option, getParent()));
					String[] list = MacroResolver.resolveStringListValues(option.getStringListValue(), macroSubstitutor, true);
					if(list != null){
						for (int j = 0; j < list.length; j++) {
							String temp = list[j];
							if(temp.length() > 0)
								sb.append( evaluateCommand( listCmd, temp ) + WHITE_SPACE );
						}
					}
					break;
					
				case IOption.INCLUDE_PATH :
					String incCmd = option.getCommand();
					macroSubstitutor.setMacroContextInfo(IBuildMacroProvider.CONTEXT_FILE,
						new FileContextData(inputFileLocation, outputFileLocation, option, getParent()));
					String[] paths = MacroResolver.resolveStringListValues(option.getIncludePaths(), macroSubstitutor, true);
					if(paths != null){
						for (int j = 0; j < paths.length; j++) {
							String temp = paths[j];
							if(temp.length() > 0)
								sb.append( evaluateCommand( incCmd, temp ) + WHITE_SPACE);
						}
					}
					break;
	
				case IOption.PREPROCESSOR_SYMBOLS :
					String defCmd = option.getCommand();
					macroSubstitutor.setMacroContextInfo(IBuildMacroProvider.CONTEXT_FILE,
						new FileContextData(inputFileLocation, outputFileLocation, option, getParent()));
					String[] symbols = MacroResolver.resolveStringListValues(option.getDefinedSymbols(), macroSubstitutor, true);
					if(symbols != null){
						for (int j = 0; j < symbols.length; j++) {
							String temp = symbols[j];
							if(temp.length() > 0)
								sb.append( evaluateCommand( defCmd, temp ) + WHITE_SPACE);
						}
					}
					break;
	
				default :
					break;
				}
					
				if (sb.toString().trim().length() > 0)
					flags.add(sb.toString().trim());
				} catch (BuildMacroException e) {
					
				}
			}
		}
		String[] f = new String[ flags.size() ];
		return (String[])flags.toArray( f );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getToolCommandFlags(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath)
	 */
	public String[] getToolCommandFlags(IPath inputFileLocation, IPath outputFileLocation) throws BuildException{
		IMacroSubstitutor macroSubstitutor = new BuildfileMacroSubstitutor(null,EMPTY_STRING,WHITE_SPACE);
		return getToolCommandFlags(inputFileLocation, outputFileLocation, macroSubstitutor );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getToolCommandFlagsString(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath)
	 */
	public String getToolCommandFlagsString(IPath inputFileLocation, IPath outputFileLocation) throws BuildException{
		// Get all of the optionList
		StringBuffer buf = new StringBuffer();
		String[] flags = getToolCommandFlags(inputFileLocation,outputFileLocation);
		for (int index = 0; index < flags.length; index++) {
			if( flags[ index ] != null ) { 
				buf.append( flags[ index ] + WHITE_SPACE );
			}
		}

		return buf.toString().trim();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#isHeaderFile(java.lang.String)
	 */
	public boolean isHeaderFile(String ext) {
		if (ext == null) {
			return false;
		}
		String[] exts = getAllDependencyExtensions();
		for (int i=0; i<exts.length; i++) {
			if (ext.equals(exts[i])) return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#buildsFileType(java.lang.String)
	 */
	public boolean buildsFileType(String extension) {
		if (extension == null)  { 
			return false;
		}
		if (getInputType(extension) != null) {
			return true;
		}
		//  If no InputTypes, check the attribute
		if (!hasInputTypes()) {
			return getInputExtensionsAttribute().contains(extension);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#producesFileType(java.lang.String)
	 */
	public boolean producesFileType(String extension) {
		if (extension == null)  { 
			return false;
		}
		//  Check the output-types first
		if (getOutputType(extension) != null) {
			return true;
		}
		//  If there are no OutputTypes, check the attribute
		if (!hasOutputTypes()) {
			String[] exts = getOutputsAttribute();
			if (exts != null) {
				for (int i = 0; i < exts.length; i++) {
					if (exts[i].equals(extension))
						return true;
				}
			}
		}
		return false;
	}

/*
 *  O B J E C T   S T A T E   M A I N T E N A N C E
 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionTool;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension tool
 		if (isExtensionTool) return false;
		
		// If I need saving, just say yes
		if (isDirty) return true;
		
		// Check my children
		List typeElements = getInputTypeList();
		Iterator iter = typeElements.listIterator();
		while (iter.hasNext()) {
			InputType type = (InputType) iter.next();
			if (type.isDirty()) return true;
		}
		typeElements = getOutputTypeList();
		iter = typeElements.listIterator();
		while (iter.hasNext()) {
			OutputType type = (OutputType) iter.next();
			if (type.isDirty()) return true;
		}
		
		// Otherwise see if any options need saving
		if (super.isDirty()) {
			return true;
		}
		
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
		// Propagate "false" to options
		super.setDirty(isDirty);
		// Propagate "false" to the children
		if (!isDirty) {
			List typeElements = getInputTypeList();
			Iterator iter = typeElements.listIterator();
			while (iter.hasNext()) {
				InputType type = (InputType) iter.next();
				type.setDirty(false);
			}
			typeElements = getOutputTypeList();
			iter = typeElements.listIterator();
			while (iter.hasNext()) {
				OutputType type = (OutputType) iter.next();
				type.setDirty(false);
			}
		}
	}

	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Resolve superClass
			if (superClassId != null && superClassId.length() > 0) {
				setSuperClass( ManagedBuildManager.getExtensionTool(superClassId) );
				if (getSuperClass() == null) {
					// Report error
					ManagedBuildManager.OutputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"tool",	//$NON-NLS-1$
							getId());
				}
			}
			//  Resolve HoldsOptions 
			super.resolveReferences();
			//  Call resolveReferences on our children
			Iterator typeIter = getInputTypeList().iterator();
			while (typeIter.hasNext()) {
				InputType current = (InputType)typeIter.next();
				current.resolveReferences();
			}
			typeIter = getOutputTypeList().iterator();
			while (typeIter.hasNext()) {
				OutputType current = (OutputType)typeIter.next();
				current.resolveReferences();
			}
		}		
	}
	
	private String evaluateCommand( String command, String values ) {
	    if( command == null ) return values.trim();
	    if( command.indexOf( "${" ) >= 0 ) { //$NON-NLS-1$
	    	return command.replaceAll("\\$\\{[vV][aA][lL][uU][eE]\\}", values.trim() ).trim(); //$NON-NLS-1$
	    }
	    else {
	    	return (new String(command + values)).trim();
	    }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getConvertToId()
	 */
	public String getConvertToId() {
		if (convertToId == null) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				return getSuperClass().getConvertToId();
			} else {
				return EMPTY_STRING;
			}
		}
		return convertToId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setConvertToId(String)
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getVersionsSupported()
	 */
	public String getVersionsSupported() {
		if (versionsSupported == null) {
			// If I have a superClass, ask it
			if (getSuperClass() != null) {
				return getSuperClass().getVersionsSupported();
			} else {
				return EMPTY_STRING;
			}
		}
		return versionsSupported;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setVersionsSupported(String)
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getEnvVarBuildPaths()
	 */
	public IEnvVarBuildPath[] getEnvVarBuildPaths(){
		if(envVarBuildPathList != null){
			return (IEnvVarBuildPath[])envVarBuildPathList.toArray(
					new IEnvVarBuildPath[envVarBuildPathList.size()]);
		}
		else if(getSuperClass() != null)
			return getSuperClass().getEnvVarBuildPaths();
		return null;
	}
	
	private void addEnvVarBuildPath(IEnvVarBuildPath path){
		if(path == null)
			return;
		if(envVarBuildPathList == null)
			envVarBuildPathList = new ArrayList();
			
		envVarBuildPathList.add(path);
	}

	/*
	 * This function checks for migration support for the tool, while
	 * loading. If migration support is needed, looks for the available
	 * converters and stores them.
	 */

	private void checkForMigrationSupport() {

		boolean isExists = false;
	
		if ( getSuperClass() == null) {
			// If 'getSuperClass()' is null, then there is no tool available in
			// plugin manifest file with the same 'id' & version.
			// Look for the 'versionsSupported' attribute
			String high = (String) ManagedBuildManager.getExtensionToolMap()
					.lastKey();

			SortedMap subMap = null;
			if (superClassId.compareTo(high) <= 0) {
				subMap = ManagedBuildManager.getExtensionToolMap().subMap(
						superClassId, high + "\0"); //$NON-NLS-1$
			} else {
				// It means there are no entries in the map for the given id.
				// make the project is invalid

				// It means there are no entries in the map for the given id.
				// make the project is invalid
				// If the parent is a tool chain
				IToolChain parent = (IToolChain) getParent();
				IConfiguration parentConfig = parent.getParent();
				IManagedProject managedProject = parentConfig
						.getManagedProject();
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

			ITool[] toolElements = (ITool[]) subMap.values().toArray();

			for (int i = 0; i < toolElements.length; i++) {
				ITool toolElement = toolElements[i];

				if (ManagedBuildManager.getIdFromIdAndVersion(
						toolElement.getId()).compareTo(baseId) > 0)
					break;

				// First check if both base ids are equal
				if (ManagedBuildManager.getIdFromIdAndVersion(
						toolElement.getId()).equals(baseId)) {

					// Check if 'versionsSupported' attribute is available'
					String versionsSupported = toolElement
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
										.getVersionFromIdAndVersion(toolElement
												.getId());
								setId(ManagedBuildManager
										.getIdFromIdAndVersion(getId())
										+ "_" + supportedVersion); //$NON-NLS-1$

								// If control comes here means that superClass
								// is null.
								// So, set the superClass to this tool element
								setSuperClass(toolElement);
								superClassId = getSuperClass().getId();
								isExists = true;
								break;
							}
						}
						if (isExists)
							break; // break the outer for loop if 'isExists' is
									// true
					}					
				}
			}
		}
		
		if (getSuperClass() != null) {
			// If 'getSuperClass()' is not null, look for 'convertToId'
			// attribute in plugin
			// manifest file for this tool.
			String convertToId = getSuperClass().getConvertToId();
			if ((convertToId == null) || (convertToId.equals(""))) { //$NON-NLS-1$
				// It means there is no 'convertToId' attribute available and
				// the version is still actively
				// supported by the tool integrator. So do nothing, just return
				return;
			} else {
				// Incase the 'convertToId' attribute is available,
				// it means that Tool integrator currently does not support this
				// version of tool.
				// Look for the converters available for this tool version.

				getConverter(convertToId);
			}

		} else {
			// make the project is invalid
			// 
			// It means there are no entries in the map for the given id.
			// make the project is invalid
			IToolChain parent = (IToolChain) getParent();
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
						// the selected tool

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

		// It means there are no entries in the map for the given id.
		// make the project is invalid
		IToolChain parent = (IToolChain) getParent();
		IConfiguration parentConfig = parent.getParent();
		IManagedProject managedProject = parentConfig.getManagedProject();
		if (managedProject != null) {
			managedProject.setValid(false);
		}
		return;
	}

	public IConfigurationElement getPreviousMbsVersionConversionElement() {
		return previousMbsVersionConversionElement;
	}

	public IConfigurationElement getCurrentMbsVersionConversionElement() {
		return currentMbsVersionConversionElement;
	}

	public IProject getProject() {
		IBuildObject toolParent = getParent();
		if (toolParent != null) {
			if (toolParent instanceof IToolChain) {
				IConfiguration config = ((IToolChain)toolParent).getParent();
				if (config == null) return null;
				return (IProject)config.getOwner();
			} else if (toolParent instanceof IResourceConfiguration) {
				return (IProject)((IResourceConfiguration)toolParent).getOwner();
			}
		}
		return null;
	}

	public String[] getContentTypeFileSpecs (IContentType type) {
		String[] globalSpecs = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC); 
		IContentTypeSettings settings = null;
		IProject project = getProject();
		if (project != null) {
			IScopeContext projectScope = new ProjectScope(project);
			try {
				settings = type.getSettings(projectScope);
			} catch (Exception e) {}
			if (settings != null) {
				String[] specs = settings.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				if (specs.length > 0) {
					int total = globalSpecs.length + specs.length;
					String[] projSpecs = new String[total];
					int i=0;
					for (int j=0; j<specs.length; j++) {
						projSpecs[i] = specs[j];
						i++;
					}								
					for (int j=0; j<globalSpecs.length; j++) {
						projSpecs[i] = globalSpecs[j];
						i++;
					}								
					return projSpecs;
				}
			}
		}
		return globalSpecs;		
	}

}
