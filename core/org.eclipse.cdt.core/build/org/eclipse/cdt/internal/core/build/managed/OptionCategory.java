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
import org.eclipse.cdt.core.build.managed.ITool;

/**
 * 
 */
public class OptionCategory extends BuildObject implements IOptionCategory {

	private IOptionCategory owner;
	private List children;

	private static final IOptionCategory[] emtpyCategories = new IOptionCategory[0];
	
	public OptionCategory(IOptionCategory owner) {
		this.owner = owner;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#createChildCategory()
	 */
	public IOptionCategory createChildCategory() {
		IOptionCategory category = new OptionCategory(this);
		
		if (children == null)
			children = new ArrayList();
		children.add(category);
		
		return category;
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
	public IOption[] getOptions(ITool tool) {
		List myOptions = new ArrayList();
		IOption[] allOptions = tool.getOptions();
		
		for (int i = 0; i < allOptions.length; ++i) {
			IOption option = allOptions[i];
			if (option.getCategory().equals(this))
				myOptions.add(option);
		}
		
		return (IOption[])myOptions.toArray(new IOption[myOptions.size()]);
	}

}
