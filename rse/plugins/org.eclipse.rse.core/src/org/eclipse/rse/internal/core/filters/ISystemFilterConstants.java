/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - removing extension of ISystemFilterSavePolicies
 ********************************************************************************/

package org.eclipse.rse.internal.core.filters;

/**
 * Constants used throughout filters framework.
 */
public interface ISystemFilterConstants {
	/**
	 * Parameter value on create operations when a restore should be attempted first
	 */
	public static final boolean TRY_TO_RESTORE_YES = true;
	/**
	 * Parameter value on create operations when no restore should be attempted first
	 */
	public static final boolean TRY_TO_RESTORE_NO = false;

	/**
	 * Suffix used when persisting data to a file.
	 */
	public static final String SAVEFILE_SUFFIX = ".xmi"; //$NON-NLS-1$

	/**
	 * Default value for the type attribute for filter pools, filters and filterstrings
	 */
	public static final String DEFAULT_TYPE = "default"; //$NON-NLS-1$
}