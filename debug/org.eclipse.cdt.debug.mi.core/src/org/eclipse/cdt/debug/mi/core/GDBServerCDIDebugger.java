/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.command.MITargetSelect;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Implementing cdebugger extension point
 */
public class GDBServerCDIDebugger extends GDBCDIDebugger {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.GDBCDIDebugger#createLaunchSession(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Session createLaunchSession(ILaunchConfiguration config, IBinaryObject exe, IProgressMonitor monitor)
			throws CoreException {
		Session session = null;
		boolean failed = false;
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb"); //$NON-NLS-1$
			String miVersion = getMIVersion(config);
			File cwd = getProjectPath(config).toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
			if (config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, false)) {
				String remote = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_HOST, "invalid"); //$NON-NLS-1$
				remote += ":"; //$NON-NLS-1$
				remote += config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_PORT, "invalid"); //$NON-NLS-1$
				String[] args = new String[] {"remote", remote}; //$NON-NLS-1$
				session = MIPlugin.getDefault().createCSession(gdb, miVersion, exe.getPath().toFile(), 0, args, cwd, gdbinit, monitor);
			} else {
				MIPlugin plugin = MIPlugin.getDefault();
				Preferences prefs = plugin.getPluginPreferences();
				int launchTimeout = prefs.getInt(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT);

				String remote = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV, "invalid"); //$NON-NLS-1$
				String remoteBaud = config.getAttribute(IGDBServerMILaunchConfigurationConstants.ATTR_DEV_SPEED, "invalid"); //$NON-NLS-1$
				session = MIPlugin.getDefault().createCSession(gdb, miVersion, exe.getPath().toFile(), -1, null, cwd, gdbinit, monitor);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.GDBCDIDebugger#createAttachSession(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Session createAttachSession(ILaunchConfiguration config, IBinaryObject exe, IProgressMonitor monitor)
			throws CoreException {
		String msg = MIPlugin.getResourceString("src.GDBServerDebugger.GDBServer_attaching_unsupported"); //$NON-NLS-1$
		throw newCoreException(msg, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.GDBCDIDebugger#createCoreSession(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Session createCoreSession(ILaunchConfiguration config, IBinaryObject exe, IProgressMonitor monitor)
			throws CoreException {
		String msg = MIPlugin.getResourceString("src.GDBServerDebugger.GDBServer_corefiles_unsupported"); //$NON-NLS-1$
		throw newCoreException(msg, null);
	}

}
