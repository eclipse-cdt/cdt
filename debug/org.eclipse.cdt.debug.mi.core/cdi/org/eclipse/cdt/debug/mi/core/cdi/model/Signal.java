/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.cdi.SignalManager;
import org.eclipse.cdt.debug.mi.core.output.MISigHandle;

/**
 */
public class Signal extends CObject implements ICDISignal {

	MISigHandle sig;

	public Signal(Target target, MISigHandle s) {
		super(target);
		sig = s;
	}
		
	public void setMISignal(MISigHandle s) {
		sig = s;
	}

	public MISigHandle getMISignal() {
		return sig;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#getMeaning()
	 */
	public String getDescription() {
		return sig.getDescription();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#getName()
	 */
	public String getName() {
		return sig.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#handle()
	 */
	public void handle(boolean ignore, boolean stop) throws CDIException {
		SignalManager mgr = (SignalManager)getTarget().getSession().getSignalManager();
		mgr.handle(this, ignore, stop);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#isIgnore()
	 */
	public boolean isIgnore() {
		return !sig.isPass();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#isStopSet()
	 */
	public boolean isStopSet() {
		return sig.isStop();
	}

	/**
	 * Continue program giving it signal specified by the argument.
	 */
	public void signal() throws CDIException {
		getTarget().signal(this);
	}
}
