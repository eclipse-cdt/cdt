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

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * 
 * Represents a line, function or address breakpoint.
 * 
 * @since Jul 9, 2002
 */
public interface ICDILocationBreakpoint extends ICDIBreakpoint {
	/**
	 * Returns the location of this breakpoint.
	 * 
	 * @return the location of this breakpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDILocation getLocation() throws CDIException;
}
