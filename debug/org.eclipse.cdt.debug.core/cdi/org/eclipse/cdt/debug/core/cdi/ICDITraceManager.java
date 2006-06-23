/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.model.ICDITracepoint;

/**
 * Manages the collection of registered tracepoints and trace snapshoits 
 * in the debug session. Provides methods to control tracing.
 * 
 * @since May 15, 2003
 */
public interface ICDITraceManager extends ICDISessionObject {
	/**
	 * Returns a collection of all tracepoints set for this session. 
	 * Returns an empty array if no tracepoints are set.
	 * 
	 * @return a collection of all tracepoints set for this session
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDITracepoint[] getTracepoints() throws CDIException;

	/**
	 * Deletes the given tracepoint.
	 * 
	 * @param tracepoint - a tracepoint to be deleted
	 * @throws CDIException on failure. Reasons include:
	 */
	void deleteTracepoint( ICDITracepoint tracepoint ) throws CDIException;

	/**
	 * Deletes the given array of tracepoints.
	 * 
	 * @param tracepoints - the array of tracepoints to be deleted
	 * @throws CDIException on failure. Reasons include:
	 */
	void deleteTracepoints( ICDITracepoint[] tracepoints ) throws CDIException;

	/**
	 * Deletes all tracepoints.
	 * 
	 * @throws CDIException on failure. Reasons include:
	 */
	void deleteAllTracepoints() throws CDIException;

	/**
	 * Sets a tracepoint at the given location.
	 * The tracepoint is set acording to the choices:
	 * <pre>
	 * if location.getFile() != null then
	 *    if location.getFunction() != null then
	 *       tracepoint = file:function
	 *    else
	 *       tracepoint = file:line
	 * else if (location.getFuntion() != null) then
	 *    tracepoint = function
	 * else if (location.getLineNumber() != 0 then
	 *    tracepoint = line
	 * else
	 *    tracepoint = address
	 * end
	 * </pre> 
	 * 
	 * @param location - the location 
	 * @return a tracepoint
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDITracepoint setTracepoint( ICDILocation location ) throws CDIException;

	/**
	 * Allows the manager to interrupt the excution of program
	 * when setting a tracepoint.
	 */
	void allowProgramInterruption( boolean allow );

	/**
	 * Starts the tracing and begins collecting data.
	 * 
	 * @throws CDIException on failure. Reasons include:
	 */
	void startTracing() throws CDIException;

	/**
	 * Stops the tracing and ends collecting data.
	 * 
	 * @throws CDIException on failure. Reasons include:
	 */
	void stopTracing() throws CDIException;

	/**
	 * Returns the status of tracing.
	 * 
	 * @return the status of tracing
	 * @throws CDIException on failure. Reasons include:
	 */
	boolean isTracing() throws CDIException;

	/**
	 * Enables/disables the snapshot debugging mode.
	 * 
	 * @param enabled <code>true</code> to enable, and <code>false</code> 
	 * to disable
	 * @throws CDIException on failure. Reasons include:
	 */
	void enableSnapshotMode( boolean enabled ) throws CDIException;

	/**
	 * Returns all trace snapshots for this session.
	 * 
	 * @return all trace snapshots for this session
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDITraceSnapshot[] getSnapshots() throws CDIException;

	/**
	 * Returns all trace snapshots associated with the given tracepoints.
	 * 
	 * @param tracepoints - an array of tracepoints
	 * @return all trace snapshots associated with the given tracepoints
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDITraceSnapshot[] getSnapshots( ICDITracepoint[] tracepoints ) throws CDIException;

	/**
	 * Returns all trace snapshots associated with the given locations.
	 * 
	 * @param locations - an array of locations
	 * @return all trace snapshots associated with the given locations
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDITraceSnapshot[] getSnapshots( ICDILocation[] locations ) throws CDIException;

	/**
	 * Creates an ICDILocation object for given file name and line number or function.
	 * 
	 * @param file - a file name
	 * @param function - a function name
	 * @param line - a line number
	 * @return an ICDILocation object 
	 */
	ICDILocation createLocation( String file, String function, int line );

	/**
	 * Creates an ICDILocation object for given address.
	 * 
	 * @param address - an address
	 * @return an ICDILocation object 
	 */
	ICDILocation createLocation( BigInteger address );
}
