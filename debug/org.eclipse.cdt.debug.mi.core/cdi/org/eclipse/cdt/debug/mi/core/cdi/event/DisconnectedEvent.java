package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.cdi.Session;

/**
 */
public class DisconnectedEvent implements ICDIDisconnectedEvent {

	ICDIObject source;

	public DisconnectedEvent(Session session) {
		source = session.getCurrentTarget();
	}

	/**
	 * @see org.eclipse.cdt.debug.core..ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return source;
	}

}
