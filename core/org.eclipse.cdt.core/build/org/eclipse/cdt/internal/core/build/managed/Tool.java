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
package org.eclipse.cdt.internal.core.build.managed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Represents a tool that can be invoked during a build.
 * Note that this class implements IOptionCategory to represent the top
 * category.
 */
public class Tool extends BuildObject implements ITool, IOptionCategory {

	private ITarget target;
	private List options;
	private Map optionMap;
	private List childOptionCategories;
	private Map categoryMap;
	
	private static IOption[] emptyOptions = new IOption[0];
	private static IOptionCategory[] emptyCategories = new IOptionCategory[0];
	
	public Tool(Target target) {
		this.target = target;
	}
	
	public Tool(Target target, IConfigurationElement element) {
		this(target);

		// id		
		setId(element.getAttribute("id"));
		
		// hook me up
		target.addTool(this);

		// name
		setName(element.getAttribute("name"));

		// set up the category map
		categoryMap = new HashMap();
		addOptionCategory(this);

		// Check for options
		IConfigurationElement[] toolElements = element.getChildren();
		for (int l = 0; l < toolElements.length; ++l) {
			IConfigurationElement toolElement = toolElements[l];
			if (toolElement.getName().equals("option")) {
				new Option(this, toolElement);
			} else if (toolElement.getName().equals("optionCategory")) {
				new OptionCategory(this, toolElement);
			}
		}
	}
	
	public ITarget getTarget() {
		return target;	
	}
	
	public IOptionCategory getOptionCategory(String id) {
		return (IOptionCategory)categoryMap.get(id);
	}
	
	void addOptionCategory(IOptionCategory category) {
		categoryMap.put(category.getId(), category);
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
			return emptyOptions;
	}

	public void addOption(Option option) {
		if (options == null)
			options = new ArrayList();
		options.add(option);
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
			return emptyCategories;
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
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOptions(org.eclipse.cdt.core.build.managed.ITool)
	 */
	public IOption[] getOptions(IConfiguration configuration) {
		ITool tool = this;
		if (configuration != null) {
			// TODO don't like this much
			ITool[] tools = configuration.getTools();
			for (int i = 0; i < tools.length; ++i) {
				if (tools[i] instanceof ToolReference) {
					if (((ToolReference)tools[i]).references(tool)) {
						tool = tools[i];
						break;
					}
				} else if (tools[i].equals(tool))
					break;
			}
		}

		IOption[] allOptions = tool.getOptions();
		List myOptions = new ArrayList();
			
		for (int i = 0; i < allOptions.length; ++i) {
			IOption option = allOptions[i];
			if (option.getCategory().equals(this))
				myOptions.add(option);
		}

		return (IOption[])myOptions.toArray(new IOption[myOptions.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOption(java.lang.String)
	 */
	public IOption getOption(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
