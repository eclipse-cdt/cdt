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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * This class represents a project configuration in the old (CDT 2.0)
 * managed build system model.
 * <p>  
 * The configuration contains one or more children of type tool-reference.
 *
 * This class was deprecated in 2.1
 */
public interface IConfigurationV2 extends IBuildObject {
	// Schema element names
	public static final String CONFIGURATION_ELEMENT_NAME = "configuration";	//$NON-NLS-1$
	public static final String TOOLREF_ELEMENT_NAME = "toolReference";	//$NON-NLS-1$
	public static final String PARENT = "parent";	//$NON-NLS-1$

	/**
	 * Projects have C or CC natures. Tools can specify a filter so they are not 
	 * misapplied to a project. This method allows the caller to retrieve a list 
	 * of tools from a project that are correct for a project's nature.  
	 * 
	 * @param project the project to filter for
	 * @return an array of <code>ITools</code> that have compatible filters 
	 * for the specified project
	 */
	ITool[] getFilteredTools(IProject project);
	
	/**
	 * Returns the resource that owns the project that owns the configuration.
	 * @return
	 */
	public IResource getOwner();
	
	/**
	 * Answers the configuration that this configuration is based on. 
	 * 
	 * @return
	 */
	public IConfigurationV2 getParent();
	
	/**
	 * Returns the target for this configuration.
	 * 
	 * @return
	 */
	public ITarget getTarget();
	
	/**
	 * Answers the <code>ITool</code> in the receiver with the same 
	 * id as the argument, or <code>null</code>. 
	 * 
	 * @param id unique identifier to search for
	 * @return ITool
	 */
	public ITool getToolById(String id);
	
	/**
	 * Returns the tools that are used in this configuration.
	 * 
	 * @return ITool[]
	 */
	public ITool[] getTools();
	
	/**
	 * Returns the tool references that are children of this configuration.
	 * 
	 * @return
	 */
	public IToolReference[] getToolReferences();

	/**
	 * Answers <code>true</code> the receiver has changes that need to be saved 
	 * in the project file, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isDirty();

	/**
	 * Answers whether the receiver has been changed and requires the 
	 * project to be rebuilt.
	 * 
	 * @return <code>true</code> if the receiver contains a change 
	 * that needs the project to be rebuilt
	 */
	public boolean needsRebuild();

	/**
	 * Sets the element's "dirty" (have I been modified?) flag.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);

	/**
	 * Sets the name of the receiver to the value specified in the argument
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Sets the value of a boolean option for this configuration.
	 * 
	 * @param option The option to change.
	 * @param value The value to apply to the option.
	 * 
	 * @throws BuildException
	 */
	public void setOption(IOption option, boolean value) 
		throws BuildException;	

	/**
	 * Sets the value of a string option for this configuration.
	 * 
	 * @param option The option that will be effected by change.
	 * @param value The value to apply to the option.
	 * 
	 * @throws BuildException
	 */
	public void setOption(IOption option, String value)
		throws BuildException;
	
	/**
	 * Sets the value of a list option for this configuration.
	 * 
	 * @param option The option to change.
	 * @param value The values to apply to the option.
	 * 
	 * @throws BuildException
	 */
	public void setOption(IOption option, String[] value)
		throws BuildException;

	/**
	 * Sets the rebuild state in the receiver. 
	 * 
	 * @param rebuild <code>true</code> will force a rebuild the next time the project builds
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo#setRebuildState(boolean)
	 */
	void setRebuildState(boolean rebuild);

	/**
	 * Overrides the tool command for a tool defined in the receiver.
	 * 
	 * @param tool The tool that will have its command modified
	 * @param command The command
	 */
	public void setToolCommand(ITool tool, String command);

	/**
	 * Sets the configuration that was created from this V2.0 configuration.
	 * 
	 * @param config
	 */
	public void setCreatedConfig(IConfiguration config);

	/**
	 * Returns the configuration that was created from this V2.0 configuration.
	 * 
	 * @return IConfiguration
	 */
	public IConfiguration getCreatedConfig();

}
