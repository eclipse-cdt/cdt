/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.model;

/**
 * Common interface for representing different contents types of artifacts that
 * can be stored in an IRemoteContainer.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemContentsType {
	/*
	 * Indicates the type of this contents
	 */
	public String getType();

	/*
	 * Indicates whether or not the contents
	 * can be flushed or not when a container becomes
	 * stale.
	 */
	public boolean isPersistent();
}
