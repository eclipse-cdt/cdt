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
	 * Returns whether this target is abstract
	 * @return
	 */
	public boolean isAbstract();
	
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
	 * Returns all of the configurations defined by this target.
	 * @return
	 */
	public IConfiguration[] getConfigurations();

	/**
	 * Creates a new configuration for the target.  It is populated with
	 * the tools defined for that target and options set at their defaults.
	 * 
	 * @param id id for this configuration.
	 * @return
	 */
	public IConfiguration createConfiguration(String id);
	
	/**
	 * Creates a configuration for the target populated with the tools and
	 * options settings from the parent configuration.  As options and tools
	 * change in the parent, unoverridden values are updated in the child
	 * config as well.
	 * 
	 * @param parent
	 * @param id
	 * @return
	 */
	public IConfiguration createConfiguration(IConfiguration parent, String id);
}
