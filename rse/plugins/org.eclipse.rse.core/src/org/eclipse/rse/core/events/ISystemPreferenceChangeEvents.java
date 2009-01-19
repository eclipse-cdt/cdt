/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/

package org.eclipse.rse.core.events;

/**
 * Interface of event ID constants for preferences changed
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemPreferenceChangeEvents {
	/**
	 * The Show Filter Pools preference has changed
	 */
	public static final int EVENT_SHOWFILTERPOOLS = 5;
	/**
	 * The Show Filter String preference has changed
	 */
	public static final int EVENT_SHOWFILTERSTRINGS = 10;
	/**
	 * The Qualify Connection Names preference has changed
	 */
	public static final int EVENT_QUALIFYCONNECTIONNAMES = 15;
	/**
	 * The Restore State preference has changed
	 */
	public static final int EVENT_RESTORESTATE = 20;

	/**
	 * A connection type has been enabled or disabled
	 */
	public static final int EVENT_ENABLED_CONNECTIONS_CHANGED = 25;

}