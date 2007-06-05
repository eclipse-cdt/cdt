/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.internal.processes.ui.view;

public interface ISystemProcessPropertyConstants
{
	public static final String P_PREFIX = org.eclipse.rse.ui.ISystemIconConstants.PREFIX;
	
	// PROCESS PROPERTIES
	public static String P_PROCESS_PID				= P_PREFIX+"process.pid"; //$NON-NLS-1$
	public static String P_PROCESS_NAME				= P_PREFIX+"process.name";  //$NON-NLS-1$
	public static String P_PROCESS_UID				= P_PREFIX+"process.uid"; //$NON-NLS-1$
	public static String P_PROCESS_USERNAME			= P_PREFIX+"process.username"; //$NON-NLS-1$
	public static String P_PROCESS_PPID				= P_PREFIX+"process.ppid"; //$NON-NLS-1$
	public static String P_PROCESS_GID				= P_PREFIX+"process.gid"; //$NON-NLS-1$
	public static String P_PROCESS_STATE			= P_PREFIX+"process.state"; //$NON-NLS-1$
	public static String P_PROCESS_TGID				= P_PREFIX+"process.tgid"; //$NON-NLS-1$
	public static String P_PROCESS_TRACERPID		= P_PREFIX+"process.tracerpid"; //$NON-NLS-1$
	public static String P_PROCESS_VMSIZE			= P_PREFIX+"process.vmsize"; //$NON-NLS-1$
	public static String P_PROCESS_VMRSS			= P_PREFIX+"process.vmrss"; //$NON-NLS-1$

}
