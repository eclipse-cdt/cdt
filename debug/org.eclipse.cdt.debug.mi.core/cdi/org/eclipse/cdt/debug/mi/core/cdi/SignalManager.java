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
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISignalManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Signal;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIHandle;
import org.eclipse.cdt.debug.mi.core.command.MIInfoSignals;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIInfoSignalsInfo;
import org.eclipse.cdt.debug.mi.core.output.MISigHandle;

/**
 */
public class SignalManager extends Manager implements ICDISignalManager {

	ICDISignal[] EMPTY_SIGNALS = {};
	MISigHandle[] noSigs =  new MISigHandle[0];
	Map signalsMap;

	public SignalManager(Session session) {
		super(session, false);
		signalsMap = new Hashtable();
	}

	synchronized List getSignalsList(Target target) {
		List signalsList = (List)signalsMap.get(target);
		if (signalsList == null) {
			signalsList = Collections.synchronizedList(new ArrayList());
			signalsMap.put(target, signalsList);
		}
		return signalsList;
	}

	MISigHandle[] getMISignals(MISession miSession) throws CDIException {
		MISigHandle[] miSigs;
		CommandFactory factory = miSession.getCommandFactory();
		MIInfoSignals sigs = factory.createMIInfoSignals();
		try {
			miSession.postCommand(sigs);
			MIInfoSignalsInfo info = sigs.getMIInfoSignalsInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			miSigs =  info.getMISignals();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return miSigs;
	}

	MISigHandle getMISignal(MISession miSession, String name) throws CDIException {
		MISigHandle sig = null;
		CommandFactory factory = miSession.getCommandFactory();
		MIInfoSignals sigs = factory.createMIInfoSignals(name);
		try {
			miSession.postCommand(sigs);
			MIInfoSignalsInfo info = sigs.getMIInfoSignalsInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			MISigHandle[] miSigs =  info.getMISignals();
			if (miSigs.length > 0) {
				sig = miSigs[0];
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return sig;
	}

	/**
	 * Method hasSignalChanged.
	 * @param sig
	 * @param mISignal
	 * @return boolean
	 */
	private boolean hasSignalChanged(ICDISignal sig, MISigHandle miSignal) {
		return !sig.getName().equals(miSignal.getName()) ||
			sig.isStopSet() != miSignal.isStop() ||
			sig.isIgnore() != !miSignal.isPass();
	}

	protected ICDISignal findSignal(Target target, String name) {
		ICDISignal sig = null;
		List signalsList = (List) signalsMap.get(target);
		if (signalsList != null) {
			ICDISignal[] sigs = (ICDISignal[])signalsList.toArray(new ICDISignal[0]);
			for (int i = 0; i < sigs.length; i++) {
				if (sigs[i].getName().equals(name)) {
					sig = sigs[i];
					break;
				}
			}
		}
		return sig;
	}

	public ICDISignal getSignal(MISession miSession, String name) {
		Session session = (Session)getSession();
		Target target = session.getTarget(miSession);
		return getSignal(target, name);
	}
	public ICDISignal getSignal(Target target, String name) {
		ICDISignal sig = findSignal(target, name);
		if (sig == null) {
			MISigHandle miSig = null;
			try {
				miSig = getMISignal(target.getMISession(), name);
				sig = new Signal(target, miSig);
				List signalsList = getSignalsList(target);
				signalsList.add(sig);
			} catch (CDIException e) {
				// The session maybe terminated because of the signal.
				miSig = new MISigHandle(name, false, false, false, name);
				sig = new Signal(target, miSig);
			}
		}
		return sig;
	}

	public void handle(Signal sig, boolean isIgnore, boolean isStop) throws CDIException {
		Target target = (Target)sig.getTarget();
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		StringBuffer buffer = new StringBuffer(sig.getName());
		buffer.append(" "); //$NON-NLS-1$
		if (isIgnore) {
			buffer.append("ignore"); //$NON-NLS-1$
		} else {
			buffer.append("noignore"); //$NON-NLS-1$
		}
		buffer.append(" "); //$NON-NLS-1$
		if (isStop) {
			buffer.append("stop"); //$NON-NLS-1$
		} else  {
			buffer.append("nostop"); //$NON-NLS-1$
		}
		MIHandle handle = factory.createMIHandle(buffer.toString());
		try {
			miSession.postCommand(handle);
			handle.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		sig.getMISignal().handle(isIgnore, isStop);
		miSession.fireEvent(new MISignalChangedEvent(miSession, sig.getName()));
	}

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#getSignals()
	 */
	public ICDISignal[] getSignals() throws CDIException {
		Target target = ((Session)getSession()).getCurrentTarget();
		return getSignals(target);
	}

	public ICDISignal[] getSignals(Target target) throws CDIException {
		List signalsList = (List)signalsMap.get(target);
		if (signalsList == null) {
			update(target);
		}
		signalsList = (List)signalsMap.get(target);
		if (signalsList != null) {
			return (ICDISignal[])signalsList.toArray(new ICDISignal[0]);
		}
		return EMPTY_SIGNALS;
	}

	public void update(Target target) throws CDIException {
		MISession miSession = target.getMISession();
		MISigHandle[] miSigs = getMISignals(miSession);
		List eventList = new ArrayList(miSigs.length);
		List signalsList = getSignalsList(target);
		for (int i = 0; i < miSigs.length; i++) {
			ICDISignal sig = findSignal(target, miSigs[i].getName());
			if (sig != null) {
				if (hasSignalChanged(sig, miSigs[i])) {
					// Fire ChangedEvent
					((Signal)sig).setMISignal(miSigs[i]);
					eventList.add(new MISignalChangedEvent(miSession, miSigs[i].getName())); 
				}
			} else {
				// add the new breakpoint and fire CreatedEvent
				signalsList.add(new Signal(target, miSigs[i]));
				//eventList.add(new MISignCreatedEvent(miSession, miSigs[i].getName()));
			}
		}
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		miSession.fireEvents(events);
	}

} 
