package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;

/**
 */
public class DisconnectedEvent implements ICDIDisconnectedEvent {

	ICDIObject source;

	public DisconnectedEvent(CSession session) {
		source = (ICDIObject)session.getCTarget();
	}

	/**
	 * @see org.eclipse.cdt.debug.core..ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return source;
	}

}
