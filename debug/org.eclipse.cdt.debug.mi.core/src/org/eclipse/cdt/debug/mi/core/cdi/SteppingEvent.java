package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICDISteppingEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SteppingEvent implements ICDISteppingEvent {

	CSession session;
	MIEvent event;
	
	public SteppingEvent(CSession s, MIEvent e) {
		session = s;
		event = e;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDISteppingEvent#getType()
	 */
	public int getType() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return session.getTarget();
	}

}
