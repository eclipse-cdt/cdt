/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core;

import java.util.Properties;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 */
public class GDBProcess extends PlatformObject implements IProcess {

	MISession session;
	ILaunch launch;
	Properties props;
	GDBStreamsProxy streams;
	
	public GDBProcess(ILaunch l, MISession s) {
		launch = l;
		session = s;
		props = new Properties();
	}
	
	/**
	 * @see org.eclipse.debug.core.model.IProcess#getAttribute(String)
	 */
	public String getAttribute(String key) {
		return props.getProperty(key);
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getExitValue()
	 */
	public int getExitValue() throws DebugException {
		return session.getMIProcess().exitValue();
	}

	/**
	 * @see org.eclipse.debug.core.model.IProcess#getLabel()
	 */
	public String getLabel() {
		return "GDB MI Debugger";
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
	 * @see org.eclipse.debug.core.model.IProcess#setAttribute(String, String)
	 */
	public void setAttribute(String key, String value) {
		props.setProperty(key, value);
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return super.getAdapter(adapter);
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return true;
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
