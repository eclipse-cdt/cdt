package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
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
public class EventAdapter {

	public static ICDIEvent getCEvent(final CSession session, final MIEvent miEvent) {
		if (miEvent instanceof MIBreakpointEvent) {
			return new SuspendedEvent(session, (MIBreakpointEvent)miEvent);
		} else if (miEvent instanceof MIInferiorExitEvent) {
		} else if (miEvent instanceof MIExitEvent) {
		} else if (miEvent instanceof MIFunctionFinishedEvent) {
		} else if (miEvent instanceof MILocationReachedEvent) {
		} else if (miEvent instanceof MISignalEvent) {
		} else if (miEvent instanceof MIStepEvent) {
		} else if (miEvent instanceof MIWatchpointEvent) {
		}
		return null;
	}
}
