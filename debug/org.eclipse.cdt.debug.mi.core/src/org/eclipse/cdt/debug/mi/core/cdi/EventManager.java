/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICEventManager;
import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.event.ICEventListener;
import org.eclipse.cdt.debug.mi.core.MISession;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation oEventManagerts go to
 * Window>Preferences>Java>Code Generation.
 */
public class EventManager implements ICEventManager {

	MISession session;
	
	public EventManager(MISession s) {
		session = s;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICEventManager#addEventListener(ICEventListener)
	 */
	public void addEventListener(ICEventListener listener) {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICEventManager#removeEventListener(ICEventListener)
	 */
	public void removeEventListener(ICEventListener listener) {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSessionObject#getSession()
	 */
	public ICSession getSession() {
		return null;
	}

}
