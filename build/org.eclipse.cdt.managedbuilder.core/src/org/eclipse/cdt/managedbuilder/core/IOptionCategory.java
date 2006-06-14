/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.net.URL;

/**
 * 
 */
public interface IOptionCategory extends IBuildObject {

	// Schema element names
	public static final String OWNER = "owner"; //$NON-NLS-1$
	public static final String ICON  = "icon";  //$NON-NLS-1$
	
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
	 * 
	 * @since 3.1
	 */
	public Object[][] getOptions(IConfiguration configuration, IHoldsOptions optHolder);
	/**
	 * @deprecated since 3.1 - use getOption with IHoldsOptions aregument instead
	 */
	public Object[][] getOptions(IConfiguration configuration);

	/**
	 * Returns an array of ITool/IOption pairs for the options in this category
	 * for a given resource configuration.
	 * 
	 * @param tool
	 * @return Object[][]
	 * 
	 * @since 3.1
	 */
	public Object[][] getOptions(IResourceConfiguration resConfig, IHoldsOptions optHolder);
	/**
	 * @deprecated since 3.1 - use getOption with IHoldsOptions aregument instead
	 */
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
	 * If owned by a toolChain return null.
	 * 
	 * @return
	 * @deprecated since 3.0 - use getOptionHolder() instead
	 */
	public ITool getTool();

	/**
	 * Returns the holder (parent) of this category. This may be an object
	 * implementing ITool or IToolChain, which both extend IHoldsOptions.
	 * The call can return null, for example the top option category of a tool 
	 * will return null.
	 * 
	 * Note that the name getOptionHolder() has been choosen, because Tool implements
	 * both ITool and IOptionCategory and ITool.getParent() exists already.
	 *  
	 * @return IHoldsOptions
	 * @since 3.0
	 */
	public IHoldsOptions getOptionHolder();

	/**
	 * Get the path name of an alternative icon for the option group.
	 * Or null if no alternative icon was defined.
	 * 
	 * @return URL
	 * @since 3.0
	 */
	public URL getIconPath();

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
