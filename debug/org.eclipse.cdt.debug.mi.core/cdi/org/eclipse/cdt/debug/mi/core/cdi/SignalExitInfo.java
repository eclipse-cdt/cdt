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
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISignalExitInfo;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorSignalExitEvent;

/**.
 */
public class SignalExitInfo extends SessionObject implements ICDISignalExitInfo {

	MIInferiorSignalExitEvent event;

	public SignalExitInfo(Session session, MIInferiorSignalExitEvent e) {
		super(session);
		event = e;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalExitInfo#getName()
	 */
	public String getName() {
		return event.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalExitInfo#getDescription()
	 */
	public String getDescription() {
		return event.getMeaning();
	}

}
