/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
 
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICEventManager;
import org.eclipse.cdt.debug.core.cdi.ICExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICMemoryManager;
import org.eclipse.cdt.debug.core.cdi.ICRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.ICSignalManager;
import org.eclipse.cdt.debug.core.cdi.ICSourceManager;
import org.eclipse.cdt.debug.core.cdi.model.ICTarget;
import org.eclipse.cdt.debug.mi.core.MISession;

/**
 * @see org.eclipse.cdt.debug.core.cdi.ICSession
 */
public class Session implements ICSession {

	Properties props;
	MISession session;
	BreakpointManager breakpointManager;
	EventManager eventManager;
	ExpressionManager expressionManager;
	MemoryManager memoryManager;
	SignalManager signalManager;
	SourceManager sourceManager;
	CTarget ctarget;

	public Session(MISession s) {
		session = s;
		props = new Properties();
		breakpointManager = new BreakpointManager(this);
		eventManager = new EventManager(this);
		expressionManager = new ExpressionManager(this);
		memoryManager = new MemoryManager(this);
		signalManager = new SignalManager(this);
		sourceManager = new SourceManager(this);
		ctarget = new CTarget(this);
	}
	
	MISession getMISession() {
		return session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#getAttribute(String)
	 */
	public String getAttribute(String key) {
		return props.getProperty(key);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#getBreakpointManager()
	 */
	public ICBreakpointManager getBreakpointManager() {
		return breakpointManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#getEventManager()
	 */
	public ICEventManager getEventManager() {
		return eventManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#getExpressionManager()
	 */
	public ICExpressionManager getExpressionManager() {
		return expressionManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#getMemoryManager()
	 */
	public ICMemoryManager getMemoryManager() {
		return memoryManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#getSignalManager()
	 */
	public ICSignalManager getSignalManager() {
		return signalManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#getSourceManager()
	 */
	public ICSourceManager getSourceManager() {
		return sourceManager;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#getTargets()
	 */
	public ICTarget[] getTargets() {
		return new ICTarget[]{ctarget};
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#isTerminated()
	 */
	public boolean isTerminated() {
		return session.isTerminated();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#setAttribute(String, String)
	 */
	public void setAttribute(String key, String value) {
		props.setProperty(key, value);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#terminate()
	 */
	public void terminate() throws CDIException {
		session.terminate();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSuration()
	 */
	public ICDebugConfiguration getConfiguration() {
		return new DebugConfiguration();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSession#getRuntimeOptions()
	 */
	public ICRuntimeOptions getRuntimeOptions() {
		return new RuntimeOptions();
	}
}
