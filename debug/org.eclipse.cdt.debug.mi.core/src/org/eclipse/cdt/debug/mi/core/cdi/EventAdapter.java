package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MILocationReachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;
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
public class EventAdapter {

	public static ICDIEvent getCDIEvent(CSession session, MIEvent miEvent) {
		if (miEvent instanceof MIBreakpointEvent) {
			return new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MIInferiorExitEvent) {
		} else if (miEvent instanceof MIExitEvent) {
		} else if (miEvent instanceof MIFunctionFinishedEvent) {
		} else if (miEvent instanceof MILocationReachedEvent) {
		} else if (miEvent instanceof MISignalEvent) {
		} else if (miEvent instanceof MIStepEvent) {
			return new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MIWatchpointEvent) {
		} else if (miEvent instanceof MIRunningEvent) {
			MIRunningEvent running = (MIRunningEvent)miEvent;
			if (running.isStepping()) {
				return new SteppingEvent(session, miEvent);
			} else {
				return new ResumedEvent(session, miEvent);
			}
		}
		return null;
	}
}
