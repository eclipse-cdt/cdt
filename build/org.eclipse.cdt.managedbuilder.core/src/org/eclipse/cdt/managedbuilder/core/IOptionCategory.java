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
	
//	 Resource Filter type
	public static final int FILTER_ALL = 0;
	public static final String ALL = "all";	//$NON-NLS-1$
	public static final int FILTER_FILE = 1;
	public static final int FILTER_PROJECT = 2;
	public static final String PROJECT = "project";	//$NON-NLS-1$

	/**
	 * Returns the list of children of this node in the option category tree
	 * 
	 * @return
	 */
	public IOptionCategory[] getChildCategories();
	
	/**
	 * Returns an array of ITool/IOption pairs for the options in this category
	 * for a given configuration.
	 * 
	 * @param tool
	 * @return Object[][]
	 */
	public Object[][] getOptions(IConfiguration configuration);
	public Object[][] getOptions(IResourceConfiguration resConfig);

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

	/**
	 * Returns <code>true</code> if this element has changes that need to 
	 * be saved in the project file, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isDirty();
	
	/**
	 * Sets the element's "dirty" (have I been modified?) flag.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);
}
