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

/**
 * This class represents targets for the managed build process.  A target
 * is some type of resource built using a given collection of tools.
 */
public interface ITarget extends IBuildObject {
	public static final String TARGET_ELEMENT_NAME = "target";	//$NON-NLS-1$

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

	/**
	 * Creates a new configuration for the target.  It is populated with
	 * the tools defined for that target and options set at their defaults.
	 * 
	 * @param id id for this configuration.
	 * @return
	 */
	public IConfiguration createConfiguration(String id);
	
	/**
	 * Get the name of the final build artifact.
	 * 
	 * @return 
	 */
	public String getArtifactName();
	
	/**
	 * Answers the OS-specific command to remove files created by the build
	 *  
	 * @return
	 */
	public String getCleanCommand();

	/**
	 * Returns all of the configurations defined by this target.
	 * @return
	 */
	public IConfiguration[] getConfigurations();

	/**
	 * Get the default extension that should be applied to build artifacts
	 * created by this target.
	 * 
	 * @return
	 */
	public String getDefaultExtension();	

	/**
	 * Answers the name of the make utility for the target.
	 *  
	 * @return
	 */
	public String getMakeCommand();

	/**
	 * Returns the configuration with the given id, or null if not found.
	 * 
	 * @param id
	 * @return
	 */
	public IConfiguration getConfiguration(String id);
	
	/**
	 * Gets the resource that this target is applied to.
	 * 
	 * @return
	 */
	public IResource getOwner();

	/**
	 * @return the <code>ITarget</code> that is the parent of the receiver.
	 */
	public ITarget getParent();
	
	/**
	 * Returns the list of platform specific tools associated with this
	 * platform.
	 * 
	 * @return
	 */
	public ITool[] getTools();

	/**
	 * Returns whether this target is abstract.
	 * @return 
	 */
	public boolean isAbstract();
	
	/**
	 * Answers <code>true</code> if the receiver is a target that is defined 
	 * for testing purposes only, else <code>false</code>. A test target will 
	 * not be shown in the UI but can still be manipulated programmatically.
	 * 
	 * @return
	 */
	public boolean isTestTarget();

	/**
	 * Set the name of the artifact that will be produced when the receiver
	 * is built.
	 * 
	 * @param name The name of the build artifact.
	 */
	public void setBuildArtifact(String name);

	
}
