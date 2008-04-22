/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.rse.core.filters;

/**
 * The system filter wizard allows callers to pass a list of wrapper objects
 * for the user to select a filter pool.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemFilterPoolWrapper {

	/**
	 * Get the name to display in the combo box for this wrapper
	 */
	public String getDisplayName();

	/**
	 * Get the wrappered SystemFilterPool object
	 */
	public ISystemFilterPool getSystemFilterPool();
}
