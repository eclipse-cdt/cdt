/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
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
			throw new CDIException(MIPlugin.getResourceString("src.GDBServerDebugger.Error_initializing") + e.getMessage()); //$NON-NLS-1$
		}
	}

	public ICDISession createLaunchSession(ILaunchConfiguration config, IFile exe) throws CDIException {
		Session session = null;
		boolean failed = false;
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb"); //$NON-NLS-1$
			File cwd = exe.getProject().getLocation().toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit"); //$NON-NLS-1$
			if (config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, false)) {
				String remote = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, "invalid"); //$NON-NLS-1$
				remote += ":"; //$NON-NLS-1$
				remote += config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT, "invalid"); //$NON-NLS-1$
				String[] args = new String[] {"remote", remote}; //$NON-NLS-1$
				session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getLocation().toFile(), 0, args, cwd, gdbinit);
			} else {
				MIPlugin plugin = MIPlugin.getDefault();
				Preferences prefs = plugin.getPluginPreferences();
				int launchTimeout = prefs.getInt(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT);

				String remote = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV, "invalid"); //$NON-NLS-1$
				String remoteBaud = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV_SPEED, "invalid"); //$NON-NLS-1$
				session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getLocation().toFile(), -1, null, cwd, gdbinit);
				ICDITarget[] targets = session.getTargets();
				for (int i = 0; i < targets.length; ++i) {
					Target target = (Target)targets[i];
					MISession miSession = target.getMISession();
					CommandFactory factory = miSession.getCommandFactory();
					MIGDBSet setRemoteBaud = factory.createMIGDBSet(new String[]{"remotebaud", remoteBaud}); //$NON-NLS-1$
					// Set serial line parameters
					miSession.postCommand(setRemoteBaud, launchTimeout);
					MIInfo info = setRemoteBaud.getMIInfo();
					if (info == null) {
						throw new MIException (MIPlugin.getResourceString("src.GDBServerDebugger.Can_not_set_Baud")); //$NON-NLS-1$
					}
					MITargetSelect select = factory.createMITargetSelect(new String[] {"remote", remote}); //$NON-NLS-1$
					miSession.postCommand(select, launchTimeout);
					select.getMIInfo();
					if (info == null) {
						throw new MIException (MIPlugin.getResourceString("src.common.No_answer")); //$NON-NLS-1$
					}
				}
			}
			initializeLibraries(config, session);
			return session;
		} catch (IOException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBServerDebugger.Error_initializing") + e.getMessage()); //$NON-NLS-1$
		} catch (MIException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBServerDebugger.Error_initializing") + e.getMessage()); //$NON-NLS-1$
		} catch (CoreException e) {
			failed = true;
			throw new CDIException(MIPlugin.getResourceString("src.GDBServerDebugger.Error_initializing") + e.getMessage()); //$NON-NLS-1$
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
		throw new CDIException(MIPlugin.getResourceString("src.GDBServerDebugger.GDBServer_attaching_unsupported")); //$NON-NLS-1$
	}

	public ICDISession createCoreSession(ILaunchConfiguration config, IFile exe, IPath corefile) throws CDIException {
		throw new CDIException(MIPlugin.getResourceString("src.GDBServerDebugger.GDBServer_corefiles_unsupported")); //$NON-NLS-1$
	}
}
