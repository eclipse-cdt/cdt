/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

/*
 * These constants for model change events were taken from ISystemModelChangeEvents.  
 */
public interface IUserActionsModelChangeEvents {

	/**
	 * Resource Type: user action
	 */
	public static final int SYSTEM_RESOURCETYPE_USERACTION = 128;
	/**
	 * Resource Type: named type, which are used in user actions
	 */
	public static final int SYSTEM_RESOURCETYPE_NAMEDTYPE = 256;
	/**
	 * Resource Type: compile command
	 */
	public static final int SYSTEM_RESOURCETYPE_COMPILECMD = 512;

}
