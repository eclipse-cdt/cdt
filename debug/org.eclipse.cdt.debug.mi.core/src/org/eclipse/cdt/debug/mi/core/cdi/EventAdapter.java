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
import org.eclipse.cdt.debug.mi.core.event.MISteppingRangeEvent;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointEvent;

/**
 */
public class EventAdapter {

	public static ICDIEvent getCDIEvent(CSession session, MIEvent miEvent) {
		if (miEvent instanceof MIBreakpointEvent
			|| miEvent instanceof MIFunctionFinishedEvent
			|| miEvent instanceof MILocationReachedEvent
			|| miEvent instanceof MISignalEvent
			|| miEvent instanceof MISteppingRangeEvent
			|| miEvent instanceof MIWatchpointEvent) {
			return new SuspendedEvent(session, miEvent);
		} else if (miEvent instanceof MIRunningEvent) {
			return new ResumedEvent(session, (MIRunningEvent)miEvent);
		} else if (miEvent instanceof MIInferiorExitEvent) {
			return new ExitedEvent(session, (MIInferiorExitEvent)miEvent);
		} else if (miEvent instanceof MIExitEvent) {
			return new DestroyedEvent(session, (MIExitEvent)miEvent);
		}
		return null;
	}
}
