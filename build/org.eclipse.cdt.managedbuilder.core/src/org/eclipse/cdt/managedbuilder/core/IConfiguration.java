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
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.core.resources.IResource;

public interface IConfiguration extends IBuildObject {
	// Schema element names
	public static final String CONFIGURATION_ELEMENT_NAME = "configuration";	//$NON-NLS-1$
	public static final String TOOLREF_ELEMENT_NAME = "toolReference";	//$NON-NLS-1$
	public static final String PARENT = "parent";	//$NON-NLS-1$

	/**
	 * Returns the target for this configuration.
	 * 
	 * @return
	 */
	public ITarget getTarget();
	
	/**
	 * Returns the resource that owns the target that owns the configuration.
	 * @return
	 */
	public IResource getOwner();
	
	/**
	 * Answers the configuration that the receiver is based on. 
	 * 
	 * @return
	 */
	public IConfiguration getParent();
	
	/**
	 * Returns the tools that are used in this configuration.
	 * 
	 * @return
	 */
	public ITool[] getTools();

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
	 * @throws BuildException
	 */
	public void setOption(IOption option, boolean value) 
		throws BuildException;	

	/**
	 * Sets the value of a string option for this configuration.
	 * 
	 * @param option The option that will be effected by change.
	 * @param value The value to apply to the option.
	 */
	public void setOption(IOption option, String value)
		throws BuildException;
	
	/**
	 * Sets the value of a list option for this configuration.
	 * 
	 * @param option The option to change.
	 * @param value The values to apply to the option.
	 */
	public void setOption(IOption option, String[] value)
		throws BuildException;

	/**
	 * Overrides the tool command for a tool defined in the receiver.
	 * 
	 * @param tool
	 * @param command
	 */
	public void setToolCommand(ITool tool, String command);

}
