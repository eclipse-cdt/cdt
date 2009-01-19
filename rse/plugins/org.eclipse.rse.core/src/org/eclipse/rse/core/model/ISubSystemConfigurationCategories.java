/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/

package org.eclipse.rse.core.model;

/**
 * Constants for predefined subsystem factory categories. Use these in calls to
 * {@link org.eclipse.rse.core.model.ISystemRegistry#getHostsBySubSystemConfigurationCategory(String)}
 * . This is a constant interface. The individual items should be referenced
 * directly.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISubSystemConfigurationCategories {
	/**
	 * Job subsystems
	 */
	public static final String SUBSYSTEM_CATEGORY_JOBS = "jobs"; //$NON-NLS-1$
	/**
	 * File subsystems
	 */
	public static final String SUBSYSTEM_CATEGORY_FILES = "files"; //$NON-NLS-1$
	/**
	 * Command subsystems
	 */
	public static final String SUBSYSTEM_CATEGORY_CMDS = "commands"; //$NON-NLS-1$

}
