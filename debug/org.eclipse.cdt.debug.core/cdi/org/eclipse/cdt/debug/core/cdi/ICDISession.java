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
	 * Returns the signal manager of this debug session.
	 * 
	 * @return the signal manager
	 */
	ICDISignalManager getSignalManager();

	/**
	 * Returns the variable manager of this debug session.
	 * 
	 * @return the variable manager
	 */
	ICDIVariableManager getVariableManager();

	/**
	 * Returns the expression manager of this debug session.
	 * 
	 * @return the expression manager
	 */
	ICDIExpressionManager getExpressionManager();

	/**
	 * Returns the register manager of this debug session.
	 * 
	 * @return the register manager
	 */
	ICDIRegisterManager getRegisterManager();


	/**
	 * Returns the memory manager of this debug session.
	 * 
	 * @return the memory manager
	 */
	ICDIMemoryManager getMemoryManager();

	/**
	 * Returns the source manager of this debug session.
	 * 
	 * @return the source manager
	 */
	ICDISourceManager getSourceManager();

	/**
	 * Returns the event manager of this debug session.
	 * 
	 * @return the event manager
	 */
	ICDIEventManager getEventManager();

	/**
	 * Returns the shared library manager of this debug session.
	 * 
	 * @return the shared library manager
	 */
	ICDISharedLibraryManager getSharedLibraryManager();

	/**
	 * Returns the configuration description of this debug session.
	 * 
	 * @return the configuration description
	 */
	ICDIConfiguration getConfiguration();

	/**
	 * Returns the Runtime options for this debug session.
	 * 
	 * @return the configuration description
	 */
	ICDIRuntimeOptions getRuntimeOptions();

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
