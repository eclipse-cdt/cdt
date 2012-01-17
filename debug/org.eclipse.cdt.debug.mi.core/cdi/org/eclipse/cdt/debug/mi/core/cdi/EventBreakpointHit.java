/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIEventBreakpointHit;

/**
 * @since 7.0
 */
public class EventBreakpointHit extends SessionObject implements ICDIEventBreakpointHit {

	/**
	 * See description of eventType param in constructor
	 */
	private String fEventType;

	/**
	 * @param session
	 * @param eventType
	 *            the type of event breakpoint, in descriptive form (rather than
	 *            an ID). E.g., "signal", or "load". These are not standardized,
	 *            and can vary slightly from one gdb version to another, because
	 *            of difference in how catchpoint hits are reported. This string
	 *            should be used solely for display purposes.
	 */
	public EventBreakpointHit(Session session, String eventType) {
		super(session);
		assert (eventType != null) && (eventType.length() > 0);
		fEventType = eventType;
	}

	@Override
	public String getEventBreakpointType() {
		return fEventType;
	}
}
