/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolReference;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Represents a tool that can be invoked during a build.
 * Note that this class implements IOptionCategory to represent the top
 * category.
 */
public class Tool extends BuildObject implements ITool, IOptionCategory {

	private static final String DEFAULT_SEPARATOR = ","; //$NON-NLS-1$
	private static final IOptionCategory[] EMPTY_CATEGORIES = new IOptionCategory[0];
	private static final IOption[] EMPTY_OPTIONS = new IOption[0];

	private Map categoryMap;
	private List childOptionCategories;
	private String command;
	private List inputExtensions;
	private List interfaceExtensions;
	private int natureFilter;
	private Map optionMap;
	private List options;
	private String outputExtension;
	private String outputFlag;
	private String outputPrefix;
	private boolean resolved = true;
	

	public Tool(IConfigurationElement element) {
		loadFromManifest(element);

		// hook me up
		ManagedBuildManager.addExtensionTool(this);
	}
	
	/**
	 * Constructor to create a new tool for a target based on the information
	 * defined in the plugin.xml manifest. 
	 * 
	 * @param target The target the receiver will belong to.
	 * @param element The element containing the information.
	 */
	public Tool(Target target, IConfigurationElement element) {
		loadFromManifest(element);
		
		// hook me up
		target.addTool(this);
	}
	
	public IOptionCategory getOptionCategory(String id) {
		return (IOptionCategory)categoryMap.get(id);
	}
	
	void addOptionCategory(IOptionCategory category) {
		categoryMap.put(category.getId(), category);
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

	void addChildCategory(IOptionCategory category) {
		if (childOptionCategories == null)
			childOptionCategories = new ArrayList();
		childOptionCategories.add(category);
	}
	
	public IOption[] getOptions() {
		if (options != null)
			return (IOption[])options.toArray(new IOption[options.size()]);
		else
			return EMPTY_OPTIONS;
	}

	public void addOption(Option option) {
		if (options == null) {
			options = new ArrayList();
			optionMap = new HashMap();
		}
		options.add(option);
		optionMap.put(option.getId(), option);
	}
	
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
	 * Safe accessor method to retrieve the list of valid source extensions 
	 * the receiver know how to build.
	 * 
	 * @return List
	 */
	private List getInputExtensions() {
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
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#createChildCategory()
	 */
	public IOptionCategory createChildCategory() {
		IOptionCategory category = new OptionCategory(this);
		
		if (childOptionCategories == null)
			childOptionCategories = new ArrayList();
		childOptionCategories.add(category);
		
		return category;
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
		// Get all of the options
		StringBuffer buf = new StringBuffer();
		IOption[] opts = getOptions();
		for (int index = 0; index < opts.length; index++) {
			IOption option = opts[index];
			switch (option.getValueType()) {
				case IOption.BOOLEAN :
					if (option.getBooleanValue()) {
						buf.append(option.getCommand() + WHITE_SPACE);
					}
					break;
				
				case IOption.ENUMERATED :
					String enum = option.getEnumCommand(option.getSelectedEnum());
					if (enum.length() > 0) {
						buf.append(enum + WHITE_SPACE);
					}
					break;
				
				case IOption.STRING :
					String val = option.getStringValue();
					if (val.length() > 0) { 
						buf.append(val + WHITE_SPACE);
					}
					break;
					
				case IOption.STRING_LIST :
					String listCmd = option.getCommand();
					String[] list = option.getStringListValue();
					for (int j = 0; j < list.length; j++) {
						String temp = list[j];
						buf.append(listCmd + temp + WHITE_SPACE);
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
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOption(java.lang.String)
	 */
	public IOption getOption(String id) {
		return (IOption)optionMap.get(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutput(java.lang.String)
	 */
	public String getOutputExtension(String inputExtension) {
		// Examine the list of input extensions
		ListIterator iter = getInputExtensions().listIterator();
		while (iter.hasNext()) {
			if (((String)iter.next()).equals(inputExtension)) {
				return outputExtension;
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

	protected void loadFromManifest(IConfigurationElement element) {
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
		outputExtension = element.getAttribute(ITool.OUTPUTS) == null ? 
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
		categoryMap = new HashMap();
		addOptionCategory(this);

		// Check for options
		IConfigurationElement[] toolElements = element.getChildren();
		for (int l = 0; l < toolElements.length; ++l) {
			IConfigurationElement toolElement = toolElements[l];
			if (toolElement.getName().equals(ITool.OPTION)) {
				new Option(this, toolElement);
			} else if (toolElement.getName().equals(ITool.OPTION_CAT)) {
				new OptionCategory(this, toolElement);
			}
		}
		
	}
	
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
//			IConfigurationElement element = ManagedBuildManager.getConfigElement(this);
			// Tool doesn't have any references, but children might
			Iterator optionIter = options.iterator();
			while (optionIter.hasNext()) {
				Option current = (Option)optionIter.next();
				current.resolveReferences();
			}
			Iterator catIter = categoryMap.values().iterator();
			while (catIter.hasNext()) {
				IOptionCategory current = (IOptionCategory)catIter.next();
				if (current instanceof Tool) {
					((Tool)current).resolveReferences();
				} else if (current instanceof OptionCategory) {
					((OptionCategory)current).resolveReferences();
				}
			}
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#producesFileType(java.lang.String)
	 */
	public boolean producesFileType(String outputExtension) {
		return this.outputExtension.equals(outputExtension);
	}

}
