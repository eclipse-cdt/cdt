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
package org.eclipse.cdt.managedbuilder.core;

/**
 * 
 */
public interface IOptionCategory extends IBuildObject {

	// Schema element names
	public static final String OWNER = "owner"; //$NON-NLS-1$

	/**
	 * Returns the list of children of this node in the option category tree
	 * 
	 * @return
	 */
	public IOptionCategory[] getChildCategories();
	
	/**
	 * Returns the options in this category for a given configuration.
	 * 
	 * @param tool
	 * @return
	 */
	public IOption[] getOptions(IConfiguration configuration);

	/**
	 * Returns the category that owns this category, or null if this is the
	 * top category for a tool.
	 * 
	 * @return
	 */
	public IOptionCategory getOwner();
	
	/**
	 * Returns the tool that ultimately owns this category.
	 * 
	 * @return
	 */
	public ITool getTool();
}
