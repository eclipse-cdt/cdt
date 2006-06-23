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

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * Provides the ability to step into, over, and until
 * from the current execution location.  Implementations
 * must be non-blocking.
 */
public interface ICDIExecuteStep {

	/**
	 * Steps over the current source line.
	 * if count <= 0 it is a noop.
	 * Can only be called when the associated target/thread is suspended. 
	 * 
	 * @param count as in `step', but do so count times.
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOver(int count) throws CDIException;

	/**
	 * Steps over the current machine instruction. Can only be called
	 * when the associated target/thread is suspended. 
	 * if count <= 0 it is a noop.
	 * 
	 * @param count as in `stepOverInstruction', but do so count times.
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepOverInstruction(int count) throws CDIException;

	/**
	 * Steps into the current source line. Can only be called
	 * when the associated target/thread is suspended. 
	 * if count <= 0 it is a noop.
	 * 
	 * @param count as in `step', but do so count times.
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepInto(int count) throws CDIException;

	/**
	 * Steps into the current machine instruction. Can only be called
	 * when the associated target/thread is suspended. 
	 * if count <= 0 it is a noop.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepIntoInstruction(int count) throws CDIException;

	/**
	 * Continues running until location is reached.
	 * If the program will be suspended if attempt to exit the current frame.
	 * Can only be called when the associated target is suspended.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void stepUntil(ICDILocation location) throws CDIException;

}
