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
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolReference;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

/**
 * 
 */
public class OptionCategory extends BuildObject implements IOptionCategory {

	private IOptionCategory owner;
	private List children;
	private Tool tool;
	private boolean resolved = true;

	private static final IOptionCategory[] emtpyCategories = new IOptionCategory[0];
	
	public OptionCategory(IOptionCategory owner) {
		this.owner = owner;
	}
	
	public OptionCategory(Tool tool, IManagedConfigElement element) {
		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		resolved = false;
		this.tool = tool;
		
		// id
		setId(element.getAttribute(IOptionCategory.ID));
		
		// Name
		setName(element.getAttribute(IOptionCategory.NAME));
		

		tool.addOptionCategory(this);
	}

	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			IManagedConfigElement element = ManagedBuildManager.getConfigElement(this);
			String parentId = element.getAttribute(IOptionCategory.OWNER);
			if (parentId != null)
				owner = tool.getOptionCategory(parentId);
			else
				owner = tool;
			
			// Hook me in
			if (owner instanceof Tool)
				((Tool)owner).addChildCategory(this);
			else
				((OptionCategory)owner).addChildCategory(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getChildCategories()
	 */
	public IOptionCategory[] getChildCategories() {
		if (children != null)
			return (IOptionCategory[])children.toArray(new IOptionCategory[children.size()]);
		else
			return emtpyCategories;
	}

	public void addChildCategory(OptionCategory category) {
		if (children == null)
			children = new ArrayList();
		children.add(category);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOwner()
	 */
	public IOptionCategory getOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getTool()
	 */
	public ITool getTool() {
		// This will stop at the Tool's top category
		return owner.getTool();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOptions(org.eclipse.cdt.core.build.managed.ITool)
	 */
	public IOption[] getOptions(IConfiguration configuration) {
		ITool tool = getTool();
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
		List myOptions = new ArrayList();
			
		for (int i = 0; i < allOptions.length; ++i) {
			IOption option = allOptions[i];
			if (option.getCategory().equals(this))
				myOptions.add(option);
		}

		return (IOption[])myOptions.toArray(new IOption[myOptions.size()]);
	}

}
