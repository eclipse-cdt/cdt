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
import org.eclipse.cdt.debug.mi.core.event.MICatchpointHitEvent;

/**
 * @since 7.0
 */
public class EventBreakpointHit extends SessionObject implements ICDIEventBreakpointHit {

	MICatchpointHitEvent fMiEvent;

	public EventBreakpointHit(Session session, MICatchpointHitEvent miEvent) {
		super(session);
		fMiEvent = miEvent;
	}

	public String getEventBreakpointType() {
		return fMiEvent.getCatchpointType();
	}
}
