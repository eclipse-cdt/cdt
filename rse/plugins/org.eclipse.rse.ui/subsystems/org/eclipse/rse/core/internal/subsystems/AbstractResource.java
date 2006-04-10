/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core.internal.subsystems;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.subsystems.ISubSystem;


/**
 * This class can be used as the base class for model objects that represent
 *  remote resources returned from the subsystem via resolveFilterStrings. Its
 *  advantages are:
 * <ul>
 *   <li>It already implements IAdaptable.
 *   <li>It already maintains the reference to the owning subsystem, which simplifies action processing.
 * </ul>
 */
public abstract class AbstractResource implements IAdaptable
{
	private ISubSystem parentSubSystem;

	/**
	 * Default constructor
	 */
	public AbstractResource(ISubSystem parentSubSystem)
	{
		super();
		this.parentSubSystem = parentSubSystem;
	}
	/**
	 * Constructor.
	 * @see #setSubSystem(ISubSystem)
	 */
	public AbstractResource()
	{
		super();
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter)
	{
   	    return Platform.getAdapterManager().getAdapter(this, adapter);	
	}

	/**
	 * Returns the parent SubSystem which produced this remote resource.
	 * @return SubSystem
	 */
	public ISubSystem getSubSystem()
	{
		return parentSubSystem;
	}

	/**
	 * Resets the parent SubSystem.
	 * @param parentSubSystem The parentSubSystem to set
	 */
	public void setSubSystem(ISubSystem parentSubSystem)
	{
		this.parentSubSystem = parentSubSystem;
	}
	
}