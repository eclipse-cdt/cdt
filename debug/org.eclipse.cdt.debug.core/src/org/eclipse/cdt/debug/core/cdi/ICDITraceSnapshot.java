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

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITracepoint;

/**
 * Represents a trace snapshot in the debug session.
 * 
 * @since May 15, 2003
 */
public interface ICDITraceSnapshot extends ICDISessionObject {

	/**
	 * Returns the number of this snapshot.
	 * 
	 * @return the number of this snapshot
	 */
	int getNumber();

	/**
	 * Selects this snapshot.
	 * 
	 * @throws CDIException on failure. Reasons include:
	 */
	void select() throws CDIException;

	/**
	 * Returns the data collected at this snapshot.
	 * 
	 * @return the data collected at this snapshot
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIObject[] getData() throws CDIException;

	/**
	 * Returns the array of tracepoints associated with this snapshot.
	 * 
	 * @return array of tracepoints
	 */
	ICDITracepoint[] getTracepoints();
}
