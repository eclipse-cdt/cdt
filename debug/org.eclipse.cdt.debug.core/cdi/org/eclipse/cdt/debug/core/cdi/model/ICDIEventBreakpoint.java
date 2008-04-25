/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.core.runtime.CoreException;

public interface ICDIEventBreakpoint extends ICDIBreakpoint {
	/**
	 * Get event breakpoint type. This is usually id in reverse web notation. 
	 * @return event breakpoint type id
	 * @throws CoreException 
	 */
	String getEventType() throws CoreException;

	/**
	 * Get extra event argument. For example name of the exception or number of a signal.
	 * @return event argument
	 * @throws CoreException 
	 */
	String getExtraArgument() throws CoreException;
}
