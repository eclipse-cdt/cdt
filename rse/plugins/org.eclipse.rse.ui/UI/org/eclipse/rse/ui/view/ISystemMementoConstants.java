/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;
public interface ISystemMementoConstants 
{
	
	/**
	 * Memento ID for profiles
	 */
	public static final String MEMENTO_KEY_PROFILE = "Profile";
	/**
	 * Memento ID for connections
	 */
	public static final String MEMENTO_KEY_CONNECTION = "Conn";
	/**
	 * Memento ID for subsystems
	 */
	public static final String MEMENTO_KEY_SUBSYSTEM = "Subs";
	/**
	 * Memento ID for filter pool references
	 */
	public static final String MEMENTO_KEY_FILTERPOOLREFERENCE = "FPoolRef";
	/**
	 * Memento ID for filter references
	 */
	public static final String MEMENTO_KEY_FILTERREFERENCE = "FRef";
	/**
	 * Memento ID for filter string references
	 */
	public static final String MEMENTO_KEY_FILTERSTRINGREFERENCE = "FSRef";
	/**
	 * Memento ID for remote objects
	 */
	public static final String MEMENTO_KEY_REMOTE = "Remote";

}