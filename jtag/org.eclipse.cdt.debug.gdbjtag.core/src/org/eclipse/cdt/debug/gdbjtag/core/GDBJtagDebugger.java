/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.AbstractGDBCDIDebugger;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetNewConsole;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @author Doug Schaefer
 *
 */
public class GDBJtagDebugger extends AbstractGDBCDIDebugger {

	public ICDISession createSession(ILaunch launch, File executable,
			IProgressMonitor monitor) throws CoreException {
		return super.createSession(launch, executable, monitor);
	}

	public ICDISession createDebuggerSession(ILaunch launch, IBinaryObject exe,
			IProgressMonitor monitor) throws CoreException {
		return super.createDebuggerSession(launch, exe, monitor);
	}
	
	protected CommandFactory getCommandFactory(ILaunchConfiguration config)
			throws CoreException {
		String miVersion = MIPlugin.getMIVersion(config);
		return new GDBJtagCommandFactory(miVersion);
	}
	
	protected void doStartSession(ILaunch launch, Session session, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		ICDITarget[] targets = session.getTargets();
		if (targets.length == 0 || !(targets[0] instanceof Target))
			return ; // TODO should raise an exception
		MISession miSession = ((Target)targets[0]).getMISession();
		getMISession( session );
		CommandFactory factory = miSession.getCommandFactory();
		try {
			MIGDBSetNewConsole newConsole = factory.createMIGDBSetNewConsole();
			miSession.postCommand( newConsole );
			MIInfo info = newConsole.getMIInfo();
			if ( info == null ) {
				throw new MIException( MIPlugin.getResourceString( "src.common.No_answer" ) ); //$NON-NLS-1$
			}
		}
		catch( MIException e ) {
			// We ignore this exception, for example
			// on GNU/Linux the new-console is an error.
		}
		
		// TODO execute init script

		// TODO execute load
	}
	
	protected MISession getMISession(Session session) {
		ICDITarget[] targets = session.getTargets();
		if (targets.length == 0 || !(targets[0] instanceof Target))
			return null;
		return ((Target)targets[0]).getMISession();
	}

	public void doRunSession(ILaunch launch, ICDISession session, IProgressMonitor monitor) {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		ICDITarget[] targets = session.getTargets();
		if ( targets.length == 0 || !(targets[0] instanceof Target) )
			return;
		MISession miSession = ((Target)targets[0]).getMISession();
		CommandFactory factory = miSession.getCommandFactory();

		// TODO execute run script
	}
	
}
