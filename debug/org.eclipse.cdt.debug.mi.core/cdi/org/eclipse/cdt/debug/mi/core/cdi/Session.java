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
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager;
import org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.ICDISignalManager;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;

/**
 * @see org.eclipse.cdt.debug.core.cdi.ICDISession
 */
public class Session implements ICDISession, ICDISessionObject {

	Properties props;
	MISession session;
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
	Target ctarget;

	public Session(MISession s, boolean attach) {
		commonSetup(s);
		configuration = new Configuration(s, attach);
	}

	public Session(MISession s) {
		commonSetup(s);
		configuration = new CoreFileConfiguration();
	}
	
	private void commonSetup(MISession s) {
		session = s;
		props = new Properties();

		breakpointManager = new BreakpointManager(this);

		eventManager = new EventManager(this);
		s.addObserver(eventManager);

		expressionManager = new ExpressionManager(this);
		variableManager = new VariableManager(this);
		registerManager = new RegisterManager(this);
		memoryManager = new MemoryManager(this);
		signalManager = new SignalManager(this);
		sourceManager = new SourceManager(this);
		sharedLibraryManager = new SharedLibraryManager(this);
		ctarget = new Target(this);
	}

	public MISession getMISession() {
		return session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getAttribute(String)
	 */
	public String getAttribute(String key) {
		return props.getProperty(key);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getBreakpointManager()
	 */
	public ICDIBreakpointManager getBreakpointManager() {
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
		return new ICDITarget[]{ctarget};
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getCurrentTarget()
	 */
	public ICDITarget getCurrentTarget() {
		return ctarget;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#setCurrentTarget()
	 */
	public void setCurrentTarget(ICDITarget target) throws CDIException {
		if (target instanceof Target) {
			ctarget = (Target)target;
		} else {
			throw new CDIException(CdiResources.getString("cdi.Session.Unknown_target")); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#setAttribute(String, String)
	 */
	public void setAttribute(String key, String value) {
		props.setProperty(key, value);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#terminate()
	 */
	public void terminate() throws CDIException {
		session.terminate();
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
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getRuntimeOptions()
	 */
	public ICDIRuntimeOptions getRuntimeOptions() {
		return new RuntimeOptions(this);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISessionObject#getSession()
	 */
	public ICDISession getSession() {
		return this;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#addSearchPaths(String[])
	 */
	public void addSearchPaths(String[] dirs) throws CDIException {
		CommandFactory factory = session.getCommandFactory();
		MIEnvironmentDirectory dir = factory.createMIEnvironmentDirectory(dirs);
		try {
			session.postCommand(dir);
		 	dir.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSessionProcess()
	 */
	public Process getSessionProcess() throws CDIException {
		return session.getSessionProcess();
	}

}
