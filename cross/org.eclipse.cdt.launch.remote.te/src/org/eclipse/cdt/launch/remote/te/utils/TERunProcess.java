/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation              - initial API and implementation
 * Anna Dushistova (MontaVista) - adapted from org.eclipse.debug.core.model.RuntimeProcess     
 *******************************************************************************/

package org.eclipse.cdt.launch.remote.te.utils;

import java.util.EventObject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.services.IProcesses.ProcessContext;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
import org.eclipse.tcf.te.tcf.processes.core.launcher.ProcessLauncher;
import org.eclipse.tcf.te.tcf.processes.core.launcher.ProcessStateChangeEvent;

public class TERunProcess extends PlatformObject implements IProcess,
		IEventListener {

	private ProcessLauncher prLauncher;
	private String prName;
	private boolean terminated;
	private ILaunch launch;
	private ProcessContext context;
	private int exitValue;

	public TERunProcess(ILaunch launch, String remoteExePath, String arguments,
			String label, IPeer peer, SubProgressMonitor monitor) {
		this.launch = launch;
		// initializeAttributes(attributes);
		prName = remoteExePath;
		terminated = false;
		launch.addProcess(this);
		fireCreationEvent();
		EventManager.getInstance().addEventListener(this,
				ProcessStateChangeEvent.class);
		try {
			prLauncher = TEHelper.launchCmd(peer, remoteExePath, arguments,
					null, monitor, new Callback());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
		if (adapter.equals(ILaunch.class)) {
			return getLaunch();
		}
		// CONTEXTLAUNCHING
		if (adapter.equals(ILaunchConfiguration.class)) {
			return getLaunch().getLaunchConfiguration();
		}
		return super.getAdapter(adapter);
	}

	public boolean canTerminate() {
		return !terminated;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void terminate() throws DebugException {
		if (!isTerminated()) {
			prLauncher.terminate();
			terminated = true;
			fireTerminateEvent();
		}
	}

	public String getLabel() {
		return prName;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	public IStreamsProxy getStreamsProxy() {
		// NOT SUPPORTED
		return null;
	}

	public void setAttribute(String key, String value) {
		// NOT SUPPORTED FOR NOW

	}

	public String getAttribute(String key) {
		// NOT SUPPORTED FOR NOW
		return null;
	}

	public int getExitValue() throws DebugException {
		if (isTerminated()) {
			return exitValue;
		}
		throw new DebugException(
				new Status(
						IStatus.ERROR,
						DebugPlugin.getUniqueIdentifier(),
						DebugException.TARGET_REQUEST_FAILED,
						DebugCoreMessages.RuntimeProcess_Exit_value_not_available_until_process_terminates__1,
						null));
	}

	public void eventFired(EventObject event) {
		if (event instanceof ProcessStateChangeEvent) {
			ProcessStateChangeEvent pscEvent = (ProcessStateChangeEvent) event;
			if (pscEvent.getEventId().equals(
					ProcessStateChangeEvent.EVENT_PROCESS_CREATED)) {
				if ((pscEvent.getSource() instanceof ProcessContext)) {
					context = (ProcessContext) pscEvent.getSource();
				}
			} else if (pscEvent.getEventId().equals(
					ProcessStateChangeEvent.EVENT_PROCESS_TERMINATED)) {
				if ((pscEvent.getSource() instanceof ProcessContext)) {
					if (((ProcessContext) pscEvent.getSource()).getID().equals(
							context.getID())) {
						exitValue = pscEvent.getExitCode();
						terminated = true;
						fireTerminateEvent();
					}
				}
			}
		}

	}

	/**
	 * Fires a creation event.
	 */
	protected void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/**
	 * Fires the given debug event.
	 * 
	 * @param event
	 *            debug event to fire
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}

	/**
	 * Fires a terminate event.
	 */
	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/**
	 * Fires a change event.
	 */
	protected void fireChangeEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
	}

}
