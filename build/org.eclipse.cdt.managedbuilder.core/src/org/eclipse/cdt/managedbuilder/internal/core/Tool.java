/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a tool that can be invoked during a build.
 * Note that this class implements IOptionCategory to represent the top
 * category.
 */
public class Tool extends BuildObject implements ITool, IOptionCategory {

	public static final String DEFAULT_PATTERN = "${COMMAND} ${FLAGS} ${OUTPUT_FLAG}${OUTPUT_PREFIX}${OUTPUT} ${INPUTS}"; //$NON-NLS-1$

	private static final String DEFAULT_SEPARATOR = ","; //$NON-NLS-1$
	private static final IOptionCategory[] EMPTY_CATEGORIES = new IOptionCategory[0];
	private static final IOption[] EMPTY_OPTIONS = new IOption[0];
	private static final String EMPTY_STRING = new String();

	//  Superclass
	private ITool superClass;
	private String superClassId;
	//  Parent and children
	private IBuildObject parent;
	private Vector categoryIds;
	private Map categoryMap;
	private List childOptionCategories;
	private Vector optionList;
	private Map optionMap;
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
	private IConfigurationElement commandLineGeneratorElement = null;
	private IManagedCommandLineGenerator commandLineGenerator = null;
	private IConfigurationElement dependencyGeneratorElement = null;
	private IManagedDependencyGenerator dependencyGenerator = null;
	//  Miscellaneous
	private boolean isExtensionTool = false;
	private boolean isDirty = false;
	private boolean resolved = true;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * Constructor to create a tool based on an element from the plugin
	 * manifest. 
	 * 
	 * @param element The element containing the information about the tool.
	 */
	public Tool(IManagedConfigElement element) {
		isExtensionTool = true;
		
		// setup for resolving
		resolved = false;

		loadFromManifest(element);

		// hook me up
		ManagedBuildManager.addExtensionTool(this);

		// set up the category map
		addOptionCategory(this);

		// Check for optionList
		IManagedConfigElement[] toolElements = element.getChildren();
		for (int l = 0; l < toolElements.length; ++l) {
			IManagedConfigElement toolElement = toolElements[l];
			if (toolElement.getName().equals(ITool.OPTION)) {
				Option option = new Option(this, toolElement);
				addOption(option);
			} else if (toolElement.getName().equals(ITool.OPTION_CAT)) {
				new OptionCategory(this, toolElement);
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
	 */
	public Tool(IBuildObject parent, IManagedConfigElement element) {
		this(element);
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
		this.parent = parent;
		this.superClass = superClass;
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
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
		this.parent = parent;
		this.superClass = superClass;
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
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
	 */
	public Tool(IBuildObject parent, Element element) {
		this.parent = parent;
		isExtensionTool = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);

		// set up the category map
		addOptionCategory(this);

		// Check for optionList
		NodeList toolElements = element.getChildNodes();
		for (int i = 0; i < toolElements.getLength(); ++i) {
			Node toolElement = toolElements.item(i);
			if (toolElement.getNodeName().equals(ITool.OPTION)) {
				Option option = new Option(this, (Element)toolElement);
				addOption(option);
			} else if (toolElement.getNodeName().equals(ITool.OPTION_CAT)) {
				new OptionCategory(this, (Element)toolElement);
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
		this.parent = parent;
		if (toolSuperClass != null) {
			superClass = toolSuperClass;
		} else {
		    superClass = tool.superClass;
		}
		if (superClass != null) {
			superClassId = superClass.getId();
		}
		setId(Id);
		setName(name);
		isExtensionTool = false;
		
		//  Copy the remaining attributes
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

		commandLineGeneratorElement = tool.commandLineGeneratorElement; 
		commandLineGenerator = tool.commandLineGenerator; 
		dependencyGeneratorElement = tool.dependencyGeneratorElement; 
		dependencyGenerator = tool.dependencyGenerator; 

		//  Clone the children
		//  Note: This constructor ignores OptionCategories since they should not be
		//        found on an non-extension tool
		if (tool.optionList != null) {
			Iterator iter = tool.getOptionList().listIterator();
			while (iter.hasNext()) {
				Option option = (Option) iter.next();
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId;
				String subName;
				if (option.getSuperClass() != null) {
					subId = option.getSuperClass().getId() + "." + nnn;		//$NON-NLS-1$
					subName = option.getSuperClass().getName();
				} else {
					subId = option.getId() + "." + nnn;		//$NON-NLS-1$
					subName = option.getName();
				}
				Option newOption = new Option(this, subId, subName, option);
				addOption(newOption);
			}
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
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);

		// Get the unused children, if any
		unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		
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
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		if (superClassId != null && superClassId.length() > 0) {
			if( getParent() instanceof IResourceConfiguration ) {
				IResourceConfiguration resConfig = (IResourceConfiguration) getParent();
				superClass = resConfig.getParent().getTool(superClassId);
			} else {
				superClass = ManagedBuildManager.getExtensionTool(superClassId);
			}
			if (superClass == null) {
				// TODO:  Report error
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
	}

	/**
	 * Persist the tool to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		try {
			if (superClass != null)
				element.setAttribute(IProjectType.SUPERCLASS, superClass.getId());
			
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
			
			// Serialize my children
			if (childOptionCategories != null) {
				Iterator iter = childOptionCategories.listIterator();
				while (iter.hasNext()) {
					OptionCategory optCat = (OptionCategory)iter.next();
					Element optCatElement = doc.createElement(OPTION);
					element.appendChild(optCatElement);
					optCat.serialize(doc, optCatElement);
				}
			}
			List optionElements = getOptionList();
			Iterator iter = optionElements.listIterator();
			while (iter.hasNext()) {
				Option option = (Option) iter.next();
				Element optionElement = doc.createElement(OPTION);
				element.appendChild(optionElement);
				option.serialize(doc, optionElement);
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#createOption(IOption, String, String, boolean)
	 */
	public IOption createOption(IOption superClass, String Id, String name, boolean isExtensionElement) {
		Option option = new Option(this, superClass, Id, name, isExtensionElement);
		addOption(option);
		setDirty(true);
		return option;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#createOption(IOption, String, String, boolean)
	 */
	public void removeOption(IOption option) {
		getOptionList().remove(option);
		getOptionMap().remove(option.getId());
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOptions()
	 */
	public IOption[] getOptions() {
		IOption[] options = null;
		// Merge our options with our superclass' options.
		if (superClass != null) {
			options = superClass.getOptions();
		}
		// Our options take precedence.
		Vector ourOpts = getOptionList();
		if (options != null) {
			for (int i = 0; i < ourOpts.size(); i++) {
				IOption ourOpt = (IOption)ourOpts.get(i);
				int j;
				for (j = 0; j < options.length; j++) {
					if (options[j].overridesOnlyValue()) {
						if (ourOpt.getSuperClass().getId().equals(options[j].getSuperClass().getId())) {
							options[j] = ourOpt;
							break;
						}
					} else {
						if (ourOpt.getSuperClass().getId().equals(options[j].getId())) {
							options[j] = ourOpt;
							break;
						}
					}
				}
				//  No Match?  Add it.
				if (j == options.length) {
					IOption[] newOptions = new IOption[options.length + 1];
					for (int k = 0; k < options.length; k++) {
						newOptions[k] = options[k];
					}						 
					newOptions[j] = ourOpt;
					options = newOptions;
				}
			}
		} else {
			options = (IOption[])ourOpts.toArray(new IOption[ourOpts.size()]);
		}
		return options;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getTopOptionCategory()
	 */
	public IOptionCategory getTopOptionCategory() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOption(java.lang.String)
	 */
	public IOption getOption(String id) {
		return getOptionById(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOptionById(java.lang.String)
	 */
	public IOption getOptionById(String id) {
		IOption opt = (IOption)getOptionMap().get(id);
		if (opt == null) {
			if (superClass != null) {
				return superClass.getOptionById(id);
			}
		}
		return opt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getChildCategories()
	 */
	public IOptionCategory[] getChildCategories() {
		if (childOptionCategories != null)
			return (IOptionCategory[])childOptionCategories.toArray(new IOptionCategory[childOptionCategories.size()]);
		else {
			if (superClass != null) {
				return superClass.getChildCategories();
			} else {
				return EMPTY_CATEGORIES;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOwner()
	 */
	public IOptionCategory getOwner() {
		return null;
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
	 * Memory-safe way to access the vector of category IDs
	 */
	private Vector getCategoryIds() {
		if (categoryIds == null) {
			categoryIds = new Vector();
		}
		return categoryIds;
	}
	
	/**
	 * @param category
	 */
	public void addChildCategory(IOptionCategory category) {
		if (childOptionCategories == null)
			childOptionCategories = new ArrayList();
		childOptionCategories.add(category);
	}
	
	/**
	 * @param option
	 */
	public void addOption(Option option) {
		getOptionList().add(option);
		getOptionMap().put(option.getId(), option);
	}
	
	protected void addOptionCategory(IOptionCategory category) {
		// To preserve the order of the categories, record the ids in the order they are read
		getCategoryIds().add(category.getId());
		// Map the categories by ID for resolution later
		getCategoryMap().put(category.getId(), category);
	}

	/**
	 * Answers the <code>IOptionCategory</code> that has the unique identifier 
	 * specified in the argument. 
	 * 
	 * @param id The unique identifier of the option category
	 * @return <code>IOptionCategory</code> with the id specified in the argument
	 */
	public IOptionCategory getOptionCategory(String id) {
		return (IOptionCategory)getCategoryMap().get(id);
	}
	
	/* (non-Javadoc)
	 * Memeory-safe way to access the map of category IDs to categories
	 */
	private Map getCategoryMap() {
		if (categoryMap == null) {
			categoryMap = new HashMap();
		}
		return categoryMap;
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of options
	 */
	private Vector getOptionList() {
		if (optionList == null) {
			optionList = new Vector();
		}
		return optionList;
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of IDs to options
	 */
	private Map getOptionMap() {
		if (optionMap == null) {
			optionMap = new HashMap();
		}
		return optionMap;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getSuperClass()
	 */
	public ITool getSuperClass() {
		return superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getName()
	 */
	public String getName() {
		return (name == null && superClass != null) ? superClass.getName() : name;
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
			if (superClass != null) {
				ids = superClass.getErrorParserIds();
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
	 */
	public List getInputExtensions() {
		if( (inputExtensions == null) || ( inputExtensions.size() == 0) ) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getInputExtensions();
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
	
	public List getInterfaceExtensions() {
		if (interfaceExtensions == null || interfaceExtensions.size() == 0) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getInterfaceExtensions();
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
			if (superClass != null) {
				return superClass.getOutputFlag();
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
		if (outputPrefix == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getOutputPrefix();
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
			if (superClass != null) {
				return superClass.getToolCommand();
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
		if (commandLinePattern != null) {
			return commandLinePattern;
		} else {
			return new String(DEFAULT_PATTERN);  // Default pattern
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getCommandLineGeneratorElement()
	 */
	public IConfigurationElement getCommandLineGeneratorElement() {
		if (commandLineGeneratorElement == null) {
			if (superClass != null) {
				return superClass.getCommandLineGeneratorElement();
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
	 */
	public IConfigurationElement getDependencyGeneratorElement() {
		if (dependencyGeneratorElement == null) {
			if (superClass != null) {
				return superClass.getDependencyGeneratorElement();
			}
		}
		return dependencyGeneratorElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setDependencyGeneratorElement(String)
	 */
	public void setDependencyGeneratorElement(IConfigurationElement element) {
		dependencyGeneratorElement = element;
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getDependencyGenerator()
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getNatureFilter()
	 */
	public int getNatureFilter() {
		if (natureFilter == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.getNatureFilter();
			} else {
				return FILTER_BOTH;
			}
		}
		return natureFilter.intValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOutputExtensions()
	 */
	public String[] getOutputExtensions() {
		// TODO:  Why is this treated differently than inputExtensions?
		if (outputExtensions == null) {
			if (superClass != null) {
				return superClass.getOutputExtensions();
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
		// Examine the list of input extensions
		ListIterator iter = getInputExtensions().listIterator();
		int i = 0;
		while (iter.hasNext()) {
			if (((String)iter.next()).equals(inputExtension)) {
				String[] exts = getOutputExtensions();
				if (exts != null) {
					if (i < exts.length) {
						return exts[i];
					} else {
						return exts[exts.length - 1];
					}
				}
			}
			i++;
		}
		return null;
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setOutputExtensions(java.lang.String)
	 */
	public void setOutputExtensions(String ext) {
		if (ext == null && outputExtensions == null) return;
		if (outputExtensions == null || ext == null || !(ext.equals(outputExtensions))) {
			outputExtensions = ext;
			isDirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getCommandFlags()
	 */
	public String[] getCommandFlags() throws BuildException {
		IOption[] opts = getOptions();
		ArrayList flags = new ArrayList();
		StringBuffer sb = new StringBuffer();
		for (int index = 0; index < opts.length; index++) {
			IOption option = opts[index];
			sb.setLength( 0 );
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
				String enum = option.getEnumCommand(option.getSelectedEnum());
				if (enum.length() > 0) {
					sb.append(enum);
				}
				break;
			
			case IOption.STRING :
				String strCmd = option.getCommand();
				String val = option.getStringValue();
				if (val.length() > 0) {
					sb.append( evaluateCommand( strCmd, val ) );
				}
				break;
				
			case IOption.STRING_LIST :
				String listCmd = option.getCommand();
				String[] list = option.getStringListValue();
				for (int j = 0; j < list.length; j++) {
					String temp = list[j];
					sb.append( evaluateCommand( listCmd, temp ) + WHITE_SPACE );
				}
				break;
				
			case IOption.INCLUDE_PATH :
				String incCmd = option.getCommand();
				String[] paths = option.getIncludePaths();
				for (int j = 0; j < paths.length; j++) {
					String temp = paths[j];
					sb.append( evaluateCommand( incCmd, temp ) + WHITE_SPACE);
				}
				break;

			case IOption.PREPROCESSOR_SYMBOLS :
				String defCmd = option.getCommand();
				String[] symbols = option.getDefinedSymbols();
				for (int j = 0; j < symbols.length; j++) {
					String temp = symbols[j];
					sb.append( evaluateCommand( defCmd, temp ) + WHITE_SPACE);
				}
				break;

			default :
				break;
			}
			if( sb.toString().trim().length() > 0 ) flags.add( sb.toString().trim() );
		}
		String[] f = new String[ flags.size() ];
		return (String[])flags.toArray( f );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getToolFlags()
	 */
	public String getToolFlags() throws BuildException {
		// Get all of the optionList
		StringBuffer buf = new StringBuffer();
		String[] flags = getCommandFlags();
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
		return getInterfaceExtensions().contains(ext);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#buildsFileType(java.lang.String)
	 */
	public boolean buildsFileType(String extension) {
		if (extension == null)  { 
			return false;
		}
		return getInputExtensions().contains(extension);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#producesFileType(java.lang.String)
	 */
	public boolean producesFileType(String outputExtension) {
		String[] exts = getOutputExtensions();
		if (exts != null) {
			for (int i = 0; i < exts.length; i++) {
				if (exts[i].equals(outputExtension))
					return true;
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
		
		// Otherwise see if any options need saving
		List optionElements = getOptionList();
		Iterator iter = optionElements.listIterator();
		while (iter.hasNext()) {
			Option option = (Option) iter.next();
			if (option.isDirty()) return true;
		}
		
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
		// Propagate "false" to the children
		if (!isDirty) {
			List optionElements = getOptionList();
			Iterator iter = optionElements.listIterator();
			while (iter.hasNext()) {
				Option option = (Option) iter.next();
				option.setDirty(false);
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
				superClass = ManagedBuildManager.getExtensionTool(superClassId);
				if (superClass == null) {
					// Report error
					ManagedBuildManager.OutputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"tool",	//$NON-NLS-1$
							getId());
				}
			}
			//  Call resolveReferences on our children
			Iterator optionIter = getOptionList().iterator();
			while (optionIter.hasNext()) {
				Option current = (Option)optionIter.next();
				current.resolveReferences();
			}
			// Somewhat wasteful, but use the vector to retrieve the categories in proper order
			Iterator catIter = getCategoryIds().iterator();
			while (catIter.hasNext()) {
				String id = (String)catIter.next();
				IOptionCategory current = (IOptionCategory)getCategoryMap().get(id);
				if (current instanceof Tool) {
					((Tool)current).resolveReferences();
				} else if (current instanceof OptionCategory) {
					((OptionCategory)current).resolveReferences();
				}
			}
		}		
	}
	
	private String evaluateCommand( String command, String values ) {
	    if( command == null ) return values.trim();
	    if( command.indexOf( "$(" ) > 0 ) return command.replaceAll( "\\$\\([value|Value|VALUE]\\)", values.trim() ).trim(); //$NON-NLS-1$ //$NON-NLS-2$
	    else return (new String(command + values)).trim();
	}
}
