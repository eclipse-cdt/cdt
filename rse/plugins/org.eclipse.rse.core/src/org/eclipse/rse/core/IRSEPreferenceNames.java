/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kushal Munir (IBM) - Initial API and implementation.
 * David Dykstal (IBM) - updated with comments, removed keys that are not to be used globally
 * David Dykstal (IBM) - [197167] adding notification and waiting for RSE model
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/
package org.eclipse.rse.core;

/**
 * These constants define the set of preference names that the RSE core uses.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */

/*
 * Preference names that are API should be all capitals and have words separated
 * by an underscore (that is, "public static final" style).
 * Preferences that are not API should using "method name" style.
 */
public interface IRSEPreferenceNames {

	/**
	 * The key for the value that specifies that queries should be "deferred",
	 * that is, run when needed and in the background, as nodes are asked for
	 * their children. This value is not part of the API.
	 *
	 * This field is not intended to be referenced by clients.
	 */
	public static final String USE_DEFERRED_QUERIES = "useDeferredQueries"; //$NON-NLS-1$

	/**
	 * The key for the default system type. Used when a system type is needed
	 * but not declared when creating new connections (hosts) and for password
	 * determination. This value is not part of the API.
	 *
	 * This field is not intended to be referenced by clients.
	 */
	public static final String SYSTEMTYPE = "systemtype"; //$NON-NLS-1$

	/**
	 * The key for an hash table, encoded as a string, that contains user ids as
	 * values keyed by some key - usually a system type, a connection name, or a
	 * combination of a connection name and subsystem. This value is not part of
	 * the API.
	 *
	 * This field is not intended to be referenced by clients.
	 */
	public static final String USERIDPERKEY = "useridperkey"; //$NON-NLS-1$

	/**
	 * The key for the string containing the list of active user profiles in
	 * alphabetical order. As profiles are activated, deactivated, or renamed
	 * this string must be modified. This value is not part of the API.
	 *
	 * This field is not intended to be referenced by clients.
	 */
	public static final String ACTIVEUSERPROFILES = "activeuserprofiles"; //$NON-NLS-1$

	/**
	 * The key of the string containing the id of the default persistence provider.
	 * Value is "DEFAULT_PERSISTENCE_PROVIDER".
	 * This value is part of the API and may be used to customize products.
	 */
	public static final String DEFAULT_PERSISTENCE_PROVIDER = "DEFAULT_PERSISTENCE_PROVIDER"; //$NON-NLS-1$

	/**
	 * The key of the string containing the id of the boolean value to create a
	 * local connection. Value is "CREATE_LOCAL_CONNECTION". This value is part
	 * of the API and may be used to customize products.
	 *
	 * @since org.eclipse.rse.core 3.0
	 */
	public static final String CREATE_LOCAL_CONNECTION = "CREATE_LOCAL_CONNECTION"; //$NON-NLS-1$

}
