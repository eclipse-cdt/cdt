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
import java.util.List;

import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;

/**
 * Represents a tool that can be invoked during a build.
 * Note that this class implements IOptionCategory to represent the top
 * category.
 */
public class Tool extends BuildObject implements ITool, IOptionCategory {

	private ITarget target;
	private List options;
	private IOptionCategory topOptionCategory;
	private List childOptionCategories;
	
	private static IOption[] emptyOptions = new IOption[0];
	private static IOptionCategory[] emptyCategories = new IOptionCategory[0];
	
	public Tool(Target target) {
		this.target = target;
	}
	
	public ITarget getTarget() {
		return target;	
	}
	
	public IOption[] getOptions() {
		if (options != null)
			return (IOption[])options.toArray(new IOption[options.size()]);
		else
			return emptyOptions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#createOption()
	 */
	public IOption createOption() {
		IOption option = new Option(this);
		
		if (options == null)
			options = new ArrayList();
		options.add(option);
		
		return option;
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
	public IOption[] getOptions(ITool tool) {
		List myOptions = new ArrayList();
		IOption[] allOptions = tool.getOptions();
		
		for (int i = 0; i < allOptions.length; ++i) {
			IOption option = allOptions[i];
			if (option.getCategory() == null || option.getCategory().equals(this))
				myOptions.add(option);
		}
		
		return (IOption[])myOptions.toArray(new IOption[myOptions.size()]);
	}

}
