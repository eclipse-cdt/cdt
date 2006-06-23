/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

/**
 * 
 * Represents a debug session.
 * 
 * @since Jun 28, 2002
 */
public interface ICDISession {
	/**
	 * Returns all the debug targets associatd with this sesion, 
	 * or an empty collection if no debug targets are associated 
	 * with this session. 
	 * 
	 * @return an array of debug targets
	 */
	ICDITarget[] getTargets();

	/**
	 * Sets the value of a debug session attribute.
	 *
	 * @param key the attribute key
	 * @param value the attribute value
	 */
	void setAttribute(String key, String value);

	/**
	 * Returns the value of a debug session attribute.
	 *
	 * @param key the attribute key
	 * @return value the attribute value, or <code>null</code> if undefined
	 */
	String getAttribute(String key);

	/**
	 * Returns the event manager of this debug session.
	 * 
	 * @return the event manager
	 */
	ICDIEventManager getEventManager();

	/**
	 * Returns the configuration description of this debug session.
	 * 
	 * @return the configuration description
	 */
	ICDISessionConfiguration getConfiguration();

	/**
	 * Causes this element to terminate, generating a <code>KIND_TERMINATE</code> event.  
	 *
	 * @exception CDIException on failure. Reasons include:
	 */
	void terminate() throws CDIException;
	
	/**
	 * Gaves direct access to the underlying debugger process.
	 * @return the debugger process.
	 */
	Process getSessionProcess() throws CDIException;
	
}
