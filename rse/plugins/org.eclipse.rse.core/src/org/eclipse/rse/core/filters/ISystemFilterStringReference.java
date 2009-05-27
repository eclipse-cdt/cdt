/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
 * David Dykstal (IBM) - [226561] Add API markup to RSE Javadocs where extend / implement is allowed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.filters;

import org.eclipse.rse.core.references.IRSEBaseReferencingObject;

/**
 * Represents a reference to a filter string.
 * Needed so the UI can show the same filter string multiple times.
 * @noimplement This interface is not intended to be implemented by clients.
 * The allowable implementations are already present in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemFilterStringReference extends IRSEBaseReferencingObject {
	/**
	 * Return the reference manager which is managing this filter reference
	 * framework object.
	 */
	public ISystemFilterPoolReferenceManager getFilterPoolReferenceManager();

	/**
	 * Return the object which instantiated the pool reference manager object.
	 * Makes it easy to get back to the point of origin, given any filter reference
	 * framework object
	 */
	public ISystemFilterPoolReferenceManagerProvider getProvider();

	/**
	 * Get the master filter string
	 */
	public ISystemFilterString getReferencedFilterString();

	/**
	 * Get the referenced filter that contains this filter string reference.
	 */
	public ISystemFilterReference getParent();

	/**
	 * Get the actual filter that contain the actual filter string we reference
	 */
	public ISystemFilter getParentSystemFilter();

	/**
	 * Same as getReferencedFilterString().getString()
	 */
	public String getString();
}
