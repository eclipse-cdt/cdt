/**********************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
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
	public static final String ARTIFACT_NAME = "artifactName";	//$NON-NLS-1$
	public static final String BINARY_PARSER = "binaryParser";	//$NON-NLS-1$
	public static final String CLEAN_COMMAND = "cleanCommand";	//$NON-NLS-1$
	public static final String DEFAULT_EXTENSION = "defaultExtension";	//$NON-NLS-1$
	public static final String EXTENSION = "extension";	//$NON-NLS-1$
	public static final String IS_ABSTRACT = "isAbstract";	//$NON-NLS-1$
	public static final String IS_TEST = "isTest";	//$NON-NLS-1$
	public static final String MAKE_COMMAND = "makeCommand";	//$NON-NLS-1$
	public static final String OS_LIST = "osList";	//$NON-NLS-1$
	public static final String PARENT = "parent";	//$NON-NLS-1$
	
	/**
	 * Creates a configuration for the target populated with the tools and
	 * options settings from the parent configuration.  As options and tools
	 * change in the parent, unoverridden values are updated in the child
	 * config as well.
	 * 
	 * @param parent The <code>IConfiguration</code> to use as a settings template
	 * @param id The unique id the new configuration will have
	 * @return IConfiguration
	 */
	public IConfiguration createConfiguration(IConfiguration parent, String id);

	/**
	 * Creates a new configuration for the target.  It is populated with
	 * the tools defined for that target and options set at their defaults.
	 * 
	 * @param id id for this configuration.
	 * @return IConfiguration
	 */
	public IConfiguration createConfiguration(String id);
	
	/**
	 * Answers the extension that should be applied to build artifacts created by 
	 * this target.
	 * 
	 * @return String
	 */
	public String getArtifactExtension();	

	/**
	 * Get the name of the final build artifact.
	 * 
	 * @return String
	 */
	public String getArtifactName();
	
	/**
	 * Answers the unique ID of the binary parser associated with the target.
	 * 
	 * @return String
	 */
	public String getBinaryParserId();
	
	/**
	 * Answers the OS-specific command to remove files created by the build
	 *  
	 * @return String
	 */
	public String getCleanCommand();

	/**
	 * Returns all of the configurations defined by this target.
	 * 
	 * @return IConfiguration[]
	 */
	public IConfiguration[] getConfigurations();

	/**
	 * Get the default extension that should be applied to build artifacts
	 * created by this target.
	 * 
	 * @return String
	 * @deprecated
	 */
	public String getDefaultExtension();	

	/**
	 * Answers the name of the make utility for the target.
	 *  
	 * @return String
	 */
	public String getMakeCommand();

	/**
	 * Returns the configuration with the given id, or <code>null</code> if not found.
	 * 
	 * @param id
	 * @return IConfiguration
	 */
	public IConfiguration getConfiguration(String id);
	
	/**
	 * Gets the resource that this target is applied to.
	 * 
	 * @return IResource
	 */
	public IResource getOwner();

	/**
	 * Answers the <code>ITarget</code> that is the parent of the receiver.
	 * 
	 * @return ITarget
	 */
	public ITarget getParent();
	
	/**
	 * Answers an array of operating systems the target can be created on.
	 * 
	 * @return String[]
	 */
	public String[] getTargetOSList();

	/**
	 * Returns the list of platform specific tools associated with this
	 * platform.
	 * 
	 * @return ITool[]
	 */
	public ITool[] getTools();

	
	/**
	 * Answers true if the receiver has a make command that differs from its 
	 * parent specification.
	 * 
	 * @return boolean
	 */
	public boolean hasOverridenMakeCommand();
	
	/**
	 * Returns whether this target is abstract.
	 * @return boolean 
	 */
	public boolean isAbstract();
	
	/**
	 * Answers <code>true</code> if the receiver is a target that is defined 
	 * for testing purposes only, else <code>false</code>. A test target will 
	 * not be shown in the UI but can still be manipulated programmatically.
	 * 
	 * @return boolean
	 */
	public boolean isTestTarget();

	/**
	 * Removes the configuration with the ID specified in the argument.
	 * 
	 * @param id
	 */
	public void removeConfiguration(String id);
	
	/**
	 * Resets the make command in the receiver to the value specified in 
	 * its parent.
	 * 
	 */
	public void resetMakeCommand();
	
	/**
	 * Set (override) the extension that should be appended to the build artifact
	 * for the receiver.
	 *  
	 * @param extension
	 */
	public void setArtifactExtension(String extension);

	/**
	 * Set the name of the artifact that will be produced when the receiver
	 * is built.
	 * 
	 * @param name
	 */
	public void setArtifactName(String name);

	/**
	 * Sets the make command for the receiver to the value in the argument.
	 * 
	 * @param command
	 */
	public void setMakeCommand(String command);

	/**
	 * Sets the resource that owns the receiver.
	 * 
	 * @param resource
	 */
	public void updateOwner(IResource resource);

}
