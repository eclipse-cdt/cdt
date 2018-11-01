/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;
public interface ISystemMementoConstants 
{
	
	/**
	 * Memento ID for profiles
	 */
	public static final String MEMENTO_KEY_PROFILE = "Profile"; //$NON-NLS-1$
	/**
	 * Memento ID for connections
	 */
	public static final String MEMENTO_KEY_CONNECTION = "Conn"; //$NON-NLS-1$
	/**
	 * Memento ID for subsystems
	 */
	public static final String MEMENTO_KEY_SUBSYSTEM = "Subs"; //$NON-NLS-1$
	/**
	 * Memento ID for filter pool references
	 */
	public static final String MEMENTO_KEY_FILTERPOOLREFERENCE = "FPoolRef"; //$NON-NLS-1$
	/**
	 * Memento ID for filter references
	 */
	public static final String MEMENTO_KEY_FILTERREFERENCE = "FRef"; //$NON-NLS-1$
	/**
	 * Memento ID for filter string references
	 */
	public static final String MEMENTO_KEY_FILTERSTRINGREFERENCE = "FSRef"; //$NON-NLS-1$
	/**
	 * Memento ID for remote objects
	 */
	public static final String MEMENTO_KEY_REMOTE = "Remote"; //$NON-NLS-1$

}
