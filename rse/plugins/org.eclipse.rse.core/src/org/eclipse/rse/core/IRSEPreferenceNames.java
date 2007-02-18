/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kushal Munir (IBM) - Initial API and implementation.
 * David Dykstal (IBM) - updated with comments, removed keys that are not to be used globally
 ********************************************************************************/
package org.eclipse.rse.core;

/**
 * These constants define the set of preference names that the RSE core uses.
 */
public interface IRSEPreferenceNames {
	/**
	 * The key for the value that specifies that queries should be "deferred", that is, run
	 * when needed and in the background, as nodes are asked for their children.
	 */
	public static final String USE_DEFERRED_QUERIES = "useDeferredQueries"; //$NON-NLS-1$
	/**
	 * The key for the default system type. Used when a system type is needed but not declared
	 * when creating new connections (hosts) and for password determination.
	 */
	public static final String SYSTEMTYPE = "systemtype"; //$NON-NLS-1$
	/**
	 * The key for an hash table, encoded as a string, that contains user ids as values
	 * keyed by some key - usually a system type, a connection name, or a combination of
	 * a connection name and subsystem.
	 */
	public static final String USERIDPERKEY = "useridperkey"; //$NON-NLS-1$
	/**
	 * The key for the string containing the list of active user profiles in alphabetical order.
	 * As profiles are activated, deactivated, or renamed this string must be modified.
	 */
	public static final String ACTIVEUSERPROFILES = "activeuserprofiles"; //$NON-NLS-1$
}
