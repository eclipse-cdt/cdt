/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;


/**
 * Handle for a Remote Systems Project
 *
 * <p>A Remote Systems Project is a singleton created initially by the
 * remote systems plugin. It is a container for all the connections and
 * subsystems defined for working with remote systems.
 *
 * @see IRemoteSystemsProject
 */
public class RemoteSystemsProject extends PlatformObject                      
                                          implements IRemoteSystemsProject, IProjectNature 
{
	
	/**
	 * ID of the nature for the remote system explorer project: "org.eclipse.rse.ui.remotesystemsnature"
	 */
	public static final String ID = "org.eclipse.rse.ui.remotesystemsnature";
	/**
	 * Name of the nature for the remote system explorer project: "Remote Systems Nature"
	 */
	public static final String NAME = "Remote Systems Nature";
	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	protected static final String[] NO_PREREQUISITES= new String[0];

	/**
	 * The platform project this <code>IRemoteProject</code> is based on
	 */
	protected IProject fProject;

	/**
	 * Constructor needed for <code>IProject.getNature()</code> and <code>IProject.addNature()</code>.
	 *
	 * @see #setProject
	 */
	public RemoteSystemsProject() 
	{
		super();
	}
	public RemoteSystemsProject(IProject project) 
	{
		super();
		fProject= project;
	}
	
	public void setProject(IProject project) 
	{
		SystemBasePlugin.logInfo("Inside setProject");
		fProject= project;
	}
	/**
	 * Configure the project with Java nature.
	 */
	public void configure() throws CoreException 
	{
		SystemBasePlugin.logInfo("Inside configure");		
	}
	/**
	 * Removes the Java nature from the project.
	 */
	public void deconfigure() throws CoreException 
	{
	}
	public IProject getProject() 
	{
		return fProject;
	}
	public int hashCode() 
	{
		return fProject.hashCode();
	}
}