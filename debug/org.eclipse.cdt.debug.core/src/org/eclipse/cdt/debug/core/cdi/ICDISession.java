/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

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
	 * Returns the current debug target associatd with this sesion, 
	 * or null if no debug targets are associated with this session. 
	 * 
	 * @return ICDITarget the current debug target
	 */
	ICDITarget getCurrentTarget();

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
	 * Returns the breakpoint manager of this debug session.
	 * 
	 * @return the breakpoint manager
	 */
	ICDIBreakpointManager getBreakpointManager();

	/**
	 * Returns the signal manager of this debug session.
	 * 
	 * @return the signal manager
	 */
	ICDISignalManager getSignalManager();

	/**
	 * Returns the expression manager of this debug session.
	 * 
	 * @return the expression manager
	 */
	ICDIExpressionManager getExpressionManager();

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
	 * Add directories to the begining of the search path
	 * for source files.
	 */
	void addSearchPaths(String[] dirs) throws CDIException;
}
