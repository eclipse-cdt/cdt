/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

public enum SessionType { 
	LOCAL, 
	REMOTE, 
	CORE, 
	/**
	 * Denotes a session of an, as of yet, unknown or potentially changing type 
	 * @since 5.3
	 */
	FLUID,
	/**
	 * Special type of session where we intend to connect-to and use an already started debugger
	 * @since 5.3
	 */
	EXISTING
}
