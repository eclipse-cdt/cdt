/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
 
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDIDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager;
import org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISignalManager;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MISession;

/**
 * @see org.eclipse.cdt.debug.core.cdi.ICDISession
 */
public class CSession implements ICDISession, ICDISessionObject {

	Properties props;
	MISession session;
	BreakpointManager breakpointManager;
	EventManager eventManager;
	ExpressionManager expressionManager;
	MemoryManager memoryManager;
	SignalManager signalManager;
	SourceManager sourceManager;
	CTarget ctarget;

	public CSession(MISession s) {
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

	ICDITarget getTarget() {
		return ctarget;
	}
	
	CTarget getCTarget() {
			return ctarget;
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
	 * @see org.eclipse.cdt.debug.core.cdi.ICSuration()
	 */
	public ICDIDebugConfiguration getConfiguration() {
		return new DebugConfiguration();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getRuntimeOptions()
	 */
	public ICDIRuntimeOptions getRuntimeOptions() {
		return new RuntimeOptions();
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISessionObject#getSession()
	 */
	public ICDISession getSession() {
		return this;
	}

}
