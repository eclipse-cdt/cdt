package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MILocationReachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;
import org.eclipse.cdt.debug.mi.core.event.MIStepEvent;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointEvent;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SuspendedEvent implements ICDISuspendedEvent {

	MIBreakpointEvent event;
	CSession session;

	public SuspendedEvent(CSession s, MIBreakpointEvent e) {
		session = s;
		event = e;
	}

	public ICDISessionObject getReason() {
		return new SessionObject(session);
	}
				
	public ICDIStackFrame getStackFrame() {
		return new StackFrame(session, event.getMIFrame());
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return null;
	}

}
