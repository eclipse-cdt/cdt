/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.model;

/**
 * 
 * Represents the current state of debug element.
 * 
 * @since Aug 6, 2002
 */
public interface IState
{
	public static final int UNKNOWN = 0;
	public static final int NOT_RESPONDING = 1;
	public static final int STARTING = 2;
	public static final int ATTACHING = 3;
	public static final int DISCONNECTING = 4;
	public static final int RUNNING = 5;
	public static final int STEPPING = 6;
	public static final int SUSPENDED = 7;
	public static final int EXITED = 8;
	public static final int DISCONNECTED = 9;
	public static final int TERMINATED = 10;
	public static final int CORE_DUMP_FILE = 11;
	
	/**
	 * Returns the identifier of the current state.
	 * 
	 * @return the identifier of the current state
	 */
	int getCurrentStateId();
	
	/**
	 * Returns the info object associated with the current state.
	 * 
	 * @return the info object associated with the current state
	 */
	Object getCurrentStateInfo();
}
