/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

/**
 * The state of a debug element.
 */
public class CDebugElementState {
	
	private final String fName;
	
	private CDebugElementState( String name ) {
		this.fName = name;
	}
	
	public String toString() {
		return this.fName;
	}

	public static final CDebugElementState UNDEFINED = new CDebugElementState( "undefined" ); //$NON-NLS-1$
	public static final CDebugElementState TERMINATING = new CDebugElementState( "terminating" ); //$NON-NLS-1$
	public static final CDebugElementState TERMINATED = new CDebugElementState( "terminated" ); //$NON-NLS-1$
	public static final CDebugElementState DISCONNECTING = new CDebugElementState( "disconnecting" ); //$NON-NLS-1$
	public static final CDebugElementState DISCONNECTED = new CDebugElementState( "disconnected" ); //$NON-NLS-1$
	public static final CDebugElementState RESUMING = new CDebugElementState( "resuming" ); //$NON-NLS-1$
	public static final CDebugElementState RESUMED = new CDebugElementState( "resumed" ); //$NON-NLS-1$
	public static final CDebugElementState STEPPING = new CDebugElementState( "stepping" ); //$NON-NLS-1$
	public static final CDebugElementState STEPPED = new CDebugElementState( "stepped" ); //$NON-NLS-1$
	public static final CDebugElementState SUSPENDING = new CDebugElementState( "suspending" ); //$NON-NLS-1$
	public static final CDebugElementState SUSPENDED = new CDebugElementState( "suspended" ); //$NON-NLS-1$
	public static final CDebugElementState RESTARTING = new CDebugElementState( "restarting" ); //$NON-NLS-1$
	public static final CDebugElementState EXITED = new CDebugElementState( "exited" ); //$NON-NLS-1$
}
