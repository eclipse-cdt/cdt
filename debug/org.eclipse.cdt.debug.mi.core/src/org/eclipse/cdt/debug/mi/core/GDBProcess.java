/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core;

import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 */
public class GDBProcess extends PlatformObject implements IProcess {

	MISession session;
	ILaunch launch;
	Properties props;
	GDBStreamsProxy streams;
	String label;
	
	public GDBProcess(ILaunch l, MISession s, String n) {
		launch = l;
		session = s;
		label = n;
	}
	
	/**
	 * @see org.eclipse.debug.core.model.IProcess#getAttribute(String)
	 */
	public String getAttribute(String key) {
		if (props == null) {
			return null;
		}
		return props.getProperty(key);
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#setAttribute(String, String)
	 */
	public void setAttribute(String key, String value) {
		if (props == null) {
			props = new Properties();
		}
		props.setProperty(key, value);
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getExitValue()
	 */
	public int getExitValue() throws DebugException {
		try {
			return session.getMIProcess().exitValue();
		} catch (IllegalThreadStateException e) {
			IStatus status = new Status(IStatus.ERROR,
				MIPlugin.getUniqueIdentifier(), 1, "process not terminated", e);
			throw new DebugException(status);
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getLabel()
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getLaunch()
	 */
	public ILaunch getLaunch() {
		return launch;
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getStreamsProxy()
	 */
	public IStreamsProxy getStreamsProxy() {
		if (streams == null) {
			streams = new GDBStreamsProxy(session);
		}
		return streams;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IProcess.class)) {
			return this;
		}
		if (adapter.equals(IDebugTarget.class)) {
			ILaunch launch = getLaunch();
			IDebugTarget[] targets = launch.getDebugTargets();
			for (int i = 0; i < targets.length; i++) {
				if (this.equals(targets[i].getProcess())) {
					return targets[i];
				}
			}
			return null;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !isTerminated();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return session.isTerminated();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		session.terminate();
	}

}
