/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Cygwin GDB Debugger overrides the GDB Debugger to apply the Cygwin
 * Command Factory to the MI Session.
 */
public class CygwinGDBDebugger extends GDBDebugger {

	protected void initializeLibraries(ILaunchConfiguration config, Session session) throws CDIException {
		// the "search-solib-path" and "stop-on-solib-events" options are not supported in CygWin
	}

	public ICDISession createLaunchSession(ILaunchConfiguration config, IFile exe) throws CDIException {
		Session session = null;
		boolean failed = false;
		try {
			session = (Session) super.createLaunchSession(config, exe);
			ICDITarget[] targets = session.getTargets();
			for (int i = 0; i < targets.length; ++i) {
				Target target = (Target)targets[i];
				MISession miSession = target.getMISession();
				String miVersion = miSession.getCommandFactory().getMIVersion();
				miSession.setCommandFactory(new CygwinCommandFactory(miVersion));
				// For windows we need to start the inferior in a new console window
				// to separate the Inferior std{in,out,err} from gdb std{in,out,err}
				try {
					CommandFactory factory = miSession.getCommandFactory();
					MIGDBSet set = factory.createMIGDBSet(new String[] { "new-console" }); //$NON-NLS-1$
					miSession.postCommand(set);
					MIInfo info = set.getMIInfo();
					if (info == null) {
						throw new MIException(MIPlugin.getResourceString("src.common.No_answer")); //$NON-NLS-1$
					}
				} catch (MIException e) {
					// We ignore this exception, for example
					// on GNU/Linux the new-console is an error.
				}
			}
			return session;
		} catch (CDIException e) {
			failed = true;
			throw e;
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
			session = (Session) super.createAttachSession(config, exe, pid);
			ICDITarget[] targets = session.getTargets();
			for (int i = 0; i < targets.length; ++i) {
				Target target = (Target)targets[i];
				MISession miSession = target.getMISession();
				String miVersion = miSession.getCommandFactory().getMIVersion();
				miSession.setCommandFactory(new CygwinCommandFactory(miVersion));
			}
			initializeLibraries(config, session);
			return session;
		} catch (CDIException e) {
			failed = true;
			throw e;
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
			session = (Session) super.createCoreSession(config, exe, corefile);
			ICDITarget[] targets = session.getTargets();
			for (int i = 0; i < targets.length; ++i) {
				Target target = (Target)targets[i];
				MISession miSession = target.getMISession();
				String miVersion = miSession.getCommandFactory().getMIVersion();
				miSession.setCommandFactory(new CygwinCommandFactory(miVersion));
			}
			initializeLibraries(config, session);
			return session;
		} catch (CDIException e) {
			failed = true;
			throw e;
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
