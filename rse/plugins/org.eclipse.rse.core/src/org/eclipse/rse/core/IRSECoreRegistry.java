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
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.core;

/**
 * Interface for RSE core registry. Clients should use this interface as the starting point for querying and
 * manipulating model objects in the RSE framework.
 *
 * This interface is not intended to be implemented by clients.
 */
public interface IRSECoreRegistry {

	public static final String PI_RSE_CORE = "org.eclipse.rse.core";
	public static final String PI_SYSTEM_TYPES = "systemTypes";

	/**
	 * Returns all defined system types.
	 * @return an array of all defined system types.
	 */
	public IRSESystemType[] getSystemTypes();

	/**
	 * Returns the names of all defined system types.
	 */
	public String[] getSystemTypeNames();

	/**
	 * Returns a system type object given the name.
	 * @param name the name of the system type
	 * @return the system type object with the given name, or <code>null</code> if none is found
	 */
	public IRSESystemType getSystemType(String name);
}