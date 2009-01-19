/********************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/

package org.eclipse.rse.core;

/**
 * Constants for user id management. Used when specifying the scope of a user id
 * when setting a user id.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRSEUserIdConstants {

	/**
	 * Value 0. Location of user id has not yet been set. Used only as a return value.
	 */
	public static final int USERID_LOCATION_NOTSET = 0;

	/**
	 * Value 1. Location of user id is scoped to the connector service inside the host.
	 */
	public static final int USERID_LOCATION_CONNECTORSERVICE = 1;

	/**
	 * Value 2. Location of user id is scoped to the host, sometimes call "connection".
	 */
	public static final int USERID_LOCATION_HOST = 2;

	/**
	 * Value 3. Location of user id is scoped to system type. It will be the default
	 * for all hosts of this system type that do not have a specified user id assigned.
	 */
	public static final int USERID_LOCATION_DEFAULT_SYSTEMTYPE = 3;

	/**
	 * Value 4. Location of user id is scoped to workspace.
	 */
	public static final int USERID_LOCATION_DEFAULT_OVERALL = 4;

}