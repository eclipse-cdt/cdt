/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.ICDISignalManager;

/**
 */
public class SignalManager extends SessionObject implements ICDISignalManager {

	public SignalManager(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#getSignals()
	 */
	public ICDISignal[] getSignals() throws CDIException {
		return null;
	}

}
