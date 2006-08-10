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
 * This models a remote resource representing a developer defined on a particular system.
 */
public class DeveloperResource extends AbstractResource {

	private String name;
	private String id;
	private String deptNbr;	

	/**
	 * Default constructor for DeveloperResource.
	 */
	public DeveloperResource()
	{
		super();
	}
	
	/**
	 * Constructor for DeveloperResource when given parent subsystem.
	 * @param parentSubSystem the parent subsystem
	 */
	public DeveloperResource(ISubSystem parentSubSystem)
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
	 * Returns the id.
	 * @return String
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	/**
	 * Returns the deptNbr.
	 * @return String
	 */
	public String getDeptNbr()
	{
		return deptNbr;
	}

	/**
	 * Sets the deptNbr.
	 * @param deptNbr The deptNbr to set
	 */
	public void setDeptNbr(String deptNbr)
	{
		this.deptNbr = deptNbr;
	}

}
