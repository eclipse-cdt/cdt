package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;

/**
 */
public class ResumedEvent implements ICDIResumedEvent {

	CSession session;
	MIRunningEvent event;

	public ResumedEvent(CSession s, MIRunningEvent e) {
		session = s;
		event = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return session.getCurrentTarget();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent#getType()
	 */
	public int getType() {
		MIRunningEvent running = (MIRunningEvent)event;
		return running.getType();
	}

}
