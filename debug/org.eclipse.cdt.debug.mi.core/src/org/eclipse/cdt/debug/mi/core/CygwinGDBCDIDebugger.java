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

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Cygwin debugger extension point.
 */
public class CygwinGDBCDIDebugger extends GDBCDIDebugger {
	static final CygwinCommandFactory commandFactory = new CygwinCommandFactory();

	protected void initializeLibraries(ILaunchConfiguration config, Session session) throws CoreException {
		// the "search-solib-path" and "stop-on-solib-events" options are not supported in CygWin
	}

	public Session createLaunchSession(ILaunchConfiguration config, IBinaryObject exe, IProgressMonitor monitor) throws CoreException {
		Session session = super.createLaunchSession(config, exe, monitor);
		ICDITarget[] targets = session.getTargets();
		for (int i = 0; i < targets.length; ++i) {
			Target target = (Target)targets[i];
			MISession miSession = target.getMISession();
			miSession.setCommandFactory(commandFactory);
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
	}

	public Session createAttachSession(ILaunchConfiguration config, IBinaryObject exe, IProgressMonitor monitor) throws CoreException {
		Session session = super.createAttachSession(config, exe, monitor);
		ICDITarget[] targets = session.getTargets();
		for (int i = 0; i < targets.length; ++i) {
			Target target = (Target)targets[i];
			target.getMISession().setCommandFactory(commandFactory);
		}
		initializeLibraries(config, session);
		return session;
	}

	public Session createCoreSession(ILaunchConfiguration config, IBinaryObject exe, IProgressMonitor monitor) throws CoreException {
		Session session = super.createCoreSession(config, exe, monitor);
		ICDITarget[] targets = session.getTargets();
		for (int i = 0; i < targets.length; ++i) {
			Target target = (Target)targets[i];
			target.getMISession().setCommandFactory(commandFactory);
		}
		initializeLibraries(config, session);
		return session;
	}

}
