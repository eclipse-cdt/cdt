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

package org.eclipse.cdt.debug.mi.core;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;

/**
 * Implementing cdebugger extension point
 */
public class GDBCDIDebugger implements ICDIDebugger {

	ILaunch fLaunch;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICDIDebugger#createDebuggerSession(org.eclipse.debug.core.ILaunch, org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICDISession createDebuggerSession(ILaunch launch, IBinaryExecutable exe, IProgressMonitor monitor)
			throws CoreException {
		fLaunch = launch;
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		Session dsession = null;
		String debugMode = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
			dsession = createLaunchSession(config, exe, monitor);
		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
			dsession = createAttachSession(config, exe, monitor);
		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
			dsession = createCoreSession(config, exe, monitor);
		}
		if (dsession != null) {
			ICDITarget[] dtargets = dsession.getTargets();
			for (int i = 0; i < dtargets.length; i++) {
				Process debugger = dsession.getSessionProcess(dtargets[i]);
				if (debugger != null) {
					IProcess debuggerProcess = DebugPlugin.newProcess(launch, debugger, renderDebuggerProcessLabel());
					launch.addProcess(debuggerProcess);
				}
			}
		}

		return dsession;
	}

	public Session createLaunchSession(ILaunchConfiguration config, IBinaryExecutable exe, IProgressMonitor monitor) throws CoreException {
		Session session = null;
		boolean failed = false;
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb"); //$NON-NLS-1$
			File cwd = getProjectPath(config).toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit"); //$NON-NLS-1$
			session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getPath().toFile(), cwd, gdbinit, monitor);
			initializeLibraries(config, session);
			return session;
		} catch (Exception e) {
			// Catch all wrap them up and rethrow
			failed = true;
			if (e instanceof CoreException) {
				throw (CoreException)e;
			}
			throw newCoreException(e);
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

	public Session createAttachSession(ILaunchConfiguration config, IBinaryExecutable exe, IProgressMonitor monitor) throws CoreException {
		Session session = null;
		boolean failed = false;
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb"); //$NON-NLS-1$
			int pid = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1);
			File cwd = getProjectPath(config).toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit"); //$NON-NLS-1$
			session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getPath().toFile(), pid, null, cwd, gdbinit, monitor);
			initializeLibraries(config, session);
			return session;
		} catch (Exception e) {
			// Catch all wrap them up and rethrow
			failed = true;
			if (e instanceof CoreException) {
				throw (CoreException)e;
			}
			throw newCoreException(e);
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

	public Session createCoreSession(ILaunchConfiguration config, IBinaryExecutable exe, IProgressMonitor monitor) throws CoreException {
		Session session = null;
		boolean failed = false;
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb"); //$NON-NLS-1$
			File cwd = getProjectPath(config).toFile();
			IPath coreFile = new Path(config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, (String)null));
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit"); //$NON-NLS-1$
			session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getPath().toFile(), coreFile.toFile(), cwd, gdbinit, monitor);
			initializeLibraries(config, session);
			return session;
		} catch (Exception e) {
			// Catch all wrap them up and rethrow
			failed = true;
			if (e instanceof CoreException) {
				throw (CoreException)e;
			}
			throw newCoreException(e);
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

	protected void initializeLibraries(ILaunchConfiguration config, Session session) throws CoreException {
		try {
			SharedLibraryManager sharedMgr = session.getSharedLibraryManager();
			boolean autolib = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, IMILaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT);
			boolean stopOnSolibEvents = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, IMILaunchConfigurationConstants.DEBUGGER_STOP_ON_SOLIB_EVENTS_DEFAULT);
			List p = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.EMPTY_LIST);
			ICDITarget[] dtargets = session.getTargets();
			for (int i = 0; i < dtargets.length; ++i) {
				Target target = (Target)dtargets[i];
				try {
					sharedMgr.setAutoLoadSymbols(target, autolib);
					sharedMgr.setStopOnSolibEvents(target, stopOnSolibEvents);
					// The idea is that if the user set autolib, by default
					// we provide with the capability of deferred breakpoints
					// And we set setStopOnSolib events for them(but they should not see those things.
					//
					// If the user explicitly set stopOnSolibEvents well it probably
					// means that they wanted to see those events so do no do deferred breakpoints.
					if (autolib && !stopOnSolibEvents) {
						sharedMgr.setDeferredBreakpoint(true);
						sharedMgr.setStopOnSolibEvents(target, true);
					}
				} catch (CDIException e) {
					// Ignore this error
					// it seems to be a real problem on many gdb platform
				}
				if (p.size() > 0) {
					String[] oldPaths = sharedMgr.getSharedLibraryPaths(target);
					String[] paths = new String[oldPaths.length + p.size()];
					System.arraycopy(p.toArray(new String[p.size()]), 0, paths, 0, p.size());
					System.arraycopy(oldPaths, 0, paths, p.size(), oldPaths.length);
					sharedMgr.setSharedLibraryPaths(target, paths);
				}
			}
		} catch (CDIException e) {
			throw newCoreException(MIPlugin.getResourceString("src.GDBDebugger.Error_initializing_shared_lib_options") + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	public static IPath getProjectPath(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				IPath p = project.getLocation();
				if (p != null) {
					return p;
				}
			}
		}
		return Path.EMPTY;
	}

	public static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}

	protected ILaunch getLauch() {
		return fLaunch;
	}

	protected String renderDebuggerProcessLabel() {
		String format = "{0} ({1})"; //$NON-NLS-1$
		String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		String message = MIPlugin.getResourceString("src.GDBDebugger.Debugger_process");
		return MessageFormat.format(format, new String[]{message, timestamp}); //$NON-NLS-1$
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	protected CoreException newCoreException(Throwable exception) {
		String message = MIPlugin.getResourceString("src.GDBDebugger.Error_creating_session") + exception.getMessage();//$NON-NLS-1$
		int code =  ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR;
		String ID = MIPlugin.getUniqueIdentifier();
		MultiStatus status = new MultiStatus(ID, code, message, exception);
		status.add(new Status(IStatus.ERROR, ID, code, exception == null ? new String() : exception.getLocalizedMessage(), exception));
		return new CoreException(status);
	}

	protected CoreException newCoreException(String message, Throwable exception) {
		int code =  ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR;
		String ID = MIPlugin.getUniqueIdentifier();
		MultiStatus status = new MultiStatus(ID, code, message, exception);
		status.add(new Status(IStatus.ERROR, ID, code, exception == null ? new String() : exception.getLocalizedMessage(), exception));
		return new CoreException(status);
	}

}
