/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICTarget;

/**
 * 
 * Represents a debug session.
 * 
 * @since Jun 28, 2002
 */
public interface ICSession
{
	/**
	 * Returns all the debug targets associatd with this sesion, 
	 * or an empty collection if no debug targets are associated 
	 * with this session. 
	 * 
	 * @return an array of debug targets
	 */
	ICTarget[] getTargets();

	/**
	 * Sets the value of a debug session attribute.
	 *
	 * @param key the attribute key
	 * @param value the attribute value
	 */
	void setAttribute( String key, String value );

	/**
	 * Returns the value of a debug session attribute.
	 *
	 * @param key the attribute key
	 * @return value the attribute value, or <code>null</code> if undefined
	 */
	String getAttribute( String key );

	/**
	 * Returns the breakpoint manager of this debug session.
	 * 
	 * @return the breakpoint manager
	 */
	ICBreakpointManager getBreakpointManager();

	/**
	 * Returns the signal manager of this debug session.
	 * 
	 * @return the signal manager
	 */
	ICSignalManager getSignalManager();

	/**
	 * Returns the expression manager of this debug session.
	 * 
	 * @return the expression manager
	 */
	ICExpressionManager getExpressionManager();

	/**
	 * Returns the memory manager of this debug session.
	 * 
	 * @return the memory manager
	 */
	ICMemoryManager getMemoryManager();

	/**
	 * Returns the source manager of this debug session.
	 * 
	 * @return the source manager
	 */
	ICSourceManager getSourceManager();

	/**
	 * Returns the event manager of this debug session.
	 * 
	 * @return the event manager
	 */
	ICEventManager getEventManager();

	/**
	 * Returns the configuration description of this debug session.
	 * 
	 * @return the configuration description
	 */
	ICDebugConfiguration getConfiguration();

	/**
	 * Returns whether this element is terminated.
	 *
	 * @return whether this element is terminated
	 */
	boolean isTerminated();

	/**
	 * Causes this element to terminate, generating a <code>KIND_TERMINATE</code> event.  
	 *
	 * @exception CDIException on failure. Reasons include:
	 */
	void terminate() throws CDIException;
}
