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

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.event.DestroyedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 * @see org.eclipse.cdt.debug.core.cdi.ICDISession
 */
public class Session implements ICDISession, ICDISessionObject {

	public final static Target[] EMPTY_TARGETS = {};
	Properties props;
	ProcessManager processManager;
	EventManager eventManager;
	BreakpointManager breakpointManager;
	ExpressionManager expressionManager;
	VariableManager variableManager;
	RegisterManager registerManager;
	MemoryManager memoryManager;
	SharedLibraryManager sharedLibraryManager;
	SignalManager signalManager;
	SourceManager sourceManager;
	ICDISessionConfiguration configuration;

	public Session(MISession miSession, boolean attach) {
		commonSetup();
		//setConfiguration(new SessionConfiguration(this));

		Target target = new Target(this, miSession);
		addTargets(new Target[] { target });
	}

	public Session(MISession miSession) {
		commonSetup();
		//setConfiguration(new CoreFileConfiguration());

		Target target = new Target(this, miSession);
		addTargets(new Target[] { target });
	}

	private void commonSetup() {
		props = new Properties();
		setConfiguration(new SessionConfiguration(this));

		processManager = new ProcessManager(this);
		breakpointManager = new BreakpointManager(this);
		eventManager = new EventManager(this);
		expressionManager = new ExpressionManager(this);
		variableManager = new VariableManager(this);
		registerManager = new RegisterManager(this);
		memoryManager = new MemoryManager(this);
		signalManager = new SignalManager(this);
		sourceManager = new SourceManager(this);
		sharedLibraryManager = new SharedLibraryManager(this);
	}

	public void addTargets(Target[] targets) {
		ProcessManager pMgr = getProcessManager();
		pMgr.addTargets(targets);
	}

	public void removeTargets(Target[] targets) {
		ProcessManager pMgr = getProcessManager();
		pMgr.removeTargets(targets);
	}

	public Target getTarget(MISession miSession) {
		ProcessManager pMgr = getProcessManager();
		return pMgr.getTarget(miSession);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getAttribute(String)
	 */
	public String getAttribute(String key) {
		return props.getProperty(key);
	}

	public ProcessManager getProcessManager() {
		return processManager;
	}

	public BreakpointManager getBreakpointManager() {
		return breakpointManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getEventManager()
	 */
	public ICDIEventManager getEventManager() {
		return eventManager;
	}

	public ExpressionManager getExpressionManager() {
		return expressionManager;
	}

	public VariableManager getVariableManager() {
		return variableManager;
	}

	public RegisterManager getRegisterManager() {
		return registerManager;
	}

	public SharedLibraryManager getSharedLibraryManager() {
		return sharedLibraryManager;
	}

	public MemoryManager getMemoryManager() {
		return memoryManager;
	}

	public SignalManager getSignalManager() {
		return signalManager;
	}

	public SourceManager getSourceManager() {
		return sourceManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getTargets()
	 */
	public ICDITarget[] getTargets() {
		ProcessManager pMgr = getProcessManager();
		return pMgr.getCDITargets();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#setAttribute(String, String)
	 */
	public void setAttribute(String key, String value) {
		props.setProperty(key, value);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getConfiguration()
	 */
	public ICDISessionConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ICDISessionConfiguration conf) {
		configuration = conf;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISessionObject#getSession()
	 */
	public ICDISession getSession() {
		return this;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#terminate(ICDITarget)
	 */
	public void terminate() throws CDIException {
		ProcessManager pMgr = getProcessManager();
		Target[] targets = pMgr.getTargets();
		for (int i = 0; i < targets.length; ++i) {
			if (!targets[i].getMISession().isTerminated()) {
				targets[i].getMISession().terminate();
			}
		}
		// Do not do the removeTargets(), Target.getMISession().terminate() will do it 
		// via an event, MIGDBExitEvent of the mi session
		//removeTargets(targets);
		
		// wait ~2 seconds for the targets to be terminated.
		for (int i = 0; i < 2; ++i) {
			targets = pMgr.getTargets();
			if (targets.length == 0) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//
			}
		}
		// send our goodbyes.
		EventManager eMgr = (EventManager)getEventManager();
		eMgr.fireEvents(new ICDIEvent[] { new DestroyedEvent(this) });
		eMgr.removeEventListeners();
	}

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSessionProcess()
	 */
	public Process getSessionProcess() throws CDIException {
		ICDITarget[] targets = getTargets();
		if (targets != null && targets.length > 0) {
			return getSessionProcess(targets[0]);
		}
		return null;
	}
	
	public Process getSessionProcess(ICDITarget target) {
		MISession miSession = ((Target)target).getMISession();
		return miSession.getSessionProcess();
	}

}
