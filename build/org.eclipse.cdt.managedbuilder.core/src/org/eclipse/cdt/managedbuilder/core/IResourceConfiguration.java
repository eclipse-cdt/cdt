/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.core.resources.IResource;

/**
 * This class is a place to define build attributes of individual 
 * resources that are different from the configuration as a whole.  The 
 * resourceConfiguration element can have multiple tool children.  They 
 * define the tool(s) to be used to build the specified resource.  The 
 * tool(s) can execute before, after, or instead of the default tool for 
 * the resources (see the toolOrder attribute in the tool element).
 * 
 * @since 2.1
 */
public interface IResourceConfiguration extends IBuildObject {
	public static final String RESOURCE_CONFIGURATION_ELEMENT_NAME = "resourceConfiguration"; //$NON-NLS-1$
	public static final String RESOURCE_PATH = "resourcePath";					  //$NON-NLS-1$
	public static final String EXCLUDE = "exclude";								  //$NON-NLS-1$

	//TODO:  Set name and ID in the constructors to be 
	//       configuration-name#resource-path
	
	/**
	 * Returns the configuration that is the parent of this resource configuration.
	 * 
	 * @return IConfiguration
	 */
	public IConfiguration getParent();
	
	/**
	 * Returns whether the resource referenced by this element should be excluded
	 * from builds of the parent configuration.  
	 * Returns <code>false</code> if the attribute was not specified.
	 * 
	 * @return boolean 
	 */
	public boolean isExcluded();
	
	/**
	 * Returns the path of the project resource that this element references. 
	 *  TODO:  What is the format of the path? Absolute? Relative? Canonical?
	 *
	 * @return String 
	 */
	public String getResourcePath();
	
	/**
	 * Sets the "excluded" flag for the resource.
	 * If <code>true</code>, the project resource identified by the resoursePath
	 * attribute is excluded from the build of the parent configuration.
	 * 
	 * @param boolean
	 */
	public void setExclude(boolean excluded);

	/**
	 * Sets the resource path to which this resource configuration applies.
	 */
	public void setResourcePath(String path);

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

	/**
	 * Returns the list of tools associated with this resource configuration.
	 * 
	 * @return ITool[]
	 */
	public ITool[] getTools();

	/**
	 * Returns the tool in this resource configuration with the ID specified 
	 * in the argument, or <code>null</code> 
	 * 
	 * @param id The ID of the requested tool
	 * @return ITool
	 */
	public ITool getTool(String id);
	
	/**
	 * Creates a <code>Tool</code> child for this resource configuration.
	 *
	 * @param ITool The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 * 
	 * @return ITool
	 */
	public ITool createTool(ITool superClass, String Id, String name, boolean isExtensionElement);

	/**
	 * Overrides the tool command for a tool defined in this resource configuration's tool.
	 * 
	 * @param tool The tool that will have its command modified
	 * @param command The command
	 */
	public void setToolCommand(ITool tool, String command);
	/**
	 * Sets the value of a boolean option for this resource configuration.
	 * 
	 * @param tool The Tool parent of the option.
	 * @param option The option to change.
	 * @param value The value to apply to the option.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @throws BuildException
	 */
	public IOption setOption(ITool tool, IOption option, boolean value) 
		throws BuildException;	

	/**
	 * Sets the value of a string option for this resource configuration.
	 * 
	 * @param tool The Tool parent of the option.
	 * @param option The option that will be effected by change.
	 * @param value The value to apply to the option.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @throws BuildException
	 */
	public IOption setOption(ITool tool, IOption option, String value)
		throws BuildException;
	
	/**
	 * Sets the value of a list option for this resource configuration.
	 * 
	 * @param tool The Tool parent of the option.
	 * @param option The option to change.
	 * @param value The values to apply to the option.
	 * 
	 * @return IOption The modified option.  This can be the same option or a newly created option.
	 * 
	 * @throws BuildException
	 */
	public IOption setOption(ITool tool, IOption option, String[] value)
		throws BuildException;

	
	/**
	 * Returns the Eclipse project that owns the resource configuration.
	 * 
	 * @return IResource
	 */
	public IResource getOwner();
	
}
