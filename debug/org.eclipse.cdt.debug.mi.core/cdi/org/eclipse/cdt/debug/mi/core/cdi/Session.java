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
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
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

	public final static Target[] EMPTY_TARGETS = {};
	Properties props;
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
	Target[] debugTargets = EMPTY_TARGETS;
	Target currentTarget;

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
		Target[] newTargets = new Target[debugTargets.length + targets.length];
		System.arraycopy(debugTargets, 0, newTargets, 0, debugTargets.length);
		System.arraycopy(targets, 0, newTargets, debugTargets.length, targets.length);
		if (current != null) {
			currentTarget = current;
		}
		for (int i = 0; i < targets.length; ++i) {
			MISession miSession = targets[i].getMISession();
			if (miSession != null) {
				miSession.addObserver((EventManager)getEventManager());
			}
		}
	}

	public void removeTargets(Target[] targets) {
		ArrayList list = new ArrayList(Arrays.asList(debugTargets));
		for (int i = 0; i < targets.length; ++i) {
			MISession miSession = targets[i].getMISession();
			if (miSession != null) {
				miSession.deleteObserver((EventManager)getEventManager());
			}
			if (currentTarget != null && currentTarget.equals(targets[i])) {
				currentTarget = null;
			}
			list.remove(targets[i]);
		}
		debugTargets = (Target[]) list.toArray(new Target[list.size()]);
	}

	public Target getTarget(MISession miSession) {
		for (int i = 0; i < debugTargets.length; ++i) {
			MISession mi = debugTargets[i].getMISession();
			if (mi.equals(miSession)) {
				return debugTargets[i];
			}
		}
		// ASSERT: it should not happen.
		return null;
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
		return debugTargets;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getCurrentTarget()
	 */
	public ICDITarget getCurrentTarget() {
		return currentTarget;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#setCurrentTarget()
	 */
	public void setCurrentTarget(ICDITarget target) throws CDIException {
		if (target instanceof Target) {
			currentTarget = (Target)target;
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
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#terminate(ICDITarget)
	 */
	public void terminate() throws CDIException {
		for (int i = 0; i < debugTargets.length; ++i) {
			debugTargets[i].terminate();
		}
		//TODO: the ExitEvent is sent by MISession.terminate()
		// We nee move it here.
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
		addSearchPaths(currentTarget, dirs);
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
		return getSessionProcess(currentTarget);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getSessionProcess()
	 */
	public Process getSessionProcess(ICDITarget target) throws CDIException {
		MISession miSession = ((Target)target).getMISession();
		return miSession.getSessionProcess();
	}

}
