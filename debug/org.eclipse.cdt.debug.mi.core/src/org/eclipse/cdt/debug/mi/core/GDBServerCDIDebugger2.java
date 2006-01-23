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

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.command.MITargetSelect;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
 
/**
 * Implementing the cdebugger extension point for gdbserver.
 */
public class GDBServerCDIDebugger2 extends GDBCDIDebugger2 {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.GDBCDIDebugger2#doStartSession(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.cdt.debug.mi.core.cdi.Session, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doStartSession( ILaunchConfiguration config, Session session, IProgressMonitor monitor ) throws CoreException {
		initializeLibraries( config, session );
		if ( monitor.isCanceled() ) {
			throw new OperationCanceledException();
		}
		String debugMode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN.equals( debugMode ) )
			startGDBServerSession( config, session, monitor );
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH.equals( debugMode ) ) {
			String msg = MIPlugin.getResourceString( "src.GDBServerDebugger.GDBServer_attaching_unsupported" ); //$NON-NLS-1$
			throw newCoreException( msg, null );
		}
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE.equals( debugMode ) ) {
			String msg = MIPlugin.getResourceString( "src.GDBServerDebugger.GDBServer_corefiles_unsupported" ); //$NON-NLS-1$
			throw newCoreException( msg, null );
		}
	}

	protected void startGDBServerSession( ILaunchConfiguration config, Session session, IProgressMonitor monitor ) throws CoreException {
		if ( monitor.isCanceled() ) {
			throw new OperationCanceledException();
		}
		ICDITarget[] targets = session.getTargets();
		int launchTimeout = MIPlugin.getLaunchTimeout();
		boolean tcpConnection = config.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP, false );
		// Set serial line parameters
		if ( !tcpConnection ) {
			String remoteBaud = config.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_DEV_SPEED, "invalid" ); //$NON-NLS-1$
			for( int i = 0; i < targets.length; ++i ) {
				if ( monitor.isCanceled() ) {
					throw new OperationCanceledException();
				}
				Target target = (Target)targets[i];
				MISession miSession = target.getMISession();
				CommandFactory factory = miSession.getCommandFactory();
				MIGDBSet setRemoteBaud = factory.createMIGDBSet( new String[]{ "remotebaud", remoteBaud } ); //$NON-NLS-1$
				// Set serial line parameters
				MIInfo info = null;
				MIException ex = null;
				try {
					// shouldn't we use the command timeout instead?
					miSession.postCommand( setRemoteBaud, launchTimeout );
					info = setRemoteBaud.getMIInfo();
				}
				catch( MIException e ) {
					ex = e;
				}
				if ( info == null ) {
					throw newCoreException( MIPlugin.getResourceString( "src.GDBServerDebugger.Can_not_set_Baud" ), ex ); //$NON-NLS-1$
				}
			}		
		}
		for( int i = 0; i < targets.length; ++i ) {
			if ( monitor.isCanceled() ) {
				throw new OperationCanceledException();
			}
			Target target = (Target)targets[i];
			MISession miSession = target.getMISession();
			CommandFactory factory = miSession.getCommandFactory();
			String[] targetParams = getTargetParams( config, tcpConnection );
			MITargetSelect select = factory.createMITargetSelect( targetParams );
			MIInfo info = null;
			MIException ex = null;
			try {
				miSession.postCommand( select, launchTimeout );
				info = select.getMIInfo();
			}
			catch( MIException e ) {
				ex = e;
			}
			if ( info == null ) {
				throw newCoreException( MIPlugin.getResourceString( "src.GDBServerCDIDebugger.target_selection_failed" ), ex ); //$NON-NLS-1$
			}
		}
	}

	protected String[] getTargetParams( ILaunchConfiguration config, boolean tcpConnection ) throws CoreException {
		String remote = null;
		if ( tcpConnection ) {
			remote = config.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_HOST, "invalid" ); //$NON-NLS-1$
			remote += ":"; //$NON-NLS-1$
			remote += config.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_PORT, "invalid" ); //$NON-NLS-1$
		}
		else {
			remote = config.getAttribute( IGDBServerMILaunchConfigurationConstants.ATTR_DEV, "invalid" ); //$NON-NLS-1$		
		}
		return new String[]{ "remote", remote }; //$NON-NLS-1$
	}
}
