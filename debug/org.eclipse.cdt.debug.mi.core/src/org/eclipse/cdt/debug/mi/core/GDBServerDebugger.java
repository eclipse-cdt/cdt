/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Monta Vista - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.command.MITargetSelect;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.ILaunchConfiguration;

public class GDBServerDebugger implements ICDebugger {

	void initializeLibraries(ILaunchConfiguration config, Session session) throws CDIException {
		try {
			ICDISharedLibraryManager mgr = session.getSharedLibraryManager();
			if (mgr instanceof SharedLibraryManager) {
				boolean autolib = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, IMILaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT);
				try {
					((SharedLibraryManager)mgr).setAutoLoadSymbols(autolib);
				} catch (CDIException e) {
					// ignore this one, cause problems for many gdb.
				}
			}			
			List p = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, new ArrayList(1));
			if (p.size() > 0) {
				String[] paths = (String[])p.toArray(new String[0]);
				mgr.setSharedLibraryPaths(paths);
			}
		} catch (CoreException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}
	}

	public ICDISession createLaunchSession(ILaunchConfiguration config, IFile exe) throws CDIException {
		Session session = null;
		boolean failed = false;
		try {
			String gdb = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
			File cwd = exe.getProject().getLocation().toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit");
			if (config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, false)) {
				String remote = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, "invalid");
				remote += ":";
				remote += config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT, "invalid");
				String[] args = new String[] {"remote", remote};
				session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getLocation().toFile(), 0, args, cwd, gdbinit);
			} else {
				MIPlugin plugin = MIPlugin.getDefault();
				Preferences prefs = plugin.getPluginPreferences();
				int launchTimeout = prefs.getInt(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT);

				String remote = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV, "invalid");
				String remoteBaud = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV_SPEED, "invalid");
				session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getLocation().toFile(), -1, null, cwd, gdbinit);
				MISession miSession = session.getMISession();
				CommandFactory factory = miSession.getCommandFactory();
				MIGDBSet setRemoteBaud = factory.createMIGDBSet(new String[]{"remotebaud", remoteBaud});
				// Set serial line parameters
				miSession.postCommand(setRemoteBaud, launchTimeout);
				MIInfo info = setRemoteBaud.getMIInfo();
				if (info == null) {
					throw new MIException ("Can not set Baud");
				}
				MITargetSelect select = factory.createMITargetSelect(new String[] {"remote", remote});
				miSession.postCommand(select, launchTimeout);
				select.getMIInfo();
				if (info == null) {
					throw new MIException ("No answer");
				}
			}
			initializeLibraries(config, session);
			return session;
		} catch (IOException e) {
			failed = true;
			throw new CDIException("Error initializing: " + e.getMessage());
		} catch (MIException e) {
			failed = true;
			throw new CDIException("Error initializing: " + e.getMessage());
		} catch (CoreException e) {
			failed = true;
			throw new CDIException("Error initializing: " + e.getMessage());
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
		throw new CDIException("GDBServer does not support attaching");
	}

	public ICDISession createCoreSession(ILaunchConfiguration config, IFile exe, IPath corefile) throws CDIException {
		throw new CDIException("GDBServer does not support core files");
	}
}
