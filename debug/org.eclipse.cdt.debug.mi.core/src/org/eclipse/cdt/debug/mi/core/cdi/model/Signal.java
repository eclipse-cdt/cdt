/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.cdi.SignalManager;
import org.eclipse.cdt.debug.mi.core.output.MISignal;

/**
 */
public class Signal extends CObject implements ICDISignal {

	SignalManager mgr;
	MISignal sig;

	public Signal(SignalManager m, MISignal s) {
		super(m.getSession().getCurrentTarget());
		sig = s;
	}
		
	public void setMISignal(MISignal s) {
		sig = s;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#getMeaning()
	 */
	public String getMeaning() {
		return sig.getDescription();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#getName()
	 */
	public String getName() {
		return sig.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#handle()
	 */
	public void handle(boolean ignore, boolean stop) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#isIgnore()
	 */
	public boolean isIgnore() {
		return !sig.isPass();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalReceived#isStopSet()
	 */
	public boolean isStopSet() {
		return sig.isStop();
	}

}
