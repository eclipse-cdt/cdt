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
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.ICDISignalManager;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.event.DestroyedEvent;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;

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
	ICDIConfiguration configuration;

	public Session(MISession miSession, boolean attach) {
		commonSetup();
		setConfiguration(new Configuration(miSession, attach));

		Target target = new Target(this, miSession);
		addTargets(new Target[] { target }, target);
	}

	public Session(MISession miSession) {
		commonSetup();
		setConfiguration(new CoreFileConfiguration());

		Target target = new Target(this, miSession);
		addTargets(new Target[] { target }, target);
	}

	public Session() {
		commonSetup();
		setConfiguration(new CoreFileConfiguration());
	}

	private void commonSetup() {
		props = new Properties();

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

	public void addTargets(Target[] targets, Target current) {
		ProcessManager pMgr = getProcessManager();
		pMgr.addTargets(targets, current);
	}

	public void removeTargets(Target[] targets) {
		ProcessManager pMgr = getProcessManager();
		pMgr.removeTargets(targets);
	}

	public Target getTarget(MISession miSession) {
		ProcessManager pMgr = getProcessManager();
		return pMgr.getTarget(miSession);
	}

	public Target getCurrentTarget() {
		ProcessManager pMgr = getProcessManager();
		return pMgr.getCurrentTarget();
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

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getBreakpointManager()
	 */
	public BreakpointManager getBreakpointManager() {
		return breakpointManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getEventManager()
	 */
	public ICDIEventManager getEventManager() {
		return eventManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getExpressionManager()
	 */
	public ICDIExpressionManager getExpressionManager() {
		return expressionManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getVariableManager()
	 */
	public ICDIVariableManager getVariableManager() {
		return variableManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getRegisterManager()
	 */
	public ICDIRegisterManager getRegisterManager() {
		return registerManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSharedLibraryManager()
	 */
	public ICDISharedLibraryManager getSharedLibraryManager() {
		return sharedLibraryManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getMemoryManager()
	 */
	public ICDIMemoryManager getMemoryManager() {
		return memoryManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSignalManager()
	 */
	public ICDISignalManager getSignalManager() {
		return signalManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSourceManager()
	 */
	public ICDISourceManager getSourceManager() {
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
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#setCurrentTarget()
	 */
	public void setCurrentTarget(Target target) throws CDIException {
		ProcessManager pMgr = getProcessManager();
		pMgr.setCurrentTarget((Target)target);
//		throw new CDIException(CdiResources.getString("cdi.Session.Unknown_target")); //$NON-NLS-1$
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
	public ICDIConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ICDIConfiguration conf) {
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
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#terminate(ICDITarget)
	 */
	public void terminate(ICDITarget target) throws CDIException {
		((Target)target).getMISession().terminate();
	}

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#addSearchPaths(String[])
	 */
	public void addSearchPaths(String[] dirs) throws CDIException {
		addSearchPaths(getCurrentTarget(), dirs);
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#addSearchPaths(String[])
	 */
	public void addSearchPaths(ICDITarget target, String[] dirs) throws CDIException {
		MISession miSession = ((Target)target).getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		MIEnvironmentDirectory dir = factory.createMIEnvironmentDirectory(dirs);
		try {
			miSession.postCommand(dir);
		 	dir.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSessionProcess()
	 */
	public Process getSessionProcess() throws CDIException {
		return getSessionProcess(getCurrentTarget());
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSessionProcess()
	 */
	public Process getSessionProcess(ICDITarget target) throws CDIException {
		MISession miSession = ((Target)target).getMISession();
		return miSession.getSessionProcess();
	}

}
