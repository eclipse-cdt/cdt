package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISignal;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Signal extends SessionObject implements ICDISignal {

	MISignalEvent event;
	public Signal(CSession session, MISignalEvent e) {
		super(session);
		event = e;
	}
		
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#getMeaning()
	 */
	public String getMeaning() {
		return event.getMeaning();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#getName()
	 */
	public String getName() {
		return event.getName();
	}

}
