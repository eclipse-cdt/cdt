/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core; 

import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.command.factories.win32.CygwinCommandFactory;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Cygwin debugger extension point.
 */
public class CygwinGDBCDIDebugger2 extends GDBCDIDebugger2 {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.GDBCDIDebugger2#getCommandFactory(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	protected CommandFactory getCommandFactory( ILaunchConfiguration config ) throws CoreException {
		return new CygwinCommandFactory( getMIVersion( config ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.GDBCDIDebugger2#doStartSession(org.eclipse.debug.core.ILaunch, org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.cdt.debug.mi.core.cdi.Session, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doStartSession( ILaunch launch, ILaunchConfiguration config, Session session, IProgressMonitor monitor ) throws CoreException {
		// For windows we need to start the inferior in a new console window
		// to separate the Inferior std{in,out,err} from gdb std{in,out,err}
		MISession miSession = getMISession( session );
		try {
			CommandFactory factory = miSession.getCommandFactory();
			MIGDBSet set = factory.createMIGDBSet( new String[]{ "new-console" } ); //$NON-NLS-1$
			miSession.postCommand( set );
			MIInfo info = set.getMIInfo();
			if ( info == null ) {
				throw new MIException( MIPlugin.getResourceString( "src.common.No_answer" ) ); //$NON-NLS-1$
			}
		}
		catch( MIException e ) {
			// We ignore this exception, for example
			// on GNU/Linux the new-console is an error.
		}		
		super.doStartSession( launch, config, session, monitor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.GDBCDIDebugger2#initializeLibraries(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.cdt.debug.mi.core.cdi.Session)
	 */
	protected void initializeLibraries( ILaunchConfiguration config, Session session ) throws CoreException {
		// the "search-solib-path" and "stop-on-solib-events" options are not supported in CygWin
	}
}
