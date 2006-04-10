/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.model;
/**
 * Constants for pre-defined subsystem factory categories.
 * Use these in calls to {@link org.eclipse.rse.model.ISystemRegistry#getHostsBySubSystemConfigurationCategory(String)}.
 */
public interface ISubSystemFactoryCategories
{
	/**
	 * Job subsystems 
	 */
	public static final String SUBSYSTEM_CATEGORY_JOBS = "jobs";
	/**
	 * File subsystems 
	 */
	public static final String SUBSYSTEM_CATEGORY_FILES = "files";
	/**
	 * Command subsystems 
	 */
	public static final String SUBSYSTEM_CATEGORY_CMDS = "commands";

}