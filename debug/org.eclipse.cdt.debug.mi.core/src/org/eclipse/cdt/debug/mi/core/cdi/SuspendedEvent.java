package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICLocation;
import org.eclipse.cdt.debug.core.cdi.ICSessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICSuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICObject;
import org.eclipse.cdt.debug.core.cdi.model.ICStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICTarget;
import org.eclipse.cdt.debug.core.cdi.model.ICVariable;
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
public class SuspendedEvent implements ICSuspendedEvent {

	MIBreakpointEvent event;
	CSession session;

	public SuspendedEvent(CSession s, MIBreakpointEvent e) {
		session = s;
		event = e;
	}

	public ICSessionObject getReason() {
		return new SessionObject(session);
	}
				
	public ICStackFrame getStackFrame() {
		return new StackFrame(session, event.getMIFrame());
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICEvent#getSource()
	 */
	public ICObject getSource() {
		return null;
	}

}
