/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICEventManager;
import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.event.ICEventListener;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation oEventManagerts go to
 * Window>Preferences>Java>Code Generation.
 */
public class EventManager extends SessionObject implements ICEventManager {

	public EventManager(Session session) {
		super(session);
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

}
