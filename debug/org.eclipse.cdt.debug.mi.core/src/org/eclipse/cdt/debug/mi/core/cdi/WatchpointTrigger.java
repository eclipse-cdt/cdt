package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointEvent;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WatchpointTrigger extends WatchpointScope implements ICDIWatchpointTrigger {

	public WatchpointTrigger(CSession session, MIWatchpointEvent e) {
		super(session, e);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger#getNewValue()
	 */
	public String getNewValue() {
		return watchEvent.getNewValue();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger#getOldValue()
	 */
	public String getOldValue() {
		return watchEvent.getOldValue();
	}

}
