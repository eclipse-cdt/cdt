package org.eclipse.cdt.core.build.managed;

import java.util.List;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

public interface IResourceBuildInfo {

	/**
	 * Add a new target to the build information for the receiver
	 * 
	 * @param target
	 */
	public void addTarget(ITarget target);
		
	/**
	 * Returns the name of the artifact to build for the receiver.
	 * 
	 * @return
	 */
	public String getBuildArtifactName();

	/**
	 * Get the default configuration associated with the receiver
	 * 
	 * @return
	 */
	public IConfiguration getDefaultConfiguration(ITarget target);
	
	
	/**
	 * Returns the default target in the receiver.
	 * 
	 * @return
	 */
	public ITarget getDefaultTarget();
	
	/**
	 * Answers the extension that will be built by the current configuration
	 * for the extension passed in the argument or <code>null</code>.
	 * 
	 * @param resourceName
	 * @return
	 */
	public String getOutputExtension(String resourceExtension);
	
	/**
	 * Get the target specified in the argument.
	 * 
	 * @param id
	 * @return
	 */
	public ITarget getTarget(String id);
	
	/**
	 * Get all of the targets associated with the receiver.
	 * 
	 * @return
	 */
	public List getTargets();
	
	/**
	 * Returns a <code>String</code> containing the flags, including 
	 * those overridden by the user, for the tool that handles the 
	 * type of source file defined by the argument.
	 * 
	 * @param extension
	 * @return
	 */
	public String getFlagsForSource(String extension);

	/**
	 * Returns a <code>String</code> containing the flags, including 
	 * those overridden by the user, for the tool that handles the 
	 * type of target defined by the argument.
	 * 
	 * @param extension
	 * @return
	 */
	public String getFlagsForTarget(String extension);

	/**
	 * Returns a <code>String</code> containing the command-line invocation 
	 * for the tool associated with the source extension.
	 * 
	 * @param extension
	 * @return
	 */
	public String getToolForSource(String extension);

	/**
	 * Returns a <code>String</code> containing the command-line invocation 
	 * for the tool associated with the target extension.
	 * 
	 * @param extension
	 * @return
	 */
	public String getToolForTarget(String extension);
	
	/**
	 * Set the primary configuration for the receiver.
	 * 
	 * @param configuration The <code>IConfiguration</code> that will be used as the default
	 * for all building.
	 */
	public void setDefaultConfiguration(IConfiguration configuration);
	
	/**
	 * Set the primary target for the receiver.
	 * 
	 * @param target
	 */
	public void setDefaultTarget(ITarget target);
	
}
