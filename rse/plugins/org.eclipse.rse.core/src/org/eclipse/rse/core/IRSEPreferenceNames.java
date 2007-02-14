/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kushal Munir (IBM) - Initial API and implementation.
 ********************************************************************************/
package org.eclipse.rse.core;

/**
 * These constants define the set of preference names that the RSE core uses.
 */
public interface IRSEPreferenceNames {
	/*
	 * core preference keys
	 */
	public static final String ST_ENABLED = "systemType.enabled"; //$NON-NLS-1$
	public static final String ST_DEFAULT_USERID = "systemType.defaultUserId"; //$NON-NLS-1$
	public static final String SYSTEMTYPE = "systemtype"; //$NON-NLS-1$
	public static final String USERIDPERKEY = "useridperkey"; //$NON-NLS-1$
	public static final String USERIDKEYS = "userid.keys"; //$NON-NLS-1$
	public static final String ACTIVEUSERPROFILES = "activeuserprofiles"; //$NON-NLS-1$
	public static final String USE_DEFERRED_QUERIES = "useDeferredQueries"; //$NON-NLS-1$
	/*
	 * core preference default values
	 */
	public static final String DEFAULT_SYSTEMTYPE = ""; //$NON-NLS-1$
	public static final String DEFAULT_USERID = ""; //$NON-NLS-1$
	public static final boolean DEFAULT_USE_DEFERRED_QUERIES = true;
	public static final String DEFAULT_TEAMPROFILE = "Team"; //$NON-NLS-1$
	public static final String DEFAULT_ACTIVEUSERPROFILES = "Team"; //$NON-NLS-1$
}
