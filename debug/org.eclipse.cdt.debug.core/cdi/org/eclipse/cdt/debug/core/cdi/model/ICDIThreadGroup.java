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

/**
 */
public interface ICDIThreadGroup extends ICDIBreakpointManagement, ICDIExecuteStep, ICDIExecuteResume,
	ICDISuspend, ICDISignalManagement, ICDIObject {

	/**
	 * Returns the threads contained in this target. 
	 * An empty collection is returned if this target contains no 
	 * threads.
	 * 
	 * @return a collection of threads
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIThread[] getThreads() throws CDIException;

	/**
	 * Returns the currently selected thread.
	 * 
	 * @return the currently selected thread
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIThread getCurrentThread() throws CDIException;

}
