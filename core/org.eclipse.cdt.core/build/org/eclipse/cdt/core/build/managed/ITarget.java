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
package org.eclipse.cdt.core.build.managed;

import org.eclipse.core.resources.IResource;

/**
 * This class represents targets for the managed build process.  A target
 * is some type of resource built using a given collection of tools.
 */
public interface ITarget extends IBuildObject {

	/**
	 * Gets the resource that this target is applied to.
	 * 
	 * @return
	 */
	public IResource getOwner();
	
	/**
	 * Returns the list of platform specific tools associated with this
	 * platform.
	 * 
	 * @return
	 */
	public ITool[] getTools();

	/**
	 * Creates a new tool.
	 * 
	 * @return
	 */
	public ITool createTool();
	
	/**
	 * Returns all of the configurations defined by this target.
	 * @return
	 */
	public IConfiguration[] getConfigurations();

	/**
	 * Creates a new configuration for this target.
	 * 
	 * @return
	 */
	public IConfiguration createConfiguration()
		throws BuildException;
	
	/**
	 * Creates a new configuration based on the parent config for this target.
	 * 
	 * @param parentConfig
	 * @return
	 */
	public IConfiguration createConfiguration(IConfiguration parentConfig)
		throws BuildException;

}
