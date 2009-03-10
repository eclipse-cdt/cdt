/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM)        - [187739] [refresh] Sub Directories are collapsed when Parent Directory is Refreshed on Remote Systems
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/
package org.eclipse.rse.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * This class is used by tree views to pass context information from the views
 * to the view providers and model adapters for queries. The context information
 * consists of a model object and it may optionally contain the associated
 * filter reference a subsystem.
 *
 * Context objects are created transiently and are only used to aid in providing
 * filter information during a query.
 *
 * @noimplement This interface is not intended to be implemented by clients
 *              (this message added in RSE 3.0.1).
 * @noextend This interface is not intended to be extended by clients.      
 */
public interface IContextObject
{
	/**
	 * Gets the associated filter reference for the corresponding model object
	 * @return the associated filter reference
	 */
	public ISystemFilterReference getFilterReference();

	/**
	 * Gets the model object for which this context applies
	 * @return the model object
	 */
	public IAdaptable getModelObject();

	/**
	 * Gets the associated subsystem for the corresponding model object
	 * @return the associated subsystem
	 */
	public ISubSystem getSubSystem();
}
