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
import org.eclipse.cdt.debug.mi.core.MISession;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SignalManager implements ICSignalManager {

	MISession session;
	
	public SignalManager(MISession s) {
		session = s;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSignalManager#getSignals()
	 */
	public ICSignal[] getSignals() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSessionObject#getSession()
	 */
	public ICSession getSession() {
		return null;
	}

}
