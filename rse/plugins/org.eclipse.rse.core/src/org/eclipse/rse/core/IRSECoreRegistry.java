/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Uwe Stieber (Wind River) - Added system types provider extension.
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/
package org.eclipse.rse.core;

/**
 * Interface for RSE core registry. Clients should use this interface as the
 * starting point for querying and manipulating model objects in the RSE
 * framework.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRSECoreRegistry {

	public static final String PI_RSE_CORE = "org.eclipse.rse.core"; //$NON-NLS-1$
	public static final String PI_SYSTEM_TYPES = "systemTypes"; //$NON-NLS-1$
	public static final String PI_SYSTEM_TYPES_PROVIDER = "systemTypeProviders"; //$NON-NLS-1$

	/**
	 * Returns all defined system types.
	 *
	 * @return an array of all defined system types.
	 */
	public IRSESystemType[] getSystemTypes();

	/**
	 * Returns a system type object given the name.
	 *
	 * @param name the name of the system type
	 * @return the system type object with the given name, or <code>null</code> if none is found
	 *
	 * @deprecated Use {@link #getSystemTypeById(String)}.
	 */
	public IRSESystemType getSystemType(String name);

	/**
	 * Returns a system type object given by the id.
	 *
	 * @param systemTypeId The system type id.
	 * @return The system type object with the given id, or <code>null</code> if none is found
	 */
	public IRSESystemType getSystemTypeById(String systemTypeId);
}
