/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.ICSignal;
import org.eclipse.cdt.debug.core.cdi.ICSignalManager;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SignalManager extends SessionObject implements ICSignalManager {

	public SignalManager(Session session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSignalManager#getSignals()
	 */
	public ICSignal[] getSignals() throws CDIException {
		return null;
	}

}
