/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
 
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.Properties;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager;
import org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISignalManager;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import sun.security.krb5.internal.crypto.e;

/**
 * @see org.eclipse.cdt.debug.core.cdi.ICDISession
 */
public class CSession implements ICDISession, ICDISessionObject {

	Properties props;
	MISession session;
	BreakpointManager breakpointManager;
	EventManager eventManager;
	VariableManager variableManager;
	MemoryManager memoryManager;
	SignalManager signalManager;
	SourceManager sourceManager;
	ICDIConfiguration configuration;
	CTarget ctarget;

	public CSession(MISession s, boolean attach) {
		commonSetup(s);
		configuration = new Configuration(attach);
	}

	public CSession(MISession s) {
		commonSetup(s);
		configuration = new CoreFileConfiguration();
	}
	
	private void commonSetup(MISession s) {
		session = s;
		props = new Properties();
		breakpointManager = new BreakpointManager(this);
		eventManager = new EventManager(this);
		s.addObserver(eventManager);
		variableManager = new VariableManager(this);
		memoryManager = new MemoryManager(this);
		signalManager = new SignalManager(this);
		sourceManager = new SourceManager(this);
		ctarget = new CTarget(this);
	}
	MISession getMISession() {
		return session;
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
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getVariableManager()
	 */
	public ICDIExpressionManager getExpressionManager() {
		return variableManager;
	}

	/**
	 * 
	 */
	public VariableManager getVariableManager() {
		return variableManager;
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
	 */
	public void setCurrentTarget(ICDITarget target) throws CDIException {
		if (target instanceof CTarget) {
			ctarget = (CTarget)target;
			return;
		}
		throw new CDIException("Unkown target");
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
	public ICDIConfiguration getConfiguration() {
		return configuration;
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
			MIInfo info = dir.getMIInfo();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

}
