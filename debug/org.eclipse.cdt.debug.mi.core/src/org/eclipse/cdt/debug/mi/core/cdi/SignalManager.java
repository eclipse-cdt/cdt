/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISignalManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;

/**
 */
public class SignalManager extends SessionObject implements ICDISignalManager {

	boolean autoupdate;

	public SignalManager(Session session) {
		super(session);
		autoupdate = false;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#getSignals()
	 */
	public ICDISignal[] getSignals() throws CDIException {
		return new ICDISignal[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoupdate;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		autoupdate = update;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#update()
	 */
	public void update() throws CDIException {
	}

}
