/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation oEventManagerts go to
 * Window>Preferences>Java>Code Generation.
 */
public class EventManager extends SessionObject implements ICDIEventManager {

	Map map = Collections.synchronizedMap(new HashMap());

	class CDIObserver implements Observer {
		ICDIEventListener listener;
		public CDIObserver(ICDIEventListener l) {
			listener = l;
		}
		public void update(Observable o, Object args) {
			MIEvent[] events = (MIEvent[])args;
			for (int i = 0; i < events.length; i++) {
				ICDIEvent cdiEvent = EventAdapter.getCDIEvent(getCSession(), events[i]);
				listener.handleDebugEvent(cdiEvent);
			}
		}
	}

	public EventManager(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#addEventListener(ICDIEventListener)
	 */
	public void addEventListener(ICDIEventListener listener) {
		CDIObserver cdiObserver = new CDIObserver(listener);
		map.put(listener, cdiObserver);
		MISession session = getCSession().getMISession();
		session.addObserver(cdiObserver);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#removeEventListener(ICDIEventListener)
	 */
	public void removeEventListener(ICDIEventListener listener) {
		CDIObserver cdiObserver = (CDIObserver)map.remove(listener);
		if (cdiObserver != null) {
			MISession session = getCSession().getMISession();
			session.deleteObserver(cdiObserver);
		}
	}
}
