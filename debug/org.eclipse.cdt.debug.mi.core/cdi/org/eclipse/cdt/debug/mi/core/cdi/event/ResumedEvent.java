/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;

/**
 */
public class ResumedEvent implements ICDIResumedEvent {

	Session session;
	MIRunningEvent event;

	public ResumedEvent(Session s, MIRunningEvent e) {
		session = s;
		event = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		// We can send the target as the Source.  CDI
		// Will assume that all threads are supended for this.
		// This is true for gdb when it suspend the inferior
		// all threads are suspended.
		Target target = session.getTarget(event.getMISession());
		return target;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent#getType()
	 */
	public int getType() {
		MIRunningEvent running = event;
		int type = running.getType();
		int cdiType = 0;
		switch (type) {
			case MIRunningEvent.CONTINUE:
				cdiType = ICDIResumedEvent.CONTINUE;
			break;

			case MIRunningEvent.UNTIL:
			case MIRunningEvent.NEXT:
				cdiType = ICDIResumedEvent.STEP_OVER;
			break;

			case MIRunningEvent.NEXTI:
				cdiType = ICDIResumedEvent.STEP_OVER_INSTRUCTION;
			break;

			case MIRunningEvent.STEP:
				cdiType = ICDIResumedEvent.STEP_INTO;
			break;

			case MIRunningEvent.STEPI:
				cdiType = ICDIResumedEvent.STEP_INTO_INSTRUCTION;
			break;

			case MIRunningEvent.RETURN:
			case MIRunningEvent.FINISH:
				cdiType = ICDIResumedEvent.STEP_RETURN;
			break;

		}
		return cdiType;
	}

}
