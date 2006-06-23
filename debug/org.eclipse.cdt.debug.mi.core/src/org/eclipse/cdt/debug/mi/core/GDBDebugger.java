/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;

public class GDBDebugger implements ICDebugger {

	protected void initializeLibraries(ILaunchConfiguration config, Session session) throws CDIException {
		try {
			SharedLibraryManager mgr = session.getSharedLibraryManager();
			boolean autolib = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, IMILaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT);
			boolean stopOnSolibEvents = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, IMILaunchConfigurationConstants.DEBUGGER_STOP_ON_SOLIB_EVENTS_DEFAULT);
			List p = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.EMPTY_LIST);
			ICDITarget[] dtargets = session.getTargets();
			for (int i = 0; i < dtargets.length; ++i) {
				Target target = (Target)dtargets[i];
				try {
					mgr.setAutoLoadSymbols(target, autolib);
					mgr.setStopOnSolibEvents(target, stopOnSolibEvents);
					// The idea is that if the user set autolib, by default
					// we provide with the capability of deferred breakpoints
					// And we set setStopOnSolib events for them(but they should not see those things.
					//
					// If the user explicitly set stopOnSolibEvents well it probably
					// means that they wanted to see those events so do no do deferred breakpoints.
					if (autolib && !stopOnSolibEvents) {
						mgr.setStopOnSolibEvents(target, true);
						mgr.setDeferredBreakpoint(target, true);
					}
				} catch (CDIException e) {
					// Ignore this error
					// it seems to be a real problem on many gdb platform
				}
				if (p.size() > 0) {
					String[] oldPaths = mgr.getSharedLibraryPaths(target);
					String[] paths = new String[oldPaths.length + p.size()];
					System.arraycopy(p.toArray(new String[p.size()]), 0, paths, 0, p.size());
					System.arraycopy(oldPaths, 0, paths, p.size(), oldPaths.length);
					mgr.setSharedLibraryPaths(target, paths);
				}
			}
		} catch (CoreException e) {
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_initializing_shared_lib_options") + e.getMessage()); //$NON-NLS-1$
		}
	}

	public ICDISession createLaunchSession(ILaunchConfiguration config, IFile exe) throws CDIException {
		Session session = null;
		boolean failed = false;
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb"); //$NON-NLS-1$
			String miVersion = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, "mi"); //$NON-NLS-1$
			File cwd = exe.getProject().getLocation().toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
			session = MIPlugin.getDefault().createCSession(gdb, miVersion, exe.getLocation().toFile(), cwd, gdbinit, null);
			initializeLibraries(config, session);
			return session;
		} catch (IOException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + e.getMessage()); //$NON-NLS-1$
		} catch (MIException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + e.getMessage()); //$NON-NLS-1$
		} catch (CoreException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + e.getMessage()); //$NON-NLS-1$
		} finally {
			if (failed) {
				if (session != null) {
					try {
						session.terminate();
					} catch (Exception ex) {
						// ignore the exception here.
					}
				}
			}
		}
	}

	public ICDISession createAttachSession(ILaunchConfiguration config, IFile exe, int pid) throws CDIException {
		Session session = null;
		boolean failed = false;
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb"); //$NON-NLS-1$
			String miVersion = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, "mi"); //$NON-NLS-1$
			File cwd = exe.getProject().getLocation().toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
			session = MIPlugin.getDefault().createCSession(gdb, miVersion, exe.getLocation().toFile(), pid, null, cwd, gdbinit, null);
			initializeLibraries(config, session);
			return session;
		} catch (IOException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + e.getMessage()); //$NON-NLS-1$
		} catch (MIException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + e.getMessage()); //$NON-NLS-1$
		} catch (CoreException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + e.getMessage()); //$NON-NLS-1$
		} finally {
			if (failed) {
				if (session != null) {
					try {
						session.terminate();
					} catch (Exception ex) {
						// ignore the exception here.
					}
				}
			}
		}
	}

	public ICDISession createCoreSession(ILaunchConfiguration config, IFile exe, IPath corefile) throws CDIException {
		Session session = null;
		boolean failed = false;
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb"); //$NON-NLS-1$
			String miVersion = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, "mi"); //$NON-NLS-1$
			File cwd = exe.getProject().getLocation().toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
			session = MIPlugin.getDefault().createCSession(gdb, miVersion, exe.getLocation().toFile(), corefile.toFile(), cwd, gdbinit, null);
			initializeLibraries(config, session);
			return session;
		} catch (IOException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + e.getMessage()); //$NON-NLS-1$
		} catch (MIException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + e.getMessage()); //$NON-NLS-1$
		} catch (CoreException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + e.getMessage()); //$NON-NLS-1$
		} finally {
			if (failed) {
				if (session != null) {
					try {
						session.terminate();
					} catch (Exception ex) {
						// ignore the exception here.
					}
				}
			}
		}
	}

}
