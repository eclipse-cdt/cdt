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
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolReference;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

/**
 * Represents a tool that can be invoked during a build.
 * Note that this class implements IOptionCategory to represent the top
 * category.
 */
public class Tool extends BuildObject implements ITool, IOptionCategory {

	private static final String DEFAULT_SEPARATOR = ","; //$NON-NLS-1$
	private static final IOptionCategory[] EMPTY_CATEGORIES = new IOptionCategory[0];
	private static final IOption[] EMPTY_OPTIONS = new IOption[0];

	private Vector categoryIds;
	private Map categoryMap;
	private List childOptionCategories;
	private String command;
	private List inputExtensions;
	private List interfaceExtensions;
	private int natureFilter;
	private Vector optionList;
	private Map optionMap;
	private String outputExtensions;
	private String outputFlag;
	private String outputPrefix;
	private boolean resolved = true;
	

	/**
	 * Constructor to create a tool based on an element from the plugin
	 * manifest. 
	 * 
	 * @param element The element containing the information about the tool.
	 */
	public Tool(IManagedConfigElement element) {
		loadFromManifest(element);

		// hook me up
		ManagedBuildManager.addExtensionTool(this);
	}
	
	/**
	 * Constructor to create a new tool for a target based on the information
	 * defined in the plugin.xml manifest. 
	 * 
	 * @param target The target the receiver will belong to.
	 * @param element The element containing the information about the tool.
	 */
	public Tool(Target target, IManagedConfigElement element) {
		loadFromManifest(element);
		
		// hook me up
		target.addTool(this);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#handlesFileType(java.lang.String)
	 */
	public boolean buildsFileType(String extension) {
		if (extension == null)  { 
			return false;
		}
		return getInputExtensions().contains(extension);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOptions()
	 */
	public IOption[] getOptions() {
		return (IOption[])getOptionList().toArray(new IOption[getOptionList().size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getTopOptionCategory()
	 */
	public IOptionCategory getTopOptionCategory() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getChildCategories()
	 */
	public IOptionCategory[] getChildCategories() {
		if (childOptionCategories != null)
			return (IOptionCategory[])childOptionCategories.toArray(new IOptionCategory[childOptionCategories.size()]);
		else
			return EMPTY_CATEGORIES;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getInputExtensions()
	 */
	public List getInputExtensions() {
		if (inputExtensions == null) {
			inputExtensions = new ArrayList();
		}
		return inputExtensions;
	}
	
	private List getInterfaceExtensions() {
		if (interfaceExtensions == null) {
			interfaceExtensions = new ArrayList();
		}
		return interfaceExtensions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutputFlag()
	 */
	public String getOutputFlag() {
		return outputFlag == null ? new String() : outputFlag.trim();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutputPrefix()
	 */
	public String getOutputPrefix() {
		return outputPrefix == null ? new String() : outputPrefix.trim();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOwner()
	 */
	public IOptionCategory getOwner() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getTool()
	 */
	public ITool getTool() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getToolCommand()
	 */
	public String getToolCommand() {
		return command.trim();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getToolFlags()
	 */
	public String getToolFlags() throws BuildException {
		// Get all of the optionList
		StringBuffer buf = new StringBuffer();
		IOption[] opts = getOptions();
		for (int index = 0; index < opts.length; index++) {
			IOption option = opts[index];
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
						buf.append(boolCmd + WHITE_SPACE);
					}
					break;
				
				case IOption.ENUMERATED :
					String enum = option.getEnumCommand(option.getSelectedEnum());
					if (enum.length() > 0) {
						buf.append(enum + WHITE_SPACE);
					}
					break;
				
				case IOption.STRING :
					String strCmd = option.getCommand();
					String val = option.getStringValue();
					if (val.length() > 0) {
						if (strCmd != null) buf.append(strCmd);
						buf.append(val + WHITE_SPACE);
					}
					break;
					
				case IOption.STRING_LIST :
					String listCmd = option.getCommand();
					String[] list = option.getStringListValue();
					for (int j = 0; j < list.length; j++) {
						String temp = list[j];
						if (listCmd != null) buf.append(listCmd);
						buf.append(temp + WHITE_SPACE);
					}
					break;
					
				case IOption.INCLUDE_PATH :
					String incCmd = option.getCommand();
					String[] paths = option.getIncludePaths();
					for (int j = 0; j < paths.length; j++) {
						String temp = paths[j];
						buf.append(incCmd + temp + WHITE_SPACE);
					}
					break;

				case IOption.PREPROCESSOR_SYMBOLS :
					String defCmd = option.getCommand();
					String[] symbols = option.getDefinedSymbols();
					for (int j = 0; j < symbols.length; j++) {
						String temp = symbols[j];
						buf.append(defCmd + temp + WHITE_SPACE);
					}
					break;

				default :
					break;
			}

		}

		return buf.toString().trim();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOptions(org.eclipse.cdt.core.build.managed.ITool)
	 */
	public IOption[] getOptions(IConfiguration configuration) {
		ITool tool = this;
		if (configuration != null) {
			// TODO don't like this much
			ITool[] tools = configuration.getTools();
			for (int i = 0; i < tools.length; ++i) {
				if (tools[i] instanceof IToolReference) {
					if (((IToolReference)tools[i]).references(tool)) {
						tool = tools[i];
						break;
					}
				} else if (tools[i].equals(tool))
					break;
			}
		}

		IOption[] allOptions = tool.getOptions();
		ArrayList myOptions = new ArrayList();
			
		for (int i = 0; i < allOptions.length; ++i) {
			IOption option = allOptions[i];
			if (option.getCategory().equals(this))
				myOptions.add(option);
		}

		myOptions.trimToSize();
		return (IOption[])myOptions.toArray(new IOption[myOptions.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getNatureFilter()
	 */
	public int getNatureFilter() {
		return natureFilter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOption(java.lang.String)
	 */
	public IOption getOption(String id) {
		return getOptionById(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOption(java.lang.String)
	 */
	public IOption getOptionById(String id) {
		return (IOption)getOptionMap().get(id);
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOutputExtensions()
	 */
	public String[] getOutputExtensions() {
		return outputExtensions.split(DEFAULT_SEPARATOR);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutput(java.lang.String)
	 */
	public String getOutputExtension(String inputExtension) {
		// Examine the list of input extensions
		ListIterator iter = getInputExtensions().listIterator();
		while (iter.hasNext()) {
			if (((String)iter.next()).equals(inputExtension)) {
				return outputExtensions;
			}
		}
		return null;
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
	 * Load the tool information from the XML element specified in the 
	 * argument
	 * @param element An XML element containing the tool information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		this.resolved = false;
		
		// id		
		setId(element.getAttribute(ITool.ID));
		
		// name
		setName(element.getAttribute(ITool.NAME));
		
		// Get the nature filter
		String nature = element.getAttribute(NATURE);
		if (nature == null || "both".equals(nature)) {	//$NON-NLS-1$
			natureFilter = FILTER_BOTH;
		} else if ("cnature".equals(nature)) {	//$NON-NLS-1$
			natureFilter = FILTER_C;
		} else if ("ccnature".equals(nature)) {	//$NON-NLS-1$
			natureFilter = FILTER_CC;
		} else {
			natureFilter = FILTER_BOTH;
		}
		
		// Get the supported input file extension
		String inputs = element.getAttribute(ITool.SOURCES) == null ? 
			new String() : 
			element.getAttribute(ITool.SOURCES);
		StringTokenizer tokenizer = new StringTokenizer(inputs, DEFAULT_SEPARATOR);
		while (tokenizer.hasMoreElements()) {
			getInputExtensions().add(tokenizer.nextElement());
		}
		
		// Get the interface (header file) extensions
		String headers = element.getAttribute(INTERFACE_EXTS);
		if (headers == null) {
			headers = new String();
		}
		tokenizer = new StringTokenizer(headers, DEFAULT_SEPARATOR);
		while (tokenizer.hasMoreElements()) {
			getInterfaceExtensions().add(tokenizer.nextElement());
		}
		
		// Get the output extension
		outputExtensions = element.getAttribute(ITool.OUTPUTS) == null ? 
			new String() : 
			element.getAttribute(ITool.OUTPUTS);
			
		// Get the tool invocation
		command = element.getAttribute(ITool.COMMAND) == null ? 
			new String() : 
			element.getAttribute(ITool.COMMAND);
			
		// Get the flag to control output
		outputFlag = element.getAttribute(ITool.OUTPUT_FLAG) == null ?
			new String() :
			element.getAttribute(ITool.OUTPUT_FLAG);
			
		// Get the output prefix
		outputPrefix = element.getAttribute(ITool.OUTPUT_PREFIX) == null ?
			new String() :
			element.getAttribute(ITool.OUTPUT_PREFIX);

		// set up the category map
		addOptionCategory(this);

		// Check for optionList
		IManagedConfigElement[] toolElements = element.getChildren();
		for (int l = 0; l < toolElements.length; ++l) {
			IManagedConfigElement toolElement = toolElements[l];
			if (toolElement.getName().equals(ITool.OPTION)) {
				new Option(this, toolElement);
			} else if (toolElement.getName().equals(ITool.OPTION_CAT)) {
				new OptionCategory(this, toolElement);
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#producesFileType(java.lang.String)
	 */
	public boolean producesFileType(String outputExtension) {
		return this.outputExtensions.equals(outputExtension);
	}

	/**
	 * 
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Tool doesn't have any references, but children might
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
}
