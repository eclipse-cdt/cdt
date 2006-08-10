/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 ********************************************************************************/

package samples.model;

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * This models a remote resource representing a team defined on a particular system.
 */
public class TeamResource extends AbstractResource {

	private String name;
	private DeveloperResource[] developers;
	
	/**
	 * Default constructor
	 */
	public TeamResource()
	{
		super();
	}
	/**
	 * Constructor for TeamResource when given a parent subsystem.
	 * @param parentSubSystem the parent subsystem
	 */
	public TeamResource(ISubSystem parentSubSystem)
	{
		super(parentSubSystem);
	}
	
	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the developers.
	 * @return DeveloperResource[]
	 */
	public DeveloperResource[] getDevelopers()
	{
		return developers;
	}

	/**
	 * Sets the developers.
	 * @param developers The developers to set
	 */
	public void setDevelopers(DeveloperResource[] developers)
	{
		this.developers = developers;
	}


}
