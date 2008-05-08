/*******************************************************************************
 * Copyright (c) 2008 Freescale and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.model.ICBreakpointType;

/**
 * Extension of the ICDIBreakpoint interface 
 */
public interface ICDIBreakpoint2 extends ICDIBreakpoint {

	/**
	 * Return the type of the breakpoint. If the breakpoint's creation
	 * originated in CDT, then CDT specified the type as part of that request
	 * and this method must return that value. If the breakpoint's creation
	 * originated in the CDI client, then this method is invoked by CDT to
	 * discover the type of the breakpoint.
	 * 
	 * If the CDI breakpoint implements this interface, then
	 * {@link ICDIBreakpoint#isTemporary()} and
	 * {@link ICDIBreakpoint#isHardware()} will never get called by CDT, as this
	 * method is meant to replace those.
	 * 
	 * @return one of the type constants defined in ICBreakpointType (note that
	 *         {@link ICBreakpointType#TEMPORARY} can be bit-applied to any of
	 *         the type values to qualify it as a temporary breakpoint.
	 */
	int getType();
}
