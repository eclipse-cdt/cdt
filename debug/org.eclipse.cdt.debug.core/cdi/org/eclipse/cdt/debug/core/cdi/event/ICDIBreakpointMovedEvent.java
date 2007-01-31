/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.ICDILocator;

/**
 * 
 * Notifies that a breakpoint has changed location.
 */

public interface ICDIBreakpointMovedEvent extends ICDIEvent {

	/** Returns the new location for the breakpoint.
	 * @return the breakpoint's new location.
	 */
	ICDILocator getNewLocation();
}
