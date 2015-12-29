/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
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
	@Override
	public String getDescription() {
		return sig.getDescription();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#getName()
	 */
	@Override
	public String getName() {
		return sig.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#handle()
	 */
	@Override
	public void handle(boolean ignore, boolean stop) throws CDIException {
		SignalManager mgr = ((Session)getTarget().getSession()).getSignalManager();
		mgr.handle(this, ignore, stop);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#isIgnore()
	 */
	@Override
	public boolean isIgnore() {
		return !sig.isPass();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#isStopSet()
	 */
	@Override
	public boolean isStopSet() {
		return sig.isStop();
	}

	/**
	 * Continue program giving it signal specified by the argument.
	 */
	@Override
	public void signal() throws CDIException {
		getTarget().resume(this);
	}
}
